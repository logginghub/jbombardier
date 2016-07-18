/*
 * Copyright (c) 2009-2015 Vertex Labs Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jbombardier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jbombardier.agent.Agent2;
import com.jbombardier.common.*;
import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.*;
import com.jbombardier.console.charts.FrequencyChart;
import com.jbombardier.console.configuration.*;
import com.jbombardier.console.model.*;
import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.console.statisticcapture.JMXStatisticCapture;
import com.jbombardier.console.statisticcapture.LoggingHubStatisticCapture;
import com.jbombardier.reports.ReportGenerator;
import com.jbombardier.result.JBombardierResultsController;
import com.jbombardier.result.JBombardierRunResult;
import com.jbombardier.xml.CsvProperty;
import com.logginghub.messaging2.ReflectionDispatchMessageListener;
import com.logginghub.messaging2.api.ConnectionListener;
import com.logginghub.messaging2.kryo.KryoClient;
import com.logginghub.messaging2.kryo.ResponseHandler;
import com.logginghub.utils.*;
import com.logginghub.utils.MemorySnapshot.LowMemoryNotificationHandler;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservablePropertyListener;
import com.logginghub.utils.remote.ClasspathResolver;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.InvalidReferenceEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.util.introspection.Info;

import java.io.File;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

public class JBombardierController {

    private static final Logger logger = Logger.getLoggerFor(JBombardierController.class);
    private TimeProvider timeProvider = new SystemTimeProvider();
    // TODO : figure out a nicer way of doing this!!
    private static Stream<String> eventStream = new Stream<String>();
    // private InteractiveConfiguration configuration;
    private final JBombardierModel model;
    private Map<String, String> properties = new ConcurrentHashMap<String, String>();
    private Map<String, CsvPropertiesProvider> csvPropertyProviders = new HashMap<String, CsvPropertiesProvider>();
    private Map<ReflectionDispatchMessageListener, KryoClient> dispatchingListeners = new
            HashMap<ReflectionDispatchMessageListener, KryoClient>();
    private Map<String, DataSource> dataByName = new HashMap<String, DataSource>();
    private RawResultsController rawResultsController;
    private CapturedStatisticsHelper capturedStatisticsHelper;
    private ExecutorService pool = Executors.newCachedThreadPool(new NamedThreadFactory
                                                                         ("JBombardierController-worker-"));
    //    private int autostartAgents;
    //private File reportsPath = new File("reports");
    private StatBundle statBundle = new StatBundle();
    //    private boolean sendCloseMessageOnWindowClose = true;
    private Timer agentPingTimer;
    private JBombardierConfiguration configuration;
    private IntegerStat newConnectionsStat;
    private IntegerStat disconnectionsStat;
    private IntegerStat connectionsStat;
    private IntegerStat agentStatusUpdatesStat;
    private IntegerStat classloaderRequestsStat;
    private IntegerStat expectedAgentsStat;
    private WorkerThread memoryMonitorWorkerThread;
    private ClasspathResolver resolver = new ClasspathResolver();
    //    private boolean outputControllerStats = !EnvironmentProperties.getBoolean(
    //            "jbombardierConsoleController.disableStats");

    private JBombardierRunResult newResultModel;
    private JBombardierResultsController newResultsController;

    public void establishInitialState() {
        List<StateEstablisherConfiguration> stateEstablishers = configuration.getStateEstablishers();
        runStateEstablishers(stateEstablishers);
    }

    private void runStateEstablishers(List<StateEstablisherConfiguration> stateEstablishers) {
        for (StateEstablisherConfiguration stateEstablisherConfiguration : stateEstablishers) {
            StateEstablisher stateEstablisher = ReflectionUtils.instantiate(stateEstablisherConfiguration.getClassName());
            try {
                stateEstablisher.establishState(stateEstablisherConfiguration.getConfiguration());
            } catch (Exception e) {
                logger.warn(e, "Failed to establish initial state");
                endTestAbnormally();
                break;
            }
        }
    }

    public void setThreadsInTest(String testName, int threads) {

        ObservableList<TestModel> testModels = model.getCurrentPhase().get().getTestModels();
        for (TestModel testModel : testModels) {
            if(testModel.getName().equals(testName)) {
                updateTestVariable(testName, TestField.targetThreads, threads);
                break;
            }
        }

    }


    public enum State {
        Configured, AgentConnectionsRunning, TestRunning, Stopped, Completed
    }

    private State state;
    private List<Agent2> embeddedAgents = new ArrayList<Agent2>();

    public JBombardierController(final JBombardierModel model, JBombardierConfiguration configuration) {
        this.model = model;
        this.configuration = configuration;

        model.setNoResultsTimeout((int) (configuration.getNoResultsTimeout() / 1000));
        model.getTestName().set(configuration.getTestName());
        model.setFailedTransactionCountFailureThreshold(configuration.getFailedTransactionCountFailureThreshold());
        model.setMaximumConsoleEntries(configuration.getMaximumConsoleEntries());

        newResultModel = new JBombardierRunResult(configuration);
        newResultsController = new JBombardierResultsController(newResultModel);

        capturedStatisticsHelper = new CapturedStatisticsHelper(model);

        rawResultsController = new RawResultsController(new File(configuration.getReportsFolder()));
        rawResultsController.setMaximumResultsPerKey(configuration.getMaximumResultToStore());

        initialiseStats();

        // Attach a listener to pickup changes to the transaction rate modifier changer
        model.getTransactionRateModifier().addListenerAndNotifyCurrent(new ObservablePropertyListener<Double>() {
            @Override
            public void onPropertyChanged(Double aDouble, Double t1) {
                updateTransactionRateModifier(model.getTransactionRateModifier().get());
            }
        });

        initialiseStatisticsCapture(configuration, model);
        initialiseModelFromConfiguration(configuration, model);
        initialiseProperties(configuration, model);

        state = State.Configured;

    }

    public State getState() {
        return state;
    }

    public void startAgentConnections() {

        memoryMonitorWorkerThread = MemorySnapshot.runMonitorToLogging(90, new LowMemoryNotificationHandler() {
            @Override
            public void onLowMemory(float percentage, int consecutive) {
                if (consecutive >= 2) {
                    handleLowMemory();
                }
            }
        });

        attachMessagingToAgentModels(configuration, model);

        state = State.AgentConnectionsRunning;
    }

    public synchronized void publishTestInstructions() {

        if (model.isTestRunning()) {
            throw new IllegalStateException("Test has already been started");
        } else {
            model.getTestRunning().set(true);
        }
        logger.debug("Starting test...");
        state = State.TestRunning;


        startStatisticsCapture();

        List<AgentModel> connectedAgentModels = model.getConnectedAgents();
        logger.debug("We have {} connected agents", connectedAgentModels.size());

        FactoryMap<String, FactoryMap<String, DataBucket>> dataBucketsByAgentName = divideDataIntoBuckets(
                connectedAgentModels);

        Map<String, List<PhaseInstruction>> instructions = new FactoryMap<String, List<PhaseInstruction>>() {
            @Override
            protected List<PhaseInstruction> createEmptyValue(String s) {
                return new ArrayList<PhaseInstruction>();
            }
        };
        populateInstructionsList(instructions);

        publishTestInstructionsToAgents(instructions, dataBucketsByAgentName);
        logger.info("Test instruction have been sent...");

        model.getTestStartTime().set(System.currentTimeMillis());

        agentPingTimer = TimerUtils.everySecond("Agent ping timer", new Runnable() {
            @Override
            public void run() {
                sendPings();
            }
        });

        rawResultsController.startStatsUpdater(model);
    }

    protected void sendPings() {
        logger.debug("Sending pings to all agents...");
        for (final AgentModel agentModel : model.getAgentModels()) {
            if (agentModel.getConnected().get()) {
                KryoClient client = agentModel.getKryoClient();
                client.send("agent", new PingMessage());
            }
        }
    }

    public JBombardierModel getModel() {
        return model;
    }

    //    public void setModel(final JBombardierModel model) {
    //        this.model = model;
    //    }

    @SuppressWarnings("serial")
    public FactoryMap<String, FactoryMap<String, DataBucket>> divideDataIntoBuckets(List<AgentModel>
                                                                                                connectedAgentModels) {

        Is.greaterThanZero(connectedAgentModels.size(), "You must have at least one connected agent to start the test");

        FactoryMap<String, FactoryMap<String, DataBucket>> dataBuckets = new FactoryMap<String, FactoryMap<String,
                DataBucket>>() {
            @Override
            protected FactoryMap<String, DataBucket> createEmptyValue(String key) {
                return new FactoryMap<String, DataBucket>() {
                    @Override
                    protected DataBucket createEmptyValue(String key) {
                        return new DataBucket(key);
                    }
                };
            }
        };

        logger.debug("Dividing data into agent buckets...");

        // Iterate through each datasource
        for (String dataName : dataByName.keySet()) {

            DataSource data = dataByName.get(dataName);
            String dataSourceName = data.getDataSourceName();

            logger.debug("Processing data source {}", data);

            // Depending on the strategy, we populate each agents data buckets
            // in a slightly different way
            DataStrategy strategy = data.getStrategy();
            switch (strategy) {
                case fixedThread: {
                    // We dont distribute anything for this strategy, each
                    // agent/thread has to make a unique request and the server
                    // will dish them out.
                    for (AgentModel agent : connectedAgentModels) {
                        String agentName = agent.getName().get();
                        FactoryMap<String, DataBucket> dataSourceBucketsForAgent = dataBuckets.get(agentName);
                        DataBucket dataBucket = dataSourceBucketsForAgent.get(dataSourceName);

                        // Just set the strategy so the agent knows what to do
                        dataBucket.setStrategy(strategy);
                    }

                    break;
                }
                case pooledThread:
                case pooledAgent:
                    // Split between each agent

                    // Divide the number of data records between the connected
                    // agents
                    int rows = data.getValues().length;
                    int agents = connectedAgentModels.size();

                    DataBucket[] buckets = new DataBucket[agents];
                    for (int i = 0; i < agents; i++) {
                        buckets[i] = new DataBucket(dataSourceName);
                        buckets[i].setColumns(data.getHeader());
                        buckets[i].setStrategy(strategy);
                    }

                    // Iterate through the rows
                    if (rows >= connectedAgentModels.size()) {
                        int currentRow = 0;
                        while (currentRow < rows) {
                            int agentForRow = currentRow % agents;
                            String[] record = data.getRecord(currentRow);
                            buckets[agentForRow].addRecord(record);
                            currentRow++;
                        }
                    } else {
                        // We dont have enough entries to go around, so we need
                        // to do a slightly different iteration
                        for (int i = 0; i < connectedAgentModels.size(); i++) {
                            int rowForAgent = i % rows;
                            String[] record = data.getRecord(rowForAgent);
                            buckets[i].addRecord(record);
                        }
                    }

                    // Allocate a bucket to each agents
                    int i = 0;
                    for (AgentModel agent : connectedAgentModels) {
                        String agentName = agent.getName().get();
                        dataBuckets.get(agentName).put(dataSourceName, buckets[i]);
                        i++;
                    }

                    break;
                case pooledGlobal:

                    for (AgentModel agent : connectedAgentModels) {
                        String agentName = agent.getName().get();
                        FactoryMap<String, DataBucket> dataSourceBucketsForAgent = dataBuckets.get(agentName);
                        DataBucket dataBucket = dataSourceBucketsForAgent.get(dataSourceName);

                        // Add everything
                        dataBucket.setColumns(data.getHeader());
                        String[][] values = data.getValues();
                        List<String[]> asList = new ArrayList<String[]>();
                        for (String[] strings : values) {
                            asList.add(strings);
                        }

                        dataBucket.setValues(asList);
                        dataBucket.setStrategy(strategy);
                    }

                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported strategy " + strategy);
            }
        }

        logger.debug("Properties divided into {} buckets", dataBuckets.size());
        return dataBuckets;
    }

    public void handleAgentFailedInstruction(AgentFailedInstruction afi) {
        endTestAbnormally();
        model.abandonTest(
                "One of the agents reported an exception during setup; please check the console tab to find out what " +
                        "went wrong",
                afi);
    }

    public void handleAgentLogMessage(AgentLogMessage agentLogMessage) {
        ConsoleEventModel model = new ConsoleEventModel();
        String message = agentLogMessage.getMessage();

        if (message.startsWith("TestFailedException")) {
            String actualMessage = message.split("\\|")[1];
            model.setMessage(actualMessage);
            model.setSeverity(ConsoleEventModel.Severity.Warning);
            model.setThrowable("");
        } else {
            model.setMessage(message);
            if (agentLogMessage.getObject() != null) {
                model.setSeverity(ConsoleEventModel.Severity.Severe);
            } else {
                model.setSeverity(ConsoleEventModel.Severity.Information);
            }
            model.setThrowable(agentLogMessage.getObject());
        }
        model.setTime(new Date());
        model.setSource(agentLogMessage.getThreadName());
        this.model.log(model);
    }

    public AgentPropertyResponse handleAgentPropertyRequest(AgentPropertyRequest agentPropertyRequest) {
        // TODO : support the property strategies to out different values to
        // different agent/thread combos.
        AgentPropertyResponse response;
        String propertyValue;
        synchronized (properties) {
            propertyValue = properties.get(agentPropertyRequest.getPropertyName());
        }
        // TODO : why are we repeating all the values in the reponse? The
        // request/response mapping code should ensure we dont have to do that
        // anymore!
        response = new AgentPropertyResponse(agentPropertyRequest.getPropertyName(),
                                             agentPropertyRequest.getThreadName(),
                                             propertyValue);
        return response;
    }

    public void handleAgentStatusUpdate(AgentStats agentStats) {

        if (state != State.TestRunning) {
            throw new IllegalStateException(
                    "Unable to handle agent stats whilst the test isn't running - looks like something has gone wrong");
        }

        agentStatusUpdatesStat.increment();

        model.onAgentStatusUpdate(agentStats);
        rawResultsController.handleAgentStatusUpdate(agentStats);
    }

    public void handleThreadsChangedMessage(ThreadsChangedMessage message) {
        model.incrementActiveThreadCount(message.getAgent(), message.getThreads());
    }

    public AgentPropertyEntryResponse handleAgentPropertyEntryRequest(AgentPropertyEntryRequest request) {
        AgentPropertyEntryResponse response;

        String propertyName = request.getPropertyName();
        CsvPropertiesProvider csvPropertiesProvider = csvPropertyProviders.get(propertyName);
        if (csvPropertiesProvider != null) {
            // TODO : work out why we have to provide a string there in this
            // case.
            PropertyEntry propertyEntry = csvPropertiesProvider.getPropertyEntry("dontmatteratthisbit");
            response = new AgentPropertyEntryResponse(request.getPropertyName(),
                                                      request.getThreadName(),
                                                      propertyEntry);
        } else {
            response = new AgentPropertyEntryResponse(request.getPropertyName(), request.getThreadName(), null);
        }

        return response;
    }

    private static void assertTestClassesAreValid(List<PhaseModel> phases) {

        Set<String> alreadyChecked = new HashSet<String>();

        StringBuilder buffer = new StringBuilder();
        for (PhaseModel phaseModel : phases) {
            for (TestModel testModel : phaseModel.getTestModels()) {
                String classname = testModel.getClassname();

                if (alreadyChecked.contains(classname)) {
                    // Skip it
                } else {

                    logger.debug("Validing test class {}", classname);
                    try {
                        @SuppressWarnings("unused") PerformanceTest performanceTest = (PerformanceTest) Class.forName(
                                classname).newInstance();
                    } catch (ClassNotFoundException e) {
                        buffer.append("Class '")
                              .append(classname)
                              .append("' could not be found, please check your configuration!\n");
                    } catch (InstantiationException e) {
                        buffer.append("Class '")
                              .append(classname)
                              .append("' could not be instantiated, please ensure it has a default constructor. Any " +
                                              "arguments should be setup using the properties methods on the " +
                                              "TestContext class before the test starts.\n");
                    } catch (IllegalAccessException e) {
                        buffer.append("Class '")
                              .append(classname)
                              .append("' could not be instantiated, please check to make sure the default constructor" +
                                              " is public.\n");
                    } catch (ClassCastException e) {
                        buffer.append("Class '")
                              .append(classname)
                              .append("' does't implement the PerformanceTest interface. All tests must implement " +
                                              "this interface, please check your test code.\n");
                    }

                    alreadyChecked.add(classname);
                }
            }
        }

        String errors = buffer.toString();
        if (errors.length() > 0) {
            logger.warn("There were errors with the test configuration {}", errors);
            throw new RuntimeException("There were problems with your test configuration : \n" + errors);
        }
    }

    public void stopTelemetry() {
        for (final AgentModel agentModel : model.getAgentModels()) {
            KryoClient client = agentModel.getKryoClient();
            client.sendRequest("agent", new StopTelemetryRequest(), new ResponseHandler<String>() {
                public void onResponse(String response) {
                }
            });
        }

    }

    //    public boolean isSendCloseMessageOnWindowClose() {
    //        return sendCloseMessageOnWindowClose;
    //    }

    public void killAgents() {

        for (final AgentModel agentModel : model.getAgentModels()) {
            KryoClient client = agentModel.getKryoClient();
            if (client.isConnected()) {
                logger.info("Sending kill message to agent {}", client);
                client.send("agent", new AgentKillInstruction(2));
            }
        }

    }

    private void closeAgentConnections() {

        for (final AgentModel agentModel : model.getAgentModels()) {
            KryoClient client = agentModel.getKryoClient();
            if (client.isConnected()) {
                client.stop();
            }
        }

    }

    public void killAgentsAndReset() {
        killAgents();
        if (state == State.TestRunning) {
            endTestAbnormally();
        }
    }

    private void initialiseStatisticsCapture(JBombardierConfiguration configuration, JBombardierModel model) {
        logger.debug("Initialising statistics capture...");

        List<StatisticsCapture> statisticsCapture = configuration.getStatisticsCapture();
        for (StatisticsCapture capture : statisticsCapture) {
            String className = capture.getClassName();
            StatisticProvider capturePlugin = ReflectionUtils.newInstance(className);
            Metadata properties = Metadata.fromProperties(capture.getProperties());
            capturePlugin.configure(properties);

            capturePlugin.addDestination(new Destination<CapturedStatistic>() {
                @Override
                public void send(CapturedStatistic statistic) {
                    handleCapturedStatistic(statistic);
                }
            });

            model.getStatisticsProviders().add(capturePlugin);
        }

        List<HubCapture> hubCapture = configuration.getHubCapture();
        for (HubCapture capture : hubCapture) {

            LoggingHubStatisticCapture loggingHubStatisticCapture = new LoggingHubStatisticCapture();
            loggingHubStatisticCapture.setHosts(capture.getHub());

            List<HubCapturePattern> patterns = capture.getPatterns();
            for (HubCapturePattern capturePattern : patterns) {

                LoggingHubStatisticCapture.CapturePattern pattern = new LoggingHubStatisticCapture.CapturePattern();
                pattern.path = capturePattern.getPath();
                pattern.pattern = capturePattern.getPattern();
                pattern.values = capturePattern.getValues();


                loggingHubStatisticCapture.getCapturePatterns().add(pattern);
            }

            loggingHubStatisticCapture.addDestination(new Destination<CapturedStatistic>() {
                @Override
                public void send(CapturedStatistic statistic) {
                    handleCapturedStatistic(statistic);
                }
            });
            model.getStatisticsProviders().add(loggingHubStatisticCapture);
        }

        List<JmxCapture> jmxCapture = configuration.getJmxCapture();
        for (JmxCapture capture : jmxCapture) {

            JMXStatisticCapture jmxStatisticCapture = new JMXStatisticCapture();

            jmxStatisticCapture.setUsername(capture.getUsername());
            jmxStatisticCapture.setPassword(capture.getPassword());
            jmxStatisticCapture.setConnectionPoints(capture.getConnectionPoints());

            List<JmxTarget> jmx = capture.getJmx();
            for (JmxTarget jmxTarget : jmx) {
                JMXStatisticCapture.CaptureElement element = new JMXStatisticCapture.CaptureElement();
                element.attribute = jmxTarget.getAttribute();
                element.objectName = jmxTarget.getObjectName();
                element.path = jmxTarget.getPath();
                jmxStatisticCapture.add(element);
            }

            jmxStatisticCapture.setDelay(1000);
            jmxStatisticCapture.addDestination(new Destination<CapturedStatistic>() {
                @Override
                public void send(CapturedStatistic statistic) {
                    handleCapturedStatistic(statistic);
                }
            });
            model.getStatisticsProviders().add(jmxStatisticCapture);
        }

    }

    private void handleCapturedStatistic(CapturedStatistic statistic) {
        capturedStatisticsHelper.addCapturedStatistic(statistic);
    }

    protected void handleLowMemory() {
        rawResultsController.reduceResultStorage();
        System.gc();
        // System.gc();
        // System.gc();
    }

    public void initialiseStats() {
        connectionsStat = statBundle.createStat("connections");
        newConnectionsStat = statBundle.createStat("new connections");
        disconnectionsStat = statBundle.createStat("disconnections");
        expectedAgentsStat = statBundle.createStat("expected agents");
        agentStatusUpdatesStat = statBundle.createStat("agent status updates");
        classloaderRequestsStat = statBundle.createStat("RCL requests");
        statBundle.createHeapStat();

        newConnectionsStat.setIncremental(true);
        disconnectionsStat.setIncremental(true);
        connectionsStat.setIncremental(false);
        expectedAgentsStat.setIncremental(false);
        agentStatusUpdatesStat.setIncremental(true);
        classloaderRequestsStat.setIncremental(true);
    }

    public void initialiseProperties(JBombardierConfiguration configuration, JBombardierModel model) {
        logger.debug("Initialising properties...");

        synchronized (properties) {
            List<Property> variables = configuration.getProperties();
            for (Property variable : variables) {
                String name = variable.getName();
                String value = variable.getValue();
                properties.put(name, value);
            }
        }

        Map<String, DataSource> alreadyLoaded = new HashMap<String, DataSource>();

        synchronized (csvPropertyProviders) {
            logger.debug("Loading csv properties...");
            List<CsvProperty> csvProperties = configuration.getCsvProperties();
            for (CsvProperty csvProperty : csvProperties) {

                // Register the csv provider for dealing with
                // AgentPropertyEntryRequests
                // TODO : this might be considered legacy now!
                String csvfile = csvProperty.getCsvfile();
                CsvPropertiesProvider provider = new CsvPropertiesProvider(csvfile);
                csvPropertyProviders.put(csvProperty.getName(), provider);

                logger.debug("Loading {} data from {}", csvProperty.getName(), csvfile);

                // Also process the config information into DataSources
                DataSource dataSource = new DataSource(csvProperty.getName(),
                                                       DataStrategy.valueOf(csvProperty.getStrategy()));

                DataSource existingDataSource = alreadyLoaded.get(csvfile);
                if (existingDataSource != null) {
                    logger.debug("Already loaded this file, reusing data ");
                    dataSource.setData(existingDataSource.getHeader(), existingDataSource.getValues());
                } else {

                    String content = ResourceUtils.read(csvfile);
                    if (content.isEmpty()) {
                        throw new RuntimeException(String.format(
                                "The csv data file for property '%s' provided was '%s', but this file was empty.",
                                csvProperty.getName(),
                                csvfile));
                    }

                    String[] lines = content.split("\r\n|\n");
                    String[] header = lines[0].split(",");

                    String[][] values = new String[lines.length - 1][];
                    for (int i = 1; i < lines.length; i++) {
                        String line = lines[i];
                        String[] lineValues = line.split(",");
                        String[] trimmed = new String[lineValues.length];
                        for (int j = 0; j < lineValues.length; j++) {
                            trimmed[j] = lineValues[j].trim();
                        }

                        values[i - 1] = trimmed;
                    }

                    dataSource.setData(header, values);
                    alreadyLoaded.put(csvfile, dataSource);
                }
                dataByName.put(dataSource.getDataSourceName(), dataSource);
            }
        }
    }

    public static void initialiseModelFromConfiguration(JBombardierConfiguration configuration, JBombardierModel
            model) {
        logger.debug("Initialising initialiseModelFromConfiguration...");

        List<PhaseConfiguration> phases = configuration.getPhases();
        if (phases.isEmpty()) {

            // Legacy mode - create a default phase
            PhaseModel phaseModel = new PhaseModel();
            phaseModel.getPhaseName().set("Default");

            if (StringUtils.isNotNullOrEmpty(configuration.getDuration())) {
                phaseModel.getPhaseDuration().set(TimeUtils.parseInterval(configuration.getDuration()));
            }

            if (StringUtils.isNotNullOrEmpty(configuration.getWarmupTime())) {
                phaseModel.getWarmupDuration().set(TimeUtils.parseInterval(configuration.getWarmupTime()));
            }

            List<TestConfiguration> tests = configuration.getTests();
            for (TestConfiguration test : tests) {
                TestModel testModel = createTestModel(test);
                phaseModel.getTestModels().add(testModel);
            }

            model.getPhaseModels().add(phaseModel);

        } else {
            for (PhaseConfiguration phase : phases) {
                if(phase.isEnabled()) {
                    PhaseModel phaseModel = new PhaseModel();
                    phaseModel.getPhaseName().set(phase.getPhaseName());
                    phaseModel.setStateEstablishers(phase.getStateEstablishers());

                    List<PhaseControllerConfiguration> phaseControllers = phase.getPhaseControllers();
                    for (PhaseControllerConfiguration phaseController : phaseControllers) {
                        PhaseController phaseControllerInstance = (PhaseController) ReflectionUtils.instantiate(phaseController.getClassName());
                        phaseControllerInstance.configure(phaseController.getConfiguration());
                        phaseModel.getPhaseControllers().add(phaseControllerInstance);
                    }

                    Is.notNullOrEmpty(phase.getDuration(), StringUtils.format("Phase duration must be set for phase '{}'", phase.getPhaseName()));

                    phaseModel.getPhaseDuration().set(TimeUtils.parseInterval(phase.getDuration()));

                    if (StringUtils.isNotNullOrEmpty(phase.getWarmupDuration())) {
                        phaseModel.getWarmupDuration().set(TimeUtils.parseInterval(phase.getWarmupDuration()));
                    }

                    List<TestConfiguration> tests = phase.getTests();
                    for (TestConfiguration testConfiguration : tests) {
                        TestModel testModel = createTestModel(testConfiguration);
                        phaseModel.getTestModels().add(testModel);
                    }

                    String inheritFrom = phase.getInheritFrom();
                    if (StringUtils.isNotNullOrEmpty(inheritFrom)) {

                        List<TestConfiguration> inheritedTests = getTestsForPhase(configuration, inheritFrom);
                        if (inheritedTests == null) {
                            throw new IllegalArgumentException(StringUtils.format(
                                    "Phase '{}' attempted to inherit from '{}' - this phase could not be found in the " + "configuration",
                                    phase.getPhaseName(),
                                    inheritFrom));
                        } else {
                            for (TestConfiguration testConfiguration : inheritedTests) {
                                TestModel testModel = createTestModel(testConfiguration);
                                phaseModel.getTestModels().add(testModel);
                            }
                        }
                    }

                    // Apply the transaction multiplier
                    ObservableList<TestModel> testModels = phaseModel.getTestModels();
                    for (TestModel testModel : testModels) {
                        testModel.getTargetRate().set(testModel.getTargetRate().get() * phase.getRateMultiplier());
                    }

                    model.getPhaseModels().add(phaseModel);
                }
            }
        }

        List<AgentConfiguration> agentConfigurations = configuration.getAgents();
        for (AgentConfiguration agentConfiguration : agentConfigurations) {

            final AgentModel agentModel = new AgentModel();

            agentModel.getConnected().set(false);
            agentModel.getName().set(agentConfiguration.getName());

            if (agentConfiguration.getName().startsWith(AgentConfiguration.embeddedName)) {
                agentModel.getAddress().set("localhost");
            } else {
                agentModel.getAddress().set(agentConfiguration.getAddress());
                agentModel.getPort().set(agentConfiguration.getPort());
            }

            model.addAgentModel(agentModel);
        }


        model.getTransactionRateModifier().set(configuration.getTransactionRateModifier());

        validateModel(model);

    }

    private static List<TestConfiguration> getTestsForPhase(JBombardierConfiguration configuration, String
            inheritFrom) {

        List<TestConfiguration> tests = null;

        for (PhaseConfiguration phaseConfiguration : configuration.getPhases()) {
            if (phaseConfiguration.getPhaseName().equals(inheritFrom)) {
                tests = phaseConfiguration.getTests();
                break;
            }
        }

        return tests;
    }

    private static TestModel createTestModel(TestConfiguration testConfiguration) {
        TestModel testModel = new TestModel();
        testModel.setClassname(testConfiguration.getClassname());
        testModel.setRateStep(testConfiguration.getRateStep());

        if (StringUtils.isNotNullOrEmpty(testConfiguration.getName())) {
            testModel.setName(testConfiguration.getName());
        } else {
            testModel.setName(StringUtils.afterLast(testConfiguration.getClassname(), "."));
        }

        testModel.setRateStepTime(testConfiguration.getRateStepTime());
        testModel.getTargetRate().set(testConfiguration.getTargetRate());
        testModel.getTargetThreads().set(testConfiguration.getTargetThreads());
        testModel.getThreadStep().set(testConfiguration.getThreadStep());
        testModel.getThreadStepTime().set(testConfiguration.getThreadStepTime());
        testModel.setRecordAllValues(testConfiguration.getRecordAllValues());
        testModel.setProperties(testConfiguration.buildPropertyMap());
        testModel.setTransactionSLAs(testConfiguration.buildSLAMap());
        testModel.setFailureThreshold(testConfiguration.getFailureThreshold());
        testModel.setFailureThresholdMode(testConfiguration.getFailureThresholdMode());
        testModel.setFailedTransactionCountThreshold(testConfiguration.getFailedTransactionCountFailureThreshold());
        testModel.setFailureThresholdResultCountMinimum(testConfiguration.getFailureThresholdResultCountMinimum());
        testModel.setMovingAveragePoints(testConfiguration.getMovingAveragePoints());
        testModel.getAgent().set(testConfiguration.getAgent());

        return testModel;
    }

    private static void validateModel(JBombardierModel model) {
        Is.greaterThanZero(model.getPhaseModels().size(),
                           "The test model has no phases; please make sure you have at least one phase or one test in" +
                                   " your configuration.");
        assertTestClassesAreValid(model.getPhaseModels());
    }

    private void attachMessagingToAgentModels(final JBombardierConfiguration configuration, JBombardierModel model) {
        logger.info("Starting agent connections...");

        List<AgentConfiguration> agentConfigurations = configuration.getAgents();
        for (AgentConfiguration agentConfiguration : agentConfigurations) {

            final List<AgentModel> agentModels = model.getAgentModels();
            for (final AgentModel agentModel : agentModels) {

                if (agentModel.getName().get().equals(agentConfiguration.getName())) {
                    int objectBufferSize = agentConfiguration.getObjectBufferSize();
                    int writeBufferSize = agentConfiguration.getWriteBufferSize();
                    final KryoClient client = new KryoClient("controller", writeBufferSize, objectBufferSize);

                    KryoHelper.registerTypes(client.getKryo());

                    if (agentConfiguration.getName().startsWith(AgentConfiguration.embeddedName)) {
                        Agent2 embeddedAgent = new Agent2();
                        int freePort = NetUtils.findFreePort();
                        embeddedAgent.disableSystemExitOnKill();
                        embeddedAgent.setOutputStats(configuration.isOutputEmbeddedAgentStats());
                        embeddedAgent.setWriteBufferSize(agentConfiguration.getWriteBufferSize());
                        embeddedAgent.setObjectBufferSize(agentConfiguration.getObjectBufferSize());
                        embeddedAgent.setBindPort(freePort);
                        embeddedAgent.setPingTimeout(agentConfiguration.getPingTimeout());
                        embeddedAgent.start();
                        embeddedAgents.add(embeddedAgent);

                        agentModel.getAddress().set("localhost");
                        agentModel.getPort().set(freePort);
                    } else {
                        agentModel.getAddress().set(agentConfiguration.getAddress());
                        agentModel.getPort().set(agentConfiguration.getPort());
                    }

                    client.addConnectionPoint(new InetSocketAddress(agentModel.getAddress().get(),
                                                                    agentModel.getPort().get()));
                    agentModel.setKryoClient(client);

                    client.addConnectionListener(new ConnectionListener() {
                        public void onDisconnected() {
                            logger.info("Agent has disconnected {}", agentModel);
                            disconnectionsStat.increment();
                            connectionsStat.decrement();
                            agentModel.getConnected().set(false);
                        }

                        public void onConnected() {
                            connectionsStat.increment();
                            newConnectionsStat.increment();
                            logger.info("Agent has connected {}", agentModel);
                            agentModel.getConnected().set(true);

                            client.sendRequest("agent",
                                               new SendTelemetryRequest(NetUtils.getLocalIP(),
                                                                        configuration.getTelemetryHubPort()),
                                               new ResponseHandler<String>() {
                                                   public void onResponse(String response) {

                                                   }
                                               });

                            attachReflectionDispatcher(client);
                            //                    checkForAutostart();
                        }
                    });

                    client.startBackground();
                }
            }
        }

        // James - workaround here, if we have a test already running, we can
        // make the console have a reasonable go at picking up where we left off
        // if we tell the model to expect some clients to already be running -
        // this should make the charts and result table work without starting
        // another test

        // James - actually this is a bug - a race condition - as the real
        // agents connect, they'll increment this value, so if they are running
        // locally and connect really quickly you can end up with too many
        // agents expected. This means no results will be gathered, so is much
        // worse than that previous hack!
        // model.setAgentsInTest(agents.size());

        model.log(ConsoleEventModel.Severity.Information, "%d agents added", agentConfigurations.size());
    }

    public Map<String, AgentModel> getAgentsByAgentName() {
        return model.getAgentsByAgentName();
    }

    // TODO : this is a gui thing. It should really be done via listeners in the gui. The headless mode already has
    // it sorted.
    // TODO : our new design philosohpy is that the controller is dumb - it needs to be driven by something else with
    // a purpose in mind.
    //    protected synchronized void checkForAutostart() {
    //        if (!model.isTestRunning()) {
    //            int autostartAgents = configuration.getAutostartAgents();
    //            if (autostartAgents > 0 && model.getConnectionAgentCount() >= autostartAgents) {
    //                publishTestInstructions();
    //            }
    //        }
    //    }

    public AgentClassResponse handleClassRequest(AgentClassRequest acr) {
        logger.debug("Agent class request : {}", acr);
        classloaderRequestsStat.increment();

        byte[] data;
        if (acr.isClassNotResource()) {
            data = resolver.getClassBytes(acr.getClassName());
        } else {
            data = resolver.getResourceBytes(acr.getClassName());
        }

        AgentClassResponse agentClassResponse = new AgentClassResponse();
        agentClassResponse.setData(data);

        logger.debug("Sent {} bytes back to agent for class {}", data.length, acr.getClassName());

        return agentClassResponse;
    }

    protected void updateTransactionRateModifier(double transactionRateModifier) {
        TestVariableUpdateRequest request = new TestVariableUpdateRequest();
        request.setTestName("");
        request.setField(TestField.transactionRateModifier);
        request.setValue(transactionRateModifier);
        for (final AgentModel agentModel : model.getAgentModels()) {
            KryoClient client = agentModel.getKryoClient();
            if (client != null) {
                client.sendRequest("agent", request, new ResponseHandler<String>() {
                    public void onResponse(String response) {
                        logger.info("Transaction rate modifier successfully applied to client : '{}'", response);
                    }
                });
            } else {
                logger.warn("No messaging attached to agent model '{}', unable to send message '{}'",
                            agentModel,
                            request);
            }
        }

        // Update the target rates for the current transactions
        ObservableList<PhaseModel> phaseModels = model.getPhaseModels();
        for (PhaseModel phaseModel : phaseModels) {
            ObservableList<TestModel> testModels = phaseModel.getTestModels();
            for (TestModel testModel : testModels) {

                // TODO : refactor fix me - need TRMs by phase and then by test
                //                List<String> names = model.getTestNames();
                //                for (String name : names) {
                //                    TestModel testModel = model.getTestModelForTest(name);
                //                    model.getTransactionResultModelForTest(name)
                //                         .getTargetSuccessfulTransactionsPerSecond()
                //                         .set(testModel.getTargetRate() * transactionRateModifier);
                //                }
            }
        }


    }

    public void updateTestVariable(final String testName, final TestField field, final Object newValue) {

        TestVariableUpdateRequest request = new TestVariableUpdateRequest();
        request.setTestName(testName);
        request.setField(field);
        request.setValue(newValue);
        for (final AgentModel agentModel : model.getAgentModels()) {
            KryoClient client = agentModel.getKryoClient();
            client.sendRequest("agent", request, new ResponseHandler<String>() {
                public void onResponse(String response) {
                    List<TestModel> testModels = model.getCurrentPhase().get().getTestModels();

                    for (TestModel testModel : testModels) {
                        if (testModel.getName().equals(testName)) {
                            switch (field) {
                                case rateStep:
                                    testModel.setRateStep((Float) newValue);
                                    break;
                                case rateStepTime:
                                    testModel.setRateStepTime((Long) newValue);
                                    break;
                                case targetRate:
                                    testModel.getTargetRate().set((Double)newValue);
                                    break;
                                case targetThreads:
                                    testModel.getTargetThreads().set((Integer) newValue);
                                    break;
                                case threadStep:
                                    testModel.getThreadStep().set((Integer) newValue);
                                    break;
                                case threadStepTime:
                                    testModel.getThreadStepTime().set((Long) newValue);
                                    break;
                            }
                        }
                    }

                }
            });
        }
    }

    private void publishTestInstructionsToAgents(Map<String, List<PhaseInstruction>> instructions, FactoryMap<String,
            FactoryMap<String, DataBucket>> dataBucketsByAgentName) {

        int agents = model.getConnectionAgentCount();

        for (final AgentModel agentModel : model.getAgentModels()) {
            logger.info("Publishing data buckets to agent {}", agentModel);
            if (agentModel.getConnected().get()) {

                FactoryMap<String, DataBucket> data = dataBucketsByAgentName.get(agentModel.getName().get());
                logger.debug("This agent has a data bucket with {} keys and {} items", data.size(), countItems(data));

                KryoClient client = agentModel.getKryoClient();
                Set<String> keySet = data.keySet();

                // Send the buckets one at a time in case we blow the buffer size
                for (String dataBucketName : keySet) {
                    final DataBucket dataBucket = data.get(dataBucketName);
                    logger.debug("Sending bucket {}", dataBucket);

                    final QuietLatch latch = new QuietLatch(1);
                    client.sendRequest("agent", dataBucket, new ResponseHandler<String>() {
                        public void onResponse(String response) {
                            latch.countDown();
                        }
                    });

                    boolean wasReceived = latch.await();
                    if (!wasReceived) {
                        logger.warn(
                                "The data bucket wasn't received by agent {} within the wait period, the console " +
                                        "can't be sure the agent is ready to start",
                                agentModel);
                    } else {
                        logger.info("Agent {} has received data bucket {}", agentModel, dataBucket);
                    }
                }
            }
        }


        final CountDownLatch instructionsReceivedLatch = new CountDownLatch(agents);

        for (final AgentModel agentModel : model.getAgentModels()) {
            logger.debug("Publishing test instruction to agent {}", agentModel);
            KryoClient client = agentModel.getKryoClient();
            if (agentModel.getConnected().get()) {

                TestPackage testPackage = new TestPackage(agentModel.getName().get(),
                                                          instructions.get(agentModel.getName().get()));
                testPackage.setLoggingHubs(configuration.getLoggingHubs());
                testPackage.setLoggingType(configuration.getLoggingTypes());

                client.sendRequest("agent", testPackage, new ResponseHandler<String>() {
                    public void onResponse(String response) {
                        logger.debug("Agent response received from agent {}, test package received", agentModel);
                        agentModel.getPackageReceived().set(true);
                        instructionsReceivedLatch.countDown();
                    }
                });

                logger.debug("Message sent to agent {}", agentModel);

            } else {
                logger.trace("Agent {} isn't connected, skipping", agentModel);
            }
        }

        try {
            if (!instructionsReceivedLatch.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException(String.format(
                        "One or more agent have not responded to the test package message after the timeout interval " +
                                "- aborting test"));
            } else {
                logger.info("All agents have confirmed receipt of test instructions");
            }
        } catch (InterruptedException e) {
            logger.warn(
                    "Thread interupted waiting for confirmation that all agents have received their instructions. We " +
                            "have no way of knowing the agent state from this point.");
        }


        expectedAgentsStat.setValue(agents);
        model.setAgentsInTest(agents);
        // model.getTestRunning().set(true);
    }

    private void attachReflectionDispatcher(KryoClient client) {
        // Attach a reflection notifier to fire message back at the
        // controller if we get anything sent to us

        ReflectionDispatchMessageListener messageListener = new ReflectionDispatchMessageListener("controller",
                                                                                                  client,
                                                                                                  this);
        dispatchingListeners.put(messageListener, client);
        client.addMessageListener(messageListener);
    }

    private int countItems(FactoryMap<String, DataBucket> data) {

        int count = 0;
        Set<String> keySet = data.keySet();
        for (String string : keySet) {
            DataBucket dataBucket = data.get(string);
            if (dataBucket.getStrategy() == DataStrategy.fixedThread) {
                // This strategy doesn't send anything...
            } else {
                count += dataBucket.getValues().size();
            }
        }

        return count;
    }

    public void populateInstructionsList(Map<String, List<PhaseInstruction>> instructions) {

        int agents = model.getConnectionAgentCount();

        List<PhaseModel> phases = model.getPhaseModels();
        for (PhaseModel phase : phases) {

            // Build a phase instruction for each agent
            Map<String, PhaseInstruction> phaseInstructionsByAgent = new HashMap<String, PhaseInstruction>();
            for (final AgentModel agentModel : model.getAgentModels()) {
                PhaseInstruction phaseInstruction = new PhaseInstruction();
                phaseInstruction.setPhaseName(phase.getPhaseName().get());
                phaseInstructionsByAgent.put(agentModel.getName().get(), phaseInstruction);
            }

            // Add the tests to each of these phase models
            for (TestModel test : phase.getTestModels()) {

                // Work out what the agent distribution will be
                double targetRate = test.getTargetRate().get();

                double perAgentRate = targetRate / agents;

                int index = 0;
                for (final AgentModel agentModel : model.getAgentModels()) {
                    TestInstruction instruction = buildInstruction(test);
                    phaseInstructionsByAgent.get(agentModel.getName().get()).getInstructions().add(instruction);

                    // Set the appropriate target rate
                    if (StringUtils.isNotNullOrEmpty(test.getAgent().get())) {
                        Set<String> agentNames = new HashSet<String>();
                        String[] split = test.getAgent().get().split(",");
                        for (String s : split) {
                            agentNames.add(s.trim());
                        }

                        double specificAgentsSplit = targetRate / split.length;

                        // This test has been restricted to a particular agent
                        if (agentNames.contains(agentModel.getName().get())) {
                            instruction.setTargetRate(specificAgentsSplit);
                        } else {
                            instruction.setTargetRate(0);
                        }
                    } else {
                        if (perAgentRate < 1) {
                            // Give the work to the first agent, there is no point in spreading this around
                            if (index == 0) {
                                instruction.setTargetRate(targetRate);
                            } else {
                                instruction.setTargetRate(0);
                            }
                        } else {
                            instruction.setTargetRate(perAgentRate);
                        }
                    }
                    index++;
                }

            }

            // Apply the phase instructions to the main map
            for (final AgentModel agentModel : model.getAgentModels()) {
                instructions.get(agentModel.getName().get())
                            .add(phaseInstructionsByAgent.get(agentModel.getName().get()));
            }

        }
    }

    private TestInstruction buildInstruction(TestModel test) {
        TestInstruction instruction = new TestInstruction();
        instruction.setTestName(test.getName());
        instruction.setClassname(test.getClassname());
        instruction.setTargetThreads(test.getTargetThreads().get());
        instruction.setThreadRampupStep(test.getThreadStep().get());
        instruction.setThreadRampupTime(test.getThreadStepTime().get());
        instruction.setRateStep(test.getRateStep());
        instruction.setRateStepTime(test.getRateStepTime());
        instruction.setRecordAllValues(test.getRecordAllValues());
        instruction.setTransactionRateModifier(model.getTransactionRateModifier().get());
        instruction.getProperties().putAll(properties);
        instruction.getProperties().putAll(test.getProperties());
        return instruction;
    }

    public void generateReportInBackgroundAndShowInBrowser() {
        pool.execute(new Runnable() {
            public void run() {
                RunResultBuilder runResultBuilder = new RunResultBuilder();
                RunResult snapshot = runResultBuilder.createSnapshot(model,
                                                                     capturedStatisticsHelper,
                                                                     rawResultsController);
                generateReport(snapshot);
                BrowserUtils.browseTo(new File(getReportsFolder(), "index.html").toURI());
            }
        });
    }

    public void generateReport(RunResult snapshot) {
        final File reportsFolder = getReportsFolder();
        reportsFolder.mkdirs();
        ReportGenerator.generateReport(reportsFolder, snapshot, TimeUnit.MILLISECONDS);
    }


    public String generateReportOld(boolean openInBrowser, boolean queueCharts) {

        capturedStatisticsHelper.flush();

        final VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();

        final StringUtilsBuilder errors = new StringUtilsBuilder();
        EventCartridge eventCartridge = new EventCartridge();
        eventCartridge.addInvalidReferenceEventHandler(new InvalidReferenceEventHandler() {

            @Override
            public Object invalidGetMethod(Context context, String reference, Object object, String property, Info
                    info) {
                // This mean quiet references dont trigger errors - this is used
                // for things like null checks
                if (!reference.startsWith("$!")) {
                    errors.appendLine("Invalid get method : reference {} object {} property {} info {}",
                                      reference,
                                      object,
                                      property,
                                      info);
                }
                return null;
            }

            @Override
            public boolean invalidSetMethod(Context context, String leftreference, String rightreference, Info info) {
                errors.appendLine("Invalid set method : leftReference {} rightReference {} info {}",
                                  leftreference,
                                  rightreference,
                                  info);
                return false;

            }

            @Override
            public Object invalidMethod(Context context, String reference, Object object, String method, Info info) {
                // This mean quiet references dont trigger errors - this is used
                // for things like null checks
                if (!reference.startsWith("$!")) {
                    errors.appendLine("Invalid method : reference '{}' object '{}' method '{}' info [{}]",
                                      reference,
                                      object,
                                      method,
                                      info);
                }
                return null;
            }
        });

        ListBackedMap<String, TransactionResultModel> results = null; // = model.getTotalTransactionModelsByTestName();
        rawResultsController.generateStats();

        // Replaced velocity tools with this to save a bit of jar space
        VelocityUtils utils = new VelocityUtils();

        // Populate the context
        final VelocityContext context = new VelocityContext();
        context.attachEventCartridge(eventCartridge);

        context.put("model", model);
        context.put("agents", model.getAgentModels());
        context.put("phases", model.getPhaseModels());
        context.put("resultsController", rawResultsController);
        context.put("results", results);
        context.put("number", utils);
        context.put("math", utils);
        context.put("utils", utils);

        final File reportsFolder = getReportsFolder();

        reportsFolder.mkdirs();

        // Do the json report first
        final List<CapturedStatistic> capturedStatistics = new ArrayList<CapturedStatistic>();
        //        capturedStatisticsHelper.visitStreamingFile(new Destination<CapturedStatistic>() {
        //            @Override public void send(CapturedStatistic statistic) {
        //                capturedStatistics.add(statistic);
        //            }
        //        });

        outputJSONResults(results, capturedStatistics);

        // Create each of the pages
        File output = new File(reportsFolder, "output.html");
        process(ve, context, "/velocity/index.vm.html", output);
        process(ve, context, "/velocity/csv.vm", new File(reportsFolder, "output.csv"));

        // New approach (not using velocity) for the captured statistics
        File statisticsCaptureOutput = new File(reportsFolder, "capture.html");
        createStatisticsCaptureFile(statisticsCaptureOutput, rawResultsController);

        ExecutorService pool = Executors.newFixedThreadPool(1);

        // Queue up the per test pages and chart generator
        Collection<TransactionResultModel> values = results.values();
        for (final TransactionResultModel transactionResultModel : values) {
            context.put("test", transactionResultModel);
            final File testoutput = new File(reportsFolder, transactionResultModel.getKey() + ".html");

            process(ve, context, "/velocity/testdetails.vm", testoutput);

            if (queueCharts) {
                pool.execute(new Runnable() {
                    public void run() {
                        saveFrequencyChart(transactionResultModel, rawResultsController, reportsFolder);
                    }
                });
            } else {
                saveFrequencyChart(transactionResultModel, rawResultsController, reportsFolder);
            }
        }

        // Copy the css file over
        File cssFile = new File(reportsFolder, "report.css");
        FileUtils.copy(ResourceUtils.openStream("velocity/report.css"), cssFile);

        if (openInBrowser) {
            BrowserUtils.browseTo(output.toURI());
        }

        if (errors.toString().length() > 0) {
            logger.warn(errors.toString());
        }

        return errors.toString();
    }

    private File getReportsFolder() {
        return new File(configuration.getReportsFolder());
    }

    private void createStatisticsCaptureFile(File statisticsCaptureOutput, RawResultsController rawResultsController) {
        HTMLBuilder2 builder = new HTMLBuilder2();

        HTMLBuilder2.Element div = builder.getBody().div();
        final HTMLBuilder2.TableElement table = div.createTable();

        //        capturedStatisticsHelper.visitStreamingFile(new Destination<CapturedStatistic>() {
        //            @Override public void send(CapturedStatistic statistic) {
        //                HTMLBuilder2.RowElement row = table.createRow();
        //                row.cell(Logger.toDateString(statistic.getTime()).toString());
        //                row.cell(statistic.getPath());
        //                row.cell(statistic.getValue());
        //            }
        //        });

        builder.toFile(statisticsCaptureOutput);
    }

    public void outputJSONResults(ListBackedMap<String, TransactionResultModel> results, List<CapturedStatistic>
            capturedStatistics) {
        RunResult result = new RunResult();
        result.setConfigurationName(model.getTestName().get());
        result.setStartTime(model.getTestStartTime().get());

        // TODO : refactor fix me
        //        result.setTestResultsFromModel(results.getMap());
        result.setFailureReason(model.getFailureReason());
        //        result.setCapturedStatistics(capturedStatistics);

        outputJSONResults(result);
    }

    public void outputJSONResults(RunResult result) {

        Gson gson = new Gson();
        String json = gson.toJson(result);
        File jsonResultFile = getJSONResultsFile(getReportsFolder(),
                                                 model.getTestName().get(),
                                                 model.getTestStartTime().get());
        FileUtils.write(json, jsonResultFile);
        logger.info("JSON results written to '{}'", jsonResultFile.getAbsolutePath());

        if (configuration.getResultRepositoryHost() != null) {

        }

    }

    public static File getJSONResultsFile(File path, String testName, long testTime) {
        File jsonResultFile = new File(path,
                                       testName + "." + TimeUtils.toFileSafeOrderedNoMillis(testTime) + ".results" +
                                               ".json");
        return jsonResultFile;
    }

    protected void saveFrequencyChart(TransactionResultModel transactionResultModel, RawResultsController
            rawResultsController, File reportsPath) {

        File file = new File(reportsPath, transactionResultModel.getKey() + ".frequency.png");
        FrequencyChart chart = new FrequencyChart();

        SinglePassStatisticsLongPrecisionCircular singlePassStatisticsLongPrecision = rawResultsController.getSuccessStatsByTest()
                                                                                                          .get(transactionResultModel
                                                                                                                       .getKey());
        SinglePassStatisticsLongPrecisionCircular copy = singlePassStatisticsLongPrecision.copy();
        chart.generateChart(transactionResultModel, copy, file);

        // Throw the stats away to free up some memory
        singlePassStatisticsLongPrecision.clear();
    }

    private void process(VelocityEngine ve, VelocityContext context, String template, File output) {
        try {
            FileWriter writer = new FileWriter(output);
            ve.mergeTemplate(template, "UTF-8", context, writer);
            writer.close();
            logger.info("Report written to " + output.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetStats() {
        logger.info("Reseting stats...");
        rawResultsController.resetStats();
        model.resetStats();

        // Reset the phase start time
        model.setPhaseStartTime(timeProvider.getTime());
    }

    //    public List<TestInstruction> getTestInstructionsList() {
    //        List<TestInstruction> instructions = new ArrayList<TestInstruction>();
    //        populateTestInstructionsList(instructions, model.getTestModels());
    //        return instructions;
    //    }

    //    public void setReportsPath(File reportsPath) {
    //        this.reportsPath = reportsPath;
    //    }

    //    public int getAutostartAgents() {
    //        return autostartAgents;
    //    }
    //
    //    public void setAutostartAgents(int autostartAgents) {
    //        this.autostartAgents = autostartAgents;
    //    }

    public RawResultsController getRawResultsController() {
        return rawResultsController;
    }

    public void startStats() {
        if (configuration.isOutputControllerStats()) {
            statBundle.startPerSecond(logger);
        }
    }

    public void stopStats() {
        statBundle.stop();
    }

    public static void event(String string) {
        eventStream.send(string);
    }

    public static Stream<String> getEventStream() {
        return eventStream;
    }

    public void waitForEmbeddedIfNeeded() {
        List<AgentModel> agentModels = model.getAgentModels();
        for (final AgentModel agentModel : agentModels) {
            if (agentModel.getName().get().startsWith(AgentConfiguration.embeddedName)) {
                ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return agentModel.getConnected().get();
                    }
                });
            }
        }
    }

    public void startStatisticsCapture() {
        List<StatisticProvider> statisticsProviders = model.getStatisticsProviders();
        for (StatisticProvider statisticsProvider : statisticsProviders) {
            statisticsProvider.start();
        }
    }

    public void stopStatisticsCapture() {
        capturedStatisticsHelper.closeStreamingFiles();
        List<StatisticProvider> statisticsProviders = model.getStatisticsProviders();
        for (StatisticProvider statisticsProvider : statisticsProviders) {
            statisticsProvider.stop();
        }
    }

    public synchronized void endTestNormally() {

        if (state != State.TestRunning) {
            throw new IllegalStateException(StringUtils.format(
                    "The controller state is '{}' - you can't end a test normally from there",
                    state));
        }

        regularStop();

        logger.info("Test has been stopped normally");
        state = State.Completed;

        model.getCurrentPhase().set(null);
    }

    private void decoupleFromAgentUpdates() {
        for (ReflectionDispatchMessageListener reflectionDispatchMessageListener : dispatchingListeners.keySet()) {
            KryoClient client = dispatchingListeners.get(reflectionDispatchMessageListener);
            client.removeMessageListener(reflectionDispatchMessageListener);
            reflectionDispatchMessageListener.stop();
        }
    }

    public synchronized void endTestAbnormally() {

        if (state != State.TestRunning) {
            throw new IllegalStateException(StringUtils.format(
                    "The controller state is '{}' - you can't end a test from there",
                    state));
        }

        regularStop();

        //        model.reset();

        logger.info("Test has been stopped abnormally");
        state = State.Stopped;
    }

    private void regularStop() {
        model.getTestRunning().set(false);

        decoupleFromAgentUpdates();
        sendAgentStopMessages();
        killAgentPingTimer();

        stopStatisticsCapture();
        capturedStatisticsHelper.closeStreamingFiles();
        rawResultsController.stopStatsUpdater();

        closeAgentConnections();
        killEmbeddedAgents();
        killMemoryMonitor();

    }

    private void killMemoryMonitor() {
        if (memoryMonitorWorkerThread != null) {
            memoryMonitorWorkerThread.stop();
            memoryMonitorWorkerThread = null;
        }
    }

    private void killEmbeddedAgents() {
        for (Agent2 embeddedAgent : embeddedAgents) {
            embeddedAgent.stop();
        }
    }

    private void killAgentPingTimer() {
        if (agentPingTimer != null) {
            agentPingTimer.cancel();
            agentPingTimer = null;
        }
    }

    private void sendAgentStopMessages() {// Send the stop messages
        for (final AgentModel agentModel : model.getAgentModels()) {
            KryoClient client = agentModel.getKryoClient();
            if (client != null) {
                client.sendRequest("agent", new StopTestRequest(), new ResponseHandler<StopTestResponse>() {
                    public void onResponse(StopTestResponse response) {
                        model.getTestRunning().set(false);
                    }
                });
            }
        }
    }

    private void sendPhaseStartInstruction(String phase) {

        final CountDownLatch receivedLatch = new CountDownLatch(model.getAgentModels().size());

        for (final AgentModel agentModel : model.getAgentModels()) {
            KryoClient client = agentModel.getKryoClient();
            if (client != null) {
                client.sendRequest("agent", new PhaseStartInstruction(phase), new ResponseHandler<String>() {
                    public void onResponse(String response) {
                        logger.info("Agent response to phase start instruction : " + response);
                        receivedLatch.countDown();
                    }
                });
            }
        }

        try {
            if (!receivedLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(String.format(
                        "One or more agent have not responded to the PhaseStartInstruction  after the timeout " +
                                "interval - aborting test"));
            } else {
                logger.info("All agents have confirmed receipt of PhaseStartInstruction");
            }
        } catch (InterruptedException e) {
            logger.warn(
                    "Thread interrupted waiting for confirmation that all agents have received their " +
                            "PhaseStartInstruction. We have no way of knowing the agent state from this point.");
        }

    }

    private void sendPhaseStopInstruction() {

        final CountDownLatch receivedLatch = new CountDownLatch(model.getAgentModels().size());

        for (final AgentModel agentModel : model.getAgentModels()) {
            KryoClient client = agentModel.getKryoClient();
            if (client != null) {
                client.sendRequest("agent", new PhaseStopInstruction(), new ResponseHandler<String>() {
                    public void onResponse(String response) {
                        logger.info("Agent response to phase stop instruction : " + response);
                        receivedLatch.countDown();
                    }
                });
            }
        }

        try {
            if (!receivedLatch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(String.format(
                        "One or more agent have not responded to the PhaseStopInstruction  after the timeout interval" +
                                " - aborting test"));
            } else {
                logger.info("All agents have confirmed receipt of PhaseStopInstruction");
            }
        } catch (InterruptedException e) {
            logger.warn(
                    "Thread interrupted waiting for confirmation that all agents have received their " +
                            "PhaseStopInstruction. We have no way of knowing the agent state from this point.");
        }

    }

    //    public String getCurrentPhaseName() {
    //        String phaseName;
    //        if (currentPhase != null) {
    //            phaseName = currentPhase.getPhaseName();
    //        } else {
    //            phaseName = null;
    //        }
    //
    //        return phaseName;
    //    }

    //    public void phaseComplete() {
    //
    //        logger.info("Phase '{}' complete, progressing test...", getCurrentPhaseName());
    //
    //        // TODO : not sure this should be moving to the next phase or just finishing the current phase
    //
    //        PhaseModel currentPhase = model.getCurrentPhase().get();
    //        if (currentPhase == null) {
    //            throw new IllegalStateException(StringUtils.format("Unable to complete phase, no phase has been
    // started"));
    //        }
    //
    //        if (hasNextPhase()) {
    //
    //            PhaseModel nextPhase = currentPhase = getNextPhase();
    //            model.getCurrentPhase().set(nextPhase);
    //
    //            logger.info("Starting new phase '{}'", getCurrentPhaseName());
    //
    //        } else {
    //            logger.info("All phases completed, ending test normally");
    //            endTestNormally();
    //        }
    //    }

    public String getCurrentPhaseName() {
        PhaseModel phaseModel = model.getCurrentPhase().get();
        if (phaseModel != null) {
            return phaseModel.getPhaseName().get();
        } else {
            return null;
        }
    }

    public JBombardierConfiguration getConfiguration() {
        return configuration;
    }

    public void forcePreviousPhase() {
        logger.info("Forcing previous phase...");
        forcePhaseEnd();
        if (hasPreviousPhase()) {
            startPreviousPhase();
        }
    }

    private void forcePhaseEnd() {
        logger.info("Forcing current phase end...");
        sendPhaseStopInstruction();
        resetStats();
    }

    public void stopPhase() {
        logger.info("Ending current phase...");
        model.setPhaseEndTime(timeProvider.getTime());
        stopPhaseControllers(model.getCurrentPhase().get());
        sendPhaseStopInstruction();

    }

    private void stopPhaseControllers(PhaseModel phaseModel) {
        List<PhaseController> phaseControllers = phaseModel.getPhaseControllers();
        for (PhaseController phaseController : phaseControllers) {
            phaseController.stop();
        }
    }

    public void forceNextPhase() {
        logger.info("Forcing next phase...");
        forcePhaseEnd();
        if (hasNextPhase()) {
            startNextPhase();
        }
    }

    private void startPreviousPhase() {

        PhaseModel previous;

        PhaseModel currentPhase = model.getCurrentPhase().get();
        if (currentPhase == null) {
            throw new IllegalStateException("No test running, can't go back to previous phase");
        } else {
            int index = model.getPhaseModels().indexOf(currentPhase);

            int previousIndex = index - 1;
            if (previousIndex < 0) {
                throw new IllegalStateException("You are already at the first phase - cannot go back");
            } else {
                previous = model.getPhaseModels().get(previousIndex);
            }
        }

        startPhase(previous);
    }

    public PhaseModel getNextPhase() {
        PhaseModel nextPhase;

        PhaseModel currentPhase = model.getCurrentPhase().get();
        if (currentPhase == null) {
            nextPhase = model.getPhaseModels().get(0);
        } else {
            int index = model.getPhaseModels().indexOf(currentPhase);

            int next = index + 1;
            if (next == model.getPhaseModels().size()) {
                throw new IllegalStateException("You are already at the last phase!");
            } else {
                nextPhase = model.getPhaseModels().get(next);
            }
        }

        return nextPhase;
    }

    public void startNextPhase() {
        rawResultsController.resetStats();

        PhaseModel nextPhase = getNextPhase();
        startPhase(nextPhase);
    }

    private void startPhase(PhaseModel nextPhase) {

        establishPhaseState(nextPhase);

        nextPhase.getPhaseRemainingTime().set(nextPhase.getPhaseDuration().get());
        model.getCurrentPhase().set(nextPhase);
        logger.info("Starting phase '{}'", getCurrentPhaseName());

        sendPhaseStartInstruction(getCurrentPhaseName());
        model.setPhaseStartTime(timeProvider.getTime());

        startPhaseControllers(nextPhase);
    }

    private void startPhaseControllers(PhaseModel nextPhase) {
        List<PhaseController> phaseControllers = nextPhase.getPhaseControllers();
        for (PhaseController phaseController : phaseControllers) {
            phaseController.start(this);
        }
    }

    private void establishPhaseState(PhaseModel nextPhase) {
        List<StateEstablisherConfiguration> stateEstablishers = nextPhase.getStateEstablishers();
        runStateEstablishers(stateEstablishers);
    }

    public boolean hasPreviousPhase() {
        boolean hasPreviousPhase;

        PhaseModel currentPhase = model.getCurrentPhase().get();
        if (currentPhase == null) {
            hasPreviousPhase = false;
        } else {
            int index = model.getPhaseModels().indexOf(currentPhase);

            int previous = index - 1;
            if (previous >= 0) {
                hasPreviousPhase = true;
            } else {
                hasPreviousPhase = false;
            }
        }

        return hasPreviousPhase;
    }

    public boolean hasNextPhase() {

        boolean hasNextPhase;

        PhaseModel currentPhase = model.getCurrentPhase().get();
        if (currentPhase == null) {
            hasNextPhase = true;
        } else {
            int index = model.getPhaseModels().indexOf(currentPhase);

            int next = index + 1;
            if (next == model.getPhaseModels().size()) {
                hasNextPhase = false;
            } else {
                hasNextPhase = true;
            }
        }

        return hasNextPhase;
    }

    public void startTest() {
        logger.info("Starting test...");
        state = State.TestRunning;

        resetState();
        //        initialiseResultsStreaming();
        publishTestInstructions();
        startNextPhase();
    }

    private void resetState() {
        rawResultsController.resetStats();
    }

    //    private void initialiseResultsStreaming() {
    //        try {
    //            capturedStatisticsHelper.openStreamingFiles();
    //        } catch (IOException e) {
    //            throw new RuntimeException("Failed to open streaming files", e);
    //        }
    //    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }


    public void doPostTestTasks() {

        RunResultBuilder runResultBuilder = new RunResultBuilder();
        RunResult snapshot = runResultBuilder.createSnapshot(model, capturedStatisticsHelper, rawResultsController);

        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting().create();
        String json = gson.toJson(snapshot);

        writeJSONResults(json);

        generateReport(snapshot);

        if (configuration.isOpenReport()) {
            BrowserUtils.browseTo(new File(getReportsFolder(), "index.html").toURI());
        }

        if (StringUtils.isNotNullOrEmpty(configuration.getResultRepositoryHost())) {

            RepositoryMessagingClient client = new RepositoryMessagingClient();

            try {
                logger.info("Attempting to connect to the results repository on {}:{}",
                            configuration.getResultRepositoryHost(),
                            configuration.getResultRepositoryPort());
                client.connect(configuration.getResultRepositoryHost(), configuration.getResultRepositoryPort());
                client.postResult(json);
                logger.info("Sent results to remote repository at {}", configuration.getResultRepositoryHost());
            } catch (Exception e) {
                logger.warning(e,
                               "Failed to send results to remote repository at {}",
                               configuration.getResultRepositoryHost());
            } finally {
                client.close();
            }
        }

    }

    private void writeJSONResults(String json) {
        File jsonResultFile = getJSONResultsFile(getReportsFolder(),
                                                 model.getTestName().get(),
                                                 model.getTestStartTime().get());
        FileUtils.write(json, jsonResultFile);
        logger.info("JSON results written to '{}'", jsonResultFile.getAbsolutePath());
    }

}

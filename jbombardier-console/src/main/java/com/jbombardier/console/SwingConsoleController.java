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

package com.jbombardier.console;

import com.jbombardier.common.KryoHelper;
import com.jbombardier.console.charts.FrequencyChart;
import com.jbombardier.console.configuration.Agent;
import com.jbombardier.console.configuration.HubCapture;
import com.jbombardier.console.configuration.HubCapturePattern;
import com.jbombardier.console.configuration.InteractiveConfiguration;
import com.jbombardier.console.configuration.JmxCapture;
import com.jbombardier.console.configuration.JmxTarget;
import com.jbombardier.console.configuration.Property;
import com.jbombardier.console.configuration.StatisticsCapture;
import com.jbombardier.console.configuration.TestConfiguration;
import com.jbombardier.console.model.AgentModel;
import com.jbombardier.console.model.ConsoleEventModel;
import com.jbombardier.console.model.DataSource;
import com.jbombardier.console.model.JSONHelper;
import com.jbombardier.console.model.TestModel;
import com.jbombardier.console.model.TransactionResultModel;
import com.jbombardier.console.model.result.TestRunResult;
import com.jbombardier.console.statisticcapture.JMXStatisticCapture;
import com.jbombardier.console.statisticcapture.LoggingHubStatisticCapture;
import com.logginghub.messaging2.ReflectionDispatchMessageListener;
import com.logginghub.messaging2.api.ConnectionListener;
import com.logginghub.messaging2.kryo.KryoClient;
import com.logginghub.messaging2.kryo.ResponseHandler;
import com.jbombardier.agent.Agent2;
import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.xml.CsvProperty;
import com.logginghub.utils.*;
import com.logginghub.utils.MemorySnapshot.LowMemoryNotificationHandler;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.remote.ClasspathResolver;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.InvalidReferenceEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.util.introspection.Info;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SwingConsoleController {

    private static final Logger logger = Logger.getLoggerFor(SwingConsoleController.class);
    // TODO : figure out a nicer way of doing this!!
    private static Stream<String> eventStream = new Stream<String>();
    // private InteractiveConfiguration configuration;
    private ConsoleModel model;
    private Map<String, String> properties = new ConcurrentHashMap<String, String>();
    private Map<String, com.jbombardier.common.CsvPropertiesProvider> csvPropertyProviders = new HashMap<String, com.jbombardier.common.CsvPropertiesProvider>();
    private Map<ReflectionDispatchMessageListener, KryoClient> dispatchingListeners = new HashMap<ReflectionDispatchMessageListener, KryoClient>();
    private Map<String, DataSource> dataByName = new HashMap<String, DataSource>();
    private ResultsController resultsController;
    private ExecutorService pool = Executors.newCachedThreadPool();
    private int autostartAgents;
    private File reportsPath = new File("reports");
    private StatBundle statBundle = new StatBundle();
    private boolean sendCloseMessageOnWindowClose = true;
    private Timer agentPingTimer;
    private InteractiveConfiguration configuration;
    private IntegerStat newConnectionsStat;
    private IntegerStat disconnectionsStat;
    private IntegerStat connectionsStat;
    private IntegerStat agentStatusUpdatesStat;
    private IntegerStat classloaderRequestsStat;
    private IntegerStat expectedAgentsStat;
    private WorkerThread memoryMonitorWorkerThread;
    private ClasspathResolver resolver = new ClasspathResolver();
    private boolean outputControllerStats = !EnvironmentProperties.getBoolean("jbombardierConsoleController.disableStats");

    public synchronized void startTest() {

        if (model.isTestRunning()) {
            throw new IllegalStateException("Test has already been started");
        } else {
            model.getTestRunning().set(true);
        }
        logger.debug("Starting test...");

        resultsController.resetStats();
        try {
            resultsController.openStreamingFiles();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open streaming files", e);
        }

        startStatisticsCapture();

        List<AgentModel> connectedAgentModels = getConnectedAgents();
        logger.debug("We have {} connected agents", connectedAgentModels.size());

        List<com.jbombardier.common.TestInstruction> instructions = new ArrayList<com.jbombardier.common.TestInstruction>();
        List<TestModel> tests = model.getTestModels();

        FactoryMap<String, FactoryMap<String, com.jbombardier.common.DataBucket>> dataBucketsByAgentName = divideDataIntoBuckets(
                connectedAgentModels);

        populateTestInstructionsList(instructions, tests);
        publishTestInstructionsToAgents(instructions, dataBucketsByAgentName);
        logger.info("Test has started...");

        model.setTestStartTime(System.currentTimeMillis());

        agentPingTimer = TimerUtils.everySecond("Agent ping timer", new Runnable() {
            @Override public void run() {
                sendPings();
            }
        });

        resultsController.startStatsUpdater(model);
    }

    protected void sendPings() {
        logger.debug("Sending pings to all agents...");
        for (final AgentModel agentModel : model.getAgentModels()) {
            if (agentModel.isConnected()) {
                KryoClient client = agentModel.getKryoClient();
                client.send("agent", new com.jbombardier.common.PingMessage());
            }
        }
    }

    public ConsoleModel getModel() {
        return model;
    }

    public void setModel(final ConsoleModel model) {
        this.model = model;
    }

    @SuppressWarnings("serial")
    public FactoryMap<String, FactoryMap<String, com.jbombardier.common.DataBucket>> divideDataIntoBuckets(List<AgentModel> connectedAgentModels) {

        Is.greaterThanZero(connectedAgentModels.size(), "You must have at least one connected agent to start the test");

        FactoryMap<String, FactoryMap<String, com.jbombardier.common.DataBucket>> dataBuckets = new FactoryMap<String, FactoryMap<String, com.jbombardier.common.DataBucket>>() {
            @Override protected FactoryMap<String, com.jbombardier.common.DataBucket> createEmptyValue(String key) {
                return new FactoryMap<String, com.jbombardier.common.DataBucket>() {
                    @Override protected com.jbombardier.common.DataBucket createEmptyValue(String key) {
                        return new com.jbombardier.common.DataBucket(key);
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
            com.jbombardier.common.DataStrategy strategy = data.getStrategy();
            switch (strategy) {
                case fixedThread: {
                    // We dont distribute anything for this strategy, each
                    // agent/thread has to make a unique request and the server
                    // will dish them out.
                    for (AgentModel agent : connectedAgentModels) {
                        String agentName = agent.getName();
                        FactoryMap<String, com.jbombardier.common.DataBucket> dataSourceBucketsForAgent = dataBuckets.get(agentName);
                        com.jbombardier.common.DataBucket dataBucket = dataSourceBucketsForAgent.get(dataSourceName);

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

                    com.jbombardier.common.DataBucket[] buckets = new com.jbombardier.common.DataBucket[agents];
                    for (int i = 0; i < agents; i++) {
                        buckets[i] = new com.jbombardier.common.DataBucket(dataSourceName);
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
                        String agentName = agent.getName();
                        dataBuckets.get(agentName).put(dataSourceName, buckets[i]);
                        i++;
                    }

                    break;
                case pooledGlobal:

                    for (AgentModel agent : connectedAgentModels) {
                        String agentName = agent.getName();
                        FactoryMap<String, com.jbombardier.common.DataBucket> dataSourceBucketsForAgent = dataBuckets.get(agentName);
                        com.jbombardier.common.DataBucket dataBucket = dataSourceBucketsForAgent.get(dataSourceName);

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

    private List<AgentModel> getConnectedAgents() {

        List<AgentModel> connectedModels = new LinkedList<AgentModel>();
        for (AgentModel agentModel : model.getAgentModels()) {
            if (agentModel.isConnected()) {
                connectedModels.add(agentModel);
            }
        }

        return connectedModels;

    }

    public void handleAgentFailedInstruction(com.jbombardier.common.AgentFailedInstruction afi) {
        stopTest(true);
        model.abandonTest(
                "One of the agents reported an exception during setup; please check the console tab to find out what went wrong",
                afi);
    }

    public void handleAgentLogMessage(com.jbombardier.common.AgentLogMessage agentLogMessage) {
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

    public com.jbombardier.common.AgentPropertyResponse handleAgentPropertyRequest(com.jbombardier.common.AgentPropertyRequest agentPropertyRequest) {
        // TODO : support the property strategies to out different values to
        // different agent/thread combos.
        com.jbombardier.common.AgentPropertyResponse response;
        String propertyValue;
        synchronized (properties) {
            propertyValue = properties.get(agentPropertyRequest.getPropertyName());
        }
        // TODO : why are we repeating all the values in the reponse? The
        // request/response mapping code should ensure we dont have to do that
        // anymore!
        response = new com.jbombardier.common.AgentPropertyResponse(agentPropertyRequest.getPropertyName(),
                                             agentPropertyRequest.getThreadName(),
                                             propertyValue);
        return response;
    }

    public void handleAgentStatusUpdate(com.jbombardier.common.AgentStats agentStats) {
        agentStatusUpdatesStat.increment();
        model.onAgentStatusUpdate(agentStats);
        resultsController.handleAgentStatusUpdate(agentStats);
    }

    public void handleThreadsChangedMessage(com.jbombardier.common.ThreadsChangedMessage message) {
        model.incrementActiveThreadCount(message.getAgent(), message.getThreads());
    }

    public com.jbombardier.common.AgentPropertyEntryResponse handleAgentPropertyEntryRequest(com.jbombardier.common.AgentPropertyEntryRequest request) {
        com.jbombardier.common.AgentPropertyEntryResponse response;

        String propertyName = request.getPropertyName();
        com.jbombardier.common.CsvPropertiesProvider csvPropertiesProvider = csvPropertyProviders.get(propertyName);
        if (csvPropertiesProvider != null) {
            // TODO : work out why we have to provide a string there in this
            // case.
            com.jbombardier.common.PropertyEntry propertyEntry = csvPropertiesProvider.getPropertyEntry("dontmatteratthisbit");
            response = new com.jbombardier.common.AgentPropertyEntryResponse(request.getPropertyName(),
                                                      request.getThreadName(),
                                                      propertyEntry);
        } else {
            response = new com.jbombardier.common.AgentPropertyEntryResponse(request.getPropertyName(), request.getThreadName(), null);
        }

        return response;
    }

    private void assertTestClassesAreValid(List<TestModel> tests) {

        Set<String> alreadyChecked = new HashSet<String>();

        StringBuilder buffer = new StringBuilder();
        for (TestModel testModel : tests) {
            String classname = testModel.getClassname();

            if (alreadyChecked.contains(classname)) {
                // Skip it
            } else {

                logger.debug("Validing test class {}", classname);
                try {
                    @SuppressWarnings("unused") com.jbombardier.common.PerformanceTest performanceTest = (com.jbombardier.common.PerformanceTest) Class.forName(
                            classname).newInstance();
                } catch (ClassNotFoundException e) {
                    buffer.append("Class '")
                          .append(classname)
                          .append("' could not be found, please check your configuration!\n");
                } catch (InstantiationException e) {
                    buffer.append("Class '")
                          .append(classname)
                          .append("' could not be instantiated, please ensure it has a default constructor. Any arguments should be setup using the properties methods on the TestContext class before the test starts.\n");
                } catch (IllegalAccessException e) {
                    buffer.append("Class '")
                          .append(classname)
                          .append("' could not be instantiated, please check to make sure the default constructor is public.\n");
                } catch (ClassCastException e) {
                    buffer.append("Class '")
                          .append(classname)
                          .append("' does't implement the PerformanceTest interface. All tests must implement this interface, please check your test code.\n");
                }

                alreadyChecked.add(classname);
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
            client.sendRequest("agent", new com.jbombardier.common.StopTelemetryRequest(), new ResponseHandler<String>() {
                public void onResponse(String response) {
                }
            });
        }

    }

    public synchronized void stopTest(boolean reseltModel) {

        if (model.isTestRunning()) {
            model.getTestRunning().set(false);

            // Decouple from agent updates
            for (ReflectionDispatchMessageListener reflectionDispatchMessageListener : dispatchingListeners.keySet()) {
                KryoClient client = dispatchingListeners.get(reflectionDispatchMessageListener);
                client.removeMessageListener(reflectionDispatchMessageListener);
            }

            // Send the stop messages
            for (final AgentModel agentModel : model.getAgentModels()) {
                KryoClient client = agentModel.getKryoClient();
                if (client != null) {
                    client.sendRequest("agent", new com.jbombardier.common.StopTestRequest(), new ResponseHandler<com.jbombardier.common.StopTestResponse>() {
                        public void onResponse(com.jbombardier.common.StopTestResponse response) {
                            model.getTestRunning().set(false);
                        }
                    });
                }
            }

            if (agentPingTimer != null) {
                agentPingTimer.cancel();
                agentPingTimer = null;
            }

            stopStatisticsCapture();
            resultsController.closeStreamingFiles();
            resultsController.stopStatsUpdater();

            if (reseltModel) {
                model.reset();
            }
            logger.info("Test has been stopped");
        }
    }

    public boolean isSendCloseMessageOnWindowClose() {
        return sendCloseMessageOnWindowClose;
    }

    public void killAgents() {

        for (final AgentModel agentModel : model.getAgentModels()) {
            KryoClient client = agentModel.getKryoClient();
            if (client.isConnected()) {
                logger.info("Sending kill message to agent {}", client);
                client.send("agent", new com.jbombardier.common.AgentKillInstruction(2));
            }
        }

    }

    public void killAgentsAndReset() {
        killAgents();
        stopTest(true);
    }

    /**
     * Load everything, create agent connections etc
     */
    public void initialise(InteractiveConfiguration configuration, final ConsoleModel model) {
        this.configuration = configuration;

        resultsController = new ResultsController(new File(configuration.getReportsFolder()));

        this.outputControllerStats = configuration.isOutputControllerStats();

        memoryMonitorWorkerThread = MemorySnapshot.runMonitorToLogging(90, new LowMemoryNotificationHandler() {
            @Override public void onLowMemory(float percentage, int consecutive) {
                if (consecutive >= 2) {
                    handleLowMemory();
                }
            }
        });

        initialiseStats();

        logger.debug("Initialising controller");
        setModel(model);
        logger.info("Initialising agents...");
        initialiseAgents(configuration, model);
        logger.info("Initialising tests...");
        initialiseTests(configuration, model);
        logger.info("Initialising properties...");
        initialiseProperties(configuration, model);
        logger.info("Initialising statistics capture...");
        initialiseStatisticsCapture(configuration, model);


        model.setNoResultsTimeout((int) (configuration.getNoResultsTimeout() / 1000));
        model.setTestName(configuration.getTestName());
        model.setFailedTransactionCountFailureThreshold(configuration.getFailedTransactionCountFailureThreshold());
        model.setMaximumConsoleEntries(configuration.getMaximumConsoleEntries());

        autostartAgents = configuration.getAutostartAgents();
        sendCloseMessageOnWindowClose = configuration.isSendKillOnConsoleClose();
        resultsController.setMaximumResultsPerKey(configuration.getMaximumResultToStore());

        // Attach a listener to pickup changes to the transaction rate modifier
        // changer
        model.addPropertyChangeListener("transactionRateModifier", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateTransactionRateModifier(model.getTransactionRateModifier());
            }
        });

        // Fire up the embedded telemetry hub
        //        if (configuration.getTelemetryHubPort() != -1) {
        //            TelemetryHub hub = new TelemetryHub();
        //            hub.start(configuration.getTelemetryHubPort());
        //        }

        // Connect a telemetry listener to our built in telemetry hub - we can
        // ask the agents to connect to this once they connect
        //        TelemetryListener listener = new TelemetryListener();
        //        listener.start("localhost", configuration.getTelemetryHubPort(), new TelemetryInterface() {
        //            @Override public void publishTelemetry(DataStructure dataStructure) {
        //                model.update(dataStructure);
        //            }
        //        });

        // If the configuration wants us to, we can connect out to another
        // telemetry hub to pull in stats for other machines and processes as
        // well
        //        String externalTelemetry = configuration.getExternalTelemetryHub();
        //        if (externalTelemetry != null) {
        //            listener = new TelemetryListener();
        //            listener.start(externalTelemetry, new TelemetryInterface() {
        //                @Override public void publishTelemetry(DataStructure dataStructure) {
        //                    model.update(dataStructure);
        //                }
        //            });
        //        }

        logger.info("Initialision complete.");

    }

    private void initialiseStatisticsCapture(InteractiveConfiguration configuration, ConsoleModel model) {

        List<StatisticsCapture> statisticsCapture = configuration.getStatisticsCapture();
        for (StatisticsCapture capture : statisticsCapture) {
            String className = capture.getClassName();
            com.jbombardier.common.StatisticProvider capturePlugin = ReflectionUtils.newInstance(className);
            Metadata properties = Metadata.fromProperties(capture.getProperties());
            capturePlugin.configure(properties);

            capturePlugin.addDestination(new Destination<CapturedStatistic>() {
                @Override public void send(CapturedStatistic statistic) {
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
                @Override public void send(CapturedStatistic statistic) {
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
                @Override public void send(CapturedStatistic statistic) {
                    handleCapturedStatistic(statistic);
                }
            });
            model.getStatisticsProviders().add(jmxStatisticCapture);
        }

    }

    private void handleCapturedStatistic(CapturedStatistic statistic) {
        resultsController.addCapturedStatistic(statistic);
    }

    protected void handleLowMemory() {
        resultsController.reduceResultStorage();
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

    public void initialiseProperties(InteractiveConfiguration configuration, ConsoleModel model) {
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
                com.jbombardier.common.CsvPropertiesProvider provider = new com.jbombardier.common.CsvPropertiesProvider(csvfile);
                csvPropertyProviders.put(csvProperty.getName(), provider);

                logger.info("Loading {} data from {}", csvProperty.getName(), csvfile);

                // Also process the config information into DataSources
                DataSource dataSource = new DataSource(csvProperty.getName(),
                                                       com.jbombardier.common.DataStrategy.valueOf(csvProperty.getStrategy()));

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

    private void initialiseTests(InteractiveConfiguration configuration, ConsoleModel model) {
        List<TestConfiguration> tests = configuration.getTests();
        for (TestConfiguration testConfiguration : tests) {

            TestModel testModel = new TestModel();
            testModel.setClassname(testConfiguration.getClassname());
            testModel.setRateStep(testConfiguration.getRateStep());
            testModel.setName(testConfiguration.getName());
            testModel.setRateStepTime(testConfiguration.getRateStepTime());
            testModel.setTargetRate(testConfiguration.getTargetRate());
            testModel.setTargetThreads(testConfiguration.getTargetThreads());
            testModel.setThreadStep(testConfiguration.getThreadStep());
            testModel.setThreadStepTime(testConfiguration.getThreadStepTime());
            testModel.setRecordAllValues(testConfiguration.getRecordAllValues());
            testModel.setProperties(testConfiguration.buildPropertyMap());
            testModel.setTransactionSLAs(testConfiguration.buildSLAMap());
            testModel.setFailureThreshold(testConfiguration.getFailureThreshold());
            testModel.setFailureThresholdMode(testConfiguration.getFailureThresholdMode());
            testModel.setFailedTransactionCountThreshold(testConfiguration.getFailedTransactionCountFailureThreshold());
            testModel.setFailureThresholdResultCountMinimum(testConfiguration.getFailureThresholdResultCountMinimum());
            testModel.setMovingAveragePoints(testConfiguration.getMovingAveragePoints());

            model.addTestModel(testModel);
        }

        model.setTransactionRateModifier(configuration.getTransactionRateModifier());

        assertTestClassesAreValid(model.getTestModels());
    }

    private void initialiseAgents(final InteractiveConfiguration configuration, ConsoleModel model) {
        List<Agent> agents = configuration.getAgents();
        for (Agent agent : agents) {

            int objectBufferSize = agent.getObjectBufferSize();
            int writeBufferSize = agent.getWriteBufferSize();
            final KryoClient client = new KryoClient("controller", writeBufferSize, objectBufferSize);

            KryoHelper.registerTypes(client.getKryo());

            final AgentModel agentModel = new AgentModel();

            agentModel.setConnected(false);
            agentModel.setName(agent.getName());

            if (agent.getName().equals(Agent.embeddedName)) {
                Agent2 embeddedAgent = new Agent2();
                int freePort = NetUtils.findFreePort();
                embeddedAgent.disableSystemExitOnKill();
                embeddedAgent.setOutputStats(configuration.isOutputEmbeddedAgentStats());
                embeddedAgent.setWriteBufferSize(agent.getWriteBufferSize());
                embeddedAgent.setObjectBufferSize(agent.getObjectBufferSize());
                embeddedAgent.setBindPort(freePort);
                embeddedAgent.setPingTimeout(agent.getPingTimeout());
                embeddedAgent.start();
                agentModel.setAddress("localhost");
                agentModel.setPort(freePort);
            } else {
                agentModel.setAddress(agent.getAddress());
                agentModel.setPort(agent.getPort());
            }

            client.addConnectionPoint(new InetSocketAddress(agentModel.getAddress(), agentModel.getPort()));
            agentModel.setKryoClient(client);

            model.addAgentModel(agentModel);

            client.addConnectionListener(new ConnectionListener() {
                public void onDisconnected() {
                    logger.info("Agent has disconnected {}", agentModel);
                    disconnectionsStat.increment();
                    connectionsStat.decrement();
                    agentModel.setConnected(false);
                }

                public void onConnected() {
                    connectionsStat.increment();
                    newConnectionsStat.increment();
                    logger.info("Agent has connected {}", agentModel);
                    agentModel.setConnected(true);

                    client.sendRequest("agent",
                                       new com.jbombardier.common.SendTelemetryRequest(NetUtils.getLocalIP(),
                                                                configuration.getTelemetryHubPort()),
                                       new ResponseHandler<String>() {
                                           public void onResponse(String response) {

                                           }
                                       });

                    attachReflectionDispatcher(client);
                    checkForAutostart();
                }
            });

            client.startBackground();
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

        model.log(ConsoleEventModel.Severity.Information, "%d agents added", agents.size());
    }

    public Map<String, AgentModel> getAgentsByAgentName() {
        return model.getAgentsByAgentName();
    }

    protected synchronized void checkForAutostart() {
        if (!model.isTestRunning()) {
            if (autostartAgents > 0 && getConnectedAgents().size() >= autostartAgents) {
                startTest();
            }
        }
    }

    public com.jbombardier.common.AgentClassResponse handleClassRequest(com.jbombardier.common.AgentClassRequest acr) {
        logger.debug("Agent class request : {}", acr);
        classloaderRequestsStat.increment();

        byte[] data;
        if (acr.isClassNotResource()) {
            data = resolver.getClassBytes(acr.getClassName());
        } else {
            data = resolver.getResourceBytes(acr.getClassName());
        }

        com.jbombardier.common.AgentClassResponse agentClassResponse = new com.jbombardier.common.AgentClassResponse();
        agentClassResponse.setData(data);

        logger.debug("Sent {} bytes back to agent for class {}", data.length, acr.getClassName());

        return agentClassResponse;
    }

    protected void updateTransactionRateModifier(double transactionRateModifier) {
        com.jbombardier.common.TestVariableUpdateRequest request = new com.jbombardier.common.TestVariableUpdateRequest();
        request.setTestName("");
        request.setField(com.jbombardier.common.TestField.transactionRateModifier);
        request.setValue(transactionRateModifier);
        for (final AgentModel agentModel : model.getAgentModels()) {
            KryoClient client = agentModel.getKryoClient();
            client.sendRequest("agent", request, new ResponseHandler<String>() {
                public void onResponse(String response) {
                    logger.info("Transaction rate modifier successfully applied to client : '{}'", response);
                }
            });
        }
    }

    public void updateTestVariable(final String testName, final com.jbombardier.common.TestField field, final Object newValue) {

        com.jbombardier.common.TestVariableUpdateRequest request = new com.jbombardier.common.TestVariableUpdateRequest();
        request.setTestName(testName);
        request.setField(field);
        request.setValue(newValue);
        for (final AgentModel agentModel : model.getAgentModels()) {
            KryoClient client = agentModel.getKryoClient();
            client.sendRequest("agent", request, new ResponseHandler<String>() {
                public void onResponse(String response) {
                    List<TestModel> testModels = model.getTestModels();
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
                                    testModel.setTargetRate((Float) newValue);
                                    break;
                                case targetThreads:
                                    testModel.setTargetThreads((Integer) newValue);
                                    break;
                                case threadStep:
                                    testModel.setThreadStep((Integer) newValue);
                                    break;
                                case threadStepTime:
                                    testModel.setThreadStepTime((Long) newValue);
                                    break;
                            }
                        }
                    }

                }
            });
        }
    }

    private void publishTestInstructionsToAgents(List<com.jbombardier.common.TestInstruction> instructions,
                                                 FactoryMap<String, FactoryMap<String, com.jbombardier.common.DataBucket>> dataBucketsByAgentName) {
        int agentsInTest = 0;
        for (final AgentModel agentModel : model.getAgentModels()) {
            logger.info("Publishing data buckets to agent {}", agentModel);
            if (agentModel.isConnected()) {

                FactoryMap<String, com.jbombardier.common.DataBucket> data = dataBucketsByAgentName.get(agentModel.getName());
                logger.debug("This agent has a data bucket with {} keys and {} items", data.size(), countItems(data));

                KryoClient client = agentModel.getKryoClient();

                Set<String> keySet = data.keySet();

                // Send the buckets one at a time in case we blow the buffer
                // size
                for (String dataBucketName : keySet) {

                    final com.jbombardier.common.DataBucket dataBucket = data.get(dataBucketName);

                    logger.debug("Sending bucket {}", dataBucket);

                    final QuietLatch latch = new QuietLatch(1);
                    client.sendRequest("agent", dataBucket, new ResponseHandler<String>() {
                        public void onResponse(String response) {
                            logger.info("Agent {} has received data bucket {}", agentModel, dataBucket);
                            latch.countDown();
                        }
                    });

                    latch.await();
                }
            }
        }

        for (final AgentModel agentModel : model.getAgentModels()) {
            logger.debug("Publishing test instruction to agent {}", agentModel);
            KryoClient client = agentModel.getKryoClient();
            if (agentModel.isConnected()) {

                com.jbombardier.common.TestPackage testPackage = new com.jbombardier.common.TestPackage(agentModel.getName(), instructions);
                testPackage.setLoggingHubs(configuration.getLoggingHubs());
                testPackage.setLoggingType(configuration.getLoggingTypes());

                client.sendRequest("agent", testPackage, new ResponseHandler<String>() {
                    public void onResponse(String response) {
                        logger.debug("Agent response received from agent {}, test package received", agentModel);
                        agentModel.setPackageReceived(true);
                    }
                });

                logger.debug("Message sent to agent {}", agentModel);

                agentsInTest++;
            } else {
                logger.trace("Agent {} isn't connected, skipping", agentModel);
            }
        }

        expectedAgentsStat.setValue(agentsInTest);
        model.setAgentsInTest(agentsInTest);
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

    private int countItems(FactoryMap<String, com.jbombardier.common.DataBucket> data) {

        int count = 0;
        Set<String> keySet = data.keySet();
        for (String string : keySet) {
            com.jbombardier.common.DataBucket dataBucket = data.get(string);
            if (dataBucket.getStrategy() == com.jbombardier.common.DataStrategy.fixedThread) {
                // This strategy doesn't send anything...
            } else {
                count += dataBucket.getValues().size();
            }
        }

        return count;
    }

    private void populateTestInstructionsList(List<com.jbombardier.common.TestInstruction> instructions, List<TestModel> tests) {
        for (TestModel test : tests) {
            com.jbombardier.common.TestInstruction instruction = new com.jbombardier.common.TestInstruction();
            instruction.setTestName(test.getName());
            instruction.setClassname(test.getClassname());
            instruction.setTargetThreads(test.getTargetThreads());
            instruction.setThreadRampupStep(test.getThreadStep());
            instruction.setThreadRampupTime(test.getThreadStepTime());
            instruction.setTargetRate(test.getTargetRate());
            instruction.setRateStep(test.getRateStep());
            instruction.setRateStepTime(test.getRateStepTime());
            instruction.setRecordAllValues(test.getRecordAllValues());
            instruction.setTransactionRateModifier(model.getTransactionRateModifier());
            // Remember to add the default properties first, then the test
            // specific ones afterwards
            instruction.getProperties().putAll(properties);
            instruction.getProperties().putAll(test.getProperties());
            instructions.add(instruction);
        }
    }

    public void generateReportAsync() {
        pool.execute(new Runnable() {
            public void run() {
                generateReport(true);
            }
        });
    }

    public void generateReport(boolean openInBrowser) {
        generateReport(openInBrowser, true);
    }

    public String generateReport(boolean openInBrowser, boolean queueCharts) {

        resultsController.flush();

        final VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();

        final StringUtilsBuilder errors = new StringUtilsBuilder();
        EventCartridge eventCartridge = new EventCartridge();
        eventCartridge.addInvalidReferenceEventHandler(new InvalidReferenceEventHandler() {

            @Override public Object invalidGetMethod(Context context,
                                                     String reference,
                                                     Object object,
                                                     String property,
                                                     Info info) {
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

            @Override public boolean invalidSetMethod(Context context,
                                                      String leftreference,
                                                      String rightreference,
                                                      Info info) {
                errors.appendLine("Invalid set method : leftReference {} rightReference {} info {}",
                                  leftreference,
                                  rightreference,
                                  info);
                return false;

            }

            @Override public Object invalidMethod(Context context,
                                                  String reference,
                                                  Object object,
                                                  String method,
                                                  Info info) {
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

        List<TestModel> testModels = this.model.getTestModels();

        ListBackedMap<String, TransactionResultModel> results = model.getTotalTransactionModelsByTestName();
        resultsController.generateStats();

        // Replaced velocity tools with this to save a bit of jar space
        VelocityUtils utils = new VelocityUtils();

        // Populate the context
        final VelocityContext context = new VelocityContext();
        context.attachEventCartridge(eventCartridge);

        context.put("model", model);
        context.put("agents", model.getAgentModels());
        context.put("tests", testModels);
        context.put("resultsController", resultsController);
        context.put("results", results);
        context.put("number", utils);
        context.put("math", utils);
        context.put("utils", utils);

        reportsPath.mkdirs();

        // Do the json report first
        final List<CapturedStatistic> capturedStatistics = new ArrayList<CapturedStatistic>();
        resultsController.visitStreamingFile(new Destination<CapturedStatistic>() {
            @Override public void send(CapturedStatistic statistic) {
                capturedStatistics.add(statistic);
            }
        });

        outputJSONResults(results, capturedStatistics);

        // Create each of the pages
        File output = new File(reportsPath, "output.html");
        process(ve, context, "/velocity/index.vm", output);
        process(ve, context, "/velocity/csv.vm", new File(reportsPath, "output.csv"));

        // New approach (not using velocity) for the captured statistics
        File statisticsCaptureOutput = new File(reportsPath, "capture.html");
        createStatisticsCaptureFile(statisticsCaptureOutput, resultsController);

        ExecutorService pool = Executors.newFixedThreadPool(1);

        // Queue up the per test pages and chart generator
        Collection<TransactionResultModel> values = results.values();
        for (final TransactionResultModel transactionResultModel : values) {
            context.put("test", transactionResultModel);
            final File testoutput = new File(reportsPath, transactionResultModel.getKey() + ".html");

            process(ve, context, "/velocity/testdetails.vm", testoutput);

            if (queueCharts) {
                pool.execute(new Runnable() {
                    public void run() {
                        saveFrequencyChart(transactionResultModel, resultsController, reportsPath);
                    }
                });
            } else {
                saveFrequencyChart(transactionResultModel, resultsController, reportsPath);
            }
        }

        // Copy the css file over
        File cssFile = new File(reportsPath, "report.css");
        FileUtils.copy(ResourceUtils.openStream("velocity/report.css"), cssFile);

        if (openInBrowser) {
            BrowserUtils.browseTo(output.toURI());
        }

        if (errors.toString().length() > 0) {
            logger.warn(errors.toString());
        }

        return errors.toString();
    }

    private void createStatisticsCaptureFile(File statisticsCaptureOutput, ResultsController resultsController) {
        HTMLBuilder2 builder = new HTMLBuilder2();

        HTMLBuilder2.Element div = builder.getBody().div();
        final HTMLBuilder2.TableElement table = div.createTable();

        resultsController.visitStreamingFile(new Destination<CapturedStatistic>() {
            @Override public void send(CapturedStatistic statistic) {
                HTMLBuilder2.RowElement row = table.createRow();
                row.cell(Logger.toDateString(statistic.getTime()).toString());
                row.cell(statistic.getPath());
                row.cell(statistic.getValue());
            }
        });

        builder.toFile(statisticsCaptureOutput);
    }

    public void outputJSONResults(ListBackedMap<String, TransactionResultModel> results,
                                  List<CapturedStatistic> capturedStatistics) {
        TestRunResult result = new TestRunResult(model.getTestName(), model.getTestStartTime());
        result.setTestResultsFromModel(results.getMap());
        result.setFailureReason(model.getFailureReason());
        result.setCapturedStatistics(capturedStatistics);

        outputJSONResults(result);
    }

    public void outputJSONResults(TestRunResult result ) {

        JSONHelper helper = new JSONHelper();
        String json = helper.toJSON(result);
        File jsonResultFile = getJSONResultsFile(reportsPath, model.getTestName(), model.getTestStartTime());
        FileUtils.write(json, jsonResultFile);
        logger.info("JSON results written to '{}'", jsonResultFile.getAbsolutePath());

        if (configuration.getResultRepositoryHost() != null) {
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

    public static File getJSONResultsFile(File path, String testName, long testTime) {
        File jsonResultFile = new File(path,
                                       testName + "." + TimeUtils.toFileSafeOrderedNoMillis(testTime) + ".results.json");
        return jsonResultFile;
    }

    protected void saveFrequencyChart(TransactionResultModel transactionResultModel,
                                      ResultsController resultsController,
                                      File reportsPath) {

        File file = new File(reportsPath, transactionResultModel.getKey() + ".frequency.png");
        FrequencyChart chart = new FrequencyChart();

        SinglePassStatisticsLongPrecisionCircular singlePassStatisticsLongPrecision = resultsController.getSuccessStatsByTest()
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
        resultsController.resetStats();
        model.resetStats();
    }

    public List<com.jbombardier.common.TestInstruction> getTestInstructionsList() {
        List<com.jbombardier.common.TestInstruction> instructions = new ArrayList<com.jbombardier.common.TestInstruction>();
        populateTestInstructionsList(instructions, model.getTestModels());
        return instructions;
    }

    public void setReportsPath(File reportsPath) {
        this.reportsPath = reportsPath;
    }

    public int getAutostartAgents() {
        return autostartAgents;
    }

    public void setAutostartAgents(int autostartAgents) {
        this.autostartAgents = autostartAgents;
    }

    public ResultsController getResultsController() {
        return resultsController;
    }

    public void startStats() {
        if (outputControllerStats) {
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
            if (agentModel.getName().equals(Agent.embeddedName)) {
                ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
                    @Override public Boolean call() throws Exception {
                        return agentModel.isConnected();
                    }
                });
            }
        }
    }

    public void stop() {
        if (memoryMonitorWorkerThread != null) {
            memoryMonitorWorkerThread.stop();
            memoryMonitorWorkerThread = null;
        }
    }

    public void startStatisticsCapture() {
        List<com.jbombardier.common.StatisticProvider> statisticsProviders = model.getStatisticsProviders();
        for (com.jbombardier.common.StatisticProvider statisticsProvider : statisticsProviders) {
            statisticsProvider.start();
        }
    }

    public void stopStatisticsCapture() {
        resultsController.closeStreamingFiles();
        List<com.jbombardier.common.StatisticProvider> statisticsProviders = model.getStatisticsProviders();
        for (com.jbombardier.common.StatisticProvider statisticsProvider : statisticsProviders) {
            statisticsProvider.stop();
        }
    }
}

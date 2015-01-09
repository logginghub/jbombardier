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

package com.jbombardier.agent;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.ConsoleHandler;

import com.esotericsoftware.kryo.Kryo;
import com.jbombardier.common.AgentFailedInstruction;
import com.jbombardier.common.AgentKillInstruction;
import com.jbombardier.common.AgentLogMessage;
import com.jbombardier.common.AgentStats;
import com.jbombardier.common.BasicTestStats;
import com.jbombardier.common.DataBucket;
import com.jbombardier.common.LoggingStrategy;
import com.jbombardier.common.PerformanceTest;
import com.jbombardier.common.PingMessage;
import com.jbombardier.common.SendTelemetryRequest;
import com.jbombardier.common.StopTelemetryRequest;
import com.jbombardier.common.StopTestRequest;
import com.jbombardier.common.StopTestResponse;
import com.jbombardier.common.TestField;
import com.jbombardier.common.TestInstruction;
import com.jbombardier.common.TestPackage;
import com.jbombardier.common.TestVariableUpdateRequest;
import com.jbombardier.common.ThreadsChangedMessage;
import com.jbombardier.common.KryoHelper;
import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.SingleLineTextFormatter;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.handlers.SocketHandler;
import com.logginghub.logging.log4j.SocketAppender;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.messaging2.ClassRegisterer;
import com.logginghub.messaging2.ReflectionDispatchMessageListener;
import com.logginghub.messaging2.api.Message;
import com.logginghub.messaging2.api.MessageListener;
import com.logginghub.messaging2.kryo.KryoHub;
import com.logginghub.messaging2.local.LocalClient;
import com.logginghub.utils.*;
import com.logginghub.utils.logging.LogEvent;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.LoggerStream;
import com.logginghub.utils.logging.SystemErrStream;
import com.jbombardier.agent.ThreadController.ThreadControllerListener;

public class Agent2 implements Asynchronous {

    private ExecutorService executorService = Executors.newCachedThreadPool(new NamedThreadFactory("Agent2-worker-"));
    private final List<ThreadController> threadControllers = new ArrayList<ThreadController>();
    private Map<String, ThreadController> threadControllersByTestName = new HashMap<String, ThreadController>();
    private KryoHub hub;
    private Timer statsTimer;
    private LocalClient localClient;
    private BasicTestStats[] stats;
    private int statsFailures;
    private volatile boolean abortingTest = false;
    private boolean systemExitOnKill = true;
    private volatile long lastPingTime = 0;

    // These will override the kryohub defaults - the Agent will only have a single connection so
    // its not so bad
    private int writeBufferSize = Integer.getInteger("kryo.writebuffersize", 20 * 1024 * 1024);
    private int objectBufferSize = Integer.getInteger("kryo.objectbuffersize", 20 * 1024 * 1024);

    private Map<String, DataBucket> dataBuckets = new HashMap<String, DataBucket>();
    private SocketClientManager socketClientManager;
    private SocketClient loggingSocketClient;

    private static final Logger logger = Logger.getLoggerFor(Agent2.class);
    private long pingTimeout = Integer.getInteger("agentPingTimeout", 10000);
    private int bindPort;

    private StatBundle statBundle = new StatBundle();

    private IntegerStat individualResultsStat;
    private IntegerStat resultPackageStat;
    private IntegerStat threadControllersStat;
    private IntegerStat pingsStat;
    private boolean outputStats = !EnvironmentProperties.getBoolean("jbombardierAgent.disableStats");
    private String name;
    private ReflectionDispatchMessageListener reflectionDispatchMessageListener;

    public void stop() {
        stopTest();
        if(hub != null) {
            hub.stop();
            hub = null;
        }

        if(statsTimer != null) {
            statsTimer.cancel();
            statsTimer = null;
        }

        if(statBundle != null) {
            statBundle.stop();
            statBundle = null;
        }

        if(localClient != null) {
            localClient.disconnect();
            localClient.stop();
            localClient = null;
        }

        if(reflectionDispatchMessageListener != null) {
            reflectionDispatchMessageListener.stop();
            reflectionDispatchMessageListener = null;
        }

        executorService.shutdownNow();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public int getBindPort() {
        return bindPort;
    }

    public static void main(String[] args) {
        SystemErrStream.gapThreshold = 1000;
        Logger.setLevelFromSystemProperty();

        logger.info("Starting jbombardier agent...");

        startTimerHackThread();

        int bindPort = 20001;
        if (args.length > 0) {
            bindPort = Integer.parseInt(args[0]);
        }

        Agent2 agent2 = new Agent2();
        agent2.setBindPort(bindPort);
        agent2.start();
    }

    private static void startTimerHackThread() {
        new Thread() {
            {
                this.setDaemon(true);
                this.start();
            }

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Integer.MAX_VALUE);
                    }
                    catch (InterruptedException ex) {}
                }
            }
        };
    }

    public void start() {
        ClassRegisterer classRegisterer = new ClassRegisterer() {
            public void registerClasses(Kryo kryo) {
                KryoHelper.registerTypes(kryo);
            }
        };

        if (writeBufferSize != -1 && objectBufferSize != -1) {
            hub = new KryoHub(writeBufferSize, objectBufferSize, classRegisterer);
        }
        else {
            hub = new KryoHub(classRegisterer);
        }


        hub.addGlobalMessageListener(new MessageListener() {
            public void onNewMessage(Message message) {
                logger.debug("Message received : {}", message);
            }

        });

        reflectionDispatchMessageListener = new ReflectionDispatchMessageListener("agent", hub, this);
        hub.connect("agent", reflectionDispatchMessageListener);
        hub.start(bindPort);

        statBundle.addStats(hub.getConnectionsCountStat(), hub.getConnectionsStat(), hub.getDisconnectionsStat(), hub.getMessagesReceivedStat(), hub.getMessagesSentStat());

        individualResultsStat = statBundle.createStat("individual results");
        resultPackageStat = statBundle.createStat("results updates sent");
        threadControllersStat = statBundle.createStat("thread controllers");
        pingsStat = statBundle.createStat("ping(s)");

        individualResultsStat.setIncremental(true);
        resultPackageStat.setIncremental(true);
        threadControllersStat.setIncremental(false);
        pingsStat.setIncremental(true);

        if (outputStats) {
            statBundle.startPerSecond(logger);
        }

        localClient = new LocalClient("agent-classloader");
        localClient.addConnectionPoint(hub);

        // Wire up a message listener to the remote classloader to update the ping time - this way
        // tests wont kill themselves if they spend a long time loading classes and resources
        localClient.addMessageListener(new MessageListener() {
            public void onNewMessage(Message message) {
                updatePing();
            }
        });
        localClient.connect();

        logger.info("jbombardierAgent started and bound on port {}", bindPort);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String handleStartTelemetryRequest(final SendTelemetryRequest str) {
        return "Done";
    }

    @SuppressWarnings("UnusedDeclaration")
    public void handlePing(PingMessage pingMessage) {
        updatePing();
    }

    private void updatePing() {
        pingsStat.increment();
        logger.debug("Updating agent ping, test is still alive");
        lastPingTime = System.currentTimeMillis();
    }

    @SuppressWarnings("UnusedDeclaration")
    public String handleDataBucket(DataBucket dataBucket) {
        Is.notEmpty(dataBucket.getValues(), "Data bucket for source " + dataBucket.getDataSourceName() + " cannot be empty");
        dataBuckets.put(dataBucket.getDataSourceName(), dataBucket);
        return "Bucket stored";
    }

    public String handleTestPackage(final TestPackage testPackage) {
        logger.info("Test package received");
        stopTest();
        updatePing();

        executorService.execute(new Runnable() {
            public void run() {
                startTest(testPackage);
            }
        });

        // Reset the failure tracking state variables
        statsFailures = 0;
        abortingTest = false;

        return "Test package received";
    }

    protected void startTest(TestPackage testPackage) {
        logger.info("Executing test package " + testPackage);

        final String agentName = testPackage.getAgentName();

        final boolean logInternallyToHub = setupHubLogging(testPackage);

        // TODO : we should probably find a way of asking the server if any
        // classes have changed since our last update, to save redownloading
        // lots of classes that haven't changed! We will always need a new class
        // loaded though to avoid the duplicate class definition issue, but at
        // least the instance could inherit the existing definitions from the
        // preivous classloader and only have to load the changes?
        Messaging2ClassLoader classloader = new Messaging2ClassLoader(localClient, "controller", this.getClass().getClassLoader());

        List<TestInstruction> instructions = testPackage.getInstructions();
        stats = new BasicTestStats[instructions.size()];
        int statsIndex = 0;
        for (final TestInstruction testInstruction : instructions) {
            String classname = testInstruction.getClassname();

            String testName = testInstruction.getTestName();
            BasicTestStats basicTestStats = new BasicTestStats(testName);
            stats[statsIndex] = basicTestStats;

            try {
                @SuppressWarnings("unchecked") Class<? extends PerformanceTest> testClass = classloader.findClass(classname);

                SimpleTestContextFactory testContextFactory = new SimpleTestContextFactory();

                testContextFactory.setLoggingStrategy(new LoggingStrategy() {
                    public void log(String format, Object... params) {
                        String message = StringUtils.format(format, params);
                        hub.send("controller", "agent", new AgentLogMessage(agentName + "." + Thread.currentThread().getName(), message));

                        if (logInternallyToHub) {
                            logToHub(loggingSocketClient, message);
                        }
                    }
                });

                testContextFactory.setRecordAllValues(testInstruction.getRecordAllValues());

                final Messaging2PropertiesProvider propertiesProvider = new Messaging2PropertiesProvider(localClient, "controller");
                propertiesProvider.setupAgentCachedData(dataBuckets);
                propertiesProvider.setStartingProperties(testInstruction.getProperties());
                testContextFactory.setPropertiesProvider(propertiesProvider);

                String simpleName = testClass.getSimpleName();
                final String fullName = testClass.getName();
                logger.info("Starting thread controller to run " + simpleName);
                ThreadController controller = new ThreadController(testName, testClass, testContextFactory, testInstruction);

                controller.setExceptionHandler(new ExceptionHandler() {
                    public void handleException(String action, Throwable t) {
                        hub.send("controller", "agent", new AgentFailedInstruction("Error in controller", Thread.currentThread().getName(), action, t));
                    }
                });

                controller.addThreadControllerListener(new ThreadControllerListener() {
                    public void onThreadStarted(int threads) {
                        if (!abortingTest && hub != null) {
                            hub.send("controller", "agent", new ThreadsChangedMessage(agentName, fullName, threads));
                        }
                    }

                    public void onThreadKilled(int threads) {
                        if (!abortingTest && hub != null) {
                            hub.send("controller", "agent", new ThreadsChangedMessage(agentName, fullName, -threads));
                        }
                    }

                    public void onException(String message, String threadName, Throwable throwable) {
                        logger.warning(throwable);
                        hub.send("controller", "agent", new AgentLogMessage(agentName + "." + threadName, message, throwable));
                        // TODO : this is hateful
                        if (message.contains("Setup")) {
                            hub.send("controller", "agent", new AgentFailedInstruction("Error in setup", threadName, message, throwable));
                        }
                    }
                });

                controller.setStats(basicTestStats);
                controller.setResultsAggregator(testContextFactory.getResultAggregator());
                synchronized (threadControllers) {
                    threadControllers.add(controller);
                    threadControllersStat.increment();
                }
                threadControllersByTestName.put(testName, controller);
                controller.start();
            }
            catch (Exception e) {
                hub.send("controller", "agent", new AgentLogMessage(agentName, "Failed to create test", e));

                // This should cause the controller to send out kill
                // instructions to everyone shortly
                hub.send("controller", "agent", new AgentFailedInstruction());

                // This should kill any other attempts to create more tests in
                // this agent
                throw new RuntimeException(e);
            }
        }

        if (statsTimer == null) {
            statsTimer = TimerUtils.everySecond("StatsTimer", new Runnable() {
                public void run() {
                    sendStatsUpdate(agentName);
                    checkPing();
                }
            });
        }
    }

    private boolean setupHubLogging(TestPackage testPackage) {

        String loggingType = testPackage.getLoggingType();

        final boolean logInternallyToHub = loggingType != null && loggingType.toLowerCase().contains("internal");

        if (logInternallyToHub) {
            loggingSocketClient = new SocketClient();
            List<InetSocketAddress> connectionPoints = NetUtils.toInetSocketAddressList(testPackage.getLoggingHubs(), VLPorts.getSocketHubDefaultPort());
            loggingSocketClient.addConnectionPoints(connectionPoints);
            loggingSocketClient.setAutoSubscribe(false);
            socketClientManager = new SocketClientManager(loggingSocketClient);
            socketClientManager.startDaemon();
        }

        final boolean logVLLoggingToHub = loggingType != null && loggingType.toLowerCase().contains("vl");
        if (logVLLoggingToHub) {
            Logger.root().addStream(new LoggerStream() {
                public void onNewLogEvent(LogEvent event) {
                    DefaultLogEvent logEvent = LogEventBuilder.start()
                                                              .setMessage(event.getMessage())
                                                              .setSourceApplication(getAgentName())
                                                              .setSourceClassName(event.getSourceClassName())
                                                              .setSourceMethodName(event.getSourceMethodName())
                                                              .setLevel(event.getLevel())
                                                              .setLocalCreationTimeMillis(event.getOriginTime())
                                                              .setSequenceNumber(event.getSequenceNumber())
                                                              .setThreadName(event.getThreadName())
                                                              .toLogEvent();
                    try {
                        loggingSocketClient.send(new LogEventMessage(logEvent));
                    }
                    catch (LoggingMessageSenderException e) {
                        System.err.println("Failed to send logging message to the hub : " + e.getMessage());
                    }
                }
            });
        }

        final boolean logFromLog4j = loggingType != null && loggingType.toLowerCase().contains("log4j");
        if (logFromLog4j) {

            org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
            rootLogger.setLevel(org.apache.log4j.Level.INFO);

            SocketAppender socketAppender = new SocketAppender();
            socketAppender.setCpuLogging(false);
            socketAppender.setDetailedCpuLogging(false);
            socketAppender.setDontThrowExceptionsIfHubIsntUp(false);
            socketAppender.setFailureDelay(100);
            socketAppender.setForceFlush(false);
            socketAppender.setHeapLogging(false);
            socketAppender.setHost(testPackage.getLoggingHubs());
            socketAppender.setPublishMachineTelemetry(false);
            socketAppender.setPublishProcessTelemetry(false);
            socketAppender.setSourceApplication(getAgentName());
            socketAppender.setUseDispatchThread(true);
            rootLogger.addAppender(socketAppender);
        }

        final boolean logFromJul = loggingType != null && loggingType.toLowerCase().contains("j.u.l");
        if (logFromJul) {

            SocketHandler socketHandler = new SocketHandler();
            socketHandler.setSourceApplication(getAgentName());
            List<InetSocketAddress> connectionPoints = NetUtils.toInetSocketAddressList(testPackage.getLoggingHubs(), VLPorts.getSocketHubDefaultPort());
            for (InetSocketAddress inetSocketAddress : connectionPoints) {
                socketHandler.addConnectionPoint(inetSocketAddress);
            }
            socketHandler.setLevel(java.util.logging.Level.ALL);

            java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
            root.addHandler(socketHandler);

            // Stop the loggers logging to themselves
            ConsoleHandler localConsoleHandler = new ConsoleHandler();
            localConsoleHandler.setFormatter(new SingleLineTextFormatter());
            localConsoleHandler.setLevel(java.util.logging.Level.WARNING);

            java.util.logging.Logger loggingHandlers = java.util.logging.Logger.getLogger("com.logginghub.logging");
            loggingHandlers.setUseParentHandlers(false);
            loggingHandlers.addHandler(localConsoleHandler);
            loggingHandlers.setLevel(java.util.logging.Level.WARNING);
        }

        return logInternallyToHub;
    }

    protected String getAgentName() {
        return "Agent (" + bindPort + ")";

    }

    protected void checkPing() {
        if (System.currentTimeMillis() - lastPingTime > pingTimeout) {
            logger.warn("Agent hasn't received a ping message for too long, killing the agent");
            AgentKillInstruction instruction = new AgentKillInstruction(-1);
            handleAgentKillInstruction(instruction);
        }
    }

    protected void sendStatsUpdate(String agentName) {
        AgentStats agentStats = new AgentStats();
        agentStats.setAgentName(agentName);

        long resultCount = 0;

        synchronized (threadControllers) {
            for (ThreadController threadController : threadControllers) {
                AgentStats.TestStats testStats = new AgentStats.TestStats();

                testStats.setTestName(threadController.getTestName());
                testStats.setThreadCount(threadController.getThreadCount());

                // TODO : refactor the test stats to work in the same (one-per
                // thread, then aggregated) way the transaction stats are
                // currently
                // working

                // This basic stats instance represents the overall transaction
                // rate
                // for this test; its not the individual transaction tests
                // stats...
                BasicTestStats basicTestStats = threadController.getBasicTestStats();
                resultCount += basicTestStats.successResults.size() + basicTestStats.failResults.size();
                testStats.fromBasicTestStats(basicTestStats);
                basicTestStats.reset();
                testStats.setTransaction(false);
                agentStats.addTestStats(testStats);

                // As we have to pull these together a different way...
                ResultAggregator resultAggregator = threadController.getResultAggregator();

                Map<String, BasicTestStats> results = resultAggregator.getResultsAndReset();
                for (BasicTestStats subtransactionStats : results.values()) {
                    AgentStats.TestStats ts = new AgentStats.TestStats();
                    ts.fromBasicTestStats(subtransactionStats);
                    ts.setTransaction(true);
                    ts.setTestName(threadController.getTestName());
                    ts.setTransactionName(subtransactionStats.getTestName());
                    agentStats.addTestStats(ts);
                }
            }
        }

        individualResultsStat.increment((int) resultCount);
        logger.debug("Sending {} results to the console", resultCount);

        int failureLimit = 10;
        try {
            logger.fine("Sending stats to the controller...");
            hub.send("controller", "agent", agentStats);
            resultPackageStat.increment();
        }
        catch (RuntimeException se) {
            statsFailures++;
            logger.info(se, "Failed to send stats to the agent ({} out of {} attempts): {}", statsFailures, failureLimit, se.getMessage());
        }

        if (statsFailures > failureLimit) {
            abortingTest = true;
            logger.warn("We've had " +
                        failureLimit +
                        " failures when trying to send stats updates to the controller, it doesn't look like its there anymore so the agent is giving up running the tests");
            stopTest();
        }
    }

    @SuppressWarnings("UnusedDeclaration") public StopTestResponse handleStopTestRequest(StopTestRequest request) {
        stopTest();
        return new StopTestResponse();
    }

    @SuppressWarnings("UnusedDeclaration")
    public String handleStopTelemetryRequest(StopTelemetryRequest request) {
        return "Done";
    }

    private void stopTest() {
        if (statsTimer != null) {
            statsTimer.cancel();
            statsTimer = null;
        }

        if (threadControllers.size() > 0) {
            logger.info("Stopping all test threads");
            synchronized (threadControllers) {
                for (ThreadController threadController : threadControllers) {
                    threadController.setExceptionHandler(null);
                    threadController.clearListeners();
                    threadController.stopAllChildren();
                    threadController.stop();
                    threadControllersStat.decrement();
                }
                threadControllers.clear();
            }
        }

        threadControllersByTestName.clear();

        if (socketClientManager != null) {
            socketClientManager.stop();
        }

        if (loggingSocketClient != null) {
            loggingSocketClient.close();
        }
    }

    public void handleAgentKillInstruction(AgentKillInstruction agentKillInstruction) {
        if (systemExitOnKill) {
            logger.info("Agent kill request received, calling System.exit() with exit code " + agentKillInstruction.getCode());
            System.exit(agentKillInstruction.getCode());
        }
        else {
            logger.info("Agent kill request received, but systemExitOnKill has been disabled, ignoring.");
            stopTest();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public String handleTestVariableUpdateRequest(TestVariableUpdateRequest request) {

        if (request.getField() == TestField.transactionRateModifier) {
            synchronized (threadControllers) {
                for (ThreadController threadController : threadControllers) {
                    threadController.setTransactionRateModifier((Double) request.getNewValue());
                }
            }
        }
        else {
            String testName = request.getTestName();
            ThreadController threadController = threadControllersByTestName.get(testName);
            Object newValue = request.getNewValue();

            TestField field = request.getField();

            logger.info("Updating field " + field + " to " + newValue + " for test " + testName);

            switch (field) {
                case rateStep:
                    threadController.setRateStep((Float) newValue);
                    break;
                case rateStepTime:
                    threadController.setRateStepTime((Long) newValue);
                    break;
                case targetRate:
                    threadController.setTargetRate((Float) newValue);
                    break;
                case targetThreads:
                    threadController.setTargetThreads((Integer) newValue);
                    break;
                case threadStep:
                    threadController.setThreadRampupStepSize((Integer) newValue);
                    break;
                case threadStepTime:
                    threadController.setThreadRampupStepTime((Long) newValue);
                    break;
            }
        }
        return "Value successfully updated";
    }

    public void disableSystemExitOnKill() {
        this.systemExitOnKill = false;
    }

    private void logToHub(final SocketClient finalClient, String message) {
        DefaultLogEvent logEvent = LogEventBuilder.start()
                                                  .setMessage(message)
                                                  .setSourceApplication(getAgentName())
                                                  .setSourceClassName(StacktraceUtils.getCallingClassName(4))
                                                  .setSourceMethodName(StacktraceUtils.getCallingMethodName(4))
                                                  .setThreadName(Thread.currentThread().getName())
                                                  .toLogEvent();
        try {
            finalClient.send(new LogEventMessage(logEvent));
        }
        catch (LoggingMessageSenderException e) {}
    }

    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    public void setObjectBufferSize(int objectBufferSize) {
        this.objectBufferSize = objectBufferSize;
    }

    public void setPingTimeout(long pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    @SuppressWarnings("UnusedDeclaration")
    public long getPingTimeout() {
        return pingTimeout;
    }
    
    public void setOutputStats(boolean outputStats) {
        this.outputStats = outputStats;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isOutputStats() {
        return outputStats;
    }
}

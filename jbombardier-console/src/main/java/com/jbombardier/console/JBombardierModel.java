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

import com.jbombardier.common.AgentFailedInstruction;
import com.jbombardier.common.AgentStats;
import com.jbombardier.common.AgentStats.TestStats;
import com.jbombardier.common.StatisticProvider;
import com.jbombardier.console.model.AgentModel;
import com.jbombardier.console.model.ConsoleEventModel;
import com.jbombardier.console.model.ConsoleEventModel.Severity;
import com.jbombardier.console.model.TestModel;
import com.jbombardier.console.model.TransactionResultModel;
import com.logginghub.utils.AbstractBean;
import com.logginghub.utils.ArrayListBackedHashMap;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.ListBackedMap;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class JBombardierModel extends AbstractBean {

    private List<AgentModel> agentModels = new CopyOnWriteArrayList<AgentModel>();

    private Map<String, AgentModel> agentsByAgentName = new HashMap<String, AgentModel>();

    private int agentsInTest;

    // private Map<String, NewTransactionResultModel> newTransactionResultsModelByTestName = new HashMap<String, NewTransactionResultModel>();

    // private List<ConsoleEventModel> events = new ArrayList<ConsoleEventModel>();
    private int failedTransactionCountFailureThreshold = -1;

    private String failureReason = null;
    private Map<String, Map<String, TestStats>> latestTestStatsByTestThenAgentName = new FactoryMap<String, Map<String, TestStats>>() {
        @Override protected Map<String, TestStats> createEmptyValue(String key) {
            return new HashMap<String, AgentStats.TestStats>();
        }
    };

    private List<InteractiveModelListener> listeners = new CopyOnWriteArrayList<InteractiveModelListener>();

    private int maximumConsoleEntries = 10000;

    private int noResultsTimeout = 30;

    private List<TestModel> testModels = new CopyOnWriteArrayList<TestModel>();

    private Map<String, TestModel> testModelsByName = new HashMap<String, TestModel>();

    // private boolean testRunning = false;

    private String testName;
    private ObservableProperty<Boolean> testRunning = new ObservableProperty<Boolean>(false);
    private long testStartTime;
    private ListBackedMap<String, TransactionResultModel> transactionModelsByTestName = new ArrayListBackedHashMap<String, TransactionResultModel>();

    private double transactionRateModifier;

    private ObservableList<TransactionResultModel> transactionResultModels = new ObservableList<TransactionResultModel>(
            new ArrayList<TransactionResultModel>());
    private List<StatisticProvider> statisticsProviders = new ArrayList<StatisticProvider>();

    public List<StatisticProvider> getStatisticsProviders() {
        return statisticsProviders;
    }



    public static interface InteractiveModelListener {
        void onConsoleEvent(ConsoleEventModel event);

        void onModelReset();

        // void onNewTestResult(TransactionResultModel testModel);

        void onNewAgent(AgentModel model);

        void onNewTest(TestModel testModel);

        void onTelemetryData(DataStructure data);

        void onTestAbandoned(String reason, AgentFailedInstruction afi);

        void onTestEnded();

        void onTestStarted();
    }

    public static class InteractiveModelListenerAdaptor implements InteractiveModelListener {
        public void onConsoleEvent(ConsoleEventModel event) {}

        public void onModelReset() {}

        // public void onNewTestResult(TransactionResultModel testModel) {}

        public void onNewAgent(AgentModel model) {}

        public void onNewTest(TestModel testModel) {}

        public void onTelemetryData(DataStructure data) {}

        @Override public void onTestAbandoned(String reason, AgentFailedInstruction afi) {}

        public void onTestEnded() {}

        public void onTestStarted() {}
    }

    private static final Logger logger = Logger.getLoggerFor(JBombardierModel.class);

    public JBombardierModel() {

        getTestRunning().addListener(new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    for (InteractiveModelListener listener : listeners) {
                        listener.onTestStarted();
                    }
                } else {
                    for (InteractiveModelListener listener : listeners) {
                        listener.onTestEnded();
                    }
                }
            }
        });
    }

    public void abandonTest(String string, AgentFailedInstruction afi) {
        for (InteractiveModelListener listener : listeners) {
            listener.onTestAbandoned(string, afi);
        }
    }

    public void addAgentModel(AgentModel agentModel) {
        agentsByAgentName.put(agentModel.getName(), agentModel);

        agentModels.add(agentModel);
        for (InteractiveModelListener listener : listeners) {
            listener.onNewAgent(agentModel);
        }
    }

    public void addListener(InteractiveModelListener listener) {
        listeners.add(listener);
    }

    public void addTestModel(TestModel testModel) {
        testModels.add(testModel);
        testModelsByName.put(testModel.getName(), testModel);

        for (InteractiveModelListener listener : listeners) {
            listener.onNewTest(testModel);
        }
    }

    public List<AgentModel> getAgentModels() {
        return agentModels;
    }

    public Map<String, AgentModel> getAgentsByAgentName() {
        return agentsByAgentName;

    }

    public int getAgentsInTest() {
        return agentsInTest;
    }

    // public List<ConsoleEventModel> getEvents() {
    // return events;
    // }

    public int getFailedTransactionCountFailureThreshold() {
        return failedTransactionCountFailureThreshold;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Map<String, Map<String, TestStats>> getLatestTestStatsByTestThenAgentName() {
        return latestTestStatsByTestThenAgentName;
    }

    public int getMaximumConsoleEntries() {
        return maximumConsoleEntries;
    }

    public int getNoResultsTimeout() {
        return noResultsTimeout;
    }

    public void setNoResultsTimeout(int noResultsTimeout) {
        this.noResultsTimeout = noResultsTimeout;
    }

    public List<TestModel> getTestModels() {
        return testModels;
    }

    public String getTestName() {
        return testName;
    }

    public ObservableProperty<Boolean> getTestRunning() {
        return testRunning;
    }

    public long getTestStartTime() {
        return testStartTime;
    }

    public synchronized ListBackedMap<String, TransactionResultModel> getTotalTransactionModelsByTestName() {

        ListBackedMap<String, TransactionResultModel> copy = new ListBackedMap<String, TransactionResultModel>(this.transactionModelsByTestName);
        return copy;
    }

    public double getTransactionRateModifier() {
        return transactionRateModifier;
    }

    public TransactionResultModel getTransactionResultModelForTest(String key) {
        return transactionModelsByTestName.get(key);
    }

    public ObservableList<TransactionResultModel> getTransactionResultModels() {
        return transactionResultModels;
    }

    public void incrementActiveThreadCount(String agent, int threads) {
        agentsByAgentName.get(agent).incrementActiveThreadCount(threads);
    }

    public boolean isTestRunning() {
        return testRunning.asBoolean();
    }

    public void log(ConsoleEventModel event) {
        // synchronized (events) {
        // events.add(event);
        // }

        for (InteractiveModelListener listener : listeners) {
            listener.onConsoleEvent(event);
        }
    }

    public void log(Severity severity, String string, Object... objects) {
        ConsoleEventModel event = new ConsoleEventModel();
        event.setMessage(String.format(string, objects));
        event.setSource("Console");
        event.setTime(new Date());
        event.setSeverity(severity);
        log(event);
    }

    public synchronized void onAgentStatusUpdate(AgentStats agentStats) {
        logger.debug("Agent stats received from agent {}", agentStats.getAgentName());

        String agent = agentStats.getAgentName();
        agentsByAgentName.get(agent).update(agentStats);

        List<TestStats> testStatsList = agentStats.getTestStats();
        for (TestStats testStats : testStatsList) {
            String testTransactionKey = testStats.getKey().toString();

            TestStats previous = latestTestStatsByTestThenAgentName.get(testTransactionKey)
                                                                   .put(agentStats.getAgentName(), testStats);
            if (previous != null) {
                logger.warn(
                        "We've replaced the test stats for agent '{}' and test '{}' - this shouldn't happen unless agents are sending back multiple results for the same test each second - in this case you agents may be running two tests in parallel and should be killed",
                        agentStats.getAgentName(),
                        previous.getKey());
            }

            int size = latestTestStatsByTestThenAgentName.get(testTransactionKey).size();
            if (size == agentsInTest) {
                // We have all of the values we need to put up an all agents
                // total
                logger.debug("{} results have been received for test '{}', this is enough to add a new data point",
                             size,
                             testTransactionKey);

                Map<String, TestStats> map = latestTestStatsByTestThenAgentName.get(testTransactionKey);
                Collection<TestStats> values = map.values();

                TransactionResultModel transactionResultModel = transactionModelsByTestName.get(testTransactionKey);
                if (transactionResultModel == null) {

                    final TestModel testModel = testModelsByName.get(testStats.getTestName());
                    Double transactionSLA = testModel.getTransactionSLAs().get(testStats.getTransactionName());

                    double tsla;
                    if (transactionSLA != null) {
                        tsla = transactionSLA;
                    } else {
                        tsla = Double.NaN;
                    }

                    double targetRate = testModel.getTargetRate() * getTransactionRateModifier() * testModel.getTargetThreads();

                    transactionResultModel = new TransactionResultModel(testStats.getTestName(),
                                                                        testStats.getTransactionName(),
                                                                        testStats.isTransaction(),
                                                                        targetRate,
                                                                        testModel.getTargetThreads(),
                                                                        agentsInTest,
                                                                        tsla,
                                                                        testModel.getFailureThreshold(),
                                                                        testModel.getFailedTransactionCountThreshold(),
                                                                        testModel.getFailureThresholdResultCountMinimum(),
                                                                        testModel.getMovingAveragePoints());

                    transactionResultModel.setFailureThresholdMode(testModel.getFailureThresholdMode());

                    transactionResultModels.add(transactionResultModel);
                    transactionModelsByTestName.put(testTransactionKey, transactionResultModel);
                    // for (InteractiveModelListener listener : listeners) {
                    // listener.onNewTestResult(transactionResultModel);
                    // }

                    // Wire up the transaction results to listen for changes in
                    // the target rate
                    final TransactionResultModel finalPointer = transactionResultModel;
                    testModel.addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            finalPointer.setTargetTransactions(testModel.getTargetRate() * getTransactionRateModifier() * testModel
                                    .getTargetThreads());
                        }
                    }, "targetRate", "targetThreads");
                }

                double successPerSecondTotal = 0;
                double failurePerSecondTotal = 0;
                double sumSuccessDuration = 0;
                double sumSuccessTotalDuration = 0;
                double sumFailureDuration = 0;
                double totalSuccessDuration = 0;
                double totalFailureDuration = 0;

                long successes = 0;
                long failures = 0;

                for (TestStats perAgentTestStats : values) {

                    double successRatePerSecond = perAgentTestStats.transactionsSuccess / (perAgentTestStats.sampleDuration / 1000d);
                    double failRatePerSecond = perAgentTestStats.transactionsFailed / (perAgentTestStats.sampleDuration / 1000d);

                    double averageSuccess;
                    double averageTotalSuccess;
                    double averageFailures;

                    if (perAgentTestStats.transactionsSuccess > 0) {
                        averageSuccess = perAgentTestStats.totalDurationSuccess / perAgentTestStats.transactionsSuccess;
                        averageTotalSuccess = perAgentTestStats.totalDurationTotalSuccess / perAgentTestStats.transactionsSuccess;
                    } else {
                        averageSuccess = 0;
                        averageTotalSuccess = 0;
                    }

                    if (perAgentTestStats.transactionsFailed > 0) {
                        averageFailures = perAgentTestStats.totalDurationFailed / perAgentTestStats.transactionsFailed;
                    } else {
                        averageFailures = 0;
                    }

                    successPerSecondTotal += successRatePerSecond;
                    failurePerSecondTotal += failRatePerSecond;
                    sumSuccessDuration += averageSuccess;
                    sumSuccessTotalDuration += averageTotalSuccess;
                    sumFailureDuration += averageFailures;
                    successes += testStats.transactionsSuccess;
                    failures += testStats.transactionsFailed;
                    totalSuccessDuration += testStats.totalDurationSuccess;
                    totalFailureDuration += testStats.totalDurationFailed;

                }

                double averageSuccessDuration = sumSuccessDuration / agentsInTest;
                double averageSuccessTotalDuration = sumSuccessTotalDuration / agentsInTest;
                double averageFailureDuration = sumFailureDuration / agentsInTest;

                transactionResultModel.update(successes,
                                              failures,
                                              successPerSecondTotal,
                                              failurePerSecondTotal,
                                              averageSuccessDuration,
                                              averageSuccessTotalDuration,
                                              averageFailureDuration,
                                              totalSuccessDuration,
                                              totalFailureDuration,
                                              testStats.sampleDuration);

                logger.debug("Transaction {} successes {} failures {} success per second {} success mean {}",
                             testStats.getKey().toString(),
                             successes,
                             failures,
                             successPerSecondTotal,
                             averageSuccessDuration);

                // Reset the values for this test
                latestTestStatsByTestThenAgentName.get(testTransactionKey).clear();
            }
        }
    }

    public void reset() {
        for (InteractiveModelListener listener : listeners) {
            listener.onModelReset();
        }

        resetStats();

        agentsInTest = -1;
    }

    public void resetStats() {
        transactionResultModels.clear();
        transactionModelsByTestName.clear();
        latestTestStatsByTestThenAgentName.clear();
    }

    public void setAgentsInTest(int agentsInTest) {
        logger.info("Settings agents in test to {}", agentsInTest);
        this.agentsInTest = agentsInTest;
    }

    public void setFailedTransactionCountFailureThreshold(int failedTransactionFailureThreshold) {
        this.failedTransactionCountFailureThreshold = failedTransactionFailureThreshold;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setMaximumConsoleEntries(int maximumConsoleEntries) {
        this.maximumConsoleEntries = maximumConsoleEntries;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setTestStartTime(long testStartTime) {
        this.testStartTime = testStartTime;
    }

    public void setTransactionRateModifier(double doubleValue) {
        double old = transactionRateModifier;
        transactionRateModifier = doubleValue;
        firePropertyChange("transactionRateModifier", old, doubleValue);

        // Update the target rates for the current transactions
        Set<String> keys = transactionModelsByTestName.keySet();
        for (String name : keys) {
            TestModel testModel = testModelsByName.get(name);
            transactionModelsByTestName.get(name).setTargetTransactions(testModel.getTargetRate() * doubleValue);
        }
    }

    public void update(DataStructure mt) {
        for (InteractiveModelListener listener : listeners) {
            listener.onTelemetryData(mt);
        }
    }

    public List<AgentModel> getConnectedAgents() {

        List<AgentModel> connectedModels = new LinkedList<AgentModel>();
        for (AgentModel agentModel : getAgentModels()) {
            if (agentModel.isConnected()) {
                connectedModels.add(agentModel);
            }
        }

        return connectedModels;
    }

    public int getConnectionAgentCount() {return getConnectedAgents().size();}
}

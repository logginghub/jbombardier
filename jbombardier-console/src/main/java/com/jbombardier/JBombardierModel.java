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

import com.jbombardier.common.AgentFailedInstruction;
import com.jbombardier.common.AgentStats;
import com.jbombardier.common.AgentStats.TestStats;
import com.jbombardier.common.StatisticProvider;
import com.jbombardier.console.TransactionMovingAverages;
import com.jbombardier.console.model.*;
import com.jbombardier.console.model.ConsoleEventModel.Severity;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class JBombardierModel extends Observable {

    private TimeProvider timeProvider = new SystemTimeProvider();

    private List<AgentModel> agentModels = new CopyOnWriteArrayList<AgentModel>();

    private Map<String, AgentModel> agentsByAgentName = new HashMap<String, AgentModel>();

    private int agentsInTest;

    private ObservableProperty<PhaseModel> currentPhase = createProperty("currentPhase", PhaseModel.class, null);

    // private Map<String, NewTransactionResultModel> newTransactionResultsModelByTestName = new HashMap<String, NewTransactionResultModel>();

    // private List<ConsoleEventModel> events = new ArrayList<ConsoleEventModel>();
    private int failedTransactionCountFailureThreshold = -1;

    private String failureReason = null;
    private Map<String, Map<String, TestStats>> latestTestStatsByTestThenAgentName = new FactoryMap<String, Map<String, TestStats>>() {
        @Override
        protected Map<String, TestStats> createEmptyValue(String key) {
            return new HashMap<String, AgentStats.TestStats>();
        }
    };

    private List<InteractiveModelListener> listeners = new CopyOnWriteArrayList<InteractiveModelListener>();

    private int maximumConsoleEntries = 10000;

    private int noResultsTimeout = 30;

    private ObservableList<PhaseModel> phaseModels = createListProperty("phaseModels", PhaseModel.class);

    //    private Map<String, TestModel> testModelsByName = new HashMap<String, TestModel>();
    // private boolean testRunning = false;

    private ObservableProperty<String> testName = createStringProperty("testName", "");

    private ObservableProperty<Boolean> testRunning = new ObservableProperty<Boolean>(false);
    private ObservableLong testStartTime = createLongProperty("testStartTime", -1);

    private Map<String, TransactionMovingAverages> movingAveragesByTestName = new FactoryMap<String, TransactionMovingAverages>() {
        @Override
        protected TransactionMovingAverages createEmptyValue(String s) {
            return new TransactionMovingAverages();
        }
    };

    private ObservableDouble transactionRateModifier = createDoubleProperty("transactionRateModifier", 1);

    //private ObservableList<TransactionResultModel> transactionResultModels = new ObservableList<TransactionResultModel>(new ArrayList<TransactionResultModel>());

    private List<StatisticProvider> statisticsProviders = new ArrayList<StatisticProvider>();
    private long currentPhaseStart;

    public List<StatisticProvider> getStatisticsProviders() {
        return statisticsProviders;
    }

    public List<String> getTestNames(PhaseModel phaseModel) {
        List<String> testNames = new ArrayList<String>();
        for (TestModel testModel : phaseModel.getTestModels()) {
            testNames.add(testModel.getName());
        }
        Collections.sort(testNames);
        return testNames;
    }

    public List<String> getPhaseNames() {
        List<String> testNames = new ArrayList<String>();
        for (PhaseModel testModel : phaseModels) {
            testNames.add(testModel.getPhaseName().get());
        }
        Collections.sort(testNames);
        return testNames;
    }

    public TestModel getTestModelForTest(PhaseModel phaseModel, String name) {
        TestModel found = null;

        for (TestModel testModel : phaseModel.getTestModels()) {
            if (testModel.getName().equals(name)) {
                found = testModel;
                break;
            }
        }

        return found;
    }

    public void setPhaseStartTime(long time) {
        currentPhaseStart = time;
    }

    public void setPhaseEndTime(long time) {
        ObservableList<TransactionResultModel> transactionResultModels = currentPhase.get().getTransactionResultModels();
        for (TransactionResultModel transactionResultModel : transactionResultModels) {
            transactionResultModel.getTestDuration().set(time - currentPhaseStart);
        }
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
        public void onConsoleEvent(ConsoleEventModel event) {
        }

        public void onModelReset() {
        }

        // public void onNewTestResult(TransactionResultModel testModel) {}

        public void onNewAgent(AgentModel model) {
        }

        public void onNewTest(TestModel testModel) {
        }

        public void onTelemetryData(DataStructure data) {
        }

        @Override
        public void onTestAbandoned(String reason, AgentFailedInstruction afi) {
        }

        public void onTestEnded() {
        }

        public void onTestStarted() {
        }
    }

    private static final Logger logger = Logger.getLoggerFor(JBombardierModel.class);

    public JBombardierModel() {

        getTestRunning().addListener(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
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
        agentsByAgentName.put(agentModel.getName().get(), agentModel);

        agentModels.add(agentModel);
        for (InteractiveModelListener listener : listeners) {
            listener.onNewAgent(agentModel);
        }
    }

    public void addListener(InteractiveModelListener listener) {
        listeners.add(listener);
    }

    //    public void addTestModel(TestModel testModel) {
    //        testModels.add(testModel);
    //        testModelsByName.put(testModel.getName(), testModel);
    //
    //        for (InteractiveModelListener listener : listeners) {
    //            listener.onNewTest(testModel);
    //        }
    //    }

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

    public ObservableList<PhaseModel> getPhaseModels() {
        return phaseModels;
    }

    public ObservableProperty<String> getTestName() {
        return testName;
    }

    public ObservableProperty<Boolean> getTestRunning() {
        return testRunning;
    }

    public ObservableLong getTestStartTime() {
        return testStartTime;
    }

    //    public synchronized ListBackedMap<String, TransactionResultModel> getTotalTransactionModelsByTestName() {
    //        ListBackedMap<String, TransactionResultModel> copy = new ListBackedMap<String, TransactionResultModel>(this.transactionModelsByTestName);
    //        return copy;
    //    }

    public ObservableDouble getTransactionRateModifier() {
        return transactionRateModifier;
    }

    //    public TransactionResultModel getTransactionResultModelForTest(String key) {
    //        return transactionModelsByTestName.get(key);
    //    }


    public void incrementActiveThreadCount(String agent, int threads) {
        agentsByAgentName.get(agent).incrementActiveThreadCount(threads);
    }

    public boolean isTestRunning() {
        return testRunning.asBoolean();
    }

    public void log(ConsoleEventModel event) {
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

            TestStats previous = latestTestStatsByTestThenAgentName.get(testTransactionKey).put(agentStats.getAgentName(), testStats);
            if (previous != null) {
                logger.warn(
                        "We've replaced the test stats for agent '{}' and test '{}' - this shouldn't happen unless agents are sending back multiple results for the same test each second - in this case you agents may be running two tests in parallel and should be killed",
                        agentStats.getAgentName(),
                        previous.getKey());
            }

            int size = latestTestStatsByTestThenAgentName.get(testTransactionKey).size();
            if (size == agentsInTest) {
                // We have all of the values we need to put up an all agents total
                aggregateAgentResponses(latestTestStatsByTestThenAgentName.get(testTransactionKey), testStats.getTestName(),
                        testStats.getTransactionName(), testTransactionKey, size);
            }
        }
    }

    private void aggregateAgentResponses(Map<String, TestStats> map, String testName, String transactionName, String transactionKey, int size) {

        logger.debug("{} results have been received for test '{}', this is enough to add a new data point", size, transactionKey);

        Collection<TestStats> values = map.values();

        TransactionMovingAverages transactionMovingAverages = movingAveragesByTestName.get(transactionKey);

        TransactionResultModel transactionResultModel = currentPhase.get().getTransactionResultModelForTransaction(transactionKey);
        if (transactionResultModel == null) {

            final TestModel testModel = getTestModelForTest(currentPhase.get(), testName);
            if (testModel == null) {
                logger.warn("No test model found for test '{}' in phase '{}' - is something sending us resuls from a different test or phase?", testName, currentPhase.get());
                return;
            } else {

                Double transactionSLA = testModel.getTransactionSLAs().get(transactionName);

                double tsla;
                if (transactionSLA != null) {
                    tsla = transactionSLA;
                } else {
                    tsla = Double.NaN;
                }

                double targetRate = testModel.getTargetRate().get() * getTransactionRateModifier().get() * testModel.getTargetThreads().get();

                transactionResultModel = new TransactionResultModel();

                transactionResultModel.getTestName().set(testName);
                transactionResultModel.getTransactionName().set(transactionName);
                transactionResultModel.getTargetSuccessfulTransactionsPerSecond().set(targetRate);
                transactionResultModel.getSuccessfulTransactionDurationSLA().set(tsla);
                transactionResultModel.getSuccessfulTransactionsDurationFailureThreshold().set(testModel.getFailureThreshold());
                transactionResultModel.getSuccessfulTransactionsDurationFailureType().set(testModel.getFailureThresholdMode());
                transactionResultModel.getSuccessfulTransactionsTotalFailureResultCountMinimum().set(testModel.getFailureThresholdResultCountMinimum());
                transactionResultModel.getUnsuccessfulTransactionsTotalFailureThreshold().set(testModel.getFailedTransactionCountThreshold());
                transactionResultModel.getThreadCount().set(testModel.getTargetThreads().get());

                currentPhase.get().getTransactionResultModels().add(transactionResultModel);

                // Wire up the transaction results to listen for changes in the target rate
                final TransactionResultModel finalPointer = transactionResultModel;
                ObservablePropertyListener listener = new ObservablePropertyListener() {
                    @Override
                    public void onPropertyChanged(Object o, Object t1) {
                        finalPointer.getTargetSuccessfulTransactionsPerSecond().set(testModel.getTargetRate().get() * getTransactionRateModifier().get() * testModel.getTargetThreads().get());
                    }
                };

                testModel.getTargetRate().addListener(listener);
                testModel.getTargetThreads().addListener(listener);
            }
        }

        double successPerSecondTotal = 0;
        double failurePerSecondTotal = 0;
        double sumSuccessDuration = 0;
        double sumSuccessTotalDuration = 0;
        double sumFailureDuration = 0;

        long totalSuccessDuration = 0;
        long totalSuccessIncludingPreAndPostDuration = 0;
        long totalFailureDuration = 0;

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

            successes += perAgentTestStats.transactionsSuccess;
            failures += perAgentTestStats.transactionsFailed;

            totalSuccessDuration += perAgentTestStats.totalDurationSuccess;
            totalSuccessIncludingPreAndPostDuration += perAgentTestStats.getTotalDurationTotalSuccess();
            totalFailureDuration += perAgentTestStats.totalDurationFailed;
        }

        double averageSuccessDuration = sumSuccessDuration / agentsInTest;
        double averageSuccessTotalDuration = sumSuccessTotalDuration / agentsInTest;
        double averageFailureDuration = sumFailureDuration / agentsInTest;

        transactionResultModel.getSuccessfulTransactionsCountTotal().increment(successes);
        transactionResultModel.getUnsuccessfulTransactionsCountTotal().increment(failures);

        transactionResultModel.getSuccessfulTransactionsDurationTotal().increment(totalSuccessDuration);
        transactionResultModel.getSuccessfulTransactionsIncludingPreandPostDurationTotal().increment(totalSuccessIncludingPreAndPostDuration);
        transactionResultModel.getUnsuccessfulTransactionsDurationTotal().increment(totalFailureDuration);

        // Update the moving averages
        if (successPerSecondTotal > 0) {
            transactionMovingAverages.getSuccessfulTransactionDuration().addValue(averageSuccessDuration);
            transactionMovingAverages.getSuccessfulTransactionTotalDuration().addValue(averageSuccessTotalDuration);
        }

        if (failurePerSecondTotal > 0) {
            transactionMovingAverages.getUnsuccessfulTransactionDuration().addValue(averageFailureDuration);
        }

        transactionMovingAverages.getSuccessfulTransactionCount().addValue(successPerSecondTotal);
        transactionMovingAverages.getUnsuccessfulTransactionCount().addValue(failurePerSecondTotal);

        // Take the values from the moving averages and apply them to the transaction model
        transactionResultModel.getSuccessfulMeanTransactionsPerSecond().set(transactionMovingAverages.getSuccessfulTransactionCount().calculateMovingAverage());

        transactionResultModel.getUnsuccessfulMeanTransactionsPerSecond().set(transactionMovingAverages.getUnsuccessfulTransactionCount().calculateMovingAverage());

        transactionResultModel.getSuccessfulTransactionDuration().set(transactionMovingAverages.getSuccessfulTransactionDuration().calculateMovingAverage());
        transactionResultModel.getUnsuccessfulTransactionDuration().set(transactionMovingAverages.getUnsuccessfulTransactionDuration().calculateMovingAverage());

        transactionResultModel.getSuccessfulTransactionTotalDuration().set(transactionMovingAverages.getSuccessfulTransactionTotalDuration().calculateMovingAverage());


        // Keep a view of how long each transaction has been running, in case someone requests a report mid-test
        transactionResultModel.getTestDuration().set(timeProvider.getTime() - currentPhaseStart);

        // jshaw - this simulates an atomic update trigger to model listeners who want a coherent picture
        // after the full update has been applied
        transactionResultModel.getModelUpdates().increment(1);

        logger.debug("Transaction {} successes {} failures {} success per second {} success mean {}",
                transactionKey,
                successes,
                failures,
                successPerSecondTotal,
                averageSuccessDuration);

        // Reset the values for this test
        latestTestStatsByTestThenAgentName.get(transactionKey).clear();
    }

    public void reset() {
        for (InteractiveModelListener listener : listeners) {
            listener.onModelReset();
        }

        resetStats();

        agentsInTest = -1;
    }

    public void resetStats() {
        getCurrentPhase().get().resetStats();
        latestTestStatsByTestThenAgentName.clear();
    }

    public void setAgentsInTest(int agentsInTest) {
        logger.info("Agents in test : {}", agentsInTest);
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

    //    public void setTransactionRateModifier(double doubleValue) {
    //        double old = transactionRateModifier;
    //        transactionRateModifier = doubleValue;
    //
    //        // TODO : refactor fix me
    //        //firePropertyChange("transactionRateModifier", old, doubleValue);
    //
    //        // Update the target rates for the current transactions
    //        Set<String> keys = transactionModelsByTestName.keySet();
    //        for (String name : keys) {
    //            TestModel testModel = testModelsByName.get(name);
    //            transactionModelsByTestName.get(name)
    //                                       .getTargetSuccessfulTransactionsPerSecond()
    //                                       .set(testModel.getTargetRate() * doubleValue);
    //        }
    //    }

    public void update(DataStructure mt) {
        for (InteractiveModelListener listener : listeners) {
            listener.onTelemetryData(mt);
        }
    }

    public List<AgentModel> getConnectedAgents() {

        List<AgentModel> connectedModels = new LinkedList<AgentModel>();
        for (AgentModel agentModel : getAgentModels()) {
            if (agentModel.getConnected().get()) {
                connectedModels.add(agentModel);
            }
        }

        return connectedModels;
    }

    public int getConnectionAgentCount() {
        return getConnectedAgents().size();
    }


    public ObservableProperty<PhaseModel> getCurrentPhase() {
        return currentPhase;
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }
}

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

package com.jbombardier.console.model;

import com.logginghub.utils.observable.*;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Represents the summary values for a single test or transaction.
 */
public class TransactionResultModel extends Observable {

    private ObservableLong testDuration = createLongProperty("testDuration", 0);

    private ObservableProperty<String> testName = createStringProperty("testName", "");
    private ObservableProperty<String> transactionName = createStringProperty("transactionName", "");

    private ObservableDouble tp90 = createDoubleProperty("tp90", Double.NaN);
    private ObservableDouble stddev = createDoubleProperty("stddev", Double.NaN);

    /**
     * A "fake" property that you can watch to be notified when the whole object has been updated. Not sure I like it too much, but it works for now. Would be nice if the Observable provided some sort
     * of atomic update and notification.
     */
    private ObservableLong modelUpdates = createLongProperty("modelUpdates", 0);

    /**
     * The total number of successful transactions we should expect per second, based on the test configuration
     */
    private ObservableDouble targetSuccessfulTransactionsPerSecond = createDoubleProperty("targetSuccessfulTransactionsPerSecond", 0);


    /**
     * The SLA for successful transactions durations
     */
    private ObservableDouble successfulTransactionDurationSLA = createDoubleProperty("successfulTransactionDurationSLA", 0);

    /**
     * The total number of successful transactions
     */
    private ObservableLong successfulTransactionsCountTotal = createLongProperty("successfulTransactionsCountTotal", 0);

    /**
     * The total number of unsuccessful transactions
     */
    private ObservableLong unsuccessfulTransactionsCountTotal = createLongProperty("unsuccessfulTransactionsCountTotal", 0);

    private ObservableLong successfulTransactionsDurationTotal = createLongProperty("successfulTransactionsDurationTotal", 0);
    private ObservableLong successfulTransactionsIncludingPreandPostDurationTotal = createLongProperty("successfulTransactionsIncludingPreandPostDurationTotal", 0);
    private ObservableLong unsuccessfulTransactionsDurationTotal = createLongProperty("unsuccessfulTransactionsDurationTotal", 0);

    /**
     * The number of unsuccessful transations that will trigger a test failure
     */
    private ObservableLong unsuccessfulTransactionsTotalFailureThreshold = createLongProperty("unsuccessfulTransactionsTotalFailureThreshold", -1);


    /**
     * The minimum number of succesful transactions that need to be processed before we can fail a test due to poor performance
     */
    private ObservableLong successfulTransactionsTotalFailureResultCountMinimum = createLongProperty("successfulTransactionsTotalFailureResultCountMinimum", -1);

    /**
     * The threshold below which we will fail a test due to poor performance on successful transactions
     */
    private ObservableDouble successfulTransactionsDurationFailureThreshold = createDoubleProperty("successfulTransactionsDurationFailureThreshold", Double.NaN);

    /**
     * The number of successful transactions in the time period (ie not total, most likely per second)
     */

    private ObservableDouble successfulMeanTransactionsPerSecond = createDoubleProperty("successfulMeanTransactionsPerSecond", Double.NaN);
    private ObservableDouble unsuccessfulMeanTransactionsPerSecond = createDoubleProperty("unsuccessfulMeanTransactionsPerSecond", Double.NaN);


    private ObservableDouble successfulTransactionDuration = createDoubleProperty("successfulTransactionDuration", Double.NaN);
    private ObservableDouble successfulTransactionTotalDuration = createDoubleProperty("successfulTransactionTotalDuration", Double.NaN);
    private ObservableDouble unsuccessfulTransactionDuration = createDoubleProperty("successfulTransactionDuration", Double.NaN);

    private ObservableInteger threadCount = createIntProperty("threadCount", -1);

    @JsonIgnore private ObservableProperty<ChartLineFormat> chartLineFormat = new ObservableProperty<ChartLineFormat>(null, this);

    private ObservableList<ChartEvent> chartEvents = createListProperty("chartEvents", ChartEvent.class);

    public enum SuccessfulTransactionsDurationFailureType {
        Mean,
        TP90,
        Stddev
    }

    private ObservableProperty<SuccessfulTransactionsDurationFailureType> successfulTransactionsDurationFailureType = createProperty("successfulTransactionsDurationFailureType",
            SuccessfulTransactionsDurationFailureType.class,
            SuccessfulTransactionsDurationFailureType.TP90);


    public ObservableLong getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(ObservableLong testDuration) {
        this.testDuration = testDuration;
    }


    //    private long failedTransactionsCount = 0;
    //    private long successTransactionsCount = 0;

    //    private double failedTransactionsTotalTime = 0;
    //    private double successTransactionsTotalTime = 0;


    //    private boolean isTransaction;

    //    private String transactionName;

    //    private double transactionSLA;

    //    private ObservableDouble targetTransactions = new ObservableDouble(0d, this);
    //    private ObservableInteger resultCount = new ObservableInteger(0, this);

    //    private Object updateLock = new Object();

    //    private double successTransactionTimeFailureThreshold;

    //    private int transactionFailureCountThreshold;

    //    private TransactionTimeThresholdMode transactionTimeThresholdMode = TransactionTimeThresholdMode.Mean;

    //    private long totalSampleDurationMillis;

    //    private int threads;
    //    private int agents;

    //    private final int failureThresholdResultCountMinimum;


    public static class ChartEvent {
        public long time;
        public String text;
    }


    public TransactionResultModel() {

        //        failureThresholdResultCountMinimum = 10;
    }

    //    public TransactionResultModel(String testName,
    //                                  String transactionName,
    //                                  boolean isTransaction,
    //                                  double targetRate,
    //                                  int threads,
    //                                  int agents,
    //                                  double transactionSLA,
    //                                  double failureThreshold,
    //                                  int transactionFailureCountThreshold,
    //                                  int failureThresholdResultCountMinimum,
    //                                  int movingAveragePoints) {
    //        this.testName.set(testName);
    //        //        this.transactionName = transactionName;
    //        //        this.isTransaction = isTransaction;
    //        //        this.transactionSLA = transactionSLA;
    //        //        this.successTransactionTimeFailureThreshold = failureThreshold;
    //        //        this.threads = threads;
    //        //        this.agents = agents;
    //        //        this.transactionFailureCountThreshold = transactionFailureCountThreshold;
    ////        this.targetTransactions.set(targetRate);
    //        //        this.failureThresholdResultCountMinimum = failureThresholdResultCountMinimum;
    //        //        this.movingAveragePoints = movingAveragePoints;
    //        //
    //        //        maTransactionsSuccess = new ConcurrentMovingAverage(movingAveragePoints);
    //        //        maTransactionsFailed = new ConcurrentMovingAverage(movingAveragePoints);
    //        //        maSuccessDuration = new ConcurrentMovingAverage(movingAveragePoints);
    //        //        maFailedDuration = new ConcurrentMovingAverage(movingAveragePoints);
    //        //        maSuccessTotalDuration = new ConcurrentMovingAverage(movingAveragePoints);
    //    }

    public ObservableProperty<String> getTestName() {
        return testName;
    }

    //    public ObservableInteger getResultCount() {
    //        return resultCount;
    //    }

    public ObservableDouble getTp90() {
        return tp90;
    }

    public ObservableDouble getStddev() {
        return stddev;
    }


    public ObservableDouble getSuccessfulTransactionDuration() {
        return successfulTransactionDuration;
    }

    public ObservableDouble getSuccessfulTransactionTotalDuration() {
        return successfulTransactionTotalDuration;
    }

    public ObservableDouble getUnsuccessfulTransactionDuration() {
        return unsuccessfulTransactionDuration;
    }

    //    public int getAgents() {
    //        return agents;
    //    }

    //    public int getTransactionFailureCountThreshold() {
    //        return transactionFailureCountThreshold;
    //    }

    //    public double getFailureThreshold() {
    //        return successTransactionTimeFailureThreshold;
    //    }

    //    public ChartLineFormat getChartLineFormat() {
    //        return chartLineFormat.get();
    //    }

    //    public double getTransactionSLA() {
    //        return transactionSLA;
    //    }

    //    public String getTestName() {
    //        return testName.get();
    //    }

    //    public String getTransactionName() {
    //        return transactionName;
    //    }

    //    public ObservableDouble getTargetTransactions() {
    //        return targetTransactions;
    //    }

    //    public void setTargetTransactions(double targetTransactions) {
    //        this.targetTransactions.set(targetTransactions);
    //    }

    public long calculateTotalTransactions() {
        return successfulTransactionsCountTotal.get() + unsuccessfulTransactionsCountTotal.get();
    }

    //    public long getSuccessTransactions() {
    //        return successfulTransactionsCountPerSecond.get();
    //    }

    //    public long getFailedTransactions() {
    //        return unsuccessfulTransactionsCountPerSecond.get();
    //    }

    //    public double getSuccessPerSecond() {
    //        return maTransactionsSuccess.calculateMovingAverage();
    //    }

    //    public double getFailuresPerSecond() {
    //        return maTransactionsFailed.calculateMovingAverage();
    //    }

    //    public double getSuccessfulTransactionsAverageNanos() {
    //        return maSuccessDuration.calculateMovingAverage();
    //    }

    //    public double getSuccessfulTransactionsTotalAverageNanos() {
    //        return maSuccessTotalDuration.calculateMovingAverage();
    //    }

    //    public double getFailedTransactionsAverageNanos() {
    //        return maFailedDuration.calculateMovingAverage();
    //    }

    //    public double getFailureElapsedMS() {
    //        return getFailedTransactionsAverageNanos() * 1e-6;
    //
    //    }

    //    public double getSuccessElapsedMS() {
    //        return getSuccessfulTransactionsAverageNanos() * 1e-6;
    //    }

    //    public double getSuccessfulTransactionsTotalMS() {
    //        return getSuccessfulTransactionsTotalAverageNanos() * 1e-6;
    //    }

    public double calculateMaximumRate() {
        return 1000d / (successfulTransactionTotalDuration.get() * 1e-6);
    }

    //    public int getThreads() {
    //        return threads;
    //    }

    //    public double getSuccessPerSecondNew() {
    //        return (successfulTransactionsCountPerSecond.get() / threads / agents) / (totalSampleDurationMillis / 1000d);
    //    }

    //    public double getFailedPerSecondNew() {
    //        return (successfulTransactionsCountPerSecond.get() / threads / agents) / (totalSampleDurationMillis / 1000d);
    //    }

    //    public long getTotalSampleDuration() {
    //        return totalSampleDurationMillis;
    //    }

    //    public double getSuccessTimeMeanMillis() {
    //        return getSuccessTransactionsTotalTimeMillis() / getSuccessTransactions();
    //    }

    public ObservableDouble getSuccessfulMeanTransactionsPerSecond() {
        return successfulMeanTransactionsPerSecond;
    }

    public ObservableDouble getUnsuccessfulMeanTransactionsPerSecond() {
        return unsuccessfulMeanTransactionsPerSecond;
    }

    public ObservableLong getUnsuccessfulTransactionsTotalFailureThreshold() {
        return unsuccessfulTransactionsTotalFailureThreshold;
    }

    public ObservableDouble getSuccessfulTransactionsDurationFailureThreshold() {
        return successfulTransactionsDurationFailureThreshold;
    }

    public ObservableLong getUnsuccessfulTransactionsCountTotal() {
        return unsuccessfulTransactionsCountTotal;
    }

    //    public double getFailedTimeMeanMillis() {
    //        return getFailedTransactionsTotalTimeMillis() / getFailedTransactions();
    //    }

    //    public double getSuccessTransactionsTotalTimeMillis() {
    //        return successTransactionsTotalTime * 1e-6;
    //    }

    //    public double getFailedTransactionsTotalTimeMillis() {
    //        return failedTransactionsTotalTime * 1e-6;
    //    }

    //    public double getSuccessTransactionsTotalTime() {
    //        return successTransactionsTotalTime;
    //    }

    //    public double getFailedTransactionsTotalTime() {
    //        return failedTransactionsTotalTime;
    //    }

    //    public void setChartLineFormat(ChartLineFormat chartLineFormat) {
    //        this.chartLineFormat.set(chartLineFormat);
    //    }

    public boolean isTransaction() {
        boolean isTransaction = transactionName.get().length() != 0;
        return isTransaction;
    }

    public ObservableList<ChartEvent> getChartEvents() {
        return chartEvents;
    }

    public String getKey() {
        String key;

        if (isTransaction()) {
            key = testName.get() + "." + transactionName;
        } else {
            key = testName.get();
        }

        return key;
    }

    //    @Override public String toString() {
    //        return "TransactionResultModel [testName=" +
    //                testName.getName() +
    //                ", movingAveragePoints=" +
    //                movingAveragePoints +
    //                ", transactionsSuccess=" +
    //                maTransactionsSuccess +
    //                ", transactionsFailed=" +
    //                maTransactionsFailed +
    //                ", successDuration=" +
    //                maSuccessDuration +
    //                ", failedDuration=" +
    //                maFailedDuration +
    //                ", successTotalDuration=" +
    //                maSuccessTotalDuration +
    //                ", totalTransactions=" +
    //                calculateTotalTransactions() +
    //                ", chartLineFormat=" +
    //                chartLineFormat +
    //                ", isTransaction=" +
    //                isTransaction +
    //                ", transactionName=" +
    //                transactionName +
    //                ", transactionSLA=" +
    //                transactionSLA +
    //                ", targetTransactions=" +
    //                targetTransactions +
    //                ", updateLock=" +
    //                updateLock +
    //                "]";
    //    }


    //    public TransactionTimeThresholdMode getFailureThresholdMode() {
    //        return transactionTimeThresholdMode;
    //    }

    //    public void setFailureThresholdMode(TransactionTimeThresholdMode mode) {
    //        this.transactionTimeThresholdMode = mode;
    //    }

    //    public int getFailureThresholdResultCountMinimum() {
    //        return failureThresholdResultCountMinimum;
    //    }


    public ObservableProperty<SuccessfulTransactionsDurationFailureType> getSuccessfulTransactionsDurationFailureType() {
        return successfulTransactionsDurationFailureType;
    }

    public ObservableLong getSuccessfulTransactionsTotalFailureResultCountMinimum() {
        return successfulTransactionsTotalFailureResultCountMinimum;
    }

    public ObservableLong getSuccessfulTransactionsCountTotal() {
        return successfulTransactionsCountTotal;
    }

    public ObservableDouble getTargetSuccessfulTransactionsPerSecond() {
        return targetSuccessfulTransactionsPerSecond;
    }

    public ObservableProperty<String> getTransactionName() {
        return transactionName;
    }

    public ObservableProperty<ChartLineFormat> getChartLineFormat() {
        return chartLineFormat;
    }

    public ObservableDouble getSuccessfulTransactionDurationSLA() {
        return successfulTransactionDurationSLA;
    }

    public ObservableLong getModelUpdates() {
        return modelUpdates;
    }

    public ObservableLong getSuccessfulTransactionsDurationTotal() {
        return successfulTransactionsDurationTotal;
    }

    public ObservableLong getUnsuccessfulTransactionsDurationTotal() {
        return unsuccessfulTransactionsDurationTotal;
    }

    public ObservableInteger getThreadCount() {
        return threadCount;
    }

    public ObservableLong getSuccessfulTransactionsIncludingPreandPostDurationTotal() {
        return successfulTransactionsIncludingPreandPostDurationTotal;
    }
}

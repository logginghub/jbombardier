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

import org.codehaus.jackson.annotate.JsonIgnore;

import com.logginghub.utils.ConcurrentMovingAverage;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableDouble;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

public class TransactionResultModel extends Observable {

    public enum TransactionTimeThresholdMode {
        Mean,
        TP90,
        Stddev
    }

    private String testName;

    // TODO : this will be a bad approximation if the samples are arriving at
    // random times!
    private final static int defaultMovingAveragePoints = 5;
    private int movingAveragePoints = defaultMovingAveragePoints;

    private ConcurrentMovingAverage maTransactionsSuccess = new ConcurrentMovingAverage(defaultMovingAveragePoints);
    private ConcurrentMovingAverage maTransactionsFailed = new ConcurrentMovingAverage(defaultMovingAveragePoints);
    private ConcurrentMovingAverage maSuccessDuration = new ConcurrentMovingAverage(defaultMovingAveragePoints);
    private ConcurrentMovingAverage maFailedDuration = new ConcurrentMovingAverage(defaultMovingAveragePoints);
    private ConcurrentMovingAverage maSuccessTotalDuration = new ConcurrentMovingAverage(defaultMovingAveragePoints);

    private long failedTransactionsCount = 0;
    private long successTransactionsCount = 0;

    private double failedTransactionsTotalTime = 0;
    private double successTransactionsTotalTime = 0;

    @JsonIgnore private ObservableProperty<ChartLineFormat> chartLineFormat = new ObservableProperty<ChartLineFormat>(null, this);

    private ObservableDouble tp90 = new ObservableDouble(Double.NaN);
    private ObservableDouble stddev = new ObservableDouble(Double.NaN);

    private boolean isTransaction;

    private String transactionName;

    private double transactionSLA;

    private ObservableDouble targetTransactions = new ObservableDouble(0d, this);
    private ObservableInteger resultCount = new ObservableInteger(0, this);

    private Object updateLock = new Object();

    private double successTransactionTimeFailureThreshold;

    private int transactionFailureCountThreshold;

    private TransactionTimeThresholdMode transactionTimeThresholdMode = TransactionTimeThresholdMode.Mean;

    private long totalSampleDurationMillis;

    private int threads;
    private int agents;

    private final int failureThresholdResultCountMinimum;

    
    public static class ChartEvent {
        public long time;
        public String text;
    }
    
    private ObservableList<ChartEvent> chartEvents = createListProperty("chartEvents", ChartEvent.class);
    
    public TransactionResultModel() {

        failureThresholdResultCountMinimum = 10;
    }

    public TransactionResultModel(String testName,
                                  String transactionName,
                                  boolean isTransaction,
                                  double targetRate,
                                  int threads,
                                  int agents,
                                  double transactionSLA,
                                  double failureThreshold,
                                  int transactionFailureCountThreshold,
                                  int failureThresholdResultCountMinimum,
                                  int movingAveragePoints) {
        this.testName = testName;
        this.transactionName = transactionName;
        this.isTransaction = isTransaction;
        this.transactionSLA = transactionSLA;
        this.successTransactionTimeFailureThreshold = failureThreshold;
        this.threads = threads;
        this.agents = agents;
        this.transactionFailureCountThreshold = transactionFailureCountThreshold;
        this.targetTransactions.set(targetRate);
        this.failureThresholdResultCountMinimum = failureThresholdResultCountMinimum;
        this.movingAveragePoints = movingAveragePoints;
        
        maTransactionsSuccess = new ConcurrentMovingAverage(movingAveragePoints);
        maTransactionsFailed = new ConcurrentMovingAverage(movingAveragePoints);
        maSuccessDuration = new ConcurrentMovingAverage(movingAveragePoints);
        maFailedDuration = new ConcurrentMovingAverage(movingAveragePoints);
        maSuccessTotalDuration = new ConcurrentMovingAverage(movingAveragePoints);
    }

    public int getAgents() {
        return agents;
    }

    public int getTransactionFailureCountThreshold() {
        return transactionFailureCountThreshold;
    }

    public double getFailureThreshold() {
        return successTransactionTimeFailureThreshold;
    }

    public ChartLineFormat getChartLineFormat() {
        return chartLineFormat.get();
    }

    public double getTransactionSLA() {
        return transactionSLA;
    }

    public String getTestName() {
        return testName;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public ObservableDouble getTargetTransactions() {
        return targetTransactions;
    }

    public void setTargetTransactions(double targetTransactions) {
        this.targetTransactions.set(targetTransactions);
    }

    public long getTotalTransactions() {
        return successTransactionsCount + failedTransactionsCount;
    }

    public long getSuccessTransactions() {
        return successTransactionsCount;
    }

    public long getFailedTransactions() {
        return failedTransactionsCount;
    }

    public double getSuccessPerSecond() {
        return maTransactionsSuccess.calculateMovingAverage();
    }

    public double getFailuresPerSecond() {
        return maTransactionsFailed.calculateMovingAverage();
    }

    public double getSuccessfulTransactionsAverageNanos() {
        return maSuccessDuration.calculateMovingAverage();
    }

    public double getSuccessfulTransactionsTotalAverageNanos() {
        return maSuccessTotalDuration.calculateMovingAverage();
    }

    public double getFailedTransactionsAverageNanos() {
        return maFailedDuration.calculateMovingAverage();
    }

    public double getFailureElapsedMS() {
        return getFailedTransactionsAverageNanos() * 1e-6;

    }

    public double getSuccessElapsedMS() {
        return getSuccessfulTransactionsAverageNanos() * 1e-6;
    }

    public double getSuccessfulTransactionsTotalMS() {
        return getSuccessfulTransactionsTotalAverageNanos() * 1e-6;
    }

    public double getMaximumRate() {
        return 1000d / getSuccessfulTransactionsTotalMS();
    }

    public void update(long successCount,
                       long failureCount,
                       double successPerSecondTotal,
                       double failurePerSecondTotal,
                       double averageSuccessDuration,
                       double averageSuccessTotalDuration,
                       double averageFailureDuration,
                       double successTotalTime,
                       double failureTotalTime,
                       long sampleDuration) {

        this.failedTransactionsCount += failureCount;
        this.successTransactionsCount += successCount;

        this.successTransactionsTotalTime += successTotalTime;
        this.failedTransactionsTotalTime += failureTotalTime;

        this.totalSampleDurationMillis += sampleDuration;

        maTransactionsSuccess.addValue(successPerSecondTotal);
        maTransactionsFailed.addValue(failurePerSecondTotal);

        if (successPerSecondTotal > 0) {
            maSuccessDuration.addValue(averageSuccessDuration);
        }

        if (failurePerSecondTotal > 0) {
            maFailedDuration.addValue(averageFailureDuration);
        }

        maSuccessTotalDuration.addValue(averageSuccessTotalDuration);
        resultCount.increment(1);
    }

    public int getThreads() {
        return threads;
    }

    public double getSuccessPerSecondNew() {
        return (successTransactionsCount / threads / agents) / (totalSampleDurationMillis / 1000d);
    }

    public double getFailedPerSecondNew() {
        return (failedTransactionsCount / threads / agents) / (totalSampleDurationMillis / 1000d);
    }

    public long getTotalSampleDuration() {
        return totalSampleDurationMillis;
    }

    public double getSuccessTimeMeanMillis() {
        return getSuccessTransactionsTotalTimeMillis() / getSuccessTransactions();
    }

    public double getFailedTimeMeanMillis() {
        return getFailedTransactionsTotalTimeMillis() / getFailedTransactions();
    }

    public double getSuccessTransactionsTotalTimeMillis() {
        return successTransactionsTotalTime * 1e-6;
    }

    public double getFailedTransactionsTotalTimeMillis() {
        return failedTransactionsTotalTime * 1e-6;
    }

    public double getSuccessTransactionsTotalTime() {
        return successTransactionsTotalTime;
    }

    public double getFailedTransactionsTotalTime() {
        return failedTransactionsTotalTime;
    }

    public void setChartLineFormat(ChartLineFormat chartLineFormat) {
        this.chartLineFormat.set(chartLineFormat);
    }

    public boolean isTransaction() {
        return isTransaction;
    }
    
    public ObservableList<ChartEvent> getChartEvents() {
        return chartEvents;
    }

    public String getKey() {
        String key;
        if (transactionName.length() == 0) {
            key = testName;
        }
        else {
            key = testName + "." + transactionName;
        }
        return key;
    }

    @Override public String toString() {
        return "TransactionResultModel [testName=" +
               testName +
               ", movingAveragePoints=" +
               movingAveragePoints +
               ", transactionsSuccess=" +
               maTransactionsSuccess +
               ", transactionsFailed=" +
               maTransactionsFailed +
               ", successDuration=" +
               maSuccessDuration +
               ", failedDuration=" +
               maFailedDuration +
               ", successTotalDuration=" +
               maSuccessTotalDuration +
               ", totalTransactions=" +
               getTotalTransactions() +
               ", chartLineFormat=" +
               chartLineFormat +
               ", isTransaction=" +
               isTransaction +
               ", transactionName=" +
               transactionName +
               ", transactionSLA=" +
               transactionSLA +
               ", targetTransactions=" +
               targetTransactions +
               ", updateLock=" +
               updateLock +
               "]";
    }

    public ObservableInteger getResultCount() {
        return resultCount;
    }

    public ObservableDouble getTp90() {
        return tp90;
    }

    public ObservableDouble getStddev() {
        return stddev;
    }

    public TransactionTimeThresholdMode getFailureThresholdMode() {
        return transactionTimeThresholdMode;
    }

    public void setFailureThresholdMode(TransactionTimeThresholdMode mode) {
        this.transactionTimeThresholdMode = mode;
    }

    public int getFailureThresholdResultCountMinimum() {
        return failureThresholdResultCountMinimum;
    }

}

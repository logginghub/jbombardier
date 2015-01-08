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

package com.jbombardier.console.model.result;

import com.jbombardier.console.model.TransactionResultModel;

public class TransactionResultSnapshot {

    private double failedDuration = Double.NaN;
    private double sla = Double.NaN;
    private double successDuration = Double.NaN;
    private double successTotalDuration = Double.NaN;
    private String testName;
    private long transactionCount = 0;
    private String transactionName;
    private double transactionsFailed = Double.NaN;
    private double transactionsSuccess = Double.NaN;
    private long testTime;

    public static TransactionResultSnapshot fromModel(TransactionResultModel value) {

        TransactionResultSnapshot snapshot = new TransactionResultSnapshot();

        snapshot.setTransactionsSuccess(value.getSuccessPerSecond());
        snapshot.setTransactionsFailed(value.getFailuresPerSecond());

        snapshot.setSuccessDuration(value.getSuccessfulTransactionsAverageNanos());
        snapshot.setFailedDuration(value.getFailedTransactionsAverageNanos());
        snapshot.setSuccessTotalDuration(value.getSuccessfulTransactionsTotalAverageNanos());

        snapshot.setTestName(value.getTestName());
        snapshot.setTransactionName(value.getTransactionName());
        snapshot.setTransactionCount(value.getTotalTransactions());
        snapshot.setSla(value.getTransactionSLA());

        return snapshot;
    }

    public double getFailedDuration() {
        return failedDuration;
    }

    public double getSla() {
        return sla;
    }

    public double getSuccessDuration() {
        return successDuration;
    }

    public double getSuccessTotalDuration() {
        return successTotalDuration;
    }

    public String getTestName() {
        return testName;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public double getTransactionsFailed() {
        return transactionsFailed;
    }

    public double getTransactionsSuccess() {
        return transactionsSuccess;
    }

    public void setFailedDuration(double failedDuration) {
        this.failedDuration = failedDuration;
    }

    public void setSla(double sla) {
        this.sla = sla;
    }

    public void setSuccessDuration(double successDuration) {
        this.successDuration = successDuration;
    }

    public double getSuccessDurationMS() {
        return successDuration * 1e-6;
    }

    public double getFailedDurationMS() {
        return failedDuration * 1e-6;
    }

    public void setSuccessTotalDuration(double successTotalDuration) {
        this.successTotalDuration = successTotalDuration;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public void setTransactionsFailed(double transactionsFailed) {
        this.transactionsFailed = transactionsFailed;
    }

    public void setTransactionsSuccess(double transactionsSuccess) {
        this.transactionsSuccess = transactionsSuccess;
    }

    public void setTestTime(long testTime) {
        this.testTime = testTime;
    }

    public long getTestTime() {
        return testTime;
    }
    
    // private long totalTransactions = 0;
    // private boolean isTransaction;
    // private String transactionName;
    // private double transactionSLA;
    // private double targetTransactions = 0;
    // private double maximumRate;
    // private double successfulTransactionsTotalMS;

}

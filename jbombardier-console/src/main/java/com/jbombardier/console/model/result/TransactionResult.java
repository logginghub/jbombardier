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

/**
 * Encapsulation of a TransactionResultModel for a single test/transaction. Serialised into JSON and sent to the repository to keep a record
 * of the test run as part of the TestRunResult object.
 */
public class TransactionResult {

//    // Counters for the number of transactions per second, and total transactions during the phase
//    private long successfulTransactionsCountTotal;
//    private long successfulTransactionsDurationTotal;
//
//    private double successfulTransactionsCountPerSecond;
//
//
//    // Counters for the total durations
//    private long unsuccessfulTransactionsDurationTotal;
//
//    private long unsuccessfulTransactionsCountTotal;
//    private double unsuccessfulTransactionsCountPerSecond;
//
//    private double successfulTransactionsCountPerSecond;
//    private double unsuccessfulTransactionsCountPerSecond;
//
//
//    private long unsuccessfulTransactionsTotalFailureThreshold;
//    private long successfulTransactionsTotalFailureResultCountMinimum;
//
//    private double targetSuccessfulTransactionsPerSecond;
//    private double successfulTransactionDurationSLA;
//    private double successfulTransactionsDurationFailureThreshold;
//
//    private double successfulTransactionDuration;
//    private double successfulTransactionTotalDuration;
//    private double unsuccessfulTransactionDuration;


    private double unsucccesfulDuration = Double.NaN;
    private double sla = Double.NaN;
    private double successDuration = Double.NaN;
    private double successTotalDuration = Double.NaN;
    private String testName;
    private long transactionCount = 0;
    private String transactionName;
    private double transactionsUnsuccessful = Double.NaN;
    private double transactionsSuccessful = Double.NaN;
    private long testTime;

    public static TransactionResult fromModel(TransactionResultModel value) {

        TransactionResult snapshot = new TransactionResult();

        snapshot.setTransactionsSuccessful(value.getSuccessfulTransactionsTotal().get());
        snapshot.setTransactionsUnsuccessful(value.getUnsuccessfulTransactionsTotal().get());

        snapshot.setSuccessDuration(value.getSuccessfulTransactionDuration().get());
        snapshot.setUnsucccesfulDuration(value.getUnsuccessfulTransactionDuration().get());
        snapshot.setSuccessTotalDuration(value.getSuccessfulTransactionTotalDuration().get());

        snapshot.setTestName(value.getTestName().get());
        snapshot.setTransactionName(value.getTransactionName().get());
        snapshot.setTransactionCount(value.calculateTotalTransactions());
        snapshot.setSla(value.getSuccessfulTransactionDurationSLA().get());

        return snapshot;
    }

    public double getUnsucccesfulDuration() {
        return unsucccesfulDuration;
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

    public double getTransactionsUnsuccessful() {
        return transactionsUnsuccessful;
    }

    public double getTransactionsSuccessful() {
        return transactionsSuccessful;
    }

    public void setUnsucccesfulDuration(double unsucccesfulDuration) {
        this.unsucccesfulDuration = unsucccesfulDuration;
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

    public double getUnsuccessfulDurationMS() {
        return unsucccesfulDuration * 1e-6;
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

    public void setTransactionsUnsuccessful(double transactionsUnsuccessful) {
        this.transactionsUnsuccessful = transactionsUnsuccessful;
    }

    public void setTransactionsSuccessful(double transactionsSuccessful) {
        this.transactionsSuccessful = transactionsSuccessful;
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

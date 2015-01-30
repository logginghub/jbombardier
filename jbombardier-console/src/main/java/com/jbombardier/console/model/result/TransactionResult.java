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

    private double sla = Double.NaN;

    private String testName;
    private long totalTransactionCount = 0;
    private String transactionName;

    private long testTime;
    private long successfulTransactionCount;
    private double successfulTransactionMeanDuration;
    private double successfulTransactionMeanTransactionsPerSecond;
    private long unsuccessfulTransactionCount;
    private double unsuccessfulTransactionMeanDuration;
    private double unsuccessfulTransactionMeanTransactionsPerSecond;
    private double successfulTransactionMeanTotalDuration;
    private double successfulTransactionMeanTransactionsPerSecondTarget;

    //    public static TransactionResult fromModel(TransactionResultModel value) {
//
//        TransactionResult snapshot = new TransactionResult();
//
//        snapshot.setTransactionsSuccessful(value.getSuccessfulTransactionsCountTotal().get());
//        snapshot.setTransactionsUnsuccessful(value.getUnsuccessfulTransactionsCountTotal().get());
//
//        snapshot.setSuccessDuration(value.getSuccessfulTransactionDuration().get());
//        snapshot.setUnsucccesfulDuration(value.getUnsuccessfulTransactionDuration().get());
//        snapshot.setSuccessTotalDuration(value.getSuccessfulTransactionTotalDuration().get());
//
//        snapshot.setTestName(value.getTestName().get());
//        snapshot.setTransactionName(value.getTransactionName().get());
//        snapshot.setTotalTransactionCount(value.calculateTotalTransactions());
//        snapshot.setSla(value.getSuccessfulTransactionDurationSLA().get());
//
//        return snapshot;
//    }


    public double getSla() {
        return sla;
    }


    public String getTestName() {
        return testName;
    }

    public long getTotalTransactionCount() {
        return totalTransactionCount;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setSla(double sla) {
        this.sla = sla;
    }



    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setTotalTransactionCount(long totalTransactionCount) {
        this.totalTransactionCount = totalTransactionCount;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public void setTestTime(long testTime) {
        this.testTime = testTime;
    }

    public long getTestTime() {
        return testTime;
    }

    public long getSuccessfulTransactionCount() {
        return successfulTransactionCount;
    }

    public void setSuccessfulTransactionCount(long successfulTransactionCount) {
        this.successfulTransactionCount = successfulTransactionCount;
    }

    public double getSuccessfulTransactionMeanDuration() {
        return successfulTransactionMeanDuration;
    }

    public void setSuccessfulTransactionMeanDuration(double successfulTransactionMeanDuration) {
        this.successfulTransactionMeanDuration = successfulTransactionMeanDuration;
    }

    public double getSuccessfulTransactionMeanTransactionsPerSecond() {
        return successfulTransactionMeanTransactionsPerSecond;
    }

    public void setSuccessfulTransactionMeanTransactionsPerSecond(double successfulTransactionMeanTransactionsPerSecond) {
        this.successfulTransactionMeanTransactionsPerSecond = successfulTransactionMeanTransactionsPerSecond;
    }

    public long getUnsuccessfulTransactionCount() {
        return unsuccessfulTransactionCount;
    }

    public void setUnsuccessfulTransactionCount(long unsuccessfulTransactionCount) {
        this.unsuccessfulTransactionCount = unsuccessfulTransactionCount;
    }

    public double getUnsuccessfulTransactionMeanDuration() {
        return unsuccessfulTransactionMeanDuration;
    }

    public void setUnsuccessfulTransactionMeanDuration(double unsuccessfulTransactionMeanDuration) {
        this.unsuccessfulTransactionMeanDuration = unsuccessfulTransactionMeanDuration;
    }

    public double getUnsuccessfulTransactionMeanTransactionsPerSecond() {
        return unsuccessfulTransactionMeanTransactionsPerSecond;
    }

    public void setUnsuccessfulTransactionMeanTransactionsPerSecond(double unsuccessfulTransactionMeanTransactionsPerSecond) {
        this.unsuccessfulTransactionMeanTransactionsPerSecond = unsuccessfulTransactionMeanTransactionsPerSecond;
    }

    public void setSuccessfulTransactionMeanTotalDuration(double successfulTransactionMeanTotalDuration) {
        this.successfulTransactionMeanTotalDuration = successfulTransactionMeanTotalDuration;
    }

    public double getSuccessfulTransactionMeanTotalDuration() {
        return successfulTransactionMeanTotalDuration;
    }

    public void setSuccessfulTransactionMeanTransactionsPerSecondTarget(double successfulTransactionMeanTransactionsPerSecondTarget) {
        this.successfulTransactionMeanTransactionsPerSecondTarget = successfulTransactionMeanTransactionsPerSecondTarget;
    }

    public double getSuccessfulTransactionMeanTransactionsPerSecondTarget() {
        return successfulTransactionMeanTransactionsPerSecondTarget;
    }


    // private long totalTransactions = 0;
    // private boolean isTransaction;
    // private String transactionName;
    // private double transactionSLA;
    // private double targetTransactions = 0;
    // private double maximumRate;
    // private double successfulTransactionsTotalMS;

}

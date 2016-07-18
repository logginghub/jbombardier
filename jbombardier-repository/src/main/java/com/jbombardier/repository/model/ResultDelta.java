/*
 * Copyright (c) 2009-2016 Vertex Labs Limited.
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

package com.jbombardier.repository.model;

public class ResultDelta {

    private String test;

    private double currentTransactions;
    private double currentTransactionTime;

    private double deltaTransactionsTotal;
    private double deltaTransactionsPerSecond;
    private double deltaTransactionTime;

    private double sla;

    private long transactionCount;

    private double percentageDeltaTransactions;

    private double percentageDeltaTime;

    private double percentageDeltaTotalTransactions;

    public void setTest(String test) {
        this.test = test;
    }

    public String getTest() {
        return test;
    }

    public void setCurrentTransactions(double currentTransactions) {
        this.currentTransactions = currentTransactions;
    }

    public void setCurrentTransactionTime(double currentTransactionTime) {
        this.currentTransactionTime = currentTransactionTime;
    }

    public double getCurrentTransactions() {
        return currentTransactions;
    }

    public double getCurrentTransactionTime() {
        return currentTransactionTime;
    }

    public void setDeltaTransactionsTotal(double deltaTransactionsTotal) {
        this.deltaTransactionsTotal = deltaTransactionsTotal;
    }

    public double getDeltaTransactionsTotal() {
        return deltaTransactionsTotal;
    }

    public void setDeltaSuccessTransactionTime(double deltaTransactionTime) {
        this.deltaTransactionTime = deltaTransactionTime;
    }

    public void setDeltaSuccessTransactionsPerSecond(double deltaTransactions) {
        this.deltaTransactionsPerSecond = deltaTransactions;
    }

    public double getDeltaTransactionsPerSecond() {
        return deltaTransactionsPerSecond;
    }

    public double getDeltaTransactionTime() {
        return deltaTransactionTime;
    }

    public void setSLA(double sla) {
        this.sla = sla;
    }

    public double getSla() {
        return sla;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setPercentageDeltaTransactionsPerSecond(double percentageDeltaTransactions) {
        this.percentageDeltaTransactions = percentageDeltaTransactions;
    }

    public double getPercentageDeltaTransactions() {
        return percentageDeltaTransactions;
    }

    public void setPercentageDeltaTransactionTime(double percentageDeltaTime) {
        this.percentageDeltaTime = percentageDeltaTime;
    }

    public double getPercentageDeltaTransactionTime() {
        return percentageDeltaTime;
    }

    public void setPercentageDeltaTotalTransactions(double percentageDeltaTotalTransactions) {
        this.percentageDeltaTotalTransactions = percentageDeltaTotalTransactions;
    }
    
    public double getPercentageDeltaTotalTransactions() {
        return percentageDeltaTotalTransactions;
    }

}

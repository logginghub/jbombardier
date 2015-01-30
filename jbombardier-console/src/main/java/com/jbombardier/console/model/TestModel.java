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

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableDouble;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;

import java.util.HashMap;
import java.util.Map;

public class TestModel extends Observable {

    private ObservableInteger targetThreads = createIntProperty("targetThreads", 1);
    private ObservableInteger threadStep = createIntProperty("threadStep", 1);
    private ObservableLong threadStepTime = createLongProperty("threadStepTime", 1000);
    private ObservableDouble targetRate = createDoubleProperty("targetRate", 1);

    private ObservableProperty<String> agent = createStringProperty("agent", null);

    // TODO : none of the rest of the parameters are observable... they probably should be?
    private String name;
    private String classname;
    private float rateStep = 1;
    private long rateStepTime = 1000;
    private boolean recordAllValues;
    private Map<String, String> properties = new HashMap<String, String>();
    private Map<String, Double> transactionSLAs = new HashMap<String, Double>();

    private double failureThreshold = Double.NaN;
    private TransactionResultModel.SuccessfulTransactionsDurationFailureType failureThresholdMode = TransactionResultModel.SuccessfulTransactionsDurationFailureType.Mean;
    private int failedTransactionCountThreshold = -1;
    private int failureThresholdResultCountMinimum = 10;
    private int movingAveragePoints = 10;

    public TestModel() {}

    public TestModel(String name, String classname) {
        this.name = name;
        this.classname = classname;
    }

    public void setMovingAveragePoints(int movingAveragePoints) {
        this.movingAveragePoints = movingAveragePoints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFailedTransactionCountThreshold() {
        return failedTransactionCountThreshold;
    }

    public void setFailureThresholdResultCountMinimum(int failureThresholdResultCountMinimum) {
        this.failureThresholdResultCountMinimum = failureThresholdResultCountMinimum;
    }

    public void setFailedTransactionCountThreshold(int failedTransactionCountThreshold) {
        this.failedTransactionCountThreshold = failedTransactionCountThreshold;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public ObservableDouble getTargetRate() {
        return targetRate;
    }

    public ObservableInteger getTargetThreads() {
        return targetThreads;
    }

    public ObservableInteger getThreadStep() {
        return threadStep;
    }

    public ObservableLong getThreadStepTime() {
        return threadStepTime;
    }

    public float getRateStep() {
        return rateStep;
    }

    public void setRateStep(float rateStep) {
        this.rateStep = rateStep;
    }

    public long getRateStepTime() {
        return rateStepTime;
    }

    public void setRateStepTime(long rateStepTime) {
        this.rateStepTime = rateStepTime;
    }

    public void setRecordAllValues(boolean recordAllValues) {
        this.recordAllValues = recordAllValues;
    }

    public boolean getRecordAllValues() {
        return recordAllValues;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setTransactionSLAs(Map<String, Double> transactionSLAs) {
        this.transactionSLAs = transactionSLAs;
    }

    public Map<String, Double> getTransactionSLAs() {
        return transactionSLAs;
    }

    public double getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(double failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public void setFailureThresholdMode(TransactionResultModel.SuccessfulTransactionsDurationFailureType failureThresholdMode) {
        this.failureThresholdMode = failureThresholdMode;
    }

    public TransactionResultModel.SuccessfulTransactionsDurationFailureType getFailureThresholdMode() {
        return failureThresholdMode;
    }

    @Override public String toString() {
        return "TestModel [name=" + name + ", classname=" + classname + ", targetThreads=" + targetThreads + ", targetRate=" + targetRate + "]";
    }

    public int getFailureThresholdResultCountMinimum() {
        return failureThresholdResultCountMinimum;
    }

    public int getMovingAveragePoints() {
        return this.movingAveragePoints;
    }

    public ObservableProperty<String> getAgent() {
        return agent;
    }
}

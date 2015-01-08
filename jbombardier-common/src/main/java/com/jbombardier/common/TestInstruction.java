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

package com.jbombardier.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "classname", "threads" }) public class TestInstruction {
    private String classname;

    private int targetThreads;
    private long threadRampupTime;
    private int threadRampupStep;

    private long duration = -1;

    private double targetRate;
    private double rateStep;
    private long rateStepTime;
    
    private String testName;

    private boolean recordAllValues;
    
    private Map<String, String> properties = new HashMap<String, String>();

    private double transactionRateModifier =1;

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    public void setThreadRampupTime(long threadRampupTime) {
        this.threadRampupTime = threadRampupTime;
    }

    @XmlAttribute public long getThreadRampupTime() {
        return threadRampupTime;
    }

    @XmlAttribute public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @XmlAttribute public long getDuration() {
        return duration;
    }

    public void setThreadRampupStep(int threadRampupStep) {
        this.threadRampupStep = threadRampupStep;
    }

    @XmlAttribute public int getThreadRampupStep() {
        return threadRampupStep;
    }

    public void setTargetThreads(int targetThreads) {
        this.targetThreads = targetThreads;
    }

    @XmlAttribute public int getTargetThreads() {
        return targetThreads;
    }
    
    public void setRateStepTime(long rateStepTime) {
        this.rateStepTime = rateStepTime;
    }

    @XmlAttribute public void setRateStep(double rateStep) {
        this.rateStep = rateStep;
    }

    public void setTargetRate(double targetRate) {
        this.targetRate = targetRate;
    }

    @XmlAttribute public double getRateStep() {
        return rateStep;
    }

    @XmlAttribute public long getRateStepTime() {
        return rateStepTime;
    }

    @XmlAttribute public double getTargetRate() {
        return targetRate;
    }

    @Override public String toString() {
        return "TestInstruction [classname=" +
               classname +
               ", targetThreads=" +
               targetThreads +
               ", threadRampupTime=" +
               threadRampupTime +
               ", threadRampupStep=" +
               threadRampupStep +
               ", duration=" +
               duration +
               ", targetRate=" +
               targetRate +
               ", rateStep=" +
               rateStep +
               ", rateStepTime=" +
               rateStepTime +
               ", recordAllValues=" +
               recordAllValues +
               "]";
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

    @XmlAttribute public double getTransactionRateModifier() {
        return transactionRateModifier;         
    }
    
    public void setTransactionRateModifier(double transactionRateModifier) {
        this.transactionRateModifier = transactionRateModifier;
    }
}
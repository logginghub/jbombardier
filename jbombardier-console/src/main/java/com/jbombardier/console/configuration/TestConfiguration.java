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

package com.jbombardier.console.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.jbombardier.console.model.TransactionResultModel;

@SuppressWarnings("restriction") @XmlAccessorType(XmlAccessType.FIELD) public class TestConfiguration {

    @XmlAttribute private String name;
    @XmlAttribute(name = "class")  private String classname;
    @XmlAttribute private String properties ="";
    @XmlAttribute private int targetThreads = 1;
    @XmlAttribute private int threadStep = 1;
    @XmlAttribute private long threadStepTime = 1000;
    @XmlAttribute private float targetRate = 1;
    @XmlAttribute private float rateStep = 1;
    @XmlAttribute private long rateStepTime = 1000;
    @XmlAttribute private boolean recordAllValues = true;
    @XmlAttribute private double sla = Double.NaN;
    
    @XmlAttribute private double failureThreshold = Double.NaN;
    @XmlAttribute private TransactionResultModel.SuccessfulTransactionsDurationFailureType failureThresholdMode = TransactionResultModel.SuccessfulTransactionsDurationFailureType.Mean;
    @XmlAttribute private int failedTransactionCountFailureThreshold = -1;
    @XmlAttribute private int failureThresholdResultCountMinimum = 10;
    @XmlAttribute private int movingAveragePoints = 5;
    @XmlAttribute private String agent = null;
    
    @XmlElementWrapper(name = "properties") @XmlElement(name = "property") private List<Property> propertiesList = new ArrayList<Property>();
    @XmlElementWrapper(name = "transactionSLAs") @XmlElement(name = "transactionSLA") private List<TransactionSLA> transactionSLAs = new ArrayList<TransactionSLA>();
    
    public String getProperties() {
        return properties;
    }
    
    public void setFailureThresholdResultCountMinimum(int failureThresholdResultCountMinimum) {
        this.failureThresholdResultCountMinimum = failureThresholdResultCountMinimum;
    }
    
    public void setFailedTransactionCountFailureThreshold(int failedTransactionCountFailureThreshold) {
        this.failedTransactionCountFailureThreshold = failedTransactionCountFailureThreshold;
    }
    
    public int getFailedTransactionCountFailureThreshold() {
        return failedTransactionCountFailureThreshold;
    }
     
    public void setProperties(String properties) {
        this.properties = properties;
    }
        
    public List<Property> getPropertiesList() {
        return propertiesList;
    }

    public void setPropertiesList(List<Property> properties) {
        this.propertiesList = properties;
    }

    public String getName() {
        return name;
    }
    
    public void setSla(double tp90sla) {
        this.sla = tp90sla;
    }
    
    public double getSla() {
        return sla;
    }

   public void setRecordAllValues(boolean recordAllValues) {
        this.recordAllValues = recordAllValues;
    }

    public boolean getRecordAllValues() {
        return recordAllValues;
    }

   public void setName(String name) {
        this.name = name;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public int getTargetThreads() {
        return targetThreads;
    }

   public void setTargetThreads(int targetThreads) {
        this.targetThreads = targetThreads;
    }

    public int getThreadStep() {
        return threadStep;
    }

   public void setThreadStep(int threadStep) {
        this.threadStep = threadStep;
    }

    public long getThreadStepTime() {
        return threadStepTime;
    }

   public void setThreadStepTime(long threadStepTime) {
        this.threadStepTime = threadStepTime;
    }

    public float getTargetRate() {
        return targetRate;
    }

   public void setTargetRate(float targetRate) {
        this.targetRate = targetRate;
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

    public Map<String, String> buildPropertyMap() {

        Map<String, String> propertyMap = new HashMap<String, String>();

        if (propertiesList != null) {
            for (Property property : propertiesList) {
                propertyMap.put(property.getName(), property.getValue());
            }
        }
        
        if(properties != null && !properties.trim().isEmpty()) {
            String[] split = properties.split(",");
            for (String string : split) {
                String[] split2 = string.split("=");
                propertyMap.put(split2[0], split2[1]);
            }
        }
        
        return propertyMap;
    }

    public Map<String, Double> buildSLAMap() {
        Map<String, Double> map = new HashMap<String, Double>();
        if (propertiesList != null) {
            for (TransactionSLA tsla : transactionSLAs) {
                map.put(tsla.getName(), tsla.getValue());
            }
        }
        
        if(sla != Double.NaN) {
            map.put("", sla);
        }
        
        return map;
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

    public int getFailureThresholdResultCountMinimum() {
        return failureThresholdResultCountMinimum;
         
    }

    public int getMovingAveragePoints() {
        return movingAveragePoints;
    }
    
    public void setMovingAveragePoints(int movingAveragePoints) {
        this.movingAveragePoints = movingAveragePoints;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }
}

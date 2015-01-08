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

import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.model.TransactionResultModel;

import java.util.*;
import java.util.Map.Entry;

public class TestRunResult {

    private long startTime;
    private String configurationName;

    private Map<String, TransactionResultSnapshot> testResults = new HashMap<String, TransactionResultSnapshot>();
    private String failureReason;
    private List<CapturedStatistic> capturedStatistics = new ArrayList<CapturedStatistic>();

    public TestRunResult(String name, long startTime) {
        configurationName = name;
        this.startTime = startTime;
    }

    public TestRunResult() {
    }

    public void setTestResultsFromModel(Map<String, TransactionResultModel> map) {

        testResults = new HashMap<String, TransactionResultSnapshot>();

        Set<Entry<String, TransactionResultModel>> entrySet = map.entrySet();
        for (Entry<String, TransactionResultModel> entry : entrySet) {
            TransactionResultSnapshot snapshot = TransactionResultSnapshot.fromModel(entry.getValue());
            testResults.put(entry.getKey(), snapshot);
        }

    }

    public Map<String, TransactionResultSnapshot> getTestResults() {
        return testResults;
    }

    public void setTestResults(Map<String, TransactionResultSnapshot> testResults) {
        this.testResults = testResults;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override public String toString() {
        return "TestRunResult{" +
                "startTime=" + startTime +
                ", configurationName='" + configurationName + '\'' +
                ", testResults=" + testResults +
                '}';
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public List<CapturedStatistic> getCapturedStatistics() {
        return capturedStatistics;
    }

    public void setCapturedStatistics(List<CapturedStatistic> capturedStatistics) {
        this.capturedStatistics = capturedStatistics;
    }
}

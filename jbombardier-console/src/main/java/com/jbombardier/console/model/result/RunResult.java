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

package com.jbombardier.console.model.result;

import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;
import com.jbombardier.console.model.TransactionResultModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Encapsulation of the full results of a single run. Serialised into JSON and sent to the repository to keep a record
 * of the test run.
 */
public class RunResult {

    private long startTime;
    private String configurationName;

    private List<PhaseResult> phaseResults = new ArrayList<PhaseResult>();
    private List<AgentResult> agentResults = new ArrayList<AgentResult>();

//    private Map<String, TransactionResult> testResults = new HashMap<String, TransactionResult>();
    private String failureReason;

//    public RunResult(String name, long startTime) {
//        configurationName = name;
//        this.startTime = startTime;
//    }

    public RunResult() {
    }



//    public void setTestResultsFromModel(Map<String, TransactionResultModel> map) {
//
//        testResults = new HashMap<String, TransactionResult>();
//
//        Set<Entry<String, TransactionResultModel>> entrySet = map.entrySet();
//        for (Entry<String, TransactionResultModel> entry : entrySet) {
//            TransactionResult snapshot = TransactionResult.fromModel(entry.getValue());
//            testResults.put(entry.getKey(), snapshot);
//        }
//
//    }

//    public Map<String, TransactionResult> getTestResults() {
//        return testResults;
//    }

//    public void setTestResults(Map<String, TransactionResult> testResults) {
//        this.testResults = testResults;
//    }

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

//    @Override public String toString() {
//        return "TestRunResult{" +
//                "startTime=" + startTime +
//                ", configurationName='" + configurationName + '\'' +
//                ", testResults=" + testResults +
//                '}';
//    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public List<PhaseResult> getPhaseResults() {
        return phaseResults;
    }

    //    public List<CapturedStatistic> getCapturedStatistics() {
//        return capturedStatistics;
//    }

//    public void setCapturedStatistics(List<CapturedStatistic> capturedStatistics) {
//        this.capturedStatistics = capturedStatistics;
//    }


    public List<AgentResult> getAgentResults() {
        return agentResults;
    }


}

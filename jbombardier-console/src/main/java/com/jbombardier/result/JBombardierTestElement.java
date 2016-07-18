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

package com.jbombardier.result;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulation of a single time slice of results during the test run. Can contain lists containing the actual results
 * if that level of detail is required.
 */
public class JBombardierTestElement {
    private long time;

    private long successCount;
    private long failureCount;

    private List<Long> successResults = new ArrayList<Long>();
    private List<Long> failureResults = new ArrayList<Long>();
    private long totalSuccessDurationsIncludingSetup;
    private long totalFailureDurations;
    private long totalSuccessDurations;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(long failureCount) {
        this.failureCount = failureCount;
    }

    public List<Long> getSuccessResults() {
        return successResults;
    }

    public void setSuccessResults(List<Long> successResults) {
        this.successResults = successResults;
    }

    public List<Long> getFailureResults() {
        return failureResults;
    }

    public void setFailureResults(List<Long> failureResults) {
        this.failureResults = failureResults;
    }

    public void setTotalSuccessDurationsIncludingSetup(long totalSuccessDurationsIncludingSetup) {
        this.totalSuccessDurationsIncludingSetup = totalSuccessDurationsIncludingSetup;
    }

    public long getTotalSuccessDurationsIncludingSetup() {
        return totalSuccessDurationsIncludingSetup;
    }

    public void setTotalFailureDurations(long totalFailureDurations) {
        this.totalFailureDurations = totalFailureDurations;
    }

    public long getTotalFailureDurations() {
        return totalFailureDurations;
    }

    public void setTotalSuccessDurations(long totalSuccessDurations) {
        this.totalSuccessDurations = totalSuccessDurations;
    }

    public long getTotalSuccessDurations() {
        return totalSuccessDurations;
    }

    public void toStringDeep(StringUtils.StringUtilsBuilder builder) {

        builder.appendLine("TestElement : time='{}' successCount='{}' failureCount='{}'", Logger.toDateString(time), successCount, failureCount);

//        private List<Long> successResults = new ArrayList<Long>();
//        private List<Long> failureResults = new ArrayList<Long>();
//        private long totalSuccessDurationsIncludingSetup;
//        private long totalFailureDurations;
//        private long totalSuccessDurations;

    }
}

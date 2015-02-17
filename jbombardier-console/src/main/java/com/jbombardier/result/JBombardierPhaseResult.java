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

package com.jbombardier.result;

import com.logginghub.utils.CollectionUtils;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;

import java.util.List;
import java.util.Map;

/**
 * Created by james on 10/01/15.
 */
public class JBombardierPhaseResult {
    private String phaseName = JBombardierRunResult.unknown_state;
    private long phaseStartTime = JBombardierRunResult.unknown_time;
    private long phaseDuration = JBombardierRunResult.unknown_time;
    private String outcomeSummary = JBombardierRunResult.unknown_state;

    private Map<String, JBombardierTestResult> testResults = new FactoryMap<String, JBombardierTestResult>() {
        @Override protected JBombardierTestResult createEmptyValue(String key) {
            return new JBombardierTestResult(key);
        }
    };

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public long getPhaseStartTime() {
        return phaseStartTime;
    }

    public void setPhaseStartTime(long phaseStartTime) {
        this.phaseStartTime = phaseStartTime;
    }

    public long getPhaseDuration() {
        return phaseDuration;
    }

    public void setPhaseDuration(long phaseDuration) {
        this.phaseDuration = phaseDuration;
    }

    public String getOutcomeSummary() {
        return outcomeSummary;
    }

    public void setOutcomeSummary(String outcomeSummary) {
        this.outcomeSummary = outcomeSummary;
    }

    public Map<String, JBombardierTestResult> getTestResults() {
        return testResults;
    }

    public void toStringDeep(StringUtilsBuilder builder) {
        builder.appendLine("PhaseResult : phaseName='{}' outcomeSummary='{}' startTime='{}' duration='{}'",
                       phaseName,
                       outcomeSummary,
                       phaseStartTime,
                       phaseDuration);

//        builder.indent();

        List<String> testNames = CollectionUtils.toSortedList(getTestResults().keySet());

        for (String testName : testNames) {
            JBombardierTestResult testResult = testResults.get(testName);
            testResult.toStringDeep(builder);
        }

//        builder.outdent();
    }
}

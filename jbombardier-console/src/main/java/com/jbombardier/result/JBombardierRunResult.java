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

import com.jbombardier.console.configuration.JBombardierConfiguration;
import com.jbombardier.console.configuration.PhaseConfiguration;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 10/01/15.
 */
public class JBombardierRunResult {
    public final static long unknown_time = -1;
    public final static String unknown_state = "Unknown";
    public final static String defaultPhase = "default";

    private String testName = unknown_state;
    private long startTime = JBombardierRunResult.unknown_time;
    private long duration = JBombardierRunResult.unknown_time;
    private String outcomeSummary = unknown_state;

    private Map<String, JBombardierPhaseResult> phaseResults = new HashMap<String, JBombardierPhaseResult>();
    private List<String> phaseOrder =new ArrayList<String>();

    public JBombardierRunResult(JBombardierConfiguration configuration) {
        this.testName = configuration.getTestName();

        List<PhaseConfiguration> phases = configuration.getPhases();
        if (phases.isEmpty()) {
            JBombardierPhaseResult defaultPhase = new JBombardierPhaseResult();
            defaultPhase.setPhaseName(this.defaultPhase);
            phaseResults.put(this.defaultPhase, defaultPhase);
            phaseOrder.add(this.defaultPhase);
        }else{
            for (PhaseConfiguration phase : phases) {
                JBombardierPhaseResult phaseResult = new JBombardierPhaseResult();
                phaseResult.setPhaseName(phase.getPhaseName());
                phaseResults.put(phase.getPhaseName(), phaseResult);
                phaseOrder.add(phase.getPhaseName());
            }
        }
    }

    public String getTestName() {
        return testName;
    }

    public Map<String, JBombardierPhaseResult> getPhaseResults() {
        return phaseResults;
    }

    public long getDuration() {
        return duration;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getOutcomeSummary() {
        return outcomeSummary;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setOutcomeSummary(String outcomeSummary) {
        this.outcomeSummary = outcomeSummary;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String toStringDeep() {
        StringUtilsBuilder builder = new StringUtilsBuilder();

        builder.appendLine("JBombardierRunResult : name='{}' outcomeState='{}' startTime='{}' duration='{}'",
                           testName,
                           outcomeSummary,
                           Logger.toDateString(startTime),
                           TimeUtils.formatIntervalMilliseconds(duration));

        builder.indent();
        for (String phase : phaseOrder) {
            JBombardierPhaseResult phaseResult = phaseResults.get(phase);
            phaseResult.toStringDeep(builder);
            builder.newline();
        }
        builder.outdent();

        return builder.toString();
    }
}

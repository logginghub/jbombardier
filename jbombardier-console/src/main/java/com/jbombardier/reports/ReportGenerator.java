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

package com.jbombardier.reports;

import com.jbombardier.console.model.result.AgentResult;
import com.jbombardier.console.model.result.PhaseResult;
import com.jbombardier.console.model.result.RunResult;
import com.logginghub.utils.HTMLBuilder2;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;

import java.io.File;
import java.util.List;

/**
 * Created by james on 25/01/15.
 */
public class ReportGenerator {

    private static final Logger logger = Logger.getLoggerFor(ReportGenerator.class);

    public static void generateReport(File folder, RunResult runResult) {

        logger.info("Generating report into folder '{}'", folder.getAbsolutePath());

        HTMLBuilder2 builder = new HTMLBuilder2();
        HTMLBuilder2.Element body = builder.getBody();

        HTMLBuilder2.Element phasesDiv = body.div();
        phasesDiv.h1("Phases");
        HTMLBuilder2.TableElement phasesTable = phasesDiv.table();
        List<PhaseResult> phaseResults = runResult.getPhaseResults();
        phasesTable.row().cells("Name", "Warm up", "Duration");
        for (PhaseResult phaseResult : phaseResults) {
            phasesTable.row().cells(phaseResult.getPhaseName(),
                    TimeUtils.formatIntervalMilliseconds(phaseResult.getWarmup()),
                    TimeUtils.formatIntervalMilliseconds(phaseResult.getDuration()));
        }

        HTMLBuilder2.Element agentsDiv = body.div();
        agentsDiv.h1("Agents");
        HTMLBuilder2.TableElement agentsTable = phasesDiv.table();
        agentsTable.row().cells("Name", "Address", "Port");
        List<AgentResult> agentResults = runResult.getAgentResults();
        for (AgentResult agentResult : agentResults) {
            agentsTable.row().cells(agentResult.getName(), agentResult.getAddress(), Integer.toString(agentResult.getPort()));
        }


    }

}

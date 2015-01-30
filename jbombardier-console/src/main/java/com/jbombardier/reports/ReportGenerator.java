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

import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.model.result.AgentResult;
import com.jbombardier.console.model.result.PhaseResult;
import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.console.model.result.TransactionResult;
import com.logginghub.utils.HTMLBuilder2;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by james on 25/01/15.
 */
public class ReportGenerator {

    private static final Logger logger = Logger.getLoggerFor(ReportGenerator.class);
    private static NumberFormat numberFormat = NumberFormat.getInstance();

    public static void generateReport(File folder, RunResult runResult, TimeUnit reportTimeUnits) {

        logger.info("Generating report into folder '{}'", folder.getAbsolutePath());

        List<PhaseResult> phaseResults = runResult.getPhaseResults();
        for (PhaseResult phaseResult : phaseResults) {
            generatePhase(folder, phaseResult, reportTimeUnits);
        }

        generateIndex(folder, runResult, reportTimeUnits);
    }

    private static void generatePhase(File folder, PhaseResult phaseResult, TimeUnit reportTimeUnits) {
        HTMLBuilder2 builder = new HTMLBuilder2();
        HTMLBuilder2.Element body = builder.getBody();

        HTMLBuilder2.Element testsDiv = body.div();
        testsDiv.h1("Tests");

        buildTestsTable(phaseResult.getTransactionResults(), reportTimeUnits, testsDiv);

        HTMLBuilder2.Element statsDiv = body.div();
        statsDiv.h1("Captured Statistics");
        HTMLBuilder2.TableElement statsTable = statsDiv.table();

        List<CapturedStatistic> capturedStatistics = phaseResult.getCapturedStatistics();
        for (CapturedStatistic capturedStatistic : capturedStatistics) {

            HTMLBuilder2.RowElement row = statsTable.row();
            row.cell(Logger.toLocalDateString(capturedStatistic.getTime()));
            row.cell(capturedStatistic.getPath());
            row.cell(capturedStatistic.getValue());
        }

        builder.toFile(new File(folder, StringUtils.format("phase-{}.html", phaseResult.getPhaseName())));
    }

    private static void buildTestsTable(List<TransactionResult> transactionResults, TimeUnit reportTimeUnits, HTMLBuilder2.Element testsDiv) {
        HTMLBuilder2.TableElement testsTable = testsDiv.table();

        String timeUnitDescription;
        if (reportTimeUnits == TimeUnit.NANOSECONDS) {
            timeUnitDescription = "(ns)";
        } else if (reportTimeUnits == TimeUnit.MICROSECONDS) {
            timeUnitDescription = "(μs)";
        } else if (reportTimeUnits == TimeUnit.MILLISECONDS) {
            timeUnitDescription = "(ms)";
        } else {
            timeUnitDescription = "(s)";
        }

        HTMLBuilder2.RowElement headerRow = testsTable.row();
        headerRow.cells("Test name").cells("Transaction").cells("Total transaction count");
        headerRow.cell("Successful").setAttribute("colspan", "4");
        headerRow.cell("Unsuccessful").setAttribute("colspan", "3");

        testsTable.row().cells("").cells("").cells("").cells("Transaction count").cells("Mean duration " + timeUnitDescription).cells("SLA").
                cells("Mean TPS").cells("Target TPS").cells("Transaction count").cells("Mean duration " + timeUnitDescription).cells("Mean TPS");

        for (TransactionResult transactionResult : transactionResults) {

            HTMLBuilder2.RowElement row = testsTable.row();

            row.cell(transactionResult.getTestName());
            row.cell(transactionResult.getTransactionName());
            row.cell(format(transactionResult.getTotalTransactionCount()));

            row.cell(format(transactionResult.getSuccessfulTransactionCount()));
            row.cell(formatTime(transactionResult.getSuccessfulTransactionMeanDuration(), reportTimeUnits));
            row.cell(formatTime(transactionResult.getSla(), reportTimeUnits));
            row.cell(format(transactionResult.getSuccessfulTransactionMeanTransactionsPerSecond()));
            row.cell(format(transactionResult.getSuccessfulTransactionMeanTransactionsPerSecondTarget()));

            row.cell(format(transactionResult.getUnsuccessfulTransactionCount()));
            row.cell(formatTime(transactionResult.getUnsuccessfulTransactionMeanDuration(), reportTimeUnits));
            row.cell(format(transactionResult.getUnsuccessfulTransactionMeanTransactionsPerSecond()));
        }
    }

    private static String formatTime(double valueInNanoseconds, TimeUnit reportTimeUnits) {
        double scaled = valueInNanoseconds;

        if (Double.isNaN(scaled)) {
            return "-";
        } else {
            if (reportTimeUnits == TimeUnit.MICROSECONDS) {
                scaled *= 1e-3;
            } else if (reportTimeUnits == TimeUnit.MILLISECONDS) {
                scaled *= 1e-6;
            } else if (reportTimeUnits == TimeUnit.SECONDS) {
                scaled *= 1e-9;
            }

            return numberFormat.format(scaled);
        }
    }

    private static String format(double value) {
        return numberFormat.format(value);
    }


    private static void generateIndex(File folder, RunResult runResult, TimeUnit reportTimeUnits) {
        HTMLBuilder2 builder = new HTMLBuilder2();
        HTMLBuilder2.Element body = builder.getBody();

        HTMLBuilder2.Element phasesDiv = body.div();
        phasesDiv.h1("Phases");
        HTMLBuilder2.TableElement phasesTable = phasesDiv.table();
        List<PhaseResult> phaseResults = runResult.getPhaseResults();
        phasesTable.row().cells("Name", "Warm up", "Duration");
        for (PhaseResult phaseResult : phaseResults) {
            HTMLBuilder2.RowElement row = phasesTable.row();
            row.cell().a(StringUtils.format("phase-{}.html", phaseResult.getPhaseName()), phaseResult.getPhaseName());
            row.cell(TimeUtils.formatIntervalMilliseconds(phaseResult.getWarmup()));
            row.cell(TimeUtils.formatIntervalMilliseconds(phaseResult.getDuration()));
        }

        HTMLBuilder2.Element agentsDiv = body.div();
        agentsDiv.h1("Agents");
        HTMLBuilder2.TableElement agentsTable = agentsDiv.table();
        agentsTable.row().cells("Name", "Address", "Port");
        List<AgentResult> agentResults = runResult.getAgentResults();
        for (AgentResult agentResult : agentResults) {
            agentsTable.row().cells(agentResult.getName(), agentResult.getAddress(), Integer.toString(agentResult.getPort()));
        }

        for (PhaseResult phaseResult : phaseResults) {
            HTMLBuilder2.Element phaseResultsDiv = body.div();
            phaseResultsDiv.h2(phaseResult.getPhaseName());
            buildTestsTable(phaseResult.getTransactionResults(), reportTimeUnits, phaseResultsDiv);
        }

        builder.toFile(new File(folder, "index.html"));
    }

}

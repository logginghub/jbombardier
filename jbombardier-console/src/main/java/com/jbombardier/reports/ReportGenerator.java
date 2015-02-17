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

import com.esotericsoftware.minlog.Log;
import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.model.result.AgentResult;
import com.jbombardier.console.model.result.PhaseResult;
import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.console.model.result.TransactionResult;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        builder.getHead().css("box.css");
        builder.getHead().css("table.css");

        HTMLBuilder2.Element bodyx = builder.getBody();

        final HTMLBuilder2.Element content = bodyx.div("wide-box");

        HTMLBuilder2.Element testsDiv = content.div();
        testsDiv.h1("Tests");

        buildTestsTable(phaseResult.getTransactionResults(), reportTimeUnits, testsDiv);

        List<CapturedStatistic> capturedStatistics = phaseResult.getCapturedStatistics();
        if(!capturedStatistics.isEmpty()) {

            HTMLBuilder2.Element statsDiv = content.div();
            statsDiv.h1("Captured Statistics");
            HTMLBuilder2.TableElement statsTable = statsDiv.table();
            statsTable.getThead().headerRow().cells("Time", "Path", "Value");

            Set<String> distinctPaths = new HashSet<String>();

            for (CapturedStatistic capturedStatistic : capturedStatistics) {

                HTMLBuilder2.RowElement row = statsTable.getTbody().row();
                row.cell(Logger.toLocalDateString(capturedStatistic.getTime()));
                row.cell(capturedStatistic.getPath());
                row.cell(capturedStatistic.getValue());

                distinctPaths.add(capturedStatistic.getPath());
            }

            HTMLBuilder2.Element pathDiv = content.div();
            HTMLBuilder2.TableElement pathTable = pathDiv.table();
            pathTable.getThead().headerRow().cells("Path");

            for (String distinctPath : distinctPaths) {
                pathTable.getTbody().row().cell().a(StringUtils.format("phase-{}-path-{}.html", phaseResult.getPhaseName(), distinctPath.replace('/', '-')), distinctPath);
                generatePathView(folder, phaseResult.getPhaseName(), distinctPath, capturedStatistics);
            }
        }

        builder.toFile(new File(folder, StringUtils.format("phase-{}.html", phaseResult.getPhaseName())));

    }

    private static void generatePathView(File folder, String phase, String distinctPath, List<CapturedStatistic> capturedStatistics) {

        HTMLBuilder2 builder = new HTMLBuilder2();

        builder.getHead().css("box.css");
        builder.getHead().css("table.css");

        HTMLBuilder2.Element bodyx = builder.getBody();

        final HTMLBuilder2.Element content = bodyx.div("wide-box");

        TimeSeries series = new TimeSeries(distinctPath);
        TimeSeriesCollection data = new TimeSeriesCollection();
        data.addSeries(series);

        try {
            for (CapturedStatistic capturedStatistic : capturedStatistics) {
                if (capturedStatistic.getPath().equals(distinctPath)) {
                    series.add(new Second(new Date(capturedStatistic.getTime())), Double.parseDouble(capturedStatistic.getValue()));
                }
            }
        } catch (NumberFormatException nfe) {
            // Skip this one, its not numeric
        }

        if (series.getItemCount() > 0) {
            final String imageName = StringUtils.format("phase-{}-path-{}.png", phase, distinctPath.replace('/','-'));
            content.image(imageName);

            final File file = new File(folder, imageName);
            render(StringUtils.format("phase-{}-path-{}.html", phase, distinctPath.replace('/','-')), data, file);
        }

        builder.toFile(new File(folder, StringUtils.format("phase-{}-path-{}.html", phase, distinctPath.replace('/', '-'))));

    }

    private static void buildTestsTable(List<TransactionResult> transactionResults, TimeUnit reportTimeUnits, HTMLBuilder2.Element testsDiv) {
        HTMLBuilder2.TableElement testsTable = testsDiv.table();

        testsTable.addClass("table");
        testsTable.addClass("table-striped");
        testsTable.addClass("table-header-rotated");

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

        HTMLBuilder2.THeadRow headerRow = testsTable.getThead().headerRow();

        headerRow.cell("Test name").addClass("rotate-45");
        headerRow.cell("Transaction").addClass("rotate-45");
        headerRow.cell("Total transaction count").addClass("rotate-45");

        headerRow.cell("Agents").addClass("rotate-45");
        headerRow.cell("Threads").addClass("rotate-45");
//        headerRow.cell("Sample Time") .addClass("rotate-45");

        headerRow.cell("Successful transactions").addClass("rotate-45");
        headerRow.cell("Unsuccessful transactions").addClass("rotate-45");

        headerRow.cell("Mean duration " + timeUnitDescription).addClass("rotate-45");
        headerRow.cell("SLA").addClass("rotate-45");
        headerRow.cell("Mean TPS").addClass("rotate-45");
        headerRow.cell("Target TPS").addClass("rotate-45");
        headerRow.cell("Maximum TPS").addClass("rotate-45");

        headerRow.cell("Abs dev").addClass("rotate-45");
        headerRow.cell("%").addClass("rotate-45");
        headerRow.cell("stdevp").addClass("rotate-45");
        headerRow.cell("TP90").addClass("rotate-45");
        headerRow.cell("TP99").addClass("rotate-45");
        headerRow.cell("Median").addClass("rotate-45");
        headerRow.cell("Fastest").addClass("rotate-45");
        headerRow.cell("Slowest").addClass("rotate-45");

        headerRow.cell("Unsuccessful mean duration " + timeUnitDescription).addClass("rotate-45");
        headerRow.cell("Unsuccessful Mean TPS").addClass("rotate-45");

        for (TransactionResult transactionResult : transactionResults) {

            HTMLBuilder2.RowElement row = testsTable.row();

            row.cell(transactionResult.getTestName());
            row.cell(transactionResult.getTransactionName());
            row.cell(format(transactionResult.getTotalTransactionCount()));

            row.cell(format(transactionResult.getAgents()));
            row.cell(format(transactionResult.getThreads()));
//            row.cell(TimeUtils.formatIntervalMilliseconds(transactionResult.getSampleTime()));

            row.cell(format(transactionResult.getSuccessfulTransactionCount()));
            row.cell(format(transactionResult.getUnsuccessfulTransactionCount()));

            row.cell(formatTime(transactionResult.getSuccessfulTransactionMeanDuration(), reportTimeUnits));
            row.cell(formatTime(transactionResult.getSla(), reportTimeUnits));
            row.cell(format(transactionResult.getSuccessfulTransactionMeanTransactionsPerSecond()));
            row.cell(format(transactionResult.getSuccessfulTransactionMeanTransactionsPerSecondTarget()));
            row.cell(format(transactionResult.getSuccessfulMaximumTransactionsPerSecond()));

            row.cell(formatTime(transactionResult.getSuccessfulAbsoluteDeviation(), reportTimeUnits));
            row.cell(format(transactionResult.getSuccessfulAbsoluteDeviationAsPercentage()));
            row.cell(formatTime(transactionResult.getSuccessfulStandardDeviation(), reportTimeUnits));
            row.cell(formatTime(transactionResult.getSuccessfulPercentiles()[90], reportTimeUnits));
            row.cell(formatTime(transactionResult.getSuccessfulPercentiles()[99], reportTimeUnits));
            row.cell(formatTime(transactionResult.getSuccessfulMedian(), reportTimeUnits));
            row.cell(formatTime(transactionResult.getSuccessfulFastestResult(), reportTimeUnits));
            row.cell(formatTime(transactionResult.getSuccessfulSlowestResult(), reportTimeUnits));

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

        builder.getHead().css("box.css");
        builder.getHead().css("table.css");
        builder.associateResource("/velocity/table.css");
        builder.associateResource("/velocity/box.css");

        HTMLBuilder2.Element bodyx = builder.getBody();

        final HTMLBuilder2.Element content = bodyx.div("wide-box");

        content.h1(runResult.getConfigurationName());
        content.h2(Logger.toLocalDateString(runResult.getStartTime()).toString());

        HTMLBuilder2.Element phasesDiv = content.div();
        phasesDiv.h2("Phases");
        HTMLBuilder2.TableElement phasesTable = phasesDiv.table();
        List<PhaseResult> phaseResults = runResult.getPhaseResults();
        phasesTable.getThead().headerRow().cells("Name", "Warm up", "Duration");
        for (PhaseResult phaseResult : phaseResults) {
            HTMLBuilder2.RowElement row = phasesTable.row();
            row.cell().a(StringUtils.format("phase-{}.html", phaseResult.getPhaseName()), phaseResult.getPhaseName());
            row.cell(TimeUtils.formatIntervalMilliseconds(phaseResult.getWarmup()));
            row.cell(TimeUtils.formatIntervalMilliseconds(phaseResult.getDuration()));
        }

        HTMLBuilder2.Element agentsDiv = content.div();
        agentsDiv.h2("Agents");
        HTMLBuilder2.TableElement agentsTable = agentsDiv.table();
        agentsTable.getThead().headerRow().cells("Name", "Address", "Port");
        List<AgentResult> agentResults = runResult.getAgentResults();
        for (AgentResult agentResult : agentResults) {
            agentsTable.row().cells(agentResult.getName(), agentResult.getAddress(), Integer.toString(agentResult.getPort()));
        }

        for (PhaseResult phaseResult : phaseResults) {
            HTMLBuilder2.Element phaseResultsDiv = content.div();
            phaseResultsDiv.h2(phaseResult.getPhaseName());
            buildTestsTable(phaseResult.getTransactionResults(), reportTimeUnits, phaseResultsDiv);
        }

        File indexFile = new File(folder, "index.html");
        builder.toFilePretty(indexFile);
        builder.copyAssociatedResourcesTo(folder);

        logger.info("Generated index '{}'", indexFile.getAbsolutePath());

    }

    private static void render(String title, TimeSeriesCollection timeSeriesCollection, File file) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(title,// title
                "Time",// x-axislabel
                "Elapsed / ms",// y-axislabel
                timeSeriesCollection,// data
                true,// createlegend?
                true,// generatetooltips?
                false// generateURLs?
                                                             );

        try {
            FileOutputStream fos = new FileOutputStream(file);
            Log.info("Rendered " + title + " to " + file.getAbsolutePath());
            ChartUtilities.writeChartAsPNG(fos, chart, 640, 480);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

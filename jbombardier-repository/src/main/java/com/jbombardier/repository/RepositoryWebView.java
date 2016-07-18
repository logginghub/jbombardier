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

package com.jbombardier.repository;

import com.jbombardier.console.VelocityUtils;
import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.console.model.result.TransactionResult;
import com.jbombardier.repository.model.RepositoryModel;
import com.jbombardier.repository.model.RepositoryTestModel;
import com.jbombardier.repository.model.ResultDelta;
import com.jbombardier.repository.model.ResultDeltasForRun;
import com.logginghub.utils.*;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.web.JettyLauncher;
import com.logginghub.web.RequestContext;
import com.logginghub.web.WebController;
import org.apache.velocity.VelocityContext;

import java.text.NumberFormat;
import java.util.*;

@WebController(staticFiles = "/repostatic/", defaultUrl = "index") public class RepositoryWebView {

    private static final Logger logger = Logger.getLoggerFor(RepositoryWebView.class);
    private RepositoryController controller;
    private JettyLauncher jettyLauncher;
    private RepositoryModel model;
    private VelocityHelper velocityHelper = new VelocityHelper();
    private int httpPort = VLPorts.getRepositoryWebDefaultPort();

    public void bind(RepositoryController controller) {
        this.controller = controller;
        this.model = controller.getModel();
    }

    public void stop() {
        if (jettyLauncher != null) {
            jettyLauncher.close();
        }
    }

    public String index() {
        VelocityContext context = velocityHelper.buildContext();
        ObservableList<RepositoryTestModel> testModels = model.getTestModels();
        context.put("tests", testModels);
        logger.info("Showing {} test models", testModels.size());

        return velocityHelper.help(context, "repository/velocity/index.vm");
    }

    public String resultsSuccessPerSecond(String testName, String resultName) {

        List<TransactionResult> results = getSortedResultsForTest(testName, resultName);

        StringBuilder builder = new StringBuilder();
        builder.append("[[");

        String div = "";
        for (TransactionResult transactionResult : results) {
            builder.append(div);
            builder.append("[");
            builder.append(transactionResult.getSampleTime())
                   .append(",")
                   .append(transactionResult.getSuccessfulTransactionCount());
            builder.append("]");
            div = ",";
        }

        builder.append("]]");
        return builder.toString();
    }

    public String resultsSuccessTimes(String testName, String resultName) {

        List<TransactionResult> results = getSortedResultsForTest(testName, resultName);

        StringBuilder builder = new StringBuilder();
        builder.append("[");

        // Build the first series, the actual results
        builder.append("[");
        String div = "";
        for (TransactionResult transactionResult : results) {
            builder.append(div);
            builder.append("[");
            builder.append(transactionResult.getSampleTime())
                   .append(",")
                   .append(transactionResult.getSuccessfulTransactionMeanDuration());
            builder.append("]");
            div = ",";
        }
        builder.append("],");

        // The second series is the SLA values
        builder.append("[");
        div = "";
        for (TransactionResult transactionResult : results) {
            builder.append(div);
            builder.append("[");
            double sla = transactionResult.getSla();
            if (!Double.isNaN(sla)) {
                builder.append(transactionResult.getSampleTime())
                       .append(",")
                       .append(transactionResult.getSla());
            }
            builder.append("]");
            div = ",";
        }
        builder.append("]");

        builder.append("]");

        return builder.toString();
    }

    public void start() {
        try {
            jettyLauncher = JettyLauncher.launchNonBlocking(this, httpPort);
        } catch (Exception e) {
            throw new FormattedRuntimeException("Failed to start jetty launcher", e);
        }
    }

    public String runs(String testname, String testStartTime) {
        HTMLBuilder2 html = new HTMLBuilder2();
        HTMLBuilder2.Element div = setupStandardHeader(html);

        long startTime = Long.parseLong(testStartTime);

        RunResult result = null;
        RepositoryTestModel repositoryTestModelForTest = model.getRepositoryTestModelForTest(testname);
        if (repositoryTestModelForTest != null) {
            List<RunResult> lastXResults = repositoryTestModelForTest.getLastXResults(50);
            for (RunResult lastXResult : lastXResults) {
                if (lastXResult.getStartTime() == startTime) {
                    result = lastXResult;
                    break;
                }
            }
        }

        if (result != null) {

            long startTime1 = result.getStartTime();

            div.h2("Test '{}', started at {}", testname, Logger.toDateString(startTime1).toString());

            String failureReason = result.getFailureReason();

            if (StringUtils.isNotNullOrEmpty(failureReason)) {
                div.h3("Failure reason : {}", failureReason);
            }

            String configurationName = result.getConfigurationName();
            div.h3("Configuration name : {}", configurationName);


            div.h3("Test results : ");

            HTMLBuilder2.TableElement resultsTable = div.table();

            resultsTable.getThead().row().cells("Test name", "Total transactions", "Mean success time", "SLA");
            // TODO : refactor fix me
//            Map<String, TransactionResult> testResults = result.getTestResults();
//            Set<String> testNamesSet = testResults.keySet();
            List<String> sortedTestNames = new ArrayList<String>();
//            sortedTestNames.addAll(testNamesSet);
            Collections.sort(sortedTestNames);

            NumberFormat nf = NumberFormat.getInstance();

//            for (String testName : sortedTestNames) {
//                TransactionResult transactionResult = testResults.get(testName);
//
//                resultsTable.getTbody()
//                            .row()
//                            .cells(transactionResult.getTestName(),
//                                   nf.format(transactionResult.getTotalTransactionCount()),
//                                   nf.format(transactionResult.getSuccessDurationMS()),
//                                   nf.format(transactionResult.getSla()));
//            }

            div.h3("Captured statistics : ");
//            List<CapturedStatistic> capturedStatistics = result.getCapturedStatistics();

//            HTMLBuilder2.TableElement statisticsTable = div.table();
//            statisticsTable.getThead().row().cells("Time", "Path", "Value");
//            for (CapturedStatistic capturedStatistic : capturedStatistics) {
//                statisticsTable.getTbody()
//                            .row()
//                            .cells(Logger.toDateString(capturedStatistic.getTime()).toString(),
//                                   capturedStatistic.getPath(),
//                                   capturedStatistic.getValue());
//
//
//            }
        } else {
            div.span("Could not find a run that started at '{}' for test '{}'", testStartTime, testname);
        }

        return html.toString();
    }

    public String runs(String testname) {

        HTMLBuilder2 html = new HTMLBuilder2();
        HTMLBuilder2.Element div = setupStandardHeader(html);

        HTMLBuilder2.TableElement table = div.table();
        table.id("rounded-corner");
        table.addClass("smallFont");
        table.getThead().row().cell("Test run time");
        HTMLBuilder2.Element tbody = table.getTbody();

        RepositoryTestModel repositoryTestModelForTest = model.getRepositoryTestModelForTest(testname);
        List<RunResult> lastXResults = repositoryTestModelForTest.getLastXResults(50);
        for (RunResult lastXResult : lastXResults) {
            HTMLBuilder2.Element cell = tbody.row().cell();
            cell.a(StringUtils.format("/runs/{}/{}", testname, lastXResult.getStartTime()),
                   Logger.toDateString(lastXResult.getStartTime()).toString());
        }

        return html.toString();

    }

    private HTMLBuilder2.Element setupStandardHeader(HTMLBuilder2 html) {
        html.setDocType("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        html.getHeader().addStyleSheet("/style/repository.css");
        html.getHeader().addJavascript("/scripts/jquery.min.js");

        HTMLBuilder2.Element div = html.getBody().div();
        div.addClass("wide-box");

        HTMLBuilder2.Element headline = div.h1();
        headline.image("/images/black-flask-hi.png", 32, 32);
        headline.span("JBombardierResults Browser");
        return div;
    }

    public String overview(String testname) {
        VelocityContext context = velocityHelper.buildContext();

        RepositoryTestModel repositoryTestModelForTest = model.getRepositoryTestModelForTest(testname);
        List<RunResult> lastXResults = repositoryTestModelForTest.getLastXResults(100);
        Set<String> testNames = new HashSet<String>();
        for (RunResult runResult : lastXResults) {
        // TODO : refactor fix me
//            Set<String> keySet = runResult.getTestResults().keySet();
//            testNames.addAll(keySet);
        }

        List<String> sortedNames = new ArrayList<String>();
        sortedNames.addAll(testNames);
        Collections.sort(sortedNames);

        List<ResultDeltasForRun> resultsDeltas = buildResultDeltas(sortedNames, lastXResults);

        String units = RequestContext.getRequestContext().getForm().getFirstValue("units", "ms");
        updateForUnits(resultsDeltas, units);

        context.put("repositoryTestModel", repositoryTestModelForTest);
        context.put("results", lastXResults);
        context.put("deltas", resultsDeltas);
        context.put("testNames", sortedNames);
        context.put("utils", new VelocityUtils());

        return velocityHelper.help(context, "repository/velocity/overview.vm.html");
    }

    public String detail(String testname) {
        VelocityContext context = velocityHelper.buildContext();

        RepositoryTestModel repositoryTestModelForTest = model.getRepositoryTestModelForTest(testname);
        List<RunResult> lastXResults = repositoryTestModelForTest.getLastXResults(100);

        Set<String> testNames = new HashSet<String>();
        for (RunResult runResult : lastXResults) {
            // TODO : refactor fix me
//            Set<String> keySet = runResult.getTestResults().keySet();
//            testNames.addAll(keySet);
        }

        List<String> sortedNames = new ArrayList<String>();
        sortedNames.addAll(testNames);
        Collections.sort(sortedNames);

        List<ResultDeltasForRun> resultsDeltas = buildResultDeltas(sortedNames, lastXResults);

        String units = RequestContext.getRequestContext().getForm().getFirstValue("units", "ms");
        updateForUnits(resultsDeltas, units);

        context.put("repositoryTestModel", repositoryTestModelForTest);
        context.put("results", lastXResults);
        context.put("deltas", resultsDeltas);
        context.put("testNames", sortedNames);
        context.put("utils", new VelocityUtils());

        return velocityHelper.help(context, "repository/velocity/detail.vm.html");
    }

    public String test(String testName, String resultName) {
        VelocityContext context = velocityHelper.buildContext();

        List<TransactionResult> results = getResultsForTest(testName, resultName);

        Collections.sort(results, new Comparator<TransactionResult>() {
            @Override public int compare(TransactionResult o1, TransactionResult o2) {
                return CompareUtils.compareLongs(o2.getSampleTime(), o1.getSampleTime());
            }
        });

        context.put("testName", testName);
        context.put("resultName", resultName);
        context.put("results", results);
        context.put("utils", new VelocityUtils());
        return velocityHelper.help(context, "repository/velocity/results.vm");
    }

    private List<ResultDeltasForRun> buildResultDeltas(List<String> sortedNames, List<RunResult> lastXResults) {
        List<ResultDeltasForRun> resultDeltas = new ArrayList<ResultDeltasForRun>();

        if (lastXResults.size() >= 2) {

            Iterator<RunResult> iterator = lastXResults.iterator();
            RunResult currentResult = iterator.next();

            // Go through the results in order
            while (iterator.hasNext()) {
                RunResult nextResult = iterator.next();

                ResultDeltasForRun deltas = new ResultDeltasForRun();
                deltas.setStartTime(currentResult.getStartTime());
                if (currentResult.getFailureReason() != null) {
                    deltas.setStatus(currentResult.getFailureReason());
                }

                // Iterate through the sorted test names to get the column order right
                for (String testName : sortedNames) {
                    // TODO : refactor fix me
//                    TransactionResult currentSnapshot = currentResult.getTestResults().get(testName);
//                    TransactionResult nextSnapshot = nextResult.getTestResults().get(testName);

//                    ResultDelta resultDelta = new ResultDelta();
//                    resultDelta.setTest(testName);
//
//                    if (currentSnapshot != null && nextSnapshot != null) {
//
//                        double deltaTotalTransactions = currentSnapshot.getTotalTransactionCount() - nextSnapshot.getTotalTransactionCount();
//                        double deltaTransactionsPerSecond = currentSnapshot.getTransactionsSuccessful() - nextSnapshot.getTransactionsSuccessful();
//                        double deltaTransactionTimes = currentSnapshot.getSuccessDuration() - nextSnapshot.getSuccessDuration();
//
//                        double percentageDeltaTransactions = -100 + currentSnapshot.getTransactionsSuccessful() / nextSnapshot
//                                .getTransactionsSuccessful() * 100f;
//                        double percentageDeltaTotalTransactions = -100 + ((double) currentSnapshot.getTotalTransactionCount()) / ((double) nextSnapshot
//                                .getTotalTransactionCount()) * 100f;
//                        double percentageDeltaTime = -100 + currentSnapshot.getSuccessDuration() / nextSnapshot.getSuccessDuration() * 100f;
//
//                        resultDelta.setDeltaTransactionsTotal(deltaTotalTransactions);
//                        resultDelta.setTotalTransactionCount(currentSnapshot.getTotalTransactionCount());
//                        resultDelta.setDeltaSuccessTransactionsPerSecond(deltaTransactionsPerSecond);
//                        resultDelta.setDeltaSuccessTransactionTime(deltaTransactionTimes);
//                        resultDelta.setPercentageDeltaTransactionsPerSecond(percentageDeltaTransactions);
//                        resultDelta.setPercentageDeltaTotalTransactions(percentageDeltaTotalTransactions);
//                        resultDelta.setPercentageDeltaTransactionTime(percentageDeltaTime);
//                        resultDelta.setCurrentTransactions(currentSnapshot.getTransactionsSuccessful());
//                        resultDelta.setCurrentTransactionTime(currentSnapshot.getSuccessDuration());
//                        resultDelta.setSLA(currentSnapshot.getSla());
//                    } else {
//                        One of these results didn't have the test we are interested in comparing
//                    }
//
//                    deltas.add(resultDelta);
                }

                resultDeltas.add(deltas);
                currentResult = nextResult;
            }
        }

        return resultDeltas;

    }

    private List<TransactionResult> getResultsForTest(String testName, String resultName) {
        RepositoryTestModel repositoryTestModelForTest = model.getRepositoryTestModelForTest(testName);
        return repositoryTestModelForTest.getResultsForTest(resultName);
    }

    private List<TransactionResult> getSortedResultsForTest(String testName, String resultName) {
        List<TransactionResult> results = getResultsForTest(testName, resultName);
        Collections.sort(results, new Comparator<TransactionResult>() {
            @Override public int compare(TransactionResult o1, TransactionResult o2) {
                return CompareUtils.compareLongs(o2.getSampleTime(), o1.getSampleTime());
            }
        });
        return results;
    }

    private void updateForUnits(List<ResultDeltasForRun> resultsDeltas, String units) {
        double modifier;

        if (units.equals("s")) {
            modifier = 1e-6;
        } else if (units.equals("ms")) {
            modifier = 1e-6;
        } else if (units.equals("mus")) {
            modifier = 1e-3;
        } else if (units.equals("ns")) {
            modifier = 1;
        } else {
            throw new FormattedRuntimeException("Unsupported units '{}' - must be s, ms, mus or ns", units);
        }

        for (ResultDeltasForRun resultDeltasForRun : resultsDeltas) {
            List<ResultDelta> resultDeltas = resultDeltasForRun.getResultDeltas();
            for (ResultDelta resultDelta : resultDeltas) {
                resultDelta.setDeltaSuccessTransactionTime(resultDelta.getDeltaTransactionTime() * modifier);
                resultDelta.setCurrentTransactionTime(resultDelta.getCurrentTransactionTime() * modifier);
            }
        }
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }
}

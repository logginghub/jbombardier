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

package com.jbombardier.console.headless;

import com.esotericsoftware.minlog.Log;
import com.jbombardier.console.SwingConsoleController;
import com.jbombardier.console.ConsoleModel;
import com.jbombardier.console.configuration.InteractiveConfiguration;
import com.jbombardier.console.model.AgentModel;
import com.jbombardier.console.model.TransactionResultModel;
import com.jbombardier.console.model.TransactionResultModel.TransactionTimeThresholdMode;
import com.logginghub.utils.*;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.SystemErrStream;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;

import java.io.File;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Main class for running the jbombardier console/controller in headless mode.
 *
 * @author James
 */
public class Headless {

    private static final Logger logger = Logger.getLoggerFor(Headless.class);
    // private int failureThresholdResultCountMinimum =
    // Integer.getInteger("failureThresholdResultCountMinimum", 10);
    private long warmupTime = 10000;
    private long sampleTime = 10000;
    private long timeToWaitForAgents = 10000;
    private int agentsRequired = 1;
    private String reportFolder = "reports";
    private Timer timer;

    private volatile boolean warmedUp = false;
    private volatile boolean failureDetected = false;
    private String failureReason = null;
    private HeadlessModeListener listener;

    private QuietLatch failureLatch = new QuietLatch(1);

    private long lastTotalResults = 0;
    private int resultsNotIncreased = 0;

    public int getAgentsRequired() {
        return agentsRequired;
    }

    public void setAgentsRequired(int agentsRequired) {
        this.agentsRequired = agentsRequired;
    }

    public long getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(long sampleTime) {
        this.sampleTime = sampleTime;
    }

    public long getTimeToWaitForAgents() {
        return timeToWaitForAgents;
    }

    public void setTimeToWaitForAgents(long timeToWaitForAgents) {
        this.timeToWaitForAgents = timeToWaitForAgents;
    }

    public long getWarmupTime() {
        return warmupTime;
    }

    public void setWarmupTime(long warmupTime) {
        this.warmupTime = warmupTime;
    }

    public SwingConsoleController run(String config) {
        Log.set(Log.LEVEL_WARN);
        InteractiveConfiguration configuration = InteractiveConfiguration.loadConfiguration(config);
        return run(configuration);
    }

    public SwingConsoleController run(InteractiveConfiguration configuration) {
        ConsoleModel model = new ConsoleModel();
        SwingConsoleController controller = new SwingConsoleController();
        controller.setReportsPath(new File(reportFolder));
        controller.initialise(configuration, model);
        controller.startStats();
        controller.waitForEmbeddedIfNeeded();

        // Attach the monitor to detect async failures
        attachFailureMonitor(controller);

        // Turn off auto-start, we'll sort that out ourselves
        controller.setAutostartAgents(Integer.MAX_VALUE);

        listener = new HeadlessModeListener();
        model.addListener(listener);
        waitForAgents(model);

        controller.startStatisticsCapture();
        controller.startTest();
        controller.getResultsController().stopStatsUpdater();

        failureLatch.setTimeout(new Timeout(warmupTime, TimeUnit.MILLISECONDS));
        if (!failureLatch.await()) {

            logger.info("------------------- Warmup complete, real test run started ------------------------");
            controller.resetStats();
            controller.getResultsController().startStatsUpdater(controller.getModel());
            warmedUp = true;

            long endTime = System.currentTimeMillis() + sampleTime;
            while (System.currentTimeMillis() < endTime) {
                ThreadUtils.sleep(1000);
                if (failureDetected) {
                    break;
                }
            }
        }

        timer.cancel();
        controller.getResultsController().stopStatsUpdater();
        controller.stopStats();
        logger.info("-------------------         Test execution complete         ------------------------");

        if (failureDetected) {
            controller.getModel().setFailureReason(failureReason);
            logger.severe("Test FAILED : {}", failureReason);
        }

        controller.killAgents();
        controller.stopStatisticsCapture();
        controller.generateReport(false, false);
        controller.stopTest(false);

        return controller;
    }

    private void attachFailureMonitor(final SwingConsoleController controller) {
        timer = TimerUtils.everySecond("Failing transaction monitor", new Runnable() {
            @Override
            public void run() {
                logger.debug("Failure timer check...");
                checkForFailures(controller);
            }
        });
    }

    private void checkForFailures(final SwingConsoleController controller) {
        final ConsoleModel model = controller.getModel();

        if (listener.getAbandoned() != null) {
            flagTestFailure("Test run has been abandoned : {}", listener.getAbandoned());
        }

        // Always check transaction count failures, even in the warmup
        checkForTransactionFailureCountThresholdFailures(model);

        // Always make sure we are receiving agent updates
        checkWeAreStillReceivingResults(model);

        if (warmedUp) {
            // Only check the transaction time thresholds after the warmup has finished
            checkForTransactionTimeThresholdFailures(model);
        }
    }

    private void checkWeAreStillReceivingResults(ConsoleModel model) {
        long totalResults = 0;
        final ObservableList<TransactionResultModel> transactionResultModels = model.getTransactionResultModels();
        for (TransactionResultModel trm : transactionResultModels) {
            ObservableInteger resultCount = trm.getResultCount();
            totalResults += resultCount.intValue();
        }

        if (totalResults == lastTotalResults) {
            resultsNotIncreased++;

            if (resultsNotIncreased == model.getNoResultsTimeout()) {
                flagTestFailure(
                        "We seem to have stopped receving results from one or more agents ({} seconds have elapsed since the last result was recorded) - if you can't see any other errors you might need to check the agents wrapper logs to find out what when wrong - abandoning test...",
                        resultsNotIncreased);
            }
        } else {
            resultsNotIncreased = 0;
        }

        lastTotalResults = totalResults;
    }

    private void checkForTransactionFailureCountThresholdFailures(ConsoleModel model) {
        final ObservableList<TransactionResultModel> transactionResultModels = model.getTransactionResultModels();

        long totalFailures = 0;
        for (TransactionResultModel trm : transactionResultModels) {
            totalFailures += trm.getFailedTransactions();
            if (trm.getTransactionFailureCountThreshold() != -1) {
                if (trm.getFailedTransactions() >= trm.getTransactionFailureCountThreshold()) {
                    flagTestFailure(
                            "Test '{}' has exceeded its threshold for failed transactions - {} failures have been recorded out of the threshold of {} transactions",
                            trm.getTestName(),
                            trm.getFailedTransactions(),
                            trm.getTransactionFailureCountThreshold());
                }
            }
        }

        if (model.getFailedTransactionCountFailureThreshold() != -1) {
            if (totalFailures >= model.getFailedTransactionCountFailureThreshold()) {
                flagTestFailure(
                        "The threshold for total transaction failures has been exceeded - {} failures have been recorded out of the threshold of {} transactions",
                        totalFailures,
                        model.getFailedTransactionCountFailureThreshold());
            }
        }
    }

    private void checkForTransactionTimeThresholdFailures(ConsoleModel model) {

        final ObservableList<TransactionResultModel> transactionResultModels = model.getTransactionResultModels();
        for (TransactionResultModel trm : transactionResultModels) {
            if (trm.getFailureThreshold() != Double.NaN) {

                double valueForComparison;

                TransactionTimeThresholdMode mode = trm.getFailureThresholdMode();
                switch (mode) {
                    case Mean:
                        valueForComparison = trm.getSuccessElapsedMS();
                        break;
                    case TP90:
                        valueForComparison = trm.getTp90().doubleValue();
                        break;
                    case Stddev:
                        valueForComparison = trm.getStddev().doubleValue();
                        break;
                    default: {
                        throw new FormattedRuntimeException("Unsupported threshold mode {}", mode);
                    }
                }

                if (valueForComparison > trm.getFailureThreshold()) {
                    NumberFormat nf = NumberFormat.getInstance();

                    dumpTestValues(model);

                    if (trm.getResultCount().intValue() > trm.getFailureThresholdResultCountMinimum()) {
                        flagTestFailure(
                                "The failure threshold for test '{}' ({}) result {} ms was higher than the failure threshold of {} ms",
                                trm.getTestName(),
                                mode,
                                nf.format(valueForComparison),
                                nf.format(trm.getFailureThreshold()));
                    } else {
                        logger.info(
                                "The failure threshold for test '{}' ({}) result {} ms was higher than the failure threshold of {} ms - we haven't received enough results ({} out of {}) to cause the test to fail yet though",
                                trm.getTestName(),
                                mode,
                                nf.format(valueForComparison),
                                nf.format(trm.getFailureThreshold()),
                                trm.getResultCount().intValue(),
                                trm.getFailureThresholdResultCountMinimum());
                    }
                }
            }

        }
    }

    private void dumpTestValues(ConsoleModel model) {
        NumberFormat nf = NumberFormat.getInstance();
        final ObservableList<TransactionResultModel> transactionResultModels = model.getTransactionResultModels();
        logger.info(String.format(" %20s | %20s | %20s | %20s |", "Test", "Transactions", "TPS", "Mean"));
        for (TransactionResultModel trm : transactionResultModels) {
            logger.info(String.format(" %20s | %20s | %20s | %20s |",
                                      trm.getTestName(),
                                      nf.format(trm.getTotalTransactions()),
                                      nf.format(trm.getSuccessPerSecondNew()),
                                      nf.format(trm.getSuccessTimeMeanMillis())));
        }
    }

    protected void flagTestFailure(String format, Object... params) {
        logger.warn(format, params);
        failureDetected = true;
        failureReason = StringUtils.format(format, params);
        failureLatch.countDown();
        timer.cancel();
    }

    private void waitForAgents(ConsoleModel model) {
        long startTime = System.currentTimeMillis();

        boolean gotAgents = false;


        while (!gotAgents) {

            logger.info("Waiting for agent connections...");
            int agentCount = 0;

            List<AgentModel> agentModels = model.getAgentModels();
            for (AgentModel agentModel : agentModels) {
                if (agentModel.isConnected()) {
                    agentCount++;
                    logger.info("Agent '{}' is connected", agentModel);
                }
            }

            if (agentCount >= agentsRequired) {
                logger.info("Agents connected {}, this is enough to meet the agentsRequired threshold of {}",
                            agentCount,
                            agentsRequired);
                gotAgents = true;
            } else {
                long waitingTime = System.currentTimeMillis() - startTime;

                logger.info(
                        "Agents connected {}, this is not enough to meet the agentsRequired threshold of {}, waiting ({}) for more agents...",
                        agentCount,
                        agentsRequired,
                        TimeUtils.formatIntervalMilliseconds(waitingTime));
                if (waitingTime > timeToWaitForAgents) {
                    throw new RuntimeException("Couldn't find enough agents to start the test");
                }
                ThreadUtils.sleep(500);
            }
        }
    }

    public void setReportFolder(String reportFolder) {
        this.reportFolder = reportFolder;
    }

    public static void runStatic(String configuration, long warmup, long testTime, int agents) {
        Headless headless = new Headless();
        headless.setWarmupTime(warmup);
        headless.setSampleTime(testTime);
        headless.setAgentsRequired(agents);
        headless.run(configuration);
    }

    public static void main(String[] args) {
        SystemErrStream.gapThreshold = 1500;
        System.out.println(Arrays.toString(args));

        if (args.length == 6) {
            Headless headless = new Headless();
            headless.setWarmupTime(Long.parseLong(args[1]));
            headless.setSampleTime(Long.parseLong(args[2]));
            headless.setAgentsRequired(Integer.parseInt(args[3]));
            headless.setTimeToWaitForAgents(Long.parseLong(args[4]));
            headless.setReportFolder(args[5]);

            try {
                SwingConsoleController run = headless.run(args[0]);
                if (run.getModel().getFailureReason() != null) {
                    System.err.println("Test failed : " + run.getModel().getFailureReason());
                    System.exit(-1);
                } else {
                    System.out.println("Test completed, exiting JVM...");
                    System.exit(0);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            System.out.println(
                    "Usage : Headless [xml config file] [warmup time ms] [sample time ms] [agents required to start] [time to wait for agents ms] [report folder]");
        }

    }
}

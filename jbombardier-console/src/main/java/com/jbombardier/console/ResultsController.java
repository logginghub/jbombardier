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

package com.jbombardier.console;

import com.jbombardier.common.AgentStats;
import com.jbombardier.common.AgentStats.TestStats;
import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.model.AgentModel;
import com.jbombardier.console.model.PhaseModel;
import com.jbombardier.console.model.TransactionResultModel;
import com.jbombardier.console.model.result.AgentResult;
import com.jbombardier.console.model.result.PhaseResult;
import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.console.model.result.TransactionResult;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.SinglePassStatisticsLongPrecisionCircular;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofStreamSerialiser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class ResultsController {

    private static final Logger logger = Logger.getLoggerFor(ResultsController.class);
    private File resultsFolder;
    private BufferedOutputStream statisticsStream;
    private SofConfiguration sofConfig;
    private int maximumResultsPerKey = 100;

    private HashMap<String, SinglePassStatisticsLongPrecisionCircular> successStatsByTest = new FactoryMap<String, SinglePassStatisticsLongPrecisionCircular>() {
        @Override protected SinglePassStatisticsLongPrecisionCircular createEmptyValue(String key) {
            SinglePassStatisticsLongPrecisionCircular stats = new SinglePassStatisticsLongPrecisionCircular(TimeUnit.NANOSECONDS);
            stats.setMaximumResults(maximumResultsPerKey);
            return stats;
        }
    };
    private HashMap<String, SinglePassStatisticsLongPrecisionCircular> failureStatsByTest = new FactoryMap<String, SinglePassStatisticsLongPrecisionCircular>() {
        @Override protected SinglePassStatisticsLongPrecisionCircular createEmptyValue(String key) {
            SinglePassStatisticsLongPrecisionCircular stats = new SinglePassStatisticsLongPrecisionCircular(TimeUnit.NANOSECONDS);
            stats.setMaximumResults(maximumResultsPerKey / 10);
            return stats;
        }
    };
    private Timer statsUpdater;

    public ResultsController(File resultsFolder) {
        this.resultsFolder = resultsFolder;
        resultsFolder.mkdirs();

        sofConfig = new SofConfiguration();
        sofConfig.registerType(CapturedStatistic.class, 0);
    }

    public void handleAgentStatusUpdate(AgentStats agentStats) {

        logger.fine("New stats bundle received from an agent : {}", agentStats);

        List<TestStats> testStatsList = agentStats.getTestStats();
        for (TestStats testStats : testStatsList) {
            String testName = testStats.getKey().toString();

            SinglePassStatisticsLongPrecisionCircular successStats;
            SinglePassStatisticsLongPrecisionCircular failureStats;

            synchronized (successStatsByTest) {
                successStats = successStatsByTest.get(testName);
            }

            synchronized (failureStatsByTest) {
                failureStats = failureStatsByTest.get(testName);
            }

            synchronized (successStats) {
                List<Long> successResults = testStats.successResults;
                if (successResults.isEmpty()) {
                    double meanSuccess;
                    if (testStats.transactionsSuccess > 0) {
                        meanSuccess = testStats.totalDurationSuccess / testStats.transactionsSuccess;
                    } else {
                        meanSuccess = 0;
                    }

                    successStats.addValue((long) meanSuccess);
                } else {
                    for (Long result : successResults) {
                        successStats.addValue(result);
                    }
                }
            }

            synchronized (failureStats) {
                List<Long> failResults = testStats.failResults;
                if (failResults.isEmpty()) {
                    double meanFailed;

                    if (testStats.transactionsFailed > 0) {
                        meanFailed = testStats.totalDurationFailed / testStats.transactionsFailed;
                    } else {
                        meanFailed = 0;
                    }

                    failureStats.addValue((long) meanFailed);
                } else {
                    for (Long result : failResults) {
                        failureStats.addValue(result);
                    }
                }
            }
        }
    }

    public void setMaximumResultsPerKey(int maximumResultsPerKey) {
        this.maximumResultsPerKey = maximumResultsPerKey;
    }

    public HashMap<String, SinglePassStatisticsLongPrecisionCircular> getSuccessStatsByTest() {
        return successStatsByTest;
    }

    public HashMap<String, SinglePassStatisticsLongPrecisionCircular> getFailureStatsByTest() {
        return failureStatsByTest;
    }

    public void generateStats() {
        Collection<SinglePassStatisticsLongPrecisionCircular> values = successStatsByTest.values();
        for (SinglePassStatisticsLongPrecisionCircular singlePassStatistics : values) {
            singlePassStatistics.doCalculations();
        }

        values = failureStatsByTest.values();
        for (SinglePassStatisticsLongPrecisionCircular singlePassStatistics : values) {
            singlePassStatistics.doCalculations();
        }
    }

    public void resetStats() {
        successStatsByTest.clear();
        failureStatsByTest.clear();
    }

    public void startStatsUpdater(final JBombardierModel model) {

        statsUpdater = TimerUtils.everySecond("DetailedStatsUpdater", new Runnable() {
            @Override public void run() {

                Stopwatch stopwatch = Stopwatch.start("DetailedStatsUpdate");
                Set<String> keyCopy;
                synchronized (successStatsByTest) {
                    Set<String> keySet = successStatsByTest.keySet();
                    keyCopy = new HashSet<String>(keySet);
                }

                for (String key : keyCopy) {
                    SinglePassStatisticsLongPrecisionCircular stats = successStatsByTest.get(key);

                    TransactionResultModel transactionResultModel = model.getCurrentPhase().get().getTransactionResultModelForTransaction(key.toString());

                    if (transactionResultModel != null) {

                        synchronized (stats) {
                            stats.doCalculations();
                            transactionResultModel.getTp90().set(stats.getPercentiles()[90] * 1e-6);

                            double value = stats.getStandardDeviationPopulationDistrubution() * 1e-6;
                            transactionResultModel.getStddev().set(value);
                        }
                    } else {
                        logger.warning("No transaction results model found for key '{}' - this looks like a bug, the results controller hasn't been reset", key);
                    }

                }
                logger.debug(stopwatch);
            }
        });

    }

    public void stopStatsUpdater() {
        if (statsUpdater != null) {
            statsUpdater.cancel();
            statsUpdater = null;
        }
    }

    public void reduceResultStorage() {

        maximumResultsPerKey = maximumResultsPerKey * 50 / 100;
        logger.warning("Reduced the results per key down to {} due to memory pressures", maximumResultsPerKey);

        synchronized (successStatsByTest) {
            Set<Entry<String, SinglePassStatisticsLongPrecisionCircular>> entrySet = successStatsByTest.entrySet();
            for (Entry<String, SinglePassStatisticsLongPrecisionCircular> entry : entrySet) {
                entry.getValue().setMaximumResults(maximumResultsPerKey);
            }
        }

        synchronized (failureStatsByTest) {
            Set<Entry<String, SinglePassStatisticsLongPrecisionCircular>> entrySet = failureStatsByTest.entrySet();
            for (Entry<String, SinglePassStatisticsLongPrecisionCircular> entry : entrySet) {
                entry.getValue().setMaximumResults(maximumResultsPerKey);
            }
        }
    }

    public void addCapturedStatistic(CapturedStatistic statistic) {
        logger.info("Captured stat : {}", statistic);
        try {
            SofStreamSerialiser.write(statisticsStream, statistic, sofConfig);
        } catch (SofException e) {
            logger.warn(e, "Failed to serialise captured statistic '{}'", statistic);
        }
    }

    public void openStreamingFiles() throws IOException {
        File file = getStreamingFile();
        logger.info("Opening streaming file for captured statistics '{}'", file.getAbsolutePath());
        statisticsStream = new BufferedOutputStream(new FileOutputStream(file));
    }

    private File getStreamingFile() {
        return new File(resultsFolder, "statistics.sof");
    }

    public void closeStreamingFiles() {
        if (statisticsStream != null) {
            FileUtils.closeQuietly(statisticsStream);
            logger.info("Closed streaming file for captured statistics");
            statisticsStream = null;
        }
    }

    public void visitStreamingFile(final Destination<CapturedStatistic> destination) {
        File streamingFile = getStreamingFile();
        if (streamingFile.exists()) {
            BufferedInputStream bis = null;

            try {
                bis = new BufferedInputStream(new FileInputStream(streamingFile));
                SofStreamSerialiser.visit(bis, streamingFile.length(), sofConfig, new Destination<SerialisableObject>() {
                            @Override public void send(SerialisableObject serialisableObject) {
                                destination.send((CapturedStatistic) serialisableObject);
                            }
                        });
            } catch (IOException e) {
                logger.warn(e, "Failed to read serialised captured statistics file");
            } catch (SofException e) {
                logger.warn(e, "Failed to read serialised captured statistics file");
            } finally {
                FileUtils.closeQuietly(bis);
            }
        }
    }

    public void flush() {
        if (statisticsStream != null) {
            try {
                statisticsStream.flush();
            } catch (IOException e) {
            }
        }

    }

    public RunResult createSnapshot(JBombardierModel model) {

        RunResult result = new RunResult();

        result.setStartTime(model.getTestStartTime());
        result.setConfigurationName(model.getTestName());
        result.setFailureReason(model.getFailureReason());

        ObservableList<PhaseModel> phaseModels = model.getPhaseModels();
        for (PhaseModel phaseModel : phaseModels) {

            PhaseResult phaseResult = new PhaseResult();
            phaseResult.setDuration(phaseModel.getPhaseDuration().get());
            phaseResult.setWarmup(phaseModel.getWarmupDuration().get());
            phaseResult.setPhaseName(phaseModel.getPhaseName().get());

            ObservableList<TransactionResultModel> transactionResultModels = phaseModel.getTransactionResultModels();
            for (TransactionResultModel trm : transactionResultModels) {

                TransactionResult tr = new TransactionResult();

                tr.setTestName(trm.getTestName().get());
                tr.setTransactionName(trm.getTransactionName().get());
                tr.setTotalTransactionCount(trm.getSuccessfulTransactionsCountTotal().get());

                tr.setSuccessfulTransactionCount(trm.getSuccessfulTransactionsCountTotal().get());

                long totalTransactions = trm.getSuccessfulTransactionsCountTotal().get();
                long totalTransactionTime = trm.getSuccessfulTransactionsDurationTotal().get();
                long testDuration = trm.getTestDuration().get();

                double meanTPS = totalTransactions / (testDuration/1000d);
                double meanTransactionTimeNS = totalTransactionTime / totalTransactions;

                tr.setSuccessfulTransactionMeanTransactionsPerSecond(meanTPS);
                tr.setSuccessfulTransactionMeanDuration(meanTransactionTimeNS);
                tr.setSuccessfulTransactionMeanTotalDuration(trm.getSuccessfulTransactionTotalDuration().get());
                tr.setSuccessfulTransactionMeanTransactionsPerSecondTarget(trm.getTargetSuccessfulTransactionsPerSecond().get());

                tr.setUnsuccessfulTransactionCount(trm.getUnsuccessfulTransactionsCountTotal().get());
                tr.setUnsuccessfulTransactionMeanDuration(trm.getUnsuccessfulTransactionDuration().get());

                tr.setSla(trm.getSuccessfulTransactionDurationSLA().get());

                phaseResult.getTransactionResults().add(tr);

            }

            result.getPhaseResults().add(phaseResult);
        }

        List<AgentModel> agentModels = model.getAgentModels();
        for (AgentModel agentModel : agentModels) {

            AgentResult agentResult = new AgentResult();
            agentResult.setAddress(agentModel.getAddress().get());
            agentResult.setPort(agentModel.getPort().get());
            agentResult.setName(agentModel.getName().get());

            result.getAgentResults().add(agentResult);
        }

        return result;

    }
}

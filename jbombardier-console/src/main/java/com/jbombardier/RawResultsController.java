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

package com.jbombardier;

import com.jbombardier.common.AgentStats;
import com.jbombardier.common.AgentStats.TestStats;
import com.jbombardier.console.model.TransactionResultModel;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.SinglePassStatisticsLongPrecisionCircular;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.logging.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class RawResultsController {

    private static final Logger logger = Logger.getLoggerFor(RawResultsController.class);
    private File resultsFolder;
    private BufferedOutputStream statisticsStream;

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

    public RawResultsController(File resultsFolder) {
        this.resultsFolder = resultsFolder;
        resultsFolder.mkdirs();
    }

    public RawResultsController() {

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



}

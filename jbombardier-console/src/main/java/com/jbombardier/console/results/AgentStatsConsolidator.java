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

package com.jbombardier.console.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.jbombardier.JBombardierModel;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.logging.Logger;
import com.jbombardier.common.AgentStats;
import com.jbombardier.common.AgentStats.TestStats;
import com.jbombardier.common.TestKey;

// Not currently used!
public class AgentStatsConsolidator {

    private static final Logger logger = Logger.getLoggerFor(AgentStatsConsolidator.class);
    private int agentsInTest;
    private volatile Map<String, AgentStats> currentAgentStats = new HashMap<String, AgentStats>();
    private JBombardierModel model;

    public AgentStatsConsolidator(JBombardierModel model) {
        this.model = model;
    }

    public void setAgentsToExpect(int agentsToExpect) {
        this.agentsInTest = agentsToExpect;
    }

    public void addAgentStatus(AgentStats agentStats) {
        AgentStats existing = currentAgentStats.put(agentStats.getAgentName(), agentStats);
        if (existing != null) {
            logger.warn("We've replaced the test stats for agent '{}' - this shouldn't happen unless agents are sending back multiple results for the same test each second - in this case you agents may be running two tests in parallel and should be killed",
                        agentStats.getAgentName());
        }
    }

    public void consolidateStats() {

        // Hopefully only one thread will make it through this block
        Map<String, AgentStats> stats = null;
        synchronized (this) {
            if (currentAgentStats.size() == agentsInTest) {
                // Rotate the current stats map
                stats = currentAgentStats;
                currentAgentStats = new HashMap<String, AgentStats>();
            }            
        }
        
        if (stats != null) {

            // Juggle the structures around to get lists of stats by test key
            Map<TestKey, List<TestStats>> statsByTest = new FactoryMap<TestKey, List<TestStats>>() {
                @Override protected List<TestStats> createEmptyValue(TestKey key) {
                    return new ArrayList<TestStats>();
                }
            };

            Collection<AgentStats> agentStatsList = stats.values();
            for (AgentStats agentStats : agentStatsList) {

                List<TestStats> testStatsList = agentStats.getTestStats();
                for (TestStats testStats : testStatsList) {
                    TestKey key = testStats.getKey();
                    statsByTest.get(key).add(testStats);
                }
            }

            // Now aggregate each test
            Set<Entry<TestKey, List<TestStats>>> entrySet = statsByTest.entrySet();
            for (Entry<TestKey, List<TestStats>> entry : entrySet) {

                TestKey testKey = entry.getKey();
                List<TestStats> values = entry.getValue();

                // James - have decided to switch back to the old ones
                NewTransactionResultModel transactionModel = null;//model.getNewTransactionResultModelForTest(testKey);

                double successPerSecondTotal = 0;
                double failurePerSecondTotal = 0;
                double sumSuccessDuration = 0;
                double sumSuccessTotalDuration = 0;
                double sumFailureDuration = 0;
                long failedTransactions = 0;
                long successTransactions = 0;

                for (TestStats perAgentTestStats : values) {

                    double successRatePerSecond = perAgentTestStats.transactionsSuccess / (perAgentTestStats.sampleDuration / 1000d);
                    double failRatePerSecond = perAgentTestStats.transactionsFailed / (perAgentTestStats.sampleDuration / 1000d);

                    double averageSuccess;
                    double averageTotalSuccess;
                    double averageFailures;

                    if (perAgentTestStats.transactionsSuccess > 0) {
                        averageSuccess = perAgentTestStats.totalDurationSuccess / perAgentTestStats.transactionsSuccess;
                        averageTotalSuccess = perAgentTestStats.totalDurationTotalSuccess / perAgentTestStats.transactionsSuccess;
                    }
                    else {
                        averageSuccess = 0;
                        averageTotalSuccess = 0;
                    }

                    if (perAgentTestStats.transactionsFailed > 0) {
                        averageFailures = perAgentTestStats.totalDurationFailed / perAgentTestStats.transactionsFailed;
                    }
                    else {
                        averageFailures = 0;
                    }

                    successPerSecondTotal += successRatePerSecond;
                    failurePerSecondTotal += failRatePerSecond;
                    sumSuccessDuration += averageSuccess;
                    sumSuccessTotalDuration += averageTotalSuccess;
                    sumFailureDuration += averageFailures;

                    failedTransactions += perAgentTestStats.transactionsFailed;
                    successTransactions += perAgentTestStats.transactionsSuccess;

                    List<Long> successResults = perAgentTestStats.successResults;
                    transactionModel.addResults(successResults);
                }

                double averageSuccessDuration = sumSuccessDuration / agentsInTest;
                double averageSuccessTotalDuration = sumSuccessTotalDuration / agentsInTest;
                double averageFailureDuration = sumFailureDuration / agentsInTest;

                transactionModel.getFailedCount().set(failedTransactions);
                transactionModel.getFailedMeanNanos().set(averageFailureDuration);
                transactionModel.getFailuresPerSecond().set(failurePerSecondTotal);
                transactionModel.getSuccessCount().set(successTransactions);
                transactionModel.getSuccessMeanNanos().set(averageSuccessDuration);
                transactionModel.getSuccessPerSecond().set(successPerSecondTotal);
                transactionModel.getSuccessTotalMeanNanos().set(averageSuccessTotalDuration);
                transactionModel.getSuccessTP90Nanos().set(0);
            }
        }
    }

}

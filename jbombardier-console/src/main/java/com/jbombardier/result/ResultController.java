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

import com.jbombardier.common.AgentStats;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;

import java.util.List;

/**
 * Results structure:
 * <p/>
 * Run results -> Phase results -> Test results -> Per Agent Results -> Time periods containing results // TODO : this
 * current supports aggregated results, it would be nice if the raw results was just x/y ie non-aggregated at its root
 * Transaction Results -> Per agent transaction results -> Time periods containing results
 * <p/>
 * You can view aggregations as you move higher up towards the Test results level.
 * <p/>
 * Created by james on 10/01/15.
 */
public class ResultController {

    private final JBombardierRunResult model;
    private TimeProvider time = new SystemTimeProvider();
    private JBombardierPhaseResult currentPhase;

    public ResultController(JBombardierRunResult resultModel) {
        this.model = resultModel;
    }

    public void setTime(TimeProvider time) {
        this.time = time;
    }

    public void onRunStarted() {
        model.setStartTime(time.getTime());
    }

    public void onAgentStatsResult(AgentStats stats, String agentAddress) {
        if (currentPhase == null) {
            throw new IllegalStateException(
                    "No current phase - make sure you call onPhaseStarted before the first agent results arrive.");
        } else {

            JBombardierAgentTestResult result = new JBombardierAgentTestResult();
            String agentName = stats.getAgentName();
            result.setAgentName(agentName);
            result.setAgentName(agentAddress);

            List<AgentStats.TestStats> testStats = stats.getTestStats();
            for (AgentStats.TestStats testStat : testStats) {

                List<Long> failResults = testStat.failResults;
                List<Long> successResults = testStat.successResults;

                // These two make up the key for the results object. Note that the time is not sent from the agent.
                String testName = testStat.getTestName();
                String transactionName = testStat.getTransactionName();

                // TODO : not sure why thread count is here, must be a frontend display thing?
                int threadCount = testStat.getThreadCount();

                JBombardierTestResult target;

                // jshaw - if there is a transactionId, we need to apply the results to the transaction directly, rather than to the parent test
                JBombardierTestResult testResult = currentPhase.getTestResults().get(testName);
                if (StringUtils.isNotNullOrEmpty(transactionName)) {
                    JBombardierTestResult transactionResult = testResult.getTransactionResults().get(transactionName);
                    target = transactionResult;
                } else {
                    target = testResult;
                }

                JBombardierAgentTestResult perAgentResult = target.getPerAgentResults().get(agentName);
                JBombardierTestElement newTestElement = new JBombardierTestElement();

                // These are the summary counts for the total transactions in this time period
                newTestElement.setSuccessCount(testStat.transactionsSuccess);
                newTestElement.setFailureCount(testStat.transactionsFailed);

                newTestElement.setTotalSuccessDurations(testStat.totalDurationSuccess);
                newTestElement.setTotalFailureDurations(testStat.totalDurationFailed);

                // This is the extra info about how long the iteration takes including setup
                newTestElement.setTotalSuccessDurationsIncludingSetup(testStat.totalDurationTotalSuccess);

                // Now - if they exist - apply the individual results
                newTestElement.getSuccessResults().addAll(testStat.successResults);
                newTestElement.getFailureResults().addAll(testStat.failResults);

                perAgentResult.getTestElements().add(newTestElement);

            }


        }
    }

    public JBombardierRunResult getModel() {
        return model;
    }

    public void onPhaseStarted(String phaseName) {

        JBombardierPhaseResult phaseResult = new JBombardierPhaseResult();
        phaseResult.setPhaseName(phaseName);
        phaseResult.setPhaseStartTime(time.getTime());

        model.getPhaseResults().add(phaseResult);

        currentPhase = phaseResult;

    }
}

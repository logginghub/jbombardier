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

import com.jbombardier.JBombardierModel;
import com.jbombardier.RawResultsController;
import com.jbombardier.RunResultBuilder;
import com.jbombardier.common.AgentStats;
import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.model.AgentModel;
import com.jbombardier.console.model.PhaseModel;
import com.jbombardier.console.model.TestModel;
import com.jbombardier.console.model.result.PhaseResult;
import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.console.model.result.TransactionResult;
import com.logginghub.utils.FixedTimeProvider;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.jbombardier.common.AgentStats.AgentStatsBuilder.agentStats;
import static com.jbombardier.common.AgentStats.TestStatsBuilder.testStats;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RunResultBuilderTest {

    @Test
    public void test_empty() {
        JBombardierModel model = new JBombardierModel();

        RunResultBuilder runResultBuilder = new RunResultBuilder();
        CapturedStatisticsHelper capturedStatisticsHelper = new CapturedStatisticsHelper(model);
        RawResultsController rawResultsController = new RawResultsController();

        RunResult snapshot = runResultBuilder.createSnapshot(model, capturedStatisticsHelper, rawResultsController);

        assertThat(snapshot.getAgentResults().size(), is(0));
    }

    @Test
    public void test_captured_stats() throws IOException {

        PhaseModel phase1 = new PhaseModel();
        phase1.getPhaseName().set("Phase 1");

        PhaseModel phase2 = new PhaseModel();
        phase2.getPhaseName().set("Phase 2");

        JBombardierModel model = new JBombardierModel();
        model.getPhaseModels().add(phase1);
        model.getPhaseModels().add(phase2);

        CapturedStatistic capturedStatistic1 = new CapturedStatistic(0, "a/b/c", "stat1");
        CapturedStatistic capturedStatistic2 = new CapturedStatistic(0, "a/b/c", "stat2");
        CapturedStatistic capturedStatistic3 = new CapturedStatistic(0, "a/b/c", "stat3");
        CapturedStatistic capturedStatistic4 = new CapturedStatistic(0, "a/b/c", "stat4");

        RunResultBuilder runResultBuilder = new RunResultBuilder();
        CapturedStatisticsHelper capturedStatisticsHelper = new CapturedStatisticsHelper(model);
        RawResultsController rawResultsController = new RawResultsController();

        // Imagine the test is started here...
        model.getCurrentPhase().set(phase1);

        capturedStatisticsHelper.addCapturedStatistic(capturedStatistic1);
        capturedStatisticsHelper.addCapturedStatistic(capturedStatistic2);

        model.getCurrentPhase().set(phase2);

        capturedStatisticsHelper.addCapturedStatistic(capturedStatistic3);
        capturedStatisticsHelper.addCapturedStatistic(capturedStatistic4);

        // And finishes here...
        capturedStatisticsHelper.closeStreamingFiles();

        RunResult snapshot = runResultBuilder.createSnapshot(model, capturedStatisticsHelper, rawResultsController);

        assertThat(snapshot.getPhaseResults().size(), is(2));
        assertThat(snapshot.getPhaseResults().get(0).getPhaseName(), is("Phase 1"));
        assertThat(snapshot.getPhaseResults().get(1).getPhaseName(), is("Phase 2"));

        assertThat(snapshot.getPhaseResults().get(0).getCapturedStatistics().size(), is(2));
        assertThat(snapshot.getPhaseResults().get(0).getCapturedStatistics().get(0).getValue(), is("stat1"));
        assertThat(snapshot.getPhaseResults().get(0).getCapturedStatistics().get(1).getValue(), is("stat2"));

        assertThat(snapshot.getPhaseResults().get(1).getCapturedStatistics().size(), is(2));
        assertThat(snapshot.getPhaseResults().get(1).getCapturedStatistics().get(0).getValue(), is("stat3"));
        assertThat(snapshot.getPhaseResults().get(1).getCapturedStatistics().get(1).getValue(), is("stat4"));

    }

    @Test
    public void test_snapshot() {

        PhaseModel phase1 = new PhaseModel();
        phase1.getPhaseName().set("Phase 1");

        TestModel test1 = new TestModel();
        test1.setName("Test1");
        test1.getTargetThreads().set(2);
        Map<String, Double> slas = new HashMap<String, Double>();
        slas.put("Transaction1", 1.23d);
        test1.setTransactionSLAs(slas);

        phase1.getTestModels().add(test1);

        JBombardierModel model = new JBombardierModel();
        FixedTimeProvider timeProvider = new FixedTimeProvider(0);
        model.setTimeProvider(timeProvider);
        model.getPhaseModels().add(phase1);

        AgentModel agent1 = new AgentModel("Agent1", "", 0);
        AgentModel agent2 = new AgentModel("Agent2", "", 0);

        model.addAgentModel(agent1);
        model.addAgentModel(agent2);

        model.setAgentsInTest(2);

        model.getCurrentPhase().set(phase1);
        model.setPhaseStartTime(0);

        timeProvider.setTime(1000);
        AgentStats agent1Stats = agentStats().agentName("Agent1").testStats(testStats("Test1").transactionName("Transaction1").successResult(10, 20, 30).totalDurationSuccessResult(12, 22, 32)).toStats();
        AgentStats agent2Stats = agentStats().agentName("Agent2").testStats(testStats("Test1").transactionName("Transaction1").successResult(11, 21, 31).totalDurationSuccessResult(13, 23, 33)).toStats();

        RunResultBuilder runResultBuilder = new RunResultBuilder();
        CapturedStatisticsHelper capturedStatisticsHelper = new CapturedStatisticsHelper(model);
        RawResultsController rawResultsController = new RawResultsController();

        model.onAgentStatusUpdate(agent1Stats);
        model.onAgentStatusUpdate(agent2Stats);

        rawResultsController.handleAgentStatusUpdate(agent1Stats);
        rawResultsController.handleAgentStatusUpdate(agent2Stats);

        RunResult snapshot = runResultBuilder.createSnapshot(model, capturedStatisticsHelper, rawResultsController);

        assertThat(snapshot.getPhaseResults().size(), is(1));
        assertThat(snapshot.getPhaseResults().get(0).getPhaseName(), is("Phase 1"));

        PhaseResult phaseResult = snapshot.getPhaseResults().get(0);

        assertThat(phaseResult.getTransactionResults().size(), is(1));

        TransactionResult transactionResult1 = phaseResult.getTransactionResults().get(0);

        assertThat(transactionResult1.getTestName(), is("Test1"));
        assertThat(transactionResult1.getTransactionName(), is("Transaction1"));

        assertThat(transactionResult1.getAgents(), is(2));
        assertThat(transactionResult1.getThreads(), is(2));

        assertThat(transactionResult1.getSampleTime(), is(1000L));

        assertThat(transactionResult1.getTotalTransactionCount(), is(6L));
        assertThat(transactionResult1.getSuccessfulTransactionCount(), is(6L));
        assertThat(transactionResult1.getUnsuccessfulTransactionCount(), is(0L));

        assertThat(transactionResult1.getSuccessfulTransactionMeanDuration(), is(20.5d));
        assertThat(transactionResult1.getSuccessfulTransactionMeanTotalDuration(), is(22.5d));
        assertThat(transactionResult1.getSuccessfulMaximumTransactionsPerSecond(), is(1000/22.5d));

        assertThat(transactionResult1.getSuccessfulTransactionMeanTransactionsPerSecond(), is(6d));
        assertThat(transactionResult1.getSuccessfulTransactionMeanTransactionsPerSecondTarget(), is(2d));

        assertThat(transactionResult1.getSuccessfulAbsoluteDeviation(), is(6.833333333333333d));
        assertThat(transactionResult1.getSuccessfulAbsoluteDeviationAsPercentage(), is(33.33333333333333d));
        assertThat(transactionResult1.getSuccessfulFastestResult(), is(10d));
        assertThat(transactionResult1.getSuccessfulSlowestResult(), is(31d));
        assertThat(transactionResult1.getSuccessfulMedian(), is(20d));
        assertThat(transactionResult1.getSuccessfulPercentiles()[90], is(30.5d));
        assertThat(transactionResult1.getSuccessfulStandardDeviation(), is(8.180260794538684d));

        assertThat(transactionResult1.getSla(), is(1.23d));

        assertThat(transactionResult1.getUnsuccessfulTransactionMeanTransactionsPerSecond(), is(0d));

    }


}
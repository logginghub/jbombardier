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

package com.jbombardier;

import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.CapturedStatisticsHelper;
import com.jbombardier.console.PhaseController;
import com.jbombardier.console.model.AgentModel;
import com.jbombardier.console.model.PhaseModel;
import com.jbombardier.console.model.TransactionResultModel;
import com.jbombardier.console.model.result.AgentResult;
import com.jbombardier.console.model.result.PhaseResult;
import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.console.model.result.TransactionResult;
import com.logginghub.utils.Destination;
import com.logginghub.utils.SinglePassStatisticsLongPrecisionCircular;
import com.logginghub.utils.observable.ObservableList;

import java.util.List;

/**
 * Created by james on 06/02/15.
 */
public class RunResultBuilder {

    public RunResult createSnapshot(JBombardierModel model, CapturedStatisticsHelper helper, RawResultsController rawResultsController) {

        RunResult result = new RunResult();

        result.setStartTime(model.getTestStartTime().get());
        result.setConfigurationName(model.getTestName().get());
        result.setFailureReason(model.getFailureReason());

        ObservableList<PhaseModel> phaseModels = model.getPhaseModels();
        for (PhaseModel phaseModel : phaseModels) {
            PhaseResult phaseResult = capturePhaseResults(phaseModel, model.getAgentsInTest(), rawResultsController);
            captureStatistics(phaseResult, helper);
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

    private void captureStatistics(final PhaseResult phaseResult, CapturedStatisticsHelper helper) {
        helper.visitStreamingFile(phaseResult.getPhaseName(), new Destination<CapturedStatistic>() {
            @Override
            public void send(CapturedStatistic capturedStatistic) {
                phaseResult.getCapturedStatistics().add(capturedStatistic);
            }
        });
    }

    private PhaseResult capturePhaseResults(PhaseModel phaseModel, int agents, RawResultsController rawResultsController) {
        PhaseResult phaseResult = new PhaseResult();
        phaseResult.setDuration(phaseModel.getPhaseDuration().get());
        phaseResult.setWarmup(phaseModel.getWarmupDuration().get());
        phaseResult.setPhaseName(phaseModel.getPhaseName().get());

        ObservableList<TransactionResultModel> transactionResultModels = phaseModel.getTransactionResultModels();
        for (TransactionResultModel trm : transactionResultModels) {

            TransactionResult tr = new TransactionResult();

            tr.setTestName(trm.getTestName().get());
            tr.setTransactionName(trm.getTransactionName().get());
            tr.setTotalTransactionCount(trm.getSuccessfulTransactionsCountTotal().get() + trm.getUnsuccessfulTransactionsCountTotal().get());

            tr.setSuccessfulTransactionCount(trm.getSuccessfulTransactionsCountTotal().get());

            long totalTransactions = trm.getSuccessfulTransactionsCountTotal().get();
            long totalTransactionTime = trm.getSuccessfulTransactionsDurationTotal().get();
            long testDuration = trm.getTestDuration().get();
            double totalDurationIncludingPreAndPost = trm.getSuccessfulTransactionsIncludingPreandPostDurationTotal().get();

            double meanTPS = totalTransactions / (testDuration / 1000d);
            double meanTransactionTimeNS;
            double meanTransactionTimeIncludingPreAndPostNS;

            if (totalTransactions > 0) {
                meanTransactionTimeNS = totalTransactionTime / (double) totalTransactions;
                meanTransactionTimeIncludingPreAndPostNS = totalDurationIncludingPreAndPost / (double)totalTransactions;
            } else {
                meanTransactionTimeNS = Double.NaN;
                meanTransactionTimeIncludingPreAndPostNS = Double.NaN;
            }

            tr.setSuccessfulTransactionMeanTransactionsPerSecond(meanTPS);
            tr.setSuccessfulTransactionMeanDuration(meanTransactionTimeNS);
            tr.setSuccessfulTransactionMeanTotalDuration(meanTransactionTimeIncludingPreAndPostNS);
            tr.setSuccessfulTransactionMeanTransactionsPerSecondTarget(trm.getTargetSuccessfulTransactionsPerSecond().get());

            tr.setSuccessfulMaximumTransactionsPerSecond(1000 / meanTransactionTimeIncludingPreAndPostNS);

            tr.setUnsuccessfulTransactionCount(trm.getUnsuccessfulTransactionsCountTotal().get());
            tr.setUnsuccessfulTransactionMeanDuration(trm.getUnsuccessfulTransactionDuration().get());

            tr.setSla(trm.getSuccessfulTransactionDurationSLA().get());

            tr.setAgents(agents);
            tr.setThreads(trm.getThreadCount().get());
            tr.setSampleTime(trm.getTestDuration().get());

            // Now for the more complex statistics stuff
            SinglePassStatisticsLongPrecisionCircular stats = rawResultsController.getSuccessStatsByTest().get(trm.getKey());
            if (stats != null) {
                stats.doCalculations();

                tr.setSuccessfulStandardDeviation(stats.getStandardDeviationPopulationDistrubution());
                tr.setSuccessfulMedian(stats.getMedian());
                tr.setSuccessfulSlowestResult(stats.getMaximum());
                tr.setSuccessfulFastestResult(stats.getMinimum());
                tr.setSuccessfulAbsoluteDeviation(stats.getAbsoluteDeviation());
                tr.setSuccessfulAbsoluteDeviationAsPercentage(stats.getPercentageAbsoluteDeviation());
                tr.setSuccessfulPercentiles(stats.getPercentiles());
            }


            phaseResult.getTransactionResults().add(tr);

        }

        List<PhaseController> phaseControllers = phaseModel.getPhaseControllers();
        for (PhaseController phaseController : phaseControllers) {
            if(phaseController instanceof ResultsSnapshotAware) {
                ResultsSnapshotAware rsa = (ResultsSnapshotAware)phaseController;
                List<ControllerResultSnapshot> results = rsa.getResultsVsBaselineEfficiency();
                phaseResult.getControllerResults().addAll(results);
            }
        }

        return phaseResult;
    }
}

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

package com.jbombardier.console;

import com.jbombardier.JBombardierController;
import com.jbombardier.JBombardierModel;
import com.jbombardier.common.AgentStats;
import com.jbombardier.common.AgentStats.TestStats;
import com.jbombardier.console.configuration.JBombardierConfiguration;
import com.jbombardier.console.model.AgentModel;
import com.jbombardier.console.model.PhaseModel;
import com.jbombardier.console.model.TestModel;
import com.jbombardier.console.sample.SleepTest;
import com.logginghub.messaging2.kryo.KryoClient;
import com.logginghub.messaging2.kryo.ResponseHandler;
import com.logginghub.utils.FileUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestConsoleController {

    @Test public void test_generate_report() {

        JBombardierModel model = new JBombardierModel();
        JBombardierConfiguration configuration = new JBombardierConfiguration();

        File reports = FileUtils.createRandomFolder("target/tests/testJBombardierConsoleController");
        configuration.setReportsFolder(reports.getAbsolutePath());

        JBombardierController controller = new JBombardierController(model, configuration);

        TestStats testStats = new TestStats();
        testStats.sampleDuration = 1000;

        testStats.totalDurationSuccess = 200;
        testStats.totalDurationFailed = 0;

        testStats.transactionsSuccess = 5;
        testStats.transactionsFailed = 0;

        testStats.totalDurationTotalSuccess = 300;

        testStats.setTestName("test1");

        AgentStats agentStats = new AgentStats();
        agentStats.setAgentName("Agent1");
        agentStats.addTestStats(testStats);

        model.getTransactionRateModifier().set(1);

        TestModel testModel = new TestModel("test1", SleepTest.class.getName());
        testModel.getTargetRate().set(100);
        testModel.getTargetThreads().set(5);

        PhaseModel phaseModel = new PhaseModel();
        phaseModel.getPhaseName().set("Phase 1");
        phaseModel.getTestModels().add(testModel);

        model.getPhaseModels().add(phaseModel);

        // Create a fake agent
        List<AgentModel> agentModels = new ArrayList<AgentModel>();
        model.setAgentsInTest(1);
        AgentModel agentModel = new AgentModel("Agent1", "localhost", 20001);
        agentModel.getConnected().set(true);
        agentModels.add(agentModel);

        // Setup a mock kryo client to response to the package messages
        final KryoClient kryoClient = Mockito.mock(KryoClient.class);

        Answer answer = new Answer() {
            public Object answer(InvocationOnMock invocation) {
                ResponseHandler handler = (ResponseHandler) invocation.getArguments()[2];
                handler.onResponse("Success");
                return null;
            }
        };

        Mockito.doAnswer(answer)
               .when(kryoClient)
               .sendRequest(Mockito.anyString(), Mockito.anyObject(), Mockito.any(ResponseHandler.class));

        agentModel.setKryoClient(kryoClient);

        model.getAgentModels().addAll(agentModels);
        controller.getAgentsByAgentName().put("Agent1", agentModel);

        controller.initialiseStats();
        controller.startTest();

        // Simulate the generate results bit

        // Simulate the agent sending in a status update
        controller.handleAgentStatusUpdate(agentStats);

        // Make sure the results have been captured properly
        // TODO : refactor fix
//        ListBackedMap<String, TransactionResultModel> results = controller.getModel().getTotalTransactionModelsByTestName();
//        assertThat(results.get("test1"), is(not(nullValue())));

        controller.doPostTestTasks();

        File reportFile = new File(reports, "index.html");
        assertThat(reportFile.exists(), is(true));
//        assertThat(new File(reports, "output.csv").exists(), is(true));
//        assertThat(new File(reports, "report.css").exists(), is(true));

//        String content = FileUtils.read(reportFile);
//        assertThat(content, containsString("<h2>All transactions</h2>"));
    }
}

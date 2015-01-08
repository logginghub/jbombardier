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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jbombardier.console.model.AgentModel;
import org.junit.Test;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.ListBackedMap;
import com.jbombardier.common.AgentStats;
import com.jbombardier.common.AgentStats.TestStats;
import com.jbombardier.console.configuration.InteractiveConfiguration;
import com.jbombardier.console.model.TestModel;
import com.jbombardier.console.model.TransactionResultModel;
import com.jbombardier.console.sample.SleepTest;

public class TestConsoleController {

    @Test public void testGenerateReport() {

        SwingConsoleController controller = new SwingConsoleController();

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

        ConsoleModel model = new ConsoleModel();
        model.setTransactionRateModifier(1);

        TestModel testModel = new TestModel("test1", SleepTest.class.getName());
        testModel.setTargetRate(100);
        testModel.setTargetThreads(5);

        model.addTestModel(testModel);

        controller.setModel(model);

        InteractiveConfiguration configuration = new InteractiveConfiguration();

        // Create a fake agent
        List<AgentModel> agentModels = new ArrayList<AgentModel>();
        model.setAgentsInTest(1);
        AgentModel agentModel = new AgentModel("Agent1", "localhost", 20001);
        agentModels.add(agentModel);
        model.getAgentModels().addAll(agentModels);
        controller.getAgentsByAgentName().put("Agent1", agentModel);

        controller.initialiseStats();

        // Simulate the generate results bit
        File reports = FileUtils.createRandomFolder("target/tests/testJBombardierConsoleController");
        controller.setReportsPath(reports);

        // Initialise the controller internals
        controller.initialise(configuration, model);
        // Simulate the agent sending in a status update
        controller.handleAgentStatusUpdate(agentStats);

        // Make sure the results have been captured properly
        ListBackedMap<String, TransactionResultModel> results = controller.getModel().getTotalTransactionModelsByTestName();

        assertThat(results.get("test1"), is(not(nullValue())));

        String errors = controller.generateReport(false, false);
        assertThat(errors, is(""));

        File reportFile = new File(reports, "output.html");
        assertThat(reportFile.exists(), is(true));
        assertThat(new File(reports, "output.csv").exists(), is(true));
        assertThat(new File(reports, "report.css").exists(), is(true));

        String content = FileUtils.read(reportFile);
        assertThat(content, containsString("<h2>All transactions</h2>"));
    }
}

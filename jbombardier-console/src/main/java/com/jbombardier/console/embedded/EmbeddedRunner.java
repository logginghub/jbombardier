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

package com.jbombardier.console.embedded;

import com.logginghub.utils.TimerUtils;
import com.jbombardier.agent.SimpleTestContextFactory;
import com.jbombardier.agent.ThreadController;
import com.jbombardier.common.AgentStats;
import com.jbombardier.common.AgentStats.TestStats;
import com.jbombardier.common.BasicTestStats;
import com.jbombardier.common.TestFactory;
import com.jbombardier.common.TestInstruction;
import com.jbombardier.JBombardierModel;

public class EmbeddedRunner {

    public void run(final String testName, TestFactory factory, final JBombardierModel model) {

        TestInstruction testInstruction = new TestInstruction();
        testInstruction.setTargetThreads(1);
        testInstruction.setTargetRate(1);
        testInstruction.setClassname("");
        testInstruction.setRateStep(1);
        testInstruction.setRateStepTime(1000);
        testInstruction.setRecordAllValues(true);
        testInstruction.setTestName(testName);
        testInstruction.setThreadRampupStep(1);
        testInstruction.setThreadRampupTime(1000);
        testInstruction.setTransactionRateModifier(1);

        SimpleTestContextFactory testContextFactory = new SimpleTestContextFactory();
        ThreadController threadController = new ThreadController(testName, factory, testContextFactory, testInstruction);

        final BasicTestStats basicTestStats = new BasicTestStats(testName);
        threadController.setStats(basicTestStats);

        final ExecutionAgent agent = new ExecutionAgent(threadController);
        
        TimerUtils.everySecond("Results Sweeper", new Runnable() {
            @Override public void run() {
                AgentStats agentStats = new AgentStats();
                TestStats testStats = new TestStats();
                testStats.fromBasicTestStats(basicTestStats);
                testStats.setTransactionName("");
                testStats.setTestName(testName);
                agentStats.addTestStats(testStats);
                model.onAgentStatusUpdate(agentStats);
                agent.onStatsUpdated(basicTestStats);
                basicTestStats.reset();
            }
        });

        threadController.start();
        
    }

}

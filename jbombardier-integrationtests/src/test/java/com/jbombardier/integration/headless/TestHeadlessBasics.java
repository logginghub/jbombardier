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

package com.jbombardier.integration.headless;

import com.jbombardier.agent.Agent2;
import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;
import com.jbombardier.console.sample.SleepTest;
import com.jbombardier.repository.JBombardierRepositoryLauncher;
import com.logginghub.utils.Out;
import com.jbombardier.integration.JBombardierTestBase;
import org.testng.annotations.Test;

/**
 * Created by james on 12/11/14.
 */
public class TestHeadlessBasics extends JBombardierTestBase {

    @Test(enabled = false)
    public void test_full_lifecycle() {

        Agent2 agent = dsl.createAgent("agent!");
        agent.start();

        JBombardierRepositoryLauncher repository = dsl.createRepository();
        Out.out("Repo server port {}", repository.getConfiguration().getServerPort());

        JBombardierConfigurationBuilder.configurationBuilder()
                                .addAgent("Agent1", "localhost", agent.getBindPort())
                                .addTest(JBombardierConfigurationBuilder.TestBuilder.start(SleepTest.class)
                                                                             .targetRate(1)
                                                                             .threads(1))
                                .warmUpTime("0 seconds")
                                .testDuration("5 seconds")
                                .resultRepository("localhost", repository.getConfiguration().getServerPort())
                                .executeHeadlessNoExit();



    }

}

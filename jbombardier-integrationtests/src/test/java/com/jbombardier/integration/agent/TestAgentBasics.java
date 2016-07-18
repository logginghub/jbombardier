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

package com.jbombardier.integration.agent;

import com.jbombardier.agent.Agent2;
import com.logginghub.utils.ThreadUtils;
import com.jbombardier.integration.JBombardierTestBase;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;

/**
 * Created by james on 12/11/14.
 */
public class TestAgentBasics extends JBombardierTestBase {

    @Test(enabled = false)
    public void test_start_stop() {

        Agent2 agent = dsl.createAgent("agent1");
        agent.start();
        agent.stop();

        // Workaround for Kryo hub not closing when it says it does
        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ThreadUtils.isThreadDead("Server");
            }
        });

    }

    @Test(enabled = false) public void test_start_test() {

        Agent2 agent = dsl.createAgent("agent1");
        agent.start();



    }

}

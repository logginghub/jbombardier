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

import org.junit.Ignore;

@Ignore
public class TestClassLoader { 
//extends AbstractAgentBase
//{
//    @Test public void test() throws IOException, InterruptedException
//    {
//        ProcessWrapper agent = launchAgent(true);
//
//        TestInstruction testInstruction = new TestInstruction();
//        testInstruction.setClassname("class.doesnt.exist");
//        testInstruction.setTargetThreads(1);
//        
//        ArrayList<TestInstruction> testInstructions = new ArrayList<TestInstruction>();        
//        testInstructions.add(testInstruction);
//        
//        AgentInstruction agentInstruction = new AgentInstruction();
//        agentInstruction.setHost("localhost");        
//        agentInstruction.setTestInstructions(testInstructions);
//        
//        Bucket<Object> bucket  = new Bucket<Object>();
//        Client connectClient = connectClient(bucket);
//        connectClient.sendTCP(agentInstruction);
//        
//        Thread.sleep(500);
//
//        // At the moment we get a debug string back first, we have have to refactor that
//        bucket.waitForMessages(1);
//        Object object = bucket.get(0);
//        assertThat(object, is(AgentClassRequest.class));
//        AgentClassRequest acr = (AgentClassRequest)object;
//        assertThat(acr.getClassName(), is("class.doesnt.exist"));
//        assertThat(acr.isClassNotResource(), is(true));
//        
//        connectClient.sendTCP(new AgentKillInstruction(0));
//        
//        agent.getProcess().waitFor();
//        assertThat(agent.getError(), is(""));
//        assertThat(agent.getProcess().exitValue(), is(0));
//    }
}

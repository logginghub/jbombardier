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
public class TestProperties { 
//
//extends AbstractAgentBase
//{
//    @Test public void test() throws IOException, InterruptedException
//    {
//        ProcessWrapper agent = launchAgent();
//
//        Bucket<Object> bucket  = new Bucket<Object>();
//
//        AgentInstruction createAgentInstruction = createAgentInstruction(PropertiesTest.class.getName());
//        createAgentInstruction.getTestInstructions().get(0).setTargetThreads(2);
//        
//        PerformanceHost host = new PerformanceHost();
//        HostConfiguration hostConfiguration = new HostConfiguration();
//        
//        hostConfiguration.getAgentProperties().add(new AgentProperty("propertyExists", "66"));
//        hostConfiguration.getAgentListProperties().add(new AgentListProperty("propertyUnique", "a,b,c,d,e"));
//        
//        hostConfiguration.setTimeout(10);
//        hostConfiguration.setTimeoutUnits("SECONDS");
//        List<AgentInstruction> agentInstructions = new ArrayList<AgentInstruction>();
//        agentInstructions.add(createAgentInstruction);
//        
//        hostConfiguration.setAgentInstructions(agentInstructions);
//        host.setConfiguration(hostConfiguration);
//        host.attachListener(new BucketListener<Object>(bucket));
//        host.run();
//        
//        List<AgentLogMessage> logMessages = bucket.extract(new BucketMatcher<Object>()
//        {
//            public boolean matches(Object t)
//            {
//                return t instanceof AgentLogMessage;
//            }
//        });
//        
//        assertThat(logMessages, is(notNullValue()));
//        assertThat(logMessages.size(), is(2));
//        
//        Set<String> logMessageStrings = new HashSet<String>();
//        for (AgentLogMessage agentLogMessage : logMessages)
//        {
//            logMessageStrings.add(agentLogMessage.getMessage());
//        }
//        
//        assertThat(logMessageStrings.contains("Property exists 66 property default -1 property unique a"), is(true));
//        assertThat(logMessageStrings.contains("Property exists 66 property default -1 property unique b"), is(true));
//    }
}

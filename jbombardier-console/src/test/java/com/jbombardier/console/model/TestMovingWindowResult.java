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

package com.jbombardier.console.model;

import org.junit.Test;

import com.jbombardier.common.AgentStats;
import com.jbombardier.common.AgentStats.TestStats;

public class TestMovingWindowResult {

    @Test public void test() {
        
        AgentStats a1 = new AgentStats();
        AgentStats a2 = new AgentStats();
        AgentStats a3 = new AgentStats();
        
        a1.setAgentName("agentA");
        a2.setAgentName("agentA");
        a3.setAgentName("agentA");
        
        TestStats a1ts1 = new TestStats();
        TestStats a1ts2 = new TestStats();
        
        TestStats a2ts1 = new TestStats();
        TestStats a2ts2 = new TestStats();
        
        TestStats a3ts1 = new TestStats();
        TestStats a3ts2 = new TestStats();
        
        a1.addTestStats(a1ts1);
        a1.addTestStats(a1ts2);
        
        a2.addTestStats(a2ts1);
        a2.addTestStats(a2ts2);
        
        a3.addTestStats(a3ts1);
        a3.addTestStats(a3ts2);
        
        a1ts1.transactionsSuccess = 1;
        a1ts2.transactionsSuccess = 2;
        a2ts1.transactionsSuccess = 3;
        a2ts2.transactionsSuccess = 4;
        a3ts1.transactionsSuccess = 5;
        a3ts2.transactionsSuccess = 6;
        
        a1ts1.totalDurationSuccess = 10;
        a1ts2.totalDurationSuccess = 20;
        a2ts1.totalDurationSuccess = 30;
        a2ts2.totalDurationSuccess = 40;
        a3ts1.totalDurationSuccess = 50;
        a3ts2.totalDurationSuccess = 60;
        

        MovingWindowResult result = new MovingWindowResult();
        result.setWindowLength(2000);
        
        result.addAgentStats(1, a1);
        result.addAgentStats(2, a2);
        result.addAgentStats(3, a3);
        
        
        
        
    }
    
}

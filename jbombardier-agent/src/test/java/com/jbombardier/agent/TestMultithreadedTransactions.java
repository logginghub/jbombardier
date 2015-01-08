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

package com.jbombardier.agent;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.jbombardier.common.TestInstruction;
import com.jbombardier.common.TestPackage;

public class TestMultithreadedTransactions {

    @Ignore /* this didn't find anything, and has no asserts */
    @Test public void test() throws InterruptedException {

        Agent2 agent2 = new Agent2();

        TestInstruction instruction=  new TestInstruction();
        instruction.setClassname(SlowTransactions.class.getName());
        instruction.setDuration(10000);
        instruction.setRateStep(100);
        instruction.setRecordAllValues(false);
        instruction.setTargetRate(1);
        instruction.setTargetThreads(100);
        instruction.setTestName("name");
        instruction.setThreadRampupStep(10);
        instruction.setThreadRampupTime(100);
        
        List<TestInstruction> instructions = new ArrayList<TestInstruction>();
        instructions.add(instruction);
        
        TestPackage testPackage = new TestPackage();        
        testPackage.setInstructions(instructions);
        agent2.handleTestPackage(testPackage);
        
        Thread.sleep(20000);

    }

}

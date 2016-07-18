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
import com.jbombardier.common.PhaseInstruction;
import com.jbombardier.common.TestInstruction;
import com.logginghub.utils.FactoryMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class TestPerTestProperties {

    @Test public void testPropertiesAreSentWithInstructions(){
        
        JBombardierSwingConsole swingConsole = new JBombardierSwingConsole();
        swingConsole.loadConfigurationFile("/com/jbombardier/perTestProperties.xml");
        swingConsole.initialise();
        
        JBombardierController controller = swingConsole.getController();

        Map<String, List<PhaseInstruction>> phaseInstructions = new FactoryMap<String, List<PhaseInstruction>>() {
            @Override protected List<PhaseInstruction> createEmptyValue(String s) {
                return new ArrayList<PhaseInstruction>();
            }
        };
        controller.populateInstructionsList(phaseInstructions);

        assertThat(phaseInstructions.size(), is(1));
        PhaseInstruction phaseInstruction = phaseInstructions.get("embedded").get(0);

        List<TestInstruction> testInstructionsList = phaseInstruction.getInstructions();

        assertThat(testInstructionsList.size(), is(3));
        
        TestInstruction testInstruction1 = testInstructionsList.get(0);
        TestInstruction testInstruction2 = testInstructionsList.get(1);
        TestInstruction testInstruction3 = testInstructionsList.get(2);
        
        assertThat(testInstruction1.getClassname(), is("com.jbombardier.console.sample.old.Test1"));
        assertThat(testInstruction2.getClassname(), is("com.jbombardier.console.sample.old.Test1"));
        assertThat(testInstruction3.getClassname(), is("com.jbombardier.console.sample.old.Test1"));
        
        assertThat(testInstruction1.getProperties().get("property"), is("firstInstance"));
        assertThat(testInstruction2.getProperties().get("property"), is("secondInstance"));
        assertThat(testInstruction3.getProperties().get("property"), is("default"));
    }
    
}

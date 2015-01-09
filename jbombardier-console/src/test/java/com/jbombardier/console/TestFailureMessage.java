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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.jbombardier.console.headless.JBombardierHeadless;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.logginghub.utils.TextFileBuilder;

public class TestFailureMessage {

 @Rule public ExpectedException thrown = ExpectedException.none();
    
    @Test public void testFailureMessagesReceived() throws IOException, InterruptedException {

        String message = "something went wrong";

        File tempFile = File.createTempFile("testEmptyConfiguration", ".xml");
        TextFileBuilder builder = new TextFileBuilder(new FileWriter(tempFile));        
        builder.appendLine("<jbombardierConfiguration duration='100' warmUp='100'>");
        builder.appendLine("<agents><agent name='embedded'/></agents>");
        builder.appendLine("<tests><test name='propertyReader' class='com.jbombardier.console.sample.FailingTest' targetRate='10' rateStep='100' properties='failureReason=" + message + "'/></tests>");
        builder.appendLine("</jbombardierConfiguration>");
        builder.close();
        
        JBombardierHeadless headless = new JBombardierHeadless();
        headless.setAgentsRequired(1);
        headless.setTimeToWaitForAgents(5000);
        JBombardierController controller = headless.run(tempFile.getAbsolutePath());
        
        JBombardierModel model = controller.getModel();
        // TODO : refactor to use the event console
//        assertThat(model.getEvents().size(), is(greaterThan(0)));
//        
//        List<ConsoleEventModel> events = model.getEvents();
//        assertThat(events.size(), is(greaterThan(2)));
//        for (int i = 1; i < events.size(); i++) {
//            ConsoleEventModel consoleEventModel = events.get(i);
//            assertThat(consoleEventModel.getMessage(), is(message));
//        }
        
    }
    
}

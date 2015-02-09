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

package com.jbombardier.console.configuration;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FileUtilsWriter;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class JBombardierConfigurationTest {

    @Test public void test_phases() {

        File file = FileUtils.createRandomTestFileForClass(JBombardierConfiguration.class);
        FileUtilsWriter writer = FileUtils.createWriter(file);

        writer.appendLine("<jbombardierConfiguration>");
        writer.appendLine("   <test name='Test1' class='test class'/>");
        writer.appendLine("   <phase name='Phase 1' duration='10 minutes'>");
        writer.appendLine("       <test name='Test1' class='test class'/>");
        writer.appendLine("   </phase>");
        writer.appendLine("</jbombardierConfiguration>");
        writer.close();

        JBombardierConfiguration configuration = JBombardierConfiguration.loadConfiguration(file.getAbsolutePath());
        assertThat(configuration.getPhases().size(), is(1));
        assertThat(configuration.getPhases().get(0).getPhaseName(), is("Phase 1"));
        assertThat(configuration.getPhases().get(0).getDuration(), is("10 minutes"));


    }


}
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.logginghub.utils.TextFileBuilder;

public class TestEmptyConfiguration {
    
    @Rule public ExpectedException thrown = ExpectedException.none();
    
    @Test public void test() throws IOException {

        File tempFile = File.createTempFile("testEmptyConfiguration", ".xml");
        TextFileBuilder builder = new TextFileBuilder(new FileWriter(tempFile));        
        builder.appendLine("<jbombardierConfiguration>");
        builder.appendLine("<agents><agent name='embedded'/></agents>");
        builder.appendLine("<tests><test name='propertyReader' class='com.jbombardier.console.sample.old.PropertyReader' targetRate='10' rateStep='100' properties='property=empty'/></tests>");
        builder.appendLine("<data><csvProperty name='empty' csvfile='/com/jbombardier/console/configuration/empty_data.csv' strategy='fixedThread'/></data>");
        builder.appendLine("</jbombardierConfiguration>");
        builder.close();
        
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("The csv data file for property 'empty' provided was '/com/jbombardier/console/configuration/empty_data.csv', but this file was empty.");

        JBombardierSwingConsole.runWithAutostart(tempFile.getAbsolutePath(), 1);
        
    }
}

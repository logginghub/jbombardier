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

package com.jbombardier.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;

import org.junit.Test;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Xml;
import com.jbombardier.console.model.JSONHelper;
import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.console.model.result.TestRunResultBuilder;

public class TestJBombardierRepositoryLauncher {

    private File folder = FileUtils.createRandomTestFolderForClass(TestJBombardierRepositoryLauncher.class);
    private JSONHelper helper = new JSONHelper();
    
    @Test public void test_posting_saves_file() {

        RepositoryConfiguration configuration = new RepositoryConfiguration();
        configuration.setDataFolder(folder.getAbsolutePath());

        int httpPort = NetUtils.findFreePort();
        int serverPort = NetUtils.findFreePort();

        configuration.setHttpPort(httpPort);
        configuration.setServerPort(serverPort);

        JBombardierRepositoryLauncher launcher = JBombardierRepositoryLauncher.runLauncher(configuration);
        launcher.start();

        RepositoryMessagingClient client = new RepositoryMessagingClient();
        client.connect("localhost", serverPort);

        RunResult runResult = TestRunResultBuilder.start()
                                                          .name("MyTest")
                                                          .results(TestRunResultBuilder.result()
                                                                                       .testName("Test1")
                                                                                       .testTime(1000)
                                                                                       .transactionCount(100)
                                                                                       .successDuration(50)
                                                                                       .successTotalDuration(60)
                                                                                       .transactionsSuccess(100))
                                                          .toTestRunResult();

        String json = helper.toJSON(runResult);

        client.postResult(json);

        System.out.println(folder.getAbsolutePath());
        assertThat(folder.list().length, is(1));

        launcher.stop();
    }
    
    @Test public void test_posted_file_still_there_after_restart() {


        RepositoryConfiguration configuration = new RepositoryConfiguration();
        configuration.setDataFolder(folder.getAbsolutePath());

        int httpPort = NetUtils.findFreePort();
        int serverPort = NetUtils.findFreePort();

        configuration.setHttpPort(httpPort);
        configuration.setServerPort(serverPort);

        JBombardierRepositoryLauncher launcher = JBombardierRepositoryLauncher.runLauncher(configuration);
        launcher.start();

        RepositoryMessagingClient client = new RepositoryMessagingClient();
        client.connect("localhost", serverPort);

        RunResult runResult = TestRunResultBuilder.start()
                                                          .name("MyTest")
                                                          .results(TestRunResultBuilder.result()
                                                                                       .testName("Test1")
                                                                                       .testTime(1000)
                                                                                       .transactionCount(100)
                                                                                       .successDuration(50)
                                                                                       .successTotalDuration(60)
                                                                                       .transactionsSuccess(100))
                                                          .toTestRunResult();

        String json = helper.toJSON(runResult);

        client.postResult(json);

        assertThat(folder.list().length, is(1));

        String index = FileUtils.get("http://localhost:{}/", httpPort);
        System.out.println(index);
//        Logger.setLevel(Xml.class, Logger.trace);
        Xml xml = new Xml(index);
        
        assertThat(xml.nodePath("html.body.div.table.tbody").find("tr").size(), is(1));
        assertThat(xml.nodePath("html.body.div.table.tbody.tr.td[0].a").getElementData(), is("MyTest"));
        assertThat(xml.nodePath("html.body.div.table.tbody.tr.td[1]").getElementData(), is("1"));
        
        client.close();
        launcher.stop();
        
        // Open it again, and make sure the file is still there
        launcher = JBombardierRepositoryLauncher.runLauncher(configuration);
        launcher.start();
        launcher.getModelLoader().join();
        
        index = FileUtils.get("http://localhost:{}/", httpPort);
        xml = new Xml(index);
        
        assertThat(xml.nodePath("html.body.div.table.tbody").find("tr").size(), is(1));
        assertThat(xml.nodePath("html.body.div.table.tbody.tr.td[0].a").getElementData(), is("MyTest"));
        assertThat(xml.nodePath("html.body.div.table.tbody.tr.td[1]").getElementData(), is("1"));
    }
}

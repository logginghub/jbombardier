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

package com.jbombardier.integration.repository;

import com.logginghub.messaging.Level3AsyncServer;
import com.jbombardier.common.RepositoryInterface;
import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.RepositoryMessagingClient;
import com.jbombardier.JBombardierController;
import com.jbombardier.JBombardierModel;
import com.jbombardier.console.configuration.JBombardierConfiguration;
import com.jbombardier.console.model.TransactionResultModel;
import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.repository.RepositoryWebView;
import com.jbombardier.repository.JBombardierRepositoryLauncher;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.ListBackedMap;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.ThreadUtils;
import com.jbombardier.integration.JBombardierTestBase;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 12/11/14.
 */
public class TestRepositoryBasics extends JBombardierTestBase {

//    @Test(threadPoolSize = 5, invocationCount = 20)
    @Test public void test_messaging_server_start_stop() {
        Level3AsyncServer server = new Level3AsyncServer("RepositoryMessaging3Server");
        server.setPort(NetUtils.findFreePort());
        server.start();
        server.stop();
    }

//    @Test(threadPoolSize = 5, invocationCount = 20)
    @Test public void test_messaging_client_start_stop() {

        Level3AsyncServer server = new Level3AsyncServer("RepositoryMessaging3Server");
        server.setPort(NetUtils.findFreePort());

        server.register("repository", RepositoryInterface.class, new RepositoryInterface() {
            @Override public void postResult(String jsonResult) {

            }
        });

        server.start();

        RepositoryMessagingClient client = new RepositoryMessagingClient();
        client.connect("localhost", server.getPort());
        client.stop();

        server.stop();
    }

    // @Test(threadPoolSize = 5, invocationCount = 20)
    @Test public void test_jetty_start_stop() {
        RepositoryWebView view = new RepositoryWebView();
        view.setHttpPort(NetUtils.findFreePort());
        view.start();
        view.stop();
    }

    @Test(enabled = false, threadPoolSize = 5, invocationCount = 20) public void test_start_stop() {

        JBombardierRepositoryLauncher repository = dsl.createRepository();
        repository.start();
        repository.stop();

    }

    @Test(enabled = false, threadPoolSize = 10, invocationCount = 100) public void test_send_data() {

        JBombardierRepositoryLauncher repository = dsl.createRepository();
        repository.start();

        JBombardierModel model = new JBombardierModel();
        JBombardierConfiguration configuration = new JBombardierConfiguration();
        JBombardierController controller = new JBombardierController(model, configuration);

        configuration.setResultRepositoryHost("localhost");
        configuration.setResultRepositoryPort(repository.getConfiguration().getServerPort());

        Map<String, TransactionResultModel> map = new HashMap<String, TransactionResultModel>();
        List<TransactionResultModel> list = new ArrayList<TransactionResultModel>();

        ListBackedMap<String, TransactionResultModel> resultsStructure = new ListBackedMap<String, TransactionResultModel>(
                list,
                map);

        List<CapturedStatistic> capturedStatistics = new ArrayList<CapturedStatistic>();
        capturedStatistics.add(new CapturedStatistic(1000, "path/branch1/a", "1"));
        capturedStatistics.add(new CapturedStatistic(1000, "path/branch1/b", "2"));
        capturedStatistics.add(new CapturedStatistic(2000, "path/branch1/a", "3"));
        capturedStatistics.add(new CapturedStatistic(2000, "path/branch1/b", "4"));
        capturedStatistics.add(new CapturedStatistic(3000, "path/branch2", "5"));
        capturedStatistics.add(new CapturedStatistic(4000, "path/branch2", "6"));

        controller.outputJSONResults(resultsStructure, capturedStatistics);

        repository.stop();
    }

    @Test(enabled = false) public void test_captured_data() {

        JBombardierRepositoryLauncher repository = dsl.createRepository();
        repository.getConfiguration().setHttpPort(8089);
        repository.start();

        JBombardierModel model = new JBombardierModel();

        JBombardierConfiguration configuration = new JBombardierConfiguration();
        configuration.setResultRepositoryHost("localhost");
        configuration.setResultRepositoryPort(repository.getConfiguration().getServerPort());
        configuration.setReportsFolder(FileUtils.createRandomTestFolderForClass(this.getClass()).getAbsolutePath());

        JBombardierController controller = new JBombardierController(model, configuration);

        sendFirstResult(controller, model);
        sendSecondResult(controller, model);

        ThreadUtils.sleep(1000000);

        repository.stop();
    }

    private void sendFirstResult(JBombardierController controller, JBombardierModel model) {
        Map<String, TransactionResultModel> map = new HashMap<String, TransactionResultModel>();

        TransactionResultModel test1Results = new TransactionResultModel();//"Test1", "", false, 10, 1, 1, 1, 1, 1, 1, 1);
        TransactionResultModel test2Results = new TransactionResultModel(); //"Test2", "", false, 10, 1, 1, 1, 1, 1, 1, 1);

//        test1Results.update(1, 2, 3, 4, 5, 6, 7, 8, 9, 1000);
//        test2Results.update(1, 2, 3, 4, 5, 6, 7, 8, 9, 1000);

        map.put("Test1", test1Results);
        map.put("Test2", test2Results);

        List<CapturedStatistic> capturedStatistics = new ArrayList<CapturedStatistic>();
        capturedStatistics.add(new CapturedStatistic(1000, "path/branch1/a", "1"));
        capturedStatistics.add(new CapturedStatistic(1000, "path/branch1/b", "2"));
        capturedStatistics.add(new CapturedStatistic(2000, "path/branch1/a", "3"));
        capturedStatistics.add(new CapturedStatistic(2000, "path/branch1/b", "4"));
        capturedStatistics.add(new CapturedStatistic(3000, "path/branch2", "5"));
        capturedStatistics.add(new CapturedStatistic(4000, "path/branch2", "6"));

        RunResult result = new RunResult();
        result.setConfigurationName("TestRun1");
        result.setStartTime(1000);
        // TODO : refactor fix me
//        result.setTestResultsFromModel(map);
        result.setFailureReason(model.getFailureReason());
        // TODO : refactor fix me
//        result.setCapturedStatistics(capturedStatistics);

        controller.outputJSONResults(result);
    }

    private void sendSecondResult(JBombardierController controller, JBombardierModel model) {
        Map<String, TransactionResultModel> map = new HashMap<String, TransactionResultModel>();

        TransactionResultModel test1Results = new TransactionResultModel();//"Test1", "", false, 10, 1, 1, 1, 1, 1, 1, 1);
        TransactionResultModel test2Results = new TransactionResultModel();//"Test2", "", false, 10, 1, 1, 1, 1, 1, 1, 1);

//        test1Results.update(1, 2, 3, 4, 5, 6, 7, 8, 9, 1000);
//        test2Results.update(1, 2, 3, 4, 5, 6, 7, 8, 9, 1000);

        map.put("Test1", test1Results);
        map.put("Test2", test2Results);

        List<CapturedStatistic> capturedStatistics = new ArrayList<CapturedStatistic>();
        capturedStatistics.add(new CapturedStatistic(1000, "path/branch1/a", "1"));
        capturedStatistics.add(new CapturedStatistic(1000, "path/branch1/b", "2"));
        capturedStatistics.add(new CapturedStatistic(2000, "path/branch1/a", "3"));
        capturedStatistics.add(new CapturedStatistic(2000, "path/branch1/b", "4"));
        capturedStatistics.add(new CapturedStatistic(3000, "path/branch2", "5"));
        capturedStatistics.add(new CapturedStatistic(4000, "path/branch2", "6"));

        RunResult result = new RunResult();
        result.setConfigurationName("TestRun1");
        result.setStartTime(11000);
        // TODO : refactor fix me
//        result.setTestResultsFromModel(map);
        result.setFailureReason(model.getFailureReason());
        // TODO : refactor fix me
//        result.setCapturedStatistics(capturedStatistics);

        controller.outputJSONResults(result);
    }

}

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

import com.jbombardier.common.AgentStats;
import com.jbombardier.common.DataBucket;
import com.jbombardier.common.DataStrategy;
import com.jbombardier.console.configuration.JBombardierConfiguration;
import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;
import com.jbombardier.console.model.AgentModel;
import com.jbombardier.console.sample.SleepTest;
import com.jbombardier.xml.CsvProperty;
import com.logginghub.messaging2.kryo.KryoClient;
import com.logginghub.messaging2.kryo.ResponseHandler;
import com.logginghub.utils.CollectionUtils;
import com.logginghub.utils.FactoryMap;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.configurationBuilder;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SuppressWarnings("unchecked") public class TestJBombardierControllerTest {


    @Test public void test_controller_lifecycle() {
        JBombardierModel model = new JBombardierModel();
        JBombardierConfiguration configuration = new JBombardierConfiguration();

        JBombardierController controller = new JBombardierController(model, configuration);

        assertThat(controller.getState(), is(JBombardierController.State.Configured));

        controller.startAgentConnections();

        assertThat(controller.getState(), is(JBombardierController.State.AgentConnectionsRunning));

        controller.startWarmUp();

        assertThat(controller.getState(), is(JBombardierController.State.Warmup));

        controller.endWarmup();
        controller.startMainTest();

        assertThat(controller.getState(), is(JBombardierController.State.TestRunning));

        controller.endTestNormally();

        assertThat(controller.getState(), is(JBombardierController.State.Completed));
    }

    @Test public void test_controller_lifecycle_with_phases() {
        JBombardierModel model = new JBombardierModel();
        JBombardierConfiguration configuration =
                configurationBuilder().addPhase(configurationBuilder().phase("Phase 1")
                                                                      .duration("1 second")
                                                                      .addTest(JBombardierConfigurationBuilder.TestBuilder
                                                                                       .start(SleepTest.class)))
                                      .addPhase(configurationBuilder().phase("Phase 2")
                                                                      .duration("1 second")
                                                                      .addTest(JBombardierConfigurationBuilder.TestBuilder
                                                                                       .start(SleepTest.class)))
                                      .toConfiguration();

        JBombardierController controller = new JBombardierController(model, configuration);

        assertThat(controller.getState(), is(JBombardierController.State.Configured));

        try {
            controller.handleAgentStatusUpdate(AgentStats.agentStats()
                                                         .agentName("agent1")
                                                         .testStats(AgentStats.testStats("test1", 1, 2, 3))
                                                         .toStats());
            fail("The test isn't running, we can't process results");
        }catch(IllegalStateException e){
            assertThat(e.getMessage(), is("Unable to handle agent stats whilst the test isn't running - looks like something has gone wrong"));
        }

        controller.startAgentConnections();

        assertThat(controller.getState(), is(JBombardierController.State.AgentConnectionsRunning));

        controller.startWarmUp();

        assertThat(controller.getState(), is(JBombardierController.State.Warmup));

        controller.endWarmup();
        controller.startMainTest();

        assertThat(controller.getState(), is(JBombardierController.State.TestRunning));
        assertThat(controller.getCurrentPhaseName(), is("Phase 1"));

        controller.phaseComplete();

        assertThat(controller.getState(), is(JBombardierController.State.TestRunning));
        assertThat(controller.getCurrentPhaseName(), is("Phase 2"));

        controller.phaseComplete();

        assertThat(controller.getState(), is(JBombardierController.State.Completed));
    }

    @Test public void test_divideDataIntoBuckets_less_entries() {

        JBombardierModel model = new JBombardierModel();
        JBombardierConfiguration configuration = new JBombardierConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_less.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1",
                                                                                    sampleData,
                                                                                    DataStrategy.pooledAgent.toString())));

        JBombardierController controller = new JBombardierController(model, configuration);

        AgentModel a = new AgentModel();
        AgentModel b = new AgentModel();
        AgentModel c = new AgentModel();

        a.setName("agentA");
        b.setName("agentB");
        c.setName("agentC");

        a.setConnected(true);
        b.setConnected(true);
        c.setConnected(true);

        List<AgentModel> agents = CollectionUtils.newArrayList(a, b, c);

        FactoryMap<String, FactoryMap<String, DataBucket>> bucketsByAgentBySource = controller.divideDataIntoBuckets(
                agents);

        assertThat(bucketsByAgentBySource.size(), is(3));

        assertThat(bucketsByAgentBySource.keySet().contains("agentA"), is(true));
        assertThat(bucketsByAgentBySource.keySet().contains("agentB"), is(true));
        assertThat(bucketsByAgentBySource.keySet().contains("agentC"), is(true));

        FactoryMap<String, DataBucket> agentABucket = bucketsByAgentBySource.get("agentA");
        FactoryMap<String, DataBucket> agentBBucket = bucketsByAgentBySource.get("agentB");
        FactoryMap<String, DataBucket> agentCBucket = bucketsByAgentBySource.get("agentC");

        assertThat(agentABucket.size(), is(1));
        assertThat(agentBBucket.size(), is(1));
        assertThat(agentCBucket.size(), is(1));

        assertThat(agentABucket.keySet().contains("data1"), is(true));
        assertThat(agentBBucket.keySet().contains("data1"), is(true));
        assertThat(agentBBucket.keySet().contains("data1"), is(true));

        assertThat(agentABucket.get("data1").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentBBucket.get("data1").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentCBucket.get("data1").getColumns(), is(new String[]{"string", "value"}));

        assertThat(agentABucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentBBucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentCBucket.get("data1").getDataSourceName(), is("data1"));

        assertThat(agentABucket.get("data1").getValues().toArray(new String[0][0]),
                   is(new String[][]{{"one", "1"}, {"four", "4"}}));
        assertThat(agentBBucket.get("data1").getValues().toArray(new String[0][0]),
                   is(new String[][]{{"two", "2"}, {"five", "5"}}));
        assertThat(agentCBucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][]{{"three", "3"}}));
    }

    @Test public void test_divideDataIntoBuckets_more_entries() {

        JBombardierModel model = new JBombardierModel();
        JBombardierConfiguration configuration = new JBombardierConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_more.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1",
                                                                                    sampleData,
                                                                                    DataStrategy.pooledAgent.toString())));

        JBombardierController controller = new JBombardierController(model, configuration);

        AgentModel a = new AgentModel();
        AgentModel b = new AgentModel();
        AgentModel c = new AgentModel();

        a.setName("agentA");
        b.setName("agentB");
        c.setName("agentC");

        a.setConnected(true);
        b.setConnected(true);
        c.setConnected(true);

        List<AgentModel> agents = CollectionUtils.newArrayList(a, b, c);

        FactoryMap<String, FactoryMap<String, DataBucket>> bucketsByAgentBySource = controller.divideDataIntoBuckets(
                agents);

        assertThat(bucketsByAgentBySource.size(), is(3));

        assertThat(bucketsByAgentBySource.keySet().contains("agentA"), is(true));
        assertThat(bucketsByAgentBySource.keySet().contains("agentB"), is(true));
        assertThat(bucketsByAgentBySource.keySet().contains("agentC"), is(true));

        FactoryMap<String, DataBucket> agentABucket = bucketsByAgentBySource.get("agentA");
        FactoryMap<String, DataBucket> agentBBucket = bucketsByAgentBySource.get("agentB");
        FactoryMap<String, DataBucket> agentCBucket = bucketsByAgentBySource.get("agentC");

        assertThat(agentABucket.size(), is(1));
        assertThat(agentBBucket.size(), is(1));
        assertThat(agentCBucket.size(), is(1));

        assertThat(agentABucket.keySet().contains("data1"), is(true));
        assertThat(agentBBucket.keySet().contains("data1"), is(true));
        assertThat(agentBBucket.keySet().contains("data1"), is(true));

        assertThat(agentABucket.get("data1").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentBBucket.get("data1").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentCBucket.get("data1").getColumns(), is(new String[]{"string", "value"}));

        assertThat(agentABucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentBBucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentCBucket.get("data1").getDataSourceName(), is("data1"));

        assertThat(agentABucket.get("data1").getValues().toArray(new String[0][0]),
                   is(new String[][]{{"one", "1"}, {"four", "4"}, {"seven", "7"}, {"ten", "10"}}));
        assertThat(agentBBucket.get("data1").getValues().toArray(new String[0][0]),
                   is(new String[][]{{"two", "2"}, {"five", "5"}, {"eight", "8"}, {"eleven", "11"}}));
        assertThat(agentCBucket.get("data1").getValues().toArray(new String[0][0]),
                   is(new String[][]{{"three", "3"}, {"six", "6"}, {"nine", "9"}}));
    }

    @Test public void test_divideDataIntoBuckets_too_few_entries() {

        JBombardierModel model = new JBombardierModel();
        JBombardierConfiguration configuration = new JBombardierConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_too_few.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1",
                                                                                    sampleData,
                                                                                    DataStrategy.pooledAgent.toString())));

        JBombardierController controller = new JBombardierController(model, configuration);

        AgentModel a = new AgentModel();
        AgentModel b = new AgentModel();
        AgentModel c = new AgentModel();

        a.setName("agentA");
        b.setName("agentB");
        c.setName("agentC");

        a.setConnected(true);
        b.setConnected(true);
        c.setConnected(true);

        List<AgentModel> agents = CollectionUtils.newArrayList(a, b, c);

        FactoryMap<String, FactoryMap<String, DataBucket>> bucketsByAgentBySource = controller.divideDataIntoBuckets(
                agents);

        assertThat(bucketsByAgentBySource.size(), is(3));

        assertThat(bucketsByAgentBySource.keySet().contains("agentA"), is(true));
        assertThat(bucketsByAgentBySource.keySet().contains("agentB"), is(true));
        assertThat(bucketsByAgentBySource.keySet().contains("agentC"), is(true));

        FactoryMap<String, DataBucket> agentABucket = bucketsByAgentBySource.get("agentA");
        FactoryMap<String, DataBucket> agentBBucket = bucketsByAgentBySource.get("agentB");
        FactoryMap<String, DataBucket> agentCBucket = bucketsByAgentBySource.get("agentC");

        assertThat(agentABucket.size(), is(1));
        assertThat(agentBBucket.size(), is(1));
        assertThat(agentCBucket.size(), is(1));

        assertThat(agentABucket.keySet().contains("data1"), is(true));
        assertThat(agentBBucket.keySet().contains("data1"), is(true));
        assertThat(agentBBucket.keySet().contains("data1"), is(true));

        assertThat(agentABucket.get("data1").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentBBucket.get("data1").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentCBucket.get("data1").getColumns(), is(new String[]{"string", "value"}));

        assertThat(agentABucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentBBucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentCBucket.get("data1").getDataSourceName(), is("data1"));

        assertThat(agentABucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][]{{"one", "1"}}));
        assertThat(agentBBucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][]{{"two", "2"}}));
        assertThat(agentCBucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][]{{"one", "1"}}));
    }

    @Test public void test_divideDataIntoBuckets_just_fixed_thread() {

        JBombardierModel model = new JBombardierModel();
        JBombardierConfiguration configuration = new JBombardierConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_exact.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1",
                                                                                    sampleData,
                                                                                    DataStrategy.fixedThread.toString())));

        JBombardierController controller = new JBombardierController(model, configuration);

        AgentModel a = new AgentModel();
        AgentModel b = new AgentModel();
        AgentModel c = new AgentModel();

        a.setName("agentA");
        b.setName("agentB");
        c.setName("agentC");

        a.setConnected(true);
        b.setConnected(true);
        c.setConnected(true);

        List<AgentModel> agents = CollectionUtils.newArrayList(a, b, c);

        FactoryMap<String, FactoryMap<String, DataBucket>> bucketsByAgentBySource = controller.divideDataIntoBuckets(
                agents);

        assertThat(bucketsByAgentBySource.size(), is(3));

        assertThat(bucketsByAgentBySource.keySet().contains("agentA"), is(true));
        assertThat(bucketsByAgentBySource.keySet().contains("agentB"), is(true));
        assertThat(bucketsByAgentBySource.keySet().contains("agentC"), is(true));

        FactoryMap<String, DataBucket> agentABucket = bucketsByAgentBySource.get("agentA");
        FactoryMap<String, DataBucket> agentBBucket = bucketsByAgentBySource.get("agentB");
        FactoryMap<String, DataBucket> agentCBucket = bucketsByAgentBySource.get("agentC");

        assertThat(agentABucket.size(), is(1));
        assertThat(agentBBucket.size(), is(1));
        assertThat(agentCBucket.size(), is(1));

        assertThat(agentCBucket.keySet().contains("data1"), is(true));
        assertThat(agentCBucket.keySet().contains("data1"), is(true));
        assertThat(agentCBucket.keySet().contains("data1"), is(true));

        assertThat(agentABucket.get("data1").getStrategy(), is(DataStrategy.fixedThread));
        assertThat(agentBBucket.get("data1").getStrategy(), is(DataStrategy.fixedThread));
        assertThat(agentCBucket.get("data1").getStrategy(), is(DataStrategy.fixedThread));

        assertThat(agentABucket.get("data1").getColumns(), is(nullValue()));
        assertThat(agentBBucket.get("data1").getColumns(), is(nullValue()));
        assertThat(agentCBucket.get("data1").getColumns(), is(nullValue()));

        assertThat(agentABucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentBBucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentCBucket.get("data1").getDataSourceName(), is("data1"));

        assertThat(agentABucket.get("data1").getValues(), is(nullValue()));
        assertThat(agentBBucket.get("data1").getValues(), is(nullValue()));
        assertThat(agentCBucket.get("data1").getValues(), is(nullValue()));
    }

    @Test public void test_divideDataIntoBuckets_just_fixed_thread_with_mock_clients() {

        JBombardierModel model = new JBombardierModel();
        JBombardierConfiguration configuration = new JBombardierConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_exact.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1",
                                                                                    sampleData,
                                                                                    DataStrategy.fixedThread.toString())));

        JBombardierController controller = new JBombardierController(model, configuration);

        AgentModel a = new AgentModel();
        AgentModel b = new AgentModel();
        AgentModel c = new AgentModel();

        a.setName("agentA");
        b.setName("agentB");
        c.setName("agentC");

        a.setConnected(true);
        b.setConnected(true);
        c.setConnected(true);

        KryoClient aKryoMock = Mockito.mock(KryoClient.class);
        KryoClient bKryoMock = Mockito.mock(KryoClient.class);
        KryoClient cKryoMock = Mockito.mock(KryoClient.class);

        // Setup the mocks to reply to the message
        Answer answer = new Answer() {
            public Object answer(InvocationOnMock invocation) {
                ResponseHandler handler = (ResponseHandler) invocation.getArguments()[2];
                handler.onResponse("Success");
                return null;
            }
        };

        Mockito.doAnswer(answer)
               .when(aKryoMock)
               .sendRequest(Mockito.anyString(), Mockito.anyObject(), Mockito.any(ResponseHandler.class));
        Mockito.doAnswer(answer)
               .when(bKryoMock)
               .sendRequest(Mockito.anyString(), Mockito.anyObject(), Mockito.any(ResponseHandler.class));
        Mockito.doAnswer(answer)
               .when(cKryoMock)
               .sendRequest(Mockito.anyString(), Mockito.anyObject(), Mockito.any(ResponseHandler.class));

        a.setKryoClient(aKryoMock);
        b.setKryoClient(bKryoMock);
        c.setKryoClient(cKryoMock);

        List<AgentModel> agents = CollectionUtils.newArrayList(a, b, c);

        model.getAgentModels().addAll(agents);

        controller.publishTestInstructionsAndStartRunning();

        Mockito.verify(aKryoMock, Mockito.times(2))
               .sendRequest(Mockito.anyString(), Mockito.anyObject(), Mockito.any(ResponseHandler.class));
        Mockito.verify(bKryoMock, Mockito.times(2))
               .sendRequest(Mockito.anyString(), Mockito.anyObject(), Mockito.any(ResponseHandler.class));
        Mockito.verify(cKryoMock, Mockito.times(2))
               .sendRequest(Mockito.anyString(), Mockito.anyObject(), Mockito.any(ResponseHandler.class));


    }


    @Test public void test_divideDataIntoBuckets_multiple_buckets() {

        JBombardierModel model = new JBombardierModel();
        JBombardierConfiguration configuration = new JBombardierConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_exact.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1",
                                                                                    sampleData,
                                                                                    DataStrategy.fixedThread.toString()),
                                                                    new CsvProperty("data2",
                                                                                    sampleData,
                                                                                    DataStrategy.pooledAgent.toString()),
                                                                    new CsvProperty("data3",
                                                                                    sampleData,
                                                                                    DataStrategy.pooledGlobal.toString()),
                                                                    new CsvProperty("data4",
                                                                                    sampleData,
                                                                                    DataStrategy.pooledThread.toString())));

        JBombardierController controller = new JBombardierController(model, configuration);

        AgentModel a = new AgentModel();
        AgentModel b = new AgentModel();
        AgentModel c = new AgentModel();

        a.setName("agentA");
        b.setName("agentB");
        c.setName("agentC");

        a.setConnected(true);
        b.setConnected(true);
        c.setConnected(true);

        List<AgentModel> agents = CollectionUtils.newArrayList(a, b, c);

        FactoryMap<String, FactoryMap<String, DataBucket>> bucketsByAgentBySource = controller.divideDataIntoBuckets(
                agents);

        System.out.println(bucketsByAgentBySource);

        assertThat(bucketsByAgentBySource.size(), is(3));

        assertThat(bucketsByAgentBySource.keySet().contains("agentA"), is(true));
        assertThat(bucketsByAgentBySource.keySet().contains("agentB"), is(true));
        assertThat(bucketsByAgentBySource.keySet().contains("agentC"), is(true));

        FactoryMap<String, DataBucket> agentABucket = bucketsByAgentBySource.get("agentA");
        FactoryMap<String, DataBucket> agentBBucket = bucketsByAgentBySource.get("agentB");
        FactoryMap<String, DataBucket> agentCBucket = bucketsByAgentBySource.get("agentC");

        // This is 3 - not 4 - as the first strategy doesn't result in a bucket
        assertThat(agentABucket.size(), is(4));
        assertThat(agentBBucket.size(), is(4));
        assertThat(agentCBucket.size(), is(4));

        assertThat(agentCBucket.keySet().contains("data1"), is(true));
        assertThat(agentCBucket.keySet().contains("data1"), is(true));
        assertThat(agentCBucket.keySet().contains("data1"), is(true));


        assertThat(agentABucket.keySet().contains("data2"), is(true));
        assertThat(agentABucket.keySet().contains("data3"), is(true));
        assertThat(agentABucket.keySet().contains("data4"), is(true));

        assertThat(agentBBucket.keySet().contains("data2"), is(true));
        assertThat(agentBBucket.keySet().contains("data3"), is(true));
        assertThat(agentBBucket.keySet().contains("data4"), is(true));

        assertThat(agentCBucket.keySet().contains("data2"), is(true));
        assertThat(agentCBucket.keySet().contains("data3"), is(true));
        assertThat(agentCBucket.keySet().contains("data4"), is(true));

        assertThat(agentABucket.get("data1").getStrategy(), is(DataStrategy.fixedThread));
        assertThat(agentBBucket.get("data1").getStrategy(), is(DataStrategy.fixedThread));
        assertThat(agentCBucket.get("data1").getStrategy(), is(DataStrategy.fixedThread));

        assertThat(agentABucket.get("data2").getStrategy(), is(DataStrategy.pooledAgent));
        assertThat(agentBBucket.get("data2").getStrategy(), is(DataStrategy.pooledAgent));
        assertThat(agentCBucket.get("data2").getStrategy(), is(DataStrategy.pooledAgent));

        assertThat(agentABucket.get("data3").getStrategy(), is(DataStrategy.pooledGlobal));
        assertThat(agentBBucket.get("data3").getStrategy(), is(DataStrategy.pooledGlobal));
        assertThat(agentCBucket.get("data3").getStrategy(), is(DataStrategy.pooledGlobal));

        assertThat(agentABucket.get("data4").getStrategy(), is(DataStrategy.pooledThread));
        assertThat(agentBBucket.get("data4").getStrategy(), is(DataStrategy.pooledThread));
        assertThat(agentCBucket.get("data4").getStrategy(), is(DataStrategy.pooledThread));

        assertThat(agentABucket.get("data1").getColumns(), is(nullValue()));
        assertThat(agentBBucket.get("data1").getColumns(), is(nullValue()));
        assertThat(agentCBucket.get("data1").getColumns(), is(nullValue()));

        assertThat(agentABucket.get("data2").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentBBucket.get("data2").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentCBucket.get("data2").getColumns(), is(new String[]{"string", "value"}));

        assertThat(agentABucket.get("data3").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentBBucket.get("data3").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentCBucket.get("data3").getColumns(), is(new String[]{"string", "value"}));

        assertThat(agentABucket.get("data4").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentBBucket.get("data4").getColumns(), is(new String[]{"string", "value"}));
        assertThat(agentCBucket.get("data4").getColumns(), is(new String[]{"string", "value"}));

        assertThat(agentABucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentBBucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentCBucket.get("data1").getDataSourceName(), is("data1"));

        assertThat(agentABucket.get("data2").getDataSourceName(), is("data2"));
        assertThat(agentBBucket.get("data2").getDataSourceName(), is("data2"));
        assertThat(agentCBucket.get("data2").getDataSourceName(), is("data2"));

        assertThat(agentABucket.get("data3").getDataSourceName(), is("data3"));
        assertThat(agentBBucket.get("data3").getDataSourceName(), is("data3"));
        assertThat(agentCBucket.get("data3").getDataSourceName(), is("data3"));

        assertThat(agentABucket.get("data4").getDataSourceName(), is("data4"));
        assertThat(agentBBucket.get("data4").getDataSourceName(), is("data4"));
        assertThat(agentCBucket.get("data4").getDataSourceName(), is("data4"));

        assertThat(agentABucket.get("data1").getValues(), is(nullValue()));
        assertThat(agentBBucket.get("data1").getValues(), is(nullValue()));
        assertThat(agentCBucket.get("data1").getValues(), is(nullValue()));

        assertThat(agentABucket.get("data2").getValues().toArray(new String[0][0]),
                   is(new String[][]{{"one", "1"}, {"four", "4"}, {"seven", "7"}}));
        assertThat(agentBBucket.get("data2").getValues().toArray(new String[0][0]),
                   is(new String[][]{{"two", "2"}, {"five", "5"}, {"eight", "8"}}));
        assertThat(agentCBucket.get("data2").getValues().toArray(new String[0][0]),
                   is(new String[][]{{"three", "3"}, {"six", "6"}, {"nine", "9"}}));

        String[][] allEntries = new String[][]{{"one", "1"}, {"two", "2"}, {"three", "3"}, {"four", "4"}, {"five", "5"}, {"six", "6"}, {"seven", "7"}, {"eight", "8"}, {"nine", "9"}};
        assertThat(agentABucket.get("data3").getValues().toArray(new String[0][0]), is(allEntries));
        assertThat(agentBBucket.get("data3").getValues().toArray(new String[0][0]), is(allEntries));
        assertThat(agentCBucket.get("data3").getValues().toArray(new String[0][0]), is(allEntries));

        assertThat(agentABucket.get("data4").getValues().toArray(new String[0][0]),
                   is(new String[][]{{"one", "1"}, {"four", "4"}, {"seven", "7"}}));
        assertThat(agentBBucket.get("data4").getValues().toArray(new String[0][0]),
                   is(new String[][]{{"two", "2"}, {"five", "5"}, {"eight", "8"}}));
        assertThat(agentCBucket.get("data4").getValues().toArray(new String[0][0]),
                   is(new String[][]{{"three", "3"}, {"six", "6"}, {"nine", "9"}}));
    }
}

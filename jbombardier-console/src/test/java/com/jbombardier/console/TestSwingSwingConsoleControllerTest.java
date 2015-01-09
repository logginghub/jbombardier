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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import com.jbombardier.console.model.AgentModel;
import org.junit.Test;
import org.mockito.Mockito;

import com.logginghub.messaging2.kryo.KryoClient;
import com.logginghub.messaging2.kryo.ResponseHandler;
import com.logginghub.utils.CollectionUtils;
import com.logginghub.utils.FactoryMap;
import com.jbombardier.common.DataBucket;
import com.jbombardier.common.DataStrategy;
import com.jbombardier.console.configuration.InteractiveConfiguration;
import com.jbombardier.xml.CsvProperty;

@SuppressWarnings("unchecked")
public class TestSwingSwingConsoleControllerTest {


    @Test
    public void test_divideDataIntoBuckets_less_entries() {
    
        ConsoleModel model = new ConsoleModel();
        InteractiveConfiguration configuration = new InteractiveConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_less.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1", sampleData, DataStrategy.pooledAgent.toString()) ));

        SwingConsoleController controller = new SwingConsoleController();
        controller.initialise(configuration, model);
        
        AgentModel a = new AgentModel();
        AgentModel b = new AgentModel();
        AgentModel c = new AgentModel();
        
        a.setName("agentA");
        b.setName("agentB");
        c.setName("agentC");
        
        a.setConnected(true);
        b.setConnected(true);
        c.setConnected(true);
        
        List<AgentModel> agents = CollectionUtils.newArrayList(a,b,c);
        
        FactoryMap<String, FactoryMap<String, DataBucket>> bucketsByAgentBySource = controller.divideDataIntoBuckets(agents);
        
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
        
        assertThat(agentABucket.get("data1").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentBBucket.get("data1").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentCBucket.get("data1").getColumns(), is(new String[] { "string", "value"}));
        
        assertThat(agentABucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentBBucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentCBucket.get("data1").getDataSourceName(), is("data1"));
        
        assertThat(agentABucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][] {{"one", "1"}, {"four", "4"}}));
        assertThat(agentBBucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][] {{"two", "2"}, {"five", "5"}}));
        assertThat(agentCBucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][] {{"three", "3"}}));
    }
    
    @Test
    public void test_divideDataIntoBuckets_more_entries() {
    
        ConsoleModel model = new ConsoleModel();
        InteractiveConfiguration configuration = new InteractiveConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_more.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1", sampleData, DataStrategy.pooledAgent.toString()) ));

        SwingConsoleController controller = new SwingConsoleController();
        controller.initialise(configuration, model);
        
        AgentModel a = new AgentModel();
        AgentModel b = new AgentModel();
        AgentModel c = new AgentModel();
        
        a.setName("agentA");
        b.setName("agentB");
        c.setName("agentC");
        
        a.setConnected(true);
        b.setConnected(true);
        c.setConnected(true);
        
        List<AgentModel> agents = CollectionUtils.newArrayList(a,b,c);
        
        FactoryMap<String, FactoryMap<String, DataBucket>> bucketsByAgentBySource = controller.divideDataIntoBuckets(agents);
        
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
        
        assertThat(agentABucket.get("data1").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentBBucket.get("data1").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentCBucket.get("data1").getColumns(), is(new String[] { "string", "value"}));
        
        assertThat(agentABucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentBBucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentCBucket.get("data1").getDataSourceName(), is("data1"));
        
        assertThat(agentABucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][] {{"one", "1"}, {"four", "4"}, {"seven", "7"}, {"ten", "10"}}));
        assertThat(agentBBucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][] {{"two", "2"}, {"five", "5"}, {"eight", "8"}, {"eleven", "11"}}));
        assertThat(agentCBucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][] {{"three", "3"}, {"six", "6"}, {"nine", "9"}}));
    }
    
    @Test
    public void test_divideDataIntoBuckets_too_few_entries() {
    
        ConsoleModel model = new ConsoleModel();
        InteractiveConfiguration configuration = new InteractiveConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_too_few.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1", sampleData, DataStrategy.pooledAgent.toString()) ));

        SwingConsoleController controller = new SwingConsoleController();
        controller.initialise(configuration, model);
        
        AgentModel a = new AgentModel();
        AgentModel b = new AgentModel();
        AgentModel c = new AgentModel();
        
        a.setName("agentA");
        b.setName("agentB");
        c.setName("agentC");
        
        a.setConnected(true);
        b.setConnected(true);
        c.setConnected(true);
        
        List<AgentModel> agents = CollectionUtils.newArrayList(a,b,c);
        
        FactoryMap<String, FactoryMap<String, DataBucket>> bucketsByAgentBySource = controller.divideDataIntoBuckets(agents);
        
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
        
        assertThat(agentABucket.get("data1").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentBBucket.get("data1").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentCBucket.get("data1").getColumns(), is(new String[] { "string", "value"}));
        
        assertThat(agentABucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentBBucket.get("data1").getDataSourceName(), is("data1"));
        assertThat(agentCBucket.get("data1").getDataSourceName(), is("data1"));
        
        assertThat(agentABucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][] {{"one", "1"}}));
        assertThat(agentBBucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][] {{"two", "2"}}));
        assertThat(agentCBucket.get("data1").getValues().toArray(new String[0][0]), is(new String[][] {{"one", "1"}}));
    }
    
    @Test
    public void test_divideDataIntoBuckets_just_fixed_thread() {
        
        ConsoleModel model = new ConsoleModel();
        InteractiveConfiguration configuration = new InteractiveConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_exact.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1", sampleData, DataStrategy.fixedThread.toString())));

        SwingConsoleController controller = new SwingConsoleController();
        controller.initialise(configuration, model);
        
        AgentModel a = new AgentModel();
        AgentModel b = new AgentModel();
        AgentModel c = new AgentModel();
        
        a.setName("agentA");
        b.setName("agentB");
        c.setName("agentC");
        
        a.setConnected(true);
        b.setConnected(true);
        c.setConnected(true);
        
        List<AgentModel> agents = CollectionUtils.newArrayList(a,b,c);
        
        FactoryMap<String, FactoryMap<String, DataBucket>> bucketsByAgentBySource = controller.divideDataIntoBuckets(agents);
        
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
    
    @Test
    public void test_divideDataIntoBuckets_just_fixed_thread_with_mock_clients() {
        
        ConsoleModel model = new ConsoleModel();
        InteractiveConfiguration configuration = new InteractiveConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_exact.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1", sampleData, DataStrategy.fixedThread.toString())));

        SwingConsoleController controller = new SwingConsoleController();
        controller.initialise(configuration, model);
        
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
        
        a.setKryoClient(aKryoMock);
        b.setKryoClient(bKryoMock);
        c.setKryoClient(cKryoMock);
        
        List<AgentModel> agents = CollectionUtils.newArrayList(a,b,c);

        model.getAgentModels().addAll(agents);
        
        
        controller.startTest();
        
        Mockito.verify(aKryoMock, Mockito.times(2)).sendRequest(Mockito.anyString(), Mockito.anyObject(), Mockito.any(ResponseHandler.class));
        Mockito.verify(bKryoMock, Mockito.times(2)).sendRequest(Mockito.anyString(), Mockito.anyObject(), Mockito.any(ResponseHandler.class));
        Mockito.verify(cKryoMock, Mockito.times(2)).sendRequest(Mockito.anyString(), Mockito.anyObject(), Mockito.any(ResponseHandler.class));
        
        
    }
    
    
    @Test
    public void test_divideDataIntoBuckets_multiple_buckets() {
    
        ConsoleModel model = new ConsoleModel();
        InteractiveConfiguration configuration = new InteractiveConfiguration();
        String sampleData = "/com/jbombardier/interactive/sample_data_exact.csv";
        configuration.setCsvProperties(CollectionUtils.newArrayList(new CsvProperty("data1", sampleData, DataStrategy.fixedThread.toString()),
                                                          new CsvProperty("data2", sampleData, DataStrategy.pooledAgent.toString()),
                                                          new CsvProperty("data3", sampleData, DataStrategy.pooledGlobal.toString()),
                                                          new CsvProperty("data4", sampleData, DataStrategy.pooledThread.toString())
                                                          ));

        SwingConsoleController controller = new SwingConsoleController();
        controller.initialise(configuration, model);
        
        AgentModel a = new AgentModel();
        AgentModel b = new AgentModel();
        AgentModel c = new AgentModel();
        
        a.setName("agentA");
        b.setName("agentB");
        c.setName("agentC");
        
        a.setConnected(true);
        b.setConnected(true);
        c.setConnected(true);
        
        List<AgentModel> agents = CollectionUtils.newArrayList(a,b,c);
        
        FactoryMap<String, FactoryMap<String, DataBucket>> bucketsByAgentBySource = controller.divideDataIntoBuckets(agents);
        
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
        
        assertThat(agentABucket.get("data2").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentBBucket.get("data2").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentCBucket.get("data2").getColumns(), is(new String[] { "string", "value"}));
        
        assertThat(agentABucket.get("data3").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentBBucket.get("data3").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentCBucket.get("data3").getColumns(), is(new String[] { "string", "value"}));
        
        assertThat(agentABucket.get("data4").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentBBucket.get("data4").getColumns(), is(new String[] { "string", "value"}));
        assertThat(agentCBucket.get("data4").getColumns(), is(new String[] { "string", "value"}));
        
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
        
        assertThat(agentABucket.get("data2").getValues().toArray(new String[0][0]), is(new String[][] {{"one", "1"}, {"four", "4"}, {"seven", "7"}}));
        assertThat(agentBBucket.get("data2").getValues().toArray(new String[0][0]), is(new String[][] {{"two", "2"}, {"five", "5"}, {"eight", "8"}}));
        assertThat(agentCBucket.get("data2").getValues().toArray(new String[0][0]), is(new String[][] {{"three", "3"}, {"six", "6"}, {"nine", "9"}}));
        
        String[][] allEntries = new String[][] {{"one", "1"}, {"two", "2"}, {"three", "3"},{"four", "4"}, {"five", "5"},{"six", "6"},{"seven", "7"},{"eight", "8"},{"nine", "9"}};
        assertThat(agentABucket.get("data3").getValues().toArray(new String[0][0]), is(allEntries));
        assertThat(agentBBucket.get("data3").getValues().toArray(new String[0][0]), is(allEntries));
        assertThat(agentCBucket.get("data3").getValues().toArray(new String[0][0]), is(allEntries));
       
        assertThat(agentABucket.get("data4").getValues().toArray(new String[0][0]), is(new String[][] {{"one", "1"}, {"four", "4"}, {"seven", "7"}}));
        assertThat(agentBBucket.get("data4").getValues().toArray(new String[0][0]), is(new String[][] {{"two", "2"}, {"five", "5"}, {"eight", "8"}}));
        assertThat(agentCBucket.get("data4").getValues().toArray(new String[0][0]), is(new String[][] {{"three", "3"}, {"six", "6"}, {"nine", "9"}}));
    }
}

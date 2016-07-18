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

import java.util.List;

import com.logginghub.messaging2.kryo.KryoClient;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.jbombardier.common.AgentStats;
import com.jbombardier.common.AgentStats.TestStats;
import com.logginghub.utils.observable.ObservableProperty;

public class AgentModel extends Observable {

    private ObservableProperty<String> name = createStringProperty("name", null);
    private ObservableProperty<String> address = createStringProperty("address", null);
    private ObservableInteger port = createIntProperty("port", 0);

    private ObservableProperty<Boolean> connected = createBooleanProperty("connected", false);
    private ObservableProperty<Boolean> packageReceived = createBooleanProperty("packageReceived", false);

    private KryoClient client;
    
    private ObservableInteger threadCount = createIntProperty("threadCount", 0);
    
//    private int threadCount;
    
//    public static interface AgentModelListener {
//        void statsUpdated();
//    }
    
    public AgentModel() {}
    
    public AgentModel(String name, String address, int port) {
        super();
        this.name.set(name);
        this.address.set(address);
        this.port.set(port);
    }

//    private List<AgentModelListener> listeners = new CopyOnWriteArrayList<AgentModel.AgentModelListener>();
//    
//    public void addAgentModelListener(AgentModelListener listener){
//        listeners.add(listener);
//    }
//    
//    public void removeAgentModelListener(AgentModelListener listener){
//        listeners.remove(listener);
//    }


    public ObservableInteger getPort() {
        return port;
    }

    public ObservableProperty<Boolean> getConnected() {
        return connected;
    }

    public ObservableProperty<Boolean> getPackageReceived() {
        return packageReceived;
    }

    public ObservableProperty<String> getAddress() {
        return address;
    }

    public ObservableProperty<String> getName() {
        return name;
    }

    public void setKryoClient(KryoClient client) {
        this.client = client;
    }

    public KryoClient getKryoClient() {
        return client;
    }

    public void incrementActiveThreadCount(int threads) {
        threadCount.increment(threads);
    }

    public ObservableInteger getThreadCount() {
        return threadCount;
    }
    
    public void setThreadCount(int threadCount) {
        this.threadCount.set(threadCount);
    }

    public void update(AgentStats agentStats) {
        int threads =0 ;
        List<TestStats> testStats = agentStats.getTestStats();
        for (TestStats testStats2 : testStats) {
            threads += testStats2.getThreadCount();
        }
        
        setThreadCount(threads);
    }

    @Override public String toString() {
        return "AgentModel [name=" + name.get() + ", address=" + address.get() + ", port=" + port.get() + ", connected=" + connected.get() + "]";
    }
    
    
}

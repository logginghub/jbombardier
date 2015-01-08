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

package com.jbombardier.console.model;

import java.util.List;

import com.logginghub.messaging2.kryo.KryoClient;
import com.logginghub.utils.AbstractBean;
import com.logginghub.utils.observable.ObservableInteger;
import com.jbombardier.common.AgentStats;
import com.jbombardier.common.AgentStats.TestStats;

public class AgentModel extends AbstractBean {

    private String name;
    private String address;
    private int port;
    private boolean connected = false;
    private boolean packageReceived = false;
    
    private KryoClient client;
    
    private ObservableInteger threadCount = new ObservableInteger(0);
    
//    private int threadCount;
    
//    public static interface AgentModelListener {
//        void statsUpdated();
//    }
    
    public AgentModel() {}
    
    public AgentModel(String name, String address, int port) {
        super();
        this.name = name;
        this.address = address;
        this.port = port;
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

    public void setName(String name) {
        String old = this.name;
        this.name = name;
        firePropertyChange(name, old, name);
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        String old = this.address;
        this.address = address;
        firePropertyChange("address", old, address);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        int old = this.port;
        this.port = port;
        firePropertyChange("port", old, port);
    }

    public boolean isPackageReceived() {
        return packageReceived;
    }
    
    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        boolean old = this.connected;
        this.connected = connected;
        firePropertyChange("connected", old, connected);
    }
    
    public void setPackageReceived(boolean packageReceived) {
        boolean old = this.packageReceived;
        this.packageReceived = packageReceived;
        firePropertyChange("packageReceived", old, packageReceived);
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
        
//        for (AgentModelListener agentModelListener : listeners) {
//            agentModelListener.statsUpdated();
//        }
        
    }

    @Override public String toString() {
        return "AgentModel [name=" + name + ", address=" + address + ", port=" + port + ", connected=" + connected + "]";
    }
    
    
}

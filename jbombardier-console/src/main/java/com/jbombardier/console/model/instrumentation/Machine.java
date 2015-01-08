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

package com.jbombardier.console.model.instrumentation;

import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.utils.AbstractBean;

public class Machine extends AbstractBean {

    private float cpuUsage;
    private long totalMemory;
    private long usedMemory;
    private int cpuCores;
    private float ioBytesPerSecond;
    private float networkBytesPerSecond;
        
    private TimeSeriesData history = new TimeSeriesData();
    
    public float getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(float cpuUsage) {
        firePropertyChange("cpuUsage", this.cpuUsage, this.cpuUsage = cpuUsage);
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        firePropertyChange("totalMemory", this.totalMemory, this.totalMemory = totalMemory);
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        firePropertyChange("usedMemory", this.usedMemory, this.usedMemory = usedMemory);
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(int cpuCores) {
        firePropertyChange("cpuCores", this.cpuCores, this.cpuCores = cpuCores);
    }

    public float getIoBytesPerSecond() {
        return ioBytesPerSecond;
    }

    public void setIoBytesPerSecond(float ioBytesPerSecond) {
        firePropertyChange("ioBytesPerSecond", this.ioBytesPerSecond, this.ioBytesPerSecond = ioBytesPerSecond);
    }

    public float getNetworkBytesPerSecond() {
        return networkBytesPerSecond;
    }

    public void setNetworkBytesPerSecond(float networkBytesPerSecond) {
        firePropertyChange("networkBytesPerSecond", this.networkBytesPerSecond, this.networkBytesPerSecond = networkBytesPerSecond);
    }

}

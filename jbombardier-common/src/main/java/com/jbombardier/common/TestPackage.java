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

package com.jbombardier.common;

import java.util.List;

public class TestPackage {

    private List<PhaseInstruction> phases;

    private String agentName;
    
    private String loggingType;
    private String loggingHubs;

    public TestPackage(String agentName, List<PhaseInstruction> phases) {
        this.agentName = agentName;
        this.phases = phases;
    }
    
    public TestPackage() {

    }

    public void setLoggingHubs(String loggingHubs) {
        this.loggingHubs = loggingHubs;
    }
    
    public void setLoggingType(String loggingType) {
        this.loggingType = loggingType;
    }
    
    public String getLoggingHubs() {
        return loggingHubs;
    }
    
    public String getLoggingType() {
        return loggingType;
    }
    
    public String getAgentName() {
        return agentName;
    }

    public List<PhaseInstruction> getPhases() {
        return phases;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public void setPhases(List<PhaseInstruction> phases) {
        this.phases = phases;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("TestPackage{");
        sb.append("phases=").append(phases);
        sb.append(", agentName='").append(agentName).append('\'');
        sb.append(", loggingType='").append(loggingType).append('\'');
        sb.append(", loggingHubs='").append(loggingHubs).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

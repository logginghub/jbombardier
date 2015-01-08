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

package com.jbombardier.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.jbombardier.common.AgentInstruction;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)

@XmlRootElement
public class HostConfiguration {

    private List<AgentInstruction> agentInstructionszz;
    private long timeout = 10;
    private String timeoutUnits = "MINUTES";
    private String reportsDir = "reports";
    
    private List<AgentProperty> agentProperties = new ArrayList<AgentProperty>();
    private List<AgentListProperty> agentListProperties = new ArrayList<AgentListProperty>();
    private List<CsvProperty> csvProperties = new ArrayList<CsvProperty>();

    public void setReportsDir(String reportsDir)
    {
        this.reportsDir = reportsDir;
    }
    
    @XmlAttribute
    public String getReportsDir()
    {
        return reportsDir;
    }
    
    public void setAgentInstructions(List<AgentInstruction> agentInstructions)
    {
        this.agentInstructionszz = agentInstructions;
    }
    
    @XmlElement(name="property")
    @XmlElementWrapper( name="properties" )
    public List<AgentProperty> getAgentProperties(){
        return this.agentProperties;
    }
    
    @XmlElement(name="listProperty")
    @XmlElementWrapper( name="listProperties" )
    public List<AgentListProperty> getAgentListProperties()
    {
        return agentListProperties;
    }
    
    @XmlElement(name="csvProperty")
    @XmlElementWrapper( name="csvProperties" )
    public List<CsvProperty> getCsvProperties() {
        return csvProperties;
    }
    
    public void setAgentListProperties(List<AgentListProperty> agentListProperties)
    {
        this.agentListProperties = agentListProperties;
    }
    
    
    public void setAgentProperties(List<AgentProperty> agentProperties)
    {
        this.agentProperties = agentProperties;
    }
        
    @XmlElement(name="agentInstruction")
    public List<AgentInstruction> getAgentInstructions()
    {
        return this.agentInstructionszz;
    }
    
    public void setTimeoutUnits(String timeoutUnits)
    {
        System.out.println("Timeout units set " + timeoutUnits);
        this.timeoutUnits = timeoutUnits;
    }
    
    @XmlAttribute
    public String getTimeoutUnits()
    {
        return timeoutUnits;
    }
    
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }
    
    @XmlAttribute
    public long getTimeout()
    {
        return timeout;
    }
}
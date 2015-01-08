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

package com.jbombardier.common;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class AgentInstruction
{        
    private String host;
    
    private ArrayList<TestInstruction> testInstructions;
    
    public void setHost(String host)
    {
        this.host = host;
    }
        
    public void setTestInstructions(ArrayList<TestInstruction> instructions)
    {
        this.testInstructions = instructions;
    }

    @XmlAttribute
    public String getHost()
    {
        return host;
    }
    
    
    @XmlElement(name="testInstruction")
    public ArrayList<TestInstruction> getTestInstructions()
    {
        return testInstructions;
    }
}
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

package com.jbombardier.console.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Agent {
    private String name;
    private String address;
    private int port = 20001;
    private int objectBufferSize = 1024 * 1024;
    private int writeBufferSize = 1024 * 1024;
    public static final String embeddedName = "embedded";
    private int pingTimeout = 5000;
    
    @XmlAttribute
    public void setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
    }
    
    public int getPingTimeout() {
        return pingTimeout;
    }
    
    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public int getPort() {
        return port;
    }
    
    @XmlAttribute
    public void setAddress(String address) {
        this.address = address;
    }
    
    @XmlAttribute
    public void setPort(int port) {
        this.port = port;
    }

    public int getObjectBufferSize() {
        return objectBufferSize;
    }

    @XmlAttribute
    public void setObjectBufferSize(int objectBufferSize) {
        this.objectBufferSize = objectBufferSize;
    }

    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    @XmlAttribute
    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

}
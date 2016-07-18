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

package com.jbombardier.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.logginghub.utils.VLPorts;

@XmlAccessorType(XmlAccessType.FIELD) @XmlRootElement public class RepositoryConfiguration {
    
    @XmlAttribute private String dataFolder = "data/";
    @XmlAttribute private int httpPort = VLPorts.getRepositoryWebDefaultPort();
    @XmlAttribute private int serverPort = VLPorts.getRepositoryDefaultPort();
    
    public int getHttpPort() {
        return httpPort;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }
    
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
    
    public String getDataFolder() {
        return dataFolder;
    }
    
    public void setDataFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }
}

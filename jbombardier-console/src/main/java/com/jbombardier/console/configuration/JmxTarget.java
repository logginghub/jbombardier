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

package com.jbombardier.console.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by james on 21/11/14.
 */
@XmlAccessorType(XmlAccessType.FIELD) public class JmxTarget {
    @XmlAttribute String objectName;
    @XmlAttribute String attribute;
    @XmlAttribute String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
}

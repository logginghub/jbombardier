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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 09/01/15.
 */
@XmlAccessorType(XmlAccessType.FIELD) public class PhaseConfiguration {

    @XmlAttribute(name = "name") private String phaseName;
    @XmlElementWrapper(name = "tests") @XmlElement(name = "test")
    private List<TestConfiguration> tests = new ArrayList<TestConfiguration>();
    @XmlAttribute private String duration;
    @XmlAttribute private String warmupDuration;

    public List<TestConfiguration> getTests() {
        return tests;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getWarmupDuration() {
        return warmupDuration;
    }

    public void setWarmupDuration(String warmupDuration) {
        this.warmupDuration = warmupDuration;
    }
}

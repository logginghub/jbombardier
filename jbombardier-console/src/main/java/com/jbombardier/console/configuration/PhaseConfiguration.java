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
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 09/01/15.
 */
@XmlAccessorType(XmlAccessType.FIELD) public class PhaseConfiguration {

    @XmlAttribute(name = "name") private String phaseName;
    @XmlElement private List<TestConfiguration> test = new ArrayList<TestConfiguration>();
    @XmlAttribute private String duration;
    @XmlAttribute private String warmupDuration;
    @XmlAttribute private String inheritFrom;
    @XmlAttribute private double rateMultiplier = 1;
    @XmlElement private List<StateEstablisherConfiguration> stateEstablishers = new ArrayList<StateEstablisherConfiguration>();
    @XmlElement private List<PhaseControllerConfiguration> phaseControllers = new ArrayList<PhaseControllerConfiguration>();
    @XmlAttribute private boolean enabled = true;

    public void setRateMultiplier(double rateMultiplier) {
        this.rateMultiplier = rateMultiplier;
    }

    public double getRateMultiplier() {
        return rateMultiplier;
    }

    public String getInheritFrom() {
        return inheritFrom;
    }

    public void setInheritFrom(String inheritFrom) {
        this.inheritFrom = inheritFrom;
    }

    public List<TestConfiguration> getTests() {
        return test;
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

    public List<StateEstablisherConfiguration> getStateEstablishers() {
        return stateEstablishers;
    }

    public void setStateEstablishers(List<StateEstablisherConfiguration> stateEstablishers) {
        this.stateEstablishers = stateEstablishers;
    }


    public List<PhaseControllerConfiguration> getPhaseControllers() {
        return phaseControllers;
    }

    public void setPhaseControllers(List<PhaseControllerConfiguration> phaseControllers) {
        this.phaseControllers = phaseControllers;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}



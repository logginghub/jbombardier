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

import com.jbombardier.xml.CsvProperty;
import com.logginghub.utils.EnvironmentProperties;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.VLPorts;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.annotation.*;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD) @XmlRootElement(name = "jbombardierConfiguration")
public class JBombardierConfiguration {

    @XmlElement private List<PhaseConfiguration> phase = new ArrayList<PhaseConfiguration>();
    @XmlElement private List<AgentConfiguration> agent = new ArrayList<AgentConfiguration>();
    @XmlElement private List<TestConfiguration> test = new ArrayList<TestConfiguration>();
    @XmlElement private List<Transaction> transaction = new ArrayList<Transaction>();
    @XmlElement private List<Property> property = new ArrayList<Property>();
    @XmlElement private List<CsvProperty> csvProperty = new ArrayList<CsvProperty>();

    @XmlElement private List<HubCapture> hubCapture = new ArrayList<HubCapture>();
    @XmlElement private List<JmxCapture> jmxCapture = new ArrayList<JmxCapture>();
    @XmlElement private List<StatisticsCapture> statisticsCapture = new ArrayList<StatisticsCapture>();

    @XmlAttribute private int autostartAgents = Integer.getInteger("jbombardierConsoleAutostartAgents", -1);

    @XmlAttribute private String testName = "default";
    @XmlAttribute private double transactionRateModifier = 1;
    @XmlAttribute private int telemetryHubPort = VLPorts.getTelemetryHubDefaultPort() + 1;
    @XmlAttribute private String externalTelemetryHub = null;
    @XmlAttribute private boolean sendKillOnConsoleClose = true;
    @XmlAttribute private int maximumResultToStore = 10000;
    @XmlAttribute private boolean visualErrorMessages = true;
    @XmlAttribute private int failedTransactionCountFailureThreshold = -1;

    @XmlAttribute private String loggingTypes = null;
    @XmlAttribute private String loggingHubs = null;
    @XmlAttribute private int maximumConsoleEntries = 10000;
    @XmlAttribute private long noResultsTimeout = 30000;

    @XmlAttribute private String resultRepositoryHost = null;

    @XmlAttribute private boolean outputEmbeddedAgentStats;
    @XmlAttribute private boolean outputControllerStats = EnvironmentProperties.getBoolean(
            "jbombardierConsoleController.disableStats");
    @XmlAttribute private int resultRepositoryPort = VLPorts.getRepositoryDefaultPort();
    @XmlAttribute private String reportsFolder = "reports";

    @XmlAttribute private String warmupTime = "0";
    @XmlAttribute private String duration = "1 minute";
    @XmlAttribute private boolean openReport = false;

    @XmlElement private List<StateEstablisherConfiguration> stateEstablishers = new ArrayList<StateEstablisherConfiguration>();

    public String getResultRepositoryHost() {
        return resultRepositoryHost;
    }

    public void setResultRepositoryHost(String resultRepositoryHost) {
        this.resultRepositoryHost = resultRepositoryHost;
    }

    public String getLoggingHubs() {
        return loggingHubs;
    }

    public void setLoggingHubs(String loggingHubs) {
        this.loggingHubs = loggingHubs;
    }

    public String getLoggingTypes() {
        return loggingTypes;
    }

    public void setLoggingTypes(String loggingTypes) {
        this.loggingTypes = loggingTypes;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public int getMaximumResultToStore() {
        return maximumResultToStore;
    }

    public void setMaximumResultToStore(int maximumResultToStore) {
        this.maximumResultToStore = maximumResultToStore;
    }

    public boolean isSendKillOnConsoleClose() {
        return sendKillOnConsoleClose;
    }

    public void setSendKillOnConsoleClose(boolean sendKillOnConsoleClose) {
        this.sendKillOnConsoleClose = sendKillOnConsoleClose;
    }

    public int getTelemetryHubPort() {
        return telemetryHubPort;
    }

    public void setTelemetryHubPort(int telemetryHubPort) {
        this.telemetryHubPort = telemetryHubPort;
    }

    public double getTransactionRateModifier() {
        return transactionRateModifier;
    }

    public void setTransactionRateModifier(double transactionRateModifier) {
        this.transactionRateModifier = transactionRateModifier;
    }

    public List<Transaction> getTransactions() {
        return transaction;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transaction = transactions;
    }

    public List<Property> getProperties() {
        return property;
    }

    public void setProperties(List<Property> properties) {
        this.property = properties;
    }

    public List<AgentConfiguration> getAgents() {
        return agent;
    }

    public void setAgents(List<AgentConfiguration> agentConfigurationDetails) {
        this.agent = agentConfigurationDetails;
    }

    public List<CsvProperty> getCsvProperties() {
        return csvProperty;
    }

    public void setCsvProperties(List<CsvProperty> csvProperties) {
        this.csvProperty = csvProperties;
    }

    public List<TestConfiguration> getTests() {
        return test;
    }

    public void setTests(List<TestConfiguration> tests) {
        this.test = tests;
    }

    public static JBombardierConfiguration loadConfiguration(String configurationPath) {
        try {
            InputStream resource = ResourceUtils.openStream(configurationPath);
            JAXBContext context = JAXBContext.newInstance(JBombardierConfiguration.class);
            Unmarshaller um = context.createUnmarshaller();
            um.setEventHandler(new DefaultValidationEventHandler() {
                @Override public boolean handleEvent(ValidationEvent event) {

                    System.out.println(event);

                    return super.handleEvent(event);
                }
            });
            JBombardierConfiguration configuration = (JBombardierConfiguration) um.unmarshal(new InputStreamReader(
                    resource));
            return configuration;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration from " + configurationPath, e);
        }
    }

    public int getAutostartAgents() {
        return autostartAgents;
    }

    public void setAutostartAgents(int autostartAgents) {
        this.autostartAgents = autostartAgents;
    }

    public String getExternalTelemetryHub() {
        return externalTelemetryHub;
    }

    public void setExternalTelemetryHub(String externalTelemetryHub) {
        this.externalTelemetryHub = externalTelemetryHub;
    }

    public void validate() {
        ConfigurationValidator validator = new ConfigurationValidator();
        validator.validate(this);
    }

    public void setVisualErrorMessages(boolean visualErrorMessages) {
        this.visualErrorMessages = visualErrorMessages;
    }

    public boolean showVisualErrorMessages() {
        return visualErrorMessages;
    }

    public int getFailedTransactionCountFailureThreshold() {
        return failedTransactionCountFailureThreshold;
    }

    public void setFailedTransactionCountFailureThreshold(int failedTransactionFailureThreshold) {
        this.failedTransactionCountFailureThreshold = failedTransactionFailureThreshold;
    }

    public int getMaximumConsoleEntries() {
        return maximumConsoleEntries;
    }

    public void setMaximumConsoleEntries(int maximumConsoleEntries) {
        this.maximumConsoleEntries = maximumConsoleEntries;
    }

    public long getNoResultsTimeout() {
        return noResultsTimeout;
    }

    public void setNoResultsTimeout(long noResultsTimeout) {
        this.noResultsTimeout = noResultsTimeout;
    }

    public boolean isOutputEmbeddedAgentStats() {
        return outputEmbeddedAgentStats;
    }

    public void setOutputEmbeddedAgentStats(boolean outputEmbeddedAgentStats) {
        this.outputEmbeddedAgentStats = outputEmbeddedAgentStats;
    }

    public boolean isOutputControllerStats() {
        return outputControllerStats;
    }

    public void setOutputControllerStats(boolean outputControllerStats) {
        this.outputControllerStats = outputControllerStats;
    }

    public int getResultRepositoryPort() {
        return resultRepositoryPort;
    }

    public void setResultRepositoryPort(int resultRepositoryPort) {
        this.resultRepositoryPort = resultRepositoryPort;
    }

    public List<StatisticsCapture> getStatisticsCapture() {
        return statisticsCapture;
    }

    public void setReportsFolder(String reportsFolder) {
        this.reportsFolder = reportsFolder;
    }

    public String getReportsFolder() {
        return reportsFolder;
    }

    public List<HubCapture> getHubCapture() {
        return hubCapture;
    }

    public List<JmxCapture> getJmxCapture() {
        return jmxCapture;
    }

    public List<PhaseConfiguration> getPhases() {
        return phase;
    }

    public void setWarmUpTime(String warmupTime) {
        this.warmupTime = warmupTime;
    }

    public String getWarmupTime() {
        return warmupTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isOpenReport() {
        return openReport;
    }

    public void setOpenReport(boolean openReport) {
        this.openReport = openReport;
    }

    public List<StateEstablisherConfiguration> getStateEstablishers() {
        return stateEstablishers;
    }

    public void setStateEstablishers(List<StateEstablisherConfiguration> stateEstablishers) {
        this.stateEstablishers = stateEstablishers;
    }
}

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

import com.jbombardier.common.PerformanceTest;
import com.jbombardier.common.StatisticProvider;
import com.jbombardier.console.JBombardierController;
import com.jbombardier.console.JBombardierSwingConsole;
import com.jbombardier.console.headless.JBombardierHeadless;
import com.jbombardier.console.model.TransactionResultModel;
import com.logginghub.utils.JAXBConfiguration;

import java.util.List;

public class JBombardierConfigurationBuilder {

    private JBombardierConfiguration configuration = new JBombardierConfiguration();

    public static JBombardierConfigurationBuilder start() {
        return configurationBuilder();
    }

    public static JBombardierConfigurationBuilder configurationBuilder() {
        return new JBombardierConfigurationBuilder();
    }

    public static TestBuilder test(Class<? extends PerformanceTest> testClass) {
        return new TestBuilder(testClass);
    }

    public static AgentBuilder agent() {
        return new AgentBuilder();
    }

    public JBombardierConfiguration getConfiguration() {
        return configuration;
    }

    public JBombardierConfiguration toConfiguration() {
        return configuration;
    }

    public static AgentBuilder embeddedAgent() {
        return new AgentBuilder().name(Agent.embeddedName);
    }

    public JBombardierConfigurationBuilder loggingHubs(String loggingHubs) {
        configuration.setLoggingHubs(loggingHubs);
        return this;
    }

    public JBombardierConfigurationBuilder duration(String duration) {
        configuration.setDuration(duration);
        return this;
    }

    public JBombardierConfigurationBuilder warmUpTime(String duration) {
        configuration.setWarmUpTime(duration);
        return this;
    }

    public JBombardierConfigurationBuilder loggingTypes(String loggingTypes) {
        configuration.setLoggingTypes(loggingTypes);
        return this;
    }

    public JBombardierConfigurationBuilder telemetryPort(int port) {
        configuration.setTelemetryHubPort(port);
        return this;
    }

    public JBombardierConfigurationBuilder failedTransactionCountFailureThreshold(int threshold) {
        configuration.setFailedTransactionCountFailureThreshold(threshold);
        return this;
    }

    public JBombardierConfigurationBuilder externalTelemetryHub(String hub) {
        configuration.setExternalTelemetryHub(hub);
        return this;
    }

    public JBombardierConfigurationBuilder addAgent(AgentBuilder agentBuilder) {
        configuration.getAgents().add(agentBuilder.toAgent());
        configuration.setAutostartAgents(configuration.getAgents().size());
        return this;
    }

    public JBombardierConfigurationBuilder addAgent(String name, String host, int port) {
        Agent agent = new Agent();
        agent.setName(name);
        agent.setAddress(host);
        agent.setPort(port);
        configuration.getAgents().add(agent);
        configuration.setAutostartAgents(configuration.getAgents().size());
        return this;
    }


    public JBombardierConfigurationBuilder addAgent(String name, String host, int port, int sizes) {
        Agent agent = new Agent();
        agent.setName(name);
        agent.setAddress(host);
        agent.setPort(port);
        agent.setObjectBufferSize(sizes);
        agent.setWriteBufferSize(sizes);
        configuration.getAgents().add(agent);
        configuration.setAutostartAgents(configuration.getAgents().size());
        return this;
    }

    public JBombardierConfigurationBuilder addTest(TestBuilder testBuilder) {
        configuration.getTests().add(testBuilder.toTestConfiguration());
        return this;
    }

    public JBombardierConfigurationBuilder addStatisticsProvider(Class<StatisticProvider> providerClass,
                                                                 String properties) {
        //        configuration.getStatisticProviders().add(providerClass);
        return this;
    }

    public JBombardierConfigurationBuilder fromXml(String resourceOrFile) {
        this.configuration = JAXBConfiguration.loadConfiguration(JBombardierConfiguration.class, resourceOrFile);
        return this;
    }

    public JBombardierConfigurationBuilder addEmbeddedAgent(int bufferSizes, int pingTimeout) {
        Agent agent = new Agent();
        agent.setName(Agent.embeddedName);
        agent.setObjectBufferSize(bufferSizes);
        agent.setWriteBufferSize(bufferSizes);
        agent.setPingTimeout(pingTimeout);
        configuration.getAgents().add(agent);
        return this;
    }

    public JBombardierConfigurationBuilder addEmbeddedAgent(int bufferSizes) {
        Agent agent = new Agent();
        agent.setName(Agent.embeddedName);
        agent.setObjectBufferSize(bufferSizes);
        agent.setWriteBufferSize(bufferSizes);
        configuration.getAgents().add(agent);
        return this;
    }

    public JBombardierConfigurationBuilder addEmbeddedAgent() {
        Agent agent = new Agent();
        agent.setName(Agent.embeddedName);
        configuration.getAgents().add(agent);
        return this;
    }

    public void executeHeadless() {
        executeHeadlessNoExit();
        System.exit(0);
    }

    public JBombardierController executeHeadlessNoExit() {
        if (configuration.getAgents().size() == 0) {
            addEmbeddedAgent();
        }

        configuration.validate();

        JBombardierHeadless headless = new JBombardierHeadless();
        headless.setAgentsRequired(configuration.getAutostartAgents());
        headless.setTimeToWaitForAgents(5000);
        return headless.run(configuration);
    }

    public JBombardierController execute() {
        if (configuration.getAgents().size() == 0) {
            addEmbeddedAgent();
        }

        configuration.validate();
        JBombardierSwingConsole swingConsole = JBombardierSwingConsole.run(configuration);
        return swingConsole.getController();
    }

    public JBombardierConfigurationBuilder autostart(int agents) {
        configuration.setAutostartAgents(agents);
        return this;
    }

    public JBombardierConfigurationBuilder testName(String string) {
        configuration.setTestName(string);
        return this;
    }

    public JBombardierConfigurationBuilder testDuration(String headlessTestDuration) {
        configuration.setDuration(headlessTestDuration);
        return this;
    }

    public JBombardierConfigurationBuilder reportsFolder(String reportsFolder) {
        this.configuration.setReportsFolder(reportsFolder);
        return this;
    }

    public JBombardierConfigurationBuilder resultRepository(String resultRepository) {
        configuration.setResultRepositoryHost(resultRepository);
        return this;
    }

    public JBombardierConfigurationBuilder resultRepository(String resultRepository, int port) {
        configuration.setResultRepositoryHost(resultRepository);
        configuration.setResultRepositoryPort(port);
        return this;
    }

    public JBombardierConfigurationBuilder maximumConsoleEntries(int maximumConsoleEntries) {
        configuration.setMaximumConsoleEntries(maximumConsoleEntries);
        return this;
    }

    public JBombardierConfigurationBuilder maximumResultsToStore(int maximumResultsToStore) {
        configuration.setMaximumResultToStore(maximumResultsToStore);
        return this;

    }

    public JBombardierConfigurationBuilder outputEmbeddedAgentStats(boolean b) {
        configuration.setOutputEmbeddedAgentStats(b);
        return this;
    }

    public JBombardierConfigurationBuilder outputControllerStats(boolean b) {
        configuration.setOutputControllerStats(b);
        return this;

    }

    public PhaseBuilder phase(String s) {
        PhaseBuilder phaseBuilder = new PhaseBuilder();
        phaseBuilder.phaseConfiguration.setPhaseName(s);
        return phaseBuilder;
    }

    public JBombardierConfigurationBuilder warmUpTime(long milliseconds) {
        return warmUpTime(Long.toString(milliseconds));
    }

    public JBombardierConfigurationBuilder testDuration(long milliseconds) {
        return testDuration(Long.toString(milliseconds));
    }

    public JBombardierConfigurationBuilder warmupTime(long milliseconds) {
        return warmUpTime(Long.toString(milliseconds));
    }

    public final static class PhaseBuilder {
        private PhaseConfiguration phaseConfiguration = new PhaseConfiguration();

        public PhaseBuilder addTest(TestBuilder start) {
            phaseConfiguration.getTests().add(start.toTestConfiguration());
            return this;
        }

        public PhaseConfiguration toPhaseConfiguration() {
            return phaseConfiguration;
        }

        public PhaseBuilder duration(String duration) {
            phaseConfiguration.setDuration(duration);
            return this;
        }
    }

    public JBombardierConfigurationBuilder addPhase(PhaseBuilder builder) {
        configuration.getPhases().add(builder.phaseConfiguration);
        return this;
    }

    public static class AgentBuilder {

        private Agent agent = new Agent();

        public AgentBuilder name(String name) {
            agent.setName(name);
            return this;
        }

        public Agent toAgent() {
            return agent;
        }

        public AgentBuilder address(String address) {
            agent.setAddress(address);
            return this;
        }

        public AgentBuilder port(int port) {
            agent.setPort(port);
            return this;
        }

        public AgentBuilder objectBufferSize(int objectBufferSize) {
            agent.setObjectBufferSize(objectBufferSize);
            return this;
        }

        public AgentBuilder writeBufferSize(int writeBufferSize) {
            agent.setWriteBufferSize(writeBufferSize);
            return this;
        }
    }

    public static class TestBuilder {

        private TestConfiguration configuration = new TestConfiguration();

        public TestBuilder(Class<? extends PerformanceTest> clazz) {
            classname(clazz.getName());
            name(clazz.getSimpleName());
        }

        public TestConfiguration toTestConfiguration() {
            return configuration;
        }

        public static TestBuilder start(Class<? extends PerformanceTest> clazz) {
            return new TestBuilder(clazz);
        }

        public TestBuilder properties(String properties) {
            configuration.setProperties(properties);
            return this;
        }

        public TestBuilder propertiesList(List<Property> properties) {
            configuration.setPropertiesList(properties);
            return this;
        }

        public TestBuilder sla(double tp90sla) {
            configuration.setSla(tp90sla);
            return this;
        }

        public TestBuilder failureThreshold(double threshold) {
            configuration.setFailureThreshold(threshold);
            return this;
        }

        public TestBuilder failureThresholdResultCountMinimum(int failureThresholdResultCountMinimum) {
            configuration.setFailureThresholdResultCountMinimum(failureThresholdResultCountMinimum);
            return this;
        }

        public TestBuilder failureThresholdMode(TransactionResultModel.TransactionTimeThresholdMode mode) {
            configuration.setFailureThresholdMode(mode);
            return this;
        }

        public TestBuilder failedTransactionCountFailureThreshold(int threshold) {
            configuration.setFailedTransactionCountFailureThreshold(threshold);
            return this;
        }

        public TestBuilder recordAllValues(boolean recordAllValues) {
            configuration.setRecordAllValues(recordAllValues);
            return this;
        }

        public TestBuilder name(String name) {
            configuration.setName(name);
            return this;
        }

        public TestBuilder classname(String classname) {
            configuration.setClassname(classname);
            return this;
        }

        public TestBuilder threads(int targetThreads) {
            configuration.setTargetThreads(targetThreads);
            return this;
        }

        public TestBuilder threadStep(int threadStep) {
            configuration.setThreadStep(threadStep);
            return this;
        }

        public TestBuilder threadStepTime(long threadStepTime) {
            configuration.setThreadStepTime(threadStepTime);
            return this;
        }

        public TestBuilder targetRate(float targetRate) {
            configuration.setTargetRate(targetRate);
            return this;
        }

        public TestBuilder rateStep(float rateStep) {
            configuration.setRateStep(rateStep);
            return this;
        }

        public TestBuilder rateStepTime(long rateStepTime) {
            configuration.setRateStepTime(rateStepTime);
            return this;
        }

        public TestBuilder movingAveragePoints(int i) {
            configuration.setMovingAveragePoints(i);
            return this;
        }

    }


}

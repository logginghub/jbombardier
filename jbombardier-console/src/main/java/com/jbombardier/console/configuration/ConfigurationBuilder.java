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
import com.jbombardier.console.headless.Headless;
import com.jbombardier.console.SwingConsole;
import com.jbombardier.console.SwingConsoleController;
import com.jbombardier.console.model.TransactionResultModel;
import com.logginghub.utils.JAXBConfiguration;
import com.logginghub.utils.TimeUtils;

import java.util.List;

public class ConfigurationBuilder {

    private InteractiveConfiguration configuration = new InteractiveConfiguration();
    private long headlessWarmupTime = 1000;
    private long headlessTestDuration = 5000;
    private String headlessReportsFolder = "target/reports";

    public static ConfigurationBuilder start() {
        return builder();
    }

    public static ConfigurationBuilder builder() {
        return new ConfigurationBuilder();
    }

    public static TestBuilder test(Class<? extends PerformanceTest> testClass) {
        return new TestBuilder(testClass);
    }

    public static AgentBuilder agent() {
        return new AgentBuilder();
    }

    public InteractiveConfiguration getConfiguration() {
        return configuration;
    }

    public InteractiveConfiguration toConfiguration() {
        return configuration;
    }

    public static AgentBuilder embeddedAgent() {
        return new AgentBuilder().name(Agent.embeddedName);
    }

    public ConfigurationBuilder loggingHubs(String loggingHubs) {
        configuration.setLoggingHubs(loggingHubs);
        return this;
    }

    public ConfigurationBuilder loggingTypes(String loggingTypes) {
        configuration.setLoggingTypes(loggingTypes);
        return this;
    }

    public ConfigurationBuilder telemetryPort(int port) {
        configuration.setTelemetryHubPort(port);
        return this;
    }

    public ConfigurationBuilder failedTransactionCountFailureThreshold(int threshold) {
        configuration.setFailedTransactionCountFailureThreshold(threshold);
        return this;
    }

    public ConfigurationBuilder externalTelemetryHub(String hub) {
        configuration.setExternalTelemetryHub(hub);
        return this;
    }

    public ConfigurationBuilder addAgent(AgentBuilder agentBuilder) {
        configuration.getAgents().add(agentBuilder.toAgent());
        configuration.setAutostartAgents(configuration.getAgents().size());
        return this;
    }

    public ConfigurationBuilder addAgent(String name, String host, int port) {
        Agent agent = new Agent();
        agent.setName(name);
        agent.setAddress(host);
        agent.setPort(port);
        configuration.getAgents().add(agent);
        configuration.setAutostartAgents(configuration.getAgents().size());
        return this;
    }


    public ConfigurationBuilder addAgent(String name, String host, int port, int sizes) {
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

    public ConfigurationBuilder addTest(TestBuilder testBuilder) {
        configuration.getTests().add(testBuilder.toTestConfiguration());
        return this;
    }

    public ConfigurationBuilder addStatisticsProvider(Class<StatisticProvider> providerClass, String properties) {
        //        configuration.getStatisticProviders().add(providerClass);
        return this;
    }

    public ConfigurationBuilder fromXml(String resourceOrFile) {
        this.configuration = JAXBConfiguration.loadConfiguration(InteractiveConfiguration.class, resourceOrFile);
        return this;
    }

    public ConfigurationBuilder addEmbeddedAgent(int bufferSizes, int pingTimeout) {
        Agent agent = new Agent();
        agent.setName(Agent.embeddedName);
        agent.setObjectBufferSize(bufferSizes);
        agent.setWriteBufferSize(bufferSizes);
        agent.setPingTimeout(pingTimeout);
        configuration.getAgents().add(agent);
        return this;
    }

    public ConfigurationBuilder addEmbeddedAgent(int bufferSizes) {
        Agent agent = new Agent();
        agent.setName(Agent.embeddedName);
        agent.setObjectBufferSize(bufferSizes);
        agent.setWriteBufferSize(bufferSizes);
        configuration.getAgents().add(agent);
        return this;
    }

    public ConfigurationBuilder addEmbeddedAgent() {
        Agent agent = new Agent();
        agent.setName(Agent.embeddedName);
        configuration.getAgents().add(agent);
        return this;
    }

    public void executeHeadless() {
        // jshaw - I wish I knew why I'd done this - it seems a bit silly as we need that value later on
        // Turn off auto-start if we are running headless
        // configuration.setAutostartAgents(-1);
        executeHeadlessNoExit();
        System.exit(0);
    }

    public SwingConsoleController executeHeadlessNoExit() {
        if (configuration.getAgents().size() == 0) {
            addEmbeddedAgent();
        }

        configuration.validate();

        Headless headless = new Headless();
        headless.setAgentsRequired(configuration.getAutostartAgents());
        headless.setReportFolder(headlessReportsFolder);
        headless.setSampleTime(headlessTestDuration);
        headless.setWarmupTime(headlessWarmupTime);
        headless.setTimeToWaitForAgents(5000);
        return headless.run(configuration);
    }

    public SwingConsoleController execute() {
        if (configuration.getAgents().size() == 0) {
            addEmbeddedAgent();
        }

        configuration.validate();
        SwingConsole swingConsole = SwingConsole.run(configuration);
        return swingConsole.getController();
    }

    public ConfigurationBuilder autostart(int agents) {
        configuration.setAutostartAgents(agents);
        return this;
    }

    public ConfigurationBuilder testName(String string) {
        configuration.setTestName(string);
        return this;
    }

    public ConfigurationBuilder warmupTime(long headlessWarmupTime) {
        this.headlessWarmupTime = headlessWarmupTime;
        return this;
    }

    public ConfigurationBuilder testDuration(String headlessTestDuration) {
        this.headlessTestDuration = TimeUtils.parseInterval(headlessTestDuration);
        return this;
    }

    public ConfigurationBuilder testDuration(long headlessTestDuration) {
        this.headlessTestDuration = headlessTestDuration;
        return this;
    }

    public ConfigurationBuilder reportsFolder(String reportsFolder) {
        this.headlessReportsFolder = reportsFolder;
        this.configuration.setReportsFolder(reportsFolder);
        return this;
    }

    public ConfigurationBuilder resultRepository(String resultRepository) {
        configuration.setResultRepositoryHost(resultRepository);
        return this;
    }

    public ConfigurationBuilder resultRepository(String resultRepository, int port) {
        configuration.setResultRepositoryHost(resultRepository);
        configuration.setResultRepositoryPort(port);
        return this;
    }

    public ConfigurationBuilder maximumConsoleEntries(int maximumConsoleEntries) {
        configuration.setMaximumConsoleEntries(maximumConsoleEntries);
        return this;
    }

    public ConfigurationBuilder maximumResultsToStore(int maximumResultsToStore) {
        configuration.setMaximumResultToStore(maximumResultsToStore);
        return this;

    }

    public ConfigurationBuilder warmupTime(String string) {
        this.headlessTestDuration = TimeUtils.parseInterval(string);
        return this;

    }

    public ConfigurationBuilder outputEmbeddedAgentStats(boolean b) {
        configuration.setOutputEmbeddedAgentStats(b);
        return this;
    }

    public ConfigurationBuilder outputControllerStats(boolean b) {
        configuration.setOutputControllerStats(b);
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

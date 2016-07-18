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

package com.jbombardier.console;

import static org.hamcrest.Matchers.containsString;

import com.jbombardier.console.configuration.JBombardierConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.jbombardier.console.configuration.AgentConfiguration;
import com.jbombardier.console.configuration.TestConfiguration;

public class TestConsoleStartFailures {

    private JBombardierConfiguration configuration = new JBombardierConfiguration();
    private TestConfiguration testConfiguration1 = new TestConfiguration();
    private TestConfiguration testConfiguration2 = new TestConfiguration();
    private AgentConfiguration agentConfiguration1 = new AgentConfiguration();
    private AgentConfiguration agentConfiguration2 = new AgentConfiguration();
    
    @Before public void disableVisualErrors() {
        configuration.setVisualErrorMessages(false);
    }
    
    
    @Rule public ExpectedException exception = ExpectedException.none();

    @Test public void test_fail_empty_configuration() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("No tests have been specified in your configuration"));
        JBombardierSwingConsole.run(configuration);
    }
    
    @Test public void test_fail_no_agents_configuration() {
        configuration.getTests().add(testConfiguration1);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("No agents have been provided in your configuration"));
        JBombardierSwingConsole.run(configuration);
    }
    
    @Test public void test_fail_test_has_null_classname() {        
        testConfiguration1.setClassname(null);
        configuration.getTests().add(testConfiguration1);
        configuration.getAgents().add(new AgentConfiguration());
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("One of your tests has a null or empty classname"));
        JBombardierSwingConsole.run(configuration);
    }
    
    @Test public void test_fail_test_has_empty_classname() {        
        testConfiguration1.setClassname("");
        configuration.getTests().add(testConfiguration1);
        configuration.getAgents().add(new AgentConfiguration());
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("One of your tests has a null or empty classname"));
        JBombardierSwingConsole.run(configuration);
    }
    
    @Test public void test_fail_class_not_found() {
        testConfiguration1.setClassname("class");
        testConfiguration1.setName("test");
        configuration.getTests().add(testConfiguration1);
        configuration.getAgents().add(new AgentConfiguration());
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("One of your tests (test) specifies a class (class) that could not be loaded"));
        JBombardierSwingConsole.run(configuration);
    }
    
    @Test public void test_fail_two_tests_with_same_name() {
        testConfiguration1.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration2.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration1.setName("test");
        testConfiguration2.setName("test");
        configuration.getTests().add(testConfiguration1);
        configuration.getTests().add(testConfiguration2);
        configuration.getAgents().add(new AgentConfiguration());
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("Some of your tests have the same name"));
        JBombardierSwingConsole.run(configuration);
    }

    @Test public void test_fail_embedded_agent_has_address() {
        testConfiguration1.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration1.setName("test");
        configuration.getTests().add(testConfiguration1);
        agentConfiguration1.setName(AgentConfiguration.embeddedName);
        agentConfiguration1.setAddress("123.123.123.123");
        configuration.getAgents().add(agentConfiguration1);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("You have specified an address for the embedded agent"));
        JBombardierSwingConsole.run(configuration);
    }
     
    @Test public void test_fail_agent_have_same_name() {
        testConfiguration1.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration1.setName("test");
        configuration.getTests().add(testConfiguration1);
        agentConfiguration1.setName("agent");
        agentConfiguration1.setAddress("123.123.123.123");
        agentConfiguration2.setName("agent");
        agentConfiguration2.setAddress("123.123.123.123");
        configuration.getAgents().add(agentConfiguration1);
        configuration.getAgents().add(agentConfiguration2);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("Some of your agents have the same name (agent)"));
        JBombardierSwingConsole.run(configuration);
    }

    @Test public void test_fail_agent_missing_address() {
        testConfiguration1.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration1.setName("test");
        configuration.getTests().add(testConfiguration1);
        agentConfiguration1.setName("agent");
        agentConfiguration1.setAddress(null);
        configuration.getAgents().add(agentConfiguration1);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("You haven't provided a valid address for agent 'agent'"));
        JBombardierSwingConsole.run(configuration);
    }
    
    @Test public void test_fail_agent_invalid_port() {
        testConfiguration1.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration1.setName("test");
        configuration.getTests().add(testConfiguration1);
        agentConfiguration1.setName("agent");
        agentConfiguration1.setAddress("123.123.123.123");
        agentConfiguration1.setPort(-1);
        configuration.getAgents().add(agentConfiguration1);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("You haven't provided a valid port for agent 'agent'"));
        JBombardierSwingConsole.run(configuration);
    }
}

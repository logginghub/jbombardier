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

package com.jbombardier.console;

import static org.hamcrest.Matchers.containsString;

import com.jbombardier.console.configuration.InteractiveConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.jbombardier.console.configuration.Agent;
import com.jbombardier.console.configuration.TestConfiguration;

public class TestConsoleStartFailures {

    private InteractiveConfiguration configuration = new InteractiveConfiguration();
    private TestConfiguration testConfiguration1 = new TestConfiguration();
    private TestConfiguration testConfiguration2 = new TestConfiguration();
    private Agent agent1 = new Agent();
    private Agent agent2 = new Agent();
    
    @Before public void disableVisualErrors() {
        configuration.setVisualErrorMessages(false);
    }
    
    
    @Rule public ExpectedException exception = ExpectedException.none();

    @Test public void test_fail_empty_configuration() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("No tests have been specified in your configuration"));
        SwingConsole.run(configuration);
    }
    
    @Test public void test_fail_no_agents_configuration() {
        configuration.getTests().add(testConfiguration1);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("No agents have been provided in your configuration"));
        SwingConsole.run(configuration);
    }
    
    @Test public void test_fail_test_has_null_classname() {        
        testConfiguration1.setClassname(null);
        configuration.getTests().add(testConfiguration1);
        configuration.getAgents().add(new Agent());
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("One of your tests has a null or empty classname"));
        SwingConsole.run(configuration);
    }
    
    @Test public void test_fail_test_has_empty_classname() {        
        testConfiguration1.setClassname("");
        configuration.getTests().add(testConfiguration1);
        configuration.getAgents().add(new Agent());
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("One of your tests has a null or empty classname"));
        SwingConsole.run(configuration);
    }
    
    @Test public void test_fail_class_not_found() {
        testConfiguration1.setClassname("class");
        testConfiguration1.setName("test");
        configuration.getTests().add(testConfiguration1);
        configuration.getAgents().add(new Agent());
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("One of your tests (test) specifies a class (class) that could not be loaded"));
        SwingConsole.run(configuration);
    }
    
    @Test public void test_fail_two_tests_with_same_name() {
        testConfiguration1.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration2.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration1.setName("test");
        testConfiguration2.setName("test");
        configuration.getTests().add(testConfiguration1);
        configuration.getTests().add(testConfiguration2);
        configuration.getAgents().add(new Agent());
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("Some of your tests have the same name"));
        SwingConsole.run(configuration);
    }

    @Test public void test_fail_embedded_agent_has_address() {
        testConfiguration1.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration1.setName("test");
        configuration.getTests().add(testConfiguration1);
        agent1.setName(Agent.embeddedName);
        agent1.setAddress("123.123.123.123");
        configuration.getAgents().add(agent1);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("You have specified an address for the embedded agent"));
        SwingConsole.run(configuration);
    }
     
    @Test public void test_fail_agent_have_same_name() {
        testConfiguration1.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration1.setName("test");
        configuration.getTests().add(testConfiguration1);
        agent1.setName("agent");
        agent1.setAddress("123.123.123.123");
        agent2.setName("agent");
        agent2.setAddress("123.123.123.123");
        configuration.getAgents().add(agent1);
        configuration.getAgents().add(agent2);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("Some of your agents have the same name (agent)"));
        SwingConsole.run(configuration);
    }

    @Test public void test_fail_agent_missing_address() {
        testConfiguration1.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration1.setName("test");
        configuration.getTests().add(testConfiguration1);
        agent1.setName("agent");
        agent1.setAddress(null);
        configuration.getAgents().add(agent1);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("You haven't provided a valid address for agent 'agent'"));
        SwingConsole.run(configuration);
    }
    
    @Test public void test_fail_agent_invalid_port() {
        testConfiguration1.setClassname("com.jbombardier.console.sample.SleepTest");
        testConfiguration1.setName("test");
        configuration.getTests().add(testConfiguration1);
        agent1.setName("agent");
        agent1.setAddress("123.123.123.123");
        agent1.setPort(-1);
        configuration.getAgents().add(agent1);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("You haven't provided a valid port for agent 'agent'"));
        SwingConsole.run(configuration);
    }
}

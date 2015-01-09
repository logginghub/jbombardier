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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.logginghub.utils.Is;
import com.logginghub.utils.StringUtils;

public class ConfigurationValidator {

    public static void validateConfiguration(JBombardierConfiguration configuration) {
        ConfigurationValidator validator = new ConfigurationValidator();
        validator.validate(configuration);
    }

    public void validate(JBombardierConfiguration configuration) {

        Is.notEmpty(configuration.getTests(), "No tests have been specified in your configuration; you must provide at least one test to start the console");
        Is.notEmpty(configuration.getAgents(),
                    "No agents have been provided in your configuration; you must have at least one agent (which could be the embedded agent if you want to run locally) in the configuration to start the console");

        Set<String> testnames = new HashSet<String>();
        List<TestConfiguration> tests = configuration.getTests();
        for (TestConfiguration testConfiguration : tests) {
            Is.notNullOrEmpty(testConfiguration.getClassname(), "One of your tests has a null or empty classname");
            Is.notIn(testConfiguration.getName(), testnames, "Some of your tests have the same name ({}); they must all be unique");
            testnames.add(testConfiguration.getName());

            String classname = testConfiguration.getClassname();
            try {
                Class.forName(classname);
            }
            catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(StringUtils.format("One of your tests ({}) specifies a class ({}) that could not be loaded by the console - please check your configuration and/or classpath for issues.", testConfiguration.getName(), classname ), e);
            }
        }

        Set<String> agentNames = new HashSet<String>();
        List<Agent> agents = configuration.getAgents();
        for (Agent agent : agents) {

            Is.notNullOrEmpty(agent.getName(), "One of your agents has a null or empty name");
            if (agent.getName().equals(Agent.embeddedName)) {
                Is.nullOrEmpty(agent.getAddress(),
                               "You have specified an address for the embedded agent - or maybe you have called a real agent 'embedded'? Either way, you can't do that - rename the agent or remove the address");
            }
            else {
                Is.notNullOrEmpty(agent.getAddress(), StringUtils.format("You haven't provided a valid address for agent '{}'", agent.getName()));
                Is.greaterThanZero(agent.getPort(), StringUtils.format("You haven't provided a valid port for agent '{}'", agent.getName()));
            }

            Is.notIn(agent.getName(), agentNames, "Some of your agents have the same name ({}); they must all be unique");
            agentNames.add(agent.getName());

        }

    }
}

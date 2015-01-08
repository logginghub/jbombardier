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

package com.jbombardier.examples;

import static com.jbombardier.console.configuration.ConfigurationBuilder.builder;
import static com.jbombardier.console.configuration.ConfigurationBuilder.test;

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;

public class LoggingTest extends PerformanceTestAdaptor {

    @Override public void runIteration(TestContext pti) throws Exception {
        pti.log("This is a jbombardier log message");
        com.logginghub.utils.logging.Logger.root().info("This is a vertex labs logging entry");
        
        java.util.logging.Logger.getLogger("root").info("This is a java.util.logging entry");
        org.apache.log4j.Logger.getRootLogger().info("This is a log4j entry");
    }

    public static void main(String[] args) {
        builder().addTest(test(LoggingTest.class).name("test").threads(1).targetRate(1))
                 .addAgent("Agent1", "localhost", 20001)
                 .addAgent("Agent2", "localhost", 20002)
                 .addAgent("Agent3", "localhost", 20003)
                 .loggingHubs("localhost:58770")
                 .loggingTypes("internal,vl,log4j,j.u.l")
                 .autostart(3)
                 .execute();
    }

}

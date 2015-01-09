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

import com.jbombardier.console.configuration.JBombardierConfiguration;
import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;
import com.jbombardier.console.headless.JBombardierHeadless;
import com.jbombardier.console.sample.old.BrokenRunTest;

import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.configurationBuilder;
import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.test;

public class MemoryTestBed {

    public static void main(String[] args) {

        int targetRate = 100000;

        JBombardierConfigurationBuilder builder = configurationBuilder().testName("SleepBenchmark").maximumResultsToStore(1000000);

        int tests = 20;
        for (int i = 0; i < tests; i++) {
            //            configurationBuilder.addTest(test(NoopTest.class).name("Noop-" + i).targetRate(targetRate).rateStep(1000000).threads(1));
            builder.addTest(test(BrokenRunTest.class).name("Broken-" + i)
                                                     .targetRate(targetRate)
                                                     .rateStep(1000000)
                                                     .threads(1));
        }

        JBombardierConfiguration configuration = builder.addAgent("agent1", "server", 20001, 20 * 1024 * 1024)
                                                        .warmupTime(1000)
                                                        .testDuration(600000)
                                                        .autostart(1)
                                                        .reportsFolder("target/reports")
                                                        .toConfiguration();

        JBombardierHeadless headless = new JBombardierHeadless();
        headless.setAgentsRequired(1);
        headless.setTimeToWaitForAgents(2000);

        try {
            JBombardierController run = headless.run(configuration);
            if (run.getModel().getFailureReason() != null) {
                System.err.println("Test failed : " + run.getModel().getFailureReason());
                System.exit(-1);
            } else {
                System.out.println("Test completed, exiting JVM...");
                System.exit(0);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

}

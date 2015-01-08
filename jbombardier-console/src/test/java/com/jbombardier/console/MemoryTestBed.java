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

import static com.jbombardier.console.configuration.ConfigurationBuilder.builder;
import static com.jbombardier.console.configuration.ConfigurationBuilder.test;

import com.jbombardier.console.headless.Headless;
import com.jbombardier.console.configuration.InteractiveConfiguration;
import com.jbombardier.console.configuration.ConfigurationBuilder;
import com.jbombardier.console.sample.old.BrokenRunTest;

public class MemoryTestBed {

    public static void main(String[] args) {

        int targetRate = 100000;

        ConfigurationBuilder builder = builder().testName("SleepBenchmark").maximumResultsToStore(1000000);

        int tests = 20;
        for (int i = 0; i < tests; i++) {
//            builder.addTest(test(NoopTest.class).name("Noop-" + i).targetRate(targetRate).rateStep(1000000).threads(1));
            builder.addTest(test(BrokenRunTest.class).name("Broken-" + i).targetRate(targetRate).rateStep(1000000).threads(1));
        }

        InteractiveConfiguration configuration = builder.addAgent("agent1", "server", 20001, 20 * 1024 * 1024).warmupTime(1000).testDuration(1000).autostart(1).toConfiguration();

        Headless headless = new Headless();
        headless.setWarmupTime(1000);
        headless.setSampleTime(600000);
        headless.setAgentsRequired(1);
        headless.setTimeToWaitForAgents(2000);
        headless.setReportFolder("target/reports");

        try {
            SwingConsoleController run = headless.run(configuration);
            if (run.getModel().getFailureReason() != null) {
                System.err.println("Test failed : " + run.getModel().getFailureReason());
                System.exit(-1);
            }
            else {
                System.out.println("Test completed, exiting JVM...");
                System.exit(0);
            }
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

}

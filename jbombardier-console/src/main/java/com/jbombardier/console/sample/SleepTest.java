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

package com.jbombardier.console.sample;

import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;
import com.logginghub.logging.utils.LoggingUtils;
import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;

/**
 * Benchmark test that sleeps for a duration specified by a parameter 'delay'.
 * 
 * @author James
 */
public class SleepTest extends PerformanceTestAdaptor {

    private int integerProperty;

    @Override public void setup(TestContext pti) throws Exception {
        super.setup(pti);
    }
    
    public void beforeIteration(TestContext pti) throws Exception {
        integerProperty = pti.getIntegerProperty("delay", 50);
        pti.sleep(pti.getIntegerProperty("beforeDelay", 0));
    }

    public void runIteration(TestContext pti) throws Exception {
        pti.sleep(integerProperty);
    }

    public static void main(String[] args) {
        LoggingUtils.setupRemoteVLLoggingFromSystemProperties();
        JBombardierConfigurationBuilder.configurationBuilder().testName("SleepBenchmark")
                 .addTest(JBombardierConfigurationBuilder.test(SleepTest.class).name("sleep 10@100").properties("delay=10").targetRate(100).rateStep(100).threads(1).threadStep(1).sla(10))
                 .addTest(JBombardierConfigurationBuilder.test(SleepTest.class).name("sleep 25@40").properties("delay=25").targetRate(40).rateStep(40).sla(25))
                 .addTest(JBombardierConfigurationBuilder.test(SleepTest.class).name("sleep 100@10").properties("delay=100").targetRate(10).rateStep(10).sla(90))
                 .addEmbeddedAgent()
                // .telemetryPort(12342)
                 //.resultRepository("www.vertexlabs.co.uk")
                 .warmupTime(100)
                 .testDuration(10000)
                 .autostart(1)
                 .execute();
    }
}

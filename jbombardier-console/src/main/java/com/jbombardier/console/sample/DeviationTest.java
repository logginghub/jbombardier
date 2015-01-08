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

import static com.jbombardier.console.configuration.ConfigurationBuilder.builder;
import static com.jbombardier.console.configuration.ConfigurationBuilder.test;

import com.logginghub.logging.utils.LoggingUtils;
import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;

/**
 * Benchmark test that verifies that the warmup timings are not included in the stddev calculations
 * 
 * @author James
 */
public class DeviationTest extends PerformanceTestAdaptor {

    private long start = 0;
    private boolean warmingup = true;
    private static long warmupTime = 5000;

    public void runIteration(TestContext pti) throws Exception {
        if (start == 0) {
            start = System.currentTimeMillis();
        }

        if (warmingup && System.currentTimeMillis() - start > (warmupTime/2)) {
            System.out.println("Warmup over");
            warmingup = false;
        }

        if (warmingup) {
            pti.sleep(20 + pti.random(100));
        }
        else {
            pti.sleep(20);
        }

    }

    public static void main(String[] args) {
        LoggingUtils.setupRemoteVLLoggingFromSystemProperties();
        builder().testName("DeviationTest")
                 .addTest(test(DeviationTest.class).name("sleep 100@10").targetRate(10).rateStep(10))
                 .warmupTime(warmupTime)
                 .testDuration(warmupTime)
                 .autostart(1)
                 .executeHeadless();
    }
}
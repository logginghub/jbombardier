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

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;
import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;

public class FailingOverallTransactionThresholdTest extends PerformanceTestAdaptor {

    private int i = 0;

    @Override public void runIteration(TestContext pti) throws Exception {
        i++;
        if (i % 10 == 0) {
            throw new RuntimeException("Deliberately thrown exception");
        }
    }

    public static void main(String[] args) {
        JBombardierConfigurationBuilder.configurationBuilder().testName("FailingOverallTransactionThreshold")
                 .addTest(JBombardierConfigurationBuilder.test(FailingOverallTransactionThresholdTest.class).name("Failure1").targetRate(100).rateStep(100))
                 .addTest(JBombardierConfigurationBuilder.test(FailingOverallTransactionThresholdTest.class).name("Failure2").targetRate(100).rateStep(100))
                 .addAgent(JBombardierConfigurationBuilder.embeddedAgent())
                 .autostart(1)
                 .failedTransactionCountFailureThreshold(30)
                 .executeHeadless();
    }

}

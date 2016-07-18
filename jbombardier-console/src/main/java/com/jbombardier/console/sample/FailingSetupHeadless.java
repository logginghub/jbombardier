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

package com.jbombardier.console.sample;

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;
import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;

public class FailingSetupHeadless extends PerformanceTestAdaptor {

    @Override public void setup(TestContext pti) throws Exception {
        throw new RuntimeException("Deliberately thrown exception in setup");
    }

    public void runIteration(TestContext pti) throws Exception {}

    public static void main(String[] args) {
        JBombardierConfigurationBuilder.configurationBuilder().testName("FailingSetupHeadless")
                 .addTest(JBombardierConfigurationBuilder.test(FailingSetupHeadless.class).name("Failure1").targetRate(100).rateStep(100))
                 .addTest(JBombardierConfigurationBuilder.test(FailingSetupHeadless.class).name("Failure2").targetRate(100).rateStep(100))
                 .addAgent(JBombardierConfigurationBuilder.embeddedAgent())
                 .autostart(1)
                 .executeHeadless();
    }
}

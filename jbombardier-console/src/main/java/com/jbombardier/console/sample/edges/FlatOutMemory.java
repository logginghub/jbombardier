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

package com.jbombardier.console.sample.edges;

import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;
import com.jbombardier.console.sample.old.NoopTest;

public class FlatOutMemory {
    public static void main(String[] args) {
        JBombardierConfigurationBuilder.configurationBuilder().testName("FlatOutMemoryTest")
        // .addTest(test(NoopTest.class).name("NoOp1").targetRate(700000).rateStep(700000))
                 .addTest(JBombardierConfigurationBuilder.test(NoopTest.class).name("NoOp1").targetRate(-1).rateStep(700000).recordAllValues(true))
                  .addEmbeddedAgent(10 * 1024 * 1024)
//                 .addEmbeddedAgent()
                 .autostart(1)
                 .maximumResultsToStore(100)
                 .warmupTime(2000)
                 .testDuration(5000000)
                 .execute();
    }
}

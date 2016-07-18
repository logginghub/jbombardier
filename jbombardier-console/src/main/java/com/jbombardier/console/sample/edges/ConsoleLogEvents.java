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

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;
import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;

public class ConsoleLogEvents extends PerformanceTestAdaptor {

    @Override public void runIteration(TestContext pti) throws Exception {
        pti.log("This is a log message with quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text quite a bit of text asdfasdf asdfa dfasdf adfasdf asdf asdfasdfasdfasdfasdfas");
    }

    public static void main(String[] args) {
        JBombardierConfigurationBuilder.configurationBuilder().testName("ConsoleLogEvents")
                 .addTest(JBombardierConfigurationBuilder.test(ConsoleLogEvents.class).name("Logging").targetRate(20).rateStep(10).recordAllValues(false))
                 .addEmbeddedAgent(10 * 1024 * 1024, 30000)
                 .maximumConsoleEntries(5000)
                 .autostart(1)
                 .warmupTime(2000)
                 .testDuration(5000000)
                 .execute();
    }
}

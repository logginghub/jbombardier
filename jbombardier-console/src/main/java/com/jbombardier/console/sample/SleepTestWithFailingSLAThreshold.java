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
import com.jbombardier.console.model.TransactionResultModel.TransactionTimeThresholdMode;

public class SleepTestWithFailingSLAThreshold {

    public static void main(String[] args) {
        JBombardierConfigurationBuilder.configurationBuilder().testName("SleepBenchmark-withfailure")
                 .addTest(JBombardierConfigurationBuilder.test(SleepTest.class).name("sleep 10@100").properties("delay=10").targetRate(100).rateStep(100))
                 .addTest(JBombardierConfigurationBuilder.test(SleepTest.class).name("sleep 25@40")
                                               .properties("delay=25")
                                               .targetRate(40)
                                               .rateStep(40)
                                               .sla(25)
                                               .failureThreshold(9)
                                               .failureThresholdMode(TransactionTimeThresholdMode.Mean)
                                               .failureThresholdResultCountMinimum(20))
                 .addTest(JBombardierConfigurationBuilder.test(SleepTest.class).name("sleep 100@10").properties("delay=100").targetRate(10).rateStep(10))
                 .addAgent(JBombardierConfigurationBuilder.embeddedAgent())
                 .autostart(1)
                 .warmUpTime(2000)
                 .testDuration(5000000)
                 .executeHeadless();
    }
}

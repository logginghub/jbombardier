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

package com.jbombardier.console.sample.verification;

import com.jbombardier.console.sample.SleepTest;

import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.configurationBuilder;
import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.phase;
import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.test;

/**
 * Created by james on 22/01/15.
 */
public class ConsoleSleepTestWithPhases {
    public static void main(String[] args) {

        configurationBuilder().testName("Phases Sample")
                              .addEmbeddedAgent()
                              .addPhase(phase("Phase 1").duration("50 seconds").addTest(test(SleepTest.class).name("sleep 10@100")
                                                                                      .properties("delay=10")
                                                                                      .targetRate(100)
                                                                                      .rateStep(100)
                                                                                      .threads(10)
                                                                                      .threadStep(10)
                                                                                      .sla(10)))
                              .addPhase(phase("Phase 2").duration("60 seconds").addTest(test(SleepTest.class).name("sleep 25@40")
                                                                                      .properties("delay=25")
                                                                                      .targetRate(40)
                                                                                      .rateStep(40)
                                                                                      .sla(25)))
                              .addPhase(phase("Phase 3").duration("70 seconds").addTest(test(SleepTest.class).name("sleep 100@10")
                                                                                      .properties("delay=100")
                                                                                      .targetRate(10)
                                                                                      .rateStep(10)
                                                                                      .sla(90)))
                              .testDuration(10000)
                              .autostart(1)
                              .execute();
    }
}

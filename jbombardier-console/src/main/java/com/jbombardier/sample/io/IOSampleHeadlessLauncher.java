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

package com.jbombardier.sample.io;

import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.configurationBuilder;
import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.phase;
import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.test;

/**
 * Created by james on 30/01/15.
 */
public class IOSampleHeadlessLauncher {
    public static void main(String[] args) {

        String warmupDuration = "1 seconds";
        String phaseDuration = "2 seconds";

        configurationBuilder().testName("IO Performance Sample")
                              .addEmbeddedAgent()
                              .addPhase(phase("Phase 1").duration(phaseDuration)
                                                        .warmup(warmupDuration)
                                                        .addTest(test(DiskWritePerformance.class).name("File output stream")
                                                                                                 .targetRate(-1)
                                                                                                 .threads(1)))
                              .addPhase(phase("Phase 2").duration(phaseDuration).warmup(warmupDuration).addTest(test(DiskWritePerformance.class).name("File output stream")
                                                                                                                                                .targetRate(-1)
                                                                                                                                                .threads(2)
                                                                                                                                                .threadStep(2)))
                              .addPhase(phase("Phase 3").duration(phaseDuration).warmup(warmupDuration).addTest(test(DiskWritePerformance.class).name("File output stream")
                                                                                                                                                .targetRate(-1)
                                                                                                                                                .threads(5)
                                                                                                                                                .threadStep(5)))
                              .addPhase(phase("Phase 4").duration(phaseDuration).warmup(warmupDuration).addTest(test(DiskWritePerformance.class).name("File output stream")
                                                                                                                                                .targetRate(-1)
                                                                                                                                                .threads(10)
                                                                                                                                                .threadStep(10)))
                              .autostart(1)
                              .openReport(true)
                              .executeHeadless();

    }
}

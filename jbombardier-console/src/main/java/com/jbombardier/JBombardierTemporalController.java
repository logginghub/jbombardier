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

package com.jbombardier;

import com.jbombardier.console.JBombardierController;
import com.jbombardier.console.model.PhaseModel;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by james on 09/01/15.
 */
public class JBombardierTemporalController implements Asynchronous {

    private static final Logger logger = Logger.getLoggerFor(JBombardierTemporalController.class);
    private final JBombardierController controller;
    //    private final JBombardierConfiguration configuration;
    private TimeProvider timeProvider = new SystemTimeProvider();
    private WorkerThread workerThread;
    private long baselineTime;
    private long endTime;
    private long warmupEnd;

    private PhaseModel phase;
    private boolean phaseHasWarmup;

    public JBombardierTemporalController(JBombardierController controller) {
        this.controller = controller;
        //        this.configuration = configuration;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override public void start() {
        stop();

        logger.info("Temporal controller starting...");

        controller.publishTestInstructions();

        phase = controller.getNextPhase();
        initialisePhase();
        controller.startNextPhase();
        workerThread = WorkerThread.every("JBombardier-TemporalController", 100, TimeUnit.MILLISECONDS, new Runnable() {
            @Override public void run() {
                progressTest();
            }
        });
    }

    private void initialisePhase() {
        long warmup = phase.getWarmupDuration().get();

        phaseHasWarmup = warmup != PhaseModel.NO_WARMUP;

        long duration = phase.getPhaseDuration().get();

        if (phaseHasWarmup) {
            logger.info("Next phase is '{}' - warmup {} and duration {}",
                    phase.getPhaseName().get(),
                    TimeUtils.formatIntervalMilliseconds(warmup),
                    TimeUtils.formatIntervalMilliseconds(duration));
        } else {
            logger.info("Next phase is '{}' - there is no warmup and duration {}",
                    phase.getPhaseName().get(),
                    TimeUtils.formatIntervalMilliseconds(duration));
        }

        baselineTime = timeProvider.getTime();

        if (phaseHasWarmup) {
            warmupEnd = baselineTime + warmup;
            endTime = warmupEnd + duration;
        } else {
            endTime = baselineTime + duration;
        }

        if (phaseHasWarmup) {
            logger.info("Time is now '{}' - warmup will end at '{}' and the test will end at'{}'",
                    Logger.toLocalDateString(baselineTime),
                    Logger.toLocalDateString(warmupEnd),
                    Logger.toLocalDateString(endTime));
        } else {
            logger.info("Time is now '{}' - there is no warmup and the test will end at'{}'", Logger.toLocalDateString(
                    baselineTime), Logger.toLocalDateString(endTime));
        }
    }

    private void progressTest() {

        long now = timeProvider.getTime();

        if (phaseHasWarmup) {
            if (now >= warmupEnd) {
                logger.info("Warmup for phase '{}' is complete, clearing stats", phase.getPhaseName().get());
                controller.resetStats();
                phaseHasWarmup = false;
            }
        }

        if (now >= endTime) {

            if (controller.hasNextPhase()) {
                logger.info("Phase '{}' is complete, recording stats and moving to next phase",
                        phase.getPhaseName().get());

                controller.stopPhase();
                controller.resetStats();

                phase = controller.getNextPhase();
                initialisePhase();

                controller.startNextPhase();


            } else {
                logger.info("Phase '{}' is complete - this is the last phease, finishing the test",
                        phase.getPhaseName().get());

                controller.stopPhase();
                controller.endTestNormally();

                logger.info("Temporal controller test execution complete - ending control thread");
                workerThread.dontRunAgain();
            }

        } else {
            phase.getPhaseRemainingTime().set(endTime - now);
        }


    }

    //    private void checkTimes() {
    //        long currentTime = timeProvider.getTime();
    //
    //        if (controller.getState() == JBombardierController.State.Warmup) {
    //            if (currentTime >= warmupEnd) {
    //                logger.info("Warmup time has ended, notifying controller...");
    //                controller.endWarmupAndStartMainTest();
    //            }
    //        } else if (controller.getState() == JBombardierController.State.TestRunning) {
    //            if (currentTime >= endTime) {
    //                logger.info("Test duration has ended, notifying controller...");
    //                controller.endTestNormally();
    //
    //                logger.info("Temporal controller is done now, this is the last iteration");
    //                workerThread.dontRunAgain();
    //            }
    //        }
    //    }

    @Override public void stop() {
        if (workerThread != null) {
            workerThread.stop();
            workerThread = null;
        }
    }


}

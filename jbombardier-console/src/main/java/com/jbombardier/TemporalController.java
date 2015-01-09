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
import com.jbombardier.console.configuration.JBombardierConfiguration;
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
public class TemporalController implements Asynchronous {

    private static final Logger logger = Logger.getLoggerFor(TemporalController.class);
    private final JBombardierController controller;
    private final JBombardierConfiguration configuration;
    private TimeProvider timeProvider = new SystemTimeProvider();
    private WorkerThread workerThread;
    private long baselineTime;
    private long endTime;
    private long warmupEnd;

    public TemporalController(JBombardierController controller, JBombardierConfiguration configuration) {
        this.controller = controller;
        this.configuration = configuration;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public void startTest() {
        controller.publishTestInstructionsAndStartRunning();
        baselineTime = timeProvider.getTime();
        start();
    }

    @Override public void start() {
        stop();

        long tempEndTime = 0;
        long tempWarmUpEnd = 0;

        tempWarmUpEnd = baselineTime + TimeUtils.parseInterval(configuration.getWarmupTime());

        long testDuration = TimeUtils.parseInterval(configuration.getDuration());
        if (tempWarmUpEnd == 0) {
            tempEndTime = baselineTime + testDuration;
        } else {
            tempEndTime = tempWarmUpEnd + testDuration;
        }

        endTime = tempEndTime;
        warmupEnd = tempWarmUpEnd;

        workerThread = WorkerThread.every("JBombardier-TemporalController", 100, TimeUnit.MILLISECONDS, new Runnable() {
            @Override public void run() {
                checkTimes();
            }
        });

    }

    private void checkTimes() {
        long currentTime = timeProvider.getTime();

        if (controller.getState() == JBombardierController.State.Warmup) {
            if (currentTime >= warmupEnd) {
                logger.info("Warmup time has ended, notifying controller...");
                controller.endWarmupAndStartMainTest();
            }
        } else if (controller.getState() == JBombardierController.State.TestRunning) {
            if (currentTime >= endTime) {
                logger.info("Test duration has ended, notifying controller...");
                controller.endTestNormally();

                logger.info("Temporal controller is done now, this is the last iteration");
                workerThread.dontRunAgain();
            }
        }
    }

    @Override public void stop() {
        if (workerThread != null) {
            workerThread.stop();
            workerThread = null;
        }
    }


}

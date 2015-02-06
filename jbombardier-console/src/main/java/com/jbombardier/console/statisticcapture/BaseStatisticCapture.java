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

package com.jbombardier.console.statisticcapture;

import com.jbombardier.common.StatisticProvider;
import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.WorkerThread;

import java.util.concurrent.TimeUnit;

/**
 * Created by james on 13/11/14.
 */
public abstract class BaseStatisticCapture extends Multiplexer<CapturedStatistic> implements StatisticProvider {

    private TimeProvider timeProvider = new SystemTimeProvider();
    private WorkerThread thread;
    private long delay = 1000;

    @Override public void start() {
        stop();

        thread = WorkerThread.every(this.getClass().getSimpleName() + "-statisticsCaptureWorker",
                                    delay,
                                    TimeUnit.MILLISECONDS,
                                    new Runnable() {
                                        @Override public void run() {
                                            doCapture();
                                        }
                                    });

    }

    protected abstract void doCapture();

    @Override public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }


    public void setDelay(long delay) {
        this.delay = delay;
    }
}

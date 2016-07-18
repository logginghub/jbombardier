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

package com.jbombardier.agent;

import com.jbombardier.common.PerformanceTest;
import com.jbombardier.common.TestContext;
import com.jbombardier.common.TestContextException;
import com.jbombardier.common.TestFailedException;
import com.logginghub.utils.IteratingRunnable;
import com.logginghub.utils.logging.Logger;

import java.util.List;

/**
 * Responsible for maintaining the core test iteration loop (all interactions with the actual test) and collecting the
 * raw transaction times. Doesn't have anything to do with controller the execution rates or actually dealing with the
 * results.
 *
 * @author James
 */
public class TestRunner implements IteratingRunnable {

    private static final Logger logger = Logger.getLoggerFor(TestRunner.class);
    private final PerformanceTest test;
    private final TestContext testContext;
    private final List<ThreadController.ThreadControllerListener> listeners;
    private final String threadName;
    private final TestStats testStats;
    private RateControlledIteratingWorkerThread workerThread;

    public TestRunner(PerformanceTest test,
                      TestContext testContext,
                      TestStats testStats,
                      String threadName,
                      List<ThreadController.ThreadControllerListener> listeners) {
        this.test = test;
        this.testContext = testContext;
        this.testStats = testStats;
        this.threadName = threadName;
        this.listeners = listeners;

    }

    public void iterate() {

        boolean success = true;
        Throwable throwable = null;

        if (logger.willLog(Logger.fine)) {
            logger.fine("Iterating test thread");
        }

        long total = 0;
        long time = 0;
        long end = 0;
        long elapsed = 0;
        try {
            total = System.nanoTime();
            test.beforeIteration(testContext);
            time = System.nanoTime();
            test.runIteration(testContext);
            end = System.nanoTime();
            elapsed = end - time;
            test.afterIteration(testContext, elapsed);
            success = true;
            throwable = null;
        } catch (TestFailedException tfe) {
            // The user code has called the fail(...) method on the test context
            for (ThreadController.ThreadControllerListener threadControllerListener : listeners) {
                threadControllerListener.onException("TestFailedException|" + tfe.getMessage(), threadName, tfe);
            }
            success = false;
            throwable = tfe;
        } catch (TestContextException tce) {
            // Hmm something has gone bang in the test content, its unlikely
            // we'll be able to get over this if its a transaction mismatch or
            // bad property
            for (ThreadController.ThreadControllerListener threadControllerListener : listeners) {
                threadControllerListener.onException("Test context exception", threadName, tce);
            }
            success = false;
            throwable = tce;
        } catch (Exception e) {
            boolean okToThrow;
            logger.warn(e, "Test runner iteration failed");

            if (workerThread != null && workerThread.isRunning()) {
                if (e.getMessage() != null && e.getMessage().equals("Interupted during sleep")) {
                    // TODO : this is a hack to stop the messages that wake the
                    // thread up to control the execution rate from being sent
                    // out as a test failure
                    okToThrow = false;
                } else {
                    okToThrow = true;
                }
            } else {
                okToThrow = true;
            }

            if (okToThrow) {
                for (ThreadController.ThreadControllerListener threadControllerListener : listeners) {
                    threadControllerListener.onException("Runtime exception", threadName, e);
                }
                success = false;
                throwable = e;
            }
        } finally {
            testContext.forceTransactionEnd(success, throwable);
        }

        total = end - total;

        if (success) {
            testStats.onSuccess(elapsed, total);
        } else {
            testStats.onFailed(elapsed, total);
        }

    }

    public void beforeFirst() {
        try {
            test.setup(testContext);
        } catch (Exception e) {
            for (ThreadController.ThreadControllerListener threadControllerListener : listeners) {
                threadControllerListener.onException("Setup method threw an exception", threadName, e);
            }
        }
    }

    public void afterLast() {
        try {
            test.teardown(testContext);
        } catch (Exception e) {
            for (ThreadController.ThreadControllerListener threadControllerListener : listeners) {
                threadControllerListener.onException("Teardown method threw an exception", threadName, e);
            }
        }
    }

    public void setWorkerThread(RateControlledIteratingWorkerThread workerThread) {
        this.workerThread = workerThread;
    }
};

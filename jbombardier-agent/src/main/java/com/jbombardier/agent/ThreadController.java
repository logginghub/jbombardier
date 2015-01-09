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

package com.jbombardier.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.jbombardier.common.BasicTestStats;
import com.jbombardier.common.BasicTestStatsResultStrategy;
import com.jbombardier.common.PerformanceTest;
import com.jbombardier.common.ReflectionTestFactory;
import com.jbombardier.common.TestContext;
import com.jbombardier.common.TestFactory;
import com.jbombardier.common.TestInstruction;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

/**
 * Provides all the various services to ramp up/down threads based on the values
 * specified in the test details. All of the values, apart from the
 * IteratingRunnable, can be changed in real-time and the controller thread will
 * react the next time it wakes up (after the step interval.)
 * 
 * @author James
 * 
 */
public class ThreadController extends WorkerThread {

    
    private static final Logger logger = Logger.getLoggerFor(ThreadController.class);
    
    // private final IteratingRunnable runnable;
    private volatile int targetThreads;
    private volatile int threadRampupStepSize;
    private List<RateControlledIteratingWorkerThread> threads = new ArrayList<RateControlledIteratingWorkerThread>();
    private List<ThreadControllerListener> listeners = new CopyOnWriteArrayList<ThreadControllerListener>();
    private volatile long threadRampupStepTime;
    private volatile double targetRate;
    private volatile double rateStep;
    private volatile long rateStepTime;
    private final String testName;
    private BasicTestStats basicTestStats;

    // private final Class<? extends PerformanceTest> testClass;
    private TestFactory testFactory;

    private final TestContextFactory testContextFactory;
    private BasicTestStatsResultStrategy resultStrategy;
    private ResultAggregator resultAggregator;
    private final TestInstruction testInstruction;
    private double transactionRateModifier;

    public static interface ThreadControllerListener {

        void onThreadStarted(int threads);

        void onThreadKilled(int threads);

        void onException(String source, String threadName, Throwable throwable);
    }

    public ThreadController(String testName, Class<? extends PerformanceTest> testClass, TestContextFactory testContext, TestInstruction testInstruction) {
        this(testName, new ReflectionTestFactory(testClass), testContext, testInstruction);
    }

    public ThreadController(String testName, TestFactory factory, TestContextFactory testContext, TestInstruction testInstruction) {
        super("ThreadController(" + testName + ")");

        this.testName = testName;
        this.testFactory = factory;

        this.testContextFactory = testContext;
        this.testInstruction = testInstruction;
        this.targetThreads = testInstruction.getTargetThreads();
        this.threadRampupStepSize = testInstruction.getThreadRampupStep();
        this.threadRampupStepTime = testInstruction.getThreadRampupTime();
        this.targetRate = testInstruction.getTargetRate();
        this.rateStep = testInstruction.getRateStep();
        this.rateStepTime = testInstruction.getRateStepTime();
        this.transactionRateModifier = testInstruction.getTransactionRateModifier();

        // Get WorkerThread to do the delay work for us
        setIterationDelay(threadRampupStepTime);
    }

    @Override public String toString() {
        return "ThreadController [testName=" + testName + "]";
    }

    private TestRunner buildRunnable(String testName, final String threadName, PerformanceTest test, final TestContextFactory contextFactory) {
        final TestContext testContext = contextFactory.createTestContext(testName, threadName);

        TestStats stats = new TestStats() {
            public void onSuccess(long transactionElapsed, long totalElapsed) {
                basicTestStats.totalDurationSuccess += transactionElapsed;
                basicTestStats.totalDurationTotalSuccess += totalElapsed;
                basicTestStats.transactionsSuccess++;
                if (testInstruction.getRecordAllValues()) {
                    // TODO : this aren't atomic, and we dont want to slow down the
                    // threads by forcing sequential updates, so we need to thread
                    // local these guys at some point
                    synchronized (basicTestStats.successResults) {
                        basicTestStats.successResults.add(transactionElapsed);
                    }
                }
            }

            public void onFailed(long transactionElapsed, long totalElapsed) {
                basicTestStats.totalDurationFailed += transactionElapsed;
                basicTestStats.transactionsFailed++;
                if (testInstruction.getRecordAllValues()) {
                    // TODO : this will be a killer performance hit, see todo in the success case
                    synchronized (basicTestStats.failResults) {
                        basicTestStats.failResults.add(transactionElapsed);
                    }
                }
            }
        };

        TestRunner runner = new TestRunner(test, testContext, stats, threadName, listeners);

        return runner;

    }

    public void setThreadRampupStepSize(int threadRampupStepSize) {
        this.threadRampupStepSize = threadRampupStepSize;
    }

    public void setThreadRampupStepTime(long threadRampupStepTime) {
        this.threadRampupStepTime = threadRampupStepTime;
        setIterationDelay(threadRampupStepTime);
    }

    public void setRateStep(double rateStep) {
        this.rateStep = rateStep;

        synchronized (threads) {
            for (RateControlledIteratingWorkerThread rateControlledIteratingWorkerThread : threads) {
                rateControlledIteratingWorkerThread.setStep(rateStep);
            }
        }
    }

    public void setRateStepTime(long rateStepTime) {
        this.rateStepTime = rateStepTime;

        synchronized (threads) {
            for (RateControlledIteratingWorkerThread rateControlledIteratingWorkerThread : threads) {
                rateControlledIteratingWorkerThread.setStepTime(rateStepTime);
            }
        }
    }

    public void setTargetThreads(int targetThreads) {
        this.targetThreads = targetThreads;
    }

    public void setTargetRate(double targetRate) {
        this.targetRate = targetRate;

        synchronized (threads) {
            for (RateControlledIteratingWorkerThread rateControlledIteratingWorkerThread : threads) {
                rateControlledIteratingWorkerThread.setTargetRate(targetRate);
            }
        }
    }

    public void addThreadControllerListener(ThreadControllerListener listener) {
        listeners.add(listener);
    }

    public void removeThreadControllerListener(ThreadControllerListener listener) {
        listeners.remove(listener);
    }

    private void fireThreadStarted() {
        for (ThreadControllerListener threadControllerListener : listeners) {
            threadControllerListener.onThreadStarted(1);
        }
    }

    private void fireThreadKilled() {
        for (ThreadControllerListener threadControllerListener : listeners) {
            threadControllerListener.onThreadKilled(1);
        }
    }

    public void stopAllChildren() {
        synchronized (threads) {
            for (WorkerThread workerThread : threads) {
                stopThread(workerThread);
            }
        }
    }

    @Override protected void onRun() throws Throwable {

        synchronized (threads) {
            int deltaThreads = targetThreads - threads.size();

            if (deltaThreads > 0) {
                logger.info("ThreadController for " + testName + " is iterating. Delta threads is " + deltaThreads);
                int threadsToStart = Math.min(deltaThreads, threadRampupStepSize);
                for (int i = 0; i < threadsToStart; i++) {
                    logger.info("Starting a new instance of " + testName + " target rate is " + targetRate + ", rate step is " + rateStep + " and rateStepTime " + rateStepTime);

                    String threadName = testName + "-thread-" + threads.size();

                    PerformanceTest test = testFactory.createTest();

                    TestRunner runnable = buildRunnable(testName, threadName, test, testContextFactory);

                    RateControlledIteratingWorkerThread workerThread = new RateControlledIteratingWorkerThread(threadName, runnable, 1, targetRate, rateStep, rateStepTime, transactionRateModifier) {
                        protected void beforeStart() {
                            super.beforeStart();
                            fireThreadStarted();
                        };
                    };

                    // This is getting incestuous, but the test runner needs to
                    // know if the thread has been stopped to block exceptions has it dies
                    runnable.setWorkerThread(workerThread);
                    workerThread.setThreadContextClassLoader(test.getClass().getClassLoader());
                    workerThread.start();
                    threads.add(workerThread);
                }
            }
            else {
                int threadsToStop = Math.abs(deltaThreads);
                for (int i = 0; i < threadsToStop; i++) {
                    WorkerThread workerThread = threads.remove(threads.size() - 1);
                    logger.info("Stopping instance of " + testName);
                    stopThread(workerThread);
                }
            }
        }
    }

    private void stopThread(WorkerThread workerThread) {
        workerThread.stop(new Runnable() {
            public void run() {
                fireThreadKilled();
            }
        });
    }

    public String getTestName() {
        return testName;
    }

    public int getThreadCount() {
        return threads.size();
    }

    public void setStats(BasicTestStats basicTestStats) {
        this.basicTestStats = basicTestStats;
    }

    public BasicTestStats getBasicTestStats() {
        return basicTestStats;
    }

    public void setResultsAggregator(ResultAggregator resultAggregator) {
        this.resultAggregator = resultAggregator;
    }

    public ResultAggregator getResultAggregator() {
        return resultAggregator;
    }

    public void setTransactionRateModifier(double transactionRateModifier) {
        this.transactionRateModifier = transactionRateModifier;

        synchronized (threads) {
            for (RateControlledIteratingWorkerThread rateControlledIteratingWorkerThread : threads) {
                rateControlledIteratingWorkerThread.setTransactionRateModifier(transactionRateModifier);
            }
        }

    }

    public void clearListeners() {
        listeners.clear();
    }

    // public void setResultsStrategy(BasicTestStatsResultStrategy
    // resultStrategy) {
    // this.resultStrategy = resultStrategy;
    // }
    //
    // public BasicTestStatsResultStrategy getResultStrategy() {
    // return resultStrategy;
    // }
}

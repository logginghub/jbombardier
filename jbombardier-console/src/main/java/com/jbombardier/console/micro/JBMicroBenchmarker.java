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

package com.jbombardier.console.micro;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.logginghub.utils.Out;
import com.logginghub.utils.Pair;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.swing.TestFrame;
import com.jbombardier.agent.RateControlledIteratingWorkerThread;
import com.jbombardier.agent.SimpleTestContextFactory;
import com.jbombardier.agent.TestRunner;
import com.jbombardier.agent.TestStats;
import com.jbombardier.agent.ThreadController.ThreadControllerListener;
import com.jbombardier.common.BasicTestStats;
import com.jbombardier.common.PerformanceTest;
import com.jbombardier.common.TestContext;
import com.jbombardier.common.TestFactory;

public class JBMicroBenchmarker {

    private List<Pair<String, TestFactory>> testFactories = new ArrayList<Pair<String, TestFactory>>();
    private MicroBenchmarkModel model = new MicroBenchmarkModel();

    public void addTest(String name, TestFactory testFactory) {
        testFactories.add(new Pair<String, TestFactory>(name, testFactory));
    }

    public void start() {

        final MicroBenchmarkPanel panel = new MicroBenchmarkPanel();
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                TestFrame.show("JBombardierMicrobenchmark", panel, 1024, 768);
            }
        });
        panel.bind(model);
        
        TimerUtils.everySecond("Stats timer", new Runnable() {
            @Override public void run() {
                updateStats();
            }
        });

        SimpleTestContextFactory simpleTestContextFactory = new SimpleTestContextFactory();

        int threadCount = 0;

        for (Pair<String, TestFactory> testFactory : testFactories) {

            List<ThreadControllerListener> listeners = new ArrayList<ThreadControllerListener>();
            listeners.add(new ThreadControllerListener() {
                
                @Override public void onThreadStarted(int threads) {}
                
                @Override public void onThreadKilled(int threads) {}
                
                @Override public void onException(String source, String threadName, Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
            String threadName = "thread-" + (threadCount++);
            String testName = testFactory.getA();

            MicroBenchmarkTestModel testModel = new MicroBenchmarkTestModel();
            testModel.getName().set(testName);
            model.add(testModel);
            
            final BasicTestStats basicTestStats = new BasicTestStats(testName);
            testModel.getStats().set(basicTestStats);

            TestStats testStats = new TestStats() {
                public void onSuccess(long transactionElapsed, long totalElapsed) {
                    basicTestStats.totalDurationSuccess += transactionElapsed;
                    basicTestStats.totalDurationTotalSuccess += totalElapsed;
                    basicTestStats.transactionsSuccess++;
                }

                public void onFailed(long transactionElapsed, long totalElapsed) {
                    basicTestStats.totalDurationFailed += transactionElapsed;
                    basicTestStats.transactionsFailed++;
                }
            };

            
            TestContext testContext = simpleTestContextFactory.createTestContext(testName, threadName);

            PerformanceTest test = testFactory.getB().createTest();
            TestRunner runner = new TestRunner(test, testContext, testStats, threadName, listeners);

            RateControlledIteratingWorkerThread workerThread = new RateControlledIteratingWorkerThread(threadName, runner, -1, -1, 1, 1, 1);

            workerThread.setThreadContextClassLoader(test.getClass().getClassLoader());
            workerThread.start();

        }

    }

    private void updateStats() {

        ObservableList<MicroBenchmarkTestModel> models = model.getModels();
        for (MicroBenchmarkTestModel model : models) {
            BasicTestStats value = model.getStats().get();
            model.getStats().set(value);
            Out.out("{} - {} tps {} mus/t", model.getName().get(), value.transactionsSuccess, (value.totalDurationSuccess*1e-3d) / value.transactionsSuccess);
            value.reset();
        }
    }
}

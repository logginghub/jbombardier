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

import com.jbombardier.common.*;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.MutableLong;
import com.logginghub.utils.ThreadUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestRunnerTest {

    @Test public void test_after_iteration() throws InterruptedException {

        final Bucket<Throwable> bucket = new Bucket<Throwable>();

        List<ThreadController.ThreadControllerListener> listeners = new ArrayList<ThreadController.ThreadControllerListener>();
        ThreadController.ThreadControllerListener listener = new ThreadController.ThreadControllerListener() {

            public void onThreadStarted(int threads) {
            }

            public void onThreadKilled(int threads) {
            }

            public void onException(String source, String threadName, Throwable throwable) {
                bucket.add(throwable);
            }
        };

        listeners.add(listener);


        TestStats stats = new TestStats() {
            public void onSuccess(long transactionElapsed, long totalElapsed) {
            }

            public void onFailed(long transactionElapsed, long totalElapsed) {
            }
        };

        LoggingStrategy loggingStrategy = new MinlogLoggingStrategy();
        BasicTestStatsResultStrategy resultStrategy = new BasicTestStatsResultStrategy("name", true);
        PropertiesStrategy propertiesStrategy = new DefaultPropertiesProvider();

        TestContext context = new SimpleTestContext("testName", resultStrategy, propertiesStrategy, loggingStrategy);

        final CountDownLatch setupLatch = new CountDownLatch(1);
        final CountDownLatch beforeLatch = new CountDownLatch(1);
        final CountDownLatch runLatch = new CountDownLatch(1);
        final CountDownLatch afterLatch = new CountDownLatch(1);
        final CountDownLatch teardownLatch = new CountDownLatch(1);

        final MutableLong timeHolder = new MutableLong(-1);

        PerformanceTest test = new PerformanceTest() {
            @Override public void setup(TestContext pti) throws Exception {
                setupLatch.countDown();
            }

            @Override public void beforeIteration(TestContext pti) throws Exception {
                beforeLatch.countDown();
            }

            @Override public void runIteration(TestContext pti) throws Exception {
                ThreadUtils.sleep(50);
                runLatch.countDown();
            }

            @Override public void afterIteration(TestContext pti, long nanos) throws Exception {
                timeHolder.value = nanos;
                afterLatch.countDown();
            }

            @Override public void teardown(TestContext pti) throws Exception {
                teardownLatch.countDown();
            }
        };

        TestRunner runner = new TestRunner(test, context, stats, "threadName", listeners);

        runner.beforeFirst();

        assertThat(setupLatch.await(1, TimeUnit.SECONDS), is(true));

        runner.iterate();

        assertThat(beforeLatch.await(1, TimeUnit.SECONDS), is(true));
        assertThat(runLatch.await(1, TimeUnit.SECONDS), is(true));
        assertThat(afterLatch.await(1, TimeUnit.SECONDS), is(true));

        assertThat(timeHolder.value, is(both(greaterThanOrEqualTo(50L * 1000000)).and(lessThan(60L * 1000000))));

        runner.afterLast();

        assertThat(teardownLatch.await(1, TimeUnit.SECONDS), is(true));

    }

    /**
     * [95] jbombardier doesn't abandon the current transaction when an exception is fired
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored") @Test public void testExceptionClearsTransaction() {

        final Bucket<Throwable> bucket = new Bucket<Throwable>();

        List<ThreadController.ThreadControllerListener> listeners = new ArrayList<ThreadController.ThreadControllerListener>();
        ThreadController.ThreadControllerListener listener = new ThreadController.ThreadControllerListener() {

            public void onThreadStarted(int threads) {
            }

            public void onThreadKilled(int threads) {
            }

            public void onException(String source, String threadName, Throwable throwable) {
                bucket.add(throwable);
            }
        };

        listeners.add(listener);


        TestStats stats = new TestStats() {
            public void onSuccess(long transactionElapsed, long totalElapsed) {
            }

            public void onFailed(long transactionElapsed, long totalElapsed) {
            }
        };

        LoggingStrategy loggingStrategy = new MinlogLoggingStrategy();
        BasicTestStatsResultStrategy resultStrategy = new BasicTestStatsResultStrategy("name", true);
        PropertiesStrategy propertiesStrategy = new DefaultPropertiesProvider();

        TestContext context = new SimpleTestContext("testName", resultStrategy, propertiesStrategy, loggingStrategy);

        PerformanceTest test = new PerformanceTestAdaptor() {
            public void runIteration(TestContext pti) throws Exception {
                pti.startTransaction("transaction");
                throw new Exception("This is supposed to throw");
            }
        };

        TestRunner runner = new TestRunner(test, context, stats, "threadName", listeners);

        runner.iterate();

        assertThat(bucket.get(0).getMessage(), is("This is supposed to throw"));

        runner.iterate();

        assertThat(bucket.get(1).getMessage(), is("This is supposed to throw"));

        runner.iterate();

        assertThat(bucket.get(2).getMessage(), is("This is supposed to throw"));

        Map<String, BasicTestStats> results = resultStrategy.getResults();
        BasicTestStats basicTestStats = results.get("transaction");

        assertThat(basicTestStats.transactionsSuccess, is(0L));
        assertThat(basicTestStats.transactionsFailed, is(3L));
        assertThat(basicTestStats.totalDurationSuccess, is(0L));
        assertThat(basicTestStats.totalDurationFailed, is(greaterThan(0L)));
        assertThat(basicTestStats.getTestName(), is("transaction"));
        assertThat(basicTestStats.sampleTimeStart, is(greaterThan(0L)));

        assertThat(basicTestStats.successResults.isEmpty(), is(true));
        assertThat(basicTestStats.failResults.size(), is(3));


    }

}

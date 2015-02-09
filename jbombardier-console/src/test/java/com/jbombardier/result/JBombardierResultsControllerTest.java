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

package com.jbombardier.result;

import com.jbombardier.console.configuration.JBombardierConfiguration;
import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;
import com.jbombardier.console.sample.SleepTest;
import com.logginghub.utils.FixedTimeProvider;
import org.junit.Test;

import static com.jbombardier.common.AgentStats.AgentStatsBuilder.build;
import static com.jbombardier.common.AgentStats.AgentStatsBuilder.test;
import static com.jbombardier.result.JBombardierRunResult.unknown_state;
import static com.jbombardier.result.JBombardierRunResult.unknown_time;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JBombardierResultsControllerTest {

    private JBombardierConfiguration configuration1 = JBombardierConfigurationBuilder.start()
                                                                                     .addTest(
                                                                                             JBombardierConfigurationBuilder
                                                                                                     .test(SleepTest.class)
                                                                                                     .name("Sleep 20")
                                                                                                     .properties(
                                                                                                             "delay=20"))
                                                                                     .addTest(
                                                                                             JBombardierConfigurationBuilder
                                                                                                     .test(SleepTest.class)
                                                                                                     .name("Sleep 10")
                                                                                                     .properties(
                                                                                                             "delay=10"))
                                                                                     .addAgent("Agent 1", "host1", 1)
                                                                                     .addAgent("Agent 2", "host2", 2)
                                                                                     .testName("My Test")
                                                                                     .toConfiguration();
    private JBombardierRunResult model = new JBombardierRunResult(configuration1);
    private JBombardierResultsController resultsController = new JBombardierResultsController(model);

    @Test public void test_basics() {


        FixedTimeProvider time = new FixedTimeProvider(0);
        resultsController.setTime(time);

        assertThat(model.getDuration(), is(unknown_time));
        assertThat(model.getOutcomeSummary(), is(unknown_state));
        assertThat(model.getPhaseResults().size(), is(1));
        assertThat(model.getPhaseResults().get(JBombardierRunResult.defaultPhase).getPhaseName(),
                   is(JBombardierRunResult.defaultPhase));
        assertThat(model.getStartTime(), is(unknown_time));
        assertThat(model.getTestName(), is("My Test"));

        resultsController.onRunStarted();

        assertThat(model.getDuration(), is(unknown_time));
        assertThat(model.getOutcomeSummary(), is(unknown_state));
        assertThat(model.getPhaseResults().size(), is(1));
        assertThat(model.getPhaseResults().get(JBombardierRunResult.defaultPhase).getPhaseName(),
                   is(JBombardierRunResult.defaultPhase));
        assertThat(model.getStartTime(), is(0L));
        assertThat(model.getTestName(), is("My Test"));

        time.increment(1);
        resultsController.onPhaseStarted(JBombardierRunResult.defaultPhase);

        assertThat(model.getDuration(), is(unknown_time));
        assertThat(model.getOutcomeSummary(), is(unknown_state));
        assertThat(model.getPhaseResults().size(), is(1));
        assertThat(model.getStartTime(), is(0L));
        assertThat(model.getTestName(), is("My Test"));

        JBombardierPhaseResult firstPhase = model.getPhaseResults().get(JBombardierRunResult.defaultPhase);

        assertThat(firstPhase.getTestResults().size(), is(0));
        assertThat(firstPhase.getOutcomeSummary(), is(unknown_state));
        assertThat(firstPhase.getPhaseDuration(), is(unknown_time));
        assertThat(firstPhase.getPhaseName(), is(JBombardierRunResult.defaultPhase));
        assertThat(firstPhase.getPhaseStartTime(), is(1L));

        resultsController.onAgentStatsResult(build().agentName("Agent 1")
                                                    .testStats(test("Test 1").successResult(10, 20))
                                                    .toStats(), "123.123.123.123/host1");

        assertThat(firstPhase.getTestResults().size(), is(1));

        System.out.println(model.toStringDeep());
    }

    @Test public void test_initialise_from_configuration() {

        JBombardierRunResult runResult = new JBombardierRunResult(configuration1);

        assertThat(runResult.getTestName(), is("My Test"));
        assertThat(runResult.getPhaseResults().size(), is(1));
        assertThat(runResult.getPhaseResults().get(JBombardierRunResult.defaultPhase).getPhaseName(),
                   is(JBombardierRunResult.defaultPhase));

    }

//    @Test public void test_result_buckets() {
//
//        final Bucket<Pair<Long, Map<String, JBombardierTestResult>>> completedBuckets = new Bucket<Pair<Long, Map<String, JBombardierTestResult>>>();
//
//        resultsController.addCompletedBucketListener(new JBombardierResultsController.CompletedBucketListener() {
//            @Override public void onCompletedResultBucket(long bucketTime, Map<String, JBombardierTestResult> result) {
//                completedBuckets.add(new Pair<Long, Map<String, JBombardierTestResult>>(bucketTime, result));
//            }
//        });
//
//        resultsController.onRunStarted();
//        resultsController.onPhaseStarted(JBombardierRunResult.defaultPhase);
//
//        resultsController.onAgentStatsResult(build().agentName("Agent 1")
//                                                    .testStats(test("Test 1").successResult(10, 20))
//                                                    .toStats(), "123.123.123.123/host1");
//
//        assertThat(completedBuckets.size(), is(0));
//
//        resultsController.onAgentStatsResult(build().agentName("Agent 2")
//                                                    .testStats(test("Test 1").successResult(15, 25))
//                                                    .toStats(), "123.123.123.123/host2");
//
//        assertThat(completedBuckets.size(), is(1));
//
//    }

}
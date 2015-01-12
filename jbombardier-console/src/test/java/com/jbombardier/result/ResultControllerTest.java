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

import com.logginghub.utils.FixedTimeProvider;
import org.junit.Test;

import static com.jbombardier.common.AgentStats.AgentStatsBuilder.build;
import static com.jbombardier.common.AgentStats.AgentStatsBuilder.test;
import static com.jbombardier.result.JBombardierRunResult.unknown_state;
import static com.jbombardier.result.JBombardierRunResult.unknown_time;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ResultControllerTest {

    @Test public void test_basics() {

        JBombardierRunResult model = new JBombardierRunResult();
        ResultController controller = new ResultController(model);

        FixedTimeProvider time = new FixedTimeProvider(0);
        controller.setTime(time);

        assertThat(model.getDuration(), is(unknown_time));
        assertThat(model.getOutcomeSummary(), is(unknown_state));
        assertThat(model.getPhaseResults().size(), is(0));
        assertThat(model.getStartTime(), is(unknown_time));
        assertThat(model.getTestName(), is(unknown_state));

        controller.onRunStarted();

        assertThat(model.getDuration(), is(unknown_time));
        assertThat(model.getOutcomeSummary(), is(unknown_state));
        assertThat(model.getPhaseResults().size(), is(0));
        assertThat(model.getStartTime(), is(0L));
        assertThat(model.getTestName(), is(unknown_state));

        time.increment(1);
        controller.onPhaseStarted("Phase 1");

        assertThat(model.getDuration(), is(unknown_time));
        assertThat(model.getOutcomeSummary(), is(unknown_state));
        assertThat(model.getPhaseResults().size(), is(1));
        assertThat(model.getStartTime(), is(0L));
        assertThat(model.getTestName(), is(unknown_state));

        JBombardierPhaseResult firstPhase = model.getPhaseResults().get(0);

        assertThat(firstPhase.getTestResults().size(), is(0));
        assertThat(firstPhase.getOutcomeSummary(), is(unknown_state));
        assertThat(firstPhase.getPhaseDuration(), is(unknown_time));
        assertThat(firstPhase.getPhaseName(), is("Phase 1"));
        assertThat(firstPhase.getPhaseStartTime(), is(1L));

        controller.onAgentStatsResult(build().agentName("Agent 1").testStats(test("Test 1").successResult(10, 20)).toStats(), "123.123.123.123/host1");

        assertThat(firstPhase.getTestResults().size(), is(1));

        System.out.println(model.toStringDeep());
    }

}
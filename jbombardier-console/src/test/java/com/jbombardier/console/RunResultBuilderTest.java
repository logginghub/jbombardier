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

package com.jbombardier.console;

import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.model.PhaseModel;
import com.jbombardier.console.model.result.RunResult;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RunResultBuilderTest {

    @Test public void test_empty() {
        JBombardierModel model = new JBombardierModel();

        RunResultBuilder runResultBuilder = new RunResultBuilder();
        CapturedStatisticsHelper capturedStatisticsHelper = new CapturedStatisticsHelper(model);

        RunResult snapshot = runResultBuilder.createSnapshot(model, capturedStatisticsHelper);

        assertThat(snapshot.getAgentResults().size(), is(0));
    }

    @Test public void test_captured_stats() throws IOException {

        PhaseModel phase1 = new PhaseModel();
        phase1.getPhaseName().set("Phase 1");

        PhaseModel phase2 = new PhaseModel();
        phase2.getPhaseName().set("Phase 2");

        JBombardierModel model = new JBombardierModel();
        model.getPhaseModels().add(phase1);
        model.getPhaseModels().add(phase2);

        CapturedStatistic capturedStatistic1 = new CapturedStatistic(0, "a/b/c", "stat1");
        CapturedStatistic capturedStatistic2 = new CapturedStatistic(0, "a/b/c", "stat2");
        CapturedStatistic capturedStatistic3 = new CapturedStatistic(0, "a/b/c", "stat3");
        CapturedStatistic capturedStatistic4 = new CapturedStatistic(0, "a/b/c", "stat4");

        RunResultBuilder runResultBuilder = new RunResultBuilder();
        CapturedStatisticsHelper capturedStatisticsHelper = new CapturedStatisticsHelper(model);

        // Imagine the test is started here...
        model.getCurrentPhase().set(phase1);

        capturedStatisticsHelper.addCapturedStatistic(capturedStatistic1);
        capturedStatisticsHelper.addCapturedStatistic(capturedStatistic2);

        model.getCurrentPhase().set(phase2);

        capturedStatisticsHelper.addCapturedStatistic(capturedStatistic3);
        capturedStatisticsHelper.addCapturedStatistic(capturedStatistic4);

        // And finishes here...
        capturedStatisticsHelper.closeStreamingFiles();

        RunResult snapshot = runResultBuilder.createSnapshot(model, capturedStatisticsHelper);

        assertThat(snapshot.getPhaseResults().size(), is(2));
        assertThat(snapshot.getPhaseResults().get(0).getPhaseName(), is("Phase 1"));
        assertThat(snapshot.getPhaseResults().get(1).getPhaseName(), is("Phase 2"));

        assertThat(snapshot.getPhaseResults().get(0).getCapturedStatistics().size(), is(2));
        assertThat(snapshot.getPhaseResults().get(0).getCapturedStatistics().get(0).getValue(), is("stat1"));
        assertThat(snapshot.getPhaseResults().get(0).getCapturedStatistics().get(1).getValue(), is("stat2"));

        assertThat(snapshot.getPhaseResults().get(1).getCapturedStatistics().size(), is(2));
        assertThat(snapshot.getPhaseResults().get(1).getCapturedStatistics().get(0).getValue(), is("stat3"));
        assertThat(snapshot.getPhaseResults().get(1).getCapturedStatistics().get(1).getValue(), is("stat4"));


    }


}
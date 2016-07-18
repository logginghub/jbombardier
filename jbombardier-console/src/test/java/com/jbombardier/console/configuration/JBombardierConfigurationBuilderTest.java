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

package com.jbombardier.console.configuration;

import com.jbombardier.console.sample.SleepTest;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class JBombardierConfigurationBuilderTest {

    @Test public void test_phases() {

        JBombardierConfigurationBuilder builder = JBombardierConfigurationBuilder.configurationBuilder();
        builder.addPhase(builder.phase("Phase 1")
                                .duration("10 minutes")
                                .addTest(JBombardierConfigurationBuilder.TestBuilder.start(SleepTest.class)));

        JBombardierConfiguration configuration = builder.toConfiguration();
        assertThat(configuration.getPhases().size(), is(1));
        assertThat(configuration.getPhases().get(0).getPhaseName(), is("Phase 1"));
        assertThat(configuration.getPhases().get(0).getDuration(), is("10 minutes"));
        assertThat(configuration.getPhases().get(0).getTests().size(), is(1));
        assertThat(configuration.getPhases().get(0).getTests().get(0).getClassname(), is("com.jbombardier.console.sample.SleepTest"));

    }

}
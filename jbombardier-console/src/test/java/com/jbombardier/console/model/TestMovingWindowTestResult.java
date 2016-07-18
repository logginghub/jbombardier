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

package com.jbombardier.console.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

import com.jbombardier.common.AgentStats.TestStats;

public class TestMovingWindowTestResult {

    private TestStats ts1 = new TestStats();
    private TestStats ts2 = new TestStats();
    private TestStats ts3 = new TestStats();
    private TestStats ts4 = new TestStats();
    private TestStats ts5 = new TestStats();
    private TestStats ts6 = new TestStats();
    private MovingWindowTestResult result;

    @Before public void setup() {
        ts1.transactionsSuccess = 1;
        ts2.transactionsSuccess = 2;
        ts3.transactionsSuccess = 3;
        ts4.transactionsSuccess = 4;
        ts5.transactionsSuccess = 5;
        ts6.transactionsSuccess = 6;

        ts1.totalDurationSuccess = 10;
        ts2.totalDurationSuccess = 20;
        ts3.totalDurationSuccess = 30;
        ts4.totalDurationSuccess = 40;
        ts5.totalDurationSuccess = 50;
        ts6.totalDurationSuccess = 60;

        result = new MovingWindowTestResult();
    }

    @Test public void test_add() throws Exception {

        result.setWindowLength(2000);

        result.addTestsStats(1, ts1);
        result.addTestsStats(2, ts2);
        result.addTestsStats(3, ts3);
        result.addTestsStats(4, ts4);
        result.addTestsStats(5, ts5);
        result.addTestsStats(6, ts6);

        assertThat(result.size(), is(6));

        // Right - the window is currently 5 milliseconds long
        assertThat(result.getCurrentWindowDuration(), is(5L));

        // If its 5 milliseconds long, and we've added 21 transaction - this gives us a per second
        // value of 1000 / 5 * 21
        assertThat(result.getCurrentWindowSuccessRate(), is(21.0 * 1000.0 / 5.0));

    }

    @Test public void test_roll_off() throws Exception {

        result.setWindowLength(2000);

        result.addTestsStats(0, ts1);
        assertThat(result.size(), is(1));
        assertThat(result.getCurrentWindowDuration(), is(0L));
        assertThat(result.getEarliest(), is(0L));
        assertThat(result.getLatest(), is(0L));
        assertThat(Double.isInfinite(result.getCurrentWindowSuccessRate()), is(true));
        
        result.addTestsStats(1000, ts2);
        assertThat(result.size(), is(2));
        assertThat(result.getCurrentWindowDuration(), is(1000L));
        assertThat(result.getEarliest(), is(0L));
        assertThat(result.getLatest(), is(1000L));
        assertThat(result.getCurrentWindowSuccessRate(), is((1.0 + 2.0) * 1000 / 1000));
        
        result.addTestsStats(2000, ts3);
        assertThat(result.size(), is(3));
        assertThat(result.getCurrentWindowDuration(), is(2000L));
        assertThat(result.getEarliest(), is(0L));
        assertThat(result.getLatest(), is(2000L));
        assertThat(result.getCurrentWindowSuccessRate(), is((1.0 + 2.0 + 3.0) * 1000 / 2000));
        
        result.addTestsStats(3000, ts4);
        assertThat(result.size(), is(3));
        assertThat(result.getCurrentWindowDuration(), is(2000L));
        assertThat(result.getEarliest(), is(1000L));
        assertThat(result.getLatest(), is(3000L));
        assertThat(result.getCurrentWindowSuccessRate(), is((2.0 + 3.0 + 4.0) * 1000 / 2000));
        
        result.addTestsStats(3500, ts5);
        assertThat(result.size(), is(3));
        assertThat(result.getCurrentWindowDuration(), is(1500L));
        assertThat(result.getEarliest(), is(2000L));
        assertThat(result.getLatest(), is(3500L));
        assertThat(result.getCurrentWindowSuccessRate(), is((3.0 + 4.0 + 5.0) * 1000 / 1500));
        
        result.addTestsStats(4000, ts6);
        assertThat(result.size(), is(4));
        assertThat(result.getCurrentWindowDuration(), is(2000L));
        assertThat(result.getEarliest(), is(2000L));
        assertThat(result.getLatest(), is(4000L));
        assertThat(result.getCurrentWindowSuccessRate(), is((3.0 + 4.0 + 5.0 + 6.0) * 1000 / 2000));

        


    }

}

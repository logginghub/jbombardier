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

package com.jbombardier.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicTestStats {

    public long transactionsSuccess;
    public long transactionsFailed;
    
    /**
     * The total of the iteration times for the successful tests
     */
    public long totalDurationSuccess;
    
    /**
     * The total of the total durations (before + iteration) for the successful tests.
     */
    public long totalDurationTotalSuccess;
    
    public long totalDurationFailed;
    private final String testName;
    public long sampleTimeStart;
           
    // TODO : these will be slowing things down with lots of threads on one test, they should be thread localled really and aggregated when gathering stats
    public List<Long> successResults = Collections.synchronizedList(new ArrayList<Long>(100));
    public List<Long> failResults = Collections.synchronizedList(new ArrayList<Long>(100));


    public BasicTestStats(String testName) {
        this.testName = testName;
        sampleTimeStart = System.currentTimeMillis();
    }

    @Override public String toString() {
        return "BasicTestStats [testName=" + testName + "]";
    }

    public String getTestName() {
        return testName;
    }

    public void reset() {
        transactionsFailed = 0;
        totalDurationFailed = 0;
        totalDurationSuccess = 0;
        totalDurationTotalSuccess = 0;
        transactionsSuccess = 0;
        sampleTimeStart = System.currentTimeMillis();
    }

    public void add(BasicTestStats other) {
        transactionsFailed += other.transactionsFailed;
        totalDurationFailed += other.totalDurationFailed;
        totalDurationSuccess += other.totalDurationSuccess;
        transactionsSuccess += other.transactionsSuccess;
        sampleTimeStart = Math.min(this.sampleTimeStart, other.sampleTimeStart);
    }
    

    public List<Long> switchOutSuccessResults() {
        List<Long> successResults = this.successResults;
        this.successResults = new ArrayList<Long>(100);
        return successResults;
    }

    public List<Long> switchOutFailResults() {
        List<Long> failResults = this.failResults;
        this.failResults = new ArrayList<Long>(100);
        return failResults;
    }
}

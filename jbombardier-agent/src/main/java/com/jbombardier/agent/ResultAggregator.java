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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.utils.FactoryMap;
import com.jbombardier.common.BasicTestStats;
import com.jbombardier.common.BasicTestStatsResultStrategy;
import com.jbombardier.common.ResultStrategy;

/**
 * Because each test thread needs its own results object (to avoid any need for
 * locking/serialisation on updates) the agent needs someone to aggregate the
 * results for each transaction across all threads before sending them back.
 * Enter the ResultsAggregator...
 * 
 * @author James
 * 
 */
public class ResultAggregator {

    private List<ResultStrategy> individualThreadsResultsHandlers = new CopyOnWriteArrayList<ResultStrategy>();

    public void addResultStrategy(ResultStrategy strategy) {
        individualThreadsResultsHandlers.add(strategy);
    }

    public List<ResultStrategy> getIndividualThreadsResultsHandlers() {
        return individualThreadsResultsHandlers;
    }

    public Map<String, BasicTestStats> getResultsAndReset() {

        HashMap<String, BasicTestStats> totalResultsForAllThreads = new FactoryMap<String, BasicTestStats>() {
            private static final long serialVersionUID = 1L;

            @Override protected BasicTestStats createEmptyValue(String key) {
                return new BasicTestStats(key);
            }
        };

        for (ResultStrategy resultStrategy : individualThreadsResultsHandlers) {

            if (resultStrategy instanceof BasicTestStatsResultStrategy) {
                BasicTestStatsResultStrategy basicResults = (BasicTestStatsResultStrategy) resultStrategy;

                Map<String, BasicTestStats> results = basicResults.getResults();
                for (String testName : results.keySet()) {
                    BasicTestStats statsForThisThread = results.get(testName);
                    
                    BasicTestStats total = totalResultsForAllThreads.get(testName);
                    total.add(statsForThisThread);
                    

                    List<Long> switchOutFailResults = statsForThisThread.switchOutFailResults();
                    List<Long> switchOutSuccessResults = statsForThisThread.switchOutSuccessResults();
                    
                    total.successResults.addAll(switchOutSuccessResults);
                    total.failResults.addAll(switchOutFailResults);
                    
                    statsForThisThread.reset();
                }
            }
            else {
                throw new RuntimeException("We dont know how to aggregate " + resultStrategy.getClass());
            }

        }

        return totalResultsForAllThreads;
    }  
}

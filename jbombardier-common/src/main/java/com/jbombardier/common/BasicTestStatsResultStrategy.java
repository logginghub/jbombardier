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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.logginghub.utils.FactoryMapDecorator;

// TODO : the results need to be thread localled to avoid inconsistent non-atomic updates from multiple threads
public class BasicTestStatsResultStrategy implements ResultStrategy {

    private Map<String, BasicTestStats> results = new FactoryMapDecorator<String, BasicTestStats>(new ConcurrentHashMap<String, BasicTestStats>()) {
        @Override protected BasicTestStats createNewValue(String key) {
            return new BasicTestStats(key);
        }
    };
    
    private final String name;
    private boolean captureResults;
    

    public BasicTestStatsResultStrategy(String name, boolean captureResults) {
        this.name = name;
        this.captureResults = captureResults;        
    }

    public void onNewFailResult(String transactionID, long elapsedNanos, Throwable t) {
        BasicTestStats basicTestStats = results.get(transactionID);
        basicTestStats.totalDurationFailed += elapsedNanos;
        basicTestStats.transactionsFailed++;
        
        if(captureResults){
           basicTestStats.failResults.add(elapsedNanos);
        }
    }

    public void onNewFailResult(String transactionID, long elapsedNanos, String message) {
        BasicTestStats basicTestStats = results.get(transactionID);
        basicTestStats.totalDurationFailed += elapsedNanos;
        basicTestStats.transactionsFailed++;
        
        if(captureResults){
            basicTestStats.failResults.add(elapsedNanos);
         }
    }

    public void onNewSuccessResult(String transactionID, long elapsedNanos) {
        BasicTestStats basicTestStats = results.get(transactionID);
        basicTestStats.totalDurationSuccess += elapsedNanos;
        basicTestStats.transactionsSuccess++;
        
        if(captureResults){
            basicTestStats.successResults.add(elapsedNanos);
         }
    }
    
    public Map<String, BasicTestStats> getResults() {
        return results;
    }
    
    public void resetResult(){
        Collection<BasicTestStats> values = results.values();
        for (BasicTestStats basicTestStats : values) {
            basicTestStats.reset();
        }
    }
    
    @Override public String toString() {
        return "BasicTestStatsResultStrategy [name=" + name + ", results=" + results + "]";
    }
}

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

import com.jbombardier.common.BasicTestStatsResultStrategy;
import com.jbombardier.common.DefaultPropertiesProvider;
import com.jbombardier.common.LoggingStrategy;
import com.jbombardier.common.MinlogLoggingStrategy;
import com.jbombardier.common.PropertiesStrategy;
import com.jbombardier.common.ResultStrategy;
import com.jbombardier.common.SimpleTestContext;
import com.jbombardier.common.TestContext;

public class SimpleTestContextFactory implements TestContextFactory {

    private PropertiesStrategy propertiesProvider = new DefaultPropertiesProvider();
    private LoggingStrategy loggingStrategy = new MinlogLoggingStrategy();
    private ResultAggregator resultAggregator = new ResultAggregator();
    private boolean recordAllValues = true;

    private final ResultStrategyFactory resultStrategyFactory = new ResultStrategyFactory() {
        public ResultStrategy createResultStrategy(String testName, String threadName) {
            ResultStrategy strategy;
            strategy = new BasicTestStatsResultStrategy(testName + "/" + threadName, recordAllValues);
            resultAggregator.addResultStrategy(strategy);
            return strategy;
        }
    };

    public LoggingStrategy getLoggingStrategy() {
        return loggingStrategy;
    }
    
    public PropertiesStrategy getPropertiesProvider() {
        return propertiesProvider;
    }
    
    public ResultAggregator getResultAggregator() {
        return resultAggregator;
    }
    
    public ResultStrategyFactory getResultStrategyFactory() {
        return resultStrategyFactory;
    }
    
    public void setLoggingStrategy(LoggingStrategy loggingStrategy) {
        this.loggingStrategy = loggingStrategy;
    }
    
    public void setResultAggregator(ResultAggregator resultAggregator) {
        this.resultAggregator = resultAggregator;
    }
    
    public void setPropertiesProvider(PropertiesStrategy propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }
    
    public void setRecordAllValues(boolean recordAllValues) {
        this.recordAllValues = recordAllValues;
    }
    
    public TestContext createTestContext(String testName, String threadName) {
        TestContext context = new SimpleTestContext(testName, resultStrategyFactory.createResultStrategy(testName, threadName), propertiesProvider, loggingStrategy);
        return context;

    }

}

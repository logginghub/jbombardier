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

package com.jbombardier.common;

import java.util.HashMap;
import java.util.Map;

public class ResultsPackage {

    public static class ThreadResults extends HashMap<String, AggregatedResultSeries>{};

    private Map<String, ThreadResults> threadResults = new HashMap<String, ThreadResults>();

    public void add(String name, ThreadResults resultSeries) {
        threadResults.put(name, resultSeries);
    }
    
    public Map<String, ThreadResults> getThreadResults() {
        return threadResults;
    }
}

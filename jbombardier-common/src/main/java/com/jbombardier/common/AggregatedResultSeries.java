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
import java.util.List;

public class AggregatedResultSeries {
    private List<AggregatedResult> results = new ArrayList<AggregatedResult>();
    private String transactionID;
    
    public AggregatedResultSeries(){
    }
    
    
    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }
    
    public AggregatedResultSeries(String transactionID){
        this.transactionID = transactionID;        
    }
    
    public List<AggregatedResult> getResults() {
        return results;
    }
    public String getTransactionID() {
        return transactionID;
    }
}

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

import java.util.Date;

public class AggregatedResult {

    public double max = -Double.MAX_VALUE;
    public double min = Double.MAX_VALUE;
    public double open = Double.NaN;
    public double close = Double.NaN;
    public double total = 0;
    public long count = 0;
    public long time;       
    
    public AggregatedResult(long now) {
        time = now;
    }

    public AggregatedResult() {

    }

    @Override public String toString() {     
        return String.format("%s : max %.2f min %.2f open %.2f close %.2f total %.2f count %d", new Date(time), max, min, open, close, total, count);
    }
    
    public void update(double value){
        max = Math.max(max, value);
        min = Math.min(min, value);
        close = value;
        if(Double.isNaN(open)){
            open = value;
        }
        total += value;
        count++;
    }

    public double mean() {
        return total/count;
    }
    
    
}

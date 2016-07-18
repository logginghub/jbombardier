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

package com.jbombardier.console.sample.old;

import java.util.Random;

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;

public class GaussianDistributionMillisecondResolution extends PerformanceTestAdaptor {

    private Random random = new Random();
    private int milliseconds;
    
    @Override public void beforeIteration(TestContext pti) throws Exception {
        double value = getValue();                
        milliseconds = (int) value;    
    }

    public void runIteration(TestContext pti) throws Exception {             
        pti.sleep(milliseconds);        
    }

    private long getValue() {
        double val = random.nextGaussian() * 10 + 50;
        int millisDelay = (int) Math.round(val);
        return millisDelay;
    }
}

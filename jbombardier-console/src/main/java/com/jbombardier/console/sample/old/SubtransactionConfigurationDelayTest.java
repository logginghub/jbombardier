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

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;

/**
 * Benchmark test that support fixed duration subtransaction delays. You
 * configure it by providing a property called 'delay' in the format
 * '{transactionID delay|transactionID delay|...}'
 * 
 * @author James
 */
public class SubtransactionConfigurationDelayTest extends PerformanceTestAdaptor {

    private String[] transactions;
    private int[] delays;
    
    public void setup(TestContext pti) throws Exception {
        String property = pti.getProperty("delay");
        String[] split = property.split("\\|");
        
        transactions = new String[split.length];
        delays = new int[split.length];
        
        int index = 0;
        for (String string : split) {
            String[] split2 = string.split(" ");
            transactions[index] = split2[0];
            delays[index] = Integer.parseInt(split2[1]);
            index++;
        }        
    }

    public void runIteration(TestContext pti) throws Exception {
        for(int i = 0; i < transactions.length; i++){
            pti.startTransaction(transactions[i]);
            pti.sleep(delays[i]);
            pti.endTransaction(transactions[i]);
        }
    }
}

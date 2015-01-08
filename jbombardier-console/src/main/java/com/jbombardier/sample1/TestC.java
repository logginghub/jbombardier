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

package com.jbombardier.sample1;

import java.util.Random;

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;

public class TestC extends PerformanceTestAdaptor {

    private Random random = new Random();

    public void runIteration(TestContext pti) throws Exception {
        pti.startTransaction("transactionOne");
        int millis = random.nextInt(100);
        Thread.sleep(millis);
        pti.endTransaction("transactionOne");        
        
        pti.startTransaction("transactionTwo");
        int millis2 = random.nextInt(50);
        Thread.sleep(millis2);
        pti.endTransaction("transactionTwo");
    }
}

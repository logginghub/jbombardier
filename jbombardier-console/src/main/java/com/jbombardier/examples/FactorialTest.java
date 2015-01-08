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

package com.jbombardier.examples;

import java.math.BigDecimal;

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;

public class FactorialTest extends PerformanceTestAdaptor
{
    public static BigDecimal factorial(int n)
    {
        BigDecimal value = BigDecimal.valueOf(1);
        for (int i = 1; i <= n; i++)
        {
            value = value.multiply(BigDecimal.valueOf(i));
        }
        return value;
    }

    public void runIteration(TestContext pti)
    {
        int input = 10000;
        for(int i = 0; i < input; i+=100){
            String transactionID  = "factorial";
            pti.startTransaction(transactionID);
            factorial(i);
            pti.endTransaction(transactionID);
        }
    }
}

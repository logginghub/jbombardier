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

import org.junit.Ignore;
import org.junit.Test;

import com.esotericsoftware.minlog.Log;
import com.logginghub.utils.IteratingRunnable;

@Ignore public class TestRateControlledIteratingWorkerThreadTest {

    @Test public void test() throws InterruptedException {

        Log.set(Log.LEVEL_TRACE);

        final long start = System.currentTimeMillis();
        IteratingRunnable runnable = new IteratingRunnable() {

            public void iterate() {
                System.out.println(Long.toString(System.currentTimeMillis() - start) + " : iterating");
//                return 0;
            }

            public void beforeFirst() {

            }

            public void afterLast() {

            }
        };
        RateControlledIteratingWorkerThread thread = new RateControlledIteratingWorkerThread("Name", runnable, 1, 5, 1, 1000, 1);

        thread.start();
        Thread.sleep(10000);
        System.out.println("--------------------- Raising to 10         -------------------------");

        thread.setTargetRate(10);
        Thread.sleep(10000);

        System.out.println("--------------------- Dropping to 0         -------------------------");
        thread.setTargetRate(0);
        Thread.sleep(10000);

        System.out.println("--------------------- Raising to 20 quickly -------------------------");
        thread.setTargetRate(20);
        thread.setStep(2);
        thread.setStepTime(500);
        Thread.sleep(10000);

        thread.stop();
    }

}

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

package com.jbombardier.console.sample.old;

import java.io.InputStream;

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.PropertyEntry;
import com.jbombardier.common.TestContext;
import com.jbombardier.common.TestRunner;

public class Test2 extends PerformanceTestAdaptor {

    public void setup(TestContext pti) throws Exception {
        pti.startTransaction("setupTransaction");
        pti.endTransaction("setupTransaction");
    }

    public void runIteration(TestContext pti) throws Exception {

        InputStream resourceAsStream = Test2.class.getResourceAsStream("thisDoesntExist");

        PropertyEntry propertyEntry = pti.getPropertyEntry("data-poolThread");
        String string = propertyEntry.getString("string");
        int value = propertyEntry.getInteger("value");
        System.out.println(Thread.currentThread().getName() +
                           " :" +
                           " Test2 (poolThread): " +
                           string +
                           " : " +
                           value +
                           " (non-existant resource is " +
                           resourceAsStream +
                           ")");

        pti.startTransaction("subTransaction1");
        Thread.sleep(pti.random(50));
        pti.endTransaction("subTransaction1");
    }

    public void teardown(TestContext pti) throws Exception {

    }

    public static void main(String[] args) {
        TestRunner.benchmark(new Test2())
                  .addPropertyEntries("data-poolThread", new String[] { "string", "value" }, new String[] { "a", "10" })
                  .run();
    }
}

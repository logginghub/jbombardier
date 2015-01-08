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

import com.logginghub.utils.Is;
import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.PropertyEntry;
import com.jbombardier.common.TestContext;
import com.jbombardier.common.TestRunner;

public class Test1 extends PerformanceTestAdaptor {

    public void setup(TestContext pti) throws Exception {
        String property = pti.getProperty("testProperty");
        System.out.println("Test1 individual property is " + property);
        String simpleName = Thread.currentThread().getContextClassLoader().getClass().getSimpleName();
        Is.equals(simpleName, "Messaging2ClassLoader", "Wrong context classloader");
    }

    public void runIteration(TestContext pti) throws Exception {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Is.equals(contextClassLoader.getClass().getSimpleName(), "Messaging2ClassLoader", "Wrong context classloader");

        PropertyEntry shakespeare = pti.getPropertyEntry("data-shakespeare");
        String shakespeareLine = shakespeare.getString("entry");

        PropertyEntry propertyEntry = pti.getPropertyEntry("data-fixedThread");
        String string = propertyEntry.getString("string");
        int value = propertyEntry.getInteger("value");
        System.out.println(Thread.currentThread().getName() + " :" + " Test1 (fixedThread): " + string + " : " + value + " - Shakespeare says '" + shakespeareLine + "'");
    }

    public void teardown(TestContext pti) throws Exception {
        Is.equals(Thread.currentThread().getContextClassLoader().getClass().getSimpleName(), "Messaging2ClassLoader", "Wrong context classloader");

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        System.out.println(contextClassLoader.getClass().getName());
    }

    public static void main(String[] args) {
        TestRunner.benchmark(new Test1()).run();
    }
}

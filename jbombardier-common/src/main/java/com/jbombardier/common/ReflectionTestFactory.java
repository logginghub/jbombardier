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

public class ReflectionTestFactory implements TestFactory {

    private Class<? extends PerformanceTest> testClass;

    public ReflectionTestFactory(Class<? extends PerformanceTest> testClass) {
        this.testClass = testClass;
    }

    public PerformanceTest createTest() {
        try {
            return testClass.newInstance();
        }
        catch (InstantiationException e) {
            throw new RuntimeException(String.format("Failed to create new instance of test class '%s' - are you sure there is a default constructor?", testClass.getName()), e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Failed to create new instance of test class '%s' - are you sure there is a default constructor?", testClass.getName()), e);
        }
    }

}

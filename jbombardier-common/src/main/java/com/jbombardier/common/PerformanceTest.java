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

public interface PerformanceTest {
    /**
     * Sets the test up. This method is called once per test instance before the
     * main iteration loop is started. You'd typically put one-time
     * configuration here, things that you dont want to repeat every single
     * iteration.
     * 
     * @param pti
     *            The TestContext interface that provides services to your test.
     * @throws Exception
     *             You can throw anything from the methods on this interface;
     *             the errors will be sent back to the console. Exceptions from
     *             the setup method will halt the entire test by default.
     */
    void setup(TestContext pti) throws Exception;

    void beforeIteration(TestContext pti) throws Exception;

    void runIteration(TestContext pti) throws Exception;

    void afterIteration(TestContext pti, long duration) throws Exception;

    void teardown(TestContext pti) throws Exception;
}

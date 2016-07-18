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

package com.jbombardier.examples;

import java.io.IOException;

import com.logginghub.utils.ThreadUtils;
import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;

/**
 * This should produce a sine wave if you plot the raw results.
 * 
 * @author James
 * 
 */
public class SineWaveTest extends PerformanceTestAdaptor {

    private int count = 0;

    public void runIteration(TestContext pti) throws IOException {
        int sleep = (int) ((1 + Math.sin(count)) * 250);
        System.out.println(sleep);
        ThreadUtils.sleep(sleep);
        count++;
    }
}

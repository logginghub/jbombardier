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

import java.io.InputStream;

import com.logginghub.utils.FileUtils;
import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.PropertyEntry;
import com.jbombardier.common.TestContext;

public class Test4 extends PerformanceTestAdaptor {

    public void runIteration(TestContext pti) throws Exception {
        PropertyEntry propertyEntry = pti.getPropertyEntry("data-poolGlobal");
        String string= propertyEntry.getString("string");
        int value= propertyEntry.getInteger("value");
        
        InputStream resourceAsStream1 = Test4.class.getResourceAsStream("/org/apache/velocity/runtime/defaults/directive.properties");
        String data1 = FileUtils.read(resourceAsStream1);        
        
        InputStream resourceAsStream = Test4.class.getResourceAsStream("datafile.txt");
        String data = FileUtils.read(resourceAsStream);        
        System.out.println(Thread.currentThread().getName() + " :" +  " Test4 (poolGlobal): " + string + " : " + value + "(loaded relative resource :" + data + ")(loaded jar resource : "+ data1.subSequence(0,  20) + ")");
    }
}

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

package com.jbombardier.sample.io;

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by james on 30/01/15.
 */
public class DiskWritePerformance extends PerformanceTestAdaptor {

    private FileOutputStream fos;
    private byte[] data;
    private File file;

    @Override
    public void setup(TestContext pti) throws Exception {
        file = new File("/tmp/fos" + Thread.currentThread().getName() + ".dat");
        FileUtils.ensurePathExists(file);
        data = StringUtils.randomString(1 * 1024 * 1024).getBytes();
    }

    @Override
    public void beforeIteration(TestContext pti) throws Exception {
        fos = new FileOutputStream(file);
    }

    @Override
    public void runIteration(TestContext pti) throws Exception {
        fos.write(data);
    }

    @Override
    public void afterIteration(TestContext pti, long nanos) throws Exception {
        fos.close();
        file.delete();
    }
}

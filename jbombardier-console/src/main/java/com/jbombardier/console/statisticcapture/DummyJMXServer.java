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

package com.jbombardier.console.statisticcapture;

import com.j256.simplejmx.common.JmxAttributeMethod;
import com.j256.simplejmx.common.JmxOperation;
import com.j256.simplejmx.common.JmxResource;
import com.j256.simplejmx.server.JmxServer;
import com.logginghub.utils.logging.Logger;

import javax.management.JMException;

/**
 * Created by james on 18/11/14.
 */
public class DummyJMXServer {

    private final static Logger logger = Logger.getLoggerFor(DummyJMXServer.class);

    @JmxResource(description = "Lookup cache", domainName = "logginghub", beanName = "LookupCache")
    public static class LookupCache {

        private int hitCount;

        @JmxAttributeMethod(description = "Number of hits")
        public int getHitCount() {
            return hitCount++;
        }

        @JmxOperation(description = "Increment the counter")
        public void flushCache() {
            hitCount++;
        }
    }

    public static void main(String[] args) throws JMException {

        JmxServer jmxServer = new JmxServer(12345);

        logger.info("Starting server...");
        jmxServer.start();

        jmxServer.register(new LookupCache());
        logger.info("Registered objects");



        //jmxServer.stop();
    }
}

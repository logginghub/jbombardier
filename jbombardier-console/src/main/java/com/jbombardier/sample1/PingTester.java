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

package com.jbombardier.sample1;

import java.net.InetAddress;

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;
import com.jbombardier.common.PropertyEntry;

public class PingTester extends PerformanceTestAdaptor {

    public void runIteration(TestContext pti) throws Exception {

        PropertyEntry entry = pti.getPropertyEntry("target");
        String description = entry.getString("Description");
        String ip = entry.getString("Host");
        int timeout = entry.getInteger("Timeout");

        InetAddress address = InetAddress.getByName(ip);
        pti.startTransaction("ping-" + description);
        boolean reachable = address.isReachable(timeout);
        if (reachable) {
            pti.endTransaction("ping-" + description);
        }
        else {
            pti.failTransaction("ping-" + description, "isReachable timed out");
        }
    }


}

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

package com.jbombardier.repository;

import java.io.Closeable;

import com.logginghub.messaging.Level3AsyncClient;
import com.logginghub.utils.NetUtils;
import com.jbombardier.common.RepositoryInterface;

public class RepositoryMessagingClient implements RepositoryInterface, Closeable {

    private Level3AsyncClient client;
    private RepositoryInterface service;

    public RepositoryMessagingClient() {
        client = new Level3AsyncClient("Client");
    }
    
    @Override public void postResult(String jsonResult) {
        service.postResult(jsonResult);
    }

    public void connect(String string, int port) {
        client.addConnectionPoint(NetUtils.toInetSocketAddress(string, port));
        client.connect().await();
        service = client.getService("repository", RepositoryInterface.class).awaitService();
    }

    @Override public void close() {
        client.close();
    }

}

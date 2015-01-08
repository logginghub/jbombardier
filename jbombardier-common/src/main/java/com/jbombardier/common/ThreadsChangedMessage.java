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

package com.jbombardier.common;

public class ThreadsChangedMessage {

    private final int threads;
    private final String agent;
    private final String test;

    public ThreadsChangedMessage() {
        threads = 0;
        agent = null;
        test = null;
    }

    public ThreadsChangedMessage(String agent, String test, int threads) {
        this.agent = agent;
        this.test = test;
        this.threads = threads;
    }

    public int getThreads() {
        return threads;
    }

    public String getAgent() {
        return agent;         
    }

    public String getTest() {
        return test;
    }
}

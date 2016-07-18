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

public enum DataStrategy {
    /**
     * Each thread in each agent will get a unique record from the data source.
     * We recommend you always do this in your setup(...) method as it will
     * require a rountrip to the console to ensure each agent/thread pairing
     * gets a unique record. Once all of the data records are exhausted, they
     * will start repeating for new agent/thread pairs.
     */
    fixedThread,
    /**
     * The full dataset is divided first by the number of agents, then by the
     * number of threads. Each thread will then iterate through its unique
     * subset. The set available to the agent will is always fixed for a given
     * test run, but as the number of threads in an agent is variable, adding
     * and removing threads will cause the pool to change size. Records will be
     * repeated if there are not enough records to split between the number of
     * threads running.
     */
    pooledThread,
    /**
     * The full dataset is divided between the number of agents. Each thread can
     * then receive any of the records in the agent pool at random. If there are
     * fewer data records than agents, the data will be duplicated until each
     * agent has at least on item in their pool.
     */
    pooledAgent,
    /**
     * The full dataset is available to all agent/thread pairings; data records
     * will be provided in a random order for each request.
     */
    pooledGlobal;
}

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

import java.util.ArrayList;
import java.util.HashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.rmi.ObjectSpace;

public class KryoHelper {
    public static int meg = 1024 * 1024;
    public static final int objectBufferSize = 10 * meg;
    public static final int writeBufferSize = 10 * meg;

    public static void registerTypes(Kryo kryo) {
        ObjectSpace.registerClasses(kryo);
        kryo.register(ArrayList.class);
        kryo.register(AgentInstruction.class);
        kryo.register(AgentFailedInstruction.class);
        kryo.register(AgentCompletedInstruction.class);
        kryo.register(AgentKillInstruction.class);
        kryo.register(TestInstruction.class);
        kryo.register(AgentClassRequest.class);
        kryo.register(AgentClassResponse.class);
        kryo.register(Result.class);
        kryo.register(byte[].class);
        kryo.register(HashMap.class);
        kryo.register(AgentPropertyRequest.class);
        kryo.register(AgentPropertyResponse.class);
        kryo.register(AgentPropertyEntryRequest.class);
        kryo.register(AgentPropertyEntryResponse.class);
        kryo.register(AgentLogMessage.class);
        kryo.register(HostInterface.class);
        kryo.register(ResultsPackage.class);
        kryo.register(AggregatedResultSeries.class);
        kryo.register(AggregatedResult.class);
        kryo.register(ResultsPackage.ThreadResults.class);
        kryo.register(TestPackage.class);
        kryo.register(StopTestRequest.class);
        kryo.register(StopTestResponse.class);
        kryo.register(ThreadsChangedMessage.class);
        kryo.register(AgentStats.class);
        kryo.register(AgentStats.TestStats.class);
        kryo.register(TestVariableUpdateRequest.class);
        kryo.register(TestField.class);
        kryo.register(PropertyEntry.class);
        kryo.register(int[].class);
        kryo.register(String[].class);
        kryo.register(DataBucket.class);
        kryo.register(DataStrategy.class);
        kryo.register(SendTelemetryRequest.class);
        kryo.register(StopTelemetryRequest.class);
        kryo.register(PingMessage.class);
        kryo.register(PhaseInstruction.class);
        kryo.register(PhaseStartInstruction.class);
        kryo.register(PhaseStopInstruction.class);
    }
}


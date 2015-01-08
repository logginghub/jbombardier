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

package com.jbombardier.agent;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.esotericsoftware.kryonet.Client;
import com.logginghub.utils.ProcessWrapper;
import com.jbombardier.common.AgentKillInstruction;
import com.jbombardier.common.KryoHelper;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

@Ignore
public class AgentProcessTest extends AbstractAgentBase
{
    @Test public void test() throws IOException, InterruptedException
    {
        ProcessWrapper process = launchAgent();

        Thread.sleep(500);
        process.getProcess().destroy();

        process.waitFor();

        assertThat(process.getProcess().exitValue(), is(1));

//        System.out.println(process.getOutput());
//        System.err.println(process.getError());
//
//        assertThat(process.getOutput(), is(containsString("Agent is bound")));
//        assertThat(process.getError().isEmpty(), is(true));
    }

    @Test public void testAgentKillMessage() throws IOException, InterruptedException
    {
        ProcessWrapper process = launchAgent();

        Client client = new Client(KryoHelper.writeBufferSize, KryoHelper.objectBufferSize);
        KryoHelper.registerTypes(client.getKryo());
        client.setTimeout(10000);
        client.start();
        client.connect(5000, "localhost", 54555);
        client.sendTCP(new AgentKillInstruction(-666));
        process.waitFor();

        assertThat(process.getProcess().exitValue(), is(-666));

//        System.out.println(process.getOutput());
//        System.err.println(process.getError());
//
//        assertThat(process.getOutput(), is(containsString("Agent is bound")));
//        assertThat(process.getError().isEmpty(), is(true));
    }
}

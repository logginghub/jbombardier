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

package com.jbombardier.agent;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.ProcessWrapper;
import com.jbombardier.common.AgentInstruction;
import com.jbombardier.common.KryoHelper;
import com.jbombardier.common.TestInstruction;

public class AbstractAgentBase
{
    private List<ProcessWrapper> processes = new ArrayList<ProcessWrapper>();

    protected AgentInstruction createAgentInstruction(String classname)
    {
        TestInstruction testInstruction = new TestInstruction();
        testInstruction.setClassname(classname);
        testInstruction.setTargetThreads(1);
        
        ArrayList<TestInstruction> testInstructions = new ArrayList<TestInstruction>();        
        testInstructions.add(testInstruction);
        
        AgentInstruction agentInstruction = new AgentInstruction();
        agentInstruction.setHost("localhost");        
        agentInstruction.setTestInstructions(testInstructions);
        
        return agentInstruction;
    }
    
    public Client connectClient(final Bucket<Object> bucket) throws IOException
    {
        Client client = new Client(KryoHelper.writeBufferSize, KryoHelper.objectBufferSize);
        KryoHelper.registerTypes(client.getKryo());
        client.setTimeout(10000);
        client.start();
        client.addListener(new Listener()
        {
            @Override public void received(Connection connection, Object object)
            {
                bucket.add(object);
            }
        });
        client.connect(5000, "localhost", 54555);
        return client;
    }

    public ProcessWrapper launchAgent() throws IOException
    {
        return launchAgent(false);
    }
    
    public ProcessWrapper launchAgent(boolean echo) throws IOException
    {
        List<String> command = new ArrayList<String>();
        command.add("java");
        command.add("-cp");
        command.add("temp\\test\\classes;lib\\kryonet-1.04-all.jar;D:\\Development\\mavenRepository\\com\\vertexlabs\\utils\\1.0.4-SNAPSHOT\\utils-1.0.4-SNAPSHOT.jar");
        command.add("com.jbombardier.agent.Agent");

        ProcessWrapper process = ProcessWrapper.execute(command, echo);
        processes.add(process);
        return process;
    }

    @Before public void setup()
    {
        File src = new File("target/classes");
        File dest = new File("temp/test/classes");

        FileUtils.deleteFolderAndContents(dest);

        FileUtils.recursiveCopy(src, dest, new FileFilter()
        {
            public boolean accept(File pathname)
            {
                if (pathname.getAbsolutePath().contains("performance\\agent") || pathname.getAbsolutePath().contains("performance\\common"))
                {
                    if (pathname.getName().endsWith(".class"))
                    {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @After public void tearDown()
    {
        for (ProcessWrapper processWrapper : processes)
        {         
            processWrapper.getProcess().destroy();
            try
            {
                processWrapper.getProcess().waitFor();
            }
            catch (InterruptedException e)
            {
            }
            
            System.out.println("-------------------------- Agent output --------------------------");
//            System.out.println(processWrapper.getOutput());
            System.out.println("------------------------------------------------------------------");
            System.out.println("-------------------------- Agent error ---------------------------");
//            System.err.println(processWrapper.getError());
            System.out.println("------------------------------------------------------------------");
        }
    }
}

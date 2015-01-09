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

package com.jbombardier.console.sample;

import java.net.InetSocketAddress;

import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;
import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;

public class LoggingHubTest {

    public static class Writer extends PerformanceTestAdaptor {

        private SocketClient client;
        private SocketClientManager manager;
        private int messageCount = 0;
        private DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        private LoggingMessage message = new LogEventMessage(event);

        @Override public void setup(TestContext pti) throws Exception {
            super.setup(pti);

            client = new SocketClient();
            client.addConnectionPoint(new InetSocketAddress("localhost", 58770));
            client.setAutoSubscribe(false);

            manager = new SocketClientManager(client);
            manager.start();
        }

        @Override public void beforeIteration(TestContext pti) throws Exception {
            event.setLocalCreationTimeMillis(System.currentTimeMillis());
            event.setMessage("Log event " + messageCount);
            messageCount++;
        }

        @Override public void runIteration(TestContext pti) throws Exception {
            client.send(message);
        }

        @Override public void teardown(TestContext pti) throws Exception {
            manager.stop();
            client.close();
        }
    }

    public static class Reader extends PerformanceTestAdaptor {

        private SocketClient client;
        private SocketClientManager manager;

        private volatile int messageCount = 0;
        private int batchStart = 0;

        @Override public void setup(TestContext pti) throws Exception {
            super.setup(pti);

            client = new SocketClient();
            client.addConnectionPoint(new InetSocketAddress("localhost", 58770));
            client.setAutoSubscribe(true);

            client.addLogEventListener(new LogEventListener() {
                @Override public void onNewLogEvent(LogEvent event) {
                    messageCount++;
                }
            });

            manager = new SocketClientManager(client);
            manager.start();
        }

        @Override public void beforeIteration(TestContext pti) throws Exception {
            batchStart = messageCount;
        }

        @Override public void runIteration(TestContext pti) throws Exception {
            while (messageCount < batchStart + 1000) {
                pti.sleep(10);
            }
        }

        @Override public void teardown(TestContext pti) throws Exception {
            manager.stop();
            client.close();
        }
    }

    public static void main(String[] args) {
        JBombardierConfigurationBuilder.configurationBuilder().testName("LoggingHubTest")
                 .addTest(JBombardierConfigurationBuilder.test(LoggingHubTest.Writer.class).name("Log event writer").targetRate(1000).rateStep(100).threads(1).threadStep(1))
                 .addTest(JBombardierConfigurationBuilder.test(LoggingHubTest.Reader.class).name("Log event reader").targetRate(1).rateStep(1).threads(2).threadStep(1).threadStepTime(1000))
                 .addEmbeddedAgent()
                 .resultRepository("localhost")
                 .autostart(1)
                 .warmupTime(5000)
                 .testDuration(10 /** 60*/ * 1000)
                 .execute();
    }

}

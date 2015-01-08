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

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.messaging.SocketClientManagerListener;
import com.logginghub.logging.modules.PatternCollection;
import com.logginghub.logging.utils.AggregatedPatternParser;
import com.logginghub.logging.utils.CompiledPattern;
import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.logginghub.utils.*;
import com.logginghub.utils.logging.Logger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 13/11/14.
 */
public class LoggingHubStatisticCapture extends BaseStatisticCapture {

    private final static Logger logger = Logger.getLoggerFor(LoggingHubStatisticCapture.class);
    private SocketClient client;
    private SocketClientManager manager;
    private TimeProvider timeProvider = new SystemTimeProvider();
    private String hosts;
    private List<CapturePattern> capturePatterns = new ArrayList<CapturePattern>();
    private PatternCollection patternCollection = new PatternCollection();

    @Override public void start() {

        client = new SocketClient();

        List<InetSocketAddress> inetSocketAddresses = NetUtils.toInetSocketAddressList(hosts,
                                                                                       VLPorts.getSocketHubDefaultPort());

        client.addConnectionPoints(inetSocketAddresses);
        client.setAutoSubscribe(true);
        client.setAutoGlobalSubscription(true);

        final Map<Integer, CapturePattern> patternsByID = new HashMap<Integer, CapturePattern>();
        final Map<Integer, CompiledPattern> compiledPatternsByID = new HashMap<Integer, CompiledPattern>();

        int patternID = 0;

        for (CapturePattern capturePattern : capturePatterns) {

            Pattern model = new Pattern();
            model.setName(capturePattern.path);
            model.setPattern(capturePattern.pattern);
            model.setPatternID(patternID++);

            CompiledPattern compiledPattern = new CompiledPattern(model);
            compiledPatternsByID.put(model.getPatternID(), compiledPattern);

            patternCollection.add(model);
            patternsByID.put(model.getPatternID(), capturePattern);
        }

        patternCollection.addDestination(new Destination<PatternisedLogEvent>() {
            @Override public void send(PatternisedLogEvent patternisedLogEvent) {
                int patternID = patternisedLogEvent.getPatternID();
                CapturePattern pattern = patternsByID.get(patternID);

                CompiledPattern compiledPattern = compiledPatternsByID.get(patternID);

                AggregatedPatternParser parser = new AggregatedPatternParser();
                parser.parse(pattern.path, compiledPattern.getValueStripper());

                String fullPath = parser.format(patternisedLogEvent);

                String values = pattern.values;
                String[] split = values.split(",");
                for (String s : split) {

                    int index = compiledPattern.getValueStripper().getLabelIndex(s);
                    String value = patternisedLogEvent.getVariable(index);

                    LoggingHubStatisticCapture.this.send(new CapturedStatistic(getTimeProvider().getTime(),
                                                                               fullPath + "/" + s,
                                                                               value));

                }
            }
        });

        client.addLogEventListener(new LogEventListener() {
            @Override public void onNewLogEvent(LogEvent event) {
                patternCollection.send(event);
            }
        });

        manager = new SocketClientManager(client);
        manager.addSocketClientManagerListener(new SocketClientManagerListener() {
            @Override public void onStateChanged(SocketClientManager.State fromState,
                                                 SocketClientManager.State toState) {
                send(new CapturedStatistic(timeProvider.getTime(), "hubcapture/connectionstate/", toState.name()));
            }
        });

        logger.info("Starting logging hub statistics capture connection...");
        manager.start();

    }

    @Override protected void doCapture() {
        // Nothing to do, this is purely async
    }

    @Override public void stop() {
        if (manager != null) {
            manager.stop();
            manager = null;
        }

        if (client != null) {
            client.close();
            client = null;
        }
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override public void configure(Metadata properties) {
        this.hosts = properties.getString("hubConnectionPoints");
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public List<CapturePattern> getCapturePatterns() {
        return capturePatterns;
    }

    public final static class CapturePattern {
        public String pattern;
        public String path;
        public String values;
    }
}

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

package com.jbombardier.console.statisticcapture;

import com.j256.simplejmx.client.JmxClient;
import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.logginghub.utils.*;
import com.logginghub.utils.logging.Logger;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 13/11/14.
 */
public class JMXStatisticCapture extends BaseStatisticCapture {

    private final static Logger logger = Logger.getLoggerFor(JMXStatisticCapture.class);
    private String username;
    private String password;
    private List<CaptureElement> captureElements = new ArrayList<CaptureElement>();

    private Map<InetSocketAddress, JmxClient> clients = new HashMap<InetSocketAddress, JmxClient>();
    private List<InetSocketAddress> connectionPoints;

    private void setupCaptures(String captures) {
        String[] split = captures.split("\\|");
        for (String s : split) {
            String[] split1 = s.split("/");

            CaptureElement element = new CaptureElement();
            element.path = split1[0].trim();
            element.objectName = split1[1].trim();
            element.attribute = split1[2].trim();

            captureElements.add(element);
        }
    }

    @Override public void configure(Metadata properties) {
        setConnectionPoints(properties.getString("connectionPoints"));
        username = properties.getString("username");
        password = properties.getString("password");
        setupCaptures(properties.getString("captures"));
    }

    @Override protected void doCapture() {
        for (InetSocketAddress connectionPoint : connectionPoints) {
            JmxClient client = getClient(connectionPoint);

            for (CaptureElement element : captureElements) {
                try {
                    String result = client.getAttribute(new ObjectName(element.objectName), element.attribute)
                                          .toString();
                    String path = element.path;

                    path = path.replace("{target}", connectionPoint.getHostName() + ":" + connectionPoint.getPort());

                    send(new CapturedStatistic(getTimeProvider().getTime(), path, result));
                } catch (MalformedObjectNameException e) {
                    logger.warn(e, "One of your jmx captures isn't working : {}", e.getMessage());
                } catch (Exception e) {
                    logger.warn(e,"Failed to capture JMX setting, resetting the client connection");
                    closeClient(connectionPoint);
                }
            }
        }
    }

    private void closeClient(InetSocketAddress connectionPoint) {
        synchronized (clients) {
            JmxClient jmxClient = clients.remove(connectionPoint);
            if (jmxClient != null) {
                jmxClient.close();
            }
        }
    }


    private JmxClient getClient(InetSocketAddress connectionPoint) {
        JmxClient jmxClient = null;
        synchronized (clients) {
            jmxClient = clients.get(connectionPoint);
            if (jmxClient == null) {
                try {
                    if (StringUtils.isNotNullOrEmpty(username)) {
                        logger.info("Connecting to {}:{} using username and password", connectionPoint.getHostName(), connectionPoint.getPort());
                        jmxClient = new JmxClient(JmxClient.generalJmxUrlForHostNamePort(connectionPoint.getHostName(), connectionPoint.getPort()));
                    } else {
                        logger.info("Connecting to {}:{} (no authentication)", connectionPoint.getHostName(), connectionPoint.getPort());
                        jmxClient = new JmxClient(connectionPoint.getHostName(), connectionPoint.getPort());
                    }

                    clients.put(connectionPoint, jmxClient);

                } catch (JMException e) {
                    logger.info(e, "Failed JMX connection to {} : {}", connectionPoint, e.getMessage());
                }

                logger.info("Connected to {}", connectionPoint);
            }
        }

        return jmxClient;

    }

//    private void jmxClientApproach() {
//
//        try {
//            if (client == null) {
//                if (StringUtils.isNotNullOrEmpty(username)) {
//                    logger.info("Connecting to {}:{} using username and password", host, port);
//                    client = new JmxClient(JmxClient.generalJmxUrlForHostNamePort(host, port), username, password);
//                } else {
//                    logger.info("Connecting to {}:{} (no authentication)", host, port);
//                    client = new JmxClient(host, port);
//                }
//
//                logger.info("Connected");
//            }
//
//
//            for (CaptureElement element : captureElements) {
//                String result = client.getAttribute(new ObjectName(element.objectName), element.attribute).toString();
//                String path = element.path;
//
//                send(new CapturedStatistic(getTimeProvider().getTime(), path, result));
//            }
//
//        } catch (MalformedObjectNameException e) {
//            closeClient();
//        } catch (JMException e) {
//            closeClient();
//        } catch (Exception e) {
//            closeClient();
//        } finally {
//
//        }
//    }

//    private void closeClient() {
//        if (client != null) {
//            client.close();
//            client = null;
//        }
//    }

    public void setConnectionPoints(String connectionPoints) {
        this.connectionPoints = NetUtils.toInetSocketAddressList(connectionPoints, 1801);
    }

    public void add(CaptureElement element) {
        captureElements.add(element);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static class CaptureElement {
        public String path;
        public String objectName;
        public String attribute;
    }

//    public static void main(String[] args) {
//        final JMXStatisticCapture capture = new JMXStatisticCapture();
//
//        JMXStatisticCaptureConfiguration configuration = new JMXStatisticCaptureConfiguration();
//        configuration.setHost("localhost");
//        configuration.setPort(12345);
//        configuration.setCaptures(
//                "Uptime/java.lang:type=Runtime/Uptime|CacheValid/java.lang:type=MemoryManager,name=CodeCacheManager/Valid|hitCount/logginghub:name=LookupCache/hitCount");
//
//        capture.configure(configuration);
//
//        capture.addDestination(new Destination<CapturedStatistic>() {
//            @Override public void send(CapturedStatistic statistic) {
//                Out.out("{}", statistic);
//            }
//        });
//
//        WorkerThread.everySecond("Test", new Runnable() {
//            @Override public void run() {
//                capture.doCapture();
//            }
//        });
//
//
//    }
}
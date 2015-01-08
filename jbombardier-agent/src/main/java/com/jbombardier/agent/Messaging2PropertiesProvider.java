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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.logginghub.messaging2.api.MessagingInterface;
import com.logginghub.utils.MutableInt;
import com.jbombardier.common.AgentPropertyEntryRequest;
import com.jbombardier.common.AgentPropertyEntryResponse;
import com.jbombardier.common.AgentPropertyRequest;
import com.jbombardier.common.AgentPropertyResponse;
import com.jbombardier.common.DataBucket;
import com.jbombardier.common.DataStrategy;
import com.jbombardier.common.PropertiesStrategy;
import com.jbombardier.common.PropertyEntry;

public class Messaging2PropertiesProvider implements PropertiesStrategy {

    private Random random = new Random();

    private final MessagingInterface client;
    private final String propertyRequestChannel;

    private Map<String, String> nearCache = new ConcurrentHashMap<String, String>();
    private Map<String, Map<String, PropertyEntry>> propertyEntryNearCacheByDataSourceName = new ConcurrentHashMap<String, Map<String, PropertyEntry>>();

    private Map<String, PropertyEntry> fixedThreadStrategyNearCache = new ConcurrentHashMap<String, PropertyEntry>();

    private Map<String, DataBucket> data;
    private Map<String, MutableInt> currentIndex = new HashMap<String, MutableInt>();

    public Messaging2PropertiesProvider(MessagingInterface client, String propertyRequestChannel) {
        this.client = client;
        this.propertyRequestChannel = propertyRequestChannel;
    }

    public int getIntegerProperty(String propertyName, int defaultValue) {
        String stringValue = getStringProperty(propertyName, Integer.toString(defaultValue));
        int integerValue = Integer.parseInt(stringValue);
        return integerValue;
    }
    
    public boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        String stringValue = getStringProperty(propertyName, Boolean.toString(defaultValue));
        boolean booleanValue = Boolean.parseBoolean(stringValue);
        return booleanValue;
    }

    public String getStringProperty(String propertyName, String defaultValue) {
        String propertyValue;
        synchronized (nearCache) {
            propertyValue = nearCache.get(propertyName);
            if (propertyValue == null) {

                AgentPropertyRequest request = new AgentPropertyRequest(propertyName, Thread.currentThread().getName());
                AgentPropertyResponse response = client.sendRequest(propertyRequestChannel, request);
                propertyValue = response.getPropertyValue();
                if (propertyValue == null) {
                    if (defaultValue == null) {
                        throw new RuntimeException(String.format("Property '%s' wasn't found on the controller, please check your configuration",
                                                                 propertyName));
                    }
                    else {
                        propertyValue = defaultValue;
                        
                        // Might as well cache the property, as its not able to change at the moment
                        nearCache.put(propertyName, defaultValue);
                    }
                }
                else {
                    nearCache.put(propertyName, propertyValue);
                }
            }
        }

        return propertyValue;
    }

    public synchronized PropertyEntry getPropertyEntry(String dataSource) {

        PropertyEntry entry;

        DataBucket dataBucket;
        synchronized (data) {
            dataBucket = data.get(dataSource);
        }

        if (dataBucket == null) {
            throw new RuntimeException("No datasource called '" +
                                       dataSource +
                                       "' was provided, please check your test and your configuration have the correct data source names");
        }

        DataStrategy strategy = dataBucket.getStrategy();
        switch (strategy) {
            case fixedThread:
                entry = getFixedThreadStrategy(dataSource);
                break;

            case pooledAgent:
            case pooledGlobal:
            case pooledThread:
                List<String[]> values = dataBucket.getValues();

                entry = new PropertyEntry();
                entry.setHeaders(dataBucket.getColumns());

                // TODO : who knows?!
                MutableInt mutableInt;
                synchronized (currentIndex) {
                    mutableInt = currentIndex.get(dataSource);
                    if (mutableInt == null) {
                        mutableInt = new MutableInt(random.nextInt(values.size()));
                        currentIndex.put(dataSource, mutableInt);
                    }
                }

                entry.setValues(values.get(mutableInt.getValue()));
                mutableInt.increment();
                if (mutableInt.getValue() == values.size()) {
                    mutableInt.setValue(0);
                }

                break;

            // TODO : implement pooled thread
            // case pooledThread:
            // break;

            default:
                throw new UnsupportedOperationException("Unsupported data strategy : " + strategy);
        }

        return entry;
    }

    private PropertyEntry getFixedThreadStrategy(String dataSource) {

        PropertyEntry propertyEntry;

        synchronized (fixedThreadStrategyNearCache) {

            String key = dataSource + ":" + Thread.currentThread().getName();
            propertyEntry = fixedThreadStrategyNearCache.get(key);
            if (propertyEntry == null) {
                AgentPropertyEntryRequest request = new AgentPropertyEntryRequest(dataSource, Thread.currentThread().getName());
                AgentPropertyEntryResponse response = client.sendRequest(propertyRequestChannel, request);
                propertyEntry = response.getPropertyEntry();
                fixedThreadStrategyNearCache.put(key, propertyEntry);
            }
        }

        return propertyEntry;

    }

    public void setupAgentCachedData(Map<String, DataBucket> data) {
        this.data = data;
    }

    public void setStartingProperties(Map<String, String> properties) {
        if (properties != null) {
            nearCache.putAll(properties);
        }
    }


}

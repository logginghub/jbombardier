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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DefaultPropertiesProvider implements PropertiesStrategy {

    private Random random = new Random();
    private Map<String, List<PropertyEntry>> dataSources = new HashMap<String, List<PropertyEntry>>();
    private Map<String, String> properties = new HashMap<String, String>();

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public int getIntegerProperty(String propertyName, int defaultValue) {
        int intValue;
        String value = getStringProperty(propertyName, null);
        if (value != null) {
            intValue = Integer.parseInt(value);
        }
        else {
            intValue = defaultValue;
        }
        return intValue;
    }

    public boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        boolean booleanValue;
        String value = getStringProperty(propertyName, null);
        if (value != null) {
            booleanValue = Boolean.parseBoolean(value);
        }
        else {
            booleanValue = defaultValue;
        }
        return booleanValue;
    }

    public String getStringProperty(String propertyName, String defaultValue) {
        String value = properties.get(propertyName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public PropertyEntry getPropertyEntry(String dataSource) {

        PropertyEntry entry;
        List<PropertyEntry> list = dataSources.get(dataSource);
        if (list != null) {
            entry = list.get(random.nextInt(list.size()));
        }
        else {
            throw new RuntimeException("Datasource '" + dataSource + "' did not have an property entries assigned to it; please check your configuration.");
        }

        return entry;
    }

    public void setPropertyEntrys(String dataSourceName, List<PropertyEntry> propertyEntries) {
        dataSources.put(dataSourceName, propertyEntries);
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}

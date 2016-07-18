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

import com.logginghub.utils.ResourceUtils;

/**
 * @author James
 */
public class CsvPropertiesProvider implements PropertiesStrategy {

    private String resourcePath;
    private String content;
    private String[] lines;
    private String[] header;
    private int currentLine = 0;

    public CsvPropertiesProvider(String resourcePath) {
        this.resourcePath = resourcePath;
        content = ResourceUtils.read(resourcePath);
        lines = content.split("\r\n");
        header = lines[0].split(",");
        currentLine = 1;
    }

    public int getIntegerProperty(String propertyName, int defaultValue) {
        return 0;
    }

    public boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        return false;
    }

    public String getStringProperty(String propertyName, String defaultValue) {
        return null;
    }

    public PropertyEntry getPropertyEntry(String dataSource) {

        String line = lines[currentLine];
        currentLine++;
        if (currentLine == lines.length) {
            currentLine = 1;
        }

        String[] values = line.split(",");
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }

        PropertyEntry entry = new PropertyEntry();
        entry.setHeaders(header);
        entry.setValues(values);

        return entry;
    }

}

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

import java.util.Arrays;

public class PropertyEntry {

    private String[] values;
    private String[] headers;

    // We have to maintain a second level array of the original indicies as we
    // are sorting the labels in order to do a binary search. Bear in mind this
    // code might be executing in test code, so it needs to be fast.
    private int[] indicies;

    public String getString(String string) {

        int index = Arrays.binarySearch(headers, string);
        if (index < 0) {
            throw new RuntimeException("Property '" + string + "' was not foud in this property entry, please check your test and your data file to ensure the column header names match up!");
        }

        String value = values[indicies[index]];
        return value;
    }

    public int getInteger(String string) {

        String result = getString(string);
        int intValue = Integer.parseInt(result);
        return intValue;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public void setHeaders(String[] headers) {

        String[] copy = Arrays.copyOf(headers, headers.length);
        Arrays.sort(copy);
        indicies = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            String value = copy[i];
            indicies[i] = findIndex(headers, value);
        }

        this.headers = copy;
    }

    private int findIndex(String[] headers, String value) {

        int found = -1;
        for (int i = 0; i < headers.length && found < 0; i++) {
            if (headers[i].equals(value)) {
                found = i;
            }
        }
        return found;
    }

    @Override public String toString() {
        return "PropertyEntry [headers=" + Arrays.toString(headers) + ", values=" + Arrays.toString(values) + "]";
    }

}

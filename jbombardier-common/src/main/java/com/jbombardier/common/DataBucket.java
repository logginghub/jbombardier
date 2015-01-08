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
import java.util.List;

/**
 * Encapsulates a sub-set of Data that can be distributed in advance to an agent
 * to populate their Data near cache with data for its threads to use.
 * 
 * @author James
 * 
 */
public class DataBucket {

    private List<String[]> values = null;
    private String[] columns;
    private int[] indices;
    private String dataSourceName;
    private DataStrategy strategy;

    public DataBucket() {}

    public DataBucket(String key) {
        setDataSourceName(key);
    }
    
    public void setStrategy(DataStrategy strategy) {
        this.strategy = strategy;
    }
    
    public DataStrategy getStrategy() {
        return strategy;
    }

    public List<String[]> getValues() {
        return values;
    }

    public void setValues(List<String[]> values) {
        this.values = values;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public int[] getIndices() {
        return indices;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public void addRecord(String[] record) {
        if(values == null){
            values = new ArrayList<String[]>();
        }
        values.add(record);
    }

    @Override public String toString() {
        return "DataBucket [dataSourceName=" + dataSourceName + ", strategy=" + strategy + "]";
    }

    
}

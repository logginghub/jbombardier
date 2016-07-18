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

package com.jbombardier.console.model;

import java.util.Arrays;

import com.jbombardier.common.DataBucket;
import com.jbombardier.common.DataStrategy;
import com.jbombardier.xml.CsvProperty;

/**
 * The model-side representation of the csv data sources. Maps to the
 * {@link CsvProperty} object in the xml configuration world, and also to
 * {@link DataBucket} which is used to ferry subsets of the data to the agents
 * at the start of the test.
 * 
 * @author James
 * 
 */
public class DataSource {

    private DataStrategy strategy;
    private String dataSourceName;
    private String[] header;
    private String[][] values;

    public DataSource(String dataSourceName, DataStrategy strategy) {
        super();
        this.strategy = strategy;
        this.dataSourceName = dataSourceName;
    }

    public DataStrategy getStrategy() {
        return strategy;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public String[] getHeader() {
        return header;
    }

    public void setHeader(String[] header) {
        this.header = header;
    }

    public void setStrategy(DataStrategy strategy) {
        this.strategy = strategy;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public void setData(String[] header, String[][] values) {
        this.header = header;
        this.values = values;
        
    }
    
    public String[][] getValues() {
        return values;
    }

    public String[] getRecord(int currentRow) {
        return values[currentRow];         
    }

    @Override public String toString() {
        return "DataSource [strategy=" + strategy + ", dataSourceName=" + dataSourceName + ", header=" + Arrays.toString(header) + "]";
    }
    
    
}

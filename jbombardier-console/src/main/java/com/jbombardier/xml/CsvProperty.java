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

package com.jbombardier.xml;

import javax.xml.bind.annotation.XmlAttribute;

public class CsvProperty {
    
    private String name;
    private String csvfile;
    private String strategy;
    
    public CsvProperty(String name, String csvfile, String strategy)
    {
        this.name = name;
        this.csvfile = csvfile;
        this.strategy = strategy;
    }

    public CsvProperty() {
     
    }

    @XmlAttribute
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    @XmlAttribute
    public String getCsvfile()
    {
        return csvfile;
    }
    
    public void setCsvfile(String csvfile)
    {
        this.csvfile = csvfile;
    }
    
    @XmlAttribute
    public String getStrategy() {
        return strategy;
    }
    
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}

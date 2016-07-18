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

package com.jbombardier.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;

public class AgentListProperty
{
    // XML attributes
    private String name;
    private String values;
    private String separator;
    private boolean trim;
    private String order;
    private String strategy;

    // Internals
    private Order orderEnum;
    private Strategy strategyEnum;
    private boolean initialised;
    private List<String> allValues = new ArrayList<String>();
    private List<String> unusedValues = new ArrayList<String>();
    private Map<String, String> agentToAssignedValueMap = new HashMap<String, String>();
    private Map<String, String> agentThreadToAssignedValueMap = new HashMap<String, String>();

    public enum Order
    {
        random,
        fifo
    };

    public enum Strategy
    {
        consistentPerAgent,
        consistentPerThread,
        alwaysUnique
    }

    public AgentListProperty(String name, String csvList)
    {
        this.name = name;
        this.values = csvList;
        this.separator = ",";
        this.trim = true;
        setOrder(Order.fifo.name());
        setStrategy(Strategy.consistentPerThread.name());
    }

    public AgentListProperty()
    {

    }

    @XmlAttribute public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @XmlAttribute public String getValues()
    {
        return values;
    }

    public void setValues(String values)
    {
        this.values = values;
    }

    @XmlAttribute public String getSeparator()
    {
        return separator;
    }

    public void setSeparator(String separator)
    {
        this.separator = separator;
    }

    @XmlAttribute public boolean getTrim()
    {
        return trim;
    }

    public void setTrim(boolean trim)
    {
        this.trim = trim;
    }

    @XmlAttribute public String getOrder()
    {
        return order;
    }

    public void setOrder(String order)
    {
        this.order = order;
        this.orderEnum = Order.valueOf(order);
    }

    @XmlAttribute public String getStrategy()
    {
        return strategy;
    }

    public void setStrategy(String strategy)
    {
        this.strategy = strategy;
        this.strategyEnum = Strategy.valueOf(strategy);
    }

    public String getValueFor(String agentName, String threadName)
    {
        lazyInit();
        String value;
        switch (strategyEnum)
        {
            case alwaysUnique:
            {
                value = assignNextValue();
                break;
            }
            case consistentPerAgent:
            {
                value = agentToAssignedValueMap.get(agentName);
                if (value == null)
                {
                    value = assignNextValue();
                    agentToAssignedValueMap.put(agentName, value);
                }

                break;
            }
            case consistentPerThread:
            {
                String key = agentName + threadName;
                value = agentThreadToAssignedValueMap.get(key);
                if (value == null)
                {
                    value = assignNextValue();
                    agentThreadToAssignedValueMap.put(key, value);
                }

                break;
            }
            default:
                throw new RuntimeException("Unsupported strategy " + strategyEnum);
        }
        return value;
    }

    private String assignNextValue()
    {
        String value;
        if (unusedValues.isEmpty())
        {
            throw new RuntimeException("We've run out of values for property '" + this.name + "'");
        }
        value = unusedValues.remove(0);
        return value;
    }

    private void lazyInit()
    {
        if (!initialised)
        {
            initialised = true;

            String[] split = values.split(separator);
            for (String valueWithSpaces : split)
            {
                String value;
                if (this.trim)
                {
                    value = valueWithSpaces.trim();
                }
                else
                {
                    value = valueWithSpaces;
                }

                allValues.add(value);
                unusedValues.add(value);
            }
        }
    }

}

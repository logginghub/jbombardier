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

public class AgentPropertyRequest
{
    private String propertyName;
    private String threadName;

    public AgentPropertyRequest(String propertyName, String threadName)
    {
        this.propertyName = propertyName;
        this.threadName = threadName;
    }

    public AgentPropertyRequest()
    {

    }

    public String getThreadName()
    {
        return threadName;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public String toString()
    {
        return String.format("AgentPropertyRequest: propertyName='%s' threadName='%s'", propertyName, threadName);
    }
}

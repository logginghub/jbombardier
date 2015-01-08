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

package com.jbombardier.console.model;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;

public class JSONHelper {

    private ObjectMapper mapper = new ObjectMapper();
    private ObjectWriter writer;

    public JSONHelper() {        
        mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enableDefaultTyping();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, As.WRAPPER_OBJECT); // all non-final types
        writer = mapper.writerWithDefaultPrettyPrinter();
    }
    
    public String toJSON(Object object) {
        String json;
        try {
            json = writer.writeValueAsString(object);
        }
        catch (JsonGenerationException e) {
            throw new RuntimeException(String.format("Failed to encode json"), e);
        }
        catch (JsonMappingException e) {
            throw new RuntimeException(String.format("Failed to encode json"), e);
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to encode json"), e);
        }
        return json;
    }
    
    public <T> T fromJSON(String json) {
        try {
            T t = (T) mapper.readValue(json, Object.class);
            return t;
        }
        catch (JsonGenerationException e) {
            throw new RuntimeException(String.format("Failed to decode json"), e);
        }
        catch (JsonMappingException e) {
            throw new RuntimeException(String.format("Failed to decode json"), e);
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to decode json"), e);
        }
    }
    
}

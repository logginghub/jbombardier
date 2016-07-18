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

package com.jbombardier.repository;

import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.InvalidReferenceEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.util.introspection.Info;

import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;

public class VelocityHelper {

    private VelocityEngine ve;

    public VelocityHelper() {
        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();

        final StringUtilsBuilder errors = new StringUtilsBuilder();
        EventCartridge eventCartridge = new EventCartridge();
        eventCartridge.addInvalidReferenceEventHandler(new InvalidReferenceEventHandler() {

            @Override public Object invalidGetMethod(Context context, String reference, Object object, String property, Info info) {
                errors.appendLine("Invalid get method : reference {} object {} property {} info {}", reference, object, property, info);
                return null;
            }

            @Override public boolean invalidSetMethod(Context context, String leftreference, String rightreference, Info info) {
                errors.appendLine("Invalid set method : leftReference {} rightReference {} info {}", leftreference, rightreference, info);
                return false;

            }

            @Override public Object invalidMethod(Context context, String reference, Object object, String method, Info info) {
                errors.appendLine("Invalid method : reference '{}' object '{}' method '{}' info [{}]", reference, object, method, info);
                return null;
            }
        });

        
//        context.attachEventCartridge(eventCartridge);
    }
    
    public VelocityContext buildContext() { 
        final VelocityContext context = new VelocityContext();
        return context;
    }
    
    public String help(VelocityContext context, String template){
        return process(context, template);
    }

    private String process(VelocityContext context, String template) {
        try {
            StringWriter writer = new StringWriter();
            ve.mergeTemplate(template, "UTF-8", context, writer);
            writer.close();
            return writer.toString();
        }
        catch (Exception e) {
            throw new FormattedRuntimeException("Failed to process template", e);
        }
    }

}

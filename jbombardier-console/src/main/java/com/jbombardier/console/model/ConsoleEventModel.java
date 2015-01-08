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

import java.util.Date;

import com.logginghub.utils.Is;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;

public class ConsoleEventModel {

    public enum Severity {
        Debug,
        Information,
        Warning,
        Severe
    }
    
    private Date time;
    private Severity severity;
    private String source;
    private String message = "[message was not set]";
    private String throwable;
    
    public String toString() {
        return StringUtils.format("{} : {} : {} : {}", Logger.toDateString(time.getTime()), severity, source, message);
    }
    
    public Date getTime() {
        return time;
    }
    
    public void setTime(Date time) {
        this.time = time;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        Is.notNull(message, "Message cannot be null for a console message");
        Is.not(message, "null", "Message cannot be 'null' for a console message");
        this.message = message;
    }

    public void setThrowable(String throwable) {
        this.throwable = throwable;
    }
    
    public String getThrowable() {
        return throwable;
    }
}

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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class AgentFailedInstruction {

    private String message;
    private String stackTrace;
    private String source;
    private String thread;

    public AgentFailedInstruction() {}

    public AgentFailedInstruction(String source, String thread, String message, Throwable throwable) {
        this.source = source;
        this.thread = thread;
        this.message = message;
        this.stackTrace = getStackTraceFromThrowable(throwable);
    }

    public static String getStackTraceFromThrowable(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }
    
    public String getSource() {
        return source;
    }
    
    public String getThread() {
        return thread;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public String getMessage() {
        return message;
    }
}

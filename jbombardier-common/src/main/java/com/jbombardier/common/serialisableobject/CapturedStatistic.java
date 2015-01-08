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

package com.jbombardier.common.serialisableobject;

import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * Created by james on 13/11/14.
 */
public class CapturedStatistic implements TimeProvider, SerialisableObject {

    private long time;
    private String path;
    private String value;

    public CapturedStatistic(long time, String path, String value) {
        this.time = time;
        this.path = path;
        this.value = value;
    }

    public CapturedStatistic() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    @Override public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("CapturedStatistic{");
        sb.append("time=").append(Logger.toDateString(time));
        sb.append(", path='").append(path).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override public void read(SofReader reader) throws SofException {
        time = reader.readLong(0);
        path = reader.readString(1);
        value = reader.readString(2);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, time);
        writer.write(1, path);
        writer.write(2, value);
    }
}

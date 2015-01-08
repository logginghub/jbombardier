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

public class RawDataSeries {

    private List<DataPoint> data = new ArrayList<DataPoint>();

    public void addErrorDataPoint(long currentTimeMillis, long elapsedNanos, String message) {
        data.add(new ErrorDataPoint(currentTimeMillis, elapsedNanos, message));
    }

    public void addDataPoint(long currentTimeMillis, long elapsedNanos) {
        data.add(new DataPoint(currentTimeMillis, elapsedNanos));
    }

    public List<DataPoint> getData() {
        return data;        
    }

}

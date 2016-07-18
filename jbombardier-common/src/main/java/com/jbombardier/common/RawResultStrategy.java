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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RawResultStrategy implements ResultStrategy {

    private Map<String, RawDataSeries> dataSeries = new HashMap<String, RawDataSeries>();

    public void onNewFailResult(String transactionID, long elapsedNanos, Throwable t) {
        RawDataSeries rawDataSeries = getDataSeries(transactionID);
        rawDataSeries.addErrorDataPoint(System.nanoTime(), elapsedNanos, toString(t));
    }

    private String toString(Throwable t) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        t.printStackTrace(printWriter);
        return result.toString();

    }

    public void onNewFailResult(String transactionID, long elapsedNanos, String message) {
        RawDataSeries rawDataSeries = getDataSeries(transactionID);
        rawDataSeries.addErrorDataPoint(System.nanoTime(), elapsedNanos, message);

    }

    public void onNewSuccessResult(String transactionID, long elapsedNanos) {
        RawDataSeries rawDataSeries = getDataSeries(transactionID);
        rawDataSeries.addDataPoint(System.currentTimeMillis(), elapsedNanos);
    }

    public RawDataSeries getDataSeries(String transactionID) {
        RawDataSeries rawDataSeries = dataSeries.get(transactionID);
        if (rawDataSeries == null) {
            rawDataSeries = new RawDataSeries();
            dataSeries.put(transactionID, rawDataSeries);
        }
        return rawDataSeries;
    }

    public Set<String> getTransactionSet() {
        Set<String> set = dataSeries.keySet();
        return set;
    }

}

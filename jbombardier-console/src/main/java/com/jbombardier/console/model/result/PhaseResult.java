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

package com.jbombardier.console.model.result;

import com.jbombardier.ControllerResultSnapshot;
import com.jbombardier.common.serialisableobject.CapturedStatistic;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of the results from a particular phase.
 */
public class PhaseResult {
    private List<CapturedStatistic> capturedStatistics = new ArrayList<CapturedStatistic>();
    private List<TransactionResult> transactionResults = new ArrayList<TransactionResult>();
    private List<ControllerResultSnapshot> controllerResults = new ArrayList<ControllerResultSnapshot>();
    private String phaseName;
    private long duration;
    private long warmup;

    public List<TransactionResult> getTransactionResults() {
        return transactionResults;
    }

    public List<CapturedStatistic> getCapturedStatistics() {
        return capturedStatistics;
    }

    public List<ControllerResultSnapshot> getControllerResults() {
        return controllerResults;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }


    public long getDuration() {
        return duration;
    }

    public long getWarmup() {
        return warmup;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setWarmup(long warmup) {
        this.warmup = warmup;
    }
}

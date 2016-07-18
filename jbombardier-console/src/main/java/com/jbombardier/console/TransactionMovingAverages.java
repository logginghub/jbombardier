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

package com.jbombardier.console;

import com.logginghub.utils.ConcurrentMovingAverage;

/**
 * 
 */
public class TransactionMovingAverages {

    private final static int defaultMovingAveragePoints = 5;
    private int movingAveragePoints = defaultMovingAveragePoints;

    private ConcurrentMovingAverage successfulTransactionCount = new ConcurrentMovingAverage(defaultMovingAveragePoints);
    private ConcurrentMovingAverage unsuccessfulTransactionCount = new ConcurrentMovingAverage(defaultMovingAveragePoints);
    private ConcurrentMovingAverage successfulTransactionDuration = new ConcurrentMovingAverage(defaultMovingAveragePoints);
    private ConcurrentMovingAverage unsuccessfulTransactionDuration = new ConcurrentMovingAverage(defaultMovingAveragePoints);
    private ConcurrentMovingAverage successfulTransactionTotalDuration = new ConcurrentMovingAverage(defaultMovingAveragePoints);

    public ConcurrentMovingAverage getSuccessfulTransactionDuration() {
        return successfulTransactionDuration;
    }

    public static int getDefaultMovingAveragePoints() {
        return defaultMovingAveragePoints;
    }

    public ConcurrentMovingAverage getSuccessfulTransactionTotalDuration() {
        return successfulTransactionTotalDuration;
    }

    public ConcurrentMovingAverage getUnsuccessfulTransactionDuration() {
        return unsuccessfulTransactionDuration;
    }

    public ConcurrentMovingAverage getSuccessfulTransactionCount() {
        return successfulTransactionCount;
    }

    public ConcurrentMovingAverage getUnsuccessfulTransactionCount() {
        return unsuccessfulTransactionCount;
    }

    public int getMovingAveragePoints() {
        return movingAveragePoints;
    }
}

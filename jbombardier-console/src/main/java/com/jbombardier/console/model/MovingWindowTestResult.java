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

package com.jbombardier.console.model;

import java.util.Iterator;

import com.logginghub.utils.CircularArrayList;
import com.jbombardier.common.AgentStats.TestStats;

public class MovingWindowTestResult {

    private long windowLength;

    public static class WindowElement<T> {
        public long time;
        public T element;

        public WindowElement(long time, T element) {
            super();
            this.time = time;
            this.element = element;
        }

    }

    private CircularArrayList<WindowElement<TestStats>> movingWindow = new CircularArrayList<WindowElement<TestStats>>();
    private long latest;
    private long earliest;

    public void setWindowLength(long windowLength) {
        this.windowLength = windowLength;
    }

    public void addTestsStats(long time, TestStats testStats) {

        if (time < latest) {
            throw new IllegalArgumentException("We dont support out of order entries yet");
        }

        movingWindow.add(new WindowElement<TestStats>(time, testStats));

        long windowCutoff = time - windowLength;

        while (movingWindow.peek().time < windowCutoff) {
            movingWindow.poll();
        }

        this.latest = time;
        this.earliest = movingWindow.get(0).time;

    }

    public int size() {
        return movingWindow.size();
    }

    public long getCurrentWindowDuration() {
        return latest - earliest;
    }

    public long getLatest() {
        return latest;
    }

    public long getEarliest() {
        return earliest;
    }

    public double getCurrentWindowSuccessRate() {

        long totalSuccessfulTransactions = 0;
        Iterator<WindowElement<TestStats>> iterator = movingWindow.iterator();
        while (iterator.hasNext()) {
            WindowElement<TestStats> windowElement = (WindowElement<TestStats>) iterator.next();

            totalSuccessfulTransactions += windowElement.element.transactionsSuccess;
        }

        double rate;
        long currentWindowDuration = getCurrentWindowDuration();
        if (currentWindowDuration == 0) {
            rate = Double.POSITIVE_INFINITY;
        }
        else {
            rate = totalSuccessfulTransactions * (1000.0 / getCurrentWindowDuration());
        }
        return rate;

    }

}

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

package com.jbombardier.console.results;

import java.util.Arrays;
import java.util.List;

import com.logginghub.utils.CircularArrayList;
import com.logginghub.utils.ThreadUtils;
import com.jbombardier.common.PerformanceTest;
import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;
import com.jbombardier.common.TestFactory;
import com.jbombardier.console.micro.JBMicroBenchmarker;

public class FullResultsHandler {

    // private TreeList orderedResults = new TreeList();

    // TODO : make this configurable
    private int maxResults = 1000000;
    private CircularArrayList<Long> orderedResults = new CircularArrayList<Long>(maxResults);
    // private List<Long> orderedResults = new ArrayList<Long>();
    private long[] sorted;

    private long total = 0;
    private int count = 0;

    public void add(List<Long> successResults) {
        for (Long value : successResults) {
            addResult(value);
        }
    }

    public void capture() {

        long[] copy = new long[orderedResults.size()];
        synchronized (orderedResults) {
            for (int i = 0; i < orderedResults.size(); i++) {
                copy[i] = orderedResults.get(i);
            }
        }

        Arrays.sort(copy);
        sorted = copy;
    }

    @SuppressWarnings("unchecked") public void addResult(long value) {
        synchronized (orderedResults) {
            orderedResults.add(value);
            // sortedResults.add(value);

            total += value;
            count++;

            if (orderedResults.size() > maxResults) {
                long removed = (Long) orderedResults.remove(0);
                // sortedResults.removeFast(removed);
                // total -= removed;
                // count--;
            }
        }
    }

    public double percentile(int percentile) {

        // TODO : this is a rubbish approximation!!
        double percentileResult;
        if (sorted.length < 100) {
            percentileResult = Double.NaN;
        }
        else {
            int index = (int) (sorted.length * (percentile / 100d));
            percentileResult = sorted[index];
        }

        return percentileResult;
    }

    public double mean() {
        return total / count;
    }

    public static void main(String[] args) {

        final FullResultsHandler handler = new FullResultsHandler();

        JBMicroBenchmarker benchmarker = new JBMicroBenchmarker();

        benchmarker.addTest("addResult", new TestFactory() {
            int count = 0;
            @Override public PerformanceTest createTest() {
                return new PerformanceTestAdaptor() {
                    @Override public void runIteration(TestContext pti) throws Exception {
                        handler.addResult(count++);
                    }
                };
            }
        });
        
        benchmarker.addTest("percentile", new TestFactory() {
            @Override public PerformanceTest createTest() {
                return new PerformanceTestAdaptor() {
                    @Override public void runIteration(TestContext pti) throws Exception {
                        handler.percentile(90);
                    }
                };
            }
        });

        benchmarker.addTest("capture", new TestFactory() {
            @Override public PerformanceTest createTest() {
                return new PerformanceTestAdaptor() {
                    @Override public void runIteration(TestContext pti) throws Exception {
                        handler.capture();
                        ThreadUtils.sleep(1000);
                    }
                };
            }
        });

        final long[] longs = new long[1000000];

        benchmarker.addTest("arraySort", new TestFactory() {
            @Override public PerformanceTest createTest() {
                return new PerformanceTestAdaptor() {
                    @Override public void runIteration(TestContext pti) throws Exception {
                        Arrays.sort(longs);
                    }
                };
            }
        });

        benchmarker.start();

    }
}

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

public class AgentStats {

    private String agentName;
    private List<TestStats> testStats = new ArrayList<AgentStats.TestStats>();

    public static class TestStats {

        private String testName;
        private boolean isTransaction;
        private int threadCount;
        public long transactionsSuccess;
        public long transactionsFailed;
        public long totalDurationSuccess;
        public long totalDurationFailed;
        public long sampleDuration;
        public List<Long> successResults = new ArrayList<Long>(100);
        public List<Long> failResults = new ArrayList<Long>(100);
        private String transactionName = "";

        /**
         * The total of the total durations (before + iteration) for the
         * successful tests.
         */
        public long totalDurationTotalSuccess;

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public void setThreadCount(int threadCount) {
            this.threadCount = threadCount;
        }

        public String getTestName() {
            return testName;
        }

        public int getThreadCount() {
            return threadCount;
        }

        @Override public String toString() {
            return "TestStats [testName=" +
                   testName +
                   ", transactionName=" +
                   transactionName +
                   ", isTransaction=" +
                   isTransaction +
                   ", threadCount=" +
                   threadCount +
                   ", transactionsSuccess=" +
                   transactionsSuccess +
                   ", transactionsFailed=" +
                   transactionsFailed +
                   ", totalDurationSuccess=" +
                   totalDurationSuccess +
                   ", totalDurationFailed=" +
                   totalDurationFailed +
                   ", sampleDuration=" +
                   sampleDuration +
                   "]";
        }

        public void fromBasicTestStats(BasicTestStats basicTestStats) {
            this.transactionsSuccess = basicTestStats.transactionsSuccess;
            this.totalDurationFailed = basicTestStats.totalDurationFailed;
            this.totalDurationSuccess = basicTestStats.totalDurationSuccess;
            this.totalDurationTotalSuccess = basicTestStats.totalDurationTotalSuccess;
            this.transactionsFailed = basicTestStats.transactionsFailed;
            this.sampleDuration = System.currentTimeMillis() - basicTestStats.sampleTimeStart;
            this.successResults.addAll(basicTestStats.switchOutSuccessResults());
            this.failResults.addAll(basicTestStats.switchOutFailResults());
        }

        public void setTransaction(boolean isTransaction) {
            this.isTransaction = isTransaction;
        }

        public boolean isTransaction() {
            return isTransaction;
        }

        public void setTransactionName(String transactionName) {
            this.transactionName = transactionName;
        }

        public String getTransactionName() {
            return transactionName;
        }

        public TestKey getKey() {
            TestKey key = new TestKey(testName, transactionName);
            return key;
        }

        public void dump() {
            System.out.println(toString());
            System.out.print("Successes { ");
            String div = "";
            for (Long success : successResults) {
                System.out.print(div);
                System.out.print(success);
                div = ",";
            }
            System.out.print(" }");
            System.out.println();
        }
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public void addTestStats(TestStats testStats) {
        this.testStats.add(testStats);
    }

    public List<TestStats> getTestStats() {
        return testStats;
    }

    public String getAgentName() {
        return agentName;
    }

    @Override public String toString() {
        return "AgentStats [agentName=" + agentName + ", testStats=" + testStats + "]";
    }

    public void dump() {
        System.out.println("Agent stats for agent : " + agentName);
        for (TestStats stat : testStats) {
            stat.dump();
        }
    }

}

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

import java.util.ArrayList;
import java.util.List;

public class AgentStats {

    private String agentName;
    private List<TestStats> testStats = new ArrayList<AgentStats.TestStats>();

    public static TestStatsBuilder testStats(String name, long... successStats) {

        TestStatsBuilder builder = new TestStatsBuilder();
        long total = 0;
        for (long successStat : successStats) {
            builder.successResult(successStat);
            total += successStat;
        }

        // TODO : fake the rest of these so they are rougly consistent for tests?
        builder.isTransaction(false);
        builder.testName(name);
        builder.totalDurationSuccess(total);

        return builder;
    }

    public static class AgentStatsBuilder {
        private AgentStats stats = new AgentStats();

        public AgentStatsBuilder agentName(String agentName) {
            stats.agentName = agentName;
            return this;
        }

        public AgentStatsBuilder testStats(TestStatsBuilder testStats) {
            stats.testStats.add(testStats.toStats());
            return this;
        }

        public static AgentStatsBuilder agentStats() {
            return new AgentStatsBuilder();
        }

        public static TestStatsBuilder test(String testName) {
            TestStatsBuilder stats = new TestStatsBuilder();
            stats.testName(testName);
            return stats;
        }

        public AgentStats toStats() {
            return stats;
        }
    }

    public static class TestStatsBuilder {
        private TestStats stats = new TestStats();

        public TestStatsBuilder testName(String testName) {
            stats.testName = testName;
            return this;
        }

        public TestStatsBuilder isTransaction(boolean isTransaction) {
            stats.isTransaction = isTransaction;
            return this;
        }

        public TestStatsBuilder threadCount(int threadCount) {
            stats.threadCount = threadCount;
            return this;
        }

        public TestStatsBuilder transactionsSuccess(long transactionsSuccess) {
            stats.transactionsSuccess = transactionsSuccess;
            return this;
        }

        public TestStatsBuilder transactionsFailed(long transactionsFailed) {
            stats.transactionsFailed = transactionsFailed;
            return this;
        }

        public TestStatsBuilder totalDurationSuccess(long totalDurationSuccess) {
            stats.totalDurationSuccess = totalDurationSuccess;
            return this;
        }

        public TestStatsBuilder totalDurationFailed(long totalDurationFailed) {
            stats.totalDurationFailed = totalDurationFailed;
            return this;
        }

        public TestStatsBuilder sampleDuration(long sampleDuration) {
            stats.sampleDuration = sampleDuration;
            return this;
        }

        public TestStatsBuilder transactionName(String transactionName) {
            stats.transactionName = transactionName;
            return this;
        }

        public TestStatsBuilder successResult(long... result) {
            long total =0;
            for (long l : result) {
                stats.successResults.add(l);
                total += l;
            }

            stats.setTransactionsSuccess(result.length);
            stats.setTotalDurationSuccess(total);

            // If the total duration (test + pre-iteration setup/teardown) isn't set, set it as a short cut
            if(stats.getTotalDurationTotalSuccess() == 0) {
                stats.setTotalDurationTotalSuccess(result.length);
                stats.setTotalDurationTotalSuccess(total);
            }

            return this;
        }

        public TestStatsBuilder totalDurationSuccessResult(long... result) {
            long total =0;
            for (long l : result) {
                total += l;
            }

            stats.setTotalDurationTotalSuccess(result.length);
            stats.setTotalDurationTotalSuccess(total);

            return this;
        }

        public TestStatsBuilder failResult(long... result) {
            for (long l : result) {
                stats.failResults.add(l);
            }

            stats.setTransactionsFailed(result.length);

            return this;
        }

        public TestStats toStats() {
            return stats;
        }

        public static TestStatsBuilder testStats(String name) {
            TestStatsBuilder testStatsBuilder = new TestStatsBuilder();
            testStatsBuilder.testName(name);
            return testStatsBuilder;
        }
    }

    public static TestStatsBuilder testStats() {
        return new TestStatsBuilder();
    }

    public static AgentStatsBuilder agentStats() {
        return new AgentStatsBuilder();
    }

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
         * The total of the total durations (before + iteration) for the successful tests.
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

        public void setTotalDurationTotalSuccess(long totalDurationTotalSuccess) {
            this.totalDurationTotalSuccess = totalDurationTotalSuccess;
        }

        public long getTotalDurationTotalSuccess() {
            return totalDurationTotalSuccess;
        }

        public long getSampleDuration() {
            return sampleDuration;
        }

        public void setSampleDuration(long sampleDuration) {
            this.sampleDuration = sampleDuration;
        }

        public long getTotalDurationFailed() {
            return totalDurationFailed;
        }

        public void setTotalDurationFailed(long totalDurationFailed) {
            this.totalDurationFailed = totalDurationFailed;
        }

        public long getTotalDurationSuccess() {
            return totalDurationSuccess;
        }

        public void setTotalDurationSuccess(long totalDurationSuccess) {
            this.totalDurationSuccess = totalDurationSuccess;
        }

        public long getTransactionsFailed() {
            return transactionsFailed;
        }

        public void setTransactionsFailed(long transactionsFailed) {
            this.transactionsFailed = transactionsFailed;
        }

        public long getTransactionsSuccess() {
            return transactionsSuccess;
        }

        public void setTransactionsSuccess(long transactionsSuccess) {
            this.transactionsSuccess = transactionsSuccess;
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

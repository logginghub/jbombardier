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





public class TestRunResultBuilder {

    private RunResult runResult = new RunResult();

    public static TestRunResultBuilder start() {
        return new TestRunResultBuilder();
    }
    
    public static TransactionResultSnapshotBuilder result() {
        return new TransactionResultSnapshotBuilder();
    }
    
    public RunResult toTestRunResult() {
        return runResult;
    }
    
    public TestRunResultBuilder name(String string) {
        runResult.setConfigurationName(string);
        return this;
    }
    
    public TestRunResultBuilder failureReason(String string) {
        runResult.setFailureReason(string);
        return this;
    }
    
    public TestRunResultBuilder startTime(long startTime) {
        runResult.setStartTime(startTime);
        return this;
    }

    public void result(String test, TransactionResultSnapshotBuilder builder) {
        // TODO : refactor fix me
//        runResult.getTestResults().put(test, builder.toSnapshot());
    }
    
    public TestRunResultBuilder results(TransactionResultSnapshotBuilder... builders) {
        for (TransactionResultSnapshotBuilder transactionResultSnapshotBuilder : builders) {
            TransactionResult snapshot = transactionResultSnapshotBuilder.toSnapshot();
            // TODO : refactor fix me
//            runResult.getTestResults().put(snapshot.getTestName(), snapshot);
        }
        return this;
    }
    
    
    public static class TransactionResultSnapshotBuilder {

        TransactionResult snapshot = new TransactionResult();
        
        public TransactionResultSnapshotBuilder failedDuration(double f) {
            snapshot.setUnsucccesfulDuration(f);
            return this;
        }
        
        public TransactionResultSnapshotBuilder sla(double f) {
            snapshot.setSla(f);
            return this;
        }
        
        public TransactionResultSnapshotBuilder successDuration(double f) {
            snapshot.setSuccessDuration(f);
            return this;
        }

        public TransactionResultSnapshotBuilder successTotalDuration(double f) {
            snapshot.setSuccessTotalDuration(f);
            return this;
        }

        public TransactionResultSnapshotBuilder testName(String f) {
            snapshot.setTestName(f);
            return this;
        }
        
        public TransactionResultSnapshotBuilder transactionName(String f) {
            snapshot.setTransactionName(f);
            return this;
        }
        
        public TransactionResultSnapshotBuilder testTime(long f) {
            snapshot.setTestTime(f);
            return this;
        }
        
        public TransactionResultSnapshotBuilder transactionsFailed(long f) {
            snapshot.setTransactionsUnsuccessful(f);
            return this;
        }
        
        public TransactionResultSnapshotBuilder transactionsSuccess(long f) {
            snapshot.setTransactionsSuccessful(f);
            return this;
        }

        public TransactionResultSnapshotBuilder transactionCount(long f) {
            snapshot.setTransactionCount(f);
            return this;
        }
        
        public TransactionResult toSnapshot() {
            return snapshot;             
        }
        
    }

    
    
}

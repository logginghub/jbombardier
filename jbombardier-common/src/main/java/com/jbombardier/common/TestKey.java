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

public class TestKey {

    private String testName;
    private String transactionName;

    public TestKey(String testName, String transactionName) {
        super();
        this.testName = testName;
        this.transactionName = transactionName;
    }

    public TestKey(String testName) {
        super();
        this.testName = testName;
        this.transactionName = "";
    }

    public String getTestName() {
        return testName;
    }

    public String getTransactionName() {
        return transactionName;
    }

    @Override public String toString() {
        String key;
        if (transactionName != null && transactionName.length() == 0) {
            key = testName;
        }
        else {
            key = testName + "." + transactionName;
        }
        return key;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((testName == null) ? 0 : testName.hashCode());
        result = prime * result + ((transactionName == null) ? 0 : transactionName.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TestKey other = (TestKey) obj;
        if (testName == null) {
            if (other.testName != null) return false;
        }
        else if (!testName.equals(other.testName)) return false;
        if (transactionName == null) {
            if (other.transactionName != null) return false;
        }
        else if (!transactionName.equals(other.transactionName)) return false;
        return true;
    }

}

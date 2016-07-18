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

public interface TestContext {
    void createTransaction(String transactionID, long elapsedNanos, boolean success);
    
    void startTransaction(String transactionID);

    void endTransaction(String transactionID);

    void failTransaction(String transactionID, String message);

    void failTransaction(String transactionID, Throwable t);

    int getIntegerProperty(String string, int defaultValue);
    boolean getBooleanProperty(String string, boolean defaultValue);

    String getProperty(String string);

    String getProperty(String string, String defaultValue);

    void log(String format, Object... params);

    PropertyEntry getPropertyEntry(String string);

    void sleepSeconds(int seconds);

    int random(int i);

    void sleep(int milliseconds);
    void sleep(int milliseconds, int nanoseconds);

    void forceTransactionEnd(boolean success, Throwable failureReason);
    
    /**
     * Fails the current transaction. The failureReason will be reported to the console.
     * @param failureReason The message to appear in the jbombardier console.
     */
    void fail(String failureReason);

    

}

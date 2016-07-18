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

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.utils.ThreadUtils;

/**
 * We are assuming there will be one context per thread.
 * 
 * @author James
 * 
 */
public class SimpleTestContext implements TestContext {

    private Random random = new Random();
    private final ResultStrategy resultHandler;
    private final PropertiesStrategy propertiesProvider;

    private long startTimeNanos;
    private String currentTransaction;
    private final LoggingStrategy loggingHandler;
    
    // Trying to hunt down an annoying bug
    private static AtomicInteger nextInstanceID = new AtomicInteger();
    private int instanceID;

    private Logger logger = Logger.getLogger(SimpleTestContext.class.getName());
    private final String testName;
    private String failureReason;
    
    public SimpleTestContext(String testName, ResultStrategy handler, PropertiesStrategy propertiesProvider, LoggingStrategy loggingHandler) {
        this.testName = testName;
        this.resultHandler = handler;
        this.propertiesProvider = propertiesProvider;
        this.loggingHandler = loggingHandler;
        this.instanceID = nextInstanceID.getAndIncrement();
    }

    public void createTransaction(String transactionID, long elapsedNanos, boolean success) {
        resultHandler.onNewSuccessResult(transactionID, elapsedNanos);
    }

    public void startTransaction(String transactionID) {
        if(logger.isLoggable(Level.FINE)){
            logger.fine(String.format("Thread [%s] starting transaction [%s]/[%s] in simpleTestContext instance [%d]", Thread.currentThread().getName(), testName, transactionID, instanceID));
        }
        if (currentTransaction != null) {
            throw new TestContextException(String.format("You've attempted to start a new transaction ('%s') before the last one was complete ('%s') - please check your code is correctly closing its transactions",
                                                     currentTransaction,
                                                     transactionID));
        }

        this.currentTransaction = transactionID;
        this.startTimeNanos = System.nanoTime();
    }

    public void forceTransactionEnd(boolean success, Throwable t){
        if(currentTransaction != null){
            if(success){
            endTransaction(currentTransaction);
            }else {
                failTransaction(currentTransaction, t);
            }
        }
    }
    
    public void endTransaction(String transactionID) {        
        if(logger.isLoggable(Level.FINE)){
            logger.fine(String.format("Thread [%s] ending transaction [%s]/[%s] in simpleTestContext instance [%d]", Thread.currentThread().getName(), testName, transactionID, instanceID));
        }
        assertCurrentTransaction(transactionID);

        long currentTimeNanos = System.nanoTime();
        long elapsedNanos = currentTimeNanos - startTimeNanos;

        createTransaction(transactionID, elapsedNanos, true);
        currentTransaction = null;
    }

    public void failTransaction(String transactionID, String message) {
        if(logger.isLoggable(Level.FINE)){
            logger.fine(String.format("Thread [%s] failing transaction [%s]/[%s] in simpleTestContext instance [%d]", Thread.currentThread().getName(), testName, transactionID, instanceID));
        }
        assertCurrentTransaction(transactionID);

        long currentTimeNanos = System.nanoTime();
        long elapsedNanos = currentTimeNanos - startTimeNanos;

        resultHandler.onNewFailResult(transactionID, elapsedNanos, message);
        currentTransaction = null;
    }

    public void failTransaction(String transactionID, Throwable t) {
        if(logger.isLoggable(Level.FINE)){
            logger.fine(String.format("Thread [%s] failing transaction [%s]/[%s] in simpleTestContext instance [%d]", Thread.currentThread().getName(), testName, transactionID, instanceID));
        }
        assertCurrentTransaction(transactionID);

        long currentTimeNanos = System.nanoTime();
        long elapsedNanos = currentTimeNanos - startTimeNanos;

        resultHandler.onNewFailResult(transactionID, elapsedNanos, t);
        currentTransaction = null;
    }

    public int getIntegerProperty(String string, int defaultValue) {
        return propertiesProvider.getIntegerProperty(string, defaultValue);
    }

    public boolean getBooleanProperty(String string, boolean defaultValue) {
        return propertiesProvider.getBooleanProperty(string, defaultValue);
    }

    public String getProperty(String string) {
        return propertiesProvider.getStringProperty(string, null);
    }

    public String getProperty(String string, String defaultValue) {
        return propertiesProvider.getStringProperty(string, defaultValue);
    }

    public void log(String format, Object... params) {
        loggingHandler.log(format, (Object[]) params);
    }

    public PropertyEntry getPropertyEntry(String propertyName) {
        return propertiesProvider.getPropertyEntry(propertyName);
    }

    private void assertCurrentTransaction(String transactionID) {
        if (currentTransaction == null) {
            throw new RuntimeException(String.format("You've attempted to end a transaction ('%s') before it was started - please check your code is correctly starting its transactions",
                                                     transactionID));
        }

        if (!currentTransaction.equals(transactionID)) {
            throw new RuntimeException(String.format("You've attempted to stop transaction ('%s') that was different from the current transaction ('%s') - please check your code is correctly starting and ending its transactions",
                                                     transactionID,
                                                     currentTransaction));
        }
    }

    public void sleepSeconds(int seconds) {
        ThreadUtils.sleep(seconds * 1000);
    }

    public int random(int i) {
        return random.nextInt(i);
    }

    public void sleep(int milliseconds) {
        ThreadUtils.sleep(milliseconds);
    }
    
    public void sleep(int milliseconds, int nanoseconds) {
        ThreadUtils.sleep(milliseconds, nanoseconds);
    }

    public void fail(String failureReason) {
        this.failureReason = failureReason;
        throw new TestFailedException(failureReason);
    }


}

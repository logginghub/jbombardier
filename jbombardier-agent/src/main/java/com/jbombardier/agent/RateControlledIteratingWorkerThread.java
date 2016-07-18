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

package com.jbombardier.agent;

import com.logginghub.utils.IteratingRunnable;
import com.logginghub.utils.IteratingRunnableWorkerThread;
import com.logginghub.utils.logging.Logger;

/**
 * Class that will control the rate of execution of the IteratingWorkerThread in
 * line with three values - the target rate, the step change for that rate, and
 * a time period to increment the current rate towards the target.
 * 
 * @author James
 * 
 */
public class RateControlledIteratingWorkerThread extends IteratingRunnableWorkerThread {
    
    private static final Logger logger = Logger.getLoggerFor(RateControlledIteratingWorkerThread.class);
    private volatile double currentRate = 1;
    private long timeOfLastChange = 0;
    private volatile double targetRate;
    private volatile double step;
    private volatile long stepTime;
    private volatile boolean needToStep = true;
    private double transactionRateModifier;

    public RateControlledIteratingWorkerThread(String threadName,
                                               IteratingRunnable runnable,
                                               double startingRate,
                                               double targetRate,
                                               double step,
                                               long stepTime,
                                               double transactionRateModifier) {
        super(threadName, runnable);

        this.targetRate = targetRate;
        this.step = step;
        this.stepTime = stepTime;
        this.transactionRateModifier = transactionRateModifier;
        setIterationDelay((long) (1000 / startingRate));               
    }
        
    @Override protected void onRun() throws Throwable {
        super.onRun();
        
        
        if (needToStep) {
            
            if(logger.willLog(Logger.fine)) {
                logger.fine("Iterating the rate controller thread");
            }
            
            if (calculateTargetRate() < 0) {
                setIterationDelay(-1);
                needToStep = false;
            }
            else {

                long now = System.currentTimeMillis();
                long timeDelta = now - timeOfLastChange;
                if (timeDelta >= stepTime) {
                    logger.trace("Enough time (" + timeDelta + ") has elapsed to ramp up again");
                    double delta = calculateTargetRate() - currentRate;

                    logger.trace("The rate delta is " + delta);
                    if (Math.abs(delta) > 0.01) {
                        double amountToIncrease;

                        if (delta > 0) {
                            // Increasing
                            amountToIncrease = Math.min(delta, step);
                        }
                        else {
                            amountToIncrease = Math.max(delta, -step);
                        }

                        logger.trace("Ramping up by " + amountToIncrease);

                        currentRate += amountToIncrease;

                        long delayNS = (long) (1000000000d / currentRate);
                        logger.fine("New delay nanos {}", delayNS);
                        setIterationDelayNanos(delayNS);
                    }
                    else {
                        needToStep = false;
                    }
                    timeOfLastChange = now;
                }
            }
        }
    }

    private double calculateTargetRate() {
        return targetRate * transactionRateModifier;
         
    }

    public void setStep(double step) {
        this.step = step;
    }

    public void setStepTime(long stepTime) {
        this.stepTime = stepTime;
    }

    public void setTargetRate(double targetRate) {
        this.targetRate = targetRate;
        wakeUpThread();
    }

    private void wakeUpThread() {
        logger.fine("Waking up thread to apply rate change...");
        
        if (Math.abs(calculateTargetRate() - currentRate) > 0.01) {
            needToStep = true;
        }
        
        // This will unfortunately trigger another iteration, but its the only
        // way to get the thread to wake up and take note of the new settings!
        // This is especially true if the rate has been reduced to zero, we have
        // to give the thread a right old kick and force a step change right
        // away.
        timeOfLastChange = 0;
        interupt();
    }

    public void setTransactionRateModifier(double transactionRateModifier) {
        this.transactionRateModifier = transactionRateModifier;        
        wakeUpThread();
    }
    
    public double getTransactionRateModifier() {
        return transactionRateModifier;
    }

}

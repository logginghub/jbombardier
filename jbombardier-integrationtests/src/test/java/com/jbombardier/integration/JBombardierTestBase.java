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

package com.jbombardier.integration;

import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by james on 12/11/14.
 */
public class JBombardierTestBase {
    protected JBombardierDSL dsl;
    private int threadsBefore;
    private Thread[] threadsBeforeArray;

    @BeforeMethod public void setup() {

//        System.out.println("!!!!!!!!!!!!!! SETUP DSL !!!!!!!!!!!!!!!!!!!!");

        threadsBefore = Thread.activeCount();
        threadsBeforeArray = new Thread[threadsBefore];
        Thread.enumerate(threadsBeforeArray);

        dsl = new JBombardierDSL();
        dsl.start();
    }

    public WorkerThread startThreadMonitor() {
        final Set<Thread> threadsBefore = new HashSet<Thread>();

        WorkerThread thread = WorkerThread.everyNowDaemon("ThreadMonitor", 10, TimeUnit.MILLISECONDS, new Runnable() {
            @Override public void run() {
                int threadsAfter = Thread.activeCount();
                Thread[] threadsAfterArray = new Thread[threadsAfter];
                Thread.enumerate(threadsAfterArray);

                HashSet<Thread> nowThreads = new HashSet<Thread>();
                for (int i = 0; i < threadsAfterArray.length; i++) {
                    Thread threadAfter = threadsAfterArray[i];
                    if (threadAfter != null) {
                        nowThreads.add(threadAfter);
                    }
                }

                StringUtils.StringUtilsBuilder stringBuilder = new StringUtils.StringUtilsBuilder();

                for (Thread thread : nowThreads) {
                    if (!threadsBefore.contains(thread)) {
                        System.out.println(StringUtils.format(" | Thread started : {} | daemon={}",
                                                              thread.getName(),
                                                              thread.isDaemon()));
                    }
                }

                for (Thread thread : threadsBefore) {
                    if (!nowThreads.contains(thread)) {
                        System.out.println(StringUtils.format(" | Thread died    : {} | daemon={}",
                                                              thread.getName(),
                                                              thread.isDaemon()));
                    }
                }

                threadsBefore.clear();
                threadsBefore.addAll(nowThreads);

            }
        });

        return thread;
    }

    @AfterMethod public void teardown() {
//        System.out.println("!!!!!!!!!!!!!! TEARDOWN DSL !!!!!!!!!!!!!!!!!!!!");

        dsl.stop();

        waitForThreadDeath();

        int threadsAfter = Thread.activeCount();
        Thread[] threadsAfterArray = new Thread[threadsAfter];
        Thread.enumerate(threadsAfterArray);

        HashSet<Thread> threadsBefore = new HashSet<Thread>();
        for (int i = 0; i < threadsBeforeArray.length; i++) {
            threadsBefore.add(threadsAfterArray[i]);
        }

        HashSet<Thread> nonDaemonAfterThreads = new HashSet<Thread>();
        for (int i = 0; i < threadsAfterArray.length; i++) {
            Thread threadAfter = threadsAfterArray[i];
            if (!threadAfter.isDaemon()) {
                nonDaemonAfterThreads.add(threadAfter);
            }
        }

        if (this.threadsBefore != threadsAfter) {
            StringUtils.StringUtilsBuilder stringBuilder = new StringUtils.StringUtilsBuilder();
            stringBuilder.appendLine("Threads before {} threads after {}", this.threadsBefore, threadsAfter);

            boolean hasFailureOccured = false;
            Iterator<Thread> iterator = nonDaemonAfterThreads.iterator();

            while (iterator.hasNext()) {
                Thread afterThread = (Thread) iterator.next();

                // jshaw - special check in for running with a lot of TestNG worker threads
                if (!threadsBefore.contains(afterThread) && !afterThread.getName().equals("TestNG")) {
                    stringBuilder.appendLine("Thread: '" + afterThread.getName() + "'");
                    hasFailureOccured = true;
                }
            }

            if (hasFailureOccured) {
                throw new RuntimeException("Leaked threads : " + stringBuilder.toString());
            }
        }
    }

    public void waitForThreadDeath(){
        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return haveThreadsDied();
            }
        });
    }


    public boolean haveThreadsDied() {

        int threadsAfter = Thread.activeCount();
        Thread[] threadsAfterArray = new Thread[threadsAfter];
        Thread.enumerate(threadsAfterArray);

        HashSet<Thread> threadsBefore = new HashSet<Thread>();
        for (int i = 0; i < threadsBeforeArray.length; i++) {
            threadsBefore.add(threadsAfterArray[i]);
        }

        HashSet<Thread> nonDaemonAfterThreads = new HashSet<Thread>();
        for (int i = 0; i < threadsAfterArray.length; i++) {
            Thread threadAfter = threadsAfterArray[i];
            if (!threadAfter.isDaemon()) {
                nonDaemonAfterThreads.add(threadAfter);
            }
        }

        boolean threadsOk = true;

        if (this.threadsBefore != threadsAfter) {
            Iterator<Thread> iterator = nonDaemonAfterThreads.iterator();

            while (iterator.hasNext()) {
                Thread afterThread = (Thread) iterator.next();

                // jshaw - special check in for running with a lot of TestNG worker threads
                if (!threadsBefore.contains(afterThread) && !afterThread.getName().equals("TestNG")) {
                    Out.err("WARNING : Thread still running: '{}'", afterThread.getName());
                    threadsOk = false;
                }
            }
        }

        return threadsOk;

    }

}

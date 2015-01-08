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

package com.jbombardier.console.embedded;

import com.logginghub.utils.logging.Logger;
import com.jbombardier.agent.ThreadController;
import com.jbombardier.common.BasicTestStats;

public class ExecutionAgent {

    private ThreadController threadController;

    private static final Logger logger = Logger.getLoggerFor(ExecutionAgent.class);

    public ExecutionAgent(ThreadController threadController) {
        this.threadController = threadController;
    }

    public void onStatsUpdated(BasicTestStats basicTestStats) {

        double average = (1e-6 * basicTestStats.totalDurationSuccess) / (double) basicTestStats.transactionsSuccess;
        double maxRate = 1000d / average;
        threadController.setTargetRate(maxRate);
        threadController.setRateStep(maxRate / 10);
        logger.info("Setting target rate to {}", maxRate);
    }

}

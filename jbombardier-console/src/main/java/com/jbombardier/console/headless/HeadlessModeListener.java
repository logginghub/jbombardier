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

package com.jbombardier.console.headless;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.logging.Logger;
import com.jbombardier.common.AgentFailedInstruction;
import com.jbombardier.console.ConsoleModel.InteractiveModelListener;
import com.jbombardier.console.model.AgentModel;
import com.jbombardier.console.model.ConsoleEventModel;
import com.jbombardier.console.model.TestModel;

public class HeadlessModeListener implements InteractiveModelListener {

    private String abandoned;
    private static final Logger logger = Logger.getLoggerFor(HeadlessModeListener.class);

    @Override public void onNewAgent(AgentModel model) {
        logger.info("New agent found {}", model);
    }

    @Override public void onNewTest(TestModel testModel) {
        logger.info("New test found {}", testModel);
    }

//    @Override public void onNewTestResult(TransactionResultModel testModel) {
//        logger.info("New test result received {}", testModel);
//    }

    @Override public void onModelReset() {
        logger.info("Model has been reset");
    }

    @Override public void onTestStarted() {
//        logger.info("Test has started");
    }

    @Override public void onTestEnded() {
        logger.info("Test has ended");
    }

    @Override public void onConsoleEvent(ConsoleEventModel event) {
        logger.info("{} | {} | {} | {} ",event.getSeverity(),  event.getSource(), event.getMessage(),  event.getThrowable());
    }

    public String getAbandoned() {
        return abandoned;
    }
    
    @Override public void onTestAbandoned(String reason, AgentFailedInstruction afi) {
        String fullReason = StringUtils.format("[{}] [{}] {} %n%n {}", afi.getSource(), afi.getThread(), afi.getMessage(), afi.getStackTrace());
        logger.warn(fullReason);
        this.abandoned = fullReason; 
    }

    @Override public void onTelemetryData(DataStructure data) {}

}

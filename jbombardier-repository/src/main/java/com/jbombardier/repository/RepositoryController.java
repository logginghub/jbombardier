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

package com.jbombardier.repository;

import java.io.File;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.jbombardier.console.SwingConsoleController;
import com.jbombardier.console.model.JSONHelper;
import com.jbombardier.console.model.result.TestRunResult;
import com.jbombardier.repository.model.RepositoryModel;
import com.jbombardier.repository.model.RepositoryTestModel;

public class RepositoryController {

    
    private static final Logger logger = Logger.getLoggerFor(RepositoryController.class);
    private RepositoryModel model;
    private JSONHelper helper = new JSONHelper();

    public RepositoryController(RepositoryModel model) {
        this.model = model;
    }

    public RepositoryModel getModel() {
        return model;
    }

    public void postResult(String jsonResult) {

        TestRunResult testRunResult = helper.fromJSON(jsonResult);
        addResult(testRunResult);

        File jsonResultsFile = getFile(testRunResult);
        FileUtils.write(jsonResult, jsonResultsFile);
    }

    public void postResult(TestRunResult testRunResult) {
        File jsonResultsFile = getFile(testRunResult);
        logger.debug("Posting result for test '{}' at '{}' to '{}'", testRunResult.getConfigurationName(), Logger.toDateString(testRunResult.getStartTime()), jsonResultsFile.getAbsolutePath());
        addResult(testRunResult);

        String jsonResult = helper.toJSON(testRunResult);
        FileUtils.write(jsonResult, jsonResultsFile);
    }

    private File getFile(TestRunResult testRunResult) {
        
        File dataFolder = new File(model.getDataPath().get());
        File testFolder = new File(dataFolder, testRunResult.getConfigurationName());
        File timeFolder = new File(testFolder, TimeUtils.toDailyFolderSplit(testRunResult.getStartTime()));
        
        timeFolder.mkdirs();

        File jsonResultsFile = SwingConsoleController.getJSONResultsFile(timeFolder,
                                                                         testRunResult.getConfigurationName(),
                                                                         testRunResult.getStartTime());
        return jsonResultsFile;
    }

    private void addResult(TestRunResult testRunResult) {
        RepositoryTestModel testModel = model.getRepositoryTestModelForTest(testRunResult.getConfigurationName());
        testModel.add(testRunResult);
    }

}

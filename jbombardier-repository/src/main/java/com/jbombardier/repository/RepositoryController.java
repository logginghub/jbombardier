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

package com.jbombardier.repository;

import com.google.gson.Gson;
import com.jbombardier.JBombardierController;
import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.repository.model.RepositoryModel;
import com.jbombardier.repository.model.RepositoryTestModel;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;

import java.io.File;

public class RepositoryController {

    
    private static final Logger logger = Logger.getLoggerFor(RepositoryController.class);
    private RepositoryModel model;

    public RepositoryController(RepositoryModel model) {
        this.model = model;
    }

    public RepositoryModel getModel() {
        return model;
    }

    public void postResult(String jsonResult) {

        Gson gson = new Gson();
        RunResult runResult = gson.fromJson(jsonResult, RunResult.class);
        addResult(runResult);

        File jsonResultsFile = getFile(runResult);
        FileUtils.write(jsonResult, jsonResultsFile);
    }

    public void postResult(RunResult runResult) {
        File jsonResultsFile = getFile(runResult);
        logger.debug("Posting result for test '{}' at '{}' to '{}'", runResult.getConfigurationName(), Logger.toDateString(
                runResult.getStartTime()), jsonResultsFile.getAbsolutePath());
        addResult(runResult);

        Gson gson = new Gson();
        String jsonResult = gson.toJson(runResult);
        FileUtils.write(jsonResult, jsonResultsFile);
    }

    private File getFile(RunResult runResult) {
        
        File dataFolder = new File(model.getDataPath().get());
        File testFolder = new File(dataFolder, runResult.getConfigurationName());
        File timeFolder = new File(testFolder, TimeUtils.toDailyFolderSplit(runResult.getStartTime()));
        
        timeFolder.mkdirs();

        File jsonResultsFile = JBombardierController.getJSONResultsFile(timeFolder,
                                                                        runResult.getConfigurationName(),
                                                                        runResult.getStartTime());
        return jsonResultsFile;
    }

    private void addResult(RunResult runResult) {
        RepositoryTestModel testModel = model.getRepositoryTestModelForTest(runResult.getConfigurationName());
        testModel.add(runResult);
    }

}

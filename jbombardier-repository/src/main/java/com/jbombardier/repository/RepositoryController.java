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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jbombardier.JBombardierController;
import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.repository.model.RepositoryConfigurationModel;
import com.jbombardier.repository.model.RepositoryModel;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;

import java.io.File;
import java.util.List;

public class RepositoryController {


    private static final Logger logger = Logger.getLoggerFor(RepositoryController.class);
    private RepositoryModel model;

    public RepositoryController(RepositoryModel model) {
        this.model = model;
    }

    public String getConfiguration(String configurationName) {

        RepositoryConfigurationModel repositoryConfigurationModel = model.getRepositoryConfigurationModel(configurationName);

        List<RunResult> lastXResults = repositoryConfigurationModel.getLastXResults(10);

        JsonObject container = new JsonObject();
        JsonArray results = new JsonArray();
        Gson gson = RepositoryWebView.getGson();

        for (RunResult lastXResult : lastXResults) {
            JsonObject result = gson.toJsonTree(lastXResult).getAsJsonObject();
            results.add(result);
        }

        container.add("lastXResults", results);
        container.add("resultCount", new JsonPrimitive(repositoryConfigurationModel.getCount()));
        return container.toString();
    }

    public RepositoryModel getModel() {
        return model;
    }

    public String getSummary() {

        ObservableList<RepositoryConfigurationModel> testModels = model.getRepositoryConfigurationModels();

        JsonArray results = new JsonArray();

        for (RepositoryConfigurationModel testModel : testModels) {

            // TODO : get rid of this once we've worked out who is creating nulls
            if(testModel.getName().get() != null) {
                JsonObject object = new JsonObject();
                object.add("name", new JsonPrimitive(testModel.getName().get()));
                object.add("results", new JsonPrimitive(testModel.getCount()));
                results.add(object);
            }
        }

        return results.toString();
    }

    public void postResult(String jsonResult) {

        Gson gson = new Gson();
        RunResult runResult = gson.fromJson(jsonResult, RunResult.class);
        addResult(runResult);

        File jsonResultsFile = getFile(runResult);
        FileUtils.write(jsonResult, jsonResultsFile);
    }

    private void addResult(RunResult runResult) {
        RepositoryConfigurationModel testModel = model.getRepositoryConfigurationModel(runResult.getConfigurationName());
        testModel.add(runResult);
    }

    private File getFile(RunResult runResult) {

        File dataFolder = new File(model.getDataPath().get());
        File testFolder = new File(dataFolder, runResult.getConfigurationName());
        File timeFolder = new File(testFolder, TimeUtils.toDailyFolderSplit(runResult.getStartTime()));

        timeFolder.mkdirs();

        File jsonResultsFile = JBombardierController.getJSONResultsFile(timeFolder, runResult.getConfigurationName(), runResult.getStartTime());
        return jsonResultsFile;
    }

    public void postResult(RunResult runResult) {
        File jsonResultsFile = getFile(runResult);
        logger.debug("Posting result for test '{}' at '{}' to '{}'",
                     runResult.getConfigurationName(),
                     Logger.toDateString(runResult.getStartTime()),
                     jsonResultsFile.getAbsolutePath());
        addResult(runResult);

        Gson gson = new Gson();
        String jsonResult = gson.toJson(runResult);
        FileUtils.write(jsonResult, jsonResultsFile);
    }

}

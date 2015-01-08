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
import java.io.FileFilter;
import java.util.List;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.logging.Logger;
import com.jbombardier.console.model.JSONHelper;
import com.jbombardier.console.model.result.TestRunResult;
import com.jbombardier.repository.model.RepositoryModel;
import com.jbombardier.repository.model.RepositoryTestModel;

public class ModelBuilder {

    private static final Logger logger = Logger.getLoggerFor(ModelBuilder.class);
    private JSONHelper helper = new JSONHelper();

    public void buildFrom(File resultsFolder, RepositoryModel model) {
        logger.info("Scanning path file {} for results", resultsFolder.getAbsolutePath());
        List<File> files = FileUtils.findFilesRecursively(resultsFolder, new FileFilter() {
            @Override public boolean accept(File pathname) {
                return pathname.getName().endsWith(".json");
            }
        });

        for (File file : files) {
            buildFromFile(file, model);
        }
        
        logger.info("Model loaded.");
    }

    public void buildFromFile(File file, RepositoryModel model) {
        logger.debug("Loading file {}", file.getName());
        try {
            TestRunResult testRunResult = helper.fromJSON(FileUtils.read(file));
            RepositoryTestModel testModel = model.getRepositoryTestModelForTest(testRunResult.getConfigurationName());
            testModel.add(testRunResult);
        }
        catch (RuntimeException e) {
            logger.warn(e, "Failed to load file '{}', skipping", file.getAbsolutePath());
        }
    }

}
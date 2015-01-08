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

package com.jbombardier.repository.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;
import com.jbombardier.repository.RepositoryConfiguration;

public class RepositoryModel extends Observable {

    private static final Logger logger = Logger.getLoggerFor(RepositoryModel.class);
    private ObservableList<RepositoryTestModel> testModels = new ObservableList<RepositoryTestModel>(new ArrayList<RepositoryTestModel>());
    private ObservableProperty<String> dataPath = new ObservableProperty<String>(null);

    private Map<String, RepositoryTestModel> repositoryTestModelByName = new HashMap<String, RepositoryTestModel>();

    public ObservableList<RepositoryTestModel> getTestModels() {
        return testModels;
    }

    public RepositoryTestModel getRepositoryTestModelForTest(String configurationName) {
        synchronized (repositoryTestModelByName) {
            RepositoryTestModel repositoryTestModel = repositoryTestModelByName.get(configurationName);
            if (repositoryTestModel == null) {
                repositoryTestModel = new RepositoryTestModel();
                repositoryTestModel.getName().set(configurationName);
                repositoryTestModelByName.put(configurationName, repositoryTestModel);
                testModels.add(repositoryTestModel);
            }
            return repositoryTestModel;
        }

    }

    @Override public String toString() {
        return "RepositoryModel{" + "testModels=" + testModels + ", repositoryTestModelByName=" + repositoryTestModelByName + '}';
    }

    public ObservableProperty<String> getDataPath() {
        return dataPath;
    }

    public static RepositoryModel fromConfiguration(RepositoryConfiguration configuration) {
        RepositoryModel model = new RepositoryModel();
        model.getDataPath().set(configuration.getDataFolder());
        return model;

    }
}

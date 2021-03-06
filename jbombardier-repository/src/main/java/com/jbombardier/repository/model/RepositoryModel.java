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

package com.jbombardier.repository.model;

import com.jbombardier.repository.RepositoryConfiguration;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RepositoryModel extends Observable {

    private static final Logger logger = Logger.getLoggerFor(RepositoryModel.class);
    private ObservableList<RepositoryConfigurationModel> repositoryConfigurationModels = new ObservableList<RepositoryConfigurationModel>(new ArrayList<RepositoryConfigurationModel>());
    private ObservableProperty<String> dataPath = new ObservableProperty<String>(null);

    private Map<String, RepositoryConfigurationModel> repositoryTestModelByName = new HashMap<String, RepositoryConfigurationModel>();

    public ObservableList<RepositoryConfigurationModel> getRepositoryConfigurationModels() {
        return repositoryConfigurationModels;
    }

    public List<String> getConfigurationNames() {

        Set<String> configurationNames = new HashSet<>();
        for (RepositoryConfigurationModel repositoryConfigurationModel : repositoryConfigurationModels) {
            String name = repositoryConfigurationModel.getName().get();
            configurationNames.add(name);
        }

        List<String> sorted = new ArrayList<>(configurationNames);
        Collections.sort(sorted);

        return sorted;

    }

    public RepositoryConfigurationModel getRepositoryConfigurationModel(String configurationName) {
        synchronized (repositoryTestModelByName) {
            RepositoryConfigurationModel repositoryConfigurationModel = repositoryTestModelByName.get(configurationName);
            if (repositoryConfigurationModel == null) {
                repositoryConfigurationModel = new RepositoryConfigurationModel();
                repositoryConfigurationModel.getName().set(configurationName);
                repositoryTestModelByName.put(configurationName, repositoryConfigurationModel);
                repositoryConfigurationModels.add(repositoryConfigurationModel);
            }
            return repositoryConfigurationModel;
        }

    }

    @Override public String toString() {
        return "RepositoryModel{" + "repositoryConfigurationModels=" + repositoryConfigurationModels + ", repositoryTestModelByName=" + repositoryTestModelByName + '}';
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

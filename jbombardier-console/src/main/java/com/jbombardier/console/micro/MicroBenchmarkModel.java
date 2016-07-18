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

package com.jbombardier.console.micro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;

public class MicroBenchmarkModel extends Observable {

    private Map<String, MicroBenchmarkTestModel> testModelsByName = new HashMap<String, MicroBenchmarkTestModel>();

    private ObservableList<MicroBenchmarkTestModel> models = new ObservableList<MicroBenchmarkTestModel>(new ArrayList<MicroBenchmarkTestModel>());

    public ObservableList<MicroBenchmarkTestModel> getModels() {
        return models;
    }

    public void add(MicroBenchmarkTestModel testModel) {
        models.add(testModel);
        testModelsByName.put(testModel.getName().get(), testModel);
    }
    
    public MicroBenchmarkTestModel getModel(String testName) {
        return testModelsByName.get(testName);
    }

}

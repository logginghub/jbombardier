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

package com.jbombardier.console.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;

public class PhaseModel extends Observable {

    public final static long NO_WARMUP = 0;

    private ObservableList<TestModel> testModels = createListProperty("testModels", TestModel.class);
    private ObservableProperty<String> phaseName = createStringProperty("phaseName", "");
    private ObservableLong phaseDuration = createLongProperty("phaseDuration", -1);
    private ObservableLong warmupDuration = createLongProperty("warmupDuration", NO_WARMUP);
    private ObservableLong phaseRemainingTime = createLongProperty("phaseRemainingTime", -1);

    public ObservableList<TestModel> getTestModels() {
        return testModels;
    }

    public ObservableProperty<String> getPhaseName() {
        return phaseName;
    }

    public ObservableLong getPhaseDuration() {
        return phaseDuration;
    }

    public ObservableLong getPhaseRemainingTime() {
        return phaseRemainingTime;
    }

    public ObservableLong getWarmupDuration() {
        return warmupDuration;
    }
}

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

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;
import com.jbombardier.common.BasicTestStats;

public class MicroBenchmarkTestModel extends Observable{
    
    private ObservableProperty<String> name = new ObservableProperty<String>(null);    
    private ObservableProperty<Boolean> enabled = new ObservableProperty<Boolean>(true);   
    private ObservableInteger targetRate = new ObservableInteger(-1);
    private ObservableInteger threads = new ObservableInteger(1);
    
    private ObservableProperty<BasicTestStats> stats = new ObservableProperty<BasicTestStats>(null);
    
    public ObservableProperty<BasicTestStats> getStats() {
        return stats;
    }
    
    public ObservableProperty<String> getName() {
        return name;
    }
    
    public ObservableInteger getThreads() {
        return threads;
    }
    
    public ObservableProperty<Boolean> getEnabled() {
        return enabled;
    }
    
    public ObservableInteger getTargetRate() {
        return targetRate;
    }

}

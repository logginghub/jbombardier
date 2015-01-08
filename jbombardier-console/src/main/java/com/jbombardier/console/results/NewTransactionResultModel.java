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

package com.jbombardier.console.results;

import java.util.List;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableDouble;
import com.logginghub.utils.observable.ObservableLong;
import com.jbombardier.common.TestKey;

public class NewTransactionResultModel extends Observable {

    private ObservableDouble successMeanNanos = new ObservableDouble(0, this);
    private ObservableDouble failedMeanNanos = new ObservableDouble(0, this);
    private ObservableDouble successTotalMeanNanos = new ObservableDouble(0, this);

    private ObservableLong successCount = new ObservableLong(0, this);
    private ObservableLong failedCount = new ObservableLong(0, this);
    
    private ObservableDouble successPerSecond = new ObservableDouble(0, this);
    private ObservableDouble failuresPerSecond = new ObservableDouble(0, this);

    private ObservableDouble successTP90Nanos = new ObservableDouble(0, this);

    private final TestKey testKey;
    
    private FullResultsHandler fullResults = new FullResultsHandler();

    public NewTransactionResultModel(TestKey testKey) {
        this.testKey = testKey;
    }

    public String getTestName() {
        return testKey.getTestName();
    }
    
    public String getTransactionName() {
        return testKey.getTransactionName();
    }

    public ObservableDouble getFailuresPerSecond() {
        return failuresPerSecond;
    }
    
    public ObservableDouble getSuccessPerSecond() {
        return successPerSecond;
    }
    
    public ObservableDouble getSuccessTotalMeanNanos() {
        return successTotalMeanNanos;
    }
    
    public TestKey getTestKey() {
        return testKey;
    }
    
    public ObservableLong getFailedCount() {
        return failedCount;
    }

    public ObservableDouble getFailedMeanNanos() {
        return failedMeanNanos;
    }

    public ObservableLong getSuccessCount() {
        return successCount;
    }

    public ObservableDouble getSuccessMeanNanos() {
        return successMeanNanos;
    }

    public ObservableDouble getSuccessTP90Nanos() {
        return successTP90Nanos;
    }

    public void addResults(List<Long> successResults) {
        fullResults.add(successResults);
    }
}

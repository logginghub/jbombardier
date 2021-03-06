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

import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.console.model.result.TransactionResult;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RepositoryConfigurationModel extends Observable {

    private ObservableProperty<String> name = new ObservableProperty<String>("");
    private ObservableList<RunResult> results = new ObservableList<RunResult>(new ArrayList<RunResult>());

    public ObservableProperty<String> getName() {
        return name;
    }

    @Override public String toString() {
        return "RepositoryConfigurationModel{" + "name=" + name + ", results=" + results + '}';
    }

    public List<RunResult> getLastXResults(int resultCount) {

        List<RunResult> xResults = new ArrayList<RunResult>();

        synchronized (results) {

            Collections.sort(results, new Comparator<RunResult>() {
                @Override public int compare(RunResult o1, RunResult o2) {
                    return CompareUtils.compare(o2.getStartTime(), o1.getStartTime());
                }
            });

            int count = Math.min(results.size(), resultCount);
            for (int i = 0; i < count; i++) {
                xResults.add(results.get(i));
            }
        }

        return xResults;

    }

    public boolean hasResults() {
        synchronized (results) {
            return !results.isEmpty();
        }
    }

    public int getCount() {
        synchronized (results) {
            return results.size();
        }
    }

    public void add(RunResult runResult) {
        synchronized (results) {
            results.add(runResult);
        }
    }

    public List<TransactionResult> getResultsForTest(String resultName) {
        List<RunResult> lastXResults = getLastXResults(100);
        List<TransactionResult> results = new ArrayList<TransactionResult>();
        for (RunResult runResult : lastXResults) {

            // TODO : refactor fix me
            //TransactionResult transactionResultModel = runResult.getTestResults().get(resultName);
//            if (transactionResultModel != null) {
//                transactionResultModel.setTestTime(runResult.getStartTime());
//                results.add(transactionResultModel);
//            }
        }
        return results;
    }


}

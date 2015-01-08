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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;
import com.jbombardier.console.model.result.TestRunResult;
import com.jbombardier.console.model.result.TransactionResultSnapshot;

public class RepositoryTestModel extends Observable {

    private ObservableProperty<String> name = new ObservableProperty<String>("");
    private ObservableList<TestRunResult> results = new ObservableList<TestRunResult>(new ArrayList<TestRunResult>());

    public ObservableProperty<String> getName() {
        return name;
    }

    @Override public String toString() {
        return "RepositoryTestModel{" + "name=" + name + ", results=" + results + '}';
    }

    public List<TestRunResult> getLastXResults(int resultCount) {

        List<TestRunResult> xResults = new ArrayList<TestRunResult>();

        synchronized (results) {

            Collections.sort(results, new Comparator<TestRunResult>() {
                @Override public int compare(TestRunResult o1, TestRunResult o2) {
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

    public void add(TestRunResult testRunResult) {
        synchronized (results) {
            results.add(testRunResult);
        }
    }

    public List<TransactionResultSnapshot> getResultsForTest(String resultName) {
        List<TestRunResult> lastXResults = getLastXResults(100);
        List<TransactionResultSnapshot> results = new ArrayList<TransactionResultSnapshot>();
        for (TestRunResult testRunResult : lastXResults) {
            TransactionResultSnapshot transactionResultModel = testRunResult.getTestResults().get(resultName);
            if (transactionResultModel != null) {
                transactionResultModel.setTestTime(testRunResult.getStartTime());
                results.add(transactionResultModel);
            }
        }
        return results;
    }


}

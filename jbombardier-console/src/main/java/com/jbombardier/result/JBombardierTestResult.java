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

package com.jbombardier.result;

import com.logginghub.utils.CollectionUtils;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by james on 10/01/15.
 */
public class JBombardierTestResult {
    private String testName;

    private Map<String, JBombardierTestResult> transactionResults = new FactoryMap<String, JBombardierTestResult>() {
        @Override protected JBombardierTestResult createEmptyValue(String testName) {
            return new JBombardierTestResult(testName);
        }
    };

    private Map<String, JBombardierAgentTestResult> perAgentResults = new FactoryMap<String, JBombardierAgentTestResult>() {
        @Override protected JBombardierAgentTestResult createEmptyValue(String agentName) {
            return new JBombardierAgentTestResult(agentName);
        }
    };

    public JBombardierTestResult(String testName) {
        this.testName = testName;
    }

    public JBombardierTestResult() {

    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Map<String, JBombardierAgentTestResult> getPerAgentResults() {
        return perAgentResults;
    }

    public Map<String, JBombardierTestResult> getTransactionResults() {
        return transactionResults;
    }

    public void toStringDeep(StringUtils.StringUtilsBuilder builder) {
        builder.appendLine("TestResult : testName='{}'", testName);

//        builder.indent();

        List<String> agents = CollectionUtils.toSortedList(perAgentResults.keySet());

        for (String agentName : agents) {
            JBombardierAgentTestResult agentResult = perAgentResults.get(agentName);
            agentResult.toStringDeep(builder);
        }

//        builder.outdent();
    }
}

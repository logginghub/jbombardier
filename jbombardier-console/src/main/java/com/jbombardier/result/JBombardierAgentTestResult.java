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

import com.logginghub.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the test results from a particular agent.
 */
public class JBombardierAgentTestResult {
    private String agentName = JBombardierRunResult.unknown_state;
    private String agentAddress = JBombardierRunResult.unknown_state;

    private List<JBombardierTestElement> testElements = new ArrayList<JBombardierTestElement>();

    public JBombardierAgentTestResult(String agentName) {
        this.agentName = agentName;
    }

    public JBombardierAgentTestResult() {

    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public List<JBombardierTestElement> getTestElements() {
        return testElements;
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public String getAgentName() {
        return agentName;
    }

    public void toStringDeep(StringUtils.StringUtilsBuilder builder) {
        builder.appendLine("AgentTestResult : agentName='{}'",
                       agentName);

//        builder.indent();

        for (JBombardierTestElement testElement : testElements) {
            testElement.toStringDeep(builder);
        }

//        builder.outdent();
    }
}

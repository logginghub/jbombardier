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

package com.jbombardier.console.panels;

import java.util.List;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;

import com.jbombardier.console.JBombardierModel;
import com.jbombardier.console.components.JDynamicStateButton;
import com.jbombardier.console.model.AgentModel;

/** 
 * Displays agent status via coloured labels, in order to keep the space required to a minimum.
 * @author James
 *
 */
public class AgentStatusButtonsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /**
     * Create the panel.
     */
    public AgentStatusButtonsPanel() {
        setLayout(new MigLayout("ins 0, gap 0", "[grow]", "[grow]"));

        
        add(new JButton("demo agent 1"), "flowx");
        JButton button = new JButton("demo agent 2");
        add(button, "cell 0 0");
        JButton button_1 = new JButton("demo agent 3");
        add(button_1, "cell 0 0");
    }

    public void initialise(JBombardierModel model) {
        
        removeAll();
        
        model.addListener(new JBombardierModel.InteractiveModelListenerAdaptor() {
            public void onNewAgent(AgentModel model) {
                addAgentButton(model);
            }
        });
        
        List<AgentModel> agentModels = model.getAgentModels();
        for (AgentModel agentModel : agentModels) {
            addAgentButton(agentModel);
        }
    }
    
    private void addAgentButton(AgentModel model) {
        JDynamicStateButton button = new JDynamicStateButton(model.getName().get(), model.getConnected());
        add(button, "cell 0 0");
    }
}

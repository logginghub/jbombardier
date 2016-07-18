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

package com.jbombardier.console.panels;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import com.jbombardier.JBombardierModel;
import com.jbombardier.console.components.ReflectiveTable;
import com.jbombardier.console.model.AgentModel;

public class AgentStatusTablePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private ReflectiveTable<AgentModel> table;

    /**
     * Create the panel.
     */
    public AgentStatusTablePanel() {
        setLayout(new MigLayout("", "[grow]", "[grow]"));
        
        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, "flowx,cell 0 0");

        table = new ReflectiveTable<AgentModel>(AgentModel.class);
        scrollPane.setViewportView(table);
    }

    public void initialise(JBombardierModel model) {
        model.addListener(new JBombardierModel.InteractiveModelListenerAdaptor() {
            public void onNewAgent(AgentModel model) {
                table.addItem(model);
            }
        });
    }
}

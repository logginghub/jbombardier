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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.jbombardier.console.model.AgentModel;

public class AgentStatusTable extends JTable {

    private List<AgentModel> agents = new ArrayList<AgentModel>();
    private AgentStatusTableModel model = new AgentStatusTableModel();

    private class AgentStatusTableModel extends DefaultTableModel {
        
        @Override public int getColumnCount() {
         return 4;
        }
        
        @Override public int getRowCount() {        
            return agents.size();
        }
        
        @Override public Object getValueAt(int row, int column) {
            Object value;

            AgentModel agentModel = agents.get(row);
            switch (column) {
                case 0:
                    value = agentModel.getName();
                    break;
                case 1:
                    value = agentModel.getAddress();
                    break;
                case 2:
                    value = agentModel.getPort();
                    break;
                case 3:
                    value = agentModel.isConnected();
                    break;
                default:
                    throw new RuntimeException("Illegal column value");
            }

            return value;
        }

        @Override public String getColumnName(int column) {
            String name;
            switch (column) {
                case 0:
                    name = "Name";
                    break;
                case 1:
                    name = "Address";
                    break;
                case 2:
                    name = "Port";
                    break;
                case 3:
                    name = "Connected";
                    break;
                default:
                    throw new RuntimeException("Illegal column value");
            }
            return name;
        }

        public void addAgentModel(AgentModel model) {
            final int index = agents.size();
            agents.add(model);
            fireTableRowsInserted(index, index);
            
            model.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    fireTableRowsUpdated(index, index);
                }
            });
        }
    }

    public AgentStatusTable() {
        setModel(model);
    }

    public void addAgentModel(AgentModel model) {
        this.model.addAgentModel(model);
    }

}

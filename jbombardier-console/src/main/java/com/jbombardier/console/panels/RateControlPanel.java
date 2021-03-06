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

import com.jbombardier.common.TestField;
import com.jbombardier.JBombardierController;
import com.jbombardier.JBombardierModel;
import com.jbombardier.console.charts.XYTimeChartPanel;
import com.jbombardier.console.components.ReflectiveTable;
import com.jbombardier.console.model.AgentModel;
import com.jbombardier.console.model.PhaseModel;
import com.jbombardier.console.model.TestModel;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.List;

public class RateControlPanel extends JPanel {

    private ReflectiveTable<TestModel> table;
    private XYTimeChartPanel chartPanel;
    private JBombardierController controller;
    private JSpinner transactionRateMultiplier;
    private SpinnerNumberModel transactionRateModel;

    /**
     * Create the panel.
     */
    public RateControlPanel() {
        setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill][grow,fill]"));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.8);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        add(splitPane, "flowx,cell 0 0");

        JPanel threadStatePanel = new JPanel();
        splitPane.setLeftComponent(threadStatePanel);
        threadStatePanel.setBorder(new TitledBorder(null,
                "Thread State",
                TitledBorder.LEADING,
                TitledBorder.TOP,
                null,
                null));
        threadStatePanel.setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));

        chartPanel = new XYTimeChartPanel();
        chartPanel.setDatapoints(120);
        chartPanel.setShapesVisible(false);
        threadStatePanel.add(chartPanel, "cell 0 0,grow");

        JPanel threadControlPanel = new JPanel();
        splitPane.setRightComponent(threadControlPanel);
        threadControlPanel.setBorder(new TitledBorder(null,
                "Thread Control",
                TitledBorder.LEADING,
                TitledBorder.TOP,
                null,
                null));
        threadControlPanel.setLayout(new MigLayout("", "[grow,fill]", "[][grow,fill]"));

        JLabel lblNewLabel = new JLabel("Transaction rate multiplier");
        threadControlPanel.add(lblNewLabel, "flowx,cell 0 0");

        transactionRateMultiplier = new JSpinner();
        transactionRateModel = new SpinnerNumberModel(1.0, 0.0, 1000000.0, 0.1);
        transactionRateMultiplier.setModel(transactionRateModel);
        threadControlPanel.add(transactionRateMultiplier, "cell 0 0");

        JScrollPane scrollPane = new JScrollPane();
        threadControlPanel.add(scrollPane, "cell 0 1,grow");

        table = new ReflectiveTable<TestModel>(TestModel.class);
        table.dontShowColumns("Properties",
                "RecordAllValues",
                "Classname",
                "TransactionSLAs",
                "FailureThreshold",
                "FailureThresholdMode",
                "FailedTransactionCountThreshold");
        table.setEditable(true);
        scrollPane.setViewportView(table);
    }

    public void bind(final JBombardierModel model, final JBombardierController controller) {

        this.controller = controller;
        model.addListener(new JBombardierModel.InteractiveModelListenerAdaptor() {
            public void onNewAgent(final AgentModel model) {
                addAgentModel(model);
            }

            public void onNewTest(TestModel testModel) {
                table.addItem(testModel);
            }

            public void onTestStarted() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        chartPanel.clearChartData();
                    }
                });
            }
        });

        table.addChangeHandler(new ReflectiveTable.ChangeHandler<TestModel>() {
            public void onChange(TestModel entry, String field, Object newValue) {
                TestField fieldEnum = TestField.valueOf(dropCap(field));
                controller.updateTestVariable(entry.getName(), fieldEnum, newValue);
            }
        });

        List<AgentModel> agentModels = model.getAgentModels();
        for (AgentModel agentModel : agentModels) {
            addAgentModel(agentModel);
        }

        model.getCurrentPhase().addListenerAndNotifyCurrent(new ObservablePropertyListener<PhaseModel>() {
            @Override public void onPropertyChanged(PhaseModel phaseModel, PhaseModel t1) {
                if (t1 != null) {
                    table.clear();
                    t1.getTestModels().addListenerAndNotifyCurrent(new ObservableListListener<TestModel>() {
                        @Override public void onAdded(TestModel testModel) {
                            table.addItem(testModel);
                        }

                        @Override public void onRemoved(TestModel testModel, int index) {
                        }

                        @Override public void onCleared() {
                        }
                    });
                }
            }
        });

//        List<TestModel> testModels = model.getTestModels();
//        for (TestModel testModel : testModels) {
//
//        }

        // Wire up the transaction rate changer
        transactionRateModel.setValue(model.getTransactionRateModifier().get());
        transactionRateModel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                double doubleValue = transactionRateModel.getNumber().doubleValue();
                model.getTransactionRateModifier().set(doubleValue);
            }
        });
    }

    private void addAgentModel(final AgentModel model) {
        model.getThreadCount().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>() {
            @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        chartPanel.addValue(model.getName().get(), System.currentTimeMillis(), model.getThreadCount().get());
                        chartPanel.removeOldDataPoints();
                    }
                });
            }
        });
    }


    protected String dropCap(String field) {
        return Character.toLowerCase(field.charAt(0)) + field.substring(1);

    }
}

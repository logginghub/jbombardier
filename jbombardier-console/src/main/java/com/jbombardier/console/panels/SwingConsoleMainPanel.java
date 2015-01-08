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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import com.jbombardier.console.components.JDynamicStateButton;
import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jbombardier.console.SwingConsoleController;
import com.jbombardier.console.ConsoleModel;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.observable.ObservablePropertyListener;

public class SwingConsoleMainPanel extends JPanel {

    private SwingConsoleController controller;
    private AgentStatusButtonsPanel agentStatusPanel;
    private RateControlPanel threadStatePanel;
    private JDynamicStateButton stopTestButton;
    private JDynamicStateButton startTestButton;
    private TransactionStatePanel transactionStatePanel;
    private ConsolePanel consolePanel;
    private JPanel topPanel;
    // private JSplitPane splitPane;
    private JButton killAgentsButton;
    private JButton generateReportButton;
    private JButton resetStatsButton;
    private MachineTelemetryPanel machineTelemetryPanel;
    private ProcessTelemetryPanel processTelemetryPanel;

    private static final Logger logger = LoggerFactory.getLogger(SwingConsoleMainPanel.class);
    private JPanel agentControlPanel;

    public void setController(SwingConsoleController controller) {
        this.controller = controller;
    }

    /**
     * Create the panel.
     */
    public SwingConsoleMainPanel() {
        setLayout(new MigLayout("ins 0, gap 0", "[grow,fill]", "[top][grow,fill]"));

        topPanel = new JPanel();
        // topPanel.setBorder(new TitledBorder(null, "Agent Control",
        // TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(topPanel, "cell 0 0");
        topPanel.setLayout(new MigLayout("ins 0, gap 0", "[][grow,fill]", "[][grow]"));

        agentControlPanel = new JPanel();
        agentControlPanel.setBorder(new TitledBorder(null, "Agent Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        topPanel.add(agentControlPanel, "cell 0 0");
        agentControlPanel.setLayout(new MigLayout("ins 0, gap 0", "[grow,center]", "[grow,center]"));

        startTestButton = new JDynamicStateButton("Start test");
        startTestButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    controller.startTest();
                }
                catch (Throwable t) {
                    JOptionPane.showMessageDialog(SwingConsoleMainPanel.this, t.getMessage(), "Failed to start test", JOptionPane.ERROR_MESSAGE);
                    logger.warn("Failed to start test", t);
                }
            }
        });
        agentControlPanel.add(startTestButton, "flowx,cell 0 0,alignx left,aligny top");

        stopTestButton = new JDynamicStateButton("Stop test");
        stopTestButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.stopTest(true);
            }
        });
        agentControlPanel.add(stopTestButton, "cell 0 0");

        killAgentsButton = new JButton("Kill Agents");
        killAgentsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.killAgentsAndReset();
            }
        });
        agentControlPanel.add(killAgentsButton, "cell 0 0");

        resetStatsButton = new JButton("Reset stats");
        resetStatsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.resetStats();
            }
        });
        agentControlPanel.add(resetStatsButton, "cell 0 0");

        generateReportButton = new JButton("Report");
        generateReportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.generateReportAsync();
            }
        });
        agentControlPanel.add(generateReportButton, "cell 0 0");

        agentStatusPanel = new AgentStatusButtonsPanel();
        agentStatusPanel.setBorder(new TitledBorder(null, "Agents available", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        topPanel.add(agentStatusPanel, "cell 1 0,alignx left");

        threadStatePanel = new RateControlPanel();
        transactionStatePanel = new TransactionStatePanel();
        machineTelemetryPanel = new MachineTelemetryPanel();
        processTelemetryPanel = new ProcessTelemetryPanel();
        consolePanel = new ConsolePanel();

        JTabbedPane telemetryTabbedPane = new JTabbedPane();
        telemetryTabbedPane.addTab("Machine telemetry", machineTelemetryPanel);
        telemetryTabbedPane.addTab("Process telemetry", processTelemetryPanel);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Transaction View", transactionStatePanel);
        tabbedPane.addTab("Telemetry", telemetryTabbedPane);
        tabbedPane.addTab("Rate control", threadStatePanel);
        tabbedPane.addTab("Console", consolePanel);

        add(tabbedPane, "cell 0 1");
    }

    public void setDisplayAgentControl(boolean b) {
        if (!b && agentControlPanel.getParent() != null) {
            topPanel.remove(agentControlPanel);
            topPanel.remove(agentStatusPanel);
        }
    }

    public void setModel(ConsoleModel model) {
        agentStatusPanel.initialise(model);
        threadStatePanel.initialise(model, controller);
        transactionStatePanel.initialise(model);

        model.addListener(new ConsoleModel.InteractiveModelListenerAdaptor() {

            @Override public void onTelemetryData(DataStructure data) {
                machineTelemetryPanel.update(data);
                processTelemetryPanel.update(data);
            }
        });

        model.getTestRunning().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    startTestButton.setEnabled(false);
                    stopTestButton.setEnabled(true);
                }
                else {
                    startTestButton.setEnabled(true);
                    stopTestButton.setEnabled(false);
                }
            }
        });

        stopTestButton.setName("stopTestButton");
        stopTestButton.setName("startTestButton");

        consolePanel.initialise(model);

        model.addListener(new ConsoleModel.InteractiveModelListenerAdaptor() {
            public void onTestAbandoned(String reason) {
                JOptionPane.showMessageDialog(SwingConsoleMainPanel.this, reason, "Test abandoned", JOptionPane.WARNING_MESSAGE);
            };
        });
    }

}

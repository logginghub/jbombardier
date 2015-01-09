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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.jbombardier.console.JBombardierModel;
import com.jbombardier.console.components.ReflectiveTable;
import com.jbombardier.console.model.ConsoleEventModel;
import net.miginfocom.swing.MigLayout;

import com.logginghub.utils.StringUtils;

public class ConsolePanel extends JPanel {

    private ReflectiveTable<ConsoleEventModel> table;
    private JTextArea messageView;
    private JLabel eventCountLabel;

    public ConsolePanel() {
        setLayout(new MigLayout("", "[grow,fill]", "[][grow,fill][]"));

        JButton clear = new JButton("Clear events");
        clear.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                table.clear();
            }
        });
        add(clear, "cell 0 0");

        table = new ReflectiveTable<ConsoleEventModel>(ConsoleEventModel.class);
        table.setEditable(false);
        table.setHiddenColumns("Throwable");
        table.setColumnOrder("Time", "Source", "Severity", "Message");
        JScrollPane tableScroller = new JScrollPane(table);

        table.addSelectionListener(new ReflectiveTable.SelectionHandler<ConsoleEventModel>() {
            public void onSelected(ConsoleEventModel item) {
                messageView.setText(item.getMessage() + "\r\n" + (item.getThrowable() != null ? item.getThrowable() : ""));
            }
        });

        messageView = new JTextArea();
        messageView.setEditable(false);        
        messageView.setLineWrap(true);
        JScrollPane messageViewScroller = new JScrollPane(messageView, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setLeftComponent(tableScroller);
        splitPane.setRightComponent(messageViewScroller);
        add(splitPane, "cell 0 1");

        eventCountLabel = new JLabel("0 events");
        eventCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(eventCountLabel, "cell 0 2");

    }

    public void initialise(final JBombardierModel model) {
        
        table.setMaximumEntries(model.getMaximumConsoleEntries());

        model.addListener(new JBombardierModel.InteractiveModelListenerAdaptor() {
            public void onConsoleEvent(ConsoleEventModel event) {
                table.addItem(event);
//                model.getEvents().clear();
                eventCountLabel.setText(StringUtils.format("{} events", table.getRowCount()));
                table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
            }
        });

//        List<ConsoleEventModel> events = model.getEvents();
//        synchronized (events) {
//            for (ConsoleEventModel consoleEventModel : events) {
//                table.addItem(consoleEventModel);
//            }
//        }

        eventCountLabel.setText(StringUtils.format("{} events", table.getRowCount()));
    }
}

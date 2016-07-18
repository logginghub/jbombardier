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

package com.jbombardier.console.micro;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import com.logginghub.utils.observable.Binder;

public class TestControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JCheckBox enabledCheckbox;
    private JSpinner targetRateSpinner;
    private JSpinner threadsSpinner;
    private JLabel nameLabel;

    public TestControlPanel() {
        setLayout(new MigLayout("", "[][][][][][]", "[]"));
        
        nameLabel = new JLabel("Test name");
        add(nameLabel, "cell 0 0");
        
        enabledCheckbox = new JCheckBox("Enabled");
        add(enabledCheckbox, "cell 1 0");
        
        JLabel lblNewLabel = new JLabel("Target rate");
        add(lblNewLabel, "cell 2 0");
        
        targetRateSpinner = new JSpinner();
        add(targetRateSpinner, "cell 3 0");
        
        JLabel lblNewLabel_1 = new JLabel("Threads");
        add(lblNewLabel_1, "cell 4 0");
        
        threadsSpinner = new JSpinner();
        add(threadsSpinner, "cell 5 0");
        
    }
    
    public void bind(MicroBenchmarkTestModel model) {
        
        Binder.bind(model.getName(), nameLabel);
        Binder.bind(model.getEnabled(), enabledCheckbox);
        Binder.bind(model.getTargetRate(), targetRateSpinner);
        Binder.bind(model.getThreads(), threadsSpinner);
        
    }
    
}

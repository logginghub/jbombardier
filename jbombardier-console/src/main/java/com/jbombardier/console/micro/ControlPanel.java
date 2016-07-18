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

import com.logginghub.utils.observable.ObservableListListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public ControlPanel() {
        setLayout(new MigLayout("", "", ""));
        add(new TestControlPanel(), "cell 0 0,alignx left,aligny top");
        add(new TestControlPanel(), "cell 0 1,alignx left,aligny top");
        add(new TestControlPanel(), "cell 0 2,alignx left,aligny top");
    }
    
    public void bind(MicroBenchmarkModel model) {
        removeAll();
        model.getModels().addListenerAndNotifyCurrent(new ObservableListListener<MicroBenchmarkTestModel>() {
            @Override public void onRemoved(MicroBenchmarkTestModel t, int index) {}
            @Override public void onCleared() {}
            @Override public void onAdded(MicroBenchmarkTestModel t) {
                TestControlPanel panel = new TestControlPanel();
                panel.bind(t);
                add(panel, "wrap");
            }
        });
    }

}

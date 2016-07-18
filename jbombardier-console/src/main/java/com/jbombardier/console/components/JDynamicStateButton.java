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

package com.jbombardier.console.components;

import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;

import javax.swing.*;

/**
 * Extended JButton that changes it's enabled/disabled state in response to a bean update for a boolean value.
 *
 * @author James
 */
public class JDynamicStateButton extends JButton {
    private static final long serialVersionUID = 1L;
    private boolean enabledOnTrue = true;

    public JDynamicStateButton(String text, ObservableProperty<Boolean> property) {
        super(text);
        bind(property);
        changeState(property.get());
    }

    public JDynamicStateButton(String text) {
        super(text);
    }

    public void bind(ObservableProperty<Boolean> property) {
        property.addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if (oldValue != newValue) {
                    changeState(newValue);
                }
            }
        });
    }

    private void changeState(final boolean newValue) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (enabledOnTrue == newValue) {
                    setEnabled(true);
                } else {
                    setEnabled(false);
                }
            }
        });
    }
}

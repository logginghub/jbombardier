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

package com.jbombardier.console.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import com.logginghub.utils.AbstractBean;

/**
 * Extended JButton that changes it's enabled/disabled state in response to a bean
 * update for a boolean value.
 * 
 * @author James
 */
public class JDynamicStateButton extends JButton {
    private static final long serialVersionUID = 1L;
    
    private AbstractBean bean;
    private String propertyName;
    private boolean enabledOnTrue = true;

    public JDynamicStateButton(AbstractBean bean, String propertyName) {
        super();
        bind(bean, propertyName);
    }

    public JDynamicStateButton(Action a, AbstractBean bean, String propertyName) {
        super(a);
        this.bean = bean;
        this.propertyName = propertyName;
    }

    public JDynamicStateButton(Icon icon, AbstractBean bean, String propertyName) {
        super(icon);
        this.bean = bean;
        this.propertyName = propertyName;
    }

    public JDynamicStateButton(String text, Icon icon, AbstractBean bean, String propertyName, boolean initialValue) {
        super(text, icon);
        bind(bean, propertyName);
        changeState(initialValue);
    }

    public JDynamicStateButton(String text, AbstractBean bean, String propertyName, boolean initialValue) {
        super(text);
        bind(bean, propertyName);
        changeState(initialValue);
    }
    
    public JDynamicStateButton(String text) {
        super(text);
    }
    
    public AbstractBean getBean() {
        return bean;
    }
    
    public String getPropertyName() {
        return propertyName;
    }

    public void setEnabledOnTrue(boolean enabledOnTrue) {
        this.enabledOnTrue = enabledOnTrue;
    }
    
    public boolean isEnabledOnTrue() {
        return enabledOnTrue;
    }

    public void bind(AbstractBean bean, String propertyName) {
        this.bean = bean;
        this.propertyName = propertyName;
        bean.addPropertyChangeListener(propertyName, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                boolean oldValue = (Boolean) evt.getOldValue();
                boolean newValue = (Boolean) evt.getNewValue();
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
                }
                else {
                    setEnabled(false);
                }
            }
        });
    }
}

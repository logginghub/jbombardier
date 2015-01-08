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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.logginghub.utils.AbstractBean;

/**
 * Extended JLabel that changes it's background colour in response to a bean
 * update for a boolean value.
 * 
 * @author James
 */
public class JDynamicBackgroundLabel extends JLabel {
    private static final long serialVersionUID = 1L;
    
    private AbstractBean bean;
    private String propertyName;
    private Color trueColour = Color.green;
    private Color falseColour = Color.red;

    public JDynamicBackgroundLabel() {
        super();
    }

    public JDynamicBackgroundLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
    }

    public JDynamicBackgroundLabel(Icon image) {
        super(image);
    }

    public JDynamicBackgroundLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
    }

    public JDynamicBackgroundLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
    }

    public JDynamicBackgroundLabel(String text, AbstractBean bean, String property, boolean initialValue) {
        super(text);
        bind(bean, property);
        changeBackground(initialValue);
        setOpaque(true);
    }

    public AbstractBean getBean() {
        return bean;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public Color getTrueColour() {
        return trueColour;
    }
    
    public Color getFalseColour() {
        return falseColour;
    }
    
    public void setTrueColour(Color trueColour) {
        this.trueColour = trueColour;
    }
    
    public void setFalseColour(Color falseColour) {
        this.falseColour = falseColour;
    }

    private void bind(AbstractBean bean, String propertyName) {
        this.bean = bean;
        this.propertyName = propertyName;
        bean.addPropertyChangeListener(propertyName, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                boolean oldValue = (Boolean) evt.getOldValue();
                boolean newValue = (Boolean) evt.getNewValue();
                if (oldValue != newValue) {
                    changeBackground(newValue);
                }
            }
        });
    }

    private void changeBackground(final boolean newValue) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (newValue) {
                    setBackground(trueColour);
//                    setForeground(trueColour);
                }
                else {                    
                    setBackground(falseColour);
//                    setForeground(falseColour);
                }
            }
        });
    }
}

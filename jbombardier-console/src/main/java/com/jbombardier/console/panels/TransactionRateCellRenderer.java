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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.jbombardier.console.components.ReflectiveTable;
import com.jbombardier.console.model.TransactionResultModel;

/**
 * Custom cell renderer for transaction rate cells in the transaction table.
 * @author James
 */
public class TransactionRateCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;

    private Color goodColour = Color.GREEN.darker().darker();
    private Color warningColour = Color.YELLOW.darker().darker();
    private Color errorColour = Color.RED.darker().darker();

    public TransactionRateCellRenderer() {
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        ReflectiveTable<TransactionResultModel> t = (ReflectiveTable<TransactionResultModel>)table;
        TransactionResultModel itemAtRow = t.getItemAtRow(row);
        
        double targetTransactions = itemAtRow.getTargetSuccessfulTransactionsPerSecond().get();
        double successPerSecond = itemAtRow.getSuccessfulMeanTransactionsPerSecond().get();
        
        double delta = successPerSecond - targetTransactions;        
        double abs = Math.abs(delta);
        
        double percent = 100d * abs / targetTransactions;
        
        if(percent < 2) {
            label.setForeground(goodColour);
        }else if(percent < 5){
            label.setForeground(warningColour);
        }
        else {            
            label.setForeground(errorColour);
        }
        
        if(!isSelected){        
        if(row % 2 == 0){
            Color color = new Color(235, 245, 252);
            label.setBackground(color);
            
        }else{
            label.setBackground(Color.white);
        }
        }
        
        setText(value.toString());
        
        return label;
    }
}

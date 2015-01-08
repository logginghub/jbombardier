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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.jbombardier.console.model.ChartLineFormat;

/**
 * Custom cell renderer for the line paint/stroke column in the transaction
 * table.
 * 
 * @author James
 */
public class ChartLineFormatCellRenderer extends JComponent implements TableCellRenderer {
    private static final long serialVersionUID = 1L;
    private ChartLineFormat chartLineFormat;

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        chartLineFormat = (ChartLineFormat) value;
        return this;
    }

    @Override public void paintComponent(Graphics g) {
        super.paintComponents(g);

        // Due to random NPE, guess there is no guarantee the other method will
        // be called first?
        if (chartLineFormat != null) {
            Graphics2D g2d = (Graphics2D) g;

            Stroke originalStroke = g2d.getStroke();
            Paint originalPaint = g2d.getPaint();

            g2d.setPaint(chartLineFormat.getPaint());
            g2d.setStroke(chartLineFormat.getStroke());

            int width = getWidth();
            int height = getHeight();
            g2d.drawLine(0, height / 2, width, height / 2);

            g2d.setPaint(originalPaint);
            g2d.setStroke(originalStroke);
        }
    }

    public void validate() {}

    public void revalidate() {}

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}

    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}

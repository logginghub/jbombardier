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

package com.jbombardier.console.charts;



import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class StackedBarChart extends JPanel {

    public StackedBarChart() {
        this("Stacked bar chart");
    }
    
    public StackedBarChart(String title) {
        setLayout(new MigLayout());
        add(createChart(title), "grow");
    }
    
    private static final long serialVersionUID = 1L;
    private DefaultCategoryDataset categorydataset = new DefaultCategoryDataset();
    private CategoryPlot categoryplot;

//    public void update(MachineTelemetry machineTelemetry) {
//        categorydataset.setValue(machineTelemetry.getSystemCPU() * 100, "System", machineTelemetry.getHost());
//        categorydataset.setValue(machineTelemetry.getUserCPU() * 100, "User", machineTelemetry.getHost());
//        categorydataset.setValue(machineTelemetry.getWaitCPU() * 100, "Wait", machineTelemetry.getHost());
//        categorydataset.setValue(machineTelemetry.getIdleCPU() * 100, "Idle", machineTelemetry.getHost());
//        
//        categoryplot.getRangeAxis().setAutoRange(false);
//        categoryplot.getRangeAxis().setRange(0, 100);
//    }
//    
//    public void updateIO(MachineTelemetry machineTelemetry) {
//        categorydataset.setValue(machineTelemetry.getNetworkReceivedBytes(), "Received", machineTelemetry.getHost());
//        categorydataset.setValue(machineTelemetry.getNetworkSentBytes(), "Sent", machineTelemetry.getHost());        
//        categoryplot.getRangeAxis().setAutoRange(true);        
//    }
//
//    public void updateMemory(MachineTelemetry machineTelemetry) {
//        categorydataset.setValue(machineTelemetry.getMemoryFree(), "Free", machineTelemetry.getHost());               
//        categoryplot.getRangeAxis().setAutoRange(true);
//    }
    
    public JPanel createChart(String title) {
        JFreeChart jfreechart = ChartFactory.createStackedBarChart(title,
                                                                   "Category",
                                                                   "Value",
                                                                   categorydataset,
                                                                   PlotOrientation.VERTICAL,
                                                                   true,
                                                                   true,
                                                                   false);
        categoryplot = (CategoryPlot) jfreechart.getPlot();
        StackedBarRenderer stackedbarrenderer = (StackedBarRenderer) categoryplot.getRenderer();
        stackedbarrenderer.setDrawBarOutline(false);
        stackedbarrenderer.setBaseItemLabelsVisible(true);
        stackedbarrenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());

        ChartPanel moo = new ChartPanel(jfreechart);
        moo.setMinimumDrawHeight(0);
        moo.setMinimumDrawWidth(0);
        moo.setMaximumDrawHeight(Integer.MAX_VALUE);
        moo.setMaximumDrawWidth(Integer.MAX_VALUE);
        return moo;
    }

    

    

}

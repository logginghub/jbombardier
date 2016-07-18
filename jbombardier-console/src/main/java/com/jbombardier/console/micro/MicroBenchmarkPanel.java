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

import com.jbombardier.common.BasicTestStats;
import com.jbombardier.console.charts.XYTimeChartPanel;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class MicroBenchmarkPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private XYTimeChartPanel timeChartPanel;
    private XYTimeChartPanel rateChartPanel;
    
    private ControlPanel controlPanel;
    
    public MicroBenchmarkPanel() {
        setLayout(new MigLayout("", "[][grow]", "[grow][grow]"));
        
        controlPanel = new ControlPanel();
        controlPanel.setBorder(new TitledBorder(null, "Test control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(controlPanel, "cell 0 0,grow");
        
        timeChartPanel = new XYTimeChartPanel();
        add(timeChartPanel, "cell 1 0");
        
        rateChartPanel = new XYTimeChartPanel();
        add(rateChartPanel, "cell 2 0");
        
        timeChartPanel.setTitle("Transaction times (/ms)");
        rateChartPanel.setTitle("Transaction rates (tp/s)");
        
        ResultsTablePanel resultsTablePanel = new ResultsTablePanel();
        resultsTablePanel.setBorder(new TitledBorder(null, "Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(resultsTablePanel, "cell 0 1 2 1,grow");
    }

    public void bind(MicroBenchmarkModel model) {
        controlPanel.bind(model);
        
        model.getModels().addListenerAndNotifyCurrent(new ObservableListListener<MicroBenchmarkTestModel>() {
            @Override public void onRemoved(MicroBenchmarkTestModel t, int index) {}
            @Override public void onCleared() {}
            @Override public void onAdded(final MicroBenchmarkTestModel t) {
                t.getStats().addListener(new ObservablePropertyListener<BasicTestStats>() {
                    @Override public void onPropertyChanged(BasicTestStats oldValue, BasicTestStats newValue) {
                        
                        double time = (newValue.totalDurationSuccess*1e-6d) / newValue.transactionsSuccess;
                        timeChartPanel.addValue(t.getName().get(), System.currentTimeMillis(), time);
                        rateChartPanel.addValue(t.getName().get(), System.currentTimeMillis(), newValue.transactionsSuccess);
                        
                    }
                });
            }
        });
        
    }
    
}

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

import com.jbombardier.console.JBombardierController;
import com.jbombardier.console.JBombardierModel;
import com.jbombardier.console.charts.LineFormatController;
import com.jbombardier.console.charts.XYTimeChartPanel;
import com.jbombardier.console.components.ReflectiveTable;
import com.jbombardier.console.components.TableDataProvider;
import com.jbombardier.console.model.ChartLineFormat;
import com.jbombardier.console.model.PhaseModel;
import com.jbombardier.console.model.TransactionResultModel;
import com.jbombardier.console.model.TransactionResultModel.ChartEvent;
import com.logginghub.swingutils.MigPanel;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.Comparator;

/**
 * The main view - it displays the pair of charts for transaction rates and times, and below them the result table.
 *
 * @author James
 */
public class TransactionStatePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private ReflectiveTable<TransactionResultModel> table;
    private XYTimeChartPanel rateChartPanel;
    private XYTimeChartPanel elapsedChartPanel;
    private LineFormatController lineFormatController;

    /**
     * Create the panel.
     */
    public TransactionStatePanel() {
        setLayout(new MigLayout("ins 0", "[grow]", "[grow]"));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.8);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        add(splitPane, "cell 0 0,grow");

        JPanel transactionRatePanel = new MigPanel("", "[grow][grow]", "[grow]");
        splitPane.setLeftComponent(transactionRatePanel);
        transactionRatePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "Transaction Rate",
                TitledBorder.LEADING,
                TitledBorder.TOP,
                null,
                Color.black));

        lineFormatController = new LineFormatController();

        rateChartPanel = new XYTimeChartPanel();
        rateChartPanel.setTitle("Transaction rates");
        rateChartPanel.setDatapoints(120);
        rateChartPanel.setLineFormatController(lineFormatController);
        rateChartPanel.setShapesVisible(false);
        rateChartPanel.setLegendVisible(false);
        rateChartPanel.disableJFreeChartMenu();
        rateChartPanel.setYAxisLabel("Total transactions / sec (5 second moving average)");
        transactionRatePanel.add(rateChartPanel, "cell 0 0,grow");

        elapsedChartPanel = new XYTimeChartPanel();
        elapsedChartPanel.setTitle("Transaction Elapsed Times");
        elapsedChartPanel.setDatapoints(120);
        elapsedChartPanel.setShapesVisible(false);
        elapsedChartPanel.setLineFormatController(lineFormatController);
        elapsedChartPanel.setYAxisLabel("Average elapsed time / ms");
        elapsedChartPanel.setLegendVisible(false);
        elapsedChartPanel.disableJFreeChartMenu();
        transactionRatePanel.add(elapsedChartPanel, "cell 1 0,grow");

        JPanel rateControlPanel = new MigPanel("", "[grow]", "[fill]");
        splitPane.setRightComponent(rateControlPanel);
        rateControlPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "Realtime results",
                TitledBorder.LEADING,
                TitledBorder.TOP,
                null,
                new Color(0, 0, 0)));

        JScrollPane scrollPane = new JScrollPane();
        final JCheckBox hideBaseTest = new JCheckBox("Hide base tests");
        hideBaseTest.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                changeHideBaseTestState(hideBaseTest.isSelected());
            }
        });
        rateControlPanel.add(hideBaseTest, "wrap");
        rateControlPanel.add(scrollPane, "aligny top,grow");

        final String[] columnNames = new String[]{"", "Test", "Transaction", "SLA", "Transactions", "Target", "Successes/s", "Success duration", "Tp90", "Stddev", "Time in test (ms)", "MaximumRate"};

        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        table = new ReflectiveTable<TransactionResultModel>(TransactionResultModel.class,
                new TableDataProvider<TransactionResultModel>() {
                    @Override public Object getValueForColumn(int column, TransactionResultModel item) {
                        switch (column) {
                            case 0:
                                return item.getChartLineFormat().get();
                            case 1:
                                return item.getTestName().get();
                            case 2:
                                return item.getTransactionName().get();
                            case 3:
                                return nf.format(item.getSuccessfulTransactionDurationSLA().get());
                            case 4:
                                return nf.format(item.calculateTotalTransactions());
                            case 5:
                                return nf.format(item.getTargetSuccessfulTransactionsPerSecond().get());
                            case 6:
                                return nf.format(item.getSuccessfulMeanTransactionsPerSecond().get());
                            case 7:
                                return TimeUtils.formatIntervalNanoseconds(item.getSuccessfulTransactionDuration()
                                                                               .get());
                            case 8:
                                return nf.format(item.getTp90().get());
                            case 9:
                                return nf.format(item.getStddev().get());
                            case 10:
                                return nf.format(item.getSuccessfulTransactionTotalDuration().get() * 1e-6);
                            case 11:
                                return nf.format(item.calculateMaximumRate());
                            default:
                                return "?";
                        }
                    }

                    @Override public String getColumnName(int column) {
                        return columnNames[column];
                    }

                    @Override public int getColumnCount() {
                        return columnNames.length;
                    }
                });
        //
        // table.dontShowColumns("successfulTransactionsAverageNanos",
        // "failedTransactionsAverageNanos",
        // "successfulTransactionsTotalAverageNanos",
        // "Transaction",
        // "FailureThreshold",
        // "TransactionFailureCountThreshold",
        // "ResultCount",
        // "TotalTransactions",
        // "FailureThresholdMode");
        //
        // table.setColumnIndex("ChartLineFormat", 0);
        // table.setColumnIndex("Test", 1);
        // table.setColumnIndex("TransactionName", 2);
        // table.setColumnIndex("TransactionSLA", 3);
        // table.setColumnIndex("TotalTransactions", 4);
        // table.setColumnIndex("TargetTransactions", 5);
        // table.setColumnIndex("SuccessPerSecond", 6);
        // table.setColumnIndex("SuccessElapsedMS", 7);
        // table.setColumnIndex("Tp90", 8);
        // table.setColumnIndex("Stddev", 9);
        // table.setColumnIndex("SuccessfulTransactionsTotalMS", 10);
        // table.setColumnIndex("MaximumRate", 11);
        //
        // table.setColumnNameReplacement("ChartLineFormat", "");
        // table.setColumnNameReplacement("TestName", "Test");
        // table.setColumnNameReplacement("SuccessElapsedMS", "Success (ms)");
        // table.setColumnNameReplacement("SuccessfulTransactionsTotalMS", "Success (total ms)");

        table.forceColumnWidth(8, 50);
        table.forceColumnWidth(9, 50);

        table.getColumnModel().getColumn(0).setCellRenderer(new ChartLineFormatCellRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(20);
        table.getColumnModel().getColumn(0).setWidth(20);
        table.getColumnModel().getColumn(0).setMaxWidth(20);
        table.getColumnModel().getColumn(0).setMinWidth(20);

        TransactionRateCellRenderer transactionRateCellRenderer = new TransactionRateCellRenderer();
        table.getColumnModel().getColumn(5).setCellRenderer(transactionRateCellRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(transactionRateCellRenderer);

        table.setRowComparator(new Comparator<TransactionResultModel>() {
            public int compare(TransactionResultModel o1, TransactionResultModel o2) {
                return CompareUtils.compare(o1.getTestName(), o2.getTestName());
            }
        });
        scrollPane.setViewportView(table);
    }

    protected void changeHideBaseTestState(boolean selected) {
        if (selected) {
            table.setRowVisibilityFilter(new ReflectiveTable.RowVisibilityFilter<TransactionResultModel>() {
                public boolean isVisible(TransactionResultModel row) {
                    return row.isTransaction();
                }
            });
        } else {
            table.clearRowVisibilityFilter();
        }
    }

    public void bind(JBombardierModel model) {

        final ObservableListListener<TransactionResultModel> listener = new ObservableListListener<TransactionResultModel>() {
            @Override public void onRemoved(TransactionResultModel t, int index) {}

            @Override public void onCleared() {
                table.clear();
                rateChartPanel.clearChartData();
                elapsedChartPanel.clearChartData();
            }

            @Override public void onAdded(final TransactionResultModel testModel) {
                configureTransactionResultModel(testModel);
            }
        };

        model.getCurrentPhase().addListenerAndNotifyCurrent(new ObservablePropertyListener<PhaseModel>() {
            @Override public void onPropertyChanged(PhaseModel oldModel, PhaseModel newModel) {

                if (oldModel != null) {
                    oldModel.getTransactionResultModels().removeListener(listener);
                }

                if (newModel != null) {
                    rateChartPanel.clearChartData();
                    elapsedChartPanel.clearChartData();
                    table.clear();
                    newModel.getTransactionResultModels().addListenerAndNotifyCurrent(listener);
                }
            }
        });

        JBombardierController.getEventStream().addListener(new StreamListener<String>() {
            @Override public void onNewItem(final String t) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        rateChartPanel.addEvent(System.currentTimeMillis(), t);
                        elapsedChartPanel.addEvent(System.currentTimeMillis(), t);
                    }
                });
            }
        });

        model.addListener(new JBombardierModel.InteractiveModelListenerAdaptor() {
            public void onTestStarted() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        table.clear();
                        rateChartPanel.clearChartData();
                        elapsedChartPanel.clearChartData();
                    }
                });
            }
        });
    }

    private void configureTransactionResultModel(final TransactionResultModel testModel) {
        table.addItem(testModel);

        String key = testModel.getKey();

        Paint paint = lineFormatController.allocateColour(key);
        final String successName = key + ".success";
        final String failureName = key + ".failure";

        lineFormatController.setPaint(successName, paint);
        lineFormatController.setPaint(failureName, paint);
        BasicStroke successStroke = new BasicStroke(2);
        lineFormatController.setStroke(successName, successStroke);

        float[] dashes = {3.0F, 3.0F, 3.0F, 3.0F};
        BasicStroke failStroke = new BasicStroke(1.0F,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0F,
                dashes,
                0.F);
        lineFormatController.setStroke(failureName, failStroke);

        testModel.getChartLineFormat().set(new ChartLineFormat(paint, successStroke));

        testModel.getModelUpdates().addListener(new ObservablePropertyListener<Long>() {
            @Override public void onPropertyChanged(Long oldValue, Long newValue) {
                final long now = System.currentTimeMillis();

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        if (testModel.getSuccessfulMeanTransactionsPerSecond().get() > 0) {
                            rateChartPanel.addValue(successName,
                                    now,
                                    testModel.getSuccessfulMeanTransactionsPerSecond().get());
                        }

                        if (testModel.getUnsuccessfulMeanTransactionsPerSecond().get() > 0) {
                            rateChartPanel.addValue(failureName,
                                    now,
                                    testModel.getUnsuccessfulMeanTransactionsPerSecond().get());
                        }

                        if (testModel.getSuccessfulTransactionDuration().get() > 0) {
                            elapsedChartPanel.addValue(successName,
                                    now,
                                    testModel.getSuccessfulTransactionDuration().get() * 1e-6);
                        }

                        if (testModel.getUnsuccessfulTransactionDuration().get() > 0) {
                            elapsedChartPanel.addValue(failureName,
                                    now,
                                    testModel.getUnsuccessfulTransactionDuration().get()* 1e-6);
                        }

                        rateChartPanel.removeOldDataPoints();
                        elapsedChartPanel.removeOldDataPoints();
                    }
                });
            }
        });

        testModel.getChartEvents().addListenerAndNotifyCurrent(new ObservableListListener<ChartEvent>() {
            @Override public void onRemoved(ChartEvent t, int index) {}

            @Override public void onCleared() {}

            @Override public void onAdded(final ChartEvent t) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        elapsedChartPanel.addEvent(t.time, t.text);
                        rateChartPanel.addEvent(t.time, t.text);
                    }
                });
            }
        });
    }
}

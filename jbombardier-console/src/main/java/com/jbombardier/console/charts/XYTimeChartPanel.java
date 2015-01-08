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

package com.jbombardier.console.charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Out;

/**
 * XY scatter chart that takes care of itself. You just give it [series, x, y] values and it does
 * the rest. The x values are chunked based on the chunk interval. x values that fall off the edge
 * are removed. You can also update a particular x value rather than set it directly.
 * 
 * @author James
 * 
 */
public class XYTimeChartPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private Map<String, XYSeries> seriesForSource = new HashMap<String, XYSeries>();
    private XYSeriesCollection xyseriescollection = new XYSeriesCollection();
    private JFreeChart chart;

    private String imageFilename;
    private int imageFileWidth = 1024;
    private int imageFileHeight = 768;
    private XYTimeChartPanel chartpanel;

    private XYPlot xyplot;

    private long timePeriod = 2 * 60 * 1000;
    private int datapoints = 1000;
    private long chunkPeriod = 1000;
    private NumberAxis yAxis;
    private long mostRecentTimeValue;
    private ChartPanel jFreeChartPanel;

    public XYTimeChartPanel() {

        DateAxis numberaxis = new DateAxis("Time");

        yAxis = new NumberAxis("Count");
        yAxis.setAutoRangeIncludesZero(true);

        XYSplineRenderer renderer = new XYSplineRenderer();
        // XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        xyplot = new XYPlot(xyseriescollection, numberaxis, yAxis, renderer);
        xyplot.setBackgroundPaint(Color.white);
        xyplot.setDomainGridlinePaint(Color.lightGray);
        xyplot.setRangeGridlinePaint(Color.lightGray);

        // xyplot.setAxisOffset(new RectangleInsets(4D, 4D, 4D, 4D));

        // XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)
        // xyplot.getRenderer();
        // xylineandshaperenderer.setBaseShapesVisible(false);
        // xylineandshaperenderer.setBaseShapesFilled(false);

        chart = new JFreeChart("Running threads", JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);

        /*
         * ValueMarker valuemarker1 = new ValueMarker(175D);
         * valuemarker1.setLabelOffsetType(LengthAdjustmentType.EXPAND);
         * valuemarker1.setPaint(Color.red); valuemarker1.setLabel("Target Price");
         * valuemarker1.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
         * valuemarker1.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
         * xyplot.addRangeMarker(valuemarker1);
         */

        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        // ChartUtilities.applyCurrentTheme(chart);
        setLayout(new BorderLayout());
        jFreeChartPanel = new ChartPanel(chart);
        jFreeChartPanel.setMinimumDrawHeight(0);
        jFreeChartPanel.setMinimumDrawWidth(0);
        jFreeChartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);
        jFreeChartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);

        add(jFreeChartPanel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new MigLayout("gap 0, ins 0", "[grow,center,fill]", "[grow,center]"));
        final JCheckBox checkbox = new JCheckBox("Auto-scale");
        checkbox.setSelected(true);
        checkbox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                toggleAutoscroll(checkbox.isSelected());
            }
        });
        checkbox.setHorizontalAlignment(SwingConstants.RIGHT);
        controls.add(checkbox, "cell 0 0,alignx center");
        add(controls, BorderLayout.SOUTH);
    }

    public void setShapesVisible(boolean visible) {
        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyplot.getRenderer();
        xylineandshaperenderer.setBaseShapesVisible(visible);
        xylineandshaperenderer.setBaseShapesFilled(visible);
    }

    protected void toggleAutoscroll(boolean selected) {
        yAxis.setAutoRange(selected);
    }

    public void setYAxisLabel(String yAxisLabel) {
        yAxis.setLabel(yAxisLabel);
    }

    public void setSplineRenderer(boolean splineRenderer) {
        if (splineRenderer) {
            xyplot.setRenderer(new XYSplineRenderer());
        }
        else {
            xyplot.setRenderer(new XYLineAndShapeRenderer());
        }
    }

    public void setTitle(String title) {
        chart.setTitle(title);
    }

    public void updateValue(String series, long currentTimeMillis, double value) {
        XYSeries xySeries = getSeriesForSource(series);
        synchronized (xySeries) {
            xySeries.update(chunk(currentTimeMillis), value);
        }
    }

    public void addValue(String series, long time, double value) {
        XYSeries xySeries = getSeriesForSource(series);
        long chunk = chunk(time);
        synchronized (xySeries) {
            xySeries.add(chunk, value);
        }

        mostRecentTimeValue = Math.max(time, mostRecentTimeValue);
    }

    private long chunk(long time) {
        return time - (time % chunkPeriod);
    }

    protected XYSeries getSeriesForSource(String label) {
        XYSeries xySeries;
        synchronized (seriesForSource) {
            xySeries = seriesForSource.get(label);
            if (xySeries == null) {
                xySeries = new XYSeries(label);
                int seriesIndex = xyseriescollection.getSeriesCount();
                xyseriescollection.addSeries(xySeries);

                xySeries.setMaximumItemCount(datapoints);
                seriesForSource.put(label, xySeries);

                if (lineFormatController != null) {
                    Paint paint = lineFormatController.allocateColour(label);

                    XYPlot xyPlot = chart.getXYPlot();
                    XYItemRenderer xyir = xyPlot.getRenderer();
                    xyir.setSeriesPaint(seriesIndex, paint);

                    Stroke stroke = lineFormatController.getStroke(label);
                    xyir.setSeriesStroke(seriesIndex, stroke);
                }
            }
        }
        return xySeries;
    }

    private void addUpperValueExceeded(double value, long startOfCurrentChunk) {
        // Hour hour = new Hour(2, new Day(22, 2, 2010));
        // Minute minute = new Minute(15, hour);
        // long d = minute.getFirstMillisecond();
        //
        ValueMarker valuemarker3 = new ValueMarker(startOfCurrentChunk);
        valuemarker3.setPaint(Color.black);
        valuemarker3.setLabel("Threshold exceeded (" + value + ")");
        valuemarker3.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        valuemarker3.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        xyplot.addDomainMarker(valuemarker3);
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    public void setImageFileHeight(int imageFileHeight) {
        this.imageFileHeight = imageFileHeight;
    }

    public void setImageFileWidth(int imageFileWidth) {
        this.imageFileWidth = imageFileWidth;
    }

    public void complete() {
        String filename = imageFilename;

        if (filename == null) {
            filename = chart.getTitle().getText() + ".png";
        }

        File file = new File(filename);
        try {
            ChartUtilities.saveChartAsPNG(file, chart, imageFileWidth, imageFileHeight);
            Out.out("Chart written to '{}'", file.getAbsolutePath());
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to save png [%s]", file.getAbsolutePath()), e);
        }
    }

    public JComponent getComponent() {
        return chartpanel;
    }

    public JFreeChart getChart() {
        return chart;
    }

    public void setDatapoints(int datapoints) {
        this.datapoints = datapoints;
    }

    public void clearChartData() {
        synchronized (seriesForSource) {
            Collection<XYSeries> values = seriesForSource.values();
            for (XYSeries xySeries : values) {
                xySeries.clear();
            }
        }
    }

    public static String newline = System.getProperty("line.separator");
    private float yMinimumFilter = Float.NaN;
    private LineFormatController lineFormatController;

    public void saveChartData() {
        StringBuilder builder = new StringBuilder();
        synchronized (seriesForSource) {
            Set<Long> xValues = new HashSet<Long>();

            Collection<XYSeries> values = seriesForSource.values();
            for (XYSeries xySeries : values) {
                List<XYDataItem> items = xySeries.getItems();
                for (XYDataItem item : items) {
                    double xValue = item.getXValue();
                    long xTimeValue = (long) xValue;
                    xValues.add(xTimeValue);
                }
            }

            List<Long> xValuesList = new ArrayList<Long>(xValues);
            Collections.sort(xValuesList);
            Set<String> keys = seriesForSource.keySet();

            builder.append("Time,");
            for (String seriesKey : keys) {
                builder.append(seriesKey).append(",");
            }
            builder.append(newline);

            for (Long xValue : xValuesList) {
                Date date = new Date(xValue);
                builder.append(date.toString());
                builder.append(",");
                for (String seriesKeys : keys) {
                    XYSeries xySeries = seriesForSource.get(seriesKeys);
                    Double d = findValue(xySeries, xValue);
                    if (d != null) {
                        builder.append(d);
                    }
                    builder.append(",");
                }

                builder.append(newline);
            }
        }

        String filename = chart.getTitle().getText() + ".csv";
        File file = new File(filename);
        FileUtils.write(builder.toString(), file);
        Out.out("Data saved to '{}'", file.getAbsolutePath());
    }

    private Double findValue(XYSeries xySeries, long xValue) {
        Double value = null;

        List<XYDataItem> items = xySeries.getItems();
        for (XYDataItem item : items) {
            double itemXValue = item.getXValue();
            long xTimeValue = (long) itemXValue;

            if (xTimeValue == xValue) {
                value = item.getYValue();
                break;
            }
        }

        return value;
    }

    public void saveChartImage() {
        complete();
    }

    public void setYMinimumFilter(float yMinimumFilter) {
        this.yMinimumFilter = yMinimumFilter;
    }

    public void setLegendVisible(boolean visible) {
        chart.getLegend().setVisible(visible);
    }

    public void setLineFormatController(LineFormatController lineFormatController) {
        this.lineFormatController = lineFormatController;
    }

    @SuppressWarnings("unchecked") public void removeOldDataPoints() {
        List<XYSeries> series = xyseriescollection.getSeries();
        for (XYSeries xySeries : series) {
            while (xySeries.getItemCount() > 0) {
                XYDataItem xyDataItem = xySeries.getDataItem(0);
                long itemTime = xyDataItem.getX().longValue();
                if (mostRecentTimeValue - timePeriod > itemTime) {
                    xySeries.remove(0);
                }
                else {
                    // Fast exit, the items will be in time order
                    break;
                }
            }
        }
    }

    public void addEvent(long time, String text) {

        ValueMarker valuemarker3 = new ValueMarker(time);
        // valuemarker3.setPaint(colour);
        valuemarker3.setLabel(text);
        valuemarker3.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        valuemarker3.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        // valuemarker3.setLabelOffset(new RectangleInsets(topOffset, 0, 0, 0));

        // double upperBound = xyplot.getDomainAxis().getUpperBound();
        // double newBound = Math.max(upperBound, xValue + 10);
        // xyplot.getDomainAxis().setUpperBound(newBound);

        xyplot.addDomainMarker(valuemarker3);
    }

    public void disableJFreeChartMenu() {
        jFreeChartPanel.setPopupMenu(null);
    }

}

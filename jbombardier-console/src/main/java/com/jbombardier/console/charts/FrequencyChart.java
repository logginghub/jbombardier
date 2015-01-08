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

import java.awt.Color;
import java.io.File;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.ChartBuilder;
import com.logginghub.analytics.GeneralAggregator;
import com.logginghub.analytics.model.GeneralAggregatedData;
import com.logginghub.utils.CircularArrayList;
import com.logginghub.utils.CollectionUtils;
import com.logginghub.utils.Function;
import com.logginghub.utils.SinglePassStatisticsLongPrecisionCircular;
import com.logginghub.utils.logging.Logger;
import com.jbombardier.console.model.TransactionResultModel;

public class FrequencyChart {

    private static final Logger logger = Logger.getLoggerFor(FrequencyChart.class);

    public void generateChart(TransactionResultModel transactionResultModel, SinglePassStatisticsLongPrecisionCircular copy, File file) {
        CircularArrayList<Long> values = copy.getValues();
        if (values.size() == 0) {
            logger.warn("Cannont generate chart with zero values - test was {}", transactionResultModel.getTestName());
        }
        else {

            Collections.sort(values);

            // Convert to Millis
            Function<Long, Double> function = new Function<Long, Double>() {
                public Double apply(Long input) {
                    return input.doubleValue() / 1e6d;

                }
            };
            List<Double> transformed = CollectionUtils.transform(values, function);

            // Note that the values are in nanoseconds... actually it doesn't
            // matter, lets just shoot for 100 buckets
            copy.doCalculations();

            int buckets = 1 + (int) (Math.log10(values.size()) * 100f);

            double max = copy.getMaximum() / 1e6d;
            double rangePerBucket = max / buckets;

            GeneralAggregator aggregator = new GeneralAggregator();
            GeneralAggregatedData aggregated = aggregator.aggregate(transactionResultModel.getKey(), transformed, rangePerBucket, true);
            // aggregated.dump();

            double mean = copy.getMean() / 1e6;
            double tp90 = copy.getPercentiles()[90] / 1e6;

            ChartBuilder.startXY()
                        .addSeries("Frequency", aggregated, AggregatedDataKey.Count)
                        .setTitle("Frequency distribution of " + transactionResultModel.getKey())
                        .setPlainXY()
                        .addYMarker(transactionResultModel.getTransactionSLA(), "SLA (" + transactionResultModel.getTransactionSLA() + ")", Color.BLACK, 0)
                        .addYMarker(tp90, "TP90 (" + NumberFormat.getInstance().format(tp90) + ")", Color.BLACK, 15)
                        .addYMarker(mean, "Mean (" + NumberFormat.getInstance().format(mean) + ")", Color.BLACK, 30)
                        .setXAxisLabel("Elapsed time")
                        .setYAxisLabel("Count")
                        .toPng(file, 1000, 600);
        }

    }

}

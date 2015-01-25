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

package com.jbombardier.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.esotericsoftware.minlog.Log;
import com.logginghub.utils.Statistics;
import com.logginghub.utils.Statistics.Statistic;
import com.jbombardier.common.AggregatedResult;
import com.jbombardier.common.AggregatedResultSeries;
import com.jbombardier.common.ResultsPackage;
import com.jbombardier.common.ResultsPackage.ThreadResults;

public class OldReportGenerator
{
    public void generate(Map<InetSocketAddress, ResultsPackage> agentResults, File reportsDir)
    {
        Chunker chunker = new Chunker();

        Log.set(Log.LEVEL_DEBUG);
        
        Set<String> totalsTransactionNames = new HashSet<String>();
        Set<String> perAgentTransactionNames = new HashSet<String>();
        Set<String> perAgentPerThreadTransactionNames = new HashSet<String>();
        Set<String> agentStrings = new HashSet<String>();

        Set<InetSocketAddress> keySet = agentResults.keySet();
        for (InetSocketAddress inetSocketAddress : keySet)
        {
            String agentString = inetSocketAddress.getHostName();
            Log.debug("Process result from agent " + agentString);
            agentStrings.add(agentString);
            
            ResultsPackage resultsPerThread = agentResults.get(inetSocketAddress);

            Set<String> threadNames = resultsPerThread.getThreadResults().keySet();
            for (String threadName : threadNames)
            {
                Log.debug("Processing result for thread name ", threadName);
                ThreadResults results = resultsPerThread.getThreadResults().get(threadName);
                
                Set<String> transactionIDs = results.keySet();
                for (String transactionID : transactionIDs) {
                    Log.debug("Processing transactionID '" + transactionID  + "'");
                    String agentKey = agentString + "." + transactionID + ".elapsed";
                    String agentThreadKey = agentString + "." + threadName + "." + transactionID + ".elapsed";
                    String totalKey = "total." + transactionID + ".elapsed";

                    totalsTransactionNames.add(totalKey);
                    perAgentTransactionNames.add(agentKey);
                    perAgentPerThreadTransactionNames.add(agentThreadKey);
                    
                    AggregatedResultSeries aggregatedResultSeries = results.get(transactionID);
                    List<AggregatedResult> aggregatedResults = aggregatedResultSeries.getResults();
                    for (AggregatedResult result : aggregatedResults) {
                        Log.debug(result.toString());
                        chunker.onNewResult(agentKey, result.time, result.mean() * 1e-6f);
                        chunker.onNewResult(totalKey, result.time, result.mean() * 1e-6f);    
                    }
                }
            }
        }

        // The first chart is the [all agents, all threads, all transactions] mean transaction time chart
        TimeSeriesCollection timeSeriesCollection = extractTimeSeries(chunker, totalsTransactionNames, Statistic.Mean);
        render("All transactions mean per second elapsed times", timeSeriesCollection, new File(reportsDir, "all.transactions.elapsed.png"));

        // The second chart is the [all agents, all threads, all transactions] transaction count chart
        timeSeriesCollection = extractTimeSeries(chunker, totalsTransactionNames, Statistic.Count);
        render("All transactions per second counts", timeSeriesCollection, new File(reportsDir, "all.transactions.count.png"));
        
        // Now lets do the same for each agent
        for (final String agent : agentStrings)
        {
            TimeSeriesCollection agentTimeSeriesCollection = new TimeSeriesCollection();
            for (String perAgentPerThreadTransactionName : perAgentTransactionNames)
            {
                if(perAgentPerThreadTransactionName.startsWith(agent))
                {
                    List<Chunk> timeOrderedResults = chunker.getTimeOrderedResults(perAgentPerThreadTransactionName);
                    TimeSeries extractTimeSeries = extractTimeSeries(timeOrderedResults, perAgentPerThreadTransactionName, Statistic.Mean);
                    agentTimeSeriesCollection.addSeries(extractTimeSeries);
                }                
            }

            render(agent + " transactions mean per second elapsed times", agentTimeSeriesCollection, new File(reportsDir, agent + ".transactions.elapsed.png"));
        }

        // Lets have a stab at writing out some csv stuff
        writePerSecondResults(reportsDir, chunker, totalsTransactionNames);        
    }

    private void writePerSecondResults(File reportsDir, Chunker chunker, Set<String> totalsTransactionNames)
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(reportsDir, "persecond.results.csv")));
            
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
            
            writer.write("transaction,\t");
            writer.write("time,\t");
            writer.write("count,\t");
            writer.write("total,\t");
            writer.write("mean,\t");
            writer.write("median,\t");
            writer.write("mode,\t");
            writer.write("stddev,\t");
            writer.write("min,\t");
            writer.write("max,\t");
            writer.write("10th,\t");
            writer.write("20th,\t");
            writer.write("30th,\t");
            writer.write("40th,\t");
            writer.write("50th,\t");
            writer.write("60th,\t");
            writer.write("70th,\t");
            writer.write("80th,\t");
            writer.write("90th,\t");
            writer.write("95th,\t");
            writer.write("99th,\t");
            
            for (String transaction : totalsTransactionNames)
            {
                List<Chunk> timeOrderedResults = chunker.getTimeOrderedResults(transaction);
                for (Chunk chunk : timeOrderedResults)
                {
                    long chunkStart = chunk.getChunkStart();
                    Date chunkStartDate = new Date(chunkStart);
                    String timeFormatted = format.format(chunkStartDate);
                    
                    writer.write(transaction);
                    writer.write(",\t");
                    writer.write(timeFormatted);
                    writer.write(",\t");
                    Statistics statistics = chunk.getStatistics();
                    writer.write(Double.toString(statistics.getCount()));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculateSum()));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculateMean()));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculateMedian()));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculateMode()));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculateStandardDeviationFast()));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculateMinimum()));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculateMaximum()));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculatePercentile(10)));
                    writer.write(",\t");                    
                    writer.write(Double.toString(statistics.calculatePercentile(20)));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculatePercentile(30)));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculatePercentile(40)));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculatePercentile(50)));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculatePercentile(60)));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculatePercentile(70)));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculatePercentile(80)));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculatePercentile(90)));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculatePercentile(95)));
                    writer.write(",\t");
                    writer.write(Double.toString(statistics.calculatePercentile(99)));
                    writer.newLine();
                }
            }
            
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private TimeSeriesCollection extractTimeSeries(Chunker chunker, Set<String> transactionNames, Statistic statistic)
    {
        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
        for (String transaction : transactionNames)
        {
            List<Chunk> timeOrderedResults = chunker.getTimeOrderedResults(transaction);
            TimeSeries timeSeries = extractTimeSeries(timeOrderedResults, transaction, statistic);

            timeSeriesCollection.addSeries(timeSeries);
        }
        return timeSeriesCollection;
    }

    private TimeSeriesCollection extractTimeSeriesCollection(List<Chunk> timeOrderedResults, String transactionName, Statistic statistic)
    {
        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
        TimeSeries timeSeries = extractTimeSeries(timeOrderedResults, transactionName, statistic);

        timeSeriesCollection.addSeries(timeSeries);
        return timeSeriesCollection;
    }

    private TimeSeries extractTimeSeries(List<Chunk> timeOrderedResults, String transactionName, Statistic statistic)
    {
        TimeSeries timeSeries = new TimeSeries(transactionName);
        for (Chunk chunk : timeOrderedResults)
        {
            timeSeries.add(new Second(new Date(chunk.getChunkStart())), chunk.getStatistics().extract(statistic));
        }
        return timeSeries;
    }

    private void render(String title, TimeSeriesCollection timeSeriesCollection, File file)
    {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(title,// title
                                                              "Time",// x-axislabel
                                                              "Elapsed / ms",// y-axislabel
                                                              timeSeriesCollection,// data
                                                              true,// createlegend?
                                                              true,// generatetooltips?
                                                              false// generateURLs?
        );

        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            Log.info("Rendered " + title + " to " + file.getAbsolutePath());
            ChartUtilities.writeChartAsPNG(fos, chart, 640, 480);
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}

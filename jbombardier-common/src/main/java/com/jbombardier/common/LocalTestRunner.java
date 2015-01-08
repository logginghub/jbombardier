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

package com.jbombardier.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.logginghub.utils.SinglePassStatisticsDoublePrecision;

public class LocalTestRunner {

    private static final String comma = ",";
    private DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");

    public enum ResultStrategyName {
        raw,
        aggregated;
    }

    public enum OutputStyle {
        dump,
        csv;
    }

    public static void main(String[] args) throws Exception {

        LocalTestRunner runner = new LocalTestRunner();
        runner.run(args);
    }

    public void run(String[] args) throws Exception {
        int iterations = -1;
        long durationMS = -1;
        String classname = null;
        ResultStrategyName resultStrategyName = ResultStrategyName.aggregated;
        long aggregationDuration = 1000;

        int index = 0;
        while (index < args.length) {
            String option = args[index++];
            if (option.equalsIgnoreCase("-i")) {
                iterations = Integer.parseInt(args[index++]);
            }
            else if (option.equalsIgnoreCase("-t")) {
                durationMS = Long.parseLong(args[index++]);
            }
            else if (option.equals("-raw")) {
                resultStrategyName = ResultStrategyName.raw;
            }
            else if (option.equals("-aggreated")) {
                resultStrategyName = ResultStrategyName.raw;
                aggregationDuration = Long.parseLong(args[index++]);
            }
            else {
                classname = option;
            }
        }

        PropertiesStrategy propertiesProvider = new DefaultPropertiesProvider();

        runTests(iterations, durationMS, classname, resultStrategyName, aggregationDuration, propertiesProvider);
    }

    private void runTests(int iterations,
                          long durationMS,
                          String classname,
                          ResultStrategyName resultStrategyName,
                          long aggregationDuration,
                          PropertiesStrategy propertiesProvider) throws ClassNotFoundException, InstantiationException,
                    IllegalAccessException, Exception {

        PerformanceTest test = createTestInstance(classname);
        ResultStrategy resultStrategy = constructResultStrategy(resultStrategyName, aggregationDuration);
        LoggingStrategy loggingStrategy = new MinlogLoggingStrategy();
        TestContext context = new SimpleTestContext(classname, resultStrategy, propertiesProvider, loggingStrategy);

        execute(iterations, durationMS, test, context);
        processResults(resultStrategy, OutputStyle.dump);
    }

    private PerformanceTest createTestInstance(String classname) throws ClassNotFoundException, InstantiationException,
                    IllegalAccessException {
        Class<?> loadedClass = Class.forName(classname);
        Object newInstance = loadedClass.newInstance();
        final PerformanceTest test = (PerformanceTest) newInstance;
        return test;
    }

    public ResultStrategy constructResultStrategy(ResultStrategyName resultStrategyName, long aggregationDuration) {
        ResultStrategy resultStrategy = null;
        if (resultStrategyName == ResultStrategyName.aggregated) {
            resultStrategy = new BasicTestStatsResultStrategy("Name", false);
        }
        else if (resultStrategyName == ResultStrategyName.raw) {
            resultStrategy = new RawResultStrategy();
        }
        return resultStrategy;
    }

    /**
     * Execute the test based on the iterations and duration values; one of them
     * must be set or this will do nothing.
     * 
     * @param iterations
     * @param durationMS
     * @param test
     * @param context
     * @throws Exception
     */
    public void execute(int iterations, long durationMS, final PerformanceTest test, TestContext context) throws Exception {
        if (iterations > 0) {
            run(test, iterations, context);
        }
        else if (durationMS > 0) {
            run(test, durationMS, context);
        }
    }

    public static void dumpCSVHeader() {
        System.out.println("transactionID,count,mean,median,max,min,meanops/sec,stddev,10,20,30,40,50,60,70,80,90");
    }

    public void processResults(ResultStrategy resultStrategy, OutputStyle outputStyle) {

        if (resultStrategy instanceof RawResultStrategy) {

            RawResultStrategy rawResultStrategy = (RawResultStrategy) resultStrategy;
            Set<String> transactionSet = rawResultStrategy.getTransactionSet();

            for (String transactionID : transactionSet) {
                RawDataSeries dataSeries = rawResultStrategy.getDataSeries(transactionID);

                SinglePassStatisticsDoublePrecision stats = new SinglePassStatisticsDoublePrecision();
                List<DataPoint> data = dataSeries.getData();
                for (DataPoint dataPoint : data) {
                    stats.addValue(dataPoint.getElapsedNanos() / 1e6);
                }

                stats.doCalculations();

                if (outputStyle == OutputStyle.dump) {
                    System.out.println(transactionID);
                    stats.dump();

                    for (int i = 0; i <= 10; i++) {
                        double percentile = stats.getPercentiles()[i];
                        System.out.println(String.format("\tPercentile %3d %5.4f", i * 10, percentile));
                    }
                }
                else if (outputStyle == OutputStyle.csv) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(transactionID).append(comma);
                    builder.append(stats.getCount()).append(comma);
                    builder.append(stats.getMean()).append(comma);
                    builder.append(stats.getMedian()).append(comma);
                    builder.append(stats.getMinimum()).append(comma);
                    builder.append(stats.getMaximum()).append(comma);
                    builder.append(stats.getMeanOps()).append(comma);
                    builder.append(stats.getStandardDeviationPopulationDistrubution()).append(comma);

                    double[] percentile = stats.getPercentiles();
                    for (int i = 1; i < 10; i++) {
                        builder.append(percentile[i]).append(comma);
                    }

                    System.out.println(builder.toString());
                }
            }
        }
    }

    private static void run(PerformanceTest test, long durationMS, TestContext context) throws Exception {

        test.setup(context);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < durationMS) {
            test.beforeIteration(context);
            long start =System.nanoTime();
            try {
                test.runIteration(context);
                long elapsed =System.nanoTime() - start;
                context.createTransaction(test.getClass().getSimpleName(), elapsed, true);
            }
            catch (Exception e) {
                // TODO: send to an exception handler
                e.printStackTrace();
                long elapsed =System.nanoTime() - start;
                context.createTransaction(test.getClass().getSimpleName(), elapsed, false);
            }
        }
        test.teardown(context);
    }

    private static void usage() {
        // TODO Auto-generated method stub

    }

    public static void run(PerformanceTest test, int iterations, TestContext context) throws Exception {
        test.setup(context);
        for (int i = 0; i < iterations; i++) {
            test.beforeIteration(context);
            long start =System.nanoTime();
            try {                
                test.runIteration(context);
                long elapsed =System.nanoTime() - start;
                context.createTransaction(test.getClass().getSimpleName(), elapsed, true);
            }
            catch (Exception e) {
                // TODO: send to an exception handler
                e.printStackTrace();
                long elapsed =System.nanoTime() - start;
                context.createTransaction(test.getClass().getSimpleName(), elapsed, false);
            }
        }
        test.teardown(context);
    }

    private String format(long time) {
        return dateFormat.format(new Date((long) (time)));
    }

    public static void run(PerformanceTest test, long elapsed, PropertiesStrategy propertiesProvider) {
        LocalTestRunner runner = new LocalTestRunner();
        try {
            runner.runTests(-1, elapsed, test.getClass().getName(), ResultStrategyName.raw, 1000, propertiesProvider);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

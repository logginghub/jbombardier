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

package com.jbombardier.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestRunner {

    private final PerformanceTest test;
    private int iterations = -1;
    private int amount = 10;
    private TimeUnit units = TimeUnit.SECONDS;
    private LocalTestRunner.OutputStyle outputStyle = LocalTestRunner.OutputStyle.dump;
    private int repeats = 1;
    private DefaultPropertiesProvider propertiesProvider = new DefaultPropertiesProvider();

    public TestRunner(PerformanceTest test) {
        this.test = test;
    }

    public static TestRunner benchmark(PerformanceTest test) {
        return new TestRunner(test);
    }

    public TestRunner iterations(int iterations) {
        setIterations(iterations);
        return this;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getIterations() {
        return iterations;
    }

    public TestRunner time(int amount, TimeUnit units) {
        setTime(amount, units);
        return this;
    }

    private void setTime(int amount, TimeUnit units) {
        this.amount = amount;
        this.units = units;
    }

    public void run() {

        if (outputStyle == LocalTestRunner.OutputStyle.csv) {
            LocalTestRunner.dumpCSVHeader();
        }

        for (int i = 0; i < repeats; i++) {
            LocalTestRunner localTestRunner = new LocalTestRunner();

            LoggingStrategy loggingStrategy = new MinlogLoggingStrategy();
            ResultStrategy resultStrategy = new RawResultStrategy();

            TestContext context = new SimpleTestContext("test", resultStrategy, propertiesProvider, loggingStrategy);

            try {
                localTestRunner.execute(iterations, getDurationMillis(), test, context);
                localTestRunner.processResults(resultStrategy, outputStyle);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private long getDurationMillis() {
        if (units == null) {
            return -1;
        }
        else {
            return units.toMillis(amount);
        }
    }

    public TestRunner format(LocalTestRunner.OutputStyle outputStyle) {
        setOutputStyle(outputStyle);
        return this;
    }

    private void setOutputStyle(LocalTestRunner.OutputStyle outputStyle) {
        this.outputStyle = outputStyle;
    }

    public TestRunner repeats(int i) {
        setRepeats(i);
        return this;
    }

    private void setRepeats(int repeats) {
        this.repeats = repeats;
    }

    public TestRunner addPropertyEntries(String string, String[] header, String[]... values) {
        
        List<PropertyEntry> propertyEntries = new ArrayList<PropertyEntry>();
        for (String[] valuesForEntry : values) {
            PropertyEntry entry = new PropertyEntry();
            entry.setHeaders(header);
            entry.setValues(valuesForEntry);
            propertyEntries.add(entry);
        }

        propertiesProvider.setPropertyEntrys(string, propertyEntries);        
        return this;
    }
}

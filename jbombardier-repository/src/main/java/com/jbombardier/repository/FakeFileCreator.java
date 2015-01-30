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

package com.jbombardier.repository;

import com.jbombardier.console.model.result.RunResult;
import com.jbombardier.console.model.result.TestRunResultBuilder;
import com.jbombardier.repository.model.RepositoryTestModel;
import com.logginghub.utils.FactoryMapDecorator;
import com.logginghub.utils.RandomWithAcceleration;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

/**
 * Creates a load of test files to test the repo whilst we dont have any actual real data
 */
public class FakeFileCreator {

    public static final String EXAMPLE_TEST1 = "ExampleTest";

    public static final String EXAMPLE_TEST2 = "ExampleTestWithLotsOfTests";

    private static final Logger logger = Logger.getLoggerFor(FakeFileCreator.class);

    public void createFiles(RepositoryController controller) {

        RepositoryTestModel repositoryTestModelForTest = controller.getModel()
                                                                   .getRepositoryTestModelForTest(EXAMPLE_TEST1);
        if (!repositoryTestModelForTest.hasResults()) {
            logger.info("Building fake results for {}", EXAMPLE_TEST1);
            buildExampleTestResults(controller);
        }

        logger.info("Building fake results for {}", EXAMPLE_TEST2);
        buildExampleWithLotsOfTestsResults(controller);
    }

    private void buildExampleWithLotsOfTestsResults(RepositoryController controller) {

        Random random = new Random(0);

        Calendar calendar = TimeUtils.getUTCCalendarForTime("1/1/2013 00:00:00");

        FactoryMapDecorator<Integer, RandomWithAcceleration> randomTransactionsPerSecondsByTest = new FactoryMapDecorator<Integer, RandomWithAcceleration>(
                new HashMap<Integer, RandomWithAcceleration>()) {
            @Override protected RandomWithAcceleration createNewValue(Integer key) {
                return buildRandomWithAcceleration(key, 90, 110, 3, 6);
            }
        };

        FactoryMapDecorator<Integer, RandomWithAcceleration> randomTotalTransactionsByTest = new FactoryMapDecorator<Integer, RandomWithAcceleration>(
                new HashMap<Integer, RandomWithAcceleration>()) {
            @Override protected RandomWithAcceleration createNewValue(Integer key) {
                return buildRandomWithAcceleration(key, 5, 15, 3, 6);
            }
        };

        FactoryMapDecorator<Integer, RandomWithAcceleration> randomTransactionDurationsByTest = new FactoryMapDecorator<Integer, RandomWithAcceleration>(
                new HashMap<Integer, RandomWithAcceleration>()) {
            @Override protected RandomWithAcceleration createNewValue(Integer key) {
                return buildRandomWithAcceleration(key, 25000, 100000, 5, 20);
            }
        };

        while (calendar.getTimeInMillis() < System.currentTimeMillis()) {

            String failureReason = "";
            int failureChance = random.nextInt(100);
            if (failureChance > 90) {
                failureReason = "This run failed as one of the tests failed to start";
            } else if (failureChance > 80) {
                failureReason = "This run failed as the failure tolerance was exceeded";
            }

            TestRunResultBuilder builder = TestRunResultBuilder.start()
                                                               .name(EXAMPLE_TEST2)
                                                               .failureReason(failureReason)
                                                               .startTime(calendar.getTimeInMillis());

            int tests = 50;

            for (int i = 0; i < tests; i++) {
                RandomWithAcceleration randomTransactionCount = randomTransactionsPerSecondsByTest.get(i);
                RandomWithAcceleration randomTransactionDuration = randomTransactionDurationsByTest.get(i);

                double transactionTime = 1e3 * randomTransactionDuration.next();

                int succesful = (int) randomTransactionCount.next();
                int total = (int) (succesful + randomTotalTransactionsByTest.get(i).next());

//                builder.results(TestRunResultBuilder.result()
//                                                    .testName("test_" + i)
//                                                    .testTime(1000)
//                                                    .transactionCount(total)
//                                                    .sla(60)
//                                                    .successDuration(transactionTime)
//                                                    .successTotalDuration(transactionTime + 34532)
//                                                    .transactionsSuccess(succesful));
            }

            RunResult runResult = builder.toTestRunResult();

            controller.postResult(runResult);

            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

    }

    private RandomWithAcceleration buildRandomWithAcceleration(long seed,
                                                               double min,
                                                               double max,
                                                               int trendLengthMin,
                                                               int trendLengthMax) {
        RandomWithAcceleration randomWithAcceleration = new RandomWithAcceleration(new RandomWithAcceleration.RandomGenerator(
                trendLengthMin,
                trendLengthMax), new RandomWithAcceleration.RandomGenerator(min, max));
        return randomWithAcceleration;
    }

    private void buildExampleTestResults(RepositoryController controller) {
        RandomWithAcceleration randomATransactions = buildRandomWithAcceleration(0, 90, 110, 3, 6);
        RandomWithAcceleration randomADuration = buildRandomWithAcceleration(0, 25000, 100000, 5, 20);
        RandomWithAcceleration randomB = buildRandomWithAcceleration(0, 100, 200, 5, 20);
        RandomWithAcceleration randomC = buildRandomWithAcceleration(0, 5, 15, 5, 20);

        Random random = new Random(0);

        Calendar calendar = TimeUtils.getUTCCalendarForTime("1/1/2013 00:00:00");

        while (calendar.getTimeInMillis() < System.currentTimeMillis()) {

            String failureReason = "";
            int failureChance = random.nextInt(100);
            if (failureChance > 90) {
                failureReason = "This run failed as one of the tests failed to start";
            } else if (failureChance > 80) {
                failureReason = "This run failed as the failure tolerance was exceeded";
            }

//            RunResult runResult = TestRunResultBuilder.start()
//                                                              .name(EXAMPLE_TEST1)
//                                                              .failureReason(failureReason)
//                                                              .startTime(calendar.getTimeInMillis())
//                                                              .results(TestRunResultBuilder.result()
//                                                                                           .testName("write_log_event")
//                                                                                           .testTime(1000)
//                                                                                           .transactionCount(100)
//                                                                                           .sla(60)
//                                                                                           .successDuration(1e3 * randomADuration
//                                                                                                   .next())
//                                                                                           .successTotalDuration(1e3 * 60)
//                                                                                           .transactionsSuccess((long) randomATransactions
//                                                                                                   .next()))
//                                                              .results(TestRunResultBuilder.result()
//                                                                                           .testName(
//                                                                                                   "connect_disconnect")
//                                                                                           .testTime(1000)
//                                                                                           .transactionCount(1000)
//                                                                                           .successDuration(1e6 * randomB
//                                                                                                   .next())
//                                                                                           .successTotalDuration(1e6 * 60)
//                                                                                           .transactionsSuccess(100))
//                                                              .results(TestRunResultBuilder.result()
//                                                                                           .testName("read_log_events")
//                                                                                           .testTime(1000)
//                                                                                           .transactionCount(1000)
//                                                                                           .successDuration(1e6 * randomC
//                                                                                                   .next())
//                                                                                           .successTotalDuration(1e6 * 60)
//                                                                                           .transactionsSuccess(100))
//                                                              .toTestRunResult();
//
//            controller.postResult(runResult);

            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }
    }

}

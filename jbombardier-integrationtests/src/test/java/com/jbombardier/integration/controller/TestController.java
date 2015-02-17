package com.jbombardier.integration.controller;

import com.jbombardier.JBombardierTemporalController;
import com.jbombardier.agent.Agent2;
import com.jbombardier.JBombardierController;
import com.jbombardier.JBombardierModel;
import com.jbombardier.console.configuration.JBombardierConfiguration;
import com.jbombardier.console.sample.SleepTest;
import com.jbombardier.integration.JBombardierTestBase;
import com.logginghub.utils.Out;
import com.logginghub.utils.ThreadUtils;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;

import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.TestBuilder;
import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.configurationBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;

/**
 * Created by james on 09/01/15.
 */
public class TestController extends JBombardierTestBase {

    @Test public void test_full_test_headless() {
        Agent2 agent = dsl.createAgent("Agent1");
        agent.start();

        long startTime = System.currentTimeMillis();
        final JBombardierController controller = configurationBuilder().addAgent("Agent1",
                                                                                 "localhost",
                                                                                 agent.getBindPort())
                                                                       .autostart(1)
                                                                       .warmupTime(0)
                                                                       .testDuration("1 second")
                                                                       .addTest(TestBuilder.start(SleepTest.class)
                                                                                           .targetRate(1)
                                                                                           .threads(1))
                                                                       .executeHeadlessNoExit();

        long elapsed = System.currentTimeMillis() - startTime;
        assertThat(controller.getState(), is(JBombardierController.State.Completed));
        long delta = Math.abs(elapsed - 1000);

        // jshaw - this is complicated, maybe we need to verify the actual execution time from the stats, as this is usually 1-2 seconds off!
//        assertThat(delta, is(lessThan(500L)));

    }

    @Test public void test_temporal_controller_start_and_stop() {

        Agent2 agent = dsl.createAgent("Agent1");
        agent.start();

        final JBombardierModel model = new JBombardierModel();
        JBombardierConfiguration configuration =

                configurationBuilder().addAgent("Agent1", "localhost", agent.getBindPort())
                                      .autostart(10)
                                      .testDuration("1 second")
                                      .addTest(TestBuilder.start(SleepTest.class).targetRate(1).threads(1))
                                      .toConfiguration();

        final JBombardierController controller = new JBombardierController(model, configuration);

        assertThat(controller.getState(), is(JBombardierController.State.Configured));

        controller.startAgentConnections();

        assertThat(controller.getState(), is(JBombardierController.State.AgentConnectionsRunning));

        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return model.getConnectionAgentCount() == 1;
            }
        });

        JBombardierTemporalController temporalController = new JBombardierTemporalController(controller);

        temporalController.start();

        assertThat(controller.getState(), is(JBombardierController.State.TestRunning));

        long startTime = System.currentTimeMillis();

        ThreadUtils.untilTrue(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return controller.getState() == JBombardierController.State.Completed;
            }
        });

        long elapsed = System.currentTimeMillis() - startTime;

        Out.out("Elapsed {}", elapsed);

        assertThat(controller.getState(), is(JBombardierController.State.Completed));

        long delta = Math.abs(elapsed - 1000);

        // jshaw - we can't be too precise on this verification - there is a lot going on at the start of the test and we are only running for 1 second after all
        assertThat(delta, is(lessThan(500L)));

        temporalController.stop();
        agent.stop();

    }
}

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

package com.jbombardier.console;

import com.jbombardier.JBombardierController;
import com.jbombardier.JBombardierModel;
import com.jbombardier.JBombardierTemporalController;
import com.jbombardier.console.configuration.JBombardierConfiguration;
import com.jbombardier.console.panels.SwingConsoleMainPanel;
import com.logginghub.utils.MainUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.SystemErrStream;
import com.logginghub.utils.swing.VLFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit;

public class JBombardierSwingConsole {

    private static final Logger logger = Logger.getLoggerFor(JBombardierSwingConsole.class);
    // private String configurationFile =
    // "/com/jbombardier/interactive/configuration/sample_configuration.xml";
    private JBombardierModel model;
    private VLFrame frame;
    private JBombardierController controller;
    private JBombardierConfiguration configuration;
    private JBombardierTemporalController temporalController;

    public void initialise() {

        try {
            configuration.validate();
        } catch (IllegalArgumentException e) {
            if (configuration.showVisualErrorMessages()) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Failed to start test", JOptionPane.ERROR_MESSAGE);
            }
            throw e;
        }

        if (configuration.getTelemetryHubPort() == -1) {
            int findFreePort = NetUtils.findFreePort();
            Out.out("No telemetry hub port was specified in the configuration, so we've tried to find a free one : {}", findFreePort);
            configuration.setTelemetryHubPort(findFreePort);
        }

        model = new JBombardierModel();
        controller = new JBombardierController(model, configuration);
        temporalController = new JBombardierTemporalController(controller);

    }

    private void start() {

        controller.startStats();

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        //        if (ReflectionUtils.classExists("sun.swing.plaf.synth.SynthUI")) {
        //            try {
        //                UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
        //            }
        //            catch (Exception e) {
        //                e.printStackTrace();
        //            }
        //        }

        frame = new VLFrame("JBombardier - Interactive Console", "black-flask-hi.png");

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    controller.stopTelemetry();
                    if (configuration.isSendKillOnConsoleClose()) {
                        controller.endTestAbnormally();
                        controller.killAgents();
                    }
                } catch (Throwable t) {
                }
                super.windowClosing(e);
                System.exit(-1);
            }
        });

        Container contentPane = frame.getContentPane();
        initialiseComponents(contentPane);

        // frame.setSize(0.9f);
        frame.setVisible(true);

        controller.startAgentConnections();

        if (configuration.getAutostartAgents() > 0) {
            WorkerThread.every("ConsoleAutostartChecker", 100, TimeUnit.MILLISECONDS, new Runnable() {
                @Override public void run() {
                    logger.debug("Auto start {} agents {}", configuration.getAutostartAgents(), model.getConnectionAgentCount());
                    if (model.getConnectionAgentCount() >= configuration.getAutostartAgents()) {
                        logger.info("Auto start agent count reached, starting test");
                        temporalController.start();
                        throw new WorkerThread.StopRunningException();
                    }
                }
            });
        }
    }

    private void initialiseComponents(Container contentPane) {
        SwingConsoleMainPanel panel = new SwingConsoleMainPanel();
        panel.bind(controller, temporalController);
        contentPane.add(panel);
    }

    public static JBombardierSwingConsole run(String configurationFilePath) {
        SystemErrStream.gapThreshold = 1500;

        JBombardierSwingConsole swingConsole = new JBombardierSwingConsole();

        swingConsole.loadConfigurationFile(configurationFilePath);

        swingConsole.initialise();
        swingConsole.start();

        return swingConsole;
    }

    public JBombardierController getController() {
        return controller;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            String configurationFile = MainUtils.getStringArgument(args, 0, "");
//            int autostartAgents = MainUtils.getIntArgument(args, 1, -1);
            run(configurationFile);
        } else {
            System.err.println("Please pass a configuration file path (either from the file system, or from the classpath) as the command line parameter.");
        }
    }

    public JBombardierConfiguration getConfiguration() {
        return configuration;
    }

    private void setConfiguration(JBombardierConfiguration configuration) {
        this.configuration = configuration;
    }

    public static JBombardierSwingConsole run(JBombardierConfiguration configuration) {
        JBombardierSwingConsole swingConsole = new JBombardierSwingConsole();
        swingConsole.setConfiguration(configuration);
        swingConsole.initialise();
        swingConsole.start();
        return swingConsole;
    }

    public void loadConfigurationFile(String string) {
        JBombardierConfiguration configuration = JBombardierConfiguration.loadConfiguration(string);
        setConfiguration(configuration);
    }

}

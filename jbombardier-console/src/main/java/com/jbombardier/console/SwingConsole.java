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

package com.jbombardier.console;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.jbombardier.console.configuration.InteractiveConfiguration;
import com.jbombardier.console.panels.SwingConsoleMainPanel;
import com.logginghub.utils.MainUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.logging.SystemErrStream;
import com.logginghub.utils.swing.VLFrame;

public class SwingConsole {

    // private String configurationFile =
    // "/com/jbombardier/interactive/configuration/sample_configuration.xml";
    private ConsoleModel model;
    private VLFrame frame;
    private SwingConsoleController controller;
    private InteractiveConfiguration configuration;

    public void initialise() {

        try {
            configuration.validate();
        }
        catch (IllegalArgumentException e) {
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

        model = new ConsoleModel();
        controller = new SwingConsoleController();
        controller.initialise(configuration, model);
    }

    private void start() {

        controller.startStats();

        if (ReflectionUtils.classExists("sun.swing.plaf.synth.SynthUI")) {
            try {
                UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        frame = new VLFrame("jbombardier - Interactive Console", "black-flask-hi.png");

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                controller.stopTelemetry();
                if (controller.isSendCloseMessageOnWindowClose()) {
                    controller.stopTest(true);
                    controller.killAgents();
                }
            };
        });

        Container contentPane = frame.getContentPane();
        initialiseComponents(contentPane);

        // frame.setSize(0.9f);
        frame.setVisible(true);
    }

    private void initialiseComponents(Container contentPane) {
        SwingConsoleMainPanel panel = new SwingConsoleMainPanel();
        panel.setController(controller);
        panel.setModel(model);
        contentPane.add(panel);
    }

    public static void runWithAutostart(String configPath, int autostartAgents) {
        SwingConsole.main(new String[]{configPath, "" + autostartAgents});
    }

    public static void run(String configPath) {
        SwingConsole.main(new String[]{configPath});
    }

    public SwingConsoleController getController() {
        return controller;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            String configurationFile = MainUtils.getStringArgument(args, 0, "");
            int autostartAgents = MainUtils.getIntArgument(args, 1, -1);
            run(configurationFile, autostartAgents);
        }
        else {
            System.err.println("Please pass a configuration file path (either from the file system, or from the classpath) as the command line parameter.");
        }
    }

    public static SwingConsole run(String configurationFilePath, int autostartAgents) {
        SystemErrStream.gapThreshold = 1500;

        SwingConsole swingConsole = new SwingConsole();

        swingConsole.loadConfigurationFile(configurationFilePath);
        swingConsole.getConfiguration().setAutostartAgents(autostartAgents);

        swingConsole.initialise();
        swingConsole.start();

        return swingConsole;
    }

    public InteractiveConfiguration getConfiguration() {
        return configuration;
    }

    private void setConfiguration(InteractiveConfiguration configuration) {
        this.configuration = configuration;
    }

    public static SwingConsole run(InteractiveConfiguration configuration) {
        SwingConsole swingConsole = new SwingConsole();
        swingConsole.setConfiguration(configuration);
        swingConsole.initialise();
        swingConsole.start();
        return swingConsole;
    }

    public void loadConfigurationFile(String string) {
        InteractiveConfiguration configuration = InteractiveConfiguration.loadConfiguration(string);
        setConfiguration(configuration);
    }

}

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

package com.jbombardier.console.embedded;

import java.awt.Container;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

import com.jbombardier.console.SwingConsole;
import com.jbombardier.console.ConsoleModel;
import com.jbombardier.console.model.TestModel;
import com.jbombardier.console.panels.SwingConsoleMainPanel;
import com.logginghub.utils.Pair;
import com.logginghub.utils.swing.MainFrame;
import com.jbombardier.common.TestFactory;
import com.jbombardier.console.SwingConsoleController;

public class EmbeddedPerformanceTest {

    private List<Pair<String, TestFactory>> testFactories = new ArrayList<Pair<String, TestFactory>>();
    
    private ConsoleModel model;
    private MainFrame frame;
    private SwingConsoleController controller;
//    private InteractiveConfiguration configuration;
    

    public void addTestFactory(String name, TestFactory testFactory) {
        testFactories.add(new Pair<String, TestFactory>(name, testFactory));
    }

//    public void start() {
//        InteractiveConfiguration configuration = new InteractiveConfiguration();
//        jbombardierConsole console = new jbombardierConsole();
//        console.run(configuration);
//    }

    // private String configurationFile =
    // "/com/jbombardier/interactive/configuration/sample_configuration.xml";
    
    public void start() {

        model = new ConsoleModel();
        controller = new SwingConsoleController();
        
        for (Pair<String, TestFactory> pair : testFactories) {
            TestModel testModel = new TestModel();
            testModel.setClassname(pair.getA());
            testModel.setName(pair.getA());
            testModel.setTargetThreads(1);
            testModel.setRateStep(1);
            testModel.setRateStepTime(500);
            testModel.setTargetRate(1);
            testModel.setThreadStep(1);
            model.addTestModel(testModel);
        }
        
        try {
            UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        frame = new MainFrame("jbombardier - Interactive Console") {
            public void windowClosing(WindowEvent e) {
                controller.stopTelemetry();

                if (controller.isSendCloseMessageOnWindowClose()) {
                    controller.stopTest(true);
                }
            };
        };

        frame.setIcon("black-flask-hi.png");
        Container contentPane = frame.getContentPane();
        initialiseComponents(contentPane);

        frame.setSize(0.9f);
        frame.setVisible(true);

        model.setAgentsInTest(1);
        
        EmbeddedRunner runner = new EmbeddedRunner();
        runner.run(testFactories.get(0).getA(), testFactories.get(0).getB(), model);
        
    }

    private void initialiseComponents(Container contentPane) {
        SwingConsoleMainPanel panel = new SwingConsoleMainPanel();
        panel.setDisplayAgentControl(false);
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

//    public static void main(String[] args) {
//        if (args.length > 0) {
//            String configurationFile = MainUtils.getStringArgument(args, 0, "");
//            int autostartAgents = MainUtils.getIntArgument(args, 1, -1);
//            run(configurationFile, autostartAgents);
//        }
//        else {
//            System.err.println("Please pass a configuration file path (either from the file system, or from the classpath) as the command line parameter.");
//        }
//    }

//    public static jbombardierConsole run(String configurationFilePath, int autostartAgents) {
//        jbombardierConsole console = new jbombardierConsole();
//
//        console.loadConfigurationFile(configurationFilePath);
//        console.getConfiguration().setAutostartAgents(autostartAgents);
//
//        console.initialise();
//        console.start();
//
//        return console;
//    }

//    public InteractiveConfiguration getConfiguration() {
//        return configuration;
//    }
//
//    private void setConfiguration(InteractiveConfiguration configuration) {
//        this.configuration = configuration;
//    }

//    public static jbombardierConsole run(InteractiveConfiguration configuration) {
//        jbombardierConsole console = new jbombardierConsole();
//        console.setConfiguration(configuration);
//        console.initialise();
//        console.start();
//        return console;
//    }

//    public void loadConfigurationFile(String string) {
//        InteractiveConfiguration configuration = InteractiveConfiguration.loadConfiguration(string);
//        setConfiguration(configuration);
//    }

    
}
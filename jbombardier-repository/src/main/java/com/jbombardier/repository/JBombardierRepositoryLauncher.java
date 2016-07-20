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

package com.jbombardier.repository;

import com.jbombardier.repository.model.RepositoryModel;
import com.logginghub.logging.utils.LoggingUtils;
import com.logginghub.messaging.Level3AsyncServer;
import com.jbombardier.common.RepositoryInterface;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.MainUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import org.rapidoid.setup.AppRestartListener;
import org.rapidoid.setup.OnChanges;

import java.io.File;

public class JBombardierRepositoryLauncher {

    private static final Logger logger = Logger.getLoggerFor(JBombardierRepositoryLauncher.class);


    private RepositoryConfiguration configuration;

    private RepositoryController controller;

    private RepositoryWebView repositoryWebView;

    private Level3AsyncServer server;

    private WorkerThread modelLoader;

    public JBombardierRepositoryLauncher(RepositoryConfiguration configuration,
                                  RepositoryController controller,
                                  RepositoryWebView repositoryWebView,
                                  Level3AsyncServer server,
                                  WorkerThread modelLoader) {
        this.configuration = configuration;
        this.controller = controller;
        this.repositoryWebView = repositoryWebView;
        this.server = server;
        this.modelLoader = modelLoader;
    }

    public RepositoryConfiguration getConfiguration() {
        return configuration;
    }

    public RepositoryController getController() {
        return controller;
    }

    public RepositoryWebView getRepositoryWebView() {
        return repositoryWebView;
    }

    public Level3AsyncServer getServer() {
        return server;
    }

    public static JBombardierRepositoryLauncher runLauncher(RepositoryConfiguration configuration) {

        final RepositoryModel model = RepositoryModel.fromConfiguration(configuration);

        final File resultsFolder = new File(configuration.getDataFolder());
        logger.info("Loading data from '{}'", resultsFolder.getAbsolutePath());

        boolean buildFakeTests = Boolean.getBoolean("buildFakeTests");

        if (buildFakeTests) {
            File exampleData1 = new File(resultsFolder, FakeFileCreator.EXAMPLE_TEST1);
            File exampleData2 = new File(resultsFolder, FakeFileCreator.EXAMPLE_TEST2);

            logger.info("Deleting fake files from '{}'", exampleData1.getAbsolutePath());
            FileUtils.deleteContents(exampleData1);

            logger.info("Deleting fake files from '{}'", exampleData2.getAbsolutePath());
            FileUtils.deleteContents(exampleData2);
        }

        WorkerThread modelLoader = WorkerThread.execute("Initial model loader", new Runnable() {
            @Override public void run() {
                ModelBuilder modelBuilder = new ModelBuilder();
                modelBuilder.buildFrom(resultsFolder, model);
            }
        });

        final RepositoryController controller = new RepositoryController(model);

        if (buildFakeTests) {
            WorkerThread.execute("Fake file creator", new Runnable() {
                @Override public void run() {
                    FakeFileCreator fakeFileCreator = new FakeFileCreator();
                    fakeFileCreator.createFiles(controller);
                }
            });
        }

        Level3AsyncServer server = new Level3AsyncServer("RepositoryMessaging3Server");
        server.setPort(configuration.getServerPort());
        server.register("repository", RepositoryInterface.class, new RepositoryInterface() {
            @Override public void postResult(String jsonResult) {
                logger.info("Result received : {}", jsonResult);
                controller.postResult(jsonResult);
            }
        });
        //        server.bind();
        logger.info("Messaging3 server bound to {}", server.getPort());

        // Start the web interface (blocks)
        RepositoryWebView repositoryWebView = new RepositoryWebView();
        //        repositoryWebView.bind(controller);
        //        repositoryWebView.start(configuration.getHttpPort());

        JBombardierRepositoryLauncher launcher = new JBombardierRepositoryLauncher(configuration,
                                                                     controller,
                                                                     repositoryWebView,
                                                                     server,
                                                                     modelLoader);
        return launcher;
    }

    public void start() {
        server.bind();
//        repositoryWebView.setHttpPort(configuration.getHttpPort());
//        repositoryWebView.bind(controller);
//        repositoryWebView.start();

        RapidoidView rapidoidView = new RapidoidView();
        rapidoidView.configure(controller);

        OnChanges.addRestartListener(new AppRestartListener() {
            @Override
            public void beforeAppRestart() {
               server.stop();
            }

            @Override
            public void afterAppRestart() {

            }
        });


    }

    public void stop() {
        repositoryWebView.stop();
        server.stop();
    }

    public WorkerThread getModelLoader() {
        return modelLoader;
    }

    public static void main(String[] args) {
        LoggingUtils.setupRemoteVLLoggingFromSystemProperties();
        RepositoryConfiguration configuration = new RepositoryConfiguration();
        if(args == null) {
            args = new String[] {};
        }
        configuration.setDataFolder(MainUtils.getStringArgument(args, 0, "build/data/"));
        runLauncher(configuration).start();
    }
}

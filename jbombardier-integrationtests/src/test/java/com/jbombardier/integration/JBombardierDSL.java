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

package com.jbombardier.integration;

import com.jbombardier.agent.Agent2;
import com.jbombardier.repository.JBombardierRepositoryLauncher;
import com.jbombardier.repository.RepositoryConfiguration;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Out;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 12/11/14.
 */
public class JBombardierDSL {

    private List<Agent2> agents = new ArrayList<Agent2>();

    private List<JBombardierRepositoryLauncher> repositoryLaunchers = new ArrayList<JBombardierRepositoryLauncher>();
    private File resultsFolder;

    public JBombardierDSL() {
        resultsFolder = FileUtils.createRandomTestFolderForClass(this.getClass());
    }

    public File getResultsFolder() {
        return resultsFolder;
    }

    public void start() {
    }

    public void stop() {
        for (Agent2 agent : agents) {
            agent.stop();
        }

        for (JBombardierRepositoryLauncher repositoryLauncher : repositoryLaunchers) {
            if (repositoryLauncher == null) {
                Out.out("!!!!!!!!!!!!!!!!!  WHY IS THE REPO LAUNCHER NULL !!!!!!!!!!!!!");
            } else {
                repositoryLauncher.stop();
            }
        }
    }

    public Agent2 createAgent(String name) {
        Agent2 agent = new Agent2();

        agent.disableSystemExitOnKill();

        agent.setBindPort(NetUtils.findFreePort());
        agent.setName(name);

        agents.add(agent);

        return agent;
    }

    public JBombardierRepositoryLauncher createRepository() {

        RepositoryConfiguration configuration = new RepositoryConfiguration();
        configuration.setHttpPort(NetUtils.findFreePort());
        configuration.setServerPort(NetUtils.findFreePort());

        File testFolder = FileUtils.createRandomTestFileForClass(JBombardierDSL.class);
        configuration.setDataFolder(testFolder.getAbsolutePath());

        JBombardierRepositoryLauncher jbombardierRepositoryLauncher = JBombardierRepositoryLauncher.runLauncher(configuration);
        repositoryLaunchers.add(jbombardierRepositoryLauncher);

        return jbombardierRepositoryLauncher;

    }
}

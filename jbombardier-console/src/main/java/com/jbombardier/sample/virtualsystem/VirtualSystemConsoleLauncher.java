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

package com.jbombardier.sample.virtualsystem;

import com.jbombardier.console.JBombardierSwingConsole;
import com.jbombardier.console.headless.JBombardierHeadless;
import com.jbombardier.sample.io.DiskWritePerformance;
import com.logginghub.utils.VLPorts;

import static com.jbombardier.console.configuration.JBombardierConfigurationBuilder.*;

/**
 * Created by james on 30/01/15.
 */
public class VirtualSystemConsoleLauncher {
    public static void main(String[] args) {
        JBombardierSwingConsole.run("src/main/resources/samples/virtualsystem/configuration.xml");
    }
}

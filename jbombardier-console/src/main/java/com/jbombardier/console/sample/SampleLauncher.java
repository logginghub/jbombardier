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

package com.jbombardier.console.sample;

import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;


public class SampleLauncher {
    
    public static void main(String[] args) {
        
        JBombardierConfigurationBuilder.configurationBuilder().addTest(JBombardierConfigurationBuilder.test(SleepTest.class).name("delay-20").properties("delay=20").threads(10))
                 .addTest(JBombardierConfigurationBuilder.test(SleepTest.class).name("delay-10").properties("delay=10"))
                 .addAgent(JBombardierConfigurationBuilder.embeddedAgent())
                 .addAgent(JBombardierConfigurationBuilder.agent().address("localhost").port(444).name("agent1"))
                 .autostart(1)
                 .execute();
        
    }
    
}


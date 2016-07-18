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

package com.jbombardier.integration.headless;

import com.jbombardier.console.configuration.JBombardierConfigurationBuilder;
import com.jbombardier.integration.JBombardierTestBase;
import org.testng.annotations.Test;

/**
 * Created by james on 17/11/14.
 */
public class TestStatisticProvider extends JBombardierTestBase {

    @Test(enabled = false) public void test_xml() {
        JBombardierConfigurationBuilder.configurationBuilder()
                                .fromXml("statistic_provider.xml")
                                .reportsFolder(dsl.getResultsFolder().getAbsolutePath())
                                .executeHeadlessNoExit();
    }
}

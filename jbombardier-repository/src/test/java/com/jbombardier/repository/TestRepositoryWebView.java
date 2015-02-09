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
import com.jbombardier.repository.model.RepositoryModel;
import com.jbombardier.repository.model.RepositoryTestModel;
import com.logginghub.utils.Xml;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Ignore
public class TestRepositoryWebView {

    private RepositoryWebView repositoryWebView = new RepositoryWebView();
    private RepositoryModel model = new RepositoryModel();
    private RepositoryController controller = new RepositoryController(model);

    @Before public void setup() {
        repositoryWebView.bind(controller);
    }

    @Test public void testIndex() throws Exception {

        RepositoryTestModel rtm = new RepositoryTestModel();
        
        RunResult run1 = new RunResult();
        run1.setConfigurationName("Test1");
        run1.setStartTime(10);

        RunResult run2 = new RunResult();
        run2.setConfigurationName("Test1");
        run2.setStartTime(20);

        rtm.getName().set("Test1");
        rtm.add(run1);
        rtm.add(run2);
        
        model.getTestModels().add(rtm);
        
        String index = repositoryWebView.index();
        
        Xml xml = new Xml(index);
        
        assertThat(xml.nodePath("html.body.div.table.tbody").find("tr").size(), is(1));
        assertThat(xml.nodePath("html.body.div.table.tbody.tr").find("td").size(), is(2));
        assertThat(xml.nodePath("html.body.div.table.tbody.tr.td[0].a").getElementData(), is("Test1"));
        assertThat(xml.nodePath("html.body.div.table.tbody.tr.td[1]").getElementData(), is("2"));
    }

}

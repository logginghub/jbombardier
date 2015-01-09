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

package com.jbombardier.common;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PropertyEntryTest {

    @Test public void test() {
        PropertyEntry entry = new PropertyEntry();
        entry.setHeaders(new String [] { "a", "b", "c"});
        entry.setValues(new String[]{"1", "2", "foo"});

        assertThat(entry.getInteger("a"), is(1));
        assertThat(entry.getInteger("b"), is(2));

        assertThat(entry.getString("a"), is("1"));
        assertThat(entry.getString("b"), is("2"));

        assertThat(entry.getString("c"), is("foo"));
    }

}

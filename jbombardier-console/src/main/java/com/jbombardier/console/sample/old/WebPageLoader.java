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

package com.jbombardier.console.sample.old;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jbombardier.common.PerformanceTestAdaptor;
import com.jbombardier.common.TestContext;

/**
 * Loads a web page in a really simple way.
 * 
 * @author James
 */
public class WebPageLoader extends PerformanceTestAdaptor {

    private URL url;

    public void beforeIteration(TestContext pti) throws Exception {        
        url = new URL(pti.getProperty("url"));
    }
    
    public void runIteration(TestContext pti) throws Exception {
        URLConnection con = url.openConnection();
        Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
        Matcher m = p.matcher(con.getContentType());
        /* If Content-Type doesn't match this pre-conception, choose default and 
         * hope for the best. */
        String charset = m.matches() ? m.group(1) : "ISO-8859-1";
        Reader r = new InputStreamReader(con.getInputStream(), charset);
        StringBuilder buf = new StringBuilder();
        while (true) {
          int ch = r.read();
          if (ch < 0)
            break;
          buf.append((char) ch);
        }
        String str = buf.toString();
    }
}

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

package com.jbombardier.console.panels.telemetry;

import javax.swing.tree.DefaultMutableTreeNode;

public class IOCategoryNode extends DefaultMutableTreeNode {


    public IOCategoryNode(){
        add(new DefaultMutableTreeNode("Disk"));
        add(new DefaultMutableTreeNode("Network"));
    }
    
    @Override public String toString() {
        return "IO";
    }

}

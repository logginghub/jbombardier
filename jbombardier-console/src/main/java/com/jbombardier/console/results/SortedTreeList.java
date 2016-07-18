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

package com.jbombardier.console.results;

import java.util.Collections;

import org.apache.commons.collections.list.TreeList;

public class SortedTreeList<T extends Comparable> extends TreeList {

    private static final long serialVersionUID = 1L;

    public SortedTreeList() {
    }

    public int addAndReturnIndex(T item) {
        int index = Collections.binarySearch(this, item);

        if (index < 0) {
            index = ~index;
        }

        super.add(index, item);
        return index;
    }
    
    public boolean add(T item) {
        addAndReturnIndex(item);
        return true;
    }

    public boolean removeFast(T item) {
        
        int index = Collections.binarySearch(this, item);

        boolean removed;
        if (index < 0) {
            // This didn't exist
            removed = false;
        }else{
            remove(index);
            removed = true;
        }
        
        return removed;
    }
    
}

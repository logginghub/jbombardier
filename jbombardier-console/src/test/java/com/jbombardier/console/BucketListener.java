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

package com.jbombardier.console;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.logginghub.utils.Bucket;

public class BucketListener<T> extends Listener
{
    private final Bucket<T> bucket;


    public BucketListener(Bucket<T> bucket){
        this.bucket = bucket;
    }
    

    @SuppressWarnings("unchecked") @Override public void received(Connection connection, Object object)
    {
        super.received(connection, object);        
        bucket.add((T)object);
    }
}

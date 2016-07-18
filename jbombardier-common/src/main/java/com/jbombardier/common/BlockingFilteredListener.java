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

package com.jbombardier.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public abstract class BlockingFilteredListener<T> extends Listener
{
    private CountDownLatch latch = new CountDownLatch(1);
    private T message;

    protected abstract boolean accept(Object object);

    @SuppressWarnings("unchecked") @Override public void received(Connection connection, Object object)
    {
        super.received(connection, object);
        if (accept(object))
        {
            message = (T) object;
            latch.countDown();
        }
    }

    public void waitForResponse(int i, TimeUnit seconds)
    {
        try
        {
            latch.await(i, seconds);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public T getMessage()
    {
        return message;
    }

}

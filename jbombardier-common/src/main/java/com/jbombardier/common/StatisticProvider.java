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

import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.Source;

/**
 * Created by james on 13/11/14.
 */
public interface StatisticProvider extends Source<CapturedStatistic>, Asynchronous {

    /**
     * Provides a key/value mapping derived from the "properties" attribute which you can use to provide configuration properties to your stats provider.
     *
     * @param properties A Metadata instance (a helpful wrapper around standard properties) contains the variables from the configuration
     */
    public void configure(Metadata properties);

    /**
     * StatisticProviders are inherently asynchronous - so you need to start a thread or timer to actually carry out the work, or to subscribe to whatever streaming data you need.
     */
    @Override public void start();

    /**
     * Stop any asynchronous activities (kill threads, unsubscribe to streams etc) and wait for them to close. You must ensure that no CapturedStatistics are sent to any destinations after this call
     * completes.
     */
    @Override public void stop();

    /**
     * As statistics are generated asynchronously, the resulting CapturedStatistic instances need to be posted back to the console. You should store each destination (just like a listener) in a
     * list and call send(...) on each when a new statistic is captured.
     */
    @Override public void addDestination(Destination<CapturedStatistic> destination);

    /**
     * If the console is no longer interested in statistics from this provider, it will remove itself using removeDestination. Don't send any more statistics to this destination after this method is
     * called.
     */
    @Override public void removeDestination(Destination<CapturedStatistic> destination);
}

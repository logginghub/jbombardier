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

import com.jbombardier.common.serialisableobject.CapturedStatistic;
import com.jbombardier.console.statisticcapture.BaseStatisticCapture;
import com.logginghub.utils.Metadata;

/**
 * Created by james on 17/11/14.
 */
public class ControlStatisticsProvider extends BaseStatisticCapture {

    private volatile int i = 0;

    @Override public void configure(Metadata properties) {

    }


    @Override protected void doCapture() {
        send(new CapturedStatistic(getTimeProvider().getTime(), "control", Integer.toString(i++)));
    }
}

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

/**
 * Simple representation of a elapsed time result. It carries its own timestamp
 * (the agent-local millisecond the transaction was completed) so that a
 * complete picture can be reproduced on the console - this may be overkill
 * though as it doubles the size of the data!
 * 
 * @author James
 * 
 */
public class SimpleResult {
    // TODO : I've decided to start off without the times and see how we go. The
    // console probably only needs to be second accurate anyway, so when it gets
    // a result batch from the agent it can stamp them all with the
    // console-local time it received them and still be reasonably accurate.
    // public long time;
    public long elapsed;
}

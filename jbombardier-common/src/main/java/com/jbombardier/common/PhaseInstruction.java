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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 22/01/15.
 */
public class PhaseInstruction {
    private String phaseName;
    private List<TestInstruction> instructions = new ArrayList<TestInstruction>();

    public PhaseInstruction(String phaseName) {
        this.phaseName = phaseName;
    }

    public PhaseInstruction() {

    }

    public String getPhaseName() {
        return phaseName;
    }

    public List<TestInstruction> getInstructions() {
        return instructions;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("PhaseInstruction{");
        sb.append("phaseName='").append(phaseName).append('\'');
        sb.append(", instructions=").append(instructions);
        sb.append('}');
        return sb.toString();
    }
}

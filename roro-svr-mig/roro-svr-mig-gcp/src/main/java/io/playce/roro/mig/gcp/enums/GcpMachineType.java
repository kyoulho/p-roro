/*
 * Copyright 2022 The playce-roro-v3 Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * SangCheon Park   Feb 14, 2022		    First Draft.
 */
package io.playce.roro.mig.gcp.enums;

import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public enum GcpMachineType {
    GENERAL("General purpose", Arrays.asList("e2", "n2", "n2d", "n1", "f1", "g1")),
    MEMORY("Memory Optimized", Arrays.asList("m1", "m2")),
    COMPUTE("Compute Optimized", Arrays.asList("c2")),
    ACCELERATOR("Accelerator Optimized", Arrays.asList("a2")),
    UNKNOWN("Unknown", Arrays.asList());

    private String type;
    private List<String> machineList;

    GcpMachineType(String type, List<String> machineList) {
        this.type = type;
        this.machineList = machineList;
    }

    public static GcpMachineType findByMachine(String machine) {
        return Arrays.stream(GcpMachineType.values())
                .filter(m -> m.hasMachine(machine))
                .findAny()
                .orElse(GcpMachineType.UNKNOWN);
    }

    public boolean hasMachine(String machine) {
        return machineList.stream().anyMatch(m -> machine.indexOf(m) > -1);
    }

    public String getType() {
        return type;
    }

}
//end of GcpMachineType.java
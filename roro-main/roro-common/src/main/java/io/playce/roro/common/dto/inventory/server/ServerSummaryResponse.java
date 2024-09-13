/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       11월 24, 2021            First Draft.
 */
package io.playce.roro.common.dto.inventory.server;

import lombok.Getter;
import lombok.Setter;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Getter
@Setter
public class ServerSummaryResponse {

    private String hostName;
    private String vendorName;
    private int cpuCount;
    private int cpuCoreCount;
    private int cpuSocketCount;
    private String cpuArchitecture;
    private String osKernel;
    private String osName;
    private String osFamily;
    private String osVersion;
    private long memorySize;
    private long swapSize;
    private String osAlias;
}
//end of ServerSummary.java
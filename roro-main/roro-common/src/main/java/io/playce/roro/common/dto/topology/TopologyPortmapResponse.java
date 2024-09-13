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
 * Dong-Heon Han    Mar 17, 2022		First Draft.
 */

package io.playce.roro.common.dto.topology;

import lombok.Getter;
import lombok.Setter;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Setter @Getter
public class TopologyPortmapResponse {
    private String targetName;
    private String targetIp;
    private Boolean isTargetInventory;
    private Integer port;
    private String sourceName;
    private String sourceIp;
    private Boolean isSourceInventory;
}
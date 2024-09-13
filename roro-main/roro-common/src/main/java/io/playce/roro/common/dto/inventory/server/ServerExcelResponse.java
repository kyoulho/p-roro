/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Jaeeon Bae       2월 17, 2022            First Draft.
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
public class ServerExcelResponse {

    private Long projectId;
    private String projectName;
    private String customerInventoryCode;
    private String customerInventoryName;
    private Long serviceId;
    private String serviceName;
    private Long serverInventoryId;
    private String serverInventoryName;
    private String representativeIpAddress;
    private int connectionPort;
    private String userName;
    private String enableSuYn;
    private String windowsYn;
    private String migrationTypeCode;
    private String serverLocation;
    private String serverUsageTypeCode;
    private String hypervisorTypeCode;
    private String dualizationTypeCode;
    private String accessControlSystemSolutionName;
    private float tpmc;
    private String buyDate;
    private String makerName;
    private String modelName;
    private String serialNumber;
    private String labels;
    private String description;

}
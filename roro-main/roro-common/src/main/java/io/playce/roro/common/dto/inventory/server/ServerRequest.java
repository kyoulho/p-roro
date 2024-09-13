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

import io.playce.roro.common.dto.inventory.inventory.InventoryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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
public class ServerRequest extends InventoryRequest {

    @Schema(example = "Test Server", description = "server inventory name")
    private String serverInventoryName;
    @Schema(example = "rehost/replatform", description = "migration type code")
    private String migrationTypeCode;
    @Schema(example = "N", description = "windows Y/N")
    private String windowsYn;
    @Schema(example = "127.0.0.1", description = "representative ip address")
    private String representativeIpAddress;
    @Schema(example = "22", description = "connection port")
    private int connectionPort;
    @Schema(example = "1", description = "credential id")
    private Long credentialId;
    @Schema(example = "roro", description = "username")
    private String userName;
    @Schema(example = "*****", description = "user password")
    private String userPassword;
    @Schema(example = "N", description = "Enable su(switch user to root) Y/N")
    private String enableSuYn = "N";
    @Schema(example = "*****", description = "root password")
    private String rootPassword;
    @Schema(example = "DMZ", description = "server location")
    private String serverLocation;
    @Schema(example = "DEV", description = "server usage type code")
    private String serverUsageTypeCode;
    @Schema(example = "ovirt", description = "hypervisor type code")
    private String hypervisorTypeCode;
    @Schema(example = "Single / A-A / A-S", description = "dualization type code")
    private String dualizationTypeCode;
    @Schema(example = "Geteone", description = "access control system solution name")
    private String accessControlSystemSolutionName;
    @Schema(example = "19", description = "tpmc")
    private Float tpmc;
    @Schema(example = "20211102", description = "buy date")
    private String buyDate;
    @Schema(example = "HP", description = "maker name")
    private String makerName;
    @Schema(example = "A123", description = "model name")
    private String modelName;
    @Schema(example = "a1234567890", description = "serial number")
    private String serialNumber;
    @Schema(example = "Y", description = "scheduled assessment Y/N")
    private String scheduledAssessmentYn;

    private Long discoveredInstanceId;
    @Schema(example = "Y", description = "Monitoring Y/N")
    private String monitoringYn = "N";
    @Schema(example = "0 0 12 * * ?", description = "Monitoring Cycle(Cron Expression)")
    private String monitoringCycle;
    private Date monitoringStartDatetime;
    private Date monitoringEndDatetime;
}
//end of ServerRequest.java
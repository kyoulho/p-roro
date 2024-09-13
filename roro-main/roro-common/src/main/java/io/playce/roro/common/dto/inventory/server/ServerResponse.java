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

import io.playce.roro.common.dto.common.label.Label;
import io.playce.roro.common.dto.inventory.manager.Manager;
import io.playce.roro.common.dto.inventory.process.InventoryProcess;
import io.playce.roro.common.dto.inventory.service.Service;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

public class ServerResponse {
    private Long projectId;
    private Long serverInventoryId;
    private String inventoryTypeCode;
    private String inventoryDetailTypeCode;
    private String inventoryAnalysisYn;
    private String customerInventoryCode;
    private String customerInventoryName;
    private String serverInventoryName;
    private List<Service> services;
    private ServerSummaryResponse serverSummary;
    private String migrationTypeCode;
    private String windowsYn;
    private String representativeIpAddress;
    private int connectionPort;
    private String userName;
    @Getter(AccessLevel.NONE)
    private String userPassword;
    private String keyFileName;
    private String keyFilePath;
    private String keyFileContent;
    private Long credentialId;
    private String enableSuYn;
    @Getter(AccessLevel.NONE)
    private String rootPassword;
    private String monitoringYn;
    private String monitoringCycle;
    private Date monitoringStartDatetime;
    private Date monitoringEndDatetime;
    private String serverLocation;
    private String serverUsageTypeCode;
    private String hypervisorTypeCode;
    private String dualizationTypeCode;
    private String accessControlSystemSolutionName;
    private Float tpmc;
    private String buyDate;
    private String makerName;
    private String modelName;
    private String serialNumber;
    private List<Label.LabelResponse> labelList;
    private List<Manager> inventoryManagers;
    private List<InventoryProcess.Result> lastInventoryProcesses = new ArrayList<>();
    private InventoryProcess.CompleteScan lastCompleteScan;
    private String discoveredServerYn;
    private String scheduledAssessmentYn;
    private Date registDatetime;
    private Long registUserId;
    private String registUserLoginId;
    private Date modifyDatetime;
    private Long modifyUserId;
    private String modifyUserLoginId;
    private Date inventoryDiscoveredDatetime;
    private String description;
    private int middlewareCount;
    private int applicationCount;
    private int databaseCount;

    public String getUserPassword() {
        return "";
    }

    public String getRootPassword() {
        return "";
    }
}
//end of ServerResponse.java
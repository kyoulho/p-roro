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
 * Jaeeon Bae       1월 26, 2022            First Draft.
 */
package io.playce.roro.common.dto.inventory.middleware;

import io.playce.roro.common.dto.common.label.Label;
import io.playce.roro.common.dto.inventory.process.InventoryProcess;
import io.playce.roro.common.dto.inventory.service.Service;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@ToString
public class MiddlewareResponse {

    private Long projectId;
    private String customerInventoryCode;
    private String customerInventoryName;
    private String inventoryTypeCode;
    private String inventoryDetailTypeCode;
    private Long middlewareInventoryId;
    private String middlewareInventoryName;
    private Long serverInventoryId;
    private String serverInventoryName;
    private List<Service> services;
    private String representativeIpAddress;
    private String vendorName;
    private String middlewareTypeCode;
    private String solutionName;
    private String engineVersion;
    private String engineInstallPath;
    private String domainHomePath;
    private String javaVersion;
    private String javaVendor;
    private Long registUserId;
    private String registUserLoginId;
    private Date registDatetime;
    private Long modifyUserId;
    private String modifyUserLoginId;
    private Date modifyDatetime;
    private List<Label.LabelResponse> labelList;
    private String description;
    private String automaticRegistYn;
    private String dedicatedAuthenticationYn;
    private String userName;
    @Getter(AccessLevel.NONE)
    private String userPassword;
    private String keyFileName;
    private String keyFilePath;
    private String keyFileContent;
    private Long credentialId;
    private InventoryProcess.CompleteScan lastCompleteScan;
    private InventoryProcess.Result lastInventoryProcess;
    private int instanceCount;
    private int applicationCount;
    private int datasourceCount;

    public String getUserPassword() {
        return "";
    }
}
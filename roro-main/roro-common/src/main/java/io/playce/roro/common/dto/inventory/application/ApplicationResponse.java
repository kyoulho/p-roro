/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * SangCheon Park   Nov 25, 2021		    First Draft.
 */
package io.playce.roro.common.dto.inventory.application;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.playce.roro.common.dto.common.label.Label;
import io.playce.roro.common.dto.inventory.manager.Manager;
import io.playce.roro.common.dto.inventory.process.InventoryProcess;
import io.playce.roro.common.dto.inventory.service.Service;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
public class ApplicationResponse {

    private Long projectId;
    private Long applicationInventoryId;
    private String applicationInventoryName;
    private String customerInventoryCode;
    private String customerInventoryName;
    private String inventoryTypeCode;
    private String inventoryDetailTypeCode;
    private String inventoryIpTypeCode;
    private List<Service> serviceList;
    private Long serverInventoryId;
    private String serverInventoryName;
    private String representativeIpAddress;
    private String deployPath;
    private String sourceLocationUri;
    private String uploadSourceFileName;
    private String uploadSourceFilePath;
    private Long applicationSize;
    @Getter(AccessLevel.NONE)
    private String analysisLibList;
    @Getter(AccessLevel.NONE)
    private String analysisStringList;
    private List<Label.LabelResponse> labelList;
    private List<Manager> inventoryManagers;
    private InventoryProcess.CompleteScan lastCompleteScan;
    private InventoryProcess.Result lastInventoryProcess;
    private String automaticRegistYn;
    private String automaticRegistProtectionYn;
    private String dedicatedAuthenticationYn;
    private String userName;
    @Getter(AccessLevel.NONE)
    private String userPassword;
    private String keyFileName;
    private String keyFilePath;
    private String keyFileContent;
    private Long credentialId;
    private Date registDatetime;
    private Long registUserId;
    private String registUserLoginId;
    private Date modifyDatetime;
    private Long modifyUserId;
    private String modifyUserLoginId;
    @JsonIgnore
    private String note;
    private int datasourceCount;
    private String description;
    private String javaVersion;
    private String javaVendor;

    public List<String> getAnalysisLibList() {
        if (StringUtils.isEmpty(analysisLibList)) {
            return new ArrayList<String>();
        }

        return Arrays.asList(analysisLibList.split(","));
    }

    public List<String> getAnalysisStringList() {
        if (StringUtils.isEmpty(analysisStringList)) {
            return new ArrayList<String>();
        }

        return Arrays.asList(analysisStringList.split(","));
    }

    public String getUserPassword() {
        return "";
    }
}
//end of ApplicationResponse.java
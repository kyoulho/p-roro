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
package io.playce.roro.common.dto.inventory.application;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
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
public class ApplicationExcelResponse {

    private Long projectId;
    private String projectName;
    private String customerInventoryCode;
    private String customerInventoryName;
    private Long serviceId;
    private String serviceName;
    private Long serverInventoryId;
    private String serverInventoryName;
    private Long applicationInventoryId;
    private String applicationInventoryName;
    private String inventoryDetailTypeCode;
    private String deployPath;
    private Long applicationSize;
    private String sourceLocationUri;
    private String uploadSourceFileName;
    private String uploadSourceFilePath;
    @Getter(AccessLevel.NONE)
    private String analysisLibList;
    @Getter(AccessLevel.NONE)
    private String analysisStringList;
    private String automaticRegistYn;
    private String automaticRegistProtectionYn;
    private String dedicatedAuthenticationYn;
    private String userName;
    @Getter(AccessLevel.NONE)
    private String userPassword;
    private String keyFileName;
    private String keyFilePath;
    private String keyFileContent;
    private String labels;
    private String description;

    public List<String> getAnalysisLibList() {
        if (StringUtils.isEmpty(analysisLibList)) {
            return new ArrayList<>();
        }

        return Arrays.asList(analysisLibList.split(","));
    }

    public List<String> getAnalysisStringList() {
        if (StringUtils.isEmpty(analysisStringList)) {
            return new ArrayList<>();
        }

        return Arrays.asList(analysisStringList.split(","));
    }

    public String getUserPassword() {
        return "";
    }
}
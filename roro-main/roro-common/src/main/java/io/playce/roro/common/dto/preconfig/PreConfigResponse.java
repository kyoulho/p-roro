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
 * SangCheon Park   Jan 21, 2022		    First Draft.
 */
package io.playce.roro.common.dto.preconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.playce.roro.common.dto.targetcloud.CredentialDto.CredentialSimpleResponse;
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
public class PreConfigResponse {
    private Long projectId;
    private Long serverInventoryId;
    private Long migrationPreConfigId;
    @JsonIgnore
    private Long credentialId;
    private String configName;
    private CredentialSimpleResponse credential;
    private String region;
    private String gcpProjectId;
    private String imageId;
    private String connectIpAddress;
    private Integer connectSshPort;
    private String connectUserName;
    @Getter(AccessLevel.NONE)
    private String connectUserPassword;
    private String keyPair;
    private String pubKey;
    private String keyFileName;
    private String keyFilePath;
    @Getter(AccessLevel.NONE)
    private String packages;
    private String initScript;
    private List<PreConfigUserResponse> migrationPreConfigUsers;
    private List<PreConfigGroupResponse> migrationPreConfigGroups;
    private List<PreConfigFileResponse> migrationPreConfigFiles;
    private Date registDatetime;
    private Long registUserId;
    private String registUserLoginId;
    private Date modifyDatetime;
    private Long modifyUserId;
    private String modifyUserLoginId;

    public List<String> getPackages() {
        if (StringUtils.isEmpty(packages)) {
            return new ArrayList<String>();
        }

        return Arrays.asList(packages.split(","));
    }

    public String getConnectUserPassword() {
        return "";
    }
}
//end of PreConfigResponse.java
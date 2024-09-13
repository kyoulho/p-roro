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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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
public class PreConfigRequest {

    private Long serverInventoryId;
    private Long credentialId;
    private String configName;
    private String region;
    private String gcpProjectId;
    private String imageId;
    private String connectIpAddress;
    private Integer connectSshPort;
    private String connectUserName;
    private String connectUserPassword;
    private String keyPair;
    private String pubKey;
    @Getter(AccessLevel.NONE)
    private List<String> packages;
    private String initScript;

    private List<PreConfigUserRequest> migrationPreConfigUsers;
    private List<PreConfigGroupRequest> migrationPreConfigGroups;
    private List<PreConfigFileRequest> migrationPreConfigFiles;

    public String getPackages() {
        if (packages == null) {
            return null;
        }

        return String.join(",", packages);
    }
}
//end of PreConfigRequest.java
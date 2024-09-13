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
 * SangCheon Park   Mar 10, 2022		    First Draft.
 */
package io.playce.roro.common.dto.migration;

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
public class MigrationRequestDto {

    private Long serverInventoryId;
    private Long migrationPreConfigId;
    private Long credentialId;
    private String region;
    private String availabilityZone;
    private String vpcId;
    private String subnetId;
    private String enableEipYn;
    private String privateIp;
    private String hostName;
    private String instanceType;
    private String gcpProjectId;
    private List<String> securityGroupIds;
    private List<String> firewalls;
    private List<String> excludeDirectories;
    private List<Volume> volumes;
    private List<Tag> tags;
    private List<String> networkTags;

    @Getter
    @Setter
    public static class Volume {
        private String volumePath;
        private Long volumeSize;
        private String rootYn;
        private String deviceName;
    }

    @Getter
    @Setter
    public static class Tag {
        private String tagName;
        private String tagValue;
    }
}
//end of MigrationRequestDto.java
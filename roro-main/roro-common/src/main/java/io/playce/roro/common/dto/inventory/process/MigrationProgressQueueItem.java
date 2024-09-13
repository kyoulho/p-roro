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
 * SangCheon Park   Mar 15, 2022		    First Draft.
 */
package io.playce.roro.common.dto.inventory.process;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Setter
@Getter
@Builder
@ToString
public class MigrationProgressQueueItem {

    private Long inventoryProcessId;
    private String region;
    private String blockDevices;
    private String availabilityZone;
    private String vpcName;
    private String subnetName;
    private String securityGroupNames;
    private String imageId;
    private String imageName;
    private String instanceId;
    private String instanceName;
    private String publicIp;
    private String privateIp;
    private Date instanceLaunchTime;
    private Long elapsedTime;
    private String internalStatus;
    private Double progress;
}
//end of MigrationProgressQueueItem.java
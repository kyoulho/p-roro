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
 * Author			Date				Description
 * ---------------	----------------	------------
 * SangCheon Park   Jan 1, 2022		First Draft.
 */
package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "inventory_migration_process")
public class InventoryMigrationProcess {

    @Id
    private Long inventoryProcessId;

    @Column
    private Long migrationPreConfigId;

    @Column
    private Long credentialId;

    @Column
    private String excludeDirectories;

    @Column
    private Integer swapSize;

    @Column
    private String region;

    @Column
    private String subnetId;

    @Column
    private String securityGroupIds;

    @Column
    private String enableEipYn;

    @Column
    private String privateIp;

    @Column
    private String instanceType;

    @Column
    private String description;

    @Column
    private String vpcId;

    @Column
    private String vpcName;

    @Column
    private String hostName;

    @Column
    private Integer estimateTime;

    @Column
    private String gcpProjectId;

    @Column
    private String networkTags;

    @Column
    private String subnetName;

    @Column
    private Date instanceLaunchTime;

    @Column
    private String availabilityZone;

    @Column
    private String securityGroupNames;

    @Column
    private String blockDevices;

    @Column
    private String publicIp;

    @Column
    private String instanceId;

    @Column
    private String instanceName;

    @Column
    private String imageId;

    @Column
    private String imageName;

    @Column
    private String firewalls;

    private Long elapsedTime = 0L;
    private String internalStatus;
    private Double progress;
}
//end of InventoryMigrationProcess.java
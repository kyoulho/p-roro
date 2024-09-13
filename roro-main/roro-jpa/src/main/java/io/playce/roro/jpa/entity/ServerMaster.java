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
 * SangCheon Park   Oct 29, 2021		First Draft.
 */
package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "server_master")
@ToString(exclude = "rootPassword")
public class ServerMaster {

    @Id
    private Long serverInventoryId;

    @Column
    private String migrationTypeCode;

    @Column
    private String osVersion;

    @Column
    private String windowsYn;

    @Column
    private String representativeIpAddress;

    @Column
    private Integer connectionPort;

    @Column
    private String enableSuYn = "N";

    @Column
    private String rootPassword;

    @Column
    private String serverLocation;

    @Column
    private String serverUsageTypeCode;

    @Column
    private String hypervisorTypeCode;

    @Column
    private String dualizationTypeCode;

    @Column
    private String accessControlSystemSolutionName;

    @Column
    private Float tpmc;

    @Column
    private String buyDate;

    @Column
    private String makerName;

    @Column
    private String modelName;

    @Column
    private String serialNumber;

    @Column
    private String monitoringYn = "N";

    @Column
    private String monitoringCycle;

    @Column
    private Date monitoringStartDatetime;

    @Column
    private Date monitoringEndDatetime;

    @Column
    private String automaticAnalysisYn;

    @Column
    private String discoveredServerYn;

    @Column
    private String scheduledAssessmentYn;

}
//end of ServerMaster.java
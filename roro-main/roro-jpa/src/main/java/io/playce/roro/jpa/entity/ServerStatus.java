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
@Table(name = "server_status")
public class ServerStatus {

    @Id
    private Long serverInventoryId;

    @Column
    private String serverType;

    @Column
    private String manufacturer;

    @Column
    private String model;

    @Column
    private Long diskSize;

    @Column
    private Integer diskCount;

    @Column
    private Long diskUsed;

    @Column
    private Double cpuUsage;

    @Column
    private Double memUsage;

    @Column
    private Date monitoringDatetime;

}
//end of ServerStatus.java
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

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "database_master")
@ToString
public class DatabaseMaster {

    @Id
    private Long databaseInventoryId;

    @Column
    private String vendor;

    @Column
    private String engineVersion;

    @Column
    private Integer connectionPort;

    @Column
    private String databaseServiceName;

    @Column
    private String jdbcUrl;

    @Column
    private String allScanYn;

    @Column
    private String databaseAccessControlSystemSolutionName;

    @Column
    private String discoveredDatabaseYn;

}
//end of DatabaseMaster.java
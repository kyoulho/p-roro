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
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "migration_pre_config")
@Where(clause = "delete_yn='N'")
public class MigrationPreConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long migrationPreConfigId;

    @Column
    private Long serverInventoryId;

    @Column
    private Long credentialId;

    @Column
    private Long registUserId;

    @Column
    private Date registDatetime;

    @Column
    private Long modifyUserId;

    @Column
    private Date modifyDatetime;

    @Column
    private String configName;

    @Column
    private String region;

    @Column
    private String gcpProjectId;

    @Column
    private String imageId;

    @Column
    private String connectIpAddress;

    @Column
    private Integer connectSshPort;

    @Column
    private String connectUserName;

    @Column
    private String connectUserPassword;

    @Column
    private String keyPair;

    @Column
    private String pubKey;

    @Column
    private String deleteYn;

    @Column
    private String keyFileName;

    @Column
    private String keyFilePath;

    @Column
    private String packages;

    @Column
    private String initScript;

}
//end of MigrationPreConfig.java
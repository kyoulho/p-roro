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

import io.playce.roro.jpa.entity.pk.MigrationPreConfigUserPK;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@IdClass(MigrationPreConfigUserPK.class)
@Getter
@Setter
@Table(name = "migration_pre_config_user")
public class MigrationPreConfigUser {

    @Id
    private Long migrationPreConfigId;

    @Id
    private Integer uid;

    @Column
    private String userName;

    @Column
    private String userPassword;

    @Column
    private String groups;

    @Column
    private String homeDir;

    @Column
    private String profile;

    @Column
    private String crontab;

}
//end of MigrationPreConfigUser.java
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
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "project_master")
@Where(clause = "delete_yn='N'")
public class ProjectMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    @Column
    private String projectName;

    @Column
    private String projectTypeCode;

    @Column
    private String deleteYn;

    @Column
    private String description;

    @Column
    private Long registUserId;

    @Column
    private Date registDatetime;

    @Column
    private Long modifyUserId;

    @Column
    private Date modifyDatetime;

}
//end of ProjectMaster.java
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
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "inventory_master")
@Where(clause = "delete_yn='N'")
@ToString
public class InventoryMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    @Column
    private Long projectId;

    @Column
    private Long serverInventoryId;

    @Column
    private Long credentialId;

    @Column
    private String inventoryTypeCode;

    @Column
    private String inventoryDetailTypeCode;

    @Column
    private String inventoryName;

    @Column
    private String inventoryAnalysisYn;

    @Column
    private String customerInventoryCode;

    @Column
    private String customerInventoryName;

    @Column
    private String inventoryIpTypeCode;

    @Column
    private String deleteYn;

    @Column
    private String automaticRegistYn;

    @Column
    private Date inventoryDiscoveredDatetime;

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
//end of InventoryMaster.java
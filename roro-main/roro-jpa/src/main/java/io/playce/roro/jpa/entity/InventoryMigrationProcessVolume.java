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

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "inventory_migration_process_volume")
public class InventoryMigrationProcessVolume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long migrationVolumeId;

    @Column
    private Long inventoryProcessId;

    @Column
    private String volumePath;

    @Column
    private Long volumeSize;

    @Column
    private String rawFileName;

    @Column
    private Long rawFileSize;

    @Column
    private String rootYn;

    @Column
    private String volumeId;

    @Column
    private String deviceName;

    @Column
    private String manifestUrl;

}
//end of InventoryMigrationProcessVolume.java
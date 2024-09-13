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
 * Dong-Heon Han    Feb 09, 2022		First Draft.
 */

package io.playce.roro.common.dto.inventory.database;

import io.playce.roro.common.dto.inventory.service.Service;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Getter
@Setter
@ToString
public class DatabaseInstanceResponseDto {

    private String databaseServiceName;
    private Long databaseInventoryId;
    private String databaseInventoryName;
    private List<Service> services;
    private Long serverInventoryId;
    private String serverInventoryName;
    private String serverIp;
    private String vendor;
    private String inventoryDetailTypeCode; // EngineName
    private int tableCount;
    private int viewCount;
    private int functionCount;
    private int procedureCount;
    private Date registDatetime;

}
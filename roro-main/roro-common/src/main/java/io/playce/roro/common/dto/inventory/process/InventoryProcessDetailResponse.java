/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * SangCheon Park   Nov 30, 2021		    First Draft.
 */
package io.playce.roro.common.dto.inventory.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;


@Getter @Setter
public class InventoryProcessDetailResponse {
    private Long inventoryProcessId;
    private Long inventoryProcessGroupId;
    private Long inventoryId;
    private String inventoryName;
    private String inventoryTypeCode;
    private String inventoryDetailTypeCode;
    private String inventoryProcessTypeCode;
    private String inventoryProcessResultCode;
    private String inventoryProcessResultTxt;
    private Date inventoryProcessStartDatetime;
    private Date inventoryProcessEndDatetime;
    private String registUserLoginId;
    private Date registDatetime;
    private String modifyUserLonginId;
    private Date modifyDatetime;

    private String inventoryProcessResultJson;
    private String inventoryProcessResultJsonPath;
    private String inventoryProcessResultExcelPath;

    @JsonIgnore
    private String inventoryProcessResultMetaList;
    private JsonNode jsonMetas;
}
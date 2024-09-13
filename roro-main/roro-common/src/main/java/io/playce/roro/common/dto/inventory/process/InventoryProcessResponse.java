/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       1월 25, 2022            First Draft.
 */
package io.playce.roro.common.dto.inventory.process;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Getter
@Setter
@ToString
public class InventoryProcessResponse {

    private Long inventoryProcessId;
    private Long inventoryProcessGroupId;
    private Long inventoryId;
    private String inventoryProcessTypeCode;
    private String inventoryProcessResultCode;
    private String inventoryProcessResultTxt;
    private Date inventoryProcessStartDatetime;
    private Date inventoryProcessEndDatetime;
    private String inventoryProcessResultJsonPath;
    private String inventoryProcessResultExcelPath;
}
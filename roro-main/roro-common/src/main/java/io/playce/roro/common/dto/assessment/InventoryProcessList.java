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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       12월 09, 2021            First Draft.
 */
package io.playce.roro.common.dto.assessment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Getter
@Setter
@ToString
public class InventoryProcessList {
    private Long projectId;
    private Long inventoryProcessGroupId;
    private Long inventoryProcessId;
    private String inventoryProcessTypeCode;
    private Long inventoryId;
    private String inventoryProcessResultCode;
    private String inventoryProcessResultTxt;
    private String inventoryProcessResultJson;
    private String inventoryProcessResultJsonPath;
    private String inventoryProcessResultExcelPath;
    private Date inventoryProcessStartDatetime;
    private Date inventoryProcessEndDatetime;
    private Long registUserId;
    private Date registDatetime;
    private Long modifyUserId;
    private Date modifyDatetime;
}
//end of InventoryProcessDetail.java
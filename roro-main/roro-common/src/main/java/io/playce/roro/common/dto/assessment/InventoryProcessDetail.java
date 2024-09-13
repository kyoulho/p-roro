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

import java.time.LocalDateTime;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class InventoryProcessDetail {
    private Long projectId;
    private Long inventoryProcessGroupId;
    private Long inventoryProcessId;
    private String inventoryProcessTypeCode;
    private Long inventoryId;
    private String inventoryName;
    private String inventoryTypeCode;
    private String inventoryProcessResultCode;
    private String inventoryProcessResultTxt;
    private String inventoryProcessResultJson;
    private String inventoryProcessResultJsonPath;
    private String inventoryProcessResultExcelPath;
    private LocalDateTime inventoryProcessStartDatetime;
    private LocalDateTime inventoryProcessEndDatetime;
    private Long registUserLoginId;
    private LocalDateTime registDatetime;
    private Long modifyUserLoginId;
    private LocalDateTime modifyDatetime;
}
//end of InventoryProcessDetail.java
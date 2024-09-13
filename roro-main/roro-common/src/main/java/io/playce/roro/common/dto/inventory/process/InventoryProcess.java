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

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
public class InventoryProcess {

    private Long inventoryProcessId;
    private Long inventoryProcessGroupId;
    private Long inventoryId;
    private String inventoryProcessTypeCode;
    private String inventoryProcessResultCode;
    private String inventoryProcessResultTxt;
    private Date inventoryProcessStartDatetime;
    private Date inventoryProcessEndDatetime;
    private Long registUserId;
    private Date registDatetime;
    private Long modifyUserId;
    private Date modifyDatetime;

    @Setter
    @Getter
    public static class Prerequisite {
        private Long serverInventoryId;
        private String serverInventoryName;
        private String representativeIpAddress;
        private Integer connectionPort;
        private String resultJson;
    }

    @Getter
    @Setter
    public static class Result {
        private Long inventoryProcessId;
        private String inventoryProcessTypeCode;
        private String inventoryProcessResultCode;
        private String inventoryProcessResultTxt;
        private Date registDatetime;
    }

    @Getter
    @Setter
    public static class CompleteScan {
        private Long inventoryProcessId;
        private String inventoryProcessResultJsonPath;
        private String inventoryProcessResultExcelPath;
    }

}
//end of InventoryProcess.java
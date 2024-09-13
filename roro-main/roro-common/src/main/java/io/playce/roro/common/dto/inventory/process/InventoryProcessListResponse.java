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

import java.util.Date;
import java.util.List;


/**
 * <pre>
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 3.0
 */
@Getter
@Setter
public class InventoryProcessListResponse {

    // private Summary summary;
    private Data data;

    @Deprecated
    @Getter
    @Setter
    // @JsonIgnoreProperties(value = {"totalCount"})
    public static class Summary {
        private Long pending;
        private Long inProcess;
        private Long completed;
        private Long cancelled;
        private Long failed;
        private Long request;
        private Long notSupported;
        private Long partiallyCompleted;

        public Long getTotalCount() {
            return pending + inProcess + completed + cancelled + failed + request + notSupported + partiallyCompleted;
        }
    }

    @Getter
    @Setter
    public static class Data {
        private Long totalCount;
        private List<Content> contents;
    }

    @Getter
    @Setter
    public static class Content {
        private String registUserLoginId;
        private Date registDatetime;
        private String modifyUserLoginId;
        private Date modifyDatetime;
        private Long projectId;
        private Long inventoryProcessGroupId;
        private Long inventoryProcessId;
        private String inventoryProcessTypeCode;
        private Long inventoryId;
        private String inventoryName;
        private String inventoryTypeCode;
        private String inventoryProcessResultCode;
        private String inventoryProcessResultTxt;
        // private String inventoryProcessResultJson;
        private String inventoryProcessResultJsonPath;
        private String inventoryProcessResultExcelPath;
        private Date inventoryProcessStartDatetime;
        private Date inventoryProcessEndDatetime;
        private String representativeIpAddress;
    }
}
//end of InventoryProcess.java
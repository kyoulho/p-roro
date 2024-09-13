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
 * Jaeeon Bae       11월 22, 2021            First Draft.
 */
package io.playce.roro.common.dto.inventory.inventory;

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
public class InventoryResponse {

    private long uploadInventoryId;
    private long projectId;
    private String fileName;
    private String filePath;
    private String uploadStatusTypeCode;
    private String uploadProcessResultTxt;
    private int serviceCount;
    private int serverCount;
    private int middlewareCount;
    private int applicationCount;
    private int databaseCount;
    private Long registUserId;
    private String registUserLoginId;
    private Date registDatetime;
    private Long modifyUserId;
    private String modifyUserLoginId;
    private Date modifyDatetime;
}
//end of InventoryResponse.java
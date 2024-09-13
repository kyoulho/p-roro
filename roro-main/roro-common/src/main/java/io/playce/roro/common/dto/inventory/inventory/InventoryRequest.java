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
 * SangCheon Park   Dec 07, 2021		    First Draft.
 */
package io.playce.roro.common.dto.inventory.inventory;

import io.playce.roro.common.dto.inventory.application.ApplicationRequest;
import io.playce.roro.common.dto.inventory.manager.Manager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * <pre>
 * Server, Middleware, Application, Database 생성, 수정시 공통으로 사용되는 INVENTORY_MASTER 테이블 정보
 * See also {@link ApplicationRequest}
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
public class InventoryRequest {

    private List<Long> serviceIds;
    private Long serverInventoryId;
    private Long credentialId;
    private String inventoryTypeCode; // SVR, MW, APP, DBMS
    private String inventoryDetailTypeCode; // AIX, HP_UX, JEUS, TOMCAT, ORACLE, MYSQL, EAR, WAR, Etc.
    private String inventoryName;
    // private String inventoryAnalysisYn;
    private String customerInventoryCode;
    private String customerInventoryName;
    private String inventoryIpTypeCode;
    // private String deleteYn;
    // private String automaticRegistYn;
    private List<Long> labelIds;
    private List<Manager> inventoryManagers;
    private String description;

}
//end of InventoryRequest.java
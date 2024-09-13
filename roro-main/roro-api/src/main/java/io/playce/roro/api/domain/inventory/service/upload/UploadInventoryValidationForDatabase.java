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
 * Jaeeon Bae       12월 13, 2021            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.upload;

import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.dto.inventory.inventory.InventoryUploadFail;
import io.playce.roro.jpa.entity.DatabaseMaster;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.repository.InventoryMasterRepository;
import io.playce.roro.mybatis.domain.inventory.database.DatabaseMapper;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadInventoryValidationForDatabase {

    private final UploadInventoryValidationForServer uploadInventoryValidationForServer;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final DatabaseMapper databaseMapper;
    private final InventoryProcessMapper inventoryProcessMapper;

    public void validationDatabaseInventory(String sheetName, InventoryMaster serverInventoryMaster,
                                            InventoryMaster databaseInventoryMaster, DatabaseMaster databaseMaster,
                                            List<String> databaseCodeList, List<InventoryUploadFail> validationList, int row) {
        /* Database Validations */
        uploadInventoryValidationForServer.checkCustomerServerCode(sheetName, serverInventoryMaster, databaseCodeList, validationList, row);
        checkDatabaseCode(sheetName, databaseInventoryMaster.getCustomerInventoryCode(), databaseCodeList, validationList, row);
        checkServerAndJdbcUrlAndDatabaseServiceName(sheetName, serverInventoryMaster, databaseInventoryMaster, databaseMaster, validationList, row);
        checkValidateModifyServer(sheetName, serverInventoryMaster, databaseInventoryMaster, validationList, row);
    }

    /**
     * 엑셀 시트에 동일한 Database Code 중복 체크
     */
    private void checkDatabaseCode(String sheetName, String customerInventoryCode,
                                   List<String> databaseCodeList, List<InventoryUploadFail> validationList, int row) {
        if (customerInventoryCode != null && databaseCodeList.contains(customerInventoryCode)) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Database Code");
            inventoryUploadFail.setFailDetail("Database Code cannot be duplicated.");
            validationList.add(inventoryUploadFail);
        } else {
            databaseCodeList.add(customerInventoryCode);
        }
    }

    /**
     * 서버 & JDBC URL & DBMS Service Name 중복 체크
     */
    private void checkServerAndJdbcUrlAndDatabaseServiceName(String sheetName, InventoryMaster serverInventoryMaster,
                                                             InventoryMaster databaseInventoryMaster, DatabaseMaster databaseMaster,
                                                             List<InventoryUploadFail> validationList, int row) {
        InventoryMaster serverInventory = inventoryMasterRepository
                .findByCustomerInventoryCodeAndProjectId(serverInventoryMaster.getCustomerInventoryCode(), databaseInventoryMaster.getProjectId());
        InventoryMaster databaseInventory = inventoryMasterRepository
                .findByProjectIdAndInventoryIdAndInventoryTypeCode(databaseInventoryMaster.getProjectId(), databaseInventoryMaster.getInventoryId(), Domain1001.DBMS.name());
        int count;

        if (databaseInventory == null && databaseInventoryMaster.getInventoryId() != null) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Database ID");
            inventoryUploadFail.setFailDetail("Database ID(" + databaseInventoryMaster.getInventoryId() + ") does not exist in this project.");
            validationList.add(inventoryUploadFail);
        }

        if (serverInventory == null) {
            if (databaseInventory == null) {
                // database service name & jdbcUrl & project id 로 중복 검사
                count = databaseMapper.selectDatabaseCountByServiceNameAndJdbcUrlAndProjectId(databaseMaster.getDatabaseServiceName(),
                        databaseMaster.getJdbcUrl(), databaseInventoryMaster.getProjectId(), null, null);
            } else {
                count = databaseMapper.selectDatabaseCountByServiceNameAndJdbcUrlAndProjectId(databaseMaster.getDatabaseServiceName(),
                        databaseMaster.getJdbcUrl(), databaseInventoryMaster.getProjectId(), databaseInventory.getInventoryId(), null);
            }
        } else {
            if (databaseInventory == null) {
                // database service name & jdbcUrl & project id & server inventory id 로 중복 검사
                count = databaseMapper.selectDatabaseCountByServiceNameAndJdbcUrlAndProjectId(databaseMaster.getDatabaseServiceName(),
                        databaseMaster.getJdbcUrl(), databaseInventoryMaster.getProjectId(), null, serverInventory.getInventoryId());
            } else {
                count = databaseMapper.selectDatabaseCountByServiceNameAndJdbcUrlAndProjectId(databaseMaster.getDatabaseServiceName(),
                        databaseMaster.getJdbcUrl(), databaseInventoryMaster.getProjectId(), databaseInventory.getInventoryId(), serverInventory.getInventoryId());
            }
        }

        if (count > 0) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("IP Address,Port");
            inventoryUploadFail.setFailDetail("Server, Database Service Name and Jdbc Url are already exist.");
            validationList.add(inventoryUploadFail);
        }
    }

    /**
     * 기존의 데이터베이스를 수정할 때 Server Inventory Code를 수정할 경우, Database Assessment Scan의 Complete가 있는지 체크
     * Asessment Scan의 Complete가 하나라도 있으면 Server Inventory Code 수정 불가
     */
    private void checkValidateModifyServer(String sheetName, InventoryMaster serverInventoryMaster,
                                           InventoryMaster databaseInventoryMaster, List<InventoryUploadFail> validationList, int row) {
        if (databaseInventoryMaster.getInventoryId() != null) {
            InventoryMaster originDatabaseInventoryMaster = inventoryMasterRepository.findById(databaseInventoryMaster.getInventoryId()).orElse(null);
            InventoryMaster targetServerInventoryMaster = inventoryMasterRepository.findByCustomerInventoryCodeAndProjectId(serverInventoryMaster.getCustomerInventoryCode(), serverInventoryMaster.getProjectId());

            if (originDatabaseInventoryMaster != null && targetServerInventoryMaster != null) {
                // 서버를 수정할 때 성공한 Assessment 결과가 있는 경우 수정불가하다.
                if (!Objects.equals(originDatabaseInventoryMaster.getServerInventoryId(), targetServerInventoryMaster.getInventoryId())) {
                    if (inventoryProcessMapper.selectSuccessCompleteCount(databaseInventoryMaster.getInventoryId()) != 0) {
                        InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                        inventoryUploadFail.setSheet(sheetName);
                        inventoryUploadFail.setRowNumber(row);
                        inventoryUploadFail.setColumnNumber("Server Inventory Code");
                        inventoryUploadFail.setFailDetail("This database has already been successfully scanned and the Server cannot be modified.");
                        validationList.add(inventoryUploadFail);
                    }
                }
            }
        }
    }
}
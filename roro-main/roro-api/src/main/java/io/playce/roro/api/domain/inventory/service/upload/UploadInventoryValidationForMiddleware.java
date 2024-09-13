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
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.dto.inventory.inventory.InventoryUploadFail;
import io.playce.roro.jpa.entity.CredentialMaster;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.entity.MiddlewareMaster;
import io.playce.roro.jpa.repository.InventoryMasterRepository;
import io.playce.roro.jpa.repository.MiddlewareMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

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
public class UploadInventoryValidationForMiddleware extends UploadValidation {

    private final UploadInventoryValidationForServer uploadInventoryValidationForServer;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final MiddlewareMasterRepository middlewareMasterRepository;

    public void validationMiddlewareInventory(String sheetName, InventoryMaster serverInventoryMaster,
                                              InventoryMaster middlewareInventoryMaster, MiddlewareMaster middlewareMaster,
                                              CredentialMaster middlewareCredentialMaster, List<String> middlewareCodeList,
                                              List<InventoryUploadFail> validationList, int row) {
        /* Middleware Validations */
        uploadInventoryValidationForServer.checkCustomerServerCode(sheetName, serverInventoryMaster, middlewareCodeList, validationList, row);
        checkMiddlewareCode(sheetName, middlewareInventoryMaster.getCustomerInventoryCode(), middlewareCodeList, validationList, row);
        checkMiddlewareDuplicate(sheetName, serverInventoryMaster, middlewareInventoryMaster, middlewareMaster, validationList, row);
        checkDedicatedAuthentication(sheetName, middlewareMaster, middlewareCredentialMaster, validationList, row);
    }

    /**
     * 엑셀 시트에 동일한 Middleware Code 중복 체크
     */
    private void checkMiddlewareCode(String sheetName, String customerInventoryCode,
                                     List<String> middlewareCodeList, List<InventoryUploadFail> validationList, int row) {
        if (customerInventoryCode != null && middlewareCodeList.contains(customerInventoryCode)) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Middleware Code");
            inventoryUploadFail.setFailDetail("Middleware Code cannot be duplicated.");
            validationList.add(inventoryUploadFail);
        } else {
            middlewareCodeList.add(customerInventoryCode);
        }
    }

    /**
     * 미들웨어 중복 체크
     */
    private void checkMiddlewareDuplicate(String sheetName, InventoryMaster serverInventoryMaster,
                                          InventoryMaster middlewareInventoryMaster, MiddlewareMaster middlewareMaster,
                                          List<InventoryUploadFail> validationList, int row) {
        InventoryMaster middleware = inventoryMasterRepository
                .findByProjectIdAndInventoryIdAndInventoryTypeCode(middlewareInventoryMaster.getProjectId(), middlewareInventoryMaster.getInventoryId(), Domain1001.MW.name());

        InventoryMaster serverInventory = inventoryMasterRepository
                .findByCustomerInventoryCodeAndProjectId(serverInventoryMaster.getCustomerInventoryCode(), serverInventoryMaster.getProjectId());

        if (middleware == null && middlewareInventoryMaster.getInventoryId() != null) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Middleware ID");
            inventoryUploadFail.setFailDetail("Middleware ID(" + middlewareInventoryMaster.getInventoryId() + ") does not exist in this project.");
            validationList.add(inventoryUploadFail);
        }

        if (middleware != null) {
            // 이미 등록된 미들웨어
            // 미들웨어 등록 중복 체크 로직
            List<Long> middlewareIds = middlewareMasterRepository.selectDuplicateMiddlewareInventory(serverInventory.getServerInventoryId(),
                    middlewareMaster.getEngineInstallationPath(), middlewareMaster.getDomainHomePath());

            if (middlewareIds != null && middlewareIds.size() > 0) {
                if (middlewareIds.size() != 1 || !middlewareIds.get(0).equals(middleware.getInventoryId())) {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                    inventoryUploadFail.setSheet(sheetName);
                    inventoryUploadFail.setRowNumber(row);
                    inventoryUploadFail.setColumnNumber("Middleware");
                    inventoryUploadFail.setFailDetail("Middleware(" + middlewareMaster.getEngineInstallationPath() + ") cannot be duplicated.");
                    validationList.add(inventoryUploadFail);
                }
            }
        } else {
            // 신규 등록 건
            serverInventory = inventoryMasterRepository
                    .findByCustomerInventoryCodeAndProjectId(serverInventoryMaster.getCustomerInventoryCode(), serverInventoryMaster.getProjectId());

            // 미들웨어 등록 중복 체크 로직
            if (serverInventory != null) {
                List<Long> middlewareIds = middlewareMasterRepository.selectDuplicateMiddlewareInventory(serverInventory.getServerInventoryId(),
                        middlewareMaster.getEngineInstallationPath(), middlewareMaster.getDomainHomePath());

                if (middlewareIds != null && middlewareIds.size() > 0) {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                    inventoryUploadFail.setSheet(sheetName);
                    inventoryUploadFail.setRowNumber(row);
                    inventoryUploadFail.setColumnNumber("Middleware");
                    inventoryUploadFail.setFailDetail("Middleware(" + middlewareMaster.getEngineInstallationPath() + ") cannot be duplicated.");
                    validationList.add(inventoryUploadFail);
                }
            }
        }
    }

    /**
     * 전용 인증 정보 사용 여부가 'Y'인 경우 userName과 userPassword 체크
     */
    private void checkDedicatedAuthentication(String sheetName, MiddlewareMaster middlewareMaster,
                                              CredentialMaster middlewareCredentialMaster, List<InventoryUploadFail> validationList, int row) {
        if (Domain101.Y.name().equals(middlewareMaster.getDedicatedAuthenticationYn())) {
            if (StringUtils.isEmpty(middlewareCredentialMaster.getUserName())) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Middleware");
                inventoryUploadFail.setFailDetail("'Dedicated Authentication Y/N' If the value is 'Y', the Username cannot be blank.");
                validationList.add(inventoryUploadFail);
            }

            validatePasswordAndKeyFiles(sheetName, middlewareCredentialMaster, validationList, row);
        }
    }
}
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
import io.playce.roro.jpa.entity.ApplicationMaster;
import io.playce.roro.jpa.entity.CredentialMaster;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.repository.InventoryMasterRepository;
import io.playce.roro.mybatis.domain.inventory.application.ApplicationMapper;
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
public class UploadInventoryValidationForApplication extends UploadValidation {

    private final UploadInventoryValidationForServer uploadInventoryValidationForServer;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final ApplicationMapper applicationMapper;

    public void validationApplicationInventory(String sheetName, InventoryMaster serverInventoryMaster,
                                               InventoryMaster applicationInventoryMaster, ApplicationMaster applicationMaster,
                                               CredentialMaster applicationCredentialMaster, List<String> applicationCodeList, List<InventoryUploadFail> validationList, int row) {
        /* Application Validations */
        uploadInventoryValidationForServer.checkCustomerServerCode(sheetName, serverInventoryMaster, applicationCodeList, validationList, row);
        checkApplicationCode(sheetName, applicationInventoryMaster.getCustomerInventoryCode(), applicationCodeList, validationList, row);
        checkApplicationDuplicate(sheetName, serverInventoryMaster, applicationInventoryMaster, applicationMaster, validationList, row);
        checkDedicatedAuthentication(sheetName, applicationMaster, applicationCredentialMaster, validationList, row);
    }

    /**
     * 엑셀 시트에 동일한 Application Code 중복 체크
     */
    private void checkApplicationCode(String sheetName, String customerInventoryCode,
                                      List<String> applicationCodeList, List<InventoryUploadFail> validationList, int row) {
        if (customerInventoryCode != null && applicationCodeList.contains(customerInventoryCode)) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Application Code");
            inventoryUploadFail.setFailDetail("Application Code cannot be duplicated.");
            validationList.add(inventoryUploadFail);
        } else {
            applicationCodeList.add(customerInventoryCode);
        }
    }

    /**
     * 애플리케이션 중복 체크
     */
    private void checkApplicationDuplicate(String sheetName, InventoryMaster serverInventoryMaster,
                                           InventoryMaster applicationInventoryMaster, ApplicationMaster applicationMaster,
                                           List<InventoryUploadFail> validationList, int row) {
        InventoryMaster application = inventoryMasterRepository
                .findByProjectIdAndInventoryIdAndInventoryTypeCode(applicationInventoryMaster.getProjectId(), applicationInventoryMaster.getInventoryId(), Domain1001.APP.name());

        InventoryMaster serverInventory = inventoryMasterRepository
                .findByCustomerInventoryCodeAndProjectId(serverInventoryMaster.getCustomerInventoryCode(), serverInventoryMaster.getProjectId());

        if (application == null && applicationInventoryMaster.getInventoryId() != null) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Application ID");
            inventoryUploadFail.setFailDetail("Application ID(" + applicationInventoryMaster.getInventoryId() + ") does not exist in this project.");
            validationList.add(inventoryUploadFail);
        }

        if (application != null) {
            // 이미 등록된 애플리케이션
            // 애플리케이션 등록 중복 체크 로직
            List<Long> applicationIds = applicationMapper
                    .selectDuplicatedApplication(application.getProjectId(), serverInventory.getInventoryId(), applicationMaster.getDeployPath());

            if (applicationIds != null && applicationIds.size() > 0) {
                if (applicationIds.size() != 1 || !applicationIds.get(0).equals(application.getInventoryId())) {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                    inventoryUploadFail.setSheet(sheetName);
                    inventoryUploadFail.setRowNumber(row);
                    inventoryUploadFail.setColumnNumber("Application");
                    inventoryUploadFail.setFailDetail("Application(" + applicationMaster.getDeployPath() + ") cannot be duplicated.");
                    validationList.add(inventoryUploadFail);
                }
            }
        } else {
            // 신규 등록 건
            serverInventory = inventoryMasterRepository
                    .findByCustomerInventoryCodeAndProjectId(serverInventoryMaster.getCustomerInventoryCode(), serverInventoryMaster.getProjectId());
            // 애플리케이션 중복 체크 로직
            if (serverInventory != null) {
                List<Long> applicationIds = applicationMapper
                        .selectDuplicatedApplication(applicationInventoryMaster.getProjectId(), serverInventory.getInventoryId(), applicationMaster.getDeployPath());

                if (applicationIds != null && applicationIds.size() > 0) {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                    inventoryUploadFail.setSheet(sheetName);
                    inventoryUploadFail.setRowNumber(row);
                    inventoryUploadFail.setColumnNumber("Application");
                    inventoryUploadFail.setFailDetail("Application(" + applicationMaster.getDeployPath() + ") cannot be duplicated.");
                    validationList.add(inventoryUploadFail);
                }
            }
        }
    }

    /**
     * 전용 인증 정보 사용 여부가 'Y'인 경우 userName과 userPassword 체크
     */
    private void checkDedicatedAuthentication(String sheetName, ApplicationMaster applicationMaster,
                                              CredentialMaster applicationCredentialMaster, List<InventoryUploadFail> validationList, int row) {
        if (Domain101.Y.name().equals(applicationMaster.getDedicatedAuthenticationYn())) {
            if (StringUtils.isEmpty(applicationCredentialMaster.getUserName())) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Application");
                inventoryUploadFail.setFailDetail("'Dedicated Authentication Y/N' If the value is 'Y', the Username cannot be blank.");
                validationList.add(inventoryUploadFail);
            }

            validatePasswordAndKeyFiles(sheetName, applicationCredentialMaster, validationList, row);
        }
    }
}
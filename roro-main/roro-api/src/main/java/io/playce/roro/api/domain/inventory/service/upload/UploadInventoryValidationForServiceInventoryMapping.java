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

import io.playce.roro.common.dto.inventory.inventory.InventoryUploadFail;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.entity.ServiceMaster;
import io.playce.roro.jpa.repository.InventoryMasterRepository;
import io.playce.roro.jpa.repository.ServiceMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class UploadInventoryValidationForServiceInventoryMapping {

    private final ServiceMasterRepository serviceMasterRepository;
    private final InventoryMasterRepository inventoryMasterRepository;

    public void validationServiceInventoryMapping(String sheetName, ServiceMaster mappingServiceMaster,
                                                  InventoryMaster mappingInventoryMaster, List<String> serviceCodeList,
                                                  List<String> inventoryCodeList, List<InventoryUploadFail> validationList, int row) {
        /* Service Inventory Mapping Validations */
        checkValidServiceCode(sheetName, mappingServiceMaster, serviceCodeList, validationList, row);
        checkValidInventoryCode(sheetName, mappingInventoryMaster, inventoryCodeList, validationList, row);
    }

    /**
     * 엑셀 시트에 기입되어져 있거나 DB 등록된 서비스 코드가 유효한 값인지 체크
     */
    private void checkValidServiceCode(String sheetName, ServiceMaster mappingServiceMaster,
                                       List<String> serviceCodeList, List<InventoryUploadFail> validationList, int row) {
        boolean isExist = false;

        if (!serviceCodeList.isEmpty() || mappingServiceMaster.getCustomerServiceCode() != null) {
            if (serviceCodeList.contains(mappingServiceMaster.getCustomerServiceCode())) {
                isExist = true;
            } else {
                ServiceMaster serviceMaster = serviceMasterRepository
                        .findByCustomerServiceCodeAndProjectId(mappingServiceMaster.getCustomerServiceCode(), mappingServiceMaster.getProjectId());
                if (serviceMaster != null) {
                    isExist = true;
                }
            }
        }

        if (!isExist) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Service Code");
            inventoryUploadFail.setFailDetail("Service Code does not exist.");
            validationList.add(inventoryUploadFail);
        }
    }

    /**
     * 엑셀 시트에 기입되어져 있거나 DB 등록된 인벤토리 코드가 유효한 값인지 체크
     */
    private void checkValidInventoryCode(String sheetName, InventoryMaster mappingInventoryMaster,
                                         List<String> inventoryCodeList, List<InventoryUploadFail> validationList, int row) {
        boolean isExist = false;

        if (!inventoryCodeList.isEmpty() || mappingInventoryMaster.getCustomerInventoryCode() != null) {
            String customerInventoryCode = mappingInventoryMaster.getCustomerInventoryCode();
            if (inventoryCodeList.contains(customerInventoryCode)) {
                isExist = true;
            } else {
                InventoryMaster serverInventoryMaster = inventoryMasterRepository.findByCustomerInventoryCodeAndProjectId(customerInventoryCode, mappingInventoryMaster.getProjectId());
                if (serverInventoryMaster != null) {
                    isExist = true;
                }
            }
        }

        if (!isExist) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Inventory Code");
            inventoryUploadFail.setFailDetail("Inventory Code does not exist.");
            validationList.add(inventoryUploadFail);
        }
    }
}
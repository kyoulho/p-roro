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

import io.playce.roro.api.domain.inventory.service.helper.ServiceHelper;
import io.playce.roro.common.dto.inventory.inventory.InventoryUploadFail;
import io.playce.roro.jpa.entity.ServiceMaster;
import io.playce.roro.jpa.repository.ServiceMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.playce.roro.api.common.CommonConstants.*;

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
public class UploadInventoryValidationForService {

    private final ServiceMasterRepository serviceMasterRepository;
    private final ServiceHelper serviceHelper;

    public void validateServiceInventory(String sheetName, ServiceMaster serviceMaster,
                                         List<String> serviceCodeList, List<InventoryUploadFail> validationList, int row) {
        /* Service Validations */
        checkServiceCode(sheetName, serviceMaster.getCustomerServiceCode(), serviceCodeList, validationList, row);
        checkDefaultService(sheetName, serviceMaster, validationList, row);
        checkServiceDuplicate(sheetName, serviceMaster, validationList, row);
        checkDuplicateServiceCustomerCode(sheetName, serviceMaster, validationList, row);
    }

    /**
     * 엑셀 시트에 동일한 Service Code 중복 체크
     */
    private void checkServiceCode(String sheetName, String customerServiceCode,
                                  List<String> serviceCodeList, List<InventoryUploadFail> validationList, int row) {
        if (customerServiceCode != null && serviceCodeList.contains(customerServiceCode)) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheetName);
            inventoryUploadFail.setRowNumber(row);
            inventoryUploadFail.setColumnNumber("Service Code");
            inventoryUploadFail.setFailDetail("Service Code cannot be duplicated.");
            validationList.add(inventoryUploadFail);
        } else {
            serviceCodeList.add(customerServiceCode);
        }
    }

    /**
     * Default 서비스 수정 여부 체크
     */
    private void checkDefaultService(String sheetName, ServiceMaster serviceMaster, List<InventoryUploadFail> validationList, int row) {
        ServiceMaster service = serviceMasterRepository
                .findByProjectIdAndServiceId(serviceMaster.getProjectId(), serviceMaster.getServiceId());

        if (service != null) {
            // 신규가 아닌 서비스 업데이트일 경우 Default 서비스인지 체크한다.
            if (DEFAULT_SERVICE_NAME.equals(service.getServiceName()) &&
                    DEFAULT_SERVICE_BUSINESS_CATEGORY_CODE.equals(service.getBusinessCategoryCode()) &&
                    DEFAULT_SERVICE_BUSINESS_CATEGORY_NAME.equals(service.getBusinessCategoryName()) &&
                    DEFAULT_SERVICE_CUSTOMER_SERVICE_CODE.equals(service.getCustomerServiceCode())) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Service Code");
                inventoryUploadFail.setFailDetail("Default Service cannot be update.");
                validationList.add(inventoryUploadFail);
            }
        } else {
            if (serviceMaster.getServiceId() != null) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Service ID");
                inventoryUploadFail.setFailDetail("Service ID(" + serviceMaster.getServiceId() + ") does not exist in this project.");
                validationList.add(inventoryUploadFail);
            }
        }
    }

    /**
     * 서비스 네임 중복 체크
     */
    private void checkServiceDuplicate(String sheetName, ServiceMaster serviceMaster, List<InventoryUploadFail> validationList, int row) {
        ServiceMaster service = serviceMasterRepository
                .findByProjectIdAndServiceId(serviceMaster.getProjectId(), serviceMaster.getServiceId());

        if (service != null) {
            // 이미 등록된 서비스
            if (serviceHelper.isDuplicateName(service.getProjectId(), service.getServiceId(), service.getServiceName())) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Service Name");
                inventoryUploadFail.setFailDetail("Service Name '" + service.getServiceName() + "' cannot be duplicated.");
                validationList.add(inventoryUploadFail);
            }
        } else {
            // 신규 등록 건
            if (serviceHelper.isDuplicateName(serviceMaster.getProjectId(), null, serviceMaster.getServiceName())) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Service Name");
                inventoryUploadFail.setFailDetail("Service Name '" + serviceMaster.getServiceName() + "' cannot be duplicated.");
                validationList.add(inventoryUploadFail);
            }
        }
    }

    /**
     * 서비스 customer code 중복 체크
     */
    private void checkDuplicateServiceCustomerCode(String sheetName, ServiceMaster serviceMaster, List<InventoryUploadFail> validationList, int row) {
        Long projectId = serviceMaster.getProjectId();
        String customerServiceCode = serviceMaster.getCustomerServiceCode();

        if (serviceMaster.getServiceId() == null) {
            // 신규
            if (serviceHelper.isDuplicateCustomerServiceCode(projectId, null, customerServiceCode)) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Service Code");
                inventoryUploadFail.setFailDetail("Inventory code '" + customerServiceCode + "' is already exist.");
                validationList.add(inventoryUploadFail);
            }
        } else {
            // 업데이트
            if (serviceHelper.isDuplicateCustomerServiceCode(projectId, serviceMaster.getServiceId(), customerServiceCode)) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Service Code");
                inventoryUploadFail.setFailDetail("Inventory code '" + customerServiceCode + "' is already exist.");
                validationList.add(inventoryUploadFail);
            }
        }
    }
}
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
package io.playce.roro.api.domain.inventory.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.domain.common.aop.SubscriptionManager;
import io.playce.roro.api.domain.inventory.service.helper.InventoryUploadExcelHelper;
import io.playce.roro.common.code.Domain1009;
import io.playce.roro.common.dto.inventory.inventory.*;
import io.playce.roro.common.dto.subscription.Subscription;
import io.playce.roro.excel.template.config.ExcelTemplateConfig;
import io.playce.roro.excel.template.vo.RecordMap;
import io.playce.roro.excel.template.vo.SheetMap;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.entity.ServiceInventory;
import io.playce.roro.jpa.entity.UploadInventory;
import io.playce.roro.jpa.repository.InventoryMasterRepository;
import io.playce.roro.jpa.repository.ServiceInventoryRepository;
import io.playce.roro.jpa.repository.UploadInventoryRepository;
import io.playce.roro.mybatis.domain.inventory.inventory.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.playce.roro.api.common.CommonConstants.UPLOAD_STATUS_TYPE_CODE_FAIL;
import static io.playce.roro.api.common.CommonConstants.UPLOAD_STATUS_TYPE_CODE_SUCCESS;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final ServiceInventoryRepository serviceInventoryRepository;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final UploadInventoryRepository uploadInventoryRepository;
    private final ExcelTemplateConfig excelTemplateConfig;

    private final InventoryUploadExcelHelper inventoryUploadExcelHelper;
    private final InventoryMapper inventoryMapper;

    public UploadInventoryResponse getInventory(long projectId, long uploadInventoryId) {
        Map<String, Long> map = new HashMap<>();
        map.put("projectId", projectId);
        map.put("uploadInventoryId", uploadInventoryId);

        return inventoryMapper.selectInventory(map);
    }

    public List<InventoryResponse> getUploadInventoryList(long projectId) {
        return inventoryMapper.selectInventoryList(projectId);
    }

    public InventoryCountResponse getInventoryCount(long projectId, String inventoryTypeCode) {
        InventoryCountResponse response;

        Map<String, Object> map = new HashMap<>();
        map.put("projectId", projectId);
        map.put("inventoryTypeCode", inventoryTypeCode);

        if (inventoryTypeCode.equals(Domain1009.DBMS.name())) {
            response = inventoryMapper.selectInventoryDatabaseCount(map);
        } else {
            response = inventoryMapper.selectInventoryCount(map);
        }

        if (response == null) {
            response = new InventoryCountResponse();
            response.setInventoryTypeCode(inventoryTypeCode);
        }

        return response;
    }

    /**
     * validation check.
     */
    public List<InventoryUploadFail> validateInventoryUpload(SheetMap result, List<InventoryUploadFail> validationList, Long projectId) {
        List<String> inventoryCodeList = new ArrayList<>();
        List<String> serviceCodeList = new ArrayList<>();
        List<ServiceInventoryMapping> serviceInventoryMappingList = new ArrayList<>();
        int newUploadServerCount = 0;

        for (String sheetName : excelTemplateConfig.getSheets().keySet()) {
            log.debug("sheet name: {}", sheetName);
            List<RecordMap> sheet = result.getSheet(sheetName);
            newUploadServerCount += inventoryUploadExcelHelper.setValidateEntity(sheetName, sheet, validationList, serviceCodeList, inventoryCodeList, serviceInventoryMappingList, projectId);
        }

        // check subscription
        checkSubscription(validationList, newUploadServerCount);

        // check service-mapping
        checkServiceInventoryMapping(serviceCodeList, inventoryCodeList, serviceInventoryMappingList, validationList);

        return validationList;
    }

    /**
     * upload inventory.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized List<InventoryUploadSuccess> uploadInventory(SheetMap result, Long projectId, UploadInventory uploadInventory) {
        List<InventoryUploadSuccess> inventoryUploadSuccessList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        List<ServiceInventory> serviceInventoryMapping = new ArrayList<>();

        String sheetNm = null;
        try {
            for (String sheetName : excelTemplateConfig.getSheets().keySet()) {
                sheetNm = sheetName;
                List<RecordMap> sheet = result.getSheet(sheetName);
                inventoryUploadExcelHelper.uploadInventory(projectId, sheetName, sheet, resultMap, serviceInventoryMapping);
            }

            // save service inventory mapping
            saveServiceInventory(projectId, serviceInventoryMapping);

            // for (String key : resultMap.keySet()) {
            //     InventoryUploadSuccess inventoryUploadSuccess = (InventoryUploadSuccess) resultMap.get(key);
            //     inventoryUploadSuccessList.add(inventoryUploadSuccess);
            //
            //     if ("service".equals(inventoryUploadSuccess.getSheet())) {
            //         uploadInventory.setServiceCount(inventoryUploadSuccess.getTotalCount());
            //     } else if ("server".equals(inventoryUploadSuccess.getSheet())) {
            //         uploadInventory.setServerCount(inventoryUploadSuccess.getTotalCount());
            //     } else if ("middleware".equals(inventoryUploadSuccess.getSheet())) {
            //         uploadInventory.setMiddlewareCount(inventoryUploadSuccess.getTotalCount());
            //     } else if ("application".equals(inventoryUploadSuccess.getSheet())) {
            //         uploadInventory.setApplicationCount(inventoryUploadSuccess.getTotalCount());
            //     } else if ("database".equals(inventoryUploadSuccess.getSheet())) {
            //         uploadInventory.setDbmsCount(inventoryUploadSuccess.getTotalCount());
            //     }
            // }

            // https://cloud-osci.atlassian.net/browse/ROROQA-790
            if (!resultMap.isEmpty()) {
                InventoryUploadSuccess inventoryUploadSuccess;

                if (resultMap.get("service") != null) {
                    inventoryUploadSuccess = (InventoryUploadSuccess) resultMap.get("service");
                    uploadInventory.setServiceCount(inventoryUploadSuccess.getTotalCount());
                    inventoryUploadSuccessList.add(inventoryUploadSuccess);
                }

                if (resultMap.get("server") != null) {
                    inventoryUploadSuccess = (InventoryUploadSuccess) resultMap.get("server");
                    uploadInventory.setServerCount(inventoryUploadSuccess.getTotalCount());
                    inventoryUploadSuccessList.add(inventoryUploadSuccess);
                }

                if (resultMap.get("middleware") != null) {
                    inventoryUploadSuccess = (InventoryUploadSuccess) resultMap.get("middleware");
                    uploadInventory.setMiddlewareCount(inventoryUploadSuccess.getTotalCount());
                    inventoryUploadSuccessList.add(inventoryUploadSuccess);
                }

                if (resultMap.get("application") != null) {
                    inventoryUploadSuccess = (InventoryUploadSuccess) resultMap.get("application");
                    uploadInventory.setApplicationCount(inventoryUploadSuccess.getTotalCount());
                    inventoryUploadSuccessList.add(inventoryUploadSuccess);
                }

                if (resultMap.get("database") != null) {
                    inventoryUploadSuccess = (InventoryUploadSuccess) resultMap.get("database");
                    uploadInventory.setDbmsCount(inventoryUploadSuccess.getTotalCount());
                    inventoryUploadSuccessList.add(inventoryUploadSuccess);
                }
            }

            // save success upload inventory
            uploadInventory.setUploadStatusTypeCode(UPLOAD_STATUS_TYPE_CODE_SUCCESS);
            uploadInventory.setUploadProcessResultTxt("service " + uploadInventory.getServiceCount() + ", server " + uploadInventory.getServerCount() +
                    ", middleware " + uploadInventory.getMiddlewareCount() + ", application " + uploadInventory.getApplicationCount() +
                    ", database " + uploadInventory.getDbmsCount());

            uploadInventoryRepository.save(uploadInventory);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while upload inventory template.", e);

            if ("service".equalsIgnoreCase(sheetNm)) {
                uploadInventory.setServiceCount(-1);
            }
            if ("server".equalsIgnoreCase(sheetNm)) {
                uploadInventory.setServerCount(-1);
            }
            if ("middleware".equalsIgnoreCase(sheetNm)) {
                uploadInventory.setMiddlewareCount(-1);
            }
            if ("application".equalsIgnoreCase(sheetNm)) {
                uploadInventory.setApplicationCount(-1);
            }
            if ("database".equalsIgnoreCase(sheetNm)) {
                uploadInventory.setDbmsCount(-1);
            }
            uploadInventory.setUploadStatusTypeCode(UPLOAD_STATUS_TYPE_CODE_FAIL);
            uploadInventory.setUploadProcessResultTxt(getCausedException(e).getMessage());

            throw e;
        }

        return inventoryUploadSuccessList;
    }

    /**
     * Service 와 Inventory 정보 Mapping
     */
    private void saveServiceInventory(Long projectId, List<ServiceInventory> serviceInventoryMapping) {
        // 인벤토리 기준으로 서비스 매핑 정보 삭제
        for (ServiceInventory serviceInventory : serviceInventoryMapping) {
            serviceInventoryRepository.deleteByInventoryId(serviceInventory.getInventoryId());
        }
        serviceInventoryRepository.flush();

        // 신규 매핑 정보 저장
        serviceInventoryRepository.saveAll(serviceInventoryMapping);
    }

    /**
     * Check Subscription.
     */
    private void checkSubscription(List<InventoryUploadFail> validationList, int newUploadServerCount) {
        InventoryUploadFail inventoryUploadFail;
        Subscription subscription = SubscriptionManager.getSubscription();
        int usedServerCount = subscription.getUsedCount();

        if (SubscriptionManager.getSubscription().getCount() < usedServerCount + newUploadServerCount) {
            inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet("server");
            inventoryUploadFail.setRowNumber(0);
            inventoryUploadFail.setColumnNumber("servers");
            inventoryUploadFail.setFailDetail("The current subscription can not register more than " + SubscriptionManager.getSubscription().getCount() + " servers. [current : " + usedServerCount + ", upload : " + newUploadServerCount + "]");
            validationList.add(inventoryUploadFail);
        }
    }

    /**
     * <pre>
     * 인벤토리 생성/수정 시 customerServiceCode에 대한 중복 체크 수행
     * </pre>
     */
    public synchronized InventoryMaster saveInventoryMaster(InventoryMaster inventoryMaster) {
        Long projectId = inventoryMaster.getProjectId();
        Long inventoryId = inventoryMaster.getInventoryId();
        String customerInventoryCode = inventoryMaster.getCustomerInventoryCode();

        List<InventoryMaster> inventoryMasterList = inventoryMasterRepository.findByProjectIdAndCustomerInventoryCode(projectId, customerInventoryCode);

        if (inventoryMasterList.size() == 1) {
            if (inventoryId != null) {
                if (!inventoryMasterList.get(0).getInventoryId().equals(inventoryId)) {
                    throw new RoRoApiException(ErrorCode.INVENTORY_CUSTOMER_CODE_DUPLICATE, customerInventoryCode);
                }
            } else {
                throw new RoRoApiException(ErrorCode.INVENTORY_CUSTOMER_CODE_DUPLICATE, customerInventoryCode);
            }
        } else if (inventoryMasterList.size() > 1) {
            throw new RoRoApiException(ErrorCode.INVENTORY_CUSTOMER_CODE_DUPLICATE, customerInventoryCode);
        }

        return inventoryMasterRepository.save(inventoryMaster);
    }

    @Transactional
    public void setInventoryAnalysisYn(Long inventoryId, String assessmentEanbled) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.getById(inventoryId);
        inventoryMaster.setInventoryAnalysisYn(assessmentEanbled);
    }

    /**
     * Service-Mapping 테이블의 데이터가 매핑되어 있는지 또는 유효한지 체크
     */
    private void checkServiceInventoryMapping(List<String> serviceCodeList, List<String> inventoryCodeList,
                                              List<ServiceInventoryMapping> serviceInventoryMappingList, List<InventoryUploadFail> validationList) {
        // inventory + service가 1개라도 있고, service-mapping 테이블의 데이터가 없는 경우 validation check
        if ((inventoryCodeList.size() + serviceCodeList.size()) > 0 && serviceInventoryMappingList.size() == 0) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet("service-mapping");
            inventoryUploadFail.setRowNumber(0);
            inventoryUploadFail.setColumnNumber("InventoryType, Service Code, Inventory Code");
            inventoryUploadFail.setFailDetail("When inventory(Server,Middleware,Application,Database) exists, service-mapping data cannot be empty.");
            validationList.add(inventoryUploadFail);
        }

        /*
        // 서비스는 존재하지만 하위의 인벤토리는 존재하지 않을 수 있음.
        if (serviceCodeList.size() > 0) {
            for (String customerServiceCode : serviceCodeList) {
                if (!serviceInventoryMappingList.stream()
                        .map(ServiceInventoryMapping::getCustomerServiceCode).collect(Collectors.toList()).contains(customerServiceCode)) {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                    inventoryUploadFail.setSheet("service-mapping");
                    inventoryUploadFail.setRowNumber(0);
                    inventoryUploadFail.setColumnNumber("Service Code");
                    inventoryUploadFail.setFailDetail("When Service Sheet data exists, service-mapping Service Code cannot be empty.");
                    validationList.add(inventoryUploadFail);
                }
            }
        }
        //*/

        if (inventoryCodeList.size() > 0) {
            for (String customerInventoryCode : inventoryCodeList) {
                if (!serviceInventoryMappingList.stream()
                        .map(ServiceInventoryMapping::getCustomerInventoryCode).collect(Collectors.toList()).contains(customerInventoryCode)) {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                    inventoryUploadFail.setSheet("service-mapping");
                    inventoryUploadFail.setRowNumber(0);
                    inventoryUploadFail.setColumnNumber("Inventory Code");
                    inventoryUploadFail.setFailDetail("When Inventory(Server,Middleware,Application,Database) Sheet data exists, service-mapping Inventory Code cannot be empty.");
                    validationList.add(inventoryUploadFail);
                }
            }
        }
    }

    private Throwable getCausedException(Throwable e) {
        if (e.getCause() != null && e.getCause() instanceof Exception) {
            return getCausedException(e.getCause());
        }

        return e;
    }
}
//end of InventoryService.java
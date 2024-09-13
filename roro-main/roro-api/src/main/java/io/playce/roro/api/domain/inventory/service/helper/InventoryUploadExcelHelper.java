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
 * Jaeeon Bae       12월 09, 2021            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.DateTimeUtils;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.api.domain.inventory.service.upload.*;
import io.playce.roro.common.code.*;
import io.playce.roro.common.dto.inventory.database.DatabaseRequest;
import io.playce.roro.common.dto.inventory.inventory.InventoryUploadFail;
import io.playce.roro.common.dto.inventory.inventory.InventoryUploadSuccess;
import io.playce.roro.common.dto.inventory.inventory.ServiceInventoryMapping;
import io.playce.roro.common.dto.inventory.process.InventoryProcessRequest;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.excel.template.vo.RecordMap;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.discovered.DiscoveredInstanceMapper;
import io.playce.roro.mybatis.domain.inventory.database.DatabaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.playce.roro.api.common.CommonConstants.EXCEL_PW_MSG;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryUploadExcelHelper {
    // TODO 데이터 save 할때 각 code들이 하드코딩 되어져 있는데, 내용 확인 후에 수정 필요

    private final ModelMapper modelMapper;

    private final InventoryProcessService inventoryProcessService;

    private final UploadInventoryValidationForService uploadInventoryValidationForService;
    private final UploadInventoryValidationForServer uploadInventoryValidationForServer;
    private final UploadInventoryValidationForMiddleware uploadInventoryValidationForMiddleware;
    private final UploadInventoryValidationForApplication uploadInventoryValidationForApplication;
    private final UploadInventoryValidationForDatabase uploadInventoryValidationForDatabase;
    private final UploadInventoryValidationForServiceInventoryMapping uploadInventoryValidationForServiceServerMapping;

    private final DatabaseMapper databaseMapper;
    private final DiscoveredInstanceMapper discoveredInstanceMapper;

    private final ServiceMasterRepository serviceMasterRepository;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final ServerMasterRepository serverMasterRepository;
    private final CredentialMasterRepository credentialMasterRepository;
    private final MiddlewareMasterRepository middlewareMasterRepository;
    private final ApplicationMasterRepository applicationMasterRepository;
    private final DatabaseMasterRepository databaseMasterRepository;
    private final ServiceInventoryRepository serviceInventoryRepository;
    private final DiscoveredInstanceMasterRepository discoveredInstanceMasterRepository;
    private final LabelMasterRepository labelMasterRepository;
    private final ServiceLabelRepository serviceLabelRepository;
    private final InventoryLabelRepository inventoryLabelRepository;

    /**
     * 필수 값에 대한 validation은 선행 작업이 되어서 체크가 되며 그 외의 validation은
     * Excel의 필드를 ModelMapper를 통해 JPA entity로 변환 후 로직을 통해 체크.
     */
    public int setValidateEntity(String sheetName, List<RecordMap> sheet, List<InventoryUploadFail> validationList,
                                 List<String> serviceCodeList, List<String> inventoryCodeList, List<ServiceInventoryMapping> serviceInventoryMappingList, Long projectId) {
        Map<String, List<Integer>> ipPorts = new HashMap<>();
        int row = 2;
        int newServerCount = 0;

        if (sheet != null && !sheet.isEmpty()) {
            for (RecordMap record : sheet) {

                Map<String, Object> valueMap = record.getValueMap();
                for (String key : valueMap.keySet()) {
                    Object value = valueMap.get(key);
                    if (key.contains("Date") && value != null && value.equals("")) {
                        valueMap.put(key, null);
                    }
                }

                switch (sheetName.trim()) {
                    case "service":
                        log.debug("0 - {}", record.getValueMap());
                        ServiceMaster serviceMaster = modelMapper.map(record.getValueMap(), ServiceMaster.class);
                        serviceMaster.setProjectId(projectId);
                        log.debug("1 - {}", serviceMaster);

                        // check service validation
                        uploadInventoryValidationForService.validateServiceInventory(sheetName, serviceMaster, serviceCodeList, validationList, ++row);
                        break;
                    case "server":
                        log.debug("0 - {}", record.getValueMap());
                        InventoryMaster serverInventoryMaster = modelMapper.map(record.getValueMap(), InventoryMaster.class);
                        log.debug("1 - {}", serverInventoryMaster);
                        ServerMaster serverMaster = modelMapper.map(record.getValueMap(), ServerMaster.class);
                        generateMonitoringDate(record.getValueMap(), serverMaster);

                        if (StringUtils.isNotEmpty(serverMaster.getBuyDate())) {
                            serverMaster.setBuyDate(serverMaster.getBuyDate().replaceAll("-", StringUtils.EMPTY));
                        }

                        serverMaster.setMigrationTypeCode(InventoryUploadCommonCodeHelper.getMigrationTypeCode(serverMaster.getMigrationTypeCode()));
                        serverMaster.setServerUsageTypeCode(InventoryUploadCommonCodeHelper.getServerUsageTypeCode(serverMaster.getServerUsageTypeCode()));
                        serverMaster.setHypervisorTypeCode(InventoryUploadCommonCodeHelper.getHypervisorTypeCode(serverMaster.getHypervisorTypeCode()));
                        serverMaster.setDualizationTypeCode(InventoryUploadCommonCodeHelper.getDualizationTypeCode(serverMaster.getDualizationTypeCode()));
                        serverInventoryMaster.setProjectId(projectId);
                        log.debug("2 - {}", serverMaster);
                        CredentialMaster credentialMaster = modelMapper.map(record.getValueMap(), CredentialMaster.class);
                        log.debug("3 - {}", credentialMaster);

                        Long inventoryId = serverInventoryMaster.getInventoryId();
                        if (inventoryId == null) {
                            newServerCount++;
                            log.debug("Upload Server Sheet :: new Server count ==> {}", newServerCount);
                        } else {
                            InventoryMaster inventoryMaster = inventoryMasterRepository.findByInventoryIdAndInventoryTypeCode(inventoryId, Domain1001.SVR.name()).orElse(null);
                            if (inventoryMaster == null) {
                                newServerCount++;
                            }
                        }

                        // check server validation
                        uploadInventoryValidationForServer
                                .validationServerInventory(sheetName, serverInventoryMaster, serverMaster, credentialMaster, inventoryCodeList, ipPorts, validationList, ++row);
                        checkDuplicateCustomerCodeInProject(sheetName, serverInventoryMaster, validationList, row);
                        break;
                    case "middleware":
                        log.debug("0 - {}", record.getValueMap());
                        serverInventoryMaster = new InventoryMaster();
                        serverInventoryMaster.setCustomerInventoryCode((String) record.getValueMap().get("customerServerCode"));
                        serverInventoryMaster.setProjectId(projectId);
                        InventoryMaster middlewareInventoryMaster = modelMapper.map(record.getValueMap(), InventoryMaster.class);
                        middlewareInventoryMaster.setProjectId(projectId);
                        log.debug("1 - {}", middlewareInventoryMaster);
                        MiddlewareMaster middlewareMaster = modelMapper.map(record.getValueMap(), MiddlewareMaster.class);
                        log.debug("2 - {}", middlewareMaster);
                        CredentialMaster middlewareCredentialMaster = modelMapper.map(record.getValueMap(), CredentialMaster.class);
                        log.debug("3 - {}", middlewareCredentialMaster);

                        // check middleware validation
                        uploadInventoryValidationForMiddleware
                                .validationMiddlewareInventory(sheetName, serverInventoryMaster, middlewareInventoryMaster, middlewareMaster, middlewareCredentialMaster, inventoryCodeList, validationList, ++row);
                        checkDuplicateCustomerCodeInProject(sheetName, middlewareInventoryMaster, validationList, row);
                        break;
                    case "application":
                        log.debug("0 - {}", record.getValueMap());
                        serverInventoryMaster = new InventoryMaster();
                        serverInventoryMaster.setCustomerInventoryCode((String) record.getValueMap().get("customerServerCode"));
                        serverInventoryMaster.setProjectId(projectId);
                        InventoryMaster applicationInventoryMaster = modelMapper.map(record.getValueMap(), InventoryMaster.class);
                        applicationInventoryMaster.setProjectId(projectId);
                        log.debug("1 - {}", applicationInventoryMaster);
                        ApplicationMaster applicationMaster = modelMapper.map(record.getValueMap(), ApplicationMaster.class);
                        log.debug("2 - {}", applicationMaster);
                        CredentialMaster applicationCredentialMaster = modelMapper.map(record.getValueMap(), CredentialMaster.class);
                        log.debug("3 - {}", applicationCredentialMaster);

                        // check application validation
                        uploadInventoryValidationForApplication
                                .validationApplicationInventory(sheetName, serverInventoryMaster, applicationInventoryMaster, applicationMaster, applicationCredentialMaster, inventoryCodeList, validationList, ++row);
                        checkDuplicateCustomerCodeInProject(sheetName, applicationInventoryMaster, validationList, row);
                        break;
                    case "database":
                        log.debug("0 - {}", record.getValueMap());
                        serverInventoryMaster = new InventoryMaster();
                        serverInventoryMaster.setCustomerInventoryCode((String) record.getValueMap().get("customerServerCode"));
                        serverInventoryMaster.setProjectId(projectId);
                        InventoryMaster databaseInventoryMaster = modelMapper.map(record.getValueMap(), InventoryMaster.class);
                        databaseInventoryMaster.setProjectId(projectId);
                        log.debug("1 - {}", databaseInventoryMaster);
                        DatabaseMaster databaseMaster = modelMapper.map(record.getValueMap(), DatabaseMaster.class);
                        log.debug("2 - {}", databaseMaster);
                        CredentialMaster databaseCredentialMaster = modelMapper.map(record.getValueMap(), CredentialMaster.class);
                        log.debug("3 - {}", databaseCredentialMaster);

                        // check database validation
                        uploadInventoryValidationForDatabase
                                .validationDatabaseInventory(sheetName, serverInventoryMaster, databaseInventoryMaster, databaseMaster, inventoryCodeList, validationList, ++row);
                        checkDuplicateCustomerCodeInProject(sheetName, databaseInventoryMaster, validationList, row);
                        break;
                    case "service-mapping":
                        log.debug("0 - {}", record.getValueMap());
                        ServiceMaster mappingServiceMaster = modelMapper.map(record.getValueMap(), ServiceMaster.class);
                        mappingServiceMaster.setProjectId(projectId);
                        log.debug("1 - {}", mappingServiceMaster);
                        InventoryMaster mappingInventoryMaster = modelMapper.map(record.getValueMap(), InventoryMaster.class);
                        mappingInventoryMaster.setProjectId(projectId);
                        mappingInventoryMaster.setInventoryTypeCode(InventoryUploadCommonCodeHelper.getInventoryTypeCode(mappingInventoryMaster.getInventoryTypeCode()));
                        log.debug("2 - {}", mappingInventoryMaster);

                        // Service inventory mapping 생성
                        ServiceInventoryMapping serviceInventoryMapping = new ServiceInventoryMapping();
                        serviceInventoryMapping.setInventoryTypeCode(mappingInventoryMaster.getInventoryTypeCode());
                        serviceInventoryMapping.setCustomerInventoryCode(mappingInventoryMaster.getCustomerInventoryCode());
                        serviceInventoryMapping.setCustomerServiceCode(mappingServiceMaster.getCustomerServiceCode());
                        serviceInventoryMappingList.add(serviceInventoryMapping);

                        // check service-inventory-mapping validation
                        uploadInventoryValidationForServiceServerMapping
                                .validationServiceInventoryMapping(sheetName, mappingServiceMaster, mappingInventoryMaster, serviceCodeList, inventoryCodeList, validationList, ++row);
                        break;
                    default:
                        break;
                }
            }
        }

        return newServerCount;
    }

    /**
     * Upload Inventory ( Create & Modify )
     */
    @Transactional
    public void uploadInventory(Long projectId, String sheetName, List<RecordMap> sheet,
                                Map<String, Object> resultMap, List<ServiceInventory> serviceInventoryMapping) {
        int newServiceCount = 0, updateServiceCount = 0;
        int newServerCount = 0, updateServerCount = 0;
        int newMiddlewareCount = 0, updateMiddlewareCount = 0;
        int newApplicationCount = 0, updateApplicationCount = 0;
        int newDatabaseCount = 0, updateDatabaseCount = 0;

        modelMapper.getConfiguration().setSkipNullEnabled(true);

        if (sheet != null && !sheet.isEmpty()) {
            for (RecordMap record : sheet) {
                String labels = (String) record.getValueMap().get("labels");

                switch (sheetName.trim()) {
                    case "service":
                        ServiceMaster serviceMaster = modelMapper.map(record.getValueMap(), ServiceMaster.class);
                        serviceMaster.setProjectId(projectId);

                        // service id 와 project id 로 조회
                        ServiceMaster originService = serviceMasterRepository.findByProjectIdAndServiceId(serviceMaster.getProjectId(), serviceMaster.getServiceId());
                        if (originService == null) {
                            newServiceCount++;
                        } else {
                            updateServiceCount++;
                        }
                        generateInventorySuccessResponse(sheetName.trim(), resultMap, newServiceCount, updateServiceCount);

                        saveServiceInventory(serviceMaster, originService, labels);
                        break;
                    case "server":
                        InventoryMaster serverInventoryMaster = modelMapper.map(record.getValueMap(), InventoryMaster.class);
                        serverInventoryMaster.setProjectId(projectId);
                        serverInventoryMaster.setInventoryDetailTypeCode("");
                        ServerMaster serverMaster = modelMapper.map(record.getValueMap(), ServerMaster.class);

                        if (StringUtils.isNotEmpty(serverMaster.getBuyDate())) {
                            serverMaster.setBuyDate(serverMaster.getBuyDate().replaceAll("-", StringUtils.EMPTY));
                        }

                        generateMonitoringDate(record.getValueMap(), serverMaster);
                        serverMaster.setMigrationTypeCode(InventoryUploadCommonCodeHelper.getMigrationTypeCode(serverMaster.getMigrationTypeCode()));
                        serverMaster.setServerUsageTypeCode(InventoryUploadCommonCodeHelper.getServerUsageTypeCode(serverMaster.getServerUsageTypeCode()));
                        serverMaster.setHypervisorTypeCode(InventoryUploadCommonCodeHelper.getHypervisorTypeCode(serverMaster.getHypervisorTypeCode()));
                        serverMaster.setDualizationTypeCode(InventoryUploadCommonCodeHelper.getDualizationTypeCode(serverMaster.getDualizationTypeCode()));
                        CredentialMaster credentialMaster = modelMapper.map(record.getValueMap(), CredentialMaster.class);
                        credentialMaster.setProjectId(projectId);

                        // 윈도우인 경우 monitoringYn은 무조건 N이다.
                        // 추후 윈도우 모니터링을 지원할 경우 삭제한다.
                        // ---------------------------------------------------------------
                        if (serverMaster.getWindowsYn().equals(Domain101.Y.name())) {
                            serverMaster.setMonitoringYn(Domain101.N.name());
                        }
                        // ---------------------------------------------------------------

                        // server inventory id 와 project id 로 조회
                        InventoryMaster originServerInventory = inventoryMasterRepository
                                .findByProjectIdAndInventoryIdAndInventoryTypeCode(serverInventoryMaster.getProjectId(), serverInventoryMaster.getInventoryId(), Domain1001.SVR.name());
                        if (originServerInventory == null) {
                            newServerCount++;
                        } else {
                            updateServerCount++;
                        }
                        generateInventorySuccessResponse(sheetName.trim(), resultMap, newServerCount, updateServerCount);

                        saveServerInventory(serverInventoryMaster, serverMaster, credentialMaster, originServerInventory, labels);
                        break;
                    case "middleware":
                        serverInventoryMaster = new InventoryMaster();
                        serverInventoryMaster.setCustomerInventoryCode((String) record.getValueMap().get("customerServerCode"));
                        serverInventoryMaster.setProjectId(projectId);
                        InventoryMaster middlewareInventoryMaster = modelMapper.map(record.getValueMap(), InventoryMaster.class);
                        middlewareInventoryMaster.setProjectId(projectId);
//                        middlewareInventoryMaster.setInventoryDetailTypeCode(Domain1001.MW.name());
                        MiddlewareMaster middlewareMaster = modelMapper.map(record.getValueMap(), MiddlewareMaster.class);
                        CredentialMaster middlewareCredentialMaster = modelMapper.map(record.getValueMap(), CredentialMaster.class);
                        middlewareCredentialMaster.setProjectId(projectId);

                        // middleware inventory id 와 project id 로 조회
                        InventoryMaster originMiddlewareInventory = inventoryMasterRepository
                                .findByProjectIdAndInventoryIdAndInventoryTypeCode(middlewareInventoryMaster.getProjectId(), middlewareInventoryMaster.getInventoryId(), Domain1001.MW.name());
                        if (originMiddlewareInventory == null) {
                            newMiddlewareCount++;
                        } else {
                            updateMiddlewareCount++;
                        }
                        generateInventorySuccessResponse(sheetName.trim(), resultMap, newMiddlewareCount, updateMiddlewareCount);

                        saveMiddlewareInventory(serverInventoryMaster, middlewareInventoryMaster, middlewareMaster, middlewareCredentialMaster, originMiddlewareInventory, labels);
                        break;
                    case "application":
                        serverInventoryMaster = new InventoryMaster();
                        serverInventoryMaster.setCustomerInventoryCode((String) record.getValueMap().get("customerServerCode"));
                        serverInventoryMaster.setProjectId(projectId);
                        InventoryMaster applicationInventoryMaster = modelMapper.map(record.getValueMap(), InventoryMaster.class);
                        applicationInventoryMaster.setProjectId(projectId);
//                        applicationInventoryMaster.setInventoryDetailTypeCode(Domain1001.APP.name());
                        ApplicationMaster applicationMaster = modelMapper.map(record.getValueMap(), ApplicationMaster.class);
                        CredentialMaster applicationCredentialMaster = modelMapper.map(record.getValueMap(), CredentialMaster.class);
                        applicationCredentialMaster.setProjectId(projectId);

                        // application inventory id 와 project id 로 조회
                        InventoryMaster originApplicationInventory = inventoryMasterRepository
                                .findByProjectIdAndInventoryIdAndInventoryTypeCode(applicationInventoryMaster.getProjectId(), applicationInventoryMaster.getInventoryId(), Domain1001.APP.name());
                        if (originApplicationInventory == null) {
                            newApplicationCount++;
                        } else {
                            updateApplicationCount++;
                        }
                        generateInventorySuccessResponse(sheetName.trim(), resultMap, newApplicationCount, updateApplicationCount);

                        if (StringUtils.isNotEmpty(applicationMaster.getAnalysisLibList())) {
                            applicationMaster.setAnalysisLibList(applicationMaster.getAnalysisLibList().replaceAll("\\s", ""));
                        }
                        if (StringUtils.isNotEmpty(applicationMaster.getAnalysisStringList())) {
                            applicationMaster.setAnalysisStringList(applicationMaster.getAnalysisStringList().replaceAll("\\s", ""));
                        }

                        saveApplicationInventory(serverInventoryMaster, applicationInventoryMaster, applicationMaster, applicationCredentialMaster, originApplicationInventory, labels);
                        break;
                    case "database":
                        serverInventoryMaster = new InventoryMaster();
                        serverInventoryMaster.setCustomerInventoryCode((String) record.getValueMap().get("customerServerCode"));
                        serverInventoryMaster.setProjectId(projectId);
                        InventoryMaster databaseInventoryMaster = modelMapper.map(record.getValueMap(), InventoryMaster.class);
                        databaseInventoryMaster.setProjectId(projectId);
//                        databaseInventoryMaster.setInventoryDetailTypeCode(Domain1001.DBMS.name());
                        DatabaseMaster databaseMaster = modelMapper.map(record.getValueMap(), DatabaseMaster.class);

                        if (databaseMaster.getEngineVersion() == null) {
                            databaseMaster.setEngineVersion("");
                        }
                        if (databaseMaster.getVendor() == null) {
                            databaseMaster.setVendor("");
                        }
                        databaseMaster.setAllScanYn("N");
                        CredentialMaster databaseCredentialMaster = modelMapper.map(record.getValueMap(), CredentialMaster.class);
                        databaseCredentialMaster.setProjectId(projectId);

                        // database inventory id 와 project id 로 조회
                        InventoryMaster originDatabaseInventory = inventoryMasterRepository
                                .findByProjectIdAndInventoryIdAndInventoryTypeCode(databaseInventoryMaster.getProjectId(), databaseInventoryMaster.getInventoryId(), Domain1001.DBMS.name());
                        if (originDatabaseInventory == null) {
                            newDatabaseCount++;
                        } else {
                            updateDatabaseCount++;
                        }
                        generateInventorySuccessResponse(sheetName.trim(), resultMap, newDatabaseCount, updateDatabaseCount);

                        saveDatabaseInventory(serverInventoryMaster, databaseInventoryMaster, databaseMaster, databaseCredentialMaster, originDatabaseInventory, labels);
                        break;
                    case "service-mapping":
                        ServiceMaster mappingServiceMaster = modelMapper.map(record.getValueMap(), ServiceMaster.class);
                        mappingServiceMaster.setProjectId(projectId);
                        InventoryMaster mappingInventoryMaster = modelMapper.map(record.getValueMap(), InventoryMaster.class);
                        mappingInventoryMaster.setProjectId(projectId);
                        mappingInventoryMaster.setInventoryTypeCode(InventoryUploadCommonCodeHelper.getInventoryTypeCode(mappingInventoryMaster.getInventoryTypeCode()));

                        generateMappingServiceInventory(mappingServiceMaster, mappingInventoryMaster, serviceInventoryMapping);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Generate Inventory Success Response.
     */
    private void generateInventorySuccessResponse(String sheetName, Map<String, Object> resultMap, int newCount, int updateCount) {
        if (resultMap.get(sheetName) == null) {
            InventoryUploadSuccess inventoryUploadSuccess = new InventoryUploadSuccess();
            inventoryUploadSuccess.setSheet(sheetName);
            inventoryUploadSuccess.setNewCount(newCount);
            inventoryUploadSuccess.setUpdateCount(updateCount);
            inventoryUploadSuccess.setTotalCount(newCount + updateCount);

            resultMap.put(sheetName, inventoryUploadSuccess);
        } else {
            InventoryUploadSuccess inventoryUploadSuccess = (InventoryUploadSuccess) resultMap.get(sheetName);
            inventoryUploadSuccess.setNewCount(newCount);
            inventoryUploadSuccess.setUpdateCount(updateCount);
            inventoryUploadSuccess.setTotalCount(newCount + updateCount);

            resultMap.put(sheetName, inventoryUploadSuccess);
        }
    }

    /**
     * Save Service Inventory
     */
    private void saveServiceInventory(ServiceMaster serviceMaster, ServiceMaster originService, String labels) {
        if (originService == null) {
            serviceMaster.setRegistDatetime(new Date());
            serviceMaster.setRegistUserId(WebUtil.getUserId());
            serviceMaster.setModifyDatetime(new Date());
            serviceMaster.setModifyUserId(WebUtil.getUserId());
            serviceMaster.setDeleteYn(Domain101.N.name());
            serviceMaster = serviceMasterRepository.save(serviceMaster);
        } else {
            modelMapper.map(serviceMaster, originService);
            originService.setModifyDatetime(new Date());
            originService.setModifyUserId(WebUtil.getUserId());
        }

        setLabels(labels, serviceMaster.getServiceId(), null);
    }

    /**
     * Save Server Inventory
     */
    private void saveServerInventory(InventoryMaster serverInventoryMaster, ServerMaster serverMaster,
                                     CredentialMaster credentialMaster, InventoryMaster originServerInventory, String labels) {
        Long inventoryId;
        if (originServerInventory == null) {
            // credential
            credentialMaster = addNewCredential(credentialMaster);

            // inventory
            serverInventoryMaster.setInventoryAnalysisYn(Domain101.Y.name());
            serverInventoryMaster.setInventoryTypeCode(Domain1001.SVR.name());
            generateDetaulValue(serverInventoryMaster);
            serverInventoryMaster.setCredentialId(credentialMaster.getCredentialId());
            InventoryMaster inventory = inventoryMasterRepository.save(serverInventoryMaster);

            inventoryId = inventory.getInventoryId();

            // server
            serverMaster.setServerInventoryId(inventoryId);
            serverMaster.setAutomaticAnalysisYn(Domain101.Y.name());
            if (StringUtils.isEmpty(serverMaster.getAccessControlSystemSolutionName())) {
                serverMaster.setAccessControlSystemSolutionName("");
            }
            if (StringUtils.isEmpty(serverMaster.getMakerName())) {
                serverMaster.setMakerName("");
            }
            if (StringUtils.isEmpty(serverMaster.getModelName())) {
                serverMaster.setModelName("");
            }
            if (StringUtils.isEmpty(serverMaster.getSerialNumber())) {
                serverMaster.setSerialNumber("");
            }
            if (serverMaster.getTpmc() == null) { // 값이 없는 경우 default 0으로 설정
                serverMaster.setTpmc(0F);
            }
            if (StringUtils.isNotEmpty(serverMaster.getRootPassword())) {
                serverMaster.setRootPassword(GeneralCipherUtil.encrypt(serverMaster.getRootPassword()));
            }
            serverMaster.setScheduledAssessmentYn(Domain101.N.name());

            // discovered instance 체크
            List<Long> existDiscoveredInstanceId = getExistDiscoveredServerInstanceId(inventory.getProjectId(), serverMaster.getRepresentativeIpAddress());
            if (CollectionUtils.isNotEmpty(existDiscoveredInstanceId)) {
                for (Long discoveredId : existDiscoveredInstanceId) {
                    DiscoveredInstanceMaster discoveredInstanceMaster = discoveredInstanceMasterRepository.findById(discoveredId)
                            .orElseThrow(() -> new ResourceNotFoundException("Discovered Instance ID : " + discoveredId + " Not Found."));
                    serverMaster.setDiscoveredServerYn(Domain101.Y.name());

                    discoveredInstanceMaster.setPossessionInventoryId(serverInventoryMaster.getInventoryId());
                    serverInventoryMaster.setInventoryIpTypeCode(Domain1006.DISC.name());
                    serverInventoryMaster.setInventoryDiscoveredDatetime(discoveredInstanceMaster.getRegistDatetime());
                }
            } else {
                serverMaster.setDiscoveredServerYn(Domain101.N.name());
            }
            serverMasterRepository.save(serverMaster);

            // 인벤토리 업로드를 통해 신규로 추가된 서버 자원들 Prerequisite 요청 자동 등록
            List<Long> inventoryIds = new ArrayList<>();
            inventoryIds.add(serverMaster.getServerInventoryId());

            InventoryProcessRequest inventoryProcessRequest = new InventoryProcessRequest();
            inventoryProcessRequest.setInventoryTypeCode(Domain1001.SVR.name());
            inventoryProcessRequest.setInventoryIds(inventoryIds);

            inventoryProcessService.addInventoryProcess(serverInventoryMaster.getProjectId(), inventoryProcessRequest, Domain1002.PREQ);
        } else {
            inventoryId = originServerInventory.getInventoryId();

            updateCredential(credentialMaster, originServerInventory);

            ServerMaster originServer = serverMasterRepository.findById(inventoryId)
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));

            if (StringUtils.isNotEmpty(serverMaster.getRootPassword())) {
                if (EXCEL_PW_MSG.equals(serverMaster.getRootPassword())) {
                    serverMaster.setRootPassword(originServer.getRootPassword());
                } else {
                    serverMaster.setRootPassword(GeneralCipherUtil.encrypt(serverMaster.getRootPassword()));
                }
            }

            modelMapper.map(serverMaster, originServer);

            modelMapper.map(serverInventoryMaster, originServerInventory);
            originServerInventory.setModifyUserId(WebUtil.getUserId());
            originServerInventory.setModifyDatetime(new Date());
        }

        setLabels(labels, null, inventoryId);

        SSHUtil.clearSession(credentialMaster.getUserName() + "@" + serverMaster.getRepresentativeIpAddress() + ":" + serverMaster.getConnectionPort());
    }

    /**
     * Save Middleware Inventory
     */
    private void saveMiddlewareInventory(InventoryMaster serverInventoryMaster, InventoryMaster middlewareInventoryMaster,
                                         MiddlewareMaster middlewareMaster, CredentialMaster middlewareCredentialMaster, InventoryMaster originMiddlewareInventory, String labels) {
        Long inventoryId;
        InventoryMaster serverInventory = inventoryMasterRepository.findByCustomerInventoryCodeAndProjectId(serverInventoryMaster.getCustomerInventoryCode(), middlewareInventoryMaster.getProjectId());
        if (originMiddlewareInventory == null) {
            if (middlewareMaster.getDedicatedAuthenticationYn().equalsIgnoreCase("Y") &&
                    StringUtils.isNotEmpty(middlewareCredentialMaster.getUserName()) &&
                    (StringUtils.isNotEmpty(middlewareCredentialMaster.getUserPassword()) || StringUtils.isNotEmpty(middlewareCredentialMaster.getKeyFileContent()))) {
                // credential
                middlewareCredentialMaster = addNewCredential(middlewareCredentialMaster);
            }

            // inventory
            middlewareInventoryMaster.setServerInventoryId(serverInventory.getInventoryId());
            middlewareInventoryMaster.setInventoryAnalysisYn(Domain101.Y.name());
            middlewareInventoryMaster.setInventoryTypeCode(Domain1001.MW.name());
            generateDetaulValue(middlewareInventoryMaster);

            if (middlewareCredentialMaster.getCredentialId() != null) {
                middlewareInventoryMaster.setCredentialId(middlewareCredentialMaster.getCredentialId());
            }

            if ("WEBSPHERE".equalsIgnoreCase(middlewareInventoryMaster.getInventoryDetailTypeCode())) {
                middlewareInventoryMaster.setInventoryDetailTypeCode(Domain1013.WSPHERE.name());
            }

            InventoryMaster inventory = inventoryMasterRepository.save(middlewareInventoryMaster);

            inventoryId = inventory.getInventoryId();

            // middleware
            middlewareMaster.setMiddlewareInventoryId(inventoryId);
            middlewareMaster.setMiddlewareTypeCode(middlewareMaster.getMiddlewareTypeCode());
            middlewareMaster.setAutomaticRegistProtectionYn(Domain101.N.name());
            middlewareMasterRepository.save(middlewareMaster);
        } else {
            inventoryId = originMiddlewareInventory.getInventoryId();

            if (originMiddlewareInventory.getCredentialId() == null) {
                if (middlewareMaster.getDedicatedAuthenticationYn().equalsIgnoreCase("Y") &&
                        StringUtils.isNotEmpty(middlewareCredentialMaster.getUserName()) &&
                        (StringUtils.isNotEmpty(middlewareCredentialMaster.getUserPassword()) || StringUtils.isNotEmpty(middlewareCredentialMaster.getKeyFileContent()))) {
                    // credential
                    middlewareCredentialMaster = addNewCredential(middlewareCredentialMaster);
                    originMiddlewareInventory.setCredentialId(middlewareCredentialMaster.getCredentialId());
                }
            } else {
                if (middlewareMaster.getDedicatedAuthenticationYn().equalsIgnoreCase("Y") &&
                        StringUtils.isNotEmpty(middlewareCredentialMaster.getUserName()) &&
                        (StringUtils.isNotEmpty(middlewareCredentialMaster.getUserPassword()) || StringUtils.isNotEmpty(middlewareCredentialMaster.getKeyFileContent()))) {
                    // credential
                    updateCredential(middlewareCredentialMaster, originMiddlewareInventory);
                } else {
                    originMiddlewareInventory.setCredentialId(null);
                }
            }

            MiddlewareMaster originMiddleware = middlewareMasterRepository.findById(inventoryId)
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_MIDDLEWARE_NOT_FOUND));
            modelMapper.map(middlewareMaster, originMiddleware);

            modelMapper.map(middlewareInventoryMaster, originMiddlewareInventory);
            originMiddlewareInventory.setModifyUserId(WebUtil.getUserId());
            originMiddlewareInventory.setModifyDatetime(new Date());
        }

        setLabels(labels, null, inventoryId);
    }

    /**
     * Save Application Inventory
     */
    private void saveApplicationInventory(InventoryMaster serverInventoryMaster, InventoryMaster applicationInventoryMaster,
                                          ApplicationMaster applicationMaster, CredentialMaster applicationCredentialMaster, InventoryMaster originApplicationInventory, String labels) {
        Long inventoryId;
        InventoryMaster serverInventory = inventoryMasterRepository.findByCustomerInventoryCodeAndProjectId(serverInventoryMaster.getCustomerInventoryCode(), applicationInventoryMaster.getProjectId());
        if (originApplicationInventory == null) {
            if (applicationMaster.getDedicatedAuthenticationYn().equalsIgnoreCase("Y") &&
                    StringUtils.isNotEmpty(applicationCredentialMaster.getUserName()) &&
                    (StringUtils.isNotEmpty(applicationCredentialMaster.getUserPassword()) || StringUtils.isNotEmpty(applicationCredentialMaster.getKeyFileContent()))) {
                // credential
                applicationCredentialMaster = addNewCredential(applicationCredentialMaster);
            }


            // inventory
            applicationInventoryMaster.setServerInventoryId(serverInventory.getInventoryId());
            applicationInventoryMaster.setInventoryAnalysisYn(Domain101.Y.name());
            applicationInventoryMaster.setInventoryTypeCode(Domain1001.APP.name());
            generateDetaulValue(applicationInventoryMaster);

            if (applicationCredentialMaster.getCredentialId() != null) {
                applicationInventoryMaster.setCredentialId(applicationCredentialMaster.getCredentialId());
            }

            InventoryMaster inventory = inventoryMasterRepository.save(applicationInventoryMaster);

            inventoryId = inventory.getInventoryId();

            // application
            applicationMaster.setApplicationInventoryId(inventoryId);
            applicationMaster.setAutomaticRegistProtectionYn(Domain101.N.name());
            applicationMaster.setApplicationSize(0L);
            applicationMasterRepository.save(applicationMaster);
        } else {
            inventoryId = originApplicationInventory.getInventoryId();

            if (originApplicationInventory.getCredentialId() == null) {
                if (applicationMaster.getDedicatedAuthenticationYn().equalsIgnoreCase("Y") &&
                        StringUtils.isNotEmpty(applicationCredentialMaster.getUserName()) &&
                        (StringUtils.isNotEmpty(applicationCredentialMaster.getUserPassword()) || StringUtils.isNotEmpty(applicationCredentialMaster.getKeyFileContent()))) {
                    // credential
                    applicationCredentialMaster = addNewCredential(applicationCredentialMaster);
                    originApplicationInventory.setCredentialId(applicationCredentialMaster.getCredentialId());
                }
            } else {
                if (applicationMaster.getDedicatedAuthenticationYn().equalsIgnoreCase("Y") &&
                        StringUtils.isNotEmpty(applicationCredentialMaster.getUserName()) &&
                        (StringUtils.isNotEmpty(applicationCredentialMaster.getUserPassword()) || StringUtils.isNotEmpty(applicationCredentialMaster.getKeyFileContent()))) {
                    // credential
                    updateCredential(applicationCredentialMaster, originApplicationInventory);
                } else {
                    originApplicationInventory.setCredentialId(null);
                }
            }

            ApplicationMaster originApplication = applicationMasterRepository.findById(inventoryId)
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_APPLICATION_NOT_FOUND));
            modelMapper.map(applicationInventoryMaster, originApplication);

            modelMapper.map(applicationInventoryMaster, originApplicationInventory);
            originApplicationInventory.setModifyUserId(WebUtil.getUserId());
            originApplicationInventory.setModifyDatetime(new Date());
        }

        setLabels(labels, null, inventoryId);
    }

    /**
     * Save Database Inventory
     */
    private void saveDatabaseInventory(InventoryMaster serverInventoryMaster, InventoryMaster databaseInventoryMaster,
                                       DatabaseMaster databaseMaster, CredentialMaster databaseCredentialMaster, InventoryMaster originDatabaseInventory, String labels) {
        Long inventoryId;
        InventoryMaster serverInventory = inventoryMasterRepository.findByCustomerInventoryCodeAndProjectId(serverInventoryMaster.getCustomerInventoryCode(), databaseInventoryMaster.getProjectId());
        if (originDatabaseInventory == null) {
            // credential
            databaseCredentialMaster = addNewCredential(databaseCredentialMaster);

            // inventory
            databaseInventoryMaster.setServerInventoryId(serverInventory.getInventoryId());
            databaseInventoryMaster.setInventoryAnalysisYn(Domain101.Y.name());
            databaseInventoryMaster.setInventoryTypeCode(Domain1001.DBMS.name());
            generateDetaulValue(databaseInventoryMaster);
            databaseInventoryMaster.setCredentialId(databaseCredentialMaster.getCredentialId());
            InventoryMaster inventory = inventoryMasterRepository.save(databaseInventoryMaster);

            inventoryId = inventory.getInventoryId();

            // database
            databaseMaster.setDatabaseInventoryId(inventoryId);

            // discovered instance 체크
            DatabaseRequest databaseRequest = new DatabaseRequest();
            databaseRequest.setServerInventoryId(serverInventory.getInventoryId());
            databaseRequest.setConnectionPort(databaseMaster.getConnectionPort());
            databaseRequest.setDatabaseServiceName(databaseMaster.getDatabaseServiceName());
            Long discoveredDatabaseInstanceId = databaseMapper.selectDiscoveredDatabaseInstance(inventory.getProjectId(), databaseRequest);
            if (discoveredDatabaseInstanceId != null) {
                DiscoveredInstanceMaster discoveredInstanceMaster = discoveredInstanceMasterRepository.findById(discoveredDatabaseInstanceId)
                        .orElseThrow(() -> new ResourceNotFoundException("Discovered Instance ID : " + discoveredDatabaseInstanceId + " Not Found."));
                databaseMaster.setDiscoveredDatabaseYn(Domain101.Y.name());

                discoveredInstanceMaster.setPossessionInventoryId(databaseInventoryMaster.getInventoryId());
                databaseInventoryMaster.setInventoryDiscoveredDatetime(discoveredInstanceMaster.getRegistDatetime());
            } else {
                databaseMaster.setDiscoveredDatabaseYn(Domain101.N.name());
            }
            databaseMasterRepository.save(databaseMaster);
        } else {
            inventoryId = originDatabaseInventory.getInventoryId();

            updateCredential(databaseCredentialMaster, originDatabaseInventory);

            DatabaseMaster originDatabase = databaseMasterRepository.findById(inventoryId)
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_DATABASE_NOT_FOUND));
            modelMapper.map(databaseMaster, originDatabase);

            modelMapper.map(databaseInventoryMaster, originDatabaseInventory);
            originDatabaseInventory.setModifyUserId(WebUtil.getUserId());
            originDatabaseInventory.setModifyDatetime(new Date());
        }

        setLabels(labels, null, inventoryId);
    }

    /**
     * Mapping Service and Inventory
     */
    private void generateMappingServiceInventory(ServiceMaster mappingServiceMaster, InventoryMaster mappingInventoryMaster, List<ServiceInventory> serviceInventoryMapping) {
        // service-inventory mapping
        ServiceMaster service = serviceMasterRepository.findByCustomerServiceCodeAndProjectId(mappingServiceMaster.getCustomerServiceCode(), mappingServiceMaster.getProjectId());
        InventoryMaster inventory = inventoryMasterRepository.findByCustomerInventoryCodeAndProjectId(mappingInventoryMaster.getCustomerInventoryCode(), mappingInventoryMaster.getProjectId());

        ServiceInventory mappedServiceInventory = serviceInventoryRepository
                .findByServiceIdAndInventoryId(service.getServiceId(), inventory.getInventoryId());

        if (mappedServiceInventory == null) {
            ServiceInventory serviceInventory = new ServiceInventory();
            serviceInventory.setServiceId(service.getServiceId());
            serviceInventory.setInventoryId(inventory.getInventoryId());
//            serviceInventoryRepository.save(serviceInventory);
            serviceInventoryMapping.add(serviceInventory);
        } else {
            serviceInventoryMapping.add(mappedServiceInventory);
        }
    }

    /**
     * generate Excel Date to Java Date
     */
    private void generateMonitoringDate(Map<String, Object> valueMap, ServerMaster serverMaster) {
        Object startDatetime = valueMap.get("monitoringStartDatetime");
        Object endDatetime = valueMap.get("monitoringEndDatetime");

        if (startDatetime != null) {
            if (startDatetime instanceof Double) {
                serverMaster.setMonitoringStartDatetime(DateUtil.getJavaDate((Double) startDatetime));
            } else {
                serverMaster.setMonitoringStartDatetime(DateTimeUtils.convertToDate((String) startDatetime, new SimpleDateFormat(DateTimeUtils.DEFAULT_DATETIME)));
            }
        }

        if (endDatetime != null) {
            if (startDatetime instanceof Double) {
                serverMaster.setMonitoringEndDatetime(DateUtil.getJavaDate((Double) endDatetime));
            } else {
                serverMaster.setMonitoringEndDatetime(DateTimeUtils.convertToDate((String) endDatetime, new SimpleDateFormat(DateTimeUtils.DEFAULT_DATETIME)));
            }
        }
    }

    @NotNull
    private CredentialMaster addNewCredential(CredentialMaster credentialMaster) {
        credentialMaster.setCredentialTypeCode(Domain1001.SVR.name());
        credentialMaster.setDeleteYn(Domain101.N.name());
        credentialMaster.setRegistUserId(WebUtil.getUserId());
        credentialMaster.setRegistDatetime(new Date());
        credentialMaster.setModifyUserId(WebUtil.getUserId());
        credentialMaster.setModifyDatetime(new Date());
        if (StringUtils.isNotEmpty(credentialMaster.getUserPassword())) {
            credentialMaster.setUserPassword(GeneralCipherUtil.encrypt(credentialMaster.getUserPassword()));
            credentialMaster.setKeyFileName(null);
            credentialMaster.setKeyFilePath(null);
            credentialMaster.setKeyFileContent(null);
        }

        if (StringUtils.isNotEmpty(credentialMaster.getKeyFileContent())) {
            try {
                // Do NOT save the key file under the repositoryPath for security reason.
                File keyFileDirectory = new File(CommonProperties.getWorkDir() + File.separator + "keyFiles");
                if (!keyFileDirectory.exists()) {
                    keyFileDirectory.mkdir();
                }
                File tempFile = File.createTempFile("roro-", "-" + credentialMaster.getKeyFileName(), keyFileDirectory);
                String keyStr = credentialMaster.getKeyFileContent();

                IOUtils.write(keyStr, new FileOutputStream(tempFile), "UTF-8");

                credentialMaster.setKeyFileName(tempFile.getName());
                credentialMaster.setKeyFilePath(tempFile.getAbsolutePath());
                credentialMaster.setKeyFileContent(keyStr);
                credentialMaster.setUserPassword(null);
            } catch (IOException e) {
                log.error("Unhandled exception while create a credential file.", e);
            }
        }
        credentialMaster = credentialMasterRepository.save(credentialMaster);
        return credentialMaster;
    }

    private void updateCredential(CredentialMaster credentialMaster, InventoryMaster originInventory) {
        CredentialMaster originCredential = credentialMasterRepository.findById(originInventory.getCredentialId()).orElseThrow(() ->
                new ResourceNotFoundException("Credential ID : " + originInventory.getCredentialId() + " Not Found."));

        if (StringUtils.isNotEmpty(credentialMaster.getUserPassword()) && !EXCEL_PW_MSG.equals(credentialMaster.getUserPassword())) {
            originCredential.setUserPassword(GeneralCipherUtil.encrypt(credentialMaster.getUserPassword()));
            originCredential.setKeyFileName(null);
            originCredential.setKeyFilePath(null);
            originCredential.setKeyFileContent(null);
            originCredential.setModifyUserId(WebUtil.getUserId());
            originCredential.setModifyDatetime(new Date());
        }

        if (StringUtils.isNotEmpty(credentialMaster.getKeyFileContent()) && !EXCEL_PW_MSG.equals(credentialMaster.getKeyFileContent())) {
            try {
                // Do NOT save the key file under the repositoryPath for security reason.
                File keyFileDirectory = new File(CommonProperties.getWorkDir() + File.separator + "keyFiles");
                if (!keyFileDirectory.exists()) {
                    keyFileDirectory.mkdir();
                }
                File tempFile = File.createTempFile("roro-", "-" + credentialMaster.getKeyFileName(), keyFileDirectory);
                String keyStr = credentialMaster.getKeyFileContent();

                IOUtils.write(keyStr, new FileOutputStream(tempFile), "UTF-8");

                originCredential.setKeyFileName(tempFile.getName());
                originCredential.setKeyFilePath(tempFile.getAbsolutePath());
                originCredential.setKeyFileContent(keyStr);
                originCredential.setUserPassword(null);
                originCredential.setModifyUserId(WebUtil.getUserId());
                originCredential.setModifyDatetime(new Date());
            } catch (IOException e) {
                log.error("Unhandled exception while create a credential file.", e);
            }
        }
    }

    private void generateDetaulValue(InventoryMaster inventoryMaster) {
        if (inventoryMaster.getInventoryIpTypeCode() == null) {
            inventoryMaster.setInventoryIpTypeCode(Domain1006.INV.name());
        }
        inventoryMaster.setRegistUserId(WebUtil.getUserId());
        inventoryMaster.setRegistDatetime(new Date());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());
        inventoryMaster.setAutomaticRegistYn(Domain101.N.name());
        inventoryMaster.setDeleteYn(Domain101.N.name());
    }

    private List<Long> getExistDiscoveredServerInstanceId(Long projectId, String representativeIpAddress) {
        return discoveredInstanceMapper.selectAllDiscoveredServer(projectId, representativeIpAddress);
    }

    /**
     * 각 Inventory_Master 테이블의 customer_inventory_code 가 해당 프로젝트에 중복으로 있는지 체크한다.
     */
    private void checkDuplicateCustomerCodeInProject(String sheetName, InventoryMaster inventoryMaster, List<InventoryUploadFail> validationList, int row) {
        Long projectId = inventoryMaster.getProjectId();
        Long inventoryId = inventoryMaster.getInventoryId();
        String customerInventoryCode = inventoryMaster.getCustomerInventoryCode();

        if (inventoryId == null) {
            // 신규
            List<InventoryMaster> inventoryMasterList = inventoryMasterRepository.findByProjectIdAndCustomerInventoryCode(projectId, customerInventoryCode);
            if (inventoryMasterList.size() == 1) {
                if (inventoryId != null) {
                    if (!inventoryMasterList.get(0).getInventoryId().equals(inventoryId)) {
                        InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                        inventoryUploadFail.setSheet(sheetName);
                        inventoryUploadFail.setRowNumber(row);
                        inventoryUploadFail.setColumnNumber("Server Code");
                        inventoryUploadFail.setFailDetail("Inventory code '" + inventoryMaster.getCustomerInventoryCode() + "' is already exist.");
                        validationList.add(inventoryUploadFail);
                    }
                } else {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                    inventoryUploadFail.setSheet(sheetName);
                    inventoryUploadFail.setRowNumber(row);
                    inventoryUploadFail.setColumnNumber("Server Code");
                    inventoryUploadFail.setFailDetail("Inventory code '" + inventoryMaster.getCustomerInventoryCode() + "' is already exist.");
                    validationList.add(inventoryUploadFail);
                }
            } else if (inventoryMasterList.size() > 1) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Server Code");
                inventoryUploadFail.setFailDetail("Inventory code '" + inventoryMaster.getCustomerInventoryCode() + "' is already exist.");
                validationList.add(inventoryUploadFail);
            }
        } else {
            // 업데이트
            InventoryMaster inventory = inventoryMasterRepository.findByProjectIdAndCustomerInventoryCodeAndInventoryIdNot(projectId, customerInventoryCode, inventoryId);
            if (inventory != null) {
                InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                inventoryUploadFail.setSheet(sheetName);
                inventoryUploadFail.setRowNumber(row);
                inventoryUploadFail.setColumnNumber("Server Code");
                inventoryUploadFail.setFailDetail("Inventory code '" + inventoryMaster.getCustomerInventoryCode() + "' is already exist.");
                validationList.add(inventoryUploadFail);
            }
        }
    }

    private void setLabels(String labels, Long serviceId, Long inventoryId) {
        if (StringUtils.isNotEmpty(labels) && (serviceId != null || inventoryId != null)) {
            for (String label : labels.split(",")) {
                LabelMaster labelMaster = labelMasterRepository.findByLabelName(label.trim());

                if (labelMaster == null) {
                    labelMaster = new LabelMaster();
                    labelMaster.setLabelName(label.trim());
                    labelMaster.setRegistUserId(WebUtil.getUserId());
                    labelMaster.setRegistDatetime(new Date());
                    labelMaster.setModifyUserId(WebUtil.getUserId());
                    labelMaster.setModifyDatetime(new Date());

                    labelMaster = labelMasterRepository.save(labelMaster);
                }

                if (serviceId != null) {
                    ServiceLabel serviceLabel = new ServiceLabel();
                    serviceLabel.setLabelId(labelMaster.getLabelId());
                    serviceLabel.setServiceId(serviceId);

                    serviceLabelRepository.save(serviceLabel);
                }

                if (inventoryId != null) {
                    InventoryLabel inventoryLabel = new InventoryLabel();
                    inventoryLabel.setLabelId(labelMaster.getLabelId());
                    inventoryLabel.setInventoryId(inventoryId);

                    inventoryLabelRepository.save(inventoryLabel);
                }
            }
        }
    }
}
//end of InventoryUploadExcelHelper.java

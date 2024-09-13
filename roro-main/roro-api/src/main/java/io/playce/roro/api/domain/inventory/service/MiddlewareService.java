/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Jaeeon Bae       1월 26, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.assessment.service.AssessmentService;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.code.Domain1006;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.dto.common.excel.ListToExcelDto;
import io.playce.roro.common.dto.inventory.database.DeployDatasourceList;
import io.playce.roro.common.dto.inventory.manager.Manager;
import io.playce.roro.common.dto.inventory.middleware.*;
import io.playce.roro.common.dto.inventory.process.InventoryProcess;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.ExcelUtil;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.common.label.LabelMapper;
import io.playce.roro.mybatis.domain.inventory.application.ApplicationMapper;
import io.playce.roro.mybatis.domain.inventory.middleware.MiddlewareMapper;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import io.playce.roro.mybatis.domain.inventory.service.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.playce.roro.common.util.support.DistinctByKey.distinctByKey;

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
public class MiddlewareService {

    private final InventoryService inventoryService;
    private final AssessmentService assessmentService;

    private final InventoryMasterRepository inventoryMasterRepository;
    private final ProjectMasterRepository projectMasterRepository;
    private final MiddlewareMasterRepository middlewareMasterRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ServiceInventoryRepository serviceInventoryRepository;
    private final LabelMasterRepository labelMasterRepository;
    private final InventoryLabelRepository inventoryLabelRepository;
    private final UserMasterRepository userMasterRepository;
    private final InventoryManagerRepository inventoryManagerRepository;
    private final DiscoveredInstanceMasterRepository discoveredInstanceMasterRepository;
    private final CredentialMasterRepository credentialMasterRepository;
    private final InventoryProcessRepository inventoryProcessRepository;
    private final InventoryProcessResultRepository inventoryProcessResultRepository;

    private final ServiceMapper serviceMapper;
    private final MiddlewareMapper middlewareMapper;
    private final LabelMapper labelMapper;
    private final InventoryProcessMapper inventoryProcessMapper;
    private final ApplicationMapper applicationMapper;
    private final ModelMapper modelMapper;

    public List<MiddlewareResponse> getMiddlewares(Long projectId, Long serviceId, Long serverId) {
        // 1. 미들웨어 목록 조회
        List<MiddlewareResponse> middlewareList = middlewareMapper
                .selectMiddlewareList(projectId, serviceId, serverId, Domain1001.MW.name());

        // 2. label & inventory process 설정
        for (MiddlewareResponse mw : middlewareList) {
            setMiddlewareDetail(mw);
        }

        return middlewareList;
    }

    private void setMiddlewareDetail(MiddlewareResponse mw) {
        if (mw != null) {
            // 서비스 설정
            mw.setServices(serviceMapper.getServiceSummaries(mw.getMiddlewareInventoryId()));

            // 라벨 설정
            mw.setLabelList(labelMapper.getInventoryLabelList(mw.getMiddlewareInventoryId()));

            mw.setApplicationCount(getMiddlewareDeployApplicationList(mw.getProjectId(), mw.getMiddlewareInventoryId()).size());
            mw.setDatasourceCount(getMiddlewareDeployDatasourceList(mw.getProjectId(), mw.getMiddlewareInventoryId()).size());

            // 가장 마지막으로 성공한 process 설정
            InventoryProcess.CompleteScan completeScan = inventoryProcessMapper
                    .selectLastCompleteInventoryProcess(mw.getMiddlewareInventoryId(), Domain1002.SCAN.name());
            if (completeScan != null) {
                mw.setLastCompleteScan(completeScan);
            }

            // 마지막 Scan 데이터 설정
            InventoryProcess.Result result = inventoryProcessMapper
                    .selectLastInventoryProcess(mw.getMiddlewareInventoryId(), Domain1002.SCAN.name());
            if (result != null) {
                mw.setLastInventoryProcess(result);
            }
        }
    }
    
    /**
     * https://cloud-osci.atlassian.net/browse/PCR-5593
     * 이중 서브밋 방지를 위한 방어코드로 @Transactional 애노테이션에는 synchronized가 동작하기 않기 때문에
     * 별도의 synchronized 메소드 내에서 @Transactional 메소드를 호출한다.
     */
    public synchronized MiddlewareSimpleResponse createMiddleware(Long projectId, MiddlewareRequest middlewareRequest, MultipartFile keyFile) {
        return createMiddlewareInternal(projectId, middlewareRequest, keyFile);
    }

    @Transactional
    public MiddlewareSimpleResponse createMiddlewareInternal(Long projectId, MiddlewareRequest middlewareRequest, MultipartFile keyFile) {
        ProjectMaster projectMaster = projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));

        // 미들웨어 등록 중복 체크 로직
        List<Long> middlewareIds = middlewareMasterRepository.selectDuplicateMiddlewareInventory(middlewareRequest.getServerInventoryId(),
                middlewareRequest.getEngineInstallPath(), middlewareRequest.getDomainHomePath());

        if (middlewareIds != null && middlewareIds.size() > 0) {
            throw new RoRoApiException(ErrorCode.INVENTORY_MIDDLEWARE_DUPLICATE, middlewareRequest.getEngineInstallPath());
        }

        // Step 1. Save inventory_master table
        InventoryMaster inventoryMaster = new InventoryMaster();

        if (Domain101.Y.name().equals(middlewareRequest.getDedicatedAuthenticationYn())) {
            if (keyFile != null && keyFile.getSize() == 0) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
            }

            CredentialMaster credentialMaster = new CredentialMaster();
            credentialMaster.setProjectId(projectId);
            credentialMaster.setUserName(middlewareRequest.getUserName());
            credentialMaster.setCredentialTypeCode(Domain1001.MW.name());
            credentialMaster.setDeleteYn(Domain101.N.name());
            credentialMaster.setRegistUserId(WebUtil.getUserId());
            credentialMaster.setRegistDatetime(new Date());
            credentialMaster.setModifyUserId(WebUtil.getUserId());
            credentialMaster.setModifyDatetime(new Date());

            if (StringUtils.isNotEmpty(middlewareRequest.getUserPassword())) {
                try {
                    String password = GeneralCipherUtil.decrypt(middlewareRequest.getUserPassword());

                    if (StringUtils.isNotEmpty(password)) {
                        credentialMaster.setUserPassword(middlewareRequest.getUserPassword());
                        credentialMaster.setKeyFileName(null);
                        credentialMaster.setKeyFilePath(null);
                        credentialMaster.setKeyFileContent(null);
                    }
                } catch (Exception e) {
                    log.warn("Unable decrypt server password. [Reason] : ", e);
                    throw new RoRoApiException(ErrorCode.INVENTORY_INVALID_USER_PASSWORD);
                }
            }

            if (keyFile != null && keyFile.getSize() > 0) {
                try {
                    // Do NOT save the key file under the repositoryPath for security reason.
                    File keyFileDirectory = new File(CommonProperties.getWorkDir() + File.separator + "keyFiles");
                    if (!keyFileDirectory.exists()) {
                        keyFileDirectory.mkdir();
                    }
                    File tempFile = File.createTempFile("MW-" + middlewareRequest.getUserName() + "-", ".pem", keyFileDirectory);
                    String keyStr = IOUtils.toString(keyFile.getInputStream(), StandardCharsets.UTF_8);

                    if (!keyStr.startsWith("-----BEGIN RSA PRIVATE KEY")) {
                        throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
                    }

                    if (keyStr.length() > 4096) {
                        throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE_SIZE);
                    }

                    credentialMaster.setUserPassword(null);
                    credentialMaster.setKeyFileName(keyFile.getOriginalFilename());
                    credentialMaster.setKeyFilePath(tempFile.getAbsolutePath());
                    credentialMaster.setKeyFileContent(keyStr);

                    IOUtils.write(keyStr, new FileOutputStream(tempFile), "UTF-8");
                } catch (IOException e) {
                    throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
                }
            }

            credentialMaster = credentialMasterRepository.save(credentialMaster);
            inventoryMaster.setCredentialId(credentialMaster.getCredentialId());
        }

        inventoryMaster.setProjectId(projectMaster.getProjectId());
        inventoryMaster.setServerInventoryId(middlewareRequest.getServerInventoryId());
        inventoryMaster.setInventoryTypeCode(StringUtils.defaultIfEmpty(middlewareRequest.getInventoryTypeCode(), Domain1001.MW.name()));
        inventoryMaster.setInventoryDetailTypeCode(middlewareRequest.getEngineName());
        inventoryMaster.setInventoryAnalysisYn(Domain101.Y.name());
        inventoryMaster.setCustomerInventoryCode(middlewareRequest.getCustomerInventoryCode());
        inventoryMaster.setCustomerInventoryName(middlewareRequest.getCustomerInventoryName());
        inventoryMaster.setInventoryName(middlewareRequest.getMiddlewareInventoryName());
        inventoryMaster.setInventoryIpTypeCode(StringUtils.defaultIfEmpty(middlewareRequest.getInventoryIpTypeCode(), Domain1006.INV.name()));
        inventoryMaster.setDeleteYn(Domain101.N.name());
        inventoryMaster.setAutomaticRegistYn(Domain101.N.name());
        inventoryMaster.setDescription(middlewareRequest.getDescription());
        inventoryMaster.setRegistUserId(WebUtil.getUserId());
        inventoryMaster.setRegistDatetime(new Date());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());
        inventoryMaster = inventoryService.saveInventoryMaster(inventoryMaster);

        // Step 2. Save middleware_master table
        MiddlewareMaster middlewareMaster = new MiddlewareMaster();
        middlewareMaster.setMiddlewareInventoryId(inventoryMaster.getInventoryId());
        middlewareMaster.setMiddlewareTypeCode(middlewareRequest.getMiddlewareTypeCode());
        middlewareMaster.setEngineInstallationPath(middlewareRequest.getEngineInstallPath());
        middlewareMaster.setDomainHomePath(middlewareRequest.getDomainHomePath());
        middlewareMaster.setEngineVersion(middlewareRequest.getEngineVersion());
        middlewareMaster.setVendorName(middlewareRequest.getVendorName());
        middlewareMaster.setAutomaticRegistProtectionYn(Domain101.N.name());
        middlewareMaster.setDedicatedAuthenticationYn(middlewareRequest.getDedicatedAuthenticationYn());
        middlewareMasterRepository.save(middlewareMaster);

        // Step 3. Save service_inventory table
        if (middlewareRequest.getServiceIds() != null) {
            for (Long serviceId : middlewareRequest.getServiceIds()) {
                ServiceInventory serviceInventory = new ServiceInventory();

                serviceMasterRepository.findById(serviceId)
                        .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVICE_NOT_FOUND));

                serviceInventory.setServiceId(serviceId);
                serviceInventory.setInventoryId(inventoryMaster.getInventoryId());
                serviceInventoryRepository.save(serviceInventory);
            }
        }

        // Step 4. Save inventory_label table
        if (middlewareRequest.getLabelIds() != null) {
            for (Long labelId : middlewareRequest.getLabelIds()) {
                InventoryLabel inventoryLabel = new InventoryLabel();

                labelMasterRepository.findById(labelId)
                        .orElseThrow(() -> new ResourceNotFoundException("Label ID : " + labelId + " Not Found."));

                inventoryLabel.setLabelId(labelId);
                inventoryLabel.setInventoryId(inventoryMaster.getInventoryId());
                inventoryLabelRepository.save(inventoryLabel);
            }
        }

        // Step 5. Save inventory_manager table
        if (middlewareRequest.getInventoryManagers() != null) {
            for (Manager manager : middlewareRequest.getInventoryManagers()) {
                InventoryManager inventoryManager = new InventoryManager();

                userMasterRepository.findById(manager.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User ID : " + manager.getUserId() + " Not Found."));

                inventoryManager.setUserId(manager.getUserId());
                inventoryManager.setInventoryId(inventoryMaster.getInventoryId());
                inventoryManager.setManagerTypeCode(manager.getManagerTypeCode());
                inventoryManagerRepository.save(inventoryManager);
            }
        }

        // Step 6. inventory process 요청 등록
        assessmentService.createAssessment(projectId, middlewareMaster.getMiddlewareInventoryId());

        // generate response
        MiddlewareSimpleResponse response = new MiddlewareSimpleResponse();
        response.setMiddlewareInventoryId(inventoryMaster.getInventoryId());
        response.setMiddlewareInventoryName(inventoryMaster.getInventoryName());

        return response;
    }

    @Transactional
    public MiddlewareSimpleResponse modifyMiddleware(Long projectId, Long middlewareInventoryId, MiddlewareRequest middlewareRequest, MultipartFile keyFile) {
        // 프로젝트 id 확인
        ProjectMaster projectMaster = projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));

        // 인벤토리 id 확인
        InventoryMaster inventoryMaster = inventoryMasterRepository.findById(middlewareInventoryId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_MIDDLEWARE_NOT_FOUND));

        MiddlewareMaster middlewareMaster = middlewareMasterRepository.findById(inventoryMaster.getInventoryId())
                .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_MIDDLEWARE_NOT_FOUND));

        // 미들웨어 수정 중복 체크 로직
        List<Long> middlewareIds = middlewareMasterRepository.selectDuplicateMiddlewareInventory(middlewareRequest.getServerInventoryId(),
                middlewareRequest.getEngineInstallPath(), middlewareRequest.getDomainHomePath());

        if (middlewareIds != null && middlewareIds.size() > 0) {
            if (middlewareIds.size() != 1 || !middlewareIds.get(0).equals(middlewareInventoryId)) {
                throw new RoRoApiException(ErrorCode.INVENTORY_MIDDLEWARE_DUPLICATE, middlewareRequest.getEngineInstallPath());
            }
        }

        // Step 1. Update inventory_master table
        inventoryMaster.setServerInventoryId(middlewareRequest.getServerInventoryId());
        inventoryMaster.setInventoryDetailTypeCode(middlewareRequest.getEngineName());
        inventoryMaster.setCustomerInventoryCode(middlewareRequest.getCustomerInventoryCode());
        inventoryMaster.setCustomerInventoryName(middlewareRequest.getCustomerInventoryName());
        inventoryMaster.setInventoryName(middlewareRequest.getMiddlewareInventoryName());
        inventoryMaster.setDescription(middlewareRequest.getDescription());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());

        if (Domain101.Y.name().equals(middlewareRequest.getDedicatedAuthenticationYn())) {
            CredentialMaster credentialMaster = new CredentialMaster();
            credentialMaster.setRegistUserId(WebUtil.getUserId());
            credentialMaster.setRegistDatetime(new Date());

            if (middlewareRequest.getCredentialId() != null) {
                credentialMaster = credentialMasterRepository.findByProjectIdAndCredentialId(projectId, middlewareRequest.getCredentialId());

                if (credentialMaster == null) {
                    throw new RoRoApiException(ErrorCode.RESOURCE_NOT_FOUND, "Credential does not exist.");
                }
            }

            if (keyFile != null && keyFile.getSize() == 0) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
            }

            credentialMaster.setProjectId(projectId);
            credentialMaster.setUserName(middlewareRequest.getUserName());
            credentialMaster.setCredentialTypeCode(Domain1001.MW.name());
            credentialMaster.setDeleteYn(Domain101.N.name());
            credentialMaster.setModifyUserId(WebUtil.getUserId());
            credentialMaster.setModifyDatetime(new Date());

            if (StringUtils.isNotEmpty(middlewareRequest.getUserPassword())) {
                try {
                    String password = GeneralCipherUtil.decrypt(middlewareRequest.getUserPassword());

                    if (StringUtils.isNotEmpty(password)) {
                        credentialMaster.setUserPassword(middlewareRequest.getUserPassword());
                        credentialMaster.setKeyFileName(null);
                        credentialMaster.setKeyFilePath(null);
                        credentialMaster.setKeyFileContent(null);
                    }
                } catch (Exception e) {
                    log.warn("Unable decrypt server password. [Reason] : ", e);
                }
            }

            if (keyFile != null && keyFile.getSize() > 0) {
                try {
                    // Do NOT save the key file under the repositoryPath for security reason.
                    File keyFileDirectory = new File(CommonProperties.getWorkDir() + File.separator + "keyFiles");
                    if (!keyFileDirectory.exists()) {
                        keyFileDirectory.mkdir();
                    }
                    File tempFile = File.createTempFile("MW-" + middlewareRequest.getUserName() + "-", ".pem", keyFileDirectory);
                    String keyStr = IOUtils.toString(keyFile.getInputStream(), StandardCharsets.UTF_8);

                    if (!keyStr.startsWith("-----BEGIN RSA PRIVATE KEY")) {
                        throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
                    }

                    if (keyStr.length() > 4096) {
                        throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE_SIZE);
                    }

                    credentialMaster.setUserPassword(null);
                    credentialMaster.setKeyFileName(keyFile.getOriginalFilename());
                    credentialMaster.setKeyFilePath(tempFile.getAbsolutePath());
                    credentialMaster.setKeyFileContent(keyStr);

                    IOUtils.write(keyStr, new FileOutputStream(tempFile), "UTF-8");
                } catch (IOException e) {
                    throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
                }
            }

            credentialMaster = credentialMasterRepository.save(credentialMaster);
            inventoryMaster.setCredentialId(credentialMaster.getCredentialId());
        } else {
            inventoryMaster.setCredentialId(null);
        }

        // Customer Inventory Code 중복을 체크하기 위해 inventoryService를 사용한다.
        inventoryService.saveInventoryMaster(inventoryMaster);

        // Step 2. Update middleware_master table
        middlewareMaster.setMiddlewareTypeCode(middlewareRequest.getMiddlewareTypeCode());
        middlewareMaster.setEngineInstallationPath(middlewareRequest.getEngineInstallPath());
        middlewareMaster.setDomainHomePath(middlewareRequest.getDomainHomePath());
        middlewareMaster.setEngineVersion(middlewareRequest.getEngineVersion());
        middlewareMaster.setVendorName(middlewareRequest.getVendorName());
        middlewareMaster.setDedicatedAuthenticationYn(middlewareRequest.getDedicatedAuthenticationYn());

        // Step 3. Update service_inventory table
        serviceInventoryRepository.deleteAllByInventoryId(middlewareMaster.getMiddlewareInventoryId());
        if (middlewareRequest.getServiceIds() != null && !middlewareRequest.getServiceIds().isEmpty()) {
            ServiceInventory serviceInventory;
            for (Long serviceId : middlewareRequest.getServiceIds()) {
                serviceInventory = new ServiceInventory();

                serviceMasterRepository.findById(serviceId).orElseThrow(() -> new ResourceNotFoundException("Service ID : " + serviceId + " Not Found."));

                serviceInventory.setInventoryId(inventoryMaster.getInventoryId());
                serviceInventory.setServiceId(serviceId);

                serviceInventoryRepository.save(serviceInventory);
            }
        }

        // Step 4. Update inventory_label table
        inventoryLabelRepository.deleteAllByInventoryId(middlewareMaster.getMiddlewareInventoryId());
        if (middlewareRequest.getLabelIds() != null) {
            for (Long labelId : middlewareRequest.getLabelIds()) {
                InventoryLabel inventoryLabel = new InventoryLabel();

                labelMasterRepository.findById(labelId).orElseThrow(() -> new ResourceNotFoundException("Label ID : " + labelId + " Not Found."));

                inventoryLabel.setLabelId(labelId);
                inventoryLabel.setInventoryId(inventoryMaster.getInventoryId());

                inventoryLabelRepository.save(inventoryLabel);
            }
        }

        // Step 5. Save inventory_manager table
        inventoryManagerRepository.deleteAllByInventoryId(middlewareMaster.getMiddlewareInventoryId());
        if (middlewareRequest.getInventoryManagers() != null) {
            for (Manager manager : middlewareRequest.getInventoryManagers()) {
                InventoryManager inventoryManager = new InventoryManager();

                userMasterRepository.findById(manager.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User ID : " + manager.getUserId() + " Not Found."));

                inventoryManager.setUserId(manager.getUserId());
                inventoryManager.setInventoryId(inventoryMaster.getInventoryId());
                inventoryManager.setManagerTypeCode(manager.getManagerTypeCode());

                inventoryManagerRepository.save(inventoryManager);
            }
        }

        MiddlewareSimpleResponse response = new MiddlewareSimpleResponse();
        response.setMiddlewareInventoryId(inventoryMaster.getInventoryId());
        response.setMiddlewareInventoryName(inventoryMaster.getInventoryName());

        return response;
    }


    @Transactional
    public void removeMiddleware(Long projectId, Long middlewareInventoryId, boolean isPreventAutoDiscovery) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, middlewareInventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Middleware ID : " + middlewareInventoryId + " Not Found."));

        MiddlewareMaster middlewareMaster = middlewareMasterRepository.findById(middlewareInventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Middleware ID : " + middlewareInventoryId + " Not Found."));

        // TODO 인스턴스가 있으면 삭제가 안되게 할지 논의 필요 - 미들웨어 삭제 시, 연결된 인스턴스 목록의 delete_yn = 'Y'로 업데이트
        List<DiscoveredInstanceMaster> discoveredInstanceMasterList = discoveredInstanceMasterRepository.findByFinderInventoryId(inventoryMaster.getInventoryId());
        if (!discoveredInstanceMasterList.isEmpty()) {
            for (DiscoveredInstanceMaster instance : discoveredInstanceMasterList) {
                instance.setDeleteYn(Domain101.Y.name());
            }
        }

        // // 매핑 된 manager 를 삭제한다.
        // inventoryManagerRepository.deleteByInventoryId(inventoryMaster.getInventoryId());
        //
        // // 매핑 된 service 를 삭제한다.
        // serviceInventoryRepository.deleteByInventoryId(inventoryMaster.getInventoryId());

        inventoryMaster.setDeleteYn(Domain101.Y.name());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());
        if (isPreventAutoDiscovery) {
            middlewareMaster.setAutomaticRegistProtectionYn(Domain101.Y.name());
        }
    }

    public MiddlewareDetailResponse getMiddlewareDetail(Long projectId, Long middlewareInventoryId) {
        inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, middlewareInventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Middleware ID : " + middlewareInventoryId + " Not Found in Project ID : " + projectId));

        MiddlewareDetailResponse middlwareDetail = middlewareMapper.selectMiddlewareDetail(projectId, middlewareInventoryId);

        if (middlwareDetail != null) {
            setMiddlewareDetail(middlwareDetail);
        }

        return middlwareDetail;
    }

    public ByteArrayInputStream getMiddlewareListExcel(Long projectId, Long serviceId, Long serverId) {
        List<MiddlewareExcelResponse> middlewareExcels = middlewareMapper.selectMiddlewareExcel(projectId, serviceId, serverId);

        // 헤더 설정
        //-> 미들웨어 헤더
        ListToExcelDto listToExcelDto = new ListToExcelDto();
        listToExcelDto.getHeaderItemList().add("Inventory Code");
        listToExcelDto.getHeaderItemList().add("Inventory Name");
        listToExcelDto.getHeaderItemList().add("Service ID");
        listToExcelDto.getHeaderItemList().add("Service Name");
        listToExcelDto.getHeaderItemList().add("Server ID");
        listToExcelDto.getHeaderItemList().add("Server Name");
        listToExcelDto.getHeaderItemList().add("Middleware ID");
        listToExcelDto.getHeaderItemList().add("Middleware Name");
        listToExcelDto.getHeaderItemList().add("Middleware Type");
        listToExcelDto.getHeaderItemList().add("Vendor");
        listToExcelDto.getHeaderItemList().add("Engine Name");
        listToExcelDto.getHeaderItemList().add("Engine Version");
        listToExcelDto.getHeaderItemList().add("Engine Install Path");
        listToExcelDto.getHeaderItemList().add("Domain Home");
        listToExcelDto.getHeaderItemList().add("Dedicated Authentication YN");
        listToExcelDto.getHeaderItemList().add("Dedicated Authentication UserName");
        listToExcelDto.getHeaderItemList().add("No. of Instances");
        listToExcelDto.getHeaderItemList().add("Labels");
        listToExcelDto.getHeaderItemList().add("Description");

        //-> 인스턴스 헤더
        listToExcelDto.getHeaderItemList().add("Middleware Instance ID");
        listToExcelDto.getHeaderItemList().add("Middleware Instance Name");
        listToExcelDto.getHeaderItemList().add("Instance Install Path");
        listToExcelDto.getHeaderItemList().add("Config File Path");
        listToExcelDto.getHeaderItemList().add("Port");
        listToExcelDto.getHeaderItemList().add("Run User");
        listToExcelDto.getHeaderItemList().add("Java Version");

        ListToExcelDto.RowItem rowItem;
        // 인스턴스, 서비스 매핑에 따라 신규 row를 생성한다.
        for (MiddlewareExcelResponse mw : middlewareExcels) {

            rowItem = new ListToExcelDto.RowItem();
            // 미들웨어 데이터 설정
            rowItem.getCellItemList().add(mw.getCustomerInventoryCode());
            rowItem.getCellItemList().add(mw.getCustomerInventoryName());
            rowItem.getCellItemList().add(mw.getServiceId());
            rowItem.getCellItemList().add(mw.getServiceName());
            rowItem.getCellItemList().add(mw.getServerInventoryId());
            rowItem.getCellItemList().add(mw.getServerInventoryName());
            rowItem.getCellItemList().add(mw.getMiddlewareInventoryId());
            rowItem.getCellItemList().add(mw.getMiddlewareInventoryName());
            rowItem.getCellItemList().add(mw.getMiddlewareTypeCode());
            rowItem.getCellItemList().add(mw.getVendorName());
            rowItem.getCellItemList().add(mw.getInventoryDetailTypeCode());
            rowItem.getCellItemList().add(mw.getEngineVersion());
            rowItem.getCellItemList().add(mw.getEngineInstallPath());
            rowItem.getCellItemList().add(StringUtils.defaultString(mw.getDomainHomePath()));
            rowItem.getCellItemList().add(mw.getDedicatedAuthenticationYn());
            rowItem.getCellItemList().add(mw.getUserName());
            rowItem.getCellItemList().add(mw.getInstanceCount());
            rowItem.getCellItemList().add(mw.getLabels());
            rowItem.getCellItemList().add(mw.getDescription());

            // 인스턴스 데이터 설정
            rowItem.getCellItemList().add(mw.getMiddlewareInstanceId());
            rowItem.getCellItemList().add(mw.getMiddlewareInstanceName());
            rowItem.getCellItemList().add(mw.getMiddlewareInstancePath());
            rowItem.getCellItemList().add(mw.getMiddlewareConfigPath());
            rowItem.getCellItemList().add(mw.getMiddlewareInstanceServicePort());
            rowItem.getCellItemList().add(mw.getRunningUser());
            rowItem.getCellItemList().add(mw.getJavaVersion());

            listToExcelDto.getRowItemList().add(rowItem);
        }

        ByteArrayOutputStream out;
        try {
            out = ExcelUtil.listToExcel("Middlewares", listToExcelDto);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.debug("Unhandled exception occurred while create middleware excel list.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }

    public List<InstanceResponse> getInstances(Long projectId, Long middlewareInventoryId) {
        return middlewareMapper.selectMiddlewareInstanceList(projectId, middlewareInventoryId);
    }

    public void removeMiddlewareInstance(Long projectId, Long middlewareInstanceId) {
        DiscoveredInstanceMaster discoveredInstanceMaster =
                discoveredInstanceMasterRepository.findByProjectIdAndDiscoveredInstanceId(projectId, middlewareInstanceId);

        if (discoveredInstanceMaster == null) {
            throw new ResourceNotFoundException("Middleware Instance ID : " + middlewareInstanceId + " Not Found.");
        } else {
            discoveredInstanceMaster.setDeleteYn(Domain101.Y.name());
            discoveredInstanceMasterRepository.save(discoveredInstanceMaster);
        }
    }

    public InstanceDetailResponse getMiddlewareInstanceDetail(Long projectId, Long middlewareInventoryId, Long middlewareInstanceId) {
        MiddlewareDetailResponse middlewareDetailResponse = middlewareMapper.selectMiddlewareDetail(projectId, middlewareInventoryId);

        InstanceDetailResponse instanceDetail;
        if (middlewareDetailResponse == null) {
            throw new RoRoApiException(ErrorCode.INVENTORY_MIDDLEWARE_NOT_FOUND);
        } else {
            // 서비스 설정
            middlewareDetailResponse.setServices(serviceMapper.getServiceSummaries(middlewareDetailResponse.getMiddlewareInventoryId()));

            instanceDetail = middlewareMapper
                    .selectMiddlewareInstanceDetail(projectId, middlewareInventoryId, middlewareInstanceId);

            if (instanceDetail == null) {
                throw new ResourceNotFoundException("Middleware Instance ID : " + middlewareInstanceId + " Not Found.");
            } else {
                instanceDetail.setServices(middlewareDetailResponse.getServices());
                instanceDetail.setServerInventoryId(middlewareDetailResponse.getServerInventoryId());
                instanceDetail.setServerInventoryName(middlewareDetailResponse.getServerInventoryName());
                instanceDetail.setRepresentativeIpAddress(middlewareDetailResponse.getRepresentativeIpAddress());
                instanceDetail.setMiddlewareInventoryId(middlewareDetailResponse.getMiddlewareInventoryId());
                instanceDetail.setMiddlewareInventoryName(middlewareDetailResponse.getMiddlewareInventoryName());
                instanceDetail.setMiddlewareTypeCode(middlewareDetailResponse.getMiddlewareTypeCode());
                instanceDetail.setVendorName(middlewareDetailResponse.getVendorName());
                instanceDetail.setEngineVersion(middlewareDetailResponse.getEngineVersion());
                instanceDetail.setInventoryDetailTypeCode(middlewareDetailResponse.getInventoryDetailTypeCode());
                instanceDetail.setJavaVersion(middlewareDetailResponse.getJavaVersion());
                instanceDetail.setJavaVendor(middlewareDetailResponse.getJavaVendor());
            }
        }

        return instanceDetail;
    }

    public List<DeployApplicationList> getDeployApplicationList(Long projectId, Long middlewareInstanceId) {
        return middlewareMapper.selectDeployApplicationList(projectId, middlewareInstanceId);
    }

    public List<DeployDatasourceList> getDeployDatasourceList(Long projectId, Long middlewareInstanceId) {
        List<DeployDatasourceList> deployDatasourceList = new ArrayList<>();

        List<Map<String, Object>> datasourceList = middlewareMapper.selectDatasourceList(projectId, middlewareInstanceId);

        for (Map<String, Object> datasourceMap : datasourceList) {
            DeployDatasourceList deployDatasource = new DeployDatasourceList();
            Map<String, Object> databaseInstanceMap = applicationMapper.selectApplicationDatabaseInstance(
                    projectId, (String) datasourceMap.get("server_ip"), (String) datasourceMap.get("service_name"));

            deployDatasource.setProjectId((Long) datasourceMap.get("project_id"));
            deployDatasource.setUserName((String) datasourceMap.get("user_name"));
            deployDatasource.setDatasourceName((String) datasourceMap.get("descriptors_name"));
            deployDatasource.setConnectionUrl((String) datasourceMap.get("full_descriptors"));

            if (databaseInstanceMap != null) {
                deployDatasource.setProjectId((Long) databaseInstanceMap.get("project_id"));
                deployDatasource.setDatabaseInventoryId((Long) databaseInstanceMap.get("database_inventory_id"));
                deployDatasource.setDatabaseInstanceId((Long) databaseInstanceMap.get("database_instance_id"));
                deployDatasource.setUserName((String) databaseInstanceMap.get("user_name"));
            }

            deployDatasourceList.add(deployDatasource);
        }

        // 중복제거
        deployDatasourceList = deployDatasourceList.stream()
                .filter(distinctByKey(f -> f.getDatabaseInventoryId() + ":" + f.getDatabaseInstanceId() + ":" + f.getDatasourceName() + ":" + f.getConnectionUrl() + ":" + f.getUserName()))
                .collect(Collectors.toList());

        return deployDatasourceList;
    }

    public List<DeployApplicationList> getMiddlewareDeployApplicationList(Long projectId, Long middlewareInventoryId) {
        List<DeployApplicationList> deployApplicationList = new ArrayList<>();
        List<InstanceResponse> middlewareInstanceList = middlewareMapper.selectMiddlewareInstanceList(projectId, middlewareInventoryId);

        for (InstanceResponse instance : middlewareInstanceList) {
            List<DeployApplicationList> deployList = getDeployApplicationList(projectId, instance.getMiddlewareInstanceId());
            if (CollectionUtils.isNotEmpty(deployList)) {
                deployApplicationList.addAll(deployList);
            }
        }

        // 중복 제거
        deployApplicationList = deployApplicationList.stream()
                .filter(distinctByKey(f -> f.getApplicationInventoryId() + ":" + f.getApplicationInstanceId() + ":" + f.getApplicationName() + ":" + f.getDeployPath() + ":" + f.getContextPath()))
                .collect(Collectors.toList());

        return deployApplicationList;
    }

    public List<DeployDatasourceList> getMiddlewareDeployDatasourceList(Long projectId, Long middlewareInventoryId) {
        List<DeployDatasourceList> deployDatasourceList = new ArrayList<>();
        List<InstanceResponse> middlewareInstanceList = middlewareMapper.selectMiddlewareInstanceList(projectId, middlewareInventoryId);

        for (InstanceResponse instance : middlewareInstanceList) {
            List<DeployDatasourceList> deployList = getDeployDatasourceList(projectId, instance.getMiddlewareInstanceId());
            if (CollectionUtils.isNotEmpty(deployList)) {
                deployDatasourceList.addAll(deployList);
            }
        }

        // 중복제거
        deployDatasourceList = deployDatasourceList.stream()
                .filter(distinctByKey(f -> f.getDatabaseInventoryId() + ":" + f.getDatabaseInstanceId() + ":" + f.getDatasourceName() + ":" + f.getConnectionUrl() + ":" + f.getUserName()))
                .collect(Collectors.toList());

        return deployDatasourceList;
    }
}
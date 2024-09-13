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
 * SangCheon Park   Nov 25, 2021		    First Draft.
 */
package io.playce.roro.api.domain.inventory.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.FileUtil;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.assessment.service.AssessmentService;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.code.Domain1006;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.config.RoRoProperties;
import io.playce.roro.common.dto.common.excel.ListToExcelDto;
import io.playce.roro.common.dto.enums.ApplicationFileType;
import io.playce.roro.common.dto.inventory.application.*;
import io.playce.roro.common.dto.inventory.manager.Manager;
import io.playce.roro.common.dto.inventory.middleware.InstanceResponse;
import io.playce.roro.common.dto.inventory.process.InventoryProcess;
import io.playce.roro.common.dto.inventory.server.Server;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.ExcelUtil;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.common.label.LabelMapper;
import io.playce.roro.mybatis.domain.inventory.application.ApplicationMapper;
import io.playce.roro.mybatis.domain.inventory.manager.ManagerMapper;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import io.playce.roro.mybatis.domain.inventory.service.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

import static io.playce.roro.api.common.CommonConstants.APPLICATION_FILE_UPLOAD_DIR;
import static io.playce.roro.common.util.support.DistinctByKey.distinctByKey;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final AssessmentService assessmentService;
    private final ProjectMasterRepository projectMasterRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ServerMasterRepository serverMasterRepository;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final ApplicationMasterRepository applicationMasterRepository;
    private final ServiceInventoryRepository serviceInventoryRepository;
    private final LabelMasterRepository labelMasterRepository;
    private final InventoryLabelRepository inventoryLabelRepository;
    private final UserMasterRepository userMasterRepository;
    private final InventoryManagerRepository inventoryManagerRepository;
    private final DiscoveredInstanceMasterRepository discoveredInstanceMasterRepository;
    private final CredentialMasterRepository credentialMasterRepository;
    private final ApplicationMapper applicationMapper;
    private final ServiceMapper serviceMapper;
    private final ServerMapper serverMapper;
    private final InventoryProcessMapper inventoryProcessMapper;
    private final ManagerMapper managerMapper;
    private final LabelMapper labelMapper;
    private final InventoryService inventoryService;
    private final RoRoProperties roroProperties;

    /**
     * <pre>
     * 애플리케이션 목록 조회
     * </pre>
     *
     * @param projectId
     * @param serviceId
     * @param serverId
     * @return
     */
    public List<ApplicationResponse> getApplications(Long projectId, Long serviceId, Long serverId) {
        List<ApplicationResponse> applications = applicationMapper.getApplications(projectId, serviceId, serverId);

        for (ApplicationResponse application : applications) {
            setApplicationDetail(application);
        }

        return applications;
    }

    /**
     * <pre>
     * 애플리케이션 상세 조회
     * </pre>
     *
     * @param projectId
     * @param applicationId
     * @return
     */
    public ApplicationDetailResponse getApplication(Long projectId, Long applicationId) {
        inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application ID : " + applicationId + " Not Found in Project ID : " + projectId));

        ApplicationDetailResponse application = applicationMapper.getApplication(projectId, applicationId);
        setApplicationDetail(application);
        return application;
    }

    /**
     * <pre>
     * 애플리케이션 Datasource 조회
     * </pre>
     *
     * @param projectId
     * @param applicationId
     * @return
     */
    public List<ApplicationDatasourceResponse> getDatasources(Long projectId, Long applicationId) {
        List<ApplicationDatasourceResponse> applicationDatasourceResponses = new ArrayList<>();

        List<Map<String, Object>> applicationDatasourceList = applicationMapper.selectApplicationDatasource(projectId, applicationId);

        for (Map<String, Object> applicationMap : applicationDatasourceList) {
            ApplicationDatasourceResponse applicationDatasourceResponse = new ApplicationDatasourceResponse();
            Map<String, Object> databaseInstanceMap = applicationMapper.selectApplicationDatabaseInstance(
                    projectId, (String) applicationMap.get("server_ip"), (String) applicationMap.get("service_name"));

            applicationDatasourceResponse.setProjectId((Long) applicationMap.get("project_id"));
            applicationDatasourceResponse.setUserName((String) applicationMap.get("user_name"));
            applicationDatasourceResponse.setDatasourceName((String) applicationMap.get("descriptors_name"));

            if (StringUtils.isNotEmpty((String) applicationMap.get("jdbc_url"))) {
                applicationDatasourceResponse.setConnectionUrl((String) applicationMap.get("jdbc_url"));
            } else {
                applicationDatasourceResponse.setConnectionUrl((String) applicationMap.get("full_descriptors"));
            }

            if (databaseInstanceMap != null) {
                applicationDatasourceResponse.setProjectId((Long) databaseInstanceMap.get("project_id"));
                applicationDatasourceResponse.setDatabaseInventoryId((Long) databaseInstanceMap.get("database_inventory_id"));
                applicationDatasourceResponse.setDatabaseInstanceId((Long) databaseInstanceMap.get("database_instance_id"));
                applicationDatasourceResponse.setUserName((String) databaseInstanceMap.get("user_name"));
            }

            applicationDatasourceResponses.add(applicationDatasourceResponse);
        }

        // 중복제거
        applicationDatasourceResponses = applicationDatasourceResponses.stream()
                .filter(distinctByKey(f -> f.getDatabaseInventoryId() + ":" + f.getDatabaseInstanceId() + ":" + f.getDatasourceName() + ":" + f.getConnectionUrl() + ":" + f.getUserName()))
                .collect(Collectors.toList());

        return applicationDatasourceResponses;
    }

    /**
     * https://cloud-osci.atlassian.net/browse/PCR-5593
     * 이중 서브밋 방지를 위한 방어코드로 @Transactional 애노테이션에는 synchronized가 동작하기 않기 때문에
     * 별도의 synchronized 메소드 내에서 @Transactional 메소드를 호출한다.
     */
    public synchronized ApplicationSimpleResponse createApplication(Long projectId, ApplicationRequest applicationRequest,
                                                                    MultipartFile analyzeFile, MultipartFile keyFile) throws Exception {
        return createApplicationInternal(projectId, applicationRequest, analyzeFile, keyFile);
    }

    /**
     * <pre>
     * 애플리케이션 등록
     * </pre>
     *
     * @param projectId
     * @param applicationRequest
     * @param analyzeFile
     * @param keyFile
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationSimpleResponse createApplicationInternal(Long projectId, ApplicationRequest applicationRequest,
                                                               MultipartFile analyzeFile, MultipartFile keyFile) throws Exception {
        ProjectMaster projectMaster = projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));

        ServerMaster serverMaster = serverMasterRepository.findById(applicationRequest.getServerInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + applicationRequest.getServerInventoryId() + " Not Found."));

        List<Long> applicationIds = applicationMapper.selectDuplicatedApplication(projectId, applicationRequest.getServerInventoryId(), applicationRequest.getDeployPath());

        if (applicationIds != null && applicationIds.size() > 0) {
            throw new RoRoApiException(ErrorCode.INVENTORY_APPLICATION_DUPLICATE, applicationRequest.getDeployPath());
        }

        // Step 1. Save inventory_master Table
        InventoryMaster inventoryMaster = new InventoryMaster();

        if (Domain101.Y.name().equals(applicationRequest.getDedicatedAuthenticationYn())) {
            if (keyFile != null && keyFile.getSize() == 0) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
            }

            CredentialMaster credentialMaster = new CredentialMaster();
            credentialMaster.setProjectId(projectId);
            credentialMaster.setUserName(applicationRequest.getUserName());
            credentialMaster.setCredentialTypeCode(Domain1001.MW.name());
            credentialMaster.setDeleteYn(Domain101.N.name());
            credentialMaster.setRegistUserId(WebUtil.getUserId());
            credentialMaster.setRegistDatetime(new Date());
            credentialMaster.setModifyUserId(WebUtil.getUserId());
            credentialMaster.setModifyDatetime(new Date());

            if (StringUtils.isNotEmpty(applicationRequest.getUserPassword())) {
                try {
                    String password = GeneralCipherUtil.decrypt(applicationRequest.getUserPassword());

                    if (StringUtils.isNotEmpty(password)) {
                        credentialMaster.setUserPassword(applicationRequest.getUserPassword());
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
                    File tempFile = File.createTempFile("APP-" + applicationRequest.getUserName() + "-", ".pem", keyFileDirectory);
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
        inventoryMaster.setServerInventoryId(serverMaster.getServerInventoryId());
        inventoryMaster.setInventoryTypeCode(StringUtils.defaultIfEmpty(applicationRequest.getInventoryTypeCode(), Domain1001.APP.name()));
        inventoryMaster.setInventoryDetailTypeCode(applicationRequest.getInventoryDetailTypeCode()); // EAR, WAR, JAR
        inventoryMaster.setInventoryAnalysisYn(Domain101.Y.name());
        inventoryMaster.setCustomerInventoryCode(applicationRequest.getCustomerInventoryCode());
        inventoryMaster.setCustomerInventoryName(applicationRequest.getCustomerInventoryName());
        inventoryMaster.setInventoryName(applicationRequest.getApplicationInventoryName());
        inventoryMaster.setInventoryIpTypeCode(StringUtils.defaultIfEmpty(applicationRequest.getInventoryIpTypeCode(), Domain1006.INV.name()));
        inventoryMaster.setDeleteYn(Domain101.N.name());
        inventoryMaster.setAutomaticRegistYn(Domain101.N.name());
        inventoryMaster.setDescription(applicationRequest.getDescription());
        inventoryMaster.setRegistUserId(WebUtil.getUserId());
        inventoryMaster.setRegistDatetime(new Date());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());

        inventoryMaster = inventoryService.saveInventoryMaster(inventoryMaster);

        // Step 2. Save service_inventory table
        for (Long serviceId : applicationRequest.getServiceIds()) {
            ServiceInventory serviceInventory = new ServiceInventory();

            serviceMasterRepository.findById(serviceId).orElseThrow(() -> new ResourceNotFoundException("Service ID : " + serviceId + " Not Found."));

            serviceInventory.setServiceId(serviceId);
            serviceInventory.setInventoryId(inventoryMaster.getInventoryId());

            serviceInventoryRepository.save(serviceInventory);
        }

        // Step 3. Save application_master table
        ApplicationMaster applicationMaster = new ApplicationMaster();
        applicationMaster.setApplicationInventoryId(inventoryMaster.getInventoryId());
        applicationMaster.setDeployPath(applicationRequest.getDeployPath());
        applicationMaster.setApplicationSize(0L);
        applicationMaster.setSourceLocationUri(applicationRequest.getSourceLocationUri());
        applicationMaster.setAnalysisLibList(applicationRequest.getAnalysisLibList());
        applicationMaster.setAnalysisStringList(applicationRequest.getAnalysisStringList());
        applicationMaster.setAutomaticRegistProtectionYn(Domain101.N.name());
        applicationMaster.setDedicatedAuthenticationYn(applicationRequest.getDedicatedAuthenticationYn());

        if (analyzeFile != null) {
            // Save Source File & Archive File
            saveAnalyzeFile(analyzeFile, applicationMaster);
            // 2022.01.27 null => "" 으로 처리 요청 (from 김기정)
            applicationMaster.setSourceLocationUri("");
        } else {
            applicationMaster.setUploadSourceFileName(null);
            applicationMaster.setUploadSourceFilePath(null);
        }

        applicationMasterRepository.save(applicationMaster);

        // Step 4. Save inventory_label table
        if (applicationRequest.getLabelIds() != null) {
            for (Long labelId : applicationRequest.getLabelIds()) {
                InventoryLabel inventoryLabel = new InventoryLabel();

                labelMasterRepository.findById(labelId).orElseThrow(() -> new ResourceNotFoundException("Label ID : " + labelId + " Not Found."));

                inventoryLabel.setLabelId(labelId);
                inventoryLabel.setInventoryId(inventoryMaster.getInventoryId());

                inventoryLabelRepository.save(inventoryLabel);
            }
        }

        // Step 5. Save inventory_manager table
        if (applicationRequest.getInventoryManagers() != null) {
            for (Manager manager : applicationRequest.getInventoryManagers()) {
                InventoryManager inventoryManager = new InventoryManager();

                userMasterRepository.findById(manager.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User ID : " + manager.getUserId() + " Not Found."));

                inventoryManager.setUserId(manager.getUserId());
                inventoryManager.setInventoryId(inventoryMaster.getInventoryId());
                inventoryManager.setManagerTypeCode(manager.getManagerTypeCode());

                inventoryManagerRepository.save(inventoryManager);
            }
        }

        // Create an application assessment request
        // https://cloud-osci.atlassian.net/browse/PCR-5624
        if (roroProperties.isApplicationAutoScan()) {
            assessmentService.createAssessment(projectId, applicationMaster.getApplicationInventoryId());
        }

        ApplicationSimpleResponse response = new ApplicationSimpleResponse();
        response.setApplicationInventoryId(inventoryMaster.getInventoryId());
        response.setApplicationInventoryName(inventoryMaster.getInventoryName());

        return response;
    }

    /**
     * <pre>
     * 애플리케이션 수정
     * </pre>
     *
     * @param projectId
     * @param applicationId
     * @param applicationRequest
     * @param analyzeFile
     * @param keyFile
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationSimpleResponse modifyApplication(Long projectId, Long applicationId,
                                                       ApplicationRequest applicationRequest, MultipartFile analyzeFile, MultipartFile keyFile) throws Exception {

        ServerMaster serverMaster = serverMasterRepository.findById(applicationRequest.getServerInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + applicationRequest.getServerInventoryId() + " Not Found."));

        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application ID : " + applicationId + " Not Found in Project ID : " + projectId));

        ApplicationMaster applicationMaster = applicationMasterRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application ID : " + applicationId + " Not Found."));

        List<Long> applicationIds = applicationMapper.selectDuplicatedApplication(projectId, applicationRequest.getServerInventoryId(), applicationRequest.getDeployPath());

        if (applicationIds != null && applicationIds.size() > 0) {
            if (applicationIds.size() != 1 || !applicationIds.get(0).equals(applicationId)) {
                throw new RoRoApiException(ErrorCode.INVENTORY_APPLICATION_DUPLICATE, applicationRequest.getDeployPath());
            }
        }

        // Step 1. Update inventory_master Table
        inventoryMaster.setServerInventoryId(serverMaster.getServerInventoryId());
        inventoryMaster.setInventoryTypeCode(StringUtils.defaultIfEmpty(applicationRequest.getInventoryTypeCode(), Domain1001.APP.name()));
        inventoryMaster.setInventoryDetailTypeCode(applicationRequest.getInventoryDetailTypeCode()); // EAR, WAR, JAR
        inventoryMaster.setCustomerInventoryCode(applicationRequest.getCustomerInventoryCode());
        inventoryMaster.setCustomerInventoryName(applicationRequest.getCustomerInventoryName());
        inventoryMaster.setInventoryName(applicationRequest.getApplicationInventoryName());
        inventoryMaster.setInventoryIpTypeCode(StringUtils.defaultIfEmpty(applicationRequest.getInventoryIpTypeCode(), Domain1006.INV.name()));
        inventoryMaster.setDescription(applicationRequest.getDescription());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());

        if (Domain101.Y.name().equals(applicationRequest.getDedicatedAuthenticationYn())) {
            CredentialMaster credentialMaster = new CredentialMaster();
            credentialMaster.setRegistUserId(WebUtil.getUserId());
            credentialMaster.setRegistDatetime(new Date());

            if (applicationRequest.getCredentialId() != null) {
                credentialMaster = credentialMasterRepository.findByProjectIdAndCredentialId(projectId, applicationRequest.getCredentialId());

                if (credentialMaster == null) {
                    throw new RoRoApiException(ErrorCode.RESOURCE_NOT_FOUND, "Credential does not exist.");
                }
            }

            if (keyFile != null && keyFile.getSize() == 0) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
            }

            credentialMaster.setProjectId(projectId);
            credentialMaster.setUserName(applicationRequest.getUserName());
            credentialMaster.setCredentialTypeCode(Domain1001.MW.name());
            credentialMaster.setDeleteYn(Domain101.N.name());
            credentialMaster.setModifyUserId(WebUtil.getUserId());
            credentialMaster.setModifyDatetime(new Date());

            if (StringUtils.isNotEmpty(applicationRequest.getUserPassword())) {
                try {
                    String password = GeneralCipherUtil.decrypt(applicationRequest.getUserPassword());

                    if (StringUtils.isNotEmpty(password)) {
                        credentialMaster.setUserPassword(applicationRequest.getUserPassword());
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
                    File tempFile = File.createTempFile("MW-" + applicationRequest.getUserName() + "-", ".pem", keyFileDirectory);
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

        inventoryService.saveInventoryMaster(inventoryMaster);

        // Step 2. Update service_inventory table
        serviceInventoryRepository.deleteAllByInventoryId(applicationId);
        for (Long serviceId : applicationRequest.getServiceIds()) {
            ServiceInventory serviceInventory = new ServiceInventory();

            serviceMasterRepository.findById(serviceId).orElseThrow(() -> new ResourceNotFoundException("Service ID : " + serviceId + " Not Found."));

            serviceInventory.setServiceId(serviceId);
            serviceInventory.setInventoryId(inventoryMaster.getInventoryId());

            serviceInventoryRepository.save(serviceInventory);
        }

        if (analyzeFile != null) {
            // Save Source File & Archive File
            saveAnalyzeFile(analyzeFile, applicationMaster);
            // 2022.01.27 null => "" 으로 처리 요청 (from 김기정)
            applicationMaster.setSourceLocationUri("");
        } else {
            // 기존 등록된 애플리케이션 파일이 초기화되지 않도록 한다.
            if (StringUtils.isNotEmpty(applicationRequest.getSourceLocationUri())) {
                applicationMaster.setUploadSourceFileName(null);
                applicationMaster.setUploadSourceFilePath(null);

                if(!applicationRequest.getSourceLocationUri().equals(applicationMaster.getSourceLocationUri())) {
                    applicationMaster.setApplicationSize(0L);
                }
            }
        }

        // Step 3. Update application_master table
        applicationMaster.setDeployPath(applicationRequest.getDeployPath());
        applicationMaster.setSourceLocationUri(applicationRequest.getSourceLocationUri());
        applicationMaster.setAnalysisLibList(applicationRequest.getAnalysisLibList());
        applicationMaster.setAnalysisStringList(applicationRequest.getAnalysisStringList());
        applicationMaster.setDedicatedAuthenticationYn(applicationRequest.getDedicatedAuthenticationYn());

        applicationMasterRepository.save(applicationMaster);

        // Step 4. Save inventory_label table
        inventoryLabelRepository.deleteAllByInventoryId(applicationId);
        if (applicationRequest.getLabelIds() != null) {
            for (Long labelId : applicationRequest.getLabelIds()) {
                InventoryLabel inventoryLabel = new InventoryLabel();

                labelMasterRepository.findById(labelId).orElseThrow(() -> new ResourceNotFoundException("Label ID : " + labelId + " Not Found."));

                inventoryLabel.setLabelId(labelId);
                inventoryLabel.setInventoryId(inventoryMaster.getInventoryId());

                inventoryLabelRepository.save(inventoryLabel);
            }
        }

        // Step 5. Save inventory_manager table
        inventoryManagerRepository.deleteAllByInventoryId(applicationId);
        if (applicationRequest.getInventoryManagers() != null) {
            for (Manager manager : applicationRequest.getInventoryManagers()) {
                InventoryManager inventoryManager = new InventoryManager();

                userMasterRepository.findById(manager.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User ID : " + manager.getUserId() + " Not Found."));

                inventoryManager.setUserId(manager.getUserId());
                inventoryManager.setInventoryId(inventoryMaster.getInventoryId());
                inventoryManager.setManagerTypeCode(manager.getManagerTypeCode());

                inventoryManagerRepository.save(inventoryManager);
            }
        }

        ApplicationSimpleResponse response = new ApplicationSimpleResponse();
        response.setApplicationInventoryId(inventoryMaster.getInventoryId());
        response.setApplicationInventoryName(inventoryMaster.getInventoryName());

        return response;
    }

    /**
     * <pre>
     * 애플리케이션 삭제
     * </pre>
     *
     * @param projectId
     * @param applicationId
     * @param isPreventAutoDiscovery
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteApplication(Long projectId, Long applicationId, boolean isPreventAutoDiscovery) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application ID : " + applicationId + " Not Found in Project ID : " + projectId));

        ApplicationMaster applicationMaster = applicationMasterRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application ID : " + applicationId + " Not Found."));

        inventoryMaster.setDeleteYn(Domain101.Y.name());
        inventoryMaster.setModifyDatetime(new Date());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());

        if (isPreventAutoDiscovery) {
            applicationMaster.setAutomaticRegistProtectionYn(Domain101.Y.name());
        }

        DiscoveredInstanceMaster discoveredInstanceMaster = discoveredInstanceMasterRepository.findByPossessionInventoryId(applicationId).orElse(null);
        if (discoveredInstanceMaster != null) {
            discoveredInstanceMaster.setDeleteYn(Domain101.Y.name());
            // Discovered Database 등 이력을 유지하기 위해 인터페이스와 IPs 테이블은 일단 유지한다.
            // discoveredInstanceInterfaceIpsRepository.deleteAllByDiscoveredInstanceInterfaceId(discoveredInstanceMaster.getDiscoveredInstanceId());
            // discoveredInstanceInterfaceRepository.deleteAllByDiscoveredInstanceInterfaceId(discoveredInstanceMaster.getDiscoveredInstanceId());
        }
    }

    /**
     * <pre>
     * 애플리케이션 목록 Excel Download
     * </pre>
     *
     * @param projectId
     * @param serviceId
     * @param serverId
     * @return
     */
    public ByteArrayInputStream getApplicationListExcel(Long projectId, Long serviceId, Long serverId) {
        List<ApplicationExcelResponse> applicationExcels = applicationMapper.selectApplicationExcel(projectId, serviceId, serverId);

        //-> 애플리케이션 헤더 설정
        ListToExcelDto listToExcelDto = new ListToExcelDto();
        listToExcelDto.getHeaderItemList().add("Inventory Code");
        listToExcelDto.getHeaderItemList().add("Inventory Name");
        listToExcelDto.getHeaderItemList().add("Service ID");
        listToExcelDto.getHeaderItemList().add("Service Name");
        listToExcelDto.getHeaderItemList().add("Server ID");
        listToExcelDto.getHeaderItemList().add("Server Name");
        listToExcelDto.getHeaderItemList().add("Application ID");
        listToExcelDto.getHeaderItemList().add("Application Name");
        listToExcelDto.getHeaderItemList().add("Application Type");
        listToExcelDto.getHeaderItemList().add("Deploy Path");
        listToExcelDto.getHeaderItemList().add("Application Size (Byte)");
        listToExcelDto.getHeaderItemList().add("Source Location Uri");
        listToExcelDto.getHeaderItemList().add("Upload Source File Name");
        listToExcelDto.getHeaderItemList().add("Upload Source File Path");
        listToExcelDto.getHeaderItemList().add("Analysis Library List");
        listToExcelDto.getHeaderItemList().add("Analysis String List");
        listToExcelDto.getHeaderItemList().add("Dedicated Authentication YN");
        listToExcelDto.getHeaderItemList().add("Dedicated Authentication UserName");
        listToExcelDto.getHeaderItemList().add("Labels");
        listToExcelDto.getHeaderItemList().add("Description");


        ListToExcelDto.RowItem rowItem;
        for (ApplicationExcelResponse app : applicationExcels) {

            rowItem = new ListToExcelDto.RowItem();
            rowItem.getCellItemList().add(app.getCustomerInventoryCode());
            rowItem.getCellItemList().add(app.getCustomerInventoryName());
            rowItem.getCellItemList().add(app.getServiceId());
            rowItem.getCellItemList().add(app.getServiceName());
            rowItem.getCellItemList().add(app.getServerInventoryId());
            rowItem.getCellItemList().add(app.getServerInventoryName());
            rowItem.getCellItemList().add(app.getApplicationInventoryId());
            rowItem.getCellItemList().add(app.getApplicationInventoryName());
            rowItem.getCellItemList().add(app.getInventoryDetailTypeCode());
            rowItem.getCellItemList().add(app.getDeployPath());
            rowItem.getCellItemList().add((app.getApplicationSize() == null ? "" : Long.toString(app.getApplicationSize())));
            rowItem.getCellItemList().add(app.getSourceLocationUri());
            rowItem.getCellItemList().add(app.getUploadSourceFileName());
            rowItem.getCellItemList().add(app.getUploadSourceFilePath());
            rowItem.getCellItemList().add(String.join(",\r\n", app.getAnalysisLibList()));
            rowItem.getCellItemList().add(String.join(",\r\n", app.getAnalysisStringList()));
            rowItem.getCellItemList().add(app.getDedicatedAuthenticationYn());
            rowItem.getCellItemList().add(app.getUserName());
            rowItem.getCellItemList().add(app.getLabels());
            rowItem.getCellItemList().add(app.getDescription());

            listToExcelDto.getRowItemList().add(rowItem);
        }

        ByteArrayOutputStream out;
        try {
            out = ExcelUtil.listToExcel("Applications", listToExcelDto);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create application excel list.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }

    /**
     * <pre>
     * 애플리케이션 자동등록방지 필드 업데이트
     * </pre>
     *
     * @param projectId
     * @param applicationId
     * @param autoRegisterProtectionYn
     */
    @Transactional(rollbackFor = Exception.class)
    public void setAutoRegisterProtection(Long projectId, Long applicationId, String autoRegisterProtectionYn) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application ID : " + applicationId + " Not Found in Project ID : " + projectId));

        inventoryMaster.setModifyDatetime(new Date());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());

        ApplicationMaster applicationMaster = applicationMasterRepository.findById(inventoryMaster.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Application ID : " + applicationId + " Not Found."));

        applicationMaster.setAutomaticRegistProtectionYn(autoRegisterProtectionYn);
    }

    /**
     * <pre>
     * 애플리케이션 부가 정보 조회
     * </pre>
     *
     * @param application
     */
    private void setApplicationDetail(ApplicationResponse application) {
        if (application != null) {
            Server server = serverMapper.getServerSummary(application.getServerInventoryId());
            application.setServerInventoryName(server.getInventoryName());
            application.setRepresentativeIpAddress(server.getRepresentativeIpAddress());

            application.setServiceList(serviceMapper.getServiceSummaries(application.getApplicationInventoryId()));
            application.setLabelList(labelMapper.getInventoryLabelList(application.getApplicationInventoryId()));
            application.setInventoryManagers(managerMapper.getInventoryManagers(application.getApplicationInventoryId()));
            application.setDatasourceCount(getDatasources(application.getProjectId(), application.getApplicationInventoryId()).size());

            // 가장 마지막으로 성공한 process 설정
            InventoryProcess.CompleteScan completeScan = inventoryProcessMapper
                    .selectLastCompleteInventoryProcess(application.getApplicationInventoryId(), Domain1002.SCAN.name());
            if (completeScan != null) {
                application.setLastCompleteScan(completeScan);
            }

            // 마지막 Scan 데이터 설정
            InventoryProcess.Result result = inventoryProcessMapper
                    .selectLastInventoryProcess(application.getApplicationInventoryId(), Domain1002.SCAN.name());
            if (result != null) {
                application.setLastInventoryProcess(result);
            }
        }
    }

    /**
     * <pre>
     * 애플리케이션 파일 저장
     * </pre>
     *
     * @param multipartFile
     * @param applicationMaster
     * @throws IOException
     */
    private void saveAnalyzeFile(MultipartFile multipartFile, ApplicationMaster applicationMaster) throws IOException {
        if (multipartFile != null) {
            String uploadPath = APPLICATION_FILE_UPLOAD_DIR + File.separator + applicationMaster.getApplicationInventoryId();
            String analyzeFileName = multipartFile.getOriginalFilename();
            String extension = FilenameUtils.getExtension(analyzeFileName).toLowerCase();
            if (ApplicationFileType.SOURCE_FILE.isSupport(extension) || ApplicationFileType.ARCHIVE_FILE.isSupport(extension)) {
                String analyzeFilePath = FileUtil.saveFile(multipartFile, analyzeFileName, uploadPath);
                applicationMaster.setUploadSourceFileName(analyzeFileName);
                applicationMaster.setUploadSourceFilePath(analyzeFilePath);
                applicationMaster.setApplicationSize(multipartFile.getSize());
            } else {
                throw new RoRoApiException(ErrorCode.INVALID_FILE_TYPE, "Analyze file type doesn't support");
            }
        }
    }

    public List<InstanceResponse> getMiddlewareInstanceList(Long projectId, Long applicationId) {
        projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project Id : " + projectId + " Not Found."));

        inventoryMasterRepository.findById(applicationId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_APPLICATION_NOT_FOUND));

        return applicationMapper.selectApplicationMiddlewareInstance(projectId, applicationId);
    }

    public List<ApplicationExternalConnectionResponse> getApplicationExternalConnections(Long projectId, Long applicationId) {
        return applicationMapper.selectExternalConnections(projectId, applicationId);
    }
}
//end of ApplicationService.java
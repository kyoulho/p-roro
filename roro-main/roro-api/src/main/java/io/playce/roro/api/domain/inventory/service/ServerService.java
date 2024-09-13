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

import com.google.gson.Gson;
import com.jayway.jsonpath.ReadContext;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.common.aop.SubscriptionManager;
import io.playce.roro.api.domain.inventory.service.helper.InventoryProcessHelper;
import io.playce.roro.api.domain.inventory.service.helper.WindowsAssessmentHelper;
import io.playce.roro.api.domain.preconfig.service.PreConfigService;
import io.playce.roro.common.code.*;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.dto.common.ServerConnectionInfo;
import io.playce.roro.common.dto.common.excel.ListToExcelDto;
import io.playce.roro.common.dto.info.OSInfo;
import io.playce.roro.common.dto.inventory.manager.Manager;
import io.playce.roro.common.dto.inventory.process.InventoryProcess;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.dto.inventory.process.InventoryProcessRequest;
import io.playce.roro.common.dto.inventory.process.MonitoringQueueItem;
import io.playce.roro.common.dto.inventory.server.*;
import io.playce.roro.common.dto.subscription.Subscription;
import io.playce.roro.common.dto.subscription.SubscriptionStausType;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.CollectionHelper;
import io.playce.roro.common.util.ExcelUtil;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.DiskInfo;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.history.service.ServerEventHandlerService;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.common.label.LabelMapper;
import io.playce.roro.mybatis.domain.discovered.DiscoveredInstanceMapper;
import io.playce.roro.mybatis.domain.inventory.manager.ManagerMapper;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerSummaryMapper;
import io.playce.roro.mybatis.domain.inventory.service.ServiceMapper;
import io.playce.roro.scheduler.config.ScheduleConfig;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.result.AixAssessmentResult;
import io.playce.roro.svr.asmt.dto.result.HpuxAssessmentResult;
import io.playce.roro.svr.asmt.dto.result.SolarisAssessmentResult;
import io.playce.roro.svr.asmt.dto.result.WindowsAssessmentResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.playce.roro.common.util.JsonUtil.getJsonObject;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServerService {

    private final WindowsAssessmentHelper windowsAssessmentHelper;

    private final InventoryService inventoryService;
    private final PreConfigService preConfigService;
    private final MiddlewareService middlewareService;
    private final DatabaseService databaseService;
    private final ApplicationService applicationService;
    private final InventoryProcessService inventoryProcessService;
    private final InventoryProcessHelper inventoryProcessHelper;

    private final ModelMapper modelMapper;

    private final ServiceMapper serviceMapper;
    private final ServerMapper serverMapper;
    private final ServerSummaryMapper serverSummaryMapper;
    private final ManagerMapper managerMapper;
    private final LabelMapper labelMapper;
    private final InventoryProcessMapper inventoryProcessMapper;
    private final DiscoveredInstanceMapper discoveredInstanceMapper;
    private final ProjectMasterRepository projectMasterRepository;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ServerMasterRepository serverMasterRepository;
    private final ServiceInventoryRepository serviceInventoryRepository;
    private final InventoryManagerRepository inventoryManagerRepository;
    private final LabelMasterRepository labelMasterRepository;
    private final InventoryLabelRepository inventoryLabelRepository;
    private final CredentialMasterRepository credentialMasterRepository;
    private final UserMasterRepository userMasterRepository;
    private final InventoryProcessRepository inventoryProcessRepository;
    private final InventoryProcessGroupRepository inventoryProcessGroupRepository;
    private final ServerDiskInformationRepository serverDiskInformationRepository;
    private final InventoryProcessResultRepository inventoryProcessResultRepository;

    private final DiscoveredInstanceMasterRepository discoveredInstanceMasterRepository;
    private final ServerEventHandlerService serverEventHandlerService;

    private static final Gson gson = new Gson();

    private final ScheduleConfig scheduleConfig;

    public Subscription getSubscriptionWithUsedCount() {
        Subscription subscription = SubscriptionManager.getSubscription();

        // 서브스크립션 만료 여부 검사
        if (subscription.getExpireDate().getTime() < System.currentTimeMillis()) {
            subscription.setSubscriptionStausType(SubscriptionStausType.SUBSCRIPTION_EXPIRED);
        }
        serverEventHandlerService.setSubscriptionUsedCount(subscription, serverMapper.selectServerCountPerProjectId());

        return subscription;
    }

    /**
     * <pre>
     * 서버 목록 조회
     * </pre>
     */
    public List<ServerResponse> getServers(Long projectId, Long serviceId, Boolean includePreConfig) {
        List<ServerResponse> servers = serverMapper.selectServerList(projectId, serviceId);

        for (ServerResponse server : servers) {
            setServerDetail(server);
        }

        if (includePreConfig != null && includePreConfig) {
            // convert ServerResponse to ServerPreconfigResponse
            servers = servers.stream().map(s -> modelMapper.map(s, ServerPreconfigResponse.class)).collect(Collectors.toList());

            for (ServerResponse server : servers) {
                // Add preconfig for the server
                ((ServerPreconfigResponse) server).setPreConfigs(preConfigService.getPreConfigs(projectId, server.getServerInventoryId()));
            }
        }

        return servers;
    }

    /**
     * 서버 조회
     */
    public ServerDetailResponse getServer(Long projectId, Long serverId) {
        inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        ServerDetailResponse server = serverMapper.selectServerDetail(projectId, serverId);
        if (server != null) {
            setServerDetail(server);

            // https://cloud-osci.atlassian.net/browse/PCR-6486
            getVendorAndModel(server, null);

            // resource count
            server.setMiddlewareCount(middlewareService.getMiddlewares(projectId, null, serverId).size());
            server.setApplicationCount(applicationService.getApplications(projectId, null, serverId).size());
            server.setDatabaseCount(databaseService.getDatabaseEngines(projectId, null, serverId).size());
        }

        return server;
    }

    /**
     * https://cloud-osci.atlassian.net/browse/PCR-5593
     * 이중 서브밋 방지를 위한 방어코드로 @Transactional 애노테이션에는 synchronized가 동작하기 않기 때문에
     * 별도의 synchronized 메소드 내에서 @Transactional 메소드를 호출한다.
     */
    public synchronized ServerSimpleResponse createServer(Long projectId, ServerRequest serverRequest, MultipartFile keyFile) {
        return createServerInternal(projectId, serverRequest, keyFile);
    }

    /**
     * <pre>
     * 서버 등록
     * </pre>
     */
    @Transactional(rollbackFor = Exception.class)
    public ServerSimpleResponse createServerInternal(Long projectId, ServerRequest serverRequest, MultipartFile keyFile) {
        // 윈도우인 경우 monitoringYn은 무조건 N이다.
        // 추후 윈도우 모니터링을 지원할 경우 삭제한다.
        // ---------------------------------------------------------------
        if (serverRequest.getWindowsYn().equals(Domain101.Y.name())) {
            serverRequest.setMonitoringYn(Domain101.N.name());
        }
        // ---------------------------------------------------------------

        ProjectMaster projectMaster = projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));

        // IP:Port 중복 체크
        if (isDuplicateServer(projectId, serverRequest.getRepresentativeIpAddress(), serverRequest.getConnectionPort(), null)) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_DUPLICATE_IP_PORT);
        }

        // 서비스 체크
        if (serverRequest.getServiceIds() == null || serverRequest.getServiceIds().isEmpty()) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_SERVICE_REQUIRED);
        }

        // 서버 네임이 없을 경우 Ip Address로 대체
        if (serverRequest.getServerInventoryName() == null || StringUtils.isEmpty(serverRequest.getServerInventoryName())) {
            serverRequest.setServerInventoryName(serverRequest.getRepresentativeIpAddress());
        }

        // enableSuYn이 'Y' 인 경우 rootPassword는 필수
        if (Domain101.Y.name().equals(serverRequest.getEnableSuYn()) && StringUtils.isEmpty(serverRequest.getRootPassword())) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_ROOT_PASS_REQUIRED);
        }

        // monitoringCycle 필드 Cron Expression 유효성 체크
        if (Domain101.Y.name().equals(serverRequest.getMonitoringYn()) && StringUtils.isNotEmpty(serverRequest.getMonitoringCycle())) {
            if (!CronExpression.isValidExpression(serverRequest.getMonitoringCycle())) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_CRON_EXPRESSION_NOT_VALID);
            }
        }

        // Step 1. Save credential_master Table
        CredentialMaster credentialMaster = new CredentialMaster();
        credentialMaster.setProjectId(projectId);
        credentialMaster.setUserName(serverRequest.getUserName());
        credentialMaster.setCredentialTypeCode(Domain1001.SVR.name());
        credentialMaster.setDeleteYn(Domain101.N.name());
        credentialMaster.setRegistUserId(WebUtil.getUserId());
        credentialMaster.setRegistDatetime(new Date());
        credentialMaster.setModifyUserId(WebUtil.getUserId());
        credentialMaster.setModifyDatetime(new Date());

        if (StringUtils.isNotEmpty(serverRequest.getUserPassword())) {
            credentialMaster.setUserPassword(serverRequest.getUserPassword());
        }

        if (keyFile != null && keyFile.getSize() > 0) {
            try {
                // Do NOT save the key file under the repositoryPath for security reason.
                File keyFileDirectory = new File(CommonProperties.getWorkDir() + File.separator + "keyFiles");
                if (!keyFileDirectory.exists()) {
                    keyFileDirectory.mkdir();
                }
                File tempFile = File.createTempFile(serverRequest.getRepresentativeIpAddress() + "-", ".pem", keyFileDirectory);
                String keyStr = IOUtils.toString(keyFile.getInputStream(), StandardCharsets.UTF_8);

                if (!keyStr.startsWith("-----BEGIN RSA PRIVATE KEY")) {
                    throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
                }

                if (keyStr.length() > 4096) {
                    throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE_SIZE);
                }

                credentialMaster.setKeyFileName(keyFile.getOriginalFilename());
                credentialMaster.setKeyFilePath(tempFile.getAbsolutePath());
                credentialMaster.setKeyFileContent(keyStr);

                IOUtils.write(keyStr, new FileOutputStream(tempFile), "UTF-8");
            } catch (IOException e) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
            }
        }
        credentialMaster = credentialMasterRepository.save(credentialMaster);

        if (keyFile != null && keyFile.getSize() == 0) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
        }

        // Step 2. Save inventory_master Table
        InventoryMaster inventoryMaster = new InventoryMaster();
        inventoryMaster.setProjectId(projectMaster.getProjectId());
        inventoryMaster.setInventoryTypeCode(StringUtils.defaultIfEmpty(serverRequest.getInventoryTypeCode(), Domain1001.SVR.name()));
        inventoryMaster.setInventoryDetailTypeCode(serverRequest.getInventoryDetailTypeCode());
        inventoryMaster.setInventoryAnalysisYn(Domain101.Y.name());
        inventoryMaster.setCustomerInventoryCode(serverRequest.getCustomerInventoryCode());
        inventoryMaster.setCustomerInventoryName(serverRequest.getCustomerInventoryName());
        inventoryMaster.setInventoryName(serverRequest.getServerInventoryName());
        inventoryMaster.setInventoryIpTypeCode(StringUtils.defaultIfEmpty(serverRequest.getInventoryIpTypeCode(), Domain1006.INV.name()));
        inventoryMaster.setDeleteYn(Domain101.N.name());
        inventoryMaster.setAutomaticRegistYn(Domain101.N.name());
        inventoryMaster.setDescription(serverRequest.getDescription());
        inventoryMaster.setRegistUserId(WebUtil.getUserId());
        inventoryMaster.setRegistDatetime(new Date());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());
        inventoryMaster.setCredentialId(credentialMaster.getCredentialId());

        // Customer Inventory Code 중복을 체크하기 위해 inventoryService를 사용한다.
        inventoryMaster = inventoryService.saveInventoryMaster(inventoryMaster);

        // Step 3. Save service_inventory table
        if (serverRequest.getServiceIds() != null && !serverRequest.getServiceIds().isEmpty()) {
            ServiceInventory serviceInventory;
            for (Long serviceId : serverRequest.getServiceIds()) {
                serviceInventory = new ServiceInventory();

                serviceMasterRepository.findById(serviceId).orElseThrow(() -> new ResourceNotFoundException("Service ID : " + serviceId + " Not Found."));

                serviceInventory.setInventoryId(inventoryMaster.getInventoryId());
                serviceInventory.setServiceId(serviceId);

                serviceInventoryRepository.save(serviceInventory);
            }
        }

        // Step 4. Save server_master table
        ServerMaster serverMaster = modelMapper.map(serverRequest, ServerMaster.class);
        serverMaster.setServerInventoryId(inventoryMaster.getInventoryId());
        serverMaster.setAutomaticAnalysisYn(Domain101.Y.name());
        serverMaster.setMonitoringYn(serverRequest.getMonitoringYn());
        if (Domain101.Y.name().equals(serverMaster.getMonitoringYn())) {
            serverMaster.setMonitoringCycle(serverRequest.getMonitoringCycle());
            serverMaster.setMonitoringStartDatetime(serverRequest.getMonitoringStartDatetime());
            serverMaster.setMonitoringEndDatetime(serverRequest.getMonitoringEndDatetime());
        }

        if (StringUtils.isEmpty(serverMaster.getScheduledAssessmentYn())) {
            serverMaster.setScheduledAssessmentYn(Domain101.N.name());
        }

        // Step 5. Discovered Instance 체크
        //updateDiscoveredInstance(inventoryMaster, serverMaster, true);
        List<Long> existDiscoveredInstanceId = null;
        /*
         * discovered id를 파라미터로 받으면 Add To Inventory로 해당 inventory Update
         * Inventory로 등록 시, Discovered Instance에 이미 존재하는 ip의 경우 해당 instance Id 목록을 가져와서 update
         */
        if (serverRequest.getDiscoveredInstanceId() != null) {
            existDiscoveredInstanceId = new ArrayList<>();
            existDiscoveredInstanceId.add(serverRequest.getDiscoveredInstanceId());
        } else {
            existDiscoveredInstanceId = getExistDiscoveredServerInstanceId(projectId, serverRequest.getRepresentativeIpAddress());
        }

        if (CollectionUtils.isNotEmpty(existDiscoveredInstanceId)) {
            for (Long discoveredId : existDiscoveredInstanceId) {
                DiscoveredInstanceMaster discoveredInstanceMaster = discoveredInstanceMasterRepository.findById(discoveredId)
                        .orElseThrow(() -> new ResourceNotFoundException("Discovered Instance ID : " + serverRequest.getDiscoveredInstanceId() + " Not Found."));
                serverMaster.setDiscoveredServerYn(Domain101.Y.name());

                discoveredInstanceMaster.setPossessionInventoryId(inventoryMaster.getInventoryId());
                inventoryMaster.setInventoryIpTypeCode(Domain1006.DISC.name());
                inventoryMaster.setInventoryDiscoveredDatetime(discoveredInstanceMaster.getRegistDatetime());
            }
        } else {
            serverMaster.setDiscoveredServerYn(Domain101.N.name());
        }
        serverMasterRepository.save(serverMaster);

        // Step 6. Save inventory_label table
        if (serverRequest.getLabelIds() != null) {
            for (Long labelId : serverRequest.getLabelIds()) {
                InventoryLabel inventoryLabel = new InventoryLabel();

                labelMasterRepository.findById(labelId).orElseThrow(() -> new ResourceNotFoundException("Label ID : " + labelId + " Not Found."));

                inventoryLabel.setLabelId(labelId);
                inventoryLabel.setInventoryId(inventoryMaster.getInventoryId());

                inventoryLabelRepository.save(inventoryLabel);
            }
        }

        // Step 7. Save inventory_manager table
        if (serverRequest.getInventoryManagers() != null) {
            for (Manager manager : serverRequest.getInventoryManagers()) {
                InventoryManager inventoryManager = new InventoryManager();

                userMasterRepository.findById(manager.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User ID : " + manager.getUserId() + " Not Found."));

                inventoryManager.setUserId(manager.getUserId());
                inventoryManager.setInventoryId(inventoryMaster.getInventoryId());
                inventoryManager.setManagerTypeCode(manager.getManagerTypeCode());

                inventoryManagerRepository.save(inventoryManager);
            }
        }

        // Step 8. Assessment가 아닌 Prerequisite 요청이 자동 등록되어야 함
        // assessmentService.createAssessment(projectId, serverMaster.getServerInventoryId());
        List<Long> inventoryIds = new ArrayList<>();
        inventoryIds.add(serverMaster.getServerInventoryId());

        InventoryProcessRequest inventoryProcessRequest = new InventoryProcessRequest();
        inventoryProcessRequest.setInventoryTypeCode(Domain1001.SVR.name());
        inventoryProcessRequest.setInventoryIds(inventoryIds);

        inventoryProcessService.addInventoryProcess(projectId, inventoryProcessRequest, Domain1002.PREQ);

        ServerSimpleResponse response = new ServerSimpleResponse();
        response.setServerInventoryId(inventoryMaster.getInventoryId());
        response.setServerInventoryName(inventoryMaster.getInventoryName());

        return response;
    }

    /**
     * <pre>
     * 서버 수정
     * </pre>
     */
    @Transactional
    public ServerSimpleResponse modifyServer(Long projectId, Long serverId, ServerRequest serverRequest, MultipartFile keyFile) {
        // 윈도우인 경우 monitoringYn은 무조건 N이다.
        // 추후 윈도우 모니터링을 지원할 경우 삭제한다.
        // ---------------------------------------------------------------
        if (serverRequest.getWindowsYn().equals(Domain101.Y.name())) {
            serverRequest.setMonitoringYn(Domain101.N.name());
        }
        // ---------------------------------------------------------------

        InventoryMaster inventoryMaster = inventoryMasterRepository.findById(serverId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));

        ServerMaster serverMaster = serverMasterRepository.findById(serverId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));

        // IP:Port 중복 체크
        if (isDuplicateServer(projectId, serverRequest.getRepresentativeIpAddress(), serverRequest.getConnectionPort(), serverMaster.getServerInventoryId())) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_DUPLICATE_IP_PORT);
        }

        // monitoringCycle 필드 Cron Expression 유효성 체크
        if (Domain101.Y.name().equals(serverRequest.getMonitoringYn()) && StringUtils.isNotEmpty(serverRequest.getMonitoringCycle())) {
            if (!CronExpression.isValidExpression(serverRequest.getMonitoringCycle())) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_CRON_EXPRESSION_NOT_VALID);
            }
        }

        // 서버 ip 수정 시에 한번이라도 Assessment Scan이 수행되었는지 확인 후, 수행 히스토리가 있으면 Exception 발생, 없으면 IP Address 수정 가능
        if (!isAvaliableModifyIpAddress(serverMaster, serverRequest)) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_MODIFY);
        }

        // Step 1. Discovered Instance 체크
        //updateDiscoveredInstance(inventoryMaster, serverMaster, false);

        // Step 2. Update inventory_master Table
        inventoryMaster.setInventoryDetailTypeCode(serverRequest.getInventoryDetailTypeCode());
        inventoryMaster.setCustomerInventoryCode(serverRequest.getCustomerInventoryCode());
        inventoryMaster.setCustomerInventoryName(serverRequest.getCustomerInventoryName());
        inventoryMaster.setInventoryName(serverRequest.getServerInventoryName());
        inventoryMaster.setDescription(serverRequest.getDescription());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());

        // Customer Inventory Code 중복을 체크하기 위해 inventoryService를 사용한다.
        // inventoryMasterRepository.save(inventoryMaster);
        inventoryService.saveInventoryMaster(inventoryMaster);

        // Step 3. Update credential_master table
        CredentialMaster credentialMaster = credentialMasterRepository.findByProjectIdAndCredentialId(projectId, serverRequest.getCredentialId());
        if (credentialMaster != null) {
            if ((keyFile != null && !keyFile.isEmpty()) || !StringUtils.isEmpty(serverRequest.getUserPassword())) {
                if (keyFile != null && !keyFile.isEmpty()) {
                    try {
                        // Do NOT save the key file under the repositoryPath for security reason.
                        File keyFileDirectory = new File(CommonProperties.getWorkDir() + File.separator + "keyFiles");
                        if (!keyFileDirectory.exists()) {
                            keyFileDirectory.mkdir();
                        }
                        File tempFile = File.createTempFile(serverRequest.getRepresentativeIpAddress() + "-", ".pem", keyFileDirectory);
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

                if (keyFile != null && keyFile.getSize() == 0) {
                    throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
                }

                if (StringUtils.isNotEmpty(serverRequest.getUserPassword())) {
                    try {
                        String password = GeneralCipherUtil.decrypt(serverRequest.getUserPassword());

                        if (StringUtils.isNotEmpty(password)) {
                            credentialMaster.setUserPassword(serverRequest.getUserPassword());
                            credentialMaster.setKeyFileName(null);
                            credentialMaster.setKeyFilePath(null);
                            credentialMaster.setKeyFileContent(null);
                        }
                    } catch (Exception e) {
                        log.warn("Unable decrypt server password. [Reason] : ", e);
                        throw new RoRoApiException(ErrorCode.INVENTORY_INVALID_USER_PASSWORD);
                    }
                }

                credentialMaster.setUserName(serverRequest.getUserName());
                credentialMaster.setCredentialTypeCode(Domain1001.SVR.name());
                credentialMaster.setModifyUserId(WebUtil.getUserId());
                credentialMaster.setModifyDatetime(new Date());
            } else {
                if (!credentialMaster.getUserName().equals(serverRequest.getUserName())) {
                    credentialMaster.setUserName(serverRequest.getUserName());
                    credentialMaster.setModifyUserId(WebUtil.getUserId());
                    credentialMaster.setModifyDatetime(new Date());
                }
            }
        } else {
            throw new RoRoApiException(ErrorCode.RESOURCE_NOT_FOUND, "Credential does not exist.");
        }

        // Step 4. Save service_inventory table
        serviceInventoryRepository.deleteAllByInventoryId(serverId);
        if (serverRequest.getServiceIds() != null && !serverRequest.getServiceIds().isEmpty()) {
            ServiceInventory serviceInventory;
            for (Long serviceId : serverRequest.getServiceIds()) {
                serviceInventory = new ServiceInventory();

                serviceMasterRepository.findById(serviceId).orElseThrow(() -> new ResourceNotFoundException("Service ID : " + serviceId + " Not Found."));

                serviceInventory.setInventoryId(inventoryMaster.getInventoryId());
                serviceInventory.setServiceId(serviceId);

                serviceInventoryRepository.save(serviceInventory);
            }
        }

        // Step 5. Save server_master table
        String originRootPassword = serverMaster.getRootPassword();
        Long origineServerInventoryId = serverMaster.getServerInventoryId();
        modelMapper.map(serverRequest, serverMaster);
        serverMaster.setServerInventoryId(origineServerInventoryId);
        if (StringUtils.isEmpty(serverRequest.getScheduledAssessmentYn())) {
            serverMaster.setScheduledAssessmentYn(Domain101.N.name());
        }
        if (StringUtils.isEmpty(serverRequest.getEnableSuYn())) {
            serverMaster.setEnableSuYn(Domain101.N.name());
        }
        if (Domain101.Y.name().equals(serverRequest.getEnableSuYn()) && StringUtils.isEmpty(serverRequest.getRootPassword())) {
            serverMaster.setRootPassword(originRootPassword);
        }

        // Monigoring 필드가 빈 값일 경우 null로 초기화
        if (Domain101.Y.name().equals(serverRequest.getMonitoringYn())) {
            if (StringUtils.isEmpty(serverRequest.getMonitoringCycle())) {
                serverMaster.setMonitoringCycle(null);
            }
            if (serverRequest.getMonitoringStartDatetime() == null) {
                serverMaster.setMonitoringStartDatetime(null);
            }
            if (serverRequest.getMonitoringEndDatetime() == null) {
                serverMaster.setMonitoringEndDatetime(null);
            }
        }

        // Monitoring Enable 값이 N인 경우 해당 필드 null로 초기화
        if (Domain101.N.name().equals(serverRequest.getMonitoringYn())) {
            serverMaster.setMonitoringCycle(null);
            serverMaster.setMonitoringStartDatetime(null);
            serverMaster.setMonitoringEndDatetime(null);
        }

        // Step 6. Save inventory_label table
        List<InventoryLabel> inventoryLabels = inventoryLabelRepository.findByInventoryId(serverId);
        if (CollectionUtils.isNotEmpty(inventoryLabels)) {
            inventoryLabelRepository.deleteAllByInventoryId(serverId);
        }
        if (serverRequest.getLabelIds() != null) {
            for (Long labelId : serverRequest.getLabelIds()) {
                InventoryLabel inventoryLabel = new InventoryLabel();

                labelMasterRepository.findById(labelId).orElseThrow(() -> new ResourceNotFoundException("Label ID : " + labelId + " Not Found."));

                inventoryLabel.setLabelId(labelId);
                inventoryLabel.setInventoryId(inventoryMaster.getInventoryId());

                inventoryLabelRepository.save(inventoryLabel);
            }
        }

        // Step 7. Save inventory_manager table
        inventoryManagerRepository.deleteAllByInventoryId(serverId);
        if (serverRequest.getInventoryManagers() != null) {
            for (Manager manager : serverRequest.getInventoryManagers()) {
                InventoryManager inventoryManager = new InventoryManager();

                userMasterRepository.findById(manager.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User ID : " + manager.getUserId() + " Not Found."));

                inventoryManager.setUserId(manager.getUserId());
                inventoryManager.setInventoryId(inventoryMaster.getInventoryId());
                inventoryManager.setManagerTypeCode(manager.getManagerTypeCode());

                inventoryManagerRepository.save(inventoryManager);
            }
        }

        SSHUtil.clearSession(credentialMaster.getUserName() + "@" + serverMaster.getRepresentativeIpAddress() + ":" + serverMaster.getConnectionPort());

        ServerSimpleResponse response = new ServerSimpleResponse();
        response.setServerInventoryId(inventoryMaster.getInventoryId());
        response.setServerInventoryName(inventoryMaster.getInventoryName());

        return response;
    }

    /**
     * <pre>
     * 서버 삭제
     * </pre>
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteServer(Long projectId, Long serverId) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        int middlewareCount = serverMapper.selectMiddlewareCountByProjectIdAndServerId(projectId, serverId);
        int applicationCount = serverMapper.selectApplicationCountByProjectIdAndServerId(projectId, serverId);
        int databaseCount = serverMapper.selectDatabaseCountByProjectIdAndServerId(projectId, serverId);

        if (middlewareCount > 0 || applicationCount > 0 || databaseCount > 0) {
            String[] parameters = {String.valueOf(middlewareCount), String.valueOf(applicationCount), String.valueOf(databaseCount)};
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_DELETED_FAIL, parameters);
        }

        // // 매핑 된 manager 를 삭제한다.
        // inventoryManagerRepository.deleteByInventoryId(inventoryMaster.getInventoryId());
        //
        // // 매핑 된 service 를 삭제한다.
        // serviceInventoryRepository.deleteByInventoryId(inventoryMaster.getInventoryId());

        DiscoveredInstanceMaster discoveredInstanceMaster = discoveredInstanceMasterRepository.findByPossessionInventoryId(serverId).orElse(null);
        if (discoveredInstanceMaster != null) {
            discoveredInstanceMaster.setPossessionInventoryId(null);
        }

        inventoryMaster.setDeleteYn(Domain101.Y.name());
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(new Date());
    }

    /**
     * 서버 목록 Excel Download
     */
    public ByteArrayInputStream getServerListExcel(Long projectId, Long serviceId) {
        List<ServerExcelResponse> serverExcels = serverMapper.selectServerExcel(projectId, serviceId);

        //-> 서버 헤더
        ListToExcelDto listToExcelDto = new ListToExcelDto();
        listToExcelDto.getHeaderItemList().add("Inventory Code");
        listToExcelDto.getHeaderItemList().add("Inventory Name");
        listToExcelDto.getHeaderItemList().add("Service ID");
        listToExcelDto.getHeaderItemList().add("Service Name");
        listToExcelDto.getHeaderItemList().add("Server ID");
        listToExcelDto.getHeaderItemList().add("Server Name");
        listToExcelDto.getHeaderItemList().add("IP Address");
        listToExcelDto.getHeaderItemList().add("SSH Port");
        listToExcelDto.getHeaderItemList().add("Username");
        listToExcelDto.getHeaderItemList().add("Enable su Y/N");
        listToExcelDto.getHeaderItemList().add("Windows Y/N");
        listToExcelDto.getHeaderItemList().add("Migration Type");
        listToExcelDto.getHeaderItemList().add("Server Location");
        listToExcelDto.getHeaderItemList().add("Environment");
        listToExcelDto.getHeaderItemList().add("Hypervisor Type");
        listToExcelDto.getHeaderItemList().add("Cluster Type");
        listToExcelDto.getHeaderItemList().add("Access Control");
        listToExcelDto.getHeaderItemList().add("tpmc");
        listToExcelDto.getHeaderItemList().add("Purchase Date");
        listToExcelDto.getHeaderItemList().add("Manufacturer");
        listToExcelDto.getHeaderItemList().add("Model");
        listToExcelDto.getHeaderItemList().add("Serial Number");
        listToExcelDto.getHeaderItemList().add("Labels");
        listToExcelDto.getHeaderItemList().add("Description");

        ListToExcelDto.RowItem rowItem;
        for (ServerExcelResponse svr : serverExcels) {

            rowItem = new ListToExcelDto.RowItem();

            // 데이터 설정
            rowItem.getCellItemList().add(svr.getCustomerInventoryCode());
            rowItem.getCellItemList().add(svr.getCustomerInventoryName());
            rowItem.getCellItemList().add(svr.getServiceId());
            rowItem.getCellItemList().add(svr.getServiceName());
            rowItem.getCellItemList().add(svr.getServerInventoryId());
            rowItem.getCellItemList().add(svr.getServerInventoryName());
            rowItem.getCellItemList().add(svr.getRepresentativeIpAddress());
            rowItem.getCellItemList().add(svr.getConnectionPort());
            rowItem.getCellItemList().add(svr.getUserName());
            rowItem.getCellItemList().add(svr.getEnableSuYn());
            rowItem.getCellItemList().add(svr.getWindowsYn());
            rowItem.getCellItemList().add(svr.getMigrationTypeCode());
            rowItem.getCellItemList().add(svr.getServerLocation());
            rowItem.getCellItemList().add(svr.getServerUsageTypeCode());
            rowItem.getCellItemList().add(svr.getHypervisorTypeCode());
            rowItem.getCellItemList().add(svr.getDualizationTypeCode());
            rowItem.getCellItemList().add(svr.getAccessControlSystemSolutionName());
            rowItem.getCellItemList().add(svr.getTpmc());
            rowItem.getCellItemList().add(svr.getBuyDate());
            rowItem.getCellItemList().add(svr.getMakerName());
            rowItem.getCellItemList().add(svr.getModelName());
            rowItem.getCellItemList().add(svr.getSerialNumber());
            rowItem.getCellItemList().add(svr.getLabels());
            rowItem.getCellItemList().add(svr.getDescription());

            listToExcelDto.getRowItemList().add(rowItem);
        }

        ByteArrayOutputStream out;
        try {
            out = ExcelUtil.listToExcel("Servers", listToExcelDto);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create server excel list.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }

    @Transactional
    @SneakyThrows
    public Long createInventoryProcessByFile(Long projectId, Long serverId, MultipartFile assessmentFile) {
        ServerDetailResponse server = serverMapper.selectServerDetail(projectId, serverId);

        if (server == null) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND);
        }

        String jsonStr = null;
        try {
            jsonStr = new String(assessmentFile.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while read assessment result file.", e);
            throw new RoRoApiException(ErrorCode.MANUAL_ASSESSMENT_INVALID_FILE);
        }

        // 1. inventory Process group 생성
        InventoryProcessGroup inventoryProcessGroup = new InventoryProcessGroup();
        inventoryProcessGroup.setRegistUserId(WebUtil.getUserId());
        inventoryProcessGroup.setRegistDatetime(new Date());
        inventoryProcessGroup = inventoryProcessGroupRepository.save(inventoryProcessGroup);

        // 2. inventory Process 생성
        io.playce.roro.jpa.entity.InventoryProcess inventoryProcess = new io.playce.roro.jpa.entity.InventoryProcess();
        inventoryProcess.setInventoryProcessGroupId(inventoryProcessGroup.getInventoryProcessGroupId());
        inventoryProcess.setInventoryId(server.getServerInventoryId());
        inventoryProcess.setInventoryProcessTypeCode(Domain1002.SCAN.name());
        inventoryProcess.setInventoryProcessResultCode(Domain1003.PROC.name());
        inventoryProcess.setRegistUserId(WebUtil.getUserId());
        inventoryProcess.setRegistDatetime(new Date());
        inventoryProcess.setModifyUserId(WebUtil.getUserId());
        inventoryProcess.setModifyDatetime(new Date());
        inventoryProcess.setInventoryProcessStartDatetime(new Date());
        inventoryProcess.setDeleteYn("N");
        inventoryProcess = inventoryProcessRepository.save(inventoryProcess);

        ReadContext ctx = null;
        Object summaryObject;
        Object resultObject;

        // 결과 Parsing
        if ("Y".equals(server.getWindowsYn())) {
            // TODO Windows assessment 로직 처리
            JSONObject jsonObject = getJsonObject((JSONObject) new JSONParser().parse(jsonStr));

            WindowsAssessmentResult windowsResult = windowsAssessmentHelper.getAssessment(jsonObject);
            summaryObject = windowsResult;
            resultObject = windowsResult;

        } else {
            // window가 아니면 exception 처리
            throw new RoRoApiException(ErrorCode.ASSESSMENT_RESULT_UPLOAD_NOT_SUPPORT);
        }

        try {
            // 4. server summary & result 설정
            InventoryMaster inventory = inventoryMasterRepository.findById(inventoryProcess.getInventoryId())
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));

            ServerConnectionInfo serverConnectionInfo = serverMapper.selectServerConnectionInfoByInventoryId(serverId);
            InventoryProcessConnectionInfo connectionInfo = new InventoryProcessConnectionInfo();
            connectionInfo.setInventoryProcessId(inventoryProcess.getInventoryProcessId());
            connectionInfo.setProjectId(serverConnectionInfo.getProjectId());
            connectionInfo.setInventoryId(serverConnectionInfo.getInventoryId());
            connectionInfo.setDeleteYn(serverConnectionInfo.getDeleteYn());
            connectionInfo.setRepresentativeIpAddress(serverConnectionInfo.getRepresentativeIpAddress());
            connectionInfo.setUserName(serverConnectionInfo.getUserName());
            connectionInfo.setUserPassword(GeneralCipherUtil.encrypt(serverConnectionInfo.getUserPassword()));
            connectionInfo.setKeyFilePath(serverConnectionInfo.getKeyFilePath());
            connectionInfo.setKeyFileContent(serverConnectionInfo.getKeyFileContent());
            connectionInfo.setConnectionPort(serverConnectionInfo.getConnectionPort());
            connectionInfo.setEnableSuYn(serverConnectionInfo.getEnableSuYn());
            connectionInfo.setRootPassword(GeneralCipherUtil.encrypt(serverConnectionInfo.getRootPassword()));
            connectionInfo.setWindowsYn(serverConnectionInfo.getWindowsYn());

            inventoryProcessHelper.runPostProcessing(connectionInfo, (WindowsAssessmentResult) summaryObject);

            Object entity = getServer(server.getProjectId(), server.getServerInventoryId());

            inventoryProcessHelper.saveResult(server.getProjectId(), inventoryProcess, server, resultObject, entity);

        } catch (Exception e) {
            log.error("Unhandled exception occurred while processing assessment result", e);
            throw new RoRoApiException(ErrorCode.MANUAL_ASSESSMENT_PROCESSING_FAILED, e.getMessage());
        }

        // inventory process 결과 save
        // inventoryProcess.setInventoryProcessResultCode(Domain1003.CMPL.name());
        // inventoryProcess.setInventoryProcessEndDatetime(new Date());
        // inventoryProcess = inventoryProcessRepository.save(inventoryProcess);

//        try {
//            InventoryMaster inventory = inventoryMasterRepository.findById(inventoryProcess.getInventoryId()).orElse(null);
//
//            // save server summary
//            if (inventory != null && inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.CMPL.name())) {
//                summaryHelper.serverSummary(inventoryProcess.getInventoryProcessId(), server);
//                log.info("save server summary, inventory process id: {}", inventoryProcess.getInventoryProcessId());
//            }
//
//            // save middleware summary
//            // if (inventory != null && assessment.getStatus() == AssessmentStatusType.COMPLETED) {
//            //     summaryComponent.middlewareSummary(assessment.getId());
//            //     log.info("save middleware summary, inventory process id: {}", inventoryProcess.getId());
//            // }
//        } catch (Exception e) {
//            log.error("Unhandled exception occurred while save inventory process summary", e);
//        }

        return inventoryProcess.getInventoryProcessId();
    }

    // https://cloud-osci.atlassian.net/browse/PCR-6486
    public void getVendorAndModel(ServerResponse server, InventoryProcessResult inventoryProcessResult) {
        if (StringUtils.isEmpty(server.getMakerName()) || StringUtils.isEmpty(server.getModelName()) || StringUtils.isEmpty(server.getSerialNumber())) {
            InventoryProcess.CompleteScan completeScan = server.getLastCompleteScan();

            if (completeScan == null) {
                completeScan = inventoryProcessMapper.selectLastCompleteInventoryProcess(server.getServerInventoryId(), Domain1002.SCAN.name());
                server.setLastCompleteScan(completeScan);
            }

            if (completeScan != null) {
                ServerAssessmentResult result = null;

                try {
                    if (inventoryProcessResult == null) {
                        inventoryProcessResult = inventoryProcessResultRepository.findByInventoryProcessId(completeScan.getInventoryProcessId());
                    }

                    String jsonStr = inventoryProcessResult.getInventoryProcessResultJson();

                    if (Domain1013.LINUX.name().equals(server.getInventoryDetailTypeCode())) {
                        result = gson.fromJson(jsonStr, ServerAssessmentResult.class);
                    } else if (Domain1013.AIX.name().equals(server.getInventoryDetailTypeCode())) {
                        result = gson.fromJson(jsonStr, AixAssessmentResult.class);
                    } else if (Domain1013.HP_UX.name().equals(server.getInventoryDetailTypeCode())) {
                        result = gson.fromJson(jsonStr, HpuxAssessmentResult.class);
                    } else if (Domain1013.SUNOS.name().equals(server.getInventoryDetailTypeCode())) {
                        result = gson.fromJson(jsonStr, SolarisAssessmentResult.class);
                    } else if (Domain1013.WINDOWS.name().equals(server.getInventoryDetailTypeCode())) {
                        WindowsAssessmentResult.WindowsResult r = gson.fromJson(jsonStr, WindowsAssessmentResult.WindowsResult.class);

                        if (r != null && r.getSystemInformation() != null) {
                            if (StringUtils.isEmpty(server.getMakerName()) && StringUtils.isNotEmpty(r.getSystemInformation().getManufacturer())) {
                                server.setMakerName(r.getSystemInformation().getManufacturer());
                            }

                            if (StringUtils.isEmpty(server.getModelName()) && StringUtils.isNotEmpty(r.getSystemInformation().getModel())) {
                                server.setModelName(r.getSystemInformation().getModel());
                            }
                        }
                    }
                } catch (Exception e) {
                    // ignore
                    log.warn("Unhandled exception occurred while binding to ServerAssessmentResult. Reason : [{}]", e.getMessage());
                }

                if (result != null) {
                    if (StringUtils.isEmpty(server.getMakerName()) && StringUtils.isNotEmpty(result.getSystemVendor())) {
                        server.setMakerName(result.getSystemVendor());
                    }

                    if (StringUtils.isEmpty(server.getModelName()) && StringUtils.isNotEmpty(result.getProductName())) {
                        server.setModelName(result.getProductName());
                    }

                    if (StringUtils.isEmpty(server.getSerialNumber()) && StringUtils.isNotEmpty(result.getProductSerial())) {
                        server.setSerialNumber(result.getProductSerial());
                    }
                }
            }
        }
    }

    /**
     * <pre>
     * 서버 요약 정보 등록
     * </pre>
     *
     * @param server
     */
    private void setServerDetail(ServerResponse server) {
        if (server != null) {
            server.setServices(serviceMapper.getServiceSummaries(server.getServerInventoryId()));
            server.setServerSummary(serverSummaryMapper.selectServerSummary(server.getServerInventoryId()));
            server.setLabelList(labelMapper.getInventoryLabelList(server.getServerInventoryId()));
            server.setInventoryManagers(managerMapper.getInventoryManagers(server.getServerInventoryId()));

            // 가장 마지막으로 성공한 process 설정
            InventoryProcess.CompleteScan completeScan = inventoryProcessMapper
                    .selectLastCompleteInventoryProcess(server.getServerInventoryId(), Domain1002.SCAN.name());
            if (completeScan != null) {
                server.setLastCompleteScan(completeScan);
            }

            // 마지막 Preq, Scan, Mig 데이터 설정
            InventoryProcess.Result result = inventoryProcessMapper.selectLastInventoryProcess(server.getServerInventoryId(), Domain1002.PREQ.name());
            if (result != null) {
                server.getLastInventoryProcesses().add(result);
            }

            result = inventoryProcessMapper.selectLastInventoryProcess(server.getServerInventoryId(), Domain1002.SCAN.name());
            if (result != null) {
                server.getLastInventoryProcesses().add(result);
            }

            result = inventoryProcessMapper.selectLastInventoryProcess(server.getServerInventoryId(), Domain1002.MIG.name());
            if (result != null) {
                server.getLastInventoryProcesses().add(result);
            }
        }
    }

    @Transactional
    public Map<String, Object> getServerDiskUsage(Long projectId, Long serverInventoryId) {
        // 유효한 프로젝트, 인벤토리, 크리덴셜인지 체크
        projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found"));

        InventoryMaster inventoryMaster = inventoryMasterRepository.findById(serverInventoryId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));

        ServerMaster serverMaster = serverMasterRepository.findById(inventoryMaster.getInventoryId())
                .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));

        CredentialMaster credentialMaster = credentialMasterRepository.findById(inventoryMaster.getCredentialId())
                .orElseThrow(() -> new ResourceNotFoundException("Credential ID : " + inventoryMaster.getCredentialId() + " Not Found."));

        String command;
        String diskUsage;
        Pattern pattern = Pattern.compile("^/dev/");
        Map<String, Object> diskInfoMap = new LinkedHashMap<>();

        try {
            TargetHost targetHost = new TargetHost();
            targetHost.setIpAddress(serverMaster.getRepresentativeIpAddress());
            targetHost.setPort(serverMaster.getConnectionPort());
            targetHost.setUsername(credentialMaster.getUserName());
            targetHost.setPassword(GeneralCipherUtil.decrypt(credentialMaster.getUserPassword()));
            targetHost.setKeyFilePath(credentialMaster.getKeyFilePath());
            targetHost.setKeyString(credentialMaster.getKeyFileContent());

            // uname 조회
            String uname = SSHUtil.executeCommand(targetHost, "uname").trim();
            uname = uname.replaceAll("-", " ");

            String checkUname = uname;
            if (Arrays.stream(Domain1013.values()).noneMatch(t -> t.enname().equals(checkUname.toUpperCase()))) {
                throw new RuntimeException(uname + " is an unsupported OS Type.");
            }

            String[] splitLines;
            String newLine = "";

            switch (Domain1013.valueOf(inventoryMaster.getInventoryDetailTypeCode())) {
                case LINUX:
                    command = "df -PTm";
                    diskUsage = SSHUtil.executeCommand(targetHost, command);

                    splitLines = diskUsage.split("\\r\\n|\\n|\\r");
                    for (String line : splitLines) {
                        int size = line.trim().split("\\s+").length;
                        Matcher matcher = pattern.matcher(line);

                        if (matcher.find() || size > 0) {
                            if (size < 8) {
                                newLine += line;
                            }

                            //Matcher matchers = pattern.matcher(newLine);
                            if (newLine.split("\\s+").length == 7) {
                                // loop가 포함 된 내용은 제외 ex) loop0, loop1, etc
                                Pattern loopPattern = Pattern.compile("/loop\\d+");
                                Matcher m = loopPattern.matcher(newLine);

                                if (!m.find()) {
                                    Map<String, String> diskMap = new LinkedHashMap<>();
                                    String[] value = newLine.split("\\s+");

                                    if (!value[0].contains("tmpfs") && !value[1].contains("tmpfs")) {
                                        diskMap.put("device", value[0]);
                                        diskMap.put("fstype", value[1]);
                                        diskMap.put("size", value[2]);
                                        diskMap.put("free", value[4]);

                                        diskInfoMap.put(value[6], diskMap);
                                    }
                                }
                                newLine = "";
                            }
                        }
                    }

                    break;
                case AIX:
                    command = "df -m";
                    diskUsage = SSHUtil.executeCommand(targetHost, command);

                    splitLines = diskUsage.split("\\r\\n|\\n|\\r");
                    for (String line : splitLines) {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            Map<String, String> diskMap = new LinkedHashMap<>();
                            String[] value = line.split("\\s+");

                            diskMap.put("device", value[0]);
                            diskMap.put("fstype", getFileSystemType(targetHost, value[0]));
                            diskMap.put("size", value[1]);
                            diskMap.put("free", value[2]);

                            diskInfoMap.put(value[6], diskMap);
                        }
                    }

                    break;
                case SUNOS:
                    command = "df -k | tail +2";
                    diskUsage = SSHUtil.executeCommand(targetHost, command);

                    String dfnCommand = "df -n";
                    String content = SSHUtil.executeCommand(targetHost, dfnCommand);

                    Map<String, String> fsTypeMap = new HashMap<>();
                    splitLines = content.split("\\r\\n|\\n|\\r");
                    for (String line : splitLines) {
                        String[] dataArr = line.split(":");
                        fsTypeMap.put(dataArr[0].trim(), dataArr[1].trim());
                    }

                    splitLines = diskUsage.split("\\r\\n|\\n|\\r");
                    for (String line : splitLines) {
                        newLine += line;

                        if (newLine.split("\\s+").length == 6) {
                            Map<String, String> diskMap = new LinkedHashMap<>();
                            String[] value = newLine.split("\\s+");

                            diskMap.put("device", value[0]);
                            diskMap.put("fstype", fsTypeMap.get(value[5]));
                            diskMap.put("size", String.valueOf(Float.parseFloat(value[1]) / 1024));
                            diskMap.put("free", String.valueOf(Float.parseFloat(value[3]) / 1024));

                            diskInfoMap.put(value[5], diskMap);

                            newLine = "";
                        }
                    }
                    break;
                case HP_UX:
                    command = "bdf | tail -n+2";
                    diskUsage = SSHUtil.executeCommand(targetHost, command);

                    splitLines = diskUsage.split("\\r\\n|\\n|\\r");
                    for (String line : splitLines) {
                        newLine += line;

                        if (newLine.split("\\s+").length == 6) {
                            Map<String, String> diskMap = new LinkedHashMap<>();
                            String[] value = newLine.split("\\s+");

                            String fsType = SSHUtil.executeCommand(targetHost, "fstyp " + value[0]);

                            diskMap.put("device", value[0]);
                            diskMap.put("fstype", fsType.trim());
                            diskMap.put("size", String.valueOf(Float.parseFloat(value[1]) / 1024));
                            diskMap.put("free", String.valueOf(Float.parseFloat(value[3]) / 1024));

                            diskInfoMap.put(value[5], diskMap);
                            newLine = "";
                        }
                    }
                    break;
                case WINDOWS:
                    break;
                default:
                    break;
            }

            // duplication 설정
            DiskInfo.generatedDuplicated(diskInfoMap);

            // 기존의 server_disk_information table 데이터를 삭제하고 최신 조회한 Disk Usage를 신규로 추가한다.
            serverDiskInformationRepository.deleteByServerInventoryId(inventoryMaster.getInventoryId());

            for (String key : diskInfoMap.keySet()) {
                Map<String, String> diskMap = (Map<String, String>) diskInfoMap.get(key);
                ServerDiskInformation disk = new ServerDiskInformation();
                disk.setServerInventoryId(inventoryMaster.getInventoryId());
                disk.setDeviceName(diskMap.get("device"));
                disk.setMountPath(key);
                disk.setFilesystemType(diskMap.get("fstype"));
                disk.setFreeSize(Double.parseDouble(StringUtils.defaultString(diskMap.get("free"), "0")));
                disk.setTotalSize(Double.parseDouble(StringUtils.defaultString(diskMap.get("size"), "0")));
                serverDiskInformationRepository.save(disk);
            }

        } catch (Exception e) {
            log.error("Unhandled exception occurred while fetch disk usage.", e);
        }

        return diskInfoMap;
    }

    /**
     * 서버 중복 체크
     *
     * @param representativeIpAddress the Representative Ip Address
     * @param connectionPort          the Connection Port
     * @param serverId                the Server Id
     *
     * @return the boolean
     */
    private boolean isDuplicateServer(Long projectId, String representativeIpAddress, int connectionPort, Long serverId) {
        boolean isDuplicate = false;
        int serverCount = serverMapper
                .selectServerCountByIpAddressAndPortAndServerId(projectId, representativeIpAddress, connectionPort, serverId);

        if (serverCount > 0) {
            isDuplicate = true;
        }

        return isDuplicate;
    }

    @Transactional
    public void updateOsInfo(InventoryProcessQueueItem item, OSInfo osInfo) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.getById(item.getInventoryId());
        ServerMaster serverMaster = serverMasterRepository.getById(item.getInventoryId());
        inventoryMaster.setInventoryDetailTypeCode(osInfo.getInventoryDetailTypeCode().name());
        serverMaster.setOsVersion(osInfo.getOsVersion());
    }

    /**
     * Gets fsType for AIX
     */
    private String getFileSystemType(TargetHost host, String device) throws InterruptedException {
        String shortDeviceName = device.split("/")[2];
        String regex = "^" + shortDeviceName;
        Pattern pattern = Pattern.compile(regex);
        String fsType = null;

        String command = "lsvg -l rootvg";
        String result = SSHUtil.executeCommand(host, command);

        for (String line : result.split("\\r\\n|\\n|\\r")) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                fsType = line.split("\\s+")[1];
            }
        }

        return fsType;
    }

    public List<MonitoringQueueItem> getMonitoringServers(Date now) {
        return serverMapper.selectMonitoringServers(now);
    }

    public List<ScheduledScanServer> getScheduledScanServers() {
        return serverMapper.getScheduledScanServers();
    }

    public TargetHost getTargetHostByServerInventoryId(Long serverInventoryId) {
        ServerConnectionInfo info = serverMapper.selectServerConnectionInfoByInventoryId(serverInventoryId);
        return ServerConnectionInfo.targetHost(info);
    }

    private List<Long> getExistDiscoveredServerInstanceId(Long projectId, String representativeIpAddress) {
        return discoveredInstanceMapper.selectAllDiscoveredServer(projectId, representativeIpAddress);
    }

    private boolean isAvaliableModifyIpAddress(ServerMaster serverMaster, ServerRequest serverRequest) {
        boolean isAvaliable = true;
        String originIpAddress = serverMaster.getRepresentativeIpAddress();
        String newIpAddress = serverRequest.getRepresentativeIpAddress();

        if (!(originIpAddress.equals(newIpAddress))) {
            if (inventoryProcessMapper.selectSuccessCompleteCount(serverMaster.getServerInventoryId()) != 0) {
                isAvaliable = false;
            }
        }
        return isAvaliable;
    }

    public void stopMonitoringProject(String projectIds) {
        List<Integer> projectIdList = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(projectIds, ",");
        while (st.hasMoreTokens()) {
            projectIdList.add(Integer.parseInt(st.nextToken()));
        }

        List<MonitoringQueueItem> items = serverMapper.selectProjectMonitoringServers(projectIdList, new Date());

        for (MonitoringQueueItem monitoringQueueItem : items) {
            log.debug("Stop Server ID : {}", monitoringQueueItem.getServerInventoryId());
            stopServerMonitoring(monitoringQueueItem.getServerInventoryId());
        }

    }

    @SuppressWarnings("DuplicatedCode")
    private void stopServerMonitoring(Long serverInventoryId) {
        TargetHost targetHost = getTargetHostByServerInventoryId(serverInventoryId);
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByInventoryIdAndInventoryTypeCode(serverInventoryId, Domain1001.SVR.name()).orElse(null);

        Domain1013 domain1013;

        try {
            domain1013 = Domain1013.valueOf(inventoryMaster.getInventoryDetailTypeCode());
        } catch (IllegalArgumentException e) {
            throw new RoRoException("This OS is not supported: " + inventoryMaster.getInventoryDetailTypeCode());
        }
        if (!(domain1013 == Domain1013.LINUX || domain1013 == Domain1013.AIX)) {
            throw new RoRoException("This OS is not supported: " + inventoryMaster.getInventoryDetailTypeCode());
        }

        File linuxMonitoringScript = CollectionHelper.getLinuxMonitoringFile();
        File aixMonitoringScript = CollectionHelper.getAixMonitoringFile();
        String outDir = scheduleConfig.getMonitoring().getDefaultDir();

        File script = domain1013 == Domain1013.LINUX ? linuxMonitoringScript : aixMonitoringScript;

        try {
            String command = "sudo ps -ef | grep '" + script.getName() + "' | grep -v grep | awk {'print \"kill -9 \" $2'} | sh -x";
            SSHUtil.executeCommand(targetHost, command);

            TimeUnit.MILLISECONDS.sleep(1000);

            SSHUtil.executeCommand(targetHost, "sudo rm -f " + outDir + File.separator + script);
        } catch (InterruptedException e) {
            log.warn("Unable to stop monitoring script. Reason : [{}]", e.getMessage());
        }
    }


//    private void updateDiscoveredInstance(InventoryMaster inventoryMaster, ServerMaster serverMaster, boolean isNew) {
//        List<DiscoveredInstanceMaster> discoveredInstanceMasters
//                = discoveredInstanceMasterRepository.findByDiscoveredIpAddressAndInventoryTypeCode(serverMaster.getRepresentativeIpAddress(), Domain1001.SVR.name());
//        if (discoveredInstanceMasters != null && discoveredInstanceMasters.size() > 0) {
//            for (DiscoveredInstanceMaster instance : discoveredInstanceMasters) {
//                instance.setPossessionInventoryId(inventoryMaster.getInventoryId());
//                instance.setInventoryRegistTypeCode(Domain1006.DISC.name());
//                discoveredInstanceMasterRepository.save(instance);
//            }
//
//            serverMaster.setDiscoveredServerYn(Domain101.Y.name());
//        } else {
//            serverMaster.setDiscoveredServerYn(Domain101.N.name());
//        }
//
//        if (isNew) {
//            serverMasterRepository.save(serverMaster);
//        }
//    }
}
//end of ServerService.java
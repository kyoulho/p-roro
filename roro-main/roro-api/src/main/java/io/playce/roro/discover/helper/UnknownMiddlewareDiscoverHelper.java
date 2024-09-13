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
 * Hoon Oh       1월 26, 2022            First Draft.
 */
package io.playce.roro.discover.helper;

import io.jsonwebtoken.lang.Collections;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.assessment.service.AssessmentService;
import io.playce.roro.api.domain.inventory.service.InventoryService;
import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1006;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.config.RoRoProperties;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.dto.inventory.server.ServerDetailResponse;
import io.playce.roro.common.dto.inventory.service.Service;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.discover.middleware.detector.MiddlewareDetector;
import io.playce.roro.discover.middleware.detector.MiddlewareDetectorFactory;
import io.playce.roro.discover.middleware.dto.DetectResultInfo;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.entity.MiddlewareMaster;
import io.playce.roro.jpa.entity.ServiceInventory;
import io.playce.roro.jpa.entity.ServiceMaster;
import io.playce.roro.jpa.repository.InventoryMasterRepository;
import io.playce.roro.jpa.repository.MiddlewareMasterRepository;
import io.playce.roro.jpa.repository.ServiceInventoryRepository;
import io.playce.roro.jpa.repository.ServiceMasterRepository;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static io.playce.roro.api.common.CommonConstants.DEFAULT_SERVICE_CUSTOMER_SERVICE_CODE;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class UnknownMiddlewareDiscoverHelper {
    private final CommandConfig commandConfig;

    private final InventoryService inventoryService;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ServiceInventoryRepository serviceInventoryRepository;
    private final ServerService serverService;
    private final AssessmentService assessmentService;

    private final InventoryMasterRepository inventoryMasterRepository;
    private final MiddlewareMasterRepository middlewareMasterRepository;
    private final RoRoProperties roroProperties;

    private String getMiddlewareName(List<String> mwNameList, String serverName, int idx) {
        if (mwNameList.contains(serverName)) {
            serverName = serverName.replaceAll("\\(\\d+\\)", "");
            serverName = serverName + "(" + idx + ")";

            return getMiddlewareName(mwNameList, serverName, ++idx);
        } else {
            mwNameList.add(serverName);
            return serverName;
        }
    }

    private String getTemporaryName(String svrInventoryName, String solution) {
        return solution + "-" + svrInventoryName;
    }

    public void extract(InventoryProcessConnectionInfo connectionInfo, ServerAssessmentResult result, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("##############################[ProjectId-{}, FinderSvrId-{}]##############################", connectionInfo.getInventoryId(), connectionInfo.getInventoryId());

        List<String> mwNameList = inventoryMasterRepository.getMiddlewarNames(connectionInfo.getInventoryId(), connectionInfo.getProjectId());
        ServerDetailResponse svrInfo = serverService.getServer(connectionInfo.getProjectId(), connectionInfo.getInventoryId());

        TargetHost targetHost = InventoryProcessConnectionInfo.targetHost(connectionInfo);
        List<Process> processList = result.getProcesses();
        for (Process process : processList) {
            try {
                if (process.getCmd() == null)
                    continue;

//                MiddlewareDetector detector = MiddlewareDetectorFactory.getDetector(process, connectionInfo.getWindowsYn().equals("Y"));
                MiddlewareDetector detector = MiddlewareDetectorFactory.getDetector(process);

                if (detector == null) {
                    log.debug("[{}] is not middleware process", process);
                    continue;
                }

                log.debug("[{}] detected middleware", detector.getClass().getName());
                log.debug("Current {}", process);
                DetectResultInfo detectResultInfo = detector.generateMiddleware(targetHost, connectionInfo, commandConfig, strategy);
                if (detectResultInfo == null) {
                    log.error("Unable to generate middleware");
                    continue;
                }

                log.debug("[{}] extract middleware information", detectResultInfo);

                // Step 1. Check duplicate middleware
                if (checkDuplicateMiddleware(connectionInfo, detectResultInfo)) {
                    createNewMiddleware(connectionInfo, detectResultInfo, svrInfo, mwNameList);
                    log.debug("add new middleware: {}", process);
                } else {
                    log.debug("{} composed of engine path [{}] and domain path [{}] is already registered or prevented.",
                            detectResultInfo.getVendor(), detectResultInfo.getEnginePath(), detectResultInfo.getDomainPath());
                }
            } catch (RuntimeException e) {
                log.error("Unknown error occurred during extract middleware from process {}", process, e);
            }
        }
    }

    /**
     * <pre>
     * Discovered M/W에 대한 개별 Transaction을 위한 메소드
     * 하나의 M/W에 대해 inventory_master, middleware_master, serivce_inventory 테이블은 함께 저장되어야 한다.
     * </pre>
     *
     * @param connectionInfo
     * @param detectResultInfo
     * @param svrInfo
     * @param mwNameList
     */
    @Transactional
    public void createNewMiddleware(InventoryProcessConnectionInfo connectionInfo, DetectResultInfo detectResultInfo,
                                    ServerDetailResponse svrInfo, List<String> mwNameList) {
        // Step 1. Save new inventory master
        InventoryMaster inventoryMaster = registInventoryMaster(connectionInfo, detectResultInfo, svrInfo);

        // Step 2. Check inventory name
        if (StringUtils.isEmpty(inventoryMaster.getInventoryName())) {
            String temporaryName = getTemporaryName(svrInfo.getServerInventoryName(), detectResultInfo.getMwDetailType().enname());
            temporaryName = getMiddlewareName(mwNameList, temporaryName, 1);
            inventoryMaster.setInventoryName(temporaryName);
        }

        inventoryMaster = inventoryService.saveInventoryMaster(inventoryMaster);

        // Step 3. Save middleware master
        MiddlewareMaster middlewareMaster = registMiddlewareMaster(detectResultInfo, inventoryMaster);

        // Step 4. Save ServiceInventory
        registServiceInventory(inventoryMaster.getProjectId(), svrInfo, middlewareMaster.getMiddlewareInventoryId());

        // Step 5. inventory process 요청 등록
        if (roroProperties.isMiddlewareAutoScan()) {
            assessmentService.createAssessment(connectionInfo.getProjectId(), middlewareMaster.getMiddlewareInventoryId());
        }

        // Step 6. Save discovered instance
        // registDiscoveredInstance(connectionInfo, detectResultInfo, inventoryMaster, middlewareMaster);
    }

    private void registServiceInventory(Long projectId, ServerDetailResponse svrInfo, Long middlewareInventoryId) {
        ServiceMaster service = null;
        // 서버의 서비스 매핑 정보가 2개 이상이거나 없는 경우 Default Service로 매핑한다.
        List<Service> serviceList = svrInfo.getServices();
        if (serviceList.size() > 1 || CollectionUtils.isEmpty(serviceList)) {
            service = serviceMasterRepository.findByCustomerServiceCodeAndProjectId(DEFAULT_SERVICE_CUSTOMER_SERVICE_CODE, projectId);
        } else {
            service = serviceMasterRepository.findByProjectIdAndServiceId(projectId, serviceList.get(0).getServiceId());
        }

        ServiceInventory serviceInventory = new ServiceInventory();
        serviceInventory.setInventoryId(middlewareInventoryId);
        serviceInventory.setServiceId(service.getServiceId());
        serviceInventoryRepository.save(serviceInventory);
    }

    /*private void registDiscoveredInstance(InventoryProcessConnectionInfo connectionInfo, DetectResultInfo detectResultInfo, InventoryMaster inventoryMaster, MiddlewareMaster middlewareMaster) {
        if (detectResultInfo.getVendor().equals(Domain1013.TOMCAT)) {
            DiscoveredInstanceMaster instance = new DiscoveredInstanceMaster();
            instance.setDiscoveredIpAddress(connectionInfo.getRepresentativeIpAddress());
            instance.setRegistDatetime(new Date());
            instance.setInventoryTypeCode(Domain1001.MW.name());
            instance.setInventoryRegistTypeCode(Domain1006.DISC.name());
            instance.setProjectId(connectionInfo.getProjectId());
            instance.setFinderInventoryId(connectionInfo.getInventoryId());
            instance.setPossessionInventoryId(inventoryMaster.getInventoryId());
            instance.setDeleteYn(Domain101.N.name());
            instance.setInventoryDetailTypeCode(detectResultInfo.getVendor().name());

            switch (detectResultInfo.getVendor()) {
                case TOMCAT:
                case WEBLOGIC:
                case WEBTOB:
                case JEUS:
                    instance.setDiscoveredDetailDivision(middlewareMaster.getDomainHomePath());
                    break;
                case WSPHERE:
//                    instance.setDiscoveredDetailDivision(middlewareMaster.get);
            }

            discoveredInstanceManager.saveUnknownServer(instance);
        }
    }*/

    private synchronized boolean checkDuplicateMiddleware(InventoryProcessConnectionInfo connectionInfo, DetectResultInfo detectResultInfo) {
        // List<MiddlewareInventory> mw = middlewareMapper.selectDuplicateMiddlewareInventory(
        //         connectionInfo.getProjectId(), connectionInfo.getInventoryId(), detectResultInfo.getEnginePath(), detectResultInfo.getDomainPath());

        // Engine Path, Domain Path 가 모두 확인되지 않은 서로 다른 미들웨어가 중복으로 등록되지 않음
        List<Long> mw = middlewareMasterRepository.selectDuplicateMiddlewareInventory(connectionInfo.getInventoryId(), detectResultInfo.getMwDetailType().name(), detectResultInfo.getEnginePath(), detectResultInfo.getDomainPath());

        return Collections.isEmpty(mw);
    }

    private MiddlewareMaster registMiddlewareMaster(DetectResultInfo detectResultInfo, InventoryMaster inventoryMaster) {
        MiddlewareMaster middlewareMaster = new MiddlewareMaster();
        middlewareMaster.setMiddlewareInventoryId(inventoryMaster.getInventoryId());
        middlewareMaster.setMiddlewareTypeCode(detectResultInfo.getMwType().name());
        middlewareMaster.setEngineInstallationPath(detectResultInfo.getEnginePath());
        middlewareMaster.setDomainHomePath(detectResultInfo.getDomainPath());
        middlewareMaster.setVendorName(detectResultInfo.getVendor());
        middlewareMaster.setAutomaticRegistProtectionYn(Domain101.N.name());
        middlewareMaster.setProcessName(detectResultInfo.getProcessName());
        middlewareMaster.setEngineVersion(detectResultInfo.getVersion());
        middlewareMasterRepository.save(middlewareMaster);
        return middlewareMaster;
    }

    private InventoryMaster registInventoryMaster(InventoryProcessConnectionInfo connectionInfo, DetectResultInfo detectResultInfo, ServerDetailResponse svrInfo) {
        Date now = new Date();

        String generateCustomInventoryCodeName = Domain1001.MW.name().toLowerCase() + "_" +
                svrInfo.getServerInventoryName() + "_" + RandomStringUtils.randomNumeric(10);

        InventoryMaster inventoryMaster = new InventoryMaster();
        inventoryMaster.setProjectId(connectionInfo.getProjectId());
        inventoryMaster.setServerInventoryId(connectionInfo.getInventoryId());
        inventoryMaster.setInventoryTypeCode(Domain1001.MW.name());
        inventoryMaster.setInventoryDetailTypeCode(detectResultInfo.getMwDetailType().name());
        inventoryMaster.setInventoryAnalysisYn(Domain101.Y.name());
        inventoryMaster.setCustomerInventoryName(generateCustomInventoryCodeName);
        inventoryMaster.setCustomerInventoryCode(generateCustomInventoryCodeName);
        inventoryMaster.setInventoryName(StringUtils.defaultString(detectResultInfo.getName()));
        inventoryMaster.setInventoryIpTypeCode(Domain1006.DISC.name());
        inventoryMaster.setDeleteYn(Domain101.N.name());
        inventoryMaster.setInventoryDiscoveredDatetime(now);
        inventoryMaster.setDescription("Added by system");
        inventoryMaster.setAutomaticRegistYn(Domain101.Y.name());
        inventoryMaster.setRegistUserId(WebUtil.getUserId());
        inventoryMaster.setRegistDatetime(now);
        inventoryMaster.setModifyUserId(WebUtil.getUserId());
        inventoryMaster.setModifyDatetime(now);

        return inventoryMaster;
    }
}
//end of UnknownMiddlewareDiscoverManager.java
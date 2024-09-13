/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * Dong-Heon Han    Apr 01, 2022		First Draft.
 */

package io.playce.roro.scheduler.component;

import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.assessment.service.AssessmentService;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1006;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.config.RoRoProperties;
import io.playce.roro.common.dto.info.JdbcInfo;
import io.playce.roro.common.dto.inventory.application.LastInventoryApplication;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.util.JdbcURLParser;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mw.asmt.dto.DiscApplication;
import io.playce.roro.mw.asmt.dto.DiscDatabase;
import io.playce.roro.mw.asmt.dto.DiscInstanceInterface;
import io.playce.roro.mybatis.domain.inventory.application.ApplicationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.playce.roro.api.common.CommonConstants.DEFAULT_SERVICE_CUSTOMER_SERVICE_CODE;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommonMiddlewarePostProcessor {

    private final DiscoveredInstanceMasterRepository discoveredInstanceMasterRepository;
    private final DiscoveredInstanceInterfaceRepository discoveredInstanceInterfaceRepository;
    private final DiscoveredInstanceInterfaceIpsRepository discoveredInstanceInterfaceIpsRepository;
    private final ApplicationMasterRepository applicationMasterRepository;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final DatabaseInstanceRepository databaseInstanceRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ServiceInventoryRepository serviceInventoryRepository;
    private final MiddlewareInstanceApplicationInstanceRepository middlewareInstanceApplicationInstanceRepository;
    private final ApplicationMapper applicationMapper;
    private final AssessmentService assessmentService;
    private final RoRoProperties roroProperties;

    public DiscoveredInstanceMaster getDiscoveredInstanceMaster(InventoryProcessQueueItem item, TargetHost targetHost, Domain1006 code, String inventoryDetailTypeCode, String instanceDetailDivision) {
        DiscoveredInstanceMaster discoveredInstanceMaster = discoveredInstanceMasterRepository
                .findByProjectIdAndDiscoveredIpAddressAndDiscoveredDetailDivision(item.getProjectId(), targetHost.getIpAddress(), instanceDetailDivision)
                .orElse(new DiscoveredInstanceMaster());
        log.debug("==> new: {} - {} : {}", discoveredInstanceMaster.getDiscoveredInstanceId() == null, instanceDetailDivision, discoveredInstanceMaster);
        setDiscoveredInstanceMaster(discoveredInstanceMaster, item, targetHost, code, inventoryDetailTypeCode, instanceDetailDivision);
        discoveredInstanceMasterRepository.save(discoveredInstanceMaster);
        return discoveredInstanceMaster;
    }

    public List<DiscDatabase> saveInterface(DiscoveredInstanceMaster discoveredInstanceMaster, List<DiscInstanceInterface> instanceInterfaces) throws InterruptedException {
        Long discoveredInstanceId = discoveredInstanceMaster.getDiscoveredInstanceId();
        discoveredInstanceInterfaceRepository.deleteAllByDiscoveredInstanceInterfaceId(discoveredInstanceId);
        discoveredInstanceInterfaceIpsRepository.deleteAllByDiscoveredInstanceInterfaceId(discoveredInstanceId);

        List<DiscDatabase> databases = new ArrayList<>();
        List<DiscoveredInstanceInterfaceIps> ips = new ArrayList<>();
        int seq = 1;
        Set<String> addedInterfaceSet = new HashSet<>();
        for (DiscInstanceInterface iif : instanceInterfaces) {
            String fullDescriptors = iif.getFullDescriptors();
            if (addedInterfaceSet.contains(fullDescriptors))
                continue;

            addedInterfaceSet.add(fullDescriptors);
            DiscoveredInstanceInterface instanceInterface = new DiscoveredInstanceInterface();
            setDiscoveredInstanceInterface(instanceInterface, discoveredInstanceId, iif, seq++);
            discoveredInstanceInterfaceRepository.save(instanceInterface);

            String username = iif.getUsername();
            String password = iif.getPassword();
            // apache derby skip
            if (StringUtils.isEmpty(fullDescriptors))
                continue;

            // int index = url.indexOf("@");
            // url = index > -1 ? url.substring(0, index) + url.substring(index).toUpperCase() : url;

            String replacedUrl = fullDescriptors;
            if (fullDescriptors.contains("@")) {
                replacedUrl = fullDescriptors.replaceAll("(?i)service_name", "SERVICE_NAME")
                        .replaceAll("(?i)description", "DESCRIPTION")
                        .replaceAll("(?i)host", "HOST")
                        .replaceAll("localHOST", "localhost")
                        .replaceAll("(?i)port", "PORT");
            }

            List<JdbcInfo> infos = JdbcURLParser.parse(replacedUrl);
            for (JdbcInfo info : infos) {
                if (StringUtils.isEmpty(info.getHost()) && info.getPort() == null && StringUtils.isEmpty(info.getDatabase())) {
                    log.warn("Can't get discovered database information from jdbc url({}).", fullDescriptors);
                    continue;
                }

                DiscoveredInstanceInterfaceIps instanceInterfaceIps = new DiscoveredInstanceInterfaceIps();
                setDiscoveredInstanceInterfaceIps(instanceInterfaceIps, instanceInterface, username, password, info);
                ips.add(instanceInterfaceIps);

                DiscDatabase database = new DiscDatabase();
                database.setServiceName(iif.getDescriptorsName());
                database.setJdbcUrl(fullDescriptors);
                database.setIpAddress(info.getHost());
                database.setUsername(username);
                database.setPassword(password);
                database.setDbType(info.getType());
                database.setDetailDivision(String.format("%d|%s", info.getPort(), info.getDatabase()));
                database.setRealServiceName(info.getDatabase());

                databases.add(database);
            }
        }
        discoveredInstanceInterfaceIpsRepository.saveAll(ips);
        return databases;
    }

    private void setDiscoveredInstanceInterfaceIps(DiscoveredInstanceInterfaceIps instanceInterfaceIps, DiscoveredInstanceInterface instanceInterface, String username, String password, JdbcInfo info) {
        instanceInterfaceIps.setDiscoveredInstanceInterfaceId(instanceInterface.getDiscoveredInstanceInterfaceId());
        instanceInterfaceIps.setDiscoveredInstanceInterfaceSeq(instanceInterface.getDiscoveredInstanceInterfaceSeq());
        instanceInterfaceIps.setDiscoveredInstanceInterfaceIpAddress(StringUtils.defaultString(info.getHost()));
        instanceInterfaceIps.setServiceName(info.getDatabase());
        instanceInterfaceIps.setServicePort(info.getPort() == null ? 0 : info.getPort());
        instanceInterfaceIps.setUserName(username == null ? info.getUser() : username);
        instanceInterfaceIps.setUserPassword(password == null ? info.getPass() : password);
    }

    private void setDiscoveredInstanceInterface(DiscoveredInstanceInterface instanceInterface, Long discoveredInstanceId, DiscInstanceInterface instance, Integer seq) {
        instanceInterface.setDiscoveredInstanceInterfaceId(discoveredInstanceId);
        instanceInterface.setDiscoveredInstanceInterfaceSeq(seq);
        instanceInterface.setDiscoveredInstanceInterfaceDetailTypeCode(instance.getDiscoveredInstanceDetailTypeCode());
        instanceInterface.setDescriptorsName(instance.getDescriptorsName());
        instanceInterface.setFullDescriptors(instance.getFullDescriptors());
    }

    public void addDiscoveredApplications(DiscoveredInstanceMaster discoveredInstanceMasterForMiddleware, InventoryProcessQueueItem item, TargetHost targetHost, List<DiscApplication> apps) {
        if (apps == null || apps.isEmpty())
            return;

        Long projectId = discoveredInstanceMasterForMiddleware.getProjectId();
        Long discoveredInstanceId = discoveredInstanceMasterForMiddleware.getDiscoveredInstanceId();
        Long finderInventoryId = discoveredInstanceMasterForMiddleware.getFinderInventoryId();

        InventoryMaster inventoryMaster = inventoryMasterRepository.findById(finderInventoryId).orElse(new InventoryMaster());
        Long serverInventoryId = inventoryMaster.getServerInventoryId();
        if (serverInventoryId == null) {
            serverInventoryId = inventoryMaster.getInventoryId();
        }

        Date now = new Date();
        for (DiscApplication app : apps) {
            try {
                // https://cloud-osci.atlassian.net/browse/ROROQA-1045
                if (StringUtils.isEmpty(app.getDeployPath()) || app.getDeployPath().endsWith("/null") || app.getDeployPath().endsWith("\\null")) {
                    continue;
                }

                // LastInventoryApplication applicationInventory = applicationMapper.selectInventoryApplication(item.getProjectId(), serverInventoryId, app.getDeployPath());
                LastInventoryApplication applicationInventory = applicationMasterRepository.selectInventoryApplication(serverInventoryId, app.getDeployPath());

                // https://cloud-osci.atlassian.net/browse/PCR-4642
                InventoryMaster inventoryMasterForApplication;
                inventoryMasterForApplication = getInventoryMaster(applicationInventory);
                if (inventoryMasterForApplication == null)
                    continue;

                setInventoryMaster(inventoryMasterForApplication, item, serverInventoryId, inventoryMaster.getInventoryName(), app.getApplication(), now);

                boolean flag = inventoryMasterForApplication.getInventoryId() == null; //신규.. app

                // 애플리케이션 인벤토리 등록
                inventoryMasterRepository.save(inventoryMasterForApplication);

                ApplicationMaster applicationMaster = applicationMasterRepository.findById(inventoryMasterForApplication.getInventoryId()).orElse(new ApplicationMaster());
                setApplicationMaster(applicationMaster, app.getDeployPath(), inventoryMasterForApplication);
                ApplicationMaster savedApplicationMaster = applicationMasterRepository.save(applicationMaster);

                // 애플리케이션과 서비스 매핑
                setApplicationServiceMapping(projectId, inventoryMaster, savedApplicationMaster);

                // 애플리케이션 인스턴스 등록
                String discoveredInstanceDetailDivisionForApplication = applicationMaster.getSourceLocationUri();
                DiscoveredInstanceMaster discoveredInstanceMasterForApplication = discoveredInstanceMasterRepository
                        .findByProjectIdAndDiscoveredIpAddressAndDiscoveredDetailDivision(item.getProjectId(), targetHost.getIpAddress(), discoveredInstanceDetailDivisionForApplication)
                        .orElse(new DiscoveredInstanceMaster());
                setDiscoveredInstanceMasterForApplication(discoveredInstanceMasterForApplication, discoveredInstanceMasterForMiddleware, item, inventoryMasterForApplication, app, now);
                discoveredInstanceMasterRepository.save(discoveredInstanceMasterForApplication);

                // 미들웨어 인스턴스와 애플리케이션 매핑
                Long applicationDiscoveredInstanceId = discoveredInstanceMasterForApplication.getDiscoveredInstanceId();
                MiddlewareInstanceApplicationInstance middlewareInstanceApplicationInstance = middlewareInstanceApplicationInstanceRepository
                        .findByMiddlewareInstanceIdAndApplicationInstanceId(discoveredInstanceId, applicationDiscoveredInstanceId)
                        .orElse(new MiddlewareInstanceApplicationInstance());
                setMiddlewareInstanceApplicationInstance(discoveredInstanceId, app, applicationDiscoveredInstanceId, middlewareInstanceApplicationInstance);

                // Application Scan 요청
                // https://cloud-osci.atlassian.net/browse/PCR-5624
                // if (flag && savedApplicationMaster.getApplicationInventoryId() != null && roroProperties.isApplicationAutoScan()) {
                //     assessmentService.createAssessment(item.getProjectId(), applicationMaster.getApplicationInventoryId());
                // }
            } catch (Exception e) {
                log.error("Unhandled exception occurred while insert discovered instance for application.", e);
            }
        }
    }

    private void setMiddlewareInstanceApplicationInstance(Long discoveredInstanceId, DiscApplication app, Long applicationDiscoveredInstanceId, MiddlewareInstanceApplicationInstance middlewareInstanceApplicationInstance) {
        middlewareInstanceApplicationInstance.setMiddlewareInstanceId(discoveredInstanceId);
        middlewareInstanceApplicationInstance.setApplicationInstanceId(applicationDiscoveredInstanceId);
        middlewareInstanceApplicationInstance.setContextPath(app.getContextPath());
        middlewareInstanceApplicationInstance.setAutoDeployYn(app.getAutoDeployYn());
        middlewareInstanceApplicationInstance.setReloadableYn(app.getReloadableYn());
        middlewareInstanceApplicationInstanceRepository.save(middlewareInstanceApplicationInstance);
    }

    @Nullable
    private InventoryMaster getInventoryMaster(LastInventoryApplication applicationInventory) {
        if (applicationInventory == null)
            return new InventoryMaster();

        boolean isDelete = Domain101.Y.name().equals(applicationInventory.getDeleteYn());
        boolean isAutomaticRegistProtection = Domain101.Y.name().equals(applicationInventory.getAutomaticRegistProtectionYn());

        InventoryMaster inventoryMasterForApplication;
        if (isDelete) {
            if (isAutomaticRegistProtection)
                return null;
            else {
                inventoryMasterForApplication = new InventoryMaster();
            }
        } else {
            inventoryMasterForApplication = inventoryMasterRepository.findById(applicationInventory.getInventoryId()).orElse(new InventoryMaster());
        }

        return inventoryMasterForApplication;
    }

    private void setApplicationMaster(ApplicationMaster applicationMaster, String deployedPath, InventoryMaster master) {
        applicationMaster.setApplicationInventoryId(master.getInventoryId());
        applicationMaster.setSourceLocationUri(deployedPath);
        applicationMaster.setDeployPath(deployedPath);
        if (applicationMaster.getApplicationSize() == null) {
            applicationMaster.setApplicationSize(0L);
        }
        if (applicationMaster.getAutomaticRegistProtectionYn() == null) {
            applicationMaster.setAutomaticRegistProtectionYn(Domain101.N.name());
        }
    }

    private void setInventoryMaster(InventoryMaster master, InventoryProcessQueueItem item, Long serverInventoryId, String serverInventoryName, String inventoryName, Date now) {

        String generateCustomInventoryCodeName = Domain1001.APP.name().toLowerCase() + "_" +
                serverInventoryName + "_" + RandomStringUtils.randomNumeric(10);

        master.setProjectId(item.getProjectId());
        master.setServerInventoryId(serverInventoryId);
        master.setInventoryTypeCode(Domain1001.APP.name());
        master.setInventoryDetailTypeCode(Domain1013.WAR.name());
        master.setInventoryAnalysisYn(Domain101.Y.name());
        master.setInventoryName(inventoryName);
        master.setCustomerInventoryCode(generateCustomInventoryCodeName);
        master.setCustomerInventoryName(generateCustomInventoryCodeName);
        master.setInventoryIpTypeCode(Domain1006.DISC.name());
        master.setDeleteYn(Domain101.N.name());
        master.setAutomaticRegistYn(Domain101.Y.name());
        master.setDescription("Added by system.");
        master.setRegistUserId(WebUtil.getUserId());
        master.setRegistDatetime(now);
        master.setModifyUserId(WebUtil.getUserId());
        master.setModifyDatetime(now);
        master.setInventoryDiscoveredDatetime(now);
    }

    private void setApplicationServiceMapping(Long projectId, InventoryMaster inventoryMaster, ApplicationMaster applicationMaster) {
        List<ServiceInventory> serviceInventoryList = null;
        if (inventoryMaster.getInventoryId() != null) {
            serviceInventoryList = serviceInventoryRepository.findByInventoryId(inventoryMaster.getInventoryId());
        }

        if (serviceInventoryList != null) {
            // 미들웨어 엔진에 매핑된 서비스가 있으면 발견된 애플리케이션에도 동일하게 매핑 추가
            for (ServiceInventory si : serviceInventoryList) {
                try {
                    ServiceInventory serviceInventory = new ServiceInventory();
                    serviceInventory.setServiceId(si.getServiceId());
                    serviceInventory.setInventoryId(applicationMaster.getApplicationInventoryId());

                    serviceInventoryRepository.save(serviceInventory);
                } catch (Exception ignore) {
                    // 이미 동일한 서비스로 매핑되어 있는 경우 무시한다.
                }
            }
        } else {
            ServiceMaster serviceMaster = serviceMasterRepository.findByCustomerServiceCodeAndProjectId(DEFAULT_SERVICE_CUSTOMER_SERVICE_CODE, projectId);
            ServiceInventory serviceInventory = new ServiceInventory();
            serviceInventory.setServiceId(serviceMaster.getServiceId());
            serviceInventory.setInventoryId(applicationMaster.getApplicationInventoryId());

            try {
                serviceInventoryRepository.save(serviceInventory);
            } catch (Exception ignore) {
                // 이미 동일한 서비스로 매핑되어 있는 경우 무시한다.
            }
        }
    }

    private void setDiscoveredInstanceMaster(DiscoveredInstanceMaster instanceMaster, InventoryProcessQueueItem item, TargetHost targetHost, Domain1006 code, String inventoryDetailTypeCode, String middlewareInstanceDetailDivision) {
        instanceMaster.setFinderInventoryId(item.getInventoryId());
        instanceMaster.setPossessionInventoryId(item.getInventoryId());
        instanceMaster.setInventoryTypeCode(Domain1001.MW.name());
        instanceMaster.setInventoryDetailTypeCode(inventoryDetailTypeCode);
        instanceMaster.setInventoryRegistTypeCode(code.name());
        instanceMaster.setProjectId(item.getProjectId());
        instanceMaster.setDiscoveredIpAddress(targetHost.getIpAddress());
        instanceMaster.setDiscoveredDetailDivision(middlewareInstanceDetailDivision);
        instanceMaster.setRegistDatetime(new Date());
        instanceMaster.setDeleteYn(Domain101.N.name());
        instanceMaster.setInventoryProcessId(item.getInventoryProcessId());
    }

    private void setDiscoveredInstanceMasterForApplication(DiscoveredInstanceMaster instanceMaster, DiscoveredInstanceMaster middlewareMaster, InventoryProcessQueueItem item, InventoryMaster master, DiscApplication app, Date now) {
        Long middlewareFinderInventoryId = middlewareMaster.getFinderInventoryId();

        instanceMaster.setFinderInventoryId(middlewareFinderInventoryId);
        instanceMaster.setPossessionInventoryId(master.getInventoryId());
        instanceMaster.setInventoryTypeCode(Domain1001.APP.name());
        instanceMaster.setInventoryDetailTypeCode(Domain1013.WAR.name());
        instanceMaster.setInventoryRegistTypeCode(Domain1006.DISC.name());
        instanceMaster.setProjectId(item.getProjectId());
        instanceMaster.setDiscoveredIpAddress(middlewareMaster.getDiscoveredIpAddress());
        instanceMaster.setDiscoveredDetailDivision(app.getDeployPath());
        instanceMaster.setRegistDatetime(now);
        instanceMaster.setDeleteYn(Domain101.N.name());
    }

    public void addDiscoveredDatabases(DiscoveredInstanceMaster discoveredInstanceMasterForMiddleware, InventoryProcessQueueItem item, List<DiscDatabase> databases) {
        if (databases == null || databases.isEmpty()) {
            return;
        }

        Map<String, DiscDatabase> discDatabaseMap = databases.stream().collect(Collectors.toMap(d -> {
            String ip = d.getIpAddress();
            String detailDivision = d.getDetailDivision();
            return ip + "." + detailDivision;
        }, Function.identity(), (n1, n2) -> n1));
        Date now = new Date();
        for (DiscDatabase database : discDatabaseMap.values()) {
            String discoveredInstanceDetailDivisionForDatabase = database.getDetailDivision();
            DiscoveredInstanceMaster discoveredInstanceMasterForDatabase = discoveredInstanceMasterRepository
                    .findByProjectIdAndDiscoveredIpAddressAndDiscoveredDetailDivision(item.getProjectId(), database.getIpAddress(), discoveredInstanceDetailDivisionForDatabase)
                    .orElse(new DiscoveredInstanceMaster());
            setDiscoveredInstanceMasterForDatabase(discoveredInstanceMasterForDatabase, discoveredInstanceMasterForMiddleware, item, database, now);
            discoveredInstanceMasterRepository.save(discoveredInstanceMasterForDatabase);

            DatabaseInstance databaseInstance = databaseInstanceRepository.findById(discoveredInstanceMasterForDatabase.getDiscoveredInstanceId()).orElse(new DatabaseInstance());
            setDatabaseInstance(databaseInstance, discoveredInstanceMasterForDatabase, database, now);
            databaseInstanceRepository.save(databaseInstance);
        }
    }

    private void setDatabaseInstance(DatabaseInstance databaseInstance, DiscoveredInstanceMaster discoveredInstanceMasterForDatabase, DiscDatabase database, Date now) {
        databaseInstance.setDatabaseInstanceId(discoveredInstanceMasterForDatabase.getDiscoveredInstanceId());
        databaseInstance.setDatabaseServiceName(database.getRealServiceName());
        databaseInstance.setJdbcUrl(database.getJdbcUrl());
        databaseInstance.setRegistUserId(WebUtil.getUserId());
        databaseInstance.setUserName(database.getUsername());
        databaseInstance.setRegistDatetime(now);
    }

    private void setDiscoveredInstanceMasterForDatabase(DiscoveredInstanceMaster discoveredInstanceMasterForDatabase, DiscoveredInstanceMaster discoveredInstanceMasterForMiddleware, InventoryProcessQueueItem item, DiscDatabase database, Date now) {
        Long middlewareFinderInventoryId = discoveredInstanceMasterForMiddleware.getFinderInventoryId();

        discoveredInstanceMasterForDatabase.setFinderInventoryId(middlewareFinderInventoryId);
        discoveredInstanceMasterForDatabase.setInventoryTypeCode(Domain1001.DBMS.name());
        discoveredInstanceMasterForDatabase.setInventoryDetailTypeCode(DiscDatabase.getInventoryDetailType(database.getDbType()));
        discoveredInstanceMasterForDatabase.setInventoryRegistTypeCode(Domain1006.DISC.name());
        discoveredInstanceMasterForDatabase.setProjectId(item.getProjectId());
        discoveredInstanceMasterForDatabase.setDiscoveredIpAddress(database.getIpAddress());
        discoveredInstanceMasterForDatabase.setDiscoveredDetailDivision(database.getDetailDivision());
        discoveredInstanceMasterForDatabase.setRegistDatetime(now);
        discoveredInstanceMasterForDatabase.setDeleteYn(Domain101.N.name());
        discoveredInstanceMasterForDatabase.setInventoryProcessId(item.getInventoryProcessId());
    }

    public List<Long> getDiscoveredInstanceMasters(Long inventoryId) {
        List<DiscoveredInstanceMaster> masters = discoveredInstanceMasterRepository.findAllByPossessionInventoryId(inventoryId);
        return masters.stream().map(DiscoveredInstanceMaster::getDiscoveredInstanceId).collect(Collectors.toList());
    }
}
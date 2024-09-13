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
 * Jaeeon Bae       5월 17, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.domain.cloudreadiness.service.CloudReadinessExcelExporter;
import io.playce.roro.api.domain.inventory.service.helper.InventoryToExcelHelper;
import io.playce.roro.api.domain.inventory.service.helper.ServiceReportHelper;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.code.Domain1106;
import io.playce.roro.common.dto.inventory.application.ApplicationResponse;
import io.playce.roro.common.dto.inventory.database.DatabaseEngineListResponseDto;
import io.playce.roro.common.dto.inventory.middleware.MiddlewareResponse;
import io.playce.roro.common.dto.inventory.server.ServerResponse;
import io.playce.roro.common.dto.inventory.server.ServerSummaryResponse;
import io.playce.roro.common.dto.inventory.service.ServiceDetail;
import io.playce.roro.common.dto.publicagency.PublicAgencyReportDto;
import io.playce.roro.common.util.support.DistinctByKey;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.insights.InsightMapper;
import io.playce.roro.mybatis.domain.inventory.application.ApplicationMapper;
import io.playce.roro.mybatis.domain.inventory.database.DatabaseMapper;
import io.playce.roro.mybatis.domain.inventory.middleware.MiddlewareMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerSummaryMapper;
import io.playce.roro.mybatis.domain.inventory.service.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.playce.roro.api.common.util.ExcelUtil.createCellStyle;

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
public class ExportToExcelService {
    private final ServerService serverService;
    private final ProjectMasterRepository projectMasterRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final ServiceMapper serviceMapper;
    private final ServerMapper serverMapper;
    private final DatabaseMapper databaseMapper;
    private final ApplicationMapper applicationMapper;
    private final ServerSummaryMapper serverSummaryMapper;
    private final InsightMapper insightMapper;
    private final MiddlewareMapper middlewareMapper;
    private final ServerDiskInformationRepository serverDiskInformationRepository;
    private final ServerStatusRepository serverStatusRepository;
    private final ServerStorageRepository serverStorageRepository;
    private final BackupDeviceRepository backupDeviceRepository;
    private final DatabaseSummaryRepository databaseSummaryRepository;
    private final ApplicationStatusRepository applicationStatusRepository;
    private final InventoryToExcelHelper inventoryToExcelHelper;
    private final PublicAgencyReportExporter publicAgencyReportExporter;
    private final ServiceReportHelper serviceReportHelper;

    public ByteArrayInputStream exportToExcel(Long projectId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // project check
            projectMasterRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));

            XSSFWorkbook workbook = new XSSFWorkbook(Objects.requireNonNull(InventoryService.class.getResourceAsStream("/template/RoRo-Inventory-Template.xlsx")));
            createCellStyle(workbook);

            // RoRo-Inventory-Template.xlsx 맞춰서 inventory를 Excel로 만든다.
            inventoryToExcelHelper.exportToExcel(projectId, workbook);
            workbook.write(out);
        } catch (Exception e) {
            log.error("Unhandled Exception occurred while Export to Excel.", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayOutputStream getPublicAgencyReport(Long projectId) {
        List<ServiceDetail> serviceList = serviceMapper.selectServiceList(projectId);

        if (serviceList == null || serviceList.size() == 0) {
            throw new RoRoApiException(ErrorCode.INVENTORY_SERVICE_NOT_FOUND);
        }

        PublicAgencyReportDto publicAgencyReportDto = new PublicAgencyReportDto();
        setServerInformation(serviceList, publicAgencyReportDto);
        setSoftwareInformation(serviceList, publicAgencyReportDto);
        setApplicationInformation(serviceList, publicAgencyReportDto);
        setDatabaseInformation(serviceList, publicAgencyReportDto);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFWorkbook workbook = new XSSFWorkbook(Objects.requireNonNull(
                    CloudReadinessExcelExporter.class.getResourceAsStream("/template/RoRo_Public_Agency_Report_Template.xlsx")));

            publicAgencyReportExporter.writePublicAgencyReport(projectId, workbook, publicAgencyReportDto);

            workbook.write(out);

            return out;
        } catch (Exception e) {
            log.error("Unhandled Exception occurred while create public agency report.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }

    private void setServerInformation(List<ServiceDetail> serviceList, PublicAgencyReportDto publicAgencyReportDto) {
        PublicAgencyReportDto.ServerStatus serverStatus;
        PublicAgencyReportDto.StorageStatus storageStatus;
        PublicAgencyReportDto.BackupStatus backupStatus;

        for (ServiceDetail serviceDetail : serviceList) {
            List<ServerResponse> servers = serverMapper.selectServerList(serviceDetail.getProjectId(), serviceDetail.getServiceId());

            for (ServerResponse server : servers) {
                // https://cloud-osci.atlassian.net/browse/PCR-6486
                serverService.getVendorAndModel(server, null);
                ServerSummaryResponse serverSummary = serverSummaryMapper.selectServerSummary(server.getServerInventoryId());
                ServerStatus svrStatus = serverStatusRepository.findById(server.getServerInventoryId()).orElse(null);
                List<ServerStorage> serverStorageList = serverStorageRepository.findByServerInventoryId(server.getServerInventoryId());
                List<BackupDevice> backupDeviceList = backupDeviceRepository.findByServerInventoryId(server.getServerInventoryId());

                // Server Status
                serverStatus = new PublicAgencyReportDto.ServerStatus();
                serverStatus.setSystemId(serviceDetail.getServiceId());
                serverStatus.setSystemName(serviceDetail.getServiceName());
                serverStatus.setServerId(server.getServerInventoryId());
                serverStatus.setServerName(server.getServerInventoryName());
                serverStatus.setManufacturer(server.getMakerName());
                serverStatus.setModel(server.getModelName());

                if (Domain1013.LINUX.name().equals(server.getInventoryDetailTypeCode())) {
                    serverStatus.setOsType("Linux");
                } else if (Domain1013.WINDOWS.name().equals(server.getInventoryDetailTypeCode())) {
                    serverStatus.setOsType("Windows");
                } else if (Domain1013.AIX.name().equals(server.getInventoryDetailTypeCode()) ||
                        Domain1013.SUNOS.name().equals(server.getInventoryDetailTypeCode()) ||
                        Domain1013.HP_UX.name().equals(server.getInventoryDetailTypeCode())) {
                    serverStatus.setOsType("UNIX");
                }
                serverStatus.setPurchaseDate(server.getBuyDate());

                if (StringUtils.isNotEmpty(server.getDualizationTypeCode())) {
                    if (Domain1106.AA.fullname().equals(server.getDualizationTypeCode()) || Domain1106.AS.fullname().equals(server.getDualizationTypeCode())) {
                        serverStatus.setHighAvailability(true);
                    } else {
                        serverStatus.setHighAvailability(false);
                    }
                }

                try {
                    InetAddress inetAddress = InetAddress.getByName(server.getRepresentativeIpAddress());

                    if (inetAddress.isSiteLocalAddress()) {
                        serverStatus.setNetworkType("Private Network");
                    } else {
                        serverStatus.setNetworkType("Public Network");
                    }
                } catch (UnknownHostException e) {
                    // ignore
                }

                if (serverSummary != null) {
                    serverStatus.setHostname(serverSummary.getHostName());
                    serverStatus.setOsVersion(serverSummary.getOsName());
                    serverStatus.setKernel(serverSummary.getOsKernel());
                    serverStatus.setCpuCores(serverSummary.getCpuCoreCount());
                    serverStatus.setCpuSockets(serverSummary.getCpuSocketCount());
                    try {
                        serverStatus.setMemorySize(DataSize.ofMegabytes(serverSummary.getMemorySize()).toBytes());
                    } catch (Exception e) {
                        // ignore
                    }
                }

                if (svrStatus != null) {
                    serverStatus.setServerType(svrStatus.getServerType());

                    // https://cloud-osci.atlassian.net/browse/PCR-6486 - serverService.getVendorAndModel(server); 에서 대신 처리됨.
                    // if (StringUtils.isEmpty(serverStatus.getManufacturer()) && StringUtils.isNotEmpty(svrStatus.getManufacturer())) {
                    //     serverStatus.setManufacturer(svrStatus.getManufacturer());
                    // }
                    //
                    // if (StringUtils.isEmpty(serverStatus.getModel()) && StringUtils.isNotEmpty(svrStatus.getModel())) {
                    //     serverStatus.setModel(svrStatus.getModel());
                    // }

                    serverStatus.setDiskSize(svrStatus.getDiskSize());
                    serverStatus.setDiskCount(svrStatus.getDiskCount());
                    serverStatus.setDiskUsed(svrStatus.getDiskUsed());
                    serverStatus.setCpuUsage(svrStatus.getCpuUsage());
                    serverStatus.setMemUsage(svrStatus.getMemUsage());
                    serverStatus.setMonitoringDatetime(svrStatus.getMonitoringDatetime());
                }

                // SERVER_STATUS 테이블에 DISK 관련 정보가 없으면 스캔시 수집된 디스크 정보를 활용한다.
                if (serverStatus.getDiskSize() == null && serverStatus.getDiskCount() == null && serverStatus.getDiskUsed() == null) {
                    Long totalDiskSize = 0L;
                    Long freeDiskSize = 0L;

                    List<ServerDiskInformation> serverDiskInformation = serverDiskInformationRepository.findByServerInventoryId(server.getServerInventoryId());

                    if (!serverDiskInformation.isEmpty()) {
                        for (ServerDiskInformation disk : serverDiskInformation) {
                            try {
                                totalDiskSize += DataSize.ofMegabytes(disk.getTotalSize().longValue()).toBytes();
                                freeDiskSize += DataSize.ofMegabytes(disk.getFreeSize().longValue()).toBytes();
                            } catch (Exception e) {
                                // ignore
                            }
                        }

                        serverStatus.setDiskSize(totalDiskSize);
                        serverStatus.setDiskCount(serverDiskInformation.size());
                        serverStatus.setDiskUsed(totalDiskSize - freeDiskSize);
                    }
                }

                if (!backupDeviceList.isEmpty()) {
                    serverStatus.setUseBackup("Y");
                }

                // 서버 내에 Web 서버가 있으면 WEB, WAS 서버가 있으면 WAS, DB 서버가 있으면 DB
                List<String> serviceTypes = serverMapper.selectServiceTypesForServer(server.getServerInventoryId());
                if (!serviceTypes.isEmpty()) {
                    serverStatus.setServiceType(String.join(" / ", serviceTypes));
                }

                publicAgencyReportDto.getServerStatusList().add(serverStatus);

                // Storage Status
                for (ServerStorage serverStorage : serverStorageList) {
                    storageStatus = new PublicAgencyReportDto.StorageStatus();
                    storageStatus.setSystemId(serviceDetail.getServiceId());
                    storageStatus.setSystemName(serviceDetail.getServiceName());
                    storageStatus.setServerId(server.getServerInventoryId());
                    storageStatus.setServerName(server.getServerInventoryName());
                    storageStatus.setManufacturer(serverStorage.getManufacturer());
                    storageStatus.setModel(serverStorage.getModel());
                    storageStatus.setDiskType(serverStorage.getDiskType());
                    storageStatus.setConnectionType(serverStorage.getConnectionType());

                    publicAgencyReportDto.getStorageStatusList().add(storageStatus);
                }

                // Backup Status
                for (BackupDevice backupDevice : backupDeviceList) {
                    backupStatus = new PublicAgencyReportDto.BackupStatus();
                    backupStatus.setSystemId(serviceDetail.getServiceId());
                    backupStatus.setSystemName(serviceDetail.getServiceName());
                    backupStatus.setServerId(server.getServerInventoryId());
                    backupStatus.setServerName(server.getServerInventoryName());
                    backupStatus.setModel(backupDevice.getModel());

                    publicAgencyReportDto.getBackupStatusList().add(backupStatus);
                }
            }
        }
    }

    private void setSoftwareInformation(List<ServiceDetail> serviceList, PublicAgencyReportDto publicAgencyReportDto) {
        // Software Status

        for (ServiceDetail serviceDetail : serviceList) {
            List<ServerResponse> serverResponses = serverMapper.selectServerList(serviceDetail.getProjectId(), serviceDetail.getServiceId());
            for (ServerResponse serverResponse : serverResponses) {
                // 서버 세팅
                ServerSummaryResponse serverSummaryResponse = serverSummaryMapper.selectServerSummary(serverResponse.getServerInventoryId());
                if (serverSummaryResponse != null) {
                    PublicAgencyReportDto.SoftwareStatus softwareStatusSVR = new PublicAgencyReportDto.SoftwareStatus();
                    softwareStatusSVR.setSystemId(serviceDetail.getServiceId());
                    softwareStatusSVR.setSystemName(serviceDetail.getServiceName());
                    softwareStatusSVR.setServerId(serverResponse.getServerInventoryId());
                    softwareStatusSVR.setServerName(serverResponse.getServerInventoryName());
                    softwareStatusSVR.setSoftwareName(serverSummaryResponse.getOsAlias());
                    softwareStatusSVR.setVersion(serverSummaryResponse.getOsVersion());
                    softwareStatusSVR.setCategory("OS");
                    Map<String, String> map = insightMapper.selectVendorAndOpensourceYnBySolutionName(serverSummaryResponse.getOsAlias());
                    if (map != null) {
                        softwareStatusSVR.setIsOpenSource("Y".equalsIgnoreCase(map.get("opensourceYn")));
                        softwareStatusSVR.setVendor(map.get("vendor"));
                    }
                    publicAgencyReportDto.getSoftwareStatusList().add(softwareStatusSVR);
                }

                // 서버 밑에 MW
                List<MiddlewareResponse> middlewareResponses = middlewareMapper.selectMiddlewareList(serviceDetail.getProjectId(), serviceDetail.getServiceId(), serverResponse.getServerInventoryId(), Domain1001.MW.name());
                for (MiddlewareResponse middlewareResponse : middlewareResponses) {

                    PublicAgencyReportDto.SoftwareStatus softwareStatusMW = new PublicAgencyReportDto.SoftwareStatus();
                    softwareStatusMW.setSystemId(serviceDetail.getServiceId());
                    softwareStatusMW.setSystemName(serviceDetail.getServiceName());
                    softwareStatusMW.setServerId(serverResponse.getServerInventoryId());
                    softwareStatusMW.setServerName(serverResponse.getServerInventoryName());
                    softwareStatusMW.setSoftwareName(Domain1013.valueOf(middlewareResponse.getInventoryDetailTypeCode()).enname());
                    softwareStatusMW.setVersion(middlewareResponse.getEngineVersion());
                    softwareStatusMW.setCategory(middlewareResponse.getMiddlewareTypeCode());
                    Map<String, String> map2 = insightMapper.selectVendorAndOpensourceYnBySolutionName(softwareStatusMW.getSoftwareName());
                    if (map2 != null) {
                        softwareStatusMW.setIsOpenSource("Y".equalsIgnoreCase(map2.get("opensourceYn")));
                        softwareStatusMW.setVendor(map2.get("vendor"));
                    }
                    publicAgencyReportDto.getSoftwareStatusList().add(softwareStatusMW);
                }

                // 서버 밑에 DB
                List<DatabaseEngineListResponseDto> databaseResponses = databaseMapper.selectDatabaseEngineList(serviceDetail.getProjectId(), serviceDetail.getServiceId(), serverResponse.getServerInventoryId());
                for (DatabaseEngineListResponseDto databaseResponse : databaseResponses) {
                    DatabaseSummary databaseSummary = databaseSummaryRepository.findById(databaseResponse.getDatabaseInventoryId())
                            .orElse(new DatabaseSummary());
                    PublicAgencyReportDto.SoftwareStatus softwareStatusDBMS = new PublicAgencyReportDto.SoftwareStatus();
                    softwareStatusDBMS.setSystemId(serviceDetail.getServiceId());
                    softwareStatusDBMS.setSystemName(serviceDetail.getServiceName());
                    softwareStatusDBMS.setServerId(serverResponse.getServerInventoryId());
                    softwareStatusDBMS.setServerName(databaseResponse.getServerInventoryName());
                    softwareStatusDBMS.setSoftwareName(Domain1013.valueOf(databaseResponse.getInventoryDetailTypeCode()).enname());
                    softwareStatusDBMS.setVersion(databaseSummary.getVersion());
                    softwareStatusDBMS.setCategory("DBMS");
                    Map<String, String> map3 = insightMapper.selectVendorAndOpensourceYnBySolutionName(softwareStatusDBMS.getSoftwareName());
                    if (map3 != null) {
                        softwareStatusDBMS.setIsOpenSource("Y".equalsIgnoreCase(map3.get("opensourceYn")));
                        softwareStatusDBMS.setVendor(map3.get("vendor"));
                    }
                    publicAgencyReportDto.getSoftwareStatusList().add(softwareStatusDBMS);
                }
            }
        }

        List<PublicAgencyReportDto.SoftwareStatus> softwareStatusList = publicAgencyReportDto.getSoftwareStatusList();
        publicAgencyReportDto.setSoftwareStatusList(
                softwareStatusList.stream().filter(DistinctByKey.distinctByKeys(PublicAgencyReportDto.SoftwareStatus::getSystemId,
                        PublicAgencyReportDto.SoftwareStatus::getServerId,
                        PublicAgencyReportDto.SoftwareStatus::getSoftwareName,
                        PublicAgencyReportDto.SoftwareStatus::getVersion)).collect(Collectors.toList()));
    }

    private void setApplicationInformation(List<ServiceDetail> serviceList, PublicAgencyReportDto publicAgencyReportDto) {
        // Application Status
        for (ServiceDetail serviceDetail : serviceList) {
            List<ApplicationResponse> applications = applicationMapper.getApplications(serviceDetail.getProjectId(), serviceDetail.getServiceId(), null);

            for (ApplicationResponse application : applications) {
                PublicAgencyReportDto.ApplicationStatus dto = new PublicAgencyReportDto.ApplicationStatus();
                dto.setSystemId(serviceDetail.getServiceId());
                dto.setSystemName(serviceDetail.getServiceName());
                dto.setServerId(application.getServerInventoryId());
                dto.setServerName(
                        serverMapper.getServerSummary(application.getServerInventoryId()).getInventoryName()
                );
                dto.setApplicationId(application.getApplicationInventoryId());
                dto.setApplicationName(application.getApplicationInventoryName());
                dto.setApplicationSize(application.getApplicationSize());
                dto.setApplicationType(
                        (application.getInventoryDetailTypeCode().equalsIgnoreCase("EAR") || application.getInventoryDetailTypeCode().equalsIgnoreCase("WAR")) ? "WEB" : ""
                );
                applicationStatusRepository.findById(application.getApplicationInventoryId())
                        .ifPresent(applicationStatus -> {
                                    dto.setDevelopLanguage(applicationStatus.getDevelopLanguage());
                                    dto.setDevelopLanguageVersion(applicationStatus.getDevelopLanguageVersion());
                                    dto.setFrameworkName(applicationStatus.getFrameworkName());
                                    dto.setFrameworkVersion(applicationStatus.getFrameworkVersion());
                                    dto.setHttpsUseYn(applicationStatus.getHttpsUseYn());
                                    dto.setUseDbms(applicationStatus.getUseDbms());
                                }
                        );
                publicAgencyReportDto.getApplicationStatusList().add(dto);
            }
        }
    }

    private void setDatabaseInformation(List<ServiceDetail> serviceList, PublicAgencyReportDto publicAgencyReportDto) {
        // DBMS Status
        PublicAgencyReportDto.DatabaseStatus databaseStatus;

        for (ServiceDetail serviceDetail : serviceList) {
            List<DatabaseEngineListResponseDto> databases = databaseMapper.selectDatabaseEngineList(serviceDetail.getProjectId(), serviceDetail.getServiceId(), null);

            for (DatabaseEngineListResponseDto database : databases) {
                DatabaseSummary databaseSummary = databaseSummaryRepository.findById(database.getDatabaseInventoryId()).orElse(null);

                databaseStatus = new PublicAgencyReportDto.DatabaseStatus();
                databaseStatus.setSystemId(serviceDetail.getServiceId());
                databaseStatus.setSystemName(serviceDetail.getServiceName());
                databaseStatus.setServerId(database.getServerInventoryId());
                databaseStatus.setDatabaseId(database.getDatabaseInventoryId());
                databaseStatus.setDatabaseName(database.getDatabaseInventoryName());
                databaseStatus.setServerName(database.getServerInventoryName());
                databaseStatus.setEngineName(Domain1013.valueOf(database.getInventoryDetailTypeCode()).enname());

                if (databaseSummary != null) {
                    databaseStatus.setVersion(databaseSummary.getVersion());
                    databaseStatus.setDbSizeMb(databaseSummary.getDbSizeMb());
                }

                publicAgencyReportDto.getDatabaseStatusList().add(databaseStatus);
            }
        }
    }

    public ByteArrayOutputStream getRoroReport(Long projectId) {
        log.debug("===============================================");
        log.debug("+:+:+:+: Start Project Report Download +:+:+:+:");
        log.debug("===============================================");

        // project check
        ProjectMaster projectMaster = projectMasterRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));

        List<ServiceDetail> services;
        List<MiddlewareResponse> webServers;
        List<MiddlewareResponse> wasServers;
        List<ServerResponse> servers;
        List<MiddlewareResponse> middlewares;
        List<DatabaseEngineListResponseDto> databases;
        List<ApplicationResponse> applications;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Workbook workbook = new XSSFWorkbook(ReportService.class.getResourceAsStream("/template/RoRo_Service_Report_Template_v3.xlsx"));

            serviceReportHelper.createCoverToExcel(workbook, projectMaster);
            createCellStyle(workbook);

            List<ServiceMaster> serviceMasters = serviceMasterRepository.findAllByProjectIdAndDeleteYn(projectId, "N");
            log.debug("[Project Report] Get service list. Service count : [{}]", serviceMasters.size());

            int count = 0;
            for (ServiceMaster serviceMaster : serviceMasters) {
                services = new ArrayList<>();
                webServers = new ArrayList<>();
                wasServers = new ArrayList<>();

                // 서비스에 종속된 서버, 미들웨어, 데이터베이스, 애플리케이션을 가져온다.
                ServiceDetail service = serviceMapper.selectService(projectId, serviceMaster.getServiceId());
                services.add(service);

                servers = serverMapper.selectServerList(projectId, serviceMaster.getServiceId());
                log.debug("[Project Report - {}] Get server list in [{}] service. Server count : [{}]", ++count, serviceMaster.getServiceName(), servers.size());

                middlewares = middlewareMapper.selectMiddlewareList(projectId, serviceMaster.getServiceId(), null, Domain1001.MW.name());
                log.debug("[Project Report - {}] Get middleware list in [{}] service. Middlewares count : [{}]", count, serviceMaster.getServiceName(), middlewares.size());

                databases = databaseMapper.selectDatabaseEngineList(projectId, serviceMaster.getServiceId(), null);
                log.debug("[Project Report - {}] Get databases list in [{}] service. Databases count : [{}]", count, serviceMaster.getServiceName(), databases.size());

                applications = applicationMapper.getApplications(projectId, serviceMaster.getServiceId(), null);
                log.debug("[Project Report - {}] Get applications list in [{}] service. Applications count : [{}]", count, serviceMaster.getServiceName(), applications.size());

                for (MiddlewareResponse mw : middlewares) {
                    if ("WAS".equals(mw.getMiddlewareTypeCode())) {
                        wasServers.add(mw);
                    } else if ("WEB".equals(mw.getMiddlewareTypeCode())) {
                        webServers.add(mw);
                    }
                }

                serviceReportHelper.createServerToExcel(workbook, servers, service);
                serviceReportHelper.createWebServerToExcel(workbook, webServers, service);
                serviceReportHelper.createWasServerToExcel(workbook, wasServers, service);
                serviceReportHelper.createDatabaseToExcel(workbook, databases, service);
                serviceReportHelper.createApplicationToExcel(workbook, applications, service);
            }


            log.debug("[Project Report] Start writing an excel workbook.");

            ExcelUtil.autoSizeColumn(workbook);
            workbook.write(out);

            log.debug("================================================");
            log.debug("+:+:+:+: Finish Project Report Download +:+:+:+:");
            log.debug("================================================");

            return out;
        } catch (Exception e) {
            log.error("Unhandled Exception occurred while create roro project report.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }
}
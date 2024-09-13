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
 * Jaeeon Bae       1월 25, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.domain.inventory.service.helper.ServiceReportHelper;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.InstalledSoftware;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.Process;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.inventory.application.ApplicationResponse;
import io.playce.roro.common.dto.inventory.database.DatabaseEngineListResponseDto;
import io.playce.roro.common.dto.inventory.middleware.MiddlewareResponse;
import io.playce.roro.common.dto.inventory.process.InventoryProcess;
import io.playce.roro.common.dto.inventory.process.InventoryProcessResponse;
import io.playce.roro.common.dto.inventory.report.*;
import io.playce.roro.common.dto.inventory.server.ServerResponse;
import io.playce.roro.common.dto.inventory.service.ServiceDetail;
import io.playce.roro.common.dto.inventory.service.ServiceResponse;
import io.playce.roro.common.util.DeduplicationUtil;
import io.playce.roro.common.util.support.ExcelHelper;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.inventory.application.ApplicationMapper;
import io.playce.roro.mybatis.domain.inventory.database.DatabaseMapper;
import io.playce.roro.mybatis.domain.inventory.middleware.MiddlewareMapper;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import io.playce.roro.mybatis.domain.inventory.service.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static io.playce.roro.api.common.util.ExcelUtil.*;
import static io.playce.roro.api.domain.inventory.controller.ReportController.DATE_FORMAT;
import static io.playce.roro.common.util.JsonUtil.getJsonObject;
import static io.playce.roro.common.util.JsonUtil.isJsonArray;

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
public class ReportService {

    private static final String UNDER_BAR = "_";

    private final ProjectMasterRepository projectMasterRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final ServerMasterRepository serverMasterRepository;
    private final CredentialMasterRepository credentialMasterRepository;
    private final MiddlewareMasterRepository middlewareMasterRepository;
    private final DatabaseMasterRepository databaseMasterRepository;
    private final InventoryProcessRepository inventoryProcessRepository;
    private final ExcludedExcelSheetRepository excludedExcelSheetRepository;

    private final ServiceMapper serviceMapper;
    private final ServerMapper serverMapper;
    private final MiddlewareMapper middlewareMapper;
    private final DatabaseMapper databaseMapper;
    private final ApplicationMapper applicationMapper;
    private final InventoryProcessMapper inventoryProcessMapper;

    private final ServiceReportHelper serviceReportHelper;

    public InventoryProcessResponse getInventoryProcess(String inventoryTypeCode, Long inventoryId) {
        // 가장 마지막 성공 상태의 Inventory Process를 가져온다.
        return inventoryProcessMapper
                .selectLastCompletedScanByInventoryId(inventoryTypeCode, Domain1002.SCAN.name(), inventoryId);
    }

    public InventoryProcessResponse getInventoryProcess(String inventoryTypeCode, Long inventoryId, Long inventoryProcessId) {
        InventoryProcessResponse inventoryProcessResponse = inventoryProcessMapper.selectInventoryProcessById(inventoryProcessId);

        if (inventoryProcessResponse != null) {
            InventoryMaster inventory = inventoryMasterRepository.findById(inventoryProcessResponse.getInventoryId()).orElse(null);

            if (Domain1001.SVR.name().equals(inventoryTypeCode)) {
                if (inventory == null || !inventory.getInventoryId().equals(inventoryId)) {
                    throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE, "Inventory Process ID does not match with Server ID.");
                }
            } else if (Domain1001.MW.name().equals(inventoryTypeCode)) {
                if (inventory == null || !inventory.getInventoryId().equals(inventoryId)) {
                    throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE, "Inventory Process ID does not match with Middleware ID.");
                }
            } else if (Domain1001.APP.name().equals(inventoryTypeCode)) {
                if (inventory == null || !inventory.getInventoryId().equals(inventoryId)) {
                    throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE, "Inventory Process ID does not match with Application ID.");
                }
            } else if (Domain1001.DBMS.name().equals(inventoryTypeCode)) {
                if (inventory == null || !inventory.getInventoryId().equals(inventoryId)) {
                    throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE, "Inventory Process ID does not match with Database ID.");
                }
            }
        } else {
            throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE, "Inventory Process does not exist.");
        }

        if (!Domain1002.SCAN.name().equals(inventoryProcessResponse.getInventoryProcessTypeCode())) {
            throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE, "Inventory Process ID is not a SCAN type.");
        }

        if (!Domain1003.CMPL.name().equals(inventoryProcessResponse.getInventoryProcessResultCode()) &&
                !Domain1003.PC.name().equals(inventoryProcessResponse.getInventoryProcessResultCode())) {
            throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE, "The COMPLETED Inventory Process does not exist.");
        }

        return inventoryProcessResponse;
    }

    public String getFileName(String inventoryTypeCode, Long inventoryId, List<Pattern> patterns, String fileType, Date startDatetime) {
        String type = "";
        String name = "";
        String scannedDate = DATE_FORMAT.format(startDatetime);
        String projectName = "";
        String serviceName = "";
        String serverName = "";
        String serviceBusinessCode = "";
        String serviceBusinessCategory = "";
        String serverIpAddress = "";
        String serverPort = "";
        String serverUsername = "";
        String middlewareType = "";
        String middlewareVendor = "";
        String middlewareEngineName = "";
        String middlewareEngineVersion = "";
        String applicationType = "";
        String databaseType = "";
        String databasePort = "";
        String databaseServiceName = "";
        String databaseUsername = "";

        Long projectId = null;

        if (CommonConstants.SERVICE_TYPE_CODE.equals(inventoryTypeCode)) {
            ServiceMaster service = serviceMasterRepository.findById(inventoryId).orElseThrow(
                    () -> new ResourceNotFoundException("Service ID : " + inventoryId + " Not Found."));

            projectId = service.getProjectId();

            type = "Service";
            name = service.getServiceName();
            serviceBusinessCode = service.getBusinessCategoryCode();
            serviceBusinessCategory = service.getBusinessCategoryName();
        } else if (Domain1001.SVR.name().equals(inventoryTypeCode)) {
            InventoryMaster serverInventory = inventoryMasterRepository.findById(inventoryId)
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));

            ServerMaster server = serverMasterRepository.findById(inventoryId)
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));

            CredentialMaster credential = credentialMasterRepository.findById(serverInventory.getCredentialId())
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.RESOURCE_NOT_FOUND, "The Credential(" + serverInventory.getCredentialId() + ") not found"));

            projectId = serverInventory.getProjectId();

            type = "Server";
            name = serverInventory.getInventoryName();
            serviceName = generateServiceName(serverInventory);
            serverIpAddress = server.getRepresentativeIpAddress();
            serverPort = Integer.toString(server.getConnectionPort());
            serverUsername = credential.getUserName();
        } else if (Domain1001.MW.name().equals(inventoryTypeCode)) {
            InventoryMaster middlewareInventory = inventoryMasterRepository.findById(inventoryId)
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_MIDDLEWARE_NOT_FOUND));

            MiddlewareMaster middleware = middlewareMasterRepository.findById(inventoryId)
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_MIDDLEWARE_NOT_FOUND));

            InventoryMaster serverInventory = inventoryMasterRepository.findById(middlewareInventory.getServerInventoryId())
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));

            projectId = middlewareInventory.getProjectId();

            type = "Middleware";
            name = middlewareInventory.getInventoryName();
            serviceName = generateServiceName(middlewareInventory);
            serverName = serverInventory.getInventoryName();
            middlewareType = middleware.getMiddlewareTypeCode();
            middlewareVendor = middleware.getVendorName();
            middlewareEngineName = middlewareInventory.getInventoryDetailTypeCode();
            middlewareEngineVersion = middleware.getEngineVersion();
        } else if (Domain1001.APP.name().equals(inventoryTypeCode)) {
            InventoryMaster applicationInventory = inventoryMasterRepository.findById(inventoryId)
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_APPLICATION_NOT_FOUND));

            InventoryMaster serverInventory = inventoryMasterRepository.findById(applicationInventory.getServerInventoryId())
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));

            projectId = applicationInventory.getProjectId();

            type = "Application";
            name = applicationInventory.getInventoryName();
            serviceName = generateServiceName(applicationInventory);
            serverName = serverInventory.getInventoryName();
            applicationType = applicationInventory.getInventoryDetailTypeCode();
        } else if (Domain1001.DBMS.name().equals(inventoryTypeCode)) {
            InventoryMaster databaseInventory = inventoryMasterRepository.findById(inventoryId)
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_DATABASE_NOT_FOUND));

            DatabaseMaster database = databaseMasterRepository.findById(inventoryId)
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_DATABASE_NOT_FOUND));

            InventoryMaster serverInventory = inventoryMasterRepository.findById(databaseInventory.getServerInventoryId())
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));

            CredentialMaster credential = credentialMasterRepository.findById(databaseInventory.getCredentialId())
                    .orElseThrow(() -> new RoRoApiException(ErrorCode.RESOURCE_NOT_FOUND, "The Credential(" + serverInventory.getCredentialId() + ") not found"));

            projectId = databaseInventory.getProjectId();

            type = "Database";
            name = databaseInventory.getInventoryName();
            serviceName = generateServiceName(databaseInventory);
            serverName = serverInventory.getInventoryName();
            databaseType = databaseInventory.getInventoryDetailTypeCode();
            databasePort = Integer.toString(database.getConnectionPort());
            databaseServiceName = database.getDatabaseServiceName();
            databaseUsername = credential.getUserName();
        }

        if (projectId != null) {
            ProjectMaster project = projectMasterRepository.findById(projectId).orElse(null);

            if (project != null) {
                projectName = project.getProjectName();
            }
        }

        // 다운로드 파일 이름을 조건에 맞게 설정한다.
        StringBuilder sb = new StringBuilder();
        for (Pattern pattern : patterns) {
            if (sb.length() > 0 && !sb.toString().endsWith(UNDER_BAR)) {
                sb.append(UNDER_BAR);
            }

            if (Pattern.TYPE.equals(pattern)) {
                sb.append(type);
            } else if (Pattern.ID.equals(pattern)) {
                sb.append(inventoryId);
            } else if (Pattern.NAME.equals(pattern)) {
                sb.append(name);
            } else if (Pattern.SCANNED_DATE.equals(pattern)) {
                sb.append(scannedDate);
            } else if (Pattern.PROJECT_NAME.equals(pattern)) {
                sb.append(projectName);
            } else if (Pattern.SERVICE_NAME.equals(pattern)) {
                sb.append(serviceName);
            } else if (Pattern.SERVER_NAME.equals(pattern)) {
                sb.append(serverName);
            } else if (Pattern.SERVICE_BUSINESS_CODE.equals(pattern)) {
                sb.append(serviceBusinessCode);
            } else if (Pattern.SERVICE_BUSINESS_CATEGORY.equals(pattern)) {
                sb.append(serviceBusinessCategory);
            } else if (Pattern.SERVER_IP_ADDRESS.equals(pattern)) {
                sb.append(serverIpAddress);
            } else if (Pattern.SERVER_PORT.equals(pattern)) {
                sb.append(serverPort);
            } else if (Pattern.SERVER_USERNAME.equals(pattern)) {
                sb.append(serverUsername);
            } else if (Pattern.MIDDLEWARE_TYPE.equals(pattern)) {
                sb.append(middlewareType);
            } else if (Pattern.MIDDLEWARE_VENDOR.equals(pattern)) {
                sb.append(middlewareVendor);
            } else if (Pattern.MIDDLEWARE_ENGINE_NAME.equals(pattern)) {
                sb.append(middlewareEngineName);
            } else if (Pattern.MIDDLEWARE_ENGINE_VERSION.equals(pattern)) {
                sb.append(middlewareEngineVersion);
            } else if (Pattern.APPLICATION_TYPE.equals(pattern)) {
                sb.append(applicationType);
            } else if (Pattern.DATABASE_ENGINE_NAME.equals(pattern)) {
                sb.append(databaseType);
            } else if (Pattern.DATABASE_PORT.equals(pattern)) {
                sb.append(databasePort);
            } else if (Pattern.DATABASE_SERVICE_NAME.equals(pattern)) {
                sb.append(databaseServiceName);
            } else if (Pattern.DATABASE_USERNAME.equals(pattern)) {
                sb.append(databaseUsername);
            }
        }

        if (sb.length() > 0) {
            if (FileType.EXCEL.name().equals(fileType)) {
                sb.append(".xlsx");
            } else if (FileType.JSON.name().equals(fileType)) {
                sb.append(".json");
            }
        }

        return sb.toString();
    }

    /**
     * inventory process result 파일들을 압축한다.
     */
    public ByteArrayInputStream getCompressed(Long projectId, String inventoryTypeCode, Long serviceId,
                                              Long serverInventoryId, List<Long> inventoryIds, List<Pattern> patterns, String fileType) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            List<InventoryProcess.CompleteScan> inventoryProcessList = new ArrayList<>();

            if (CommonConstants.SERVICE_TYPE_CODE.equals(inventoryTypeCode)) {
                try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                    if (inventoryIds == null || inventoryIds.size() == 0) {
                        List<ServiceDetail> serviceList = serviceMapper.selectServiceList(projectId);

                        inventoryIds = new ArrayList<>();
                        for (ServiceDetail service : serviceList) {
                            inventoryIds.add(service.getServiceId());
                        }
                    }

                    for (Long id : inventoryIds) {
                        try (ByteArrayInputStream bais = serviceReport(projectId, id)) {
                            String fileName = getFileName(inventoryTypeCode, id, patterns, FileType.EXCEL.name(), new Date());

                            zos.putNextEntry(new ZipEntry(fileName));
                            IOUtils.copy(bais, zos);
                            zos.closeEntry();
                        }
                    }
                }

            } else {
                if (inventoryIds != null && inventoryIds.size() >= 0) {
                    // ids 값이 있으면 serverId는 무시한다.
                    for (Long id : inventoryIds) {
                        InventoryProcess.CompleteScan inventoryProcess = generateInventoryProcess(inventoryTypeCode, id);

                        if (inventoryProcess != null) {
                            inventoryProcessList.add(inventoryProcess);
                        }
                    }
                } else {
                    if (Domain1001.SVR.name().equals(inventoryTypeCode)) {
                        List<ServerResponse> serverList;

                        // serviceId가 null이 아니면 해당 서비스의 모든 항목 조회
                        if (serviceId != null) {
                            serverList = serverMapper.selectServerList(projectId, serviceId);
                        } else {
                            serverList = serverMapper.selectServerList(projectId, null);
                        }

                        for (ServerResponse server : serverList) {
                            InventoryProcess.CompleteScan inventoryProcess = generateInventoryProcess(inventoryTypeCode, server.getServerInventoryId());

                            if (inventoryProcess != null) {
                                inventoryProcessList.add(inventoryProcess);
                            }
                        }

                    } else if (Domain1001.MW.name().equals(inventoryTypeCode)) {
                        List<MiddlewareResponse> middlewareList;

                        // serviceId 또는 serverInventoryId가 null이 아니면 해당 서비스/서버의 모든 항목 조회
                        if (serviceId != null) {
                            middlewareList = middlewareMapper.selectMiddlewareList(projectId, serviceId, null, Domain1001.MW.name());
                        } else if (serverInventoryId != null) {
                            middlewareList = middlewareMapper.selectMiddlewareList(projectId, null, serverInventoryId, Domain1001.MW.name());
                        } else {
                            middlewareList = middlewareMapper.selectMiddlewareList(projectId, null, null, Domain1001.MW.name());
                        }

                        for (MiddlewareResponse middleware : middlewareList) {
                            InventoryProcess.CompleteScan inventoryProcess = generateInventoryProcess(inventoryTypeCode, middleware.getMiddlewareInventoryId());

                            if (inventoryProcess != null) {
                                inventoryProcessList.add(inventoryProcess);
                            }
                        }

                    } else if (Domain1001.APP.name().equals(inventoryTypeCode)) {
                        List<ApplicationResponse> applicationList;

                        // serviceId 또는 serverInventoryId가 null이 아니면 서비스/서버의 모든 항목 조회
                        if (serviceId != null) {
                            applicationList = applicationMapper.getApplications(projectId, serviceId, null);
                        } else if (serverInventoryId != null) {
                            applicationList = applicationMapper.getApplications(projectId, null, serverInventoryId);
                        } else {
                            applicationList = applicationMapper.getApplications(projectId, null, null);
                        }

                        for (ApplicationResponse application : applicationList) {
                            InventoryProcess.CompleteScan inventoryProcess = generateInventoryProcess(inventoryTypeCode, application.getApplicationInventoryId());

                            if (inventoryProcess != null) {
                                inventoryProcessList.add(inventoryProcess);
                            }
                        }

                    } else if (Domain1001.DBMS.name().equals(inventoryTypeCode)) {
                        List<DatabaseEngineListResponseDto> databaseList;

                        // serviceId 또는 serverInventoryId가 null이 아니면 서비스/서버의 모든 항목 조회
                        if (serviceId != null) {
                            databaseList = databaseMapper.selectDatabaseEngineList(projectId, serviceId, null);
                        } else if (serverInventoryId != null) {
                            databaseList = databaseMapper.selectDatabaseEngineList(projectId, null, serverInventoryId);
                        } else {
                            databaseList = databaseMapper.selectDatabaseEngineList(projectId, null, null);
                        }

                        for (DatabaseEngineListResponseDto database : databaseList) {
                            InventoryProcess.CompleteScan inventoryProcess = generateInventoryProcess(inventoryTypeCode, database.getDatabaseInventoryId());

                            if (inventoryProcess != null) {
                                inventoryProcessList.add(inventoryProcess);
                            }
                        }
                    }
                }

                if (CollectionUtils.isEmpty(inventoryProcessList) || inventoryProcessList.size() == 0) {
                    throw new Exception("COMPLETED inventory processes are not exists");
                }

                // zip 파일로 만들어서 내려준다.
                try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                    List<String> fileNameList = new ArrayList<>();
                    for (InventoryProcess.CompleteScan completeScan : inventoryProcessList) {
                        if (StringUtils.isEmpty(completeScan.getInventoryProcessResultExcelPath())) {
                            log.error("Report Error Inventory ProcessResultExcelPath is null ==> Inventory Process ID : {}, ", completeScan.getInventoryProcessId());
                        } else {
                            File file = null;

                            io.playce.roro.jpa.entity.InventoryProcess inventoryProcess = inventoryProcessRepository.findById(completeScan.getInventoryProcessId())
                                    .orElseThrow(() -> new ResourceNotFoundException("Inventory Process ID : " + completeScan.getInventoryProcessId() + " Not Found."));

                            String fileName = getFileName(inventoryTypeCode, inventoryProcess.getInventoryId(), patterns, fileType, inventoryProcess.getInventoryProcessStartDatetime());

                            if (FileType.EXCEL.name().equals(fileType)) {
                                file = new File(completeScan.getInventoryProcessResultExcelPath());
                            } else if (FileType.JSON.name().equals(fileType)) {
                                file = new File(completeScan.getInventoryProcessResultJsonPath());
                            }

                            if (fileName != null && file != null && file.exists()) {
                                fileName = getFileName(fileNameList, fileName, 1);

                                try (FileInputStream fis = new FileInputStream(file)) {
                                    zos.putNextEntry(new ZipEntry(fileName));
                                    IOUtils.copy(fis, zos);
                                    zos.closeEntry();
                                }
                            } else {
                                if (file != null && !file.exists()) {
                                    log.warn("Unhandled add file({}) to zip entry. File does not exists.", file.getAbsolutePath());
                                }
                            }
                        }
                    }
                }
            }

            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create a zip file.", e);
            throw new RoRoApiException(ErrorCode.ASSESSMENT_ZIP_FAILED, e.getMessage());
        }
    }

    @NotNull
    private String generateServiceName(InventoryMaster inventory) {
        StringBuilder sb = new StringBuilder();
        List<ServiceResponse> serviceList = serviceMapper.selectServiceByInventoryId(inventory.getInventoryId());
        for (ServiceResponse service : serviceList) {
            if (sb.length() > 0) {
                sb.append(UNDER_BAR);
            }
            sb.append(service.getServiceName());
        }
        return sb.toString();
    }

    private InventoryProcess.CompleteScan generateInventoryProcess(String inventoryTypeCode, Long inventoryId) {
        InventoryProcess.CompleteScan inventoryProcess = null;

        // 가장 마지막 성공 상태의 Inventory Process를 가져온다.
        if (Domain1001.SVR.name().equals(inventoryTypeCode)) {
            inventoryMasterRepository.findById(inventoryId).orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND));
            inventoryProcess = inventoryProcessMapper.selectLastCompleteInventoryProcess(inventoryId, Domain1002.SCAN.name());
        } else if (Domain1001.MW.name().equals(inventoryTypeCode)) {
            inventoryMasterRepository.findById(inventoryId).orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_MIDDLEWARE_NOT_FOUND));
            inventoryProcess = inventoryProcessMapper.selectLastCompleteInventoryProcess(inventoryId, Domain1002.SCAN.name());
        } else if (Domain1001.APP.name().equals(inventoryTypeCode)) {
            inventoryMasterRepository.findById(inventoryId).orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_APPLICATION_NOT_FOUND));
            inventoryProcess = inventoryProcessMapper.selectLastCompleteInventoryProcess(inventoryId, Domain1002.SCAN.name());
        } else if (Domain1001.DBMS.name().equals(inventoryTypeCode)) {
            inventoryMasterRepository.findById(inventoryId).orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_DATABASE_NOT_FOUND));
            inventoryProcess = inventoryProcessMapper.selectLastCompleteInventoryProcess(inventoryId, Domain1002.SCAN.name());
        } else {
            return null;
        }

        return inventoryProcess;
    }

    /**
     * <pre>
     * 중복되지 않는 파일명을 가져온다.
     * </pre>
     */
    private String getFileName(List<String> fileNameList, String fileName, int idx) {
        if (fileNameList.contains(fileName)) {
            String extension = FilenameUtils.getExtension(fileName);
            fileName = FilenameUtils.removeExtension(fileName).replaceAll("\\(\\d+\\)", "");
            fileName = fileName + "(" + idx + ")." + extension;

            return getFileName(fileNameList, fileName, ++idx);
        } else {
            fileNameList.add(fileName);
            return fileName;
        }
    }

    public ByteArrayInputStream serviceReport(Long projectId, Long serviceId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        serviceMasterRepository.findById(serviceId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.INVENTORY_SERVICE_NOT_FOUND));

        List<ServiceDetail> services = new ArrayList<>();
        List<MiddlewareResponse> webServers = new ArrayList<>();
        List<MiddlewareResponse> wasServers = new ArrayList<>();

        // 서비스에 종속된 서버, 미들웨어, 데이터베이스, 애플리케이션을 가져온다.
        ServiceDetail service = serviceMapper.selectService(projectId, serviceId);
        services.add(service);
        List<ServerResponse> servers = serverMapper.selectServerList(projectId, serviceId);
        List<MiddlewareResponse> middlewares = middlewareMapper.selectMiddlewareList(projectId, serviceId, null, Domain1001.MW.name());
        List<DatabaseEngineListResponseDto> databases = databaseMapper.selectDatabaseEngineList(projectId, serviceId, null);
        List<ApplicationResponse> applications = applicationMapper.getApplications(projectId, serviceId, null);

        for (MiddlewareResponse mw : middlewares) {
            if ("WAS".equals(mw.getMiddlewareTypeCode())) {
                wasServers.add(mw);
            } else if ("WEB".equals(mw.getMiddlewareTypeCode())) {
                webServers.add(mw);
            }
        }

        try {
            Workbook workbook = new XSSFWorkbook(ReportService.class.getResourceAsStream("/template/RoRo_Service_Report_Template_v3.xlsx"));

            serviceReportHelper.createCoverToExcel(workbook, services);
            createCellStyle(workbook);
            serviceReportHelper.createServerToExcel(workbook, servers, service);
            serviceReportHelper.createWebServerToExcel(workbook, webServers, service);
            serviceReportHelper.createWasServerToExcel(workbook, wasServers, service);
            serviceReportHelper.createDatabaseToExcel(workbook, databases, service);
            serviceReportHelper.createApplicationToExcel(workbook, applications, service);

            ExcelUtil.autoSizeColumn(workbook);
            workbook.write(out);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while Service Report to Excel.", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateWindowsMiddlewareExcel(Long projectId) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        List<ServerResponse> windowsServers = serverMapper.selectWindowsServer(projectId);

        List<WindowsProcessMiddlewareExcel> windowsProcessMiddlewareExcels = getWindowsMiddlewareByProcess(windowsServers, objectMapper);
        List<WindowsMiddlewareInstallSoftwareExcel> windowsMiddlewareInstallSoftwareExcels = getWindowsMiddlewareByInstalledSoftware(windowsServers, objectMapper);

        try {
            Workbook workbook = new XSSFWorkbook();

            // Process Sheet.
            Sheet workSheet = workbook.createSheet("Windows_Middleware");

            CellStyle headerStyle = createHeaderCellStyle(workbook);
            createCellStyle(workbook);

            workSheet.setColumnWidth(0, 4000);
            workSheet.setColumnWidth(1, 10000);
            workSheet.setColumnWidth(2, 4000);
            workSheet.setColumnWidth(3, 8000);
            workSheet.setColumnWidth(4, 5000);
            workSheet.setColumnWidth(5, 4000);
            workSheet.setColumnWidth(6, 4000);
            workSheet.setColumnWidth(7, 14000);
            workSheet.setColumnWidth(8, 9000);

            Row row = workSheet.createRow(CommonConstants.EXCEL_HEADER_FIRST_ROW_INDEX);

            int headerColumnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;

            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Service Id");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Service Name");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Server Id");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Server Name");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Middleware Type");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Vendor");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Solution Name");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Solution Path");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Java Version");

            int columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
            int rowIndex = CommonConstants.EXCEL_BODY_FIRST_ROW_INDEX;

            for (WindowsProcessMiddlewareExcel windowsProcessMiddlewareExcel : windowsProcessMiddlewareExcels) {
                row = workSheet.createRow(rowIndex);

                createCell(row, columnIndex++).setCellValue(windowsProcessMiddlewareExcel.getServiceIds());
                createCell(row, columnIndex++).setCellValue(windowsProcessMiddlewareExcel.getServiceNames());
                createCell(row, columnIndex++).setCellValue(windowsProcessMiddlewareExcel.getServerId());
                createCell(row, columnIndex++).setCellValue(windowsProcessMiddlewareExcel.getServerName());
                createCell(row, columnIndex++).setCellValue(windowsProcessMiddlewareExcel.getMiddlewareType());
                createCell(row, columnIndex++).setCellValue(windowsProcessMiddlewareExcel.getVendor());
                createCell(row, columnIndex++).setCellValue(windowsProcessMiddlewareExcel.getSolutionName());
                createCell(row, columnIndex++).setCellValue(windowsProcessMiddlewareExcel.getSolutionPath());
                createCell(row, columnIndex++).setCellValue(windowsProcessMiddlewareExcel.getJavaVersion());

                rowIndex++;
                columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
            }

            // Start --- Install Software Sheet
            Sheet installSoftwareWorkSheet = workbook.createSheet("Install_Software");
            installSoftwareWorkSheet.setColumnWidth(0, 4000);
            installSoftwareWorkSheet.setColumnWidth(1, 10000);
            installSoftwareWorkSheet.setColumnWidth(2, 4000);
            installSoftwareWorkSheet.setColumnWidth(3, 8000);
            installSoftwareWorkSheet.setColumnWidth(4, 20000);
            installSoftwareWorkSheet.setColumnWidth(5, 10000);

            row = installSoftwareWorkSheet.createRow(CommonConstants.EXCEL_HEADER_FIRST_ROW_INDEX);

            headerColumnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;

            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Service Id");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Service Name");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Server Id");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Server Name");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Display Name");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Version");

            columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
            rowIndex = CommonConstants.EXCEL_BODY_FIRST_ROW_INDEX;

            for (WindowsMiddlewareInstallSoftwareExcel windowsMiddlewareInstallSoftwareExcel : windowsMiddlewareInstallSoftwareExcels) {
                row = installSoftwareWorkSheet.createRow(rowIndex);

                createCell(row, columnIndex++).setCellValue(windowsMiddlewareInstallSoftwareExcel.getServiceIds());
                createCell(row, columnIndex++).setCellValue(windowsMiddlewareInstallSoftwareExcel.getServiceNames());
                createCell(row, columnIndex++).setCellValue(windowsMiddlewareInstallSoftwareExcel.getServerId());
                createCell(row, columnIndex++).setCellValue(windowsMiddlewareInstallSoftwareExcel.getServerName());
                createCell(row, columnIndex++).setCellValue(windowsMiddlewareInstallSoftwareExcel.getDisplayName());
                createCell(row, columnIndex++).setCellValue(windowsMiddlewareInstallSoftwareExcel.getDisplayVersion());

                rowIndex++;
                columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
            }

            workbook.write(out);
        } catch (Exception e) {
            log.error("Unhandled Exception occurred while Export to Excel : " + e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private List<WindowsProcessMiddlewareExcel> getWindowsMiddlewareByProcess
            (List<ServerResponse> windowsServers, ObjectMapper objectMapper) {
        List<WindowsProcessMiddlewareExcel> windowsProcessMiddlewareExcels = new ArrayList<>();

        for (ServerResponse windowsServer : windowsServers) {
            // Assessment 결과 중 최근에 성공한 1건.
            String resultJson = inventoryProcessMapper.selectInventoryProcessLastSuccessComplete(windowsServer.getServerInventoryId());
            windowsServer.setServices(serviceMapper.getServiceSummaries(windowsServer.getServerInventoryId()));

            if (StringUtils.isNotEmpty(resultJson)) {
                Map<String, Object> assessmentMap = new ObjectMapper().readValue(resultJson, HashMap.class);

                String assessmentJson = new ObjectMapper().writeValueAsString(assessmentMap);
                JSONObject jsonObject = getJsonObject((JSONObject) new JSONParser().parse(assessmentJson));

                // 결과값 중 Process 키 값을 가져와서 Mapping을 한다.
                String process = jsonObject.get("process").toString();
                String installedSoftware = jsonObject.get("installedSoftware").toString();

                List<Process> processes = new ArrayList<>();
                if (StringUtils.isNotEmpty(process)) {
                    if (isJsonArray(process)) {
                        processes = new ArrayList<>(Arrays.asList(objectMapper.readValue(process, Process[].class)));
                    } else {
                        processes.add(objectMapper.readValue(process, Process.class));
                    }
                }

                List<InstalledSoftware> installedSoftwares = new ArrayList<>();
                if (StringUtils.isNotEmpty(installedSoftware)) {
                    if (isJsonArray(installedSoftware)) {
                        installedSoftwares = new ArrayList<>(Arrays.asList(objectMapper.readValue(installedSoftware, InstalledSoftware[].class)));
                    } else {
                        installedSoftwares.add(objectMapper.readValue(installedSoftware, InstalledSoftware.class));
                    }
                }

                List<String> serviceIds = new ArrayList<>();
                List<String> serviceNames = new ArrayList<>();

                for (io.playce.roro.common.dto.inventory.service.Service service : windowsServer.getServices()) {
                    serviceIds.add(String.valueOf(service.getServiceId()));
                    serviceNames.add(service.getServiceName());
                }

                String javaVersion = "";

                for (InstalledSoftware tempInstalledSoftware : installedSoftwares) {
                    if (tempInstalledSoftware.getDisplayName().toLowerCase().contains("java")
                            || tempInstalledSoftware.getDisplayName().toLowerCase().contains("jdk")
                            || tempInstalledSoftware.getDisplayName().toLowerCase().contains("jre")) {

                        javaVersion = StringUtils.isEmpty(tempInstalledSoftware.getDisplayName())
                                ? tempInstalledSoftware.getDisplayVersion() : tempInstalledSoftware.getDisplayName();
                    }
                }

                windowsProcessMiddlewareExcels.addAll(extractWindowsMiddleware(serviceIds, serviceNames, windowsServer, processes, javaVersion));
            }
        }

        return windowsProcessMiddlewareExcels;
    }

    private List<WindowsProcessMiddlewareExcel> extractWindowsMiddleware
            (List<String> serviceIds, List<String> serviceNames, ServerResponse windowsServer,
             List<Process> processes, String javaVersion) {
        final String middlewareTypeWeb = "WEB";
        final String middlewareTypeWas = "WAS";

        final String apacheProcessName = "httpd";
        final String webSphereProcessName = "was.install.root";
        final String jbossProcessName = "jboss.home.dir";

        final String profileFiles = "Program Files";
        final String profileFilesX86 = "Program Files (x86)";

        final String APACHE = "Apache";
        final String TOMCAT = "Tomcat";
        final String WEBLOGIC = "Weblogic";
        final String WEBSPHERE = "WebSphere";

        List<WindowsProcessMiddlewareExcel> windowsProcessMiddlewareExcels = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(processes)) {
            for (Process tempProcess : processes) {
                if (StringUtils.isNotEmpty(tempProcess.getCommandLine())) {
                    // 공백으로 Split 하기 때문에 Program Files를 Short Path로 변경.
                    tempProcess.setCommandLine(tempProcess.getCommandLine().replaceAll(profileFiles, "PROGRA~1"));
                    tempProcess.setCommandLine(tempProcess.getCommandLine().replaceAll(profileFilesX86, "PROGRA~2"));

                    WindowsProcessMiddlewareExcel windowsProcessMiddlewareExcel = new WindowsProcessMiddlewareExcel();

                    windowsProcessMiddlewareExcel.setServiceIds(StringUtils.join(serviceIds, ","));
                    windowsProcessMiddlewareExcel.setServiceNames(StringUtils.join(serviceNames, ","));
                    windowsProcessMiddlewareExcel.setServerId(String.valueOf(windowsServer.getServerInventoryId()));
                    windowsProcessMiddlewareExcel.setServerName(windowsServer.getServerInventoryName());
                    windowsProcessMiddlewareExcel.setJavaVersion(javaVersion);

                    if (tempProcess.getPath().toLowerCase().contains(apacheProcessName)) {
                        windowsProcessMiddlewareExcel.setMiddlewareType(middlewareTypeWeb);
                        windowsProcessMiddlewareExcel.setVendor(APACHE);
                        windowsProcessMiddlewareExcel.setSolutionName(APACHE);
                        windowsProcessMiddlewareExcel.setSolutionPath(tempProcess.getPath().substring(0, tempProcess.getPath().indexOf("\\bin")));

                        windowsProcessMiddlewareExcels.add(windowsProcessMiddlewareExcel);

                    } else if (tempProcess.getPath().toLowerCase().contains("tomcat") && tempProcess.getPath().toLowerCase().contains("exe")) {
                        windowsProcessMiddlewareExcel.setMiddlewareType(middlewareTypeWas);
                        windowsProcessMiddlewareExcel.setVendor(APACHE);
                        windowsProcessMiddlewareExcel.setSolutionName(TOMCAT);
                        windowsProcessMiddlewareExcel.setSolutionPath(tempProcess.getPath().substring(0, tempProcess.getPath().indexOf("\\bin")));
                        windowsProcessMiddlewareExcels.add(windowsProcessMiddlewareExcel);
                    } else if (tempProcess.getCommandLine().contains("catalina.base") || tempProcess.getCommandLine().contains("catalina.home")) {
                        windowsProcessMiddlewareExcel.setMiddlewareType(middlewareTypeWas);
                        windowsProcessMiddlewareExcel.setVendor(APACHE);
                        windowsProcessMiddlewareExcel.setSolutionName(TOMCAT);

                        String[] splitArray = splitToArrayBySpace(tempProcess.getCommandLine());

                        for (String argument : splitArray) {
                            if (argument.contains("-Dcatalina.base") || argument.contains("-Dcatalina.home")) {
                                String solutionPath = getPropertyValue(argument);

                                if (solutionPath.startsWith(".")) {
                                    windowsProcessMiddlewareExcel.setSolutionPath(tempProcess.getPath() + " : Execute Path (" + solutionPath + ")");
                                } else {
                                    windowsProcessMiddlewareExcel.setSolutionPath(solutionPath);
                                }
                            }
                        }

                        windowsProcessMiddlewareExcels.add(windowsProcessMiddlewareExcel);
                    } else if (tempProcess.getCommandLine().contains("wls.home") || tempProcess.getCommandLine().contains("weblogic.home")) {
                        windowsProcessMiddlewareExcel.setMiddlewareType(middlewareTypeWas);
                        windowsProcessMiddlewareExcel.setVendor("Oracle");
                        windowsProcessMiddlewareExcel.setSolutionName(WEBLOGIC);

                        String[] splitArray = splitToArrayBySpace(tempProcess.getCommandLine());

                        for (String argument : splitArray) {
                            if (argument.contains("-Dwls.home") || argument.contains("-Dweblogic.home")) {
                                windowsProcessMiddlewareExcel.setSolutionPath(getPropertyValue(argument));
                            }
                        }

                        windowsProcessMiddlewareExcels.add(windowsProcessMiddlewareExcel);
                    } else if (tempProcess.getCommandLine().contains(webSphereProcessName)) {
                        windowsProcessMiddlewareExcel.setMiddlewareType(middlewareTypeWas);
                        windowsProcessMiddlewareExcel.setVendor("IBM");
                        windowsProcessMiddlewareExcel.setSolutionName(WEBSPHERE);

                        String[] splitArray = splitToArrayBySpace(tempProcess.getCommandLine());

                        for (String argument : splitArray) {
                            if (argument.contains("-Dwas.install.root")) {
                                windowsProcessMiddlewareExcel.setSolutionPath(getPropertyValue(argument));
                            }
                        }

                        windowsProcessMiddlewareExcels.add(windowsProcessMiddlewareExcel);
                    } else if (tempProcess.getCommandLine().contains(jbossProcessName)) {
                        windowsProcessMiddlewareExcel.setMiddlewareType(middlewareTypeWas);
                        windowsProcessMiddlewareExcel.setVendor("RedHat");
                        windowsProcessMiddlewareExcel.setSolutionName("JBoss");

                        String[] splitArray = splitToArrayBySpace(tempProcess.getCommandLine());

                        for (String argument : splitArray) {
                            if (argument.contains("jboss.home.dir")) {
                                windowsProcessMiddlewareExcel.setSolutionPath(getPropertyValue(argument));
                            }
                        }

                        windowsProcessMiddlewareExcels.add(windowsProcessMiddlewareExcel);
                    }
                }
            }
        }

        // Process로 검색하기 때문에 동일한 Process가 여러개 나올 수 있다. (그래서 SolutionName 기준으로 중복제거)
        // ex) httpd 2개씩..
        List<WindowsProcessMiddlewareExcel> deduplicationWindowsProcessMiddlewareExcels =
                DeduplicationUtil.deduplication(windowsProcessMiddlewareExcels, WindowsProcessMiddlewareExcel::getSolutionName);

        return deduplicationWindowsProcessMiddlewareExcels;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public List<WindowsMiddlewareInstallSoftwareExcel> getWindowsMiddlewareByInstalledSoftware
            (List<ServerResponse> windowsServers, ObjectMapper objectMapper) {
        List<WindowsMiddlewareInstallSoftwareExcel> windowsMiddlewareInstallSoftwareExcels = new ArrayList<>();

        for (ServerResponse windowsServer : windowsServers) {
            // Assessment 결과 중 최근에 성공한 1건.
            String resultJson = inventoryProcessMapper.selectInventoryProcessLastSuccessComplete(windowsServer.getServerInventoryId());
            windowsServer.setServices(serviceMapper.getServiceSummaries(windowsServer.getServerInventoryId()));

            if (StringUtils.isNotEmpty(resultJson)) {
                // json 데이터를 가져와서 Parsing.
                Map<String, Object> assessmentMap = new ObjectMapper().readValue(resultJson, HashMap.class);
                String assessmentJson = new ObjectMapper().writeValueAsString(assessmentMap);
                JSONObject jsonObject = getJsonObject((JSONObject) new JSONParser().parse(assessmentJson));

                // 결과값 중 installedSoftware 키 값을 가져와서 Mapping을 한다.
                String installedSoftware = jsonObject.get("installedSoftware").toString();

                List<InstalledSoftware> installedSoftwares = new ArrayList<>();
                if (StringUtils.isNotEmpty(installedSoftware)) {
                    if (isJsonArray(installedSoftware)) {
                        installedSoftwares = new ArrayList<>(Arrays.asList(objectMapper.readValue(installedSoftware, InstalledSoftware[].class)));
                    } else {
                        installedSoftwares.add(objectMapper.readValue(installedSoftware, InstalledSoftware.class));
                    }
                }

                List<String> serviceIds = new ArrayList<>();
                List<String> serviceNames = new ArrayList<>();

                for (io.playce.roro.common.dto.inventory.service.Service service : windowsServer.getServices()) {
                    serviceIds.add(String.valueOf(service.getServiceId()));
                    serviceNames.add(service.getServiceName());
                }

                for (InstalledSoftware tempInstalledSoftware : installedSoftwares) {
                    if (tempInstalledSoftware.getDisplayName().toLowerCase().contains("java")
                            || tempInstalledSoftware.getDisplayName().toLowerCase().contains("jdk")
                            || tempInstalledSoftware.getDisplayName().toLowerCase().contains("jre")
                            || tempInstalledSoftware.getDisplayName().toLowerCase().contains("apache")
                            || tempInstalledSoftware.getDisplayName().toLowerCase().contains("tomcat")
                            || tempInstalledSoftware.getDisplayName().toLowerCase().contains("weblogic")
                            || tempInstalledSoftware.getDisplayName().toLowerCase().contains("websphere")
                            || tempInstalledSoftware.getDisplayName().toLowerCase().contains("jboss")) {
                        WindowsMiddlewareInstallSoftwareExcel windowsMiddlewareInstallSoftwareExcel = new WindowsMiddlewareInstallSoftwareExcel();
                        windowsMiddlewareInstallSoftwareExcel.setServiceIds(StringUtils.join(serviceIds, ","));
                        windowsMiddlewareInstallSoftwareExcel.setServiceNames(StringUtils.join(serviceNames, ","));
                        windowsMiddlewareInstallSoftwareExcel.setServerId(String.valueOf(windowsServer.getServerInventoryId()));
                        windowsMiddlewareInstallSoftwareExcel.setServerName(windowsServer.getServerInventoryName());
                        windowsMiddlewareInstallSoftwareExcel.setDisplayName(tempInstalledSoftware.getDisplayName());
                        windowsMiddlewareInstallSoftwareExcel.setDisplayVersion(tempInstalledSoftware.getDisplayVersion());

                        windowsMiddlewareInstallSoftwareExcels.add(windowsMiddlewareInstallSoftwareExcel);
                    }
                }
            }
        }

        return windowsMiddlewareInstallSoftwareExcels;
    }

    private String[] splitToArrayBySpace(String commandLine) {
        return commandLine.split(" ", -1);
    }

    private String getPropertyValue(String source) {
        return source.substring(source.indexOf("=") + 1).trim().replaceAll("\"", "");
    }

    /**
     * 디폴트 값을 만들어 준다.
     * PROJECT_NAME, TYPE, NAME, SCANNED_DATE
     */
    public List<Pattern> getDefaultPatterns(List<Pattern> patterns) {
        patterns.add(Pattern.PROJECT_NAME);
        patterns.add(Pattern.TYPE);
        patterns.add(Pattern.NAME);
        patterns.add(Pattern.SCANNED_DATE);
        return patterns;
    }

    public List<ExcelSheetDto> getCustomExcelSheetList(String inventoryTypeCode, Long inventoryId, Long inventoryProcessId) {
        List<String> requiredSheet = List.of("Cover", "Table of Contents", "Cell Reference");
        List<String> excludedSheets = excludedExcelSheetRepository.findByInventoryId(inventoryId).stream()
                .map(ExcludedExcelSheet::getSheetName)
                .collect(Collectors.toList());
        excludedSheets.addAll(requiredSheet);

        List<ExcelSheetDto> result = new ArrayList<>();
        try (var workbook = new XSSFWorkbook(getInventoryProcess(inventoryTypeCode, inventoryId, inventoryProcessId).getInventoryProcessResultExcelPath())) {
            for (Sheet sheet : workbook) {
                String sheetName = sheet.getSheetName();
                if (requiredSheet.contains(sheetName)) {
                    continue;
                }
                var sheetDto = new ExcelSheetDto();
                sheetDto.setSheetName(sheetName);
                sheetDto.setExcluded(excludedSheets.contains(sheetName));
                result.add(sheetDto);
            }
        } catch (IOException e) {
            log.error("Unhandled exception occurred while create survey template file.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
        return result;
    }

    @Transactional
    public void excludeCustomExcelSheet(Long inventoryId, List<String> sheetNames) {
        excludedExcelSheetRepository.deleteByInventoryId(inventoryId);
        for (String sheetName : sheetNames) {
            var excludedExcelSheet = new ExcludedExcelSheet();
            excludedExcelSheet.setInventoryId(inventoryId);
            excludedExcelSheet.setSheetName(sheetName);
            excludedExcelSheetRepository.save(excludedExcelSheet);
        }
    }

    public ByteArrayInputStream getCustomExcelStream(String inventoryTypeCode, Long inventoryId, Long inventoryProcessId) {
        List<String> excludedSheets = excludedExcelSheetRepository.findByInventoryId(inventoryId).stream()
                .map(ExcludedExcelSheet::getSheetName)
                .collect(Collectors.toList());

        try {
            var workbook = new SXSSFWorkbook(new XSSFWorkbook(getInventoryProcess(inventoryTypeCode, inventoryId, inventoryProcessId).getInventoryProcessResultExcelPath()));

            for (int i = workbook.getNumberOfSheets() - 1; i >= 0; i--) {
                String sheetName = workbook.getSheetAt(i).getSheetName();
                if (excludedSheets.contains(sheetName)) {
                    workbook.removeSheetAt(i);
                }
            }

            ExcelHelper.replaceContents(workbook);
            ExcelHelper.autoSizeColumn(workbook);
            var out = new ByteArrayOutputStream();
            workbook.write(out);
            // workbook.dispose();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException | InterruptedException e) {
            log.error("Unhandled exception occurred while create survey template file.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }
}
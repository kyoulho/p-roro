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
 * Jaeeon Bae       3월 21, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.api.domain.inventory.service.ApplicationService;
import io.playce.roro.api.domain.inventory.service.DatabaseService;
import io.playce.roro.api.domain.inventory.service.MiddlewareService;
import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.api.domain.inventory.service.helper.assessment.*;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.dto.assessment.AssessmentResultDto;
import io.playce.roro.common.dto.inventory.application.ApplicationDatasourceResponse;
import io.playce.roro.common.dto.inventory.application.ApplicationResponse;
import io.playce.roro.common.dto.inventory.database.DatabaseEngineListResponseDto;
import io.playce.roro.common.dto.inventory.database.DatabaseEngineResponseDto;
import io.playce.roro.common.dto.inventory.database.DatabaseInstanceListResponseDto;
import io.playce.roro.common.dto.inventory.database.DeployDatasourceList;
import io.playce.roro.common.dto.inventory.middleware.DeployApplicationList;
import io.playce.roro.common.dto.inventory.middleware.InstanceResponse;
import io.playce.roro.common.dto.inventory.middleware.MiddlewareResponse;
import io.playce.roro.common.dto.inventory.process.InventoryProcess;
import io.playce.roro.common.dto.inventory.server.ServerResponse;
import io.playce.roro.common.dto.inventory.service.ServiceDetail;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.inventory.database.DatabaseMapper;
import io.playce.roro.mybatis.domain.inventory.middleware.MiddlewareMapper;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static io.playce.roro.api.common.util.ExcelUtil.createCellHeader;
import static io.playce.roro.api.common.util.ExcelUtil.createHeaderCellStyle;

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
public class ServiceReportHelper {

    public static final String DELIMITER = ", ";
    public static final String UNDER_BAR_DELIMITER = "_";

    private final ServerService serverService;
    private final MiddlewareService middlewareService;
    private final DatabaseService databaseService;
    private final ApplicationService applicationService;

    private final InventoryProcessMapper inventoryProcessMapper;
    private final MiddlewareMapper middlewareMapper;
    private final DatabaseMapper databaseMapper;

    private final InventoryMasterRepository inventoryMasterRepository;
    private final InventoryProcessRepository inventoryProcessRepository;
    private final InventoryProcessResultRepository inventoryProcessResultRepository;
    private final ServerSummaryRepository serverSummaryRepository;
    private final ServerDiskInformationRepository serverDiskInformationRepository;
    private final DatabaseInstanceRepository databaseInstanceRepository;
    private final ServerNetworkInformationRepository serverNetworkInformationRepository;

    private final ServiceReportWithoutScanHelper serviceReportWithoutScanHelper;

    public void createCoverToExcel(Workbook workbook, List<ServiceDetail> services) {
        Sheet workSheet = workbook.getSheet("1.Cover");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (services.size() == 1) {
            // 각 서비스의 이름으로 설정
            Row row = workSheet.getRow(5);
            row.getCell(1).setCellValue(row.getCell(1).getStringCellValue().replace("Service", services.get(0).getServiceName()));

            Row serviceNameRow = workSheet.getRow(7);
            serviceNameRow.getCell(1).setCellValue(serviceNameRow.getCell(1).getStringCellValue().replace("{serviceName}", services.get(0).getServiceName()));

            // 서비스 스캔을 통한 가장 마지막 Scan 된 정보를 가져온다.
            Date startDate = inventoryProcessMapper.selectMaxInventoryProcess(services.get(0).getServiceId());

            Row scannedDateRow = workSheet.getRow(8);
            // 분석 정보 중 가장 마지막 분석 정보의 scanned Date를 가져와서 설정 ( 없으면 해당 Row를 빈 값으로 바꾼다 )
            if (startDate != null) {
                scannedDateRow.getCell(1).setCellValue(scannedDateRow.getCell(1).getStringCellValue().replace("{scannedDate}", format.format(startDate)));
            } else {
                scannedDateRow.getCell(1).setCellValue(scannedDateRow.getCell(1).getStringCellValue().replace("Scanned date : {scannedDate}", ""));
            }
        }

        Row row = workSheet.getRow(36);
        row.getCell(1).setCellValue(format.format(new Date()));
    }

    public void createCoverToExcel(Workbook workbook, ProjectMaster projectMaster) {
        Sheet workSheet = workbook.getSheet("1.Cover");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 프로젝트 이름으로 커버 페이지 설정
        Row row = workSheet.getRow(5);
        row.getCell(1).setCellValue(row.getCell(1).getStringCellValue().replace("Service", projectMaster.getProjectName()));

        Row serviceNameRow = workSheet.getRow(7);
        serviceNameRow.getCell(1).setCellValue("Project Name : " + projectMaster.getProjectName());

        // 서비스 스캔을 통한 가장 마지막 Scan 된 정보를 가져온다.
        Date startDate = inventoryProcessMapper.selectMaxInventoryProcessInProject(projectMaster.getProjectId());

        Row scannedDateRow = workSheet.getRow(8);
        // 분석 정보 중 가장 마지막 분석 정보의 scanned Date를 가져와서 설정 ( 없으면 해당 Row를 빈 값으로 바꾼다 )
        if (startDate != null) {
            scannedDateRow.getCell(1).setCellValue(scannedDateRow.getCell(1).getStringCellValue().replace("{scannedDate}", format.format(startDate)));
        } else {
            scannedDateRow.getCell(1).setCellValue(scannedDateRow.getCell(1).getStringCellValue().replace("Scanned date : {scannedDate}", ""));
        }

        row = workSheet.getRow(36);
        row.getCell(1).setCellValue(format.format(new Date()));
    }

    public void createServerToExcel(Workbook workbook, List<ServerResponse> servers, ServiceDetail service) throws Exception {
        Sheet workSheet = workbook.getSheet("2.Servers");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        CellStyle headerStyle = ExcelUtil.createHeaderCellStyle(workbook);

        Row row;
        int rowIndex = workSheet.getPhysicalNumberOfRows();
        if (rowIndex == 0) {
            row = workSheet.createRow(CommonConstants.EXCEL_HEADER_FIRST_ROW_INDEX);

            int headerColumnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("No");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("인벤토리 코드");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("인벤토리 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서비스 ID");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서비스 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서버 ID");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서버 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("IP 주소");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("접속 포트");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("운영체제(OS)");
            // ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("운영체제 버전");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("벤더");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("기종 및 모델");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("CPU");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("아키텍처");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("소켓당 코어 수");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("소켓 수");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("메모리");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("디스크 총 용량");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("사용 가능 디스크 용량");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("커널");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("시스템 가동 시간");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("사용자 수");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("그룹 수");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("사용 중 IP 주소(쉼표로 구분)");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Listen 포트(쉼표로 구분)");
            ExcelUtil.createCellHeader(row, headerColumnIndex, headerStyle).setCellValue("마지막 검사 날짜");

            rowIndex++;
        }

        int columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
        for (ServerResponse server : servers) {
            row = workSheet.createRow(rowIndex++);
            ServerSummary serverSummary = serverSummaryRepository.findByServerInventoryId(server.getServerInventoryId()).orElse(null);
            // 마지막으로 성공한 Inventory Process를 가져온다.
            InventoryProcess.CompleteScan completeScan = inventoryProcessMapper
                    .selectLastCompleteInventoryProcess(server.getServerInventoryId(), Domain1002.SCAN.name());
            io.playce.roro.jpa.entity.InventoryProcess inventoryProcess = null;
            InventoryProcessResult inventoryProcessResult = null;
            if (completeScan != null) {
                inventoryProcess = inventoryProcessRepository.findById(completeScan.getInventoryProcessId()).orElse(null);
                inventoryProcessResult = inventoryProcessResultRepository.findByInventoryProcessId(completeScan.getInventoryProcessId());
            }

            // https://cloud-osci.atlassian.net/browse/PCR-6486
            serverService.getVendorAndModel(server, inventoryProcessResult);

            // network 정보 조회
            List<ServerNetworkInformation> networkList = serverNetworkInformationRepository.findByServerInventoryId(server.getServerInventoryId());
            List<ServerNetworkInformation> networks = new ArrayList<>();
            for (ServerNetworkInformation net : networkList) {
                if (!"lo".equals(net.getInterfaceName()) && isValidIp4Address(net.getAddress())) {
                    networks.add(net);
                }
            }

            AssessmentResultDto.ServerProperty property = null;
            if (inventoryProcessResult != null) {
                property = (AssessmentResultDto.ServerProperty) new ServerParser(serverDiskInformationRepository).parse(server, inventoryProcessResult.getInventoryProcessResultJson());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(rowIndex - 1);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(server.getCustomerInventoryCode()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(server.getCustomerInventoryName()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(service.getServiceId());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(service.getServiceName()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(server.getServerInventoryId());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(server.getServerInventoryName()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(server.getRepresentativeIpAddress()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(server.getConnectionPort());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(serverSummary != null ? serverSummary.getOsName() : "");
                // ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getOsName()) : "");
                // ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getOsVersion()) : "");
                ExcelUtil.createCell(row, columnIndex++).setCellValue(server.getMakerName() != null ? server.getMakerName() : "");
                ExcelUtil.createCell(row, columnIndex++).setCellValue(server.getModelName() != null ? server.getModelName() : "");
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getCpu()) : "");
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getArchitecture()) : "");
                ExcelUtil.createCell(row, columnIndex++).setCellValue(serverSummary != null ? String.valueOf(serverSummary.getCpuCoreCount()) : "");
                ExcelUtil.createCell(row, columnIndex++).setCellValue(serverSummary != null ? String.valueOf(serverSummary.getCpuSocketCount()) : "");
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(property.getMemory()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(property.getTotalDisk()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(property.getFreeDisk()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(property.getKernel()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(property.getUpTime()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property.getUserCount());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property.getGroupCount());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(CollectionUtils.isNotEmpty(networks) ? networks.stream().map(ServerNetworkInformation::getAddress).collect(Collectors.joining(DELIMITER)) : "");
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property.getListenPort());
                ExcelUtil.createCell(row, columnIndex).setCellValue(inventoryProcess != null ? format.format(inventoryProcess.getInventoryProcessStartDatetime()) : "");

                columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
            } else {
                // Scan을 하지 않은 데이터를 만들어서 내려준다.
                serviceReportWithoutScanHelper.generateServerSheet(service, server, serverSummary, networks, row, rowIndex);
            }
        }
    }

    public void createWebServerToExcel(Workbook workbook, List<MiddlewareResponse> webServers, ServiceDetail service) throws Exception {
        Sheet workSheet = workbook.getSheet("3.Web Servers");
        CellStyle headerStyle = ExcelUtil.createHeaderCellStyle(workbook);

        Row row;
        int rowIndex = workSheet.getPhysicalNumberOfRows();
        if (rowIndex == 0) {
            row = workSheet.createRow(CommonConstants.EXCEL_HEADER_FIRST_ROW_INDEX);

            int headerColumnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("No");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("인벤토리 코드");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("인벤토리 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서비스 ID");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서비스 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서버 ID");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서버 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("미들웨어 ID");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("미들웨어 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("미들웨어 유형");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("벤더");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("엔진 버전");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("엔진 설치 경로");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("웹 서버 인스턴스 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("웹 서버 인스턴스 경로");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("런타임 계정");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Listen 포트");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("SSL 사용 여부");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Document Root");
            ExcelUtil.createCellHeader(row, headerColumnIndex, headerStyle).setCellValue("Config 파일(쉼표로 구분)");

            rowIndex++;
        }

        int columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
        for (MiddlewareResponse web : webServers) {
            // server and server summary 조회
            InventoryMaster serverInventoryMaster = inventoryMasterRepository.findById(web.getServerInventoryId()).orElse(null);
            if (serverInventoryMaster == null) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND);
            }

            // 마지막으로 성공한 Inventory Process를 가져온다.
            InventoryProcess.CompleteScan completeScan = inventoryProcessMapper
                    .selectLastCompleteInventoryProcess(web.getMiddlewareInventoryId(), Domain1002.SCAN.name());
            InventoryProcessResult inventoryProcessResult = null;
            if (completeScan != null) {
                inventoryProcessResult = inventoryProcessResultRepository.findByInventoryProcessId(completeScan.getInventoryProcessId());
            }

            // 인스턴스 정보를 가져온다.
            List<InstanceResponse> instances = middlewareMapper.selectMiddlewareInstanceList(web.getProjectId(), web.getMiddlewareInventoryId());
            for (InstanceResponse ins : instances) {
                row = workSheet.createRow(rowIndex++);

                AssessmentResultDto.WebProperty property = null;
                if (inventoryProcessResult != null) {
                    property = (AssessmentResultDto.WebProperty) new WebServerParser().parse(web, inventoryProcessResult.getInventoryProcessResultJson());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(rowIndex - 1);
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(web.getCustomerInventoryCode());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(web.getCustomerInventoryName());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(service.getServiceId());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(service.getServiceName()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster.getInventoryId());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster != null ? defaultStringValue(serverInventoryMaster.getInventoryName()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(web.getMiddlewareInventoryId());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(web.getMiddlewareInventoryName()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(web.getMiddlewareTypeCode()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(web.getVendorName()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getEngineVersion()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getEnginePath()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(ins.getMiddlewareInstanceName()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(ins.getMiddlewareInstancePath()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(ins.getRunningUser()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(String.valueOf(property != null ? property.getListenPort() != null ? property.getListenPort() : "" : ""));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null && property.isSslUsed());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getDocumentRoot() : "");
                    ExcelUtil.createCell(row, columnIndex).setCellValue(property != null ? StringUtils.defaultString(property.getIncludeFiles()) : "");

                    columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
                }
            }

            if (inventoryProcessResult == null && CollectionUtils.isEmpty(instances)) {
                row = workSheet.createRow(rowIndex++);
                // Scan을 하지 않은 데이터를 만들어서 내려준다.
                serviceReportWithoutScanHelper.generateWebServerSheet(service, serverInventoryMaster, web, row, rowIndex);
            }
        }
    }

    public void createWasServerToExcel(Workbook workbook, List<MiddlewareResponse> wasServers, ServiceDetail service) throws Exception {
        Sheet workSheet = workbook.getSheet("4.WAS Servers");
        CellStyle headerStyle = createHeaderCellStyle(workbook);

        Row row;
        int rowIndex = workSheet.getPhysicalNumberOfRows();
        if (rowIndex == 0) {
            row = workSheet.createRow(CommonConstants.EXCEL_HEADER_FIRST_ROW_INDEX);

            int headerColumnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("No");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("인벤토리 코드");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("인벤토리 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서비스 ID");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서비스 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서버 ID");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서버 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("미들웨어 ID");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("미들웨어 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("미들웨어 유형");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("벤더");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("엔진 버전");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("엔진 설치 경로");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("WAS 인스턴스 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("WAS 인스턴스 경로");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("도메인 홈");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("런타임 계정");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Java 버전");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("최소 힙(Heap)");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("최대 힙(Heap)");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("클러스터 사용 여부");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("배포된 데이터소스(쉼표로 구분)");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("배포된 애플리케이션(쉼표로 구분)");
            ExcelUtil.createCellHeader(row, headerColumnIndex, headerStyle).setCellValue("Config 파일(쉼표로 구분)");

            rowIndex++;
        }

        int columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
        for (MiddlewareResponse was : wasServers) {
            // server and server summary 조회
            InventoryMaster serverInventoryMaster = inventoryMasterRepository.findById(was.getServerInventoryId()).orElse(null);
            if (serverInventoryMaster == null) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND);
            }

            // 마지막으로 성공한 Inventory Process를 가져온다.
            InventoryProcess.CompleteScan completeScan = inventoryProcessMapper
                    .selectLastCompleteInventoryProcess(was.getMiddlewareInventoryId(), Domain1002.SCAN.name());
            InventoryProcessResult inventoryProcessResult = null;
            if (completeScan != null) {
                inventoryProcessResult = inventoryProcessResultRepository.findByInventoryProcessId(completeScan.getInventoryProcessId());
            }

            // 인스턴스 정보를 가져온다.
            List<InstanceResponse> instances = middlewareMapper.selectMiddlewareInstanceList(was.getProjectId(), was.getMiddlewareInventoryId());
            for (InstanceResponse ins : instances) {
                row = workSheet.createRow(rowIndex++);

                List<DeployApplicationList> applications = middlewareMapper.selectDeployApplicationList(was.getProjectId(), ins.getMiddlewareInstanceId());
                List<DeployDatasourceList> datasources = middlewareService.getDeployDatasourceList(was.getProjectId(), ins.getMiddlewareInstanceId());

                AssessmentResultDto.WasProperty property = null;
                if (inventoryProcessResult != null) {
                    property = (AssessmentResultDto.WasProperty) new WasServerParser(ins).parse(was, inventoryProcessResult.getInventoryProcessResultJson());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(rowIndex - 1);
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(was.getCustomerInventoryCode());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(was.getCustomerInventoryName());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(service.getServiceId());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(service.getServiceName()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster.getInventoryId());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster != null ? defaultStringValue(serverInventoryMaster.getInventoryName()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(was.getMiddlewareInventoryId());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(was.getMiddlewareInventoryName()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(was.getMiddlewareTypeCode()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(was.getVendorName()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getEngineVersion()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getEnginePath()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(ins.getMiddlewareInstanceName()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(ins.getMiddlewareInstancePath()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getDomainHome()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getRunUser()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getJavaVersion()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getMinHeap()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? defaultStringValue(property.getMaxHeap()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? String.valueOf(property.isClusterUsed()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(CollectionUtils.isNotEmpty(datasources) ? datasources.stream().map(DeployDatasourceList::getDatasourceName).collect(Collectors.joining(DELIMITER)) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(CollectionUtils.isNotEmpty(applications) ? applications.stream().map(DeployApplicationList::getApplicationName).collect(Collectors.joining(DELIMITER)) : "");
                    ExcelUtil.createCell(row, columnIndex).setCellValue(defaultStringValue(property != null ? property.getConfigFiles() : ""));

                    columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
                }
            }

            if (inventoryProcessResult == null && CollectionUtils.isEmpty(instances)) {
                row = workSheet.createRow(rowIndex++);
                // Scan을 하지 않은 데이터를 만들어서 내려준다.
                serviceReportWithoutScanHelper.generateWasServerSheet(service, serverInventoryMaster, was, row, rowIndex);
            }
        }
    }

    public void createDatabaseToExcel(Workbook workbook, List<DatabaseEngineListResponseDto> databases, ServiceDetail service) throws Exception {
        Sheet workSheet = workbook.getSheet("5.Databases");
        CellStyle headerStyle = createHeaderCellStyle(workbook);

        Row row;
        int rowIndex = workSheet.getPhysicalNumberOfRows();
        if (rowIndex == 0) {
            row = workSheet.createRow(CommonConstants.EXCEL_HEADER_FIRST_ROW_INDEX);

            int headerColumnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("No");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("인벤토리 코드");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("인벤토리 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서비스 ID");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서비스 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서버 ID");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서버 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("데이터베이스 ID");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("데이터베이스 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("벤더");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("접속 포트");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("JDBC 연결 URL");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("사용자 계정");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("데이터베이스 버전");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("데이터베이스 서비스 이름");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("테이블 수");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("뷰 수");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("인데스 수");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("함수 수");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("프로시저 수");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("트리거 수");
            ExcelUtil.createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("시퀀스 수");
            ExcelUtil.createCellHeader(row, headerColumnIndex, headerStyle).setCellValue("데이터베이스 링크 수");

            rowIndex++;
        }

        int columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
        for (DatabaseEngineListResponseDto data : databases) {
            // 데이터베이스 상세 조회
            DatabaseEngineResponseDto database = databaseService.getDatabaseEngine(data.getProjectId(), data.getDatabaseInventoryId());
            // server and server summary 조회
            InventoryMaster serverInventoryMaster = inventoryMasterRepository.findById(database.getServerInventoryId()).orElse(null);
            if (serverInventoryMaster == null) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND);
            }

            // 마지막으로 성공한 Inventory Process를 가져온다.
            InventoryProcess.CompleteScan completeScan = inventoryProcessMapper
                    .selectLastCompleteInventoryProcess(data.getDatabaseInventoryId(), Domain1002.SCAN.name());
            InventoryProcessResult inventoryProcessResult = null;
            if (completeScan != null) {
                inventoryProcessResult = inventoryProcessResultRepository.findByInventoryProcessId(completeScan.getInventoryProcessId());
            }

            // 인스턴스 정보를 가져온다.
            List<DatabaseInstanceListResponseDto> instances = databaseMapper.selectDatabaseInstanceList(data.getProjectId(), data.getDatabaseInventoryId());
            for (DatabaseInstanceListResponseDto ins : instances) {
                DatabaseInstance instance = databaseInstanceRepository.findByDatabaseInstanceId(ins.getDatabaseInstanceId());
                row = workSheet.createRow(rowIndex++);

                AssessmentResultDto.DatabaseProperty property = null;
                if (inventoryProcessResult != null) {
                    // 데이터베이스 서비스 네임 설정
                    database.setDatabaseServiceName(ins.getDatabaseServiceName());
                    property = (AssessmentResultDto.DatabaseProperty) new DatabaseParser().parse(database, inventoryProcessResult.getInventoryProcessResultJson());

                    ExcelUtil.createCell(row, columnIndex++).setCellValue(rowIndex - 1);
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(database.getCustomerInventoryCode());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(database.getCustomerInventoryName());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(service.getServiceId());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(service.getServiceName()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster.getInventoryId());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster != null ? defaultStringValue(serverInventoryMaster.getInventoryName()) : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(database.getDatabaseInventoryId());
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(database.getDatabaseInventoryName()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(database.getVendor()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(String.valueOf(database.getConnectionPort() != null ? database.getConnectionPort() : ""));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(database.getJdbcUrl()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(instance.getUserName()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getVersion() : "");
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(instance.getDatabaseServiceName()));
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getTableCount() : 0);
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getViewCount() : 0);
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getIndexCount() : 0);
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getFunctionCount() : 0);
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getProcedureCount() : 0);
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getTriggerCount() : 0);
                    ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getSequenceCount() : 0);
                    ExcelUtil.createCell(row, columnIndex).setCellValue(property != null ? property.getDbLinkCount() : 0);

                    columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
                }
            }

            if (inventoryProcessResult == null && CollectionUtils.isEmpty(instances)) {
                row = workSheet.createRow(rowIndex++);
                // Scan을 하지 않은 데이터를 만들어서 내려준다.
                serviceReportWithoutScanHelper.generateDatabaseSheet(service, serverInventoryMaster, database, row, rowIndex);
            }
        }
    }

    public void createApplicationToExcel(Workbook workbook, List<ApplicationResponse> applications, ServiceDetail service) throws Exception {
        Sheet workSheet = workbook.getSheet("6.Applications");
        CellStyle headerStyle = createHeaderCellStyle(workbook);

        Row row;
        int rowIndex = workSheet.getPhysicalNumberOfRows();
        if (rowIndex == 0) {
            row = workSheet.createRow(CommonConstants.EXCEL_HEADER_FIRST_ROW_INDEX);

            int headerColumnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("No");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("인벤토리 코드");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("인벤토리 이름");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서비스 ID");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서비스 이름");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서버 ID");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서버 이름");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("애플리케이션 ID");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("애플리케이션 이름");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("애플리케이션 파일 업로드 경로");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("애플리케이션 유형");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("배포 경로");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("애플리케이션 용량");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("데이터소스(쉼표로 구분)");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("CSS 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("HTML 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("XML 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("JSP 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("JS 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Java 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("클래스 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("빌드 파일 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Config 파일 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("라이브러리 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("서블릿 확장 클래스 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("EJB/JTA 사용 클래스 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("특정 IP 주소가 포함된 파일 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Lookup 패턴이 포함된 클래스 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("사용자 정의 패턴이 포함된 파일 수");
            createCellHeader(row, headerColumnIndex++, headerStyle).setCellValue("Deprecated API 사용 클래스 수");
            createCellHeader(row, headerColumnIndex, headerStyle).setCellValue("삭제된 API 사용 클래스 수");

            rowIndex++;
        }

        int columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
        for (ApplicationResponse app : applications) {
            row = workSheet.createRow(rowIndex++);
            InventoryMaster serverInventoryMaster = inventoryMasterRepository.findById(app.getServerInventoryId()).orElse(null);
            if (serverInventoryMaster == null) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_NOT_FOUND);
            }

            // 애플리케이션 데이터소스
            List<ApplicationDatasourceResponse> datasoureList = applicationService.getDatasources(app.getProjectId(), app.getApplicationInventoryId());

            // 마지막으로 성공한 Inventory Process를 가져온다.
            InventoryProcess.CompleteScan completeScan = inventoryProcessMapper
                    .selectLastCompleteInventoryProcess(app.getApplicationInventoryId(), Domain1002.SCAN.name());
            io.playce.roro.jpa.entity.InventoryProcess inventoryProcess = null;
            InventoryProcessResult inventoryProcessResult = null;
            if (completeScan != null) {
                inventoryProcessResult = inventoryProcessResultRepository.findByInventoryProcessId(completeScan.getInventoryProcessId());
            }

            AssessmentResultDto.ApplicationProperty property = null;
            if (inventoryProcessResult != null) {
                property = (AssessmentResultDto.ApplicationProperty) new ApplicationParser().parse(app, inventoryProcessResult.getInventoryProcessResultJson());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(rowIndex - 1);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(app.getCustomerInventoryCode());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(app.getCustomerInventoryName());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(service.getServiceId());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(service.getServiceName()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster.getInventoryId());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(serverInventoryMaster != null ? defaultStringValue(serverInventoryMaster.getInventoryName()) : "");
                ExcelUtil.createCell(row, columnIndex++).setCellValue(app.getApplicationInventoryId());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(app.getApplicationInventoryName()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(app.getUploadSourceFilePath()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(generateApplicationType(app.getInventoryDetailTypeCode())));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(defaultStringValue(app.getDeployPath()));
                ExcelUtil.createCell(row, columnIndex++).setCellValue(app.getApplicationSize());
                ExcelUtil.createCell(row, columnIndex++).setCellValue(CollectionUtils.isNotEmpty(datasoureList) ? datasoureList.stream().map(ApplicationDatasourceResponse::getDatasourceName).collect(Collectors.joining(DELIMITER)) : "");
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getCssCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getHtmlCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getXmlCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getJspCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getJsCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getJavaCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getClassCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getBuildFileCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getConfigFileCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getLibraryCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getServletCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getEjbJtaCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getSpecificIpIncludeCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getLookupPatternCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getCustomPatternCount() : 0);
                ExcelUtil.createCell(row, columnIndex++).setCellValue(property != null ? property.getDeprecatedApiClassCount() : 0);
                ExcelUtil.createCell(row, columnIndex).setCellValue(property != null ? property.getDeleteApiClassCount() : 0);

                columnIndex = CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
            } else {
                // Scan을 하지 않은 데이터를 만들어서 내려준다.
                serviceReportWithoutScanHelper.generateApplicationSheet(service, serverInventoryMaster, app, datasoureList, row, rowIndex);
            }
        }
    }

    /**
     * 애플리케이션 타입 리턴
     */
    private String generateApplicationType(String code) {
        String type = null;
        if (Domain1013.EAR.name().equals(code)) {
            type = "Java Enterprise Application";
        } else if (Domain1013.JAR.name().equals(code)) {
            type = "Java Application";
        } else if (Domain1013.WAR.name().equals(code)) {
            type = "Java Web Application";
        } else if (Domain1013.ETC.name().equals(code)) {
            type = "Etc";
        }

        return type;
    }

    /**
     * 값이 비어있거나 null 인 경우에 '빈값'을 리턴한다.
     */
    private String defaultStringValue(String value) {
        return StringUtils.defaultString(value, "");
    }

    private boolean isValidIp4Address(String ipAddress) {
        final InetAddressValidator validator = InetAddressValidator.getInstance();

        return validator.isValidInet4Address(ipAddress);
    }
}
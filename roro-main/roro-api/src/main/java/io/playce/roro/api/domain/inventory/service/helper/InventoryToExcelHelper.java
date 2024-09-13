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
package io.playce.roro.api.domain.inventory.service.helper;

import io.playce.roro.api.domain.inventory.service.*;
import io.playce.roro.common.code.*;
import io.playce.roro.common.dto.common.label.Label;
import io.playce.roro.common.dto.inventory.application.ApplicationResponse;
import io.playce.roro.common.dto.inventory.database.DatabaseEngineListResponseDto;
import io.playce.roro.common.dto.inventory.database.DatabaseEngineResponseDto;
import io.playce.roro.common.dto.inventory.middleware.MiddlewareResponse;
import io.playce.roro.common.dto.inventory.server.ServerDetailResponse;
import io.playce.roro.common.dto.inventory.server.ServerResponse;
import io.playce.roro.common.dto.inventory.service.Service;
import io.playce.roro.common.dto.inventory.service.ServiceMapping;
import io.playce.roro.common.dto.inventory.service.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.playce.roro.api.common.CommonConstants.EXCEL_COLUMN_FIRST_ROW_INDEX;
import static io.playce.roro.api.common.CommonConstants.EXCEL_PW_MSG;
import static io.playce.roro.api.common.util.ExcelUtil.createCell;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
public class InventoryToExcelHelper {

    private final ServiceService serviceService;
    private final ServerService serverService;
    private final MiddlewareService middlewareService;
    private final ApplicationService applicationService;
    private final DatabaseService databaseService;

    public void exportToExcel(Long projectId, XSSFWorkbook workbook) {
        List<ServiceMapping> serviceMapping = new ArrayList<>();

        exportServicesToExcel(projectId, workbook);
        exportServersToExcel(projectId, workbook, serviceMapping);
        exportMiddlewaresToExcel(projectId, workbook, serviceMapping);
        exportApplicationsToExcel(projectId, workbook, serviceMapping);
        exportDatabasesToExcel(projectId, workbook, serviceMapping);
        exportServiceMappingToExcel(workbook, serviceMapping);
    }

    private void exportServicesToExcel(Long projectId, XSSFWorkbook workbook) {
        List<ServiceResponse> serviceList = serviceService.getServiceList(projectId);

        Sheet workSheet = workbook.getSheet("service");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int rowIndex = 2;
        int columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;
        for (int i = serviceList.size(); i > 0; i--) {
            final ServiceResponse service = serviceList.get(i - 1);

            // Default Service는 제외
            if (service.getCustomerServiceCode().equals("SERV-001")) {
                continue;
            }

            Row row = workSheet.createRow(rowIndex++);

            // 서비스 ID
            createCell(row, columnIndex++).setCellValue(service.getServiceId());
            // 서비스 이름
            createCell(row, columnIndex++).setCellValue(service.getServiceName());
            // 서비스 코드
            createCell(row, columnIndex++).setCellValue(service.getCustomerServiceCode());
            // 업무 구분 코드
            createCell(row, columnIndex++).setCellValue(service.getBusinessCategoryCode());
            // 업무 구분
            createCell(row, columnIndex++).setCellValue(service.getBusinessCategoryName());
            // 레이블
            createCell(row, columnIndex++).setCellValue(CollectionUtils.isNotEmpty(service.getLabelList()) ?
                    service.getLabelList().stream().map(Label.LabelResponse::getLabelName).collect(Collectors.joining(",")) : "");
            // 설명
            createCell(row, columnIndex++).setCellValue(service.getDescription());
            // 마이그레이션 여부(Y/N)
            createCell(row, columnIndex++).setCellValue(service.getMigrationTargetYn());
            // 마이그레이션 투입 인력(Man-Month)
            if (service.getMigrationManMonth() != null) {
                createCell(row, columnIndex++).setCellValue(service.getMigrationManMonth());
            } else {
                createCell(row, columnIndex++).setCellValue("");
            }
            // 마이그레이션 환경 구성 일정(시작)
            createCell(row, columnIndex++).setCellValue(service.getMigrationEnvConfigStartDatetime() != null ? formatter.format(service.getMigrationEnvConfigStartDatetime()) : "");
            // 마이그레이션 환경 구성 일정(종료)
            createCell(row, columnIndex++).setCellValue(service.getMigrationEnvConfigEndDatetime() != null ? formatter.format(service.getMigrationEnvConfigEndDatetime()) : "");
            // 마이그레이션 애플리케이션 테스트 일정(시작)
            createCell(row, columnIndex++).setCellValue(service.getMigrationTestStartDatetime() != null ? formatter.format(service.getMigrationTestStartDatetime()) : "");
            // 마이그레이션 애플리케이션 테스트 일정(종료)
            createCell(row, columnIndex++).setCellValue(service.getMigrationTestEndDatetime() != null ? formatter.format(service.getMigrationTestEndDatetime()) : "");
            // 마이그레이션 컷오버 날짜
            createCell(row, columnIndex++).setCellValue(service.getMigrationCutOverDatetime() != null ? formatter.format(service.getMigrationCutOverDatetime()) : "");
            // 마이그레이션 심각도
            createCell(row, columnIndex++).setCellValue(service.getSeverity());

            columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;
        }
    }

    private void exportServersToExcel(Long projectId, XSSFWorkbook workbook, List<ServiceMapping> serviceMapping) {
        List<ServerResponse> serverList = serverService.getServers(projectId, null, false);

        Sheet workSheet = workbook.getSheet("server");
        SimpleDateFormat beforeFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat afterFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int rowIndex = 2;
        int columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;
        for (int i = serverList.size(); i > 0; i--) {
            final ServerResponse server = serverList.get(i - 1);

            Row row = workSheet.createRow(rowIndex++);

            // 인벤토리 ID
            createCell(row, columnIndex++).setCellValue(server.getServerInventoryId());
            // 서버 이름
            createCell(row, columnIndex++).setCellValue(server.getServerInventoryName());
            // 인벤토리 코드
            createCell(row, columnIndex++).setCellValue(server.getCustomerInventoryCode());
            // Windows 여부(Y/N)
            createCell(row, columnIndex++).setCellValue(server.getWindowsYn());
            // IP 주소
            createCell(row, columnIndex++).setCellValue(server.getRepresentativeIpAddress());
            // SSH/WinRM 포트
            createCell(row, columnIndex++).setCellValue(server.getConnectionPort());
            // 사용자 계정
            createCell(row, columnIndex++).setCellValue(server.getUserName());
            // 비밀번호
            if (StringUtils.isEmpty(server.getKeyFileName())) {
                createCell(row, columnIndex++).setCellValue(EXCEL_PW_MSG);
            } else {
                createCell(row, columnIndex++).setCellValue("");
            }
            // 프라이빗 키 파일 이름
            createCell(row, columnIndex++).setCellValue(server.getKeyFileName());
            // 프라이빗 키 파일 내용
            if (StringUtils.isNotEmpty(server.getKeyFileName())) {
                createCell(row, columnIndex++).setCellValue(EXCEL_PW_MSG);
            } else {
                createCell(row, columnIndex++).setCellValue("");
            }
            // su 명령어로 root 계정 전환(Y/N)
            createCell(row, columnIndex++).setCellValue(server.getEnableSuYn());
            // root 계정 비밀번호
            if ("Y".equalsIgnoreCase(server.getEnableSuYn())) {
                createCell(row, columnIndex++).setCellValue(EXCEL_PW_MSG);
            } else {
                createCell(row, columnIndex++).setCellValue("");
            }
            // 모니터링 여부(Y/N)
            createCell(row, columnIndex++).setCellValue(server.getMonitoringYn());
            // 모니터링 일정(크론 표현식)
            createCell(row, columnIndex++).setCellValue(server.getMonitoringCycle());
            // 모니터링 시작 날짜
            createCell(row, columnIndex++).setCellValue(server.getMonitoringStartDatetime() != null ? formatter.format(server.getMonitoringStartDatetime()) : "");
            // 모니터링 종료 날짜
            createCell(row, columnIndex++).setCellValue(server.getMonitoringEndDatetime() != null ? formatter.format(server.getMonitoringEndDatetime()) : "");
            // 마이그레이션 유형
            createCell(row, columnIndex++).setCellValue(server.getMigrationTypeCode() != null ?
                    Arrays.stream(Domain1107.values()).anyMatch(v -> v.name().equals(server.getMigrationTypeCode())) ? String.valueOf(Domain1107.valueOf(server.getMigrationTypeCode()).fullname()) : "" : "");
            // 서버 위치
            createCell(row, columnIndex++).setCellValue(server.getServerLocation());
            // 배포 환경
            createCell(row, columnIndex++).setCellValue(server.getServerUsageTypeCode() != null ?
                    Arrays.stream(Domain1110.values()).anyMatch(v -> v.name().equals(server.getServerUsageTypeCode())) ? String.valueOf(Domain1110.valueOf(server.getServerUsageTypeCode()).fullname()) : "" : "");
            // 하이퍼바이저 유형
            createCell(row, columnIndex++).setCellValue(server.getHypervisorTypeCode() != null ?
                    Arrays.stream(Domain1105.values()).anyMatch(v -> v.name().equals(server.getHypervisorTypeCode())) ? String.valueOf(Domain1105.valueOf(server.getHypervisorTypeCode()).fullname()) : "" : "");
            // 클러스터 유형
            createCell(row, columnIndex++).setCellValue(server.getDualizationTypeCode() != null ?
                    Arrays.stream(Domain1106.values()).anyMatch(v -> v.name().equals(server.getDualizationTypeCode())) ? String.valueOf(Domain1106.valueOf(server.getDualizationTypeCode()).fullname()) : "" : "");
            // 접근 제어 방식
            createCell(row, columnIndex++).setCellValue(server.getAccessControlSystemSolutionName());
            // tpmC
            if (server.getTpmc() != null) {
                createCell(row, columnIndex++).setCellValue(server.getTpmc());
            } else {
                createCell(row, columnIndex++).setCellValue("");
            }
            // 구매 날짜
            try {
                createCell(row, columnIndex++).setCellValue(server.getBuyDate() != null ? afterFormat.format(beforeFormat.parse(server.getBuyDate())) : "");
            } catch (ParseException e) {
                createCell(row, columnIndex++).setCellValue(server.getBuyDate());
            }
            // 벤더
            createCell(row, columnIndex++).setCellValue(server.getMakerName());
            // 기종 및 모델
            createCell(row, columnIndex++).setCellValue(server.getModelName());
            // 일련번호
            createCell(row, columnIndex++).setCellValue(server.getSerialNumber());
            // 레이블
            createCell(row, columnIndex++).setCellValue(CollectionUtils.isNotEmpty(server.getLabelList()) ?
                    server.getLabelList().stream().map(Label.LabelResponse::getLabelName).collect(Collectors.joining(",")) : "");
            // 설명
            createCell(row, columnIndex++).setCellValue(server.getDescription());

            columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;

            // service mapping 정보 add
            if (CollectionUtils.isNotEmpty(server.getServices())) {
                for (Service serv : server.getServices()) {
                    ServiceMapping sm = new ServiceMapping();
                    sm.setType("Server");
                    sm.setCustomerInventoryCode(server.getCustomerInventoryCode());
                    sm.setCustomerServiceCode(serv.getCustomerServiceCode());
                    serviceMapping.add(sm);
                }
            }
        }
    }

    private void exportMiddlewaresToExcel(Long projectId, XSSFWorkbook workbook, List<ServiceMapping> serviceMapping) {
        List<MiddlewareResponse> middlewareList = middlewareService.getMiddlewares(projectId, null, null);

        Sheet workSheet = workbook.getSheet("middleware");

        int rowIndex = 2;
        int columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;
        for (int i = middlewareList.size(); i > 0; i--) {
            final MiddlewareResponse middleware = middlewareList.get(i - 1);

            Row row = workSheet.createRow(rowIndex++);
            ServerDetailResponse server = serverService.getServer(projectId, middleware.getServerInventoryId());

            // 인벤토리 ID
            createCell(row, columnIndex++).setCellValue(middleware.getMiddlewareInventoryId());
            // 서버 인벤토리 코드
            createCell(row, columnIndex++).setCellValue(server.getCustomerInventoryCode());
            // 미들웨어 이름
            createCell(row, columnIndex++).setCellValue(middleware.getMiddlewareInventoryName());
            // 인벤토리 코드
            createCell(row, columnIndex++).setCellValue(middleware.getCustomerInventoryCode());
            // 전용 인증 사용 여부
            createCell(row, columnIndex++).setCellValue(middleware.getDedicatedAuthenticationYn());
            // Username
            createCell(row, columnIndex++).setCellValue(middleware.getUserName());
            // Password
            if (StringUtils.isNotEmpty(middleware.getUserName())) {
                createCell(row, columnIndex++).setCellValue(EXCEL_PW_MSG);
            } else {
                createCell(row, columnIndex++).setCellValue("");
            }
            // SSH 접속 용 개인 키 파일 명
            createCell(row, columnIndex++).setCellValue(middleware.getKeyFileName());
            // SSH 접속용 개인 키 파일 내용
            if (StringUtils.isNotEmpty(middleware.getKeyFileName())) {
                createCell(row, columnIndex++).setCellValue(EXCEL_PW_MSG);
            } else {
                createCell(row, columnIndex++).setCellValue("");
            }
            // 미들웨어 타입(WEB/WAS)
            createCell(row, columnIndex++).setCellValue(middleware.getMiddlewareTypeCode() != null ?
                    Arrays.stream(Domain1102.values()).anyMatch(v -> v.name().equals(middleware.getMiddlewareTypeCode())) ? String.valueOf(Domain1102.valueOf(middleware.getMiddlewareTypeCode()).fullname()) : "" : "");
            // 벤더
            createCell(row, columnIndex++).setCellValue(middleware.getVendorName());
            // 엔진(솔루션) 명
            createCell(row, columnIndex++).setCellValue(middleware.getInventoryDetailTypeCode());
            // 엔진 버전
            createCell(row, columnIndex++).setCellValue(middleware.getEngineVersion());
            // 엔진 설치 경로
            createCell(row, columnIndex++).setCellValue(middleware.getEngineInstallPath());
            // 도메인 홈
            createCell(row, columnIndex++).setCellValue(middleware.getDomainHomePath());
            // 레이블
            createCell(row, columnIndex++).setCellValue(CollectionUtils.isNotEmpty(middleware.getLabelList()) ?
                    middleware.getLabelList().stream().map(Label.LabelResponse::getLabelName).collect(Collectors.joining(",")) : "");
            // 설명
            createCell(row, columnIndex++).setCellValue(middleware.getDescription());

            columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;

            // service mapping 정보 add
            if (CollectionUtils.isNotEmpty(middleware.getServices())) {
                for (Service serv : middleware.getServices()) {
                    ServiceMapping sm = new ServiceMapping();
                    sm.setType("Middleware");
                    sm.setCustomerInventoryCode(middleware.getCustomerInventoryCode());
                    sm.setCustomerServiceCode(serv.getCustomerServiceCode());
                    serviceMapping.add(sm);
                }
            }
        }
    }

    private void exportApplicationsToExcel(Long projectId, XSSFWorkbook workbook, List<ServiceMapping> serviceMapping) {
        List<ApplicationResponse> applicationList = applicationService.getApplications(projectId, null, null);

        Sheet workSheet = workbook.getSheet("application");

        int rowIndex = 2;
        int columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;
        for (int i = applicationList.size(); i > 0; i--) {
            final ApplicationResponse application = applicationList.get(i - 1);

            Row row = workSheet.createRow(rowIndex++);
            ServerDetailResponse server = serverService.getServer(projectId, application.getServerInventoryId());

            // 인벤토리 ID
            createCell(row, columnIndex++).setCellValue(application.getApplicationInventoryId());
            // 서버 인벤토리 코드
            createCell(row, columnIndex++).setCellValue(server.getCustomerInventoryCode());
            // 애플리케이션 이름
            createCell(row, columnIndex++).setCellValue(application.getApplicationInventoryName());
            // 인벤토리 코드
            createCell(row, columnIndex++).setCellValue(application.getCustomerInventoryCode());
            // 전용 인증 사용 여부
            createCell(row, columnIndex++).setCellValue(application.getDedicatedAuthenticationYn());
            // Username
            createCell(row, columnIndex++).setCellValue(application.getUserName());
            // Password
            if (StringUtils.isNotEmpty(application.getUserName())) {
                createCell(row, columnIndex++).setCellValue(EXCEL_PW_MSG);
            } else {
                createCell(row, columnIndex++).setCellValue("");
            }
            // SSH 접속 용 개인 키 파일 명
            createCell(row, columnIndex++).setCellValue(application.getKeyFileName());
            // SSH 접속용 개인 키 파일 내용
            if (StringUtils.isNotEmpty(application.getKeyFileName())) {
                createCell(row, columnIndex++).setCellValue(EXCEL_PW_MSG);
            } else {
                createCell(row, columnIndex++).setCellValue("");
            }
            // 애플리케이션 타입(EAR/WAR/JAR)
            createCell(row, columnIndex++).setCellValue(application.getInventoryDetailTypeCode());
            // 배포경로
            createCell(row, columnIndex++).setCellValue(application.getDeployPath());
            // 레이블
            createCell(row, columnIndex++).setCellValue(CollectionUtils.isNotEmpty(application.getLabelList()) ?
                    application.getLabelList().stream().map(Label.LabelResponse::getLabelName).collect(Collectors.joining(",")) : "");
            // 설명
            createCell(row, columnIndex++).setCellValue(application.getDescription());
            // 소스 위치 URI
            createCell(row, columnIndex++).setCellValue(application.getSourceLocationUri());
            // 업로드 애플리케이션 파일 경로
            createCell(row, columnIndex++).setCellValue(application.getUploadSourceFilePath());
            // 분석 대상 라이브러리 이름
            createCell(row, columnIndex++).setCellValue(String.join(",", application.getAnalysisLibList()));
            // 분석 대상 문자열 값
            createCell(row, columnIndex++).setCellValue(String.join(",", application.getAnalysisStringList()));

            columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;

            // service mapping 정보 add
            if (CollectionUtils.isNotEmpty(application.getServiceList())) {
                for (Service serv : application.getServiceList()) {
                    ServiceMapping sm = new ServiceMapping();
                    sm.setType("Application");
                    sm.setCustomerInventoryCode(application.getCustomerInventoryCode());
                    sm.setCustomerServiceCode(serv.getCustomerServiceCode());
                    serviceMapping.add(sm);
                }
            }
        }
    }

    private void exportDatabasesToExcel(Long projectId, XSSFWorkbook workbook, List<ServiceMapping> serviceMapping) {
        List<DatabaseEngineListResponseDto> databaseList = databaseService.getDatabaseEngines(projectId, null, null);

        Sheet workSheet = workbook.getSheet("database");

        int rowIndex = 2;
        int columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;
        for (int i = databaseList.size(); i > 0; i--) {
            final DatabaseEngineListResponseDto database = databaseList.get(i - 1);

            Row row = workSheet.createRow(rowIndex++);
            ServerDetailResponse server = serverService.getServer(projectId, database.getServerInventoryId());
            DatabaseEngineResponseDto db = databaseService.getDatabaseEngine(projectId, database.getDatabaseInventoryId());

            // 인벤토리 ID
            createCell(row, columnIndex++).setCellValue(database.getDatabaseInventoryId());
            // 서버 인벤토리 코드
            createCell(row, columnIndex++).setCellValue(server.getCustomerInventoryCode());
            // 데이터베이스 이름
            createCell(row, columnIndex++).setCellValue(database.getDatabaseInventoryName());
            // 인벤토리 코드
            createCell(row, columnIndex++).setCellValue(db.getCustomerInventoryCode());
            // 벤더
            createCell(row, columnIndex++).setCellValue(db.getVendor());
            // 엔진(솔루션) 명
            createCell(row, columnIndex++).setCellValue(db.getInventoryDetailTypeCode());
            // 엔진 버전
            createCell(row, columnIndex++).setCellValue(db.getEngineVersion());
            // 접속 포트
            createCell(row, columnIndex++).setCellValue(db.getConnectionPort());
            // 데이터베이스 서비스 이름
            createCell(row, columnIndex++).setCellValue(db.getDatabaseServiceName());
            // jdbc url
            createCell(row, columnIndex++).setCellValue(db.getJdbcUrl());
            // 사용자 계정
            createCell(row, columnIndex++).setCellValue(db.getUserName());
            // 비밀번호
            createCell(row, columnIndex++).setCellValue(EXCEL_PW_MSG);
            // 접근 제어 솔루션
            createCell(row, columnIndex++).setCellValue(db.getDatabaseAccessControlSystemSolutionName());
            // 레이블
            createCell(row, columnIndex++).setCellValue(CollectionUtils.isNotEmpty(db.getLabelList()) ?
                    db.getLabelList().stream().map(Label.LabelResponse::getLabelName).collect(Collectors.joining(",")) : "");
            // 설명
            createCell(row, columnIndex++).setCellValue(db.getDescription());

            columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;

            // service mapping 정보 add
            if (CollectionUtils.isNotEmpty(database.getServices())) {
                for (Service serv : database.getServices()) {
                    ServiceMapping sm = new ServiceMapping();
                    sm.setType("Database");
                    sm.setCustomerInventoryCode(db.getCustomerInventoryCode());
                    sm.setCustomerServiceCode(serv.getCustomerServiceCode());
                    serviceMapping.add(sm);
                }
            }
        }
    }

    private void exportServiceMappingToExcel(XSSFWorkbook workbook, List<ServiceMapping> serviceMapping) {
        Sheet workSheet = workbook.getSheet("service-mapping");

        int rowIndex = 2;
        int columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;
        for (ServiceMapping sm : serviceMapping) {
            Row row = workSheet.createRow(rowIndex++);

            createCell(row, columnIndex++).setCellValue(sm.getType());
            createCell(row, columnIndex++).setCellValue(sm.getCustomerServiceCode());
            createCell(row, columnIndex++).setCellValue(sm.getCustomerInventoryCode());

            columnIndex = EXCEL_COLUMN_FIRST_ROW_INDEX;
        }
    }
}
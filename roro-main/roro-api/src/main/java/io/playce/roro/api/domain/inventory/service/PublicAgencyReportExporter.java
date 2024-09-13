/*
 * Copyright 2023 The playce-roro-v3 Project.
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
 * SangCheon Park   Feb 23, 2023		    First Draft.
 */
package io.playce.roro.api.domain.inventory.service;

import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.cloudreadiness.service.CloudReadinessExcelExporter;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.dto.publicagency.PublicAgencyReportDto;
import io.playce.roro.common.util.support.ExcelHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PublicAgencyReportExporter {

    private static final String SERVICE = "SERVICE";

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private final CloudReadinessExcelExporter excelExporter;

    public void writePublicAgencyReport(Long projectId, XSSFWorkbook workbook, PublicAgencyReportDto publicAgencyReportDto) {
        Map<String, CellStyle> styleMap = ExcelHelper.initCellStyle(workbook);

        writeServerStatusSheet(projectId, workbook, publicAgencyReportDto.getServerStatusList(), styleMap);
        writeStorageStatusSheet(projectId, workbook, publicAgencyReportDto.getStorageStatusList(), styleMap);
        writeSoftwareStatusSheet(projectId, workbook, publicAgencyReportDto.getSoftwareStatusList(), styleMap);
        writeApplicationStatusSheet(projectId, workbook, publicAgencyReportDto.getApplicationStatusList(), styleMap);
        writeDatabaseStatusSheet(projectId, workbook, publicAgencyReportDto.getDatabaseStatusList(), styleMap);
        writeBackupStatusSheet(projectId, workbook, publicAgencyReportDto.getBackupStatusList(), styleMap);
    }

    private void writeServerStatusSheet(Long projectId, XSSFWorkbook workbook, List<PublicAgencyReportDto.ServerStatus> serverStatusList, Map<String, CellStyle> styleMap) {
        // 1. 서버 현황 시트
        XSSFSheet sheet = workbook.getSheetAt(0);
        XSSFRow row;
        XSSFCell cell;
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink link;

        CellStyle numberStyle = null;
        CellStyle floatStyle = null;

        for (PublicAgencyReportDto.ServerStatus serverStatus : serverStatusList) {
            row = sheet.createRow(sheet.getLastRowNum() + 1);

            // (1)정보시스템ID
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getSystemId());

            // (2)상위기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (3)기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (4)정보시스템명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getSystemName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, SERVICE, serverStatus.getSystemId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (5)서버명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getServerName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, Domain1001.SVR.name(), serverStatus.getServerId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (6)호스트네임
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getHostname());

            // (7) 서버구성현황 - 서버형태
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getServerType());

            // (7) 서버구성현황 - 단독
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (7) 서버구성현황 - 공용
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (7) 서버구성현황 - 물리서버명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (7) 서버구성현황 - 제조사
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getManufacturer());

            // (7) 서버구성현황 - 모델명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getModel());

            // (7) 서버구성현황 - 수량
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (8) 공용시스템잔여(운영) 업무 정보
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (9) 서비스용도
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getServiceType());

            // (10) 이중화여부
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getHighAvailability());

            // (11) 망구분
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getNetworkType());

            // (12) OS종류
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getOsType());

            // (13) OS버전
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getOsVersion());

            // (14) 커널버전
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getKernel());

            // (15) CPU * Socket
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getCpuCores() + " * " + serverStatus.getCpuSockets());

            // (16) Memory (GB)
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, toGigaBytes(serverStatus.getMemorySize()));

            // (17) 로컬디스크 - 물리용량
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, toGigaBytes(serverStatus.getDiskSize()));

            if (numberStyle == null) {
                numberStyle = copyCellStyle(workbook, cell.getCellStyle());
                numberStyle.setDataFormat(styleMap.get("#,##0").getDataFormat());
            }
            cell.setCellStyle(numberStyle);

            // (17) 로컬디스크 - 수량
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getDiskCount());

            // (17) 로컬디스크 - 사용량
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, toGigaBytes(serverStatus.getDiskUsed()));
            cell.setCellStyle(numberStyle);

            // (18) 평균사용량 - CPU
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getCpuUsage());

            if (floatStyle == null) {
                floatStyle = copyCellStyle(workbook, cell.getCellStyle());
                floatStyle.setDataFormat(styleMap.get("#,##0.00").getDataFormat());
            }
            cell.setCellStyle(floatStyle);

            // (18) 평균사용량 - Memory
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getMemUsage());
            cell.setCellStyle(floatStyle);

            // (19) 백업사용
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getUseBackup());

            // (20) 재해복구 사용 여부
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (21) 도입연도
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, serverStatus.getPurchaseDate());

            // (22) 도입금액
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (23) 1년간 유지보수 금액
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (24) 비고(고객의견)
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
        }
    }

    private void writeStorageStatusSheet(Long projectId, XSSFWorkbook workbook, List<PublicAgencyReportDto.StorageStatus> storageStatusList, Map<String, CellStyle> styleMap) {
        // 2. 스토리지 현황 시트
        XSSFSheet sheet = workbook.getSheetAt(1);
        XSSFRow row;
        XSSFCell cell;
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink link;

        for (PublicAgencyReportDto.StorageStatus storageStatus : storageStatusList) {
            row = sheet.createRow(sheet.getLastRowNum() + 1);

            // (1)정보시스템ID
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, storageStatus.getSystemId());

            // (2)상위기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (3)기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (4)정보시스템명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, storageStatus.getSystemName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, SERVICE, storageStatus.getSystemId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (5)연결서버명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, storageStatus.getServerName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, Domain1001.SVR.name(), storageStatus.getServerId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (6)스토리지 제조사 및 모델명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            if (StringUtils.isNotEmpty(storageStatus.getManufacturer()) || StringUtils.isNotEmpty(storageStatus.getModel())) {
                setCellValue(cell, storageStatus.getManufacturer() + " " + storageStatus.getModel());
            }

            // (7)스토리지 디스크타입
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, storageStatus.getDiskType());

            // (8)연결방식
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, storageStatus.getConnectionType());

            // (9)스토리지 공유 사용여부 - 공유 사용 여부
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, storageStatus.getSharingYn());

            // (9)스토리지 공유 사용여부 - 공유 사용 업무명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (10)할당용량
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (11)사용량
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (12)1년 증가량
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (13)도입연도
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (14)도입금액
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (15)비고(고객의견)
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
        }
    }

    private void writeBackupStatusSheet(Long projectId, XSSFWorkbook workbook, List<PublicAgencyReportDto.BackupStatus> backupStatusList, Map<String, CellStyle> styleMap) {
        // 6. 백업 현황 시트
        XSSFSheet sheet = workbook.getSheetAt(5);
        XSSFRow row;
        XSSFCell cell;
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink link;

        for (PublicAgencyReportDto.BackupStatus backupStatus : backupStatusList) {
            row = sheet.createRow(sheet.getLastRowNum() + 1);

            // (1)정보시스템ID
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, backupStatus.getSystemId());

            // (2)상위기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (3)기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (4)정보시스템명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, backupStatus.getSystemName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, SERVICE, backupStatus.getSystemId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (5)연결서버명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, backupStatus.getServerName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, Domain1001.SVR.name(), backupStatus.getServerId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (6)백업장비 모델명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, backupStatus.getModel());

            // (7)실 백업용량 - OS
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (7)실 백업용량 - 파일
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (7)실 백업용량 - DB
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (8)백업정책
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (9)도입연도
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (10)도입금액
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (11)비고(고객의견)
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
        }
    }

    private void writeSoftwareStatusSheet(Long projectId, XSSFWorkbook workbook, List<PublicAgencyReportDto.SoftwareStatus> softwareStatusList, Map<String, CellStyle> styleMap) {
        // 3. SW 현황 시트
        XSSFSheet sheet = workbook.getSheetAt(2);
        XSSFRow row;
        XSSFCell cell;
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink link;

        for (PublicAgencyReportDto.SoftwareStatus softwareStatus : softwareStatusList) {
            row = sheet.createRow(sheet.getLastRowNum() + 1);

            // (1)정보시스템ID
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, softwareStatus.getSystemId());

            // (2)상위기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (3)기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (4)정보시스템명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, softwareStatus.getSystemName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, SERVICE, softwareStatus.getSystemId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (5)연결서버명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, softwareStatus.getServerName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, Domain1001.SVR.name(), softwareStatus.getServerId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (6)SW명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, softwareStatus.getSoftwareName());

            // (7)버전
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, softwareStatus.getVersion());

            // (8)SW구분
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            if (Boolean.TRUE.equals(softwareStatus.getIsOpenSource())) {
                setCellValue(cell, "Open source");
            } else if (Boolean.FALSE.equals(softwareStatus.getIsOpenSource())) {
                setCellValue(cell, "Commercial");
            }

            // (9)제공업체명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, softwareStatus.getVendor());

            // (10)용도
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, softwareStatus.getCategory());

            // (11)SW 적용부분
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (12)전환대상 이외 정보시스템과 공유 여부
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (13)라이선스 수량
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (14)라이선스 유형
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (15)라이선스 기간
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (16)라이선스 재활용 가능 수량
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (17)도입년도
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (18)도입금액
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (19)1년간 유지보수 금액
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (20)비고(고객의견)
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
        }
    }

    private void writeApplicationStatusSheet(Long projectId, XSSFWorkbook workbook, List<PublicAgencyReportDto.ApplicationStatus> applicationStatusList, Map<String, CellStyle> styleMap) {
        // 4. 애플리케이션 현황 시트
        XSSFSheet sheet = workbook.getSheetAt(3);
        XSSFRow row;
        XSSFCell cell;
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink link;

        CellStyle numberStyle = null;

        for (PublicAgencyReportDto.ApplicationStatus applicationStatus : applicationStatusList) {
            row = sheet.createRow(sheet.getLastRowNum() + 1);

            // (1)정보시스템ID
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, applicationStatus.getSystemId());

            // (2)상위기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (3)기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (4)정보시스템명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, applicationStatus.getSystemName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, SERVICE, applicationStatus.getSystemId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (5)연결서버명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, applicationStatus.getServerName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, Domain1001.SVR.name(), applicationStatus.getServerId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (6)애플리케이션명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, applicationStatus.getApplicationName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, Domain1001.APP.name(), applicationStatus.getApplicationId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (7)AP소스 보유
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (8)소스코드 형상관리 도구
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (9)개발언어
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, applicationStatus.getDevelopLanguage());

            // (10)개발언어버전
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, applicationStatus.getDevelopLanguageVersion());

            // (11)사용프레임워크명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, applicationStatus.getFrameworkName());

            // (12)사용프레임워크 버전
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, applicationStatus.getFrameworkVersion());

            // (13)https 사용여부
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, applicationStatus.getHttpsUseYn());

            // (14)AP 구조
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, applicationStatus.getApplicationType());

            // (15)AP 용량
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, toMegaBytes(applicationStatus.getApplicationSize()));

            if (numberStyle == null) {
                numberStyle = copyCellStyle(workbook, cell.getCellStyle());
                numberStyle.setDataFormat(styleMap.get("#,##0").getDataFormat());
            }
            cell.setCellStyle(numberStyle);

            // (16)사용 DBMS
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, applicationStatus.getUseDbms());

            // (17)AP 상세설명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (18)정보시스템 최초 개발년도
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (19)최초개발 FunctionPoint
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (20)정보시스템 최초 개발금액
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (21)정보시스템 재개발년도
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (22)재개발 FunctionPoint
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (23)정보시스템 재개발 금액
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (24)운영 시 별도 스크립트 사용여부
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (25)비고(고객의견)
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
        }
    }

    private void writeDatabaseStatusSheet(Long projectId, XSSFWorkbook workbook, List<PublicAgencyReportDto.DatabaseStatus> databaseStatusList, Map<String, CellStyle> styleMap) {
        // 5. DBMS 현황 시트
        XSSFSheet sheet = workbook.getSheetAt(4);
        XSSFRow row;
        XSSFCell cell;
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink link;

        CellStyle numberStyle = null;

        for (PublicAgencyReportDto.DatabaseStatus databaseStatus : databaseStatusList) {
            row = sheet.createRow(sheet.getLastRowNum() + 1);

            // (1)정보시스템ID
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, databaseStatus.getSystemId());

            // (2)상위기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (3)기관
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (4)정보시스템명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, databaseStatus.getSystemName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, SERVICE, databaseStatus.getSystemId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (5)연결서버명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, databaseStatus.getServerName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, Domain1001.SVR.name(), databaseStatus.getServerId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (6)Database명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, databaseStatus.getDatabaseName());
            link = createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(makeSourceLink(projectId, Domain1001.DBMS.name(), databaseStatus.getDatabaseId()));
            cell.setHyperlink(link);
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));

            // (7)DBMS 제품명
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, databaseStatus.getEngineName());

            // (8)버전
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, databaseStatus.getVersion());

            // (9)DBMS 용량
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            setCellValue(cell, databaseStatus.getDbSizeMb());

            if (numberStyle == null) {
                numberStyle = copyCellStyle(workbook, cell.getCellStyle());
                numberStyle.setDataFormat(styleMap.get("#,##0").getDataFormat());
            }
            cell.setCellStyle(numberStyle);

            // (10)DB암호화 방식
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (11)DB암호화제품 사용일 경우 데이터 암호화 방식
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (12)개인식별정보 보유
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (13)DB 단독 이용여부
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (14)통합 DB 사용 시 분리 가능여부
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);

            // (15)비고(고객의견)
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
        }
    }

    private CellStyle copyCellStyle(XSSFWorkbook workbook, CellStyle origin) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(origin.getAlignment());
        style.setVerticalAlignment(origin.getVerticalAlignment());
        style.setBorderTop(origin.getBorderTop());
        style.setBorderBottom(origin.getBorderBottom());
        style.setBorderLeft(origin.getBorderLeft());
        style.setBorderRight(origin.getBorderRight());
        style.setFillBackgroundColor(origin.getFillBackgroundColor());
        style.setFillForegroundColor(origin.getFillForegroundColor());
        style.setFillPattern(origin.getFillPattern());
        style.setWrapText(origin.getWrapText());
        style.setDataFormat(origin.getDataFormat());

        return style;
    }

    private void setCellValue(XSSFCell cell, Object value) {
        if (value != null) {
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value);
            } else if (value instanceof Integer) {
                cell.setCellValue((Integer) value);
            } else if (value instanceof Long) {
                cell.setCellValue((Long) value);
            } else if (value instanceof Float) {
                cell.setCellValue((Float) value);
            } else if (value instanceof Double) {
                cell.setCellValue((Double) value);
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
            } else {
                cell.setCellValue(value.toString());
            }
        }
    }

    private String makeSourceLink(Long projectId, String inventoryTypeCode, Long inventoryId) {
        if (SERVICE.equals(inventoryTypeCode)) {
            return WebUtil.getBaseUrl() + "/console/projects/" + projectId
                    + "/inventory/services/" + inventoryId + "/overview";
        } else {
            return WebUtil.getBaseUrl() + "/console/projects/" + projectId
                    + "/inventory/" + Domain1001.valueOf(inventoryTypeCode).fullname().toLowerCase() + "s"
                    + "/" + inventoryId + "/overview";
        }
    }

    private Long toGigaBytes(Long bytes) {
        if (bytes != null) {
            return DataSize.ofBytes(bytes).toGigabytes();
        }

        return null;
    }

    private Long toMegaBytes(Long bytes) {
        if (bytes != null) {
            return DataSize.ofBytes(bytes).toMegabytes();
        }

        return null;
    }
}
//end of PublicAgencyReportExporter.java
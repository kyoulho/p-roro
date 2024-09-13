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
 * SangCheon Park   Nov 03, 2022		    First Draft.
 */
package io.playce.roro.api.domain.common.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.domain.cloudreadiness.service.CloudReadinessExcelExporter;
import io.playce.roro.common.code.Domain1201;
import io.playce.roro.common.dto.common.thirdparty.ThirdPartySolutionResponse;
import io.playce.roro.common.util.support.ExcelHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class ThirdPartyExcelExporter {

    private final CloudReadinessExcelExporter excelExporter;

    public ThirdPartyExcelExporter(CloudReadinessExcelExporter excelExporter) {this.excelExporter = excelExporter;}

    public ByteArrayOutputStream createExcelReport(List<ThirdPartySolutionResponse> thirdPartySolutionList) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // -1 means turn off auto-flushing and accumulate all rows in memory
            SXSSFWorkbook workbook = new SXSSFWorkbook(-1);
            Map<String, CellStyle> styleMap = ExcelHelper.initCellStyle(workbook);

            SXSSFSheet sheet = workbook.createSheet("3rd Party Solutions");
            sheet.trackAllColumnsForAutoSizing();

            createHeader(workbook.getXSSFWorkbook(), workbook.getXSSFWorkbook().getSheetAt(workbook.getNumberOfSheets() - 1), styleMap);
            createDataRow(workbook.getXSSFWorkbook(), workbook.getXSSFWorkbook().getSheetAt(workbook.getNumberOfSheets() - 1), styleMap, thirdPartySolutionList);

            // Adjust Column Size
            excelExporter.autoSizeColumn(workbook.getXSSFWorkbook());

            workbook.getXSSFWorkbook().write(out);

            // workbook.dispose();

            return out;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create an 3rd party settings excel report.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }

    private void createHeader(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, CellStyle> styleMap) {
        XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        XSSFCell cell;

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서드 파티 솔루션 ID\n(Third-Party Solution ID)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서드 파티 솔루션 이름\n(Third-Party Solution Name)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("벤더\n(Vendor)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("설명\n(Description)");
        cell.getCellStyle().setWrapText(true);

        for (int i = 0; i < 7; i++) {
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
            cell.setCellValue("탐색 유형 (Discovery Type)");
        }

        excelExporter.mergeRegion(sheet, 0, 0, 4, 10);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("생성 날짜\n(Created Date)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("생성자\n(Created By)");
        cell.getCellStyle().setWrapText(true);

        row.setHeight((short) 600);

        row = sheet.createRow(sheet.getLastRowNum() + 1);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서드 파티 솔루션 ID\n(Third-Party Solution ID)");

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서드 파티 솔루션 이름\n(Third-Party Solution Name)");

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("벤더\n(Vendor)");

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("설명\n(Description)");

        excelExporter.mergeRegion(sheet, 0, 1, 0, 0);
        excelExporter.mergeRegion(sheet, 0, 1, 1, 1);
        excelExporter.mergeRegion(sheet, 0, 1, 2, 2);
        excelExporter.mergeRegion(sheet, 0, 1, 3, 3);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("프로세스\n(Process)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("프로세스 실행 사용자\n(Process Runtime User)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("패키지\n(Package)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("서비스\n(Service)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("명령어\n(Command)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("포트\n(Port)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("스케쥴\n(Schedule)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
        cell.setCellValue("생성 날짜\n(Created Date)");

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("생성자\n(Created By)");

        excelExporter.mergeRegion(sheet, 0, 1, 11, 11);
        excelExporter.mergeRegion(sheet, 0, 1, 12, 12);
    }

    private void createDataRow(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, CellStyle> styleMap, List<ThirdPartySolutionResponse> thirdPartySolutionList) {
        XSSFRow row;
        XSSFCell cell;

        for (ThirdPartySolutionResponse thirdPartySolution : thirdPartySolutionList) {
            List<ThirdPartySolutionResponse.ThirdPartySearchTypeResponse> thirdPartySearchTypes = thirdPartySolution.getThirdPartySearchTypes();

            row = sheet.createRow(sheet.getLastRowNum() + 1);

            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(thirdPartySolution.getThirdPartySolutionId());

            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(thirdPartySolution.getThirdPartySolutionName());

            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(thirdPartySolution.getVendor());

            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(thirdPartySolution.getDescription());

            List<ThirdPartySolutionResponse.ThirdPartySearchTypeDetail> detailList = thirdPartySearchTypes.stream().filter(t -> t.getSearchType().equals(Domain1201.PROCESS.name())).findFirst().map(t -> t.getValues()).orElse(new ArrayList<>());
            List<String> result = detailList.stream().filter(d -> StringUtils.isNotEmpty(d.getSearchValue())).map(d -> d.getSearchValue()).collect(Collectors.toList());
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(String.join("\n", result));

            detailList = thirdPartySearchTypes.stream().filter(t -> t.getSearchType().equals(Domain1201.RUNUSER.name())).findFirst().map(t -> t.getValues()).orElse(new ArrayList<>());
            result = detailList.stream().filter(d -> StringUtils.isNotEmpty(d.getSearchValue())).map(d -> d.getSearchValue()).collect(Collectors.toList());
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(String.join("\n", result));

            detailList = thirdPartySearchTypes.stream().filter(t -> t.getSearchType().equals(Domain1201.PKG.name())).findFirst().map(t -> t.getValues()).orElse(new ArrayList<>());
            result = detailList.stream().filter(d -> StringUtils.isNotEmpty(d.getSearchValue())).map(d -> d.getSearchValue()).collect(Collectors.toList());
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(String.join("\n", result));

            detailList = thirdPartySearchTypes.stream().filter(t -> t.getSearchType().equals(Domain1201.SVC.name())).findFirst().map(t -> t.getValues()).orElse(new ArrayList<>());
            result = detailList.stream().filter(d -> StringUtils.isNotEmpty(d.getSearchValue())).map(d -> d.getSearchValue()).collect(Collectors.toList());
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(String.join("\n", result));

            detailList = thirdPartySearchTypes.stream().filter(t -> t.getSearchType().equals(Domain1201.CMD.name())).findFirst().map(t -> t.getValues()).orElse(new ArrayList<>());
            result = detailList.stream().filter(d -> StringUtils.isNotEmpty(d.getSearchValue())).map(d -> d.getSearchValue()).collect(Collectors.toList());
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(String.join("\n", result));

            detailList = thirdPartySearchTypes.stream().filter(t -> t.getSearchType().equals(Domain1201.PORT.name())).findFirst().map(t -> t.getValues()).orElse(new ArrayList<>());
            result = detailList.stream().filter(d -> StringUtils.isNotEmpty(d.getSearchValue())).map(d -> d.getSearchValue()).collect(Collectors.toList());
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(String.join("\n", result));

            detailList = thirdPartySearchTypes.stream().filter(t -> t.getSearchType().equals(Domain1201.SCHEDULE.name())).findFirst().map(t -> t.getValues()).orElse(new ArrayList<>());
            result = detailList.stream().filter(d -> StringUtils.isNotEmpty(d.getSearchValue())).map(d -> d.getSearchValue()).collect(Collectors.toList());
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(String.join("\n", result));

            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String createdDatetime = formatter.format(thirdPartySolution.getRegistDatetime());
            cell.setCellValue(createdDatetime);

            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(thirdPartySolution.getRegistUserId());
        }
    }
}
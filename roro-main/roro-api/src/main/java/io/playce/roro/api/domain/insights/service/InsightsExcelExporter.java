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
 * SangCheon Park   Jan 17, 2023		    First Draft.
 */
package io.playce.roro.api.domain.insights.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.cloudreadiness.service.CloudReadinessExcelExporter;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.dto.insights.InsightDto;
import io.playce.roro.common.dto.insights.InsightListDto;
import io.playce.roro.common.dto.insights.Resource;
import io.playce.roro.common.util.support.ExcelHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
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
public class InsightsExcelExporter {

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private final CloudReadinessExcelExporter excelExporter;

    private static final String[] SHEET_NAMES = {"Operating Systems", "Middlewares", "Java", "Databases"};

    public ByteArrayOutputStream createExcelReport(Long projectId, Integer within, InsightListDto insights) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // -1 means turn off auto-flushing and accumulate all rows in memory
            SXSSFWorkbook workbook = new SXSSFWorkbook(-1);
            Map<String, CellStyle> styleMap = ExcelHelper.initCellStyle(workbook);

            int sheetIdx = 0;
            String sheetName;
            SXSSFSheet sheet;

            List<InsightDto> insightDtoList;
            for (int i = 0; i < SHEET_NAMES.length; i++) {
                sheetName = SHEET_NAMES[i];

                if (i == 0) {
                    insightDtoList = insights.getOperatingSystems();
                } else if (i == 1) {
                    insightDtoList = insights.getMiddlewares();
                } else if (i == 2) {
                    insightDtoList = insights.getJava();
                } else if (i == 3) {
                    insightDtoList = insights.getDatabases();
                } else {
                    insightDtoList = new ArrayList<>();
                }

                sheet = workbook.createSheet(sheetName);
                sheet.trackAllColumnsForAutoSizing();

                createHeader(workbook.getXSSFWorkbook(), workbook.getXSSFWorkbook().getSheetAt(sheetIdx), styleMap);
                createDataRow(projectId, within, workbook.getXSSFWorkbook(), workbook.getXSSFWorkbook().getSheetAt(sheetIdx++), styleMap, insightDtoList);
            }

            // Adjust Column Size
            excelExporter.autoSizeColumn(workbook.getXSSFWorkbook());

            workbook.getXSSFWorkbook().write(out);

            // workbook.dispose();

            return out;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create an insights excel report.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }

    private void createHeader(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, CellStyle> styleMap) {
        XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        XSSFCell cell;

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("솔루션 이름\n(Solution Name)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("벤더\n(Vendor)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("버전\n(Version)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("GA 날짜\n(GA Date)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("EOL 날짜\n(EOL Date)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("EOS 날짜\n(EOS Date)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("상태\n(Status)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("리소스 이름\n(Resource Name)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("리소스 유형\n(Resource Type)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("리소스 IP\n(Resource IP)");
        cell.getCellStyle().setWrapText(true);
    }

    private void createDataRow(Long projectId, Integer within, XSSFWorkbook workbook, XSSFSheet sheet, Map<String, CellStyle> styleMap, List<InsightDto> insightDtoList) {
        XSSFRow row;
        XSSFCell cell;
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink link;

        String withinStr;
        if (within == -1) {
            withinStr = " within Next 3+ Years";
        } else if (within == 365) {
            withinStr = " within Next 1 Year";
        } else if (within > 365) {
            int years = within / 365;
            withinStr = " within Next " + years + " Years";
        } else {
            withinStr = " within Next " + within + " Days";
        }

        for (InsightDto insights : insightDtoList) {
            for (Resource resource : insights.getResources()) {
                row = sheet.createRow(sheet.getLastRowNum() + 1);

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(insights.getSolutionName());

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(insights.getVendor());

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(insights.getVersion());

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(getFormattedDate(insights.getGaDatetime()));

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(getFormattedDate(insights.getEolDatetime()));

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(getFormattedDate(insights.getEosDatetime()));

                StringBuilder sb = new StringBuilder();
                if (insights.getEol().equalsIgnoreCase("ended")) {
                    sb.append("EOL Versions");
                } else if (insights.getEol().equalsIgnoreCase("tbe")) {
                    sb.append("EOL Versions").append(withinStr);
                }

                if (insights.getEos().equalsIgnoreCase("ended")) {
                    if (sb.length() > 0) {
                        sb.append(",").append("\n");
                    }

                    sb.append("EOS Versions");
                } else if (insights.getEos().equalsIgnoreCase("tbe")) {
                    if (sb.length() > 0) {
                        sb.append(",").append("\n");
                    }

                    sb.append("EOS Versions").append(withinStr);
                }
                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(sb.toString());

                link = createHelper.createHyperlink(HyperlinkType.URL);
                link.setAddress(makeSourceLink(projectId, resource.getResourceType(), resource.getInventoryId()));
                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(resource.getResourceName());
                cell.setHyperlink(link);
                cell.setCellStyle(styleMap.get("L_THIN_LINK"));

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(Domain1001.valueOf(resource.getResourceType()).fullname());

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(resource.getResourceIp());
            }
        }
    }

    private String makeSourceLink(Long projectId, String inventoryTypeCode, Long inventoryId) {
        return WebUtil.getBaseUrl() + "/console/projects/" + projectId
                + "/inventory/" + Domain1001.valueOf(inventoryTypeCode).fullname().toLowerCase() + "s"
                + "/" + inventoryId + "/overview";
    }

    private String getFormattedDate(Date date) {
        if (date != null) {
            return formatter.format(date);
        }

        return null;
    }
}
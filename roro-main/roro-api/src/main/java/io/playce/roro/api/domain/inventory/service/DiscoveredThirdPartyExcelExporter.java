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
 * SangCheon Park   Nov 02, 2022		    First Draft.
 */
package io.playce.roro.api.domain.inventory.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.cloudreadiness.service.CloudReadinessExcelExporter;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1201;
import io.playce.roro.common.dto.inventory.service.Service;
import io.playce.roro.common.dto.inventory.thirdparty.DiscoveredThirdPartyResponse;
import io.playce.roro.common.util.support.ExcelHelper;
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
public class DiscoveredThirdPartyExcelExporter {

    private final CloudReadinessExcelExporter excelExporter;

    public DiscoveredThirdPartyExcelExporter(CloudReadinessExcelExporter excelExporter) {
        this.excelExporter = excelExporter;
    }

    public ByteArrayOutputStream createExcelReport(List<DiscoveredThirdPartyResponse> discoveredThirdPartyResponses) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // -1 means turn off auto-flushing and accumulate all rows in memory
            SXSSFWorkbook workbook = new SXSSFWorkbook(-1);
            Map<String, CellStyle> styleMap = ExcelHelper.initCellStyle(workbook);
            List<Short> colorList = ExcelHelper.initTabColors();
            int colorIdx = 0;

            SXSSFSheet sheet = workbook.createSheet("Index");
            sheet.trackAllColumnsForAutoSizing();
            sheet.setTabColor(colorList.get((colorIdx++ % colorList.size())));

            for (DiscoveredThirdPartyResponse response : discoveredThirdPartyResponses) {
                sheet = workbook.createSheet(response.getThirdPartySolutionName());
                sheet.trackAllColumnsForAutoSizing();
                sheet.setTabColor(colorList.get((colorIdx++ % colorList.size())));

                createHeader(workbook.getXSSFWorkbook(), workbook.getXSSFWorkbook().getSheetAt(workbook.getNumberOfSheets() - 1), styleMap);
                createDataRow(workbook.getXSSFWorkbook(), workbook.getXSSFWorkbook().getSheetAt(workbook.getNumberOfSheets() - 1), styleMap, response);
            }

            indexing(workbook.getXSSFWorkbook(), styleMap);

            // Adjust Column Size
            excelExporter.autoSizeColumn(workbook.getXSSFWorkbook());

            workbook.getXSSFWorkbook().write(out);

            // workbook.dispose();

            return out;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create an discovered 3rd party excel report.", e);
            throw new RoRoApiException(ErrorCode.EXCEL_CREATE_FAILED, e.getMessage());
        }
    }

    private void createHeader(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, CellStyle> styleMap) {
        XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        XSSFCell cell;

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서버 이름\n(Server Name)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서비스 이름\n(Service Name)");
        cell.getCellStyle().setWrapText(true);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("IP 주소\n(IP Address)");
        cell.getCellStyle().setWrapText(true);

        for (int i = 0; i < 7; i++) {
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, null, IndexedColors.LIGHT_TURQUOISE);
            cell.setCellValue("탐색 유형 (Discovery Type)");
        }

        excelExporter.mergeRegion(sheet, 0, 0, 3, 9);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("탐색된 날짜\n(Discovered Date)");
        cell.getCellStyle().setWrapText(true);

        row.setHeight((short) 600);

        row = sheet.createRow(sheet.getLastRowNum() + 1);

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서버 이름\n(Server Name)");

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("서비스 이름\n(Service Name)");

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("IP 주소\n(IP Address)");

        excelExporter.mergeRegion(sheet, 0, 1, 0, 0);
        excelExporter.mergeRegion(sheet, 0, 1, 1, 1);
        excelExporter.mergeRegion(sheet, 0, 1, 2, 2);

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
        cell.setCellValue("탐색된 날짜\n(Discovered Date)");

        excelExporter.mergeRegion(sheet, 0, 1, 10, 10);
    }

    private void createDataRow(XSSFWorkbook workbook, XSSFSheet sheet, Map<String, CellStyle> styleMap, DiscoveredThirdPartyResponse response) {
        XSSFRow row;
        XSSFCell cell;
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink link;

        int startRow, currentRow;
        for (DiscoveredThirdPartyResponse.DiscoveredThirdPartyInventory inventory : response.getDiscoveredThirdPartyInventories()) {
            List<DiscoveredThirdPartyResponse.DiscoveryType> discoveryTypeList = inventory.getDiscoveryTypes();

            startRow = sheet.getLastRowNum() + 1;
            for (Service service : inventory.getServices()) {
                row = sheet.createRow(sheet.getLastRowNum() + 1);

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(inventory.getServerName());
                link = createHelper.createHyperlink(HyperlinkType.URL);
                link.setAddress(makeServerLink(inventory.getProjectId(), inventory.getServerInventoryId()));
                cell.setHyperlink(link);
                cell.setCellStyle(styleMap.get("L_THIN_LINK"));

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(service.getServiceName());
                link = createHelper.createHyperlink(HyperlinkType.URL);
                link.setAddress(makeServiceLink(inventory.getProjectId(), service.getServiceId()));
                cell.setHyperlink(link);
                cell.setCellStyle(styleMap.get("L_THIN_LINK"));

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(inventory.getServerIp());

                List<List<String>> contentList = discoveryTypeList.stream().filter(d -> d.getSearchType().equals(Domain1201.PROCESS.name())).map(d -> d.getFindContents()).collect(Collectors.toList());
                List<String> result = contentList.stream().flatMap(List::stream).distinct().collect(Collectors.toList());
                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(String.join("\n", result));

                contentList = discoveryTypeList.stream().filter(d -> d.getSearchType().equals(Domain1201.RUNUSER.name())).map(d -> d.getFindContents()).collect(Collectors.toList());
                result = contentList.stream().flatMap(List::stream).distinct().collect(Collectors.toList());
                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(String.join("\n", result));

                contentList = discoveryTypeList.stream().filter(d -> d.getSearchType().equals(Domain1201.PKG.name())).map(d -> d.getFindContents()).collect(Collectors.toList());
                result = contentList.stream().flatMap(List::stream).distinct().collect(Collectors.toList());
                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(String.join("\n", result));

                contentList = discoveryTypeList.stream().filter(d -> d.getSearchType().equals(Domain1201.SVC.name())).map(d -> d.getFindContents()).collect(Collectors.toList());
                result = contentList.stream().flatMap(List::stream).distinct().collect(Collectors.toList());
                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(String.join("\n", result));

                contentList = discoveryTypeList.stream().filter(d -> d.getSearchType().equals(Domain1201.CMD.name())).map(d -> d.getFindContents()).collect(Collectors.toList());
                result = contentList.stream().flatMap(List::stream).distinct().collect(Collectors.toList());
                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(String.join("\n", result));

                contentList = discoveryTypeList.stream().filter(d -> d.getSearchType().equals(Domain1201.PORT.name())).map(d -> d.getFindContents()).collect(Collectors.toList());
                result = contentList.stream().flatMap(List::stream).distinct().collect(Collectors.toList());
                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(String.join("\n", result));

                contentList = discoveryTypeList.stream().filter(d -> d.getSearchType().equals(Domain1201.SCHEDULE.name())).map(d -> d.getFindContents()).collect(Collectors.toList());
                result = contentList.stream().flatMap(List::stream).distinct().collect(Collectors.toList());
                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(String.join("\n", result));

                cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String discoveredDatetime = formatter.format(inventory.getDiscoveredDatetime());
                cell.setCellValue(discoveredDatetime);
            }

            currentRow = sheet.getLastRowNum();

            if (startRow != currentRow) {
                excelExporter.mergeRegion(sheet, startRow, currentRow, 0, 0);

                for (int i = 2; i <= 10; i++) {
                    excelExporter.mergeRegion(sheet, startRow, currentRow, i, i);
                }
            }
        }
    }

    private void indexing(XSSFWorkbook workbook, Map<String, CellStyle> styleMap) {
        CreationHelper createHelper = workbook.getCreationHelper();

        //cell style for hyperlinks
        //by default hyperlinks are blue and underlined
        Font linkFont = workbook.createFont();
        linkFont.setUnderline(Font.U_SINGLE);
        linkFont.setColor(IndexedColors.BLUE.getIndex());

        XSSFSheet contentsSheet = workbook.getSheetAt(0);
        XSSFRow row = contentsSheet.createRow(contentsSheet.getLastRowNum() + 1);

        XSSFCell cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("No");

        cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("3rd Party Solution Name");

        XSSFSheet dataSheet;
        Hyperlink link;
        for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
            dataSheet = workbook.getSheetAt(i);

            link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
            link.setAddress("'" + dataSheet.getSheetName() + "'!A1");

            row = contentsSheet.createRow(contentsSheet.getLastRowNum() + 1);
            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(i);

            cell = excelExporter.createCellWithBorder(workbook, row, null, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setHyperlink(link);
            cell.setCellValue(dataSheet.getSheetName());
            cell.setCellStyle(styleMap.get("L_THIN_LINK"));
        }
    }

    private String makeSourceLink(Long projectId, String inventoryTypeCode, Long inventoryId) {
        return WebUtil.getBaseUrl() + "/console/projects/" + projectId
                + "/inventory/" + Domain1001.valueOf(inventoryTypeCode).fullname().toLowerCase() + "s"
                + "/" + inventoryId + "/overview";
    }

    private String makeServerLink(Long projectId, Long serverInventoryId) {
        return WebUtil.getBaseUrl() + "/console/projects/" + projectId
                + "/inventory/" + Domain1001.SVR.fullname().toLowerCase() + "s"
                + "/" + serverInventoryId + "/overview";
    }

    private String makeServiceLink(Long projectId, Long serviceId) {
        return WebUtil.getBaseUrl() + "/console/projects/" + projectId
                + "/inventory/services/" + serviceId + "/overview";
    }
}
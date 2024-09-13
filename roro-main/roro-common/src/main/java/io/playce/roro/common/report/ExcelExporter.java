/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * SangCheon Park   Apr 02, 2021		First Draft.
 */
package io.playce.roro.common.report;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.common.dto.inventory.application.ApplicationDetailResponse;
import io.playce.roro.common.dto.inventory.database.DatabaseEngineResponseDto;
import io.playce.roro.common.dto.inventory.middleware.MiddlewareDetailResponse;
import io.playce.roro.common.dto.inventory.server.ServerDetailResponse;
import io.playce.roro.common.dto.inventory.service.Service;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.common.util.support.ExcelHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.*;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.poi.util.Units.PIXEL_DPI;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 2.0.0
 */
@Slf4j
public class ExcelExporter {

    private static final String[] EXCLUDE_NODE_NAMES = new String[]{"err_msg", "def_info", "defInfo", "errorMap"};

    private SheetGenerator sheetGenerator;
    private SXSSFWorkbook workbook;
    private SXSSFRow row;
    private SXSSFCell cell;

    private Map<String, CellStyle> styleMap;
    private Map<String, Font> fontMap;
    private List<Short> colorList;
    private int colorIdx = 0;

    private String type = "";
    private String title = "";
    // Middleware solution name (weblogic, tomcat, jeus, websphere and etc.) or type
    private String solution;
    // for Jeus
    private String version;

    public static void main(String[] args) throws IOException, XmlException, InterruptedException {
        ExcelExporter excelExporter = new ExcelExporter();
        String jsonStr = FileUtils.readFileToString(new File("/tmp/result.json"), "UTF-8");
        excelExporter.export("/tmp/result.xlsx", null, JsonUtil.readTree(jsonStr));
    }

    /**
     * Export.
     *
     * @param filePath the file path
     * @param entity   the DB entity
     * @param root     the root
     */
    public void export(String filePath, Object entity, JsonNode root) throws InterruptedException {
        /**
         * 1. Workbook 생성
         *    - Cover Sheet 생성
         *    - General Sheet 생성 (DB의 정보도 함께 들어가야 함)
         * 2. root node의 fieldNames의 사이즈에 따라 분기
         *    - 2개인 경우는 미들웨어, 데이터베이스
         *    - 미들웨어의 solution은 General 정보에 포함
         *    - 데이터베이스의 UserInformation, SystemInformation은 별도로 처리하지 않음
         */
        // -1 means turn off auto-flushing and accumulate all rows in memory
        workbook = new SXSSFWorkbook(-1);
        styleMap = ExcelHelper.initCellStyle(workbook);
        fontMap = ExcelHelper.initFont(workbook);
        colorList = ExcelHelper.initTabColors();

        sheetGenerator = new SheetGenerator(workbook, styleMap, fontMap);

        SXSSFSheet coverSheet = workbook.createSheet("Cover");
        coverSheet.trackAllColumnsForAutoSizing();
        coverSheet.setTabColor(colorList.get((colorIdx++ % colorList.size())));

        SXSSFSheet contentsSheet = workbook.createSheet("Table of Contents");
        contentsSheet.setTabColor(colorList.get((colorIdx++ % colorList.size())));

        SXSSFSheet generalSheet = workbook.createSheet("General");
        generalSheet.trackAllColumnsForAutoSizing();
        generalSheet.setTabColor(colorList.get((colorIdx++ % colorList.size())));

        JsonNode node;
        Iterator<String> fieldNames;
        //XSSFSheet dataSheet;
        if (root.size() <= 3 && root.has("engine") && root.has("instance")) {
            //if (root.has("engine") && root.has("instance")) {
            // Middleware assessment result
            makeGeneral(entity, generalSheet);

            node = root.get("engine");
            if (node != null) {
                addToGeneral(generalSheet, node, "engine");
            }

            node = root.get("instance");
            if (node != null) {
                fieldNames = node.fieldNames();

                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    parseNode(generalSheet, node.get(fieldName), fieldName);
                }
            }

            node = root.get("thirdPartySolutions");
            if (node != null) {
                parseNode(generalSheet, node, "thirdPartySolutions");
            }
            //}
            /*
            else if (root.has("userInformation") || root.has("systemInformation")) {
                // Database assessment result
                solution = "database";
                makeGeneral(entity, generalSheet);

                node = root.get("userInformation");
                if (node != null) {
                    fieldNames = node.fieldNames();

                    while (fieldNames.hasNext()) {
                        String fieldName = fieldNames.next();
                        parseNode(generalSheet, node.get(fieldName), fieldName);
                    }
                }

                node = root.get("systemInformation");
                if (node != null) {
                    fieldNames = node.fieldNames();

                    while (fieldNames.hasNext()) {
                        String fieldName = fieldNames.next();
                        parseNode(generalSheet, node.get(fieldName), fieldName);
                    }
                }
            }
            */
        } else {
            if (root.has("instance") && root.has("databases")) {
                solution = "database";
            }
            if (root.has("systemInformation") && root.has("environment")) {
                solution = "windows";
            }

            // Server, Application and Database assessment result
            makeGeneral(entity, generalSheet);

            fieldNames = root.fieldNames();

            Map<String, Object> users = null;
            Map<String, Object> groups = null;
            if (!"database".equals(solution) && !"windows".equals(solution)) {
                Map<String, Object> result = JsonUtil.convertValue(root, new TypeReference<Map<String, Object>>() {});

                if (result != null) {
                    users = (Map<String, Object>) result.get("users");
                    groups = (Map<String, Object>) result.get("groups");
                }
            }

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();

                if ("databases".equals(fieldName)) {
                    JsonNode databasesNode = root.get("databases");
                    Iterator<JsonNode> databaseNodes = databasesNode.elements();

                    while (databaseNodes.hasNext()) {
                        JsonNode databaseNode = databaseNodes.next();
                        parseNode(generalSheet, databaseNode, "(DB) " + databaseNode.get("name").asText());
                    }
                } else {
                    SXSSFSheet dataSheet;
                    if (fieldName.equals("users") && users != null) {
                        dataSheet = workbook.createSheet(ExcelHelper.capitalize(fieldName));
                        dataSheet.trackAllColumnsForAutoSizing();
                        dataSheet.setTabColor(colorList.get((colorIdx++ % colorList.size())));

                        // Server - users
                        sheetGenerator.makeUsersSheet(dataSheet, users, groups);
                    } else if (fieldName.equals("groups") && groups != null) {
                        dataSheet = workbook.createSheet(ExcelHelper.capitalize(fieldName));
                        dataSheet.trackAllColumnsForAutoSizing();
                        dataSheet.setTabColor(colorList.get((colorIdx++ % colorList.size())));

                        // Server - groups
                        sheetGenerator.makeGroupsSheet(dataSheet, users, groups);
                    } else {
                        parseNode(generalSheet, root.get(fieldName), fieldName);
                    }
                }
            }
        }

        // 커버 시트 생성
        makeCover(coverSheet, type, title);

        // 목차 & 하이퍼링크 생성
        ExcelHelper.createContents(workbook, styleMap);

        // Column Size 조정 & "Cell Reference" 시트 마지막으로 이동
        ExcelHelper.autoSizeColumn(workbook);

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            log.debug("Assessment result will be saved to [{}]", filePath);
            workbook.write(outputStream);
            // workbook.dispose();
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception while create an assessment report.", e);
        }
    }

    /**
     * Parse node.
     *
     * @param generalSheet the general sheet
     * @param node         the node
     * @param nodeName     the node name
     */
    private void parseNode(SXSSFSheet generalSheet, JsonNode node, String nodeName) throws InterruptedException {
        if (!node.isNull() && !ArrayUtils.contains(EXCLUDE_NODE_NAMES, nodeName)) {
            log.debug(String.format("nodeName : %-20s, size : %3d, isNull : %5s, isEmpty : %5s, isArray : %5s, isObject : %5s",
                    nodeName, node.size(), node.isNull(), node.isEmpty(), node.isArray(), node.isObject()));

            if (node.size() == 0 || nodeName.equals("general")) {
                //if (node.isEmpty() && !node.isArray() && !node.isObject()) {
                if (!node.isArray()) {
                    addToGeneral(generalSheet, node, nodeName);
                }
            } else {
                SXSSFSheet dataSheet = workbook.createSheet(ExcelHelper.capitalize(nodeName));
                dataSheet.trackAllColumnsForAutoSizing();
                dataSheet.setTabColor(colorList.get((colorIdx++ % colorList.size())));

                if (nodeName.equals("partitions")) {
                    // Server - partitions
                    sheetGenerator.makePartitionsSheet(dataSheet, node);
                } else if (nodeName.equals("processes")) {
                    // Server - processes
                    sheetGenerator.makeProcessesSheet(dataSheet, node);
                } else if (nodeName.equals("interfaces")) {
                    // Server - interfaces
                    sheetGenerator.makeInterfacesSheet(dataSheet, node);
                } else if (nodeName.equals("checkList")) {
                    // Application - checkList
                    sheetGenerator.makeCheckListSheet(dataSheet, node);
                } else if (nodeName.equals("dataSourceList")) {
                    // Application - dataSourceList
                    sheetGenerator.makeDataSourceListSheet(dataSheet, node);
                } else if (nodeName.equals("deprecatedList")) {
                    // Application - deprecatedList
                    sheetGenerator.makeDeprecatedListSheet(dataSheet, node);
                } else if ("websphere".equals(solution) && nodeName.equals("servers")) {
                    // WebSphere - servers
                    sheetGenerator.makeWebSphereServersSheet(dataSheet, node);
                } else if ("websphere".equals(solution) && nodeName.equals("clusters")) {
                    // WebSphere - clusters
                    sheetGenerator.makeWebSphereClustersSheet(dataSheet, node);
                } else if ("websphere".equals(solution) && nodeName.equals("ports")) {
                    // WebSphere - ports
                    sheetGenerator.makePortsSheet(dataSheet, node);
                } else if ("weblogic".equals(solution) && nodeName.equals("instances")) {
                    // WebLogic - instances
                    sheetGenerator.makeInstancesSheet(dataSheet, node);
                } else if ("weblogic".equals(solution) && nodeName.equals("resource")) {
                    // WebLogic - resource
                    sheetGenerator.makeResourceSheet(dataSheet, node);
                } else if ("jeus".equals(solution) && nodeName.equals("instances")) {
                    // Jeus - servers
                    if (version == null || Float.parseFloat(version) > 6.0) {
                        sheetGenerator.makeJeus7ServersSheet(dataSheet, node);
                    } else {
                        sheetGenerator.makeJeus6ServersSheet(dataSheet, node);
                    }
                } else if ("jeus".equals(solution) && nodeName.equals("applications")) {
                    // Jeus - applications
                    sheetGenerator.makeJeusApplicationssSheet(dataSheet, node);
                } else if ("jeus".equals(solution) && nodeName.equals("clusters")) {
                    // Jeus - clusters
                    sheetGenerator.makeClustersSheet(dataSheet, node);
                } else if ("jeus".equals(solution) && nodeName.equals("sessionClusterConfig")) {
                    // Jeus - sessionClusterConfig
                    sheetGenerator.makeSessionClusterConfigSheet(dataSheet, node);
                } else if ("jeus".equals(solution) && nodeName.equals("resources")) {
                    // Jeus - resources
                    sheetGenerator.makeResourcesSheet(dataSheet, node);
                } else if ("tomcat".equals(solution) && nodeName.equals("applications")) {
                    // Tomcat - applications
                    sheetGenerator.makeApplicationsSheet(dataSheet, node);
                } else if ("jboss".equals(solution) && nodeName.equals("instances")) {
                    // Tomcat - applications
                    sheetGenerator.makeJbossInstancessSheet(dataSheet, node);
                } else if ("nginx".equals(solution) && nodeName.equals("http")) {
                    // Nginx - Log, Http Servers, Http Upstreams
                    workbook.setSheetName(workbook.getSheetIndex(dataSheet), "Log");
                    sheetGenerator.makeNginxLogSheet(dataSheet, node);

                    dataSheet = workbook.createSheet("Http Servers");
                    dataSheet.trackAllColumnsForAutoSizing();
                    dataSheet.setTabColor(colorList.get((colorIdx++ % colorList.size())));
                    sheetGenerator.makeNginxServersSheet(dataSheet, node);

                    dataSheet = workbook.createSheet("Http Upstreams");
                    dataSheet.trackAllColumnsForAutoSizing();
                    dataSheet.setTabColor(colorList.get((colorIdx++ % colorList.size())));
                    sheetGenerator.makeNginxUpstreamsSheet(dataSheet, node);
                } else if ("nginx".equals(solution) && nodeName.equals("stream")) {
                    // Nginx - Stream Servers, Stream Upstreams
                    workbook.setSheetName(workbook.getSheetIndex(dataSheet), "Stream Servers");
                    sheetGenerator.makeNginxServersSheet(dataSheet, node);

                    dataSheet = workbook.createSheet("Stream Upstreams");
                    dataSheet.trackAllColumnsForAutoSizing();
                    dataSheet.setTabColor(colorList.get((colorIdx++ % colorList.size())));
                    sheetGenerator.makeNginxUpstreamsSheet(dataSheet, node);
                } else if (nodeName.equals("thirdPartySolutions")) {
                    // thirdPartySolutions
                    sheetGenerator.makeThirdPartySolutionsSheet(dataSheet, node);
                } else if (nodeName.startsWith("(DB)")) {
                    // Database Instance Sheet
                    sheetGenerator.makeDatabaseInstanceSheet(solution, dataSheet, node, null, null);

                    // Set Filter
                    dataSheet.setAutoFilter(new CellRangeAddress(0, dataSheet.getLastRowNum(), 0, 0));
                } else if (nodeName.equals("fileSummaryMap")) {
                    // fileSummaryMap
                    sheetGenerator.makeFileSummaryMapSheet(dataSheet, node);
                } else if (nodeName.equals("eeModules")) {
                    // eeModules
                    sheetGenerator.makeEeModulesSheet(dataSheet, node);
                } else {
                    sheetGenerator.makeDataSheet(solution, dataSheet, node, null, null);
                }
            }
        }
    }

    /**
     * Make cover.
     *
     * @param coverSheet the cover sheet
     */
    private void makeCover(SXSSFSheet coverSheet, String type, String title) throws InterruptedException {
        for (int i = 0; i < 39; i++) {
            row = coverSheet.createRow(i);

            for (int j = 0; j < 8; j++) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, j, HorizontalAlignment.CENTER, BorderStyle.NONE, false);
                cell.getCellStyle().setVerticalAlignment(VerticalAlignment.CENTER);
                cell.getCellStyle().setFillForegroundColor(IndexedColors.WHITE.getIndex());
                cell.getCellStyle().setFillPattern(FillPatternType.SOLID_FOREGROUND);

                if (i == 5 && j > 0 && j < 7) {
                    cell.getCellStyle().setTopBorderColor(IndexedColors.SKY_BLUE.getIndex());
                    cell.getCellStyle().setBorderTop(BorderStyle.THIN);
                    cell.getCellStyle().setBottomBorderColor(IndexedColors.SKY_BLUE.getIndex());
                    cell.getCellStyle().setBorderBottom(BorderStyle.THICK);
                }

                if (i == 20 && j > 4 && j < 7) {
                    cell.getCellStyle().setBorderTop(BorderStyle.THICK);
                }

                if (i == 22 && j > 4 && j < 7) {
                    cell.getCellStyle().setBorderBottom(BorderStyle.THIN);
                }

                coverSheet.setColumnWidth(j, 3342);
            }

            if (i == 5) {
                row.setHeight((short) 1160);
            } else {
                row.setHeight((short) 400);
            }
        }

        try (InputStream inputStream = ExcelExporter.class.getClassLoader().getResourceAsStream("logo/roro_logo.png")) {
            int pictureIdx = workbook.addPicture(IOUtils.toByteArray(inputStream), Workbook.PICTURE_TYPE_PNG);

            CreationHelper helper = workbook.getCreationHelper();
            SXSSFDrawing drawing = coverSheet.createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();
            // ClientAnchor anchor = drawing.createAnchor(500, 500, 100, 100, 3, 3, -1, 0);

            anchor.setCol1(3);
            anchor.setRow1(3);

            Picture pict = drawing.createPicture(anchor, pictureIdx);

            // pict.resize(0.31 * 633 / PIXEL_DPI, 1.5 * 119 / PIXEL_DPI);
            pict.resize(0.031 * 633 / PIXEL_DPI, 0.15 * 119 / PIXEL_DPI);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.warn("Unable to draw \"roro_logo.png\" to assessment excel report. [Reason] : {}", e.getMessage());
        }

        row = coverSheet.getRow(5);
        SXSSFCell titleCell = row.getCell(1);
        titleCell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
        titleCell.getCellStyle().setFont(fontMap.get("20_B"));
        coverSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 6));
        titleCell.setCellValue("RoRo " + type + " Assessment Report");

        row = coverSheet.getRow(7);
        SXSSFCell nameCell = row.getCell(1);
        nameCell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);
        nameCell.getCellStyle().setFont(fontMap.get("11"));
        coverSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 6));
        nameCell.setCellValue(type + " Name : " + title);

        Date reportDate = new Date();

        row = coverSheet.getRow(8);
        SXSSFCell dateCell = row.getCell(1);
        dateCell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);
        dateCell.getCellStyle().setFont(fontMap.get("11"));
        coverSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 6));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String cellValue = formatter.format(reportDate);
        dateCell.setCellValue("Scanned Date : " + cellValue);

        row = coverSheet.getRow(21);
        nameCell = row.getCell(4);
        nameCell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);
        nameCell.getCellStyle().setFont(fontMap.get("12_B"));
        coverSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 4, 6));
        nameCell.setCellValue(title);

        row = coverSheet.getRow(36);
        dateCell = row.getCell(1);
        dateCell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
        dateCell.getCellStyle().setFont(fontMap.get("12_B"));
        coverSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 6));
        formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        cellValue = formatter.format(reportDate);
        dateCell.setCellValue(cellValue);

        row = coverSheet.getRow(37);
        SXSSFCell copyrightCell = row.getCell(1);
        copyrightCell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
        copyrightCell.getCellStyle().setFont(fontMap.get("12"));
        coverSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 6));
        copyrightCell.setCellValue("Report is created automatically by Playce RoRo - OpenSourceConsulting");

        CellRangeAddress region = new CellRangeAddress(0, 38, 0, 7);
        RegionUtil.setBorderBottom(BorderStyle.THIN, region, coverSheet);
        RegionUtil.setBorderTop(BorderStyle.THICK, region, coverSheet);
        RegionUtil.setBorderLeft(BorderStyle.THICK, region, coverSheet);
        RegionUtil.setBorderRight(BorderStyle.THICK, region, coverSheet);
    }

    /**
     * Make general.
     *
     * @param generalSheet the general sheet
     */
    private void makeGeneral(Object entity, SXSSFSheet generalSheet) throws InterruptedException {
        // Add entity information to general sheet
        if (entity != null) {
            try {
                if (entity instanceof ServerDetailResponse) {
                    type = "Server";
                    addServerToGeneral(generalSheet, (ServerDetailResponse) entity);
                } else if (entity instanceof MiddlewareDetailResponse) {
                    type = "Middleware";
                    addMiddlewareToGeneral(generalSheet, (MiddlewareDetailResponse) entity);
                } else if (entity instanceof ApplicationDetailResponse) {
                    type = "Application";
                    addApplicationToGeneral(generalSheet, (ApplicationDetailResponse) entity);
                } else if (entity instanceof DatabaseEngineResponseDto) {
                    type = "Database";
                    addDatabaseToGeneral(generalSheet, (DatabaseEngineResponseDto) entity);
                }

                //  create an empty row
                row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
            } catch (Exception e) {
                RoRoException.checkInterruptedException(e);
                log.error("Unable to add entity information to general sheet. ", e);
            }
        }

        if (!"database".equals(solution) && !"windows".equals(solution)) {
            row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.NONE, false);
            cell.setCellValue("Additional information by assessment");
            cell.getCellStyle().setFont(fontMap.get("14_B"));
            generalSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 1));
        }
    }

    /**
     * Add to general.
     *
     * @param generalSheet the general sheet
     * @param node         the node
     * @param nodeName     the node name
     */
    private void addToGeneral(SXSSFSheet generalSheet, JsonNode node, String nodeName) throws InterruptedException {
        if (solution == null && nodeName.toLowerCase().contains("name")) {
            String value = node.asText().toLowerCase();

            if (value.contains("weblogic")) {
                solution = "weblogic";
            } else if (value.contains("websphere")) {
                solution = "websphere";
            } else if (value.contains("jeus")) {
                solution = "jeus";
            } else if (value.contains("tomcat")) {
                solution = "tomcat";
            } else if (value.contains("webtob")) {
                solution = "webtob";
            } else if (value.contains("apache") || value.contains("http")) {
                solution = "apache";
            } else if (value.contains("jboss")) {
                solution = "jboss";
            } else if (value.contains("nginx")) {
                solution = "nginx";
            }
        }

        if (node.size() == 0) {
            row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue(nodeName);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, null);
            ExcelHelper.setCellData(workbook, cell, node, nodeName, styleMap, fontMap);
        } else if (("apache".equals(solution) || "nginx".equals(solution)) && ("listenPort".equals(nodeName) || "env".equals(nodeName))) {
            row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue(nodeName);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, null);
            ExcelHelper.setCellData(workbook, cell, node, nodeName, styleMap, fontMap);
        } else {
            Iterator<String> fieldNames = node.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                addToGeneral(generalSheet, node.get(fieldName), fieldName);

                if (fieldName.equals("version") && "jeus".equals(solution)) {
                    version = node.get(fieldName).asText();
                }
            }
        }
    }

    /**
     * Add server to general.
     *
     * @param generalSheet the general sheet
     * @param server       the server
     */
    private void addServerToGeneral(SXSSFSheet generalSheet, ServerDetailResponse server) {
        title = server.getServerInventoryName();
        row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Server ID");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Server Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Inventory Code");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Inventory Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Service ID(s)");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Service Name(s)");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Migration Type");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("IP Address");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Port");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Username");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Server Location");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Environment");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Hypervisor");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Cluster Type");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Access Control");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("tpmc");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Purchase Date");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Manufacturer");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Model");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Serial Number");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Description");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Labels");

        row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getServerInventoryId());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getServerInventoryName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getCustomerInventoryCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getCustomerInventoryName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (server.getServices() != null) {
            List<Service> serviceList = server.getServices();
            cell.setCellValue(String.join(",\n", serviceList.stream().map(s -> Long.toString(s.getServiceId())).collect(Collectors.toList())));
        }
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (server.getServices() != null) {
            List<Service> serviceList = server.getServices();
            cell.setCellValue(String.join(",\n", serviceList.stream().map(s -> s.getServiceName()).collect(Collectors.toList())));
        }
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getMigrationTypeCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getRepresentativeIpAddress());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getConnectionPort());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getUserName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getServerLocation());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getServerUsageTypeCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getHypervisorTypeCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getDualizationTypeCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getAccessControlSystemSolutionName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getTpmc() == null ? "" : server.getTpmc().toString());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getBuyDate());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getMakerName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getModelName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getSerialNumber());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(server.getDescription());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (server.getLabelList() != null) {
            cell.setCellValue(String.join(",", server.getLabelList().stream().map(l -> l.getLabelName()).collect(Collectors.toList())));
        }
    }

    /**
     * Add middleware to general.
     *
     * @param generalSheet the general sheet
     * @param middleware   the middleware
     */
    private void addMiddlewareToGeneral(SXSSFSheet generalSheet, MiddlewareDetailResponse middleware) {
        title = middleware.getMiddlewareInventoryName();

        row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Middleware ID");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Middleware Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Inventory Code");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Inventory Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Service ID(s)");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Service Name(s)");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Server ID");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Server Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Type");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Vendor");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Engine Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Engine Version");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Engine Path");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Domain Home");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Description");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Labels");

        row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getMiddlewareInventoryId());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getMiddlewareInventoryName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getCustomerInventoryCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getCustomerInventoryName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (middleware.getServices() != null) {
            List<Service> serviceList = middleware.getServices();
            cell.setCellValue(String.join(",\n", serviceList.stream().map(s -> Long.toString(s.getServiceId())).collect(Collectors.toList())));
        }
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (middleware.getServices() != null) {
            List<Service> serviceList = middleware.getServices();
            cell.setCellValue(String.join(",\n", serviceList.stream().map(s -> s.getServiceName()).collect(Collectors.toList())));
        }
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getServerInventoryId());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getServerInventoryName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getMiddlewareTypeCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getVendorName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getInventoryDetailTypeCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getEngineVersion());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getEngineInstallPath());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getDomainHomePath());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(middleware.getDescription());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (middleware.getLabelList() != null) {
            cell.setCellValue(String.join(",", middleware.getLabelList().stream().map(l -> l.getLabelName()).collect(Collectors.toList())));
        }
    }

    /**
     * Add application to general.
     *
     * @param generalSheet the general sheet
     * @param application  the application
     */
    private void addApplicationToGeneral(SXSSFSheet generalSheet, ApplicationDetailResponse application) {
        title = application.getApplicationInventoryName();

        row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Application ID");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Application Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Inventory Code");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Inventory Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Service ID(s)");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Service Name(s)");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Server ID");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Server Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Application Type");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Deploy Path");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Application URI");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Application File Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Application File Path");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Analyze Target Library List");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Analyze Target String List");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Description");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Labels");

        row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getApplicationInventoryId());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getApplicationInventoryName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getCustomerInventoryCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getCustomerInventoryName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (application.getServiceList() != null) {
            List<Service> serviceList = application.getServiceList();
            cell.setCellValue(String.join(",\n", serviceList.stream().map(s -> Long.toString(s.getServiceId())).collect(Collectors.toList())));
        }
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (application.getServiceList() != null) {
            List<Service> serviceList = application.getServiceList();
            cell.setCellValue(String.join(",\n", serviceList.stream().map(s -> s.getServiceName()).collect(Collectors.toList())));
        }
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getServerInventoryId());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getServerInventoryName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getInventoryDetailTypeCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getDeployPath());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getSourceLocationUri());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getUploadSourceFileName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getUploadSourceFilePath());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(String.join(",", application.getAnalysisLibList()));
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(String.join(",", application.getAnalysisStringList()));
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(application.getDescription());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (application.getLabelList() != null) {
            cell.setCellValue(String.join(",", application.getLabelList().stream().map(l -> l.getLabelName()).collect(Collectors.toList())));
        }
    }

    /**
     * Add database to general.
     *
     * @param generalSheet the general sheet
     * @param database     the database
     */
    private void addDatabaseToGeneral(SXSSFSheet generalSheet, DatabaseEngineResponseDto database) {
        title = database.getDatabaseInventoryName();

        row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Database ID");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Database Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Inventory Code");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Inventory Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Service ID(s)");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Service Name(s)");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Server ID");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Server Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Vendor");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Engine Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Engine Version");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Port");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("DB Service Name");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Connection URL");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Username");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Access Control");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Description");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("Labels");

        row = generalSheet.createRow(generalSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getDatabaseInventoryId());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getDatabaseInventoryName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getCustomerInventoryCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getCustomerInventoryName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (database.getServices() != null) {
            List<Service> serviceList = database.getServices();
            cell.setCellValue(String.join(",\n", serviceList.stream().map(s -> Long.toString(s.getServiceId())).collect(Collectors.toList())));
        }
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (database.getServices() != null) {
            List<Service> serviceList = database.getServices();
            cell.setCellValue(String.join(",\n", serviceList.stream().map(s -> s.getServiceName()).collect(Collectors.toList())));
        }
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getServerInventoryId());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getServerInventoryName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getVendor());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getInventoryDetailTypeCode());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getEngineVersion());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getConnectionPort());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getDatabaseServiceName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getJdbcUrl());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getUserName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getDatabaseAccessControlSystemSolutionName());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(database.getDescription());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        if (database.getLabelList() != null) {
            cell.setCellValue(String.join(",", database.getLabelList().stream().map(l -> l.getLabelName()).collect(Collectors.toList())));
        }
    }
}
//end of ExcelExporter.java
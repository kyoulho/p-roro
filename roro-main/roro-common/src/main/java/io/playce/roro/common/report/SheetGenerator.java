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
 * SangCheon Park   Apr 15, 2021		First Draft.
 */
package io.playce.roro.common.report;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.ExcelHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 2.0.0
 */
@Slf4j
public class SheetGenerator {

    private final SXSSFWorkbook workbook;
    private final Map<String, CellStyle> styleMap;
    private final Map<String, Font> fontMap;
    private SXSSFRow row;
    private SXSSFCell cell;

    /**
     * Instantiates a new Sheet generator.
     *
     * @param workbook the workbook
     * @param styleMap the style map
     * @param fontMap  the font map
     */
    public SheetGenerator(SXSSFWorkbook workbook, Map<String, CellStyle> styleMap, Map<String, Font> fontMap) {
        this.workbook = workbook;
        this.styleMap = styleMap;
        this.fontMap = fontMap;
    }

    /**
     * Make data sheet.
     *
     * @param solution    the solution
     * @param dataSheet   the data sheet
     * @param node        the node
     * @param parentNames the parent names
     * @param baseRow     the base row
     */
    public void makeDataSheet(String solution, SXSSFSheet dataSheet, JsonNode node, List<String> parentNames, SXSSFRow baseRow) throws InterruptedException {
        /*
        System.err.println("SheetName : " + dataSheet.getSheetName() +
                ", node.size() : " + node.size() +
                ", NodeNames : " + parentNames +
                ", RowNum : " + (baseRow == null ? null : baseRow.getRowNum()) +
                ", node.isArray() : " + node.isArray() +
                ", node.isObject() : " + node.isObject());
        //*/

        if (parentNames == null) {
            parentNames = new ArrayList<>();
        }

        int firstRow = dataSheet.getLastRowNum() + 1;
        int firstCell = parentNames.size() - 1;
        int lastCell = parentNames.size() - 1;
        SXSSFRow currentRow = null;

        if (node.isObject()) {
            if (node.size() == 0) {
                if (baseRow == null) {
                    currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                } else {
                    currentRow = baseRow;
                }

                for (String name : parentNames) {
                    cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);
                }

                return;
            }

            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String nodeName = fieldNames.next();
                JsonNode child = node.get(nodeName);

                if (child.isEmpty() && !child.isArray() && !child.isObject()) {
                    int upperRowCellNum = 0;
                    if (dataSheet.getLastRowNum() > -1) {
                        SXSSFRow upperRow = dataSheet.getRow(dataSheet.getLastRowNum());
                        upperRowCellNum = upperRow.getLastCellNum();
                    }

                    if (baseRow == null) {
                        currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                        for (String name : parentNames) {
                            cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(name);
                        }

                        cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue(nodeName);
                    } else {
                        currentRow = baseRow;
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child, nodeName, styleMap, fontMap);

                    // 상위 Row와 Cell 갯수가 다르면 merge한다.
                    if (upperRowCellNum > 0 && upperRowCellNum - parentNames.size() > 2 && currentRow != null) {
                        int columnIdx = cell.getColumnIndex();
                        int diff = upperRowCellNum - (cell.getColumnIndex() + 1);

                        for (int i = 0; i < diff; i++) {
                            cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        }

                        if (columnIdx != cell.getColumnIndex()) {
                            mergeRegion(dataSheet, currentRow.getRowNum(), currentRow.getRowNum(), columnIdx, cell.getColumnIndex());
                        }
                    }
                } else {
                    int upperRowCellNum = 0;
                    if (dataSheet.getLastRowNum() > -1) {
                        SXSSFRow upperRow = dataSheet.getRow(dataSheet.getLastRowNum());
                        upperRowCellNum = upperRow.getLastCellNum();
                    }

                    List<String> newParentNames = parentNames.stream().collect(Collectors.toList());
                    newParentNames.add(nodeName);

                    // Server
                    if (dataSheet.getSheetName().equals("Port List") ||
                            dataSheet.getSheetName().equals("Interfaces") ||
                            dataSheet.getSheetName().equals("Vgs")) {
                        if (child.isArray()) {
                            if (baseRow == null) {
                                currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                                for (String name : newParentNames) {
                                    cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                                    cell.setCellValue(name);
                                }
                            }
                        }

                        makeDataSheet(solution, dataSheet, child, newParentNames, currentRow);
                    } else {
                        makeDataSheet(solution, dataSheet, child, newParentNames, null);
                    }

                    if (dataSheet.getSheetName().startsWith("(DB)")) {
                        // (DB) 시트의 경우 autoSizeColumn 이 정상 동작하도록 첫번째 row에 20개의 컬럼을 생성한다.
                        SXSSFRow r = dataSheet.getRow(0);
                        int lastCellNum = r.getLastCellNum();

                        for (int i = lastCellNum; i < 20; i++) {
                            SXSSFCell c = r.createCell(i);
                        }
                    } else {
                        // 상위 Row와 Cell 갯수가 다르면 merge한다.
                        if (child.isArray() && child.size() == 0) {
                            if (upperRowCellNum > 0 && upperRowCellNum - parentNames.size() > 2 && currentRow != null) {
                                int columnIdx = cell.getColumnIndex();
                                int diff = upperRowCellNum - (cell.getColumnIndex() + 1);

                                for (int i = 0; i < diff; i++) {
                                    cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                }

                                if (columnIdx != cell.getColumnIndex()) {
                                    mergeRegion(dataSheet, currentRow.getRowNum(), currentRow.getRowNum(), columnIdx + 1, cell.getColumnIndex());
                                }
                            }
                        }
                    }
                }
            }
        } else if (node.isArray()) {
            Iterator<JsonNode> nodes = node.elements();

            // 횡으로 나열되는 형태에서 배열이 존재하는 경우
            // Database - Users
            // Windows - DNS, Networks, Firewalls
            if ((("database".equals(solution) && "Users".equals(dataSheet.getSheetName()))
                    || ("windows".equals(solution) && "DNS".equals(dataSheet.getSheetName()))
                    || ("windows".equals(solution) && "Networks".equals(dataSheet.getSheetName()))
                    || ("windows".equals(solution) && "Firewalls".equals(dataSheet.getSheetName())))
                    && parentNames.size() == 1) {
                if (baseRow == null) {
                    currentRow = dataSheet.getRow(dataSheet.getLastRowNum());
                } else {
                    currentRow = baseRow;
                }

                cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);

                StringBuilder sb = null;
                while (nodes.hasNext()) {
                    JsonNode child = nodes.next();

                    if (child.isValueNode()) {
                        if (sb == null) {
                            sb = new StringBuilder();
                        } else {
                            sb.append("\n");
                        }

                        sb.append(child.textValue());
                    }
                }

                if (sb != null) {
                    cell.setCellValue(sb.toString());
                }
            } else {
                boolean headerExists = false;

                /**
                 * 아래와 같이 value가 empty list인 경우 "*:4443", "127.0.0.1:9999"를 컬럼 헤더로 표시
                 * "virtualHost" : {
                 *     "*:4443" : [ ],
                 *     "127.0.0.1:9999" : [ ]
                 * }
                 */
                if (node.size() == 0) {
                    if (baseRow == null) {
                        currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                        for (String name : parentNames) {
                            cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(name);
                        }

                        cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    }
                }

                while (nodes.hasNext()) {
                    JsonNode child = nodes.next();

                    if (child.isValueNode()) {
                        if (baseRow == null) {
                            currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                        } else {
                            currentRow = baseRow;
                        }

                        for (String name : parentNames) {
                            cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(name);
                        }

                        cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, child, null, styleMap, fontMap);
                    } else {
                        // Array 형태의 경우 List<Object> 에 대한 내용으로 field 명이 동일하다는 가정으로 진행한다.
                        if (!headerExists && child.isObject()) {
                            if (baseRow == null) {
                                currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                            } else {
                                currentRow = baseRow;
                            }

                            // Application - Libraries, Tomcat - Applications
                            if (currentRow.getLastCellNum() < 1 && parentNames.size() > 0 /* && currentRow.getRowNum() == 0*/) {
                                for (String name : parentNames) {
                                    cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                                    cell.setCellValue(name);
                                }
                            }

                            Iterator<String> childNodes = child.fieldNames();

                            while (childNodes.hasNext()) {
                                cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                                String fieldName = childNodes.next();

                                // Change column header "api" to "JDK Internal API" for Java Application
                                if (dataSheet.getSheetName().equals("Removed List") && fieldName.equals("api")) {
                                    cell.setCellValue("JDK Internal API");
                                } else {
                                    cell.setCellValue(fieldName);
                                }
                            }

                            headerExists = true;
                            firstRow = currentRow.getRowNum();
                        }

                        currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                        for (String name : parentNames) {
                            cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(name);
                        }

                        makeDataSheet(solution, dataSheet, child, parentNames, currentRow);
                    }
                }
            }
        }

        if (parentNames.size() == 0) {
            // data가 1개만 존재하는 경우 row의 height, cell의 width, wrap text 등의 설정이 동작하지 않아 빈 row를 생성
            if (node.size() == 1) {
                JsonNode child = node.get(0);

                int size = 2;
                if (child != null && child.isObject()) {
                    size = child.size();
                }

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                for (int i = 0; i < size; i++) {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.NONE, false, styleMap);
                }
            }

            // Server - Interfaces
            if (dataSheet.getSheetName().equals("Interfaces")) {
                for (int i = 0; i < 4; i++) {
                    CellRangeAddress region = new CellRangeAddress(i, i, 2, 4);
                    dataSheet.addMergedRegion(region);

                    RegionUtil.setBorderBottom(BorderStyle.THIN, region, dataSheet);
                    RegionUtil.setBorderTop(BorderStyle.THIN, region, dataSheet);
                    RegionUtil.setBorderLeft(BorderStyle.THIN, region, dataSheet);
                    RegionUtil.setBorderRight(BorderStyle.THIN, region, dataSheet);
                }
            }

            // Server - Firewall, 해당 sheet의 row, cell 크기에 맞게 전체 border를 설정한다.
            if (dataSheet.getSheetName().equals("Firewall")) {
                int lr = dataSheet.getLastRowNum();
                int lc = 0;

                Iterator<org.apache.poi.ss.usermodel.Row> rowIter = dataSheet.rowIterator();
                while (rowIter.hasNext()) {
                    SXSSFRow r = (SXSSFRow) rowIter.next();

                    if (lc < r.getLastCellNum()) {
                        lc = r.getLastCellNum();
                    }
                }

                if (lr < 0) {
                    lr = 0;
                }

                if (lc < 1) {
                    lc = 1;
                }

                CellRangeAddress region = new CellRangeAddress(0, lr, 0, lc - 1);
                RegionUtil.setBorderBottom(BorderStyle.THIN, region, dataSheet);
                RegionUtil.setBorderTop(BorderStyle.THIN, region, dataSheet);
                RegionUtil.setBorderLeft(BorderStyle.THIN, region, dataSheet);
                RegionUtil.setBorderRight(BorderStyle.THIN, region, dataSheet);
            }
        } else {
            if (dataSheet.getLastRowNum() > firstRow || lastCell > firstCell) {
                //System.err.println(parentNames + " : " + firstRow + ", " + dataSheet.getLastRowNum() + ", " + firstCell + ", " + lastCell);
                mergeRegion(dataSheet, firstRow, dataSheet.getLastRowNum(), firstCell, lastCell);
            }
        }
    }

    /**
     * Make database instance sheet.
     *
     * @param solution    the solution
     * @param dataSheet   the data sheet
     * @param node        the node
     * @param parentNames the parent names
     * @param baseRow     the base row
     */
    public void makeDatabaseInstanceSheet(String solution, SXSSFSheet dataSheet, JsonNode node, List<String> parentNames, SXSSFRow baseRow) throws InterruptedException {
        /*
        System.err.println("SheetName : " + dataSheet.getSheetName() +
                ", node.size() : " + node.size() +
                ", NodeNames : " + parentNames +
                ", RowNum : " + (baseRow == null ? null : baseRow.getRowNum()) +
                ", node.isArray() : " + node.isArray() +
                ", node.isObject() : " + node.isObject());
        //*/

        if (parentNames == null) {
            parentNames = new ArrayList<>();
        }

        int firstRow = dataSheet.getLastRowNum() + 1;
        int firstCell = parentNames.size() - 1;
        int lastCell = parentNames.size() - 1;
        SXSSFRow currentRow = null;

        if (firstRow == 0) {
            currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("Category");

            // (DB) 시트의 경우 autoSizeColumn 이 정상 동작하도록 첫번째 row에 30개의 컬럼을 생성한다.
            for (int i = currentRow.getLastCellNum(); i < 30; i++) {
                currentRow.createCell(i);
            }

            currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("General");

            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String nodeName = fieldNames.next();
                JsonNode child = node.get(nodeName);

                if (child.isEmpty() && !child.isArray() && !child.isObject()) {
                    cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(nodeName);
                } else {
                    break;
                }
            }

            currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("General");

            mergeRegion(dataSheet, 1, dataSheet.getLastRowNum(), 0, 0);
        }

        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String nodeName = fieldNames.next();
                JsonNode child = node.get(nodeName);

                if (child.isEmpty() && !child.isArray() && !child.isObject()) {
                    if (currentRow == null || currentRow.getRowNum() > 2) {
                        if (baseRow == null) {
                            currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                            for (String name : parentNames) {
                                cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                                cell.setCellValue(name);
                            }

                            cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(nodeName);
                        } else {
                            currentRow = baseRow;
                        }
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child, nodeName, styleMap, fontMap);
                } else {
                    List<String> newParentNames = parentNames.stream().collect(Collectors.toList());
                    newParentNames.add(nodeName);

                    // child.size() 가 0보다 클때만 makeDataSheet()를 실행한다.
                    if (child.size() > 0) {
                        makeDatabaseInstanceSheet(solution, dataSheet, child, newParentNames, null);
                    }
                }
            }
        } else if (node.isArray()) {
            Iterator<JsonNode> nodes = node.elements();

            boolean headerExists = false;

            /**
             * 아래와 같이 value가 empty list인 경우 "*:4443", "127.0.0.1:9999"를 컬럼 헤더로 표시
             * "virtualHost" : {
             *     "*:4443" : [ ],
             *     "127.0.0.1:9999" : [ ]
             * }
             */
            if (node.size() == 0) {
                if (baseRow == null) {
                    currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                    for (String name : parentNames) {
                        cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue(name);
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                }
            }

            while (nodes.hasNext()) {
                JsonNode child = nodes.next();

                if (child.isValueNode()) {
                    if (baseRow == null) {
                        currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    } else {
                        currentRow = baseRow;
                    }

                    for (String name : parentNames) {
                        cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue(name);
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child, null, styleMap, fontMap);
                } else {
                    // Array 형태의 경우 List<Object> 에 대한 내용으로 field 명이 동일하다는 가정으로 진행한다.
                    if (!headerExists && child.isObject()) {
                        if (baseRow == null) {
                            currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                        } else {
                            currentRow = baseRow;
                        }

                        if (currentRow.getLastCellNum() < 1 && parentNames.size() > 0 /* && currentRow.getRowNum() == 0*/) {
                            for (String name : parentNames) {
                                cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);

                                if (name.contains("objectSummar")) {
                                    cell.setCellValue(name);
                                } else {
                                    cell.setCellValue(name + " (" + node.size() + ")");
                                }
                            }
                        }

                        Iterator<String> childNodes = child.fieldNames();

                        while (childNodes.hasNext()) {
                            cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            String fieldName = childNodes.next();

                            cell.setCellValue(fieldName);
                        }

                        headerExists = true;
                        firstRow = currentRow.getRowNum();
                    }

                    currentRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                    for (String name : parentNames) {
                        cell = ExcelHelper.createCellWithBorder(workbook, currentRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);

                        if (name.contains("objectSummar")) {
                            cell.setCellValue(name);
                        } else {
                            cell.setCellValue(name + " (" + node.size() + ")");
                        }
                    }

                    makeDatabaseInstanceSheet(solution, dataSheet, child, parentNames, currentRow);
                }
            }
        }

        if (parentNames.size() == 0) {
            // data가 1개만 존재하는 경우 row의 height, cell의 width, wrap text 등의 설정이 동작하지 않아 빈 row를 생성
            if (node.size() == 1) {
                JsonNode child = node.get(0);

                int size = 2;
                if (child != null && child.isObject()) {
                    size = child.size();
                }

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                for (int i = 0; i < size; i++) {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.NONE, false, styleMap);
                }
            }
        } else {
            if (dataSheet.getLastRowNum() > firstRow || lastCell > firstCell) {
                //System.err.println(parentNames + " : " + firstRow + ", " + dataSheet.getLastRowNum() + ", " + firstCell + ", " + lastCell);
                mergeRegion(dataSheet, firstRow, dataSheet.getLastRowNum(), firstCell, lastCell);
            }
        }
    }

    /**
     * Make users sheet for Server.
     *
     * @param dataSheet the data sheet
     * @param users     users
     * @param groups    groups
     */
    public void makeUsersSheet(SXSSFSheet dataSheet, Map<String, Object> users, Map<String, Object> groups) {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("user");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("uid");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("defaultGroup");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("gid");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("subGroup (gid)");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("homeDir");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("shell");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("profile");

        if (users == null) {
            users = new HashMap<>();
        }
        if (groups == null) {
            groups = new HashMap<>();
        }

        Map<String, String> userDetail;
        Map<String, Object> groupDetail;
        for (String user : users.keySet()) {
            userDetail = (Map<String, String>) users.get(user);

            String defaultGroup = null;
            StringBuilder subGroup = new StringBuilder();
            List<String> userList;
            for (String group : groups.keySet()) {
                groupDetail = (Map<String, Object>) groups.get(group);

                userList = (List<String>) groupDetail.get("users");

                if (groupDetail.get("gid").equals(userDetail.get("gid"))) {
                    defaultGroup = group;
                }

                if (userList != null && userList.contains(user)) {
                    if (StringUtils.isNotEmpty(subGroup) && !subGroup.toString().endsWith("\r\n")) {
                        subGroup.append("\r\n");
                    }

                    subGroup.append(group).append(StringUtils.SPACE).append("(").append(groupDetail.get("gid")).append(")");
                }
            }

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(user);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(Integer.parseInt(userDetail.get("uid")));

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(defaultGroup);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(Integer.parseInt(userDetail.get("gid")));

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(subGroup.toString());

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(userDetail.get("homeDir"));

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(userDetail.get("shell"));

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(userDetail.get("profile"));
        }
    }

    /**
     * Make groups sheet for Server.
     *
     * @param dataSheet the data sheet
     * @param users     users
     * @param groups    groups
     */
    public void makeGroupsSheet(SXSSFSheet dataSheet, Map<String, Object> users, Map<String, Object> groups) {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("group");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("gid");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("user (uid)");

        if (users == null) {
            users = new HashMap<>();
        }
        if (groups == null) {
            groups = new HashMap<>();
        }

        Map<String, Object> groupDetail;
        Map<String, String> userDetail;
        List<String> userList;
        for (String group : groups.keySet()) {
            groupDetail = (Map<String, Object>) groups.get(group);
            userList = (List<String>) groupDetail.get("users");

            StringBuilder userBuilder = new StringBuilder();

            if (userList != null) {
                for (String user : userList) {
                    userDetail = (Map<String, String>) users.get(user);

                    if (StringUtils.isNotEmpty(userBuilder) && !userBuilder.toString().endsWith("\r\n")) {
                        userBuilder.append("\r\n");
                    }

                    if (userDetail != null) {
                        userBuilder.append(user).append(StringUtils.SPACE).append("(").append(userDetail.get("uid")).append(")");
                    } else {
                        userBuilder.append(user);
                    }
                }
            }

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(group);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(Integer.parseInt((String) groupDetail.get("gid")));

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(userBuilder.toString());
        }
    }

    /**
     * Make partitions sheet for Server.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makePartitionsSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("partition");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("device");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("fileSystem");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("size(MB)");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("free(MB)");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("used(MB)");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("usage(%)");

        Iterator<String> fieldNames = node.fieldNames();

        CellStyle style = null;
        double total = 0, size, free, used, usage;
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode subNode = node.get(fieldName);

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(fieldName);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("device"), "device", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("fsType"), "fsType", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            if (style == null) {
                style = ExcelHelper.copyCellStyle(workbook, cell.getCellStyle());
                style.setAlignment(HorizontalAlignment.RIGHT);
                style.setDataFormat(styleMap.get("#,##0.00").getDataFormat());
            }
            cell.setCellStyle(style);
            size = subNode.get("size").asDouble();
            total += size;
            cell.setCellValue(size);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellStyle(style);
            free = subNode.get("free").asDouble();
            cell.setCellValue(free);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellStyle(style);
            used = (size - free);
            cell.setCellValue(used);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellStyle(style);
            usage = (size - free) * 100 / size;
            cell.setCellValue(usage);
        }
    }

    /**
     * Make processes sheet for Server.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeProcessesSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("name");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("cmd");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("pid");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("user");

        Iterator<JsonNode> elements = node.elements();

        while (elements.hasNext()) {
            JsonNode subNode = elements.next();

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("name"), "name", styleMap, fontMap);

            JsonNode cmdNode = subNode.get("cmd");
            StringBuilder sb = new StringBuilder();
            if (cmdNode != null && cmdNode.size() > 0) {

                Iterator<JsonNode> iter = cmdNode.elements();
                while (iter.hasNext()) {
                    if (StringUtils.isNotEmpty(sb.toString())) {
                        sb.append(" ");
                    }
                    sb.append(iter.next().asText());
                }
            }

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(sb.toString());

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("pid"), "pid", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("user"), "user", styleMap, fontMap);
        }
    }

    /**
     * Make interfaces sheet for Server.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeInterfacesSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("interface");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("ipv4");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("ipv6");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("gateway");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("netmask");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("broadcast");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("macaddress");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("script");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("rxBytes/s");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("txBytes/s");

        Iterator<JsonNode> elements = node.elements();

        while (elements.hasNext()) {
            JsonNode subNode = elements.next();
            JsonNode ipv4Nodes = subNode.get("ipv4");
            JsonNode ipv6Nodes = subNode.get("ipv6");

            StringBuilder ipv4 = new StringBuilder();
            StringBuilder ipv6 = new StringBuilder();
            StringBuilder netmask = new StringBuilder();
            StringBuilder broadcast = new StringBuilder();

            Iterator<JsonNode> ipv4NodeElements = ipv4Nodes.elements();
            Iterator<JsonNode> ipv6NodeElements = ipv6Nodes.elements();

            while (ipv4NodeElements.hasNext()) {
                JsonNode ipv4Node = ipv4NodeElements.next();

                if (StringUtils.isNotEmpty(ipv4) && !ipv4.toString().endsWith("\r\n")) {
                    ipv4.append("\r\n");
                }

                if (StringUtils.isNotEmpty(netmask) && !netmask.toString().endsWith("\r\n")) {
                    netmask.append("\r\n");
                }

                if (StringUtils.isNotEmpty(broadcast) && !broadcast.toString().endsWith("\r\n")) {
                    broadcast.append("\r\n");
                }

                if (ipv4Node.get("address") != null) {
                    ipv4.append(ipv4Node.get("address").asText());
                }
                if (ipv4Node.get("netmask") != null) {
                    netmask.append(ipv4Node.get("netmask").asText());
                }
                if (ipv4Node.get("broadcast") != null) {
                    broadcast.append(ipv4Node.get("broadcast").asText());
                }
            }

            while (ipv6NodeElements.hasNext()) {
                JsonNode ipv6Node = ipv6NodeElements.next();

                if (StringUtils.isNotEmpty(ipv6) && !ipv6.toString().endsWith("\r\n")) {
                    ipv6.append("\r\n");
                }

                if (ipv6Node.get("address") != null) {
                    ipv6.append(ipv6Node.get("address").asText());
                }
            }

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("device"), "device", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(ipv4.toString());

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(ipv6.toString());

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("gateway"), "gateway", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(netmask.toString());

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(broadcast.toString());

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("macaddress"), "macaddress", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("script"), "script", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("rxBytes/s"), "rxBytes/s", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("txBytes/s"), "txBytes/s", styleMap, fontMap);
        }
    }

    /**
     * Make check list sheet for Java Application.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeCheckListSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("fileName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("category");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("line");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("value");

        Iterator<JsonNode> elements = node.elements();

        String fileName = null;
        String category = null;
        int fileStartRow = 0;
        int fileLastRow = 0;
        int categoryStartRow = 0;
        int categoryLastRow = 0;
        while (elements.hasNext()) {
            JsonNode subNode = elements.next();

            Iterator<String> fieldNames = subNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode child = subNode.get(fieldName);

                if ("fileName".equals(fieldName)) {
                    fileName = child.asText();
                    fileStartRow = row.getRowNum() + 1;
                } else {
                    category = fieldName;
                }

                if (child.size() == 0) {
                    continue;
                }

                categoryStartRow = row.getRowNum() + 1;
                categoryLastRow = categoryStartRow + child.size() - 1;
                fileLastRow += child.size();

                for (int i = 0; i < child.size(); i++) {
                    JsonNode n = child.get(i);
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    cell.setCellValue(fileName);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    cell.setCellValue(category);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, n.get("line"), "line", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, n.get("value"), "value", styleMap, fontMap);
                }

                if (categoryLastRow > categoryStartRow) {
                    mergeRegion(dataSheet, categoryStartRow, categoryLastRow, 1, 1);
                }
            }

            if (fileLastRow > fileStartRow) {
                mergeRegion(dataSheet, fileStartRow, fileLastRow, 0, 0);
            }
        }
    }

    /**
     * Make dataSource list sheet for Java Application.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeDataSourceListSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        // 1st Row
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("datasource type");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("value");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("jdbc property");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 2, 5);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("uses");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 6, 8);

        // 2nd Row
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, 0, row.getRowNum(), 0, 0);
        mergeRegion(dataSheet, 0, row.getRowNum(), 1, 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("DB type");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("host");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("port");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("database");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("fileName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("line");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("value");

        Iterator<JsonNode> elements = node.elements();

        String name;
        int useStartRow = 0;
        int useLastRow = 0;
        while (elements.hasNext()) {
            JsonNode n = elements.next();

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
            useStartRow = row.getRowNum();

            name = "type";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "value";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            List<String> type = new ArrayList<>();
            List<String> host = new ArrayList<>();
            List<String> port = new ArrayList<>();
            List<String> database = new ArrayList<>();

            JsonNode jdbcProperties = n.get("jdbcProperties");
            if (jdbcProperties != null) {
                Iterator<JsonNode> iter = jdbcProperties.elements();
                while (iter.hasNext()) {
                    JsonNode j = iter.next();

                    type.add(j.get("type").asText());
                    host.add(j.get("host").asText());
                    port.add(j.get("port").asText());
                    database.add(j.get("database").asText());
                }
            }

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(String.join("\r\n", type));

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(String.join("\r\n", host));

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            if (port.size() == 1) {
                cell.setCellValue(Integer.parseInt(port.get(0)));
            } else {
                cell.setCellValue(String.join("\r\n", port));
            }

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(String.join("\r\n", database));

            JsonNode uses = n.get("uses");
            if (uses != null) {
                int useCnt = 0;
                Iterator<JsonNode> iter = uses.elements();
                while (iter.hasNext()) {
                    JsonNode u = iter.next();

                    if (useCnt++ > 0) {
                        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                        useLastRow = row.getRowNum();

                        for (int i = 0; i <= 5; i++) {
                            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        }
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, u.get("fileName"), "fileName", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, u.get("line"), "line", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, u.get("value"), "value", styleMap, fontMap);
                }
            }

            if (useLastRow > useStartRow) {
                for (int i = 0; i <= 5; i++) {
                    mergeRegion(dataSheet, useStartRow, useLastRow, i, i);
                }
            }
        }
    }

    /**
     * Make deprecated list sheet for Java Application.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeDeprecatedListSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("release");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("class");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("JDK Internal API");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("method");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("forRemoval");

        Iterator<JsonNode> elements = node.elements();

        Integer release = null;
        while (elements.hasNext()) {
            JsonNode subNode = elements.next();

            Iterator<String> fieldNames = subNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode child = subNode.get(fieldName);

                if ("release".equals(fieldName)) {
                    release = child.asInt();
                }

                if (child.size() == 0) {
                    continue;
                }

                for (int i = 0; i < child.size(); i++) {
                    JsonNode n = child.get(i);
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    cell.setCellValue(release);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, n.get("class"), "class", styleMap, fontMap);

                    JsonNode ref = n.get("reference");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, ref.get("class"), "class", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, ref.get("method"), "method", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, ref.get("forRemoval"), "forRemoval", styleMap, fontMap);
                }
            }
        }
    }

    /**
     * Make servers sheet for WebShpere.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeWebSphereServersSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        // 1st Row
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("profileName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("cellName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("nodeName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("clusterName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("serverName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("hostName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("status");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("listenPort");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("jvmOption");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("config");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 9, 13);

        // 2nd Row
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
        for (int i = 0; i <= 8; i++) {
            cell = ExcelHelper.createCellWithBorder(workbook, row, i, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        }

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("ioRedirect");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 9, 10);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("jvmEntries");
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 11, 13);

        // 3rd Row
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
        for (int i = 0; i <= 8; i++) {
            cell = ExcelHelper.createCellWithBorder(workbook, row, i, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            mergeRegion(dataSheet, 0, row.getRowNum(), i, i);
        }

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("stdoutFilename");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("stderrFilename");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("systemProperties");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("bootClasspath");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("properties");

        Iterator<JsonNode> elements = node.elements();

        String name;
        while (elements.hasNext()) {
            JsonNode n = elements.next();

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            name = "profileName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "cellName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "nodeName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "clusterName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "serverName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "hostName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "status";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "listenPort";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "jvmOptions";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            JsonNode config = n.get("config");
            JsonNode ioRedirect = null;
            JsonNode jvmEntries = null;

            if (config != null) {
                ioRedirect = config.get("ioRedirect");
                jvmEntries = config.get("jvmEntries");
            }

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            if (ioRedirect != null) {
                ExcelHelper.setCellData(workbook, cell, ioRedirect.get("stdoutFilename"), "stdoutFilename", styleMap, fontMap);
            }

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            if (ioRedirect != null) {
                ExcelHelper.setCellData(workbook, cell, ioRedirect.get("stderrFilename"), "stderrFilename", styleMap, fontMap);
            }

            JsonNode systemProperties = null;
            JsonNode bootClasspath = null;
            JsonNode properties = null;

            if (jvmEntries != null) {
                systemProperties = jvmEntries.get("systemProperties");
                bootClasspath = jvmEntries.get("bootClasspath");
                properties = jvmEntries.get("properties");
            }

            StringBuilder sb = new StringBuilder();
            if (systemProperties != null) {
                Iterator<JsonNode> iter = systemProperties.elements();
                while (iter.hasNext()) {
                    JsonNode p = iter.next();

                    if (StringUtils.isNotEmpty(sb.toString())) {
                        sb.append("\n");
                    }
                    sb.append(p.get("name").asText());
                    sb.append(" : ");
                    sb.append(p.get("value").asText());
                }
            }
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(sb.toString());

            sb = new StringBuilder();
            if (bootClasspath != null) {
                Iterator<JsonNode> iter = bootClasspath.elements();
                while (iter.hasNext()) {
                    JsonNode p = iter.next();

                    if (StringUtils.isNotEmpty(sb.toString())) {
                        sb.append("\n");
                    }
                    sb.append(p.asText());
                }
            }
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(sb.toString());

            sb = new StringBuilder();
            if (properties != null) {
                Iterator<String> fieldNames = properties.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();

                    if (StringUtils.isNotEmpty(sb.toString())) {
                        sb.append("\n");
                    }

                    sb.append(fieldName).append(" : ").append(properties.get(fieldName).asText());
                }
            }
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(sb.toString());
        }
    }

    /**
     * Make clusters sheet for WebShpere.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeWebSphereClustersSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        // 1st Row
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("profileName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("cellName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("clusterName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("nodeGroup");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("dwlm");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("members");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 5, 6);

        // 2nd Row
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
        for (int i = 0; i <= 4; i++) {
            cell = ExcelHelper.createCellWithBorder(workbook, row, i, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            mergeRegion(dataSheet, 0, row.getRowNum(), i, i);
        }

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("nodeName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("serverName");

        Iterator<JsonNode> elements = node.elements();

        String name;
        while (elements.hasNext()) {
            JsonNode n = elements.next();

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            name = "profileName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "cellName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "clusterName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "nodeGroup";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "dwlm";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            JsonNode members = n.get("members");

            int membersStartRow = row.getRowNum();
            int memberCnt = 0;
            Iterator<JsonNode> e = members.elements();
            while (e.hasNext()) {
                JsonNode member = e.next();

                if (memberCnt++ > 0) {
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                    for (int i = 0; i <= 4; i++) {
                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    }
                }

                name = "nodeName";
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, member.get(name), name, styleMap, fontMap);

                name = "serverName";
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, member.get(name), name, styleMap, fontMap);
            }

            for (int i = 0; i <= 4; i++) {
                mergeRegion(dataSheet, membersStartRow, row.getRowNum(), i, i);
            }
        }
    }

    /**
     * Make ports sheet for WebSphere.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makePortsSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        // 1st Row
        cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("profileName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("nodeName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("serverEntries");

        cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 2, 4);

        // 2nd Row
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("serverName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("endPoint");

        cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 3, 4);

        // 3rd Row
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("endPointName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("port");

        mergeRegion(dataSheet, 0, row.getRowNum(), 0, 0);
        mergeRegion(dataSheet, 0, row.getRowNum(), 1, 1);
        mergeRegion(dataSheet, 1, row.getRowNum(), 2, 2);

        Iterator<JsonNode> elements = node.elements();

        String profileName = null;
        String nodeName = null;
        String serverName = null;
        int profileStartRow = row.getRowNum();
        int nodeStartRow = row.getRowNum();
        int serverStartRow = row.getRowNum();
        while (elements.hasNext()) {
            JsonNode subNode = elements.next();

            Iterator<String> fieldNames = subNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode child = subNode.get(fieldName);

                if ("profileName".equals(fieldName)) {
                    if (!child.asText().equals(profileName)) {
                        if (row.getRowNum() > profileStartRow) {
                            mergeRegion(dataSheet, profileStartRow, row.getRowNum(), 0, 0);
                        }
                        profileStartRow = row.getRowNum() + 1;
                    }

                    profileName = child.asText();
                } else if ("nodeName".equals(fieldName)) {
                    if (!child.asText().equals(nodeName)) {
                        if (row.getRowNum() > nodeStartRow) {
                            mergeRegion(dataSheet, nodeStartRow, row.getRowNum(), 1, 1);
                        }
                        nodeStartRow = row.getRowNum() + 1;
                    }

                    nodeName = child.asText();
                } else if ("serverEntries".equals(fieldName)) {
                    for (int i = 0; i < child.size(); i++) {
                        JsonNode n1 = child.get(i);

                        serverName = n1.get("serverName") == null ? "" : n1.get("serverName").asText();
                        serverStartRow = row.getRowNum() + 1;

                        JsonNode endPoint = n1.get("endPoint");

                        for (int j = 0; j < endPoint.size(); j++) {
                            JsonNode n2 = endPoint.get(j);

                            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            cell.setCellValue(profileName);

                            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            cell.setCellValue(nodeName);

                            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            cell.setCellValue(serverName);

                            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, n2.get("endPointName"), "endPointName", styleMap, fontMap);

                            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, n2.get("port"), "port", styleMap, fontMap);
                        }

                        if (row.getRowNum() > serverStartRow) {
                            mergeRegion(dataSheet, serverStartRow, row.getRowNum(), 2, 2);
                        }
                    }
                }
            }
        }

        if (row.getRowNum() > profileStartRow) {
            mergeRegion(dataSheet, profileStartRow, row.getRowNum(), 0, 0);
        }

        if (row.getRowNum() > nodeStartRow) {
            mergeRegion(dataSheet, nodeStartRow, row.getRowNum(), 1, 1);
        }
    }

    /**
     * Make server sheet for WebLogic.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeInstancesSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("name");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("type");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("listenPort");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("listenAddress");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("sslEnabled");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("log");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("");

        mergeRegion(dataSheet, 0, 0, cell.getColumnIndex() - 1, cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("clusterName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("minHeap");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("maxHeap");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("runUser");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("vmOption");

        Iterator<JsonNode> elements = node.elements();

        String name;
        int nodeStartRow = 0;
        int nodeLastRow = 0;
        while (elements.hasNext()) {
            JsonNode n = elements.next();

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
            nodeStartRow = row.getRowNum();
            nodeLastRow = row.getRowNum();

            name = "name";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "type";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "listenPort";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "listenAddress";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "sslEnabled";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            JsonNode logNode = n.get("log");
            if (logNode.size() == 0) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            } else {
                nodeLastRow = nodeStartRow + logNode.size() - 1;

                int cnt = 0;
                Iterator<String> logFiledNames = logNode.fieldNames();
                while (logFiledNames.hasNext()) {
                    String logFieldName = logFiledNames.next();
                    JsonNode l = logNode.get(logFieldName);

                    if (cnt++ == 0) {
                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue(logFieldName);

                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, l, logFieldName, styleMap, fontMap);
                    } else {
                        SXSSFRow logRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                        for (int i = 0; i < 12; i++) {
                            if (i == 5) {
                                cell = ExcelHelper.createCellWithBorder(workbook, logRow, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                                cell.setCellValue(logFieldName);
                            } else if (i == 6) {
                                cell = ExcelHelper.createCellWithBorder(workbook, logRow, 6, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                ExcelHelper.setCellData(workbook, cell, l, logFieldName, styleMap, fontMap);
                            } else {
                                cell = ExcelHelper.createCellWithBorder(workbook, logRow, i, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            }
                        }
                    }
                }
            }

            name = "clusterName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "minHeap";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "maxHeap";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "runUser";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "vmOption";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            if (nodeLastRow > nodeStartRow) {
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 0, 0);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 1, 1);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 2, 2);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 3, 3);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 4, 4);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 7, 7);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 8, 8);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 9, 9);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 10, 10);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 11, 11);
            }
        }
    }

    /**
     * Make resource sheet for WebLogic.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeResourceSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        Iterator<String> fieldNames = node.fieldNames();

        int jdbcStartRow = -1, jmsStartRow = -1, nameStartRow = 0;
        String name;
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode subNode = node.get(fieldName);

            if ("jdbc".equals(fieldName) && !subNode.isEmpty()) {
                Iterator<JsonNode> elements = subNode.elements();

                jdbcStartRow = dataSheet.getLastRowNum() + 1;
                while (elements.hasNext()) {
                    JsonNode child = elements.next();
                    JsonNode dataSource = null;
                    JsonNode poolParam = null;
                    JsonNode sourceParam = null;
                    JsonNode driverParam = null;

                    if (child != null) {
                        dataSource = child.get("datasource");
                        poolParam = child.get("jdbcConnectionPoolParams");
                        sourceParam = child.get("jdbcDataSourceParams");
                    }

                    if (dataSource != null) {
                        driverParam = dataSource.get("jdbcDriverParams");
                    }

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    nameStartRow = row.getRowNum() + 1;

                    // 1st Row
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(fieldName);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("name");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("target");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("descriptorFileName");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("datasource");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("name");

                    name = "name";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 7, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 8, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
                    mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 6, 8);

                    // 2nd Row
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(fieldName);

                    name = "name";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    name = "target";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    name = "descriptorFileName";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("datasource");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("jdbcDriverParams");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("url");

                    name = "url";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 7, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (driverParam != null) {
                        ExcelHelper.setCellData(workbook, cell, driverParam.get(name), name, styleMap, fontMap);
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 8, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
                    mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 7, 8);

                    // 3rd Row
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(fieldName);

                    name = "name";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    name = "target";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    name = "descriptorFileName";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("datasource");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("jdbcDriverParams");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("dirverName");

                    name = "driverName";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 7, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (driverParam != null) {
                        ExcelHelper.setCellData(workbook, cell, driverParam.get(name), name, styleMap, fontMap);
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 8, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
                    mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 7, 8);

                    // 4th Row
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(fieldName);

                    name = "name";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    name = "target";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    name = "descriptorFileName";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("datasource");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("jdbcDriverParams");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("properties");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 7, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("name");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 8, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("value");

                    // add additional rows if properties are exist
                    if (driverParam != null && driverParam.get("properties") != null) {
                        JsonNode properties = driverParam.get("properties");
                        Iterator<JsonNode> iter = properties.elements();
                        while (iter.hasNext()) {
                            JsonNode property = iter.next();

                            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(fieldName);

                            name = "name";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                            name = "target";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                            name = "descriptorFileName";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("datasource");

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("jdbcDriverParams");

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("properties");

                            name = "name";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 7, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, property.get(name), name, styleMap, fontMap);

                            name = "value";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 8, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, property.get(name), name, styleMap, fontMap);
                        }

                        mergeRegion(dataSheet, row.getRowNum() - properties.size(), row.getRowNum(), 6, 6);
                    }

                    // 5th ~ 11st Row
                    for (int i = 5; i <= 11; i++) {
                        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                        cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue(fieldName);

                        name = "name";
                        cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                        name = "target";
                        cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                        name = "descriptorFileName";
                        cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                        if (i < 10) {
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("jdbcConnectionPoolParams");
                        } else {
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("jdbcDataSourceParams");
                        }

                        if (i == 5) {
                            name = "initialCapacity";
                        } else if (i == 6) {
                            name = "maxCapacity";
                        } else if (i == 7) {
                            name = "testConnectionsOnReserve";
                        } else if (i == 8) {
                            name = "testTableName";
                        } else if (i == 9) {
                            name = "secondsToTrustAnIdlePoolConnection";
                        } else if (i == 10) {
                            name = "jndiName";
                        } else if (i == 11) {
                            name = "globalTransactionProtocol";
                        }

                        cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue(name);

                        cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        if (i < 10) {
                            if (poolParam != null) {
                                ExcelHelper.setCellData(workbook, cell, poolParam.get(name), name, styleMap, fontMap);
                            }
                        } else {
                            if (sourceParam != null) {
                                ExcelHelper.setCellData(workbook, cell, sourceParam.get(name), name, styleMap, fontMap);
                            }
                        }

                        cell = ExcelHelper.createCellWithBorder(workbook, row, 7, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
                        cell = ExcelHelper.createCellWithBorder(workbook, row, 8, HorizontalAlignment.CENTER, BorderStyle.THIN, false, styleMap);
                        mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 6, 8);
                    }

                    mergeRegion(dataSheet, nameStartRow, row.getRowNum(), 1, 1);
                    mergeRegion(dataSheet, nameStartRow, row.getRowNum(), 2, 2);
                    mergeRegion(dataSheet, nameStartRow, row.getRowNum(), 3, 3);
                    mergeRegion(dataSheet, nameStartRow - 1, row.getRowNum() - 7, 4, 4);
                    mergeRegion(dataSheet, row.getRowNum() - 6, row.getRowNum() - 2, 4, 4);
                    mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 4, 4);
                    mergeRegion(dataSheet, nameStartRow, row.getRowNum() - 7, 5, 5);
                }

                mergeRegion(dataSheet, jdbcStartRow, row.getRowNum(), 0, 0);
            } else if ("jms".equals(fieldName) && !subNode.isEmpty()) {
                Iterator<String> jmsFieldNames = subNode.fieldNames();
                jmsStartRow = dataSheet.getLastRowNum() + 1;
                String jmsFieldName;
                while (jmsFieldNames.hasNext()) {
                    jmsFieldName = jmsFieldNames.next();
                    JsonNode jmsNode = subNode.get(jmsFieldName);

                    if ("jmsSystemResource".equals(jmsFieldName)) {
                        Iterator<JsonNode> iter = jmsNode.elements();
                        nameStartRow = dataSheet.getLastRowNum() + 1;
                        while (iter.hasNext()) {
                            JsonNode n = iter.next();

                            JsonNode deployment = null;
                            if (jmsNode != null) {
                                deployment = n.get("subDeployment");
                            }

                            // 1st Row
                            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(fieldName);

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(jmsFieldName);

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("name");

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("descriptorFileName");

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("target");

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("subDeployment");

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("name");

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 7, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("target");

                            // 2nd Row
                            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(fieldName);

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(jmsFieldName);

                            name = "name";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                            name = "descriptorFileName";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                            name = "target";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue("subDeployment");

                            if (deployment == null || deployment.isNull()) {
                                cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                cell = ExcelHelper.createCellWithBorder(workbook, row, 7, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 6, 7);
                                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 5, 5);
                            } else {
                                Iterator<JsonNode> nodes = deployment.elements();
                                int idx = 0;
                                while (nodes.hasNext()) {
                                    JsonNode d = nodes.next();

                                    if (idx++ > 0) {
                                        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                                    }

                                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                                    cell.setCellValue(fieldName);

                                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                                    cell.setCellValue(jmsFieldName);

                                    name = "name";
                                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                    ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                                    name = "descriptorFileName";
                                    cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                    ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                                    name = "target";
                                    cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                    ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                                    cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                                    cell.setCellValue("subDeployment");

                                    name = "name";
                                    cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                    ExcelHelper.setCellData(workbook, cell, d.get(name), name, styleMap, fontMap);

                                    name = "target";
                                    cell = ExcelHelper.createCellWithBorder(workbook, row, 7, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                    ExcelHelper.setCellData(workbook, cell, d.get(name), name, styleMap, fontMap);
                                }

                                mergeRegion(dataSheet, row.getRowNum() - deployment.size(), row.getRowNum(), 5, 5);
                            }

                            if (deployment != null && deployment.size() > 0) {
                                mergeRegion(dataSheet, row.getRowNum() - deployment.size() + 1, row.getRowNum(), 2, 2);
                                mergeRegion(dataSheet, row.getRowNum() - deployment.size() + 1, row.getRowNum(), 3, 3);
                                mergeRegion(dataSheet, row.getRowNum() - deployment.size() + 1, row.getRowNum(), 4, 4);
                            }
                        }

                        mergeRegion(dataSheet, nameStartRow, row.getRowNum(), 1, 1);
                    } else if ("jmsServer".equals(jmsFieldName)) {
                        // 1st Row
                        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                        nameStartRow = dataSheet.getLastRowNum() + 1;
                        cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue(fieldName);

                        cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue(jmsFieldName);

                        cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue("persistentStore");

                        cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue("name");

                        cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue("target");

                        cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue("hostingTemporaryDestinations");

                        Iterator<JsonNode> iter = jmsNode.elements();
                        while (iter.hasNext()) {
                            JsonNode n = iter.next();

                            // data Row
                            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(fieldName);

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(jmsFieldName);

                            name = "persistentStore";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                            name = "name";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                            name = "target";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                            name = "hostingTemporaryDestinations";
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);
                        }

                        mergeRegion(dataSheet, row.getRowNum() - jmsNode.size(), row.getRowNum(), 1, 1);
                    }
                }

                mergeRegion(dataSheet, jmsStartRow, row.getRowNum(), 0, 0);
            }
        }
    }

    /**
     * Make servers sheet for Jeus not more than version 6.0
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeJeus6ServersSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        // 1st Row
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("name");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("nodeName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("status");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("container");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, 0, 0, cell.getColumnIndex() - 5, cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("HTTP Listener");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, 0, 0, cell.getColumnIndex() - 2, cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("WebToB Listener");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, 0, 0, cell.getColumnIndex() - 3, cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("logs");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, 0, 0, cell.getColumnIndex() - 3, cell.getColumnIndex());

        // 2nd Row
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("name");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("type");

//        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
//        cell.setCellValue("option");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("minHeap");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("maxHeap");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("runUser");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("vmOption");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("httpListener");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("serverListener");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("port");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("name");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("registrationId");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("ipAddress");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("port");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("level");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("name");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("fileName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("rotationDir");

        Iterator<JsonNode> elements = node.elements();

        String name;
        int nodeStartRow = 0;
        int nodeLastRow = 0;
        while (elements.hasNext()) {
            JsonNode n = elements.next();

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            name = "name";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "nodeName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "status";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            List<JsonNode> engineContainerList = new ArrayList<>();
            List<JsonNode> httpListenerList = new ArrayList<>();
            List<JsonNode> webtobListenerList = new ArrayList<>();

            JsonNode engines = n.get("engines");

            if (engines != null && engines.size() > 0) {
                JsonNode engineContainer = engines.get("engineContainer");

                Iterator<JsonNode> iter = engineContainer.elements();
                while (iter.hasNext()) {
                    engineContainerList.add(iter.next());
                }

                JsonNode webEngine = engines.get("webEngine");

                iter = webEngine.elements();
                while (iter.hasNext()) {
                    JsonNode webConnections = iter.next().get("webConnections");

                    if (webConnections != null) {
                        JsonNode httpListeners = webConnections.get("httpListener");

                        Iterator<JsonNode> it = httpListeners.iterator();
                        while (it.hasNext()) {
                            httpListenerList.add(it.next());
                        }

                        JsonNode webtobListeners = webConnections.get("webtobListener");

                        it = webtobListeners.iterator();
                        while (it.hasNext()) {
                            webtobListenerList.add(it.next());
                        }
                    }
                }
            }

            int maxCnt = engineContainerList.size();
            if (httpListenerList.size() > maxCnt) {
                maxCnt = httpListenerList.size();
            }
            if (webtobListenerList.size() > maxCnt) {
                maxCnt = webtobListenerList.size();
            }

            nodeStartRow = row.getRowNum();
            nodeLastRow = row.getRowNum() + maxCnt - 1;

            for (int i = 0; i < maxCnt; i++) {
                JsonNode engineContainer = null;
                JsonNode httpListener = null;
                JsonNode webtobListener = null;

                if (engineContainerList.size() >= (i + 1)) {
                    engineContainer = engineContainerList.get(i);
                }
                if (httpListenerList.size() >= (i + 1)) {
                    httpListener = httpListenerList.get(i);
                }
                if (webtobListenerList.size() >= (i + 1)) {
                    webtobListener = webtobListenerList.get(i);
                }

                if (i > 0) {
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                    for (int j = 0; j < 3; j++) {
                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    }
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (engineContainer != null) {
                    name = "name";
                    ExcelHelper.setCellData(workbook, cell, engineContainer.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (engineContainer != null) {
                    JsonNode commands = engineContainer.get("engineCommands");
                    StringBuilder sb = new StringBuilder();
                    if (commands != null && commands.size() > 0) {
                        Iterator<JsonNode> command = commands.elements();
                        while (command.hasNext()) {
                            if (StringUtils.isNotEmpty(sb.toString())) {
                                sb.append(", ");
                            }
                            sb.append(command.next().get("type").asText());
                        }
                    }

                    cell.setCellValue(sb.toString());
                }

//                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
//                if (engineContainer != null) {
//                    name = "commandOption";
//                    ExcelHelper.setCellData(workbook, cell, engineContainer.get(name), name, styleMap, fontMap);
//                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (engineContainer != null) {
                    name = "minHeap";
                    ExcelHelper.setCellData(workbook, cell, engineContainer.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (engineContainer != null) {
                    name = "maxHeap";
                    ExcelHelper.setCellData(workbook, cell, engineContainer.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (engineContainer != null) {
                    name = "runUser";
                    ExcelHelper.setCellData(workbook, cell, engineContainer.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (engineContainer != null) {
                    name = "vmOption";
                    ExcelHelper.setCellData(workbook, cell, engineContainer.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (httpListener != null) {
                    name = "listenerId";
                    ExcelHelper.setCellData(workbook, cell, httpListener.get(name), name, styleMap, fontMap);
                }
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (httpListener != null) {
                    name = "port";
                    ExcelHelper.setCellData(workbook, cell, httpListener.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobListener != null) {
                    name = "name";
                    ExcelHelper.setCellData(workbook, cell, webtobListener.get(name), name, styleMap, fontMap);
                }
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobListener != null) {
                    name = "listenerId";
                    ExcelHelper.setCellData(workbook, cell, webtobListener.get(name), name, styleMap, fontMap);
                }
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobListener != null) {
                    name = "webtobAddress";
                    ExcelHelper.setCellData(workbook, cell, webtobListener.get(name), name, styleMap, fontMap);
                }
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobListener != null) {
                    name = "port";
                    ExcelHelper.setCellData(workbook, cell, webtobListener.get(name), name, styleMap, fontMap);
                }

                JsonNode logNode = n.get("logs");

                if (logNode != null && logNode.size() > 0) {
                    // TODO 첫 번째 system-logging만 Excel에 포함한다. (추후 전체 포함)
                    if (logNode.isArray()) {
                        logNode = logNode.get(0);
                    }

                    JsonNode handler = logNode.get("handler");
                    if (handler != null) {
                        handler = handler.get("fileHandler");
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    name = "level";
                    ExcelHelper.setCellData(workbook, cell, logNode.get(name), name, styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    name = "name";
                    ExcelHelper.setCellData(workbook, cell, logNode.get(name), name, styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (handler != null) {
                        name = "fileName";
                        ExcelHelper.setCellData(workbook, cell, handler.get(name), name, styleMap, fontMap);
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (handler != null) {
                        name = "rotationDir";
                        ExcelHelper.setCellData(workbook, cell, handler.get(name), name, styleMap, fontMap);
                    }
                } else {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                }
            }

            if (nodeLastRow > nodeStartRow) {
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 0, 0);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 1, 1);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 2, 2);

                if (engineContainerList.size() < 2) {
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 3, 3);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 4, 4);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 5, 5);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 6, 6);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 7, 7);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 8, 8);
                }

                if (httpListenerList.size() < 2) {
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 9, 9);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 10, 10);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 11, 11);
                }

                if (webtobListenerList.size() < 2) {
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 12, 12);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 13, 13);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 14, 14);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 15, 15);
                }

                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 16, 16);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 17, 17);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 18, 18);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 19, 19);
            }
        }
    }

    /**
     * Make servers sheet for Jeus more than version 7.0
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeJeus7ServersSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        // 1st Row
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("name");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("nodeName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("status");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("HTTP Listener");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, 0, 0, cell.getColumnIndex() - 2, cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("WebToB Listener");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, 0, 0, cell.getColumnIndex() - 3, cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("WebToB Connector");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, 0, 0, cell.getColumnIndex() - 3, cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("sessionConfig");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, 0, 0, cell.getColumnIndex() - 2, cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("logs");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, 0, 0, cell.getColumnIndex() - 3, cell.getColumnIndex());

//        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
//        cell.setCellValue("jvmOption");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("useEjbEngine");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("useJmsEngine");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("useWebEngine");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("minHeap");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("maxHeap");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("runUser");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("vmOption");

        // 2nd Row
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());
        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("httpListener");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("serverListener");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("port");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("name");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("listenerId");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("webtobAddress");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("port");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("name");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("registrationId");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("ipAddress");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("port");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("trackingMode");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("timeout");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("cookieName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("level");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("name");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("fileName");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("rotationDir");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

        Iterator<JsonNode> elements = node.elements();

        String name;
        int nodeStartRow = 0;
        int nodeLastRow = 0;
        while (elements.hasNext()) {
            JsonNode n = elements.next();

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            name = "name";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "nodeName";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "status";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            List<JsonNode> listenerList = new ArrayList<>();
            List<JsonNode> httpListenerList = new ArrayList<>();
            List<JsonNode> webtobListenerList = new ArrayList<>();
            List<JsonNode> webtobConnectorList = new ArrayList<>();
            List<JsonNode> sessionConfigList = new ArrayList<>();

            JsonNode listeners = n.get("listeners");

            if (listeners != null && listeners.size() > 0) {
                listeners = listeners.get("listeners");

                Iterator<JsonNode> iter = listeners.elements();
                while (iter.hasNext()) {
                    listenerList.add(iter.next());
                }
            }

            JsonNode engines = n.get("engines");

            if (engines != null && engines.size() > 0) {
                JsonNode webEngine = engines.get("webEngine");

                Iterator<JsonNode> iter = webEngine.elements();
                while (iter.hasNext()) {
                    JsonNode we = iter.next();

                    JsonNode webConnections = we.get("webConnections");

                    sessionConfigList.add(we.get("sessionConfig"));

                    if (webConnections != null) {
                        JsonNode httpListeners = webConnections.get("httpListener");

                        Iterator<JsonNode> it = httpListeners.iterator();
                        while (it.hasNext()) {
                            httpListenerList.add(it.next());
                        }

                        JsonNode webtobListeners = webConnections.get("webtobListener");

                        it = webtobListeners.iterator();
                        while (it.hasNext()) {
                            webtobListenerList.add(it.next());
                        }

                        JsonNode webtobConnectors = webConnections.get("webToBConnector");

                        it = webtobConnectors.iterator();
                        while (it.hasNext()) {
                            webtobConnectorList.add(it.next());
                        }
                    }
                }
            }

            int maxCnt = httpListenerList.size();
            if (webtobListenerList.size() > maxCnt) {
                maxCnt = webtobListenerList.size();
            }
            if (webtobConnectorList.size() > maxCnt) {
                maxCnt = webtobConnectorList.size();
            }
            if (sessionConfigList.size() > maxCnt) {
                maxCnt = sessionConfigList.size();
            }

            nodeStartRow = row.getRowNum();
            nodeLastRow = row.getRowNum() + maxCnt - 1;

            for (int i = 0; i < maxCnt; i++) {
                JsonNode httpListener = null;
                JsonNode webtobListener = null;
                JsonNode webtobConnector = null;
                JsonNode sessionConfig = null;

                if (httpListenerList.size() >= (i + 1)) {
                    httpListener = httpListenerList.get(i);
                }
                if (webtobListenerList.size() >= (i + 1)) {
                    webtobListener = webtobListenerList.get(i);
                }
                if (webtobConnectorList.size() >= (i + 1)) {
                    webtobConnector = webtobConnectorList.get(i);
                }
                if (sessionConfigList.size() >= (i + 1)) {
                    sessionConfig = sessionConfigList.get(i);
                }

                if (i > 0) {
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                    for (int j = 0; j < 3; j++) {
                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    }
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (httpListener != null) {
                    name = "name";
                    ExcelHelper.setCellData(workbook, cell, httpListener.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (httpListener != null) {
                    name = "serverListenerRef";
                    ExcelHelper.setCellData(workbook, cell, httpListener.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (httpListener != null) {
                    String ref = httpListener.get("serverListenerRef").asText();
                    for (JsonNode listener : listenerList) {
                        if (ref.equals(listener.get("name").asText())) {
                            name = "listenPort";
                            ExcelHelper.setCellData(workbook, cell, listener.get(name), name, styleMap, fontMap);
                        }
                    }
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobListener != null) {
                    name = "name";
                    ExcelHelper.setCellData(workbook, cell, webtobListener.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobListener != null) {
                    name = "listenerId";
                    ExcelHelper.setCellData(workbook, cell, webtobListener.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobListener != null) {
                    name = "webtobAddress";
                    ExcelHelper.setCellData(workbook, cell, webtobListener.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobListener != null) {
                    name = "port";
                    ExcelHelper.setCellData(workbook, cell, webtobListener.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobConnector != null) {
                    name = "name";
                    ExcelHelper.setCellData(workbook, cell, webtobConnector.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobConnector != null) {
                    name = "registrationId";
                    ExcelHelper.setCellData(workbook, cell, webtobConnector.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobConnector != null) {
                    JsonNode networkAddress = webtobConnector.get("networkAddress");
                    name = "ipAddress";
                    ExcelHelper.setCellData(workbook, cell, networkAddress.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (webtobConnector != null) {
                    JsonNode networkAddress = webtobConnector.get("networkAddress");
                    name = "port";
                    ExcelHelper.setCellData(workbook, cell, networkAddress.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (sessionConfig != null) {
                    JsonNode trackingMode = sessionConfig.get("trackingMode");

                    String mode = null;
                    if ("true".equals(trackingMode.get("url").asText())) {
                        mode = "url";
                    }
                    if ("true".equals(trackingMode.get("ssl").asText())) {
                        mode = "ssl";
                    }
                    if ("true".equals(trackingMode.get("cookie").asText())) {
                        mode = "cookie";
                    }

                    cell.setCellValue(mode);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (sessionConfig != null) {
                    name = "timeout";
                    ExcelHelper.setCellData(workbook, cell, sessionConfig.get(name), name, styleMap, fontMap);
                }

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (sessionConfig != null) {
                    JsonNode sessionCookie = sessionConfig.get("sessionCookie");

                    if (sessionCookie != null) {
                        name = "cookieName";
                        ExcelHelper.setCellData(workbook, cell, sessionCookie.get(name), name, styleMap, fontMap);
                    }
                }

                JsonNode logNode = n.get("logs");

                if (logNode != null && logNode.size() > 0) {
                    // TODO 첫 번째 system-logging만 Excel에 포함한다. (추후 전체 포함)
                    if (logNode.isArray()) {
                        logNode = logNode.get(0);
                    }

                    JsonNode handler = logNode.get("handler");
                    if (handler != null) {
                        handler = handler.get("fileHandler");
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    name = "level";
                    ExcelHelper.setCellData(workbook, cell, logNode.get(name), name, styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    name = "name";
                    ExcelHelper.setCellData(workbook, cell, logNode.get(name), name, styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (handler != null) {
                        name = "fileName";
                        ExcelHelper.setCellData(workbook, cell, handler.get(name), name, styleMap, fontMap);
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (handler != null) {
                        name = "rotationDir";
                        ExcelHelper.setCellData(workbook, cell, handler.get(name), name, styleMap, fontMap);
                    }
                } else {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                }

//                name = "jvmOption";
//                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
//                if (n.get("jvmConfig") != null) {
//                    ExcelHelper.setCellData(workbook, cell, n.get("jvmConfig").get(name), name, styleMap, fontMap);
//                }

                name = "useEjbEngine";
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                name = "useJmsEngine";
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                name = "useWebEngine";
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

                name = "minHeap";
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (n.get("minHeap") != null) {
                    ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);
                }

                name = "maxHeap";
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (n.get("maxHeap") != null) {
                    ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);
                }

                name = "runUser";
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (n.get("runUser") != null) {
                    ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);
                }

                name = "vmOption";
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                if (n.get("vmOption") != null) {
                    ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);
                }
            }

            if (nodeLastRow > nodeStartRow) {
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 0, 0);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 1, 1);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 2, 2);

                if (httpListenerList.size() < 2) {
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 3, 3);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 4, 4);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 5, 5);
                }

                if (webtobListenerList.size() < 2) {
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 6, 6);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 7, 7);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 8, 8);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 9, 9);
                }

                if (webtobConnectorList.size() < 2) {
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 10, 10);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 11, 11);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 12, 12);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 13, 13);
                }

                if (sessionConfigList.size() < 2) {
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 14, 14);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 15, 15);
                    mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 16, 16);
                }

                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 17, 17);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 18, 18);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 19, 19);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 20, 20);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 21, 21);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 22, 22);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 23, 23);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 24, 24);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 25, 25);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 26, 26);
                mergeRegion(dataSheet, nodeStartRow, nodeLastRow, 27, 27);

            }
        }
    }

    /**
     * Make applications sheet for Jeus.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeJeusApplicationssSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        SXSSFRow firstRow = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, firstRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("id");

        cell = ExcelHelper.createCellWithBorder(workbook, firstRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("type");

        cell = ExcelHelper.createCellWithBorder(workbook, firstRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("target");

        cell = ExcelHelper.createCellWithBorder(workbook, firstRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("sourcePath");

        cell = ExcelHelper.createCellWithBorder(workbook, firstRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("deployedDate");

        cell = ExcelHelper.createCellWithBorder(workbook, firstRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("contextRoot");

        Iterator<JsonNode> elements = node.elements();

        int idx = 1;
        String name;
        while (elements.hasNext()) {
            JsonNode n = elements.next();

            if (idx++ == 1) {
                JsonNode options = n.get("options");

                if (options != null && !options.isEmpty()) {
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("id");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("type");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("target");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("sourcePath");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("deployedDate");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("contextRoot");

                    Iterator<String> optionFieldNames = options.fieldNames();
                    while (optionFieldNames.hasNext()) {
                        cell = ExcelHelper.createCellWithBorder(workbook, firstRow, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue("options");

                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue(optionFieldNames.next());
                    }

                    mergeRegion(dataSheet, 0, 1, 0, 0);
                    mergeRegion(dataSheet, 0, 1, 1, 1);
                    mergeRegion(dataSheet, 0, 1, 2, 2);
                    mergeRegion(dataSheet, 0, 1, 3, 3);
                    mergeRegion(dataSheet, 0, 1, 4, 4);
                    mergeRegion(dataSheet, 0, 1, 5, 5);
                    mergeRegion(dataSheet, 0, 0, 6, cell.getColumnIndex());
                }
            }

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            name = "id";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "type";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "target";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "sourcePath";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "deployedDate";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            name = "contextRoot";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get(name), name, styleMap, fontMap);

            JsonNode options = n.get("options");

            if (options != null && !options.isEmpty()) {
                Iterator<String> optionFieldNames = options.fieldNames();
                while (optionFieldNames.hasNext()) {
                    name = optionFieldNames.next();
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, options.get(name), name, styleMap, fontMap);
                }
            }
        }
    }

    /**
     * Make clusters sheet for Jeus.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeClustersSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        Iterator<String> fieldNames = node.fieldNames();

        int sessionStartRow = -1, clusterStartRow = -1, propertiesStartRow = -1, nameStartRow = 0;
        String name;
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode subNode = node.get(fieldName);

            if ("sessionServers".equals(fieldName) && !subNode.isEmpty()) {
                sessionStartRow = dataSheet.getLastRowNum() + 1;

                // header Row
                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(fieldName);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("type");

                cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("replicatedServer");

                cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("recoveryMode");

                cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("connectTimout");

                cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("readTimeout");

                cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("backupTrigger");

                Iterator<JsonNode> elements = subNode.elements();
                while (elements.hasNext()) {
                    JsonNode child = elements.next();

                    // data Row
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(fieldName);

                    name = "type";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    name = "replicatedServer";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    name = "recoveryMode";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    name = "connectTimout";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    name = "readTimeout";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    name = "backupTrigger";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);
                }

                mergeRegion(dataSheet, sessionStartRow, row.getRowNum(), 0, 0);
            } else if ("cluster".equals(fieldName)) {
                clusterStartRow = dataSheet.getLastRowNum() + 1;

                // header 1st Row
                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(fieldName);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("name");

                cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("status");

                cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("servers");

                for (int i = 4; i <= 11; i++) {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, i, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("sessionRouterConfig");
                }
                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 4, cell.getColumnIndex());

                cell = ExcelHelper.createCellWithBorder(workbook, row, 12, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("options");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 13, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("options");

                // header 2nd Row
                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(fieldName);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("name");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("status");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("servers");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("backupLevel");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("connectTimout");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("failoverDelay");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 7, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("readTimeout");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 8, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("reservedThreadNum");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 9, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("restartDelay");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 10, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("allowFailBack");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 11, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("fileDB");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 12, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("options");
                cell = ExcelHelper.createCellWithBorder(workbook, row, 13, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("options");
                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 12, 13);

                for (int i = 1; i <= 3; i++) {
                    mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), i, i);
                }

                Iterator<JsonNode> elements = subNode.elements();
                while (elements.hasNext()) {
                    JsonNode child = elements.next();

                    // data Row
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    nameStartRow = row.getRowNum();
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(fieldName);
                    name = "name";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);
                    name = "status";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, child.get(name), name, styleMap, fontMap);

                    Iterator<JsonNode> servers = child.get("servers").elements();
                    StringBuilder sb = null;
                    while (servers.hasNext()) {
                        JsonNode server = servers.next();

                        if (server.isValueNode()) {
                            if (sb == null) {
                                sb = new StringBuilder();
                            } else {
                                sb.append("\n");
                            }

                            sb.append(server.textValue());
                        }
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 3, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (sb != null) {
                        cell.setCellValue(sb.toString());
                    }

                    JsonNode sessionRouterConfig = child.get("sessionRouterConfig");
                    name = "backupLevel";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 4, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (sessionRouterConfig != null) {
                        ExcelHelper.setCellData(workbook, cell, sessionRouterConfig.get(name), name, styleMap, fontMap);
                    }
                    name = "connectTimout";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 5, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (sessionRouterConfig != null) {
                        ExcelHelper.setCellData(workbook, cell, sessionRouterConfig.get(name), name, styleMap, fontMap);
                    }
                    name = "failoverDelay";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 6, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (sessionRouterConfig != null) {
                        ExcelHelper.setCellData(workbook, cell, sessionRouterConfig.get(name), name, styleMap, fontMap);
                    }
                    name = "readTimeout";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 7, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (sessionRouterConfig != null) {
                        ExcelHelper.setCellData(workbook, cell, sessionRouterConfig.get(name), name, styleMap, fontMap);
                    }
                    name = "reservedThreadNum";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 8, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (sessionRouterConfig != null) {
                        ExcelHelper.setCellData(workbook, cell, sessionRouterConfig.get(name), name, styleMap, fontMap);
                    }
                    name = "restartDelay";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 9, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (sessionRouterConfig != null) {
                        ExcelHelper.setCellData(workbook, cell, sessionRouterConfig.get(name), name, styleMap, fontMap);
                    }
                    name = "allowFailBack";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 10, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (sessionRouterConfig != null) {
                        ExcelHelper.setCellData(workbook, cell, sessionRouterConfig.get(name), name, styleMap, fontMap);
                    }
                    name = "fileDB";
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 11, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (sessionRouterConfig != null) {
                        ExcelHelper.setCellData(workbook, cell, sessionRouterConfig.get(name), name, styleMap, fontMap);
                    }

                    JsonNode options = child.get("options");
                    if (options != null && options.size() > 0) {
                        Iterator<String> optionNames = options.fieldNames();

                        int idx = 0;
                        while (optionNames.hasNext()) {
                            String optionName = optionNames.next();
                            // JsonNode option = options.get(optionName);

                            if (idx++ > 0) {
                                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                                for (int i = 0; i <= 11; i++) {
                                    cell = ExcelHelper.createCellWithBorder(workbook, row, i, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                }
                            }

                            cell = ExcelHelper.createCellWithBorder(workbook, row, 12, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                            cell.setCellValue(optionName);
                            cell = ExcelHelper.createCellWithBorder(workbook, row, 13, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                            ExcelHelper.setCellData(workbook, cell, options.get(optionName), optionName, styleMap, fontMap);
                        }

                        for (int i = 1; i <= 11; i++) {
                            mergeRegion(dataSheet, nameStartRow, row.getRowNum(), i, i);
                        }
                    } else {
                        cell = ExcelHelper.createCellWithBorder(workbook, row, 12, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        cell = ExcelHelper.createCellWithBorder(workbook, row, 13, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 12, cell.getColumnIndex());
                    }
                }

                mergeRegion(dataSheet, clusterStartRow, row.getRowNum(), 0, 0);
            } else if ("properties".equals(fieldName) && !subNode.isEmpty()) {
                propertiesStartRow = dataSheet.getLastRowNum() + 1;
                Iterator<String> propertyNames = subNode.fieldNames();
                List<String> propertyNameList = new ArrayList<>();

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(fieldName);

                while (propertyNames.hasNext()) {
                    String key = propertyNames.next();
                    propertyNameList.add(key);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(key);
                }

                if (propertyNameList.size() > 0) {
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(fieldName);

                    for (String propertyName : propertyNameList) {
                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, subNode.get(propertyName), propertyName, styleMap, fontMap);
                    }

                    mergeRegion(dataSheet, propertiesStartRow, row.getRowNum(), 0, 0);
                }
            }
        }
    }

    /**
     * Make sessionClusterConfig sheet for Jeus.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeSessionClusterConfigSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        if (!node.isEmpty()) {
            JsonNode usingSessionClusterNode = node.get("usingSessionCluster");
            JsonNode clusterConfigNode = node.get("commonClusterConfig");
            JsonNode sessionClustersNode = node.get("sessionClusters");

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
            cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("usingSessionCluster");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, usingSessionClusterNode, "usingSessionCluster", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            mergeRegion(dataSheet, 0, 0, 1, 3);

            if (!clusterConfigNode.isEmpty()) {
                String name = "commonCluster";

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                int startRow = row.getRowNum();
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("jeusLoginManager");

                cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("primary");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("jeusLoginManager").get("primary"), "primary", styleMap, fontMap);

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("jeusLoginManager");
                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

                cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("secondary");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("jeusLoginManager").get("secondary"), "primary", styleMap, fontMap);

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("backupLevel");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("backupLevel"), "backupLevel", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("connectTimout");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("connectTimout"), "connectTimout", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("failoverDelay");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("failoverDelay"), "failoverDelay", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("readTimeout");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("readTimeout"), "readTimeout", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("reservedThreadNum");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("reservedThreadNum"), "reservedThreadNum", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("restartDelay");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("restartDelay"), "restartDelay", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("allowFailBack");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("allowFailBack"), "allowFailBack", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("fileDB");

                cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("minHole");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("fileDB").get("minHole"), "minHole", styleMap, fontMap);

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("fileDB");

                cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("packingRate");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("fileDB").get("packingRate"), "packingRate", styleMap, fontMap);

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(name);

                cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("fileDB");
                mergeRegion(dataSheet, row.getRowNum() - 2, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

                cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("passivationTimeout");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("fileDB").get("passivationTimeout"), "passivationTimeout", styleMap, fontMap);

                mergeRegion(dataSheet, startRow, row.getRowNum(), 0, 0);
            }

            if (!sessionClustersNode.isEmpty()) {
                sessionClustersNode = sessionClustersNode.get("sessionClusters");

                Iterator<JsonNode> elements = sessionClustersNode.elements();
                while (elements.hasNext()) {
                    JsonNode subNode = elements.next();

                    String name = subNode.get("name").asText();

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    int startRow = row.getRowNum();
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("jeusLoginManager");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("primary");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("jeusLoginManager").get("primary"), "primary", styleMap, fontMap);

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("jeusLoginManager");
                    mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("secondary");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("jeusLoginManager").get("secondary"), "primary", styleMap, fontMap);

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("backupLevel");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("backupLevel"), "backupLevel", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("connectTimout");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("connectTimout"), "connectTimout", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("failoverDelay");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("failoverDelay"), "failoverDelay", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("readTimeout");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("readTimeout"), "readTimeout", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("reservedThreadNum");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("reservedThreadNum"), "reservedThreadNum", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("restartDelay");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("restartDelay"), "restartDelay", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("allowFailBack");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("allowFailBack"), "allowFailBack", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - 1, cell.getColumnIndex());

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("fileDB");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("minHole");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("fileDB").get("minHole"), "minHole", styleMap, fontMap);

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("fileDB");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("packingRate");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("fileDB").get("packingRate"), "packingRate", styleMap, fontMap);

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(name);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 1, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("fileDB");
                    mergeRegion(dataSheet, row.getRowNum() - 2, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex());

                    cell = ExcelHelper.createCellWithBorder(workbook, row, 2, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("passivationTimeout");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, clusterConfigNode.get("fileDB").get("passivationTimeout"), "passivationTimeout", styleMap, fontMap);

                    mergeRegion(dataSheet, startRow, row.getRowNum(), 0, 0);
                }
            }
        }
    }

    /**
     * Make resources sheet for Jeus.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeResourcesSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        String[] header = new String[]{
                "", "vendor", "user", "autoCommit", "supportXaEmulation",
                "stmtQueryTimeout", "serverName", "portNumber", "poolDestroyTimeout", "password",
                "loginTimeout", "isolationLevel", "exportName", "description", "databaseName",
                "dataSourceType", "dataSourceTarget", "dataSourceId", "dataSourceClassName", "connectionPool",
                "", "", "property", "", ""
        };

        int idx = 0;
        Map<Integer, List<String>> headerMap = new HashMap<>();
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[19] = "waitFreeConnection";
        header[20] = "waitTime";
        header[21] = "v:waitTime";
        header[22] = "name";
        header[23] = "type";
        header[24] = "value";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[20] = "enableWait";
        header[21] = "v:enableWait";
        header[22] = "v:name";
        header[23] = "v:type";
        header[24] = "v:value";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[19] = "useSqlTrace";
        header[20] = "v:useSqlTrace";
        header[21] = "";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[19] = "stmtFetchSize";
        header[20] = "v:stmtFetchSize";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[19] = "stmtCachingSize";
        header[20] = "v:stmtCachingSize";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[19] = "pooling";
        header[20] = "step";
        header[21] = "v:step";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[20] = "period";
        header[21] = "v:period";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[20] = "min";
        header[21] = "v:min";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[20] = "max";
        header[21] = "v:max";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[19] = "maxUseCount";
        header[20] = "v:maxUseCount";
        header[21] = "";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[19] = "keepConnectionHandleOpen";
        header[20] = "v:keepConnectionHandleOpen";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[19] = "dbaTimeout";
        header[20] = "v:dbaTimeout";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[19] = "connectionTrace";
        header[20] = "getConnectionTrace";
        header[21] = "v:getConnectionTrace";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[20] = "enabled";
        header[21] = "v:enabled";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        header[20] = "autoCommitTrace";
        header[21] = "v:autoCommitTrace";
        headerMap.put(idx++, new ArrayList<>(Arrays.asList(header)));

        Iterator<String> fieldNames = node.fieldNames();

        int databaseStartRow = -1, vendorStartRow = 0;
        String name;
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode subNode = node.get(fieldName);

            if ("databases".equals(fieldName)) {
                Iterator<JsonNode> elements = subNode.elements();
                databaseStartRow = dataSheet.getLastRowNum() + 1;
                while (elements.hasNext()) {
                    JsonNode child = elements.next();
                    JsonNode connectionPool = null;
                    JsonNode property = null;
                    JsonNode waitFreeConnection = null;
                    JsonNode pooling = null;
                    JsonNode connectionTrace = null;

                    if (child != null) {
                        connectionPool = child.get("connectionPool");
                        property = child.get("property");
                    }

                    if (connectionPool != null) {
                        waitFreeConnection = connectionPool.get("waitFreeConnection");
                        pooling = connectionPool.get("pooling");
                        connectionTrace = connectionPool.get("connectionTrace");
                    }

                    for (int i = 0; i < headerMap.size(); i++) {
                        List<String> headerList = headerMap.get(i);

                        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                        cell = ExcelHelper.createCellWithBorder(workbook, row, 0, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue(fieldName);

                        for (int j = 0; j < headerList.size(); j++) {
                            String h = headerList.get(j);

                            if (j == 0) {
                                cell = ExcelHelper.createCellWithBorder(workbook, row, j, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                                cell.setCellValue(fieldName);
                                continue;
                            }

                            if (i == 0) {
                                cell = ExcelHelper.createCellWithBorder(workbook, row, j, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                                cell.setCellValue(h);
                                vendorStartRow = row.getRowNum() + 1;
                            } else {
                                if (j <= 18) {
                                    cell = ExcelHelper.createCellWithBorder(workbook, row, j, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                    ExcelHelper.setCellData(workbook, cell, child.get(h), h, styleMap, fontMap);
                                } else {
                                    if (h.startsWith("v:")) {
                                        h = h.substring(2);
                                        cell = ExcelHelper.createCellWithBorder(workbook, row, j, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);

                                        if (j < 22) {
                                            if (i <= 2) {
                                                if (waitFreeConnection != null) {
                                                    ExcelHelper.setCellData(workbook, cell, waitFreeConnection.get(h), h, styleMap, fontMap);
                                                }
                                            } else if ((i >= 3 && i <= 5) || (i >= 10 && i <= 12)) {
                                                if (connectionPool != null) {
                                                    ExcelHelper.setCellData(workbook, cell, connectionPool.get(h), h, styleMap, fontMap);
                                                }
                                            } else if (i >= 6 && i <= 9) {
                                                if (pooling != null) {
                                                    ExcelHelper.setCellData(workbook, cell, pooling.get(h), h, styleMap, fontMap);
                                                }
                                            } else if (i >= 13 && i <= 15) {
                                                if (connectionTrace != null) {
                                                    ExcelHelper.setCellData(workbook, cell, connectionTrace.get(h), h, styleMap, fontMap);
                                                }
                                            }
                                        } else {
                                            int k = i - 2;
                                            if (property == null || property.isNull() || property.get(k) == null) {
                                                ExcelHelper.createCellWithBorder(workbook, row, 22, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                                ExcelHelper.createCellWithBorder(workbook, row, 23, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                                ExcelHelper.createCellWithBorder(workbook, row, 24, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                            } else {
                                                JsonNode p = property.get(k);
                                                ExcelHelper.setCellData(workbook, cell, p.get(h), h, styleMap, fontMap);
                                            }
                                        }
                                    } else if (!"".equals(h)) {
                                        cell = ExcelHelper.createCellWithBorder(workbook, row, j, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                                        cell.setCellValue(h);
                                    } else {
                                        cell = ExcelHelper.createCellWithBorder(workbook, row, j, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                                    }
                                }
                            }
                        }

                        if (i == 0) {
                            mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 19, 21);
                            mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 22, 24);
                        } else if (i == 2) {
                            mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 19, 19);
                        } else if (i == 9) {
                            mergeRegion(dataSheet, row.getRowNum() - 3, row.getRowNum(), 19, 19);
                        } else if (i == 15) {
                            mergeRegion(dataSheet, row.getRowNum() - 2, row.getRowNum(), 19, 19);

                            for (int j = 1; j <= 18; j++) {
                                mergeRegion(dataSheet, vendorStartRow, row.getRowNum(), j, j);
                            }
                        }

                    }
                }

                mergeRegion(dataSheet, databaseStartRow, row.getRowNum(), 0, 0);
            }
        }
    }

    /**
     * Make applications sheet for Tomcat.
     *
     * @param dataSheet the data sheet
     * @param node      the node
     */
    public void makeApplicationsSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        Iterator<JsonNode> serviceNodes = node.elements();

        while (serviceNodes.hasNext()) {
            JsonNode serviceNode = serviceNodes.next();
            String serviceName = serviceNode.get("serviceName").textValue();

            JsonNode webapps = serviceNode.get("webapps");
            JsonNode context = serviceNode.get("context");

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            int serviceStartRow = row.getRowNum();
            int webappsStartRow = row.getRowNum();

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue(serviceName);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("webapps");

            String name = "unpackWARs";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue(name);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            if (webapps != null && !webapps.isNull()) {
                ExcelHelper.setCellData(workbook, cell, webapps.get(name), name, styleMap, fontMap);
            }

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue(serviceName);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("webapps");

            name = "autoDeploy";
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("autoDeploy");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            if (webapps != null && !webapps.isNull()) {
                ExcelHelper.setCellData(workbook, cell, webapps.get(name), name, styleMap, fontMap);
            }

            if (webapps != null && !webapps.isNull()) {
                JsonNode apps = webapps.get("apps");

                int appsStartRow = row.getRowNum();
                Iterator<JsonNode> iter = apps.elements();
                while (iter.hasNext()) {
                    JsonNode n = iter.next();

                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(serviceName);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("webapps");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("apps");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, n, "apps", styleMap, fontMap);
                }
                if (!apps.isEmpty()) {
                    mergeRegion(dataSheet, appsStartRow + 1, row.getRowNum(), 2, 2);
                }
            }

            mergeRegion(dataSheet, webappsStartRow, row.getRowNum(), 1, 1);

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue(serviceName);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("context");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("path");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("docBase");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("ResourceLink");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("ResourceLink");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("ResourceLink");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("reloadable");

            mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 4, 6);

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue(serviceName);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("context");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("path");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("docBase");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("name");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("global");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("type");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("reloadable");

            mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 2, 2);
            mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 3, 3);
            mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 7, 7);

            int contextStartRow = row.getRowNum() - 1; //6

            for (int i = 0; i < context.size(); i++) {
                JsonNode c = context.get(i);
                JsonNode link = c.get("ResourceLink");

                if (link != null && link.size() > 0) {
                    Iterator<JsonNode> iter = link.elements();
                    while (iter.hasNext()) {
                        JsonNode n = iter.next();

                        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue(serviceName);

                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                        cell.setCellValue("context");

                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, c.get("path"), "path", styleMap, fontMap);

                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, c.get("docBase"), "docBase", styleMap, fontMap);

                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, n.get("name"), "name", styleMap, fontMap);
                        dataSheet.autoSizeColumn(cell.getColumnIndex(), true);

                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, n.get("global"), "global", styleMap, fontMap);
                        dataSheet.autoSizeColumn(cell.getColumnIndex(), true);

                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, n.get("type"), "type", styleMap, fontMap);
                        dataSheet.autoSizeColumn(cell.getColumnIndex(), true);

                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        ExcelHelper.setCellData(workbook, cell, c.get("reloadable"), "reloadable", styleMap, fontMap);
                        dataSheet.setColumnWidth(cell.getColumnIndex(), 3000);
                    }

                    mergeRegion(dataSheet, contextStartRow + 2, row.getRowNum(), 2, 2);
                    mergeRegion(dataSheet, contextStartRow + 2, row.getRowNum(), 3, 3);
                    mergeRegion(dataSheet, contextStartRow + 2, row.getRowNum(), 7, 7);
                } else {
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue(serviceName);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("context");

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, c.get("path"), "path", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, c.get("docBase"), "docBase", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, c.get("reloadable"), "reloadable", styleMap, fontMap);
                }
            }
            mergeRegion(dataSheet, contextStartRow, row.getRowNum(), 1, 1);

            mergeRegion(dataSheet, serviceStartRow, row.getRowNum(), 0, 0);
        }
    }

    public void makeJbossInstancessSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("name");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("svrGroupName");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("configPath");

            if (i == 0) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("svrConnectors");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("svrConnectors");

                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 3, 4);
            } else if (i == 1) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("name");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("port");
            }

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("ipAddress");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("minHeap");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("maxHeap");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("maxPermSize");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("javaVersion");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("runUser");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("isRunning");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("portOffset");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("jvmOptions");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("profileName");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("socketBindName");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("runTimeOptions");

            if (i == 1) {
                for (int j = 0; j < 17; j++) {
                    if (j != 3 && j != 4) {
                        mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), j, j);
                    }
                }
            }
        }

        Iterator<JsonNode> iter = node.elements();
        while (iter.hasNext()) {
            JsonNode n = iter.next();

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("name"), "name", styleMap, fontMap);
            //dataSheet.autoSizeColumn(cell.getColumnIndex(), true);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("svrGroupName"), "svrGroupName", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("configPath"), "configPath", styleMap, fontMap);

            StringBuilder names = new StringBuilder();
            StringBuilder ports = new StringBuilder();

            JsonNode connectorsNode = n.get("svrConnectors");

            Iterator<JsonNode> it = connectorsNode.elements();
            while (it.hasNext()) {
                JsonNode connectorNode = it.next();

                if (StringUtils.isNotEmpty(names)) {
                    names.append(",").append("\n");
                }

                if (StringUtils.isNotEmpty(ports)) {
                    ports.append(",").append("\n");
                }

                names.append(connectorNode.get("name").asText());
                ports.append(connectorNode.get("port").asText());
            }

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(names.toString());

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(ports.toString());

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("ipAddress"), "ipAddress", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("minHeap"), "minHeap", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("maxHeap"), "maxHeap", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("maxPermSize"), "maxPermSize", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("javaVersion"), "javaVersion", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("runUser"), "runUser", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("isRunning"), "isRunning", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("portOffset"), "portOffset", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("jvmOptions"), "jvmOptions", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("profileName"), "profileName", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("socketBindName"), "socketBindName", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("runTimeOptions"), "runTimeOptions", styleMap, fontMap);
        }
    }

    public void makeThirdPartySolutionsSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("name");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("vendor");

            if (i == 0) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("discoveryDetails");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("discoveryDetails");

                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 2, 3);
            } else if (i == 1) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("type");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("value");

                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 0, 0);
                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 1, 1);
            }
        }

        Iterator<JsonNode> iter = node.elements();
        int startRow;
        while (iter.hasNext()) {
            startRow = row.getRowNum() + 1;

            JsonNode n = iter.next();
            JsonNode detailsNode = n.get("discoveryDetails");
            Iterator<JsonNode> it = detailsNode.elements();
            while (it.hasNext()) {
                JsonNode detailNode = it.next();

                row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, n.get("name"), "name", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, n.get("vendor"), "vendor", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, detailNode.get("type"), "type", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, detailNode.get("value"), "value", styleMap, fontMap);
            }

            mergeRegion(dataSheet, startRow, row.getRowNum(), 0, 0);
            mergeRegion(dataSheet, startRow, row.getRowNum(), 1, 1);
        }
    }

    public void makeFileSummaryMapSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("File Extension Name");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("File Count");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("File Size(Bytes)");

        Iterator<String> fieldNames = node.fieldNames();

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode subNode = node.get(fieldName);

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            cell.setCellValue(fieldName);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("fileCount"), "fileCount", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.RIGHT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, subNode.get("fileSize"), "fileSize", styleMap, fontMap);
        }
    }

    public void makeEeModulesSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("displayName");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("description");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("ejb");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("java");

            if (i == 0) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("web");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("web");

                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 4, 5);
            } else if (i == 1) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("webUri");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("contextRoot");

                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 0, 0);
                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 1, 1);
                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 2, 2);
                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 3, 3);
            }
        }

        Iterator<JsonNode> iter = node.elements();
        while (iter.hasNext()) {
            JsonNode n = iter.next();
            JsonNode webNode = n.get("web");

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("displayName"), "displayName", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("description"), "description", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("ejb"), "ejb", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, n.get("java"), "java", styleMap, fontMap);

            if (webNode.isNull() || webNode.isEmpty()) {
                for (int i = 0; i < 2; i++) {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                }
            } else {
                Iterator<JsonNode> it = webNode.elements();
                while (it.hasNext()) {
                    JsonNode d = it.next();

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, d.get("webUri"), "webUri", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, d.get("contextRoot"), "contextRoot", styleMap, fontMap);
                }
            }
        }
    }

    public void makeNginxLogSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        JsonNode logFormatNode = node.get("logFormat");

        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("logFormat");

        if (logFormatNode.isNull() || logFormatNode.isEmpty()) {
            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        } else {
            Iterator<JsonNode> iter = logFormatNode.elements();
            while (iter.hasNext()) {
                JsonNode n = iter.next();

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                cell.setCellValue(n.textValue());

                if (iter.hasNext()) {
                    row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("logFormat");
                }
            }

            mergeRegion(dataSheet, 0, row.getRowNum(), 0, 0);
        }

        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
        cell.setCellValue("accessLog");

        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
        cell.setCellValue(node.get("accessLog").textValue());
    }

    public void makeNginxServersSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("serverName");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("listen");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("accessLog");

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("root");

            if (i == 0) {
                for (int j = 0; j < 10; j++) {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("ssl");
                }

                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 4, 13);

                for (int j = 0; j < 11; j++) {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("proxy");
                }

                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 14, 24);

                for (int j = 0; j < 6; j++) {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                    cell.setCellValue("locations");
                }

                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 25, 30);
            } else if (i == 1) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("ssl");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("sslCertificate");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("sslCertificateKey");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("sslCiphers");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("sslPreferServerCiphers");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("sslClientCertificate");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("sslSessionCache");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("sslSessionTicketKey");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("sslSessionTickets");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("sslSessionTimeout");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxySetHeader");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxyPass");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxyBuffers");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxyBufferSize");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxyReadTimeout");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxyCache");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxyCacheRevalidate");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxyCacheMinUses");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxyCacheUseStale");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxyCacheLock");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxyHttpVersion");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("uri");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("root");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("expires");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("fastcgiPass");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxySetHeader");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("proxyPass");

                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 0, 0);
                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 1, 1);
                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 2, 2);
                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 3, 3);
            }
        }

        JsonNode serverNodes = node.get("servers");

        Iterator<JsonNode> iter = serverNodes.elements();
        while (iter.hasNext()) {
            JsonNode serverNode = iter.next();

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
            int startRow = row.getRowNum();

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, serverNode.get("serverName"), "serverName", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, serverNode.get("listen"), "listen", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, serverNode.get("accessLog"), "accessLog", styleMap, fontMap);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, serverNode.get("root"), "root", styleMap, fontMap);

            JsonNode sslNode = serverNode.get("ssl");
            if (sslNode.isNull() || sslNode.isEmpty()) {
                for (int i = 0; i < 10; i++) {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                }
            } else {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, sslNode.get("ssl"), "ssl", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, sslNode.get("sslCertificate"), "sslCertificate", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, sslNode.get("sslCertificateKey"), "sslCertificateKey", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, sslNode.get("sslCiphers"), "sslCiphers", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, sslNode.get("sslPreferServerCiphers"), "sslPreferServerCiphers", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, sslNode.get("sslClientCertificate"), "sslClientCertificate", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, sslNode.get("sslSessionCache"), "sslSessionCache", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, sslNode.get("sslSessionTicketKey"), "sslSessionTicketKey", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, sslNode.get("sslSessionTickets"), "sslSessionTickets", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, sslNode.get("sslSessionTimeout"), "sslSessionTimeout", styleMap, fontMap);
            }

            JsonNode proxyNode = serverNode.get("proxy");
            if (proxyNode.isNull() || proxyNode.isEmpty()) {
                for (int i = 0; i < 11; i++) {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                }
            } else {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, proxyNode.get("proxySetHeader"), "proxySetHeader", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, proxyNode.get("proxyPass"), "proxyPass", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, proxyNode.get("proxyBuffers"), "proxyBuffers", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, proxyNode.get("proxyBufferSize"), "proxyBufferSize", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, proxyNode.get("proxyReadTimeout"), "proxyReadTimeout", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, proxyNode.get("proxyCache"), "proxyCache", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, proxyNode.get("proxyCacheRevalidate"), "proxyCacheRevalidate", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, proxyNode.get("proxyCacheMinUses"), "proxyCacheMinUses", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, proxyNode.get("proxyCacheUseStale"), "proxyCacheUseStale", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, proxyNode.get("proxyCacheLock"), "proxyCacheLock", styleMap, fontMap);

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                ExcelHelper.setCellData(workbook, cell, proxyNode.get("proxyHttpVersion"), "proxyHttpVersion", styleMap, fontMap);
            }

            JsonNode locationNodes = serverNode.get("locations");
            if (locationNodes.isNull() || locationNodes.isEmpty()) {
                for (int i = 0; i < 6; i++) {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                }
            } else {
                Iterator<JsonNode> it = locationNodes.elements();
                while (it.hasNext()) {
                    JsonNode locationNode = it.next();

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, locationNode.get("uri"), "uri", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, locationNode.get("root"), "root", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, locationNode.get("expires"), "expires", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, locationNode.get("fastcgiPass"), "fastcgiPass", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (!locationNode.get("proxy").isNull() && !locationNode.get("proxy").isEmpty()) {
                        ExcelHelper.setCellData(workbook, cell, locationNode.get("proxy").get("proxySetHeader"), "proxySetHeader", styleMap, fontMap);
                    }

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    if (!locationNode.get("proxy").isNull() && !locationNode.get("proxy").isEmpty()) {
                        ExcelHelper.setCellData(workbook, cell, locationNode.get("proxy").get("proxyPass"), "proxyPass", styleMap, fontMap);
                    }

                    if (it.hasNext()) {
                        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

                        for (int i = 0; i < 25; i++) {
                            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                        }
                    }
                }

                if (startRow < row.getRowNum()) {
                    for (int i = 0; i < 25; i++) {
                        mergeRegion(dataSheet, startRow, row.getRowNum(), i, i);
                    }
                }
            }
        }
    }

    public void makeNginxUpstreamsSheet(SXSSFSheet dataSheet, JsonNode node) throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
            cell.setCellValue("name");

            if (i == 0) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("servers");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("servers");

                mergeRegion(dataSheet, row.getRowNum(), row.getRowNum(), 1, 2);
            } else if (i == 1) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("address");

                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue("option");

                mergeRegion(dataSheet, row.getRowNum() - 1, row.getRowNum(), 0, 0);
            }
        }

        JsonNode upstreamNodes = node.get("upstreams");

        Iterator<JsonNode> iter = upstreamNodes.elements();
        while (iter.hasNext()) {
            JsonNode upstreamNode = iter.next();

            row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
            int startRow = row.getRowNum();

            cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
            ExcelHelper.setCellData(workbook, cell, upstreamNode.get("name"), "name", styleMap, fontMap);

            JsonNode serverNodes = upstreamNode.get("servers");
            if (serverNodes.isNull() || serverNodes.isEmpty()) {
                for (int i = 0; i < 2; i++) {
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                }
            } else {
                Iterator<JsonNode> it = serverNodes.elements();
                while (it.hasNext()) {
                    JsonNode serverNode = it.next();

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, serverNode.get("address"), "address", styleMap, fontMap);

                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    ExcelHelper.setCellData(workbook, cell, serverNode.get("option"), "option", styleMap, fontMap);

                    if (it.hasNext()) {
                        row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
                        cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);
                    }
                }

                if (startRow < row.getRowNum()) {
                    mergeRegion(dataSheet, startRow, row.getRowNum(), 0, 0);
                }
            }
        }
    }

    /**
     * Merge region.
     *
     * @param sheet     the sheet
     * @param firstRow  the first row
     * @param lastRow   the last row
     * @param firstCell the first cell
     * @param lastCell  the last cell
     */
    private void mergeRegion(SXSSFSheet sheet, int firstRow, int lastRow, int firstCell, int lastCell) throws InterruptedException {
        try {
            if (firstRow != lastRow || firstCell != lastCell) {
                sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCell, lastCell));
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.warn("Sheet : [{}], firstRow : [{}], lastRow : [{}], firstCell : [{}], lastCell : [{}]",
                    new Object[]{sheet.getSheetName(), firstRow, lastRow, firstCell, lastCell});
            log.error("Unable to merge cells.", e);
        }
    }
}
//end of SheetGenerator.java
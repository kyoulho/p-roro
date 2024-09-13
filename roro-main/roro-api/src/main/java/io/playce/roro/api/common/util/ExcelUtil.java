/*
 * Copyright 2019 The Playce-SmartCity Project.
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
 * Author            Date                  Description
 * ---------------  ----------------      ------------
 * Jeongho Baek      8월 28, 2019          First Draft.
 */
package io.playce.roro.api.common.util;

import io.playce.roro.api.common.CommonConstants;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import static io.playce.roro.api.common.CommonConstants.EXCEL_EXTENSION_XLS;
import static io.playce.roro.api.common.CommonConstants.EXCEL_EXTENSION_XLSX;

/**
 * <pre>
 *   Excel 관련 유틸리티 클래스입니다.
 * </pre>
 *
 * @author Jeongho Baek
 * @version 1.0
 */
public class ExcelUtil {

    private static final String REF_SHEET_NAME = "Cell Reference";
    private static CellStyle cellStyle;

    /**
     * Excel파일에 대한 Workbook객체를 반환한다.
     */
    public static Workbook getWorkbook(String filePath, String filName) throws Exception {
        return getWorkbook(new FileInputStream(filePath + filName), filName);
    }

    public static Workbook getWorkbook(InputStream inputStream, String filName) throws Exception {

        Workbook wb = null;

        // 파일의 확장자를 체크해서 xls라면 HSSFWorkbook에
        // xlsx라면 XSSFWorkbook에 각각 초기화 한다.
        if (filName.endsWith(EXCEL_EXTENSION_XLS)) {
            wb = new HSSFWorkbook(inputStream);
        }
        if (filName.endsWith(EXCEL_EXTENSION_XLSX)) {
            wb = new XSSFWorkbook(inputStream);
        }
        return wb;
    }

    /**
     * Cell 읽은 다음 타입에 맞게 변형시켜 모두 String으로 반환한다
     */
    public static String getCellData(Workbook workbook, Cell cell) {

        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        String cellData = "";

        // 셀이 빈값일경우를 위한 널체크
        if (cell == null) {
            return "";
        } else {
            // 타입별로 내용 읽기
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        cellData = formatter.format(cell.getDateCellValue());
                    } else {
                        double dData = cell.getNumericCellValue();
                        cellData = isInteger(dData) ? String.valueOf((long) dData) : String.valueOf(dData);
                    }
                    break;
                case STRING:
                    cellData = cell.toString();
                    break;
                case BOOLEAN:
                    boolean bdata = cell.getBooleanCellValue();
                    cellData = String.valueOf(bdata);
                    break;
                case BLANK:
                case ERROR:
                case FORMULA:
                    if (!(cell.toString().equals(""))) {
                        if (evaluator.evaluateFormulaCell(cell) == CellType.NUMERIC) {
                            double dData = cell.getNumericCellValue();
                            cellData = isInteger(dData) ? String.valueOf((long) dData) : String.valueOf(dData);
                        } else if (evaluator.evaluateFormulaCell(cell) == CellType.STRING) {
                            cellData = cell.getStringCellValue();
                        } else if (evaluator.evaluateFormulaCell(cell) == CellType.BOOLEAN) {
                            boolean fbdata = cell.getBooleanCellValue();
                            cellData = String.valueOf(fbdata);
                        }
                        break;
                    }
                default:
                    break;
            }
        }

        return cellData.trim();
    }

    /**
     * 정수형 데이터 인지 판단.
     */
    private static boolean isInteger(double value) {
        // 버림 값과 무한대가 아니면.
        return (value == Math.floor(value)) && !Double.isInfinite(value);
    }

    public static void createCellStyle(Workbook workbook) {
        cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setWrapText(true);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        cellStyle.setFont(font);
    }

    public static CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    /**
     * 공통 Cell Style 적용을 위한 Method.
     *
     * @param row
     * @param idx
     *
     * @return
     */
    public static Cell createCell(Row row, int idx) {
        Cell cell = row.createCell(idx);
        cell.setCellStyle(cellStyle);

        return cell;
    }

    public static Cell createCellHeader(Row row, int idx, CellStyle cellStyle) {
        Cell cell = row.createCell(idx);
        cell.setCellStyle(cellStyle);

        return cell;
    }

    /**
     * Auto size column.
     *
     * @param workbook the workbook
     */
    public static void autoSizeColumn(Workbook workbook) {
        Sheet sheet;

        for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
            try {
                sheet = workbook.getSheetAt(i);

                int rowCnt = sheet.getPhysicalNumberOfRows();
                int rowIdx = 0;
                if (rowCnt > 0) {
                    Row row = null;

                    while (rowIdx < rowCnt) {
                        row = sheet.getRow(rowIdx);

                        if (row != null) {
                            break;
                        }

                        rowIdx++;
                    }

                    if (row != null) {
                        Iterator<Cell> cellIterator = row.cellIterator();
                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();

                            if (cell != null) {
                                int columnIndex = cell.getColumnIndex();
                                sheet.autoSizeColumn(columnIndex, true);
                                int currentColumnWidth = sheet.getColumnWidth(columnIndex);
                                int maxColumnWidth = (currentColumnWidth + 2500);
                                if (maxColumnWidth > 30000) {
                                    maxColumnWidth = 30000;
                                }
                                sheet.setColumnWidth(columnIndex, maxColumnWidth);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        // Move "Cell Reference" sheet to last
        if (workbook.getSheet(REF_SHEET_NAME) != null) {
            workbook.setSheetOrder(REF_SHEET_NAME, workbook.getNumberOfSheets() - 1);
        }
    }

    public static String generateExcelFileName(String resourceName) {
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return resourceName + "_" + datetime + "." + CommonConstants.EXCEL_EXTENSION_XLSX;
    }

    public static String generatePdfFileName(String resourceName) {
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return resourceName + "_" + datetime + "." + CommonConstants.PDF_EXTENSION;
    }
}
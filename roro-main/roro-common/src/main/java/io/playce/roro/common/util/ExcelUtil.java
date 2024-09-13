/*
 * Copyright 2021 The playce-roro-v3} Project.
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
 * Dong-Heon Han    Nov 29, 2021		    First Draft.
 */

package io.playce.roro.common.util;

import io.playce.roro.common.dto.common.excel.ListToExcelDto;
import io.playce.roro.common.util.support.ExcelHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Slf4j
public class ExcelUtil {

    public static void createHeader(Row row, int startIndex, String... cols) {
        for (String col : cols) {
            row.createCell(startIndex++).setCellValue(col);
        }
    }

    /**
     * <pre>
     * Excel Export for Inventory List
     * </pre>
     *
     * @param listToExcelDto
     *
     * @return
     */
    public static ByteArrayOutputStream listToExcel(String sheetName, ListToExcelDto listToExcelDto) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // -1 means turn off auto-flushing and accumulate all rows in memory
            SXSSFWorkbook workbook = new SXSSFWorkbook(-1);
            Map<String, CellStyle> styleMap = ExcelHelper.initCellStyle(workbook);
            Map<String, Font> fontMap = ExcelHelper.initFont(workbook);

            SXSSFSheet sheet = workbook.createSheet(sheetName);
            sheet.trackAllColumnsForAutoSizing();

            SXSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
            SXSSFCell cell;
            for (String header : listToExcelDto.getHeaderItemList()) {
                cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.CENTER, BorderStyle.THIN, true, styleMap);
                cell.setCellValue(header);
            }

            for (ListToExcelDto.RowItem rowItem : listToExcelDto.getRowItemList()) {
                row = sheet.createRow(sheet.getLastRowNum() + 1);

                for (int i = 0; i < rowItem.getCellItemList().size(); i++) {
                    Object cellItem = rowItem.getCellItemList().get(i);
                    cell = ExcelHelper.createCellWithBorder(workbook, row, HorizontalAlignment.LEFT, BorderStyle.THIN, false, styleMap);

                    if (cellItem != null) {
                        ExcelHelper.setCellData(workbook, cell, styleMap, cellItem, listToExcelDto.getHeaderItemList().get(i));
                    }
                }
            }

            // Adjust Column Size
            ExcelHelper.autoSizeColumn(workbook);

            workbook.write(out);

            // workbook.dispose();

            return out;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Convert Cell index to letters.
     *
     * @param cell
     *
     * @return
     */
    public static String convertCellNumber(Cell cell) {
        return CellReference.convertNumToColString(cell.getColumnIndex());
    }
}
//end of ExcelUtil.java
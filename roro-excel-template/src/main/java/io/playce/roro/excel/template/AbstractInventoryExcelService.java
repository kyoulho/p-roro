/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * Dong-Heon Han    Dec 02, 2021		First Draft.
 */

package io.playce.roro.excel.template;

import io.playce.roro.common.dto.inventory.inventory.InventoryUploadFail;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.excel.template.config.ExcelTemplateConfig;
import io.playce.roro.excel.template.vo.RecordMap;
import io.playce.roro.excel.template.vo.SheetMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Slf4j
public abstract class AbstractInventoryExcelService implements InventoryExcelService {
    private final InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();

    @Override
    public void parse(ExcelTemplateConfig.SheetInfo sheetInfo, Sheet sheet, SheetMap result, List<InventoryUploadFail> validationList) {
        String name = sheet.getSheetName();
        log.debug("parse sheet name: {}", name);

        // 3.6.1 이전 upload template에 대한 비교 (이전 템플릿에는 ID가 아닌 아이디로 표기)
        if (!sheet.getSheetName().equals("service-mapping") &&
                (sheet.getRow(0).getLastCellNum() != sheetInfo.getCols().size() ||
                        !sheet.getRow(0).getCell(0).getStringCellValue().contains("ID"))) {
            throw new RoRoException("INVALID_FILE_TYPE");
        }

        for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            if (i < sheetInfo.getHeaderSize()) {
                continue;
            }

            // ValidationResult validationResult = new ValidationResult();
            checkMandatary(sheet, row, sheetInfo.getKeyCols(), validationList);

            if (!sheet.getSheetName().equals("service-mapping")) {
                checkIds(sheet, row, validationList);
            }

            RecordMap record = parseRow(i, row, sheetInfo.getCols());
            result.put(name, record);
        }
    }

    /**
     * ID 값이 존재하는 경우 Long 타입인지 체크
     */
    protected void checkIds(Sheet sheet, Row row, List<InventoryUploadFail> validationList) {
        Cell cell = row.getCell(0);

        Object value = getValue(cell);

        try {
            if (value != null && StringUtils.isNotEmpty(value.toString())) {
                Long.parseLong(value.toString().replaceAll("\\.0", ""));
            }
        } catch (NumberFormatException e) {
            InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
            inventoryUploadFail.setSheet(sheet.getSheetName());
            inventoryUploadFail.setRowNumber(row.getRowNum() + 1);
            inventoryUploadFail.setColumnNumber(sheet.getRow(1).getCell(0).getStringCellValue().trim());
            inventoryUploadFail.setFailDetail("This ID attribute(\"" + value + "\") must be integer or empty.");
            validationList.add(inventoryUploadFail);
        }
    }

    /**
     * check required value for Upload Template.
     * - Excel template module 의 필수 값을 체크해서 필수 값이 비어있거나 null 인 경우 ValidationList 에 Add 한다.
     */
    protected void checkMandatary(Sheet sheet, Row row, List<String> keyCols, List<InventoryUploadFail> validationList) {
        // boolean result = true;
        InventoryUploadFail inventoryUploadFail = null;
        for (String keyCol : keyCols) {
            inventoryUploadFail = new InventoryUploadFail();
            String[] cols = keyCol.split(":");
            int cellIndex = Integer.parseInt(cols[0].trim());

            Cell cell = row.getCell(cellIndex);
            if (cell == null) {
                inventoryUploadFail.setSheet(sheet.getSheetName());
                inventoryUploadFail.setRowNumber(row.getRowNum() + 1);
                inventoryUploadFail.setColumnNumber(sheet.getRow(1).getCell(cellIndex).getStringCellValue().trim());
                inventoryUploadFail.setFailDetail("This attribute is required and cannot be empty.");
                validationList.add(inventoryUploadFail);
            } else {
                Object value = getValue(cell);
                if (value == null || StringUtils.isEmpty(value.toString())) {
                    inventoryUploadFail.setSheet(sheet.getSheetName());
                    inventoryUploadFail.setRowNumber(row.getRowNum() + 1);
                    inventoryUploadFail.setColumnNumber(sheet.getRow(1).getCell(cellIndex).getStringCellValue().trim());
                    inventoryUploadFail.setFailDetail("This attribute is required and cannot be empty.");
                    validationList.add(inventoryUploadFail);
                }
                validation(sheet, row, cols, value, validationList);

//                 try {
//                     validation(sheet, row, cols, value, validationList);
//                 } catch (Exception e) {
//                     log.debug("\tcheck row: {}, {}", row, e.getMessage());
//                 }

            }
        }
    }

    private void validation(Sheet sheet, Row row, String[] cols, Object value, List<InventoryUploadFail> validationList) {
        if (cols.length > 1 && StringUtils.isNotEmpty(cols[1])) {
            if ("ip".equals(cols[1])) {
                boolean validIp = inetAddressValidator.isValidInet4Address((String) value);
                if (!validIp) {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                    inventoryUploadFail.setSheet(sheet.getSheetName());
                    inventoryUploadFail.setRowNumber(row.getRowNum() + 1);
                    inventoryUploadFail.setColumnNumber("IP Address");
                    inventoryUploadFail.setFailDetail("This attribute is not valid.");
                    validationList.add(inventoryUploadFail);
                }
            }

            if ("port".equals(cols[1])) {
                if (value instanceof Integer || value instanceof Float
                        || value instanceof Long || value instanceof Double) {
                    Double v = Double.parseDouble(value.toString());
                    if (v < 1 || v > Math.pow(2, 16) - 1) {
                        InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                        inventoryUploadFail.setSheet(sheet.getSheetName());
                        inventoryUploadFail.setRowNumber(row.getRowNum() + 1);
                        inventoryUploadFail.setColumnNumber("Port");
                        inventoryUploadFail.setFailDetail("This attribute must be in range from 1 to 65535.");
                        validationList.add(inventoryUploadFail);
                    }
                } else {
                    InventoryUploadFail inventoryUploadFail = new InventoryUploadFail();
                    inventoryUploadFail.setSheet(sheet.getSheetName());
                    inventoryUploadFail.setRowNumber(row.getRowNum() + 1);
                    inventoryUploadFail.setColumnNumber("Port");
                    inventoryUploadFail.setFailDetail("This attribute must be Number value.");
                    validationList.add(inventoryUploadFail);
                }
            }
        }
    }

//    private boolean checkIfRowIsEmpty(Row row) {
//        if (row == null) {
//            return true;
//        }
//        if (row.getLastCellNum() <= 0) {
//            return true;
//        }
//        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
//            Cell cell = row.getCell(cellNum);
//            if (cell != null && cell.getCellType() != CellType.BLANK && StringUtils.isNotBlank(cell.toString())) {
//                return false;
//            }
//        }
//        return true;
//    }

    protected RecordMap parseRow(int index, Row row, List<String> cols) {
        RecordMap record = new RecordMap();
        for (int i = 0; i < cols.size(); i++) {
            Cell cell = row.getCell(i);
            Object value = cell == null ? null : getValue(cell);

            log.trace("\t({}, {}) - {}", index, i, value);
            String[] columns = cols.get(i).split(",");
            for (String column : columns) {
                record.put(column, value);
            }
        }
        return record;
    }

    protected Object getValue(Cell cell) {
        if (cell != null) {
            CellType cellType = cell.getCellType();
            switch (cellType) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    return cell.getNumericCellValue();
                case BOOLEAN:
                    return cell.getBooleanCellValue();
            }
        }

        return null;
    }

    @Getter
    @Setter
    private static class ValidationResult {
        private boolean result;
        private String message;
    }
}
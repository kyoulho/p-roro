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
 * Jaeeon Bae       6월 22, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper.survey;

import io.playce.roro.api.common.util.ExcelUtil;
import io.playce.roro.common.dto.cloudreadiness.CloudReadinessAnswer;
import io.playce.roro.common.dto.cloudreadiness.CloudReadinessUploadFail;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
public class CloudReadinessParser extends SurveyParser {

    @Override
    public Map<Long, List<CloudReadinessAnswer>> parse(XSSFWorkbook workbook, Map<Long, List<CloudReadinessAnswer>> surveAnsweryMap, List<CloudReadinessUploadFail> validationList) {
        Sheet businessSheet = workbook.getSheetAt(1);
        int businessRows = businessSheet.getPhysicalNumberOfRows();

        Sheet technicalSheet = workbook.getSheetAt(2);
        technicalSheet.getPhysicalNumberOfRows();
        int technicalRows = technicalSheet.getPhysicalNumberOfRows();

        // 1. Business row를 읽어들인다.
        List<Long> businessServiceIdList = new ArrayList<>();
        for (int rowIndex = 1;  rowIndex < businessRows; rowIndex++) {
            Row row = businessSheet.getRow(rowIndex);

            if (row != null) {
                // Business Factor의 모든 값이 Null Or Empty면 해당 row를 Skip 한다.
                if (checkBusinessEmptyValue(row)) break;

                // Business Factor의 Question Id는 1부터 시작한다.
                Long questionId = 1L;
                for (int cellIndex = 6; cellIndex <= 12; cellIndex++) {
                    Long serviceId = Long.valueOf(ExcelUtil.getCellData(workbook, row.getCell(6)));
                    if (cellIndex == 6) {
                        if (businessServiceIdList.contains(serviceId)) {
                            CloudReadinessUploadFail cloudReadinessUploadFail = new CloudReadinessUploadFail();
                            cloudReadinessUploadFail.setSheet("Step 01_Business Factors");
                            cloudReadinessUploadFail.setServiceId(serviceId);
                            cloudReadinessUploadFail.setQuestion("-");
                            cloudReadinessUploadFail.setFailDetail("Service ID(" + serviceId + ") cannot duplicate.");
                            validationList.add(cloudReadinessUploadFail);
                        }
                        businessServiceIdList.add(serviceId);

                        surveAnsweryMap.put(serviceId, new ArrayList<>());
                    } else if (cellIndex > 7) {
                        CloudReadinessAnswer cloudReadinessAnswer = makeCloudReadinessAnswer(workbook, questionId, row.getCell(cellIndex));
                        surveAnsweryMap.get(serviceId).add(cloudReadinessAnswer);
                        questionId++;
                    }
                }
            }
        }

        // 2. Technical row를 읽어들인다.
        List<Long> technicalServiceIdList = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < technicalRows; rowIndex++) {
            Row row = technicalSheet.getRow(rowIndex);

            if (row != null) {
                // Technical Factor의 모든 값이 Null Or Empty면 해당 row를 Skip 한다.
                if (checkTechnicalEmptyValue(row)) break;

                // Technical Factor의 Question Id는 6부터 시작한다.
                Long questionId = 6L;
                for (int cellIndex = 6; cellIndex <= 17; cellIndex++) {
                    Long serviceId = Long.valueOf(ExcelUtil.getCellData(workbook, row.getCell(6)));
                    if (cellIndex == 6) {
                        if (technicalServiceIdList.contains(serviceId)) {
                            CloudReadinessUploadFail cloudReadinessUploadFail = new CloudReadinessUploadFail();
                            cloudReadinessUploadFail.setSheet("Step 02_Technical Factors");
                            cloudReadinessUploadFail.setServiceId(serviceId);
                            cloudReadinessUploadFail.setQuestion("-");
                            cloudReadinessUploadFail.setFailDetail("Service ID(" + serviceId + ") cannot duplicate.");
                            validationList.add(cloudReadinessUploadFail);
                        }
                        technicalServiceIdList.add(serviceId);

                        surveAnsweryMap.computeIfAbsent(serviceId, k -> new ArrayList<>());
                    } else if (cellIndex > 7) {
                        CloudReadinessAnswer cloudReadinessAnswer = makeCloudReadinessAnswer(workbook, questionId, row.getCell(cellIndex));
                        surveAnsweryMap.get(serviceId).add(cloudReadinessAnswer);
                        questionId++;
                    }
                }
            }
        }

        return surveAnsweryMap;
    }

    @Override
    public boolean checkBusinessEmptyValue(Row row) {
        return isEmptyValue(row);
    }

    @Override
    public boolean isEmptyValue(Row row) {
        return (row.getCell(6) == null || row.getCell(6, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null)
                && (row.getCell(7) == null || row.getCell(7, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null)
                && (row.getCell(8) == null || row.getCell(8, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null)
                && (row.getCell(9) == null || row.getCell(9, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null)
                && (row.getCell(10) == null || row.getCell(10, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null)
                && (row.getCell(11) == null || row.getCell(11, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null)
                && (row.getCell(12) == null || row.getCell(12, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null);
    }

    @Override
    public boolean checkTechnicalEmptyValue(Row row) {
        return isEmptyValue(row)
                && (row.getCell(13) == null || row.getCell(13, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null)
                && (row.getCell(14) == null || row.getCell(14, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null)
                && (row.getCell(15) == null || row.getCell(15, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null)
                && (row.getCell(16) == null || row.getCell(16, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null)
                && (row.getCell(17) == null || row.getCell(17, MissingCellPolicy.RETURN_BLANK_AS_NULL) == null);
    }
}
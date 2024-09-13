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
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
public abstract class SurveyParser {

    /**
     * Survey Parser
     */
    public abstract Map<Long, List<CloudReadinessAnswer>> parse(XSSFWorkbook workbook, Map<Long, List<CloudReadinessAnswer>> surveAnsweryMap, List<CloudReadinessUploadFail> validationList);

    abstract boolean checkBusinessEmptyValue(Row row);

    abstract boolean isEmptyValue(Row row);

    abstract boolean checkTechnicalEmptyValue(Row row);

    /**
     * Cloud Survey create/modify API와 같은 Parameter를 만들어준다.
     */
    public CloudReadinessAnswer makeCloudReadinessAnswer(XSSFWorkbook workbook, Long questionId, Cell cell) {
        CloudReadinessAnswer cloudReadinessAnswer = new CloudReadinessAnswer();
        cloudReadinessAnswer.setQuestionId(questionId);
        if (StringUtils.isNotEmpty(ExcelUtil.getCellData(workbook, cell))) {
            cloudReadinessAnswer.setAnswerId(Long.valueOf(ExcelUtil.getCellData(workbook, cell)));
        }
        return cloudReadinessAnswer;
    }
}
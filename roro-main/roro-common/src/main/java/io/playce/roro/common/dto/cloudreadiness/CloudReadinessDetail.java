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
 * Jaeeon Bae       6월 13, 2022            First Draft.
 */
package io.playce.roro.common.dto.cloudreadiness;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Getter
@Setter
@ToString
public class CloudReadinessDetail {

    private Long surveyId;
    private Long surveyProcessId;
    private Long serviceId;
    private String serviceName;
    private String surveyResult;
    private String surveyProcessResultCode;
    private float businessScore;
    private float technicalScore;
    private String registUserLoginId;
    private Date registDatetime;
    private String modifyUserLoginId;
    private Date modifyDatetime;
    private List<AnswerSummary> answerSummary;

    @Getter
    @Setter
    @ToString
    public static class AnswerSummary {
        private Long questionId;
        private float weight;
        private Long answerId;
        private int score;
    }
}
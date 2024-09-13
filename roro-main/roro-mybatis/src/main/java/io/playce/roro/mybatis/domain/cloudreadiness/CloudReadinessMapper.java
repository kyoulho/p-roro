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
 * Jaeeon Bae       5월 19, 2022            First Draft.
 */
package io.playce.roro.mybatis.domain.cloudreadiness;

import io.playce.roro.common.dto.cloudreadiness.CloudReadinessDetail;
import io.playce.roro.common.dto.cloudreadiness.CloudReadinessDetail.AnswerSummary;
import io.playce.roro.common.dto.cloudreadiness.CloudReadinessQuestionResponse;
import io.playce.roro.common.dto.cloudreadiness.CloudReadinessQuestionResponse.QuestionDto;
import io.playce.roro.common.dto.cloudreadiness.CloudReadiness;
import io.playce.roro.common.dto.cloudreadiness.CloudReadinessCategoryResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Repository
public interface CloudReadinessMapper {

    CloudReadinessQuestionResponse selectSurvey(@Param("surveyId") Long surveyId);

    List<QuestionDto> selectQuestion(@Param("surveyId") Long surveyId);

    List<CloudReadinessQuestionResponse.Answer> selectAnswer();

    List<CloudReadiness> selectSurveyAnswer(@Param("projectId") Long projectId);

    CloudReadinessDetail selectCloudReadinessDetail(@Param("projectId") Long projectId, @Param("serviceId") Long serviceId);

    List<AnswerSummary> selectAnswerSummary(@Param("surveyProcessId") Long surveyProcessId);

    List<CloudReadinessCategoryResult> selectCloudReadinessResultList(@Param("projectId") Long projectId, @Param("serviceIds") List<Integer> serviceIds);
}
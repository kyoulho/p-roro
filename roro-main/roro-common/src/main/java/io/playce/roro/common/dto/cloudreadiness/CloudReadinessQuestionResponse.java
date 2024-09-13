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
 * Jaeeon Bae       5월 24, 2022            First Draft.
 */
package io.playce.roro.common.dto.cloudreadiness;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
public class CloudReadinessQuestionResponse {

    private long surveyId;
    private String surveyNameEnglish;
    private String surveyNameKorean;

    private List<Category> categories;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class Category {
        @JsonIgnore
        private long surveyCategoryId;
        private long categoryStep;
        private String categoryNameEnglish;
        private String categoryNameKorean;

        private List<EvaluationItem> evaluationItems;

        public Category(long surveyCategoryId, long categoryStep, String categoryNameEnglish, String categoryNameKorean) {
            this.surveyCategoryId = surveyCategoryId;
            this.categoryStep = categoryStep;
            this.categoryNameEnglish = categoryNameEnglish;
            this.categoryNameKorean = categoryNameKorean;
        }
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class EvaluationItem {

        @JsonIgnore
        private long parentSurveyCategoryId;

        @JsonIgnore
        private long surveyCategoryId;
        private String evaluationItemEnglish;
        private String evaluationItemKorean;

        private List<Question> questions;

        public EvaluationItem(long parentSurveyCategoryId, long evaluationItemId, String evaluationItemEnglish, String evaluationItemKorean) {
            this.parentSurveyCategoryId = parentSurveyCategoryId;
            this.surveyCategoryId = evaluationItemId;
            this.evaluationItemEnglish = evaluationItemEnglish;
            this.evaluationItemKorean = evaluationItemKorean;
        }
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class Question {

        @JsonIgnore
        private long questionSurveyCategoryId;
        private long questionId;

        @JsonIgnore
        private long surveyCategoryId;
        private String questionContentEnglish;
        private String questionContentKorean;
        private long questionDisplayOrder;

        private List<Answer> answers;

        public Question(long questionSurveyCategoryId, long questionId, String questionContentEnglish, String questionContentKorean, long questionDisplayOrder) {
            this.questionSurveyCategoryId = questionSurveyCategoryId;
            this.questionId = questionId;
            this.questionContentEnglish = questionContentEnglish;
            this.questionContentKorean = questionContentKorean;
            this.questionDisplayOrder = questionDisplayOrder;
        }
    }


    @Getter
    @Setter
    @ToString
    public static class Answer {

        @JsonIgnore
        private long questionId;

        private long answerId;
        private String answerContentEnglish;
        private String answerContentKorean;
        private long answerDisplayOrder;

    }

    @Getter
    @Setter
    @ToString
    public static class QuestionDto {

        private long surveyCategoryId;
        private long categoryStep;
        private String categoryNameEnglish;
        private String categoryNameKorean;
        private long parentSurveyCategoryId;
        private long evaluationItemId;
        private String evaluationItemEnglish;
        private String evaluationItemKorean;
        private long questionSurveyCategoryId;
        private long questionId;
        private String questionContentEnglish;
        private String questionContentKorean;
        private long questionDisplayOrder;

    }

}
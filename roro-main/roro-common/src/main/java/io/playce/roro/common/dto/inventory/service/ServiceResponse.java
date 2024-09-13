package io.playce.roro.common.dto.inventory.service;
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
 * Hoon Oh          11월 30, 2021		First Draft.
 */

import io.playce.roro.common.dto.common.label.Label;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Getter
@Setter
public class ServiceResponse {
    @Schema(title = "Register Datetime", description = "생성 날짜")
    private Date registDatetime;

    @Schema(title = "Register user", description = "생성 사용자")
    private String registUserLoginId;

    @Schema(title = "Modify Datetime", description = "수정한 날짜")
    private Date modifyDatetime;

    @Schema(title = "Modify user", description = "수정한 사용자")
    private String modifyUserLoginId;

    @Schema(title = "Project ID", description = "프로젝트 ID")
    private long projectId;

    @Schema(title = "Service ID", description = "서비스 ID")
    private long serviceId;

    @Schema(title = "Service Name", description = "서비스 이름")
    private String serviceName;

    @Schema(title = "Customer Service Code", description = "업무 카테고리 코드")
    private String customerServiceCode;

    @Schema(title = "Customer Service Code Name", description = "업무 카테고리 이름")
    private String customerServiceName;

    @Schema(title = "Business Type Code", description = "업무 카테고리 코드")
    private String businessCategoryCode;

    @Schema(title = "Business Type Code Name", description = "업무 카테고리 이름")
    private String businessCategoryName;

    @Schema(title = "Labels", type = "array", description = "레이블")
    private List<Label.LabelResponse> labelList;

    @Schema(title = "Migration Y/N", description = "마이그레이션 대상 여부")
    private String migrationTargetYn;

    @Schema(title = "Man-Month", description = "Man Month")
    private Float migrationManMonth;

    @Schema(title = "Preferences Schedule Start Datetime", description = "마이그레이션 환경구축 시작 날짜")
    private Date migrationEnvConfigStartDatetime;

    @Schema(title = "Preferences Schedule End Datetime", description = "마이그레이션 환경구축 종료 날짜")
    private Date migrationEnvConfigEndDatetime;

    @Schema(title = "Application Schedule Test Start Datetime", description = "마이그레이션 테스트 시작 날짜")
    private Date migrationTestStartDatetime;

    @Schema(title = "Application Schedule Test End Datetime", description = "마이그레이션 테스트 종료 날짜")
    private Date migrationTestEndDatetime;

    @Schema(title = "Cut-Over Datetime", description = "마이그레이션 Cut Over 날짜")
    private Date migrationCutOverDatetime;

    @Schema(title = "Severity", description = "업무 중요도(심각도)")
    private String severity;

    @Schema(title = "Configure Manager", description = "담당자들")
    private List<ManagerResponse> managers;

    @Schema(title = "Servers", description = "서버 목록 수")
    private int serverCount;

    @Schema(title = "Middlewares", description = "미들웨어 목록 수")
    private int middlewareCount;

    @Schema(title = "Applications", description = "애플리케이션 목록 수")
    private int applicationCount;

    @Schema(title = "Databases", description = "데이터베이스 목록 수")
    private int databaseCount;

    @Schema(title = "Datasources", description = "데이터소스 목록 수")
    private int datasourceCount;

    @Schema(title = "Description", description = "설명")
    private String description;

    @Schema(title = "ID" , description = "설문 ID")
    private Long surveyProcessId;

    @Getter
    @Setter
    public static class ManagerResponse {
        @Schema(title = "ID", description = "사용자 ID")
        private Long userId;

        @Schema(title = "Role", type = "array", allowableValues = "{DEVELOPMENT, MAINTENANCE, DEPLOYMENT, OPERATION}", description = "담당자 유형")
        private String managerTypeCode;
    }

    private String surveyProcessResultCode;

}
//end of ServiceResponse.java
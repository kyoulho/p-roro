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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       12월 01, 2021            First Draft.
 */
package io.playce.roro.common.dto.inventory.service;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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
public class ServiceDetail {
    private Date registDatetime;
    private String registUserLoginId;
    private Date modifyDatetime;
    private String modifyUserLoginId;
    private long projectId;
    private long serviceId;
    private String serviceName;
    private String customerServiceCode;
    private String customerServiceName;
    private String businessCategoryCode;
    private String businessCategoryName;
    private String description;
    private String migrationTargetYn;
    private Float migrationManMonth;
    private Date migrationEnvConfigStartDatetime;
    private Date migrationEnvConfigEndDatetime;
    private Date migrationTestStartDatetime;
    private Date migrationTestEndDatetime;
    private Date migrationCutOverDatetime;
    private String severity;
    private String surveyProcessResultCode;
}
//end of ServiceDetail.java
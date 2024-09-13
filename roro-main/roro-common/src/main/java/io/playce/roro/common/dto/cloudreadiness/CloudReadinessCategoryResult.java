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
 * Jaeeon Bae       6월 14, 2022            First Draft.
 */
package io.playce.roro.common.dto.cloudreadiness;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
public class CloudReadinessCategoryResult {
    private Long surveyId;
    private Long surveyProcessId;
    private Long serviceId;
    private String serviceName;
    private Float businessRelevance;
    private Float scaleOfService;
    private Float targetOfService;
    private Float elasticityOfLoad;
    private Float businessRequirements;
    private Float businessScore;
    private Float usageOfResources;
    private Float ageingOfResources;
    private Float numberOfSystemsInterfaced;
    private Float language;
    private Float framework;
    private Float systemArchitecture;
    private Float os;
    private Float virtualization;
    private Float businessRequirementsForCloudAdoption;
    private Float technicalScore;
    private String surveyResult;
}
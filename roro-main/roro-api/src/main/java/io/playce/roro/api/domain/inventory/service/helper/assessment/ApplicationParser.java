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
 * Jaeeon Bae       3월 23, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper.assessment;

import com.fasterxml.jackson.core.type.TypeReference;
import io.playce.roro.app.asmt.result.ApplicationAssessmentResult;
import io.playce.roro.common.dto.assessment.AssessmentResultDto;
import io.playce.roro.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
public class ApplicationParser implements AssessmentParser {

    @Override
    public Object parse(Object object, String assessment) throws Exception {
        if (assessment == null) {
            return null;
        }

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(assessment);
        JSONObject jsonObj = (JSONObject) obj;
        org.json.simple.JSONObject appData = JsonUtil.getJsonObject(jsonObj);

        JSONObject fileSummaryMap = appData.get("fileSummaryMap") != null ? (JSONObject) appData.get("fileSummaryMap") : null;
        JSONArray buildFiles = appData.get("buildFiles") != null ? (JSONArray) appData.get("buildFiles") : null;
        JSONArray configFiles = appData.get("configFiles") != null ? (JSONArray) appData.get("configFiles") : null;
        JSONObject libraries = appData.get("libraries") != null ? (JSONObject) appData.get("libraries") : null;
        JSONArray checkList = appData.get("checkList") != null ? (JSONArray) appData.get("checkList") : null;
        JSONArray eeModules = appData.get("eeModules") != null ? (JSONArray) appData.get("eeModules") : null;
        JSONArray deprecatedList = appData.get("deprecatedList") != null ? (JSONArray) appData.get("deprecatedList") : null;
        JSONArray removedList = appData.get("removedList") != null ? (JSONArray) appData.get("removedList") : null;

        if (fileSummaryMap == null && buildFiles == null && configFiles == null
                && libraries == null && checkList == null && eeModules == null
                && deprecatedList == null && removedList == null) {
            return null;
        }

        // library count
        Long libraryCount = null;
        if (libraries != null && libraries.containsKey("all") && libraries.get("all") != null) {
            JSONArray libs = new JSONArray();
            if (libraries.get("all") instanceof JSONObject) {
                JSONObject libObj = (JSONObject) libraries.get("all");
                libs.add(libObj);
            } else if (libraries.get("all") instanceof JSONArray) {
                libs = (JSONArray) libraries.get("all");
            }
            libraryCount = (long) libs.size();
        }

        // eeModule count
        Long eeModuleCount = null;
        if (eeModules != null && eeModules.contains("ejb")) {
            eeModuleCount = (long) eeModules.size();
        }

        Map<String, Long> fileSummaries;

        if (fileSummaryMap.toJSONString().contains("fileCount")) {
            TypeReference<Map<String, ApplicationAssessmentResult.FileSummary>> type = new TypeReference<>() {};
            Map<String, ApplicationAssessmentResult.FileSummary> fileSummariesTemp = JsonUtil.jsonToObj(fileSummaryMap.toJSONString(), type);
            fileSummaries = new HashMap<>();

            if (fileSummariesTemp != null) {
                for (String key : fileSummariesTemp.keySet()) {
                    fileSummaries.put(key, fileSummariesTemp.get(key).getFileCount());
                }
            }
        } else {
            TypeReference<Map<String, Long>> type = new TypeReference<>() {};
            fileSummaries = JsonUtil.jsonToObj(fileSummaryMap.toJSONString(), type);
            if (fileSummaries == null) {
                fileSummaries = new HashMap<>();
            }
        }

        return AssessmentResultDto.ApplicationProperty.builder()
                .fileName((appData.containsKey("fileName") && appData.get("fileName") != null) ? appData.get("fileName").toString().replaceAll("\"", "") : "")
                .type((appData.containsKey("applicationType") && appData.get("applicationType") != null) ? appData.get("applicationType").toString().replaceAll("\"", "") : "")
                .cssCount(fileSummaries.get("css") != null ? fileSummaries.get("css") : 0)
                .htmlCount(fileSummaries.get("html") != null ? fileSummaries.get("html") : 0)
                .xmlCount(fileSummaries.get("xml") != null ? fileSummaries.get("xml") : 0)
                .jspCount(fileSummaries.get("jsp") != null ? fileSummaries.get("jsp") : 0)
                .jsCount(fileSummaries.get("js") != null ? fileSummaries.get("js") : 0)
                .javaCount(fileSummaries.get("java") != null ? fileSummaries.get("java") : 0)
                .classCount(fileSummaries.get("class") != null ? fileSummaries.get("class") : 0)
                .buildFileCount(buildFiles != null ? (long) buildFiles.size() : 0)
                .configFileCount(configFiles != null ? (long) configFiles.size() : 0)
                .libraryCount(libraryCount != null ? libraryCount : 0)
                .servletCount(checkList != null ? generatedCheckList(checkList, "servletExtends") : 0)
                .ejbJtaCount(eeModuleCount != null ? eeModuleCount : 0)
                .specificIpIncludeCount(checkList != null ? generatedCheckList(checkList, "ipPatterns") : 0)
                .lookupPatternCount(checkList != null ? generatedCheckList(checkList, "lookups") : 0)
                .customPatternCount(checkList != null ? generatedCheckList(checkList, "customPatterns") : 0)
                .deprecatedApiClassCount(deprecatedList != null ? (long) deprecatedList.size() : 0)
                .deleteApiClassCount(removedList != null ? (long) removedList.size() : 0)
                .build();
    }

    /**
     * Servlet Extends, ip Patterns, lookup Patterns, custom Patterns 갯수를 구해온다.
     */
    private long generatedCheckList(JSONArray checkList, String type) {
        long count = 0L;

        for (Object obj : checkList) {
            JSONObject node = (JSONObject) obj;
            if ("servletExtends".equals(type)) {
                JSONArray servletExtends = (JSONArray) node.get("servletExtends");
                if (servletExtends != null) {
                    count += servletExtends.size();
                }
            } else if ("ipPatterns".equals(type)) {
                JSONArray ipPatterns = (JSONArray) node.get("ipPatterns");
                if (ipPatterns != null) {
                    count += ipPatterns.size();
                }
            } else if ("lookups".equals(type)) {
                JSONArray lookups = (JSONArray) node.get("lookups");
                if (lookups != null) {
                    count += lookups.size();
                }
            } else if ("customPatterns".equals(type)) {
                JSONArray customPatterns = (JSONArray) node.get("customPatterns");
                if (customPatterns != null) {
                    count += customPatterns.size();
                }
            }
        }

        return count;
    }
}
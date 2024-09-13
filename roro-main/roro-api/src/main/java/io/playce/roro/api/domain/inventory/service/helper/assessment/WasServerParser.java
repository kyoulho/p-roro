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

import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.dto.assessment.AssessmentResultDto;
import io.playce.roro.common.dto.inventory.middleware.InstanceResponse;
import io.playce.roro.common.dto.inventory.middleware.MiddlewareResponse;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Map;

import static io.playce.roro.api.domain.inventory.service.helper.ServiceReportHelper.DELIMITER;
import static io.playce.roro.api.domain.inventory.service.helper.ServiceReportHelper.UNDER_BAR_DELIMITER;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
@RequiredArgsConstructor
public class WasServerParser implements AssessmentParser {

    private final InstanceResponse instanceResponse;

    @Override
    public Object parse(Object object, String assessment) throws Exception {
        MiddlewareResponse middleware = (MiddlewareResponse) object;

        if (assessment == null) {
            return null;
        }

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(assessment);
        JSONObject jsonObj = (JSONObject) obj;
        org.json.simple.JSONObject wasData = JsonUtil.getJsonObject(jsonObj);

        JSONObject engine = (JSONObject) wasData.get("engine");
        JSONObject instance = (JSONObject) wasData.get("instance");
        String enginePath = null;
        String engineVersion = null;
        if (engine != null) {
            if (engine.containsKey("path")) {
//                enginePath = (String) engine.get("path");
                enginePath = MWCommonUtil.getStringValue(engine, "path");
            }

            if (engine.containsKey("version")) {
//                engineVersion = (String) engine.get("version");
                engineVersion = MWCommonUtil.getStringValue(engine, "version");
            }
        }

//        if (engineName == null) {
//            return null;
//        }

        // domain home
        String domainHome = null;
        if (instance != null && instance.containsKey("domainHome")) {
//            domainHome = (String) instance.get("domainHome");
            domainHome = MWCommonUtil.getStringValue(instance, "domainHome");
        }

        // java version
        String javaVersion = null;
        if (instance != null && instance.containsKey("javaVersion")) {
//            javaVersion = (String) instance.get("javaVersion");
            javaVersion = MWCommonUtil.getStringValue(instance, "javaVersion");
        }

        // config files
        JSONObject configFile = null;
        JSONArray configFileArr = new JSONArray();
        if (instance != null && instance.containsKey("configFiles") && instance.get("configFiles") != null) {
            if (instance.get("configFiles") instanceof JSONObject) {
                configFile = (JSONObject) instance.get("configFiles");
            } else if (instance.get("configFiles") instanceof JSONArray) {
                configFileArr = (JSONArray) instance.get("configFiles");
            }
        }

        if (Domain1013.TOMCAT.name().equals(middleware.getInventoryDetailTypeCode())) {
            // tomcat config files
            String confFiles = null;
            if (configFile != null) {
                if (configFile.containsKey("SERVER") && configFile.get("SERVER") != null) {
                    JSONObject server = (JSONObject) configFile.get("SERVER");

                    if (server.containsKey("path") && server.get("path") != null) {
                        if (confFiles != null) {
                            confFiles += DELIMITER + server.get("path");
                        } else {
//                            confFiles = (String) server.get("path");
                            confFiles = MWCommonUtil.getStringValue(server, "path");
                        }
                    }
                }

                if (configFile.containsKey("ENV") && configFile.get("ENV") != null) {
                    JSONObject env = (JSONObject) configFile.get("ENV");

                    if (env.containsKey("path") && env.get("path") != null) {
                        if (confFiles != null) {
                            confFiles += DELIMITER + env.get("path");
                        } else {
//                            confFiles = (String) env.get("path");
                            confFiles = MWCommonUtil.getStringValue(env, "path");
                        }
                    }
                }

                if (configFile.containsKey("CONTEXT") && configFile.get("SERVER") != null) {
                    JSONObject context = (JSONObject) configFile.get("CONTEXT");

                    if (context.containsKey("path") && context.get("path") != null) {
                        if (confFiles != null) {
                            confFiles += DELIMITER + context.get("path");
                        } else {
//                            confFiles = (String) context.get("path");
                            confFiles = MWCommonUtil.getStringValue(context, "path");
                        }
                    }
                }
            }

            // run user
            String runUser = null;
            if (instance != null && instance.containsKey("runUser")) {
//                runUser = (String) instance.get("runUser");
                runUser = MWCommonUtil.getStringValue(instance, "runUser");
            }

            // min & max heap
            String minHeap = null;
            String maxHeap = null;
            if (instance != null) {
                if (instance.containsKey("minHeap")) {
//                    minHeap = (String) instance.get("minHeap");
                    minHeap = MWCommonUtil.getStringValue(instance, "minHeap");
                }

                if (instance.containsKey("maxHeap")) {
//                    maxHeap = (String) instance.get("maxHeap");
                    maxHeap = MWCommonUtil.getStringValue(instance, "maxHeap");
                }
            }

            return AssessmentResultDto.WasProperty.builder()
                    .enginePath(enginePath != null ? enginePath.replaceAll("\"", "") : "")
                    .engineVersion(engineVersion != null ? engineVersion.replaceAll("\"", "") : "")
                    .domainHome("")
                    .minHeap(minHeap != null ? minHeap.replaceAll("\"", "") : "")
                    .maxHeap(maxHeap != null ? maxHeap.replaceAll("\"", "") : "")
                    .clusterUsed(false)
                    .runUser(runUser != null ? runUser.replaceAll("\"", "") : "")
                    .javaVersion(javaVersion != null ? javaVersion.replaceAll("\"", "") : "")
                    .configFiles(confFiles)
                    .build();

        } else if (Domain1013.WEBLOGIC.name().equals(middleware.getInventoryDetailTypeCode())) {
            boolean isClusterUsed = false;
            if (instance != null && instance.containsKey("cluster")) {
                isClusterUsed = (instance.get("cluster") != null);
            }

            // config files
            StringBuilder confFiles = generateConfigFiles(configFileArr);

            // heap size & run user & vm option
            String minHeap = null;
            String maxHeap = null;
            String runUser = null;
            if (instance != null && instance.containsKey("instances")) {
                JSONArray instances = (JSONArray) instance.get("instances");

                for (Object ins : instances) {
                    JSONObject instanceObj = (JSONObject) ins;
//                    String instanceName = (String) instanceObj.get("name");
                    String instanceName = MWCommonUtil.getStringValue(instanceObj, "name");

                    if (instanceResponse.getMiddlewareInstanceName().equals(instanceName)) {
//                        minHeap = (String) instanceObj.get("minHeap");
                        minHeap = MWCommonUtil.getStringValue(instanceObj, "minHeap");
//                        maxHeap = (String) instanceObj.get("maxHeap");
                        maxHeap = MWCommonUtil.getStringValue(instanceObj, "maxHeap");
//                        runUser = (String) instanceObj.get("runUser");
                        runUser = MWCommonUtil.getStringValue(instanceObj, "runUser");
                    }
                }
            }

            return AssessmentResultDto.WasProperty.builder()
                    .enginePath(enginePath != null ? enginePath.replaceAll("\"", "") : "")
                    .engineVersion(engineVersion != null ? engineVersion.replaceAll("\"", "") : "")
                    .domainHome(domainHome)
                    .minHeap(minHeap != null ? minHeap.replaceAll("\"", "") : "")
                    .maxHeap(maxHeap != null ? maxHeap.replaceAll("\"", "") : "")
                    .clusterUsed(isClusterUsed)
                    .runUser(runUser != null ? runUser.replaceAll("\"", "") : "")
                    .javaVersion(javaVersion != null ? javaVersion.replaceAll("\"", "") : "")
                    .configFiles(confFiles != null ? confFiles.toString() : "")
                    .build();
        } else if (Domain1013.JEUS.name().equals(middleware.getInventoryDetailTypeCode())) {
            Double version = null;
            if (engineVersion != null) {
                version = Double.parseDouble(engineVersion);
            }

            boolean isClusterUsed = false;
            if (instance != null && instance.containsKey("clusters")) {
                JSONObject cluster = (JSONObject) instance.get("clusters");
                if (cluster != null) {
                    isClusterUsed = (cluster.containsKey("cluster") && cluster.get("cluster") != null);
                }
            }

            // config files
            StringBuilder confFiles = generateConfigFiles(configFileArr);

            // heap size & run user & vm option
            String minHeap = null;
            String maxHeap = null;
            String runUser = null;
            if (instance != null && instance.containsKey("instances")) {
                JSONArray instances = (JSONArray) instance.get("instances");

                for (Object ins : instances) {
                    JSONObject instanceObj = (JSONObject) ins;
//                    String instanceName = (String) instanceObj.get("name");
                    String instanceName = MWCommonUtil.getStringValue(instanceObj, "name");

                    if (version != null) {
                        // Jeus 버전에 따라 instance 값을 가져온다.
                        if (version > 6.0) {
                            if (instanceResponse.getMiddlewareInstanceName().equals(instanceName)) {
//                                minHeap = (String) instanceObj.get("minHeap");
                                minHeap = MWCommonUtil.getStringValue(instanceObj, "minHeap");
//                                maxHeap = (String) instanceObj.get("maxHeap");
                                maxHeap = MWCommonUtil.getStringValue(instanceObj, "maxHeap");
//                                runUser = (String) instanceObj.get("runUser");
                                runUser = MWCommonUtil.getStringValue(instanceObj, "runUser");
                            }
                        } else {
                            JSONObject engines = (JSONObject) instanceObj.get("engines");
                            JSONArray containers = (JSONArray) engines.get("engineContainer");

                            if (containers != null) {
                                for (Object con : containers) {
                                    JSONObject conatinerObject = (JSONObject) con;
                                    String name = instanceName + UNDER_BAR_DELIMITER + conatinerObject.get("name");
                                    if (instanceResponse.getMiddlewareInstanceName().equals(name)) {
//                                        minHeap = (String) conatinerObject.get("minHeap");
                                        minHeap = MWCommonUtil.getStringValue(conatinerObject, "minHeap");
//                                        maxHeap = (String) conatinerObject.get("maxHeap");
                                        maxHeap = MWCommonUtil.getStringValue(conatinerObject, "maxHeap");
//                                        runUser = (String) conatinerObject.get("runUser");
                                        runUser = MWCommonUtil.getStringValue(conatinerObject, "runUser");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return AssessmentResultDto.WasProperty.builder()
                    .enginePath(enginePath != null ? enginePath.replaceAll("\"", "") : "")
                    .engineVersion(engineVersion != null ? engineVersion.replaceAll("\"", "") : "")
                    .domainHome(domainHome)
                    .minHeap(minHeap != null ? minHeap.replaceAll("\"", "") : "")
                    .maxHeap(maxHeap != null ? maxHeap.replaceAll("\"", "") : "")
                    .clusterUsed(isClusterUsed)
                    .runUser(runUser != null ? runUser.replaceAll("\"", "") : "")
                    .javaVersion(javaVersion != null ? javaVersion.replaceAll("\"", "") : "")
                    .configFiles(confFiles != null ? confFiles.toString() : "")
                    .build();
        } else if (Domain1013.WSPHERE.name().equals(middleware.getInventoryDetailTypeCode())) {
            boolean isClusterUsed = false;
            if (instance != null && instance.containsKey("clusters")) {
                isClusterUsed = (instance.get("clusters") != null);
            }

            // heap size & run user & vm option
            String minHeap = null;
            String maxHeap = null;
            String runUser = null;
            if (instance != null && instance.containsKey("instances")) {
                JSONArray instances = (JSONArray) instance.get("instances");

                for (Object ins : instances) {
                    JSONObject instanceObj = (JSONObject) ins;
//                    String instanceName = (String) instanceObj.get("name");
                    String instanceName = MWCommonUtil.getStringValue(instanceObj, "name");

                    if (instanceResponse.getMiddlewareInstanceName().equals(instanceName)) {
//                        minHeap = (String) instanceObj.get("minHeap");
                        minHeap = MWCommonUtil.getStringValue(instanceObj, "minHeap");
//                        maxHeap = (String) instanceObj.get("maxHeap");
                        maxHeap = MWCommonUtil.getStringValue(instanceObj, "maxHeap");
//                        runUser = (String) instanceObj.get("runUser");
                        runUser = MWCommonUtil.getStringValue(instanceObj, "runUser");
                    }
                }
            }

            return AssessmentResultDto.WasProperty.builder()
                    .enginePath(enginePath != null ? enginePath.replaceAll("\"", "") : "")
                    .engineVersion(engineVersion != null ? engineVersion.replaceAll("\"", "") : "")
                    .domainHome(domainHome)
                    .minHeap(minHeap != null ? minHeap.replaceAll("\"", "") : "")
                    .maxHeap(maxHeap != null ? maxHeap.replaceAll("\"", "") : "")
                    .clusterUsed(isClusterUsed)
                    .runUser(runUser != null ? runUser.replaceAll("\"", "") : "")
                    .javaVersion(javaVersion != null ? javaVersion.replaceAll("\"", "") : "")
                    .configFiles("")
                    .build();
        } else if (Domain1013.JBOSS.name().equals(middleware.getInventoryDetailTypeCode())) {
            boolean isClusterUsed = false;
            if (instance != null && instance.containsKey("clusters")) {
                isClusterUsed = (instance.get("clusters") != null);
            }

            // heap size & run user & vm option
            String minHeap = null;
            String maxHeap = null;
            String runUser = null;
            if (instance != null && instance.containsKey("instances")) {
                JSONArray instances = (JSONArray) instance.get("instances");

                for (Object ins : instances) {
                    JSONObject instanceObj = (JSONObject) ins;
//                    String instanceName = (String) instanceObj.get("name");
                    String instanceName = MWCommonUtil.getStringValue(instanceObj, "name");

                    minHeap = MWCommonUtil.getStringValue(instanceObj, "minHeap");
//                        maxHeap = (String) instanceObj.get("maxHeap");
                    maxHeap = MWCommonUtil.getStringValue(instanceObj, "maxHeap");
//                        runUser = (String) instanceObj.get("runUser");
                    runUser = MWCommonUtil.getStringValue(instanceObj, "runUser");

                }
            }
            StringBuffer fileBuffer = new StringBuffer();
            Map<String, String> configFiles = (Map<String, String>) instance.get("configFileName");
            int i = 1;
            for (String key : configFiles.keySet()) {
                String fileName = configFiles.get(key);
                fileBuffer.append(fileName);
                if (configFiles.size() > 1 && i != configFiles.size()) {
                    fileBuffer.append(", ");
                }
                i++;
            }

            return AssessmentResultDto.WasProperty.builder()
                    .enginePath(enginePath != null ? enginePath.replaceAll("\"", "") : "")
                    .engineVersion(engineVersion != null ? engineVersion.replaceAll("\"", "") : "")
                    .domainHome((String) instance.get("domainPath"))
                    .minHeap(minHeap != null ? minHeap.replaceAll("\"", "") : "")
                    .maxHeap(maxHeap != null ? maxHeap.replaceAll("\"", "") : "")
                    .clusterUsed(isClusterUsed)
                    .runUser(runUser != null ? runUser.replaceAll("\"", "") : "")
                    .javaVersion(javaVersion != null ? javaVersion.replaceAll("\"", "") : "")
                    .configFiles(fileBuffer.toString())
                    .build();
        } else {
            return null;
        }
    }

    @Nullable
    private StringBuilder generateConfigFiles(JSONArray configFileArr) {
        StringBuilder confFiles = null;
        if (configFileArr != null) {
            for (Object c : configFileArr) {
                JSONObject confObj = (JSONObject) c;
                if (confObj.containsKey("path") && confObj.get("path") != null) {
                    if (confFiles != null) {
                        confFiles.append(DELIMITER).append(confObj.get("path"));
                    } else {
//                        confFiles = new StringBuilder((String) confObj.get("path"));
                        confFiles = new StringBuilder(MWCommonUtil.getStringValue(confObj, "path"));
                    }
                }
            }
        }
        return confFiles;
    }
}
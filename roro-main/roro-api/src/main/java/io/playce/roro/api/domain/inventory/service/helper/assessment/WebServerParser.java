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
 * Jaeeon Bae       3월 22, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper.assessment;

import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.dto.assessment.AssessmentResultDto;
import io.playce.roro.common.dto.inventory.middleware.MiddlewareResponse;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.playce.roro.api.domain.inventory.service.helper.ServiceReportHelper.DELIMITER;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
public class WebServerParser implements AssessmentParser {

    @Override
    public Object parse(Object object, String assessment) throws Exception {
        MiddlewareResponse middleware = (MiddlewareResponse) object;

        if (assessment == null) {
            return null;
        }

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(assessment);
        JSONObject jsonObj = (JSONObject) obj;
        org.json.simple.JSONObject webData = JsonUtil.getJsonObject(jsonObj);

        JSONObject engine = (JSONObject) webData.get("engine");
        JSONObject instance = (JSONObject) webData.get("instance");
        String engineName = null;
        String enginePath = null;
        String engineVersion = null;
        if (engine != null) {
            if (engine.containsKey("name")) {
//                engineName = (String) engine.get("name");
                engineName = MWCommonUtil.getStringValue(engine, "name");
            }

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

        // general
        JSONObject general = null;
        if (instance.containsKey("general")) {
            if (instance.get("general") != null) {
                general = (JSONObject) instance.get("general");
            }
        }

        // run User
        String runUser = null;
        if (engine.containsKey("runUser")) {
            if (engine.get("runUser") != null) {
//                runUser = (String) engine.get("runUser");
                runUser = MWCommonUtil.getStringValue(engine, "runUser");
            }
        }

        /**
         * Apache & Oracle & IBM
         * result json 결과를 가지고 필요한 데이터 파싱
         */
        if (Domain1013.APACHE.name().equals(middleware.getInventoryDetailTypeCode())) {
            List<String> ports = new ArrayList<>();
            if (instance.containsKey("general")) {

                if (general != null && general.containsKey("listenPort")) {
                    JSONArray listenPort = (JSONArray) general.get("listenPort");

                    String listen = null;
                    if (listenPort != null) {
                        for (Object lp : listenPort) {
                            if (lp instanceof Long) {
                                listen = String.valueOf(lp);
                            } else if (lp instanceof String) {
                                listen = (String) lp;
                            }
                            ports.add(listen);
                        }
                    }
                }
            }

            List<String> includeFiles = new ArrayList<>();
            if (instance.containsKey("configFiles")) {
                JSONArray files = (JSONArray) instance.get("configFiles");

                for (Object file : files) {
                    JSONObject fileObj = (JSONObject) file;

                    if (fileObj.containsKey("path") && fileObj.get("path") != null) {
                        String f = MWCommonUtil.getStringValue(fileObj, "path");
                        includeFiles.add(f);
                    }
                }
            }

            // ssl
            boolean isSslUsed = false;
            if (general != null && general.containsKey("useSsl")) {
                if (general.get("useSsl") != null) {
                    isSslUsed = (boolean) general.get("useSsl");
                }
            }

            // document root
            String documentRoot = null;
            if (general != null && general.containsKey("documentRoot")) {
                if (general.get("documentRoot") != null) {
//                    documentRoot = (String) general.get("documentRoot");
                    documentRoot = MWCommonUtil.getStringValue(general, "documentRoot");
                }
            }

            return AssessmentResultDto.WebProperty.builder()
                    .enginePath(enginePath != null ? enginePath.replaceAll("\"", "") : "")
                    .engineVersion(engineVersion != null ? engineVersion.replaceAll("\"", "") : "")
                    .listenPort(CollectionUtils.isNotEmpty(ports) ? !ports.isEmpty() ? Long.parseLong(ports.get(0)) : null : null)
                    .sslUsed(isSslUsed)
                    .sslPort(ports.size() > 1 ? Long.parseLong(ports.get(1)) : null)
                    .documentRoot(documentRoot != null ? documentRoot.replaceAll("\"", "") : "")
                    .logDirectory("")
                    .includeFiles(String.join(DELIMITER, includeFiles))
                    .runUser(runUser != null ? runUser.replaceAll("\"", "") : "")
                    .build();
        } else if (Domain1013.WEBTOB.name().equals(middleware.getInventoryDetailTypeCode())) {
            // port and document root
            Long port = null;
            String documentRoot = null;
            if (instance.containsKey("nodes")) {
                JSONArray nodes = new JSONArray();
                if (instance.get("nodes") instanceof JSONObject) {
                    JSONObject nodesObj = (JSONObject) instance.get("nodes");
                    nodes.add(nodesObj);
                } else if (instance.get("nodes") instanceof JSONArray) {
                    nodes = (JSONArray) instance.get("nodes");
                }

                if (!nodes.isEmpty()) {
                    JSONObject nodeObj = (JSONObject) nodes.get(0);
                    if (nodeObj.containsKey("port") && nodeObj.get("port") != null) {
                        if (nodeObj.get("port") instanceof Long) {
                            port = (Long) nodeObj.get("port");
                        } else if (nodeObj.get("port") instanceof String && StringUtils.isNotEmpty((String) nodeObj.get("port"))) {
                            port = Long.parseLong((String) nodeObj.get("port"));
                        }
                    }

                    if (nodeObj.containsKey("docRoot") && nodeObj.get("docRoot") != null) {
//                        documentRoot = (String) nodeObj.get("docRoot");
                        documentRoot = MWCommonUtil.getStringValue(nodeObj, "docRoot");
                    }
                }
            }

            // ssl
            boolean isSslUsed = false;
            if (instance.containsKey("ssls")) {
                isSslUsed = (instance.get("ssls") != null);
            }

            // ssl port
            Long sslPort = null;
            if (instance.containsKey("vhosts")) {
                JSONArray vhosts = new JSONArray();
                if (instance.get("vhosts") instanceof JSONObject) {
                    JSONObject vhostObj = (JSONObject) instance.get("vhosts");
                    vhosts.add(vhostObj);
                } else if (instance.get("vhosts") instanceof JSONArray) {
                    vhosts = (JSONArray) instance.get("vhosts");
                }

                if (!vhosts.isEmpty()) {
                    JSONObject nodeObj = (JSONObject) vhosts.get(0);
                    if (nodeObj.containsKey("port") && nodeObj.get("port") != null) {
                        sslPort = (Long) nodeObj.get("port");
                    }
                }
            }

            List<String> includeFiles = new ArrayList<>();
            if (instance.containsKey("configFiles")) {
                JSONArray files = (JSONArray) instance.get("configFiles");

                for (Object file : files) {
                    JSONObject fileObj = (JSONObject) file;

                    if (fileObj.containsKey("path") && fileObj.get("path") != null) {
//                        String f = (String) fileObj.get("path");
                        String f = MWCommonUtil.getStringValue(fileObj, "path");
                        includeFiles.add(f);
                    }
                }
            }

            return AssessmentResultDto.WebProperty.builder()
                    .enginePath(enginePath != null ? enginePath.replaceAll("\"", "") : "")
                    .engineVersion(engineVersion != null ? engineVersion.replaceAll("\"", "") : "")
                    .listenPort(port)
                    .sslUsed(isSslUsed)
                    .sslPort(sslPort)
                    .documentRoot(documentRoot != null ? documentRoot.replaceAll("\"", "") : "")
                    .logDirectory("")
                    .includeFiles(String.join(DELIMITER, includeFiles))
                    .runUser(runUser != null ? runUser.replaceAll("\"", "") : "")
                    .build();
        } else if (Domain1013.NGINX.name().equals(middleware.getInventoryDetailTypeCode())) {
            List<String> ports = new ArrayList<>();
            if (instance.containsKey("general")) {

                if (general != null && general.containsKey("listenPort")) {
                    JSONArray listenPort = (JSONArray) general.get("listenPort");

                    String listen = null;
                    if (listenPort != null) {
                        for (Object lp : listenPort) {
                            if (lp instanceof Long) {
                                listen = String.valueOf(lp);
                            } else if (lp instanceof String) {
                                listen = (String) lp;
                            }
                            ports.add(listen);
                        }
                    }
                }
            }

            List<String> includeFiles = new ArrayList<>();
            if (instance.containsKey("configFiles")) {
                JSONArray files = (JSONArray) instance.get("configFiles");

                for (Object file : files) {
                    JSONObject fileObj = (JSONObject) file;

                    if (fileObj.containsKey("path") && fileObj.get("path") != null) {
                        String f = MWCommonUtil.getStringValue(fileObj, "path");
                        includeFiles.add(f);
                    }
                }
            }

            // ssl
            boolean isSslUsed = false;
            if (general != null && general.containsKey("ssl")) {
                if (general.get("ssl") != null) {
                    isSslUsed = (boolean) general.get("ssl");
                }
            }

            // document root
            String documentRoot = null;
            List<String> documentRoots = new ArrayList<>();
            JSONObject http = (JSONObject) instance.get("http");
            if (http != null && http.get("servers") != null) {
                JSONArray servers = (JSONArray) http.get("servers");

                for (Object server : servers) {
                    JSONObject serverObj = (JSONObject) server;
                    if (serverObj.containsKey("root") && serverObj.get("root") != null) {
                        String root = MWCommonUtil.getStringValue(serverObj, "root");
                        documentRoots.add(root);
                    }

                    if (serverObj.containsKey("locations") && serverObj.get("locations") != null) {
                        JSONArray locations = (JSONArray) serverObj.get("locations");
                        for (Object location : locations) {
                            JSONObject locationObj = (JSONObject) location;
                            if (locationObj.containsKey("root") && locationObj.get("root") != null) {
                                String root = MWCommonUtil.getStringValue(locationObj, "root");
                                documentRoots.add(root);
                            }
                        }
                    }
                }
            }
            documentRoot = String.join(", ", documentRoots.stream().distinct().collect(Collectors.toList()));

            return AssessmentResultDto.WebProperty.builder()
                    .enginePath(enginePath != null ? enginePath.replaceAll("\"", "") : "")
                    .engineVersion(engineVersion != null ? engineVersion.replaceAll("\"", "") : "")
                    .listenPort(CollectionUtils.isNotEmpty(ports) ? !ports.isEmpty() ? Long.parseLong(ports.get(0)) : null : null)
                    .sslUsed(isSslUsed)
                    .sslPort(ports.size() > 1 ? Long.parseLong(ports.get(1)) : null)
                    .documentRoot(documentRoot != null ? documentRoot.replaceAll("\"", "") : "")
                    .logDirectory("")
                    .includeFiles(String.join(DELIMITER, includeFiles))
                    .runUser(runUser != null ? runUser.replaceAll("\"", "") : "")
                    .build();
        } else {
            return null;
        }
    }
}
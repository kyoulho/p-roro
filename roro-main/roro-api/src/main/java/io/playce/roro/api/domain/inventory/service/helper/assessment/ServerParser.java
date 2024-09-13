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
 * Jaeeon Bae       3월 21, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper.assessment;

import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.dto.assessment.AssessmentResultDto;
import io.playce.roro.common.dto.inventory.server.ServerResponse;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.ServerDiskInformation;
import io.playce.roro.jpa.repository.ServerDiskInformationRepository;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
@RequiredArgsConstructor
public class ServerParser implements AssessmentParser {

    private static final String LISTEN_STATUS = "LISTENING";

    private final ServerDiskInformationRepository serverDiskInformationRepository;

    @Override
    public Object parse(Object object, String assessment) throws Exception {
        ServerResponse server = (ServerResponse) object;
        DecimalFormat df = new DecimalFormat("###,###");

        if (assessment == null) {
            return null;
        }

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(assessment);
        JSONObject jsonObj = (JSONObject) obj;
        org.json.simple.JSONObject serverData = JsonUtil.getJsonObject(jsonObj);

        String modelName = null;
        if (Domain1013.WINDOWS.name().equals(server.getInventoryDetailTypeCode())) {
            JSONObject systemInformation = serverData.containsKey("systemInformation") ? (JSONObject) serverData.get("systemInformation") : null;

            String systemBootTime = null;
            String totalPhysicalMemory = null;
            String osName = null;
            String osVersion = null;
            if (systemInformation != null) {
                modelName = systemInformation.containsKey("manufacturer") ? String.valueOf(systemInformation.get("manufacturer")) : null;

                if (systemInformation.containsKey("model")) {
                    if (!StringUtils.isEmpty(modelName)) {
                        modelName += " - " + systemInformation.get("model");
                    } else {
                        modelName = String.valueOf(systemInformation.get("model"));
                    }
                }

                if (systemInformation.containsKey("systemBootTime")) {
                    systemBootTime = String.valueOf(systemInformation.get("systemBootTime")).replaceAll("\"", "");
                }

                if (systemInformation.containsKey("totalPhysicalMemory")) {
                    totalPhysicalMemory = String.valueOf(systemInformation.get("totalPhysicalMemory")).replaceAll("\"", "");
                }

                if (systemInformation.containsKey("osName")) {
                    osName = String.valueOf(systemInformation.get("osName")).replaceAll("\"", "");
                }

                if (systemInformation.containsKey("osVersion")) {
                    osVersion = String.valueOf(systemInformation.get("osVersion")).replaceAll("\"", "");
                }
            }

            // cpu
            String cpu = null;
            if (serverData.containsKey("cpu") && serverData.get("cpu") != null) {
                JSONObject cpuObj = (JSONObject) serverData.get("cpu");

                if (cpuObj.containsKey("name") && cpuObj.get("name") != null) {
//                    cpu = (String) cpuObj.get("name");
                    cpu = MWCommonUtil.getStringValue(cpuObj, "name");
                }
            }

            long totalDisk = 0;
            long freeSpace = 0;
            if (serverData.containsKey("disks")) {
                JSONArray disks = new JSONArray();

                if (serverData.get("disks") instanceof JSONObject) {
                    JSONObject diskObj = (JSONObject) serverData.get("disks");
                    disks.add(diskObj);
                } else if (serverData.get("disks") instanceof JSONArray) {
                    disks = (JSONArray) serverData.get("disks");
                }

                for (Object jobj : disks) {
                    JSONObject disk = (JSONObject) jobj;

                    if (disk.containsKey("totalSize")) {
                        String totalDiskStr = String.valueOf(disk.get("totalSize"))
                                .replaceAll(" GB", "").replaceAll("\"", "");
                        totalDisk += Double.parseDouble(totalDiskStr);
                    }

                    if (disk.containsKey("freeSpace")) {
                        String freeSpaceStr = String.valueOf(disk.get("freeSpace"))
                                .replaceAll(" GB", "").replaceAll("\"", "");
                        freeSpace += Double.parseDouble(freeSpaceStr);
                    }
                }
            }

            // user & group
            int userCount = 0;
            int groupCount = 0;
            if (serverData.containsKey("localUsers")) {
                JSONArray users = (JSONArray) serverData.get("localUsers");
                userCount = users.size();
            }

            if (serverData.containsKey("localGroupUsers")) {
                JSONArray groups = (JSONArray) serverData.get("localGroupUsers");
                groupCount = groups.size();
            }

            // listen port
            List<String> listenPort = new ArrayList<>();
            if (serverData.containsKey("ports")) {
                JSONArray ports = (JSONArray) serverData.get("ports");

                for (Object port : ports) {
                    JSONObject portObj = (JSONObject) port;

//                    if (LISTEN_STATUS.equals(portObj.get("state")) && isValidIp4Address((String) portObj.get("localAddress"))) {
                    if (LISTEN_STATUS.equals(portObj.get("state")) && isValidIp4Address(MWCommonUtil.getStringValue(portObj, "localAddress"))) {
//                        listenPort.add((String) portObj.get("localPort"));
                        listenPort.add(MWCommonUtil.getStringValue(portObj, "localPort"));
                    }
                }
            }

            // architecture
            String architecture = null;
            if (systemInformation != null) {
                // systemType
                if (systemInformation.containsKey("systemType") && systemInformation.get("systemType") != null) {
//                    architecture = (String) systemInformation.get("systemType");
                    architecture = MWCommonUtil.getStringValue(systemInformation, "systemType");
                }
            }

            return AssessmentResultDto.ServerProperty.builder()
                    .cpu(cpu != null ? cpu : "")
                    .upTime(systemBootTime != null ? systemBootTime : "")
                    .totalDisk(totalDisk != 0 ? df.format(totalDisk) + " GB" : "")
                    .freeDisk(freeSpace != 0 ? df.format(freeSpace) + " GB" : "")
                    .model(modelName != null ? modelName.replaceAll("\"", "") : "")
                    .memory(totalPhysicalMemory != null ? totalPhysicalMemory : "")
                    .osName(osName != null ? osName : "")
                    .osVersion(osVersion != null ? osVersion : "")
                    .kernel(osVersion != null ? StringUtils.substringBefore(osVersion, " ") : "")
                    .userCount(userCount)
                    .groupCount(groupCount)
                    .listenPort(String.join(DELIMITER, listenPort))
                    .architecture(architecture != null ? architecture : "")
                    .build();

        } else {
            // windows 외 다른 OS 파싱
            String osName = null;
            String osVersion = null;
            if (serverData.containsKey("distribution")) {
//                osName = (String) serverData.get("distribution");
                osName = MWCommonUtil.getStringValue(serverData, "distribution");
            }
            if (serverData.containsKey("distributionRelease")) {
//                osVersion = (String) serverData.get("distributionRelease");
                osVersion = MWCommonUtil.getStringValue(serverData, "distributionRelease");
            }

            // cpu
            String cpu = null;
            if (serverData.containsKey("cpu") && serverData.get("cpu") != null) {
                JSONObject cpuObj = (JSONObject) serverData.get("cpu");

                if (cpuObj.containsKey("processor") && cpuObj.get("processor") != null) {
//                    cpu = (String) cpuObj.get("processor");
                    cpu = MWCommonUtil.getStringValue(cpuObj, "processor");
                }
            }

            String time = null;
            if (serverData.containsKey("uptime")) {

                String upTimeString = null;
                Long upTimeLong = null;
                if (serverData.get("uptime") instanceof String) {
//                    upTimeString = (String) serverData.get("uptime");
                    upTimeString = MWCommonUtil.getStringValue(serverData, "uptime");
                } else if (serverData.get("uptime") instanceof Long) {
//                    upTimeLong = (Long) serverData.get("uptime");
                    upTimeLong = MWCommonUtil.getLongValue(serverData, "uptime");
                }

                if (upTimeString != null || upTimeLong != null) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    double uptime = 0;
                    if (upTimeString != null && upTimeLong == null) {
                        uptime = Double.parseDouble(upTimeString);
                    } else if (upTimeString == null && upTimeLong != null) {
                        uptime = upTimeLong.doubleValue();
                    }

                    // 시스템에 따라 * 1000이 불필요할 수도 있음.
                    if (uptime < 10000000000L) {
                        uptime *= 1000L;
                    }

                    time = format.format(uptime);
                }
            }

            // total size and free size
            long totalDiskSize = 0;
            long freeDiskSize = 0;
            List<ServerDiskInformation> serverDiskInformation = serverDiskInformationRepository.findByServerInventoryId(server.getServerInventoryId());
            for (ServerDiskInformation disk : serverDiskInformation) {
                totalDiskSize += disk.getTotalSize();
                freeDiskSize += disk.getFreeSize();
            }
            totalDiskSize = totalDiskSize / 1024;
            freeDiskSize = freeDiskSize / 1024;

            // Memory
            String memory = null;
            if (serverData.containsKey("memory")) {
                JSONObject memoryObj = (JSONObject) serverData.get("memory");
                if (memoryObj.containsKey("memTotalMB")) {
                    if (memoryObj.get("memTotalMB") != null) {
                        // String mem = (String) memoryObj.get("memTotalMB");
                        String mem = MWCommonUtil.getStringValue(memoryObj, "memTotalMB");
                        memory = df.format(Long.parseLong(mem)) + " MB";
                    }
                }
            }

            // user & group 수
            int userCount = 0;
            int groupCount = 0;
            if (serverData.containsKey("users") && serverData.get("users") != null) {
                JSONObject userObj = (JSONObject) serverData.get("users");
                userCount = userObj.size();
            }

            if (serverData.containsKey("groups") && serverData.get("groups") != null) {
                JSONObject groupObj = (JSONObject) serverData.get("groups");
                groupCount = groupObj.size();
            }

            // listen port
            List<String> listenPort = new ArrayList<>();
            if (serverData.containsKey("portList")) {
                JSONObject portList = (JSONObject) serverData.get("portList");

                if (portList.containsKey("listen")) {
                    JSONArray ports = (JSONArray) portList.get("listen");

                    for (Object pobj : ports) {
                        JSONObject portObj = (JSONObject) pobj;
//                        if (isValidIp4Address((String) portObj.get("bindAddr"))) {
                        if (isValidIp4Address(MWCommonUtil.getStringValue(portObj, "bindAddr"))) {
//                            listenPort.add((String) portObj.get("port"));
                            listenPort.add(MWCommonUtil.getStringValue(portObj, "port"));
                        }
                    }
                }
            }

            // architecture
            String architecture = null;
            if (serverData.containsKey("architecture") && serverData.get("architecture") != null) {
//                architecture = (String) serverData.get("architecture");
                architecture = MWCommonUtil.getStringValue(serverData, "architecture");
            }

            // kernel
            String kernel = null;
            if (serverData.containsKey("kernel") && serverData.get("kernel") != null) {
//                kernel = (String) serverData.get("kernel");
                kernel = MWCommonUtil.getStringValue(serverData, "kernel");
            }

            if (Domain1013.LINUX.name().equals(server.getInventoryDetailTypeCode())) {
                modelName = null;
                if (serverData.containsKey("productName")) {
//                    modelName = (String) serverData.get("productName");
                    modelName = MWCommonUtil.getStringValue(serverData, "productName");
                }

                return AssessmentResultDto.ServerProperty.builder()
                        .cpu(cpu != null ? cpu : "")
                        .upTime(time != null ? time : "")
                        .totalDisk(totalDiskSize != 0 ? df.format(totalDiskSize) + " GB" : "")
                        .freeDisk(freeDiskSize != 0 ? df.format(freeDiskSize) + " GB" : "")
                        .model(modelName != null ? modelName.replaceAll("\"", "") : "")
                        .memory(memory != null ? memory : "")
                        .osName(osName != null ? osName : "")
                        .osVersion(osVersion != null ? osVersion : "")
                        .kernel(kernel != null ? kernel : "")
                        .userCount(userCount)
                        .groupCount(groupCount)
                        .listenPort(String.join(DELIMITER, listenPort))
                        .architecture(architecture != null ? architecture : "")
                        .build();
            } else if (Domain1013.AIX.name().equals(server.getInventoryDetailTypeCode())) {
                modelName = null;
                if (serverData.containsKey("productName")) {
//                    modelName = (String) serverData.get("productName");
                    modelName = MWCommonUtil.getStringValue(serverData, "productName");
                }

                return AssessmentResultDto.ServerProperty.builder()
                        .cpu(cpu != null ? cpu : "")
                        .upTime(time != null ? time : "")
                        .totalDisk(totalDiskSize != 0 ? df.format(totalDiskSize) + " GB" : "")
                        .freeDisk(freeDiskSize != 0 ? df.format(freeDiskSize) + " GB" : "")
                        .model(modelName != null ? modelName.replaceAll("\"", "") : "")
                        .memory(memory != null ? memory : "")
                        .osName(osName != null ? osName : "")
                        .osVersion(osVersion != null ? osVersion : "")
                        .kernel(kernel != null ? kernel : "")
                        .userCount(userCount)
                        .groupCount(groupCount)
                        .listenPort(String.join(DELIMITER, listenPort))
                        .architecture(architecture != null ? architecture : "")
                        .build();
            } else if (Domain1013.SUNOS.name().equals(server.getInventoryDetailTypeCode())) {
                modelName = null;
                if (serverData.containsKey("productName")) {
//                    modelName = (String) serverData.get("productName");
                    modelName = MWCommonUtil.getStringValue(serverData, "productName");
                }

                return AssessmentResultDto.ServerProperty.builder()
                        .cpu(cpu != null ? cpu : "")
                        .upTime(time != null ? time : "")
                        .totalDisk(totalDiskSize != 0 ? df.format(totalDiskSize) + " GB" : "")
                        .freeDisk(freeDiskSize != 0 ? df.format(freeDiskSize) + " GB" : "")
                        .model(modelName != null ? modelName.replaceAll("\"", "") : "")
                        .memory(memory != null ? memory : "")
                        .osName(osName != null ? osName : "")
                        .osVersion(osVersion != null ? osVersion : "")
                        .kernel(kernel != null ? kernel : "")
                        .userCount(userCount)
                        .groupCount(groupCount)
                        .listenPort(String.join(DELIMITER, listenPort))
                        .architecture(architecture != null ? architecture : "")
                        .build();
            } else if (Domain1013.HP_UX.name().equals(server.getInventoryDetailTypeCode())) {
                modelName = null;
                if (serverData.containsKey("productName")) {
//                    modelName = (String) serverData.get("productName");
                    modelName = MWCommonUtil.getStringValue(serverData, "productName");
                }

                return AssessmentResultDto.ServerProperty.builder()
                        .cpu(cpu != null ? cpu : "")
                        .upTime(time != null ? time : "")
                        .totalDisk(totalDiskSize != 0 ? df.format(totalDiskSize) + " GB" : "")
                        .freeDisk(freeDiskSize != 0 ? df.format(freeDiskSize) + " GB" : "")
                        .model(modelName != null ? modelName.replaceAll("\"", "") : "")
                        .memory(memory != null ? memory : "")
                        .osName(osName != null ? osName : "")
                        .osVersion(osVersion != null ? osVersion : "")
                        .kernel(kernel != null ? kernel : "")
                        .userCount(userCount)
                        .groupCount(groupCount)
                        .listenPort(String.join(DELIMITER, listenPort))
                        .architecture(architecture != null ? architecture : "")
                        .build();
            } else {
                return null;
            }
        }
    }

    private boolean isValidIp4Address(String ipAddress) {
        final InetAddressValidator validator = InetAddressValidator.getInstance();

        return ipAddress.equals("*") || validator.isValidInet4Address(ipAddress);
    }
}
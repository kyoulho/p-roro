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
 * Jeongho Baek   2월 18, 2021		First Draft.
 */
package io.playce.roro.mw.asmt.websphere.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.WasAnalyzerUtil;
import io.playce.roro.mw.asmt.websphere.dto.WebSphereAssessmentDto;
import io.playce.roro.mw.asmt.websphere.dto.WebSphereAssessmentResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.XML;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.modelmapper.ModelMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.playce.roro.common.util.CommandUtil.getFileContentCommand;
import static io.playce.roro.common.util.CommandUtil.getSshCommandResultTrim;
import static io.playce.roro.common.util.JsonUtil.getJsonArray;
import static io.playce.roro.common.util.JsonUtil.getJsonObject;
import static io.playce.roro.common.util.StringUtil.splitToArrayByCrlf;
import static io.playce.roro.mw.asmt.util.MWCommonUtil.*;
import static io.playce.roro.mw.asmt.util.WasAnalyzerUtil.isExistDirectory;
import static io.playce.roro.mw.asmt.util.WasAnalyzerUtil.removeBackSlashAndSlash;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Slf4j
public class WebSphereHelper {

    private static final List<String> EXCLUDE_DEFAULT_APPLICATIONS = new ArrayList<>(Arrays.asList("WebSphereWSDM", "isclite", "OTiS", "ibmasyncrsp"));
    private static final List<String> EXCLUDE_DEFAULT_DATASOURCE = new ArrayList<>(Arrays.asList("built-in-derby-datasource", "OTiSDataSource", "DefaultEJBTimerDataSource"));

    private static final String PROFILE_REGISTRY_NAME = "profileRegistry.xml";
    private static final String SECURITY_NAME = "security.xml";
    private static final String CLUSTER_NAME = "cluster.xml";
    private static final String SERVER_INDEX_NAME = "serverindex.xml";
    private static final String SERVER_NAME = "server.xml";
    private static final String DYNAMIC_WEIGHT_CONTROLLER_NAME = "dynamicweightcontroller.xml";
    private static final String RESOURCE_NAME = "resources.xml";
    private static final String IBM_EDITION_METADATA = "ibm-edition-metadata.props";
    private static final String DEPLOYMENT_FILE = "deployment.xml";
    private static final String WC_DEFAULT_HOST = "WC_defaulthost";
    private static final String DIRECTORY_NAME_COMMAND = " | grep \\/$ | cut -d '/' -f 1 | tr ' ' '*'";

    public static class EngineHelper {

        public static WebSphereAssessmentResult.Engine getEngine(TargetHost targetHost, String wasInstallRootPath, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
            log.debug("*** Execute Method : getEngine ***");

            String wasInfoPath = File.separator + "properties" + File.separator + "version" + File.separator + "WAS.product";
            String wasInfoCommand = getFileContentCommand(wasInstallRootPath + wasInfoPath);
            String responseString = getSshCommandResultTrim(targetHost, wasInfoCommand);

            WebSphereAssessmentResult.Engine engine = new WebSphereAssessmentResult.Engine();

            if (StringUtils.isNotEmpty(responseString)) {
                String xmlToJson = stringValueOf(XML.toJSONObject(responseString));

                log.debug("-- Get Product Info.");
                log.debug("-- File Path ==> {}", wasInstallRootPath + wasInfoPath);
                log.debug("-- File Content ==> \n{}", responseString);
                log.debug("-- File Convert JSON ==> \n{}", xmlToJson);

                // -- Start File Save -- //
                WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), wasInstallRootPath + wasInfoPath, responseString, CommonProperties.getWorkDir(), strategy);
                // -- End File Save -- //

                JSONObject jsonObject = getJsonObject(xmlToJson);

                engine = modelMapper.map(jsonObject.get("product"), WebSphereAssessmentResult.Engine.class);
            }

            engine.setPath(wasInstallRootPath);

            return engine;
        }
    }

    public static class ProfileHelper {

        public static List<WebSphereAssessmentResult.Profile> getProfile(TargetHost targetHost, String wasInstallRootPath, ModelMapper modelMapper) throws InterruptedException {
            log.debug("*** Execute Method : getProfiles ***");

            String profilePath = wasInstallRootPath + File.separator + "properties" + File.separator + PROFILE_REGISTRY_NAME;
            String profileCommand = getFileContentCommand(profilePath);
            String responseString = getSshCommandResultTrim(targetHost, profileCommand);

            String xmlToJson = stringValueOf(XML.toJSONObject(responseString));

            log.debug("-- Get Profile Info.");
            log.debug("-- File Path ==> {}", profilePath);
            log.debug("-- File Content ==> \n{}", responseString);
            log.debug("-- File Convert JSON ==> \n{}", xmlToJson);

            List<WebSphereAssessmentResult.Profile> profiles = new ArrayList<>();

            if (StringUtils.isNotEmpty(xmlToJson)) {
                JSONObject jsonObject = getJsonObject(xmlToJson);
                JSONObject profilesJsonObject = getJsonObject((JSONObject) jsonObject.get("profiles"));

                if (profilesJsonObject.get("profile") instanceof JSONObject) {
                    JSONObject profileJsonObject = getJsonObject((JSONObject) profilesJsonObject.get("profile"));

                    profiles.add(modelMapper.map(profileJsonObject, WebSphereAssessmentResult.Profile.class));
                } else {
                    JSONArray profileJsonArray = getJsonArray((JSONArray) profilesJsonObject.get("profile"));

                    for (Object profileObject : profileJsonArray) {
                        JSONObject profileJsonObject = getJsonObject((JSONObject) profileObject);
                        profiles.add(modelMapper.map(profileJsonObject, WebSphereAssessmentResult.Profile.class));
                    }
                }

//                for (WebSphereAssessmentResult.Profile profile : profiles) {
//                    profile.setMinHeap(getHeapSize(targetHost, profile.getName(), "-Xms"));
//                    profile.setMaxHeap(getHeapSize(targetHost, profile.getName(), "-Xmx"));
//                    profile.setRunUser(getRunUser(targetHost, profile.getName()));
//                }
            }

            return profiles;
        }
    }

    public static class GeneralHelper {

        public static String getJavaVersion(TargetHost targetHost, List<WebSphereAssessmentResult.Profile> profiles, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            log.debug("*** Execute Method : getJavaVersion ***");

            String javaVersion = "";

            for (WebSphereAssessmentResult.Profile profile : profiles) {
                String setupCmdLinePath = profile.getPath() + File.separator + "bin" + File.separator + "setupCmdLine.sh";
                String scriptCommand = ". " + setupCmdLinePath + "; echo $JAVA_HOME";
                String responseString = getSshCommandResultTrim(targetHost, scriptCommand, false);

                // -- Start File Save -- //
                String setupCmdLineCommand = getFileContentCommand(setupCmdLinePath);
                String setupCmdLineContent = getSshCommandResultTrim(targetHost, setupCmdLineCommand);
                WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), setupCmdLinePath, setupCmdLineContent, CommonProperties.getWorkDir(), strategy);
                // -- End File Save -- //

                log.debug("-- JavaHome Command ==> {}", scriptCommand);
                log.debug("-- JavaHome ==> {}", responseString);

                if (responseString.isEmpty()) {
                    return WasAnalyzerUtil.getJavaVersion(targetHost, commandConfig, strategy);
                } else {
                    return WasAnalyzerUtil.getJavaVersion(targetHost, responseString, commandConfig, strategy);
                }
            }

            return javaVersion;
        }

        public static String getJavaVendor(TargetHost targetHost, List<WebSphereAssessmentResult.Profile> profiles, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            log.debug("*** Execute Method : getJavaVendor ***");

            String javaVendor = "";

            for (WebSphereAssessmentResult.Profile profile : profiles) {
                String setupCmdLinePath = profile.getPath() + File.separator + "bin" + File.separator + "setupCmdLine.sh";
                String scriptCommand = ". " + setupCmdLinePath + "; echo $JAVA_HOME";
                String responseString = getSshCommandResultTrim(targetHost, scriptCommand, false);

                log.debug("-- JavaHome Command ==> {}", scriptCommand);
                log.debug("-- JavaHome ==> {}", responseString);

                if (responseString.isEmpty()) {
                    javaVendor = WasAnalyzerUtil.getJavaVendor(targetHost, commandConfig, strategy);
                } else {
                    javaVendor = WasAnalyzerUtil.getJavaVendor(targetHost, responseString, commandConfig, strategy);
                }
            }

            if (StringUtils.isEmpty(javaVendor) &&
                    StringUtils.isNotEmpty(getJavaVersion(targetHost, profiles, commandConfig, strategy))) {
                javaVendor = ORACLE_JAVA_VENDOR;
            }

            return javaVendor;
        }

        public static WebSphereAssessmentDto.DirectoryStructure getDirectoryStructure(List<WebSphereAssessmentResult.Profile> profiles, MiddlewareInventory middleware, TargetHost targetHost, ModelMapper modelMapper, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            WebSphereAssessmentDto.DirectoryStructure directoryStructure = new WebSphereAssessmentDto.DirectoryStructure();

            // -- Start  Profile --//
            List<WebSphereAssessmentDto.ProfileFileInfo> profileFileInfos = new ArrayList<>();
            for (WebSphereAssessmentResult.Profile profile : profiles) {
                profileFileInfos.add(modelMapper.map(profile, WebSphereAssessmentDto.ProfileFileInfo.class));
            }
            directoryStructure.setProfileFileInfos(profileFileInfos);
            // -- End   Profile --//

            // -- Start  Cell --//
            for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
                List<WebSphereAssessmentDto.CellFileInfo> cellFileInfos = CellHelper.getCellFileInfos(middleware.getCellName(), profileFileInfo);
                profileFileInfo.setCellFileInfos(cellFileInfos);
            }
            // -- End   Cell --//

            // -- Start  Cluster and Node --//
            // Cluster, Node는 동일 레벨.
            for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
                for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                    List<WebSphereAssessmentDto.ClusterFileInfo> clusterFileInfos = ClusterHelper.getClusterFileInfos(targetHost, cellFileInfo, commandConfig, strategy);
                    List<WebSphereAssessmentDto.NodeFileInfo> nodeFileInfos = NodeHelper.getNodeFileInfos(middleware.getNodeName(), cellFileInfo);

                    cellFileInfo.setClusterFileInfos(clusterFileInfos);
                    cellFileInfo.setNodeFileInfos(nodeFileInfos);
                }
            }
            // -- End   Cluster and Node --//

            // -- Start  Server --//
            for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
                for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                    for (WebSphereAssessmentDto.NodeFileInfo nodeFileInfo : cellFileInfo.getNodeFileInfos()) {
                        List<WebSphereAssessmentDto.ServerFileInfo> serverFileInfos = ServerHelper.getServerFileInfos(middleware.getName(), nodeFileInfo);

                        nodeFileInfo.setServerFileInfos(serverFileInfos);
                    }
                }
            }
            // -- End  Server --//

            return directoryStructure;
        }

        public static WebSphereAssessmentDto.DirectoryStructure getDirectoryStructure(List<WebSphereAssessmentResult.Profile> profiles, TargetHost targetHost, ModelMapper modelMapper, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            WebSphereAssessmentDto.DirectoryStructure directoryStructure = new WebSphereAssessmentDto.DirectoryStructure();

            // -- Start  Profile --//
            List<WebSphereAssessmentDto.ProfileFileInfo> profileFileInfos = new ArrayList<>();
            for (WebSphereAssessmentResult.Profile profile : profiles) {
                profileFileInfos.add(modelMapper.map(profile, WebSphereAssessmentDto.ProfileFileInfo.class));
            }
            directoryStructure.setProfileFileInfos(profileFileInfos);
            // -- End   Profile --//

            // -- Start  Cell --//
            for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
                List<WebSphereAssessmentDto.CellFileInfo> cellFileInfos = CellHelper.getCellFileInfos(targetHost, profileFileInfo);
                profileFileInfo.setCellFileInfos(cellFileInfos);
            }
            // -- End   Cell --//

            // -- Start  Cluster and Node --//
            // Cluster, Node는 동일 레벨.
            for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
                for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                    List<WebSphereAssessmentDto.ClusterFileInfo> clusterFileInfos = ClusterHelper.getClusterFileInfos(targetHost, cellFileInfo, commandConfig, strategy);
                    List<WebSphereAssessmentDto.NodeFileInfo> nodeFileInfos = NodeHelper.getNodeFileInfos(targetHost, cellFileInfo);

                    cellFileInfo.setClusterFileInfos(clusterFileInfos);
                    cellFileInfo.setNodeFileInfos(nodeFileInfos);
                }
            }
            // -- End   Cluster and Node --//

            // -- Start  Server --//
            for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
                for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                    for (WebSphereAssessmentDto.NodeFileInfo nodeFileInfo : cellFileInfo.getNodeFileInfos()) {
                        List<WebSphereAssessmentDto.ServerFileInfo> serverFileInfos = ServerHelper.getServerFileInfos(targetHost, nodeFileInfo, commandConfig, strategy);

                        nodeFileInfo.setServerFileInfos(serverFileInfos);
                    }
                }
            }
            // -- End  Server --//

            return directoryStructure;
        }

    }

    public static class CellHelper {

        public static List<WebSphereAssessmentDto.CellFileInfo> getCellFileInfos(String cellName, WebSphereAssessmentDto.ProfileFileInfo profileFileInfo) {
            List<WebSphereAssessmentDto.CellFileInfo> cellFileInfos = new ArrayList<>();

            WebSphereAssessmentDto.CellFileInfo cellFileInfo = new WebSphereAssessmentDto.CellFileInfo();
            cellFileInfo.setPath(profileFileInfo.getPath() + File.separator + "config" + File.separator + "cells" + File.separator + cellName);
            cellFileInfo.setName(cellName);
            cellFileInfos.add(cellFileInfo);

            return cellFileInfos;
        }

        public static List<WebSphereAssessmentDto.CellFileInfo> getCellFileInfos(TargetHost targetHost, WebSphereAssessmentDto.ProfileFileInfo profileFileInfo) throws InterruptedException {
            String cellCommand = "ls -F " + profileFileInfo.getPath() + File.separator + "config" + File.separator + "cells" + DIRECTORY_NAME_COMMAND;
            String responseString = getSshCommandResultTrim(targetHost, cellCommand).replaceAll("\\*", " ");

            List<WebSphereAssessmentDto.CellFileInfo> cellFileInfos = new ArrayList<>();

            if (StringUtils.isNotEmpty(responseString)) {
                String[] cellNames = splitToArrayByCrlf(responseString);

                for (String cellName : cellNames) {
                    WebSphereAssessmentDto.CellFileInfo cellFileInfo = new WebSphereAssessmentDto.CellFileInfo();
                    cellFileInfo.setPath(profileFileInfo.getPath() + File.separator + "config" + File.separator + "cells" + File.separator + removeBackSlashAndSlash(cellName));
                    cellFileInfo.setName(removeBackSlashAndSlash(cellName));
                    cellFileInfos.add(cellFileInfo);
                }
            }

            return cellFileInfos;
        }
    }

    public static class ClusterHelper {

        public static List<WebSphereAssessmentDto.ClusterFileInfo> getClusterFileInfos(TargetHost targetHost, WebSphereAssessmentDto.CellFileInfo cellFileInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            List<WebSphereAssessmentDto.ClusterFileInfo> clusterFileInfos = new ArrayList<>();
            String clusterPath = cellFileInfo.getPath() + File.separator + "clusters";

            if (isExistDirectory(targetHost, clusterPath, commandConfig, strategy)) {
                String clusterCommand = "ls -F " + clusterPath + DIRECTORY_NAME_COMMAND;
                String responseString = getSshCommandResultTrim(targetHost, clusterCommand).replaceAll("\\*", " ");

                if (StringUtils.isNotEmpty(responseString)) {
                    String[] clusterNames = splitToArrayByCrlf(responseString);

                    for (String clusterName : clusterNames) {
                        WebSphereAssessmentDto.ClusterFileInfo clusterFileInfo = new WebSphereAssessmentDto.ClusterFileInfo();
                        clusterFileInfo.setPath(cellFileInfo.getPath() + File.separator + "clusters" + File.separator + removeBackSlashAndSlash(clusterName));
                        clusterFileInfo.setName(removeBackSlashAndSlash(clusterName));
                        clusterFileInfos.add(clusterFileInfo);
                    }

                }
            }

            return clusterFileInfos;
        }

        public static List<WebSphereAssessmentDto.ClusterParse> getCluster(TargetHost targetHost, WebSphereAssessmentDto.ClusterFileInfo clusterFileInfo, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
            String clusterFilePath = clusterFileInfo.getPath() + File.separator + CLUSTER_NAME;
            String clusterCommand = getFileContentCommand(clusterFilePath);
            String responseString = getSshCommandResultTrim(targetHost, clusterCommand);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), clusterFilePath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            String xmlToJson = XML.toJSONObject(responseString).toString();
            log.debug("===> {}", CLUSTER_NAME);
            log.debug(xmlToJson);

            if (StringUtils.isNotEmpty(xmlToJson)) {
                JSONObject jsonObject = getJsonObject(xmlToJson);
                JSONObject xmiJsonObject = getJsonObject((JSONObject) jsonObject.get("xmi:XMI"));
                JSONArray serverJsonArray = new JSONArray();

                if (xmiJsonObject.isEmpty()) {
                    if (jsonObject.get("topology.cluster:ServerCluster") instanceof JSONObject) {
                        serverJsonArray.add(getJsonObject((JSONObject) jsonObject.get("topology.cluster:ServerCluster")));
                    } else {
                        serverJsonArray = getJsonArray((JSONArray) jsonObject.get("topology.cluster:ServerCluster"));
                    }
                } else {
                    if (xmiJsonObject.get("topology.cluster:ServerCluster") instanceof JSONObject) {
                        serverJsonArray.add(getJsonObject((JSONObject) xmiJsonObject.get("topology.cluster:ServerCluster")));
                    } else {
                        serverJsonArray = getJsonArray((JSONArray) xmiJsonObject.get("topology.cluster:ServerCluster"));
                    }
                }

                List<WebSphereAssessmentDto.ClusterParse> clusterParses = new ArrayList<>();

                for (Object serverJsonObject : serverJsonArray) {
                    JSONObject serverClusterJsonObject = getJsonObject((JSONObject) serverJsonObject);

                    WebSphereAssessmentDto.ClusterParse clusterParse = new WebSphereAssessmentDto.ClusterParse();
                    clusterParse.setName(stringValueOf(serverClusterJsonObject.get("name")));
                    clusterParse.setNodeGroupName(stringValueOf(serverClusterJsonObject.get("nodeGroupName")));

                    List<WebSphereAssessmentDto.ClusterMember> clusterMembers = new ArrayList<>();

                    if (serverClusterJsonObject.get("members") instanceof JSONObject) {
                        JSONObject memberJsonObject = getJsonObject((JSONObject) serverClusterJsonObject.get("members"));

                        clusterMembers.add(modelMapper.map(memberJsonObject, WebSphereAssessmentDto.ClusterMember.class));
                    } else {
                        JSONArray memberJsonArray = getJsonArray((JSONArray) serverClusterJsonObject.get("members"));

                        for (Object memberObject : memberJsonArray) {
                            JSONObject memberJsonObject = getJsonObject((JSONObject) memberObject);
                            clusterMembers.add(modelMapper.map(memberJsonObject, WebSphereAssessmentDto.ClusterMember.class));
                        }
                    }

                    clusterParse.setMembers(clusterMembers);

                    clusterParses.add(clusterParse);
                }

                return clusterParses;
            }

            return null;
        }

        public static List<WebSphereAssessmentResult.Member> getMembers(List<WebSphereAssessmentDto.ClusterMember> clusterMembers) {
            if (CollectionUtils.isNotEmpty(clusterMembers)) {
                List<WebSphereAssessmentResult.Member> members = new ArrayList<>();

                for (WebSphereAssessmentDto.ClusterMember clusterMember : clusterMembers) {
                    WebSphereAssessmentResult.Member member = new WebSphereAssessmentResult.Member();
                    member.setNodeName(clusterMember.getNodeName());
                    member.setNodeName(clusterMember.getMemberName());
                    members.add(member);
                }

                return members;
            }

            return null;
        }

        public static Boolean getClusterDwlm(TargetHost targetHost, WebSphereAssessmentDto.ClusterFileInfo clusterFileInfo, GetInfoStrategy strategy) throws InterruptedException {
            String clusterDwlmFilePath = clusterFileInfo.getPath() + File.separator + DYNAMIC_WEIGHT_CONTROLLER_NAME;
            String clusterDwlmCommand = getFileContentCommand(clusterDwlmFilePath);
            String responseString = getSshCommandResultTrim(targetHost, clusterDwlmCommand);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), clusterDwlmFilePath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            if (StringUtils.isEmpty(responseString)) {
                return null;
            } else {
                String xmlToJson = XML.toJSONObject(responseString).toString();
                log.debug("===> {}", DYNAMIC_WEIGHT_CONTROLLER_NAME);
                log.debug(xmlToJson);

                JSONObject jsonObject = getJsonObject(xmlToJson);
                JSONObject dwlmJsonObject = getJsonObject((JSONObject) jsonObject.get("dynamicwtctrlr:DynamicWtCtrlr"));

                return dwlmJsonObject.get("enabled") != null;
            }

        }

        public static String getClusterName(List<WebSphereAssessmentResult.Cluster> clusters, String profileName, String cellName, String nodeName, String serverName) {
            if (CollectionUtils.isNotEmpty(clusters)) {
                for (WebSphereAssessmentResult.Cluster cluster : clusters) {
                    if (cluster.getProfileName().equals(profileName) && cluster.getCellName().equals(cellName)) {
                        if (CollectionUtils.isNotEmpty(cluster.getMembers())) {
                            for (WebSphereAssessmentResult.Member member : cluster.getMembers()) {
                                if (member.getNodeName().equals(nodeName) && member.getServerName().equals(serverName)) {
                                    return cluster.getClusterName();
                                }
                            }
                        }
                    }
                }
            }

            return "";
        }

    }

    public static class NodeHelper {

        public static List<WebSphereAssessmentDto.NodeFileInfo> getNodeFileInfos(String nodeName, WebSphereAssessmentDto.CellFileInfo cellFileInfo) {
            List<WebSphereAssessmentDto.NodeFileInfo> nodeFileInfos = new ArrayList<>();

            WebSphereAssessmentDto.NodeFileInfo nodeFileInfo = new WebSphereAssessmentDto.NodeFileInfo();
            nodeFileInfo.setPath(cellFileInfo.getPath() + File.separator + "nodes" + File.separator + nodeName);
            nodeFileInfo.setName(nodeName);
            nodeFileInfos.add(nodeFileInfo);

            return nodeFileInfos;
        }

        public static List<WebSphereAssessmentDto.NodeFileInfo> getNodeFileInfos(TargetHost targetHost, WebSphereAssessmentDto.CellFileInfo cellFileInfo) throws InterruptedException {
            String nodeCommand = "ls -F " + cellFileInfo.getPath() + File.separator + "nodes" + DIRECTORY_NAME_COMMAND;
            String responseString = getSshCommandResultTrim(targetHost, nodeCommand).replaceAll("\\*", " ");

            List<WebSphereAssessmentDto.NodeFileInfo> nodeFileInfos = new ArrayList<>();

            if (StringUtils.isNotEmpty(responseString)) {
                String[] nodeNames = splitToArrayByCrlf(responseString);

                for (String nodeName : nodeNames) {
                    WebSphereAssessmentDto.NodeFileInfo nodeFileInfo = new WebSphereAssessmentDto.NodeFileInfo();
                    nodeFileInfo.setPath(cellFileInfo.getPath() + File.separator + "nodes" + File.separator + removeBackSlashAndSlash(nodeName));
                    nodeFileInfo.setName(removeBackSlashAndSlash(nodeName));
                    nodeFileInfos.add(nodeFileInfo);
                }
            }

            return nodeFileInfos;
        }

        public static WebSphereAssessmentDto.ServerIndex getServerIndex(TargetHost targetHost, WebSphereAssessmentDto.NodeFileInfo nodeFileInfo, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
            String serverIndexNamePath = nodeFileInfo.getPath() + File.separator + SERVER_INDEX_NAME;
            String serverIndexCommand = getFileContentCommand(serverIndexNamePath);
            String responseString = getSshCommandResultTrim(targetHost, serverIndexCommand);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), serverIndexNamePath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            WebSphereAssessmentDto.ServerIndex serverIndex = new WebSphereAssessmentDto.ServerIndex();

            if (StringUtils.isNotEmpty(responseString)) {
                String xmlToJson = XML.toJSONObject(responseString).toString();
                log.debug("==> serverindex.xml");
                log.debug(xmlToJson);

                JSONObject jsonObject = getJsonObject(xmlToJson);
                JSONObject serverIndexJsonObject = getJsonObject((JSONObject) jsonObject.get("serverindex:ServerIndex"));

                serverIndex.setHostName(getStringValue(serverIndexJsonObject, "hostName"));
                serverIndex.setServerEntries(getServerEntries(serverIndexJsonObject.get("serverEntries"), modelMapper));
            }

            return serverIndex;
        }

        private static List<WebSphereAssessmentDto.ServerEntries> getServerEntries(Object object, ModelMapper modelMapper) {
            List<WebSphereAssessmentDto.ServerEntries> serverEntries = new ArrayList<>();

            if (object instanceof JSONObject) {
                JSONObject serverEntryJsonObject = getJsonObject((JSONObject) object);
                serverEntries.add(getServerEntry(serverEntryJsonObject, modelMapper));
            } else {
                JSONArray serverEntryJsonArray = getJsonArray((JSONArray) object);

                for (Object serverEntryObject : serverEntryJsonArray) {
                    JSONObject serverEntriesJsonObject = getJsonObject((JSONObject) serverEntryObject);
                    serverEntries.add(getServerEntry(serverEntriesJsonObject, modelMapper));
                }
            }

            return serverEntries;
        }

        private static WebSphereAssessmentDto.ServerEntries getServerEntry(Object object, ModelMapper modelMapper) {
            JSONObject serverEntryJsonObject = getJsonObject((JSONObject) object);

            WebSphereAssessmentDto.ServerEntries serverEntry = new WebSphereAssessmentDto.ServerEntries();
            serverEntry.setServerType(getStringValue(serverEntryJsonObject, "serverType"));
            serverEntry.setServerName(getStringValue(serverEntryJsonObject, "serverName"));

            List<WebSphereAssessmentDto.SpecialEndpoints> specialEndpoints = new ArrayList<>();
            JSONArray specialEndpointsJsonArray = getJsonArray((JSONArray) serverEntryJsonObject.get("specialEndpoints"));

            for (Object specialEndpointObject : specialEndpointsJsonArray) {
                JSONObject specialEndpointJsonObject = getJsonObject((JSONObject) specialEndpointObject);
                specialEndpoints.add(modelMapper.map(specialEndpointJsonObject, WebSphereAssessmentDto.SpecialEndpoints.class));
            }

            serverEntry.setSpecialEndpoints(specialEndpoints);
            serverEntry.setDeployedApplications(getDeployedApplications(serverEntryJsonObject));

            return serverEntry;
        }

        private static List<String> getDeployedApplications(Object object) {
            JSONObject serverEntryJsonObject = getJsonObject((JSONObject) object);

            List<String> deployedApplications = new ArrayList<>();

            if (serverEntryJsonObject.get("deployedApplications") == null) {
                return null;
            } else {
                if (serverEntryJsonObject.get("deployedApplications") instanceof String) {
                    deployedApplications.add(serverEntryJsonObject.get("deployedApplications").toString());
                } else {
                    JSONArray deployedApplicationsJsonArray = getJsonArray((JSONArray) serverEntryJsonObject.get("deployedApplications"));

                    for (Object deployedApplicationsObject : deployedApplicationsJsonArray) {
                        deployedApplications.add(deployedApplicationsObject.toString());
                    }
                }
            }

            return deployedApplications;
        }

    }

    public static class ServerHelper {

        public static List<WebSphereAssessmentDto.ServerFileInfo> getServerFileInfos(String serverName, WebSphereAssessmentDto.NodeFileInfo nodeFileInfo) {
            List<WebSphereAssessmentDto.ServerFileInfo> serverFileInfos = new ArrayList<>();

            WebSphereAssessmentDto.ServerFileInfo serverFileInfo = new WebSphereAssessmentDto.ServerFileInfo();
            serverFileInfo.setPath(nodeFileInfo.getPath() + File.separator + "servers" + File.separator + serverName);
            serverFileInfo.setName(serverName);

            serverFileInfos.add(serverFileInfo);

            return serverFileInfos;
        }

        public static List<WebSphereAssessmentDto.ServerFileInfo> getServerFileInfos(TargetHost targetHost, WebSphereAssessmentDto.NodeFileInfo nodeFileInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            List<WebSphereAssessmentDto.ServerFileInfo> serverFileInfos = new ArrayList<>();
            String serverPath = nodeFileInfo.getPath() + File.separator + "servers";

            if (isExistDirectory(targetHost, serverPath, commandConfig, strategy)) {
                String serverCommand = "ls -F " + serverPath + DIRECTORY_NAME_COMMAND;
                String responseString = getSshCommandResultTrim(targetHost, serverCommand).replaceAll("\\*", " ");

                if (StringUtils.isNotEmpty(responseString)) {
                    String[] serverNames = splitToArrayByCrlf(responseString);

                    for (String serverName : serverNames) {
                        WebSphereAssessmentDto.ServerFileInfo serverFileInfo = new WebSphereAssessmentDto.ServerFileInfo();
                        serverFileInfo.setPath(nodeFileInfo.getPath() + File.separator + "servers" + File.separator + removeBackSlashAndSlash(serverName));
                        serverFileInfo.setName(removeBackSlashAndSlash(serverName));

                        serverFileInfos.add(serverFileInfo);
                    }
                }
            }

            return serverFileInfos;
        }

        public static String getHostName(TargetHost targetHost, WebSphereAssessmentDto.NodeFileInfo nodeFileInfo, String serverName, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
            WebSphereAssessmentDto.ServerIndex serverIndex = NodeHelper.getServerIndex(targetHost, nodeFileInfo, modelMapper, strategy);

            List<WebSphereAssessmentDto.ServerEntries> serverEntries = serverIndex.getServerEntries();

            for (WebSphereAssessmentDto.ServerEntries serverEntry : serverEntries) {
                if (serverEntry.getServerName().equals(serverName)) {
                    return serverIndex.getHostName();
                }
            }

            return "";
        }

        public static Integer getServerPort(TargetHost targetHost, WebSphereAssessmentDto.NodeFileInfo nodeFileInfo, String clusterMemberName, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
            WebSphereAssessmentDto.ServerIndex serverIndex = NodeHelper.getServerIndex(targetHost, nodeFileInfo, modelMapper, strategy);

            List<WebSphereAssessmentDto.ServerEntries> serverEntries = serverIndex.getServerEntries();

            for (WebSphereAssessmentDto.ServerEntries serverEntry : serverEntries) {
                if (serverEntry.getServerName().equals(clusterMemberName)) {
                    List<WebSphereAssessmentDto.SpecialEndpoints> specialEndpoints = serverEntry.getSpecialEndpoints();
                    for (WebSphereAssessmentDto.SpecialEndpoints specialEndpoint : specialEndpoints) {
                        if (specialEndpoint.getEndPointName().equals(WC_DEFAULT_HOST)) {
                            return specialEndpoint.getEndPoint().getPort();
                        }
                    }
                }
            }

            return null;
        }

        public static WebSphereAssessmentResult.Config getConfig(TargetHost targetHost, WebSphereAssessmentDto.ServerFileInfo serverFileInfo, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
            String serverPath = serverFileInfo.getPath() + File.separator + SERVER_NAME;
            String serverCommand = getFileContentCommand(serverPath);
            String responseString = getSshCommandResultTrim(targetHost, serverCommand);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), serverPath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            String xmlToJson = XML.toJSONObject(responseString).toString();
            log.debug("==> server.xml");
            log.debug(xmlToJson);

            WebSphereAssessmentResult.Config config = new WebSphereAssessmentResult.Config();
            WebSphereAssessmentResult.JvmEntries jvmEntries = new WebSphereAssessmentResult.JvmEntries();

            JSONObject jsonObject = getJsonObject(xmlToJson);
            JSONObject serverJsonObject = getJsonObject((JSONObject) jsonObject.get("process:Server"));
            JSONObject processDefinitionsJsonObject = getJsonObject((JSONObject) serverJsonObject.get("processDefinitions"));
            JSONObject ioRedirectJsonObject = getJsonObject((JSONObject) processDefinitionsJsonObject.get("ioRedirect"));
            JSONObject jvmEntriesJsonObject = getJsonObject((JSONObject) processDefinitionsJsonObject.get("jvmEntries"));

            // System Properties
            List<WebSphereAssessmentResult.SystemProperty> systemProperties = new ArrayList<>();

            if (jvmEntriesJsonObject.get("systemProperties") instanceof JSONObject) {
                JSONObject systemPropertyJsonObject = getJsonObject((JSONObject) jvmEntriesJsonObject.get("systemProperties"));
                systemProperties.add(modelMapper.map(systemPropertyJsonObject, WebSphereAssessmentResult.SystemProperty.class));
            } else {
                JSONArray systemPropertiesJsonArray = getJsonArray((JSONArray) jvmEntriesJsonObject.get("systemProperties"));

                for (Object systemPropertiesObject : systemPropertiesJsonArray) {
                    JSONObject systemPropertyJsonObject = getJsonObject((JSONObject) systemPropertiesObject);
                    systemProperties.add(modelMapper.map(systemPropertyJsonObject, WebSphereAssessmentResult.SystemProperty.class));
                }
            }

            // Boot ClassPath
            List<String> bootClasspath = new ArrayList<>();

            if (jvmEntriesJsonObject.get("bootClasspath") instanceof String) {
                bootClasspath.add(jvmEntriesJsonObject.get("bootClasspath").toString());
            } else {
                JSONArray bootClasspathJsonArray = getJsonArray((JSONArray) jvmEntriesJsonObject.get("bootClasspath"));

                for (Object bootClasspathObject : bootClasspathJsonArray) {
                    bootClasspath.add(bootClasspathObject.toString());
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            ConcurrentHashMap<String, Object> propertyMap;
            try {
                propertyMap = objectMapper.readValue(jvmEntriesJsonObject.toJSONString(), ConcurrentHashMap.class);
            } catch (JsonProcessingException e) {
                log.error("object mapper read value error: {}", e.getMessage(), e);
                throw new RoRoException(e.getMessage());
            }

            for (Map.Entry<String, Object> elem : propertyMap.entrySet()) {
                if (elem.getKey().contains("systemProperties")
                        || elem.getKey().contains("bootClasspath")
                        || elem.getKey().contains("xmi:id")) {
                    propertyMap.remove(elem.getKey());
                }
            }

            jvmEntries.setSystemProperties(systemProperties);
            jvmEntries.setBootClasspath(bootClasspath);
            jvmEntries.setProperties(propertyMap);
            config.setIoRedirect(modelMapper.map(ioRedirectJsonObject, WebSphereAssessmentResult.IoRedirect.class));
            config.setJvmEntries(jvmEntries);

            return config;
        }

        public static String getServerType(TargetHost targetHost, WebSphereAssessmentDto.NodeFileInfo nodeFileInfo, String serverName, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
            WebSphereAssessmentDto.ServerIndex serverIndex = NodeHelper.getServerIndex(targetHost, nodeFileInfo, modelMapper, strategy);

            List<WebSphereAssessmentDto.ServerEntries> serverEntries = serverIndex.getServerEntries();

            for (WebSphereAssessmentDto.ServerEntries serverEntry : serverEntries) {
                if (serverEntry.getServerName().equals(serverName)) {
                    return StringUtils.defaultString(serverEntry.getServerType());
                }
            }

            return "";
        }
    }

    public static class DatabaseHelper {

        public static WebSphereAssessmentDto.ResourceParse getResource(TargetHost targetHost, String pathName, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
            WebSphereAssessmentDto.ResourceParse resourceParse = new WebSphereAssessmentDto.ResourceParse();

            String resourcePath = pathName + File.separator + RESOURCE_NAME;
            String resourceCommand = getFileContentCommand(resourcePath);
            String responseString = getSshCommandResultTrim(targetHost, resourceCommand);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), resourcePath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            String xmlToJson = XML.toJSONObject(responseString).toString();

            log.debug("===>" + RESOURCE_NAME);
            log.debug(xmlToJson);

            JSONObject jsonObject = getJsonObject(xmlToJson);
            JSONObject xmiJsonObject = getJsonObject((JSONObject) jsonObject.get("xmi:XMI"));

            List<WebSphereAssessmentDto.JdbcProvider> jdbcProviders = new ArrayList<>();

            if (xmiJsonObject.get("resources.jdbc:JDBCProvider") instanceof JSONObject) {
                JSONObject jdbcProviderJsonObject = getJsonObject((JSONObject) xmiJsonObject.get("resources.jdbc:JDBCProvider"));

                jdbcProviders.add(getJdbcProvider(jdbcProviderJsonObject, modelMapper));
            } else {
                JSONArray jdbcProviderJsonArray = getJsonArray((JSONArray) xmiJsonObject.get("resources.jdbc:JDBCProvider"));

                for (Object jdbcProviderObject : jdbcProviderJsonArray) {
                    JSONObject jdbcProviderJsonObject = getJsonObject((JSONObject) jdbcProviderObject);

                    jdbcProviders.add(getJdbcProvider(jdbcProviderJsonObject, modelMapper));
                }
            }

            resourceParse.setJdbcProvider(jdbcProviders);

            return resourceParse;

        }

        public static List<WebSphereAssessmentResult.DataSource> getJdbcProvider(WebSphereAssessmentDto.ProfileFileInfo profileFileInfo, WebSphereAssessmentDto.ResourceParse resourceParse, String range) {
            List<WebSphereAssessmentResult.DataSource> dataSources = new ArrayList<>();

            for (WebSphereAssessmentDto.JdbcProvider jdbcProvider : resourceParse.getJdbcProvider()) {
                for (WebSphereAssessmentDto.Factories factories : jdbcProvider.getFactories()) {
                    if (!EXCLUDE_DEFAULT_DATASOURCE.contains(factories.getName())) {
                        WebSphereAssessmentResult.DataSource dataSource = new WebSphereAssessmentResult.DataSource();
                        dataSource.setProfileName(profileFileInfo.getName());
                        dataSource.setDataSourceName(factories.getName());
                        dataSource.setJndiName(factories.getJndiName());
                        dataSource.setJdbcProvider(jdbcProvider.getName());
                        dataSource.setAuthDataAlias(factories.getAuthDataAlias());
                        dataSource.setRange(range);
                        if (factories.getConnectionPool() != null) {
                            if (factories.getConnectionPool().getMinConnections() != null) {
                                dataSource.setMin(factories.getConnectionPool().getMinConnections());
                            }
                            if (factories.getConnectionPool().getMaxConnections() != null) {
                                dataSource.setMax(factories.getConnectionPool().getMaxConnections());

                            }
                            if (factories.getConnectionPool().getConnectionTimeout() != null) {
                                dataSource.setTimeout(factories.getConnectionPool().getConnectionTimeout());
                            }
                        }
                        dataSource.setConnectionUrl(getConnectionUrl(jdbcProvider.getImplementationClassName(),
                                factories.getPropertySet().getResourceProperties()));

                        dataSources.add(dataSource);
                    }
                }
            }

            return dataSources;
        }

        private static WebSphereAssessmentDto.JdbcProvider getJdbcProvider(JSONObject jdbcProviderJsonObject, ModelMapper modelMapper) {
            WebSphereAssessmentDto.JdbcProvider jdbcProvider = new WebSphereAssessmentDto.JdbcProvider();

            jdbcProvider.setName(getStringValue(jdbcProviderJsonObject, "name"));
            jdbcProvider.setDescription(getStringValue(jdbcProviderJsonObject, "description"));
            jdbcProvider.setProviderType(getStringValue(jdbcProviderJsonObject, "providerType"));
            jdbcProvider.setImplementationClassName(getStringValue(jdbcProviderJsonObject, "implementationClassName"));
            jdbcProvider.setFactories(getFactories(jdbcProviderJsonObject.get("factories"), modelMapper));

            return jdbcProvider;
        }

        private static List<WebSphereAssessmentDto.Factories> getFactories(Object object, ModelMapper modelMapper) {
            List<WebSphereAssessmentDto.Factories> factories = new ArrayList<>();

            if (object instanceof JSONObject) {
                JSONObject factoriesJsonObject = getJsonObject((JSONObject) object);
                factories.add(getFactories(factoriesJsonObject, modelMapper));
            } else {
                JSONArray factoriesJsonArray = getJsonArray((JSONArray) object);

                for (Object factoriesObject : factoriesJsonArray) {
                    JSONObject factoriesJsonObject = getJsonObject((JSONObject) factoriesObject);
                    WebSphereAssessmentDto.Factories factory = getFactories(factoriesJsonObject, modelMapper);
                    factories.add(factory);
                }
            }

            return factories;
        }

        private static WebSphereAssessmentDto.Factories getFactories(JSONObject factoriesJsonObject, ModelMapper modelMapper) {
            WebSphereAssessmentDto.Factories factory = new WebSphereAssessmentDto.Factories();

            factory.setStatementCacheSize(getLongValue(factoriesJsonObject, "statementCacheSize"));
            factory.setName(getStringValue(factoriesJsonObject, "name"));
            factory.setDescription(getStringValue(factoriesJsonObject, "description"));
            factory.setCategory(getStringValue(factoriesJsonObject, "category"));
            factory.setJndiName(getStringValue(factoriesJsonObject, "jndiName"));
            factory.setAuthDataAlias(getStringValue(factoriesJsonObject, "authDataAlias"));
            factory.setConnectionPool(modelMapper.map(factoriesJsonObject.get("connectionPool"), WebSphereAssessmentDto.ConnectionPool.class));
            factory.setPropertySet(getPropertySet(factoriesJsonObject.get("propertySet"), modelMapper));

            return factory;
        }

        private static WebSphereAssessmentDto.PropertySet getPropertySet(Object object, ModelMapper modelMapper) {
            WebSphereAssessmentDto.PropertySet propertySet = new WebSphereAssessmentDto.PropertySet();
            JSONObject propertySetJsonObject = getJsonObject((JSONObject) object);

            List<WebSphereAssessmentDto.ResourceProperties> resourceProperties = new ArrayList<>();

            if (propertySetJsonObject.get("resourceProperties") instanceof JSONObject) {
                JSONObject resourcePropertyJsonObject = getJsonObject((JSONObject) propertySetJsonObject.get("resourceProperties"));
                WebSphereAssessmentDto.ResourceProperties resourceProperty = modelMapper.map(resourcePropertyJsonObject, WebSphereAssessmentDto.ResourceProperties.class);

                resourceProperties.add(resourceProperty);
            } else {
                JSONArray resourcePropertyJsonArray = getJsonArray((JSONArray) propertySetJsonObject.get("resourceProperties"));

                for (Object resourcePropertyObject : resourcePropertyJsonArray) {
                    JSONObject resourcePropertyJsonObject = getJsonObject((JSONObject) resourcePropertyObject);
                    WebSphereAssessmentDto.ResourceProperties resourceProperty = modelMapper.map(resourcePropertyJsonObject, WebSphereAssessmentDto.ResourceProperties.class);

                    resourceProperties.add(resourceProperty);
                }
            }

            propertySet.setResourceProperties(resourceProperties);

            return propertySet;
        }

        private static String getConnectionUrl(String implementationClassName, List<WebSphereAssessmentDto.ResourceProperties> resourceProperties) {
            final String db2ClassName = "ibm";
            final String oracleClassName = "oracle";
            final String mssqlClassName = "sqlserver";

            String serverName = getResourceProperty(resourceProperties, "serverName");
            String portNumber = getResourceProperty(resourceProperties, "portNumber");
            String databaseName = getResourceProperty(resourceProperties, "databaseName");
            String connectionUrl = "";

            if (implementationClassName.contains(db2ClassName)) {
                if (implementationClassName.toLowerCase().contains("com.ibm.db2.jdbc.app")) {
                    connectionUrl = "jdbc:db2:" + databaseName;
                } else if (implementationClassName.toLowerCase().contains("com.ibm.as400")) {
                    connectionUrl = "jdbc:as400://" + serverName;
                } else {
                    // example : com.ibm.db2.jcc
                    if (StringUtils.isEmpty(portNumber)) {
                        connectionUrl = "jdbc:db2://" + serverName + "/" + databaseName;
                    } else {
                        connectionUrl = "jdbc:db2://" + serverName + ":" + portNumber + "/" + databaseName;
                    }
                }
            } else if (implementationClassName.contains(oracleClassName)) {
                connectionUrl = getResourceProperty(resourceProperties, "URL");
            } else if (implementationClassName.contains(mssqlClassName)) {
                if (StringUtils.isEmpty(portNumber)) {
                    connectionUrl = "jdbc:sqlserver://" + serverName + ";DatabaseName=" + databaseName;
                } else {
                    connectionUrl = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";DatabaseName=" + databaseName;
                }
            } else {
                connectionUrl = getResourceProperty(resourceProperties, "URL");
            }

            return connectionUrl;
        }

        private static String getResourceProperty(List<WebSphereAssessmentDto.ResourceProperties> resourceProperties, String attribute) {
            String attributeValue = "";

            for (WebSphereAssessmentDto.ResourceProperties resourceProperty : resourceProperties) {
                if (resourceProperty.getName().equals(attribute)) {
                    attributeValue = resourceProperty.getValue();
                    break;
                }
            }

            return attributeValue;
        }

        public static List<WebSphereAssessmentDto.SecurityParse> getSecurity(TargetHost targetHost, String pathName, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
            List<WebSphereAssessmentDto.SecurityParse> securityParses = new ArrayList<>();

            String securityPath = pathName + File.separator + SECURITY_NAME;
            String securityCommand = getFileContentCommand(securityPath);
            String responseString = getSshCommandResultTrim(targetHost, securityCommand);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), securityPath, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            String xmlToJson = XML.toJSONObject(responseString).toString();

            JSONObject jsonObject = getJsonObject(xmlToJson);
            JSONObject securityJsonObject = getJsonObject((JSONObject) jsonObject.get("security:Security"));

            if (securityJsonObject.get("authDataEntries") instanceof JSONObject) {
                JSONObject authDataJsonObject = getJsonObject((JSONObject) securityJsonObject.get("authDataEntries"));

                securityParses.add(modelMapper.map(authDataJsonObject, WebSphereAssessmentDto.SecurityParse.class));
            } else {
                JSONArray authDataJsonArray = getJsonArray((JSONArray) securityJsonObject.get("authDataEntries"));

                for (Object authDataObject : authDataJsonArray) {
                    JSONObject authDataJsonObject = getJsonObject((JSONObject) authDataObject);
                    securityParses.add(modelMapper.map(authDataJsonObject, WebSphereAssessmentDto.SecurityParse.class));
                }
            }

            return securityParses;
        }

        public static WebSphereAssessmentResult.DataSource getAuthentication(WebSphereAssessmentResult.DataSource dataSource, List<WebSphereAssessmentDto.SecurityParse> securityParses) {
            if (CollectionUtils.isNotEmpty(securityParses)) {
                for (WebSphereAssessmentDto.SecurityParse securityParse : securityParses) {
                    if (securityParse.getAlias().equals(dataSource.getAuthDataAlias())) {
                        dataSource.setUserId(securityParse.getUserId());
                        dataSource.setPassword(securityParse.getPassword());
                    }

                }
            }

            return dataSource;
        }
    }

    public static class ApplicationHelper {

        public static String getEditionStatus(TargetHost targetHost, WebSphereAssessmentDto.CellFileInfo cellFileInfo, String applicationName, GetInfoStrategy strategy) throws InterruptedException {
            String path = cellFileInfo.getPath() + File.separator + "applications"
                    + File.separator + applicationName + ".ear" + File.separator + IBM_EDITION_METADATA;

            String editionStatusCommand = getFileContentCommand(path);
            String responseString = getSshCommandResultTrim(targetHost, editionStatusCommand);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), path, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            log.debug("===>" + IBM_EDITION_METADATA);
            log.debug(responseString);

            if (StringUtils.isNotEmpty(responseString)) {
                String[] metadatas = splitToArrayByCrlf(responseString);

                for (String metadata : metadatas) {
                    if (metadata.startsWith("config.state=")) {
                        String[] configState = metadata.split("=", -1);
                        return configState[1];
                    }
                }
            }

            return "";
        }

        public static boolean isDefaultApplication(String deployApplication) {
            String applicationName = "";

            if (deployApplication.contains("/")) {
                applicationName = deployApplication.substring(deployApplication.lastIndexOf("/") + 1);
            } else {
                applicationName = deployApplication;
            }

            return EXCLUDE_DEFAULT_APPLICATIONS.contains(applicationName);
        }

        public static String getBinaryUrlPath(TargetHost targetHost, String enginePath, String profilePath, String deploymentFilePath, String applicationName, GetInfoStrategy strategy) throws InterruptedException {
            String deploymentCommand = getFileContentCommand(deploymentFilePath + File.separator + DEPLOYMENT_FILE);
            String responseString = getSshCommandResultTrim(targetHost, deploymentCommand);

            // -- Start File Save -- //
            WasAnalyzerUtil.saveAssessmentFile(targetHost.getIpAddress(), deploymentFilePath + File.separator + DEPLOYMENT_FILE, responseString, CommonProperties.getWorkDir(), strategy);
            // -- End File Save -- //

            if (StringUtils.isNotEmpty(responseString)) {
                String xmlToJson = XML.toJSONObject(responseString).toString();

                log.debug("===>" + DEPLOYMENT_FILE);
                log.debug(xmlToJson);

                JSONObject jsonObject = getJsonObject(xmlToJson);
                JSONObject deploymentJsonObject = getJsonObject((JSONObject) jsonObject.get("appdeployment:Deployment"));
                JSONObject deployedJsonObject = getJsonObject((JSONObject) deploymentJsonObject.get("deployedObject"));

                String appInstallRoot = profilePath + File.separator + "installedApps";

                if (deployedJsonObject.get("binariesURL") != null) {
                    String binaryUrl = deployedJsonObject.get("binariesURL").toString();

                    binaryUrl = binaryUrl.replaceAll("\\$\\(APP_INSTALL_ROOT\\)", appInstallRoot)
                            .replaceAll("\\$\\(WAS_INSTALL_ROOT\\)", enginePath)
                            .replaceAll("\\$\\{WAS_INSTALL_ROOT}", enginePath)
                            .replaceAll("\\$\\{ARS_NAME}", applicationName + ".ear");

                    return binaryUrl;
                }
            }

            return "";
        }

    }

    private static String stringValueOf(Object object) {
        return object == null ? "" : String.valueOf(object);
    }

}
//end of WebSphereProfileHelper.java
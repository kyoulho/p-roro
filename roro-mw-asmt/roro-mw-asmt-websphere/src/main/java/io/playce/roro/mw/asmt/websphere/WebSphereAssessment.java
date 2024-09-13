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
 * Jaeeon Bae       11월 11, 2021            First Draft.
 */
package io.playce.roro.mw.asmt.websphere;

import io.playce.roro.common.exception.NotsupportedException;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.MiddlewareAssessment;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.WasAnalyzerUtil;
import io.playce.roro.mw.asmt.websphere.dto.WebSphereAssessmentDto;
import io.playce.roro.mw.asmt.websphere.dto.WebSphereAssessmentResult;
import io.playce.roro.mw.asmt.websphere.helper.WebSphereHelper;
import io.playce.roro.mw.asmt.websphere.helper.WebSphereLocalHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.jackson.JsonNodeValueReader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

import static io.playce.roro.mw.asmt.util.WasAnalyzerUtil.*;
import static io.playce.roro.mw.asmt.websphere.helper.WebSphereLocalHelper.getEngine;
import static io.playce.roro.mw.asmt.websphere.helper.WebSphereLocalHelper.getJdbcProvider;

@Component("WSPHEREAssessment")
@RequiredArgsConstructor
@Slf4j
public class WebSphereAssessment implements MiddlewareAssessment {
    private final CommandConfig commandConfig;

    @Override
    public MiddlewareAssessmentResult assessment(TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("-- Start WebSphere Analyze --");

        if (strategy.isWindows()) {
            throw new NotsupportedException("WebSphere scan not yet supported in Windows.");
        }

        final ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.getConfiguration().setCollectionsMergeEnabled(false);
        modelMapper.getConfiguration().addValueReader(new JsonNodeValueReader());

        WebSphereAssessmentResult webSphereAssessmentResult = new WebSphereAssessmentResult();

        WebSphereAssessmentResult.Engine engine;
        WebSphereAssessmentResult.Instance instance;

        // 파일이 있는 경우
        if (hasUploadedConfigFile(middleware)) {
            String wasInstallRootPath = middleware.getConfigFilePath();
            engine = WebSphereLocalHelper.getLocalEngine(targetHost, wasInstallRootPath, modelMapper, strategy);
            instance = getLocalInstance(engine, modelMapper, targetHost, middleware, strategy);
        } else {
            String wasInstallRootPath = middleware.getEngineInstallationPath();
            engine = getEngine(targetHost, wasInstallRootPath, modelMapper, strategy);
            instance = getInstance(engine, modelMapper, targetHost, middleware, strategy);
        }

        // webSphereAssessmentResult.setThirdPartySolutions(ThirdPartySolutionUtil.detectThirdPartySolutionsFromMiddleware(targetHost, strategy.isWindows(), engine.getPath(), null));
        webSphereAssessmentResult.setEngine(engine);
        webSphereAssessmentResult.setInstance(instance);

        return webSphereAssessmentResult;
    }

    public WebSphereAssessmentResult.Instance getInstance(WebSphereAssessmentResult.Engine engine, ModelMapper modelMapper, TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        WebSphereAssessmentResult.Instance instance = new WebSphereAssessmentResult.Instance();
        instance.setGeneral(getGeneral(engine, modelMapper, targetHost, middleware, strategy));

        List<WebSphereAssessmentResult.Profile> profiles = instance.getGeneral().getProfiles();

        if (CollectionUtils.isNotEmpty(profiles)) {
            WebSphereAssessmentDto.DirectoryStructure directoryStructure;

            if (isServerRunningProcess(middleware)) {
                // Assessment 속도 향상을 위해 전체 디렉토리 구조를 읽는다.
                directoryStructure = WebSphereHelper.GeneralHelper.getDirectoryStructure(profiles, middleware, targetHost, modelMapper, commandConfig, strategy);
            } else {
                // Assessment 속도 향상을 위해 전체 디렉토리 구조를 읽는다.
                directoryStructure = WebSphereHelper.GeneralHelper.getDirectoryStructure(profiles, targetHost, modelMapper, commandConfig, strategy);
            }

            // 추가된 인스턴스 필드 add
            List<WebSphereAssessmentResult.Instances> instances = new ArrayList<>();
            instance.setClusters(getCluster(directoryStructure, targetHost, modelMapper, strategy));
            instance.setServers(getServer(directoryStructure, instance.getClusters(), targetHost, modelMapper, instances, strategy));
            instance.setDataSources(getDataSource(directoryStructure, targetHost, modelMapper, strategy));
            instance.setApplications(getApplication(engine, instance.getClusters(), directoryStructure, targetHost, modelMapper, strategy));
            instance.setPorts(getPort(directoryStructure, targetHost, middleware, modelMapper, strategy));
            instance.setInstances(instances);

        }

        return instance;
    }

    public List<WebSphereAssessmentResult.Port> getPort(WebSphereAssessmentDto.DirectoryStructure directoryStructure, TargetHost targetHost, MiddlewareInventory middleware, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        List<WebSphereAssessmentResult.Port> ports = new ArrayList<>();

        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                for (WebSphereAssessmentDto.NodeFileInfo nodeFileInfo : cellFileInfo.getNodeFileInfos()) {
                    WebSphereAssessmentDto.ServerIndex serverIndex = WebSphereHelper.NodeHelper.getServerIndex(targetHost, nodeFileInfo, modelMapper, strategy);
                    List<WebSphereAssessmentDto.ServerEntries> serverEntries = serverIndex.getServerEntries();

                    WebSphereAssessmentResult.Port port = new WebSphereAssessmentResult.Port();
                    port.setProfileName(profileFileInfo.getName());
                    port.setCellName(cellFileInfo.getName());
                    port.setNodeName(nodeFileInfo.getName());

                    List<WebSphereAssessmentResult.ServerEntry> tempServerEntries = new ArrayList<>();

                    for (WebSphereAssessmentDto.ServerEntries serverEntry : serverEntries) {
                        WebSphereAssessmentResult.ServerEntry tempServerEntry = new WebSphereAssessmentResult.ServerEntry();
                        tempServerEntry.setServerName(serverEntry.getServerName());

                        List<WebSphereAssessmentResult.EndPoint> endPoints = new ArrayList<>();

                        for (WebSphereAssessmentDto.SpecialEndpoints specialEndpoint : serverEntry.getSpecialEndpoints()) {
                            WebSphereAssessmentResult.EndPoint endPoint = new WebSphereAssessmentResult.EndPoint();
                            endPoint.setEndPointName(specialEndpoint.getEndPointName());
                            endPoint.setPort(specialEndpoint.getEndPoint().getPort());
                            endPoints.add(endPoint);
                        }

                        tempServerEntry.setEndPoint(endPoints);

                        if (isServerRunningProcess(middleware)) {
                            if (middleware.getName().equals(tempServerEntry.getServerName())) {
                                tempServerEntries.add(tempServerEntry);
                            }
                        } else {
                            tempServerEntries.add(tempServerEntry);
                        }

                    }

                    port.setServerEntries(tempServerEntries);

                    ports.add(port);
                }
            }
        }

        return ports;
    }

    public List<WebSphereAssessmentResult.Application> getApplication(WebSphereAssessmentResult.Engine engine, List<WebSphereAssessmentResult.Cluster> clusters, WebSphereAssessmentDto.DirectoryStructure directoryStructure,
                                                                      TargetHost targetHost, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        List<WebSphereAssessmentResult.Application> applications = new ArrayList<>();
        List<Map<String, Object>> applicationList = new ArrayList<>();

        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                for (WebSphereAssessmentDto.NodeFileInfo nodeFileInfo : cellFileInfo.getNodeFileInfos()) {
                    WebSphereAssessmentDto.ServerIndex serverIndex = WebSphereHelper.NodeHelper.getServerIndex(targetHost, nodeFileInfo, modelMapper, strategy);
                    List<WebSphereAssessmentDto.ServerEntries> serverEntries = serverIndex.getServerEntries();

                    for (WebSphereAssessmentDto.ServerEntries serverEntry : serverEntries) {
                        if (CollectionUtils.isNotEmpty(serverEntry.getDeployedApplications())) {
                            for (String deployedApplication : serverEntry.getDeployedApplications()) {
                                if (!WebSphereHelper.ApplicationHelper.isDefaultApplication(deployedApplication)) {
                                    Map<String, Object> applicationMap = new HashMap<>();

                                    applicationMap.put("profileFileInfo", profileFileInfo);
                                    applicationMap.put("cellFileInfo", cellFileInfo);
                                    applicationMap.put("nodeFileInfo", nodeFileInfo);
                                    applicationMap.put("serverType", serverEntry.getServerType());
                                    applicationMap.put("serverName", serverEntry.getServerName());
                                    applicationMap.put("applicationPath", deployedApplication);

                                    applicationList.add(applicationMap);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Map<String, Object> applicationMap : applicationList) {
            WebSphereAssessmentDto.ProfileFileInfo profileFileInfo = (WebSphereAssessmentDto.ProfileFileInfo) applicationMap.get("profileFileInfo");
            WebSphereAssessmentDto.CellFileInfo cellFileInfo = (WebSphereAssessmentDto.CellFileInfo) applicationMap.get("cellFileInfo");
            WebSphereAssessmentDto.NodeFileInfo nodeFileInfo = (WebSphereAssessmentDto.NodeFileInfo) applicationMap.get("nodeFileInfo");

            String serverName = String.valueOf(applicationMap.get("serverName"));
            String serverType = String.valueOf(applicationMap.get("serverType"));
            String applicationPath = String.valueOf(applicationMap.get("applicationPath"));
            String applicationName = "";

            if (applicationPath.contains("/")) {
                applicationName = applicationPath.substring(applicationPath.lastIndexOf("/") + 1);
            } else {
                applicationName = applicationPath;
            }

            String deploymentPath = cellFileInfo.getPath() + File.separator + "applications" + File.separator + applicationPath;
            String applicationBinaryUrlPath = WebSphereHelper.ApplicationHelper.getBinaryUrlPath(targetHost, engine.getPath(), profileFileInfo.getPath(), deploymentPath, applicationName, strategy);

            if (StringUtils.isNotEmpty(applicationBinaryUrlPath)
                    && isExistDirectory(targetHost, applicationBinaryUrlPath, commandConfig, strategy)
                    && !isEmptyDirectory(targetHost, applicationBinaryUrlPath, commandConfig, strategy)) {
                WebSphereAssessmentResult.Application application = new WebSphereAssessmentResult.Application();

                application.setApplicationName(applicationName);
                application.setEditionStatus(WebSphereHelper.ApplicationHelper.getEditionStatus(targetHost, cellFileInfo, applicationName, strategy));
                application.setProfileName(profileFileInfo.getName());
                application.setCellName(cellFileInfo.getName());
                application.setNodeName(nodeFileInfo.getName());
                application.setClusterName(WebSphereHelper.ClusterHelper.getClusterName(clusters, profileFileInfo.getName(), cellFileInfo.getName(),
                        nodeFileInfo.getName(), serverName));
                application.setServerName(serverName);
                application.setServerType(serverType);
                application.setApplicationDeploymentFilePath(deploymentPath);
                application.setApplicationBinaryUrlPath(WebSphereHelper.ApplicationHelper.getBinaryUrlPath(targetHost, engine.getPath(), profileFileInfo.getPath(), deploymentPath, application.getApplicationName(), strategy));

                applications.add(application);
            }
        }

        return applications;
    }

    public List<WebSphereAssessmentResult.DataSource> getDataSource(WebSphereAssessmentDto.DirectoryStructure directoryStructure, TargetHost targetHost, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        List<WebSphereAssessmentResult.DataSource> dataSources = new ArrayList<>();
        List<WebSphereAssessmentDto.SecurityParse> securityParses = new ArrayList<>();

        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                String cellRange = "cell: " + cellFileInfo.getName();
                // Cell 단위
                WebSphereAssessmentDto.ResourceParse cellResourceParse = WebSphereHelper.DatabaseHelper.getResource(targetHost, cellFileInfo.getPath(), modelMapper, strategy);
                dataSources.addAll(getJdbcProvider(profileFileInfo, cellResourceParse, cellRange));

                // Security(인증)
                List<WebSphereAssessmentDto.SecurityParse> securityParseList = WebSphereHelper.DatabaseHelper.getSecurity(targetHost, cellFileInfo.getPath(), modelMapper, strategy);
                securityParses.addAll(securityParseList);

                for (WebSphereAssessmentDto.NodeFileInfo nodeFileInfo : cellFileInfo.getNodeFileInfos()) {
                    String nodeRange = cellRange + ", node: " + nodeFileInfo.getName();
                    // Node 단위
                    WebSphereAssessmentDto.ResourceParse nodeResourceParse = WebSphereHelper.DatabaseHelper.getResource(targetHost, nodeFileInfo.getPath(), modelMapper, strategy);
                    dataSources.addAll(getJdbcProvider(profileFileInfo, nodeResourceParse, nodeRange));

                    for (WebSphereAssessmentDto.ServerFileInfo serverFileInfo : nodeFileInfo.getServerFileInfos()) {
                        String serverRange = nodeRange + ", server: " + serverFileInfo.getName();
                        // Server 단위
                        WebSphereAssessmentDto.ResourceParse serverResourceParse = WebSphereHelper.DatabaseHelper.getResource(targetHost, serverFileInfo.getPath(), modelMapper, strategy);
                        dataSources.addAll(getJdbcProvider(profileFileInfo, serverResourceParse, serverRange));
                    }
                }

                // Cluster 단위
                for (WebSphereAssessmentDto.ClusterFileInfo clusterFileInfo : cellFileInfo.getClusterFileInfos()) {
                    String clusterRange = cellRange + ", cluster: " + clusterFileInfo.getName();
                    WebSphereAssessmentDto.ResourceParse clusterResourceParse = WebSphereHelper.DatabaseHelper.getResource(targetHost, clusterFileInfo.getPath(), modelMapper, strategy);
                    dataSources.addAll(getJdbcProvider(profileFileInfo, clusterResourceParse, clusterRange));
                }
            }
        }

        for (WebSphereAssessmentResult.DataSource dataSource : dataSources) {
            WebSphereHelper.DatabaseHelper.getAuthentication(dataSource, securityParses);
        }

        return dataSources;
    }

    public List<WebSphereAssessmentResult.Server> getServer(WebSphereAssessmentDto.DirectoryStructure directoryStructure, List<WebSphereAssessmentResult.Cluster> clusters,
                                                            TargetHost targetHost, ModelMapper modelMapper, List<WebSphereAssessmentResult.Instances> instances, GetInfoStrategy strategy) throws InterruptedException {
        List<WebSphereAssessmentResult.Server> servers = new ArrayList<>();

        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                for (WebSphereAssessmentDto.NodeFileInfo nodeFileInfo : cellFileInfo.getNodeFileInfos()) {
                    for (WebSphereAssessmentDto.ServerFileInfo serverFileInfo : nodeFileInfo.getServerFileInfos()) {
                        WebSphereAssessmentResult.Server tempServer = new WebSphereAssessmentResult.Server();
                        tempServer.setProfileName(profileFileInfo.getName());
                        tempServer.setCellName(cellFileInfo.getName());
                        tempServer.setClusterName(WebSphereHelper.ClusterHelper.getClusterName(clusters, profileFileInfo.getName(), cellFileInfo.getName(),
                                nodeFileInfo.getName(), serverFileInfo.getName()));
                        tempServer.setNodeName(nodeFileInfo.getName());
                        tempServer.setServerName(serverFileInfo.getName());
                        tempServer.setHostName(WebSphereHelper.ServerHelper.getHostName(targetHost, nodeFileInfo, serverFileInfo.getName(), modelMapper, strategy));
                        tempServer.setListenPort(WebSphereHelper.ServerHelper.getServerPort(targetHost, nodeFileInfo, serverFileInfo.getName(), modelMapper, strategy));
                        tempServer.setStatus(WasAnalyzerUtil.getProcessStatus(targetHost, serverFileInfo.getName(), commandConfig, strategy));
                        tempServer.setJvmOptions(WasAnalyzerUtil.getJvmOptions(targetHost, serverFileInfo.getName(), commandConfig, strategy));
                        tempServer.setConfig(WebSphereHelper.ServerHelper.getConfig(targetHost, serverFileInfo, modelMapper, strategy));
                        tempServer.setServerType(WebSphereHelper.ServerHelper.getServerType(targetHost, nodeFileInfo, serverFileInfo.getName(), modelMapper, strategy));
                        servers.add(tempServer);

                        if (StringUtils.defaultString(tempServer.getServerType()).equals("APPLICATION_SERVER")) {
                            WebSphereAssessmentResult.Instances tempInstance = new WebSphereAssessmentResult.Instances();
                            tempInstance.setName(tempServer.getServerName());

                            // min Heap & max Heap을 가져온다.
                            tempInstance.setMinHeap(WasAnalyzerUtil.getHeapSize(targetHost, tempInstance.getName(), "-Xms", commandConfig, strategy));
                            tempInstance.setMaxHeap(WasAnalyzerUtil.getHeapSize(targetHost, tempInstance.getName(), "-Xmx", commandConfig, strategy));

                            // runUser와 vm Option 정보를 가져온다.
                            tempInstance.setRunUser(WasAnalyzerUtil.getRunUser(targetHost, tempInstance.getName(), commandConfig, strategy));
                            tempInstance.setVmOption(WasAnalyzerUtil.getJvmOptions(targetHost, tempInstance.getName(), commandConfig, strategy));
                            instances.add(tempInstance);
                        }
                    }
                }
            }
        }

        return servers;
    }

    public List<WebSphereAssessmentResult.Cluster> getCluster(WebSphereAssessmentDto.DirectoryStructure directoryStructure, TargetHost targetHost, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        List<WebSphereAssessmentResult.Cluster> clusters = new ArrayList<>();

        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                for (WebSphereAssessmentDto.ClusterFileInfo clusterFileInfo : cellFileInfo.getClusterFileInfos()) {
                    List<WebSphereAssessmentDto.ClusterParse> clusterParses = WebSphereHelper.ClusterHelper.getCluster(targetHost, clusterFileInfo, modelMapper, strategy);

                    if (CollectionUtils.isNotEmpty(clusterParses)) {
                        for (WebSphereAssessmentDto.ClusterParse clusterParse : clusterParses) {
                            WebSphereAssessmentResult.Cluster cluster = new WebSphereAssessmentResult.Cluster();
                            cluster.setProfileName(profileFileInfo.getName());
                            cluster.setCellName(cellFileInfo.getName());
                            cluster.setClusterName(StringUtils.defaultString(clusterParse.getName()));
                            cluster.setNodeGroup(StringUtils.defaultString(clusterParse.getNodeGroupName()));
                            cluster.setMembers(WebSphereHelper.ClusterHelper.getMembers(clusterParse.getMembers()));
                            cluster.setDwlm(WebSphereHelper.ClusterHelper.getClusterDwlm(targetHost, clusterFileInfo, strategy));

                            List<WebSphereAssessmentResult.Member> members = new ArrayList<>();
                            if (CollectionUtils.isNotEmpty(clusterParse.getMembers())) {
                                for (WebSphereAssessmentDto.ClusterMember clusterMember : clusterParse.getMembers()) {
                                    WebSphereAssessmentResult.Member member = new WebSphereAssessmentResult.Member();
                                    member.setNodeName(clusterMember.getNodeName());
                                    member.setServerName(clusterMember.getMemberName());

                                    members.add(member);
                                }
                                cluster.setMembers(members);
                            }

                            clusters.add(cluster);
                        }
                    }
                }
            }
        }

        return clusters;
    }

    public WebSphereAssessmentResult.Instance getLocalInstance(WebSphereAssessmentResult.Engine engine, ModelMapper modelMapper, TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        WebSphereAssessmentResult.Instance instance = new WebSphereAssessmentResult.Instance();
        instance.setGeneral(getLocalGeneral(engine, modelMapper, targetHost, middleware, strategy));

        List<WebSphereAssessmentResult.Profile> profiles = instance.getGeneral().getProfiles();

        if (CollectionUtils.isNotEmpty(profiles)) {
            // Assessment 속도 향상을 위해 전체 디렉토리 구조를 읽는다.
            WebSphereAssessmentDto.DirectoryStructure directoryStructure = WebSphereLocalHelper.getLocalDirectoryStructure(profiles, targetHost, modelMapper, commandConfig, strategy);

            instance.setClusters(getLocalCluster(directoryStructure, targetHost, middleware, modelMapper, strategy));
            instance.setServers(getLocalServer(directoryStructure, instance.getClusters(), targetHost, middleware, modelMapper, strategy));
            instance.setDataSources(getLocalDataSource(directoryStructure, targetHost, middleware, modelMapper, strategy));
            instance.setApplications(getLocalApplication(engine, instance.getClusters(), directoryStructure, targetHost, middleware, modelMapper, strategy));
            instance.setPorts(getLocalPort(directoryStructure, targetHost, middleware, modelMapper, strategy));
        }

        return instance;
    }

    public List<WebSphereAssessmentResult.Port> getLocalPort(WebSphereAssessmentDto.DirectoryStructure directoryStructure, TargetHost targetHost, MiddlewareInventory middleware, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        List<WebSphereAssessmentResult.Port> ports = new ArrayList<>();

        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                for (WebSphereAssessmentDto.NodeFileInfo nodeFileInfo : cellFileInfo.getNodeFileInfos()) {
                    WebSphereAssessmentDto.ServerIndex serverIndex = WebSphereLocalHelper.getLocalServerIndex(targetHost, middleware, nodeFileInfo, modelMapper, strategy);
                    List<WebSphereAssessmentDto.ServerEntries> serverEntries = serverIndex.getServerEntries();

                    WebSphereAssessmentResult.Port port = new WebSphereAssessmentResult.Port();
                    port.setProfileName(profileFileInfo.getName());
                    port.setCellName(cellFileInfo.getName());
                    port.setNodeName(nodeFileInfo.getName());

                    List<WebSphereAssessmentResult.ServerEntry> tempServerEntries = new ArrayList<>();

                    for (WebSphereAssessmentDto.ServerEntries serverEntry : serverEntries) {
                        WebSphereAssessmentResult.ServerEntry tempServerEntry = new WebSphereAssessmentResult.ServerEntry();
                        tempServerEntry.setServerName(serverEntry.getServerName());

                        List<WebSphereAssessmentResult.EndPoint> endPoints = new ArrayList<>();

                        for (WebSphereAssessmentDto.SpecialEndpoints specialEndpoint : serverEntry.getSpecialEndpoints()) {
                            WebSphereAssessmentResult.EndPoint endPoint = new WebSphereAssessmentResult.EndPoint();
                            endPoint.setEndPointName(specialEndpoint.getEndPointName());
                            endPoint.setPort(specialEndpoint.getEndPoint().getPort());
                            endPoints.add(endPoint);
                        }

                        tempServerEntry.setEndPoint(endPoints);
                        tempServerEntries.add(tempServerEntry);
                    }

                    port.setServerEntries(tempServerEntries);

                    ports.add(port);
                }
            }
        }

        return ports;
    }

    public List<WebSphereAssessmentResult.Application> getLocalApplication(WebSphereAssessmentResult.Engine engine, List<WebSphereAssessmentResult.Cluster> clusters,
                                                                           WebSphereAssessmentDto.DirectoryStructure directoryStructure, TargetHost targetHost, MiddlewareInventory middleware, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        List<WebSphereAssessmentResult.Application> applications = new ArrayList<>();
        List<Map<String, Object>> applicationList = new ArrayList<>();

        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                for (WebSphereAssessmentDto.NodeFileInfo nodeFileInfo : cellFileInfo.getNodeFileInfos()) {
                    WebSphereAssessmentDto.ServerIndex serverIndex = WebSphereLocalHelper.getLocalServerIndex(targetHost, middleware, nodeFileInfo, modelMapper, strategy);
                    List<WebSphereAssessmentDto.ServerEntries> serverEntries = serverIndex.getServerEntries();

                    for (WebSphereAssessmentDto.ServerEntries serverEntry : serverEntries) {
                        if (CollectionUtils.isNotEmpty(serverEntry.getDeployedApplications())) {
                            for (String deployedApplication : serverEntry.getDeployedApplications()) {
                                if (!WebSphereHelper.ApplicationHelper.isDefaultApplication(deployedApplication)) {
                                    Map<String, Object> applicationMap = new HashMap<>();

                                    applicationMap.put("profileFileInfo", profileFileInfo);
                                    applicationMap.put("cellFileInfo", cellFileInfo);
                                    applicationMap.put("nodeFileInfo", nodeFileInfo);
                                    applicationMap.put("serverType", serverEntry.getServerType());
                                    applicationMap.put("serverName", serverEntry.getServerName());
                                    applicationMap.put("applicationPath", deployedApplication);

                                    applicationList.add(applicationMap);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Map<String, Object> applicationMap : applicationList) {
            WebSphereAssessmentDto.ProfileFileInfo profileFileInfo = (WebSphereAssessmentDto.ProfileFileInfo) applicationMap.get("profileFileInfo");
            WebSphereAssessmentDto.CellFileInfo cellFileInfo = (WebSphereAssessmentDto.CellFileInfo) applicationMap.get("cellFileInfo");
            WebSphereAssessmentDto.NodeFileInfo nodeFileInfo = (WebSphereAssessmentDto.NodeFileInfo) applicationMap.get("nodeFileInfo");

            String serverName = String.valueOf(applicationMap.get("serverName"));
            String serverType = String.valueOf(applicationMap.get("serverType"));
            String applicationPath = String.valueOf(applicationMap.get("applicationPath"));
            String applicationName = "";

            if (applicationPath.contains("/")) {
                applicationName = applicationPath.substring(applicationPath.lastIndexOf("/") + 1);
            } else {
                applicationName = applicationPath;
            }

            String deploymentPath = cellFileInfo.getPath() + File.separator + "applications" + File.separator + applicationPath;
            String applicationBinaryUrlPath = WebSphereHelper.ApplicationHelper.getBinaryUrlPath(targetHost, engine.getPath(), profileFileInfo.getPath(), deploymentPath, applicationName, strategy);

            if (StringUtils.isNotEmpty(applicationBinaryUrlPath)
                    && isExistDirectory(targetHost, applicationBinaryUrlPath, commandConfig, strategy)
                    && !isEmptyDirectory(targetHost, applicationBinaryUrlPath, commandConfig, strategy)) {
                WebSphereAssessmentResult.Application application = new WebSphereAssessmentResult.Application();

                application.setApplicationName(applicationName);
                application.setEditionStatus(WebSphereHelper.ApplicationHelper.getEditionStatus(targetHost, cellFileInfo, applicationName, strategy));
                application.setProfileName(profileFileInfo.getName());
                application.setCellName(cellFileInfo.getName());
                application.setNodeName(nodeFileInfo.getName());
                application.setClusterName(WebSphereHelper.ClusterHelper.getClusterName(clusters, profileFileInfo.getName(), cellFileInfo.getName(),
                        nodeFileInfo.getName(), serverName));
                application.setServerName(serverName);
                application.setServerType(serverType);
                application.setApplicationDeploymentFilePath(deploymentPath);
                application.setApplicationBinaryUrlPath(WebSphereHelper.ApplicationHelper.getBinaryUrlPath(targetHost, engine.getPath(), profileFileInfo.getPath(), deploymentPath, application.getApplicationName(), strategy));

                applications.add(application);
            }
        }

        return applications;
    }

    public List<WebSphereAssessmentResult.Server> getLocalServer(WebSphereAssessmentDto.DirectoryStructure directoryStructure, List<WebSphereAssessmentResult.Cluster> clusters, TargetHost targetHost, MiddlewareInventory middleware, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        List<WebSphereAssessmentResult.Server> servers = new ArrayList<>();

        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                for (WebSphereAssessmentDto.NodeFileInfo nodeFileInfo : cellFileInfo.getNodeFileInfos()) {
                    for (WebSphereAssessmentDto.ServerFileInfo serverFileInfo : nodeFileInfo.getServerFileInfos()) {
                        WebSphereAssessmentResult.Server tempServer = new WebSphereAssessmentResult.Server();
                        tempServer.setProfileName(profileFileInfo.getName());
                        tempServer.setCellName(cellFileInfo.getName());
                        tempServer.setClusterName(WebSphereHelper.ClusterHelper.getClusterName(clusters, profileFileInfo.getName(), cellFileInfo.getName(),
                                nodeFileInfo.getName(), serverFileInfo.getName()));
                        tempServer.setNodeName(nodeFileInfo.getName());
                        tempServer.setServerName(serverFileInfo.getName());
                        tempServer.setHostName(WebSphereLocalHelper.getLocalHostName(targetHost, middleware, nodeFileInfo, serverFileInfo.getName(), modelMapper, strategy));
                        tempServer.setListenPort(WebSphereLocalHelper.getLocalServerPort(targetHost, middleware, nodeFileInfo, serverFileInfo.getName(), modelMapper, strategy));
                        tempServer.setStatus(WasAnalyzerUtil.getProcessStatus(targetHost, serverFileInfo.getName(), commandConfig, strategy));
                        tempServer.setJvmOptions(WasAnalyzerUtil.getJvmOptions(targetHost, serverFileInfo.getName(), commandConfig, strategy));
                        tempServer.setConfig(WebSphereLocalHelper.getLocalConfig(targetHost, middleware, serverFileInfo, modelMapper, strategy));

                        servers.add(tempServer);
                    }
                }
            }
        }

        return servers;
    }

    public List<WebSphereAssessmentResult.DataSource> getLocalDataSource(WebSphereAssessmentDto.DirectoryStructure directoryStructure, TargetHost targetHost, MiddlewareInventory middleware, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        List<WebSphereAssessmentResult.DataSource> dataSources = new ArrayList<>();
        List<WebSphereAssessmentDto.SecurityParse> securityParses = new ArrayList<>();

        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                String cellRange = "cell: " + cellFileInfo.getName();
                // Cell 단위
                WebSphereAssessmentDto.ResourceParse cellResourceParse = WebSphereLocalHelper.getLocalResource(targetHost, middleware, cellFileInfo.getPath(), modelMapper, strategy);
                dataSources.addAll(getJdbcProvider(profileFileInfo, cellResourceParse, cellRange));

                // Security(인증)
                List<WebSphereAssessmentDto.SecurityParse> securityParseList = WebSphereLocalHelper.getLocalSecurity(targetHost, middleware, cellFileInfo.getPath(), modelMapper, strategy);
                securityParses.addAll(securityParseList);

                for (WebSphereAssessmentDto.NodeFileInfo nodeFileInfo : cellFileInfo.getNodeFileInfos()) {
                    String nodeRange = cellRange + ", node: " + nodeFileInfo.getName();
                    // Node 단위
                    WebSphereAssessmentDto.ResourceParse nodeResourceParse = WebSphereLocalHelper.getLocalResource(targetHost, middleware, nodeFileInfo.getPath(), modelMapper, strategy);
                    dataSources.addAll(getJdbcProvider(profileFileInfo, nodeResourceParse, nodeRange));

                    for (WebSphereAssessmentDto.ServerFileInfo serverFileInfo : nodeFileInfo.getServerFileInfos()) {
                        String serverRange = nodeRange + ", server: " + serverFileInfo.getName();
                        // Server 단위
                        WebSphereAssessmentDto.ResourceParse serverResourceParse = WebSphereLocalHelper.getLocalResource(targetHost, middleware, serverFileInfo.getPath(), modelMapper, strategy);
                        dataSources.addAll(getJdbcProvider(profileFileInfo, serverResourceParse, serverRange));
                    }
                }

                // Cluster 단위
                for (WebSphereAssessmentDto.ClusterFileInfo clusterFileInfo : cellFileInfo.getClusterFileInfos()) {
                    String clusterRange = cellRange + ", cluster: " + clusterFileInfo.getName();
                    WebSphereAssessmentDto.ResourceParse clusterResourceParse = WebSphereLocalHelper.getLocalResource(targetHost, middleware, clusterFileInfo.getPath(), modelMapper, strategy);
                    dataSources.addAll(getJdbcProvider(profileFileInfo, clusterResourceParse, clusterRange));
                }
            }
        }

        for (WebSphereAssessmentResult.DataSource dataSource : dataSources) {
            WebSphereHelper.DatabaseHelper.getAuthentication(dataSource, securityParses);
        }

        return dataSources;
    }

    public List<WebSphereAssessmentResult.Cluster> getLocalCluster(WebSphereAssessmentDto.DirectoryStructure directoryStructure, TargetHost targetHost, MiddlewareInventory middleware, ModelMapper modelMapper, GetInfoStrategy strategy) throws InterruptedException {
        List<WebSphereAssessmentResult.Cluster> clusters = new ArrayList<>();

        for (WebSphereAssessmentDto.ProfileFileInfo profileFileInfo : directoryStructure.getProfileFileInfos()) {
            for (WebSphereAssessmentDto.CellFileInfo cellFileInfo : profileFileInfo.getCellFileInfos()) {
                for (WebSphereAssessmentDto.ClusterFileInfo clusterFileInfo : cellFileInfo.getClusterFileInfos()) {
                    List<WebSphereAssessmentDto.ClusterParse> clusterParses = WebSphereLocalHelper.getLocalCluster(targetHost, middleware, clusterFileInfo, modelMapper, strategy);

                    if (CollectionUtils.isNotEmpty(clusterParses)) {
                        for (WebSphereAssessmentDto.ClusterParse clusterParse : clusterParses) {
                            WebSphereAssessmentResult.Cluster cluster = new WebSphereAssessmentResult.Cluster();
                            cluster.setProfileName(profileFileInfo.getName());
                            cluster.setCellName(cellFileInfo.getName());
                            cluster.setClusterName(StringUtils.defaultString(clusterParse.getName()));
                            cluster.setNodeGroup(StringUtils.defaultString(clusterParse.getNodeGroupName()));
                            cluster.setMembers(WebSphereHelper.ClusterHelper.getMembers(clusterParse.getMembers()));
                            cluster.setDwlm(WebSphereHelper.ClusterHelper.getClusterDwlm(targetHost, clusterFileInfo, strategy));

                            List<WebSphereAssessmentResult.Member> members = new ArrayList<>();
                            if (CollectionUtils.isNotEmpty(clusterParse.getMembers())) {
                                for (WebSphereAssessmentDto.ClusterMember clusterMember : clusterParse.getMembers()) {
                                    WebSphereAssessmentResult.Member member = new WebSphereAssessmentResult.Member();
                                    member.setNodeName(clusterMember.getNodeName());
                                    member.setServerName(clusterMember.getMemberName());

                                    members.add(member);
                                }
                                cluster.setMembers(members);
                            }

                            clusters.add(cluster);
                        }
                    }

                }
            }
        }

        log.debug("Local Cluster : " + clusters);

        return clusters;
    }

    public WebSphereAssessmentResult.General getGeneral(WebSphereAssessmentResult.Engine engine, ModelMapper modelMapper, TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        WebSphereAssessmentResult.General general = new WebSphereAssessmentResult.General();
        general.setVendor("IBM");
        general.setEngineName(engine.getName());
        general.setEngineVersion(engine.getVersion());
        general.setInstalledHome(engine.getPath());
        general.setScannedDate(new Date());

        List<WebSphereAssessmentResult.Profile> profiles = WebSphereHelper.ProfileHelper.getProfile(targetHost, engine.getPath(), modelMapper);

        // Process 에서 WebSphere를 찾을 경우.
        // Profiel 목록에서 찾은거랑 비교를 한다.
        if (isServerRunningProcess(middleware)) {
            List<WebSphereAssessmentResult.Profile> tempProfiles = new ArrayList<>();
            for (WebSphereAssessmentResult.Profile profile : profiles) {
                if (middleware.getProfileName().equals(profile.getName())) {
                    tempProfiles.add(profile);
                    break;
                }
            }

            if (CollectionUtils.isNotEmpty(tempProfiles)) {
                general.setJavaVersion(WebSphereHelper.GeneralHelper.getJavaVersion(targetHost, tempProfiles, commandConfig, strategy));
                general.setJavaVendor(WebSphereHelper.GeneralHelper.getJavaVendor(targetHost, tempProfiles, commandConfig, strategy));
                general.setProfiles(tempProfiles);
            }
        } else {
            if (CollectionUtils.isNotEmpty(profiles)) {
                general.setJavaVersion(WebSphereHelper.GeneralHelper.getJavaVersion(targetHost, profiles, commandConfig, strategy));
                general.setJavaVendor(WebSphereHelper.GeneralHelper.getJavaVendor(targetHost, profiles, commandConfig, strategy));
                general.setProfiles(profiles);
            }
        }

        return general;
    }

    private boolean isServerRunningProcess(MiddlewareInventory middleware) {
        // Profile, Cell, Node, Server Name이 모두 있어야 처리가 된다.
        if (!StringUtils.defaultString(middleware.getProfileName()).equals("")
                && !StringUtils.defaultString(middleware.getCellName()).equals("")
                && !StringUtils.defaultString(middleware.getNodeName()).equals("")
                && !StringUtils.defaultString(middleware.getName()).equals("")) {
            return true;
        }
        return false;
    }

    public WebSphereAssessmentResult.General getLocalGeneral(WebSphereAssessmentResult.Engine engine, ModelMapper modelMapper, TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        List<WebSphereAssessmentResult.Profile> profiles = WebSphereLocalHelper.getLocalProfile(targetHost, middleware, engine.getPath(), modelMapper);

        WebSphereAssessmentResult.General general = new WebSphereAssessmentResult.General();
        general.setVendor("IBM");
        general.setEngineName(engine.getName());
        general.setEngineVersion(engine.getVersion());
        general.setInstalledHome(engine.getPath());
        general.setScannedDate(new Date());

        if (CollectionUtils.isNotEmpty(profiles)) {
            general.setJavaVersion(WebSphereHelper.GeneralHelper.getJavaVersion(targetHost, profiles, commandConfig, strategy));
            general.setProfiles(profiles);
        }

        return general;
    }

    protected boolean hasUploadedConfigFile(MiddlewareInventory middleware) {
        return StringUtils.isNotEmpty(middleware.getConfigFilePath());
    }
}
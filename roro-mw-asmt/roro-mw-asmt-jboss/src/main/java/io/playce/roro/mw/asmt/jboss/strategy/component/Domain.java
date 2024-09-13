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
 * Jhpark       8월 13, 2022            First Draft.
 */
package io.playce.roro.mw.asmt.jboss.strategy.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.common.util.SplitUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.jboss.dto.JbossAssessmentResult;
import io.playce.roro.mw.asmt.jboss.dto.JbossAssessmentResult.Engine;
import io.playce.roro.mw.asmt.jboss.dto.JbossAssessmentResult.Instance;
import io.playce.roro.mw.asmt.jboss.enums.DOMAIN_CONFIG_FILES;
import io.playce.roro.mw.asmt.jboss.enums.ENGINE;
import io.playce.roro.mw.asmt.jboss.enums.NODE;
import io.playce.roro.mw.asmt.jboss.enums.attribute.CONNECTOR;
import io.playce.roro.mw.asmt.jboss.enums.attribute.DEPLOY;
import io.playce.roro.mw.asmt.jboss.enums.attribute.INTERFACE;
import io.playce.roro.mw.asmt.jboss.enums.attribute.RESOURCE;
import io.playce.roro.mw.asmt.jboss.strategy.ServerModeStrategy;
import io.playce.roro.mw.asmt.jboss.strategy.enums.StrategyName;
import io.playce.roro.mw.asmt.jboss.strategy.helper.JBossHelper;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.playce.roro.common.util.ThreadLocalUtils.MW_SCAN_ERROR;
import static io.playce.roro.mw.asmt.AbstractMiddlewareAssessment.getSshCommandResultTrim;
import static io.playce.roro.mw.asmt.util.MWCommonUtil.executeCommand;

/**
 * <pre>
 *
 * </pre>
 *
 * @author jhpark
 * @version 3.0
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class Domain implements ServerModeStrategy {

    private final CommandConfig commandConfig;

    private final JBossHelper jBossHelper;

    @Override
    public StrategyName getModeName() {
        return StrategyName.DOMAIN;
    }

    /**
     * standalone.xml Interface Element 가져오기
     *
     * @param instance
     * @param serverXml
     * @param strategy
     */
    @Override
    public void setInterFaces(JbossAssessmentResult.Instance instance, JsonNode serverXml, GetInfoStrategy strategy) {
        JsonNode nodeInterFaces = serverXml.at(NODE.INTERFACES.path() + NODE.INTERFACE.path());

        if (nodeInterFaces.isMissingNode()) {
            return;
        }

        List<Map<String, String>> interFaces = new ArrayList<>();
        for (JsonNode node : nodeInterFaces) {
            Map<String, String> map = new HashMap<>();
            map.put("name", node.get(INTERFACE.name.getCodeName()).asText());
            interFaces.add(map);
        }
        //instance.setInterfaces(interFaces);
    }

    /**
     * CMD ㅇㅔ서 현재 올라와 있는 Config 파일 인스턴스 저장
     *
     * @param targetHost
     * @param engine
     * @param sudo
     * @param strategy
     * @param instance
     *
     * @throws InterruptedException
     */
    public void findConfigFileNameFromCmd(TargetHost targetHost, JbossAssessmentResult.Engine engine, boolean sudo, GetInfoStrategy strategy, JbossAssessmentResult.Instance instance) throws InterruptedException {
        if (instance.getRunTimeOptions().size() > 0) {
            Map<String, String> map = new HashMap<>();
            String hostFile = instance.getRunTimeOptions().stream().filter(s -> s.contains(ENGINE.HOSTCONFIG.codeName())).findFirst().get().isEmpty() ? "" :
                    instance.getRunTimeOptions().stream().filter(s -> s.contains(ENGINE.HOSTCONFIG.codeName())).findFirst().get();
            String domainFile = instance.getRunTimeOptions().stream().filter(s -> s.contains(ENGINE.DOMAINCONFIG.codeName())).findFirst().get().isEmpty() ? "" :
                    instance.getRunTimeOptions().stream().filter(s -> s.contains(ENGINE.DOMAINCONFIG.codeName())).findFirst().get();

            map.put(ENGINE.HOSTCONFIG.codeName(), hostFile.substring(hostFile.indexOf("=") + 1));
            map.put(ENGINE.DOMAINCONFIG.codeName(), domainFile.substring(domainFile.indexOf("=") + 1));
            instance.setConfigFileName(map);
        }
    }

    /**
     * 모듈 Extensions  저장
     *
     * @param instance
     * @param serverXml
     * @param strategy
     *
     * @throws InterruptedException
     */
    public void setExtensions(JbossAssessmentResult.Instance instance, JsonNode serverXml, GetInfoStrategy strategy) throws InterruptedException {
        JsonNode modules = serverXml.at(NODE.EXTENSIONS.path()).findPath("extension");

        if (modules.isMissingNode()) {
            return;
        }

        //List<Map<String, String>> list = JsonUtil.getNodeValueFromJsonNode(modules, Arrays.stream(EXTENSIONS.values()).map(Enum::name).collect(Collectors.toList()));
        List<String> list = new ArrayList<>();
        for (JsonNode module : modules) {
            list.add(module.findPath("module").asText());
        }

        instance.setModules(list);
    }

    /**
     * 현재 Jboss 서버 자바 버전 가져오기
     *
     * @param targetHost
     * @param instance
     * @param strategy
     *
     * @throws InterruptedException
     */
    public void setJavaVersion(TargetHost targetHost, JbossAssessmentResult.Instance instance, GetInfoStrategy strategy, JbossAssessmentResult.Engine engine) throws InterruptedException {
        // https://cloud-osci.atlassian.net/browse/PCR-6207
        // String processNm = ENGINE.HOST_CONTROLLER.codeName();
        jBossHelper.loadJavaVersion(targetHost, instance, strategy, instance.getDomainPath(), ENGINE.HOST_CONTROLLER.codeName());
    }

    public void setJavaVendor(TargetHost targetHost, Instance instance, GetInfoStrategy strategy, Engine engine) throws InterruptedException {
        // https://cloud-osci.atlassian.net/browse/PCR-6207
        // String processNm = ENGINE.HOST_CONTROLLER.codeName();
        jBossHelper.loadJavaVendor(targetHost, instance, strategy, instance.getDomainPath(), ENGINE.HOST_CONTROLLER.codeName());
    }

    /**
     * 현재 실행 중인 프로세스 유저
     *
     * @param targetHost
     * @param instance
     * @param sudo
     * @param strategy
     *
     * @throws InterruptedException
     */
    public void setRunUser(TargetHost targetHost, JbossAssessmentResult.Instance instance, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        Map<String, String> commandMap = Map.of(COMMAND.JBOSS_RUN_USER.name(),
                COMMAND.JBOSS_RUN_USER.command(commandConfig, strategy.isWindows(), "java", ENGINE.DOMAIN_PROVIDER.codeName(), instance.getHomeDir(), instance.getBaseDir()));

        Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
        RemoteExecResult result = resultMap.get(COMMAND.JBOSS_RUN_USER.name());

        if (!result.isErr()) {
            instance.setRunUser(result.getResult());
        } else {
            log.error("error run user: {}", result.getError());
        }
    }

    /**
     * 리소스 정보 저장하기
     *
     * @param instance
     * @param serverXml
     */
    public void setResources(JbossAssessmentResult.Instance instance, JsonNode serverXml) {
        List<LinkedHashMap> list = new ArrayList<>();

        JsonNode serverGroups = serverXml.at(NODE.SERVERGROUPS.path());
        JsonNode serverGroup = serverGroups.findPath("server-group");
        String svrPrfileName = serverGroup.findPath("profile").asText();
        JsonNode profile = serverXml.at(NODE.PROFILES.path()).findPath("profile");

        if (serverGroups.isMissingNode() || serverGroup.isMissingNode() || profile.isMissingNode()) {
            return;
        }

        for (JsonNode node : profile) {
            String profileNm = node.findPath("name").asText();

            if (profileNm.equals(svrPrfileName)) {
                LinkedHashMap dataSources = new LinkedHashMap();
                JsonNode dataSource = node.findPath(RESOURCE.datasources.getCodeName()).get(RESOURCE.datasource.getCodeName());

                if (dataSource == null) {
                    break;
                }

                dataSources.put("poolName", jBossHelper.nodeNullToEmptyString(dataSource.get("pool-name")));
                dataSources.put("driver", jBossHelper.nodeNullToEmptyString(dataSource.get(RESOURCE.driver.getCodeName())));
                dataSources.put("jndiName", jBossHelper.nodeNullToEmptyString(dataSource.get("jndi-name")));
                dataSources.put("userName", jBossHelper.nodeNullToEmptyString(dataSource.findPath("user-name")));
                dataSources.put("password", jBossHelper.nodeNullToEmptyString(dataSource.findPath(RESOURCE.password.getCodeName())));
                dataSources.put("useJavaContext", jBossHelper.nodeNullToEmptyString(dataSource.get("use-java-context")));
                dataSources.put("connectionUrl", jBossHelper.nodeNullToEmptyString(dataSource.get("connection-url")));
                dataSources.put("datasourceClass", node.findPath(RESOURCE.datasources.getCodeName()).findPath("drivers").findPath("xa-datasource-class").asText());
                dataSources.put("enabled", jBossHelper.nodeNullToEmptyString(dataSource.get(RESOURCE.enabled.getCodeName())));
                list.add(dataSources);
            }
        }

        instance.setResources(list);
    }

    /**
     * 어플리케이션 정보 매핑
     *
     * @param targetHost
     * @param engine
     * @param instance
     * @param serverXml
     * @param sudo
     * @param strategy
     *
     * @throws InterruptedException
     */
    public void setApplications(TargetHost targetHost, JbossAssessmentResult.Engine engine, JbossAssessmentResult.Instance instance, JsonNode serverXml, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        JsonNode node = serverXml.at(NODE.DEPLOY.path());

        if (node.isMissingNode()) {
            return;
        }

        /**
         * scan-enabled 가 활성화 되어 있는 경우 인스턴스 하위의 deployments 디렉토리에 war 파일을 가져다 놓으면 <deployment /> 설정을 추가하지 않아도 자동으로 스캔되어 App이 구동된다.
         * 하지만 대부분의 사이트에서 운영시 해당 옵션(scan-enabled)을 비활성화 하기 때문에 이 부분은 현재 고려하지 않음.
         */

        String root = engine.getPath().equals(instance.getDomainPath()) ? engine.getPath() : instance.getDomainPath();
        instance.setApplications(new ArrayList<>());
        //List<Map<String, String>> list = JsonUtil.getNodeValueFromJsonNode(node, Arrays.stream(DEPLOY.values()).map(DEPLOY::getCodeName).collect(Collectors.toList()));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> m = objectMapper.convertValue(node, Map.class);

        for (String key : m.keySet()) {
            Object obj = m.get(key);

            List<Map<String, Object>> appList;
            if (obj instanceof Map) {
                appList = new ArrayList<>();
                appList.add((Map<String, Object>) obj);
            } else if (obj instanceof List) {
                appList = (List<Map<String, Object>>) obj;
            } else {
                appList = new ArrayList<>();
            }

            for (Map<String, Object> mapKey : appList) {
                String deployPath = null;
                JbossAssessmentResult.Applications application = new JbossAssessmentResult.Applications();

                for (Object k : mapKey.keySet()) {
                    if (k.toString().contains("fs")) {
                        String[] path = mapKey.get(k).toString().replace("{", "").replace("}", "").split("=");
                        deployPath = path[1];
                    }

                    // <fs-archive/> 및 <fs-exploded/> 내의 path 속성은 풀패스로 입력된다.
                    // <fs-archive/>에 대한 처리(deployPath + "/" + deployFile)가 필요하면 여기에서..
                    // Standalone.java 파일도 동일하게 수정 필요
                }

                String servicename = (String) mapKey.get(DEPLOY.name.getCodeName());
                String deployFile = (String) mapKey.get(DEPLOY.runtimename.getCodeName());

                int idx = deployFile.lastIndexOf(".");

                application.setServiceName(servicename);
                application.setSourcePath(deployPath.endsWith("/") ? deployPath.substring(0, deployPath.length() - 1) : deployPath);
                application.setDeployFileName(deployFile);
                application.setServerGroup(getSeverGroup(servicename, serverXml));

                String contextPath = jBossHelper.getWebContextPath(deployPath, root, strategy, sudo, targetHost);
                if (StringUtils.isEmpty(contextPath)) {
                    contextPath = "/" + FilenameUtils.removeExtension(deployFile);
                }

                application.setContextPath(contextPath);
                application.setType(deployFile.substring(idx + 1));
                int severGroupCnt = application.getServerGroup() != null ? application.getServerGroup().split(",").length : 0;
                application.setAssignmentCnt(severGroupCnt);
                instance.getApplications().add(application);
            }
        }

        // for(JsonNode deployment : node) {
        //     JbossAssessmentResult.Applications application = new JbossAssessmentResult.Applications();
        //     String servicename = deployment.get(DEPLOY.name.getCodeName()).asText();
        //     String deployFile = deployment.get(DEPLOY.runtimename.getCodeName()).asText();
        //     String deployPath = deployment.get(DEPLOY.type.getCodeName()).get(DEPLOY.path.getCodeName()).asText();
        //
        //     int idx = deployFile.lastIndexOf(".");
        //     application.setServiceName(servicename);
        //     application.setSourcePath(deployPath);
        //     application.setDeployFileName(deployFile);
        //     application.setServerGroup(getSeverGroup(servicename, serverXml));
        //     application.setContextPath(jBossHelper.getWebContextPath(deployPath, root, strategy, sudo, targetHost));
        //     application.setType(deployFile.substring(idx + 1));
        //     int severGroupCnt = application.getServerGroup() != null ? application.getServerGroup().split(",").length : 0;
        //     application.setAssignmentCnt(severGroupCnt);
        //     instance.getApplications().add(application);
        // }
    }


    /**
     * 서버 스레드 정보 가져오기
     *
     * @param instance
     * @param serverXml
     */
    public void setExecutorServer(JbossAssessmentResult.Instance instance, JsonNode serverXml) {
        //getExecutor
        List<Map<String, String>> list = new ArrayList<>();

        JsonNode serverGroups = serverXml.at(NODE.SERVERGROUPS.path());
        JsonNode serverGroup = serverGroups.findPath("server-group");
        String svrPrfileName = serverGroup.findPath("profile").asText();
        JsonNode profile = serverXml.at(NODE.PROFILES.path()).findPath("profile");

        if (serverGroups.isMissingNode() || serverGroup.isMissingNode() || profile.isMissingNode()) {
            return;
        }

        for (JsonNode node : profile) {
            String profileNm = node.findPath("name").asText();

            if (profileNm.equals(svrPrfileName)) {
                JsonNode thread = node.findPath(RESOURCE.threadpools.getCodeName());

                if (thread == null) {
                    break;
                }

                Map threadObj = new HashMap();
                threadObj.put("threadPoolName", thread.findPath("name").asText());
                threadObj.put("maxThreadCnt", thread.findPath(RESOURCE.maxthreads.getCodeName()).get("count").asText());
                threadObj.put("keepalivetime", thread.findPath(RESOURCE.keepalivetime.getCodeName()).get("time").asText());
                threadObj.put("unit", thread.findPath(RESOURCE.keepalivetime.getCodeName()).get("unit").asText());
                list.add(threadObj);
            }
        }

        instance.setThreads(list);
    }

    /**
     * 도메인 모드일때 서버 그룹을 매핑한다.
     *
     * @param instance
     * @param serverXml
     * @param hostFile
     * @param strategy
     */
    @Override
    public void setServers(JbossAssessmentResult.Instance instance, JsonNode serverXml, JsonNode hostFile, GetInfoStrategy strategy,
                           TargetHost targetHost, JbossAssessmentResult.Engine engine, boolean sudo) throws InterruptedException {
        JsonNode hostServers = hostFile.at(NODE.SERVERS.path() + "/server");

        if (hostServers.isMissingNode()) {
            return;
        }

        if (hostServers.get("name") != null) {
            hostServers = hostFile.at(NODE.SERVERS.path());
        }
        List<JbossAssessmentResult.Server> servers = new ArrayList<>();
        List<JbossAssessmentResult.Instances> serverInstances = new ArrayList<>();

        /**
         * host.xml 에서 서버 인스턴스 정보 가져온다. ::: 서버 정보
         */
        for (JsonNode node : hostServers) {
            JbossAssessmentResult.Server server = new JbossAssessmentResult.Server();
            server.setAutostart(node.findPath("auto-start").asText());
            server.setGroup(node.findPath("group").asText());
            server.setName(node.findPath("name").asText());
            server.setPortOffset(node.findPath("port-offset").asText());
            server.setSocketBindingGroup(node.findPath("socket-binding-group").asText());
            server.setHost("Host");
            server.setHeapSize(getSvrHeapSize(serverXml, server.getGroup()));
            server.setMaxHeap(getSvrHeapMaxSize(serverXml, server.getGroup()));
            server.setJvmOptions(getSvrJvmVmOptions(serverXml, hostFile, server.getGroup()));
            servers.add(server);
        }

        for (JbossAssessmentResult.Server hs : servers) {
            JbossAssessmentResult.Instances servins = new JbossAssessmentResult.Instances();
            String svrName = hs.getName();
            List<String> opitons = getServerOptions(targetHost, strategy, sudo, instance.getDomainPath(), svrName);

            servins.setSvrConnectors(getServerConnectors(instance.getRunTimeOptions(), serverXml, hs.getSocketBindingGroup(), hs.getGroup(), hs.getPortOffset(), strategy, targetHost, sudo));

            servins.setConfigPath(getValuesFromFindStr(opitons, "domain.config.dir"));
            servins.setName(svrName);
            servins.setMinHeap(getValuesFromFindStr(opitons, "Xms"));
            servins.setMaxHeap(getValuesFromFindStr(opitons, "Xmx"));
            servins.setMaxPermSize(getValuesFromFindStr(opitons, "MaxPermSize"));
            servins.setIpAddress(getValuesFromFindStr(opitons, "jboss.bind.address"));
            servins.setRunUser(getSvrRunUser(targetHost, instance, sudo, strategy, instance.getDomainPath(), svrName));
            servins.setIsRunning(instance.getIsRunning().equals("true"));
            servins.setJavaVersion(jBossHelper.loadJavaVersion(targetHost, instance, strategy, instance.getDomainPath(), svrName));
            servins.setJavaVendor(jBossHelper.loadJavaVendor(targetHost, instance, strategy, instance.getDomainPath(), svrName));
            servins.setPortOffset(hs.getPortOffset());
            servins.setJvmOptions(hs.getJvmOptions());
            servins.setRunTimeOptions(getVmOptions(instance, targetHost, sudo, strategy, instance.getDomainPath(), svrName));
            servins.setSvrGroupName(hs.getGroup());
            servins.setSocketBindName(hs.getSocketBindingGroup());
            servins.setProfileName(getSvrProfileName(serverXml, hs.getGroup()));
            serverInstances.add(servins);
        }

        instance.setInstances(serverInstances);
    }

    @Override
    public void setConnectors(JbossAssessmentResult.Instance instance, JsonNode serverXml, JsonNode hostFile, GetInfoStrategy strategy, TargetHost targetHost, boolean sudo) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode hostManagement = hostFile.at(NODE.MANAGEMENT.path() + "/management-interfaces");
        JsonNode hostServers = hostFile.at(NODE.SERVERS.path());

        if (hostManagement.isMissingNode() && hostServers.isMissingNode()) {
            return;
        }

        List<Map<String, String>> socketList = new ArrayList<>();
        JsonNode protocals = hostFile.at(NODE.MANAGEMENT.path()).findPath("management-interfaces");

        List<JbossAssessmentResult.Instances> instances = instance.getInstances();

        Map<String, Object> m = objectMapper.convertValue(protocals, Map.class);
        for (String key : m.keySet()) {
            Map<String, String> Sockets = new HashMap<>();
            HashMap mapKey = (HashMap) m.get(key);
            HashMap mapSocket = (HashMap) mapKey.get("socket");
            String port = (String) mapSocket.get("port");
            String activePort = getActivePort(instance.getRunTimeOptions(), port);
            Sockets.put("name", getActiveProtocol(key));
            Sockets.put("socketBindName", hostServers.findPath("socket-binding-group").asText());
            Sockets.put("interface", getActiveProtocol(key));
            Sockets.put("port", activePort);
            socketList.add(Sockets);
        }

        for (JbossAssessmentResult.Instances ins : instances) {
            List<Map<String, String>> svrConn = (ArrayList) ins.getSvrConnectors().stream().collect(Collectors.toList());
            for (Map con : svrConn) {
                Map<String, String> insSokets = new HashMap<>();
                insSokets.put("socketBindName", ins.getSocketBindName());
                insSokets.put("name", (String) con.get("name"));
                insSokets.put("port", (String) con.get("port"));
                insSokets.put("interface", (String) con.get("interface"));
                socketList.add(insSokets);
            }
        }

        instance.setConnectors(filterGroupBySoketList(socketList));
    }

    @Override
    public void setInstanceRemoteConfigFiles(TargetHost targetHost, JbossAssessmentResult.Engine engine, JbossAssessmentResult.Instance instance, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        Map<String, JbossAssessmentResult.ConfigFile> map = new HashMap<>();
        boolean idEngineDeployed = engine.getPath().equals(instance.getDomainPath());

        Map<String, String> commandMap = new HashMap<>();
        Map<String, JbossAssessmentResult.ConfigFile> fileMap = new HashMap<>();
        Map<String, String> confFiles = instance.getConfigFileName();

        for (String key : confFiles.keySet()) {
            JbossAssessmentResult.ConfigFile tc = new JbossAssessmentResult.ConfigFile();
            String root = idEngineDeployed ? engine.getPath() : instance.getDomainPath();

            String fileName = confFiles.get(key);
            String fileKey = fileName;

            // WindowsInfoStrategy.runCommands() 에서 처리됨.
            // if (strategy.isWindows()) {
            //     fileName = fileName.replaceAll("sh", "bat");
            //
            //     if (fileName.contains("domain")) {
            //         fileKey = DOMAIN_CONFIG_FILES.JBOSS_DOMAIN_XML.name();
            //     } else if (fileName.contains("host")) {
            //         fileKey = DOMAIN_CONFIG_FILES.JBOSS_DOMAIN_HOST_XML.name();
            //     }
            // }

            String path = root + strategy.getSeparator() + "configuration" + strategy.getSeparator() + fileName;
            tc.setPath(path);
            commandMap.put(fileKey, COMMAND.valueOf(DOMAIN_CONFIG_FILES.JBOSS_DOMAIN_XML.name()).command(commandConfig, strategy.isWindows(), tc.getPath()));
            fileMap.put(fileKey, tc);
        }

        Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);

        for (String key : resultMap.keySet()) {
            RemoteExecResult result = resultMap.get(key);

            JbossAssessmentResult.ConfigFile tc = fileMap.get(key);
            if (!result.isErr()) {
                tc.setContents(result.getResult());
                map.put(key, tc);
            } else {
                log.info("[{}] file read failed : [{}]", tc.getPath(), result.getError());
                if (DOMAIN_CONFIG_FILES.JBOSS_DOMAIN_XML.name().equals(key)) {
                    throw new RoRoException("JBoss config file(" + tc.getPath() + ") read failed. Please check file is exist and has permission to read at \"[" +
                            targetHost.getUsername() + "@" + targetHost.getIpAddress() + "]\"");
                }
            }
        }

        instance.setConfigFiles(map);
    }

    /**
     * 설정 파일 저장
     *
     * @param configFilePath
     * @param instance
     * @param engine
     */
    @Override
    public void setInstanceLocalConfigFiles(String configFilePath, JbossAssessmentResult.Instance instance, JbossAssessmentResult.Engine engine) {
        File config = new File(configFilePath);
        Map<String, JbossAssessmentResult.ConfigFile> map = new HashMap<>();

        if (config.isDirectory()) {
            for (DOMAIN_CONFIG_FILES configFile : DOMAIN_CONFIG_FILES.values()) {
                File findFile = jBossHelper.findFile(config, configFile.filename());
                if (findFile == null) {
                    log.debug("config file not found: {}", configFile.filename());
                    continue;
                }

                JbossAssessmentResult.ConfigFile configFileResult = new JbossAssessmentResult.ConfigFile();
                configFileResult.setPath(findFile.getAbsolutePath());
                try {
                    configFileResult.setContents(FileUtils.readFileToString(findFile, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    log.info("[{}] file read failed : [{}]", configFile.filename(), e.getMessage());
                    log.error("{}", e.getMessage(), e);

                    if (configFile == DOMAIN_CONFIG_FILES.JBOSS_DOMAIN_XML) {
                        throw new RoRoException("JBoss config file(" + findFile.getAbsolutePath() + ") read failed. Please check file is exist and has permission to read.");
                    }
                }
                map.put(configFile.name(), configFileResult);
            }
        }

        instance.setConfigFiles(map);
    }

    /**
     * Vm Option 정보를 저장한다 .
     *
     * @param targetHost
     * @param engine
     * @param sudo
     * @param strategy
     *
     * @throws InterruptedException
     */
    @Override
    public void setInstanceVmInfo(TargetHost targetHost, JbossAssessmentResult.Engine engine, boolean sudo, GetInfoStrategy strategy, JbossAssessmentResult.Instance instance) throws InterruptedException {
        List<String> vmoptions;
        String domainHomePath = instance.getDomainPath();

        jBossHelper.checkPath(targetHost, domainHomePath, "domain_home_path", strategy);

        // https://cloud-osci.atlassian.net/browse/PCR-6207
        //String controller = strategy.isWindows() ? "Host Controller" : "'Host Controller'";
        String controller = strategy.isWindows() ? "Host Controller" : domainHomePath + "' | grep '" + ENGINE.HOST_CONTROLLER.codeName();

        String processArgumentCommand = COMMAND.PROCESS_ARGUMENT.command(commandConfig, strategy.isWindows(), controller, controller);
        String responseString = getSshCommandResultTrim(targetHost, processArgumentCommand, COMMAND.PROCESS_ARGUMENT, strategy);
        responseString = responseString.replace("Host Controller", "HostController");
        vmoptions = Arrays.asList(responseString.split(StringUtils.SPACE));
        vmoptions.replaceAll(s -> s.contains("HostController") ? "-D[Host Controller]" : s);

        instance.setRunTimeOptions(vmoptions);
        instance.setIsRunning(instance.getRunTimeOptions().size() > 0 ? "true" : "false");
        instance.setMinHeap(jBossHelper.setDataFromVMoptions(instance, "-Xms"));
        instance.setMaxHeap(jBossHelper.setDataFromVMoptions(instance, "-Xmx"));
        instance.setMaxPermSize(jBossHelper.setDataFromVMoptionsOfContains(instance, "-XX:MaxPermSize="));
        instance.setMaxMetaspaceSize(jBossHelper.setDataFromVMoptionsOfContains(instance, "-XX:MaxMetaspaceSize="));
    }

    /**
     * 엔진 정보를 저장한다.
     *
     * @param targetHost
     * @param engine
     * @param sudo
     * @param strategy
     *
     * @throws InterruptedException
     */
    @Override
    public void setEngineInfo(TargetHost targetHost, JbossAssessmentResult.Engine engine, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        jBossHelper.checkPath(targetHost, engine.getPath(), "engine_installation_path", strategy);

        Map<String, String> commandMap = Map.of(
                COMMAND.JBOSS_VERSION_NOTE.name(), COMMAND.JBOSS_VERSION_NOTE.command(commandConfig, strategy.isWindows(), engine.getPath())
        );

        Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);

        RemoteExecResult execResult = resultMap.get(COMMAND.JBOSS_VERSION_NOTE.name());
        if (!execResult.isErr()) {
            String releaseString = execResult.getResult();

            if (releaseString.contains("Version")) {
                String[] rStrArr = SplitUtil.split(releaseString, "- Version", 2);
                engine.setName(rStrArr[0]);
                engine.setVersion(rStrArr[1]);
            } else {
                engine.setName("JBoss");
                log.error("No version found from version.txt: {}", execResult.getResult());
            }
        }

        engine.setMode(ENGINE.DOMAIN_NAME.codeName());
    }

    /**
     * 도메인 모드 소켓 정보에서 포트 및 프로토콜 정보 가져오기
     *
     * @param socketItems
     * @param instance
     * @param strategy
     *
     * @return
     */
    private List<HashMap> getDomainSocketsBindItems(JsonNode socketItems, JbossAssessmentResult.Instance instance, GetInfoStrategy strategy, String portOffset) {
        List<HashMap> list = new ArrayList<>();
        HashMap<String, List> socketBindItems = new HashMap<>();
        for (JsonNode node : socketItems) {
            List<Map<String, String>> SoketBindList = JsonUtil.getNodeValueFromJsonNode(node.at("/socket-binding"), Arrays.stream(CONNECTOR.values()).map(Enum::name).collect(Collectors.toList()));

            for (Map<String, String> map : SoketBindList) {
                String httpPort = map.get(CONNECTOR.port.getCodeName());
                findConnectorPort(httpPort, instance.getRunTimeOptions(), CONNECTOR.port, map, strategy, portOffset);
            }
            String socketName = node.get("name").asText();
            socketBindItems.put(socketName, SoketBindList);
        }

        list.add(socketBindItems);

        return list;
    }

    private void findConnectorPort(String variable, List<String> options, CONNECTOR connector,
                                   Map<String, String> map, GetInfoStrategy strategy, String portOffset) {
        if (variable == null) {
            return;
        }

        //if(!strategy.checkVariable(variable)) return;
        if (variable.length() < 3) {
            return;
        }

        String optionPath = new String();
        String port;
        if (variable.contains("$")) {
            variable = variable.substring(2, variable.length() - 1);
            String strPort = String.valueOf(jBossHelper.findNumber(variable));
            port = strPort;
            optionPath = Arrays.stream(variable.split(":")).collect(Collectors.toSet())
                    .stream().filter(o -> !o.equals(strPort)).findFirst().get();
        } else {
            port = variable;
        }

        /**
         * 프로세스에 offset 정보를 비교하여 port 값을 구한다.
         */
        int offsetValue = getOffsetVal(portOffset, options);

        for (String option : options) {
            if (option.contains(optionPath) && StringUtils.isNotEmpty(optionPath)) {
                int index = option.indexOf("=");
                String value = option.substring(index + 1);
                int portVal = offsetValue + NumberUtils.toInt(value);
                map.put(connector.getCodeName(), String.valueOf(portVal));
            } else {
                int portVal = offsetValue + NumberUtils.toInt(port);
                map.put(connector.getCodeName(), String.valueOf(portVal));
            }
        }
    }

    /**
     * port off-set value
     *
     * @param portOffset
     * @param options
     *
     * @return
     */
    private int getOffsetVal(String portOffset, List<String> options) {
        int defaultoffsetValue = 0;
        int offsetValue = 0;

        if (StringUtils.isNotEmpty(portOffset)) {
            portOffset = portOffset.substring(2, portOffset.length() - 1);
            defaultoffsetValue = jBossHelper.findNumber(portOffset);
            String strOffsetVal = String.valueOf(defaultoffsetValue);
            String offSetPortPath = Arrays.stream(portOffset.split(":")).collect(Collectors.toSet()).stream()
                    .filter(o -> !o.equals(strOffsetVal)).findFirst().get();

            String offset = options.stream().filter(s -> s.contains(offSetPortPath))
                    .collect(Collectors.toSet()).stream().findFirst().get() == null ? "" : options.stream().filter(s -> s.contains(offSetPortPath))
                    .collect(Collectors.toSet()).stream().findFirst().get();

            if (StringUtils.isNotEmpty(offset)) {
                int idx = offset.indexOf("=") + 1;
                offsetValue = Integer.parseInt(offset.substring(idx));
                return offsetValue;
            }

            return defaultoffsetValue;
        }

        return offsetValue;
    }

    private String getActiveProtocol(String key) {
        if (StringUtils.isEmpty(key)) {
            return "";
        }

        String name = Arrays.stream(key.split("-")).collect(Collectors.toList()).stream().findFirst().get();

        return "management-" + name;
    }

    private String getActivePort(List<String> options, String port) {
        if (port.contains("$")) {
            port = port.substring(2, port.length() - 1);
            String strPort = String.valueOf(jBossHelper.findNumber(port));
            String path = Arrays.stream(port.split(":")).collect(Collectors.toSet())
                    .stream().filter(o -> !o.equals(strPort)).findFirst().get();

            for (String cmd : options) {
                if (cmd.contains(path)) {
                    int index = cmd.indexOf("=");
                    String value = cmd.substring(index + 1);
                    return value;
                }
            }
        }

        return port;
    }

    private List<String> getServerOptions(TargetHost targetHost, GetInfoStrategy strategy, boolean sudo, String path, String name) throws InterruptedException {
        CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();
        List<String> param = new ArrayList<>();
        RemoteExecResult processResult = MWCommonUtil.executeCommand(targetHost, COMMAND.JBOSS_RUN_SERVER, commandConfig, strategy, sudo, path, name);

        if (!processResult.isErr()) {
            String vmString = processResult.getResult();
            vmString = vmString.replaceAll("\\\\", "\\\\\\\\");
            String[] vmArr;

            try {
                vmArr = parser.parseLine(vmString);
            } catch (IOException e) {
                log.error("VmOptions parsing error: {}", e.getMessage());
                ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while VmOptions parsing error. Detail : [" + e.getMessage() + "]");
                return null;
            }

            param = Arrays.stream(vmArr).map(o -> o = o.replaceAll("\"", StringUtils.EMPTY)).filter(o -> strategy.isAbstractPath(o) || o.startsWith("-") || o.startsWith("org")).collect(Collectors.toList());
        }

        return param;
    }

    private String getValuesFromFindStr(List<String> options, String findStr) {
        String str = options.stream().filter(s -> s.contains(findStr)).collect(Collectors.toSet()).stream().findFirst().isEmpty() ? "" :
                options.stream().filter(s -> s.contains(findStr)).collect(Collectors.toSet()).stream().findFirst().get();
        String splitStr = "=";

        if (str.contains(splitStr)) {
            str = str.split(splitStr)[1];
        }

        return str;
    }

    private List<Map<String, String>> getServerConnectors(List<String> runtimeOptions, JsonNode domainXml, String socketName, String groupName, String offset, GetInfoStrategy strategy, TargetHost targetHost, boolean sudo) throws InterruptedException {
        JsonNode socketBindGroup = domainXml.at("/socket-binding-groups").findPath("socket-binding-group");

        if (socketBindGroup.isMissingNode()) {
            return null;
        }

        if (StringUtils.isEmpty(socketName)) {
            JsonNode serverGroups = domainXml.at(NODE.SERVERGROUPS.path() + "/server-group");
            for (JsonNode server : serverGroups) {
                if (server.findPath("name").asText().equals(groupName)) {
                    socketName = server.findPath("socket-binding-group").findPath("ref").asText();
                    break;
                }
            }
        }

        JsonNode sokets = null;
        String defaultInterFace = null;
        for (JsonNode socket : socketBindGroup) {
            String sbgName = socket.findPath("name").asText();
            if (sbgName.equals(socketName)) {
                sokets = socket.findPath("socket-binding");
                defaultInterFace = socket.findPath("default-interface").asText().isEmpty() ? "" : socket.findPath("default-interface").asText();
                break;
            }
        }

        return getSvrSocketList(runtimeOptions, sokets, offset, strategy, targetHost, sudo, defaultInterFace);
    }

    public String getSvrRunUser(TargetHost targetHost, JbossAssessmentResult.Instance instance, boolean sudo, GetInfoStrategy strategy, String path, String name) throws InterruptedException {
        RemoteExecResult result = null;
        if (name.equals("Process Controller")) {
            Map<String, String> commandMap = Map.of(COMMAND.JBOSS_PROCESS_RUN_SERVER.name(),
                    COMMAND.JBOSS_PROCESS_RUN_SERVER.command(commandConfig, strategy.isWindows(), instance.getDomainPath(), "Process Controller"));
            Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
            result = resultMap.get(COMMAND.JBOSS_PROCESS_RUN_SERVER.name());
        } else {
            Map<String, String> commandMap = Map.of(COMMAND.JBOSS_RUN_USER.name(),
                    COMMAND.JBOSS_RUN_USER.command(commandConfig, strategy.isWindows(), instance.getDomainPath(), path, name, instance.getHomeDir(), instance.getBaseDir()));
            Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
            result = resultMap.get(COMMAND.JBOSS_RUN_USER.name());
        }

        if (!result.isErr()) {
            return result.getResult();
        } else {
            log.error("error run user: {}", result.getError());
        }

        return "";
    }

    private String getSvrHeapSize(JsonNode domainXml, String hostGroupName) {
        JsonNode serverGroups = domainXml.at(NODE.SERVERGROUPS.path());
        String heapSize = "";
        for (JsonNode sever : serverGroups) {
            String SvrGroupName = sever.findPath("name").asText();
            if (hostGroupName.equals(SvrGroupName)) {
                heapSize = sever.findPath("jvm").findPath("heap").get("size").asText();
                break;
            }
        }

        return heapSize;
    }

    private String getSvrProfileName(JsonNode domainXml, String sevrGroup) {
        JsonNode serverGroups = domainXml.at(NODE.SERVERGROUPS.path()).findPath("server-group");

        if (serverGroups.isMissingNode()) {
            return null;
        }

        boolean isOnlyServer = false;
        String profileName = "";

        for (JsonNode sever : serverGroups) {
            String SvrGroupName = sever.findPath("name").asText();
            if (StringUtils.isEmpty(SvrGroupName)) {
                isOnlyServer = true;
                break;
            }
            if (sevrGroup.equals(SvrGroupName)) {
                profileName = sever.findPath("profile").asText();
                break;
            }
        }

        if (isOnlyServer) {
            return getOnlyServerProfileName(domainXml, sevrGroup);
        }

        return profileName;
    }

    private String getSvrHeapMaxSize(JsonNode domainXml, String hostGroupName) {
        JsonNode serverGroups = domainXml.at(NODE.SERVERGROUPS.path());
        String heapSize = "";
        for (JsonNode sever : serverGroups) {
            String SvrGroupName = sever.findPath("name").asText();
            if (hostGroupName.equals(SvrGroupName)) {
                heapSize = sever.findPath("jvm").findPath("heap").get("max-size").asText();
                break;
            }
        }

        return heapSize;
    }

    private String getSvrJvmVmOptions(JsonNode domainXml, JsonNode hostFile, String hostGroupName) {
        JsonNode serverGroups = domainXml.at(NODE.SERVERGROUPS.path());
        String jvmName = null;
        for (JsonNode sever : serverGroups) {
            String SvrGroupName = sever.findPath("name").asText();
            if (hostGroupName.equals(SvrGroupName)) {
                jvmName = sever.findPath("jvm").findPath("name").asText();
                break;
            }
        }

        JsonNode hostJvms = hostFile.at(NODE.JVMS.path());
        for (JsonNode jvm : hostJvms) {
            String hostJvmName = jvm.get("name").asText();
            if (hostJvmName.equals(jvmName)) {
                JsonNode jvmoptions = hostFile.at(NODE.JVMS.path()).findPath("jvm").findPath("jvm-options");
                return jvmoptions.findPath("value").asText();
            }
        }

        return "";
    }

    public List<String> getVmOptions(JbossAssessmentResult.Instance instance, TargetHost targetHost, boolean sudo, GetInfoStrategy strategy, String path, String name) throws InterruptedException {
        CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();
        String domainHomePath = instance.getDomainPath();
        jBossHelper.checkPath(targetHost, domainHomePath, "domain_home_path", strategy);
        List<String> vmoptions = new ArrayList<>();

        if (strategy.isWindows()) {
            domainHomePath = domainHomePath.replaceAll("\\\\", "\\\\\\\\");
        }

        RemoteExecResult processResult = MWCommonUtil.executeCommand(targetHost, COMMAND.JBOSS_CURRENT_SEVER_VMOPTION, commandConfig, strategy, sudo, domainHomePath, path, name);
        if (!processResult.isErr()) {
            String vmString = processResult.getResult();
            vmString = vmString.replaceAll("\\\\", "\\\\\\\\");
            String[] vmArr;
            try {
                vmArr = parser.parseLine(vmString);
            } catch (IOException e) {
                log.error("VmOptions parsing error: {}", e.getMessage());
                ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while VmOptions parsing error. Detail : [" + e.getMessage() + "]");
                return null;
            }
            List<String> params = Arrays.stream(vmArr).map(s -> s = s.replaceAll("\"", StringUtils.EMPTY)).filter(s -> strategy.isAbstractPath(s) || s.startsWith("-") || s.startsWith("org")).collect(Collectors.toList());
            vmoptions.addAll(params);
        }

        return vmoptions;
    }


    private String getSeverGroup(String deployAppName, JsonNode domainXml) {
        JsonNode serverGroups = domainXml.at(NODE.SERVERGROUPS.path());
        StringBuilder str = new StringBuilder();

        if (serverGroups.isMissingNode()) {
            return "";
        }

        for (JsonNode node : serverGroups) {
            String deploymentNm = node.findPath("deployments").findPath("deployment").get("name").asText();
            if (deployAppName.equals(deploymentNm)) {
                String groupName = node.get("name") == null ? node.findPath("name").asText() : node.get("name").asText();
                str.append(groupName);
                str.append(",");
            }
        }

        return str.toString().substring(0, str.length() - 1);
    }

    /**
     * pcport, pc address 검출 및 저장
     *
     * @param vmoptions
     * @param vmArr
     */
    private void setPcAddressOrPort(List<String> vmoptions, String[] vmArr) {
        int pcPortIDx = IntStream.range(0, vmArr.length)
                .filter(i -> "--pc-port".equals(vmArr[i]))
                .findFirst().orElse(-1);

        int pcAddressIDx = IntStream.range(0, vmArr.length)
                .filter(i -> "--pc-address".equals(vmArr[i]))
                .findFirst().orElse(-1);

        if (pcAddressIDx != -1) {
            vmoptions.add("--pc-address " + String.valueOf(vmArr[pcAddressIDx + 1]));
        }

        if (pcPortIDx != -1) {
            vmoptions.add("--pc-port " + String.valueOf(vmArr[pcPortIDx + 1]));
        }
    }

    /**
     * listen 하고 있는 포트 가져오기
     *
     * @param sokets
     * @param offset
     * @param strategy
     * @param targetHost
     * @param sudo
     *
     * @throws InterruptedException
     */
    private List<Map<String, String>> getSvrSocketList(List<String> runtimeOptions, JsonNode sokets, String offset, GetInfoStrategy strategy, TargetHost targetHost, boolean sudo, String defaultInterFace) throws InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, String>> socketList = new ArrayList<>();
        Map<String, String> commandAttr = new HashMap<>();
        Map<String, RemoteExecResult> resultMap = new HashMap<>();

        if (sokets == null) {
            return socketList;
        }

        if (strategy.isWindows()) {
            for (JsonNode winSok : sokets) {
                if (winSok.get("port") != null) {
                    String protocolName = winSok.get("name").asText();
                    String tempPort = getActiveFinedPort(winSok.get("port").asText(), runtimeOptions);
                    int port = NumberUtils.toInt(tempPort) + NumberUtils.toInt(offset);
                    RemoteExecResult result = executeCommand(targetHost, COMMAND.JBOSS_LISTEN_PORT, commandConfig, strategy, sudo, port);
                    resultMap.put(protocolName, result);
                }
            }
        } else {
            for (JsonNode sk : sokets) {
                if (sk.get("port") != null) {
                    String protocolName = sk.get("name").asText();
                    String tempPort = getActiveFinedPort(sk.get("port").asText(), runtimeOptions);
                    int port = NumberUtils.toInt(tempPort) + NumberUtils.toInt(offset);
                    commandAttr.put(protocolName, COMMAND.JBOSS_LISTEN_PORT.command(commandConfig, strategy.isWindows(), port));
                }
            }

            Map<String, String> commandMap = commandAttr;
            resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
        }

        Map<String, Object> res = objectMapper.convertValue(resultMap, Map.class);
        List<String> enabledPortKey = new ArrayList<>();

        /**
         * 결과 파싱
         */
        for (String key : res.keySet()) {
            HashMap mapKey = (HashMap) res.get(key);
            String error = (String) mapKey.get("result");
            if (!StringUtils.isEmpty(error)) {
                enabledPortKey.add(key);
            }
        }

        for (JsonNode soket : sokets) {
            LinkedHashMap<String, String> Sockets = new LinkedHashMap<>();
            for (String str : enabledPortKey) {
                if (str.equals(soket.get("name").asText())) {
                    Sockets.put("name", soket.get("name").asText());
                    int port = Integer.parseInt(getActiveFinedPort(soket.get("port").asText(), runtimeOptions)) + Integer.parseInt(offset);
                    Sockets.put("port", String.valueOf(port));
                    Sockets.put("interface", soket.get("interface") == null ? defaultInterFace : soket.get("interface").asText());
                    socketList.add(Sockets);

                }
            }
        }

        return socketList;
    }

    private String getActiveFinedPort(String variable, List<String> runtimeOptions) {
        String optionPath = new String();
        String port;

        if (variable.contains("$")) {
            variable = variable.substring(2, variable.length() - 1);
            String strPort = String.valueOf(jBossHelper.findNumber(variable));
            port = strPort;
            optionPath = Arrays.stream(variable.split(":")).collect(Collectors.toSet())
                    .stream().filter(o -> !o.equals(strPort)).findFirst().get();
        } else {
            port = variable;
        }

        for (String option : runtimeOptions) {
            if (option.contains(optionPath) && StringUtils.isNotEmpty(optionPath)) {
                int index = option.indexOf("=");
                String value = option.substring(index + 1);
                port = value;
            }
        }

        return port;
    }

    /**
     * 소켓 바인드 이름과 프로토콜 네임이 같은 경우 그룹으로 필터하고 port를 더해서  다시 배열에 담는다.
     *
     * @param socketList
     *
     * @return
     */
    private List<Map<String, String>> filterGroupBySoketList(List<Map<String, String>> socketList) {
        List<Map<String, String>> finalSocketList = new ArrayList<>();
        for (Map sc : socketList) {
            Map<String, String> tempSocket = new HashMap<>();
            String socketBindName = (String) sc.get("socketBindName");
            String name = (String) sc.get("name");
            String port = (String) sc.get("port");
            String inter = (String) sc.get("interface");

            boolean bl = finalSocketList.stream().filter(s -> s.get("socketBindName").equals(socketBindName) && s.get("name").equals(name)).findFirst().isEmpty();

            if (bl) {
                tempSocket.put("socketBindName", socketBindName);
                tempSocket.put("name", name);
                tempSocket.put("port", port);
                tempSocket.put("interface", inter);
                finalSocketList.add(tempSocket);
            } else {
                finalSocketList.stream().filter(s -> s.get("socketBindName").equals(socketBindName) && s.get("name").equals(name)).collect(Collectors.toList());
                finalSocketList.stream().forEach(attr -> {
                    attr.put("port", (String) attr.get("port") + ", " + port);
                });
            }
        }

        return finalSocketList;
    }

    private String getOnlyServerProfileName(JsonNode domainXml, String sevrGroup) {
        JsonNode serverGroups = domainXml.at(NODE.SERVERGROUPS.path());
        String profileName = "";
        for (JsonNode sever : serverGroups) {
            String SvrGroupName = sever.findPath("name").asText();
            if (sevrGroup.equals(SvrGroupName)) {
                profileName = sever.findPath("profile").asText();
                break;
            }
        }

        return profileName;
    }
}
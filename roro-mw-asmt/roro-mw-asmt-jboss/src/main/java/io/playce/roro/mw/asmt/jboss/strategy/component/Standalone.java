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
import io.playce.roro.mw.asmt.jboss.enums.ENGINE;
import io.playce.roro.mw.asmt.jboss.enums.NODE;
import io.playce.roro.mw.asmt.jboss.enums.STANDALONE_CONFIG_FILES;
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
public class Standalone implements ServerModeStrategy {

    private final CommandConfig commandConfig;

    private final JBossHelper jBossHelper;

    @Override
    public StrategyName getModeName() {
        return StrategyName.STANDALONE;
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
            Set<String> configFileName = instance.getRunTimeOptions().stream().filter(s -> s.contains(".xml")).collect(Collectors.toSet());

            for (String entity : configFileName) {
                String splitEntity = Arrays.stream(entity.split("\n")).findFirst().get();
                int idx = splitEntity.lastIndexOf(".");
                if (idx > 0) {
                    if (splitEntity.substring(idx + 1).equals("xml")) {
                        if (splitEntity.contains("=")) {
                            List<String> arrEntity = Arrays.stream(splitEntity.trim().split("=")).collect(Collectors.toList());
                            map.put(arrEntity.get(0), arrEntity.get(1));
                        } else {
                            map.put("-c", splitEntity);
                        }

                    }
                }
            }

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
        //String processNm = ENGINE.STANDALONE_NAME.codeName();
        jBossHelper.loadJavaVersion(targetHost, instance, strategy, instance.getDomainPath(), ENGINE.STANDALONE_NAME.codeName());
    }

    @Override
    public void setJavaVendor(TargetHost targetHost, Instance instance, GetInfoStrategy strategy, Engine engine) throws InterruptedException {
        // https://cloud-osci.atlassian.net/browse/PCR-6207
        // String processNm = ENGINE.STANDALONE_NAME.codeName();
        jBossHelper.loadJavaVendor(targetHost, instance, strategy, instance.getDomainPath(), ENGINE.STANDALONE_NAME.codeName());
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
                COMMAND.JBOSS_RUN_USER.command(commandConfig, strategy.isWindows(), instance.getDomainPath(), ENGINE.STANDALONE_PROVIDER.codeName(), instance.getHomeDir(), instance.getBaseDir()));

        Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
        RemoteExecResult result = resultMap.get(COMMAND.JBOSS_RUN_USER.name());

        if (!result.isErr()) {
            instance.setRunUser(result.getResult());
        } else {
            log.error("error run user: {}", result.getError());
        }
    }

    /**
     * 리소스 정보 저장
     *
     * @param instance
     * @param serverXml
     */
    public void setResources(JbossAssessmentResult.Instance instance, JsonNode serverXml) {
        JsonNode profileNode = serverXml.at(NODE.RESOURCE.path());

        if (profileNode.isMissingNode()) {
            return;
        }

        JsonNode dataSources = profileNode.findPath(RESOURCE.datasources.getCodeName()).findPath(RESOURCE.datasource.getCodeName());

        if (dataSources == null) {
            return;
        }

        int datasourceSize = JsonUtil.getNodeValueFromJsonNode(dataSources, Arrays.stream(RESOURCE.values()).map(RESOURCE::getCodeName).collect(Collectors.toList())).size();
        String xaClassName = profileNode.findPath(RESOURCE.datasources.getCodeName()).findPath("drivers").findPath("xa-datasource-class").asText();
        instance.setResources(getResources(datasourceSize, dataSources, xaClassName));
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
                    // Domain.java 파일도 동일하게 수정 필요
                }

                String servicename = (String) mapKey.get(DEPLOY.name.getCodeName());
                String deployFile = (String) mapKey.get(DEPLOY.runtimename.getCodeName());

                int idx = deployFile.lastIndexOf(".");

                application.setServiceName(servicename);
                application.setSourcePath(deployPath.endsWith("/") ? deployPath.substring(0, deployPath.length() - 1) : deployPath);
                application.setDeployFileName(deployFile);

                String contextPath = jBossHelper.getWebContextPath(deployPath, root, strategy, sudo, targetHost);
                if (StringUtils.isEmpty(contextPath)) {
                    contextPath = "/" + FilenameUtils.removeExtension(deployFile);
                }

                application.setContextPath(contextPath);
                application.setType(deployFile.substring(idx + 1));
                instance.getApplications().add(application);
            }
        }

        // for(JsonNode deployment : node){
        //     JbossAssessmentResult.Applications application = new JbossAssessmentResult.Applications();
        //     String servicename = deployment.get(DEPLOY.name.getCodeName()).asText();
        //     String deployFile = deployment.get(DEPLOY.runtimename.getCodeName()).asText();
        //     String deployPath = deployment.get(DEPLOY.type.getCodeName()).get(DEPLOY.path.getCodeName()).asText();
        //     int idx = deployFile.lastIndexOf(".");
        //
        //
        //     application.setServiceName(servicename);
        //     application.setSourcePath(deployPath);
        //     application.setDeployFileName(deployFile);
        //     application.setContextPath(jBossHelper.getWebContextPath(deployPath, root, strategy , sudo, targetHost));
        //     application.setType(deployFile.substring(idx + 1));
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
        JsonNode node = serverXml.findPath(RESOURCE.threadpools.getCodeName());

        if (node.isMissingNode()) {
            return;
        }

        List<Map<String, String>> list = new ArrayList<>();
        for (JsonNode thread : node) {
            Map threadObj = new HashMap();
            threadObj.put("threadPoolName", thread.get("name").asText());
            threadObj.put("maxThreadCnt", thread.findPath(RESOURCE.maxthreads.getCodeName()).get("count").asText());
            threadObj.put("keepalivetime", thread.findPath(RESOURCE.keepalivetime.getCodeName()).get("time").asText());
            threadObj.put("unit", thread.findPath(RESOURCE.keepalivetime.getCodeName()).get("unit").asText());
            list.add(threadObj);
        }

        instance.setThreads(list);
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
            map.put("inet-address", node.findPath("value").asText());
            interFaces.add(map);
        }
        //instance.setInterfaces(interFaces);
    }


    @Override
    public void setServers(JbossAssessmentResult.Instance instance, JsonNode serverXml, JsonNode hostFile,
                           GetInfoStrategy strategy, TargetHost targetHost, JbossAssessmentResult.Engine engine, boolean sudo) throws InterruptedException {
        // https://cloud-osci.atlassian.net/browse/PCR-6207
        // String Standalone = ENGINE.STANDALONE_NAME.codeName();

        List<JbossAssessmentResult.Instances> serverInstances = new ArrayList<>();
        JbossAssessmentResult.Instances conr = new JbossAssessmentResult.Instances();

        if (serverXml == null) {
            return;
        }

        JsonNode inBoundSocket = serverXml.at(NODE.CONNECTOR.path());
        if (inBoundSocket.isMissingNode()) {
            return;
        }

        List<String> opitons = getServerOptions(targetHost, strategy, sudo, instance.getDomainPath(), ENGINE.STANDALONE_NAME.codeName());
        conr.setSvrConnectors(instance.getConnectors());
        conr.setConfigPath(getValuesFromFindStr(opitons, "server.base.dir"));
        conr.setName(instance.getDomainName());
        conr.setMinHeap(getValuesFromFindStr(opitons, "Xms"));
        conr.setMaxHeap(getValuesFromFindStr(opitons, "Xmx"));
        conr.setMaxPermSize(getValuesFromFindStr(opitons, "MaxPermSize"));
        conr.setIpAddress(getValuesFromFindStr(opitons, "jboss.bind.address"));
        conr.setRunUser(getSvrRunUser(targetHost, instance, sudo, strategy, instance.getDomainPath(), ENGINE.STANDALONE_NAME.codeName()));
        conr.setIsRunning(instance.getIsRunning().equals("true"));
        conr.setJavaVersion(jBossHelper.loadJavaVersion(targetHost, instance, strategy, instance.getDomainPath(), ENGINE.STANDALONE_NAME.codeName()));
        conr.setJavaVendor(jBossHelper.loadJavaVendor(targetHost, instance, strategy, instance.getDomainPath(), ENGINE.STANDALONE_NAME.codeName()));
        conr.setPortOffset(getValuesFromFindStr(opitons, "port-offset"));
        conr.setRunTimeOptions(instance.getRunTimeOptions());
        conr.setSocketBindName(inBoundSocket.findPath("name").asText());
        serverInstances.add(conr);

        instance.setInstances(serverInstances);
    }

    @Override
    public void setConnectors(JbossAssessmentResult.Instance instance, JsonNode serverXml, JsonNode hostFile, GetInfoStrategy strategy, TargetHost targetHost, boolean sudo) throws InterruptedException {
        /**
         * 단일 STANDALONE_NAME 모드일때 socket bind 와  Domain 모드가 다름.
         */
        JsonNode inBoundSocket = serverXml.at(NODE.CONNECTOR.path() + "/socket-binding");

        if (inBoundSocket.isMissingNode()) {
            return;
        }

        String defaultInteface = serverXml.at(NODE.CONNECTOR.path()).findPath("default-interface").asText().isEmpty() ? "" : serverXml.at(NODE.CONNECTOR.path()).findPath("default-interface").asText();
        String soketName = serverXml.at(NODE.CONNECTOR.path()).get("name").asText();
        String portOffset = new String();
        List<Map<String, String>> socketBindList = JsonUtil.getNodeValueFromJsonNode(inBoundSocket, Arrays.stream(CONNECTOR.values()).map(CONNECTOR::getCodeName).collect(Collectors.toList()));

        //socketBindList = activeFilterSocketList(socketBindList, serverXml, defaultInteface) ;

        if (serverXml.at(NODE.CONNECTOR.path()).findPath(NODE.PORTOFFSET.path()) != null) {
            portOffset = serverXml.at(NODE.CONNECTOR.path()).findPath(NODE.PORTOFFSET.path()).asText();
        }

        /**
         * 프로세스에 offset 정보를 비교하여 port 값을 구한다.
         */
        int offsetValue = getOffsetVal(portOffset, instance.getRunTimeOptions());

        for (Map<String, String> map : socketBindList) {
            map.put("socketBindName", soketName);
            String httpPort = map.get(CONNECTOR.port.getCodeName());
            findConnectorPort(httpPort, instance.getRunTimeOptions(), CONNECTOR.port, map, strategy, offsetValue);
        }

        instance.setConnectors(getSvrSocketList(socketBindList, strategy, targetHost, sudo, defaultInteface));
        instance.getInstances().get(0).setSvrConnectors(instance.getConnectors());
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
            //     if (fileName.contains("standalone")) {
            //         fileKey = STANDALONE_CONFIG_FILES.JBOSS_STANDALONE_XML.name();
            //     }
            // }

            String path = root + strategy.getSeparator() + "configuration" + strategy.getSeparator() + fileName;

            tc.setPath(path);
            commandMap.put(fileKey, COMMAND.valueOf(STANDALONE_CONFIG_FILES.JBOSS_STANDALONE_XML.name()).command(commandConfig, strategy.isWindows(), tc.getPath()));
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
                if (STANDALONE_CONFIG_FILES.JBOSS_STANDALONE_XML.name().equals(key)) {
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
            for (STANDALONE_CONFIG_FILES configFile : STANDALONE_CONFIG_FILES.values()) {
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
                    if (configFile == STANDALONE_CONFIG_FILES.JBOSS_STANDALONE_XML) {
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
        //String controller = strategy.isWindows() ? "org.jboss.as.standalone" : "'Standalone'";
        String controller = strategy.isWindows() ? "org.jboss.as.standalone" : domainHomePath + "' | grep '" + ENGINE.STANDALONE_NAME.codeName();

        String processArgumentCommand = COMMAND.PROCESS_ARGUMENT.command(commandConfig, strategy.isWindows(), controller, controller);
        String responseString = getSshCommandResultTrim(targetHost, processArgumentCommand, COMMAND.PROCESS_ARGUMENT, strategy);
        vmoptions = Arrays.asList(responseString.split(StringUtils.SPACE));
        if (strategy.isWindows()) {
            vmoptions = vmoptions.stream().map(o -> o = o.replaceAll("\"", StringUtils.EMPTY)).filter(o -> strategy.isAbstractPath(o) || o.startsWith("-") || o.startsWith("org") || o.length() != 0).collect(Collectors.toList());
        }

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

        engine.setMode(ENGINE.STANDALONE_NAME.codeName());
    }


    private void findConnectorPort(String variable, List<String> options, CONNECTOR connector,
                                   Map<String, String> map, GetInfoStrategy strategy, int offsetValue) {
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

        for (String option : options) {
            // https://cloud-osci.atlassian.net/browse/PCR-6207
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
                    .collect(Collectors.toSet()).stream().findFirst().isEmpty() == true ? "" : options.stream().filter(s -> s.contains(offSetPortPath))
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

    /**
     * 프로세스에 현재 Active 되어 있는 port 및 프로토콜를 검색한다.
     *
     * @param socketItems
     *
     * @return
     */
    private List<Map<String, String>> activeFilterSocketList(List<Map<String, String>> socketItems, JsonNode serverXml, String defaultInteface) {
        List<Map<String, String>> list = new ArrayList<>();

        for (Map<String, String> item : socketItems) {
            String interFace = item.get(CONNECTOR.inter.getCodeName()) == null ? "" : item.get(CONNECTOR.inter.getCodeName());
            String socketName = item.get(CONNECTOR.name.getCodeName());

            if (StringUtils.isNotEmpty(interFace)) {
                List<JsonNode> el = serverXml.at("/" + interFace).findValues("socket-binding")
                        .stream().collect(Collectors.toList());
                for (JsonNode sk : el) {
                    if (socketName.equals(sk.elements().next().asText())) {
                        list.add(item);
                    }
                }
            } else {
                List<JsonNode> connectors = serverXml.at(NODE.RESOURCE.path() + NODE.SUBSYSTEM.path())
                        .findValues("connector").stream().collect(Collectors.toList());
                item.put(CONNECTOR.inter.getCodeName(), defaultInteface);
                for (JsonNode ct : connectors) {
                    if (socketName.equals(ct.get("socket-binding").asText())) {
                        list.add(item);
                    }
                }
            }
        }

        return list;
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
        String str = options.stream().filter(s -> s.contains(findStr)).collect(Collectors.toSet()).size() > 0 ? options.stream().filter(s -> s.contains(findStr)).collect(Collectors.toSet()).stream().findFirst().get() : "";
        String splitStr = "=";

        if (str.contains(splitStr)) {
            str = str.split(splitStr)[1];
        }

        return str;
    }

    public String getSvrRunUser(TargetHost targetHost, JbossAssessmentResult.Instance instance, boolean sudo, GetInfoStrategy strategy, String path, String name) throws InterruptedException {
        RemoteExecResult result = null;
        if (path.equals("Process Controller")) {
            Map<String, String> commandMap = Map.of(COMMAND.JBOSS_PROCESS_RUN_SERVER.name(),
                    COMMAND.JBOSS_PROCESS_RUN_SERVER.command(commandConfig, strategy.isWindows(), path, name, instance.getHomeDir(), instance.getBaseDir()));
            Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
            result = resultMap.get(COMMAND.JBOSS_PROCESS_RUN_SERVER.name());
        } else {
            Map<String, String> commandMap = Map.of(COMMAND.JBOSS_RUN_USER.name(),
                    COMMAND.JBOSS_RUN_USER.command(commandConfig, strategy.isWindows(), path, name, instance.getHomeDir(), instance.getBaseDir()));
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

    private List<LinkedHashMap> getResources(int datasourceSize, JsonNode dataSources, String xaClassName) {
        List<LinkedHashMap> list = new ArrayList<>();
        if (datasourceSize > 1) {
            for (JsonNode ds : dataSources) {
                LinkedHashMap map = new LinkedHashMap();
                map.put("poolName", jBossHelper.nodeNullToEmptyString(ds.get("pool-name")));
                map.put("driver", jBossHelper.nodeNullToEmptyString(ds.get(RESOURCE.driver.getCodeName())));
                map.put("jndiName", jBossHelper.nodeNullToEmptyString(ds.get("jndi-name")));
                map.put("userName", jBossHelper.nodeNullToEmptyString(ds.findPath("user-name")));
                map.put("password", jBossHelper.nodeNullToEmptyString(ds.findPath(RESOURCE.password.getCodeName())));
                map.put("useJavaContext", jBossHelper.nodeNullToEmptyString(ds.get("use-java-context")));
                map.put("connectionUrl", jBossHelper.nodeNullToEmptyString(ds.get("connection-url")));
                map.put("datasourceClass", xaClassName);
                map.put("enabled", jBossHelper.nodeNullToEmptyString(ds.get(RESOURCE.enabled.getCodeName())));
                list.add(map);
            }
        } else {
            LinkedHashMap linkMap = new LinkedHashMap();
            linkMap.put("poolName", jBossHelper.nodeNullToEmptyString(dataSources.get("pool-name")));
            linkMap.put("driver", jBossHelper.nodeNullToEmptyString(dataSources.get(RESOURCE.driver.getCodeName())));
            linkMap.put("jndiName", jBossHelper.nodeNullToEmptyString(dataSources.get("jndi-name")));
            linkMap.put("userName", jBossHelper.nodeNullToEmptyString(dataSources.findPath("user-name")));
            linkMap.put("password", jBossHelper.nodeNullToEmptyString(dataSources.findPath(RESOURCE.password.getCodeName())));
            linkMap.put("useJavaContext", jBossHelper.nodeNullToEmptyString(dataSources.get("use-java-context")));
            linkMap.put("connectionUrl", jBossHelper.nodeNullToEmptyString(dataSources.get("connection-url")));
            linkMap.put("datasourceClass", xaClassName);
            linkMap.put("enabled", jBossHelper.nodeNullToEmptyString(dataSources.get(RESOURCE.enabled.getCodeName())));
            list.add(linkMap);
        }

        return list;
    }

    /**
     * listen 하고 있는 포트 가져오기
     *
     * @param socketBindList
     * @param strategy
     * @param targetHost
     * @param sudo
     * @param defaultInterFace
     *
     * @return
     *
     * @throws InterruptedException
     */
    private List<Map<String, String>> getSvrSocketList(List<Map<String, String>> socketBindList, GetInfoStrategy strategy,
                                                       TargetHost targetHost, boolean sudo, String defaultInterFace) throws InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, String>> socketList = new ArrayList<>();
        Map<String, String> commandAttr = new HashMap<>();
        Map<String, RemoteExecResult> resultMap = new HashMap<>();

        if (strategy.isWindows()) {
            for (Map<String, String> map : socketBindList) {
                // https://cloud-osci.atlassian.net/browse/PCR-6207
                String protocolName = map.get("name");
                int port = Integer.parseInt(map.get("port") == null ? "0" : map.get("port"));
                RemoteExecResult result = executeCommand(targetHost, COMMAND.JBOSS_LISTEN_PORT, commandConfig, strategy, sudo, port);
                resultMap.put(protocolName, result);
            }
        } else {
            for (Map<String, String> map : socketBindList) {
                // https://cloud-osci.atlassian.net/browse/PCR-6207
                String protocolName = map.get("name");
                int port = Integer.parseInt(map.get("port") == null ? "0" : map.get("port"));
                commandAttr.put(protocolName, COMMAND.JBOSS_LISTEN_PORT.command(commandConfig, strategy.isWindows(), port));
            }
            /**
             * 커맨드 일괄 실행
             */
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

        for (Map<String, String> map : socketBindList) {
            LinkedHashMap<String, String> Sockets = new LinkedHashMap<>();
            for (String str : enabledPortKey) {
                if (str.equals(map.get("name"))) {
                    Sockets.put("name", map.get("name"));
                    int port = Integer.parseInt(map.get("port") == null ? "0" : map.get("port"));
                    Sockets.put("port", String.valueOf(port));
                    Sockets.put("socketBindName", map.get("socketBindName"));
                    Sockets.put("interface", map.get("interface") == null ? defaultInterFace : map.get("interface"));
                    socketList.add(Sockets);
                }
            }
        }

        return socketList;
    }
}
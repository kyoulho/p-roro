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
package io.playce.roro.mw.asmt.jeus;

import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.common.windows.JeusExtract;
import io.playce.roro.common.windows.TaskListResult;
import io.playce.roro.mw.asmt.AbstractMiddlewareAssessment;
import io.playce.roro.mw.asmt.MiddlewareAssessment;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.jeus.dto.JeusAssessmentResult;
import io.playce.roro.mw.asmt.jeus.dto.JeusAssessmentResult.Application;
import io.playce.roro.mw.asmt.jeus.enums.MiddlewareChecker;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.mw.asmt.util.WasAnalyzerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.playce.roro.common.util.ThreadLocalUtils.MW_SCAN_ERROR;
import static io.playce.roro.mw.asmt.AbstractMiddlewareAssessment.getJavaVendor;
import static io.playce.roro.mw.asmt.AbstractMiddlewareAssessment.getJavaVersion;
import static io.playce.roro.mw.asmt.util.MWCommonUtil.*;

@Component("JEUSAssessment")
@RequiredArgsConstructor
@Slf4j
public class JeusAssessment implements MiddlewareAssessment {
    private final CommandConfig commandConfig;
    //    private static final String BLANK = " ";
    private final JeusExtract jeusExtract;


    @Override
    public MiddlewareAssessmentResult assessment(TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        JeusAssessmentResult assessmentResult = new JeusAssessmentResult();
        String separator = strategy.getSeparator();

        /*
         *  1. 엔진 홈(Jeus home) 경로 추출
         *     1-1. solutionPath 사용
         *     1-2. ps -ef 조회
         *  2. {engineHome}/bin/jeusadmin -version 을 사용해서 해당 엔진의 버전 추출
         *  3. Domain home 추출
         *     3-1. domainPath 사용
         *     3-2. ps -ef 조회
         * */
        String engineHome = getEngineHome(middleware);
        Float version = getVersion(targetHost, middleware, engineHome, strategy);
        String domainHome = getDomainHome(targetHost, middleware, engineHome, version, strategy);

        log.debug(":+:+:+:+:+:+:+: engine home : [{}]", engineHome);
        log.debug(":+:+:+:+:+:+:+: engine version : [{}]", version);
        log.debug(":+:+:+:+:+:+:+: domain Home : [{}]", domainHome);

        if (StringUtils.isEmpty(domainHome)) {
            throw new InsufficientException("Jeus domain home not found. Please check java.security.policy option is exist or " + engineHome + separator + "config" + separator + "JEUSMain.xml file is exist.");
        }

        /*
         *  Configure files
         * */
        String contents = null;
        List<JeusAssessmentResult.ConfigFile> configFileResult = getConfigFiles(targetHost, domainHome, version, strategy);
        JeusAssessmentResult.Instance instance = new JeusAssessmentResult.Instance();
        instance.setConfigFiles(configFileResult);
        assessmentResult.setInstance(instance);

        // config 파일 save
        String ipAddress = targetHost.getIpAddress();
        if (configFileResult.size() > 0) {
            for (JeusAssessmentResult.ConfigFile configFile : configFileResult) {
                if (FilenameUtils.getName(configFile.getPath()).equals("domain.xml") || FilenameUtils.getName(configFile.getPath()).equals("JEUSMain.xml")) {
                    contents = configFile.getContents();
                }

                WasAnalyzerUtil.saveAssessmentFile(ipAddress, configFile.getPath(), configFile.getContents(), CommonProperties.getWorkDir(), strategy);
            }
        }

        if (StringUtils.isEmpty(contents)) {
            contents = getMainXmlFile(targetHost, middleware, domainHome, version, strategy);
        }

        if (StringUtils.isEmpty(contents)) {
            throw new InsufficientException("Jeus config file read failed. Please check domain.xml or JEUSMain.xml file is exist in \"" +
                    targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + domainHome + "\"");
        }

        if (!contents.startsWith("<?xml")) {
            log.warn("XML declaration not found. Generate XML declaration.");
        }

        analyzeJeus(contents, targetHost, middleware, engineHome, domainHome, version, assessmentResult, strategy);

        // assessmentResult.setThirdPartySolutions(ThirdPartySolutionUtil.detectThirdPartySolutionsFromMiddleware(targetHost, strategy.isWindows(), engineHome, domainHome));

        return assessmentResult;
    }

    private void analyzeJeus(String sourceString, TargetHost targetHost, MiddlewareInventory middleware, String engineHome, String domainHome, Float version, JeusAssessmentResult assessmentResult, GetInfoStrategy strategy) throws InterruptedException {
        JSONObject entryPoint = null;
        JSONObject rootJsonFile = XML.toJSONObject(sourceString);
        // version higher 6.0
        if (rootJsonFile.has("domain")) {
            entryPoint = rootJsonFile.getJSONObject("domain");
        } else if (rootJsonFile.has("jeus-system")) {
            entryPoint = rootJsonFile.getJSONObject("jeus-system");
        }

        /*
         *  Jeus 설정 관련 데이터 파싱 (Version, Jeus home, etc)
         * */
        analyzeEngine(entryPoint, middleware, engineHome, version, assessmentResult);

        /*
         *  Application 서버 인스턴스 설정 관련 데이터 파싱
         * */
        analyzeInstance(entryPoint, targetHost, middleware, engineHome, domainHome, version, assessmentResult, strategy);
    }

    private void analyzeInstance(JSONObject source, TargetHost targetHost, MiddlewareInventory middleware, String engineHome, String domainHome, Float version, JeusAssessmentResult assessmentResult, GetInfoStrategy strategy) throws InterruptedException {
        JeusAssessmentResult.Instance instance = (JeusAssessmentResult.Instance) assessmentResult.getInstance();

        if (instance == null) {
            instance = new JeusAssessmentResult.Instance();
        }

        /*
         *  Engine VM options
         * */
        // 삭제예정..
        List<String> vmOptions = getVmOptions(targetHost, middleware, engineHome, version, strategy);
//        instance.setVmOptions(vmOptions);
//        log.debug("Engine VM options: {}", vmOptions.toString());

        /*
         * Get application deployed default directory
         */
        String appHome = getAppHome(vmOptions, engineHome, strategy);

        /*
         * Server 정보 수집
         * */
        JSONArray servers = new JSONArray();
        if (version > 6.0) {
            servers = source.getJSONObject("servers").getJSONArray("server");
        } else {
            servers = convertJSONArray(source.get("node"));
        }
        List<JeusAssessmentResult.Instances> serverResult = getServers(servers, targetHost, version, domainHome, strategy);
        instance.setInstances(serverResult);

        log.debug("Total discovered server count : {}", serverResult.size());

        /*
         * 배포된 애플리케이션 정보 수집
         * */
        JSONArray applications = new JSONArray();
        if (version > 6.0) {
            applications = convertJSONArray(source.getJSONObject("deployed-applications").get("deployed-application"));
        } else {
            applications = convertJSONArray(source.get("application"));
        }
        List<JeusAssessmentResult.Application> appResult = analyzeApplications(applications, targetHost, middleware, appHome, strategy);
        instance.setApplications(appResult);

        log.debug("Total discovered app count : {}", appResult.size());
        for (JeusAssessmentResult.Application app : appResult) {
            log.debug("[{}] Application deploy path : {}", app.getId(), app.getSourcePath());
            log.debug("[{}] Application target servers : {}", app.getId(),
                    app.getTarget() != null ? app.getTarget().toString() : null);
        }

        /*
         * 리소스 정보 수집
         * */
        JSONObject resources = null;
        if (version > 6.0) {
            if (source.has("resources")) {
                resources = source.getJSONObject("resources");
            }
        } else {
            if (source.has("resource")) {
                resources = source.getJSONObject("resource");
            }
        }
        JeusAssessmentResult.Resources resourcesResult = getResources(resources);
        instance.setResources(resourcesResult);


        /*
         *  클러스터 정보 수집
         * */
        if (version > 6.0) {
            if (source.has("clusters")) {
                JSONObject clusters = source.getJSONObject("clusters");
                JeusAssessmentResult.Clusters clustersResult = analyzeCluster(clusters);
                instance.setClusters(clustersResult);
            }
        }

        /*
         * 세션 클러스터 컨피그 정보 수집
         * */
        if (source.has("session-cluster-config")) {
            JSONObject sessionClusterConfig = source.getJSONObject("session-cluster-config");
            JeusAssessmentResult.SessionClusterConfig jsessionClusterConfig
                    = getSessionClusterConfig(sessionClusterConfig);
            instance.setSessionClusterConfig(jsessionClusterConfig);
        }

        // target-cluster로 된 Application을 servers에 등록된 Cluster에서 Server 목록을 가져온다.
        if (instance.getClusters() != null && instance.getClusters().getCluster() != null) {
            instance = getApplicationClusterServerName(instance);
        }

        // minHeap & maxHeap
//        instance.setMinHeap(getHeapSize("-Xms", targetHost));
//        instance.setMaxHeap(getHeapSize("-Xmx", targetHost));

        // run User
//        instance.setRunUser(getRunUser(targetHost));

        // java version
//        instance.setJavaVersion(getJavaVersion(targetHost));
        Optional<JeusAssessmentResult.Instances> first = instance.getInstances().stream().filter(i -> {
            String status = i.getStatus();
            return StringUtils.isNotEmpty(status) && status.startsWith("Running");
        }).findFirst();

        if (first.isPresent()) {
            JeusAssessmentResult.Instances firstInstance = first.get();
            String javaVersion = getJavaVersion(targetHost, "-D" + firstInstance.getName(), commandConfig, strategy);
            instance.setJavaVersion(javaVersion);

            String javaVendor = getJavaVendor(targetHost, "-D" + firstInstance.getName(), commandConfig, strategy);
            if (StringUtils.isEmpty(javaVendor) && StringUtils.isNotEmpty(javaVersion)) {
                javaVendor = ORACLE_JAVA_VENDOR;
            }
            instance.setJavaVendor(javaVendor);
        } else {
            String javaVersion = getJavaVersionFromJAVA_HOME(targetHost, commandConfig, strategy);
            instance.setJavaVersion(javaVersion);

            String javaVendor = getJavaVersionFromJAVA_VENDOR(targetHost, commandConfig, strategy);
            if (StringUtils.isEmpty(javaVendor) && StringUtils.isNotEmpty(javaVersion)) {
                javaVendor = ORACLE_JAVA_VENDOR;
            }
            instance.setJavaVendor(javaVendor);

            if (StringUtils.isEmpty(instance.getJavaVersion())) {
                instance.setJavaVersion("UNKNOWN");
            }
        }

        assessmentResult.setInstance(instance);
    }

    private String getAppHome(List<String> vmOptions, String engineHome, GetInfoStrategy strategy) {
        String separator = strategy.getSeparator();
        String appHome = engineHome + separator + "webhome" + separator + "app_home";

        for (String option : vmOptions) {
            int idx = option.indexOf("app.home=");

            if (idx > -1) {
                appHome = option.substring(idx + 9);
            }
        }

        return appHome;
    }

    private List<JeusAssessmentResult.ConfigFile> getConfigFiles(TargetHost targetHost, String domainHome, Float version, GetInfoStrategy strategy) throws InterruptedException {
        List<JeusAssessmentResult.ConfigFile> targetList = new ArrayList<>();

        /*
         *  WebtoB config files.
         * */
        // 2021.8.18 Jeus에서 사용하는 config 파일은 domain.xml 이나 JEUSMain.xml 만 있으면 됨.
        // domain home이 아닌 engine home 하위의 webserver 관련 설정은 필요 없음.
        String path = null;
        String contents = null;
        String separator = strategy.getSeparator();
        if (version > 6.0) {
            path = domainHome + separator + "config" + separator + "domain.xml";
        } else {
            path = domainHome + separator + "JEUSMain.xml";
        }

        if (AbstractMiddlewareAssessment.fileExists(targetHost, path, commandConfig, strategy)) {
            contents = AbstractMiddlewareAssessment.getFileContents(targetHost, path, commandConfig, strategy);
        }

        if (contents != null) {
            JeusAssessmentResult.ConfigFile target = new JeusAssessmentResult.ConfigFile();
            target.setPath(path);
            target.setContents(contents);
            targetList.add(target);
        }

        // List<String> confList = getFileLists(engineHome, "/webserver/config", null);
        //
        // for (String path : confList) {
        //     JeusAssessmentResult.ConfigFile target = new JeusAssessmentResult.ConfigFile();
        //     target.setPath(path.replace("\r", ""));
        //     target.setContents(getFileContents(null, null, path.replace("\r", "")));
        //     targetList.add(target);
        //
        //     log.debug("Discovered configuration files : {}", target.getPath());
        // }

        // String result = executeCommand(command);
        // if (result != null && result.length() > 1) {
        //     for (String path : result.split("\\n")) {
        //         JeusAssessmentResult.ConfigFile target = new JeusAssessmentResult.ConfigFile();
        //         target.setPath(path.replace("\r", ""));
        //         target.setContents(getFileContents(path.replace("\r", "")));
        //         targetList.add(target);
        //
        //         log.debug("Discovered configuration files : {}", target.getPath());
        //     }
        // }

        return targetList;
    }

    private List<String> getVmOptions(TargetHost targetHost, MiddlewareInventory middleware, String engineHome, Float version, GetInfoStrategy strategy) throws InterruptedException {
        String name = middleware.getName();

        String processName;
        if (strategy.isWindows()) {
            processName = "jeus.home=" + engineHome.replaceAll("\\\\", "\\\\\\\\");
        } else {
            // https://cloud-osci.atlassian.net/browse/ROROQA-1047
            processName = "java' | grep 'jeus.home=" + engineHome;

            // Middleware 이름이 Jeus-{ServerName}인 경우에는 RoRo에서 임의로 지어준 이름으로 프로세스 상에서 조회되지 않는다.
            if (StringUtils.isNotEmpty(name) && !name.startsWith(MiddlewareChecker.JEUS.getType() + "-")) {
                processName += "' | grep '" + name;
            }
        }

        String[] jvmOptionArray = WasAnalyzerUtil.getProcessArgument(targetHost, processName, commandConfig, strategy, "jeus.server.NodemanagerBootstrapper", "jeus.nodemanager.JeusNodeManager");

        List<String> vmOptions;
        if (jvmOptionArray.length > 0) {
            vmOptions = Arrays.asList(jvmOptionArray);
        } else {
            vmOptions = new ArrayList<>();
            String script = null;
            if (version > 6.0) {
                script = AbstractMiddlewareAssessment.getFileContents(targetHost, engineHome + "/bin/startDomainAdminServer", commandConfig, strategy);
            } else {
                script = AbstractMiddlewareAssessment.getFileContents(targetHost, engineHome + "/bin/jeus", commandConfig, strategy);
            }

            for (String line : script.split("\\n")) {
                line = line.replaceAll("^\\s+", StringUtils.EMPTY).trim();

                // -로 시작하고 =가 포함되는 항목외의 VM 옵션도 존재하기 때문에 -로 시작하는 항목을 모두 추출한다.
                // -로 시작하지 않는 옵션도 있으나 해당 파일은 스크립트 파일로써 모든 내용이 VM 옵션으로 포함될 수 있기 때문에 -로 시작하는 것을 포함한다.
                if (line.startsWith("-")) {
                    vmOptions.add(line);
                }
            }
        }

        return vmOptions;
    }

//    private String getJavaVersion(TargetHost targetHost) throws InterruptedException {
//        String javaHomeCommand = "echo $JAVA_HOME";
//        String responseString = SSHUtil.executeCommand(targetHost, javaHomeCommand).trim();
//        if (StringUtils.isNotEmpty(responseString)) {
//            String javaVersionCommand = responseString + File.separator + "bin" + File.separator + "java -version 2>&1 | head -n 1 | awk -F '\"' '{print $2}'";
//            return SSHUtil.executeCommand(targetHost, javaVersionCommand).trim();
//        } else {
//            String javaVersionCommand = "java -version 2>&1 | head -n 1 | awk -F '\"' '{print $2}'";
//            return SSHUtil.executeCommand(targetHost, javaVersionCommand).trim();
//        }
//    }

//    private String getRunUser(TargetHost targetHost) throws InterruptedException {
//        // Jeus 6 서버의 bootstrapper
//        String runUserCommand = "ps -ef | grep jeus.server.JeusBootstrapper | grep -v grep | awk '{print $1}' | uniq";
//        String result = SSHUtil.executeCommand(targetHost, runUserCommand).trim();
//
//        if (StringUtils.isNotEmpty(result)) {
//            return result;
//        } else {
//            // Jeus 7 서버의 DAS bootstrapper
//            runUserCommand = "ps -ef | grep jeus.server.admin.DomainAdminServerBootstrapper | grep -v grep | awk '{print $1}' | uniq";
//            result = SSHUtil.executeCommand(targetHost, runUserCommand).trim();
//
//            return StringUtils.defaultString(result);
//        }
//    }

//    private String getHeapSize(String attribute, TargetHost targetHost) throws InterruptedException {
//        String serverProcessCommand = "ps -e -o cmd | grep jeus | tr ' ' '\\n'";
//        String responseString = SSHUtil.executeCommand(targetHost, serverProcessCommand).trim();
//
//        List<Integer> heapList = new ArrayList<>();
//        if (StringUtils.isNotEmpty(responseString)) {
//            String[] jvmOptions = responseString.split("\\r?\\n");
//            for (String jvmOption : jvmOptions) {
//                if (jvmOption.startsWith(attribute)) {
//                    heapList.add(Integer.parseInt(jvmOption.replaceAll("[^0-9]", "")));
//                }
//            }
//        }
//        return heapList.isEmpty() ? "" : Collections.min(heapList) + "m";
//    }

    private JeusAssessmentResult.SessionClusterConfig getSessionClusterConfig(JSONObject source) {
        JeusAssessmentResult.SessionClusterConfig target = new JeusAssessmentResult.SessionClusterConfig();

        target.setUsingSessionCluster(getValue(source, "using-session-cluster"));

        if (source.has("session-clusters")) {
            JeusAssessmentResult.SessionClusters sessionClusters = getSessionClusters(convertJSONArray(source.get("session-clusters")));
            target.setSessionClusters(sessionClusters);
        }

        if (source.has("common-cluster-config")) {
            JeusAssessmentResult.ClusterConfig commonClusterConfig = getClusterConfig(source.getJSONObject("common-cluster-config"));
            target.setCommonClusterConfig(commonClusterConfig);
        }
        return target;
    }

    private JeusAssessmentResult.SessionClusters getSessionClusters(JSONArray source) {

        JeusAssessmentResult.SessionClusters target = new JeusAssessmentResult.SessionClusters();

        List<JeusAssessmentResult.SessionCluster> sessionClusterList = new ArrayList<>();
        for (int i = 0; i < source.length(); i++) {
            JeusAssessmentResult.SessionCluster sessionCluster = getSessionCluster(source.getJSONObject(i));
            sessionClusterList.add(sessionCluster);
        }

        target.setSessionClusters(sessionClusterList);

        return target;
    }

    private JeusAssessmentResult.SessionCluster getSessionCluster(JSONObject source) {
        JeusAssessmentResult.SessionCluster target = new JeusAssessmentResult.SessionCluster();

        if (source.has("session-cluster")) {
            JSONObject obj = source.getJSONObject("session-cluster");

            target.setName(getValue(obj, "name"));
            if (obj.has("cluster-config")) {
                JeusAssessmentResult.ClusterConfig clusterConfig = getClusterConfig(obj.getJSONObject("cluster-config"));
                target.setClusterConfig(clusterConfig);
            }
        }

        return target;
    }

    private JeusAssessmentResult.ClusterConfig getClusterConfig(JSONObject source) {
        JeusAssessmentResult.ClusterConfig target = new JeusAssessmentResult.ClusterConfig();

        if (source.has("jeus-login-manager")) {
            JSONObject obj = source.getJSONObject("jeus-login-manager");
            JeusAssessmentResult.JeusLoginManager jeusLoginManager = new JeusAssessmentResult.JeusLoginManager();
            jeusLoginManager.setPrimary(getValue(obj, "primary"));
            jeusLoginManager.setSecondary(getValue(obj, "secondary"));
            target.setJeusLoginManager(jeusLoginManager);
        }

        for (String key : source.keySet()) {
            if ("connect-timeout".equals(key)) {
                target.setConnectTimout(getValue(source, key));
            } else if ("backup-level".equals(key)) {
                target.setBackupLevel(getValue(source, key));
            } else if ("failover-delay".equals(key)) {
                target.setFailoverDelay(getValue(source, key));
            } else if ("read-timeout".equals(key)) {
                target.setReadTimeout(getValue(source, key));
            } else if ("reserved-thread-num".equals(key)) {
                target.setReservedThreadNum(getValue(source, key));
            } else if ("restart-delay".equals(key)) {
                target.setRestartDelay(getValue(source, key));
            } else if ("allow-fail-back".equals(key)) {
                target.setAllowFailBack(getValue(source, key));
            } else if ("file-db".equals(key)) {
                JeusAssessmentResult.FileDb fileDb = getFileDb(source.getJSONObject("file-db"));
                target.setFileDB(fileDb);
            }
        }


        return target;
    }

    private JeusAssessmentResult.FileDb getFileDb(JSONObject source) {
        JeusAssessmentResult.FileDb target = new JeusAssessmentResult.FileDb();

        if (source.has("passivation-timeout")) {
            target.setPassivationTimeout(getValue(source, "passivation-timeout"));
        }
        if (source.has("min-hole")) {
            target.setMinHole(getValue(source, "min-hole"));
        }
        if (source.has("packing-rate")) {
            target.setPackingRate(getValue(source, "packing-rate"));
        }

        return target;
    }

    private JeusAssessmentResult.Clusters analyzeCluster(JSONObject clusterSources) {
        JeusAssessmentResult.Clusters clusters = new JeusAssessmentResult.Clusters();
        List<JeusAssessmentResult.Cluster> list = new ArrayList<>();
        Map<String, Object> properties = new HashMap<>();

        for (String key : clusterSources.keySet()) {
            JSONArray source = convertJSONArray(clusterSources.get(key));

            if ("cluster".equals(key)) {
                for (int idx = 0; idx < source.length(); idx++) {
                    JeusAssessmentResult.Cluster cluster = getCluster(source.getJSONObject(idx));
                    list.add(cluster);
                }
            } else {
                properties.put(key, clusterSources.get(key));
            }
        }

        clusters.setCluster(list);
        clusters.setProperties(properties);

        return clusters;
    }

    private JeusAssessmentResult.Cluster getCluster(JSONObject source) {
        JeusAssessmentResult.Cluster cluster = new JeusAssessmentResult.Cluster();
        //ToDo: 클러스터의 상태 정보 구하는 로직 개발해야 함.
        cluster.setStatus("N/A");

        Map<String, Object> options = new HashMap<>();
        for (String field : source.keySet()) {
            if ("name".equals(field)) {
                cluster.setName(source.getString(field));
            } else if ("servers".equals(field)) {
                JSONObject servers = source.getJSONObject(field);
                JSONArray serverNames = convertJSONArray(servers.get("server-name"));

                List<String> targets = new ArrayList<>();
                if (serverNames != null) {
                    for (int i = 0; i < serverNames.length(); i++) {
                        targets.add(serverNames.getString(i));
                    }
                }

                cluster.setServers(targets);
            } else if ("session-router-config".equals(field)) {
                JSONObject sessionConfig = source.getJSONObject(field);
                JeusAssessmentResult.SessionRouterConfig config = getSessionRouterConfig(sessionConfig);
                cluster.setSessionRouterConfig(config);
            } else {
                options.put(field, source.get(field));
            }
        }

        cluster.setOptions(options);
        return cluster;
    }

    private JeusAssessmentResult.SessionRouterConfig getSessionRouterConfig(JSONObject source) {
        JeusAssessmentResult.SessionRouterConfig config = new JeusAssessmentResult.SessionRouterConfig();

        for (String key : source.keySet()) {
            if ("connect-timeout".equals(key)) {
                config.setConnectTimout(getValue(source, key));
            } else if ("backup-level".equals(key)) {
                config.setBackupLevel(getValue(source, key));
            } else if ("failover-delay".equals(key)) {
                config.setFailoverDelay(getValue(source, key));
            } else if ("read-timeout".equals(key)) {
                config.setReadTimeout(getValue(source, key));
            } else if ("reserved-thread-num".equals(key)) {
                config.setReservedThreadNum(getValue(source, key));
            } else if ("restart-delay".equals(key)) {
                config.setRestartDelay(getValue(source, key));
            } else if ("allow-fail-back".equals(key)) {
                config.setAllowFailBack(getValue(source, key));
            }
        }

        return config;

    }

    private JeusAssessmentResult.Resources getResources(JSONObject source) {
        JeusAssessmentResult.Resources resources = new JeusAssessmentResult.Resources();
        if (source != null) {
            if (source.has("data-source")) {
                JSONObject dataSource = source.getJSONObject("data-source");
                if (dataSource.has("database")) {
                    JSONArray databaseSource = convertJSONArray(dataSource.get("database"));
                    List<JeusAssessmentResult.Database> database = getDatabases(databaseSource);
                    resources.setDatabases(database);
                }
            }
        }
        return resources;
    }

    private List<JeusAssessmentResult.Database> getDatabases(JSONArray databaseSource) {
        List<JeusAssessmentResult.Database> databaselist = new ArrayList<>();
        for (int j = 0; j < databaseSource.length(); j++) {

            JSONObject source = databaseSource.getJSONObject(j);

            JeusAssessmentResult.Database target = new JeusAssessmentResult.Database();
            target.setAutoCommit(getValue(source, "auto-commit"));
            target.setDataSourceClassName(getValue(source, "data-source-class-name"));
            target.setDataSourceId(getValue(source, "data-source-id"));
            target.setDataSourceType(getValue(source, "data-source-type"));
            target.setDataSourceTarget(getValue(source, "data-source-target"));
            target.setDatabaseName(getValue(source, "database-name"));
            target.setDescription(getValue(source, "description"));
            target.setExportName(getValue(source, "export-name"));
            target.setIsolationLevel(getValue(source, "isolation-level"));
            target.setLoginTimeout(getValue(source, "login-timeout"));
            target.setPassword(getValue(source, "password"));
            target.setPoolDestroyTimeout(getValue(source, "pool-destroy-timeout"));
            target.setPortNumber(getValue(source, "port-number"));
            target.setServerName(getValue(source, "server-name"));
            target.setStmtQueryTimeout(getValue(source, "stmt-query-timeout"));
            target.setSupportXaEmulation(getValue(source, "support-xa-emulation"));
            target.setUser(getValue(source, "user"));
            target.setVendor(getValue(source, "vendor"));


            if (source.has("connection-pool")) {
                JSONObject sourceConPool = source.getJSONObject("connection-pool");
                JeusAssessmentResult.ConnectionPool connectionPool = getConnectionPool(sourceConPool);
                target.setConnectionPool(connectionPool);
            }

            if (source.has("property")) {
                JSONArray sourceProperty = convertJSONArray(source.get("property"));
                List<JeusAssessmentResult.DatabaseProperty> sourcePropertyList = new ArrayList<>();
                for (int i = 0; i < sourceProperty.length(); i++) {
                    JeusAssessmentResult.DatabaseProperty property = getDatabaseProperty(sourceProperty.getJSONObject(i));
                    sourcePropertyList.add(property);
                }
                target.setProperty(sourcePropertyList);
            }

            databaselist.add(target);
        }

        return databaselist;
    }

    private JeusAssessmentResult.DatabaseProperty getDatabaseProperty(JSONObject source) {
        JeusAssessmentResult.DatabaseProperty target = new JeusAssessmentResult.DatabaseProperty();
        target.setName(getValue(source, "name"));
        target.setType(getValue(source, "type"));
        target.setValue(getValue(source, "value"));
        return target;
    }

    private JeusAssessmentResult.ConnectionPool getConnectionPool(JSONObject source) {
        JeusAssessmentResult.ConnectionPool target = new JeusAssessmentResult.ConnectionPool();
        target.setUseSqlTrace(getValue(source, "use-sql-trace"));
        target.setStmtFetchSize(getValue(source, "stmt-fetch-size"));
        target.setStmtCachingSize(getValue(source, "stmt-caching-size"));
        target.setMaxUseCount(getValue(source, "max-use-count"));
        target.setKeepConnectionHandleOpen(getValue(source, "keep-connection-handle-open"));
        target.setDbaTimeout(getValue(source, "dba-timeout"));
        target.setCheckQuery(getValue(source, "check-query"));
        target.setCheckQueryPeriod(getValue(source, "check-query-period"));

        JSONObject sourcePool = source.getJSONObject("pooling");
        JeusAssessmentResult.Pooling pooling = getPooling(sourcePool);
        target.setPooling(pooling);

        if (source.has("connection-trace")) {
            JSONObject sourceTrace = source.getJSONObject("connection-trace");
            JeusAssessmentResult.ConnectionTrace trace = getConnectionTrace(sourceTrace);
            target.setConnectionTrace(trace);
        }

        if (source.has("wait-free-connection")) {
            JSONObject sourceWait = source.getJSONObject("wait-free-connection");
            JeusAssessmentResult.WaitFreeConnection waitFreeConnection = getConnectionWaitFree(sourceWait);
            target.setWaitFreeConnection(waitFreeConnection);
        }


        return target;
    }

    private JeusAssessmentResult.WaitFreeConnection getConnectionWaitFree(JSONObject source) {
        JeusAssessmentResult.WaitFreeConnection target = new JeusAssessmentResult.WaitFreeConnection();
        target.setEnableWait(getValue(source, "enable-wait"));
        target.setWaitTime(getValue(source, "wait-time"));
        return target;
    }

    private JeusAssessmentResult.ConnectionTrace getConnectionTrace(JSONObject source) {
        JeusAssessmentResult.ConnectionTrace target = new JeusAssessmentResult.ConnectionTrace();
        target.setAutoCommitTrace(getValue(source, "auto-commit-trace"));
        target.setEnabled(getValue(source, "enabled"));
        target.setGetConnectionTrace(getValue(source, "get-connection-trace"));
        return target;
    }

    private JeusAssessmentResult.Pooling getPooling(JSONObject source) {
        JeusAssessmentResult.Pooling target = new JeusAssessmentResult.Pooling();
        target.setMax(getValue(source, "max"));
        target.setMin(getValue(source, "min"));
        target.setPeriod(getValue(source, "period"));
        target.setStep(getValue(source, "step"));

        return target;
    }

    private List<JeusAssessmentResult.Application> analyzeApplications(JSONArray source, TargetHost targetHost, MiddlewareInventory middleware, String appHome, GetInfoStrategy strategy) throws InterruptedException {

        List<JeusAssessmentResult.Application> targetList = new ArrayList<>();

        for (int i = 0; i < source.length(); i++) {
            JeusAssessmentResult.Application target = getApplication(source.getJSONObject(i), targetHost, middleware, strategy);

            String enginePath = target.getSourcePath();
            if (!(enginePath.startsWith("/") || enginePath.charAt(1) == ':')) {
                target.setSourcePath(appHome + strategy.getSeparator() + target.getSourcePath());
            }
            targetList.add(target);
        }

        return targetList;
    }

    private JeusAssessmentResult.Application getApplication(JSONObject source, TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        JeusAssessmentResult.Application target = new JeusAssessmentResult.Application();
        if (source.has("id")) {
            target.setId(getValue(source, "id"));
        } else if (source.has("name")) {
            target.setId(getValue(source, "name"));
        } else {
            log.warn("Application tag not found : id or name .");
            target.setId(StringUtils.EMPTY);
        }

        if (source.has("type")) {
            target.setType(getValue(source, "type"));
        } else if (source.has("deployment-type")) {
            target.setType(getValue(source, "deployment-type"));
        } else {
            log.warn("Application tag not found : type or deployment-type .");
            target.setType(StringUtils.EMPTY);
        }

        if (source.has("path")) {
            target.setSourcePath(getValue(source, "path"));
        } else {
            log.warn("Application tag not found : path .");
            target.setSourcePath(StringUtils.EMPTY);
        }

        // ToDo: temporary
        if ("AIX".equalsIgnoreCase(middleware.getInventoryDetailTypeCode())) {

            SimpleDateFormat DATE_FORMAT1 = new SimpleDateFormat("MMM-dd-yyyy", Locale.US);
            SimpleDateFormat DATE_FORMAT2 = new SimpleDateFormat("yyyy-MMM-dd-HH:mm", Locale.US);

            String command = "sudo ls -al " + target.getSourcePath();

            String checkDir = "sudo sh -c \"[ -d '" + target.getSourcePath() + "' ] && echo 'DIR' || echo 'FILE'\"";
            String result = SSHUtil.executeCommand(targetHost, checkDir);

            if (result.contains("DIR")) {
                command = command + "| grep " + target.getSourcePath();
            }
            command = command + " | awk {'print $6\"-\"$7\"-\"$8'} | tr -d '\n'";
            result = SSHUtil.executeCommand(targetHost, command);

            try {
                if (result.contains(":")) {
                    result = Calendar.getInstance().get(Calendar.YEAR) + "-" + result;
                    target.setDeployedDate(DATE_FORMAT2.format(DATE_FORMAT2.parse(result)));
                } else {
                    target.setDeployedDate(DATE_FORMAT1.format(DATE_FORMAT1.parse(result)));
                }
            } catch (Exception e) {
                RoRoException.checkInterruptedException(e);
                log.debug("Unhandled exception occurred while check application deployed date [{}]", result);
                ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while application deployed date. Detail : [" + e.getMessage() + "]");
                target.setDeployedDate("N/A");
            }
        } else {
//            String checkDir = "sudo bash -c \"[ -d '" + target.getSourcePath() + "' ] && echo 'DIR' || echo 'FILE'\"";
//            String result = SSHUtil.executeCommand(targetHost, checkDir);
//            String command = "sudo ls -l " + target.getSourcePath() + " --time-style long-iso ";
//            if (result.contains("DIR")) {
//                command = command + "| grep " + target.getSourcePath();
//            }
//            command = command + " | awk '{print $6\" \"$7}'| tr -d '\n'";
//            result = SSHUtil.executeCommand(targetHost, command);
//            target.setDeployedDate(result.replaceAll("\\n", ""));

//            String checkDir = COMMAND.JEUS_CHECK_DIR.command(commandConfig, strategy.isWindows(), target.getSourcePath());
//            String result = AbstractMiddlewareAssessment.getSshCommandResultTrim(targetHost, checkDir, COMMAND.JEUS_DEPLOYED_DATE, strategy);

//            String command = COMMAND.JEUS_DEPLOYED_DATE.command(commandConfig, strategy.isWindows(), target.getSourcePath(), target.getSourcePath());
//            String result = AbstractMiddlewareAssessment.getSshCommandResultTrim(targetHost, command, COMMAND.JEUS_DEPLOYED_DATE, strategy);

            String result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_DEPLOYED_DATE, commandConfig, strategy, target.getSourcePath(), target.getSourcePath());
            target.setDeployedDate(result);
        }

        if (source.has("targets")) {
            JSONObject targets = source.getJSONObject("targets");

            if (targets != null && targets.has("server")) {
                JSONArray targetServers = convertJSONArray(targets.get("server"));
                List<String> serverList = new ArrayList<>();
                for (int j = 0; j < targetServers.length(); j++) {
                    JSONObject targetServer = targetServers.getJSONObject(j);
                    serverList.add(targetServer.getString("name"));
                }
                target.setTarget(serverList);
            } else if (targets != null && targets.has("cluster")) {
                JSONArray targetClusters = convertJSONArray(targets.get("cluster"));
                List<String> serverList = new ArrayList<>();
                for (int j = 0; j < targetClusters.length(); j++) {
                    JSONObject targetCluster = targetClusters.getJSONObject(j);
                    serverList.add(targetCluster.getString("name"));
                }
                target.setTarget(serverList);
            } else {
                log.warn("Application tag not found : server or cluster.");
                target.setTarget(List.of(StringUtils.EMPTY));
            }
        } else if (source.has("deployment-target")) {
            JSONObject deploymentTarget = null;
            JSONArray targetContainer = null;

            List<String> containerList = new ArrayList<>();
            if (source.has("deployment-target")) {
                deploymentTarget = source.getJSONObject("deployment-target");

                if (deploymentTarget.has("target")) {
                    targetContainer = convertJSONArray(deploymentTarget.get("target"));

                    for (int j = 0; j < targetContainer.length(); j++) {
                        JSONObject targetServer = targetContainer.getJSONObject(j);
                        String targetName = getValue(targetServer, "engine-container-name");
                        if (StringUtils.isNotEmpty(targetName)) {
                            containerList.add(targetName);
                        }
                    }
                } else if (deploymentTarget.has("all-targets")) {
                    containerList.add("All Containers");
                }
            }

            target.setTarget(containerList);
        } else if (source.has("target-server")) {
            JSONArray targetContainer = convertJSONArray(source.get("target-server"));
            // JSONArray targetContainer = convertJSONArray(source.getJSONObject("target-server"));
            List<String> containerList = new ArrayList<>();
            for (int j = 0; j < targetContainer.length(); j++) {
                JSONObject targetServer = targetContainer.getJSONObject(j);
                containerList.add(getValue(targetServer, "name"));
            }
            target.setTarget(containerList);
        } else if (source.has("target-cluster")) {  // jeus 8.0 (제주도청에서 발생.)
            // Cluster일 경우 ClusterName을 구한 뒤 Servers에서 해당 하는 Cluter를 찾은 뒤 해당 ServerName을 구한다.
            JSONArray targetContainer = convertJSONArray(source.get("target-cluster"));
            List<String> clusterList = new ArrayList<>();
            for (int j = 0; j < targetContainer.length(); j++) {
                JSONObject targetServer = targetContainer.getJSONObject(j);
                String targetName = getValue(targetServer, "name");
                if (StringUtils.isNotEmpty(targetName)) {
                    clusterList.add(targetName);
                }
            }
            target.setCluster(true);
            target.setClusterNames(clusterList);
        } else {
            log.warn("Application tag not found : targets or deployment-target or target-server.");
            target.setTarget(List.of(""));
        }

        if (source.has("options")) {
            JSONObject options = source.getJSONObject("options");
            Map<String, Object> map = new HashMap<>();
            for (String field : options.keySet()) {
                map.put(field, options.get(field));
            }
            target.setOptions(map);
        } else {
            log.warn("Application tag not found : options.");
            target.setOptions(new HashMap<>());
        }

        if (source.has("web-component")) {
            String contextRoot = getValue(source, "web-component");
            List<String> contestRoots = new ArrayList<>();
            if (contextRoot == null) {
                JSONArray webComponentList = convertJSONArray(source.get("web-component"));

                for (int i = 0; i < webComponentList.length(); i++) {
                    Object obj = webComponentList.get(i);
                    if (obj instanceof JSONObject) {
                        JSONObject webComponent = webComponentList.getJSONObject(i);
                        if (webComponent != null) {
                            if (webComponent.has("context-root")) {
                                contestRoots.add(webComponent.getString("context-root"));
                            }
                        }
                    }
                }
            } else {
                contestRoots.add(contextRoot);
            }
            target.setContextRoot(contestRoots);
        } else {
            log.warn("Application tag not found : web-component.");
            target.setContextRoot(List.of(""));
        }

        return target;
    }

    private List<JeusAssessmentResult.Instances> getServers(JSONArray source, TargetHost targetHost, Float version, String domainHome, GetInfoStrategy strategy) throws InterruptedException {

        List<JeusAssessmentResult.Instances> target = new ArrayList<>();
        for (int idx = 0; idx < source.length(); idx++) {
            JeusAssessmentResult.Instances server = getServer(source.getJSONObject(idx), targetHost, version, domainHome, strategy);
            if (version > 6) {
//                if (processRunning(targetHost, "java", "jeus.server", server.getName())) {
                if (processRunning(targetHost, server.getName(), strategy)) {
                    // 다른 프로세스가 해당 이름을 로그 패스로 지정하여 같이 사용하는 경우가 있음 (eg. -Xverbosegclog:/engn001/jeus/jeus7/logs/gclog/gclog_ContKDIPUSHV5RECV_01_%Y%m%d.txt_20210812105730)
                    // _{NAME} 으로 시작하는 항목은 제외한다.
//                    String command = "ps -eo \"%p %U %t %a\" | grep jeus.server | grep '" + server.getName() + "' | grep -v '_" + server.getName() + "' | grep -v grep | awk '{print $3}' | head -1";
////                    ps -eo "%p %U %t %a" | grep jeus.server | grep 'adminServer' | grep -v '_adminServer' | grep -v grep | awk '{print $3}' | head -1
//                    String executedTime = SSHUtil.executeCommand(targetHost, command);
//                    server.setStatus("Running(" + executedTime.replaceAll("\\n", "") + ")");

//                    String command = COMMAND.JEUS_EXECUTED_TIME.command(commandConfig, strategy.isWindows(), server.getName());
//                    String result = AbstractMiddlewareAssessment.getSshCommandResultTrim(targetHost, command, COMMAND.JEUS_EXECUTED_TIME, strategy);

                    String result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_EXECUTED_TIME, commandConfig, strategy, server.getName());
                    server.setStatus("Running(" + StringUtils.strip(result, StringUtils.SPACE + strategy.getCarriageReturn()) + ")");
                } else {
                    server.setStatus("N/A");
                }
            }
            target.add(server);
        }

        return target;
    }

    //    protected boolean processRunning(TargetHost targetHost, String processName, String... args) throws InterruptedException {
    protected boolean processRunning(TargetHost targetHost, String serverName, GetInfoStrategy strategy) throws InterruptedException {
//        StringBuilder cmd = new StringBuilder("ps").append(BLANK).append("-ef").append(BLANK)
//                .append("|").append(BLANK).append("grep").append(BLANK).append(processName);
//
//        for (String arg : args) {
//            cmd.append(BLANK).append("|").append(BLANK).append("grep").append(BLANK).append(arg);
//        }
//
//        cmd.append(BLANK).append("|").append(BLANK).append("grep -v grep");

//        String command = String.format("ps -ef | grep %s | grep %s | grep %s | grep -v grep", processName, option, serverName);
//        String result = SSHUtil.executeCommand(targetHost, command).trim();
//        String command = COMMAND.JEUS_PROCESS.command(commandConfig, strategy.isWindows(), serverName);
//        String result = AbstractMiddlewareAssessment.getSshCommandResultTrim(targetHost, command, COMMAND.JEUS_PROCESS, strategy);

        String result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_PROCESS, commandConfig, strategy, serverName);
        return StringUtils.isNotEmpty(result);

//        if (targetHost != null) {
////            String result = SSHUtil.executeCommand(targetHost, cmd.toString()).trim();
//            String result = SSHUtil.executeCommand(targetHost, command).trim();
//
//            if (result.contains(processName)) {
//                return true;
//            }
//        } else {
//            /*
//             Commons Exec does not support pipes or redirection directly.
//             You will need to tie the output from the first command to the input of the second yourself.
//             http://apache-commons.680414.n4.nabble.com/exec-command-with-single-quotes-and-pipe-td4657818.html
//             */
//            // ProcessHandle identifies and provides control of native processes over Java 9+
//            String result = null;
//
//            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//                DefaultExecutor executor = new DefaultExecutor();
//                PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
//                executor.setStreamHandler(streamHandler);
//
//                StringBuilder sb = new StringBuilder("ps -ef | grep ").append(processName);
//
//                for (String arg : args) {
//                    sb.append(" | grep " + arg);
//                }
//
//                sb.append(" | grep -v grep");
//
//                CommandLine cl = CollectionHelper.getCommandLine(CollectionHelper.findCommand("sh"),
//                        "-c",
//                        sb.toString());
//
//                /*
//                CommandLine cl = new CommandLine(CollectionHelper.findCommand("sh"))
//                        .addArguments(new String[]{"-c", sb.toString()}, false);
//                */
//
//                log.debug("processRunning()'s CommandLine : {}", cl.toString().replace(",", ""));
//
//                int exitCode = executor.execute(cl);
//
//                if (exitCode == 0) {
//                    result = baos.toString();
//                } else {
//                    throw new Exception(baos.toString());
//                }
//
//                log.debug("processRunning()'s result : {}", result);
//
//                if (result.contains(processName)) {
//                    return true;
//                }
//            } catch (Exception e) {
//                RoRoException.checkInterruptedException(e);
//                log.error("Unhandled exception occurred while check given process is running.", e);
//            }
//        }
    }

    private JeusAssessmentResult.Instances getServer(JSONObject source, TargetHost targetHost, Float version, String domainHome, GetInfoStrategy strategy) throws InterruptedException {
        JeusAssessmentResult.Instances target = new JeusAssessmentResult.Instances();
        JeusAssessmentResult.Engines engines = new JeusAssessmentResult.Engines();
        List<JeusAssessmentResult.EjbEngine> ejbEngines = new ArrayList<>();
        List<JeusAssessmentResult.JmsEngine> jmsEngines = new ArrayList<>();
        List<JeusAssessmentResult.WebEngine> webEngines = new ArrayList<>();
        for (String field : source.keySet()) {
            if ("name".equals(field)) {
                target.setName(getValue(source, field));
            } else if ("enable-webadmin".equals(field)) {
                target.setEnableWebAdmin(getValue(source, field));
            } else if ("node-name".equals(field)) {
                target.setNodeName(getValue(source, field));
            } else if ("listeners".equals(field)) {
                if (source.get("listeners") != null) {
                    JSONObject listenersSource = source.getJSONObject(field);
                    JeusAssessmentResult.Listeners listeners = getListeners(listenersSource);
                    target.setListeners(listeners);
                }
            } else if ("jvm-config".equals(field)) {
                JeusAssessmentResult.JvmConfig jvmConfig = getJvmConfig(source.getJSONObject(field));
                target.setJvmConfig(jvmConfig);
            } else if ("use-ejb-engine".equals(field)) {
                target.setUseEjbEngine(getValue(source, "use-ejb-engine"));
            } else if ("use-jms-engine".equals(field)) {
                target.setUseJmsEngine(getValue(source, "use-jms-engine"));
            } else if ("use-web-engine".equals(field)) {
                target.setUseWebEngine(getValue(source, "use-web-engine"));
            } else if ("system-logging".equals(field)) {
                JSONArray loggingSource = convertJSONArray(source.get(field));
                List<JeusAssessmentResult.SystemLogging> systemLoggingList = getSystemLoggingList(loggingSource);
                target.setLogs(systemLoggingList);
            } else if ("web-engine".equals(field)) {
                JeusAssessmentResult.WebEngine webEngine = getWebEngine(source.getJSONObject(field));
                webEngines.add(webEngine);
            } else if ("jms-engine".equals(field)) {
                JeusAssessmentResult.JmsEngine jmsEngine = getJmsEngine(source.getJSONObject(field));
                jmsEngines.add(jmsEngine);
            } else if ("ejb-engine".equals(field)) {
                JeusAssessmentResult.EjbEngine ejbEngine = getEjbEngine(source.getJSONObject(field));
                ejbEngines.add(ejbEngine);
            } else if ("webadmin-config".equals(field)) {
                JeusAssessmentResult.WebAdminConfig webAdminConfig = getWebAdminConfig(source.getJSONObject(field));
                target.setWebAdminConfig(webAdminConfig);
            } else if ("engine-container".equals(field)) {
                /*
                 * Jeus6 의 경우 JEUSMain.xml 에 컨테이너 정보가 있다.
                 * 1. 해당 컨테이너의 engine-command에 존재하는 명령어를 현재 구동중인 컨테이너로 가정한다.
                 *
                 * 2. 엔진 네이밍은 {nodeName}_{type}_{name} 이므로 engine-command에서 타입과 이름을 추출하여
                 * 분석할 엔진들의 경로를 조합한다.
                 *
                 * 컨테이너의 수만큼 반복한다.
                 *
                 **/
                List<JeusAssessmentResult.EngineContainer> engineContainer = getEngineContainers(convertJSONArray(source.get(field)), targetHost, strategy);
                engines.setEngineContainer(engineContainer);

                String nodeName = source.getString("name");
                String separator = strategy.getSeparator();
                for (JeusAssessmentResult.EngineContainer container : engineContainer) {
                    List<String> enginePaths = new ArrayList<>();

                    for (JeusAssessmentResult.EngineCommand command : container.getEngineCommands()) {
                        enginePaths.add(domainHome + separator + nodeName + "_" + command.getType() + "_" + command.getName());
                    }

                    for (String path : enginePaths) {
                        if (path.contains("ejb")) {
                            if (AbstractMiddlewareAssessment.fileExists(targetHost, path + separator + "EJBMain.xml", commandConfig, strategy)) {
                                String ejbMain = AbstractMiddlewareAssessment.getFileContents(targetHost, path + separator + "EJBMain.xml", commandConfig, strategy);
                                JSONObject ejbMainFile = XML.toJSONObject(ejbMain);
                                if (ejbMainFile.has("ejb-engine")) {
                                    JeusAssessmentResult.EjbEngine ejbEngine = getEjbEngine(ejbMainFile.getJSONObject("ejb-engine"));
                                    ejbEngines.add(ejbEngine);
                                }
                            }
                        } else if (path.contains("servlet")) {
                            if (AbstractMiddlewareAssessment.fileExists(targetHost, path + separator + "WEBMain.xml", commandConfig, strategy)) {
                                String webMain = AbstractMiddlewareAssessment.getFileContents(targetHost, path + separator + "WEBMain.xml", commandConfig, strategy);
                                JSONObject webMainFile = XML.toJSONObject(webMain);
                                if (webMainFile.has("web-container")) {
                                    JeusAssessmentResult.WebEngine webEngine = getWebEngine(webMainFile.getJSONObject("web-container"));
                                    webEngines.add(webEngine);
                                }
                            }
                        } else if (path.contains("jms")) {
                            if (AbstractMiddlewareAssessment.fileExists(targetHost, path + separator + "JMSMain.xml", commandConfig, strategy)) {
                                String jmsMain = AbstractMiddlewareAssessment.getFileContents(targetHost, path + separator + "JMSMain.xml", commandConfig, strategy);
                                JSONObject jmsMainFile = XML.toJSONObject(jmsMain);
                                if (jmsMainFile.has("jms-server")) {
                                    JeusAssessmentResult.JmsEngine jmsEngine = getJmsEngine(jmsMainFile.getJSONObject("jms-server"));
                                    jmsEngines.add(jmsEngine);
                                }
                            }
                        } else if (path.contains("ws")) {

                        }
                    }
                }
            }
        }

        String runUser = StringUtils.EMPTY;
        if (version > 6) {
            // version이 7 이상인 경우 로직 수행
            // get minHeap maxHeap
            generatedHeapSize(targetHost, target, strategy);

            // get runUser
            generatedVmOption(targetHost, target, strategy);
            runUser = getRunUser(targetHost, target.getName(), strategy);
        } else {
            List<JeusAssessmentResult.EngineContainer> engineContainer = engines.getEngineContainer();
            if (!engineContainer.isEmpty()) {
                String name = engineContainer.get(0).getName();
                runUser = getRunUser(targetHost, name, strategy);
            }
        }
        target.setRunUser(runUser);


        engines.setEjbEngine(ejbEngines);
        engines.setWebEngine(webEngines);
        engines.setJmsEngine(jmsEngines);

        target.setEngines(engines);

        return target;
    }

    private String getRunUser(TargetHost targetHost, String name, GetInfoStrategy strategy) throws InterruptedException {
        String runUser = WasAnalyzerUtil.getRunUser(targetHost, name, commandConfig, strategy);
        if (StringUtils.isEmpty(runUser) && strategy.isWindows()) {
            List<TaskListResult> processList = jeusExtract.getJeusProcessList(targetHost);
            for (TaskListResult result : processList) {
                String registryParameters = jeusExtract.getRegistryParameterByServiceName(targetHost, result.getServiceName());
                if (registryParameters.contains(name)) {
                    runUser = MWCommonUtil.getExecuteResult(targetHost, COMMAND.GET_USER_BY_PID, commandConfig, strategy, result.getPid());
                    runUser = MWCommonUtil.extractUser(runUser);
                }
            }
        }
        return runUser;
    }

    private void generatedVmOption(TargetHost targetHost, JeusAssessmentResult.Instances server, GetInfoStrategy strategy) throws InterruptedException {
        log.debug(":+:+:+:+:+:+:+:+: generatedVmOptionAndRunUser() :+:+:+:+:+:+:+:+:");
        server.setVmOption(WasAnalyzerUtil.getJvmOptions(targetHost, server.getName(), commandConfig, strategy, "jeus.server.NodemanagerBootstrapper", "jeus.nodemanager.JeusNodeManager"));
    }

    private void generatedHeapSize(TargetHost targetHost, JeusAssessmentResult.Instances server, GetInfoStrategy strategy) throws InterruptedException {
        log.debug(":+:+:+:+:+:+:+:+: generatedHeapSize() :+:+:+:+:+:+:+:+:");
        server.setMinHeap(WasAnalyzerUtil.getHeapSize(targetHost, server.getName(), "-Xms", commandConfig, strategy, "jeus.server.NodemanagerBootstrapper", "jeus.nodemanager.JeusNodeManager"));
        server.setMaxHeap(WasAnalyzerUtil.getHeapSize(targetHost, server.getName(), "-Xmx", commandConfig, strategy, "jeus.server.NodemanagerBootstrapper", "jeus.nodemanager.JeusNodeManager"));
    }

    private List<JeusAssessmentResult.EngineContainer> getEngineContainers(JSONArray source, TargetHost targetHost, GetInfoStrategy strategy) throws InterruptedException {

        List<JeusAssessmentResult.EngineContainer> targetList = new ArrayList<>();
        for (int i = 0; i < source.length(); i++) {
            JeusAssessmentResult.EngineContainer target = getEngineContainer(source.getJSONObject(i), targetHost, strategy);
            targetList.add(target);
        }

        return targetList;
    }

    private JeusAssessmentResult.EngineContainer getEngineContainer(JSONObject source, TargetHost targetHost, GetInfoStrategy strategy) throws InterruptedException {
        JeusAssessmentResult.EngineContainer target = new JeusAssessmentResult.EngineContainer();
        target.setName(getValue(source, "name"));
//        target.setCommandOption(getValue(source, "command-option"));
        target.setSequentialStart(getValue(source, "sequential-start"));
        target.setUserClassPath(getValue(source, "user-class-path"));

        if (source.has("system-logging")) {
            List<JeusAssessmentResult.SystemLogging> systemLoggingList = getSystemLoggingList(convertJSONArray(source.get("system-logging")));
            target.setSystemLogging(systemLoggingList);
        }

        if (source.has("engine-command")) {
            List<JeusAssessmentResult.EngineCommand> engineCommands = getEngineCommands(convertJSONArray(source.get("engine-command")));
            target.setEngineCommands(engineCommands);
        }

        // get minHeap, maxHeap
        target.setMinHeap(StringUtils.defaultString(WasAnalyzerUtil.getHeapSize(targetHost, target.getName(), "-Xms", commandConfig, strategy)));
        target.setMaxHeap(StringUtils.defaultString(WasAnalyzerUtil.getHeapSize(targetHost, target.getName(), "-Xmx", commandConfig, strategy)));

        // get vmOption and runUser
        target.setVmOption(StringUtils.defaultString(WasAnalyzerUtil.getJvmOptions(targetHost, target.getName(), commandConfig, strategy)));
        target.setRunUser(StringUtils.defaultString(WasAnalyzerUtil.getRunUser(targetHost, target.getName(), commandConfig, strategy)));

        return target;
    }

    private List<JeusAssessmentResult.EngineCommand> getEngineCommands(JSONArray source) {
        List<JeusAssessmentResult.EngineCommand> targetList = new ArrayList<>();

        for (int i = 0; i < source.length(); i++) {
            JeusAssessmentResult.EngineCommand target = getEngineCommand(source.getJSONObject(i));
            targetList.add(target);
        }

        return targetList;
    }

    private JeusAssessmentResult.EngineCommand getEngineCommand(JSONObject source) {
        JeusAssessmentResult.EngineCommand target = new JeusAssessmentResult.EngineCommand();
        target.setName(getValue(source, "name"));
        target.setType(getValue(source, "type"));
        return target;
    }

    private JeusAssessmentResult.WebAdminConfig getWebAdminConfig(JSONObject source) {
        JeusAssessmentResult.WebAdminConfig target = new JeusAssessmentResult.WebAdminConfig();

        if (source.has("allowed-server")) {
            JeusAssessmentResult.AllowedServer allowedServer = getAllowedServer(convertJSONArray(source.get("allowed-server")));
            target.setAllowedServer(allowedServer);
        }

        return target;
    }

    private JeusAssessmentResult.AllowedServer getAllowedServer(JSONArray source) {

        JeusAssessmentResult.AllowedServer target = new JeusAssessmentResult.AllowedServer();
        List<String> addresss = new ArrayList<>();
        for (int i = 0; i < source.length(); i++) {
            addresss.add(source.getString(i));
        }

        target.setAddress(addresss);

        return target;
    }

    private JeusAssessmentResult.EjbEngine getEjbEngine(JSONObject source) {
        JeusAssessmentResult.EjbEngine target = new JeusAssessmentResult.EjbEngine();

        target.setEnableUserNotify(getValue(source, "enable-user-notify"));
        target.setResolution(getValue(source, "resolution"));
        target.setUseDynamicProxyForEjb2(getValue(source, "use-dynamic-proxy-for-ejb2"));

        if (source.has("active-management")) {
            JeusAssessmentResult.ActiveManagement activeManagement = getActiveManagement(source.getJSONObject("active-management"));
            target.setActiveManagement(activeManagement);
        }

        if (source.has("async-service")) {
            JeusAssessmentResult.AsyncService asyncService = getAsyncService(source.getJSONObject("async-service"));
            target.setAsyncService(asyncService);
        }

        if (source.has("timer-service")) {
            JeusAssessmentResult.TimerService timerService = getTimerService(source.getJSONObject("timer-service"));
            target.setTimerService(timerService);
        }


        return target;
    }

    private JeusAssessmentResult.ActiveManagement getActiveManagement(JSONObject source) {
        JeusAssessmentResult.ActiveManagement target = new JeusAssessmentResult.ActiveManagement();
        target.setMaxBlockedThread(getValue(source, "max-blocked-thread"));
        target.setMaxIdleTime(getValue(source, "max-idle-time"));

        return target;
    }

    private JeusAssessmentResult.TimerService getTimerService(JSONObject source) {
        JeusAssessmentResult.TimerService target = new JeusAssessmentResult.TimerService();

        target.setMaxRetrialCount(getValue(source, "max-retrial-count"));
        target.setRetrialInterval(getValue(source, "retrial-interval"));
        target.setSupportPersistence(getValue(source, "support-persistence"));

        if (source.has("thread-pool")) {
            JeusAssessmentResult.ThreadPool threadPool = getThreadPool(source.getJSONObject("thread-pool"));
            target.setThreadPool(threadPool);
        }

        return target;
    }

    private JeusAssessmentResult.AsyncService getAsyncService(JSONObject source) {
        JeusAssessmentResult.AsyncService target = new JeusAssessmentResult.AsyncService();
        target.setAccessTimeout(getValue(source, "access-timeout"));
        target.setThreadMax(getValue(source, "thread-max"));
        target.setThreadMin(getValue(source, "thread-min"));

        return target;
    }

    private JeusAssessmentResult.JmsEngine getJmsEngine(JSONObject source) {
        JeusAssessmentResult.JmsEngine target = new JeusAssessmentResult.JmsEngine();

        if (source.has("thread-pool")) {
            JeusAssessmentResult.ThreadPool threadPool = getThreadPool(source.getJSONObject("thread-pool"));
            target.setThreadPool(threadPool);
        }

        if (source.has("service-config")) {
            JeusAssessmentResult.ServiceConfig serviceConfig = getServiceConfig(source.getJSONObject("service-config"));
            target.setServiceConfig(serviceConfig);
        }

        if (source.has("connection-factory")) {
            List<JeusAssessmentResult.ConnectionFactory> connectionFactories = getConnectionFactories(convertJSONArray(source.get("connection-factory")));
            target.setConnectionFactory(connectionFactories);
        }

        return target;
    }

    private List<JeusAssessmentResult.ConnectionFactory> getConnectionFactories(JSONArray sourceList) {
        List<JeusAssessmentResult.ConnectionFactory> targetList = new ArrayList<>();

        for (int i = 0; i < sourceList.length(); i++) {
            JeusAssessmentResult.ConnectionFactory target = getConnectionFactory(sourceList.getJSONObject(i));
            targetList.add(target);
        }
        return targetList;
    }

    private JeusAssessmentResult.ConnectionFactory getConnectionFactory(JSONObject source) {
        JeusAssessmentResult.ConnectionFactory target = new JeusAssessmentResult.ConnectionFactory();
        target.setType(getValue(source, "type"));
        target.setRequestBlockingTime(getValue(source, "request-blocking-time"));
        target.setReconnectPeriod(getValue(source, "reconnect-period"));
        target.setReconnectInterval(getValue(source, "reconnect-interval"));
        target.setReconnectEnabled(getValue(source, "reconnect-enabled"));
        target.setName(getValue(source, "name"));
        target.setFixedClientId(getValue(source, "fixed-client-id"));
        target.setExportName(getValue(source, "export-name"));
        target.setBrokerSelectionPolicy(getValue(source, "broker-selection-policy"));

        return target;
    }

    private JeusAssessmentResult.ServiceConfig getServiceConfig(JSONObject source) {
        JeusAssessmentResult.ServiceConfig target = new JeusAssessmentResult.ServiceConfig();
        target.setCheckSecurity(getValue(source, "check-security"));
        target.setClientKeepaliveTimeout(getValue(source, "client-keepalive-timeout"));
        target.setClientLimit(getValue(source, "client-limit"));
        target.setListenerName(getValue(source, "listener-name"));
        target.setName(getValue(source, "name"));
        return target;
    }


    private JeusAssessmentResult.WebEngine getWebEngine(JSONObject source) {
        JeusAssessmentResult.WebEngine target = new JeusAssessmentResult.WebEngine();

        Map<String, Object> undefined = new HashMap<>();
        for (String field : source.keySet()) {
            if ("access-log".equals(field)) {
                JeusAssessmentResult.AccessLog accessLog = getAccessLog(source.getJSONObject(field));
                target.setAccessLog(accessLog);
            } else if ("web-connections".equals(field)) {
                JeusAssessmentResult.WebConnections webConnections = getWebConnections(source.getJSONObject(field));
                target.setWebConnections(webConnections);
            } else if ("session-config".equals(field)) {
                JeusAssessmentResult.SessionConfig sessionConfig = getSessionConfig(source.getJSONObject(field));
                target.setSessionConfig(sessionConfig);
            } else if ("monitoring".equals(field)) {
                JeusAssessmentResult.Monitoring monitoring = getMonitoring(source.getJSONObject(field));
                target.setMonitoring(monitoring);
            } else if ("jsp-engine".equals(field)) {
                JeusAssessmentResult.JspEngine jspEngine = getJspEngine(source.getJSONObject(field));
                target.setJspEngine(jspEngine);
            } else if ("blocked-url-patterns".equals(field)) {
                JeusAssessmentResult.BlockedUrlPatterns blockedUrlPatterns = getBlockUrlPattern(source.getJSONObject(field));
                target.setBlockedUrlPatterns(blockedUrlPatterns);
            } else if ("attach-stacktrace-on-error".equals(field)) {
                target.setAttachStacktraceOnError(getValue(source, field));
            } else if ("async-timeout-min-threads".equals(field)) {
                target.setAsyncTimeoutMinThreads(getValue(source, field));
            } else if ("context-group".equals(field)) {
                JSONObject contextGroup = source.getJSONObject(field);

                if (contextGroup.has("logging")) {
                    JSONObject obj = contextGroup.getJSONObject("logging");
                    if (obj.has("access-log")) {
                        JeusAssessmentResult.AccessLog accessLog = getAccessLog(obj.getJSONObject("access-log"));
                        target.setAccessLog(accessLog);
                    }
                }

                if (contextGroup.has("webserver-connection")) {
                    JeusAssessmentResult.WebConnections webConnections = getWebConnections(contextGroup.getJSONObject("webserver-connection"));
                    target.setWebConnections(webConnections);
                }

            } else {
                undefined.put(field, source.get(field));
            }
        }
        target.setUndefined(undefined);
        return target;
    }

    private JeusAssessmentResult.BlockedUrlPatterns getBlockUrlPattern(JSONObject source) {
        JeusAssessmentResult.BlockedUrlPatterns target = new JeusAssessmentResult.BlockedUrlPatterns();
        target.setDenyLastSpaceCharacter(getValue(source, "deny-last-space-character"));
        target.setDenyNullCharacter(getValue(source, "deny-null-character"));

        if (source.has("encoded-pattern")) {
            JSONArray patterns = convertJSONArray(source.get("encoded-pattern"));
            List<String> patternList = new ArrayList<>();
            for (int i = 0; i < patterns.length(); i++) {
                patternList.add(patterns.getString(i));
            }

            target.setEncodedPattern(patternList);
        }

        if (source.has("decoded-pattern")) {
            JSONArray patterns = convertJSONArray(source.get("decoded-pattern"));
            List<String> patternList = new ArrayList<>();
            for (int i = 0; i < patterns.length(); i++) {
                patternList.add(patterns.getString(i));
            }

            target.setDecodedPattern(patternList);
        }

        return target;
    }

    private JeusAssessmentResult.JspEngine getJspEngine(JSONObject source) {
        JeusAssessmentResult.JspEngine target = new JeusAssessmentResult.JspEngine();
        target.setCheckIncludedJspfile(getValue(source, "check-included-jspfile"));
        target.setGracefulJspReloading(getValue(source, "graceful-jsp-reloading"));
        target.setGracefulJspReloadingPeriod(getValue(source, "graceful-jsp-reloading-period"));
        target.setJavaCompiler(getValue(source, "java-compiler"));
        target.setKeepGenerated(getValue(source, "keep-generated"));
        target.setUseInMemoryCompilation(getValue(source, "use-in-memory-compilation"));
        return target;
    }


    private JeusAssessmentResult.Monitoring getMonitoring(JSONObject source) {
        JeusAssessmentResult.Monitoring target = new JeusAssessmentResult.Monitoring();
        target.setCheckClassReload(getValue(source, "check-class-reload"));
        target.setCheckSession(getValue(source, "check-session"));
        target.setCheckThreadPool(getValue(source, "check-thread-pool"));
        return target;
    }

    private JeusAssessmentResult.SessionConfig getSessionConfig(JSONObject source) {
        JeusAssessmentResult.SessionConfig target = new JeusAssessmentResult.SessionConfig();

        target.setMaxSessionCount(getValue(source, "max-session-count"));
        target.setReloadPersistent(getValue(source, "reload-persistent"));
        target.setShared(getValue(source, "shared"));
        target.setTimeout(getValue(source, "timeout"));

        if (source.has("session-cookie")) {
            JeusAssessmentResult.SessionCookie sessionCookie = getSessionCookie(source.getJSONObject("session-cookie"));
            target.setSessionCookie(sessionCookie);
        }

        if (source.has("tracking-mode")) {
            JeusAssessmentResult.TrackingMode trackingMode = getTrackingMode(source.getJSONObject("tracking-mode"));
            target.setTrackingMode(trackingMode);
        }

        return target;
    }

    private JeusAssessmentResult.TrackingMode getTrackingMode(JSONObject source) {
        JeusAssessmentResult.TrackingMode target = new JeusAssessmentResult.TrackingMode();
        target.setCookie(getValue(source, "cookie"));
        target.setSsl(getValue(source, "ssl"));
        target.setUrl(getValue(source, "url"));
        return target;
    }

    private JeusAssessmentResult.SessionCookie getSessionCookie(JSONObject source) {
        JeusAssessmentResult.SessionCookie target = new JeusAssessmentResult.SessionCookie();
        target.setCookieName(getValue(source, "cookie-name"));
        target.setHttpOnly(getValue(source, "http-only"));
        target.setMaxAge(getValue(source, "max-age"));
        target.setSameSite(getValue(source, "same-site"));
        target.setSecure(getValue(source, "secure"));
        target.setVersion(getValue(source, "version"));
        return target;
    }

    private JeusAssessmentResult.WebConnections getWebConnections(JSONObject source) {
        JeusAssessmentResult.WebConnections target = new JeusAssessmentResult.WebConnections();

        if (source.has("http-listener")) {
            List<JeusAssessmentResult.HttpListener> httpListener = getHttpListener(convertJSONArray(source.get("http-listener")));
            target.setHttpListener(httpListener);
        }

        /*
         *  Jeus6 servlet engine WEBMain.xml webToB connection info
         * */
        if (source.has("webtob-listener")) {
            List<JeusAssessmentResult.HttpListener> webtobListener = getHttpListener(convertJSONArray(source.get("webtob-listener")));
            target.setWebtobListener(webtobListener);
        }

        /*
         *  Jeus7+ web engine webToB connection info
         * */
        if (source.has("webtob-connector")) {
            List<JeusAssessmentResult.WebToBConnector> webtobConnector = getwebTobConnector(convertJSONArray(source.get("webtob-connector")));
            target.setWebToBConnector(webtobConnector);
        }

        return target;
    }

    private List<JeusAssessmentResult.WebToBConnector> getwebTobConnector(JSONArray sourceArray) {
        List<JeusAssessmentResult.WebToBConnector> targetList = new ArrayList<>();
        for (int i = 0; i < sourceArray.length(); i++) {
            JeusAssessmentResult.WebToBConnector target = new JeusAssessmentResult.WebToBConnector();
            JSONObject source = sourceArray.getJSONObject(i);

            target.setName(getValue(source, "name"));
            target.setMaxHeaderCount(getValue(source, "max-header-count"));
            target.setMaxParameterCount(getValue(source, "max-parameter-count"));
            target.setMaxHeaderSize(getValue(source, "max-header-size"));
            target.setMaxPostSize(getValue(source, "max-post-size"));
            target.setMaxQuerystringSize(getValue(source, "max-querystring-size"));
            target.setPostdataReadTimeout(getValue(source, "postdata-read-timeout"));
            target.setWjpVersion(getValue(source, "wjp-version"));
            target.setRegistrationId(getValue(source, "registration-id"));

            if (source.has("network-address")) {
                JeusAssessmentResult.NetworkAddress networkAddress = getNetworkAddress(source.getJSONObject("network-address"));
                target.setNetworkAddress(networkAddress);
            }

            if (source.has("thread-pool")) {
                JeusAssessmentResult.WebToBConnector.WebToBThreadPool threadPool = getWebToBThreadPool(source.getJSONObject("thread-pool"));
                target.setThreadPool(threadPool);
            }

            target.setHthCount(getValue(source, "hth-count"));
            target.setRequestPrefetch(getValue(source, "request-prefetch"));
            target.setReadTimeout(getValue(source, "read-timeout"));
            target.setReconnectInterval(getValue(source, "reconnect-interval"));
            target.setReconnectCountForBackup(getValue(source, "reconnect-count-for-backup"));

            targetList.add(target);
        }
        return targetList;
    }

    private JeusAssessmentResult.WebToBConnector.WebToBThreadPool getWebToBThreadPool(JSONObject source) {
        JeusAssessmentResult.WebToBConnector.WebToBThreadPool target = new JeusAssessmentResult.WebToBConnector.WebToBThreadPool();

        target.setNumber(getValue(source, "number"));

        if (source.has("thread-state-notify")) {
            JeusAssessmentResult.WebToBConnector.ThreadStateNotify threadStateNotify = getThreadStateNotify(source.getJSONObject("thread-state-notify"));
            target.setThreadStateNotify(threadStateNotify);
        }

        return target;
    }

    private JeusAssessmentResult.WebToBConnector.ThreadStateNotify getThreadStateNotify(JSONObject source) {
        JeusAssessmentResult.WebToBConnector.ThreadStateNotify target = new JeusAssessmentResult.WebToBConnector.ThreadStateNotify();

        target.setMaxThreadActiveTime(getValue(source, "max-thread-active-time"));
        target.setInterruptThread(getValue(source, "interrupt-thread"));
        target.setActiveTimeoutNotification(getValue(source, "active-timeout-notification"));
        target.setNotifyThresholdRatio(getValue(source, "notify-threshold-ratio"));
        target.setRestartThresholdRatio(getValue(source, "restart-threshold-ratio"));

        return target;
    }

    private JeusAssessmentResult.NetworkAddress getNetworkAddress(JSONObject source) {
        JeusAssessmentResult.NetworkAddress target = new JeusAssessmentResult.NetworkAddress();
        target.setPort(getValue(source, "port"));
        target.setIpAddress(getValue(source, "ip-address"));
        return target;
    }

    private List<JeusAssessmentResult.HttpListener> getHttpListener(JSONArray sourceArray) {
        List<JeusAssessmentResult.HttpListener> targetList = new ArrayList<>();
        for (int i = 0; i < sourceArray.length(); i++) {
            JeusAssessmentResult.HttpListener target = new JeusAssessmentResult.HttpListener();
            JSONObject source = sourceArray.getJSONObject(i);

            target.setMaxHeaderCount(getValue(source, "max-header-count"));
            target.setMaxParameterCount(getValue(source, "max-parameter-count"));
            target.setMaxHeaderSize(getValue(source, "max-header-size"));
            target.setMaxPostSize(getValue(source, "max-post-size"));
            target.setMaxQuerystringSize(getValue(source, "max-querystring-size"));
            target.setName(getValue(source, "name"));
            target.setPostdataReadTimeout(getValue(source, "postdata-read-timeout"));
            target.setServerAccessControl(getValue(source, "server-access-control"));
            target.setServerListenerRef(getValue(source, "server-listener-ref"));
            target.setListenerId(getValue(source, "listener-id"));
            target.setPort(getValue(source, "port"));
            target.setWebtobAddress(getValue(source, "webtob-address"));
            target.setOutputBufferSize(getValue(source, "output-buffer-size"));

            if (source.has("thread-pool")) {
                JeusAssessmentResult.ThreadPool threadPool = getThreadPool(source.getJSONObject("thread-pool"));
                target.setThreadPool(threadPool);
            }

            targetList.add(target);
        }
        return targetList;
    }

    private JeusAssessmentResult.ThreadPool getThreadPool(JSONObject source) {
        JeusAssessmentResult.ThreadPool target = new JeusAssessmentResult.ThreadPool();

        target.setMax(getValue(source, "max"));
        target.setMin(getValue(source, "min"));
        target.setMaxQueue(getValue(source, "max-queue"));
        target.setMaxIdleTime(getValue(source, "max-idle-time"));
        target.setKeepAliveTime(getValue(source, "keep-alive-time"));
        target.setPeriod(getValue(source, "period"));
        target.setMaxWaitQueue(getValue(source, "max-wait-queue"));

        return target;
    }

    private JeusAssessmentResult.AccessLog getAccessLog(JSONObject source) {
        JeusAssessmentResult.AccessLog target = new JeusAssessmentResult.AccessLog();
        for (String field : source.keySet()) {
            if ("enable".equals(field)) {
                target.setEnable(getValue(source, field));
            } else if ("format".equals(field)) {
                target.setFormat(getValue(source, field));
            } else if ("level".equals(field)) {
                target.setLevel(getValue(source, field));
            } else if ("use-parent-handlers".equals(field)) {
                target.setUseParentHandlers(getValue(source, field));
            } else if ("formatter-class".equals(field)) {
                target.setFormatterClass(getValue(source, field));
            } else if ("enable-host-name-lookup".equals(field)) {
                target.setEnableHostNameLookup(getValue(source, field));
            } else if ("handler".equals(field)) {
                JeusAssessmentResult.Handler handler = getHandler(source.getJSONObject(field));
                target.setHandler(handler);
            }
        }
        return target;
    }

    private List<JeusAssessmentResult.SystemLogging> getSystemLoggingList(JSONArray sourceArray) {
        List<JeusAssessmentResult.SystemLogging> targetList = new ArrayList<>();

        for (int i = 0; i < sourceArray.length(); i++) {
            JeusAssessmentResult.SystemLogging target = new JeusAssessmentResult.SystemLogging();
            JSONObject source = sourceArray.getJSONObject(i);

            target.setName(getValue(source, "name"));
            target.setLevel(getValue(source, "level"));
            target.setFormatterClass(getValue(source, "formatter-class"));
            target.setUseParentHandlers(getValue(source, "formatter-class"));

            if (source.has("handler")) {
                JeusAssessmentResult.Handler handler = getHandler(source.getJSONObject("handler"));
                target.setHandler(handler);
            }

            targetList.add(target);
        }

        return targetList;
    }

    private JeusAssessmentResult.Handler getHandler(JSONObject source) {
        JeusAssessmentResult.Handler target = new JeusAssessmentResult.Handler();
        if (source.has("file-handler")) {
            JSONObject file = source.getJSONObject("file-handler");
            JeusAssessmentResult.FileHandler fileHandler = new JeusAssessmentResult.FileHandler();
            fileHandler.setAppend(getValue(file, "append"));
            fileHandler.setBufferSize(getValue(file, "buffer-size"));
            fileHandler.setEnableRotation(getValue(file, "enable-rotation"));
            fileHandler.setFileName(getValue(file, "file-name"));
            fileHandler.setLevel(getValue(file, "level"));
            fileHandler.setName(getValue(file, "name"));
            fileHandler.setRotationDir(getValue(file, "rotation-dir"));
            fileHandler.setValidDay(getValue(file, "valid-day"));

            target.setFileHandler(fileHandler);
        }

        return target;
    }

    private JeusAssessmentResult.JvmConfig getJvmConfig(JSONObject source) {

        JeusAssessmentResult.JvmConfig target = new JeusAssessmentResult.JvmConfig();
        JSONArray jvmOptions = convertJSONArray(source.get("jvm-option"));
        List<String> jvmOptionStrList = new ArrayList<>();
        for (int i = 0; i < jvmOptions.length(); i++) {
            jvmOptionStrList.add(jvmOptions.get(i).toString());
        }

        target.setJvmOption(jvmOptionStrList);

        return target;
    }

    private JeusAssessmentResult.Listeners getListeners(JSONObject source) {

        JeusAssessmentResult.Listeners listeners = new JeusAssessmentResult.Listeners();

        JSONArray listenerArray = convertJSONArray(source.get("listener"));

        List<JeusAssessmentResult.Listener> list = new ArrayList<>();
        for (int idx = 0; idx < listenerArray.length(); idx++) {
            JSONObject obj = listenerArray.getJSONObject(idx);

            JeusAssessmentResult.Listener listener = new JeusAssessmentResult.Listener();
            listener.setName(getValue(obj, "name"));
            listener.setBacklog(getValue(obj, "backlog"));
            listener.setListenPort(getValue(obj, "listen-port"));
            listener.setReadTimeout(getValue(obj, "read-timeout"));
            listener.setReservedThreadNum(getValue(obj, "reserved-thread-num"));
            listener.setSelectors(getValue(obj, "selectors"));
            listener.setSelectTimeout(getValue(obj, "read-timeout"));
            listener.setUseDualSelector(getValue(obj, "use-dual-selector"));
            listener.setUseNio(getValue(obj, "select-timeout"));

            list.add(listener);
        }

        listeners.setListeners(list);

        return listeners;
    }

    private JSONArray convertJSONArray(Object object) {
        JSONArray jsonArray = new JSONArray();
        if (object instanceof JSONArray) {
            jsonArray = (JSONArray) object;
        } else if (object instanceof JSONObject) {
            jsonArray.put(object);
        } else if (object instanceof String) {
            jsonArray.put(object);
        }
        return jsonArray;
    }

    private void analyzeEngine(JSONObject source, MiddlewareInventory middleware, String engineHome, Float version, JeusAssessmentResult assessmentResult) {
        JeusAssessmentResult.Engine engine = new JeusAssessmentResult.Engine();
        engine.setName("Jeus");
        engine.setPath(engineHome);
        engine.setVendor("Tmax");
        engine.setVersion(String.valueOf(version));
        engine.setProductionMode(getValue(source, "production-mode"));
        engine.setDescription(getValue(source, "description"));

        log.debug("Jeus version: {}", engine.getVersion());
        log.debug("Jeus production mode: {}", engine.getProductionMode());
        log.debug("Engine description: {}", engine.getDescription());

        assessmentResult.setEngine(engine);
    }

    private String getValue(JSONObject obj, String key) {
        if (obj.has(key)) {
            if (obj.get(key) instanceof Integer) {
                return String.valueOf((int) obj.get(key));
            } else if (obj.get(key) instanceof String) {
                return (String) obj.get(key);
            } else if (obj.get(key) instanceof Long) {
                return String.valueOf((Long) obj.get(key));
            } else if (obj.get(key) instanceof Double) {
                return String.valueOf((Double) obj.get(key));
            } else if (obj.get(key) instanceof Boolean) {
                return String.valueOf((boolean) obj.get(key));
            } else if (obj.get(key) instanceof JSONArray) {
                return null;
            }
        }
        return null;
    }

    private String getMainXmlFile(TargetHost targetHost, MiddlewareInventory middleware, String domainHome, Float version, GetInfoStrategy strategy) throws InterruptedException {
        String mainXmlFile = null;

        if (AbstractMiddlewareAssessment.hasUploadedConfigFile(middleware)) {
            log.debug("Read Jeus config file : [{}]", middleware.getConfigFilePath());
            File f = new File(middleware.getConfigFilePath());
            if (f.isDirectory()) {
                if (version == null) {
                    List<String> configs = Arrays.asList("domain.xml", "JEUSMain.xml");
                    for (String config : configs) {
                        log.debug("Find {} file in [{}]", config, middleware.getConfigFilePath());
                        String targetFilePath = AbstractMiddlewareAssessment.findFileAbsolutePath(f.getAbsolutePath(), config);
                        if (StringUtils.isNotEmpty(targetFilePath)) {
                            File confFile = new File(targetFilePath);
                            if (confFile.exists()) {
                                mainXmlFile = AbstractMiddlewareAssessment.readUploadedFile(confFile.getAbsolutePath());

                                if (config.equals("domain.xml")) {
                                    version = 8.0f;
                                } else {
                                    version = 6.0f;
                                }
                            }
                        }
                    }
                } else if (version > 6.0) {
                    String targetFilePath = AbstractMiddlewareAssessment.findFileAbsolutePath(f.getAbsolutePath(), "domain.xml");
                    if (StringUtils.isNotEmpty(targetFilePath)) {
                        File confFile = new File(targetFilePath);
                        if (confFile.exists()) {
                            mainXmlFile = AbstractMiddlewareAssessment.readUploadedFile(confFile.getAbsolutePath());
                        }
                    }
                } else {
                    String targetFilePath = AbstractMiddlewareAssessment.findFileAbsolutePath(f.getAbsolutePath(), "JEUSMain.xml");
                    if (StringUtils.isNotEmpty(targetFilePath)) {
                        File confFile = new File(targetFilePath);
                        if (confFile.exists()) {
                            mainXmlFile = AbstractMiddlewareAssessment.readUploadedFile(confFile.getAbsolutePath());
                        }
                    }
                }
            } else {
                if (middleware.getConfigFilePath().contains("domain.xml")) {
                    mainXmlFile = AbstractMiddlewareAssessment.readUploadedFile(middleware.getConfigFilePath());
                    version = 8.0f;
                } else if (middleware.getConfigFilePath().contains("JEUSMain.xml")) {
                    mainXmlFile = AbstractMiddlewareAssessment.readUploadedFile(middleware.getConfigFilePath());
                    version = 6.0f;
                }
            }
        } else {
            String separator = strategy.getSeparator();
            if (version > 6.0) {
                if (AbstractMiddlewareAssessment.fileExists(targetHost, domainHome + separator + "config" + separator + "domain.xml", commandConfig, strategy)) {
                    mainXmlFile = AbstractMiddlewareAssessment.getFileContents(targetHost, domainHome + separator + "config" + separator + "domain.xml", commandConfig, strategy);
                }
            } else {
                if (AbstractMiddlewareAssessment.fileExists(targetHost, domainHome + separator + "JEUSMain.xml", commandConfig, strategy)) {
                    mainXmlFile = AbstractMiddlewareAssessment.getFileContents(targetHost, domainHome + separator + "JEUSMain.xml", commandConfig, strategy);
                }
            }
        }

        return mainXmlFile;
    }


    private String getDomainHome(TargetHost targetHost, MiddlewareInventory middleware, String engineHome, Float version, GetInfoStrategy strategy) throws InterruptedException {
        String domainHome = null;
        if (StringUtils.isNotEmpty(middleware.getDomainHomePath())) {
            domainHome = middleware.getDomainHomePath();
//        } else if (version != null && StringUtils.isNotEmpty(engineHome)) {
//            if (version > 6.0) {
//                /*
//                 *  1. command : ps -ef | grep java | grep jeus.server | grep Djeus.home=$ENGINE_HOME | grep $SERVER_NAME | grep -v grep | tr ' ' '\n'
//                 *
//                 *  2. 해당 서버 프로세스를 조회하여 '-Djava.security.policy=' 프로퍼티값을 추출한다.
//                 *
//                 *  3. $SECURITY.POLICY 경로
//                 *
//                 * */
//                String command = "ps -ef | grep java | grep jeus.server | grep Djeus.home=" + engineHome + " | grep -v grep | tr ' ' '\n'";
//                log.debug("Get domain path command higher v7: [{}]", command);
//                String result = SSHUtil.executeCommand(targetHost, command);
//
//                if (StringUtils.isNotEmpty(result)) {
//                    String[] props = result.split("\n");
//
//                    for (String param : props) {
//                        if (param.contains("Djava.security.policy=")) {
//                            int idx = param.split("=")[1].lastIndexOf("/config");
//                            domainHome = param.split("=")[1].substring(0, idx);
//                        }
//                    }
//                }
//            } else {
//                /*
//                 * 1. 6버전의 경우 엔진 홈 '/config' 하위 중 JEUSMain.xml 파일을 찾는다.
//                 * 2. /JEUSMain.xml 이전경로를 도메인홈으로 가정
//                 **/
//                String command = "sudo find " + engineHome + "/config -name JEUSMain.xml | grep -v example | grep -v security";
//                log.debug("Get domain path command under v6: [{}]", command);
//                String mainXmlPath = SSHUtil.executeCommand(targetHost, command);
//
//                if (StringUtils.isNotEmpty(mainXmlPath) && mainXmlPath.startsWith("/")) {
//                    int idx = mainXmlPath.lastIndexOf("/JEUSMain.xml");
//                    domainHome = mainXmlPath.substring(0, idx);
//                } else {
//                    throw new RoRoException("Jeus JEUSMain.xml file read failed. Please check has a permission to execute \"" +
//                            "[" + targetHost.getUsername() + "@" + targetHost.getIpAddress() + " ~]$ find " + engineHome + "/config -name JEUSMain.xml\" and JEUSMain.xml file is exist.");
//                }
//            }
        }

        /*
         *  만약 사용자가 engine home을 입력하였다면, domain home은 해당 engine home을 기반으로 찾는다.
         *  하지만, 실행중인 process는 다른 엔진이라면 어떻게 해야하나.
         *  현재는 프로세스 종료 및 로그 가이드.
         * */
        // if (domainHome == null) {
        //
        // }

        return domainHome;
    }

    private Float getVersion(TargetHost targetHost, MiddlewareInventory middleware, String engineHome, GetInfoStrategy strategy) throws InterruptedException {
        String versionStr = null;
        Float version = null;

        try {
            if (StringUtils.isNotEmpty(middleware.getEngineVersion())) {
                versionStr = middleware.getEngineVersion();
            } else {
//                String command;
//                if (SSHUtil.isSudoer(targetHost) || StringUtils.isNotEmpty(targetHost.getRootPassword())) {
//                    command = "sudo su - " + targetHost.getUsername() + " /bin/sh -c '" + engineHome + "/bin/jeusadmin -version | egrep \"^JEUS\" | head -1'";
//                } else {
//                    command = "/bin/sh -c '" + engineHome + "/bin/jeusadmin -version | egrep \"^JEUS\" | head -1'";
//                }

//                String command = "sudo su - " + targetHost.getUsername() + " /bin/sh -c '" + engineHome + "/bin/jeusadmin -version | egrep \"^JEUS\" | head -1'";
//                versionStr = SSHUtil.executeCommand(targetHost, command);
//                String command = COMMAND.JEUS_VERSION.command(commandConfig, strategy.isWindows(), targetHost.getUsername(), engineHome);
//                versionStr = strategy.executeCommand(targetHost, command, COMMAND.JEUS_VERSION);

                // https://cloud-osci.atlassian.net/browse/PCR-5637
                // versionStr = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_VERSION, commandConfig, strategy, targetHost.getUsername(), engineHome);
                versionStr = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_VERSION, commandConfig, strategy, engineHome);

                if (StringUtils.isNotEmpty(versionStr) && versionStr.startsWith("JEUS")) {
                    versionStr = versionStr.split(" ")[1];
                } else {
                    throw new InsufficientException("Jeus version check failed.");
                }
            }

            if (StringUtils.isNotEmpty(versionStr)) {
                version = Float.valueOf(versionStr);
            }

            middleware.setEngineVersion(versionStr);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            throw new InsufficientException("Jeus version check failed. Please check \"" +
                    "[" + targetHost.getUsername() + "@" + targetHost.getIpAddress() + " ~]$ " + engineHome + "/bin/jeusadmin -version\" command is valid.");
        }

        return version;
    }

    private String getEngineHome(MiddlewareInventory middleware) throws InterruptedException {
        /*
         *  1. 엔진 홈을 입력 받은 경우 해당 경로를 사용
         *  2. 엔진 홈을 입력 받지 못한 경우
         *     - 6버전의 경우 JeusBootstrapper 클래스로 기동시킨 프로세스를 찾는다.
         *     - 7버전이상일 경우 DomainAdminServerBootstrapper 클래스로 기동시킨 프로세스를 찾는다.
         *  3. jeus.home 프로퍼티값을 엔진 홈값으로 추출.
         *
         * */
        String jeusHome = null;
        if (StringUtils.isNotEmpty(middleware.getEngineInstallationPath())) {
            jeusHome = middleware.getEngineInstallationPath();
        /*} else {
            // 2021.08.18 solution path를 입력받지 못했거나 찾지 못했을때는 Exception을 발생해야 하는건 아닌지..
            // process를 조회하는게 해당 미들웨어에 대한 내용인지를 판단할 수 없음.
            try {
                String processName = middleware.getProcessName();
                if (StringUtils.isNotEmpty(processName)) {
                    String command = "ps -ef | grep java | grep '" + processName + "' | grep -v grep";
                    String result = SSHUtil.executeCommand(targetHost, command);

                    if (StringUtils.isNotEmpty(result)) {
                        for (String param : result.split(" ")) {
                            String JEUS_HOME_PROPERTY = "jeus.home";
                            if (param.contains(JEUS_HOME_PROPERTY)) {
                                jeusHome = param.split("=")[1];
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                RoRoException.checkInterruptedException(e);
                // ignore
                log.debug("Failed extract jeus.home: {}", e.getMessage(), e);
            }

            if (jeusHome == null) {
                throw new RoRoException("Engine path is empty. Please set engine path value or check jeus process has jeus.home option.");
            }
            // try {
            //     / **
            //      *
            //      *  엔진 홈을 입력 받지 못하면 프로세스 조회로 엔진 홈을 찾는다.
            //      *
            //      *  만약, 이 때 프로세스가 여러개일떄 처리 방법.
            //      *   1. solutionPath 입력 guide.
            //      *   2. ???
            //      * * /
            //     // Jeus 6 서버의 bootstrapper
            //     String command = "ps -ef |grep java | grep jeus | grep jeus.server.JeusBootstrapper";
            //     String result = executeCommand(command);
            //
            //     if (result == null || result.equals("")) {
            //         // Jeus 7 서버의 DAS bootstrapper
            //         command = "ps -ef | grep java | grep jeus | grep jeus.server.admin.DomainAdminServerBootstrapper";
            //         result = executeCommand(command);
            //     }
            //
            //     for (String param : result.split(" ")) {
            //         if (param.contains(JEUS_HOME_PROPERTY)) {
            //             jeusHome = param.split("=")[1];
            //         }
            //     }
            // } catch (Exception e) {
            //     log.debug("Fail extract jeus home", e.getMessage());
            // }*/
        }

        return jeusHome;
    }

    // Cluster로 등록된 Deployed Application이 있을 때.
    private JeusAssessmentResult.Instance getApplicationClusterServerName(JeusAssessmentResult.Instance instance) {
        List<JeusAssessmentResult.Application> applications = instance.getApplications();
        List<JeusAssessmentResult.Cluster> clusters = instance.getClusters().getCluster();

        // application에 있는 clusterName을 cluster에 등록된 이름과 비교하여 맞으면 해당 서버 이름을 가져온다.
        for (Application application : applications) {
            if (application.isCluster() && CollectionUtils.isNotEmpty(application.getClusterNames())) {
                List<String> serverNames = new ArrayList<>();

                for (String applicationClusterName : application.getClusterNames()) {
                    for (JeusAssessmentResult.Cluster tempCluster : clusters) {
                        if (applicationClusterName.equals(tempCluster.getName())) {
//                            log.debug("############");
//                            log.debug("{}, {}", applicationClusterName, tempCluster);
//                            log.debug("{}", tempCluster.getServers());
                            serverNames.addAll(tempCluster.getServers());
                        }
                    }
                }

                application.setTarget(serverNames);
            }
        }

        instance.setApplications(applications);

        return instance;
    }

}
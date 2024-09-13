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
package io.playce.roro.mw.asmt.jboss.strategy.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.jboss.dto.JbossAssessmentResult;
import io.playce.roro.mw.asmt.jboss.enums.DOMAIN_CONFIG_FILES;
import io.playce.roro.mw.asmt.jboss.enums.ENGINE;
import io.playce.roro.mw.asmt.jboss.enums.NODE;
import io.playce.roro.mw.asmt.jboss.enums.STANDALONE_CONFIG_FILES;
import io.playce.roro.mw.asmt.jboss.enums.attribute.RESOURCE;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.playce.roro.common.util.ThreadLocalUtils.MW_SCAN_ERROR;
import static io.playce.roro.mw.asmt.util.MWCommonUtil.*;

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
public class JBossHelper {
    private final CommandConfig commandConfig;

    private final XmlMapper xmlMapper;


    /**
     * node 가 null 처리
     *
     * @param node
     *
     * @return
     */
    public String nodeNullToEmptyString(JsonNode node) {
        return node == null ? "" : node.asText();
    }

    /**
     * Stream list 중복 제거
     *
     * @param keyExtractor
     * @param <T>
     *
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static List<Map<String, String>> distinctArray(List<Map<String, String>> target, Object key) {
        if (target != null) {
            target = target.stream().filter(distinctByKey(o -> o.get(key))).collect(Collectors.toList());
        }
        return target;
    }

    /**
     * 서버 인스턴스 네임 및 home.dir , base.dir 저장
     *
     * @param instance
     *
     * @throws InterruptedException
     */
    public void setServNameOrPaths(JbossAssessmentResult.Instance instance, GetInfoStrategy strategy) throws InterruptedException {
        if (instance.getRunTimeOptions().size() > 0) {
            Optional<String> dServers = instance.getRunTimeOptions().stream().filter(s -> s.contains(ENGINE.DSERVER.codeName())).findFirst();
            if (dServers.isPresent()) {
                String servName = Arrays.stream(dServers.get().split("\n")).findFirst().get();
                int startWith = ENGINE.DSERVER.codeName().length();
                String svr = servName.substring(startWith, servName.length());
                instance.setDomainName(svr);
            }

            String baseDir = instance.getRunTimeOptions().stream().filter(s -> s.contains(NODE.BASEDIR.path())).findFirst().get().isEmpty() ? "" : instance.getRunTimeOptions().stream().filter(s -> s.contains(NODE.BASEDIR.path())).findFirst().get();
            String homeDir = instance.getRunTimeOptions().stream().filter(s -> s.contains(NODE.HOMEDIR.path())).findFirst().get().isEmpty() ? "" : instance.getRunTimeOptions().stream().filter(s -> s.contains(NODE.HOMEDIR.path())).findFirst().get();
            baseDir = Arrays.stream(baseDir.split("\n")).findFirst().get();
            homeDir = Arrays.stream(homeDir.split("\n")).findFirst().get();
            instance.setHomeDir(homeDir.substring(homeDir.indexOf("=") + 1, homeDir.length()));
            instance.setBaseDir(baseDir.substring(baseDir.indexOf("=") + 1, baseDir.length()));

            if (StringUtils.isEmpty(instance.getDomainName())) {
                instanceNameCheck(instance, strategy);
            }
        }
    }

    public String loadJavaVersion(TargetHost targetHost, JbossAssessmentResult.Instance instance, GetInfoStrategy strategy, String path, String name) throws InterruptedException {
        String version = getJavaVersion(targetHost, path, name, commandConfig, strategy);
        if (StringUtils.isNotEmpty(version)) {
            instance.setJavaVersion(version);
        }
        return version;
    }

    public String loadJavaVendor(TargetHost targetHost, JbossAssessmentResult.Instance instance, GetInfoStrategy strategy, String path, String name) throws InterruptedException {
        String vendor = getJavaVendor(targetHost, path, name, commandConfig, strategy);
        if (StringUtils.isNotEmpty(vendor)) {
            instance.setJavaVendor(vendor);
        }
        return vendor;
    }

    private String getJavaVersion(TargetHost targetHost, String path, String name, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        boolean sudo = !strategy.isWindows() && SSHUtil.isSudoer(targetHost);
        RemoteExecResult result = executeCommand(targetHost, COMMAND.JBOSS_JAVA_PATH, commandConfig, strategy, sudo, path, name);

        if (strategy.isWindows()) {
            return getWinJavaVersion(targetHost, result, sudo, commandConfig, strategy);
        }
        return MWCommonUtil.getJavaVersion(targetHost, result, sudo, commandConfig, strategy);
    }

    private String getJavaVendor(TargetHost targetHost, String path, String name, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        boolean sudo = !strategy.isWindows() && SSHUtil.isSudoer(targetHost);
        RemoteExecResult result = executeCommand(targetHost, COMMAND.JBOSS_JAVA_PATH, commandConfig, strategy, sudo, path, name);

        String vendor;

        if (strategy.isWindows()) {
            vendor = getWinJavaVendor(targetHost, result, sudo, commandConfig, strategy);
        } else {
            vendor = MWCommonUtil.getJavaVendor(targetHost, result, sudo, commandConfig, strategy);
        }

        // Java Version이 낮아서 속성을 구하지 못하고 Version 데이터가 있으면 Vendor는 Oracle로 처리를 한다.
        if (StringUtils.isEmpty(vendor) &&
                StringUtils.isNotEmpty(getJavaVersion(targetHost, path, name, commandConfig, strategy))) {
            vendor = ORACLE_JAVA_VENDOR;
        }

        return vendor;
    }


    public String getJavaVersionFromJAVA_HOME(TargetHost targetHost, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        String command = COMMAND.JAVA_VERSION_WITH_JAVAHOME.command(commandConfig, strategy.isWindows());
        return strategy.executeCommand(targetHost, command, COMMAND.JAVA_VERSION_WITH_JAVAHOME);
    }

    /**
     * 프로파일 에서 Resource 정보 가져오기
     *
     * @param profiles
     * @param instance
     *
     * @return
     */
    public List<HashMap> getDomainProfileBindItems(JsonNode profiles, JbossAssessmentResult.Instance instance) {
        List<HashMap> list = new ArrayList<>();
        for (JsonNode node : profiles) {
            HashMap<String, List> profileContents = new HashMap<>();
            HashMap dataSources = new HashMap();
            //String profileName =  node.get("name").asText();

            JsonNode dataSource = node.findPath(RESOURCE.datasources.getCodeName()).get(RESOURCE.datasource.getCodeName());
            if (dataSource == null)
                continue;
            JsonNode drivers = node.findPath(RESOURCE.datasources.getCodeName()).get(RESOURCE.drivers.getCodeName());

            dataSources.put("jndi-name", dataSource.get(RESOURCE.jndiname.getCodeName()).asText());
            dataSources.put("pool-name", dataSource.get(RESOURCE.poolname.getCodeName()).asText());
            dataSources.put("enabled", dataSource.get(RESOURCE.enabled.getCodeName()).asText());
            dataSources.put("use-java-context", dataSource.get(RESOURCE.usejavacontext.getCodeName()).asText());
            dataSources.put("connection-url", dataSource.get(RESOURCE.connectionurl.getCodeName()).asText());
            dataSources.put("driver", dataSource.get(RESOURCE.driver.getCodeName()).asText());
            dataSources.put("user-name", dataSource.findPath(RESOURCE.username.getCodeName()).asText());
            dataSources.put("password", dataSource.findPath(RESOURCE.password.getCodeName()).asText());

            list.add(dataSources);
        }
        return list;
    }

    /**
     * context path 가져오기 어플리케이션 WEB-INF 경로 아래 JBOSS-WEB.xml 파싱
     *
     * @param basePath
     * @param root
     * @param strategy
     * @param sudo
     * @param targetHost
     *
     * @return
     *
     * @throws InterruptedException
     */
    public String getWebContextPath(String basePath, String root, GetInfoStrategy strategy, boolean sudo, TargetHost targetHost) throws InterruptedException {
        String separator = strategy.getSeparator();
        String webBasePath = strategy.isAbstractPath(basePath) ? basePath : root + separator + basePath;

        if (StringUtils.isEmpty(webBasePath)) {
            log.error("JBoss base path is null.");
            throw new RoRoException("JBoss base path is null.");
        }

        // path 하위의 /WEB-INF/jboss-web.xml 을 탐색하는 과정에서 path가 /로 끝나면
        // Command에 {PATH}//WEB-INF/jboss-web.xml 와 같은 형태가 됨. (이슈는 아니지만 로그상에 보여지는 부분이라..)
        if (webBasePath.endsWith("/")) {
            webBasePath = webBasePath.substring(0, webBasePath.length() - 1);
        }

        JbossAssessmentResult.ConfigFile tc = new JbossAssessmentResult.ConfigFile();
        tc.setPath(STANDALONE_CONFIG_FILES.JBOSS_WEB_XML.path(webBasePath, strategy));
        Map<String, String> commandMap = Map.of(COMMAND.JBOSS_WEB_XML.name(), COMMAND.JBOSS_WEB_XML.command(commandConfig, strategy.isWindows(), tc.getPath()));
        Map<String, RemoteExecResult> resultMap = executeCommand(targetHost, commandMap, sudo, strategy);
        RemoteExecResult resultJbossWebXml = resultMap.get(COMMAND.JBOSS_WEB_XML.name());

        if (!resultJbossWebXml.isErr()) {
            tc.setContents(resultJbossWebXml.getResult());
            JsonNode jbossWebXml = getJsonNode(tc);
            return jbossWebXml.get("context-root") == null ? webBasePath.substring(webBasePath.lastIndexOf("/")) : jbossWebXml.get("context-root").asText();
        } else {
            log.warn("No jboss-web.xml found: {}", resultJbossWebXml.getError());
        }

        return null;
    }


    /**
     * 현재 엔진 모드네임을 가지고 온다.
     *
     * @param cmdList
     *
     * @return
     */
    private String getEngineModeName(List<String> cmdList) {
        if (cmdList.stream().anyMatch(cmd -> cmd.contains(ENGINE.STANDALONE_PROVIDER.codeName()))) {
            return ENGINE.STANDALONE_NAME.codeName();
        }
        return ENGINE.DOMAIN_NAME.codeName();
    }

    /**
     * 설정 파일 가져오기
     *
     * @param configFileMap
     * @param engine
     *
     * @return
     */
    public JbossAssessmentResult.ConfigFile getConfFile(Map<String, JbossAssessmentResult.ConfigFile> configFileMap, JbossAssessmentResult.Engine engine, Map<String, String> configFiles, GetInfoStrategy strategy) {
        if (engine.getMode().equals(ENGINE.STANDALONE_NAME.codeName())) {
            // WindowsInfoStrategy.runCommands() 에서 처리됨.
            // if (strategy.isWindows()) {
            //     return configFileMap.get("JBOSS_STANDALONE_XML");
            // }
            return configFileMap.get(configFiles.get(ENGINE.STANDALONECONF.codeName()));
        } else {
            // WindowsInfoStrategy.runCommands() 에서 처리됨.
            // if (strategy.isWindows()) {
            //     return configFileMap.get("JBOSS_DOMAIN_XML");
            // }
            return configFileMap.get(configFiles.get(ENGINE.DOMAINCONFIG.codeName()));
        }
    }

    /**
     * 설정 파일 가져오기
     *
     * @param configFileMap
     * @param engine
     *
     * @return
     */
    public JbossAssessmentResult.ConfigFile getHostFile(Map<String, JbossAssessmentResult.ConfigFile> configFileMap, JbossAssessmentResult.Engine engine, Map<String, String> configFiles, GetInfoStrategy strategy) {
        if (strategy.isWindows()) {
            return configFileMap.get("JBOSS_DOMAIN_HOST_XML");
        }
        return configFileMap.get(configFiles.get(ENGINE.HOSTCONFIG.codeName()));
    }

    public String getConfFilePath(Map<String, JbossAssessmentResult.ConfigFile> configFileMap, JbossAssessmentResult.Engine engine, Map<String, String> configFiles, GetInfoStrategy strategy) {
        if (engine.getMode().equals(ENGINE.STANDALONE_NAME.codeName())) {
            // WindowsInfoStrategy.runCommands() 에서 처리됨.
            // if (strategy.isWindows()) {
            //     return configFileMap.get("JBOSS_STANDALONE_XML").getPath();
            // }
            return configFileMap.get(configFiles.get(ENGINE.STANDALONECONF.codeName())).getPath();
        } else {
            // WindowsInfoStrategy.runCommands() 에서 처리됨.
            // if (strategy.isWindows()) {
            //     return configFileMap.get("JBOSS_DOMAIN_XML").getPath();
            // }
            return configFileMap.get(configFiles.get(ENGINE.DOMAINCONFIG.codeName())).getPath();
        }
    }

    public JbossAssessmentResult.ConfigFile getEnvFile(Map<String, JbossAssessmentResult.ConfigFile> configFileMap) {
        return configFileMap.get(STANDALONE_CONFIG_FILES.JBOSS_STANDALONE_SETUP_ENV.name());
    }

    public JbossAssessmentResult.ConfigFile getHostFile(Map<String, JbossAssessmentResult.ConfigFile> configFileMap) {
        return configFileMap.get(DOMAIN_CONFIG_FILES.JBOSS_DOMAIN_HOST_XML.name());
    }


    /**
     * 현재 서버의 서비스 모드를 가지고 온다.
     *
     * @param targetHost
     * @param instance
     * @param sudo
     * @param strategy
     *
     * @throws InterruptedException
     */
    public String getServerCheckMode(TargetHost targetHost, JbossAssessmentResult.Instance instance, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();
        List<String> vmoptions = new ArrayList<>();
        String domainHomePath = instance.getDomainPath();
        String empty = "";

        checkPath(targetHost, domainHomePath, "domain_home_path", strategy);

        if (strategy.isWindows()) {
            domainHomePath = domainHomePath.replaceAll("\\\\", "\\\\\\\\");
        }

        RemoteExecResult processResult = executeCommand(targetHost, COMMAND.JBOSS_VMOPTION, commandConfig, strategy, sudo, domainHomePath);

        if (!processResult.isErr()) {
            String vmString = processResult.getResult();
            vmString = vmString.replaceAll("\\\\", "\\\\\\\\");
            String[] vmArr;
            try {
                vmArr = parser.parseLine(vmString);
            } catch (IOException e) {
                log.error("VmOptions parsing error: {}", e.getMessage());
                ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while VmOptions parsing error. Detail : [" + e.getMessage() + "]");
                return empty;
            }
            List<String> params = Arrays.stream(vmArr).map(s -> s = s.replaceAll("\"", StringUtils.EMPTY)).filter(s -> strategy.isAbstractPath(s) || s.startsWith("-") || s.startsWith("org")).collect(Collectors.toList());
            vmoptions.addAll(params);
        }

        /**
         * JBoss 프로세스가 떠있지 않거나 구동중이 아닐때
         */
        if (vmoptions.size() == 0)
            throw new RoRoException("JBoss Process is Not Running.");
        return getEngineModeName(vmoptions);
    }


    /**
     * 엔진 패스 경로로 이동하고  확인 한다.
     *
     * @param targetHost
     * @param path
     * @param pathName
     * @param strategy
     *
     * @throws InterruptedException
     */
    public void checkPath(TargetHost targetHost, String path, String pathName, GetInfoStrategy strategy) throws InterruptedException {
        RemoteExecResult enginePath = executeCommand(targetHost, COMMAND.CHECK_PATH, commandConfig, strategy, false, path);
        if (enginePath.isErr()) {
            throw new RoRoException("Check the " + pathName + ": " + path + ", error message: " + enginePath.getError());
        }
    }


    /**
     * Jboss 검색 파일 저장
     *
     * @param configFiles
     * @param ipAddress
     * @param strategy
     *
     * @throws InterruptedException
     */
    public void saveConfigFiles(Map<String, JbossAssessmentResult.ConfigFile> configFiles, String ipAddress, GetInfoStrategy strategy) throws InterruptedException {
        String workdir = CommonProperties.getWorkDir();
        for (JbossAssessmentResult.ConfigFile file : configFiles.values()) {
            String workDir = workdir + File.separator + "assessment" + File.separator + "raw_files" + File.separator + ipAddress;
            String filePath = file.getPath();

            String saveDirectory = strategy.getParentDirectoryByPath(workDir, filePath);
            log.debug("assessment file will be save to [{}]", saveDirectory);

            try {
                FileUtils.forceMkdir(new File(saveDirectory));
                File f = new File(workDir + filePath);
                FileUtils.writeStringToFile(f, file.getContents(), "UTF-8");
            } catch (IOException e) {
                RoRoException.checkInterruptedException(e);
                log.error("JBoss assessment, file save error: {}", e.getMessage(), e);
                ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while file save error. Detail : [" + e.getMessage() + "]");
            }
        }
    }


    public String setDataFromVMoptions(JbossAssessmentResult.Instance instance, String start) {
        for (String option : instance.getRunTimeOptions()) {
            if (option.startsWith(start)) {
                return option.substring(start.length());
            }
        }
        return null;
    }

    public String setDataFromVMoptionsOfContains(JbossAssessmentResult.Instance instance, String findStr) {
        for (String option : instance.getRunTimeOptions()) {
            if (option.contains(findStr)) {
                return option.substring(findStr.length());
            }
        }
        return null;
    }

    public File findFile(File root, String configFileName) {
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files == null)
                return null;

            for (File file : files) {
                File f = findFile(file, configFileName);
                if (f != null) {
                    return f;
                }
            }
        } else {
            if (root.getName().equals(configFileName)) {
                return root;
            }
        }
        return null;
    }

    public JsonNode getJsonNode(JbossAssessmentResult.ConfigFile tc) {
        JsonNode serverXml = null;
        try {
            serverXml = xmlMapper.readTree(tc.getContents());
        } catch (JsonProcessingException e) {
            log.error("{} parse error -> {}", tc.getPath(), e);
            ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while json parse. Detail : [" + e.getMessage() + "]");
        }
        return serverXml;
    }


    /**
     * 포트 및 프로토콜  , 로 문자열 구분
     *
     * @param connectors
     * @param attr
     * @param joinStr
     *
     * @return
     */
    public String strJoin(List<Map<String, String>> connectors, String attr, String joinStr) {
        return connectors.stream().map(s -> s.get(attr)).collect(Collectors.joining(joinStr));
    }

    public static String duplicateStrRemove(String str) {
        String retStr = "";
        for (int i = 0; i < str.length(); i++) {
            if (str.indexOf(str.charAt(i)) == i) {
                if (String.valueOf(str.charAt(i)) != ",") {
                    retStr += str.charAt(i);
                }
            }
        }
        return retStr;
    }


    public int findNumber(String input) {
        return Integer.parseInt(input.replaceAll("[^0-9]", ""));
    }

    public static String replaceDefinesInString(Map<String, String> defineMap, String str) {
        String newStr = str;

        String regex;
        for (String key : defineMap.keySet()) {
            regex = "\\$\\{}";

            Pattern pattern = Pattern.compile(regex);
            newStr = pattern.matcher(newStr).replaceAll(regex);
        }

        return StringUtils.strip(newStr, StringUtils.SPACE + "\"");
    }


    public void setConfigPath(JbossAssessmentResult.Instance instance) throws InterruptedException {
        String configPath = "";
        if (instance.getRunTimeOptions().size() > 0) {
            Map<String, String> map = new HashMap<>();
            String configpath = instance.getRunTimeOptions().stream().filter(s -> s.contains("config.dir")).findFirst().isEmpty() ?
                    "" : instance.getRunTimeOptions().stream().filter(s -> s.contains("config.dir")).findFirst().get();
            if (StringUtils.isNotEmpty(configpath)) {
                int index = configpath.indexOf("=");
                configPath = configpath.substring(index + 1);
            }
            instance.setConfigPath(configPath);
        }
    }

    public String getWinJavaVersion(TargetHost targetHost, RemoteExecResult result, boolean sudo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();
        if (result.isErr()) {
            return getJavaVersionFromJAVA_HOME(targetHost, commandConfig, strategy);
        } else {
            String vmString = result.getResult();
            vmString = vmString.replaceAll("\\\\", "\\\\\\\\");
            String[] vmArr;
            try {
                vmArr = parser.parseLine(vmString);
            } catch (IOException e) {
                log.error("vmString parsing error: {}", e.getMessage());
                ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while vmString parsing. Detail : [" + e.getMessage() + "]");
                return "";
            }

            Optional<String> path = Arrays.stream(vmArr).map(s -> s = s.replaceAll("\"", StringUtils.EMPTY)).filter(s -> s.contains("bin\\java")).findFirst();
            String javapath = path.get();
            String[] values = javapath.split("=");
            log.debug("finded java path: {}", javapath);

            Map<String, String> commandMap = Map.of(COMMAND.JAVA_VERSION.name(), COMMAND.JAVA_VERSION.command(commandConfig, strategy.isWindows(), values[1]));
            Map<String, RemoteExecResult> resultMap = executeCommand(targetHost, commandMap, sudo, strategy);
            result = resultMap.get(COMMAND.JAVA_VERSION.name());
            if (!result.isErr()) {
                return result.getResult().trim();
            }
        }
        return null;
    }

    public String getWinJavaVendor(TargetHost targetHost, RemoteExecResult result, boolean sudo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();
        if (result.isErr()) {
            return getJavaVersionFromJAVA_VENDOR(targetHost, commandConfig, strategy);
        } else {
            String vmString = result.getResult();
            vmString = vmString.replaceAll("\\\\", "\\\\\\\\");
            String[] vmArr;
            try {
                vmArr = parser.parseLine(vmString);
            } catch (IOException e) {
                log.error("vmString parsing error: {}", e.getMessage());
                ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while vmString parsing. Detail : [" + e.getMessage() + "]");
                return "";
            }

            Optional<String> path = Arrays.stream(vmArr).map(s -> s = s.replaceAll("\"", StringUtils.EMPTY)).filter(s -> s.contains("bin\\java")).findFirst();
            String javapath = path.get();
            String[] values = javapath.split("=");
            log.debug("finded java path: {}", javapath);

            Map<String, String> commandMap = Map.of(COMMAND.JAVA_VENDOR.name(), COMMAND.JAVA_VENDOR.command(commandConfig, strategy.isWindows(), values[1]));
            Map<String, RemoteExecResult> resultMap = executeCommand(targetHost, commandMap, sudo, strategy);
            result = resultMap.get(COMMAND.JAVA_VENDOR.name());
            if (!result.isErr()) {
                return getJavaVendorProperty(result.getResult().trim());
            }
        }
        return null;
    }

    private void instanceNameCheck(JbossAssessmentResult.Instance instance, GetInfoStrategy strategy) {
        int idx = strategy.isWindows() ? instance.getBaseDir().lastIndexOf("\\") : instance.getBaseDir().lastIndexOf("/");
        String domainName = instance.getDomainName() == null ? instance.getBaseDir().substring(idx + 1, instance.getBaseDir().length()) : instance.getDomainName();
        instance.setDomainName(domainName);
    }

}
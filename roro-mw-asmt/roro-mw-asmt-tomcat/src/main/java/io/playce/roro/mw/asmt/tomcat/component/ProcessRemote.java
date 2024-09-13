/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * Dong-Heon Han    Feb 12, 2022		First Draft.
 */

package io.playce.roro.mw.asmt.tomcat.component;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.SplitUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.tomcat.dto.TomcatAssessmentResult;
import io.playce.roro.mw.asmt.tomcat.enums.CONFIG_FILES;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.playce.roro.common.util.ThreadLocalUtils.MW_SCAN_ERROR;
import static io.playce.roro.mw.asmt.util.MWCommonUtil.ORACLE_JAVA_VENDOR;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessRemote {
    private final CommandConfig commandConfig;


    public void loadEngineInfo(TargetHost targetHost, TomcatAssessmentResult.Engine engine, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        String engineInstallationPath = engine.getPath();
        checkPath(targetHost, engineInstallationPath, "engine_installation_path", strategy, sudo);

        Map<String, String> commandMap = Map.of(
                COMMAND.TOMCAT_RELEASE_NOTE.name(), COMMAND.TOMCAT_RELEASE_NOTE.command(commandConfig, strategy.isWindows(), engineInstallationPath),
                COMMAND.TOMCAT_VERSION.name(), COMMAND.TOMCAT_VERSION.command(commandConfig, strategy.isWindows(), engineInstallationPath),
                COMMAND.TOMCAT_NUMBER.name(), COMMAND.TOMCAT_NUMBER.command(commandConfig, strategy.isWindows(), engineInstallationPath)
        );
        Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);

        RemoteExecResult release = resultMap.get(COMMAND.TOMCAT_RELEASE_NOTE.name());

        if (release.isErr()) {
            RemoteExecResult version = resultMap.get(COMMAND.TOMCAT_VERSION.name());
            String versionString = version.getResult();

            RemoteExecResult number = resultMap.get(COMMAND.TOMCAT_NUMBER.name());
            String numberString = number.getResult();

            // error와 같이 result를 나오는 경우때문에..
            if (StringUtils.isNotEmpty(versionString) && versionString.contains("Server version: ")) {
                // NOTE: Picked up JDK_JAVA_OPTIONS:  --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED Server version: Apache Tomcat/8.5.39 (Ubuntu)
                versionString = versionString.substring(versionString.lastIndexOf(":") + 1).trim();

                if (versionString.contains("/")) {
                    String[] vStrArr = versionString.split("/");

                    engine.setName(StringUtils.defaultString(vStrArr[0]));
                    engine.setVersion(StringUtils.defaultString(vStrArr[1]).replaceAll(".*?((?<!\\w)\\d+([.-]\\d+)*).*", "$1"));
                } else if (StringUtils.isNotEmpty(numberString) && numberString.contains("Server number: ")) {
                    numberString = StringUtils.defaultString(numberString.substring(numberString.indexOf(":") + 1).trim());
                    engine.setName("Apache Tomcat");

                    int dotCount = numberString.length() - numberString.replace(".", "").length();
                    if (dotCount == 3) {
                        numberString = numberString.substring(0, numberString.lastIndexOf("."));
                        engine.setVersion(numberString);
                    } else {
                        engine.setVersion(numberString);
                    }
                } else {
                    engine.setName("Apache Tomcat");
                    log.error("No version found {}.", versionString);
                }
            } else if (StringUtils.isNotEmpty(numberString) && numberString.contains("Server number: ")) {
                numberString = StringUtils.defaultString(numberString.substring(numberString.indexOf(":") + 1).trim());
                engine.setName("Apache Tomcat");

                int dotCount = numberString.length() - numberString.replace(".", "").length();
                if (dotCount == 3) {
                    numberString = numberString.substring(0, numberString.lastIndexOf("."));
                    engine.setVersion(numberString);
                } else {
                    engine.setVersion(numberString);
                }
            } else {
                engine.setName("Apache Tomcat");
                log.error("No version found. command: {}", version.getCommand());
            }
        } else {
            String releaseString = release.getResult();

            if (releaseString.contains("Apache Tomcat Version")) {
                String[] rStrArr = SplitUtil.split(releaseString, "Version", 2);
                engine.setName(rStrArr[0]);
                engine.setVersion(rStrArr[1]);
            } else {
                engine.setName("Apache Tomcat");
                log.error("No version found from RELEASE-NOTES: {}", release.getResult());
            }
        }
    }

    private void checkPath(TargetHost targetHost, String path, String pathName, GetInfoStrategy strategy, boolean sudo) throws InterruptedException {
//        RemoteExecResult enginePath = MWCommonUtil.executeCommand(targetHost, Map.of(COMMAND.TOMCAT_CHECK_PATH.name(), COMMAND.TOMCAT_CHECK_PATH.command(commandConfig, strategy.isWindows(), path)), false, strategy)
//                .get(COMMAND.TOMCAT_CHECK_PATH.name());
        RemoteExecResult enginePath = MWCommonUtil.executeCommand(targetHost, COMMAND.CHECK_PATH, commandConfig, strategy, sudo, path);
        if (enginePath.isErr()) {
            throw new RoRoException("Check the " + pathName + ": " + path + ", error message: " + enginePath.getError());
        }
    }

    public void loadVmOption(TargetHost targetHost, TomcatAssessmentResult.Instance instance, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();
        List<String> vmoptions = new ArrayList<>();
        String domainHomePath = instance.getPath();

        checkPath(targetHost, domainHomePath, "domain_home_path", strategy, sudo);

//        RemoteExecResult processResult = MWCommonUtil.executeCommand(targetHost, Map.of(COMMAND.TOMCAT_VMOPTION.name(), COMMAND.TOMCAT_VMOPTION.command(commandConfig, strategy.isWindows(), domainHomePath)), sudo, strategy)
//                .get(COMMAND.TOMCAT_VMOPTION.name());
        RemoteExecResult processResult = MWCommonUtil.executeCommand(targetHost, COMMAND.TOMCAT_VMOPTION, commandConfig, strategy, sudo, domainHomePath);
        if (!processResult.isErr()) {
            String vmString = processResult.getResult();
            vmString = vmString.replaceAll("\\\\", "\\\\\\\\");
//            String[] vmArr = vmString.split(StringUtils.SPACE);
            String[] vmArr;
            try {
                vmArr = parser.parseLine(vmString);
            } catch (IOException e) {
                log.error("VmOpions parsing error: {}", e.getMessage());
                ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while VmOptions parsing error. Detail : [" + e.getMessage() + "]");
                return;
            }
            List<String> params = Arrays.stream(vmArr).map(s -> s = s.replaceAll("\"", StringUtils.EMPTY)).filter(s -> strategy.isAbstractPath(s) || s.startsWith("-")).collect(Collectors.toList());
            vmoptions.addAll(params);
        }
        instance.setOptions(vmoptions);
    }

    public void loadConfigFiles(TargetHost targetHost, TomcatAssessmentResult.Engine engine, TomcatAssessmentResult.Instance instance, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        Map<String, TomcatAssessmentResult.ConfigFile> map = new HashMap<>();
        boolean idEngineDeployed = engine.getPath().equals(instance.getPath());

        Map<String, String> commandMap = new HashMap<>();
        Map<String, TomcatAssessmentResult.ConfigFile> fileMap = new HashMap<>();
        for (CONFIG_FILES configFile : CONFIG_FILES.values()) {
            TomcatAssessmentResult.ConfigFile tc = new TomcatAssessmentResult.ConfigFile();
            String directory = idEngineDeployed ? engine.getPath() : instance.getPath();
            tc.setPath(configFile.path(directory, strategy));

            commandMap.put(configFile.name(), COMMAND.valueOf(configFile.name()).command(commandConfig, strategy.isWindows(), tc.getPath()));
            fileMap.put(configFile.name(), tc);
        }
        Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);

        for (String key : resultMap.keySet()) {
            RemoteExecResult result = resultMap.get(key);

            TomcatAssessmentResult.ConfigFile tc = fileMap.get(key);
            if (!result.isErr()) {
                tc.setContents(result.getResult());
                map.put(key, tc);
            } else {
                log.info("[{}] file read failed : [{}]", tc.getPath(), result.getError());
                if (CONFIG_FILES.TOMCAT_CONFIG_SERVER.name().equals(key) || CONFIG_FILES.TOMCAT_CONFIG_CONTEXT.name().equals(key)) {
                    throw new InsufficientException("Tomcat config file(" + tc.getPath() + ") read failed. Please check file is exist and has permission to read at \"[" +
                            targetHost.getUsername() + "@" + targetHost.getIpAddress() + "]\"");
                }
            }
        }

        instance.setConfigFiles(map);
    }

    public void loadDirectories(TargetHost targetHost, String enginePath, String instancePath, TomcatAssessmentResult.Webapps webapps, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        String root = enginePath.equals(instancePath) ? enginePath : instancePath;
        String basePath = webapps.getBasePath();
        if (StringUtils.isNotEmpty(basePath)) {
            basePath = strategy.isAbstractPath(basePath) ? basePath : root + strategy.getSeparator() + basePath;

            Map<String, String> commandMap = Map.of(COMMAND.TOMCAT_DIRECTORY.name(), COMMAND.TOMCAT_DIRECTORY.command(commandConfig, strategy.isWindows(), basePath));
            Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
            RemoteExecResult directories = resultMap.get(COMMAND.TOMCAT_DIRECTORY.name());
            List<String> apps = new ArrayList<>();
            if (!directories.isErr()) {
                String direcotiesStr = directories.getResult();
                String[] directoriyArr = direcotiesStr.split(strategy.getCarriageReturn());

                for (String d : directoriyArr) {
                    String[] dirArr = d.split(StringUtils.SPACE);
                    String dir = dirArr[dirArr.length - 1].replaceAll(strategy.getCarriageReturn(), StringUtils.EMPTY);
                    if (StringUtils.isNotEmpty(dir)) {
                        apps.add(dir);
                    }
                }
                webapps.setApps(apps);
            } else {
                log.error("No directoies found: {}", directories.getError());
            }
        } else {
            log.error("Tomcat webapp base path is null.");
        }
    }

    public void loadRunUser(TargetHost targetHost, TomcatAssessmentResult.Instance instance, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        Map<String, String> commandMap = Map.of(COMMAND.TOMCAT_RUN_USER.name(), COMMAND.TOMCAT_RUN_USER.command(commandConfig, strategy.isWindows(), instance.getPath()));
        Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
        RemoteExecResult result = resultMap.get(COMMAND.TOMCAT_RUN_USER.name());
        if (!result.isErr()) {
            instance.setRunUser(result.getResult());
        } else {
            log.error("error run user: {}", result.getError());
        }
    }

    public void loadJavaVersion(TargetHost targetHost, TomcatAssessmentResult.Instance instance, GetInfoStrategy strategy) throws InterruptedException {
        String version = getJavaVersion(targetHost, instance.getPath(), commandConfig, strategy);
        if (StringUtils.isNotEmpty(version)) {
            instance.setJavaVersion(version);
        }
    }

    public void loadJavaVendor(TargetHost targetHost, TomcatAssessmentResult.Instance instance, GetInfoStrategy strategy) throws InterruptedException {
        String vendor = getJavaVendor(targetHost, instance.getPath(), commandConfig, strategy);

        if (StringUtils.isEmpty(vendor) &&
                StringUtils.isNotEmpty(getJavaVersion(targetHost, instance.getPath(), commandConfig, strategy))) {
            vendor = ORACLE_JAVA_VENDOR;
        }

        if (StringUtils.isNotEmpty(vendor)) {
            instance.setJavaVendor(vendor);
        }
    }

    private String getJavaVersion(TargetHost targetHost, String path, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        boolean sudo = !strategy.isWindows() && SSHUtil.isSudoer(targetHost);
//        Map<String, String> commandMap = Map.of(COMMAND.TOMCAT_JAVA_PATH.name(), COMMAND.TOMCAT_JAVA_PATH.command(commandConfig, strategy.isWindows(), path));
//        Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
//        RemoteExecResult result = resultMap.get(COMMAND.TOMCAT_JAVA_PATH.name());
        RemoteExecResult result = MWCommonUtil.executeCommand(targetHost, COMMAND.TOMCAT_JAVA_PATH, commandConfig, strategy, sudo, path);

        return MWCommonUtil.getJavaVersion(targetHost, result, sudo, commandConfig, strategy);
    }

    private String getJavaVendor(TargetHost targetHost, String path, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        boolean sudo = !strategy.isWindows() && SSHUtil.isSudoer(targetHost);
        RemoteExecResult result = MWCommonUtil.executeCommand(targetHost, COMMAND.TOMCAT_JAVA_PATH, commandConfig, strategy, sudo, path);

        return MWCommonUtil.getJavaVendor(targetHost, result, sudo, commandConfig, strategy);
    }

}
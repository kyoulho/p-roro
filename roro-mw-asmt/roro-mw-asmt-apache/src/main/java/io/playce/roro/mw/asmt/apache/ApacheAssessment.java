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
 * Jaeeon Bae       11월 10, 2021            First Draft.
 */
package io.playce.roro.mw.asmt.apache;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.AbstractMiddlewareAssessment;
import io.playce.roro.mw.asmt.MiddlewareAssessment;
import io.playce.roro.mw.asmt.apache.dto.ApacheAssessmentResult;
import io.playce.roro.mw.asmt.apache.helper.ApacheHelper;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.mw.asmt.util.WasAnalyzerUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

import static io.playce.roro.common.util.ThreadLocalUtils.MW_SCAN_ERROR;
import static io.playce.roro.mw.asmt.apache.helper.ApacheHelper.*;

@Component("APACHEAssessment")
@RequiredArgsConstructor
@Slf4j
public class ApacheAssessment implements MiddlewareAssessment {
    private final CommandConfig commandConfig;

    @Setter
    @Getter
    private static class ClassMember {
        private String httpdParentDir;
        private boolean isSslEngineUsed = false;
        private boolean isSslCertificateUsed = false;

        private ClassMember(String httpdParentDir) {
            this.httpdParentDir = httpdParentDir;
        }
    }

    @Override
    public MiddlewareAssessmentResult assessment(TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("\n\n:+:+:+:+ Apache Httpd Analyzer :+:+:+:+");
        /**
         * conf/httpd.conf 파일 탐색
         * httpd.conf 파일 및 Include 파일 파싱
         */
        ApacheAssessmentResult assessmentResult = new ApacheAssessmentResult();
        String separator = strategy.getSeparator();
        ClassMember member = new ClassMember(separator + "bin");

        // middleware default value;
        String enginePath = middleware.getEngineInstallationPath();
//        String instancePath = StringUtils.isNotEmpty(middleware.getDomainHomePath()) ? middleware.getDomainHomePath() : middleware.getEngineInstallationPath();
        String instancePath = middleware.getDomainHomePath();
        String rootConfFile = null;

        // exec command
//        String psCommand = "sudo ps -ef | grep httpd | grep " + instancePath;
//        String psResult = SSHUtil.executeCommand(targetHost, psCommand);

        String command = COMMAND.GET_PROCESS.command(commandConfig, strategy.isWindows(), instancePath);
        String psResult = AbstractMiddlewareAssessment.getSshCommandResultTrim(targetHost, command, COMMAND.GET_PROCESS, strategy);

        log.debug("Command: [{}]", command);
        log.debug("Result : [{}]", psResult);

        // get Engine info
        ApacheAssessmentResult.Engine engine = getEngineMap(targetHost, enginePath, member, strategy);
        log.debug("engine : [{}]", engine);

        if (AbstractMiddlewareAssessment.hasUploadedConfigFile(middleware)) {
            // 업로드된 conf_file이 있으면 업로드된 conf 파일 분석
            log.debug("Read Uploaded Apache HTTP config File : [{}]", middleware.getConfigFilePath());
//            rootConfFile = (middleware.getConfigFilePath() + separator + "conf" + separator + "httpd.conf").replaceAll("//", "/");
            rootConfFile = (middleware.getConfigFilePath() + separator + "conf" + separator + "httpd.conf");

            File f = new File(rootConfFile);
            if (!f.exists()) {
                log.debug("upload path : [{}], conf file is invalid or not exists.", middleware.getConfigFilePath());
                throw new InsufficientException("httpd.conf file does not exist.");
            }
            instancePath = middleware.getConfigFilePath();
        } else {
            // 디스커버된 미들웨어의 config_file이 있으면 해당 파일을 분석 진행
//            if (engine.getName() != null) {
//                if (engine.getName().contains("Oracle")) {
//                    rootConfFile = instancePath + "/httpd.conf";
//                } else {
//                    rootConfFile = instancePath + "/conf/httpd.conf";
//                }
//            } else {
//                if (AbstractMiddlewareAssessment.fileExists(targetHost, instancePath + "/httpd.conf")) {
//                    rootConfFile = instancePath + "/httpd.conf";
//                } else if (AbstractMiddlewareAssessment.fileExists(targetHost, instancePath + "/conf/httpd.conf")) {
//                    rootConfFile = instancePath + "/conf/httpd.conf";
//                }
//            }
            rootConfFile = instancePath;
        }


//        String catCommand = "sudo cat " + rootConfFile;
//        log.debug("cat Command : [{}]", catCommand);
//        String httpdConf = SSHUtil.executeCommand(targetHost, catCommand);
        String httpdConf = getCatResult(targetHost, strategy, rootConfFile);

        if (StringUtils.isEmpty(httpdConf)) {
            log.debug("cat Result : [{}] is Empty. Please check the read config files. [{}]", httpdConf, rootConfFile);

            throw new InsufficientException("Apache config file(" + rootConfFile + ") read failed. Please check file is exist and has permission to read at \"" +
                    targetHost.getUsername() + "@" + targetHost.getIpAddress() + "\"");
        } else {
            // 읽어들인 파일 저장
            String ipAddress = targetHost.getIpAddress();
            WasAnalyzerUtil.saveAssessmentFile(ipAddress, rootConfFile, httpdConf, CommonProperties.getWorkDir(), strategy);
        }

        ApacheAssessmentResult.Instance instance = new ApacheAssessmentResult.Instance();
        ApacheAssessmentResult.General general = new ApacheAssessmentResult.General();
        ApacheAssessmentResult.SolutionSpecific solutionSpecific = new ApacheAssessmentResult.SolutionSpecific();
        List<String> wasConfigFile = new ArrayList<>();

        // set config file
        List<ApacheAssessmentResult.ConfigFile> configFiles = new ArrayList<>();
        ApacheAssessmentResult.ConfigFile configFile = new ApacheAssessmentResult.ConfigFile();
        configFile.setPath(rootConfFile);
        configFile.setContent(httpdConf);
        configFiles.add(configFile);
        instance.setConfigFiles(configFiles);

        // get Directive (Define, Listen, etc)
        Map<String, String> defineMap = getDirective(engine, httpdConf, instance, instancePath, general, solutionSpecific, wasConfigFile, true, member, strategy);

        // get module (load and compile)
        // getModule(binFile, httpdFile, instance);

        // get Enclosure (IfModule, VirtualHost, Directory, etc)
        getEnclosure(defineMap, httpdConf, instance, strategy);

        // include 항목 조회 및 추가
        String instancePathDir = instancePath.substring(0, instancePath.lastIndexOf(separator));
        getInclude(defineMap, engine, instance, instancePathDir, general, solutionSpecific, wasConfigFile, targetHost, middleware, member, strategy);
        log.debug("instance : [{}]", instance);

        // document Root 재설정
        getDocumentRoot(instance, general);

//         set run user
        engine.setRunUser(getRunUser(rootConfFile, targetHost, strategy));

        // set general
        getGeneral(engine, instance, general, middleware, rootConfFile, targetHost, member, instancePath, strategy);

        //set instance run user
        general.setRunUser(engine.getRunUser());

        // assessmentResult.setThirdPartySolutions(ThirdPartySolutionUtil.detectThirdPartySolutionsFromMiddleware(targetHost, strategy.isWindows(), engine.getPath(), instance.getGeneral().getDocumentRoot()));
        assessmentResult.setEngine(engine);
        assessmentResult.setInstance(instance);

        return assessmentResult;
    }

    private String getRunUser(String rootConfigFile, TargetHost targetHost, GetInfoStrategy strategy) throws InterruptedException {
//        String runUserCommand = "sudo ps -ef | grep httpd | grep " + rootConfigFile + " | grep -v grep | awk '{print $1}' | uniq | head -1";
//        String result = StringUtils.defaultString(SSHUtil.executeCommand(targetHost, runUserCommand).trim());
        String command = COMMAND.APACHE_RUN_USER.command(commandConfig, strategy.isWindows(), rootConfigFile);
        String result = strategy.executeCommand(targetHost, command, COMMAND.APACHE_RUN_USER);
        log.debug(":+:+:+:+:+:+:+: getRunUser() - commandLine : [{}]", command);
        if (StringUtils.isEmpty(result)) {
//            result = SSHUtil.executeCommand(targetHost, "sudo ps -ef | grep httpd | grep -v grep | awk '{print $1}' | uniq | head -1");
//            command = COMMAND.RUN_USER.command(commandConfig, strategy.isWindows(), "httpd");
//            result = strategy.executeCommand(targetHost, command, COMMAND.RUN_USER);
            result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.APACHE_RUN_USER1, commandConfig, strategy, "httpd");
        }
        if (StringUtils.isEmpty(result)) {
//            result = SSHUtil.executeCommand(targetHost, "sudo ps -ef | grep apache2 | grep -v grep | awk '{print $1}' | uniq | head -1");
//            command = COMMAND.RUN_USER.command(commandConfig, strategy.isWindows(), "apache2");
//            result = strategy.executeCommand(targetHost, command, COMMAND.RUN_USER);
            result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.RUN_USER, commandConfig, strategy, "apache2");
        }
        return result;
    }

    private void getGeneral(ApacheAssessmentResult.Engine engine, ApacheAssessmentResult.Instance instance,
                            ApacheAssessmentResult.General general, MiddlewareInventory middleware, String rootConfigFile,
                            TargetHost targetHost, ClassMember member, String instancePath, GetInfoStrategy strategy) throws InterruptedException {
        log.debug(":+:+:+:+:+:+:+: getGeneral() :+:+:+:+:+:+:+:");

        // Http Server 가 기동된 시간을 가져온다.
//        String executeTimeCmd = "sudo ps -eo '%p %U %t %a' | grep httpd | grep " + instancePath + " | grep -v grep | awk '{print $3}' | head -1";
//        String executeTime = SSHUtil.executeCommand(targetHost, executeTimeCmd);
        instancePath = instancePath.replaceAll("\\\\", "\\\\\\\\");
        String command = COMMAND.APACHE_EXECUTED_TIME.command(commandConfig, strategy.isWindows(), instancePath);
        String executeTime = AbstractMiddlewareAssessment.getSshCommandResultTrim(targetHost, command, COMMAND.APACHE_EXECUTED_TIME, strategy);
        if (StringUtils.isEmpty(executeTime)) {
//            executeTime = SSHUtil.executeCommand(targetHost, "sudo ps -eo '%p %U %t %a' | grep httpd | grep -v grep | awk '{print $3}' | head -1");
//            command = COMMAND.EXECUTED_TIME.command(commandConfig, strategy.isWindows(), "httpd");
//            executeTime = AbstractMiddlewareAssessment.getSshCommandResultTrim(targetHost, command, COMMAND.EXECUTED_TIME, strategy);
            executeTime = MWCommonUtil.getExecuteResult(targetHost, COMMAND.EXECUTED_TIME, commandConfig, strategy, "httpd");
        }
        if (StringUtils.isEmpty(executeTime)) {
//            executeTime = SSHUtil.executeCommand(targetHost, "sudo ps -eo '%p %U %t %a' | grep apache2 | grep -v grep | awk '{print $3}' | head -1");
//            command = COMMAND.APACHE_EXECUTED_TIME_NOT_MONITOR.command(commandConfig, strategy.isWindows(), "apache2");
//            executeTime = AbstractMiddlewareAssessment.getSshCommandResultTrim(targetHost, command, COMMAND.APACHE_EXECUTED_TIME_NOT_MONITOR, strategy);
            executeTime = MWCommonUtil.getExecuteResult(targetHost, COMMAND.APACHE_EXECUTED_TIME_NOT_MONITOR, commandConfig, strategy, "apache2");
        }

//        Date nowDate = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        String startTime = null;
//        if (executeTime.length() > 0) {
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(nowDate);
//
//            String day = null;
//            String[] time = null;
//            if (executeTime.contains("-")) {
//                day = executeTime.split("-")[0];
//                time = executeTime.split("-")[1].trim().split(":");
//            } else {
//                time = executeTime.split(":");
//            }
//
//            // 날짜 계산
//            if (day != null) {
//                cal.add(Calendar.DAY_OF_YEAR, -(Integer.parseInt(day)));
//            }
//
//            if (time.length == 3) {
//                cal.add(Calendar.HOUR_OF_DAY, -(Integer.parseInt(time[0].trim())));
//                cal.add(Calendar.MINUTE, -(Integer.parseInt(time[1].trim())));
//                cal.add(Calendar.SECOND, -(Integer.parseInt(time[2].trim())));
//            } else if (time.length == 2) {
//                cal.add(Calendar.MINUTE, -(Integer.parseInt(time[0].trim())));
//                cal.add(Calendar.SECOND, -(Integer.parseInt(time[1].trim())));
//            }
//
//            startTime = dateFormat.format(cal.getTime());
//        }

        // Running Type : Event / Prefork / Worker 중 설정 -> 하나가 아닐 경우 ""로 표기
        String runningType = null;
        if (instance.getModules() != null) {
            int checkCount = 0;
            for (ApacheAssessmentResult.LoadModule module : instance.getModules()) {
                if ("mpm_event_module".equals(module.getName())) {
                    runningType = "Event";
                    checkCount++;
                } else if ("mpm_prefork_module".equals(module.getName())) {
                    runningType = "Prefork";
                    checkCount++;
                } else if ("mpm_worker_module".equals(module.getName())) {
                    runningType = "Worker";
                    checkCount++;
                }

            }

            if (checkCount > 1) {
                runningType = "";
            }
        }

        // general 이 비어 있을 경우 신규 general 을 만들어 준다.
        if (general == null) {
            general = new ApacheAssessmentResult.General();
        }

        general.setVendor(StringUtils.defaultString(middleware.getVendorName()));
        general.setSolutionName(StringUtils.defaultString(engine.getName()));
        general.setSolutionVersion(StringUtils.defaultString(engine.getVersion()));
        general.setInstallHome(StringUtils.defaultString(instancePath));
        general.setServerName(StringUtils.defaultString(middleware.getInventoryName()));
        if (runningType != null) {
            general.setRunningType(runningType);
        }
        general.setUseSsl((member.isSslEngineUsed() && member.isSslCertificateUsed()));
        general.setServerStatus(StringUtils.isNotEmpty(executeTime) ? "Running" : "Stopped");

//        if (!StringUtils.isEmpty(startTime)) {
//            general.setStartedDate(startTime);
//        }
//        general.setStartedDate(executeTime);
        general.setScannedDate(new Date());
        general.setRunUser(StringUtils.defaultString(engine.getRunUser()));

        instance.setGeneral(general);
    }

    private void getDocumentRoot(ApacheAssessmentResult.Instance instance, ApacheAssessmentResult.General general) {
        log.debug(":+:+:+:+:+:+:+: getDocumentRoot() :+:+:+:+:+:+:+:");
        Map<String, String> documentRootMap = instance.getDocumentRoot();
        List<String> dataList = null;

        if (documentRootMap != null && general.getDocumentRoot() != null) {
            dataList = instance.getDirectory().get(general.getDocumentRoot());

            if (dataList != null) {
                for (String data : dataList) {
                    String[] dataArr = data.split("\\s+");

                    instance.getDocumentRoot().put(dataArr[0], data.replace(dataArr[0], "").trim().replaceAll("\"", ""));
                }
            }
        }
    }

    private ApacheAssessmentResult.Instance getInclude(Map<String, String> defineMap, ApacheAssessmentResult.Engine engine,
                                                       ApacheAssessmentResult.Instance instance, String instancePath,
                                                       ApacheAssessmentResult.General general,
                                                       ApacheAssessmentResult.SolutionSpecific solutionSpecific,
                                                       List<String> wasConfigFile, TargetHost targetHost, MiddlewareInventory middleware, ClassMember member, GetInfoStrategy strategy) throws InterruptedException {
        String separator = strategy.getSeparator();
        log.debug(":+:+:+:+:+:+:+: getInclude() :+:+:+:+:+:+:+:");
        if (instance.getIncludeFiles() != null) {
            for (String includeFile : instance.getIncludeFiles()) {
                log.debug("include files : [{}]", includeFile);

                if (!includeFile.contains("*")) {

                    String catResult = null;
                    if (AbstractMiddlewareAssessment.fileExists(targetHost, includeFile, commandConfig, strategy)) {
//                        String catCmd = "sudo cat " + includeFile;
//                        catResult = SSHUtil.executeCommand(targetHost, catCmd);
                        catResult = getCatResult(targetHost, strategy, includeFile);
                    }

                    if (StringUtils.isNotEmpty(catResult)) {
                        // set config file
                        ApacheAssessmentResult.ConfigFile configFile = new ApacheAssessmentResult.ConfigFile();
                        configFile.setPath(includeFile);
                        configFile.setContent(catResult);
                        instance.getConfigFiles().add(configFile);

                        // get Directive (Define, Listen, etc)
                        getDirective(engine, catResult, instance, instancePath, general, solutionSpecific, wasConfigFile, false, member, strategy);

                        // get Enclosure (IfModule, VirtualHost, Directory, etc)
                        getEnclosure(defineMap, catResult, instance, strategy);

                        String ipAddress = targetHost.getIpAddress();

                        if (!StringUtils.isEmpty(catResult)) {
                            // 읽어들인 파일 저장
                            log.debug("save File path : [{}]", includeFile);
                            WasAnalyzerUtil.saveAssessmentFile(ipAddress, includeFile, catResult, CommonProperties.getWorkDir(), strategy);
                        }
                    }
                } else {
                    // include 파일에 *.conf와 같이 읽어오면 해당 파일을 조회해서 저장
                    String filePath = null;
                    String substring = includeFile.substring(0, includeFile.lastIndexOf(separator));
                    if (includeFile.startsWith(separator)) {
                        filePath = instancePath + substring;
                    } else {
                        filePath = instancePath + separator + substring;
                    }

                    // ls -l *.conf | awk '{print $9}'
//                    String lsCommand = "sudo ls -l " + filePath + "/*.conf | awk '{print $9}'";
//                    String lsResult = SSHUtil.executeCommand(targetHost, lsCommand);
                    String allConf = filePath + separator + "*.conf";
                    String command = COMMAND.LS_FILES.command(commandConfig, strategy.isWindows(), allConf, allConf);
                    String lsResult = strategy.executeCommand(targetHost, command, COMMAND.LS_FILES);

                    String[] files = lsResult.split("\\s+");

                    if (files.length > 0) {
//                        List<String> newIncludeFile = new ArrayList<>();

                        for (String file : files) {
                            String catResult = null;
                            if (AbstractMiddlewareAssessment.fileExists(targetHost, file, commandConfig, strategy)) {
//                                String catCmd = "sudo cat " + file;
//                                catResult = SSHUtil.executeCommand(targetHost, catCmd);
                                catResult = getCatResult(targetHost, strategy, file);
                            }

                            if (catResult != null && catResult.length() > 0) {
                                // set config file
                                ApacheAssessmentResult.ConfigFile configFile = new ApacheAssessmentResult.ConfigFile();
                                configFile.setPath(file);
                                configFile.setContent(catResult);
                                instance.getConfigFiles().add(configFile);

                                // add include file
//                                newIncludeFile.add(file);

                                // get Directive (Define, Listen, etc)
                                getDirective(engine, catResult, instance, instancePath, general, solutionSpecific, wasConfigFile, false, member, strategy);

                                // get Enclosure (IfModule, VirtualHost, Directory, etc)
                                getEnclosure(defineMap, catResult, instance, strategy);

                                String ipAddress = targetHost.getIpAddress();

                                if (!StringUtils.isEmpty(catResult)) {
                                    // 읽어들인 파일 저장
                                    WasAnalyzerUtil.saveAssessmentFile(ipAddress, file, catResult, CommonProperties.getWorkDir(), strategy);
                                }
                            }
                        }

//                        if (instance.getIncludeFiles() != null) {
//                            log.debug("aaaaa");
//                            instance.getIncludeFiles().addAll(newIncludeFile);
//                        } else {
//                            log.debug("bbbbb");
//                            instance.setIncludeFiles(newIncludeFile);
//                        }
                    }
                }
            }
            // was config 파일을 추가한다.
            if (wasConfigFile != null && wasConfigFile.size() > 0) {
                for (String wasFile : wasConfigFile) {
//                    String catCmd = "sudo cat " + wasFile;
//                    String catResult = SSHUtil.executeCommand(targetHost, catCmd);
                    String catResult = getCatResult(targetHost, strategy, wasFile);

                    if (catResult.length() > 0) {
                        ApacheAssessmentResult.ConfigFile configFile = new ApacheAssessmentResult.ConfigFile();
                        configFile.setPath(wasFile);
                        configFile.setContent(catResult);
                        instance.getConfigFiles().add(configFile);

                        String ipAddress = targetHost.getIpAddress();
                        if (!StringUtils.isEmpty(catResult)) {
                            // 읽어들인 파일 저장
                            WasAnalyzerUtil.saveAssessmentFile(ipAddress, wasFile, catResult, CommonProperties.getWorkDir(), strategy);
                        }
                    }
                }
            }
        }

        return instance;
    }

    private String getCatResult(TargetHost targetHost, GetInfoStrategy strategy, String fileName) throws InterruptedException {
        String command = COMMAND.CAT_QUOTATION.command(commandConfig, strategy.isWindows(), fileName);
        return strategy.executeCommand(targetHost, command, COMMAND.CAT_QUOTATION);
    }

    private void getEnclosure(Map<String, String> defineMap, String file, ApacheAssessmentResult.Instance instance, GetInfoStrategy strategy) throws InterruptedException {
        log.debug(":+:+:+:+:+:+:+: getEnclosure() :+:+:+:+:+:+:+:");
        CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(file.getBytes())))) {
            String lineStr = null;
            Stack enclosureStak = new Stack();
            Stack virtualHostStack = new Stack();
            Stack virtualHostServerNameStack = new Stack();
            StringBuffer sb = new StringBuffer();

            String type = null;
            String key = null;
            while ((lineStr = buffer.readLine()) != null) {
                lineStr = lineStr.trim();
                if (StringUtils.isEmpty(lineStr) || lineStr.startsWith("#"))
                    continue;

                if (ApacheHelper.isEnclosureTypeMatch(lineStr)) {
//                    String[] lineArray = lineStr.split("\\s+");
                    String[] lineArray = parser.parseLine(lineStr);
                    if (lineArray.length > 2) {
                        key = lineArray[1] + StringUtils.SPACE + StringUtils.strip(lineArray[2], "<>\"");
                    } else {
                        key = StringUtils.strip(lineArray[1], "<>\"");
                    }

                    type = StringUtils.strip(lineArray[0], "<>\"");
                    Map<String, List<String>> stackMap = new HashMap<>();
                    stackMap.put(type + "," + key, new ArrayList<>());
                    enclosureStak.push(stackMap);

                    if (type.contains("VirtualHost")) {
                        virtualHostStack.push(type);
                    }

                    if (!virtualHostStack.isEmpty()) {
                        sb.append(lineStr + strategy.getCarriageReturn());
                    }
                }

                if (!enclosureStak.isEmpty()) {
                    if (!ApacheHelper.isCommentMatch(lineStr) && !lineStr.equals("")) {
                        if (!ApacheHelper.isEnclosureTypeMatch(lineStr) && !ApacheHelper.isCloseEnclosureTypeMatch(lineStr)) {

                            // 각 Stack 에 등록된 item 내용 추가
                            Map<String, List<String>> map = (Map<String, List<String>>) enclosureStak.peek();
                            String mapKey = null;
                            for (String k : map.keySet()) {
                                mapKey = k;
                            }

                            List<String> line = map.get(mapKey);
                            if (virtualHostStack.isEmpty()) {
                                if (!StringUtils.isEmpty(lineStr)) {
                                    line.add(lineStr);
                                }
                            } else {
                                if (lineStr.toUpperCase().startsWith("SERVERNAME")) {
                                    virtualHostServerNameStack.push(lineStr.split("\\s+")[1]);
                                    if (mapKey.contains("VirtualHost")) {
                                        sb.append(lineStr + ":" + mapKey.split(":")[1] + "\n");
                                    } else {
                                        sb.append(lineStr + strategy.getCarriageReturn());
                                        line.add(lineStr);
                                    }
                                } else {
                                    sb.append(lineStr + strategy.getCarriageReturn());
                                    line.add(lineStr);
                                }
                            }
                        }
                    }

                    if (!ApacheHelper.isCommentMatch(lineStr) && ApacheHelper.isCloseEnclosureTypeMatch(lineStr)) {
                        Map<String, List<String>> map = (Map<String, List<String>>) enclosureStak.pop();

                        if (!virtualHostStack.isEmpty() && (lineStr.startsWith("</VirtualHost") || lineStr.startsWith("</Proxy"))) {
                            String enclosureType = null;

                            // Virtual Host의 내용은 전체를 가져와서 설정한다.
                            if (lineStr.startsWith("</VirtualHost")) {
                                sb.append(lineStr);
                                String keyName = virtualHostServerNameStack.isEmpty() ? "*" : (String) virtualHostServerNameStack.pop();
                                List<String> virtualHostContents = new ArrayList<>();
                                virtualHostContents.add(sb.toString());

                                enclosureType = (String) virtualHostStack.pop();
                                // instance 등록
                                ApacheHelper.replaceEnclosureToInstance(defineMap, enclosureType, keyName, virtualHostContents, instance);
                                sb = new StringBuffer();
                            } else {
                                sb.append(lineStr + strategy.getCarriageReturn());
                            }
                        } else {
                            String mapKey = null;
                            for (String k : map.keySet()) {
                                mapKey = k;
                            }
                            String enclosureType = mapKey.split(",")[0];
                            String keyName = mapKey.split(",")[1];

                            // instance 등록
                            ApacheHelper.replaceEnclosureToInstance(defineMap, enclosureType, keyName, map.get(mapKey), instance);
                        }
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception occurred while getEnclosure(). [Reason] : " + e);
            ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while getEnclosure(). Detail : [" + e.getMessage() + "]");
        }
    }

    private Map<String, String> getDirective(ApacheAssessmentResult.Engine engine, String file,
                                             ApacheAssessmentResult.Instance instance, String instancePath,
                                             ApacheAssessmentResult.General general,
                                             ApacheAssessmentResult.SolutionSpecific solutionSpecific,
                                             List<String> wasConfigFile, boolean isRoot, ClassMember member, GetInfoStrategy strategy) throws InterruptedException {
        String separator = strategy.getSeparator();
        log.debug(":+:+:+:+:+:+:+: getDirective() :+:+:+:+:+:+:+:");
        Map<String, String> defineMap = new HashMap<>();
        List<Integer> listenList = new ArrayList<>();
        List<String> includeList = new ArrayList<>();
        List<ApacheAssessmentResult.LoadModule> moduleList = new ArrayList<>();
        List<String> logLevelList = new ArrayList<>();
        List<String> errorLogList = new ArrayList<>();
        List<String> logFormatList = new ArrayList<>();
        List<String> customLogList = new ArrayList<>();
        Map<String, String> keepAliveMap = new HashMap<>();
        Map<String, String> errorDocumentMap = new HashMap<>();
        Map<String, String> browserMatchMap = new HashMap<>();
        Map<String, String> documentRootMap = new HashMap<>();
        Map<String, List<String>> logFormatMap = new HashMap<>();
        List<String> envList = new ArrayList<>();

        CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(file.getBytes())))) {
            String lineStr = null;

            while ((lineStr = buffer.readLine()) != null) {
                lineStr = lineStr.trim();
                if (StringUtils.isEmpty(lineStr) || lineStr.startsWith("#"))
                    continue;

                // Define
                if (lineStr.startsWith(define)) {
                    String[] defineArray = lineStr.split("\\s+");
                    // log.debug("define : [{}]", lineStr);
                    if (defineArray.length > 2) {
                        defineMap.put(defineArray[1], ApacheHelper.replaceDefinesInString(defineMap, defineArray[2].replaceAll("\"", StringUtils.EMPTY)));
                    }
                }

                // Listen
                if (lineStr.startsWith(listen) || lineStr.startsWith("server.port")) {
                    if (lineStr.startsWith(listen)) {
                        String[] listenArray = lineStr.split("\\s+");
                        // log.debug("listen : [{}]", lineStr);
                        listenList.add(listenArray[1].contains(":") ? Integer.parseInt(listenArray[1].split(":")[1]) : Integer.parseInt(listenArray[1]));
                    } else {
                        String[] listenArray = lineStr.split("=");
                        listenList.add(Integer.parseInt(listenArray[1].trim()));
                    }
                }
                if (lineStr.contains("<VirtualHost")) {
                    String[] listenArry = lineStr.split("[<:>]");
                    if (listenArry.length > 0) {
                        try {
                            listenList.add(Integer.parseInt(listenArry[listenArry.length - 1]));
                        } catch (NumberFormatException ignored) {
                            log.debug("VirtualHost port parsing error: {}", lineStr);
                        }
                    }
                }


                // LoadModule
                if (lineStr.startsWith(loadModule)) {
                    String[] moduleArray = lineStr.split("\\s+");
                    // log.debug("loadModule : [{}], value : [{}]", moduleArray[1], moduleArray[2]);
                    ApacheAssessmentResult.LoadModule loadModule = new ApacheAssessmentResult.LoadModule();

                    loadModule.setName(moduleArray[1]);
                    if (instance.getDefine() != null) {
                        loadModule.setLocation(ApacheHelper.replaceDefinesInString(defineMap.size() > 0 ? defineMap : instance.getDefine(), moduleArray[2].replaceAll("\"", "")));
                    } else {
                        loadModule.setLocation(moduleArray[2].replaceAll("\"", ""));
                    }
                    moduleList.add(loadModule);
                }

                // was config files
                if (lineStr.startsWith(webSpherePluginConfig) ||
                        lineStr.startsWith(jkWorkersFile) || lineStr.startsWith(jkMountFile)) {

                    if (instance.getDefine() != null) {
                        wasConfigFile.add(ApacheHelper.replaceDefinesInString(defineMap.size() > 0 ? defineMap : instance.getDefine(), lineStr.split("\\s+")[1]).replaceAll("\"", ""));
                    } else {
                        wasConfigFile.add(lineStr.split("\\s+")[1].replaceAll("\"", ""));
                    }
                }

                // SSLEngine
                if (lineStr.startsWith(sslEngine)) {
                    // log.debug("sslEngine : [{}]", lineStr);
                    String[] sslEngineArray = lineStr.split("\\s+");
                    if ("on".equals(sslEngineArray[1])) {
                        member.setSslEngineUsed(true);
                    }
                }

                // SSLCertificate
                if (lineStr.startsWith(sslCertificate)) {
                    // log.debug("sslCertificate : [{}]", lineStr);
                    member.setSslCertificateUsed(true);
                }

                // Include or include
                if (engine.getName() != null) {
                    if (lineStr.length() >= include.length() && lineStr.substring(0, include.length()).equalsIgnoreCase(include)) {
                        String[] includeArray = lineStr.split("\\s+");
                        String replaceDefine = replaceDefinesInString(defineMap, includeArray[1]);
                        if (engine.getName().contains("Oracle")) { // Oracle HTTP Server의 경우 include 로 시작 (path도 제외 되어져 있는 것으로 확인)
                            int index = instancePath.lastIndexOf(separator);
                            String path = instancePath;
                            if (index > -1) {
                                path = instancePath.substring(0, index);
                            }
                            includeList.add(path + separator + replaceDefine);
                        } else {
                            includeList.add(replaceDefine);
                        }
                        log.debug("Include : [{}]", replaceDefine);
                    }
                }

                // ServerRoot
                if (lineStr.startsWith(serverRoot)) {
                    // log.debug("serverRoot : [{}]", lineStr);
//                    String[] serverRootArray = lineStr.split("\\s+");
                    String[] serverRootArray = parser.parseLine(lineStr);
                    // instance.setServerRoot(ApacheHelper.replaceDefinesInString(defineMap, serverRootArray[1]));

                    if (general.getServerRoot() == null) {
                        general.setServerRoot(ApacheHelper.replaceDefinesInString(defineMap, serverRootArray[1].replaceAll("\"", "")));
                    }
                }

                if (lineStr.startsWith(documentRoot)) {
                    // log.debug("documentRoot : [{}]", lineStr);
//                    String[] documentRootArray = lineStr.split("\\s+");
                    String[] documentRootArray = parser.parseLine(lineStr);
                    String documentRoot = ApacheHelper.replaceDefinesInString(defineMap, documentRootArray[1].replaceAll("\"", ""));

                    documentRootMap.put("Directory", documentRoot);
                    if (general.getDocumentRoot() == null) {
                        general.setDocumentRoot(documentRoot);
                    }
                }

                if (lineStr.startsWith(setEnvIf)) {
                    // log.debug("setEnv : [{}]", lineStr);
                    String[] envArray = lineStr.split("\\s+");
                    String env = lineStr.replace(envArray[0], "").trim();

                    if (general.getEnv() != null) {
                        general.getEnv().add(env);
                    } else {
                        envList.add(env);
                        general.setEnv(envList);
                    }
                }

                // Keep Alive
                if (lineStr.startsWith(keepAlive) || lineStr.startsWith(maxKeepAliveRequests)) {
                    // log.debug("keepAlive : [{}]", lineStr);
                    String[] keepAliveArray = lineStr.split("\\s+");
                    keepAliveMap.put(keepAliveArray[0], keepAliveArray[1]);
                }

                // Solution Specific
                if (lineStr.startsWith(useCanonicalName) || lineStr.startsWith(serverTokens) || lineStr.startsWith(traceEnable) ||
                        lineStr.startsWith(hostnameLookups) || lineStr.startsWith(user) || lineStr.startsWith(group)
                        || lineStr.startsWith(serverAdmin) || lineStr.startsWith(serverSignature)) {
                    // log.debug("useCanonicalName : [{}]", lineStr);

                    if (lineStr.startsWith(useCanonicalName)) {
                        solutionSpecific.setUseCanonicalName(lineStr.split("\\s+")[1]);
                    } else if (lineStr.startsWith(serverTokens)) {
                        solutionSpecific.setServerTokens(lineStr.split("\\s+")[1]);
                    } else if (lineStr.startsWith(traceEnable)) {
                        solutionSpecific.setTraceEnable(lineStr.split("\\s+")[1]);
                    } else if (lineStr.startsWith(hostnameLookups)) {
                        solutionSpecific.setHostnameLookups(lineStr.split("\\s+")[1]);
                    } else if (lineStr.startsWith(user)) {
                        if (user.equals(lineStr.split("\\s+")[0])) {
                            solutionSpecific.setUser(lineStr.split("\\s+")[1]);
                        }
                    } else if (lineStr.startsWith(group)) {
                        if (group.equals(lineStr.split("\\s+")[0])) {
                            solutionSpecific.setGroup(lineStr.split("\\s+")[1]);
                        }
                    } else if (lineStr.startsWith(serverAdmin)) {
                        solutionSpecific.setServerAdmin(lineStr.split("\\s+")[1]);
                    } else if (lineStr.startsWith(serverSignature)) {
                        solutionSpecific.setServerSignature(lineStr.split("\\s+")[1]);
                    }
                }

                // Log Level & ErrorLog & Log Format & Custom Log (루트 파일일 경우에만 수행)
                if (isRoot) {
                    if (lineStr.startsWith(logLevel) || lineStr.startsWith(errorLog) ||
                            lineStr.startsWith(logFormat) || lineStr.startsWith(customLog)) {
                        // log.debug("logFormat : [{}]", lineStr);

                        if (lineStr.startsWith(logLevel)) {
                            logLevelList.add(ApacheHelper.replaceDefinesInString(defineMap, lineStr.replaceAll(logLevel, "").trim()));
                        } else if (lineStr.startsWith(errorLog)) {
                            errorLogList.add(ApacheHelper.replaceDefinesInString(defineMap, lineStr.replaceAll(errorLog, "").trim()));
                        } else if (lineStr.startsWith(logFormat)) {
                            logFormatList.add(ApacheHelper.replaceDefinesInString(defineMap, lineStr.replaceAll(logFormat, "").trim()));
                        } else if (lineStr.startsWith(customLog)) {
                            customLogList.add(ApacheHelper.replaceDefinesInString(defineMap, lineStr.replaceAll(customLog, "").trim()));
                        }
                    }
                }

                // Error Documents
                if (lineStr.startsWith(errorDocument)) {
                    // log.debug("errorDocument : [{}]", lineStr);
                    String[] errorDocArr = lineStr.replaceAll(errorDocument, "").trim().split("\\s+");
                    errorDocumentMap.put(errorDocArr[0], errorDocArr[1]);
                }

                // Browser Matches
                if (lineStr.startsWith(browserMatches)) {
                    // log.debug("errorDocument : [{}]", lineStr);
                    String[] browserMatchArray = lineStr.replaceAll(browserMatches, "").trim().split("\\s+");
                    browserMatchMap.put(browserMatchArray[0], browserMatchArray[1]);
                }
            }

            // 지시문의 데이터를 찾아 instance data 에 등록해준다.
            if (instance.getDefine() != null) {
                if (defineMap.size() > 0) {
                    instance.getDefine().putAll(defineMap);
                }
            } else {
                instance.setDefine(defineMap);
            }
            if (instance.getModules() != null) {
                if (moduleList.size() > 0) {
                    instance.getModules().addAll(moduleList);
                }
            } else {
                instance.setModules(moduleList);
            }

            if (general.getListenPort() != null && general.getListenPort().size() > 0) {
                if (listenList.size() > 0) {
                    general.getListenPort().addAll(listenList);
                }

                // 중복 제거
                Set<Integer> set = new HashSet<>(general.getListenPort());
                general.setListenPort(new ArrayList<>(set));
            } else {
                general.setListenPort(listenList);
            }

            if (isRoot) {
                if (instance.getIncludeFiles() != null) {
                    if (includeList.size() > 0) {
                        instance.getIncludeFiles().addAll(includeList);
                    }
                } else {
                    instance.setIncludeFiles(includeList);
                }
            }

            if (instance.getKeepAlive() != null) {
                if (keepAliveMap.size() > 0) {
                    instance.getKeepAlive().putAll(keepAliveMap);
                }
            } else {
                instance.setKeepAlive(keepAliveMap);
            }

            if (instance.getSolutionSpecific() == null) {
                instance.setSolutionSpecific(solutionSpecific);
            }

            if (instance.getDocumentRoot() != null) {
                if (documentRootMap.size() > 0) {
                    instance.getDocumentRoot().putAll(documentRootMap);
                }
            } else {
                instance.setDocumentRoot(documentRootMap);
            }

            if (instance.getLogFormat() != null) {
                if (instance.getLogFormat().get("LogLevel") != null) {
                    if (logLevelList.size() > 0) {
                        instance.getLogFormat().get("LogLevel").addAll(logLevelList);
                    }
                }
                if (instance.getLogFormat().get("ErrorLog") != null) {
                    if (logFormatList.size() > 0) {
                        instance.getLogFormat().get("ErrorLog").addAll(errorLogList);
                    }
                }
                if (instance.getLogFormat().get("LogFormat") != null) {
                    if (logFormatList.size() > 0) {
                        instance.getLogFormat().get("LogFormat").addAll(logFormatList);
                    }
                }
                if (instance.getLogFormat().get("CustomLog") != null) {
                    if (customLogList.size() > 0) {
                        instance.getLogFormat().get("CustomLog").addAll(logFormatList);
                    }
                }
            } else {
                logFormatMap.put("LogLevel", logLevelList);
                logFormatMap.put("ErrorLog", errorLogList);
                logFormatMap.put("LogFormat", logFormatList);
                logFormatMap.put("CustomLog", customLogList);

                instance.setLogFormat(logFormatMap);
            }

            if (instance.getErrorDocuments() != null) {
                if (errorDocumentMap.size() > 0) {
                    instance.getErrorDocuments().putAll(errorDocumentMap);
                }
            } else {
                instance.setErrorDocuments(errorDocumentMap);
            }

            if (instance.getBrowserMatches() != null) {
                if (browserMatchMap.size() > 0) {
                    instance.getBrowserMatches().putAll(browserMatchMap);
                }
            } else {
                instance.setBrowserMatches(browserMatchMap);
            }

            // if (instance.getGeneral() != null) {
            //     instance.setGeneral(general);
            // }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception occurred while getDirective(). [Reason] : {}", e.getMessage(), e);
            ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while getDirective(). Detail : [" + e.getMessage() + "]");
        }

        return defineMap;
    }

    private ApacheAssessmentResult.Engine getEngineMap(TargetHost targetHost, String enginePath, ClassMember member, GetInfoStrategy strategy) throws InterruptedException {
        String separator = strategy.getSeparator();

        log.debug(":+:+:+:+:+:+:+: getEngineMap() :+:+:+:+:+:+:+:");
        ApacheAssessmentResult.Engine engine = new ApacheAssessmentResult.Engine();
        String httpdParentDir = member.getHttpdParentDir();

        String httpdExecFilePath = enginePath + httpdParentDir + separator + "httpd" + (strategy.isWindows() ? ".exe" : StringUtils.EMPTY);
        if (AbstractMiddlewareAssessment.fileExists(targetHost, httpdExecFilePath, commandConfig, strategy)
                || AbstractMiddlewareAssessment.fileExists(targetHost, enginePath + httpdParentDir + separator + "apachectl", commandConfig, strategy)
                || AbstractMiddlewareAssessment.fileExists(targetHost, enginePath + httpdParentDir + separator + "lighttpd", commandConfig, strategy)) {

//            String versionCommand = "sudo " + enginePath + httpdParentDir + separator + "httpd -v";
//            String versionResult = SSHUtil.executeCommand(targetHost, versionCommand);
            String versionCommand = enginePath + httpdParentDir + separator + "httpd";

            if (AbstractMiddlewareAssessment.fileExists(targetHost, enginePath + httpdParentDir + separator + "lighttpd", commandConfig, strategy)) {
                versionCommand = enginePath + httpdParentDir + separator + "lighttpd";
            }

            String versionResult = getVersion(COMMAND.APACHE_VERSION, strategy, versionCommand, targetHost);

            if (StringUtils.isEmpty(versionResult) || versionResult.contains("error") || versionResult.contains("command not found")) {
                versionCommand = enginePath + httpdParentDir + separator + "apachectl -v";
                versionResult = getVersion(COMMAND.APACHE_VERSION, strategy, versionCommand, targetHost);
            }
            log.debug("versionResult : [{}]", versionResult);

            String[] versionArray = null;

            if (StringUtils.isNotEmpty(versionResult) && versionResult.length() > 0 &&
                    !versionResult.contains("error") && !versionResult.contains("command not found")) {
                versionArray = versionResult.split(strategy.getCarriageReturn());
                // String value "Server version: Apache/2.4.37 (Unix)" split
                String version = versionArray[0].split("\\s+")[2];

                if (versionArray[0].startsWith("lighttpd")) {
                    // lighttpd/1.4.54 (ssl) - a light and fast webserver
                    version = versionArray[0].split("\\s+")[0];
                }

                // String value "Apache/2.4.37" split
                engine.setName(version.split("/")[0]);
                engine.setVersion(version.split("/")[1]);
            } else {
                log.debug("Apache Httpd Analyzer > getEngineMap(). : [{}] command result is empty or invalid. Please check the valid (httpd or apachectl) command.", versionCommand);
            }
        } else {
            if (!httpdParentDir.equals(separator + "sbin")) {
                member.setHttpdParentDir(separator + "sbin");
                engine = getEngineMap(targetHost, enginePath, member, strategy);
            } else {
                log.debug("Apache Httpd Analyzer > getEngineMap(). : file(http or apachectl) does not exists. Please check the valid (httpd or apachectl) command.");
            }
        }
        engine.setPath(enginePath);

        return engine;
    }

    private String getVersion(COMMAND apacheVersion, GetInfoStrategy strategy, String versionCommand, TargetHost targetHost) throws InterruptedException {
        String command = apacheVersion.command(commandConfig, strategy.isWindows(), versionCommand);
        return AbstractMiddlewareAssessment.getSshCommandResultTrim(targetHost, command, apacheVersion, strategy);
    }
}
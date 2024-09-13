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
package io.playce.roro.mw.asmt.weblogic;

import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.AbstractMiddlewareAssessment;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.weblogic.dto.WebLogicAssessmentResult;
import io.playce.roro.mw.asmt.weblogic.helper.WebLogicHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static io.playce.roro.common.util.JsonUtil.getJsonObject;
import static io.playce.roro.mw.asmt.util.MWCommonUtil.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
@Component("WEBLOGICAssessment")
@RequiredArgsConstructor
public class WebLogicAssessment extends AbstractMiddlewareAssessment {
    private final CommandConfig commandConfig;

    private final WebLogicHelper webLogicHelper;

    @Override
    public MiddlewareAssessmentResult assessment(TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        log.debug("\n\n:+:+:+:+ Start Weblogic Assessment :+:+:+:+");
        /*
         * 1. config/config.xml 탐색
         * 2. config.xml 파싱
         *    -
         */
        WebLogicAssessmentResult assessmentResult = new WebLogicAssessmentResult();

        // Middleware Domain Home 값의 유무에 따라 처리
        String domainHome = null;
        String configFilePath = null;
        String configXmlFile;
        boolean isExistFile = false;
        String separator = strategy.getSeparator();

/*
        // 프로세스 체크
//        String psCommand = "sudo ps -ef | grep java | grep weblogic.Server | grep weblogic.Name=" + middleware.getProcessName() + " | grep -v grep | awk {'print $2\" \"$3'}";
        String psCommand = COMMAND.WEBLOGIC_CHECK_PROCESS.command(commandConfig, strategy.isWindows(), middleware.getProcessName());
        String psResult = getSshCommandResultTrim(targetHost, psCommand, COMMAND.WEBLOGIC_CHECK_PROCESS, strategy);

        String[] processIds = null;
        if (StringUtils.isNotEmpty(psResult)) {
            processIds = psResult.split("\\s+");
        }
*/

        if (StringUtils.isNotEmpty(middleware.getDomainHomePath())) {
            /*
             * 1. Domain Home 추출
             *
             * middleware.getDomainHomePath()의 값이 있고
             * middleware.getDomainHomePath()/config/config.xml 파일이 있으면 해당 값을 사용한다.
             */
            domainHome = middleware.getDomainHomePath();

            if (fileExists(targetHost, domainHome + separator + "config" + separator + "config.xml", commandConfig, strategy)) {
                isExistFile = true;
                configFilePath = domainHome + separator + "config";
            }
            /*        } else {
             *//*
             * 2. 부모 프로세스 확인 & 도메인 홈 확인
             *
             * 1) ps -ef | grep java | grep weblogic.Server | grep weblogic.Name={MIDDLEWARE_NAME} | grep -v grep | awk {'print $2" "$3'}로 프로세스 ID 확인
             *    ps -ef | grep ${PARENT_PID} | grep -v ${PID} |  grep -v grep | awk {'print $9'} 를 실행하여 startWebLogic.sh 파일의 위치를 확인한다.
             *    (/bin/startWebLogic.sh 앞의 경로가 DOMAIN_HOME이 된다.)
             *
             * 2) java Options 중, Dweblogic.system.BootIdentityFile 의 데이터를 가져와서 ../domains/{SERVER} 까지 도메인으로 간주한다.
             *    e.g) -Dweblogic.system.BootIdentityFile=/app/weblogic/wls12214/user_projects/domains/wl_server/boot.properties
             *
             * 3) 프로세스에서 가져오지 못할경우, 'proc/${PID}/cwd' Current Working Directory 에서 링크로 걸려있는 path를 가져와서 설정해준다.
             *    command : sudo readlink -f /proc/${PID}/cwd 실행
             *//*
            if (processIds != null && processIds.length == 2) {
//                String domainCommand = "sudo ps -ef | grep " + processIds[1] + " | grep -v " + processIds[0] + " | grep -v grep | awk {'print $9'}";
                String domainCommand = COMMAND.WEBLOGIC_DOMAIN.command(commandConfig, strategy.isWindows(), processIds[1], processIds[0], "$9");
                String homeResult = getSshCommandResultTrim(targetHost, domainCommand, COMMAND.WEBLOGIC_DOMAIN, strategy);

//                if (homeResult.trim().equals("/bin/sh")) {
                if (homeResult.trim().equals(strategy.getShell())) {
//                    domainCommand = "sudo ps -ef | grep " + processIds[1] + " | grep -v " + processIds[0] + " | grep -v grep | awk {'print $10'}";
                    domainCommand = COMMAND.WEBLOGIC_DOMAIN.command(commandConfig, strategy.isWindows(), processIds[1], processIds[0], "$10");
                    homeResult = getSshCommandResultTrim(targetHost, domainCommand, COMMAND.WEBLOGIC_DOMAIN, strategy);
                }

                // /bin/startWebLogic.sh 인덱스 위치 조회
//                int index = homeResult.indexOf("/bin/startWebLogic.sh");
                int index = homeResult.indexOf(strategy.getWeblogicShellPath());
                domainHome = homeResult.substring(0, index);

                if (StringUtils.isNotEmpty(domainHome) && domainHome.startsWith(".")) {
//                    String processCommand = "sudo ps -ef | grep weblogic.Server | grep weblogic.Name=" + middleware.getProcessName();
                    String processCommand = COMMAND.WEBLOGIC_PROCESS_NAME.command(commandConfig, strategy.isWindows(), middleware.getProcessName());
                    String bootPropertyResult = getSshCommandResultTrim(targetHost, processCommand, COMMAND.WEBLOGIC_PROCESS_NAME, strategy);

                    String bootPath = null;
                    for (String bootProp : bootPropertyResult.split("\\s+")) {
                        if (bootProp.startsWith("-Dweblogic.system.BootIdentityFile")) {
                            String[] bootArr = bootProp.split("=");

                            if (StringUtils.isNotEmpty(bootArr[1])) {
                                index = bootArr[1].lastIndexOf("domains" + File.separator);
                                String domain = bootArr[1].substring(index);
                                bootPath = bootArr[1].substring(0, index) + domain.substring(0, domain.indexOf(File.separator, domain.indexOf(File.separator) + 1));
                                domainHome = bootPath.trim();
                            }
                        }
                    }

                    if (bootPath == null) {
                        // boot path 가 null인 경우, 프로세스의 링크의 domain home 정보를 가져온다.
//                        processCommand = "sudo readlink -f /proc/" + processIds[0] + "/cwd";
                        processCommand = COMMAND.WEBLOGIC_PROCESS_LINK.command(commandConfig, strategy.isWindows(), processIds[0]);
                        String processResult = getSshCommandResultTrim(targetHost, processCommand, COMMAND.WEBLOGIC_PROCESS_LINK, strategy);
                        domainHome = processResult.trim();
                    }
                }
            }

            if (StringUtils.isNotEmpty(domainHome) && fileExists(targetHost, domainHome + File.separator + "config" + File.separator + "config.xml", commandConfig, strategy)) {
                isExistFile = true;
                configFilePath = domainHome + File.separator + "config";
                middleware.setDomainHomePath(domainHome);
//                middlewareRepository.save(middleware);
            } else {
                log.debug("Please check the [Process Running] and files. [" + domainHome + File.separator + "config" + File.separator + "config.xml]");
            }*/
        }

//        log.debug(":+:+:+:+:+:+:+: process Result : [{}]", psResult);
        log.debug(":+:+:+:+:+:+:+: domain Home : [{}]", domainHome);
        log.debug(":+:+:+:+:+:+:+: config file Path : [{}]", configFilePath);

        /*
         * {DOMAIN_HOME}/config/config.xml 파일을 읽어서 필요한 정보 추출
         */
        if (isExistFile) {
            configXmlFile = getFileContents(targetHost, configFilePath + separator + "config.xml", commandConfig, strategy);

            // attribute (xsi:nil="true", i:nil="true") 제거
            configXmlFile = configXmlFile.replaceAll("xsi:nil=\"true\"", "");
            configXmlFile = configXmlFile.replaceAll("i:nil=\"true\"", "");
            String xmlJsonStr = String.valueOf(XML.toJSONObject(configXmlFile));

            // XML to simple.JSONObject
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) new JSONParser().parse(xmlJsonStr);
            } catch (ParseException e) {
                throw new RoRoException(e);
            }
            org.json.simple.JSONObject domain = getJsonObject((org.json.simple.JSONObject) jsonObject.get("domain"));
            log.debug(":+:+:+:+:+:+:+: domain : [{}]", domain);

            // get Engine info
            WebLogicAssessmentResult.Engine engine;
            engine = webLogicHelper.getEngineMap(targetHost, middleware, domain, strategy);

            log.debug(":+:+:+:+:+:+:+: Engine : [{}]", engine.toString());

            // get Instance info
            WebLogicAssessmentResult.Instance instance = new WebLogicAssessmentResult.Instance();
            instance.setDomainHome(domainHome);
//            if (processIds != null && processIds.length == 2) {
//                instance.setPid(StringUtils.isNotEmpty(processIds[0]) ? Long.parseLong(processIds[0]) : null);
//                instance.setParentPid(StringUtils.isNotEmpty(processIds[1]) ? Long.parseLong(processIds[1]) : null);
//            }

            // get config files
            List<WebLogicAssessmentResult.ConfigFile> configFiles = webLogicHelper.getConfigFiles(targetHost, configFilePath, domain, strategy);
            instance.setConfigFiles(configFiles);

            // config 파일 save
            if (configFiles != null && !configFiles.isEmpty()) {
                for (WebLogicAssessmentResult.ConfigFile configFile : configFiles) {
                    saveMiddlewareFile(targetHost.getIpAddress(), configFile.getPath(), configFile.getContents(), strategy);
                }
            }

            // get applications
            List<WebLogicAssessmentResult.Application> applications = webLogicHelper.getApplications(middleware, domain, strategy);
            instance.setApplication(applications);

            // get servers
            List<WebLogicAssessmentResult.Instances> server = webLogicHelper.getServer(targetHost, configFilePath, domain, strategy);
            instance.setInstances(server);

            // get cluster
            WebLogicAssessmentResult.Cluster cluster = webLogicHelper.getCluster(domain);
            instance.setCluster(cluster);

            // get resources
            WebLogicAssessmentResult.Resource resources = webLogicHelper.getResources(targetHost, configFilePath, domain, strategy);
            instance.setResource(resources);

            // java version
            Optional<WebLogicAssessmentResult.Instances> first = instance.getInstances().stream().filter(i -> StringUtils.isNotEmpty(i.getRunUser())).findFirst();
            if (first.isPresent()) {
                WebLogicAssessmentResult.Instances firstInstance = first.get();
                String javaVersion = getJavaVersion(targetHost, "weblogic.Name=" + firstInstance.getName(), commandConfig, strategy);
                instance.setJavaVersion(javaVersion);

                String javaVendor = getJavaVendor(targetHost, "weblogic.Name=" + firstInstance.getName(), commandConfig, strategy);
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
            log.debug(":+:+:+:+:+:+:+: Instance : [{}]", instance);

            // set result
            // assessmentResult.setThirdPartySolutions(ThirdPartySolutionUtil.detectThirdPartySolutionsFromMiddleware(targetHost, strategy.isWindows(), engine.getPath(), instance.getDomainHome()));
            assessmentResult.setEngine(engine);
            assessmentResult.setInstance(instance);
        } else {
            throw new RoRoException("WebLogic config file(config.xml) read failed. Please check file is exist and has permission to read in \"" +
                    targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + domainHome + "\"");
        }

        return assessmentResult;
    }

//    private String getJavaVersion(TargetHost targetHost, String instanceName, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
//        Map<String, String> commandMap = Map.of(COMMAND.JAVA_PATH.name(), COMMAND.JAVA_PATH.command(commandConfig, strategy.isWindows(), "weblogic.Name=" + instanceName));
//        boolean sudo = !strategy.isWindows() && SSHUtil.isSudoer(targetHost);
//        Map<String, RemoteExecResult> resultMap = MWCommonUtil.executeCommand(targetHost, commandMap, sudo, strategy);
//        RemoteExecResult result = resultMap.get(COMMAND.JAVA_PATH.name());
//        return MWCommonUtil.getJavaVersion(targetHost, result, sudo, commandConfig, strategy);
//    }
}
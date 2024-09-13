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
package io.playce.roro.mw.asmt.tomcat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.common.exception.NotsupportedException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.AbstractMiddlewareAssessment;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.tomcat.component.ProcessJson;
import io.playce.roro.mw.asmt.tomcat.component.ProcessLocal;
import io.playce.roro.mw.asmt.tomcat.component.ProcessRemote;
import io.playce.roro.mw.asmt.tomcat.dto.TomcatAssessmentResult;
import io.playce.roro.mw.asmt.tomcat.enums.CONFIG_FILES;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static io.playce.roro.common.util.ThreadLocalUtils.MW_SCAN_ERROR;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Component("TOMCATAssessment")
@RequiredArgsConstructor
@Slf4j
public class TomcatAssessment extends AbstractMiddlewareAssessment {
    private final ProcessRemote processRemote;
    private final ProcessLocal processLocal;
    private final ProcessJson processJson;
    private final CommandConfig commandConfig;

    @Override
    public MiddlewareAssessmentResult assessment(TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {
        boolean sudo = strategy.isSudoer(targetHost);

        long start = System.currentTimeMillis();
        log.debug("tomcat middleware assessmnet start: {}", middleware);
        TomcatAssessmentResult.Engine engine = getEngine(middleware);
        TomcatAssessmentResult.Instance instance = getInstance(middleware);

        processRemote.loadEngineInfo(targetHost, engine, sudo, strategy);
        log.debug("load engine info");
        processRemote.loadVmOption(targetHost, instance, sudo, strategy);
        log.debug("load vmoptions");
        instance.setIsRunning(instance.getOptions().size() > 0 ? "true" : "false");
        instance.setMinHeap(setDataFromVMoptions(instance, "-Xms"));
        instance.setMaxHeap(setDataFromVMoptions(instance, "-Xmx"));

        String configFilePath = middleware.getConfigFilePath();
        try {
            if (StringUtils.isNotEmpty(configFilePath)) {
                processLocal.loadConfigFiles(configFilePath, instance);
            } else {
                processRemote.loadConfigFiles(targetHost, engine, instance, sudo, strategy);
            }
        } catch (Exception e) {
            // server.xml, context.xml 파일을 찾을 수 없는 경우
            if (e instanceof InsufficientException) {
                String version = engine.getVersion();

                if (StringUtils.isEmpty(version) || version.compareTo("7") < 0) {
                    throw new NotsupportedException("Scan cannot be performed. It is an unsupported Middleware version.");
                }
            }
            ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred. Detail : [" + e.getMessage() + "]");
        }
        log.debug("load config files");
        saveConfigFiles(instance.getConfigFiles(), targetHost.getIpAddress(), strategy);

        Map<String, TomcatAssessmentResult.ConfigFile> configFileMap = instance.getConfigFiles();

        TomcatAssessmentResult.ConfigFile serverXmlFile = configFileMap.get(CONFIG_FILES.TOMCAT_CONFIG_SERVER.name());
        JsonNode serverXml = processJson.getJsonNode(serverXmlFile);

        JsonNode services = serverXml.get("Service");
        extractServicesInfo(targetHost, strategy, sudo, engine, instance, services);

        processJson.extractGlobalResourcesFromServerXml(instance, serverXml);
        log.debug("extract global resource info");
        instance.setConfigFileLocation(configFileMap.get(CONFIG_FILES.TOMCAT_CONFIG_SERVER.name()).getPath());

        TomcatAssessmentResult.ConfigFile contextXmlFile = configFileMap.get(CONFIG_FILES.TOMCAT_CONFIG_CONTEXT.name());
        JsonNode contextXml = processJson.getJsonNode(contextXmlFile);
        processJson.extractContextResourcesFromContextXml(instance, contextXml);
        log.debug("extract context resource info");
        instance.setContextFileLocation(configFileMap.get(CONFIG_FILES.TOMCAT_CONFIG_CONTEXT.name()).getPath());

        processRemote.loadRunUser(targetHost, instance, sudo, strategy);
        processRemote.loadJavaVersion(targetHost, instance, strategy);
        processRemote.loadJavaVendor(targetHost, instance, strategy);

        TomcatAssessmentResult result = new TomcatAssessmentResult();
        // result.setThirdPartySolutions(ThirdPartySolutionUtil.detectThirdPartySolutionsFromMiddleware(targetHost, strategy.isWindows(), engine.getPath(), instance.getPath()));
        result.setEngine(engine);
        result.setInstance(instance);
        log.debug("tomcat middleware assessment end: {}, duration(ms): {}", middleware, System.currentTimeMillis() - start);
        return result;
    }

    private void extractServicesInfo(TargetHost targetHost, GetInfoStrategy strategy, boolean sudo, TomcatAssessmentResult.Engine engine, TomcatAssessmentResult.Instance instance, JsonNode services) throws InterruptedException {
        if (services instanceof ArrayNode) {
            for (int i = 0; i < services.size(); i++) {
                JsonNode service = services.get(i);
                extractServiceInfo(targetHost, strategy, sudo, engine, instance, service);
            }
        } else {
            extractServiceInfo(targetHost, strategy, sudo, engine, instance, services);
        }
    }

    private void extractServiceInfo(TargetHost targetHost, GetInfoStrategy strategy, boolean sudo, TomcatAssessmentResult.Engine engine, TomcatAssessmentResult.Instance instance, JsonNode service) throws InterruptedException {
        String serviceName = JsonUtil.getValueFromJsonNode(service, "name");
        processJson.extractConnectorsFromService(instance, service, serviceName, strategy);
        log.debug("extract connector info");
        processJson.extractExecutorFromService(instance, service);
        log.debug("extract executor info");
        processJson.extractApplicationsFromService(targetHost, engine, instance, service, serviceName, sudo, strategy);
        log.debug("extract application and deploy info");
    }

    private void saveConfigFiles(Map<String, TomcatAssessmentResult.ConfigFile> configFiles, String ipAddress, GetInfoStrategy strategy) throws InterruptedException {
        String workdir = CommonProperties.getWorkDir();
        for (TomcatAssessmentResult.ConfigFile file : configFiles.values()) {
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
                log.error("tomcat assessment, file save error: {}", e.getMessage(), e);
                ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while file save error. Detail : [" + e.getMessage() + "]");
            }
        }
    }

    private String setDataFromVMoptions(TomcatAssessmentResult.Instance instance, String start) {
        for (String option : instance.getOptions()) {
            if (option.startsWith(start)) {
                return option.substring(start.length());
            }
        }
        return null;
    }

    private TomcatAssessmentResult.Engine getEngine(MiddlewareInventory middleware) {
        TomcatAssessmentResult.Engine engine = new TomcatAssessmentResult.Engine();
        engine.setPath(middleware.getEngineInstallationPath());
        engine.setVersion(middleware.getEngineVersion());
        return engine;
    }

    @NotNull
    private TomcatAssessmentResult.Instance getInstance(MiddlewareInventory middleware) {
        TomcatAssessmentResult.Instance instance = new TomcatAssessmentResult.Instance();
        instance.setPath(middleware.getDomainHomePath());
        return instance;
    }
}
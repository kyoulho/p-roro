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
 * Jhpark       8월 02, 2022            First Draft.
 */
package io.playce.roro.mw.asmt.jboss;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.common.exception.NotsupportedException;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.AbstractMiddlewareAssessment;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.jboss.dto.JbossAssessmentResult;
import io.playce.roro.mw.asmt.jboss.enums.ENGINE;
import io.playce.roro.mw.asmt.jboss.strategy.ServerModeStrategy;
import io.playce.roro.mw.asmt.jboss.strategy.enums.StrategyName;
import io.playce.roro.mw.asmt.jboss.strategy.helper.JBossHelper;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author jhpark
 * @version 3.0
 */
@Slf4j
@Component("JBOSSAssessment")
@RequiredArgsConstructor
public class JBossAssessment extends AbstractMiddlewareAssessment {

    private final CommandConfig commandConfig;
    @Autowired
    private JBossStrategyFactory jBossStrategyFactory;

    private final JBossHelper jBossHelper;


    @Override
    public MiddlewareAssessmentResult assessment(TargetHost targetHost, MiddlewareInventory middleware, GetInfoStrategy strategy) throws InterruptedException {

        JbossAssessmentResult.Engine engine = getEngine(middleware);
        JbossAssessmentResult.Instance instance = getInstance(middleware);
        boolean sudo = strategy.isSudoer(targetHost);
        long start = System.currentTimeMillis();

        /**
         * JBoss 미들웨어는 Standalone mode 와 Domain mode로 구분해서 분석을 진행한다.
         * jBossStrategyFactory 에 모드 네임을 던져서 해당 인스턴스를 가지고 온다.
         */
        String modeName = jBossHelper.getServerCheckMode(targetHost, instance, sudo, strategy);
        ServerModeStrategy servMode = jBossStrategyFactory.findServerModeStrategy
                (ENGINE.STANDALONE_NAME.codeName().equals(modeName) ? StrategyName.STANDALONE : StrategyName.DOMAIN);

        /**
         * 도메인 모드와 Standalone 모드별 분석을 시작한다.
         */
        log.debug("Server mode :::::::::::: " + servMode.getModeName());

        servMode.setEngineInfo(targetHost, engine, sudo, strategy);

        if (!engine.getName().contains("JBoss"))
            throw new NotsupportedException("Scan cannot be performed. It is not supported Middleware.");

        /**
         * RunTime Options
         */
        servMode.setInstanceVmInfo(targetHost, engine, sudo, strategy, instance);

        /**
         * Config File Name
         */
        servMode.findConfigFileNameFromCmd(targetHost, engine, sudo, strategy, instance);

        /**
         * 인스턴스 도메인 네임 및 home.dir , base.dir
         */
        jBossHelper.setServNameOrPaths(instance, strategy);

        /**
         * Config Files  Content  저장
         */
        if (StringUtils.isNotEmpty(middleware.getConfigFilePath())) {
            servMode.setInstanceLocalConfigFiles(middleware.getConfigFilePath(), instance, engine);
        } else {
            servMode.setInstanceRemoteConfigFiles(targetHost, engine, instance, sudo, strategy);
        }

        /**
         * Config File Path 저장
         */
        jBossHelper.setConfigPath(instance);

        /**
         * Config File 로컬 저장
         */
        jBossHelper.saveConfigFiles(instance.getConfigFiles(), targetHost.getIpAddress(), strategy);

        /**
         * Config File Json 파싱
         */
        Map<String, JbossAssessmentResult.ConfigFile> configFileMap = instance.getConfigFiles();
        JbossAssessmentResult.ConfigFile serverConfXmlFile = jBossHelper.getConfFile(configFileMap, engine, instance.getConfigFileName(), strategy);
        JsonNode confFile = jBossHelper.getJsonNode(serverConfXmlFile);

        /**
         * Domain 모드 일때 Host File 사용
         */
        JsonNode hostFile = null;
        if (engine.getMode().equals(ENGINE.DOMAIN_NAME.codeName())) {
            JbossAssessmentResult.ConfigFile serverHostXmlFile = jBossHelper.getHostFile(configFileMap, engine, instance.getConfigFileName(), strategy);
            hostFile = jBossHelper.getJsonNode(serverHostXmlFile);
        }

        /**
         * 인스턴스 정보 저장
         */
        servMode.setServers(instance, confFile, hostFile, strategy, targetHost, engine, sudo);

        /**
         * 프로토콜 및 포트 정보 및 소켓 바인드 저장
         */
        servMode.setConnectors(instance, confFile, hostFile, strategy, targetHost, sudo);
        // servMode.setInterFaces(instance, confFile, strategy);

        /**
         * 서버 스레드 정보 저장
         */
        servMode.setExecutorServer(instance, confFile);


        /**
         * 어플리케이션 정보 저장 :  setContextPath , setDeployFileName, deployFile
         */
        servMode.setApplications(targetHost, engine, instance, confFile, sudo, strategy);

        /**
         * 모듈정보
         */
        servMode.setExtensions(instance, confFile, strategy);

        /**
         * DB 관련 리소스 정보 저장
         */
        servMode.setResources(instance, confFile);

        /**
         * ConfigFile 위치 저장
         */
        instance.setConfigFileLocation(jBossHelper.getConfFilePath(configFileMap, engine, instance.getConfigFileName(), strategy));
        if (StringUtils.isEmpty(instance.getJavaVersion())) {
            servMode.setJavaVersion(targetHost, instance, strategy, engine);
        }

        if (StringUtils.isEmpty(instance.getJavaVendor())) {
            servMode.setJavaVendor(targetHost, instance, strategy, engine);
        }
        servMode.setRunUser(targetHost, instance, sudo, strategy);

        JbossAssessmentResult result = new JbossAssessmentResult();
        // result.setThirdPartySolutions(ThirdPartySolutionUtil.detectThirdPartySolutionsFromMiddleware(targetHost, strategy.isWindows(), engine.getPath(), instance.getDomainPath()));
        result.setEngine(engine);
        result.setInstance(instance);
        log.debug("JBoss middleware assessment end: {}, duration(ms): {}", middleware, System.currentTimeMillis() - start);

        return result;
    }

    private JbossAssessmentResult.Engine getEngine(MiddlewareInventory middleware) {
        JbossAssessmentResult.Engine engine = new JbossAssessmentResult.Engine();
        engine.setPath(middleware.getEngineInstallationPath());
        engine.setVersion(middleware.getEngineVersion());
        engine.setVendor(ENGINE.VENDOR.codeName());
        return engine;
    }

    @NotNull
    private JbossAssessmentResult.Instance getInstance(MiddlewareInventory middleware) {
        JbossAssessmentResult.Instance instance = new JbossAssessmentResult.Instance();
        instance.setDomainPath(middleware.getDomainHomePath());
        return instance;
    }
}
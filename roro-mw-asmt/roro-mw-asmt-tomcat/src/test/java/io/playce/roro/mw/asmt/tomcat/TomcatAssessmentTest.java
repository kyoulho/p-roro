package io.playce.roro.mw.asmt.tomcat;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import io.playce.roro.common.config.RoRoProperties;
import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import io.playce.roro.mw.asmt.tomcat.component.ProcessJson;
import io.playce.roro.mw.asmt.tomcat.component.ProcessLocal;
import io.playce.roro.mw.asmt.tomcat.component.ProcessRemote;
import io.playce.roro.mw.asmt.tomcat.dto.TomcatAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.UnixLikeInfoStrategy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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
 * Dong-Heon Han    Feb 13, 2022		First Draft.
 */

@Slf4j
class TomcatAssessmentTest {
    private TargetHost targetHost;
    private MiddlewareInventory middlewareInventory;
    private ProcessRemote processRemote;
    private ProcessLocal processLocal;
    private ProcessJson processJson;
    private XmlMapper xmlMapper;
    private TomcatAssessmentResult.Engine engine;
    private TomcatAssessmentResult.Instance instance;
    private TomcatAssessment tomcatAssessment;
    private RoRoProperties roRoProperties;

    @BeforeEach
    void setUp() {
        targetHost = new TargetHost();
//        targetHost.setIpAddress("127.0.0.1");
//        targetHost.setPort(2203);
        targetHost.setIpAddress("192.168.4.61");
        targetHost.setPort(22);
        targetHost.setUsername("roro");
        targetHost.setPassword("jan01jan");
        middlewareInventory = new MiddlewareInventory();
        middlewareInventory.setEngineInstallationPath("/opt/apache-tomcat-9.0.54");
        middlewareInventory.setDomainHomePath("/opt/servers/roro-svr");
//        middlewareInventory.setEngineInstallationPath("/opt/apache-tomcat-8.5.57");

//        targetHost.setPort(2204);
//        targetHost.setUsername("wasup");
//        middlewareInventory.setEngineInstallationPath("/opt/wasup/engines/wasup-7.0.105");
//        middlewareInventory.setDomainHomePath("/opt/wasup/engines/wasup-7.0.105");

        processLocal = new ProcessLocal();
        xmlMapper = new XmlMapper();
        processJson = new ProcessJson(processRemote, xmlMapper);

        engine = new TomcatAssessmentResult.Engine();
        engine.setPath(middlewareInventory.getEngineInstallationPath());
        instance = new TomcatAssessmentResult.Instance();
        instance.setPath(middlewareInventory.getDomainHomePath());

        roRoProperties.setWorking(new RoRoProperties.Working());

        CommandConfig config = new CommandConfig();
        processRemote = new ProcessRemote(config);
        tomcatAssessment = new TomcatAssessment(processRemote, processLocal, processJson, config);
    }

//    @Test
    void assessment() throws InterruptedException {
        Gson gson = new Gson();
        GetInfoStrategy strategy = new UnixLikeInfoStrategy(false);
        MiddlewareAssessmentResult result = tomcatAssessment.assessment(targetHost, middlewareInventory, strategy);

        log.debug("{}", gson.toJson(result));
    }
}
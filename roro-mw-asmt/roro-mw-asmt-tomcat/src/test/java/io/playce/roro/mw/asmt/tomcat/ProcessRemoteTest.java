package io.playce.roro.mw.asmt.tomcat;

import io.playce.roro.common.util.support.MiddlewareInventory;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.tomcat.component.ProcessRemote;
import io.playce.roro.mw.asmt.tomcat.dto.TomcatAssessmentResult;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.UnixLikeInfoStrategy;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class ProcessRemoteTest {
    private TargetHost targetHost;
    private MiddlewareInventory middlewareInventory;
    private ProcessRemote processRemote;
    private TomcatAssessmentResult.Engine engine;
    private TomcatAssessmentResult.Instance instance;

    @BeforeEach
    void setUp() {
        targetHost = new TargetHost();
        targetHost.setIpAddress("127.0.0.1");
        targetHost.setPort(2203);
        targetHost.setUsername("roro");
        targetHost.setPassword("jan01jan");
        middlewareInventory = new MiddlewareInventory();
        middlewareInventory.setEngineInstallationPath("/opt/apache-tomcat-9.0.54");
        middlewareInventory.setDomainHomePath("/opt/servers/roro-svr");
        engine = new TomcatAssessmentResult.Engine();
        engine.setPath(middlewareInventory.getEngineInstallationPath());
        instance = new TomcatAssessmentResult.Instance();
        instance.setPath(middlewareInventory.getDomainHomePath());
    }

//    @Test
    void loadEngineInfo() throws InterruptedException {
        engine.setPath(middlewareInventory.getEngineInstallationPath());
        GetInfoStrategy strategy = new UnixLikeInfoStrategy(false);
        CommandConfig tomcatConfig = new CommandConfig();
        tomcatConfig.setUnix(Map.of(COMMAND.CAT, "cat %s"));
        processRemote = new ProcessRemote(tomcatConfig);
        processRemote.loadEngineInfo(targetHost, engine, true, strategy);
        assertEquals(engine.getName(), "Apache Tomcat");
    }

//    @Test
    void loadVmOption() throws InterruptedException {
        instance.setPath(middlewareInventory.getDomainHomePath());
        GetInfoStrategy strategy = new UnixLikeInfoStrategy(false);
        CommandConfig tomcatConfig = new CommandConfig();
        tomcatConfig.setUnix(Map.of(COMMAND.CAT, "cat %s"));
        processRemote.loadVmOption(targetHost, instance, true, strategy);
//        assertEquals(instance.getMinHeap(), "2048m");
    }

//    @Test
    void loadConfigFiles() throws InterruptedException {
        instance.setPath(engine.getPath());
        GetInfoStrategy strategy = new UnixLikeInfoStrategy(false);
        CommandConfig tomcatConfig = new CommandConfig();
        tomcatConfig.setUnix(Map.of(COMMAND.CAT, "cat %s"));
        processRemote.loadConfigFiles(targetHost, engine, instance, true, strategy);
        System.out.println();
    }
}
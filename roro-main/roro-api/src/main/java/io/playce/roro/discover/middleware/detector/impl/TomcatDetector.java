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
 * Hoon Oh       1월 27, 2022            First Draft.
 */
package io.playce.roro.discover.middleware.detector.impl;

import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.code.Domain1102;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.discover.middleware.detector.AbstractDetector;
import io.playce.roro.discover.middleware.dto.DetectResultInfo;
import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.discover.middleware.scenario.tomcat.TomcatEnginePathScenario;
import io.playce.roro.discover.middleware.scenario.tomcat.TomcatEngineVersionScenario;
import io.playce.roro.discover.middleware.scenario.tomcat.TomcatInstancePathScenario;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Slf4j
public class TomcatDetector extends AbstractDetector {

    public TomcatDetector(Process process) {
        super(process);
    }

    @Override
    public DetectResultInfo generateMiddleware(TargetHost targetHost, InventoryProcessConnectionInfo connectionInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {

        // ExtractScenario resourceNameScenarios = generateResourceNameScenario(targetHost);
        ExtractScenario enginePathScenarios = generateEnginePathScenario(targetHost);
        ExtractScenario domainPathScenarios = generateDomainPathScenario(targetHost);
        // ExtractScenario versionScenarios = generateEngineVersionScenario(targetHost);

        // String name = resourceNameScenarios.execute(process);
        String enginePath = enginePathScenarios.execute(process, commandConfig, strategy);
        String domainPath = domainPathScenarios.execute(process, commandConfig, strategy);
       /* if(StringUtils.isEmpty(name)) {
            log.debug("The name could not be found. {}", process);
        }*/
        if (StringUtils.isEmpty(enginePath)) {
            log.debug("The engine path could not be found. {}", process);
        }
        if (StringUtils.isEmpty(domainPath)) {
            log.debug("The domain path could not be found. {}", process);
        }

        // String engineVersion = versionScenarios.execute(process, commandConfig, strategy);
        //
        // if(StringUtils.isEmpty(engineVersion)) {
        //     log.debug("The engineVersion could not be found. {}", process);
        // }

        return DetectResultInfo.builder()
                .vendor("Apache")
                .mwDetailType(Domain1013.TOMCAT)
                .mwType(Domain1102.WAS)
                .pid(process.getPid())
                .runUser(process.getUser())
                // .version(engineVersion)
                // .name(name)
                .enginePath(enginePath)
                .domainPath(domainPath)
                .javaVersion(getJavaVersion(process.getCmd(), targetHost, commandConfig, strategy))
                .build();
    }

    private ExtractScenario generateEngineVersionScenario(TargetHost targetHost) {
        ExtractScenario step1 = new TomcatEngineVersionScenario.Step1();
        ExtractScenario.setChain(targetHost, step1);
        return step1;
    }

    private List<String> getIgnoreCase() {
        return List.of(".");
    }

    private ExtractScenario generateDomainPathScenario(TargetHost targetHost) {
        ExtractScenario scenario = new TomcatInstancePathScenario.Step1();
        ExtractScenario step2 = new TomcatInstancePathScenario.Step2();
        ExtractScenario.setChain(targetHost, scenario, step2);
        scenario.setIgnoreCases(getIgnoreCase());
        return scenario;
    }

    /*private ExtractScenario generateResourceNameScenario(TargetHost targetHost){
        ExtractScenario scenario = new TomcatNameScenario();
        scenario.setIgnoreCases(getIgnoreCase());
        scenario.setTargetHost(targetHost);
        return scenario;
    }*/

    private ExtractScenario generateEnginePathScenario(TargetHost targetHost) {
        ExtractScenario scenario = new TomcatEnginePathScenario.Step1();
        ExtractScenario step2 = new TomcatEnginePathScenario.Step2();
        ExtractScenario.setChain(targetHost, scenario, step2);
        scenario.setIgnoreCases(getIgnoreCase());
        return scenario;
    }

}
//end of TomcatGenerator.java
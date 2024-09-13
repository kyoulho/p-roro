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
import io.playce.roro.discover.middleware.scenario.weblogic.WebLogicDomainPathScenario;
import io.playce.roro.discover.middleware.scenario.weblogic.WebLogicEnginePathScenario;
import io.playce.roro.discover.middleware.scenario.weblogic.WebLogicNameScenario;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
public class WebLogicDetector extends AbstractDetector {

    public WebLogicDetector(Process process) {
        super(process);
    }

    @Override
    public DetectResultInfo generateMiddleware(TargetHost targetHost, InventoryProcessConnectionInfo connectionInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {

        ExtractScenario resourceNameScenarios = generateResourceNameScenario(targetHost);
        ExtractScenario enginePathScenarios = generateEnginePathScenario(targetHost);
        ExtractScenario domainPathScenarios = generateDomainPathScenario(targetHost);


        String name = resourceNameScenarios.execute(process, commandConfig, strategy);
        String enginePath = enginePathScenarios.execute(process, commandConfig, strategy);
        String domainPath = domainPathScenarios.execute(process, commandConfig, strategy);
        /*if(StringUtils.isEmpty(name)) {
            log.debug("The name could not be found. {}", process);
        }*/
        if (StringUtils.isEmpty(enginePath)) {
            log.debug("The engine path could not be found. {}", process);
        }
        if (StringUtils.isEmpty(domainPath)) {
            log.debug("The domain path could not be found. {}", process);
        }

        return DetectResultInfo.builder()
                .vendor("Oracle")
                .mwDetailType(Domain1013.WEBLOGIC)
                .mwType(Domain1102.WAS)
                .pid(process.getPid())
                .runUser(process.getUser())
//                .name(StringUtils.isEmpty(name) ? null : name)
                .processName(name)
                .enginePath(enginePath)
                .domainPath(domainPath)
                .javaVersion(getJavaVersion(process.getCmd(), targetHost, commandConfig, strategy))
                .build();
    }

    /*private List<String> getIgnoreCase(){
        return List.of("-");
    }*/

    private ExtractScenario generateDomainPathScenario(TargetHost targetHost) {
        ExtractScenario scenario = new WebLogicDomainPathScenario.Step1();
        ExtractScenario step2 = new WebLogicDomainPathScenario.Step2();
        ExtractScenario step3 = new WebLogicDomainPathScenario.Step3();
        ExtractScenario.setChain(targetHost, scenario, step2, step3);
        return scenario;
    }

    private ExtractScenario generateResourceNameScenario(TargetHost targetHost) {
        ExtractScenario scenario = new WebLogicNameScenario();
        scenario.setTargetHost(targetHost);
        return scenario;
    }

    private ExtractScenario generateEnginePathScenario(TargetHost targetHost) {
        ExtractScenario scenario = new WebLogicEnginePathScenario();
        scenario.setTargetHost(targetHost);
        return scenario;
    }

}
//end of WebLogicGenerator.java
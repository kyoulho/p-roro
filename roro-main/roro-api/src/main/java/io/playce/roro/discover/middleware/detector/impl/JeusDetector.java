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
import io.playce.roro.discover.middleware.scenario.jeus.JeusDomainHomePathScenario;
import io.playce.roro.discover.middleware.scenario.jeus.JeusEnginePathScenario;
import io.playce.roro.discover.middleware.scenario.jeus.JeusVersionScenario;
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
public class JeusDetector extends AbstractDetector {

    public JeusDetector(Process process) {
        super(process);
    }

    @Override
    public DetectResultInfo generateMiddleware(TargetHost targetHost, InventoryProcessConnectionInfo connectionInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        if (checkSkipProcess(process.getCmd()
                , "jeus.server.NodemanagerBootstrapper" // jeus8
                , "jeus.nodemanager.JeusNodeManager"    // jeus7
        ))
            return null;

        ExtractScenario enginePathScenarios = generateEnginePathScenario();

        String enginePath = enginePathScenarios.execute(process, commandConfig, strategy);

        ExtractScenario jeusVersionScenario = generateJeusVersionScenario(targetHost, enginePath);

        String jeusVersion = jeusVersionScenario.execute(process, commandConfig, strategy);

        ExtractScenario domainPathScenarios = generateDomainPathScenario(targetHost, enginePath, jeusVersion);

//        ExtractScenario resourceNameScenarios = generateResourceNameScenario(targetHost);
//        String name = resourceNameScenarios.execute(process);
//        if(StringUtils.isEmpty(name)) {
//            log.debug("The name could not be found. {}", process);
//        }
        String domainPath = domainPathScenarios.execute(process, commandConfig, strategy);
        if (StringUtils.isEmpty(domainPath)) {
            log.debug("The domain path could not be found. {}", process);
        }

        return DetectResultInfo.builder()
                .vendor("Tmax")
                .mwDetailType(Domain1013.JEUS)
                .mwType(Domain1102.WAS)
                .pid(process.getPid())
                .runUser(process.getUser())
//                .name(name)
                .version(jeusVersion)
                .enginePath(enginePath)
                .domainPath(domainPath)
                .javaVersion(getJavaVersion(process.getCmd(), targetHost, commandConfig, strategy))
                .build();
    }

    private boolean checkSkipProcess(List<String> commandLines, String... skipStrs) {
        for (String commandLine : commandLines) {
            for (String skipStr : skipStrs) {
                if (commandLine.contains(skipStr)) {
                    return true;
                }
            }
        }

        return false;
    }

    private ExtractScenario generateJeusVersionScenario(TargetHost targetHost, String solutionPath) {
        ExtractScenario scenario = new JeusVersionScenario(solutionPath);
        scenario.setIgnoreCases(getIgnoreCase());
        scenario.setTargetHost(targetHost);
        return scenario;
    }

    private List<String> getIgnoreCase() {
        return List.of("-");
    }

    private ExtractScenario generateDomainPathScenario(TargetHost targetHost, String solutionPath, String version) {
        ExtractScenario scenario = new JeusDomainHomePathScenario(solutionPath, version).new Step1();
        ExtractScenario step2 = new JeusDomainHomePathScenario(solutionPath, version).new Step2();
        ExtractScenario step3 = new JeusDomainHomePathScenario(solutionPath, version).new Step3();
        ExtractScenario.setChain(targetHost, scenario, step2, step3);
        scenario.setIgnoreCases(getIgnoreCase());

        return scenario;
    }

    /*private ExtractScenario generateResourceNameScenario(TargetHost targetHost){
        ExtractScenario scenario = new JeusNameScenario.Step1();
        ExtractScenario step2 = new JeusNameScenario.Step2(); step2.setTargetHost(targetHost);
        ExtractScenario step3 = new JeusNameScenario.Step3(); step3.setTargetHost(targetHost);
        ExtractScenario step4 = new JeusNameScenario.Step4(); step4.setTargetHost(targetHost);
        ExtractScenario step5 = new JeusNameScenario.Step5(); step5.setTargetHost(targetHost);
        ExtractScenario step6 = new JeusNameScenario.Step6(); step6.setTargetHost(targetHost);
        scenario.setNext(step2).setNext(step3).setNext(step4).setNext(step5).setNext(step6);

        scenario.setIgnoreCases(getIgnoreCase());
        scenario.setTargetHost(targetHost);

        return scenario;
    }*/

    private ExtractScenario generateEnginePathScenario() {
        ExtractScenario scenario = new JeusEnginePathScenario.Step1();
        scenario.setIgnoreCases(getIgnoreCase());
        return scenario;
    }
}
//end of JeusGenerator.java
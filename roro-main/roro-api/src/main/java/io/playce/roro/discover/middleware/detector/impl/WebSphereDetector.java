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
import io.playce.roro.discover.middleware.scenario.websphere.WebSphereCellNameScenario;
import io.playce.roro.discover.middleware.scenario.websphere.WebSphereEnginePathScenario;
import io.playce.roro.discover.middleware.scenario.websphere.WebSphereNodeNameScenario;
import io.playce.roro.discover.middleware.scenario.websphere.WebSphereProfileScenario;
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
public class WebSphereDetector extends AbstractDetector {

    public WebSphereDetector(Process process) {
        super(process);
    }

    @Override
    public DetectResultInfo generateMiddleware(TargetHost targetHost, InventoryProcessConnectionInfo connectionInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {

//        ExtractScenario nameScenario = generateResourceNameScenario();
        ExtractScenario enginePathScenarios = generateEnginePathScenario();
        // ExtractScenario domainPathScenarios = generateDomainPathScenario();
        ExtractScenario profileScenario = generateProfileScenario();
        ExtractScenario nodeNameScenario = generateNodeNameScenario();
        ExtractScenario cellNameScenario = generateCellNameScenario();


//        String name = nameScenario.execute(process);
        String enginePath = enginePathScenarios.execute(process, commandConfig, strategy);
        String profile = profileScenario.execute(process, commandConfig, strategy);
        String nodeName = nodeNameScenario.execute(process, commandConfig, strategy);
        String cellName = cellNameScenario.execute(process, commandConfig, strategy);
        /*if(StringUtils.isEmpty(name)) {
            log.debug("The name could not be found. {}", process);
        }*/
        if (StringUtils.isEmpty(enginePath)) {
            log.debug("The engine path could not be found. {}", process);
        }
        if (StringUtils.isEmpty(profile)) {
            log.debug("The profile could not be found. {}", process);
        }
        if (StringUtils.isEmpty(nodeName)) {
            log.debug("The nodeName could not be found. {}", process);
        }
        if (StringUtils.isEmpty(cellName)) {
            log.debug("The cellName could not be found. {}", process);
        }

        return DetectResultInfo.builder()
                .vendor("IBM")
                .mwDetailType(Domain1013.WSPHERE)
                .mwType(Domain1102.WAS)
                .pid(process.getPid())
                .runUser(process.getUser())
//                .name(name)
                .profile(profile)
                .cellName(cellName)
                .nodeName(nodeName)
                .enginePath(enginePath)
                .javaVersion(getJavaVersion(process.getCmd(), targetHost, commandConfig, strategy))
                .build();
    }

    private ExtractScenario generateCellNameScenario() {
        return new WebSphereCellNameScenario();
    }

    private ExtractScenario generateNodeNameScenario() {
        return new WebSphereNodeNameScenario();
    }

    private ExtractScenario generateProfileScenario() {
        return new WebSphereProfileScenario();
    }

    /*private List<String> getIgnoreCase(){
        return List.of("-");
    }*/

    /*private ExtractScenario generateResourceNameScenario(){
        return new WebSphereNameScenario();
    }
*/
    private ExtractScenario generateEnginePathScenario() {
        return new WebSphereEnginePathScenario();
    }

}
//end of WebSphereGenerator.java
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
import io.playce.roro.discover.middleware.scenario.webtob.WebToBEnginePathScenario;
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
public class WebTobDetector extends AbstractDetector {

    public WebTobDetector(Process process) {
        super(process);
    }

    @Override
    public DetectResultInfo generateMiddleware(TargetHost targetHost, InventoryProcessConnectionInfo connectionInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        ExtractScenario enginePathScenarios = generateEnginePathScenario(targetHost);

        String enginePath = enginePathScenarios.execute(process, commandConfig, strategy);
        if (StringUtils.isEmpty(enginePath)) {
            log.debug("The engine path could not be found. {}", process);
        } else {
            // "/sw/eip/tmax/webtob logout" 으로 Path가 지정되는 경우가 있음 (로그 상으로는 logout 앞에 개행)
            enginePath = enginePath.replaceAll("[\n\r]", StringUtils.SPACE);
            if (enginePath.contains(StringUtils.SPACE)) {
                log.info("WebToB engine path({}) contains white spaces.", enginePath);
                enginePath = enginePath.split(StringUtils.SPACE)[0];
            }
        }

        return DetectResultInfo.builder()
                .vendor("Tmax")
                .mwDetailType(Domain1013.WEBTOB)
                .mwType(Domain1102.WEB)
                .pid(process.getPid())
                .runUser(process.getUser())
                .enginePath(enginePath)
                // .javaVersion(getJavaVersion(process.getCmd(), targetHost, commandConfig, strategy))
                .javaVersion(null)
                .build();
    }

    private ExtractScenario generateEnginePathScenario(TargetHost targetHost) {
        ExtractScenario scenario = new WebToBEnginePathScenario.Step1();
        ExtractScenario step2 = new WebToBEnginePathScenario.Step2();
//        ExtractScenario step3 = new WebToBSolutionPathScenario.Step3();
//        ExtractScenario step4 = new WebToBSolutionPathScenario.Step4();
        ExtractScenario step5 = new WebToBEnginePathScenario.Step5();
//        ExtractScenario.setChain(targetHost, scenario, step2, step3, step4, step5);

        // step3, step4는 일반화된 로직이 필요함.
        // https://cloud-osci.atlassian.net/browse/ROROQA-261
        // profile로 부터 alias wcfg='cd $WEBTOBDIR/config' 설정을 engine_path로 잘못 인식하도록 함 -> 그래서 제거함.
        ExtractScenario.setChain(targetHost, scenario, step2, step5);
        return scenario;
    }

}
//end of WebTobGenerator.java
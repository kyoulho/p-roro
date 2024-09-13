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
package io.playce.roro.discover.middleware.scenario.jeus;

import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.discover.server.util.ProcessCmdUtil;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
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
public class JeusDomainHomePathScenario {

    protected String version;
    protected String solutionPath;

    public JeusDomainHomePathScenario(String enginePath, String version) {
        this.version = version;
        this.solutionPath = enginePath;
    }

    public class Step1 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {

            if (StringUtils.isNotEmpty(solutionPath)) {
                if (StringUtils.isEmpty(version) || Float.parseFloat(version) > 6.0) {
                    if (StringUtils.isNotEmpty(ProcessCmdUtil.getParam(process.getCmd(), "-Djava.security.policy"))) {
                        String domainHome = ProcessCmdUtil.getParam(process.getCmd(), "-Djava.security.policy");
                        int idx = domainHome.lastIndexOf(strategy.getSeparator() + "config");

                        if (idx > -1) {
                            domainHome = domainHome.substring(0, idx);
                            result = domainHome;
                            log.debug("{}", process);
                        }
                    }
                }
            }
            return StringUtils.isNotEmpty(result);
        }
    }

    /*
     *  ToDo: AssessmentManager.getDefaultEnginePathList()
     * */
    public class Step2 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {


            return StringUtils.isNotEmpty(result);
        }
    }

    public class Step3 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {

//            String command = "sudo find " + solutionPath +
//                    "/config -name JEUSMain.xml | grep -v example | grep -v security 2> /dev/null";
//            String mainXmlPaths = SSHUtil.executeCommand(targetHost, command);
            String mainXmlPaths = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_DOMAIN_PATH_SCENARIO_STEP31, commandConfig, strategy, solutionPath);
            if (strategy.isWindows() && StringUtils.isEmpty(result)) {
                mainXmlPaths = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_DOMAIN_PATH_SCENARIO_STEP32, commandConfig, strategy, solutionPath);
            }

            String separator = strategy.getSeparator();

            if (StringUtils.isNotEmpty(mainXmlPaths) && mainXmlPaths.contains("JEUSMain.xml")) {
                for (String mainXmlPath : mainXmlPaths.split(strategy.getCarriageReturn())) {
                    int idx = mainXmlPath.lastIndexOf(separator + "JEUSMain.xml");
//                    String domainHome = mainXmlPath.substring(0, idx);

                    // Middleware duplicatedData = middlewareList.stream()
                    //         .filter(m -> {
                    //                     if (m.getDomainHome() != null) {
                    //                         if (!m.getDomainHome().equals(domainHome)) {
                    //                             return false;
                    //                         }
                    //                     } else if (StringUtils.isNotEmpty(domainHome)) {
                    //                         return false;
                    //                     }
                    //                     return true;
                    //                 }
                    //         )
                    //         .sorted(Comparator.comparing(Middleware::getCreateDate).reversed())
                    //         .findFirst().orElse(null);
                    //
                    // if (duplicatedData == null) {
                    //     unknownMiddleware.setDomainHome(domainHome);
                    //     break;
                    // }
//                    result = domainHome;
                    result = mainXmlPath.substring(0, idx);
                }
            }
            return StringUtils.isNotEmpty(result);
        }
    }
}
//end of Step3.java
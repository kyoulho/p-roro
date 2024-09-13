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
 * Hoon Oh       1월 30, 2022            First Draft.
 */
package io.playce.roro.discover.middleware.scenario.tomcat;

import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.discover.server.util.ProcessCmdUtil;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
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
public class TomcatInstancePathScenario {
    // 1. Process Command에서 -Dcatalina.base 인자 추출
    public static class Step1 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            result = ProcessCmdUtil.getParam(process.getCmd(), "-Dcatalina.base");

            return StringUtils.isNotEmpty(result);
        }
    }

    public static class Step2 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            // String command;
            // for (String defaultPath : AssessmentManager.getDefaultEnginePathList()) {
            //     command = "sudo find " + defaultPath + " -type f -path '*conf/server.xml' 2> /dev/null";
            //     String output = SSHUtil.executeCommand(targetHost, command);
            //
            //     if (StringUtils.isNotEmpty(output)) {
            //         if (output.indexOf("/conf/server.xml") > -1) {
            //             result = output.substring(0, output.indexOf("/conf/server.xml")));
            //
            //             break;
            //         }
            //     }
            // }

            return StringUtils.isNotEmpty(result);
        }
    }
}
//end of TomcatInstancePathScenario.java
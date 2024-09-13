package io.playce.roro.discover.middleware.scenario.nginx;/*
 * Copyright 2022 The Playce-WASUP Project.
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
 * Hoon Oh       2월 07, 2022            First Draft.
 */

import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
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
public class NginxEnginePathScenario extends ExtractScenario {

    public NginxEnginePathScenario(TargetHost targetHost) {
        super.targetHost = targetHost;
    }

    @Override
    protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        String enginePath = process.getCmd().get(3);

        if (StringUtils.defaultString(enginePath).equals("nginx")) {
            enginePath = MWCommonUtil.getExecuteResult(targetHost, COMMAND.COMMAND_WHICH, commandConfig, strategy, "nginx");
        }

        if (StringUtils.isNotEmpty(enginePath)) {
            result = enginePath.substring(0, enginePath.lastIndexOf(strategy.getSeparator()));
        }

        return StringUtils.isNotEmpty(result);
    }
}
//end of ApacheSolutionPathScenario.java
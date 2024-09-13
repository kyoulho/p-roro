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
 * Hoon Oh       2월 07, 2022            First Draft.
 */
package io.playce.roro.discover.middleware.scenario.nginx;

import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static io.playce.roro.common.util.StringUtil.splitToArrayByCrlf;

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
public class NginxInstancePathScenario extends ExtractScenario {

    protected String executePath;

    public NginxInstancePathScenario(TargetHost targetHost, String executePath) {
        super.targetHost = targetHost;
        this.executePath = executePath;
    }

    @Override
    protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        List<String> cmdList = process.getCmd();

        // nginx 기동시 -c 옵션으로 Config File을 직접 지정하는 경우.
        if (CollectionUtils.isNotEmpty(cmdList)) {
            for (int i = 0; i < cmdList.size(); i++) {
                if (cmdList.get(i).equalsIgnoreCase("-c")) {
                    result = cmdList.get(i + 1);
                    break;
                }
            }
        }

        if (StringUtils.isEmpty(result)) {
            String configFilePath;

            if (strategy.isWindows()) {
                String nginxHome = executePath.substring(0, executePath.lastIndexOf(strategy.getSeparator()));
                configFilePath = MWCommonUtil.getExecuteResult(targetHost, COMMAND.NGINX_CONFIG_FILE, commandConfig, strategy, executePath, nginxHome);
                configFilePath = configFilePath.replaceAll("/", "\\\\");
            } else {
                configFilePath = MWCommonUtil.getExecuteResult(targetHost, COMMAND.NGINX_CONFIG_FILE, commandConfig, strategy, executePath);
            }


            String[] lineArrays = splitToArrayByCrlf(configFilePath);
            for (String line : lineArrays) {
                String[] wordArray = line.split(StringUtils.SPACE);
                for (String word : wordArray) {
                    if (word.contains(".conf")) {
                        result = word;
                        break;
                    }
                }
            }
        }

        return StringUtils.isNotEmpty(result);
    }

}
//end of ApacheInstancePathScenario.java
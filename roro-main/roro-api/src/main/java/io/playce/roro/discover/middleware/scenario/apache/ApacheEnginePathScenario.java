package io.playce.roro.discover.middleware.scenario.apache;/*
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

import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.discover.middleware.scenario.ExtractScenario;
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
public class ApacheEnginePathScenario {

    protected String httpdParentDir;

    public ApacheEnginePathScenario(String httpdParentDir) {
        this.httpdParentDir=httpdParentDir;
    }

    public class Step1 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            String enginePath = process.getCmd().get(0);
            if(StringUtils.isNotEmpty(httpdParentDir)){
                result= enginePath.substring(0, enginePath.indexOf(httpdParentDir));
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    public class Step2 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            if(strategy.isWindows()) return false;

            String command = "sudo find /usr/local/apache/bin -type f -name 'http' 2> /dev/null";
            String output = SSHUtil.executeCommand(targetHost, command);

            if (StringUtils.isEmpty(result)) {
                command = "sudo find /usr/local/apache/bin -type f -name 'apachectl' 2> /dev/null";
                output = SSHUtil.executeCommand(targetHost, command);
            }

            if(StringUtils.isNotEmpty(output)){
                result= "/usr/local/apache";
            }

            return StringUtils.isNotEmpty(result);
        }
    }

}
//end of ApacheSolutionPathScenario.java
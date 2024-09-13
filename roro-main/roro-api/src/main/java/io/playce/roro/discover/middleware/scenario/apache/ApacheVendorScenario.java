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
package io.playce.roro.discover.middleware.scenario.apache;

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
public class ApacheVendorScenario {

    protected String httpdParentDir;
    protected String solutionPath;

    public ApacheVendorScenario(String httpdParentDir, String solutionPath) {
        this.httpdParentDir = httpdParentDir;
        this.solutionPath = solutionPath;
    }

    public class Step1 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
//            String command = "sudo " + solutionPath + httpdParentDir + "/httpd -v";
//            String result = SSHUtil.executeCommand(targetHost, command);

            String separator = strategy.getSeparator();
            String command = solutionPath + httpdParentDir + separator + "httpd";
            String result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.APACHE_VERSION, commandConfig, strategy, command);

            for (String line : result.split(strategy.getCarriageReturn())) {
                if (line.contains("Oracle")) {
                    result = "Oracle";
                } else if (line.contains("IBM")) {
                    result = "IBM";
                } else if (line.contains("Apache")) {
                    result = "Apache";
                }
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    public class Step2 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
//            String command  = "sudo " + solutionPath + httpdParentDir + "/apachectl -v";
//            result = SSHUtil.executeCommand(targetHost, command);

            String separator = strategy.getSeparator();
            String command = solutionPath + httpdParentDir + separator + "apachectl";
            result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.APACHE_VERSION, commandConfig, strategy, command);

            for (String line : result.split("\n")) {
                if (line.contains("Oracle")) {
                    result = "Oracle";
                } else if (line.contains("IBM")) {
                    result = "IBM";
                } else if (line.contains("Apache")) {
                    result = "Apache";
                }
            }

            return StringUtils.isNotEmpty(result);
        }
    }
}
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

import com.jcraft.jsch.JSchException;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.SSHUtil2;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.discover.server.util.ProcessCmdUtil;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

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
public class ApacheInstancePathScenario {

    protected String httpdParentDir;
    protected String enginePath;

    public ApacheInstancePathScenario(String httpdParentDir, String enginePath) {
        this.httpdParentDir = httpdParentDir;
        this.enginePath = enginePath;
    }

    public class Step1 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            int confFileIdx = ProcessCmdUtil.getIndexFromString(process.getCmd(), "-f");
            String confFilePath;
            if (confFileIdx > -1) {
                confFilePath = process.getCmd().get(confFileIdx + 1);
//               if (confFilePath.indexOf("/conf/") > -1) {
//                    result = confFilePath.substring(0, confFilePath.indexOf("/conf/"));
//                } else if (confFilePath.indexOf("/httpd.conf") > -1) {
//                    result= confFilePath.substring(0, confFilePath.indexOf("/httpd.conf"));
//                }
                result = confFilePath;
//            } else {
//                result = enginePath + "/conf/httpd.conf";
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    /**
     * yum install
     */
    public class StepYumInstall extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            result = getIntancePath(targetHost, "httpd", process.getName(), commandConfig, strategy);
            return StringUtils.isNotEmpty(result);
        }

    }

    private String getIntancePath(TargetHost targetHost, String command, String processName, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        if (strategy.isWindows())
            return null;

//        String cmdResult = SSHUtil.executeCommand(targetHost, "which " + command);
        String cmdResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.COMMAND_WHICH, commandConfig, strategy, command);

        if (StringUtils.isEmpty(cmdResult) && !command.equals(processName)) {
//            cmdResult = SSHUtil.executeCommand(targetHost, "which " + processName);
            cmdResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.COMMAND_WHICH, commandConfig, strategy, processName);
        }

        if (StringUtils.isNotEmpty(cmdResult)) {
//            cmdResult = SSHUtil.executeCommand(targetHost, cmdResult + " -V");
            try {
                String execCmd = cmdResult + " -V";
                RemoteExecResult result = SSHUtil2.runCommand(targetHost, execCmd, true);
                if (result.isErr()) {
                    log.error("error: {} - {}", execCmd, result.getError());
                    return null;
                }

                cmdResult = result.getResult();
            } catch (JSchException | IOException e) {
                throw new RoRoException(e);
            }
            String[] lines = cmdResult.split(strategy.getCarriageReturn());

            StringBuilder sb = new StringBuilder();
            extractValue("HTTPD_ROOT", lines, sb);
            sb.append('/');
            extractValue("SERVER_CONFIG_FILE", lines, sb);

            if (sb.length() > 0) {
                return sb.toString();
            }
        }

        return null;
    }

    private void extractValue(String key, String[] lines, StringBuilder sb) {
        for (String line : lines) {
            int index = line.indexOf(key);
            if (index > -1) {
                String value = line.substring(index + key.length() + 2);
                sb.append(StringUtils.strip(value, "\""));
            }
        }
    }

    public class StepAptInstall extends ExtractScenario {
        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            result = getIntancePath(targetHost, "apache2", process.getName(), commandConfig, strategy);
            return StringUtils.isNotEmpty(result);
        }
    }

    public class Step2 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
//            String command = "sudo " + enginePath + httpdParentDir + "/httpd -D DUMP_INCLUDES 2> /dev/null";
//            String cmdResult = SSHUtil.executeCommand(targetHost, command);

            String separator = strategy.getSeparator();
            String command = enginePath + httpdParentDir + separator + "httpd";
            String cmdResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.APACHE_DUMP, commandConfig, strategy, command);

            if (StringUtils.isEmpty(cmdResult)) {
//                command = "sudo " + enginePath + httpdParentDir + "/apachectl -D DUMP_INCLUDES 2> /dev/null";
//                cmdResult = SSHUtil.executeCommand(targetHost, command);
                command = enginePath + httpdParentDir + separator + "apachectl";
                cmdResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.APACHE_DUMP, commandConfig, strategy, command);
            }

            if (StringUtils.isNotEmpty(cmdResult)) {
                for (String conf : cmdResult.split("\n")) {
                    conf = StringUtils.strip(conf, "\r");
                    if (conf.contains("(*)")) {
//                        String confFilePath = conf.substring(conf.indexOf("/")).strip();
//                        result = confFilePath.substring(0, confFilePath.indexOf("/conf/"));
                        String[] splitedConf = conf.split("\\s+");
                        result = StringUtils.strip(splitedConf[splitedConf.length - 1], StringUtils.SPACE);
                        if (strategy.isWindows()) {
                            result = result.replaceAll("/", "\\\\");
                        }

                        break;
                    }
                }
            }

            return StringUtils.isNotEmpty(result);
        }
    }


    public class Step3 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            String command = "sudo find " + enginePath + " -type f -path '*bin/apachectl' 2> /dev/null";
            String filePath = SSHUtil.executeCommand(targetHost, command);

            if (StringUtils.isNotEmpty(filePath)) {
                if (filePath.contains("/bin/apachectl")) {
                    result = filePath.substring(0, filePath.indexOf("/bin/apachectl"));
                } else if (filePath.contains("/sbin/apachectl")) {
                    result = filePath.substring(0, filePath.indexOf("/sbin/apachectl"));
                }
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    public class Step4 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            String command = "sudo find " + enginePath + " -type f -path '*bin/httpd' 2> /dev/null";
            String filePath = SSHUtil.executeCommand(targetHost, command);

            if (StringUtils.isNotEmpty(filePath)) {
                if (filePath.contains("/bin/apachectl")) {
                    result = filePath.substring(0, filePath.indexOf("/bin/httpd"));
                } else if (filePath.contains("/sbin/apachectl")) {
                    result = filePath.substring(0, filePath.indexOf("/sbin/httpd"));
                }
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    public class Step5 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            String separator = strategy.getSeparator();
//            String command = "sudo find " + enginePath + " -type f -path '*conf/httpd.conf' 2> /dev/null";
//            String filePath = SSHUtil.executeCommand(targetHost, command);
            if (StringUtils.isEmpty(enginePath))
                return false;

            String path = strategy.isWindows() ? "conf" : "*conf";
            String filePath = MWCommonUtil.getExecuteResult(targetHost, COMMAND.FIND_FILE_WITH_PATH1, commandConfig, strategy, enginePath, path + separator + "httpd.conf");
            if (strategy.isWindows() && StringUtils.isEmpty(result)) {
                filePath = MWCommonUtil.getExecuteResult(targetHost, COMMAND.FIND_FILE_WITH_PATH2, commandConfig, strategy, enginePath, path + separator + "httpd.conf");
            }

            if (StringUtils.isNotEmpty(filePath)) {
//                    result = filePath.substring(0, filePath.indexOf("/conf/httpd.conf"));
                result = filePath;
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    public static class Step6 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            if (strategy.isWindows())
                return false;

            String separator = strategy.getSeparator();
//            String command = "sudo find /etc/ -type f -path '*conf/httpd.conf' 2> /dev/null";
//            String filePath = SSHUtil.executeCommand(targetHost, command);
            String path = strategy.isWindows() ? "conf" : "*conf";
            String filePath = MWCommonUtil.getExecuteResult(targetHost, COMMAND.FIND_FILE_WITH_PATH1, commandConfig, strategy, "/etc", path + separator + "httpd.conf");

            if (StringUtils.isNotEmpty(filePath)) {
//                result = filePath.substring(0, filePath.indexOf("/conf/httpd.conf"));
                result = filePath;
            }

            return StringUtils.isNotEmpty(result);
        }
    }
}
//end of ApacheInstancePathScenario.java
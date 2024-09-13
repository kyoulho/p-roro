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
package io.playce.roro.discover.middleware.scenario.weblogic;

import io.playce.roro.common.util.SSHUtil;
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
public class WebLogicDomainPathScenario {


    public static class Step1 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            if (StringUtils.isNotEmpty(ProcessCmdUtil.getParam(process.getCmd(), "-Dweblogic.system.BootIdentityFile"))) {
                String paramValue = ProcessCmdUtil.getParam(process.getCmd(), "-Dweblogic.system.BootIdentityFile");
                String separator = strategy.getSeparator();

                int idx = paramValue.lastIndexOf("domains" + separator);
                if (idx > -1) {
                    String domain = paramValue.substring(idx);

                    result = paramValue.substring(0, idx) + domain.substring(0, domain.indexOf(separator, domain.indexOf(separator) + 1));
                }
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    public static class Step2 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            if (strategy.isWindows()) {
                return false;
            }

            String weblogicName = ProcessCmdUtil.getParam(process.getCmd(), "-Dweblogic.Name");

            if (StringUtils.isEmpty(weblogicName)) {
                return false;
            }

            // ps -ef | grep java | grep weblogic.Server | grep weblogic.Name={MIDDLEWARE_NAME} | grep -v grep | awk {'print $2" "$3'}로 프로세스 ID 확인
            // ps -ef | grep ${PARENT_PID} | grep -v ${PID} |  grep -v grep 를 실행하여 startWebLogic.sh 파일의 위치를 확인한다.
            // (/bin/startWebLogic.sh 앞의 경로가 DOMAIN_HOME이 된다.)

            String command = "sudo ps -ef | grep java | grep weblogic.Server | grep weblogic.Name=" + weblogicName + " | grep -v grep | awk {'print $2\" \"$3'}";
            String cmdResult = SSHUtil.executeCommand(targetHost, command);

            if (StringUtils.isNotEmpty(cmdResult)) {
                String[] pids = cmdResult.split(StringUtils.SPACE);

                if (pids.length == 2) {
                    command = "sudo ps -ef | grep " + pids[1] + " | grep -v " + pids[0] + " | grep -v grep";
                    cmdResult = SSHUtil.executeCommand(targetHost, command);

                    if (StringUtils.isNotEmpty(cmdResult) && cmdResult.contains("startWebLogic.sh")) {
                        String[] items = cmdResult.split("\\s+");

                        for (String item : items) {
                            if (item.contains("/bin/startWebLogic.sh")) {
                                int idx = item.indexOf("/bin/startWebLogic.sh");
                                result = item.substring(0, idx);
                                break;
                            }
                        }
                    }
                }
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    public static class Step3 extends ExtractScenario {
        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            if (strategy.isWindows()) {
                String paramValue = ProcessCmdUtil.getParam(process.getCmd(), "-Djava.security.policy");
                if (StringUtils.isNotEmpty(paramValue)) {
                    int wlserverIndex = paramValue.toLowerCase().indexOf("wlserv");
                    if (wlserverIndex > -1) {
                        result = paramValue.substring(0, wlserverIndex) + "user_projects\\domains\\base_domain";
                    }
                }
                return StringUtils.isNotEmpty(result);
            } else {
                // AIX 등 UNIX에는 readlink 명령이 없을 수 있음.
                String command = "sudo readlink -f /proc/" + process.getPid() + "/cwd";
                String cwd = SSHUtil.executeCommand(targetHost, command);

                if (StringUtils.isNotEmpty(cwd)) {
                    result = cwd.strip();
                }
                return StringUtils.isNotEmpty(result);
            }
        }
    }
}
//end of WebLogicDomainPathScenario.java
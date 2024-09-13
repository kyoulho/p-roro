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
 * Hoon Oh       2월 15, 2022            First Draft.
 */
package io.playce.roro.discover.middleware.scenario.jeus;

import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.stream.Collectors;

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
public class JeusVersionScenario extends ExtractScenario {

    private String enginePath;

    public JeusVersionScenario(String enginePath) {
        this.enginePath = enginePath;
    }

    @Override
    protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
        String separator = strategy.getSeparator();
        try {
            if (StringUtils.isNotEmpty(enginePath)) {
//                String command;
//                if (SSHUtil.isSudoer(targetHost) || StringUtils.isNotEmpty(targetHost.getRootPassword())) {
//                    command = "sudo su - " + process.getUser() + " /bin/sh -c '" + solutionPath + "/bin/jeusadmin -version | egrep \"^JEUS\" | head -1'";
//                } else {
//                    command = "/bin/sh -c '" + solutionPath + "/bin/jeusadmin -version | egrep \"^JEUS\" | head -1'";
//                }
//                String version = SSHUtil.executeCommand(targetHost, command);

                String version;
                if (strategy.isWindows()) {
                    version = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_VERSION_SCENARIO2, commandConfig, strategy, enginePath);
                } else {
                    if (SSHUtil.isSudoer(targetHost) || StringUtils.isNotEmpty(targetHost.getRootPassword())) {
                        version = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_VERSION_SCENARIO1, commandConfig, strategy, process.getUser(), enginePath);

                        if (StringUtils.isEmpty(version)) {
                            // https://cloud-osci.atlassian.net/browse/PCR-5681
                            // process.getUser()가 숫자이거나, +로 끝나는지 확인한다.

                            String username = null;
                            if (NumberUtils.isDigits(process.getUser())) {
                                username = MWCommonUtil.getExecuteResult(targetHost, COMMAND.GET_USERNAME_FROM_UID, commandConfig, strategy, process.getUser());
                            } else if (process.getUser().endsWith("+")) {
                                username = MWCommonUtil.getExecuteResult(targetHost, COMMAND.GET_USERNAME_FROM_USER, commandConfig, strategy, process.getUser().replaceAll("\\+", StringUtils.EMPTY));

                                // username이 여러개 나오는 경우 첫번째 user를 대상으로 한다.
                                // TODO 전체 User를 대상으로 처리
                                username = username.lines().limit(1).collect(Collectors.toList()).get(0);
                            } else {
                                // AIX 에서는 --shell 옵션이 없음
                                version = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_VERSION_SCENARIO3, commandConfig, strategy, process.getUser(), enginePath);
                            }

                            if (StringUtils.isNotEmpty(username)) {
                                version = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_VERSION_SCENARIO1, commandConfig, strategy, username, enginePath);

                                if (StringUtils.isEmpty(version)) {
                                    // AIX 에서는 --shell 옵션이 없음
                                    version = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_VERSION_SCENARIO3, commandConfig, strategy, username, enginePath);
                                }
                            }
                        }
                    } else {
                        version = MWCommonUtil.getExecuteResult(targetHost, COMMAND.JEUS_VERSION_SCENARIO2, commandConfig, strategy, enginePath);
                    }
                }

                if (StringUtils.isNotEmpty(version)) {
                    result = version.split("\\s")[1];
                }
            }
        } catch (Exception e) {
            log.warn("Jeus version check failed. Please check \"" +
                    "[" + targetHost.getUsername() + "@" + targetHost.getIpAddress() + " ~]$ "
                    + enginePath + separator + "bin" + separator + "jeusadmin -version\" command is valid.");
        }
        return StringUtils.isNotEmpty(result);
    }
}
//end of JeusVersionScenario.java
package io.playce.roro.discover.middleware.scenario.webtob;/*
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
 * Hoon Oh       1월 31, 2022            First Draft.
 */

import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.discover.server.util.ProcessCmdUtil;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.List;
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
public class WebToBEnginePathScenario {

    public static class Step1 extends ExtractScenario {
//        private final CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();


        /**
         * WebToB solution path
         * <p>
         * 1. ps -efx 를 통해 hidden option이 있을 경우.
         */
        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            int ppid = ProcessCmdUtil.getIndexFromString(process.getCmd(), "-b") + 1;
//            String command = "ps -efx | grep " + process.getCmd().get(ppid);
//            String processes = SSHUtil.executeCommand(targetHost, command);
            String processName = process.getName();
//            if(strategy.isWindows()) {
//                int index = processName.lastIndexOf(".");
//
//                if(index > -1) {
//                    processName = processName.substring(0, index);
//                }
//            }

            List<String> options = process.getCmd();
            if (!strategy.isWindows()) {
                String processes = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_ENGINE_PATH_SCENARIO_STEP1, commandConfig, strategy, process.getCmd().get(ppid), processName);
                String[] splitProcess = processes.split(StringUtils.SPACE);
                options = Arrays.stream(splitProcess).map(String::strip).collect(Collectors.toList());
            }

            String webtobDir = null;
//            String[] commandLines;
//            try {
//                commandLines = parser.parseLine(processes);
//            } catch (IOException e) {
//                return false;
//            }

            for (String option : options) {
                if (option.contains("WEBTOBDIR=")) {
                    webtobDir = option;
                    break;
                }
            }

            if (StringUtils.isNotEmpty(webtobDir)) {
                webtobDir = webtobDir.replaceAll("export ", StringUtils.EMPTY);
                webtobDir = webtobDir.split("=")[1];
                result = StringUtils.strip(webtobDir, StringUtils.SPACE + "\"");
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    public static class Step2 extends ExtractScenario {
        /**
         * WebToB solution path
         * <p>
         * 1. 환경변수에서 WEBTOBDIR 경로 추
         */
        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            String webtobDir;
            if (strategy.isWindows()) {
                webtobDir = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_ENGINE_PATH_SCENARIO_STEP21, commandConfig, strategy);
            } else {
//                String command = "env | grep WEBTOBDIR";
//                if (StringUtils.isNotEmpty(process.getUser()) && !process.getUser().equals(targetHost.getUsername())) {
//                    boolean isSudoer = SSHUtil.isSudoer(targetHost) || StringUtils.isNotEmpty(targetHost.getRootPassword());
//                    if (isSudoer) {
//                        command = "sudo su - " + process.getUser() + " --shell /bin/sh -c '" + command + "'";
//                    }
//                }
//                String webtobDir = SSHUtil.executeCommand(targetHost, command);
                boolean isSudoer = SSHUtil.isSudoer(targetHost) || StringUtils.isNotEmpty(targetHost.getRootPassword());
                if (StringUtils.isNotEmpty(process.getUser()) && !process.getUser().equals(targetHost.getUsername()) && isSudoer) {
                    webtobDir = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_ENGINE_PATH_SCENARIO_STEP22, commandConfig, strategy, process.getUser());

                    if (StringUtils.isEmpty(webtobDir)) {
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
                            webtobDir = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_ENGINE_PATH_SCENARIO_STEP31, commandConfig, strategy, process.getUser());
                        }

                        if (StringUtils.isNotEmpty(username)) {
                            webtobDir = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_ENGINE_PATH_SCENARIO_STEP22, commandConfig, strategy, username);

                            if (StringUtils.isEmpty(webtobDir)) {
                                // AIX 에서는 --shell 옵션이 없음
                                webtobDir = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_ENGINE_PATH_SCENARIO_STEP31, commandConfig, strategy, username);
                            }
                        }
                    }
                } else {
                    webtobDir = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_ENGINE_PATH_SCENARIO_STEP21, commandConfig, strategy);
                }
            }

            if (StringUtils.isNotEmpty(webtobDir)) {
                webtobDir = webtobDir.replaceAll("export ", StringUtils.EMPTY);
                webtobDir = webtobDir.split("=")[1];
                result = webtobDir.trim();
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    public static class Step3 extends ExtractScenario {
        /**
         * WebToB solution path
         * <p>
         * 1. 환경변수에서 WEBTOBDIR 경로 추
         */
        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            String command = "sudo cat ~" + process.getUser() + "/.*profile | grep WEBTOBDIR | egrep -v \"^#\" | head -1";
            String webtobDir = SSHUtil.executeCommand(targetHost, command);

            if (StringUtils.isNotEmpty(webtobDir) && !webtobDir.startsWith("WEBTOBDIR")) {
                webtobDir = webtobDir.replaceAll("export ", "");
                webtobDir = webtobDir.split("=")[1];
                result = webtobDir.trim();
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    public static class Step4 extends ExtractScenario {
        /**
         * WebToB solution path
         * <p>
         * 1. 환경변수에서 WEBTOBDIR 경로 추
         */
        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            String command = "sudo cat ~" + process.getUser() + "/.*rc | grep WEBTOBDIR | egrep -v \"^#\" | head -1";
            String webtobDir = SSHUtil.executeCommand(targetHost, command);

            if (StringUtils.isNotEmpty(webtobDir) && !webtobDir.startsWith("WEBTOBDIR")) {
                webtobDir = webtobDir.replaceAll("export ", "");
                webtobDir = webtobDir.split("=")[1];
                result = webtobDir.trim();
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    /*
     * ToDo: AssessmentManager.getDefaultEnginePathList()
     * */
    public static class Step5 extends ExtractScenario {
        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
            // String command = "sudo find " + defaultPath + " -type f -path '*config/http.m' 2> /dev/null";
            // String result = SSHUtil.executeCommand(TargetHost.convert(unknownMiddleware.getServer()), command);
            //
            // if (StringUtils.isNotEmpty(result)) {
            //     if (result.indexOf("/config/http.m") > -1) {
            //         unknownMiddleware.setSolutionPath(result.substring(0, result.indexOf("/config/http.m")));
            //
            //         break;
            //     }
            // }
            // if (StringUtils.isNotEmpty(webtobDir) && !webtobDir.startsWith("WEBTOBDIR")) {
            //     webtobDir = webtobDir.replaceAll("export ", "");
            //
            //     webtobDir = webtobDir.split("=")[1];
            //
            //     result = webtobDir.trim();
            // }

            String cmdResult;
            if (strategy.isWindows()) {
                cmdResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.COMMAND_WHICH, commandConfig, strategy, process.getName());
            } else {
                if (SSHUtil.isSudoer(targetHost) || StringUtils.isNotEmpty(targetHost.getRootPassword())) {
                    // cmdResult = SSHUtil.executeCommand(targetHost, String.format("sudo su - %s /bin/sh -c 'which %s'", process.getUser(), process.getName()));
                    cmdResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_ENGINE_PATH_SCENARIO_STEP51, commandConfig, strategy, process.getUser(), process.getName());

                    if (StringUtils.isEmpty(cmdResult)) {
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
                            cmdResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_ENGINE_PATH_SCENARIO_STEP32, commandConfig, strategy, process.getUser(), process.getName());
                        }

                        if (StringUtils.isNotEmpty(username)) {
                            cmdResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_ENGINE_PATH_SCENARIO_STEP51, commandConfig, strategy, username, process.getName());

                            if (StringUtils.isEmpty(cmdResult)) {
                                // AIX 에서는 --shell 옵션이 없음
                                cmdResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.WEBTOB_ENGINE_PATH_SCENARIO_STEP32, commandConfig, strategy, username, process.getName());
                            }
                        }
                    }
                } else {
                    // cmdResult = SSHUtil.executeCommand(targetHost, String.format("source ~/.*profile && which %s", process.getName()));
                    cmdResult = MWCommonUtil.getExecuteResult(targetHost, COMMAND.COMMAND_WHICH, commandConfig, strategy, process.getName());
                }
            }

            String separator = strategy.getSeparator();
            String processName = process.getName() + (strategy.isWindows() ? ".exe" : StringUtils.EMPTY);
            int index = cmdResult.lastIndexOf(separator);
            if (StringUtils.isNotEmpty(cmdResult) && cmdResult.endsWith(processName)) {
                result = cmdResult.substring(0, index - 4); //separator + bin제거 ..
            }

            return StringUtils.isNotEmpty(result);
        }
    }

}
//end of WebToBSolutionPathScenario.java
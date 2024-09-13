/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Dong-Heon Han    Jun 10, 2022		First Draft.
 */

package io.playce.roro.common.util;

import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Slf4j
public class WinRmCommon {
    public static final String MULTI_CMD_SEPARATOR = "::MULTI::";
    public static final String FIND_STR = "::FIND_STR::";
    public static final String FIND_PID = "::FIND_PID::";
    public static final String FIND_REG_PATH = "::FIND_REG_PATH::";

    public static final String REG_MULTI_SZ = "REG_MULTI_SZ";

    // wmic process where ... /format:list 형태만을 위해서 만듦..
    public static List<String> getProcessList(TargetHost targetHost, String name, String wmicWhereCommmand) {
        String wmicResult = execute(targetHost, name, wmicWhereCommmand);
        log.trace("origin result: {}", wmicResult);
        if (StringUtils.isEmpty(wmicResult)) return List.of();

        List<String> wmic = Arrays.stream(wmicResult.split(StringUtils.LF)).map(a -> a.replaceAll(StringUtils.CR, StringUtils.EMPTY)).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        log.trace("change list: {}", wmic);

        int getIndex = wmicWhereCommmand.indexOf("get");
        int listIndex = wmicWhereCommmand.indexOf("/format:list");
        //정보항목: ex> get CommandList => 1
        //             get CommandList, ProcessId => 2

        if (getIndex > -1 && listIndex > -1) {
            wmicWhereCommmand = wmicWhereCommmand.substring(getIndex + 3, listIndex);
        }
        String[] fields = wmicWhereCommmand.split(",");
        int itemCnt = fields.length;
        log.trace("get data cnt: {}, fields: {}", itemCnt, Arrays.toString(fields));

        //결과
        List<String> result = new ArrayList<>();
        StringBuilder onelineProcess = new StringBuilder();
        int index = 0;
        for (String w : wmic) {
            if (index >= itemCnt) {
                result.add(onelineProcess.toString());
                onelineProcess = new StringBuilder();
                index = 0;
            }
            onelineProcess.append(StringUtils.SPACE).append(w);
            index++;
        }
        if (onelineProcess.length() > 0) {
            result.add(onelineProcess.toString());
        }
        return result;
    }

    public static List<String> processList(TargetHost targetHost, String name, String wmicCommand, String registryCommand, String... findstrs) {
        //0: 프로세스 command목록
        String wmicResult = execute(targetHost, name, wmicCommand);
        if (StringUtils.isEmpty(wmicResult)) return List.of();

        List<String> processList = getProcessList(targetHost, name, wmicCommand);
        processList = processList.stream().map(r -> {
            r = StringUtils.strip(r, " \r\n");

            if (findStringContains(findstrs, r)) {
                return r;
            } else {
                //1: Java가 아닌경우 registry조회
                String registryInfo = execute(targetHost, name, registryCommand);
                if (StringUtils.isEmpty(registryInfo)) {
                    log.error("check command: {}", registryCommand);
                    return r;
                }
                String[] regInfos = StringUtils.strip(registryInfo, " \r\n").split("\\s{4}");
                registryInfo = regInfos[regInfos.length - 1].replaceAll("\\\\0", "\" ").replaceAll("=", "=\"");

                return registryInfo + "\"" + StringUtils.SPACE + r;
            }
        }).collect(Collectors.toList());
        return processList;
    }

    private static boolean findStringContains(String[] findstrs, String r) {
        for (String finstr : findstrs) {
            if (r.contains(finstr)) return true;
        }
        return false;
    }

    @NotNull
    public static String execute(TargetHost targetHost, String commandName, String execCommands) {
        return execute(targetHost, commandName, execCommands, false);
    }

    public static String execute(TargetHost targetHost, String commandName, String execCommands, boolean isPowerShellPrompt) {
        try {
            if (isPowerShellPrompt) {
                log.debug("run powershell prompt [{}] - [{}]", commandName, execCommands);
                return WinRmUtils.executePsShell(targetHost, execCommands);
            } else {
                log.debug("run command prompt [{}] - [{}]", commandName, execCommands);
                return WinRmUtils.executeCommand(targetHost, execCommands);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;

    }
}
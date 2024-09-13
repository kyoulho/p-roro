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
 * Dong-Heon Han    Jun 02, 2022		First Draft.
 */

package io.playce.roro.mw.asmt.util;

import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.WinRmCommon;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.enums.COMMAND;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static io.playce.roro.common.util.ThreadLocalUtils.MW_SCAN_ERROR;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Getter
@Slf4j
public class WindowsInfoStrategy implements GetInfoStrategy {
    private static final String CUSTOM_ERROR = "ERROR:";
    private static final String TOMCAT_REG_PATH = "HKLM\\\\SOFTWARE\\\\Wow6432Node\\\\Apache Software Foundation";
    private static final String COMMAND_LINE = "CommandLine=";
    private static final String PID = "ProcessId=";

    private enum YN {
        EXISTS, NOT_EXISTS
    }

    private final boolean windows;

    public WindowsInfoStrategy(boolean windows) {
        this.windows = windows;
    }

    @Override
    public boolean isSudoer(TargetHost targetHost) throws InterruptedException {
        return false;
    }

    @Override
    public Map<String, RemoteExecResult> runCommands(TargetHost targetHost, Map<String, String> commandMap, boolean sudo) throws InterruptedException {
        Map<String, RemoteExecResult> resultMap = new HashMap<>();
        for (String key : commandMap.keySet()) {
            COMMAND cmd;

            try {
                cmd = COMMAND.valueOf(key);
            } catch (IllegalArgumentException e) {
                log.warn(e.getMessage());
                //continue;

                // 매핑되는 CMD가 없으면 기본 CMD로 세팅하여 설정된 command가 실행되도록 한다.
                // Domain.setInstanceRemoteConfigFiles(), Standalone.setInstanceRemoteConfigFiles() 로부터 수정됨.
                // JBossHelper.getConfFile(), JBossHelper.getConfFilePath() 에도 영향이 있음.
                cmd = COMMAND.CAT;
            }

            String command = commandMap.get(key);
            String result = executeCommand(targetHost, command, cmd);
            // TODO result 값으로 에러를 판단하기가 어려움. 정상적인 실행 결과의 값이 비어 있을 수도 있음.
            boolean error = isError(result);
            RemoteExecResult remoteExecResult = RemoteExecResult.builder()
                    .command(command)
                    .err(error)
                    .result(error ? null : result)
                    .error(error ? StringUtils.isEmpty(result) ? CUSTOM_ERROR + command : result.substring(CUSTOM_ERROR.length()) : null)
                    .build();
            resultMap.put(key, remoteExecResult);
            log.trace("==> key      : {}", key);
            log.trace("    command  : {}", remoteExecResult.getCommand());
            log.trace("    is error : {}", remoteExecResult.isErr());
            log.trace("       result: {}", remoteExecResult.getResult());
            log.trace("       error : {}", remoteExecResult.getError());
        }

        return resultMap;
    }

    private boolean isError(String result) {
        return StringUtils.isEmpty(result) || result.startsWith(CUSTOM_ERROR);
    }

    @Override
    public boolean isAbstractPath(String path) {
        if (StringUtils.isNotEmpty(path)) {
            return path.length() >= 2 && path.charAt(1) == ':';
        } else {
            return false;
        }
    }

    @Override
    public boolean checkVariable(String variable) {
        return variable.length() >= 2 && variable.startsWith("%") && variable.endsWith("%");
    }

    @Override
    public String getCarriageReturn() {
        return "\r\n";
    }

    @Override
    public String getSeparator() {
        return "\\";
    }

    @Override
    public String executeCommand(TargetHost targetHost, String execCommand, COMMAND cmd) throws InterruptedException {
        try {
            String result = getResult(targetHost, execCommand, cmd);
            result = StringUtils.strip(result);
            result = StringUtils.isNotEmpty(result) ? result.trim() : result;

            switch (cmd) {
                case CHECK_PATH:
                    return StringUtils.isEmpty(result) ? YN.EXISTS.name() : YN.NOT_EXISTS.name();

                case JAVA_VERSION_WITH_JAVAHOME:
                case JAVA_VERSION:
                    return extractJavaVersion(result);

                case PROCESS_STATUS:
                    return extractPid(result);

                case TOMCAT_CONFIG_CONTEXT:
                case TOMCAT_CONFIG_ENV:
                case TOMCAT_CONFIG_SERVER:
                case TOMCAT_CONFIG_SETENV:
                    if (StringUtils.isEmpty(result)) {
                        result = CUSTOM_ERROR + "There is no such file. file name:" + execCommand.substring(execCommand.lastIndexOf("\\") + 1, execCommand.lastIndexOf("\""));
                    }
                    return StringUtils.strip(result);
                case JAVA_PATH:
                    return getJavaPath(result);
                case EXECUTED_TIME:
                case JEUS_EXECUTED_TIME:
                case APACHE_EXECUTED_TIME:
                    return getExecuteTime(result);
                default:
                    return StringUtils.strip(result, getCarriageReturn());
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error(e.getMessage(), e);
            ThreadLocalUtils.add(MW_SCAN_ERROR, "Exception occurred while execute command. Detail : [" + e.getMessage() + "]");
        }
        return null;
    }

    private String escapeBackslash(String str) {
        return str.replaceAll("\\\\", "\\\\\\\\");
    }

    private String getExecuteTime(String value) {
        String[] splitedValues = value.split(getCarriageReturn());
        if (splitedValues.length == 0) {
            return StringUtils.strip(value);
        }

        String targetValue = splitedValues[0];
        targetValue = StringUtils.strip(targetValue, getCarriageReturn());

        String[] splitedValue = targetValue.split("=");
        String result = splitedValue[0];
        if (splitedValue.length == 2) {
            result = splitedValue[1];
        }
        return StringUtils.strip(result, StringUtils.SPACE + getCarriageReturn());
    }

    private String getJavaPath(String result) {
        int index = result.indexOf("java");
        if (index == -1)
            return null;

        int startIndex;
        if (result.charAt(index + 4) == '"') {
            startIndex = result.lastIndexOf("\"", index);
        } else {
            startIndex = result.lastIndexOf(StringUtils.SPACE, index);
        }
        return result.substring(startIndex + 1, index + 4);
    }

    @NotNull
    private String getResult(TargetHost targetHost, String execCommand, COMMAND cmd) {
        switch (cmd) {
            case CAT:
            case CAT_QUOTATION:
            case TOMCAT_CONFIG_SERVER:
            case TOMCAT_CONFIG_ENV:
            case TOMCAT_CONFIG_CONTEXT:
            case TOMCAT_CONFIG_SETENV:
            case JBOSS_STANDALONE_XML:
            case JBOSS_DOMAIN_XML:
            case JBOSS_DOMAIN_HOST_XML:
            case JBOSS_STANDALONE_SETUP_ENV:
            case JBOSS_WEB_XML:
                return WinRmCommon.execute(targetHost, cmd.name(), execCommand, true);
            case TOMCAT_JAVA_PATH:
                return getTomcatJavaPath(targetHost, execCommand, cmd.name());
            case TOMCAT_VMOPTION:
                return getTomcatJvmOptions(targetHost, execCommand, cmd.name());
            case TOMCAT_RUN_USER:
                return getTomcatRunUser(targetHost, execCommand, cmd.name());
            case JBOSS_RUN_USER:
                return getJBossRunUser(targetHost, execCommand, cmd.name());
            case RUN_USER:
                return getRunUser(targetHost, execCommand, cmd.name());
            case JEUS_DEPLOYED_DATE:
                return getDeployedDate(targetHost, execCommand, cmd.name());
            case APACHE_RUN_USER:
            case APACHE_RUN_USER1:
                return getApacheRunUser(targetHost, execCommand, cmd.name());
//                return getRunUser1(targetHost, execCommand);
            case JEUS_VERSION:
                return getJeusVersion(targetHost, execCommand, cmd.name());
            case JEUS_DOMAIN_PATH_SCENARIO_STEP31:
            case JEUS_DOMAIN_PATH_SCENARIO_STEP32:
                return getJeusDomainPath(targetHost, execCommand, cmd.name());
            case WEBTOB_RUN_USER:
                return getWebToBUser(targetHost, execCommand, cmd.name());
            case FIND_FILE_WITH_PATH1:
            case FIND_FILE_WITH_PATH2:
            case WEBTOB_FIND_CONFIG_FILE1:
            case WEBTOB_FIND_CONFIG_FILE2:
                return getWebToBConfigFileNames(targetHost, execCommand, cmd.name());
            case FILE_EXISTS:
                execCommand = execCommand.replaceAll("\\\\", "\\\\\\\\");
            default:
                return WinRmCommon.execute(targetHost, cmd.name(), execCommand);
        }
    }

    private String getWebToBConfigFileNames(TargetHost targetHost, String execCommand, String name) {
        String result = WinRmCommon.execute(targetHost, name, execCommand);
        if (StringUtils.isEmpty(result))
            return StringUtils.EMPTY;

        String[] splitedResult = result.split(getCarriageReturn());
        List<String> collect = Arrays.stream(splitedResult).filter(StringUtils::isNotEmpty).collect(Collectors.toList());

        List<String> combineCollect = new ArrayList<>();
        for (int i = 0; i < collect.size(); i += 2) {
            String directory = collect.get(i);
            String fileNames = collect.get(i + 1);
            String[] files = fileNames.split("\\s+");

            for (String file : files) {
                String filePath = getFilePath(directory, file);
                combineCollect.add(StringUtils.strip(filePath, getCarriageReturn()));
            }
        }
        return String.join(getCarriageReturn(), combineCollect);
    }

    @NotNull
    private String getFilePath(String directory, String fileName) {
        if (directory.contains("디렉토리")) {
            int index = directory.indexOf("디렉토리");
            directory = directory.substring(0, index);
            directory = StringUtils.strip(directory, StringUtils.SPACE + getCarriageReturn() + "\t");
        } else if (directory.contains("Directory of ")) {
            directory = directory.substring(13);
        } else {
            directory = directory.substring(14);
        }
        return directory + "\\" + fileName;
    }

    private String getWebToBUser(TargetHost targetHost, String execCommand, String name) {
        String result = WinRmCommon.execute(targetHost, name, execCommand);
        if (StringUtils.isEmpty(result))
            return StringUtils.EMPTY;

        String[] users = result.split("\\r\\n");
        String user = users[0];
        return StringUtils.strip(user.split("=")[1], " \";\r\n");
    }

    private String getJeusDomainPath(TargetHost targetHost, String commands, String name) {
        String[] execCommands = commands.split(WinRmCommon.MULTI_CMD_SEPARATOR);
        String dirCommand = execCommands[0];
        String exclude = execCommands[1];
        String result = WinRmCommon.execute(targetHost, name, dirCommand);
        if (StringUtils.isEmpty(result))
            return StringUtils.EMPTY;

        String[] splitedResult = result.split(getCarriageReturn());
        List<String> collect = Arrays.stream(splitedResult).filter(StringUtils::isNotEmpty).collect(Collectors.toList());

        String[] excludeNames = exclude.split(",");
        List<String> combineCollect = new ArrayList<>();
        for (int i = 0; i < collect.size(); i += 2) {
            String directory = collect.get(i);
            String fileName = collect.get(i + 1);
            String filePath = getFilePath(directory, fileName);
            if (checkFilePath(excludeNames, filePath))
                continue;
            combineCollect.add(StringUtils.strip(filePath, getCarriageReturn()));
        }
        return String.join(getCarriageReturn(), combineCollect);
    }

    private boolean checkFilePath(String[] excludeNames, String filePath) {
        for (String excludeName : excludeNames) {
            if (filePath.contains(excludeName))
                return true;
        }
        return false;
    }

    private String getJeusVersion(TargetHost targetHost, String commands, String name) {
        String[] execCommands = commands.split(WinRmCommon.MULTI_CMD_SEPARATOR);
//        String dummy = execCommands[0]; // username  unix 명령과 맞추기위해서.
        String command = execCommands[1];
        return WinRmCommon.execute(targetHost, name, command);
    }

//    private String getRunUser1(TargetHost targetHost, String execCommand) {
//        return getRunUser(targetHost, execCommand);
//    }

    private String getApacheRunUser(TargetHost targetHost, String execCommand, String name) {
        return getRunUser(targetHost, execCommand, name);
    }

    private String getDeployedDate(TargetHost targetHost, String commands, String name) {
        String[] execCommands = commands.split(WinRmCommon.MULTI_CMD_SEPARATOR);
        String fileCommand = execCommands[0];
        String directoryCommand = execCommands[1];
        String commadnResult = WinRmCommon.execute(targetHost, name, fileCommand);
        commadnResult = StringUtils.strip(commadnResult, getCarriageReturn());
        if (StringUtils.isEmpty(commadnResult)) {
            commadnResult = WinRmCommon.execute(targetHost, name, directoryCommand);
            commadnResult = StringUtils.strip(commadnResult, getCarriageReturn());
        }

        String[] splitedResult = commadnResult.split(getCarriageReturn());
        String result = splitedResult[0];
        if (splitedResult.length == 2) {
            result = splitedResult[1];
        }

        return StringUtils.strip(result, StringUtils.SPACE + getCarriageReturn());
    }

    private String getRunUser(TargetHost targetHost, String commands, String name) {
        String[] execCommands = commands.split(WinRmCommon.MULTI_CMD_SEPARATOR);
        String wmicCommand = execCommands[0];
        String getOwner = execCommands[1];
        String findStr = execCommands[2];

        String command = wmicCommand.replaceAll(WinRmCommon.FIND_STR, StringUtils.strip(escapeBackslash(findStr), "\""));
        String result = WinRmCommon.execute(targetHost, name, command);
        if (StringUtils.isEmpty(result))
            return StringUtils.EMPTY;

        result = StringUtils.strip(result, getCarriageReturn());
        String[] resultSplit = result.split(getCarriageReturn());
        Optional<String> first = Arrays.stream(resultSplit).filter(r -> {
            r = StringUtils.strip(r, getCarriageReturn());
            return StringUtils.isNotEmpty(r);
        }).findFirst();
        if (first.isPresent()) {
            String[] values = StringUtils.strip(first.get(), "\r").split("=");
            if (values.length != 2)
                return StringUtils.EMPTY;

            return getUser(targetHost, name, getOwner, values[1]);
        }
        return StringUtils.EMPTY;
    }

    @Nullable
    private String getUser(TargetHost targetHost, String name, String getOwner, String pid) {
        String result = WinRmCommon.execute(targetHost, name, getOwner.replaceAll(WinRmCommon.FIND_PID, pid));
        if (StringUtils.isEmpty(result))
            return StringUtils.EMPTY;

        return StringUtils.strip(result.split("=")[1], " \";\r\n");
    }

    private String getTomcatJavaPath(TargetHost targetHost, String commands, String name) {
        String[] execCommands = commands.split(WinRmCommon.MULTI_CMD_SEPARATOR);
        String wmicCommand = execCommands[0];
        String registryCommand = execCommands[1].replaceAll(WinRmCommon.FIND_REG_PATH, TOMCAT_REG_PATH);
        String domainPath = String.format("catalina.home=\"%s\"", execCommands[2]);

        String findedProcess = findTomcatProcess(targetHost, wmicCommand, name, registryCommand, domainPath, "jvm.dll");
        if (StringUtils.isEmpty(findedProcess)) {
            return StringUtils.EMPTY;
        }

        if (findedProcess.contains("jvm.dll")) {
            int index = findedProcess.indexOf("jvm.dll");
            if (index == -1) {
                return StringUtils.EMPTY;
            }
            //registry로 부터 찾은 Jvm 정보를 findedProcess 앞쪽에 붙여서 받음.
            return findedProcess.substring(0, index - 7) + "java.exe";
        } else {
            String[] splitArr = findedProcess.split("\\s+");
            for (String split : splitArr) {
                if (split.contains(":") && split.contains("java")) {
                    return split.substring(COMMAND_LINE.length());
                }
            }

            return StringUtils.EMPTY;
        }
    }

    private String getTomcatRunUser(TargetHost targetHost, String commands, String name) {
        String[] execCommands = commands.split(WinRmCommon.MULTI_CMD_SEPARATOR);
        String wmicCommand = execCommands[0];
        String registryCommand = execCommands[1].replaceAll(WinRmCommon.FIND_REG_PATH, TOMCAT_REG_PATH);
        String findUserCommand = execCommands[2];
        String domainPath = execCommands[3];

        String findedProcess = findTomcatProcess(targetHost, name, wmicCommand, registryCommand, domainPath);

        String[] procesArr = findedProcess.split("\\s+");
        String pid = getPid(procesArr);
        if (StringUtils.isEmpty(pid))
            return StringUtils.EMPTY;

        return getUser(targetHost, name, findUserCommand, pid);
    }

    private String getJBossRunUser(TargetHost targetHost, String commands, String name) {
        String[] execCommands = commands.split(WinRmCommon.MULTI_CMD_SEPARATOR);
        String wmicCommand = execCommands[0];
        String getOwner = execCommands[1];
        //String findStr = execCommands[2];

        String command = wmicCommand.replaceAll("\\\\", "\\\\\\\\");
        String result = WinRmCommon.execute(targetHost, name, command);
        if (StringUtils.isEmpty(result))
            return StringUtils.EMPTY;

        result = StringUtils.strip(result, getCarriageReturn());
        String[] resultSplit = result.split(getCarriageReturn());

        if (resultSplit.length > 0) {
            String[] values = resultSplit[1].split("=");
            if (values.length != 2)
                return StringUtils.EMPTY;

            return getUser(targetHost, name, getOwner, values[1]);
        }
        return StringUtils.EMPTY;
    }

    private String getPid(String[] processArr) {
        for (String processItem : processArr) {
            if (processItem.startsWith(PID)) {
                return processItem.substring(PID.length());
            }
        }
        return null;
    }

    private String getTomcatJvmOptions(TargetHost targetHost, String commands, String name) {
        String[] execCommands = commands.split(WinRmCommon.MULTI_CMD_SEPARATOR);
        String wmicCommand = execCommands[0];
        String registryCommand = execCommands[1].replaceAll(WinRmCommon.FIND_REG_PATH, TOMCAT_REG_PATH);
        String domainPath = execCommands[2];

        String findedProcess = findTomcatProcess(targetHost, name, wmicCommand, registryCommand, domainPath);
        if (StringUtils.isNotEmpty(findedProcess))
            return findedProcess;
        return StringUtils.EMPTY;
    }

    private String findTomcatProcess(TargetHost targetHost, String name, String wmicCommand, String registryCommand, String... findStrs) {
        List<String> resutlList = WinRmCommon.processList(targetHost, name, wmicCommand, registryCommand, "catalina.home");
        if (resutlList.isEmpty())
            return StringUtils.EMPTY;

        for (String findStr : findStrs) {
            String r = findProcessByFindStr(findStr, resutlList);
            if (r != null)
                return r;
        }
        return StringUtils.EMPTY;
    }

    @Nullable
    private String findProcessByFindStr(String domainPath, List<String> resutlList) {
        for (String r : resutlList) {
            if (r.contains(domainPath)) {
                r = StringUtils.strip(r);
                int index = r.indexOf(":");
                if (index > 1) {
                    r = r.substring(index - 2);
                }
                return r;
            }
        }
        return null;
    }

//    private String extractValue(String result, String fileStr) {
//        if(StringUtils.isEmpty(result)) return null;
//
//        int index = result.indexOf(fileStr);
//        if(index == -1) return null;
//
//        result = StringUtils.strip(result.substring(index + fileStr.length()), getCarriageReturn());
//        return StringUtils.strip(result);
//    }

    private String extractPid(String result) {
        //todo ?
        log.debug(result);
        return null;
    }

    private String extractJavaVersion(String result) {
        int index = result.indexOf("\"");
        if (index == -1)
            return result;

        result = result.substring(index);
        return StringUtils.strip(result, getCarriageReturn() + "\"");
    }

    @Override
    public String getShell() {
        return null;
    }

    @Override
    public String getWeblogicShellPath() {
        return null;
    }

    //windows의 경로를 unix에 맞도록 수정.  ex) C:\test => c/test
    @Override
    public String getParentDirectoryByPath(String workDir, String path) {
        path = path.replaceAll("\\\\", "/");
        path = path.replaceAll(":", StringUtils.EMPTY);

        return workDir + File.separator + path.substring(0, path.lastIndexOf("/"));
    }
}
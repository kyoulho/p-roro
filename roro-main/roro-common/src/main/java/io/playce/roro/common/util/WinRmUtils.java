/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Jeongho Baek   5월 10, 2021		First Draft.
 */
package io.playce.roro.common.util;

import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.xebialabs.overthere.*;
import io.cloudsoft.winrm4j.client.WinRmClientContext;
import io.cloudsoft.winrm4j.winrm.WinRmTool;
import io.cloudsoft.winrm4j.winrm.WinRmTool.Builder;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.AuthSchemes;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.BaseCifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Slf4j
public class WinRmUtils {

    public final static String POWERSHELL_VERSION = "PowerShellVersion";
    public final static String IS_TEMP_DIR_CREATED = "TempDirCreated";

    private static final String RORO_TEMP_DIR = CommonProperties.getWindowsTempDir();
    private static final String WRITE_OUT_FILE_CMD = " | Out-File " + RORO_TEMP_DIR + "\\";
    private static final String READ_OUT_FILE_CMD = "type " + RORO_TEMP_DIR + "\\";
    private static final String DELETE_OUT_FILE_CMD = "del " + RORO_TEMP_DIR + "\\";

    public static boolean healthCheck(InventoryProcessConnectionInfo inventoryProcessConnectionInfo) throws InterruptedException {

        String result = getMessage(inventoryProcessConnectionInfo);

        if (result.equals("hi")) {
            return true;
        } else if (result.contains("401")) {
            throw new RoRoException("Auth failed. Please check Username/Password or Auth Type.");
        } else if (result.contains("Connection refused")) {
            throw new RoRoException("WinRM connection refused. Please check Server IP");
        } else if (result.contains("Timeout") || result.contains("timeout")) {
            throw new RoRoException("WinRM connection timeout. Please check server port or firewall.");
        } else {
            throw new RoRoException("WinRM Connection Error. Please check server information or WinRM Config.");
        }

    }

    private static String getMessage(InventoryProcessConnectionInfo inventoryProcessConnectionInfo) throws InterruptedException {//throws Exception {
        WinRmClientContext context = WinRmClientContext.newInstance();
        String result = "";

        try {
            WinRmTool winRmTool = Builder.builder(inventoryProcessConnectionInfo.getRepresentativeIpAddress(), inventoryProcessConnectionInfo.getUserName(), inventoryProcessConnectionInfo.getUserPassword())
                    .authenticationScheme(AuthSchemes.BASIC)
                    .port(inventoryProcessConnectionInfo.getConnectionPort())
                    .useHttps(false)
                    .context(context)
                    .build();

            WinRmToolResponse response = winRmTool.executePs("echo hi");

            result = response.getStdOut();
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error(e.getMessage(), e);
            result = Throwables.getStackTraceAsString(e);
        } finally {
            context.shutdown();
        }

        return result.trim();
    }

    // Use Winrm4j Lib..

    public static String executeCommand(TargetHost targetHost, String command) throws Exception {
        String result = executeCommand(targetHost, Collections.singletonList(command));

        if (StringUtils.isNotEmpty(result)) {
            result = result.trim();
        }

        log.debug("WinRmUtils.executeCommand(\"{}\")'s Result : [{}]", command, result);

        return result;
    }

    public static String executeCommand(TargetHost targetHost, List<String> commands) throws Exception {
        WinRmClientContext context = WinRmClientContext.newInstance();
        try {
            WinRmTool winRmTool = getWinRmTool(targetHost, context);

            return getWinRm4jExecuteCommandResultString(winRmTool, commands);
        } finally {
            context.shutdown();
        }
    }

    private static String getWinRm4jExecuteCommandResultString(WinRmTool winRmTool, List<String> executeCommands) throws Exception {
        List<String> executeCommand = new ArrayList<>();
        // 특정 윈도우 버전(Server 2008)에서  Buffer Size가 작아서 Response 값이 짤려 나와서 수정을 함.(4K)
        executeCommand.add("mode con:cols=3840 lines=2146");
        executeCommand.addAll(executeCommands);

        WinRmToolResponse response = winRmTool.executeCommand(executeCommand);

        // Remove BOM
        return response.getStdOut().replaceAll("[\uFEFF-\uFFFF]", "");
    }

    public static String executePsShell(TargetHost targetHost, String command) throws Exception {
        // Out-File 추가로 인해 에러 발생 시 기존 Command를 재 실행하기 위해 별도로 저장한다.
        String newCommand = new String(command);

        boolean useOutFile = false;
        String tempFileName = null;

        int powerShellMajorVersion = ThreadLocalUtils.get(POWERSHELL_VERSION) == null ? 0 : (int) ThreadLocalUtils.get(POWERSHELL_VERSION);
        boolean isTemeDirCreated = ThreadLocalUtils.get(IS_TEMP_DIR_CREATED) == null ? false : (boolean) ThreadLocalUtils.get(IS_TEMP_DIR_CREATED);

        if (CommonProperties.getUseOutFile() && powerShellMajorVersion >= 3 && newCommand.toLowerCase().contains("convertto")) {
            try {
                tempFileName = RandomStringUtils.random(10, true, false) + ".txt";
                if (!isTemeDirCreated) {
                    executeCommand(targetHost, "mkdir " + RORO_TEMP_DIR);
                    ThreadLocalUtils.add(IS_TEMP_DIR_CREATED, true);
                }

                newCommand += WRITE_OUT_FILE_CMD + tempFileName + " -encoding UTF8";

                useOutFile = true;
            } catch (Exception e) {
                log.warn("Unable to make {} directory. [Reason] : {}", RORO_TEMP_DIR, e.getMessage());
            }
        }

        String result = null;
        try {
            result = executePsShell(targetHost, Collections.singletonList(newCommand));

            if (useOutFile && StringUtils.isNotEmpty(tempFileName)) {
                try {
                    result = executeCommand(targetHost, READ_OUT_FILE_CMD + tempFileName);
                } catch (Exception e) {
                    log.warn("Unable to read {} file. [Reason] : {}", RORO_TEMP_DIR + "\\" + tempFileName, e.getMessage());
                }
            }
        } catch (Exception e) {
            if (useOutFile && StringUtils.isNotEmpty(tempFileName)) {
                if (StringUtils.isEmpty(result) && !command.equals(newCommand)) {
                    // Out-File 추가로 인해 에러 발생 시 기존 Command를 재 실행한다.
                    result = executePsShell(targetHost, Collections.singletonList(command));
                }
            } else {
                log.error("Unhandled exception occurred while execute powershell command({}).", newCommand, e);
                throw e;
            }
        } finally {
            if (useOutFile && StringUtils.isNotEmpty(tempFileName)) {
                executeCommand(targetHost, DELETE_OUT_FILE_CMD + tempFileName);
            }
        }

        if (StringUtils.isNotEmpty(result)) {
            result = result.trim();
        }

        log.debug("WinRmUtils.executePsShell(\"{}\")'s Result : [{}]", newCommand, result);

        return result;
    }

    public static String executePsShell(TargetHost targetHost, List<String> commands) throws Exception {
        WinRmClientContext context = WinRmClientContext.newInstance();
        try {
            WinRmTool winRmTool = getWinRmTool(targetHost, context);

            return getWinRm4jExecutePsShellResultString(winRmTool, commands);
        } finally {
            context.shutdown();
        }
    }

    private static WinRmTool getWinRmTool(TargetHost targetHost, WinRmClientContext context) {
        return Builder.builder(targetHost.getIpAddress(), targetHost.getUsername(), targetHost.getPassword())
                .authenticationScheme(AuthSchemes.BASIC)
                .port(targetHost.getPort())
                .useHttps(false)
                .context(context)
                .build();
    }

    public static WinRmToolResponse execute(TargetHost targetHost, String executeCommand) throws Exception {
        return execute(targetHost, List.of(executeCommand));
    }

    public static WinRmToolResponse execute(TargetHost targetHost, List<String> executeCommands) throws Exception {
        WinRmClientContext context = WinRmClientContext.newInstance();
        try {
            WinRmTool winRmTool = getWinRmTool(targetHost, context);
            return getWinRmToolResponse(winRmTool, executeCommands);
        } finally {
            context.shutdown();
        }
    }

    private static String getWinRm4jExecutePsShellResultString(WinRmTool winRmTool, List<String> executeCommands) throws Exception {
        WinRmToolResponse response = getWinRmToolResponse(winRmTool, executeCommands);

        // Remove BOM
        return response.getStdOut().replaceAll("[\uFEFF-\uFFFF]", "");
    }

    private static WinRmToolResponse getWinRmToolResponse(WinRmTool winRmTool, List<String> executeCommands) {
        List<String> executeSettingCommand = getSettingCommand();
        executeSettingCommand.addAll(executeCommands);

        return winRmTool.executePs(executeSettingCommand);
    }


    // Use overthere Lib
    public static String executePsShellByOverThere(TargetHost targetHost, String command) throws Exception {
        return executePsShellByOverThere(targetHost, Collections.singletonList(command));
    }

    public static String executePsShellByOverThere(TargetHost targetHost, List<String> commands) throws Exception {
        // Out-File 추가로 인해 에러 발생 시 기존 Command를 재 실행하기 위해 별도로 저장한다.
        List<String> newCommands = new ArrayList<>(commands);

        ConnectionOptions options = new ConnectionOptions();

        options.set(ADDRESS, targetHost.getIpAddress());
        options.set(USERNAME, targetHost.getUsername());
        options.set(PASSWORD, targetHost.getPassword());
        options.set(PORT, targetHost.getPort());
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, WINRM_INTERNAL);

        boolean useOutFile = false;
        String tempFileName = null;

        int powerShellMajorVersion = ThreadLocalUtils.get(POWERSHELL_VERSION) == null ? 0 : (int) ThreadLocalUtils.get(POWERSHELL_VERSION);
        boolean isTemeDirCreated = ThreadLocalUtils.get(IS_TEMP_DIR_CREATED) == null ? false : (boolean) ThreadLocalUtils.get(IS_TEMP_DIR_CREATED);

        if (CommonProperties.getUseOutFile() && powerShellMajorVersion >= 3 && newCommands.stream().filter(c -> c.toLowerCase().contains("convertto")).count() > 0) {
            try {
                tempFileName = RandomStringUtils.random(10, true, false) + ".txt";
                if (!isTemeDirCreated) {
                    executeCommand(targetHost, "mkdir " + RORO_TEMP_DIR);
                    ThreadLocalUtils.add(IS_TEMP_DIR_CREATED, true);
                }

                if (newCommands.size() == 1) {
                    String command = newCommands.get(0);
                    command += WRITE_OUT_FILE_CMD + tempFileName + " -encoding UTF8";

                    newCommands = Collections.singletonList(command);
                } else {
                    boolean isExists = false;
                    for (int i = 0; i < newCommands.size(); i++) {
                        if (newCommands.get(i).toLowerCase().contains("convertto")) {
                            isExists = true;
                            break;
                        }
                    }

                    if (isExists) {
                        String command = newCommands.get(newCommands.size() - 1);
                        command += WRITE_OUT_FILE_CMD + tempFileName + " -encoding UTF8";
                        newCommands.set(newCommands.size() - 1, command);
                    }
                }

                useOutFile = true;
            } catch (Exception e) {
                log.warn("Unable to make {} directory. [Reason] : {}", RORO_TEMP_DIR, e.getMessage());
            }
        }

        String result = null;
        try {
            result = getOverThereExecutePsShellResultString(options, newCommands);

            if (useOutFile && StringUtils.isNotEmpty(tempFileName)) {
                try {
                    result = executeCommand(targetHost, READ_OUT_FILE_CMD + tempFileName);
                } catch (Exception e) {
                    log.warn("Unable to read {} file. [Reason] : {}", RORO_TEMP_DIR + "\\" + tempFileName, e.getMessage());
                }
            }
        } catch (Exception e) {
            if (useOutFile && StringUtils.isNotEmpty(tempFileName)) {
                // Out-File 추가로 인해 에러 발생 시 기존 Command를 재 실행한다.
                result = getOverThereExecutePsShellResultString(options, commands);
            } else {
                log.error("Unhandled exception occurred while execute powershell command({}).", newCommands, e);
            }
        } finally {
            if (useOutFile && StringUtils.isNotEmpty(tempFileName)) {
                executeCommand(targetHost, DELETE_OUT_FILE_CMD + tempFileName);
            }
        }

        return result;
    }

    private static String getOverThereExecutePsShellResultString(ConnectionOptions options, List<String> executeCommands) throws Exception {
        String result;

        List<String> executeSettingCommand = getSettingCommand();
        executeSettingCommand.addAll(executeCommands);

        try (OverthereConnection connection = Overthere.getConnection("cifs", options)) {
            OverthereProcess process = connection.startProcess(buildCmdLinePowershell(executeSettingCommand));

            // Remove BOM
            result = CharStreams.toString(new InputStreamReader(process.getStdout(), StandardCharsets.UTF_8)).replaceAll("[\uFEFF-\uFFFF]", "");
        }

        return result;
    }

    private static CmdLine buildCmdLinePowershell(List<String> psScript) {
        return buildCmdLinePowershell(joinPs(psScript));
    }

    private static CmdLine buildCmdLinePowershell(String psScript) {
        CmdLine cmdLine = new CmdLine();
        cmdLine.addRaw("powershell.exe -EncodedCommand");

        byte[] cmd = psScript.getBytes(StandardCharsets.UTF_16LE);
        String arg = DatatypeConverter.printBase64Binary(cmd);

        cmdLine.addRaw(arg);
        return cmdLine;
    }

    private static String joinPs(List<String> commands) {
        return join(commands, "\r\n", true);
    }

    private static String join(List<String> commands, String delim, boolean endWithDelim) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;

        String command;
        for (Iterator iterator = commands.iterator(); iterator.hasNext(); builder.append(command)) {
            command = (String) iterator.next();
            if (first) {
                first = false;
            } else {
                builder.append(delim);
            }
        }

        if (endWithDelim) {
            builder.append(delim);
        }

        return builder.toString();
    }


    // Common Command Setting
    private static List<String> getSettingCommand() {
        List<String> setCommand = new ArrayList<>();

        setCommand.add("$env:LC_ALL='C.UTF-8'");
        setCommand.add("[System.Console]::OutputEncoding = [System.Text.Encoding]::UTF8");

        // 특정 윈도우 버전(Server 2008)에서  Buffer Size가 작아서 Response 값이 짤려 나와서 수정을 함.(4K)
        setCommand.add("mode con:cols=3840 lines=2146");

        return setCommand;
    }
}
//end of WinRmSupport.java
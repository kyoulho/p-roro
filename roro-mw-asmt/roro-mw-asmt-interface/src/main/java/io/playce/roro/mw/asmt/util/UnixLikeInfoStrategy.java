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

import com.jcraft.jsch.JSchException;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.SSHUtil2;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.enums.COMMAND;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 * <p>
 *
 * @version 3.0
 */
@Getter
@Slf4j
public class UnixLikeInfoStrategy implements GetInfoStrategy {
    private final boolean windows;

    public UnixLikeInfoStrategy(boolean windows) {
        this.windows = windows;
    }

    @Override
    public boolean isSudoer(TargetHost targetHost) throws InterruptedException {
        return SSHUtil.isSudoer(targetHost);
    }

    @Override
    public Map<String, RemoteExecResult> runCommands(TargetHost targetHost, Map<String, String> commandMap, boolean sudo) throws JSchException, IOException, InterruptedException {
        Map<String, RemoteExecResult> resultMap = SSHUtil2.runCommands(targetHost, commandMap, sudo);
        resultMap.forEach((key, value) -> {
            log.trace("==> key      : {}", key);
            log.trace("    command  : {}", value.getCommand());
            log.trace("    is error : {}", value.isErr());
            log.trace("       result: {}", value.getResult());
            log.trace("       error : {}", value.getError());
        });
        return resultMap;
    }

    @Override
    public boolean isAbstractPath(String path) {
        return StringUtils.isEmpty(path) ? false : path.startsWith("/");
    }

    @Override
    public boolean checkVariable(String variable) {
        return variable.contains("$");
    }

    @Override
    public String getCarriageReturn() {
        return "\n";
    }

    @Override
    public String getSeparator() {
        return "/";
    }

//    @Override
//    public String getCarriageReturn() {
//        return "\\r";
//    }

    @Override
    public String executeCommand(TargetHost targetHost, String executeCommand, COMMAND cmd) throws InterruptedException {
        String result = SSHUtil.executeCommand(targetHost, executeCommand);
        result = StringUtils.isNotEmpty(result) ? result.trim() : result;
        return result;
    }

    @Override
    public String getShell() {
        return "/bin/sh";
    }

    @Override
    public String getWeblogicShellPath() {
        return "/bin/startWebLogic.sh";
    }

    @Override
    public String getParentDirectoryByPath(String workDir, String path) {
        return workDir + path.substring(0, path.lastIndexOf("/"));
    }
}
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
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.enums.COMMAND;

import java.io.IOException;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
public interface GetInfoStrategy {

    static GetInfoStrategy getStrategy(boolean windows) {
        if(windows) {
            return new WindowsInfoStrategy(true);
        }
        return new UnixLikeInfoStrategy(false);
    }

    boolean isSudoer(TargetHost targetHost) throws InterruptedException;

    Map<String, RemoteExecResult> runCommands(TargetHost targetHost, Map<String, String> commandMap, boolean sudo) throws JSchException, IOException, InterruptedException;

    boolean isAbstractPath(String path);

    boolean checkVariable(String variable);

    String getCarriageReturn();
    String getSeparator();

//    String getCarriageReturn();

    boolean isWindows();

    String executeCommand(TargetHost targetHost, String sudoExecuteCommand, COMMAND cmd) throws InterruptedException;

    String getShell();

    String getWeblogicShellPath();

    String getParentDirectoryByPath(String workDir, String path);
}
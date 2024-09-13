/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * Dong-Heon Han    Dec 27, 2021		First Draft.
 */

package io.playce.roro.common.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Slf4j
public class SSHUtil2 {

    public static List<RemoteExecResult> runCommands(TargetHost targetHost, List<String> commandList, boolean sudo) throws JSchException, IOException, InterruptedException {
        return runCommands(targetHost, false, commandList, sudo);
    }

    public static List<RemoteExecResult> runCommands(TargetHost targetHost, boolean tty, List<String> commandList, boolean sudo) throws JSchException, IOException, InterruptedException {
//        return runCmds(targetHost, tty, commandList, sudo);
//    }
//
//    public static List<RemoteExecResult> runCmds(TargetHost targetHost, boolean tty, List<String> commandList, boolean sudo) throws JSchException, IOException, InterruptedException {
        List<RemoteExecResult> results = new ArrayList<>(commandList.size());

        if (!sudo && SSHUtil.canExecuteCommandWithSu(targetHost)) {
            return SSHUtil.executeCommandsWithSu(targetHost, commandList, results);
        }

        Session session = null;
        try {
            session = SSHUtil.getSessionForTimeout(targetHost);

            for (int i = 0; i < commandList.size(); i++) {
                String cmd = commandList.get(i);
                RemoteExecResult result = runCommand(session, tty, Integer.toString(i), cmd, sudo);
                results.add(result);
            }
        } finally {
            SSHUtil.close(targetHost, null, session);
        }

        return results;
    }

    public static Map<String, RemoteExecResult> runCommands(TargetHost targetHost, Map<String, String> commandMap, boolean sudo) throws JSchException, IOException, InterruptedException {
        return runCommands(targetHost, false, commandMap, sudo);
    }

    public static Map<String, RemoteExecResult> runCommands(TargetHost targetHost, boolean tty, Map<String, String> commandMap, boolean sudo) throws JSchException, IOException, InterruptedException {
        return runCmds(targetHost, tty, commandMap, sudo);
    }

    public static Map<String, RemoteExecResult> runCmds(TargetHost targetHost, Map<String, String> commandMap, boolean sudo) throws JSchException, IOException, InterruptedException {
        return runCmds(targetHost, false, commandMap, sudo);
    }

    public static Map<String, RemoteExecResult> runCmds(TargetHost targetHost, boolean tty, Map<String, String> commandMap, boolean sudo) throws JSchException, IOException, InterruptedException {
        Map<String, RemoteExecResult> results = new HashMap<>();

        if (!sudo && SSHUtil.canExecuteCommandWithSu(targetHost)) {
            return SSHUtil.executeCommandsWithSu(targetHost, commandMap, results);
        }

        Session session = null;
        try {
            session = SSHUtil.getSessionForTimeout(targetHost);

            for (String key : commandMap.keySet()) {
                String cmd = commandMap.get(key);

                RemoteExecResult result = runCommand(session, tty, key, cmd, sudo);
                results.put(key, result);
                log.trace("[{}]: {}\nresult: [{}]\nerror: [{}]", result.getCommand(), result.isErr(), result.getResult(), result.getError());
            }
        } finally {
            SSHUtil.close(targetHost, null, session);
        }

        return results;
    }

    public static RemoteExecResult runCommand(TargetHost targetHost, String cmd, boolean sudo) throws JSchException, IOException, InterruptedException {
        Session session = SSHUtil.getSessionForTimeout(targetHost);

        try {
            if (SSHUtil.canExecuteCommandWithSu(targetHost)) {
                sudo = false;
            }

            return runCommand(session, false, null, cmd, sudo);
        } finally {
            SSHUtil.close(targetHost, null, session);
        }
    }

    private static RemoteExecResult runCommand(Session session, boolean tty, String key, String cmd, boolean sudo) throws JSchException, IOException, InterruptedException {
        cmd = cmd.replaceAll("/usr/bin/sudo ", "").replaceAll("sudo ", "");

        cmd = sudo && !session.getUserName().equals("root") ? "sudo " + cmd : cmd;

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        InputStream std = channel.getInputStream();
        InputStream err = channel.getErrStream();
        channel.setPty(tty);

        channel.setCommand(cmd);
        channel.connect();

        String stdStr = readStream(std);
        String errStr = readStream(err);

        RemoteExecResult result = RemoteExecResult.builder()
                .command(cmd)
                .key(key)
                .err(errStr.length() > 0 && stdStr.length() == 0) // TODO errStr 값으로 에러를 판단하기가 어려움. 오류 메시지가 비어 있을 수도 있음.
                .result(SSHUtil.checkResult(stdStr.trim()))
                .error(errStr.trim())
                .build();

        log.debug("execute command: [{}], \nresult : [{}], \nerrorMessage : [{}]", cmd, result.getResult(), result.getError());

        channel.disconnect();
        return result;
    }

    private static String readStream(InputStream is) throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        byte[] buff = new byte[1024];
        int i;
        try {
            while ((i = is.read(buff)) > 0) {
                sb.append(new String(buff, 0, i));
            }
        } catch (IOException e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
            throw new InterruptedException(e.getMessage());
//        } finally {
//            if (is != null) {
//                is.close();
//            }
        }
        return sb.toString();
    }
}
package io.playce.roro.common.util;

import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.dto.info.LinuxInfo;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.Map;

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
@Slf4j
public class SSHUtil2Test {
    @Test
    public void testUtil() throws Exception {
        TargetHost targetHost = new TargetHost();
        targetHost.setIpAddress("192.168.4.10");
        targetHost.setPort(22);
        targetHost.setUsername("wasup");
        targetHost.setPassword("jan01jan");

        Map<String, String> commandMap = new HashMap<>();
        commandMap.put("01", "uname -m");
        commandMap.put("02", "netstat -nap | grep LISTEN");
        commandMap.put("03", "cat /etc/shadow");
        commandMap.put("04", "vmstat -s");
        commandMap.put("05", "systemctl list-units --type service | tail -n+2 | head -n-6");
        commandMap.put("06", String.format("cat /etc/*-release 2>/dev/null | uniq | egrep '^%s='", LinuxInfo.INFO.ID.name()));

        StopWatch stopWatch = new StopWatch();

        stopWatch.start("old");
        for (String command : commandMap.values()) {
            String result = SSHUtil.executeCommand(targetHost, command);
            log.debug("[OLD, '{}'] ==> {}", command, result);
        }
        stopWatch.stop();

        stopWatch.start("new1");
        boolean sudo = SSHUtil.isSudoer(targetHost);
        Map<String, RemoteExecResult> results = SSHUtil2.runCommands(targetHost, commandMap, sudo);
        for (RemoteExecResult result : results.values()) {
            log.debug("[NEW1, '{}'] ==> {}", result.getKey(), result);
        }
        stopWatch.stop();

        targetHost.setUsername("roro");
        targetHost.setRootPassword("jan01jan");

        stopWatch.start("single command with su");
        if (SSHUtil.canExecuteCommandWithSu(targetHost)) {
            for (String command : commandMap.values()) {
                String result = SSHUtil.executeCommandWithSu(targetHost, command);
                log.debug("[single command with su, '{}'] ==> {}", command, result);
            }
        } else {
            log.error("User is already root or cannot su to \"root\" because authentication failure.");
        }
        stopWatch.stop();


        stopWatch.start("multi commands with su");
        if (SSHUtil.canExecuteCommandWithSu(targetHost)) {
            results = SSHUtil.executeCommandsWithSu(targetHost, commandMap, new HashMap<>());
            for (RemoteExecResult result : results.values()) {
                log.debug("[multi commands with su, '{}'] ==> {}", result.getKey(), result);
            }
        } else {
            log.error("User is already root or cannot su to \"root\" because authentication failure.");
        }
        stopWatch.stop();

        log.debug("result : {}", stopWatch.prettyPrint());
    }
}
package io.playce.roro.mw.asmt.util;

import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
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
 * Dong-Heon Han    Jun 08, 2022		First Draft.
 */

@Slf4j
class WindowsInfoStrategyTest {
    @Test
    void testCommand() throws Exception {
        TargetHost targetHost = getTargetHost();
        String cmd = "\"C:\\Program Files\\Java\\jre1.8.0_321\\bin\\java\" -version";
        String result = WinRmUtils.executeCommand(targetHost, cmd);
        log.debug("run: {},  result: {}", cmd, result);
    }

    @Test
    void testPs() throws Exception {
        TargetHost targetHost = getTargetHost();
        WinRmToolResponse res = WinRmUtils.execute(targetHost, "java -version");
        log.debug("status: {}, out: {}", res.getStatusCode(), res.getStdOut().replaceAll("[\uFEFF-\uFFFF]", ""));
    }

    @NotNull
    private TargetHost getTargetHost() {
        TargetHost targetHost = new TargetHost();
        targetHost.setUsername("roro");
        targetHost.setPassword("jan01jan");
        targetHost.setIpAddress("13.124.162.209");
        targetHost.setPort(5985);
        return targetHost;
    }
}
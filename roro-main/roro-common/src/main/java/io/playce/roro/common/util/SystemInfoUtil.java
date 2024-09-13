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
 * Dong-Heon Han    Dec 16, 2021		First Draft.
 */

package io.playce.roro.common.util;

import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.dto.info.LinuxInfo;
import io.playce.roro.common.dto.info.OSInfo;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
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
public class SystemInfoUtil {
    private static final String UNAME = "uname";

    public static OSInfo getOSInfo(TargetHost targetHost) throws InterruptedException {
        String uname = SSHUtil.executeCommand(targetHost, UNAME);
        uname = uname.replaceAll("-", "_");
        uname = uname.toUpperCase();

        Domain1013 inventoryDetailTypeCode = Domain1013.valueOf(uname);
        OSInfo osInfo = new OSInfo(inventoryDetailTypeCode);

        switch (inventoryDetailTypeCode) {
            case LINUX:
                LinuxInfo linuxInfo = getLinuxInfo(inventoryDetailTypeCode, targetHost);
                osInfo.setOsVersion(linuxInfo.getVersionOnly());
                break;

            case AIX:
                String aixVersion = SSHUtil.executeCommand(targetHost, "oslevel -q").replaceAll("\\.", "");
                osInfo.setOsVersion(aixVersion.substring(0, 2));
                break;

            case HP_UX:
                String hpuxVersion = SSHUtil.executeCommand(targetHost, "uname -a");
                int bindex = hpuxVersion.indexOf("B.");
                hpuxVersion = hpuxVersion.substring(bindex + 2, bindex + 7).replaceAll("\\.", "");
                osInfo.setOsVersion(hpuxVersion);
                break;

            case SUNOS:
                String solarisVersion = SSHUtil.executeCommand(targetHost, "uname -r");
                osInfo.setOsVersion(solarisVersion.substring(2));
                break;
        }

        return osInfo;
    }

    public static LinuxInfo getLinuxInfo(Domain1013 inventoryDetailTypeCode, TargetHost targetHost) throws InterruptedException {
        Map<String, String> commandMap = new HashMap<>();

        // grep에 의해 ansi color가 빨간색으로 지정된 내용을 제거한다.
        commandMap.put(LinuxInfo.INFO.ID.name(), String.format("cat /etc/*-release 2>/dev/null | uniq | egrep --color=never '^%s='", LinuxInfo.INFO.ID.name()));
        commandMap.put(LinuxInfo.INFO.ID_LIKE.name(), String.format("cat /etc/*-release 2>/dev/null | uniq | egrep --color=never '^%s='", LinuxInfo.INFO.ID_LIKE.name()));
        commandMap.put(LinuxInfo.INFO.VERSION_ID.name(), String.format("cat /etc/*-release 2>/dev/null | uniq | egrep --color=never '^%s='", LinuxInfo.INFO.VERSION_ID.name()));

        //*
        try {
            boolean sudo = SSHUtil.isSudoer(targetHost);
            Map<String, RemoteExecResult> results = SSHUtil2.runCommands(targetHost, commandMap, sudo);

            return new LinuxInfo(inventoryDetailTypeCode,
                    results.get(LinuxInfo.INFO.ID_LIKE.name()).getResult(),
                    results.get(LinuxInfo.INFO.VERSION_ID.name()).getResult(),
                    results.get(LinuxInfo.INFO.ID.name()).getResult()
            );
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
            throw new RoRoException(e.getMessage());
        }
        /*/
        com.jcraft.jsch.Session session = null;
        try {
            session = SSHUtil2.getSession(targetHost);
            session.connect();
            boolean sudo = SSHUtil.isSudoer(targetHost);
            Map<String, SSHExecResult> results = SSHUtil2.runCommands(session, commandMap, sudo);
            session.disconnect();

            return new LinuxInfo(inventoryDetailTypeCode,
                    results.get(LinuxInfo.INFO.ID_LIKE.name()).getResult(),
                    results.get(LinuxInfo.INFO.VERSION_ID.name()).getResult(),
                    results.get(LinuxInfo.INFO.ID.name()).getResult()
            );
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
            throw new RoRoException(e.getMessage());
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        //*/
    }
}
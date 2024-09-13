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
 * SangCheon Park   May 03, 2022		    First Draft.
 */
package io.playce.roro.common.util;

import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.dto.info.LinuxInfo;
import io.playce.roro.common.util.support.TargetHost;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class SSHUtilTest {

    public static void main(String[] args) throws InterruptedException {
        String step = "ALL";

        if (args.length > 0) {
            step = args[0];
        }

        String result;
        String command = "sudo cat /etc/shadow";

        TargetHost targetHost = new TargetHost();
        targetHost.setIpAddress("192.168.4.10");
        targetHost.setPort(22);
        targetHost.setUsername("roro");  // not sudoer
        // targetHost.setPassword("jan01jan");
        targetHost.setKeyFilePath("/Users/nices96/.ssh/osci-key.pem");
        targetHost.setRootPassword("jan01jan");

        if ("ALL".equals(step) || "1".equals(step)) {
            boolean canSu = SSHUtil.canExecuteCommandWithSu(targetHost);
            System.err.println("1. Can switch user to root : " + canSu);
        }

        if ("ALL".equals(step) || "2".equals(step)) {
            targetHost.setUsername("roro");  // not sudoer
            result = SSHUtil.executeCommandWithSu(targetHost, command);
            System.err.println("\n2. [" + targetHost.getUsername() + "@" + targetHost.getIpAddress() + " ~]$ su -");
            System.err.println("[root@" + targetHost.getIpAddress() + " ~]# " + command.replaceAll("sudo ", ""));
            System.err.println("=> [" + result + "]\n");
        }

        if ("ALL".equals(step) || "3".equals(step)) {
            targetHost.setUsername("roro");  // not sudoer
            result = SSHUtil.executeCommand(targetHost, command);
            System.err.println("\n3. [" + targetHost.getUsername() + "@" + targetHost.getIpAddress() + " ~]$ " + command);
            System.err.println("=> [" + result + "]\n");
        }

        if ("ALL".equals(step) || "4".equals(step)) {
            targetHost.setUsername("wasup"); // sudoer
            result = SSHUtil.executeCommand(targetHost, command);
            System.err.println("\n4. [" + targetHost.getUsername() + "@" + targetHost.getIpAddress() + " ~]$ " + command);
            System.err.println("=> [" + result + "]");
        }

        if ("ALL".equals(step) || "5".equals(step)) {
            // Multi Commands
            Map<String, String> commandMap = new HashMap<>();
            commandMap.put(LinuxInfo.INFO.uname.name(), "uname");
            commandMap.put(LinuxInfo.INFO.ID.name(), String.format("cat /etc/*-release 2>/dev/null | uniq | egrep '^%s='", LinuxInfo.INFO.ID.name()));
            commandMap.put(LinuxInfo.INFO.ID_LIKE.name(), String.format("cat /etc/*-release 2>/dev/null | uniq | egrep '^%s='", LinuxInfo.INFO.ID_LIKE.name()));
            commandMap.put(LinuxInfo.INFO.VERSION_ID.name(), String.format("cat /etc/*-release 2>/dev/null | uniq | egrep '^%s='", LinuxInfo.INFO.VERSION_ID.name()));
            commandMap.put("SHADOWS", "cat /etc/shadow");
            commandMap.put("PASSWD", "cut -f1 -d: /etc/passwd");
            commandMap.put("GROUP", "cat /etc/group | egrep -v '^#'");
            commandMap.put("NAMESERVER", "cat /etc/resolv.conf | egrep -v '^#'");
            commandMap.put("PROCESS", "ps -ef");
            commandMap.put("KERNEL", "/sbin/sysctl -a");

            Map<String, RemoteExecResult> resultMap = SSHUtil.executeCommandsWithSu(targetHost, commandMap, new HashMap<String, RemoteExecResult>());
            System.err.println("\n5. [" + targetHost.getUsername() + "@" + targetHost.getIpAddress() + " ~]$ " + commandMap);
            System.err.println("=> [" + resultMap + "]");
        }

        if ("ALL".equals(step) || "6".equals(step)) {
            // AIX
            // command = "cat /etc/security/passwd";
            // command = "/usr/sbin/lsconf";
            // command = "/usr/bin/cat /etc/security/passwd |egrep ':|password' | sed 's/password = //g' | tr -d '	'";
            // command = "entstat -d lo0 | egrep Hardware | uniq";
            // command = "/usr/sbin/rmsock f1000500001673b8 tcpcb";
            command = "/usr/sbin/lsvg -o | /usr/bin/xargs /usr/sbin/lsvg -p";
            targetHost.setIpAddress("192.168.13.42");
            targetHost.setUsername("roro");  // not sudoer
            targetHost.setPassword("jan01jan");
            result = SSHUtil.executeCommandWithSu(targetHost, command);
            System.err.println("\n6-1. [" + targetHost.getUsername() + "@" + targetHost.getIpAddress() + " ~]$ " + command);
            System.err.println("=> [" + result + "]");

            targetHost.setUsername("root");  // not sudoer
            targetHost.setPassword("jan01jan");
            result = SSHUtil.executeCommand(targetHost, command);
            System.err.println("\n6-2. [" + targetHost.getUsername() + "@" + targetHost.getIpAddress() + " ~]$ " + command);
            System.err.println("=> [" + result + "]");
        }

        if ("ALL".equals(step) || "7".equals(step)) {
            targetHost.setIpAddress("192.168.4.68");
            targetHost.setPort(22);
            targetHost.setUsername("root");
            targetHost.setPassword("root");

            String command1 = "/usr/sbin/swlist | egrep 'HPUX.*OE.*[AB].[0-9]+\\.[0-9]+'";
            String command2 = "netstat -in | tail -n+2 | awk '{print $1}' | xargs -I% ifconfig '%'";
            result = SSHUtil.executeCommand(targetHost, command1);
            System.err.println("7-1. HP-UX Distribution : " + result);

            result = SSHUtil.executeCommand(targetHost, command2);
            System.err.println("7-2. HP-UX Interface : " + result);

            targetHost.setUsername("roro");  // not sudoer
            targetHost.setPassword("jan01jan");
            targetHost.setRootPassword("root");

            result = SSHUtil.executeCommandWithSu(targetHost, command1);
            System.err.println("7-3. HP-UX Distribution : " + result);

            result = SSHUtil.executeCommandWithSu(targetHost, command2);
            System.err.println("7-4. HP-UX Interface : " + result);
        }
    }
}
//end of SSHUtilTest.java
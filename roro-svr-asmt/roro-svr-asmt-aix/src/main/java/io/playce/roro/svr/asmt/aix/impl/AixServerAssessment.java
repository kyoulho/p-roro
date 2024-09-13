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
 * Hoon Oh          11월 10, 2021		First Draft.
 */
package io.playce.roro.svr.asmt.aix.impl;

import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.dto.thirdparty.ThirdPartyDiscoveryResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.ThirdPartySolutionUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.AbstractServerAssessment;
import io.playce.roro.svr.asmt.AssessmentItems;
import io.playce.roro.svr.asmt.aix.AixCommand;
import io.playce.roro.svr.asmt.aix.util.DiskParserUtil;
import io.playce.roro.svr.asmt.dto.aix.*;
import io.playce.roro.svr.asmt.dto.common.config.Hosts;
import io.playce.roro.svr.asmt.dto.common.disk.Partition;
import io.playce.roro.svr.asmt.dto.common.hardware.CpuInfo;
import io.playce.roro.svr.asmt.dto.common.hardware.MemoryInfo;
import io.playce.roro.svr.asmt.dto.common.interfaces.InterfaceInfo;
import io.playce.roro.svr.asmt.dto.common.interfaces.Ipv4Address;
import io.playce.roro.svr.asmt.dto.common.interfaces.Ipv6Address;
import io.playce.roro.svr.asmt.dto.common.network.*;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import io.playce.roro.svr.asmt.dto.linux.security.DefInfo;
import io.playce.roro.svr.asmt.dto.result.AixAssessmentResult;
import io.playce.roro.svr.asmt.dto.user.Group;
import io.playce.roro.svr.asmt.dto.user.User;
import io.playce.roro.svr.asmt.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Component("AIXAssessment")
@Slf4j
public class AixServerAssessment extends AbstractServerAssessment {

    public Map<String, String> generateCommand() {
        Map<String, String> cmdMap = new HashMap<>();
        cmdMap.put(AssessmentItems.ARCHITECTURE.toString(), AixCommand.ARCHITECTURE);
        cmdMap.put(AssessmentItems.CRONTAB1.toString(), AixCommand.CRONTAB1);
        cmdMap.put(AssessmentItems.ULIMITS.toString(), AixCommand.ULIMITS);
        cmdMap.put(AssessmentItems.DISTRIBUTION.toString(), AixCommand.DISTRIBUTION_AIX);
        cmdMap.put(AssessmentItems.FIRMWARE_VERSION.toString(), AixCommand.FIRMWARE_VERSION);
        cmdMap.put(AssessmentItems.GROUPS.toString(), AixCommand.GROUPS);
        cmdMap.put(AssessmentItems.HOSTNAME.toString(), AixCommand.HOSTNAME);
        cmdMap.put(AssessmentItems.INTERFACES.toString(), AixCommand.INTERFACES);
        cmdMap.put(AssessmentItems.INTERFACES_DEFAULT_GATEWAY.toString(), AixCommand.INTERFACES_DEFAULT_GATEWAY);
        cmdMap.put(AssessmentItems.KERNEL.toString(), AixCommand.KERNEL);
        cmdMap.put(AssessmentItems.KERNEL_PARAM.toString(), AixCommand.KERNEL_PARAM);
        cmdMap.put(AssessmentItems.NET_LISTEN_PORT.toString(), AixCommand.NET_LISTEN_PORT);
        cmdMap.put(AssessmentItems.NET_TRAFFICS.toString(), AixCommand.NET_TRAFFICS);
        cmdMap.put(AssessmentItems.PARTITIONS.toString(), AixCommand.PARTITIONS);
        cmdMap.put(AssessmentItems.PARTITIONS_TYPE.toString(), AixCommand.PARTITIONS_TYPE);
        cmdMap.put(AssessmentItems.EXSTRA_PARTITIONS.toString(), AixCommand.EXSTRA_PARTITIONS);
        cmdMap.put(AssessmentItems.PROCESSES.toString(), AixCommand.PROCESSES);
        cmdMap.put(AssessmentItems.PROCESSOR_COUNT.toString(), AixCommand.PROCESSOR_COUNT);
        cmdMap.put(AssessmentItems.PROCESSOR.toString(), AixCommand.PROCESSOR);
        cmdMap.put(AssessmentItems.PROCESSOR_CORES.toString(), AixCommand.PROCESSOR_CORES);
        cmdMap.put(AssessmentItems.DMI_FACTS.toString(), AixCommand.DMI_FACTS);
        cmdMap.put(AssessmentItems.SHADOWS.toString(), AixCommand.SHADOW);
        cmdMap.put(AssessmentItems.TIMEZONE1.toString(), AixCommand.TIMEZONE1);
        cmdMap.put(AssessmentItems.MEMORY_FACTS.toString(), AixCommand.MEMORY_FACTS);
        cmdMap.put(AssessmentItems.SWAP_FACTS.toString(), AixCommand.SWAP_FACTS);
        cmdMap.put(AssessmentItems.ROUTE_TABLE.toString(), AixCommand.ROUTE_TABLE);
        cmdMap.put(AssessmentItems.USERS.toString(), AixCommand.USERS);
        cmdMap.put(AssessmentItems.USER_LIST.toString(), AixCommand.USER_LIST);
        cmdMap.put(AssessmentItems.ENV.toString(), AixCommand.ENV);
        cmdMap.put(AssessmentItems.DNS.toString(), AixCommand.DNS);
        cmdMap.put(AssessmentItems.LOCALE.toString(), AixCommand.LOCALE);
        cmdMap.put(AssessmentItems.HOSTS.toString(), AixCommand.HOSTS);
        cmdMap.put(AssessmentItems.FILESYSTEM.toString(), AixCommand.FILESYSTEM);
        cmdMap.put(AssessmentItems.SECURITY_LOGIN.toString(), AixCommand.SECURITY_LOGIN);
        cmdMap.put(AssessmentItems.SECURITY_PASSWORD.toString(), AixCommand.SECURITY_PASSWORD);
        cmdMap.put(AssessmentItems.DAEMON_LIST.toString(), AixCommand.DAEMON_LIST);
        cmdMap.put(AssessmentItems.LVM_VGS.toString(), AixCommand.LVM_VGS);
        cmdMap.put(AssessmentItems.UPTIME.toString(), AixCommand.UPTIME);
        cmdMap.put(AssessmentItems.OS_FAMILY.toString(), AixCommand.OS_FAIMLY);
        cmdMap.put(AssessmentItems.PACKAGES.toString(), AixCommand.PACKAGES);
        return cmdMap;
    }

    public String getArchitecture(String result, Map<String, String> errorMap) throws InterruptedException {
        try {
            result = result.replaceAll("\n", "");
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.ARCHITECTURE.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result;
    }

    public Map<String, String> getCrontabs(TargetHost targetHost, String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, String> cronTab = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                for (String path : result.split("\n")) {
                    String contents = SSHUtil.executeCommand(targetHost, "/usr/bin/cat " + path);
                    cronTab.put(path, contents);
                }
            }

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.CRONTAB1.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return cronTab;
    }

    public Map<String, Map<String, String>> getUlimits(TargetHost targetHost, String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, Map<String, String>> ulimits = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                result = result.replaceAll("\\t", "");

                if ("root".equals(targetHost.getUsername()) || StringUtils.isNotEmpty(targetHost.getRootPassword()) || SSHUtil.isSudoer(targetHost)) {
                    Map<String, String> ulimit = null;

                    List<String> passwordList = ResultUtil.removeCommentLine(result);
                    for (String line : passwordList) {

                        if (line.contains(":")) {
                            String user = line.split(":")[0];
                            ulimit = new HashMap<>();
                            ulimits.put(user, ulimit);
                        }

                        if (line.contains(" = ")) {
                            String[] value = line.split(" = ");
                            if (ulimit != null) {
                                ulimit.put(value[0], value[1]);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.ULIMITS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return ulimits;
    }

    public String getDistributionVersion(String result, Map<String, String> errorMap) throws InterruptedException {
        // 5.3.0.0, 6.1.0.0
        String version = null;
        try {
            if (StringUtils.isNotEmpty(result) && result.length() > 2) {
                version = result.substring(0, 3);
            } else {
                version = result;
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DISTRIBUTION.name() + "_VERSION", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return version;
    }

    public String getFirmware(String result, Map<String, String> errorMap) throws InterruptedException {
        String firmware = null;
        try {
            if (StringUtils.isNotEmpty(result)) {
                String[] data = result.split("\\s+");
                firmware = data[1].strip();
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.FIRMWARE_VERSION.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return firmware;
    }

    public Map<String, Group> getGroups(String result, Map<String, String> errorMap) throws InterruptedException {

        String[] exceptGroups = {
                "root", "daemon", "bin", "sys", "adm", "uucp", "guest",
                "nobody", "lpd",
                "lp", "invscout", "snapp", "ipsec", "nuucp", "sshd", "ftp",
                "anonymou"
        };

        Map<String, Group> groups = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {

                    if (StringUtils.isNotEmpty(line)) {
                        // split 크기 지정
                        // ex> root:x:0:
                        String[] infos = line.split(":", 4);

                        if (!Arrays.stream(exceptGroups).anyMatch(u -> u.equals(infos[0]))) {
                            Group group = new Group();
                            group.setGid(infos[2]);
                            group.setUsers(Arrays.asList(infos[3].split(",")));

                            groups.put(infos[0], group);
                        }
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.GROUPS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return groups;
    }

    public String getHostname(String result, Map<String, String> errorMap) throws InterruptedException {
        try {
            result = result.replaceAll("\n", "");
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.HOSTNAME.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result;
    }

    public Map<String, InterfaceInfo> getInterfacesInfo(TargetHost targetHost, String ifResult, String gwResult, Long uptime, Map<String, String> errorMap) throws InterruptedException {
        Map<String, InterfaceInfo> interfaces = new HashMap<>();
        try {
            InterfaceInfo iterInfo = null;
            if (StringUtils.isNotEmpty(ifResult)) {
                String netstat = SSHUtil.executeCommand(targetHost, "netstat -v");
                for (String line : ifResult.split("\n")) {
                    if (StringUtils.isNotEmpty(line)) {
                        String[] words = line.trim().split("\\s+");

                        Pattern p = Pattern.compile("^\\w*\\d*:");
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            iterInfo = initInterface(words);
                            interfaces.put(iterInfo.getDevice(), iterInfo);
                            getDefaultGateway(gwResult, iterInfo, errorMap);
                            getMacAddress(targetHost, iterInfo);
                            if (StringUtils.isNotEmpty(iterInfo.getMacaddress()) && !"unknown".equalsIgnoreCase(iterInfo.getMacaddress()) &&
                                    StringUtils.isNotEmpty(netstat) && uptime != null && uptime != 0L) {
                                getRxTxBytes(targetHost, iterInfo, netstat, uptime);
                            }
                        } else if (words[0].equals("ether")) {
                            parseEtherLine(words, iterInfo);
                        } else if (words[0].equals("inet")) {
                            parseInetLine(words, iterInfo);
                        } else if (words[0].equals("inet6")) {
                            parseInet6line(words, iterInfo);
                        } else {
                            parseUnkownLine(words, iterInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.INTERFACES.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return interfaces;
    }

    private void getMacAddress(TargetHost targetHost, InterfaceInfo iterInfo) throws InterruptedException {
        String result = null;
        try {
            result = SSHUtil.executeCommand(targetHost, String.format(AixCommand.INTERFACES_MAC_ADDRESS, iterInfo.getDevice()));

            if (StringUtils.isNotEmpty(result)) {
                result = result.split("\n")[0];
                String[] data = result.split(":", 2);

                iterInfo.setMacaddress(data[1].trim());
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
        }
    }

    private void getRxTxBytes(TargetHost targetHost, InterfaceInfo iterInfo, String netstat, Long uptime) throws InterruptedException {
        try {
            /** AIX NOT working below command */
            // String result = SSHUtil.executeCommand(targetHost, String.format(CommonCommand.INTERFACES_RX_TX_SCRIPTS1, iterInfo.getDevice()));
            //
            // if (StringUtils.isNotEmpty(result) && result.contains(StringUtils.SPACE)) {
            //     Double rxBytes = Double.parseDouble(result.split(StringUtils.SPACE)[0]) * 1024;
            //     Double txBytes = Double.parseDouble(result.split(StringUtils.SPACE)[1]) * 1024;
            //
            //     iterInfo.setRxBytes(rxBytes.longValue());
            //     iterInfo.setTxBytes(txBytes.longValue());
            // } else {
            //     /** AIX NOT working below command */
            //      result = SSHUtil.executeCommand(targetHost, String.format(CommonCommand.INTERFACES_RX_TX_SCRIPTS2, iterInfo.getDevice(), iterInfo.getDevice()));
            //
            //      if (StringUtils.isNotEmpty(result)) {
            //          result = result.replaceAll("(\r\n|\r|\n|\n\r)", StringUtils.SPACE);
            //          Long rxBytes = Long.parseLong(result.split(StringUtils.SPACE)[0]);
            //          Long txBytes = Long.parseLong(result.split(StringUtils.SPACE)[1]);
            //          Long uptime = Long.parseLong(result.split(StringUtils.SPACE)[2]);
            //
            //          rxBytes = rxBytes / uptime;
            //          txBytes = txBytes / uptime;
            //
            //          iterInfo.setRxBytes(rxBytes);
            //          iterInfo.setTxBytes(txBytes);
            //      }
            // }

            Long rxBytes = getBytesByHardwareAddress(netstat, iterInfo.getMacaddress(), "Receive Statistics", uptime);
            Long txBytes = getBytesByHardwareAddress(netstat, iterInfo.getMacaddress(), "Transmit Statistics", uptime);

            iterInfo.setRxBytes(rxBytes);
            iterInfo.setTxBytes(txBytes);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
        }
    }

    private Long getBytesByHardwareAddress(String netstat, String hwAddress, String type, Long uptime) {
        Long bytes = 0L;
        String pattern = null;

        if (type.toLowerCase().contains("receive")) {
            pattern = "Hardware Address: " + hwAddress + ".*?Packets:.*?Packets:.*?Bytes:.*?Bytes: (\\d+)";
        } else if (type.toLowerCase().contains("transmit")) {
            pattern = "Hardware Address: " + hwAddress + ".*?Packets:.*?Packets:.*?Bytes: (\\d+)";
        }

        if (StringUtils.isNotEmpty(pattern)) {
            Pattern statsPattern = Pattern.compile(pattern, Pattern.DOTALL);
            Matcher matcher = statsPattern.matcher(netstat);

            if (matcher.find()) {
                bytes = Long.parseLong(matcher.group(1));
                bytes = bytes / uptime;
            }
        }

        return bytes;
    }

    public String getKernel(String result, Map<String, String> errorMap) throws InterruptedException {
        try {
            String[] data = null;
            for (String line : result.split("\n")) {
                data = line.strip().split("\\s+");
                break;
            }
            result = data != null ? data[1] : null;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.KERNEL.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result;
    }

    public Map<String, String> getKernelParams(String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, String> kernelParamMap = new HashMap<>();

        try {
            if (StringUtils.isNotEmpty(result)) {

                for (String line : result.split("\n")) {
                    String[] keyValues = line.split("\\s+");

                    String key = keyValues[0].strip();
                    String value = keyValues[1].replaceAll("\t", " ");

                    if (value.length() > 1) {
                        if (kernelParamMap.containsKey(key)) {
                            kernelParamMap.put(key, kernelParamMap.get(key) + "," + value.strip());
                        } else {
                            kernelParamMap.put(key, value.strip());
                        }
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.KERNEL_PARAM.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return kernelParamMap;
    }

    public PortList getListenPort(TargetHost targetHost, String listenPort, String traffics, Map<String, String> errorMap) throws InterruptedException {

        PortList portList = new PortList();

        List<ListenPort> listen = new ArrayList<>();
        EstablishedPort established = new EstablishedPort();
        WaitPort wait = new WaitPort();

        portList.setListen(listen);
        portList.setEstablished(established);
        portList.setWait(wait);

        try {
            Map<String, String> rmSockCmds = new HashMap<>();
            if (StringUtils.isNotEmpty(listenPort)) {
                Map<String, ListenPort> tempMap = new HashMap<>();

                for (String line : listenPort.split("\n")) {
                    String[] data = line.split("\\s+");

                    if (data.length > 1) {
                        String[] sourceAddrPort, destinationAddrPort;

                        if (data[4].contains(":")) {
                            sourceAddrPort = data[4].split(":");
                            destinationAddrPort = data[5].split(":");
                        } else {
                            sourceAddrPort = new String[]{data[4].substring(0, data[4].lastIndexOf(".")), data[4].substring(data[4].lastIndexOf(".") + 1)};
                            destinationAddrPort = new String[]{data[5].substring(0, data[5].lastIndexOf(".")), data[5].substring(data[5].lastIndexOf(".") + 1)};
                        }

                        rmSockCmds.put(data[0], String.format(AixCommand.NET_RMSOCK, data[0]));

                        ListenPort port = new ListenPort();
                        port.setProtocol(data[1]);
                        port.setBindAddr(sourceAddrPort[0]);
                        port.setPort(sourceAddrPort[1]);
                        tempMap.put(data[0], port);
                    }
                }

                Map<String, RemoteExecResult> resultMap = runCommands(targetHost, rmSockCmds);

                for (String key : resultMap.keySet()) {
                    ListenPort port = tempMap.getOrDefault(key, null);

                    RemoteExecResult result = resultMap.get(key);
                    if (StringUtils.isNotEmpty(result.getResult())) {
                        String[] usageInfo = result.getResult().split("\\s+proccess\\s");

                        String[] psInfo = new String[2];
                        if (usageInfo.length > 1) {
                            String[] pInfo = usageInfo[1].split("\\s+");
                            try {
                                psInfo[0] = pInfo[0];
                                psInfo[1] = pInfo[1].substring(pInfo[1].indexOf("(") + 1, pInfo[1].indexOf(")"));
                            } catch (Exception e) {
                                RoRoException.checkInterruptedException(e);
                                log.debug("Error occurred when parse usage info");
                                psInfo[0] = "";
                                psInfo[1] = "";
                            }
                        } else {
                            psInfo[0] = "";
                            psInfo[1] = "";
                        }
                        port.setPid(psInfo[0]);
                        port.setName(psInfo[1]);
                    }
                    listen.add(port);
                }
            }

            if (StringUtils.isNotEmpty(traffics)) {
                Map<String, Traffic> tempMap = new HashMap<>();
                for (String line : traffics.split("\n")) {
                    String[] data = line.split("\\s+");
                    if (data != null && data.length > 1) {
                        String[] sourceAddrPort = getSplitAddress(data[4]);
                        String[] destinationAddrPort = getSplitAddress(data[5]);

                        if (sourceAddrPort[0].equals("127.0.0.1") && destinationAddrPort[0].equals("127.0.0.1"))
                            continue;

                        String conStatus = null;

                        try {
                            conStatus = data[6].toLowerCase();
                        } catch (IndexOutOfBoundsException e) {
                            conStatus = "";
                        }

                        rmSockCmds.put(data[0], String.format(AixCommand.NET_RMSOCK, data[0]));

                        Traffic traffic = new Traffic();
                        traffic.setProtocol(data[1]);
                        traffic.setFaddr(destinationAddrPort[0]);
                        traffic.setFport(destinationAddrPort[1]);
                        traffic.setLaddr(sourceAddrPort[0]);
                        traffic.setLport(sourceAddrPort[1]);
                        traffic.setStatus(conStatus);
                        tempMap.put(data[0], traffic);

                        Optional<ListenPort> portInfo = listen.stream().filter(p -> p.getPort().equals(sourceAddrPort[1])).findFirst();

                        if (conStatus.contains("wait")) {
                            if (portInfo.isPresent()) {
                                wait.getAnyToLocal().add(traffic);
                            } else {
                                wait.getLocalToAny().add(traffic);
                            }
                        } else if (conStatus.contains("established")) {
                            if (portInfo.isPresent()) {
                                established.getAnyToLocal().add(traffic);
                            } else {
                                established.getLocalToAny().add(traffic);
                            }
                        }
                    }
                }

                Map<String, RemoteExecResult> resultMap = runCommands(targetHost, rmSockCmds);

                for (String key : resultMap.keySet()) {
                    Traffic traffic = tempMap.getOrDefault(key, null);

                    if (traffic != null) {
                        RemoteExecResult result = resultMap.get(key);
                        if (StringUtils.isNotEmpty(result.getResult())) {
                            String[] usageInfo = result.getResult().split("\\s+proccess\\s");

                            String[] psInfo = new String[2];
                            if (usageInfo.length > 1) {
                                String[] pInfo = usageInfo[1].split("\\s+");
                                try {
                                    psInfo[0] = pInfo[0];
                                    psInfo[1] = pInfo[1].substring(pInfo[1].indexOf("(") + 1, pInfo[1].indexOf(")"));
                                } catch (Exception e) {
                                    RoRoException.checkInterruptedException(e);
                                    log.debug("Error occurred when parse usage info");
                                    psInfo[0] = "";
                                    psInfo[1] = "";
                                }
                            } else {
                                psInfo[0] = "";
                                psInfo[1] = "";
                            }
                            traffic.setPid(psInfo[0]);
                            traffic.setName(psInfo[1]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
        }
        return portList;
    }

    public String[] getSplitAddress(String addr) {
        String[] data = {
                addr.substring(0, addr.lastIndexOf(".")),
                addr.substring(addr.lastIndexOf(".") + 1)
        };

        return data;
    }

    public Map<String, Partition> getDf(String partitions, String types, Map<String, String> errorMap) throws InterruptedException {

        Map<String, Partition> partitionMap = new HashMap<>();
        try {

            if (StringUtils.isNotEmpty(partitions)) {
                String rootVgs = types;

                for (String line : partitions.split("\n")) {
                    Pattern a = Pattern.compile("^/dev/");
                    if (a.matcher(line).find()) {
                        String[] pt = line.split("\\s+");

                        Partition partition = new Partition();
                        partition.setDevice(pt[0]);
                        partition.setSize(pt[1]);
                        partition.setFree(pt[2]);
                        partition.setFsType(getFsType(pt[0], rootVgs, errorMap));
                        partition.setMountPath(pt[6]);

                        partitionMap.put(pt[6], partition);
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.PARTITIONS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return partitionMap;
    }

    public Map<String, List<ExtraPartition>> getExtraPartitions(String result, Map<String, String> errorMap) throws InterruptedException {

        Map<String, List<ExtraPartition>> extraPartitions = new HashMap<>();
        try {
            String[] ignorePartitions = {"N/A", "/", "/usr", "/var", "/tmp", "/home", "/proc",
                    "/opt", "/admin", "/var/adm/ras/livedump"};

            if (StringUtils.isNotEmpty(result)) {

                ExtraPartition partition = null;

                for (String line : result.split("\n")) {

                    String[] data = line.split("\\s+");

                    if (data[0].contains("rootvg:") || data[0].contains("LV"))
                        continue;

                    partition = new ExtraPartition();
                    partition.setMountPoint(data[0]);
                    partition.setType(data[1]);
                    partition.setLvState(data[5]);
                    partition.setExtra("False");

                    if (!extraPartitions.containsKey(data[6])) {
                        List<ExtraPartition> partitionList = new ArrayList<>();
                        partitionList.add(partition);

                        extraPartitions.put(data[6], partitionList);
                    } else if (!Arrays.stream(ignorePartitions).anyMatch(u -> u.equals(data[6]))) {
                        partition.setExtra("True");
                        extraPartitions.get(data[6]).add(partition);
                    } else {
                        extraPartitions.get(data[6]).add(partition);
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.EXSTRA_PARTITIONS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return extraPartitions;
    }

    private String getFsType(String device, String rootVgs, Map<String, String> errorMap) throws InterruptedException {
        try {
            String deviceName = device.split("/")[2];

            for (String line : rootVgs.split("\n")) {
                Pattern a = Pattern.compile(String.format("^%s", deviceName));
                if (a.matcher(line).find()) {
                    return line.split("\\s+")[1];
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.PARTITIONS_TYPE.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return null;
    }

    public List<Process> getPsInfo(String result, Map<String, String> errorMap) throws InterruptedException {
        List<Process> processes = new ArrayList<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {
                    try {
                        if (line.contains("<defunct>") || line.contains("UID")) {
                            continue;
                        }

                        String[] data = line.trim().split("\\s+");

                        Process process = new Process();
                        if (Pattern.matches("(\\d+\\-)?(([0-9]+):)+([0-9]+)", data[7])) {
                            process.setName(data[8]);
                            process.setUser(data[0]);
                            process.setPid(data[1]);
                            process.setCmd(Arrays.asList(data).subList(8, data.length));
                            processes.add(process);
                        } else if (Pattern.matches("(\\d+\\-)?(([0-9]+):)+([0-9]+)", data[6])) {
                            if (Pattern.matches("\\[(.*?)\\]", data[7])) {
                                continue;
                            }

                            process.setName(data[7]);
                            process.setUser(data[0]);
                            process.setPid(data[1]);
                            process.setCmd(Arrays.asList(data).subList(7, data.length));
                            processes.add(process);
                        } else {
                            log.warn("TIME column does not exist in (\"{}\") at index 6 or 7.", line.trim());
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        log.error("Unhandled exception occurred while parse process(\"{}\") and scan process will be continue.", line.trim(), e);
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.PROCESSES.name(), e.getMessage());
            log.error("Unhandled exception occurred while parse processes.", e);
        }
        return processes;
    }

    public CpuInfo getCpuInfo(TargetHost targetHost, Map<String, String> errorMap) throws InterruptedException {

        CpuInfo cpuInfo = new CpuInfo();
        try {
            String result = SSHUtil.executeCommand(targetHost, AixCommand.PROCESSOR_COUNT);

            if (StringUtils.isNotEmpty(result)) {
                int cnt = 0;
                String cpuDev = "";
                for (String line : result.split("\n")) {

                    if (line.contains("Available")) {
                        if (cnt == 0) {
                            String[] data = line.split(" ");
                            cpuDev = data[0];
                        }
                        cnt++;
                    }
                }
                cpuInfo.setProcessorCount(String.valueOf(cnt));

                result = SSHUtil.executeCommand(targetHost, String.format(AixCommand.PROCESSOR, cpuDev));
                cpuInfo.setProcessor(result.split(" ")[1]);

                result = SSHUtil.executeCommand(targetHost, String.format(AixCommand.PROCESSOR_CORES, cpuDev));
                cpuInfo.setProcessorCores(result.split(" ")[1]);
            }

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.CPU_FACTS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return cpuInfo;
    }

    public String getProductName(TargetHost targetHost, Map<String, String> errorMap) throws InterruptedException {
        String productName = null;
        try {
            String result = SSHUtil.executeCommand(targetHost, AixCommand.DMI_FACTS);

            if (StringUtils.isNotEmpty(result)) {

                for (String line : result.split("\n")) {
                    String[] data = line.split(":");
                    if (line.indexOf("System Model") > -1) {
                        productName = data[1].strip();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DMI_FACTS.name() + "_PD_NAME", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return productName;
    }

    public String getProductSerial(TargetHost targetHost, Map<String, String> errorMap) throws InterruptedException {
        String productSerial = null;
        try {
            String result = SSHUtil.executeCommand(targetHost, AixCommand.DMI_FACTS);

            if (StringUtils.isNotEmpty(result)) {

                for (String line : result.split("\n")) {
                    String[] data = line.split(":");
                    if (line.indexOf("Machine Serial Number") > -1) {
                        productSerial = data[1].strip();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DMI_FACTS.name() + "_PD_SERIAL", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return productSerial;
    }

    public Map<String, String> getPasswordUsers(String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, String> shadow = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                result = result.replaceAll(":\n", ":");

                List<String> shadowList = ResultUtil.removeCommentLine(result);
                for (String line : shadowList) {
                    String[] data = line.split(":");
                    if (!data[1].equals("*")) {
                        shadow.put(data[0], data[1]);
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.SHADOWS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return shadow;
    }

    public MemoryInfo getMemoryInfo(String memFacts, String swapFacts, Map<String, String> errorMap) throws InterruptedException {
        MemoryInfo memory = new MemoryInfo();
        try {
            int pageSize = 4096;
            int pageCount = 1;
            int freeCount = 1;
            for (String line : memFacts.split("\n")) {
                String[] data = line.trim().split("\\s+");
                if (line.indexOf("memory pages") > -1) {
                    pageCount = Integer.valueOf(data[0]);
                } else if (line.indexOf("free pages") > -1) {
                    freeCount = Integer.valueOf(data[0]);
                }
            }
            memory.setMemTotalMb(String.valueOf((pageSize * (long) pageCount) / (1024 * 1024)));
            memory.setMemFreeMb(String.valueOf((pageSize * (long) freeCount) / (1024 * 1024)));
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.MEMORY_FACTS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        try {

            if (StringUtils.isNotEmpty(swapFacts)) {
                String[] lines = swapFacts.split("\n");
                String[] data = lines[1].strip().split("\\s+");

                int swapTotalMb = Integer.valueOf(data[0].replaceAll("MB", "").strip());
                int percused = Integer.valueOf(data[1].replaceAll("%", "").strip());
                int swapFreeMb = Integer.valueOf(swapTotalMb * (100 - percused) / 100);

                memory.setSwapTotalMb(String.valueOf(swapTotalMb));
                memory.setSwapFreeMb(String.valueOf(swapFreeMb));
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.MEMORY_FACTS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return memory;
    }

    public String getTimezone(String result, Map<String, String> errorMap) throws InterruptedException {
        try {
            if (StringUtils.isNotEmpty(result)) {
                result = result.replaceAll("\n", "").strip();
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.TIMEZONE1.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result;
    }

    public List<RouteTable> getRouteTable(String result, Map<String, String> errorMap) throws InterruptedException {
        List<RouteTable> routeTables = new ArrayList<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {

                    if (StringUtils.isEmpty(line) ||
                            line.indexOf("Routing") > -1 ||
                            line.indexOf("Destination") > -1)
                        continue;

                    String[] route_infos = line.split("\\s+");

                    if (!route_infos[0].equals("Route")) {
                        RouteTable routeTable = new RouteTable();
                        routeTable.setDestination(route_infos[0]);
                        routeTable.setGateway(route_infos[1]);
                        routeTable.setIface(route_infos[5]);

                        routeTables.add(routeTable);
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.ROUTE_TABLE.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return routeTables;
    }

    public Map<String, User> getUsers(TargetHost targetHost, String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, User> users = new HashMap<>();

        String[] ignoreUsers = {"daemon", "bin", "sys", "adm", "uucp", "guest", "nobody",
                "lpd",
                "lp", "invscout", "snapp", "ipsec", "nuucp", "sshd", "ftp",
                "anonymou"};
        try {
            if (StringUtils.isNotEmpty(result)) {
                Map<String, String> profileCmd = new HashMap<>();
                Map<String, String> bashCmd = new HashMap<>();

                List<String> passwordList = ResultUtil.removeCommentLine(result);
                for (String line : passwordList) {

                    User user = new User();

                    String[] data = line.split(":");
                    if (!Arrays.stream(ignoreUsers).anyMatch(u -> u.equals(data[0]))) {
                        profileCmd.put(data[0], "sh -c 'cat " + data[5] + "/.*profile'");
                        bashCmd.put(data[0], "sh -c 'cat " + data[5] + "/.*rc'");

                        user.setUid(data[2]);
                        user.setGid(data[3]);
                        user.setHomeDir(data[5]);
                        user.setShell(data[6]);
                        user.setProfile("");

                        users.put(data[0], user);
                    }

                }
                Map<String, RemoteExecResult> profileMap = runCommands(targetHost, profileCmd);
                Map<String, RemoteExecResult> bashMap = runCommands(targetHost, bashCmd);

                for (String user : users.keySet()) {
                    String profile = "";
                    if (profileMap.containsKey(user)) {
                        if (StringUtils.isNotEmpty(profileMap.get(user).getResult())) {
                            profile += profileMap.get(user).getResult();
                        }
                    }

                    if (bashMap.containsKey(user)) {
                        if (StringUtils.isNotEmpty(bashMap.get(user).getResult())) {
                            profile += bashMap.get(user).getResult();
                        }
                    }
                    users.get(user).setProfile(profile);
                }
            }

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.USERS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return users;
    }

    public Map<String, String> getEnv(String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, String> envMap = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                String key = null;
                String value = null;

                for (String line : result.split("\n")) {

                    if (line.contains("=")) {
                        key = line.split("=")[0];
                        value = line.split("=")[1].replaceAll("\"", "");
                    } else {
                        value = envMap.get("key") + " " + line;
                    }

                    envMap.put(key, value);
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.ENV.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return envMap;
    }

    public Map<String, String> getLocale(String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, String> localeMap = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {

                for (String line : result.split("\n")) {
                    String[] data = line.split("=", 2);

                    localeMap.put(data[0], data[1].replaceAll("\"", ""));
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.LOCALE.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return localeMap;
    }

    public List<String> getDns(String result, Map<String, String> errorMap) throws InterruptedException {
        List<String> dns = new ArrayList<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {

                    if (StringUtils.isNotEmpty(line)) {
                        String[] dns_infos = line.split(" ");

                        dns.addAll(Arrays.asList(dns_infos).subList(1, dns_infos.length));
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DNS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return dns;
    }

    public Hosts getHosts(String result, Map<String, String> errorMap) throws InterruptedException {

        Hosts hosts = new Hosts();
        try {
            if (StringUtils.isNotEmpty(result)) {
                hosts.setContents(result);

                Map<String, List<String>> mappings = new HashMap<>();

                List<String> hostList = ResultUtil.removeCommentLine(result);
                for (String line : hostList) {
                    if (line.contains("#")) {
                        line = line.substring(0, line.indexOf("#"));
                    }

                    String[] data = line.split("\\s+");

                    if (data.length > 1) {
                        mappings.put(data[0], Arrays.asList(data).subList(1, data.length));
                    }
                }
                hosts.setMappings(mappings);
            }

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.HOSTS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return hosts;
    }

    public DefInfo getLoginDef() {
        DefInfo defInfo = new DefInfo();
        defInfo.setUidMin("201");
        defInfo.setUidMax("60000");
        defInfo.setGidMin("201");
        defInfo.setGidMax("60000");
        return defInfo;
    }

    public String getDistributionRelease(String result, Map<String, String> errorMap) throws InterruptedException {
        String release = "AIX ";
        try {
            if (StringUtils.isNotEmpty(result) && result.length() > 2) {
                release += result.substring(0, 3);
            } else {
                release += result;
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DISTRIBUTION.name() + "_RELEASE", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return release;
    }

    public String getFamily() {
        return "aix";
    }

    private void parseEtherLine(String[] words, InterfaceInfo iterInfo) {
        iterInfo.setMacaddress(words[1]);
    }

    private void parseUnkownLine(String[] words, InterfaceInfo iterInfo) {
        return;
    }

    private void parseInet6line(String[] words, InterfaceInfo iterInfo) {

        String sAddr = words[1];
        String prefix = getInet6PrefixValue(words);
        String scope = getInet6ScopeValue(words);

        String[] localhost6 = {"::1", "::1/128", "fe80::1%lo0"};

        Ipv6Address address = new Ipv6Address();
        address.setAddress(sAddr);
        address.setPrefix(prefix);
        address.setScope(scope);

        iterInfo.getIpv6().add(address);
    }

    private String getInet6PrefixValue(String[] words) {
        String value = "";
        if (words.length >= 4 && words[2].equals("prefixlen")) {
            value = words[3];
        }
        return value;
    }

    private String getInet6ScopeValue(String[] words) {
        String value = "";
        if (words.length >= 6 && words[4].equals("scopeid")) {
            value = words[5];
        }
        return value;
    }

    private void parseInetLine(String[] words, InterfaceInfo iterInfo) {
        String sAddr = words[1];
        String netmask = null;
        String broadcast = null;

        Pattern p = Pattern.compile("([0-9a-f]){8}");
        if (p.matcher(words[3]).find() && words[3].length() == 8) {
            words[3] = "0x" + words[3];
        }

        if (words[3].startsWith("0x")) {
            String hexMask = words[3].replaceAll("0x", "");
            BigInteger hex = new BigInteger(hexMask, 16);
            netmask = getNetMask(hex.longValue());
        } else {
            netmask = words[3];
        }

        if (words.length > 5) {
            broadcast = words[5];
        } else {
            SubnetUtils utils = new SubnetUtils(sAddr, netmask);
            broadcast = utils.getInfo().getBroadcastAddress();
        }

        Ipv4Address address = new Ipv4Address();

        address.setAddress(sAddr);
        address.setNetmask(netmask);
        address.setBroadcast(broadcast);

        iterInfo.getIpv4().add(address);
    }

    private String getBroadCast(String word) {
        SubnetUtils subnetUtils = new SubnetUtils(word);
        return subnetUtils.getInfo().getBroadcastAddress();
    }

    private String getNetMask(long ip) {
        return ((ip >> 24) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "."
                + (ip & 0xFF);

    }

    private void getDefaultGateway(String result, InterfaceInfo iterInfo, Map<String, String> errorMap) throws InterruptedException {
        try {
            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {
                    String[] data = line.split("\\s+");

                    if (data.length > 1 && data[0].equals("default")) {

                        if (data[5].equals(iterInfo.getDevice())) {
                            iterInfo.setGateway(data[1]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.INTERFACES_DEFAULT_GATEWAY.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
    }

    private InterfaceInfo initInterface(String[] words) {
        InterfaceInfo info = new InterfaceInfo();
        info.setDevice(words[0].substring(0, words[0].length() - 1));
        info.setGateway("unknown");
        info.setScript("unknown");
        info.setMacaddress("unknown");
        return info;
    }

    private Map<String, FileSystem> getFileSystem(String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, FileSystem> fileSystems = new HashMap<>();

        try {
            if (StringUtils.isNotEmpty(result)) {
                String fs = null;
                for (String line : result.split("\n")) {
                    Pattern p = Pattern.compile("^[*]");
                    if (p.matcher(line).find())
                        continue;

                    FileSystem fileSystem = null;
                    if (line.contains(":")) {
                        fs = line.split(":")[0];
                        fileSystem = new FileSystem();
                        fileSystems.put(fs, fileSystem);
                    } else if (line.contains("=")) {
                        fileSystem = fileSystems.get(fs);
                        line = line.replaceAll("[\\t|\\p{Z}|\"]", "");

                        String[] info = line.split("=", 2);

                        DiskParserUtil.parseFileSystemDetail(fileSystem, info);
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.FILESYSTEM.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return fileSystems;
    }

    private Security getSecurity(String loginPolicy, String passwordPolicy, Map<String, String> errorMap) throws InterruptedException {
        Security security = new Security();
        try {
            Map<String, Object> loginPolicyMap = getSecurityLogin(loginPolicy);
            security.setLogin(loginPolicyMap);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.SECURITY_LOGIN.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        try {
            Map<String, Object> passwordPolicyMap = getSecurityPassword(passwordPolicy);
            security.setPassword(passwordPolicyMap);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.SECURITY_PASSWORD.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return security;
    }

    private Map<String, Object> getSecurityLogin(String result) {
        Map<String, Object> loginMap = new HashMap<>();

        if (StringUtils.isNotEmpty(result)) {

            Map<String, String> map = null;
            String user = null;

            Pattern p = Pattern.compile("^\\*");
            for (String line : result.split("\n")) {
                if (p.matcher(line).find())
                    continue;

                if (StringUtils.isNotEmpty(line)) {
                    if (line.contains(":")) {
                        map = new HashMap<>();
                        user = line.replaceAll(":", "");
                        loginMap.put(user, map);
                    } else if (line.contains("=")) {
                        String[] data = line.split("=", 2);
                        if (map != null) {
                            map.put(data[0].replaceAll("\t", ""), data[1].strip());
                        }
                    }
                }
            }
        }
        return loginMap;
    }

    private Map<String, Map<String, String>> getDaemons(String result) throws InterruptedException {
        Map<String, Map<String, String>> daemons = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {

                for (String line : result.split("\n")) {

                    Map<String, String> daemon = new HashMap<>();

                    String[] value = line.trim().split("\\s+");

                    if (value.length == 3) {
                        if (Pattern.compile("\\d").matcher(value[1]).find()) {
                            daemon.put("group", "");
                            daemon.put("pid", value[1]);
                            daemon.put("status", value[2]);
                        } else {
                            daemon.put("group", value[1]);
                            daemon.put("pid", "");
                            daemon.put("status", value[2]);
                        }
                    } else if (value.length == 2) {
                        daemon.put("group", "");
                        daemon.put("pid", "");
                        daemon.put("status", value[1]);
                    } else {
                        daemon.put("group", value[1]);
                        daemon.put("pid", value[2]);
                        daemon.put("status", value[3]);
                    }
                    daemons.put(value[0], daemon);
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
        }

        return daemons;
    }

    private Map<String, Object> getSecurityPassword(String result) {
        Map<String, Object> passwordMap = new HashMap<>();

        if (StringUtils.isNotEmpty(result)) {

            Map<String, String> map = null;
            String user = null;

            Pattern p = Pattern.compile("^\\*");
            for (String line : result.split("\n")) {
                if (p.matcher(line).find())
                    continue;

                if (StringUtils.isNotEmpty(line)) {
                    if (line.contains(":")) {
                        map = new HashMap<>();
                        user = line.replaceAll(":", "");
                        passwordMap.put(user, map);
                    } else if (line.contains("=")) {
                        String[] data = line.split("=", 2);
                        if (map != null) {
                            map.put(data[0].replaceAll("\t", ""), data[1].strip());
                        }
                    }
                }
            }
        }

        return passwordMap;
    }

    private Map<String, VolumeGroup> getLvmInfo(TargetHost targetHost, String lvmInfo, Map<String, String> errorMap) throws InterruptedException {
        Map<String, VolumeGroup> vgs = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(lvmInfo)) {
                Pattern p = Pattern.compile("(\\S+):\\n.*FREE DISTRIBUTION(\\n(\\S+)\\s+(\\w+)\\s+(\\d+)\\s+(\\d+).*)+");
                Matcher m = p.matcher(lvmInfo);

                for (MatchResult matchResult : m.results().collect(Collectors.toList())) {
                    VolumeGroup vg = new VolumeGroup();
                    vgs.put(matchResult.group(1), vg);

                    parsePhysicalVolume(targetHost, vgs, matchResult);
                    parseLogicalVolume(targetHost, vgs, matchResult);
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.LVM_VGS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return vgs;
    }

    protected void parseLogicalVolume(TargetHost targetHost, Map<String, VolumeGroup> vgs, MatchResult matchResult) throws InterruptedException {
        String result = SSHUtil.executeCommand(targetHost, String.format(AixCommand.LVM_LVS, matchResult.group(1)));

        if (StringUtils.isNotEmpty(result)) {

            for (String line : result.split("\n")) {
                if (line.contains(matchResult.group(1)) || line.contains("LV NAME")) {
                    continue;
                }

                String[] data = line.split("\\s+");

                LogicalVolume logicalVolume = new LogicalVolume();
                if (data.length > 6) {
                    logicalVolume.setLvName(data[0]);
                    logicalVolume.setLvType(data[1]);
                    logicalVolume.setLps(data[2]);
                    logicalVolume.setPps(data[3]);
                    logicalVolume.setPvs(data[4]);
                    logicalVolume.setLvState(data[5]);
                    logicalVolume.setMountPoint(data[6]);
                } else {
                    logicalVolume.setLvName(data[0]);
                    logicalVolume.setLvType("");
                    logicalVolume.setLps(data[1]);
                    logicalVolume.setPps(data[2]);
                    logicalVolume.setPvs(data[3]);
                    logicalVolume.setLvState(data[4]);
                    logicalVolume.setMountPoint(data[5]);
                }
                vgs.get(matchResult.group(1)).getLvs().add(logicalVolume);
            }
        }
    }

    protected void parsePhysicalVolume(TargetHost targetHost, Map<String, VolumeGroup> vgs, MatchResult matchResult) throws InterruptedException {

        String result = SSHUtil.executeCommand(targetHost, String.format(AixCommand.LVM_PVS, matchResult.group(1)));

        if (StringUtils.isNotEmpty(result)) {

            Pattern p3 = Pattern.compile("(\\S+)\\s+(\\w+)\\s+(\\d+)\\s+(\\d+).*");
            Matcher m3 = p3.matcher(matchResult.group(0));
            for (MatchResult matchResult2 : m3.results().collect(Collectors.toList())) {
                PhysicalVolume physicalVolume = new PhysicalVolume();
                physicalVolume.setPvName(matchResult2.group(1));
                physicalVolume.setPvState(matchResult2.group(2));
                physicalVolume.setTotalPps(matchResult2.group(3));
                physicalVolume.setFreePps(matchResult2.group(4));
                vgs.get(matchResult.group(1)).getPvs().add(physicalVolume);
            }
        }
    }

    protected Long getUptime(String uptime, Map<String, String> errorMap) throws InterruptedException {
        // Long result = null;
        // try {
        //     int day;
        //     int hour;
        //     int min;
        //     if (StringUtils.isNotEmpty(uptime)) {
        //         uptime = uptime.replaceAll("00", "0");
        //         String[] times = uptime.split("\\s+");
        //
        //         day = Integer.parseInt(times[0]);
        //         hour = Integer.parseInt(times[1]);
        //         if (uptime.contains("min")) {
        //             min = hour;
        //             hour = 0;
        //         } else if (uptime.contains("user")) {
        //             min = day;
        //             day = 0;
        //             hour = 0;
        //         } else {
        //             min = Integer.parseInt(times[2]);
        //         }
        //
        //         long now = System.currentTimeMillis();
        //
        //         long timestamp = (((day) * 24 + (hour)) * 60 + (min)) * 60000;
        //         result = now - timestamp;
        //     }
        // } catch (Exception e) {
        //     errorMap.put(AssessmentItems.UPTIME.name(), e.getMessage());
        //     log.error("{}", e.getMessage(), e);
        // }
        // return result;

        Long result = null;
        try {
            result = getUptime(uptime);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.UPTIME.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return result;
    }

    protected Long getUptimeSec(String uptimeStr) {
        Long uptime = null;
        try {
            if (!uptimeStr.contains("up")) {
                return null;
            }

            String[] uptimes = uptimeStr.split(",");

            String dayStr = null, hourStr = null, minStr = null;
            long day = 0, hour = 0, min = 0;
            if (uptimes[0].contains("up")) {
                String tmp = uptimes[0].substring(uptimes[0].indexOf("up") + 2).trim();

                if (tmp.contains("d")) {
                    dayStr = tmp.split("\\s")[0];
                } else if (tmp.contains("h")) {
                    hourStr = tmp.split("\\s")[0];
                } else if (tmp.contains("m")) {
                    minStr = tmp.split("\\s")[0];
                } else if (tmp.contains(":")) {
                    hourStr = tmp.split(":")[0];
                    minStr = tmp.split(":")[1];
                }
            }

            if (!uptimes[1].contains("user")) {
                String tmp = uptimes[1].trim();

                if (tmp.contains("h")) {
                    hourStr = tmp.split("\\s")[0];
                } else if (tmp.contains("m")) {
                    minStr = tmp.split("\\s")[0];
                } else if (tmp.contains(":")) {
                    hourStr = tmp.split(":")[0];
                    minStr = tmp.split(":")[1];
                }
            }

            if (NumberUtils.isDigits(dayStr)) {
                day = Long.parseLong(dayStr);
            }

            if (NumberUtils.isDigits(hourStr)) {
                hour = Long.parseLong(hourStr);
            }

            if (NumberUtils.isDigits(minStr)) {
                min = Long.parseLong(minStr);
            }

            uptime = (((day) * 24 + (hour)) * 60 + (min)) * 60;
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
        }

        return uptime;
    }

    private List<ThirdPartyDiscoveryResult> getThirdPartySolutions(TargetHost targetHost, Map<String, RemoteExecResult> resultMap, String componentName) {
        List<ThirdPartyDiscoveryResult> thirdPartySolutions = null;

        try {
            List<Process> processList = getPsInfo(getResult(AssessmentItems.PROCESSES, resultMap), new HashMap<>());
            PortList ports = getListenPort(targetHost, getResult(AssessmentItems.NET_LISTEN_PORT, resultMap), getResult(AssessmentItems.NET_TRAFFICS, resultMap), new HashMap<>());
            Map<String, String> crontabMap = getCrontabs(targetHost, getResult(AssessmentItems.CRONTAB1, resultMap), new HashMap<>());
            Map<String, Map<String, String>> serviceMap = getDaemons(getResult(AssessmentItems.DAEMON_LIST, resultMap));

            List<String> processStringList = processList.stream().filter(p -> p.getCmd() != null).map(p -> String.join(" ", p.getCmd())).collect(Collectors.toList());
            List<String> processUserList = processList.stream().filter(p -> p.getUser() != null).map(p -> p.getUser()).collect(Collectors.toList());
            List<String> serviceList = new ArrayList<>(serviceMap.keySet());
            List<Integer> portList = ports.getListen().stream().filter(p -> StringUtils.isNotEmpty(p.getPort())).map(p -> Integer.parseInt(p.getPort())).collect(Collectors.toList());

            thirdPartySolutions = ThirdPartySolutionUtil.detectThirdPartySolutionsFromServer(targetHost, false, componentName, processStringList, processUserList, null, serviceList, portList, crontabMap);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while detect third party solutions.", e);
        }

        return thirdPartySolutions;
    }

    @Override
    public AixAssessmentResult assessment(TargetHost targetHost) throws InterruptedException {
        Map<String, String> cmdMap = generateCommand();
        Map<String, RemoteExecResult> resultMap = runCommands(targetHost, cmdMap);
        Map<String, String> errorMap = new HashMap<>();

        return AixAssessmentResult.builder()
                .architecture(getArchitecture(getResult(AssessmentItems.ARCHITECTURE, resultMap), errorMap))
                .firmwareVersion(getFirmware(getResult(AssessmentItems.FIRMWARE_VERSION, resultMap), errorMap))
                .cpu(getCpuInfo(targetHost, errorMap))
                .crontabs(getCrontabs(targetHost, getResult(AssessmentItems.CRONTAB1, resultMap), errorMap))
                .defInfo(getLoginDef())
                .interfaces(
                        getInterfacesInfo(
                                targetHost,
                                getResult(AssessmentItems.INTERFACES, resultMap),
                                getResult(AssessmentItems.INTERFACES_DEFAULT_GATEWAY, resultMap),
                                getUptimeSec(getResult(AssessmentItems.UPTIME, resultMap)),
                                errorMap
                        )
                )
                .distributionVersion(getDistributionVersion(getResult(AssessmentItems.DISTRIBUTION, resultMap), errorMap))
                .distributionRelease(getDistributionRelease(getResult(AssessmentItems.DISTRIBUTION, resultMap), errorMap))
                .dns(getDns(getResult(AssessmentItems.DNS, resultMap), errorMap))
                .hostname(getHostname(getResult(AssessmentItems.HOSTNAME, resultMap), errorMap))
                .family(getResult(AssessmentItems.OS_FAMILY, resultMap))
                .groups(getGroups(getResult(AssessmentItems.GROUPS, resultMap), errorMap))
                .hosts(getHosts(getResult(AssessmentItems.HOSTS, resultMap), errorMap))
                .kernel(getKernel(getResult(AssessmentItems.KERNEL, resultMap), errorMap))
                .kernelParameters(getKernelParams(getResult(AssessmentItems.KERNEL_PARAM, resultMap), errorMap))
                .portList(
                        getListenPort(
                                targetHost,
                                getResult(AssessmentItems.NET_LISTEN_PORT, resultMap),
                                getResult(AssessmentItems.NET_TRAFFICS, resultMap), errorMap
                        )
                )
                .processes(getPsInfo(getResult(AssessmentItems.PROCESSES, resultMap), errorMap))
                .shadows(getPasswordUsers(getResult(AssessmentItems.SHADOWS, resultMap), errorMap))
                .locale(getLocale(getResult(AssessmentItems.LOCALE, resultMap), errorMap))
                .security(
                        getSecurity(
                                getResult(AssessmentItems.SECURITY_LOGIN, resultMap),
                                getResult(AssessmentItems.SECURITY_PASSWORD, resultMap), errorMap
                        )
                )
                .partitions(
                        getDf(
                                getResult(AssessmentItems.PARTITIONS, resultMap),
                                getResult(AssessmentItems.PARTITIONS_TYPE, resultMap), errorMap
                        )
                )
                .extraPartitions(getExtraPartitions(getResult(AssessmentItems.EXSTRA_PARTITIONS, resultMap), errorMap))
                .daemons(getDaemons(getResult(AssessmentItems.DAEMON_LIST, resultMap)))
                .productName(getProductName(targetHost, errorMap))
                .productSerial(getProductSerial(targetHost, errorMap))
                .routeTables(getRouteTable(getResult(AssessmentItems.ROUTE_TABLE, resultMap), errorMap))
                .fileSystems(getFileSystem(getResult(AssessmentItems.FILESYSTEM, resultMap), errorMap))
                .timezone(getTimezone(getResult(AssessmentItems.TIMEZONE1, resultMap), errorMap))
                .memory(
                        getMemoryInfo(
                                getResult(AssessmentItems.MEMORY_FACTS, resultMap),
                                getResult(AssessmentItems.SWAP_FACTS, resultMap),
                                errorMap
                        )
                )
                .ulimits(getUlimits(targetHost, getResult(AssessmentItems.USER_LIST, resultMap), errorMap))
                .users(getUsers(targetHost, getResult(AssessmentItems.USERS, resultMap), errorMap))
                .env(getEnv(getResult(AssessmentItems.ENV, resultMap), errorMap))
                .vgs(getLvmInfo(targetHost, getResult(AssessmentItems.LVM_VGS, resultMap), errorMap))
                .uptime(getUptime(getResult(AssessmentItems.UPTIME, resultMap), errorMap))
                .systemVendor("IBM")
                .thirdPartySolutions(getThirdPartySolutions(targetHost, resultMap, this.getClass().getName()))
                .errorMap(errorMap)
                .packages(getPackage(getResult(AssessmentItems.PACKAGES, resultMap)))
                .build();
    }
}
//end of AixServerAssessment.java
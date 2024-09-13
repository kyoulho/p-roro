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
package io.playce.roro.svr.asmt.hpux.impl;

import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.dto.thirdparty.ThirdPartyDiscoveryResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.ThirdPartySolutionUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.AbstractServerAssessment;
import io.playce.roro.svr.asmt.AssessmentItems;
import io.playce.roro.svr.asmt.dto.common.config.Hosts;
import io.playce.roro.svr.asmt.dto.common.disk.FsTab;
import io.playce.roro.svr.asmt.dto.common.disk.Partition;
import io.playce.roro.svr.asmt.dto.common.hardware.CpuInfo;
import io.playce.roro.svr.asmt.dto.common.hardware.MemoryInfo;
import io.playce.roro.svr.asmt.dto.common.interfaces.InterfaceInfo;
import io.playce.roro.svr.asmt.dto.common.interfaces.Ipv4Address;
import io.playce.roro.svr.asmt.dto.common.interfaces.Ipv6Address;
import io.playce.roro.svr.asmt.dto.common.network.*;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import io.playce.roro.svr.asmt.dto.hpux.LogicalVolume;
import io.playce.roro.svr.asmt.dto.hpux.PhysicalVolume;
import io.playce.roro.svr.asmt.dto.hpux.VolumeGroup;
import io.playce.roro.svr.asmt.dto.linux.security.DefInfo;
import io.playce.roro.svr.asmt.dto.result.HpuxAssessmentResult;
import io.playce.roro.svr.asmt.dto.user.Group;
import io.playce.roro.svr.asmt.dto.user.User;
import io.playce.roro.svr.asmt.hpux.HpuxCommand;
import io.playce.roro.svr.asmt.linux.CommonCommand;
import io.playce.roro.svr.asmt.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Component("HPUXAssessment")
@Slf4j
public class HpuxServerAssessment extends AbstractServerAssessment {

    /**
     * Assessment 실행 전 추출 항목별 사용 Command 사전 정의.
     */
    public Map<String, String> generateCommand() {
        Map<String, String> cmdMap = new HashMap<>();
        cmdMap.put(AssessmentItems.ARCHITECTURE.toString(), HpuxCommand.ARCHITECTURE);
        cmdMap.put(AssessmentItems.CRONTAB1.toString(), HpuxCommand.CRONTAB1);
        cmdMap.put(AssessmentItems.GROUPS.toString(), HpuxCommand.GROUPS);
        cmdMap.put(AssessmentItems.DNS.toString(), HpuxCommand.DNS);
        cmdMap.put(AssessmentItems.HOSTNAME.toString(), HpuxCommand.HOSTNAME);
        cmdMap.put(AssessmentItems.KERNEL.toString(), HpuxCommand.KERNEL);
        cmdMap.put(AssessmentItems.KERNEL_PARAM.toString(), HpuxCommand.KERNEL_PARAM);
        cmdMap.put(AssessmentItems.LOCALE.toString(), HpuxCommand.LOCALE);
        cmdMap.put(AssessmentItems.TIMEZONE1.toString(), HpuxCommand.TIMEZONE);
        cmdMap.put(AssessmentItems.ROUTE_TABLE.toString(), HpuxCommand.ROUTE_TABLE);

        cmdMap.put(AssessmentItems.DISTRIBUTION.toString(), HpuxCommand.DISTRIBUTION_HPUX);

        cmdMap.put(AssessmentItems.PRODUCT_SERIAL.toString(), HpuxCommand.PRODUCT_SERIAL);

        cmdMap.put(AssessmentItems.USERS.toString(), HpuxCommand.USERS);
        cmdMap.put(AssessmentItems.ENV.toString(), HpuxCommand.ENV);

        cmdMap.put(AssessmentItems.MEM_FREE.toString(), HpuxCommand.MEM_FREE);
        cmdMap.put(AssessmentItems.MEM_TOTAL.toString(), HpuxCommand.MEM_TOTAL);
        cmdMap.put(AssessmentItems.SWAP_FREE.toString(), HpuxCommand.SWAP_FREE);
        cmdMap.put(AssessmentItems.SWAP_TOTAL.toString(), HpuxCommand.SWAP_TOTAL);

        cmdMap.put(AssessmentItems.SHADOWS.toString(), HpuxCommand.SHADOW);
        cmdMap.put(AssessmentItems.NET_LISTEN_PORT.toString(), HpuxCommand.NET_LISTEN_PORT);
        cmdMap.put(AssessmentItems.NET_TRAFFICS.toString(), HpuxCommand.NET_TRAFFICS);
        cmdMap.put(AssessmentItems.PARTITIONS.toString(), HpuxCommand.PARTITIONS);
        cmdMap.put(AssessmentItems.PROCESSES.toString(), HpuxCommand.PROCESSES);
        cmdMap.put(AssessmentItems.HOSTS.toString(), HpuxCommand.HOSTS);
        cmdMap.put(AssessmentItems.INTERFACES.toString(), HpuxCommand.INTERFACES);
        cmdMap.put(AssessmentItems.INTERFACES_DEFAULT_GATEWAY.toString(), HpuxCommand.INTERFACES_DEFAULT_GATEWAY);

        cmdMap.put(AssessmentItems.FSTAB.toString(), HpuxCommand.FSTAB);
        cmdMap.put(AssessmentItems.LVM_VGS.toString(), HpuxCommand.LVM_VGS);
        cmdMap.put(AssessmentItems.UPTIME.toString(), HpuxCommand.UPTIME);
        cmdMap.put(AssessmentItems.OS_FAMILY.toString(), HpuxCommand.OS_FAIMLY);
        cmdMap.put(AssessmentItems.PACKAGES.toString(), HpuxCommand.PACKAGES);
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

    public Map<String, Map<String, String>> getUlimits(TargetHost targetHost, Map<String, String> errorMap) throws InterruptedException {
        Map<String, Map<String, String>> ulimits = new HashMap<>();
        String[] ignoreUsers = {"daemon", "bin", "sys", "adm", "uucp", "guest", "nobody",
                "lpd",
                "lp", "invscout", "snapp", "ipsec", "nuucp", "sshd", "ftp",
                "anonymou"};
        try {
            String userList = SSHUtil.executeCommand(targetHost, HpuxCommand.USER_LIST);

            Map<String, String> cmdMaps = new HashMap<>();
            if (StringUtils.isNotEmpty(userList)) {
                userList = userList.replaceAll("\\t", StringUtils.EMPTY);

                if ("root".equals(targetHost.getUsername()) || StringUtils.isNotEmpty(targetHost.getRootPassword()) || SSHUtil.isSudoer(targetHost)) {
                    List<String> users = ResultUtil.removeCommentLine(userList);
                    for (String user : users) {
                        if (Arrays.stream(ignoreUsers).noneMatch(u -> u.equals(user))) {
                            cmdMaps.put(user, String.format(HpuxCommand.ULIMIT, user));
                        }
                    }

                    Map<String, RemoteExecResult> resultMap = runCommands(targetHost, cmdMaps);

                    for (String key : resultMap.keySet()) {
                        String result = resultMap.get(key).getResult();
                        if (StringUtils.isNotEmpty(result)) {
                            Map<String, String> ulimit = new HashMap<>();
                            for (String limit : result.split("\n")) {
                                String item = limit.substring(0, limit.indexOf('(')).strip();
                                String[] value = limit.substring(limit.indexOf('(')).split("\\s+");
                                ulimit.put(item, value[value.length - 1]);
                            }
                            ulimits.put(key, ulimit);
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

    public List<FsTab> getFstabInfo(String result, Map<String, String> errorMap) throws InterruptedException {
        List<FsTab> fsTabs = new ArrayList<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                Pattern p = Pattern.compile("^#");

                for (String line : result.split("\n")) {

                    if (p.matcher(line).find() || line.equals("") || line.equals("\n")) {
                        continue;
                    }

                    String[] data = line.split("\\s+");

                    FsTab fsTab = new FsTab();

                    fsTab.setDevice(data[0]);
                    fsTab.setMount(data[1]);
                    fsTab.setType(data[2]);
                    fsTab.setOption(data[4]);
                    fsTab.setDump(data[5]);

                    fsTabs.add(fsTab);
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.FSTAB.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return fsTabs;
    }

    public String getDistributionVersion(String result, Map<String, String> errorMap) throws InterruptedException {
        // HP-UX rx236 B.11.31 U ia64 3973013555 unlimited-user license
        try {
            if (StringUtils.isNotEmpty(result)) {
                Pattern p = Pattern.compile("HPUX.*OE.*([AB]\\.[0-9]+\\.[0-9]+)\\.([0-9]+).*");
                Matcher m = p.matcher(result);

                if (m.find()) {
                    result = m.group(1);
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DISTRIBUTION.name() + "_VERSION", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return result;
    }

    public String getDistributionRelease(String result, Map<String, String> errorMap) throws InterruptedException {
        // HP-UX rx236 B.11.31 U ia64 3973013555 unlimited-user license
        try {
            if (StringUtils.isNotEmpty(result)) {
                Pattern p = Pattern.compile("HPUX.*OE.*([AB]\\.[0-9]+\\.[0-9]+)\\.([0-9]+).*");
                Matcher m = p.matcher(result);

                // B.11.31
                if (m.find()) {
                    result = "HP-UX " + m.group(1).replaceAll("B.", "");
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DISTRIBUTION.name() + "_RELEASE", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return result;
    }

    // public String getFirmware() {
    //     String firmware = null;
    //     try {
    //         String result = SSHUtil.executeCommand(TargetHost.convert(server), HpuxCommand.FIRMWARE_VERSION);
    //
    //         if (StringUtils.isNotEmpty(result)) {
    //             String[] data = result.split("\\s+");
    //             firmware = data[1].strip();
    //         }
    //     } catch (Exception e) {
    //         log.debug(e.getMessage());
    //     }
    //
    //     return firmware;
    // }

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
                        String[] g_infos = line.split(":", 4);

                        if (Arrays.stream(exceptGroups).noneMatch(u -> u.equals(g_infos[0]))) {
                            Group group = new Group();
                            group.setGid(g_infos[2]);
                            group.setUsers(Arrays.asList(g_infos[3].split(",")));

                            groups.put(g_infos[0], group);
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

    public Map<String, InterfaceInfo> getInterfacesInfo(TargetHost targetHost, String ifResult, String gwResult, Map<String, String> errorMap) throws InterruptedException {

        Map<String, InterfaceInfo> interfaces = new HashMap<>();
        try {

            InterfaceInfo iterInfo = null;
            if (StringUtils.isNotEmpty(ifResult)) {
                for (String line : ifResult.split("\n")) {

                    if (StringUtils.isNotEmpty(line)) {

                        String[] words = line.trim().split("\\s+");

                        Pattern p = Pattern.compile("^\\w*\\d*:");
                        Matcher m = p.matcher(line);
                        if (m.find()) {
                            iterInfo = initInterface(words);
                            interfaces.put(iterInfo.getDevice(), iterInfo);
                            getDefaultGateway(iterInfo, gwResult, errorMap);
                            getMacAddress(targetHost, iterInfo);
                            getRxTxBytes(targetHost, iterInfo);
                            // getIfcfgScript(iterInfo);
                        } else if (words[0].startsWith("options=")) {
                            // parseOptionsLine(words, iterInfo);
                        } else if (words[0].equals("nd6")) {
                            // parseOptionsLine(words, iterInfo);
                        } else if (words[0].equals("media")) {
                            // parseOptionsLine(words, iterInfo);
                        } else if (words[0].equals("lladdr")) {
                            // parseOptionsLine(words, iterInfo);
                        } else if (words[0].equals("status")) {
                            // parseOptionsLine(words, iterInfo);
                        } else if (words[0].equals("ether")) {
                            parseEtherLine(words, iterInfo);
                        } else if (words[0].equals("inet")) {
                            parseInetLine(words, iterInfo);
                        } else if (words[0].equals("inet6")) {
                            parseInet6line(words, iterInfo);
//                        } else {
//                            parseUnkownLine(words, iterInfo);
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

    private InterfaceInfo initInterface(String[] words) {
        InterfaceInfo info = new InterfaceInfo();
        info.setDevice(words[0].substring(0, words[0].length() - 1));
        info.setGateway("unknown");
        info.setScript("unknown");
        info.setMacaddress("unknown");
        return info;
    }

    private void getDefaultGateway(InterfaceInfo iterInfo, String result, Map<String, String> errorMap) throws InterruptedException {
        try {
            if (StringUtils.isNotEmpty(result)) {
                String gw = null;
                String[] lines = result.split("\n");

                for (String line : lines) {
                    String[] data = line.split("\\s+");

                    if (data.length > 5) {
                        if (data[4].equals(iterInfo.getDevice())) {
                            gw = data[1];
                        }
                    }
                }

                if (StringUtils.isEmpty(gw)) {
                    for (String line : lines) {
                        String[] data = line.split("\\s+");

                        if (data.length > 5) {
                            if (iterInfo.getDevice().startsWith(data[4])) {
                                gw = data[1];
                            }
                        }
                    }
                }

                if (StringUtils.isNotEmpty(gw)) {
                    iterInfo.setGateway(gw);
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.INTERFACES_DEFAULT_GATEWAY.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
    }

    private void parseOptionsLine(String[] words, InterfaceInfo iterInfo) {

    }

    private void getMacAddress(TargetHost targetHost, InterfaceInfo iterInfo) throws InterruptedException {

        try {
            String macAddress = SSHUtil.executeCommand(targetHost, String.format(HpuxCommand.INTERFACES_MAC_ADDRESS, iterInfo.getDevice()));
            if (StringUtils.isNotEmpty(macAddress)) {
                String[] data = macAddress.split("\\s+");

                iterInfo.setMacaddress(data[1].strip().replaceAll("\n", ""));
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
        }
    }

    private void getRxTxBytes(TargetHost targetHost, InterfaceInfo iterInfo) throws InterruptedException {
        try {
            String result = SSHUtil.executeCommand(targetHost, String.format(CommonCommand.INTERFACES_RX_TX_SCRIPTS1, iterInfo.getDevice()));

            if (StringUtils.isNotEmpty(result) && result.contains(StringUtils.SPACE)) {
                Double rxBytes = Double.parseDouble(result.split(StringUtils.SPACE)[0]) * 1024;
                Double txBytes = Double.parseDouble(result.split(StringUtils.SPACE)[1]) * 1024;

                iterInfo.setRxBytes(rxBytes.longValue());
                iterInfo.setTxBytes(txBytes.longValue());
            } else {
                /** HP-UX NOT working below command */
                // result = SSHUtil.executeCommand(targetHost, String.format(CommonCommand.INTERFACES_RX_TX_SCRIPTS2, iterInfo.getDevice(), iterInfo.getDevice()));
                //
                // if (StringUtils.isNotEmpty(result)) {
                //     result = result.replaceAll("(\r\n|\r|\n|\n\r)", StringUtils.SPACE);
                //     Long rxBytes = Long.parseLong(result.split(StringUtils.SPACE)[0]);
                //     Long txBytes = Long.parseLong(result.split(StringUtils.SPACE)[1]);
                //     Long uptime = Long.parseLong(result.split(StringUtils.SPACE)[2]);
                //
                //     rxBytes = rxBytes / uptime;
                //     txBytes = txBytes / uptime;
                //
                //     iterInfo.setRxBytes(rxBytes);
                //     iterInfo.setTxBytes(txBytes);
                // }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
        }
    }

    public String getKernel(String result, Map<String, String> errorMap) throws InterruptedException {
        try {
            if (StringUtils.isNotEmpty(result)) {
                String[] data = null;
                for (String line : result.split("\n")) {
                    data = line.strip().split("\\s+");
                    break;
                }
                result = data != null && data.length > 1 ? data[1] : null;
            }
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

    public PortList getListenPort(TargetHost targetHost, String listenPort, String traffics) throws InterruptedException {

        PortList portList = new PortList();

        List<ListenPort> listen = new ArrayList<>();
        EstablishedPort established = new EstablishedPort();
        WaitPort wait = new WaitPort();

        portList.setListen(listen);
        portList.setEstablished(established);
        portList.setWait(wait);

        try {
            Map<String, String> lsofCmds = new HashMap<>();
            if (StringUtils.isNotEmpty(listenPort)) {
                Map<String, ListenPort> tempMap = new HashMap<>();

                for (String line : listenPort.split("\n")) {
                    String[] data = line.split("\\s+");

                    if (data.length > 1) {
                        if ("unix".equals(data[0]))
                            continue;

//                        String[] sourceAddrPort = data[3].split("\\.", 2);
                        int index = data[3].lastIndexOf(".");
                        if (index == -1)
                            continue;

                        String sourceAddr = data[3].substring(0, index);
                        String sourcePort = data[3].substring(index + 1);

                        // Map<String, String> psMap = getProcList(targetHost, sourceAddrPort[1]);

//                        lsofCmds.put(sourceAddrPort[1], String.format(HpuxCommand.NET_LSOF, sourceAddrPort[1]));
                        lsofCmds.put(sourcePort, String.format(HpuxCommand.NET_LSOF, sourcePort));

//                        if (StringUtils.countMatches(sourceAddrPort[0], ":") < 4) {
                        if (StringUtils.countMatches(sourceAddr, ":") < 4) {
                            ListenPort port = new ListenPort();
                            port.setProtocol(data[0]);
//                            port.setBindAddr(sourceAddrPort[0]);
                            port.setBindAddr(sourceAddr);
//                            port.setPort(sourceAddrPort[1]);
                            port.setPort(sourcePort);
                            // port.setPid(psMap.get("pid"));
                            // port.setName(psMap.get("name"));
//                            tempMap.put(sourceAddrPort[1], port);
                            tempMap.put(sourcePort, port);
                            // listen.add(port);
                        }
                    }
                }

                Map<String, RemoteExecResult> resultMap = runCommands(targetHost, lsofCmds);

                for (String key : resultMap.keySet()) {
                    ListenPort port = tempMap.getOrDefault(key, null);

                    RemoteExecResult result = resultMap.get(key);

                    String pid = "";
                    String pName = "";
                    if (StringUtils.isNotEmpty(result.getResult())) {
                        for (String ps : result.getResult().split("\n")) {
                            String[] data = ps.strip().split("\\s+");
                            pid = data[0];
                            pName = data[1];
                        }
                    }
                    port.setPid(pid);
                    port.setName(pName);
                    listen.add(port);
                }

            }

            if (StringUtils.isNotEmpty(traffics)) {
                // Map<String, Traffic> tempMap = new HashMap<>();
                for (String line : traffics.split("\n")) {
                    String[] data = line.split("\\s+");
                    if (data.length > 1) {
                        String pid = "";
                        String pname = "";
                        String[] sourceAddrPort = getSplitAddress(data[3]);
                        String[] destinationAddrPort = getSplitAddress(data[4]);

                        if (sourceAddrPort[0].equals("127.0.0.1") && destinationAddrPort[0].equals("127.0.0.1"))
                            continue;

                        String conStatus;

                        try {
                            conStatus = data[5].toLowerCase();
                        } catch (IndexOutOfBoundsException e) {
                            conStatus = "";
                        }

                        String finalConStatus = conStatus;
                        if (CHECK_NETSTAT_STATUS.stream().noneMatch(i -> i.equalsIgnoreCase(finalConStatus)))
                            continue;

                        // lsofCmds.put(sourceAddrPort[1], String.format(HpuxCommand.NET_LSOF, sourceAddrPort[1]));

                        Optional<ListenPort> portInfo =
                                listen.stream().filter(p -> p.getPort().equals(sourceAddrPort[1])).findFirst();


                        if (portInfo.isPresent()) {
                            pid = portInfo.get().getPid();
                            pname = portInfo.get().getName();
                        }

                        Traffic traffic = new Traffic();
                        traffic.setProtocol(data[0]);
                        traffic.setFaddr(destinationAddrPort[0]);
                        traffic.setFport(destinationAddrPort[1]);
                        traffic.setLaddr(sourceAddrPort[0]);
                        traffic.setLport(sourceAddrPort[1]);
                        traffic.setPid(pid);
                        traffic.setName(pname);
                        traffic.setStatus(conStatus);

                        if (conStatus.contains("wait")) {
                            if (portInfo.isPresent()) {
                                wait.getAnyToLocal().add(traffic);
                            } else {
                                wait.getLocalToAny().add(traffic);
                            }
                        } else {
                            if (portInfo.isPresent()) {
                                established.getAnyToLocal().add(traffic);
                            } else {
                                established.getLocalToAny().add(traffic);
                            }
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
        return new String[]{
                addr.substring(0, addr.lastIndexOf(".")),
                addr.substring(addr.lastIndexOf(".") + 1)
        };
    }

    // public Map<String, String> getProcList(TargetHost targetHost, String port) {
    //     Map<String, String> psMap = new HashMap<>();
    //     try {
    //         String result = SSHUtil.executeCommand(targetHost, String.format(HpuxCommand.NET_LSOF, port));
    //
    //         if (StringUtils.isNotEmpty(result)) {
    //
    //             for (String ps : result.split("\n")) {
    //                 String[] data = ps.strip().split("\\s+");
    //
    //
    //                 psMap.put("pid", data[0]);
    //                 psMap.put("name", data[1]);
    //             }
    //
    //         }
    //     } catch (Exception e) {
    //         log.debug(e.getMessage());
    //     }
    //     return psMap;
    // }

    public Map<String, Partition> getDf(TargetHost targetHost, String result, Map<String, String> errorMap) throws InterruptedException {

        Map<String, Partition> partitions = new HashMap<>();
        try {
            String[] ignorePartitions = {"tmpfs"};
            String[] pt_combine = {};
            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {
                    String[] temp = line.split("\\s+");

                    pt_combine = Stream.of(
                                    pt_combine, temp)
                            .flatMap(Stream::of)
                            .toArray(String[]::new);

                    if (pt_combine.length == 6) {
                        String[] copyPartitions = pt_combine;
                        boolean isMatch = Arrays.stream(ignorePartitions).allMatch(p -> p.equals(copyPartitions[0]));

                        if (!isMatch) {
                            result = SSHUtil.executeCommand(targetHost, String.format(HpuxCommand.PARTITIONS_TYPE, pt_combine[0]));

                            Partition p = new Partition();
                            p.setDevice(pt_combine[0]);
                            p.setFsType(result.strip());
                            p.setSize(String.valueOf(Integer.parseInt(pt_combine[1]) / 1024));
                            p.setFree(String.valueOf(Integer.parseInt(pt_combine[3]) / 1024));
                            p.setMountPath(pt_combine[5]);

                            partitions.put(pt_combine[5], p);
                        }

                        pt_combine = new String[]{};
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.PARTITIONS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return partitions;
    }

    //
    // public Map<String, List<ExtraPartition>> getExtraPartitions() {
    //
    //     Map<String, List<ExtraPartition>> extraPartitions = new HashMap<>();
    //     try {
    //         String[] ignorePartitions = {"N/A", "/", "/usr", "/var", "/tmp", "/home", "/proc",
    //                 "/opt", "/admin", "/var/adm/ras/livedump"};
    //
    //         String result = SSHUtil.executeCommand(TargetHost.convert(server), HpuxCommand.EXSTRA_PARTITIONS);
    //
    //         if (StringUtils.isNotEmpty(result)) {
    //
    //             ExtraPartition partition = null;
    //
    //             for (String line : result.split("\n")) {
    //
    //                 String[] data = line.split("\\s+");
    //
    //                 if (data[0].contains("rootvg:") || data[0].contains("LV"))
    //                     continue;
    //
    //                 partition = new ExtraPartition();
    //                 partition.setMountPoint(data[0]);
    //                 partition.setType(data[1]);
    //                 partition.setLvState(data[5]);
    //                 partition.setExtra("False");
    //
    //                 if (!extraPartitions.containsKey(data[6])) {
    //                     List<ExtraPartition> partitionList = new ArrayList<>();
    //                     partitionList.add(partition);
    //
    //                     extraPartitions.put(data[6], partitionList);
    //                 } else if (!Arrays.stream(ignorePartitions).anyMatch(u -> u.equals(data[6]))) {
    //                     partition.setExtra("True");
    //                     extraPartitions.get(data[6]).add(partition);
    //                 } else {
    //                     extraPartitions.get(data[6]).add(partition);
    //                 }
    //             }
    //         }
    //     } catch (Exception e) {
    //         log.debug(e.getMessage());
    //     }
    //     return extraPartitions;
    // }
    //
    // private String getFsType(String device, String rootVgs) {
    //     try {
    //         String deviceName = device.split("/")[2];
    //
    //         for (String line : rootVgs.split("\n")) {
    //             Pattern a = Pattern.compile(String.format("^%s", deviceName));
    //             if (a.matcher(line).find()) {
    //                 return line.split("\\s+")[1];
    //             }
    //         }
    //     } catch (Exception e) {
    //         log.debug(e.getMessage());
    //     }
    //
    //     return null;
    // }


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
                            if (Pattern.matches("\\[(.*?)]", data[7])) {
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

    public CpuInfo getCpuInfo(TargetHost targetHost, String distributeResult, Map<String, String> errorMap) throws InterruptedException {

        CpuInfo cpuInfo = new CpuInfo();

        try {
            String version = getDistributionVersion(distributeResult, errorMap);
            Map<String, String> cmdMap = new HashMap<>();
            if (version.equals("B.11.23")) {

                cmdMap.put(AssessmentItems.PROCESSOR.toString(), HpuxCommand.PROCESSOR_11_23);
                cmdMap.put(AssessmentItems.PROCESSOR_COUNT.toString(), HpuxCommand.PROCESSOR_COUNT_11_23);
                cmdMap.put(AssessmentItems.PROCESSOR_CORES.toString(), HpuxCommand.PROCESSOR_CORES_11_23);

                Map<String, RemoteExecResult> resultMap = runCommands(targetHost, cmdMap);

                String count = resultMap.get(AssessmentItems.PROCESSOR_COUNT.toString()).getResult();
                if (StringUtils.isNotEmpty(count)) {
                    cpuInfo.setProcessorCount(String.valueOf(count.strip().split("=")[1]));
                }
                String cpu = resultMap.get(AssessmentItems.PROCESSOR.toString()).getResult();
                if (StringUtils.isNotEmpty(cpu)) {
                    cpuInfo.setProcessor(cpu.strip().split("=")[1].strip().replaceAll("\"", ""));
                }

                String cores = resultMap.get(AssessmentItems.PROCESSOR_CORES.toString()).getResult();
                if (StringUtils.isNotEmpty(cores)) {
                    cpuInfo.setProcessorCores(String.valueOf(cores.strip()));
                }
            } else if (version.equals("B.11.31")) {

                String result = SSHUtil.executeCommand(targetHost, HpuxCommand.PROCESSOR_MACHINE_TYPE);
                if (result.strip().equals("0")) {

                    cmdMap.put(AssessmentItems.PROCESSOR.toString(), HpuxCommand.PROCESSOR1_11_31);
                    cmdMap.put(AssessmentItems.PROCESSOR_COUNT.toString(), HpuxCommand.PROCESSOR1_COUNT_11_31);

                    Map<String, RemoteExecResult> resultMap = runCommands(targetHost, cmdMap);

                    String count = resultMap.get(AssessmentItems.PROCESSOR_COUNT.toString()).getResult();
                    if (StringUtils.isNotEmpty(count)) {
                        cpuInfo.setProcessorCount(String.valueOf(count.strip().split("\\s+")[0]));
                    }

                    String cpu = resultMap.get(AssessmentItems.PROCESSOR.toString()).getResult();
                    if (StringUtils.isNotEmpty(cpu)) {
                        cpuInfo.setProcessor(String.valueOf(cpu.strip()));
                    }

                    String temp = SSHUtil.executeCommand(targetHost, HpuxCommand.PROCESSOR1_CORES_HYPER_THREADING_11_31);
                    String[] data = temp.replaceAll(" +", "").strip().split("\\s+");
                    String hyperthreading = "OFF";
                    if (data.length != 1) {
                        hyperthreading = data[1];
                    }

                    temp = SSHUtil.executeCommand(targetHost, HpuxCommand.PROCESSOR1_CORES_LOGICAL_11_31);
                    data = temp.strip().split("\\s+");

                    if (hyperthreading.equals("ON")) {
                        cpuInfo.setProcessorCores(String.valueOf(Integer.parseInt(data[0]) / 2));
                    } else {
                        if (data.length == 1) {
                            cpuInfo.setProcessorCores(cpuInfo.getProcessorCount());
                        } else {
                            cpuInfo.setProcessorCores(String.valueOf(data[0]));
                        }
                    }

                } else {

                    cmdMap.put(AssessmentItems.PROCESSOR.toString(), HpuxCommand.PROCESSOR2_11_31);
                    cmdMap.put(AssessmentItems.PROCESSOR_COUNT.toString(), HpuxCommand.PROCESSOR2_COUNT_11_31);
                    cmdMap.put(AssessmentItems.PROCESSOR_CORES.toString(), HpuxCommand.PROCESSOR2_CORES_11_31);

                    Map<String, RemoteExecResult> resultMap = runCommands(targetHost, cmdMap);

                    String count = resultMap.get(AssessmentItems.PROCESSOR_COUNT.toString()).getResult();
                    if (StringUtils.isNotEmpty(count)) {
                        cpuInfo.setProcessorCount(count.strip().split("\\s+")[0]);
                    }
                    String cpu = resultMap.get(AssessmentItems.PROCESSOR.toString()).getResult();
                    if (StringUtils.isNotEmpty(cpu)) {
                        cpuInfo.setProcessor(String.valueOf(cpu.strip().split("\\s+")[0]));
                    }

                    String cores = resultMap.get(AssessmentItems.PROCESSOR_CORES.toString()).getResult();
                    if (StringUtils.isNotEmpty(cores)) {
                        cpuInfo.setProcessorCores(String.valueOf(cores.strip()));
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.CPU_FACTS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return cpuInfo;
    }

    public String getProductName() {
        return "HP-UX";
    }

    public String getProductSerial(String result, Map<String, String> errorMap) throws InterruptedException {
        String productSerial = null;
        try {
            if (StringUtils.isNotEmpty(result)) {
                productSerial = result.strip();
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.PRODUCT_SERIAL.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return productSerial;
    }

    public Map<String, String> getPasswordUsers(String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, String> shadow = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                result = result.replaceAll(":\n", ":");

                List<String> passwordList = ResultUtil.removeCommentLine(result);
                for (String line : passwordList) {
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

    public MemoryInfo getMemoryInfo(TargetHost targetHost, String architectureResult, Map<String, String> errorMap) throws InterruptedException {

        MemoryInfo memory = new MemoryInfo();
        try {
            String bitMode = getArchitecture(architectureResult, errorMap);
            if (bitMode.equals("9000/800")) {

            } else {
                Map<String, String> cmdMap = new HashMap<>();
                cmdMap.put(AssessmentItems.MEM_FREE.toString(), HpuxCommand.MEM_FREE);
                cmdMap.put(AssessmentItems.MEM_TOTAL.toString(), HpuxCommand.MEM_TOTAL);
                cmdMap.put(AssessmentItems.SWAP_FREE.toString(), HpuxCommand.SWAP_FREE);
                cmdMap.put(AssessmentItems.SWAP_TOTAL.toString(), HpuxCommand.SWAP_TOTAL);

                Map<String, RemoteExecResult> resultMap = runCommands(targetHost, cmdMap);

                String memFree = resultMap.get(AssessmentItems.MEM_FREE.toString()).getResult();
                if (StringUtils.isNotEmpty(memFree)) {
                    memFree = memFree.replaceAll(" +", StringUtils.SPACE).split("\\s+")[3].strip();
                    memory.setMemFreeMb(memFree);
                }

                String memTotal = resultMap.get(AssessmentItems.MEM_TOTAL.toString()).getResult();
                if (StringUtils.isNotEmpty(memTotal)) {
                    Pattern p = Pattern.compile("Memory[ :=]*(\\d*).*MB.*");
                    Matcher m = p.matcher(memTotal);
                    if (m.find()) {
                        memory.setMemTotalMb(m.group(1).strip());
                    }
                }
                String swapFree = resultMap.get(AssessmentItems.SWAP_FREE.toString()).getResult();
                if (StringUtils.isNotEmpty(swapFree)) {
                    swapFree = swapFree.strip();
                    memory.setSwapFreeMb(swapFree);
                }

                String swapTotal = resultMap.get(AssessmentItems.SWAP_TOTAL.toString()).getResult();
                if (StringUtils.isNotEmpty(swapTotal)) {
                    int swapSum = 0;
                    for (String line : swapTotal.split("\n")) {
                        swapSum += Integer.parseInt(line.replaceAll(" +", " ").split("\\s+")[3].strip());
                    }
                    memory.setSwapTotalMb(String.valueOf(swapSum));
                }
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
                            line.contains("Routing") ||
                            line.contains("Destination"))
                        continue;

                    String[] route_infos = line.split("\\s+");

                    if (!route_infos[0].equals("Route")) {
                        RouteTable routeTable = new RouteTable();
                        routeTable.setDestination(route_infos[0]);
                        routeTable.setGateway(route_infos[1]);
                        routeTable.setIface(route_infos[4]);

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

        try {
            String[] ignoreUsers = {"daemon", "bin", "sys", "adm", "uucp", "guest", "nobody",
                    "lpd",
                    "lp", "invscout", "snapp", "ipsec", "nuucp", "sshd", "ftp",
                    "anonymou", "www"};

            Map<String, String> profileCmd = new HashMap<>();
            Map<String, String> bashCmd = new HashMap<>();
            if (StringUtils.isNotEmpty(result)) {

                List<String> passwordList = ResultUtil.removeCommentLine(result);
                for (String line : passwordList) {
                    User user = new User();

                    String[] data = line.split(":");
                    if (data.length <= 6) {
                        log.debug("not defined shell: {}", Arrays.toString(data));
                        continue;
                    }
                    if (Arrays.stream(ignoreUsers).noneMatch(u -> u.equals(data[0]))) {
                        profileCmd.put(data[0], "sh -c '/usr/bin/cat " + data[5] + "/.*profile'");
                        bashCmd.put(data[0], "sh -c '/usr/bin/cat " + data[5] + "/.*rc'");

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
                String value;

                for (String line : result.split("\n")) {

                    if (line.contains("=")) {
                        String[] data = line.split("=", 2);
                        key = data[0];
                        value = data[1].replaceAll("\"", "");
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

                for (String line : result.split("\n")) {
                    if (Pattern.matches("^#", line))
                        continue;

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
        defInfo.setUidMin("0");
        defInfo.setUidMax("2147483646");
        defInfo.setGidMin("0");
        defInfo.setGidMax("2147483646");
        return defInfo;
    }

    public String getFamily() {
        return "hp-ux";
    }

    private void parseEtherLine(String[] words, InterfaceInfo iterInfo) {
        iterInfo.setMacaddress(words[1]);
    }

    /*private void parseUnkownLine(String[] words, InterfaceInfo iterInfo) {
        return;
    }*/

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

        Pattern p = Pattern.compile("([\\da-f]){8}");
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

    private void getIfcfgScript(InterfaceInfo iterInfo) {

    }

    //
    // public String getUptime() {
    //     System.out.println("getUptime");
    //     return null;
    // }

    protected Map<String, VolumeGroup> getLvmInfo(String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, VolumeGroup> vgs = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
//                LogicalVolume lv = null;
                LogicalVolume lv = new LogicalVolume();
//                PhysicalVolume pv = null;
                PhysicalVolume pv = new PhysicalVolume();
//                VolumeGroup vg = null;
                VolumeGroup vg = new VolumeGroup();
                String vgName = null;
                String value = null;
                for (String line : result.split("\n")) {
                    line = line.strip();
                    if (line.contains("VG Name")) {
                        String[] data = line.split("\\s+");
                        vgName = data[data.length - 1];
//                        vg = new VolumeGroup();
                        vgs.put(vgName, vg);
                    } else if (line.contains("LV Name")) {
//                        lv = new LogicalVolume();
                        value = line.replace("LV Name", StringUtils.EMPTY).strip();
                        lv.setLvName(value);
                        vg.getLvs().add(lv);
                    } else if (line.contains("LV Status")) {
                        value = line.replace("LV Status", StringUtils.EMPTY).strip();
                        lv.setLvStatus(value);
                    } else if (line.contains("LV Size (Mbytes)")) {
                        value = line.replace("LV Size (Mbytes)", StringUtils.EMPTY).strip();
                        lv.setLvSize(value);
                    } else if (line.contains("Current LE")) {
                        value = line.replace("Current LE", StringUtils.EMPTY).strip();
                        lv.setCurrentLe(value);
                    } else if (line.contains("Allocated PE")) {
                        value = line.replace("Allocated PE", StringUtils.EMPTY).strip();
                        lv.setAllocatedPe(value);
                    } else if (line.contains("Used PV")) {
                        value = line.replace("Used PV", StringUtils.EMPTY).strip();
                        lv.setUsedPv(value);
                    } else if (line.contains("PV Name")) {
//                        pv = new PhysicalVolume();
                        value = line.replace("PV Name", StringUtils.EMPTY).strip();
                        pv.setPvName(value);
                        vg.getPvs().add(pv);
                    } else if (line.contains("PV Status")) {
                        value = line.replace("PV Status", StringUtils.EMPTY).strip();
                        pv.setPvStatus(value);
                    } else if (line.contains("Total PE")) {
                        value = line.replace("Total PE", StringUtils.EMPTY).strip();
                        pv.setTotalPe(value);
                    } else if (line.contains("Free PE")) {
                        value = line.replace("Free PE", StringUtils.EMPTY).strip();
                        pv.setFreePe(value);
                    } else if (line.contains("Autoswitch")) {
                        value = line.replace("Autoswitch", StringUtils.EMPTY).strip();
                        pv.setAutoSwitch(value);
                    } else if (line.contains("Proactive Polling")) {
                        value = line.replace("Proactive Polling", StringUtils.EMPTY).strip();
                        pv.setProactivePolling(value);
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.LVM_VGS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return vgs;
    }

    protected Long getUptime(String uptime, Map<String, String> errorMap) throws InterruptedException {
        // Long result = null;
        // try {
        //     if (StringUtils.isNotEmpty(uptime)) {
        //         String[] times = uptime.split("\\s+");
        //
        //         int day;
        //         int hour;
        //         int sec;
        //
        //         day = Integer.parseInt(times[0]);
        //         hour = Integer.parseInt(times[1]);
        //
        //         if (times[2].equals("min") || times[2].equals("mins")) {
        //             sec = hour;
        //             hour = 0;
        //         } else {
        //             sec = Integer.parseInt(times[2]);
        //         }
        //
        //         long now = System.currentTimeMillis();
        //
        //         long timestamp = (((day) * 24L + (hour)) * 60 + (sec)) * 60;
        //         result = now - timestamp;
        //     }
        // } catch (Exception e) {
        //     RoRoException.checkInterruptedException(e);
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

    private List<ThirdPartyDiscoveryResult> getThirdPartySolutions(TargetHost targetHost, Map<String, RemoteExecResult> resultMap, String componentName) {
        List<ThirdPartyDiscoveryResult> thirdPartySolutions = null;

        try {
            List<Process> processList = getPsInfo(getResult(AssessmentItems.PROCESSES, resultMap), new HashMap<>());
            PortList ports = getListenPort(targetHost, getResult(AssessmentItems.NET_LISTEN_PORT, resultMap), getResult(AssessmentItems.NET_TRAFFICS, resultMap));
            Map<String, String> crontabMap = getCrontabs(targetHost, getResult(AssessmentItems.CRONTAB1, resultMap), new HashMap<>());

            List<String> processStringList = processList.stream().filter(p -> p.getCmd() != null).map(p -> String.join(" ", p.getCmd())).collect(Collectors.toList());
            List<String> processUserList = processList.stream().filter(p -> p.getUser() != null).map(p -> p.getUser()).collect(Collectors.toList());
            List<String> serviceList = null;
            List<Integer> portList = ports.getListen().stream().filter(p -> StringUtils.isNotEmpty(p.getPort())).map(p -> Integer.parseInt(p.getPort())).collect(Collectors.toList());

            thirdPartySolutions = ThirdPartySolutionUtil.detectThirdPartySolutionsFromServer(targetHost, false, componentName, processStringList, processUserList, null, serviceList, portList, crontabMap);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while detect third party solutions.", e);
        }

        return thirdPartySolutions;
    }

    @Override
    public HpuxAssessmentResult assessment(TargetHost targetHost) throws InterruptedException {

        /*
         *  Assessment 명령어 생성
         * */
        Map<String, String> cmdMap = generateCommand();

        /*
         *  Assessment 명령어 실행
         * */
        Map<String, RemoteExecResult> resultMap = runCommands(targetHost, cmdMap);
        Map<String, String> errorMap = new HashMap<>();

        /*
         * 수집 항목 별 명령어 결과값 파싱
         * */
        return HpuxAssessmentResult.builder()
                .architecture(getArchitecture(getResult(AssessmentItems.ARCHITECTURE, resultMap), errorMap))
                .crontabs(getCrontabs(targetHost, getResult(AssessmentItems.CRONTAB1, resultMap), errorMap))
                .defInfo(getLoginDef())
                .interfaces(
                        getInterfacesInfo(
                                targetHost,
                                getResult(AssessmentItems.INTERFACES, resultMap),
                                getResult(AssessmentItems.INTERFACES_DEFAULT_GATEWAY, resultMap),
                                errorMap
                        )
                )
                .distribution(getResult(AssessmentItems.OS_FAMILY, resultMap))
                .distributionVersion(getDistributionVersion(getResult(AssessmentItems.DISTRIBUTION, resultMap), errorMap))
                .distributionRelease(getDistributionRelease(getResult(AssessmentItems.DISTRIBUTION, resultMap), errorMap))
                .dns(getDns(getResult(AssessmentItems.DNS, resultMap), errorMap))
                .hostname(getHostname(getResult(AssessmentItems.HOSTNAME, resultMap), errorMap))
                .family(getResult(AssessmentItems.OS_FAMILY, resultMap))
                .groups(getGroups(getResult(AssessmentItems.GROUPS, resultMap), errorMap))
                .hosts(getHosts(getResult(AssessmentItems.HOSTS, resultMap), errorMap))
                .kernel(getKernel(getResult(AssessmentItems.DISTRIBUTION, resultMap), errorMap))
                .kernelParameters(getKernelParams(getResult(AssessmentItems.KERNEL_PARAM, resultMap), errorMap))
                .portList(
                        getListenPort(
                                targetHost,
                                getResult(AssessmentItems.NET_LISTEN_PORT, resultMap),
                                getResult(AssessmentItems.NET_TRAFFICS, resultMap)
                        )
                )
                .processes(getPsInfo(getResult(AssessmentItems.PROCESSES, resultMap), errorMap))
                .shadows(getPasswordUsers(getResult(AssessmentItems.SHADOWS, resultMap), errorMap))
                .locale(getLocale(getResult(AssessmentItems.LOCALE, resultMap), errorMap))
                .partitions(
                        getDf(
                                targetHost,
                                getResult(AssessmentItems.PARTITIONS, resultMap),
                                errorMap
                        )
                )
                .productName(getProductName())
                .productSerial(getProductSerial(getResult(AssessmentItems.PRODUCT_SERIAL, resultMap), errorMap))
                .routeTables(getRouteTable(getResult(AssessmentItems.ROUTE_TABLE, resultMap), errorMap))
                .timezone(getTimezone(getResult(AssessmentItems.TIMEZONE1, resultMap), errorMap))
                .memory(
                        getMemoryInfo(
                                targetHost,
                                getResult(AssessmentItems.ARCHITECTURE, resultMap),
                                errorMap
                        )
                )
                .ulimits(getUlimits(targetHost, errorMap))
                .users(getUsers(targetHost, getResult(AssessmentItems.USERS, resultMap), errorMap))
                .env(getEnv(getResult(AssessmentItems.ENV, resultMap), errorMap))
                .vgs(getLvmInfo(getResult(AssessmentItems.LVM_VGS, resultMap), errorMap))
                .cpu(getCpuInfo(
                                targetHost,
                                getResult(AssessmentItems.DISTRIBUTION, resultMap),
                                errorMap
                        )
                )
                .fsTabs(getFstabInfo(getResult(AssessmentItems.FSTAB, resultMap), errorMap))
                .uptime(getUptime(getResult(AssessmentItems.UPTIME, resultMap), errorMap))
                .systemVendor("HP")
                .thirdPartySolutions(getThirdPartySolutions(targetHost, resultMap, this.getClass().getName()))
                .errorMap(errorMap)
                .packages(getPackage(getResult(AssessmentItems.PACKAGES, resultMap)))
                .build();
    }

}
//end of HpuxServerAssessment.java
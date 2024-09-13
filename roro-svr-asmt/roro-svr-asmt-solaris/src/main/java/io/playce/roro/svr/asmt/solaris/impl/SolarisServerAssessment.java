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
package io.playce.roro.svr.asmt.solaris.impl;

import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.dto.thirdparty.ThirdPartyDiscoveryResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.ThirdPartySolutionUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.AbstractServerAssessment;
import io.playce.roro.svr.asmt.AssessmentItems;
import io.playce.roro.svr.asmt.dto.common.Package;
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
import io.playce.roro.svr.asmt.dto.linux.security.DefInfo;
import io.playce.roro.svr.asmt.dto.result.SolarisAssessmentResult;
import io.playce.roro.svr.asmt.dto.solaris.KernelParameter;
import io.playce.roro.svr.asmt.dto.user.Group;
import io.playce.roro.svr.asmt.dto.user.User;
import io.playce.roro.svr.asmt.linux.CommonCommand;
import io.playce.roro.svr.asmt.solaris.SolarisCommand;
import io.playce.roro.svr.asmt.util.ResultUtil;
import io.swagger.v3.core.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.MatchResult;
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
@Component("SUNOSAssessment")
@Slf4j
public class SolarisServerAssessment extends AbstractServerAssessment {

    @Override
    public Map<String, String> generateCommand() {
        Map<String, String> cmdMap = new HashMap<>();
        cmdMap.put(AssessmentItems.ARCHITECTURE.toString(), SolarisCommand.ARCHITECTURE);
        cmdMap.put(AssessmentItems.DISTRIBUTION.toString(), SolarisCommand.DISTRIBUTION_SOLARIS);
        cmdMap.put(AssessmentItems.CRONTAB1.toString(), SolarisCommand.CRONTAB);
        cmdMap.put(AssessmentItems.GROUPS.toString(), SolarisCommand.GROUPS);
        cmdMap.put(AssessmentItems.HOSTNAME.toString(), SolarisCommand.HOSTNAME);
        cmdMap.put(AssessmentItems.INTERFACES.toString(), SolarisCommand.INTERFACES);
        cmdMap.put(AssessmentItems.INTERFACES_DEFAULT_GATEWAY.toString(), SolarisCommand.INTERFACES_DEFAULT_GATEWAY);
        cmdMap.put(AssessmentItems.KERNEL.toString(), SolarisCommand.KERNEL);
        cmdMap.put(AssessmentItems.KERNEL_PARAM.toString(), SolarisCommand.KERNEL_PARAM);
        cmdMap.put(AssessmentItems.NET_LISTEN_PORT.toString(), SolarisCommand.NET_LISTEN_PORT);
        cmdMap.put(AssessmentItems.NET_TRAFFICS.toString(), SolarisCommand.NET_TRAFFICS);
        cmdMap.put(AssessmentItems.PARTITIONS.toString(), SolarisCommand.PARTITIONS);
        cmdMap.put(AssessmentItems.PARTITIONS_TYPE.toString(), SolarisCommand.PARTITIONS_TYPE);
        cmdMap.put(AssessmentItems.PRODUCT_SERIAL.toString(), SolarisCommand.PRODUCT_SERIAL);
        cmdMap.put(AssessmentItems.PRODUCT_NAME.toString(), SolarisCommand.PRODUCT_NAME);
        cmdMap.put(AssessmentItems.PROCESSES.toString(), SolarisCommand.PROCESSES);
        cmdMap.put(AssessmentItems.PROCESSOR_COUNT.toString(), SolarisCommand.PROCESSOR_COUNT);
        cmdMap.put(AssessmentItems.PROCESSOR_CORES.toString(), SolarisCommand.PROCESSOR_CORES);
        cmdMap.put(AssessmentItems.SHADOWS.toString(), SolarisCommand.SHADOW);
        cmdMap.put(AssessmentItems.TIMEZONE1.toString(), SolarisCommand.TIMEZONE_HIGHER_11);
        cmdMap.put(AssessmentItems.TIMEZONE2.toString(), SolarisCommand.TIMEZONE_BELOW_11);
        cmdMap.put(AssessmentItems.ROUTE_TABLE.toString(), SolarisCommand.ROUTE_TABLE);
        cmdMap.put(AssessmentItems.USERS.toString(), SolarisCommand.USERS);
        cmdMap.put(AssessmentItems.ENV.toString(), SolarisCommand.ENV);
        cmdMap.put(AssessmentItems.DNS.toString(), SolarisCommand.DNS);
        cmdMap.put(AssessmentItems.LOCALE.toString(), SolarisCommand.LOCALE);
        cmdMap.put(AssessmentItems.HOSTS.toString(), SolarisCommand.HOSTS);
        cmdMap.put(AssessmentItems.DAEMON_LIST.toString(), SolarisCommand.DAEMON_LIST);
        cmdMap.put(AssessmentItems.UPTIME.toString(), SolarisCommand.UPTIME);
        cmdMap.put(AssessmentItems.OS_FAMILY.toString(), SolarisCommand.OS_FAIMLY);
        cmdMap.put(AssessmentItems.PACKAGES.toString(), SolarisCommand.PACKAGES);
        return cmdMap;
    }

    public String getDistributionVersion(String result, Map<String, String> errorMap) throws InterruptedException {
        // String distributionVersion = null;
        // try {
        //     String data = result.split("\n")[0];
        //
        //     if (data.contains("Solaris")) {
        //
        //         if (data.contains("Oracle Solaris")) {
        //             data = data.replaceAll("Oracle ", "").strip();
        //         }
        //         distributionVersion = data.split("\\s+")[1];
        //     }
        // } catch (Exception e) {
        //     errorMap.put(AssessmentItems.DISTRIBUTION.name() + "_VERSION", e.getMessage());
        //     log.error("{}", e.getMessage(), e);
        // }
        // return distributionVersion;

        try {
            if (StringUtils.isNotEmpty(result)) {
                result = result.strip();
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DISTRIBUTION.name() + "_VERSION", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result;
    }

    public String getDistributionRelease(String result, Map<String, String> errorMap) throws InterruptedException {
        // String distributionRelease = null;
        // try {
        //     String data = result.split("\n")[0];
        //     String prefix = "";
        //     if (data.indexOf("Solaris") > -1) {
        //
        //         if (data.indexOf("Oracle Solaris") > -1) {
        //             data = data.replaceAll("Oracle ", "").strip();
        //             prefix = "Oracle ";
        //         }
        //         distributionRelease = prefix + data;
        //     }
        // } catch (Exception e) {
        //     errorMap.put(AssessmentItems.DISTRIBUTION.name() + "_RELEASE", e.getMessage());
        //     log.error("{}", e.getMessage(), e);
        // }
        // return distributionRelease;

        try {
            if (StringUtils.isNotEmpty(result)) {
                result = "SunOS " + result.strip();
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DISTRIBUTION.name() + "_RELEASE", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result;
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

    public CpuInfo getCpuInfo(TargetHost targetHost, Map<String, String> errorMap) throws InterruptedException {

        CpuInfo cpuInfo = new CpuInfo();
        try {

            Map<String, String> cmdMap = new HashMap<>();
            cmdMap.put(AssessmentItems.PROCESSOR.toString(), SolarisCommand.PROCESSOR);
            cmdMap.put(AssessmentItems.PROCESSOR_COUNT.toString(), SolarisCommand.PROCESSOR_COUNT);
            cmdMap.put(AssessmentItems.PROCESSOR_CORES.toString(), SolarisCommand.PROCESSOR_CORES);

            Map<String, RemoteExecResult> resultMap = runCommands(targetHost, cmdMap);
            String count = resultMap.get(AssessmentItems.PROCESSOR_COUNT.toString()).getResult();
            if (StringUtils.isNotEmpty(count)) {
                cpuInfo.setProcessorCount(count.strip());
            }
            String cpu = resultMap.get(AssessmentItems.PROCESSOR.toString()).getResult();
            if (StringUtils.isNotEmpty(cpu)) {
                String[] data = cpu.split("\\s+");

                cpuInfo.setProcessor(String.join(" ", Arrays.asList(data).subList(2, data.length)));
            }

            String cores = resultMap.get(AssessmentItems.PROCESSOR_CORES.toString()).getResult();
            if (StringUtils.isNotEmpty(cores)) {
                cpuInfo.setProcessorCores(String.valueOf(cores.strip()));
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.CPU_FACTS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return cpuInfo;
    }

    public Map<String, Map<String, String>> getUlimits(TargetHost targetHost, Map<String, String> errorMap) throws InterruptedException {
        Map<String, Map<String, String>> ulimits = new HashMap<>();
        String[] ignoreUsers = {"daemon", "bin", "sys", "adm", "uucp", "guest", "nobody",
                "lpd",
                "lp", "invscout", "snapp", "ipsec", "nuucp", "sshd", "ftp",
                "anonymou"};
        try {
            String userList = SSHUtil.executeCommand(targetHost, SolarisCommand.USER_LIST);

            Map<String, String> cmdMaps = new HashMap<>();
            if (StringUtils.isNotEmpty(userList)) {
                userList = userList.replaceAll("\\t", "");

                if ("root".equals(targetHost.getUsername()) || StringUtils.isNotEmpty(targetHost.getRootPassword()) || SSHUtil.isSudoer(targetHost)) {
                    List<String> users = ResultUtil.removeCommentLine(userList);
                    for (String user : users) {
                        if (Arrays.stream(ignoreUsers).noneMatch(u -> u.equals(user))) {
                            cmdMaps.put(user, String.format(SolarisCommand.ULIMIT, user));
                        }
                    }

                    Map<String, RemoteExecResult> resultMap = runCommands(targetHost, cmdMaps);

                    for (String key : resultMap.keySet()) {
                        String result = resultMap.get(key).getResult();
                        if (StringUtils.isNotEmpty(result)) {
                            Map<String, String> ulimit = new HashMap<>();
                            for (String limit : result.split("\n")) {
                                try {
                                    if (limit.contains("Oracle Corporation") || limit.contains("You have new mail"))
                                        continue;
                                    String item = limit.substring(0, limit.indexOf('(')).strip();
                                    String[] value = limit.substring(limit.indexOf('(')).split("\\s+");
                                    ulimit.put(item, value[value.length - 1]);
                                } catch (Exception e) {
                                    RoRoException.checkInterruptedException(e);
                                    log.error("get_ulimits parse error : {}", limit);
                                }
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

    public Map<String, Group> getGroups(String result, Map<String, String> errorMap) throws InterruptedException {

        Map<String, Group> groups = new HashMap<>();
        try {

            String[] exceptGroups = {
                    "root", "daemon", "bin", "sys", "adm", "uucp", "guest",
                    "nobody", "lpd",
                    "lp", "invscout", "snapp", "ipsec", "nuucp", "sshd", "ftp",
                    "anonymou"
            };

            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {

                    if (StringUtils.isNotEmpty(line)) {
                        // split 크기 지정
                        // ex> root:x:0:
                        String[] g_infos = line.split(":", 4);

                        if (!Arrays.stream(exceptGroups).noneMatch(u -> u.equals(g_infos[0]))) {
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
                String[] lines = ifResult.split("\n");
                for (String line : lines) {
                    if (StringUtils.isEmpty(line))
                        continue;

                    String[] words = line.trim().split("\\s+");
                    // iterInfo = initInterface(words);

                    Pattern p = Pattern.compile("^\\w*\\d*:");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        iterInfo = initInterface(words);
                        if (interfaces.get(iterInfo.getDevice()) != null) {
                            iterInfo = interfaces.get(iterInfo.getDevice());
                        }
                        interfaces.put(iterInfo.getDevice(), iterInfo);
                        getDefaultGateway(iterInfo, gwResult, errorMap);
                        getRxTxBytes(targetHost, iterInfo);
                        // getMaxAddress(iterInfo);
                        // getIfcfgScript(iterInfo);
//                    } else if (words[0].startsWith("options=")) {
//                        // parseOptionsLine(words, iterInfo);
//                    } else if (words[0].equals("nd6")) {
//                        // parseOptionsLine(words, iterInfo);
//                    } else if (words[0].equals("media")) {
//                        // parseOptionsLine(words, iterInfo);
//                    } else if (words[0].equals("lladdr")) {
//                        // parseOptionsLine(words, iterInfo);
//                    } else if (words[0].equals("status")) {
//                        // parseOptionsLine(words, iterInfo);
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
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.INTERFACES.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return interfaces;
    }

    private void parseOptionsLine(String[] words, InterfaceInfo iterInfo) {

    }

    public String getKernel(String result, Map<String, String> errorMap) throws InterruptedException {
        try {
            if (StringUtils.isNotEmpty(result)) {
                result = result.strip();
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.KERNEL.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return result;
    }


    public PortList getListenPort(TargetHost targetHost) throws InterruptedException {

        PortList portList = new PortList();

        List<ListenPort> listen = new ArrayList<>();
        EstablishedPort established = new EstablishedPort();
        WaitPort wait = new WaitPort();

        portList.setListen(listen);
        portList.setEstablished(established);
        portList.setWait(wait);

        String[] protocols = {"tcp", "udp"};
        try {
            Map<String, String> psMap = getProcMap(targetHost);

            Map<String, String> procInfo = new HashMap<>();
            for (String protocol : protocols) {
                String result = SSHUtil.executeCommand(targetHost, String.format(SolarisCommand.NET_LISTEN_PORT, protocol));

                if (StringUtils.isNotEmpty(result)) {
                    Map<String, ListenPort> tempMap = new HashMap<>();
                    for (String line : result.split("\n")) {
                        String[] data = line.strip().split("\\s+");
                        if (data.length < 2)
                            continue;

                        String[] source = getSplitAddress(data[0]);
                        String addr = source[0];
                        String port = source[1];

                        if (StringUtils.countMatches(addr, ":") < 4) {
                            ListenPort listenPort = new ListenPort();
                            listenPort.setProtocol(protocol);
                            listenPort.setBindAddr(addr);
                            listenPort.setPort(port);

                            tempMap.put(port, listenPort);

                            for (String pid : psMap.keySet()) {
                                procInfo.put(pid + Constants.COMMA + port, String.format(SolarisCommand.NET_PROC_INFO, pid, port));
                            }

                            Map<String, RemoteExecResult> resultMap = runCommands(targetHost, procInfo);
                            for (String key : resultMap.keySet()) {
                                if (key.contains(port)) {
                                    String validatePid = resultMap.get(key).getResult();

                                    if (StringUtils.isNotEmpty(validatePid)) {
                                        ListenPort obj = tempMap.getOrDefault(port, null);
                                        if (obj != null) {
                                            String pid = key.split(Constants.COMMA)[0];
                                            String pName = psMap.get(pid);
                                            obj.setPid(pid);
                                            obj.setName(pName);
                                            listen.add(obj);
                                        }
                                    }
                                }
                            }

                            procInfo.clear();
                        }
                    }
                }

                result = SSHUtil.executeCommand(targetHost, String.format(SolarisCommand.NET_TRAFFICS, protocol));

                if (StringUtils.isNotEmpty(result)) {

                    for (String line : result.split("\n")) {

                        String[] data = line.strip().split("\\s+");
                        if (data.length < 7)
                            continue;
                        if (CHECK_NETSTAT_STATUS.stream().noneMatch(i -> i.equalsIgnoreCase(data[6])))
                            continue;
                        if (line.contains("127.0.0.1"))
                            continue;

                        String[] source = getSplitAddress(data[0]);
                        String[] target = getSplitAddress(data[1]);
                        String conStatus = data[6].toLowerCase();
                        if (source.length < 2)
                            continue;

                        Optional<ListenPort> portInfo = listen.stream().filter(p -> p.getPort().equals(source[1])).findFirst();

                        Traffic traffics = new Traffic();
                        traffics.setProtocol(protocol);
                        traffics.setFaddr(target[0]);
                        traffics.setFport(target[1]);
                        traffics.setLaddr(source[0]);
                        traffics.setLport(source[1]);
                        traffics.setStatus(conStatus);

                        if (portInfo.isPresent()) {
                            traffics.setPid(portInfo.get().getPid());
                            traffics.setName(portInfo.get().getName());
                        }

                        if (conStatus.contains("wait")) {
                            if (portInfo.isPresent()) {
                                wait.getAnyToLocal().add(traffics);
                            } else {
                                wait.getLocalToAny().add(traffics);
                            }
                        } else {
                            if (portInfo.isPresent()) {
                                established.getAnyToLocal().add(traffics);
                            } else {
                                established.getLocalToAny().add(traffics);
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
        int index = addr.lastIndexOf(".");
        return new String[]{
                addr.substring(0, index),
                addr.substring(addr.lastIndexOf(".") + 1)
        };
    }

    public String[] getExtractProc(TargetHost targetHost, String port, Map<String, String> psMap) throws InterruptedException {

        String result = null;
        for (String key : psMap.keySet()) {
            result = SSHUtil.executeCommand(targetHost
                    , String.format(SolarisCommand.NET_PROC_INFO, key, port));

            if (StringUtils.isNotEmpty(result)) {
                return new String[]{key, psMap.get(key)};
            }
        }
        return new String[]{"", ""};
    }

    public Map<String, String> getProcMap(TargetHost targetHost) throws InterruptedException {
        Map<String, String> psMap = new HashMap<>();
        try {
            String result = SSHUtil.executeCommand(targetHost, SolarisCommand.NET_PORT_LIST);

            if (StringUtils.isNotEmpty(result)) {

                for (String ps : result.split("\n")) {
                    String[] data = ps.strip().split("\\s+");

                    psMap.put(data[0], data[1]);
                }

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
        }
        return psMap;
    }


    public Map<String, Partition> getDf(String partitionResult, String typeResult, Map<String, String> errorMap) throws InterruptedException {

        Map<String, Partition> partitions = new HashMap<>();
        try {
            String[] ignorePartitions = {"tmpfs"};

            if (StringUtils.isNotEmpty(partitionResult)) {

                // Get partition types
                Map<String, String> typesMap = getFsType(typeResult, errorMap);

                String[] pt_combine = {};
                for (String line : partitionResult.split("\n")) {
                    String[] temp = line.split("\\s+");

                    pt_combine = Stream.of(
                                    pt_combine, temp)
                            .flatMap(Stream::of)
                            .toArray(String[]::new);


                    if (pt_combine.length == 6) {
                        String[] copyPartitions = pt_combine;
                        boolean isMatch = Arrays.stream(ignorePartitions).allMatch(p -> p.equals(copyPartitions[0]));

                        if (!isMatch) {
                            Partition p = new Partition();
                            p.setDevice(pt_combine[0]);
                            p.setFsType(typesMap.get(pt_combine[5]));
                            p.setSize(String.valueOf(Integer.parseInt(pt_combine[1]) / 1024));
                            p.setFree(String.valueOf(Integer.parseInt(pt_combine[3]) / 1024));
                            p.setMountPath(pt_combine[5]);

                            partitions.put(pt_combine[5], p);
                        }

                        pt_combine = new String[]{};
                    } else {
                        continue;
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

    private Map<String, String> getFsType(String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, String> typesMap = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {
                    String[] data = line.split(":", 2);

                    typesMap.put(data[0].strip(), data[1].strip());
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.PARTITIONS_TYPE.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return typesMap;
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

                        String[] data = line.strip().split("\\s+");

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

    public String getProductName(String result, Map<String, String> errorMap) throws InterruptedException {
        String productName = null;
        try {
            if (StringUtils.isNotEmpty(result)) {
                productName = result.strip();
            } else {
                productName = "NA";
            }


        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.PRODUCT_NAME.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return productName;
    }


    public String getProductSerial(String result, Map<String, String> errorMap) throws InterruptedException {
        String productSerial = null;
        try {
            if (StringUtils.isNotEmpty(result)) {
                productSerial = result.strip();
            } else {
                productSerial = "NA";
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
                for (String line : result.split("\n")) {

                    String[] shadow_info = line.split(":");

                    if (!shadow_info[1].equals("*") && !shadow_info[1].equals("!!") &&
                            !shadow_info[1].equals("NP") && !shadow_info[1].equals("UP") && !shadow_info[1].equals("*LK*")) {
                        // shadows.add(new Shadow(shadow_info[0], shadow_info[1]));
                        shadow.put(shadow_info[0], shadow_info[1]);
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


    public MemoryInfo getMemoryInfo(TargetHost targetHost, Map<String, String> errorMap) throws InterruptedException {

        MemoryInfo memory = new MemoryInfo();

        try {

            int pageSize = getPageSize(targetHost);

            String totalMemoryPages = SSHUtil.executeCommand(targetHost, SolarisCommand.MEM_TOTAL);
            memory.setMemTotalMb(String.valueOf(Integer.valueOf(totalMemoryPages.strip()) * (long) pageSize / (1024 * 1024)));

            String freeMemoryPages = SSHUtil.executeCommand(targetHost, SolarisCommand.MEM_FREE);
            int totalFreeMemPages = 0;

            for (String line : freeMemoryPages.split("\n")) {
                String[] data = line.split("\\s+");
                totalFreeMemPages += Integer.valueOf(data[data.length - 3]);
            }
            memory.setMemFreeMb(String.valueOf(totalFreeMemPages * (long) pageSize / (1024 * 1024)));

            String swapInfo = SSHUtil.executeCommand(targetHost, SolarisCommand.SWAP_INFO);

            if (StringUtils.isNotEmpty(swapInfo)) {
                Pattern p = Pattern.compile("\\d+k");

                Matcher m = p.matcher(swapInfo);

                List<MatchResult> resultList = m.results().collect(Collectors.toList());

                String reservedSwap = resultList.get(2).group();
                String availableSwap = resultList.get(3).group();

                reservedSwap = reservedSwap.replace("k", "");
                availableSwap = availableSwap.replace("k", "");

                memory.setSwapTotalMb(String.valueOf(
                        (Integer.valueOf(reservedSwap) + Integer.valueOf(availableSwap)) / 1024
                ));

                memory.setSwapFreeMb(String.valueOf(
                        Integer.valueOf(availableSwap) / 1024
                ));

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.MEMORY_FACTS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return memory;
    }

    private int getPageSize(TargetHost targetHost) throws InterruptedException {
        String pageSize = SSHUtil.executeCommand(targetHost, SolarisCommand.PAGESIZE);
        return Integer.parseInt(pageSize.strip());
    }


    public String getTimezone(String timezone1, String timezone2, Map<String, String> errorMap) throws InterruptedException {
        try {
            if (StringUtils.isNotEmpty(timezone1)) {
                return timezone1.split("=")[1].strip();
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.TIMEZONE1.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        try {
            if (StringUtils.isNotEmpty(timezone2)) {
                return timezone2.split("=")[1].strip();
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.TIMEZONE2.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return null;
    }


    public List<RouteTable> getRouteTable(String result, Map<String, String> errorMap) throws InterruptedException {
        List<RouteTable> routeTables = new ArrayList<>();
        try {
            String[] ignoreStrings = {"Routing", "Destination", "-"};

            String[] lines = result.split("\n");
            for (String line : lines) {
                if (StringUtils.isEmpty(line))
                    continue;
                if (Arrays.stream(ignoreStrings).anyMatch(line::contains))
                    continue;

                String[] route_infos = line.split("\\s+");
                if (route_infos.length < 6)
                    continue;

                RouteTable routeTable = new RouteTable();
                routeTable.setDestination(route_infos[0]);
                routeTable.setGateway(route_infos[1]);
                routeTable.setIface(route_infos[5]);

                routeTables.add(routeTable);
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
                    "anonymou"};

            if (StringUtils.isNotEmpty(result)) {

                Map<String, String> profileCmd = new HashMap<>();
                Map<String, String> bashCmd = new HashMap<>();

                List<String> passwordList = ResultUtil.removeCommentLine(result);
                for (String line : passwordList) {

                    User user = new User();

                    String[] data = line.split(":", 7);
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

                        // env 결과에 "HZ="와 같이 value가 비어 있는 경우가 있음.
                        if (line.split("=").length > 1) {
                            value = line.split("=")[1].replaceAll("\"", "");
                        } else {
                            value = StringUtils.EMPTY;
                        }
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


    public List<FsTab> getFstabInfo(String result, Map<String, String> errorMap) throws InterruptedException {
        List<FsTab> fsTabs = new ArrayList<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                Pattern p = Pattern.compile("^\\#");

                for (String line : result.split("\n")) {

                    if (p.matcher(line).find() || line.equals("") || line.equals("\n"))
                        continue;

                    String[] data = line.split("\\s+");

                    FsTab fsTab = new FsTab();
                    fsTab.setDevice(data[0]);
                    fsTab.setMount(data[2]);
                    fsTab.setType(data[3]);
                    fsTab.setOption(data[6]);
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


    public List<String> getDns(String result, Map<String, String> errorMap) throws InterruptedException {
        List<String> dns = new ArrayList<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {

                    if (StringUtils.isNotEmpty(line)) {
                        String[] dns_infos = line.split("\\s+");

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
                    if (Pattern.matches("^$|^#", line))
                        continue;

                    if (line.indexOf("#") > -1) {
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
        defInfo.setUidMax("2147483647");
        defInfo.setGidMin("0");
        defInfo.setGidMax("2147483647");
        return defInfo;
    }

    public String getFamily() {
        return "Solaris";
    }

    private void parseEtherLine(String[] words, InterfaceInfo iterInfo) {
        iterInfo.setMacaddress(words[1]);
    }

    private void parseUnkownLine(String[] words, InterfaceInfo iterInfo) {
        return;
    }

    private void parseInet6line(String[] words, InterfaceInfo iterInfo) {
        String[] localhost6 = {"::1", "::1/128", "fe80::1%lo0"};

        String[] sAddrs = words[1].split("/");
        String sAddr = sAddrs[0];
        String sNetBits = sAddrs[1];

        Ipv6Address address = new Ipv6Address();
        address.setAddress(sAddr);
        address.setPrefix(sNetBits);
        if (words.length > 2) {
            address.setScope(words[3]);
        }

        iterInfo.getIpv6().add(address);
    }

    private void parseInetLine(String[] words, InterfaceInfo iterInfo) {
        String sAddr = words[1];
        String netmask = null;
        String broadcast = null;

        Pattern p = Pattern.compile("([0-9a-f]){8}");
        if (p.matcher(words[3]).find() && words[3].length() == 8) {
            words[3] = "0x" + words[3];
        } else {
            words[3] = "0x" + Integer.parseInt(words[3], 16);
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

    private void getDefaultGateway(InterfaceInfo iterInfo, String result, Map<String, String> errorMap) throws InterruptedException {
        try {
            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {
                    String[] data = line.split("\\s+");

                    if (data.length > 5 && data[0].equals("default")) {

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

    private void getRxTxBytes(TargetHost targetHost, InterfaceInfo iterInfo) throws InterruptedException {
        try {
            String result = SSHUtil.executeCommand(targetHost, String.format(CommonCommand.INTERFACES_RX_TX_SCRIPTS1, iterInfo.getDevice()));

            if (StringUtils.isNotEmpty(result) && result.contains(StringUtils.SPACE)) {
                Double rxBytes = Double.parseDouble(result.split(StringUtils.SPACE)[0]) * 1024;
                Double txBytes = Double.parseDouble(result.split(StringUtils.SPACE)[1]) * 1024;

                iterInfo.setRxBytes(rxBytes.longValue());
                iterInfo.setTxBytes(txBytes.longValue());
            } else {
                /** Solaris NOT working below command */
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

    private InterfaceInfo initInterface(String[] words) {
        InterfaceInfo info = new InterfaceInfo();
        // 디바이스 명의 콜론(:) 제거
        info.setDevice(words[0].replaceAll(":", StringUtils.EMPTY));
        info.setGateway("unknown");
        info.setScript("unknown");
        info.setMacaddress("unknown");

        return info;
    }

    private Map<String, Map<String, String>> getDaemons(String daemonList, Map<String, String> errorMap) throws InterruptedException {

        Map<String, Map<String, String>> daemons = new HashMap<>();

        try {
            if (StringUtils.isNotEmpty(daemonList)) {
                for (String daemon : daemonList.split("\n")) {
                    if (StringUtils.isNotEmpty(daemon)) {
                        Map<String, String> infoMap = new HashMap<>();
                        String[] value = daemon.trim().split("\\s+");
                        String[] name = value[2].split(":")[1].split("/");


                        infoMap.put("status", value[0]);
                        infoMap.put("description", "");
                        infoMap.put("type", value[2].split(":")[0]);

                        daemons.put(name[name.length - 1], infoMap);
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DAEMON_LIST.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return daemons;
    }


    public Map<String, Map<String, Map<String, KernelParameter>>> getKernelParams(String result, Map<String, String> errorMap) {
        Map<String, Map<String, Map<String, KernelParameter>>> kernelParamMap = new HashMap<>();

        if (StringUtils.isEmpty(result)) {
            return kernelParamMap;
        }
        String subject = null;
        String parameter = null;

        for (String line : result.split("\n")) {
            String[] data = line.strip().split("\\s+");

            if (data.length > 1) {
                Map<String, KernelParameter> parameterMap = kernelParamMap.get(subject).get(parameter);

                KernelParameter param = KernelParameter.builder()
                        .action(StringUtils.defaultString(data[1]))
                        .flag(data.length > 2 ? StringUtils.defaultString(data[2]) : StringUtils.EMPTY)
                        .recipent(data.length > 3 ? StringUtils.defaultString(data[3]) : StringUtils.EMPTY)
                        .value(data.length > 4 ? StringUtils.defaultString(data[4]) : StringUtils.EMPTY)
                        .build();
                parameterMap.put(data[0], param);
            } else {
                String[] newSubjectInfo = data[0].split("\\.");
                subject = newSubjectInfo[0];
                parameter = newSubjectInfo[1];

                if (!kernelParamMap.containsKey(subject)) {
                    kernelParamMap.put(subject, new HashMap<>());
                }
                Map<String, Map<String, KernelParameter>> subjectMap = kernelParamMap.get(subject);
                if (!subjectMap.containsKey(parameter)) {
                    subjectMap.put(parameter, new HashMap<>());
                }
            }
        }
        return kernelParamMap;
    }

    protected Long getUptime(String uptime, Map<String, String> errorMap) throws InterruptedException {
        // Long result = null;
        // try {
        //     if (StringUtils.isNotEmpty(uptime)) {
        //         uptime = uptime.replaceAll("00", "0");
        //         String[] times = uptime.split("\\s+");
        //
        //         int day;
        //         int hour;
        //         int min;
        //
        //         day = Integer.parseInt(times[0]);
        //         hour = Integer.parseInt(times[1]);
        //         if (times[2].equals("min")) {
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

    private List<ThirdPartyDiscoveryResult> getThirdPartySolutions(TargetHost targetHost, Map<String, RemoteExecResult> resultMap, String componentName) {
        List<ThirdPartyDiscoveryResult> thirdPartySolutions = null;

        try {
            List<Process> processList = getPsInfo(getResult(AssessmentItems.PROCESSES, resultMap), new HashMap<>());
            PortList ports = getListenPort(targetHost);
            Map<String, String> crontabMap = getCrontabs(targetHost, getResult(AssessmentItems.CRONTAB1, resultMap), new HashMap<>());
            Map<String, Map<String, String>> serviceMap = getDaemons(getResult(AssessmentItems.DAEMON_LIST, resultMap), new HashMap<>());

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
    protected List<Package> getPackage(String packages) {
        List<Package> result = new ArrayList<>();
        if (packages == null)
            return result;
        String[] strings = packages.split(StringUtils.LF);
        for (int i = 1; i < strings.length; i += 2) {
            Package aPackage = new Package();
            aPackage.setName(strings[i - 1]);
            aPackage.setVersion(strings[i]);
            result.add(aPackage);
        }
        return result;
    }

    @Override
    public SolarisAssessmentResult assessment(TargetHost targetHost) throws InterruptedException {
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
        return SolarisAssessmentResult.builder()
                .architecture(getArchitecture(getResult(AssessmentItems.ARCHITECTURE, resultMap), errorMap))
                .crontabs(
                        getCrontabs(
                                targetHost,
                                getResult(AssessmentItems.CRONTAB1, resultMap),
                                errorMap
                        )
                )
                .defInfo(getLoginDef())
                .interfaces(
                        getInterfacesInfo(
                                targetHost,
                                getResult(AssessmentItems.INTERFACES, resultMap),
                                getResult(AssessmentItems.INTERFACES_DEFAULT_GATEWAY, resultMap),
                                errorMap
                        )
                )
                .distributionVersion(getDistributionVersion(getResult(AssessmentItems.DISTRIBUTION, resultMap), errorMap))
                .distributionRelease(getDistributionRelease(getResult(AssessmentItems.DISTRIBUTION, resultMap), errorMap))
                .dns(getDns(getResult(AssessmentItems.DNS, resultMap), errorMap))
                .hostname(getHostname(getResult(AssessmentItems.HOSTNAME, resultMap), errorMap))
//                .family(getFamily())
                .family(getResult(AssessmentItems.OS_FAMILY, resultMap))
                .groups(getGroups(getResult(AssessmentItems.GROUPS, resultMap), errorMap))
                .hosts(getHosts(getResult(AssessmentItems.HOSTS, resultMap), errorMap))
                .kernel(getKernel(getResult(AssessmentItems.KERNEL, resultMap), errorMap))
                .kernelParameters(getKernelParams(getResult(AssessmentItems.KERNEL_PARAM, resultMap), errorMap))
                .portList(
                        getListenPort(
                                targetHost
                        )
                )
                .processes(getPsInfo(getResult(AssessmentItems.PROCESSES, resultMap), errorMap))
                .shadows(getPasswordUsers(getResult(AssessmentItems.SHADOWS, resultMap), errorMap))
                .locale(getLocale(getResult(AssessmentItems.LOCALE, resultMap), errorMap))
                .partitions(
                        getDf(
                                getResult(AssessmentItems.PARTITIONS, resultMap),
                                getResult(AssessmentItems.PARTITIONS_TYPE, resultMap),
                                errorMap
                        )
                )
                .productName(getProductName(getResult(AssessmentItems.PRODUCT_NAME, resultMap), errorMap))
                .productSerial(getProductSerial(getResult(AssessmentItems.PRODUCT_SERIAL, resultMap), errorMap))
                .routeTables(getRouteTable(getResult(AssessmentItems.ROUTE_TABLE, resultMap), errorMap))
                .timezone(
                        getTimezone(
                                getResult(AssessmentItems.TIMEZONE1, resultMap),
                                getResult(AssessmentItems.TIMEZONE2, resultMap),
                                errorMap
                        )
                )
                .memory(getMemoryInfo(targetHost, errorMap))
                .users(
                        getUsers(
                                targetHost,
                                getResult(AssessmentItems.USERS, resultMap),
                                errorMap
                        )
                )
                .ulimits(getUlimits(targetHost, errorMap))
                .env(getEnv(getResult(AssessmentItems.ENV, resultMap), errorMap))
                // .vgs(getLvmInfo())
                .cpu(getCpuInfo(targetHost, errorMap))
                .fsTabs(getFstabInfo(getResult(AssessmentItems.FSTAB, resultMap), errorMap))
                .daemons(getDaemons(getResult(AssessmentItems.DAEMON_LIST, resultMap), errorMap))
                .uptime(getUptime(getResult(AssessmentItems.UPTIME, resultMap), errorMap))
                .systemVendor("Oracle")
                .thirdPartySolutions(getThirdPartySolutions(targetHost, resultMap, this.getClass().getName()))
                .errorMap(errorMap)
                .packages(getPackage(getResult(AssessmentItems.PACKAGES, resultMap)))
                .build();
    }
}
//end of SolarisServerAssessment.java
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       11월 23, 2021            First Draft.
 */
package io.playce.roro.svr.asmt.linux;

import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.dto.publicagency.PublicAgencyReportDto;
import io.playce.roro.common.dto.thirdparty.ThirdPartyDiscoveryResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.SSHUtil2;
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
import io.playce.roro.svr.asmt.dto.linux.security.DefInfo;
import io.playce.roro.svr.asmt.dto.linux.security.Firewall;
import io.playce.roro.svr.asmt.dto.linux.security.Rule;
import io.playce.roro.svr.asmt.dto.user.Group;
import io.playce.roro.svr.asmt.dto.user.User;
import io.playce.roro.svr.asmt.util.DmiFactUtil;
import io.playce.roro.svr.asmt.util.NetworkParserUtil;
import io.playce.roro.svr.asmt.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.unit.DataSize;

import java.util.*;
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
@Slf4j
public abstract class LinuxServerAssessment extends AbstractServerAssessment {


    public Map<String, String> generateCommand() {
        Map<String, String> cmdMap = new HashMap<>();
        cmdMap.put(AssessmentItems.ARCHITECTURE.toString(), CommonCommand.ARCHITECTURE);
        cmdMap.put(AssessmentItems.CRONTAB1.toString(), CommonCommand.CRONTAB1);
        cmdMap.put(AssessmentItems.CRONTAB2.toString(), CommonCommand.CRONTAB2);
        cmdMap.put(AssessmentItems.FIREWALL_RULE.toString(), CommonCommand.FIREWALL_RULE);
        cmdMap.put(AssessmentItems.FIREWALL_EXTRA_RULE.toString(), CommonCommand.FIREWALL_EXTRA_RULE);
        cmdMap.put(AssessmentItems.GROUPS.toString(), CommonCommand.GROUPS);
        cmdMap.put(AssessmentItems.DNS.toString(), CommonCommand.DNS);
        cmdMap.put(AssessmentItems.HOSTNAME.toString(), CommonCommand.HOSTNAME);
        cmdMap.put(AssessmentItems.KERNEL.toString(), CommonCommand.KERNEL);
        cmdMap.put(AssessmentItems.KERNEL_PARAM.toString(), CommonCommand.KERNEL_PARAM);
        cmdMap.put(AssessmentItems.LOCALE.toString(), CommonCommand.LOCALE);
        cmdMap.put(AssessmentItems.LOGIN_DEF.toString(), CommonCommand.LOGIN_DEF);
        cmdMap.put(AssessmentItems.TIMEZONE1.toString(), CommonCommand.TIMEZONE1);
        cmdMap.put(AssessmentItems.TIMEZONE2.toString(), CommonCommand.TIMEZONE2);
        cmdMap.put(AssessmentItems.ROUTE_TABLE.toString(), CommonCommand.ROUTE_TABLE);
        cmdMap.put(AssessmentItems.USERS.toString(), CommonCommand.USERS);
        cmdMap.put(AssessmentItems.USER_LIST.toString(), CommonCommand.USER_LIST);
        cmdMap.put(AssessmentItems.ENV.toString(), CommonCommand.ENV);
        cmdMap.put(AssessmentItems.MEMORY_FACTS.toString(), CommonCommand.MEMORY_FACTS);
        cmdMap.put(AssessmentItems.SHADOWS.toString(), CommonCommand.SHADOW);
        cmdMap.put(AssessmentItems.NET_LISTEN_PORT.toString(), CommonCommand.NET_LISTEN_PORT);
        cmdMap.put(AssessmentItems.NET_TRAFFICS.toString(), CommonCommand.NET_TRAFFICS);
        cmdMap.put(AssessmentItems.PARTITIONS.toString(), CommonCommand.PARTITIONS);
        cmdMap.put(AssessmentItems.PROCESSES.toString(), CommonCommand.PROCESSES);
        cmdMap.put(AssessmentItems.CPU_FACTS.toString(), CommonCommand.CPU_FACTS);
        cmdMap.put(AssessmentItems.FSTAB.toString(), CommonCommand.FSTAB);
        cmdMap.put(AssessmentItems.HOSTS.toString(), CommonCommand.HOSTS);
        cmdMap.put(AssessmentItems.DAEMON_LIST.toString(), CommonCommand.DAEMON_LIST);
        cmdMap.put(AssessmentItems.DAEMON_LIST_LOWDER_7.toString(), CommonCommand.DAEMON_LIST_LOWDER_7);
        cmdMap.put(AssessmentItems.INTERFACES.toString(), CommonCommand.INTERFACES);
        cmdMap.put(AssessmentItems.INTERFACES_DEFAULT_GATEWAY.toString(), CommonCommand.INTERFACES_DEFAULT_GATEWAY);
        cmdMap.put(AssessmentItems.UPTIME.toString(), CommonCommand.UPTIME);
        return cmdMap;
    }

    public String getArchitecture(String result, Map<String, String> errorMap) throws InterruptedException {
        try {
            return result.replaceAll("\n", "");
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.ARCHITECTURE.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return null;
    }


    public Map<String, String> getCronTabs(TargetHost targetHost, String cron1, String cron2, Map<String, String> errorMap) throws InterruptedException {
        Map<String, String> cronTabMap = new HashMap<>();
        try {
            parseCronTab(targetHost, cron1, cronTabMap);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.CRONTAB1.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        try {
            parseCronTab(targetHost, cron2, cronTabMap);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.CRONTAB2.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return cronTabMap;
    }

    private void parseCronTab(TargetHost targetHost, String cron1, Map<String, String> cronTabMap) throws InterruptedException {
        if (StringUtils.isNotEmpty(cron1)) {
            for (String path : cron1.split("\n")) {
                String contents = SSHUtil.executeCommand(targetHost, "sudo cat " + path);
                cronTabMap.put(path, contents);
            }
        }
    }

    public Firewall getFirewall(String ruleString, String extraRuleString, Map<String, String> errorMap) throws InterruptedException {
        Map<String, List<Rule>> rules = parseRule(ruleString, errorMap);
        Map<String, List<Rule>> extraRules = parseRule(extraRuleString, errorMap);
        Firewall firewall = new Firewall();
        firewall.setRules(rules);
        firewall.setExtraRules(extraRules);
        return firewall;
    }

    private Map<String, List<Rule>> parseRule(String ruleString, Map<String, String> errorMap) throws InterruptedException {
        Map<String, List<Rule>> rules = new HashMap<>();
        try {
            String[] lines = ruleString.split(StringUtils.LF);
            String key = null;
            for (String line : lines) {
                line = StringUtils.trim(line);
                if (line.startsWith("target ") || line.equals(StringUtils.EMPTY)) continue;
                if (line.startsWith("Chain ")) {
                    key = line;
                    rules.put(key, new ArrayList<>());
                    continue;
                }

                if (key == null) continue;
                String[] cols = line.split(StringUtils.SPACE + "+");

                if (cols.length >= 5) {
                    int index = 0;
                    rules.get(key).add(Rule.builder().target(cols[index++]).prot(cols[index++]).opt(cols[index++]).source(cols[index++]).destination(cols[index]).build());
                }

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.FIREWALL_RULE.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return rules;
    }


    public Map<String, Group> getGroups(String result, Map<String, String> errorMap) throws InterruptedException {

        Map<String, Group> groups = new HashMap<>();
        try {
            for (String line : result.split("\n")) {

                // split 크기 지정
                // ex> root:x:0:
                String[] infos = line.split(":", 4);

                Group group = new Group();
                group.setGid(infos[2]);
                group.setUsers(Arrays.asList(infos[3].split(",")));

                groups.put(infos[0], group);
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.GROUPS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return groups;
    }


    public List<String> getDns(String result, Map<String, String> errorMap) throws InterruptedException {
        List<String> dns = new ArrayList<>();
        try {
            for (String line : result.split("\n")) {

                if (line.contains("generated by"))
                    continue;

                if (StringUtils.isNotEmpty(line)) {
                    String[] dns_infos = line.split(" ");

                    dns.addAll(Arrays.asList(dns_infos).subList(1, dns_infos.length));
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DNS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return dns;
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

    public String getKernel(String result, Map<String, String> errorMap) throws InterruptedException {
        try {
            result = result.replaceAll("\n", "");
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
                    line = line.replaceAll("\r", StringUtils.EMPTY);

                    String[] keyValues = line.split("=", 2);

                    // sysctl: reading key "net.ipv6.conf.lo.stable_secret" 와 같은 key, value 형태가 아닌 경우 skip
                    if (keyValues.length != 2) {
                        continue;
                    }

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


    public Hosts getHosts(String result, Map<String, String> errorMap) throws InterruptedException {

        Hosts hosts = new Hosts();
        try {
            if (StringUtils.isNotEmpty(result)) {
                hosts.setContents(result);

                Map<String, List<String>> mappings = new HashMap<>();

                for (String line : result.split("\n")) {
                    if (Pattern.matches("^$|^#", line))
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

    public DefInfo getLoginDef(String result, Map<String, String> errorMap) throws InterruptedException {
        DefInfo defInfo = new DefInfo();

        try {
            Pattern p = Pattern.compile("^#");
            for (String line : result.split("\n")) {
                line = line.replaceAll("\r", StringUtils.EMPTY);

                if (p.matcher(line).find())
                    continue;

                if (StringUtils.isNotEmpty(line)) {
                    String[] data = line.split("\\s+");

                    if (data == null || data.length == 0) {
                        continue;
                    }

                    switch (data[0]) {
                        case "UID_MIN":
                            defInfo.setUidMin(data[1]);
                            break;
                        case "UID_MAX":
                            defInfo.setUidMax(data[1]);
                            break;
                        case "GID_MIN":
                            defInfo.setGidMin(data[1]);
                            break;
                        case "GID_MAX":
                            defInfo.setGidMax(data[1]);
                            break;
                    }

                }

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.LOGIN_DEF.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return defInfo;
    }

    public String getTimezone(TargetHost targetHost, String timezone1, String timezone2, Map<String, String> errorMap) throws InterruptedException {
        try {
            if (StringUtils.isNotEmpty(timezone1)) {
                return timezone1.split(":")[1].strip().replaceAll("\n", "");
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.TIMEZONE1.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        try {
            if (StringUtils.isNotEmpty(timezone2)) {
                return timezone2.split("=")[1].strip().replaceAll("\n", "");
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
            for (String line : result.split("\n")) {

                String[] route_infos = line.split("\\s+");

                if (route_infos.length >= 8) {
                    RouteTable routeTable = new RouteTable();
                    routeTable.setDestination(route_infos[0]);
                    routeTable.setGateway(route_infos[1]);
                    routeTable.setIface(route_infos[7]);

                    routeTables.add(routeTable);
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.ROUTE_TABLE.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return routeTables;
    }


    public Map<String, User> getUsers(TargetHost targetHost, String result, Map<String, String> errorMap, boolean sudo) throws InterruptedException {
        Map<String, User> users = new HashMap<>();

        try {
            String[] ignoreUsers = {};

            Map<String, String> profileCmd = new HashMap<>();
            Map<String, String> bashCmd = new HashMap<>();
            if (StringUtils.isNotEmpty(result)) {

                List<String> passwordList = ResultUtil.removeCommentLine(result);
                for (String line : passwordList) {

                    User user = new User();

                    String[] data = line.split(":");
                    if (data.length < 7) {
                        log.debug("data.lenght: {} - {}", data.length, line);
                        continue;
                    }

                    if (!Arrays.stream(ignoreUsers).anyMatch(u -> u.equals(data[0]))) {
                        if (!line.endsWith("nologin")) {
                            profileCmd.put(data[0], "sh -c 'cat " + data[5] + "/.*profile'");
                            bashCmd.put(data[0], "sh -c 'cat " + data[5] + "/.*rc'");
                        }

                        user.setUid(data[2]);
                        user.setGid(data[3]);
                        user.setHomeDir(data[5]);
                        user.setShell(data[6]);
                        user.setProfile("");

                        users.put(data[0], user);
                    }

                }

                Map<String, RemoteExecResult> profileMap = SSHUtil2.runCommands(targetHost, profileCmd, sudo);
                Map<String, RemoteExecResult> bashMap = SSHUtil2.runCommands(targetHost, bashCmd, sudo);

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

    public Map<String, Map<String, String>> getUlimits(TargetHost targetHost, String userList, Map<String, String> errorMap) throws InterruptedException {
        Map<String, Map<String, String>> ulimits = new HashMap<>();

        try {
            if (StringUtils.isNotEmpty(userList)) {

                Map<String, String> cmdMaps = new HashMap<>();

                if ("root".equals(targetHost.getUsername()) || StringUtils.isNotEmpty(targetHost.getRootPassword()) || SSHUtil.isSudoer(targetHost)) {
                    List<String> users = ResultUtil.removeCommentLine(userList);
                    for (String user : users) {
                        cmdMaps.put(user, String.format(CommonCommand.ULIMIT, user));
                    }

                    Map<String, RemoteExecResult> resultMap = runCommands(targetHost, cmdMaps);

                    for (String key : resultMap.keySet()) {
                        String result = resultMap.get(key).getResult();
                        if (StringUtils.isNotEmpty(result)) {
                            Map<String, String> ulimit = new HashMap<>();
                            for (String limit : result.split("\n")) {
                                int idx = limit.indexOf('(');

                                if (idx > -1) {
                                    String item = limit.substring(0, idx).strip();
                                    String[] value = limit.substring(idx).split("\\s+");
                                    ulimit.put(item, value[value.length - 1]);
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

    public Map<String, String> getEnv(String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, String> envMap = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                String key = null;
                String value;

                for (String line : result.split("\n")) {

                    if (line.contains("=")) {
                        String[] prop = line.split("=", 2);
                        key = prop[0];
                        value = prop[1];
                        value = value != null ? value.replaceAll("\"", "") : value;
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

    public MemoryInfo getMemoryInfo(String result, Map<String, String> errorMap) throws InterruptedException {
        MemoryInfo memory = new MemoryInfo();
        try {
            for (String line : result.split("\n")) {
                String[] data = line.trim().split("\\s+");

                parseMemoryInfoDetail(memory, line, data);
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.MEMORY_FACTS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        return memory;
    }

    private void parseMemoryInfoDetail(MemoryInfo memory, String line, String[] data) {
        if (line.contains("total memory")) {
            memory.setMemTotalMb(String.valueOf(Integer.parseInt(data[0]) / 1024));
        } else if (line.contains("free memory")) {
            memory.setMemFreeMb(String.valueOf(Integer.parseInt(data[0]) / 1024));
        } else if (line.contains("total swap")) {
            memory.setSwapTotalMb(String.valueOf(Integer.parseInt(data[0]) / 1024));
        } else if (line.contains("free swap")) {
            memory.setSwapFreeMb(String.valueOf(Integer.parseInt(data[0]) / 1024));
        }
    }


    public Map<String, String> getPasswordUsers(String result, Map<String, String> errorMap) throws InterruptedException {
        Map<String, String> shadow = new HashMap<>();
        try {
            if (StringUtils.isNotEmpty(result)) {

                List<String> shadowList = ResultUtil.removeCommentLine(result);
                for (String line : shadowList) {
                    String[] shadow_info = line.split(":");
                    if (!shadow_info[1].equals("*") && !shadow_info[1].equals("!!")) {
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

    public PortList parseListenPort(TargetHost targetHost, String listenPort, String traffics, Map<String, String> errorMap) throws InterruptedException {
        PortList portList = new PortList();

        List<ListenPort> listen = new ArrayList<>();
        EstablishedPort established = new EstablishedPort();
        WaitPort wait = new WaitPort();

        portList.setListen(listen);
        portList.setEstablished(established);
        portList.setWait(wait);

        try {
            parseListenPort(listenPort, listen);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.NET_LISTEN_PORT.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }

        try {
            parseTraffics(traffics, listen.stream().map(ListenPort::getPort).collect(Collectors.toSet()), established, wait);
            // parseTraffics(traffics, listen, established, wait);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.NET_TRAFFICS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return portList;
    }

    private void parseListenPort(String result, List<ListenPort> listen) {
        if (StringUtils.isNotEmpty(result)) {
            for (String line : result.split("\n")) {
                String[] data = line.split("\\s+");

                if (data.length > 1) {
//                    if ("unix".equals(data[0]))
//                        continue;

//                    String[] sourceAddrPort = data[3].split(":", 2);
                    String[] sourceAddrPort = extractSource(data[3]);
                    String[] psInfo = extractProcessInfo(data[6]);

                    if (StringUtils.countMatches(sourceAddrPort[0], ":") < 4) {
                        ListenPort port = new ListenPort();
                        port.setProtocol(data[0]);
                        port.setBindAddr(sourceAddrPort[0]);
                        port.setPort(sourceAddrPort[1]);
                        port.setPid(psInfo[0]);
                        port.setName(psInfo[1]);
                        listen.add(port);
                    }
                }
            }
        }
    }

    private String[] extractSource(String data) {
        int index = data.lastIndexOf(":");
        if (index == -1) {
            return new String[]{"", ""};
        }
        return new String[]{data.substring(0, index), data.substring(index + 1)};
    }

    private String[] extractProcessInfo(String data) {
//        String[] psInfo = new String[2];
//        if (data.equals("-")) {
//            psInfo[0] = "";
//            psInfo[1] = "";
//        } else {
//            psInfo = data.split("/", 2);
//        }
//        return psInfo;
        String[] result = data.split("/");
        if (result.length != 2) {
            return new String[]{"", ""};
        }
        return result;
    }

    //    private void parseTraffics(String result, List<ListenPort> listen, EstablishedPort established, WaitPort wait) {
    private void parseTraffics(String result, Set<String> listen, EstablishedPort established, WaitPort wait) throws InterruptedException {
        String[] conStatusArr = {"established", "wait"};
        if (StringUtils.isNotEmpty(result)) {
            for (String line : result.split("\n")) {
                String[] data = line.split("\\s+");
                try {
                    if (data.length >= 7) {
                        String conStatus = extractConnStatus(data[5]);
                        if (Arrays.stream(conStatusArr).anyMatch(conStatus::contains)) {
                            String[] source = {
                                    data[3].substring(0, data[3].lastIndexOf(":")),
                                    data[3].substring(data[3].lastIndexOf(":") + 1)
                            };
                            //data[3].split(":", 2);
                            String[] target = {
                                    data[4].substring(0, data[4].lastIndexOf(":")),
                                    data[4].substring(data[4].lastIndexOf(":") + 1)
                            };
                            //data[4].split(":", 2);

                            if (source[0].equals("127.0.0.1") && target[0].equals("127.0.0.1"))
                                continue;
                            String[] psInfo = extractProcessInfo(data[6]);
//                            Optional<ListenPort> portInfo = getPortInfo(listen, source[1]);

                            Traffic traffic = new Traffic();
                            traffic.setProtocol(data[0]);
                            traffic.setFaddr(target[0]);
                            traffic.setFport(target[1]);
                            traffic.setLaddr(source[0]);
                            traffic.setLport(source[1]);
                            traffic.setPid(psInfo[0]);
                            traffic.setName(psInfo[1]);
                            traffic.setStatus(conStatus);

                            if (conStatus.contains("wait")) {
//                                if (portInfo.isPresent()) {
                                if (listen.contains(source[1])) {
                                    wait.getAnyToLocal().add(traffic);
                                } else {
                                    wait.getLocalToAny().add(traffic);
                                }
                            } else {
//                                if (portInfo.isPresent()) {
                                if (listen.contains(source[1])) {
                                    established.getAnyToLocal().add(traffic);
                                } else {
                                    established.getLocalToAny().add(traffic);
                                }
                            }
                        }
                    } else {
                        log.debug("ParseTraffics:traffic [{}] is ignore", Arrays.toString(data));
                    }
                } catch (Exception e) {
                    RoRoException.checkInterruptedException(e);
                    log.debug("Unhandled error occurred during parseTraffics {}", Arrays.toString(data));
                    log.error("{}", e.getMessage(), e);
                }
            }

        }
    }

    private String extractConnStatus(String datum) {
        String conStatus;
        try {
            conStatus = datum.toLowerCase();
        } catch (IndexOutOfBoundsException e) {
            conStatus = "";
        }
        return conStatus;
    }

    @NotNull
    private Optional<ListenPort> getPortInfo(List<ListenPort> listen, String anObject) {
        return listen.stream().filter(p -> p.getPort().equals(anObject)).findFirst();
    }


    public Map<String, Partition> getDf(String result, Map<String, String> errorMap) throws InterruptedException {

        Map<String, Partition> partitions = new HashMap<>();
        try {
            String[] ignorePartitions = {"tmpfs"};

            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {
                    String[] temp = line.split("\\s+");

                    if (temp.length == 7) {
                        boolean isMatch = Arrays.stream(ignorePartitions).allMatch(p -> p.equals(temp[0]));

                        if (!isMatch) {
                            Partition p = new Partition();
                            p.setDevice(temp[0]);
                            p.setFsType(temp[1]);
                            p.setSize(temp[2]);
                            p.setFree(temp[4]);
                            p.setMountPath(temp[6]);

                            partitions.put(temp[6], p);
                        }
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

    public List<Process> getPsInfo(String result, Map<String, String> errorMap) throws InterruptedException {
        List<Process> processes = new ArrayList<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {
                    try {
                        if (line.contains("<defunct>") || line.contains("UID")) {
                            continue;
                        }

                        String[] data = line.split("\\s+");

                        Process process = new Process();
                        process.setUser(data[0]);
                        process.setPid(data[1]);
                        if (Pattern.matches("(\\d+\\-)?(([0-9]+):)+([0-9]+)", data[7])) {
                            process.setName(data[8]);
                            process.setCmd(Arrays.asList(data).subList(8, data.length));
                        } else if (Pattern.matches("(\\d+\\-)?(([0-9]+):)+([0-9]+)", data[6])) {
                            if (Pattern.matches("\\[(.*?)]", data[7])) {
                                continue;
                            }
                            process.setName(data[7]);
                            process.setCmd(Arrays.asList(data).subList(7, data.length));
                        } else {
                            log.warn("TIME column does not exist in (\"{}\") at index 6 or 7.", line.trim());
                        }
                        processes.add(process);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        log.error("Unhandled exception occurred while parse process(\"{}\") and scan process will be continue.", line.trim(), e);
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            if (errorMap != null) {
                errorMap.put(AssessmentItems.PROCESSES.name(), e.getMessage());
            }
            log.error("Unhandled exception occurred while parse processes.", e);
        }
        return processes;
    }

    public CpuInfo getCpuInfo(TargetHost targetHost, String result, Map<String, String> errorMap) throws InterruptedException {

        CpuInfo cpuInfo = new CpuInfo();
        try {
            if (StringUtils.isNotEmpty(result)) {
                String[] lines = result.split("\n");

                for (String line : lines) {
                    String[] keyValues = line.split(":", 2);

                    switch (keyValues[0]) {
                        case "Core(s) per socket":
                            cpuInfo.setProcessorCores(keyValues[1].strip());
                            break;
                        case "Socket(s)":
                        case "CPU socket(s)":
                            cpuInfo.setProcessorCount(keyValues[1].strip());
                            break;
                        case "Model name":
                            cpuInfo.setProcessor(keyValues[1].strip());
                            break;
                    }
                }
            }

            if (StringUtils.isEmpty(cpuInfo.getProcessor())) {
                cpuInfo.setProcessor(SSHUtil.executeCommand(targetHost, "cat /proc/cpuinfo | grep \"model name\" | uniq | awk -F: '{print $2}'").trim());
            }

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.CPU_FACTS.name(), e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return cpuInfo;
    }

    public String getBiosVersion(TargetHost targetHost, Map<String, String> errorMap) throws InterruptedException {
        String biosVersion = null;
        try {
            biosVersion = DmiFactUtil.getAttributeByFile(targetHost, "bios_version");

            if (StringUtils.isEmpty(biosVersion)) {
                biosVersion = DmiFactUtil.getAttributeByDmidecode(targetHost, "bios-version");
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DMI_FACTS.name() + "_BIOS", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return biosVersion;
    }

    public String getSystemVendor(TargetHost targetHost, Map<String, String> errorMap) throws InterruptedException {
        String sysVendor = null;
        try {
            sysVendor = DmiFactUtil.getAttributeByFile(targetHost, "sys_vendor");
            if (StringUtils.isEmpty(sysVendor)) {
                sysVendor = DmiFactUtil.getAttributeByDmidecode(targetHost, "system-manufacturer");
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DMI_FACTS.name() + "_SYS_VENDOR", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return sysVendor;
    }

    public String getProductName(TargetHost targetHost, Map<String, String> errorMap) throws InterruptedException {
        String productName = null;
        try {
            productName = DmiFactUtil.getAttributeByFile(targetHost, "product_name");
            if (StringUtils.isEmpty(productName)) {
                productName = DmiFactUtil.getAttributeByDmidecode(targetHost, "system-product-name");
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DMI_FACTS.name() + "_PD_NAME", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return productName;
    }

    public String getProductSerial(TargetHost targetHost, Map<String, String> errorMap) throws InterruptedException {
        String productName = null;
        try {
            productName = DmiFactUtil.getAttributeByFile(targetHost, "product_serial");
            if (StringUtils.isEmpty(productName)) {
                productName = DmiFactUtil.getAttributeByDmidecode(targetHost, "system-serial-number");
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put(AssessmentItems.DMI_FACTS.name() + "_PD_SERIAL", e.getMessage());
            log.error("{}", e.getMessage(), e);
        }
        return productName;
    }

    public List<FsTab> getFstabInfo(String result, Map<String, String> errorMap) throws InterruptedException {
        List<FsTab> fsTabs = new ArrayList<>();
        try {
            if (StringUtils.isNotEmpty(result)) {
                Pattern p = Pattern.compile("^#");

                for (String line : result.split("\n")) {

                    if (p.matcher(line).find() || line.equals("") || line.equals("\n"))
                        continue;

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

    public Map<String, InterfaceInfo> getInterfacesInfo(TargetHost targetHost, String ifResult, String gwResult, Map<String, String> errorMap) throws InterruptedException {
        Map<String, InterfaceInfo> interfaces = new HashMap<>();
        try {

            // String result = SSHUtil.executeCommand(targetHost, CommonCommand.INTERFACES);
            // if (StringUtils.isEmpty(result)) {
            //     result = SSHUtil.executeCommand(targetHost, "/sbin/" + CommonCommand.INTERFACES);
            // }
            InterfaceInfo iterInfo = null;
            for (String line : ifResult.split("\n")) {

                if (StringUtils.isNotEmpty(line)) {

                    String[] words = line.trim().split("\\s+");

                    Pattern p = Pattern.compile("^\\d*:");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        iterInfo = initInterface(words);
                        interfaces.put(iterInfo.getDevice(), iterInfo);
                        parseDefaultGateway(gwResult, iterInfo, errorMap);
                        getIfcfgScript(targetHost, iterInfo);
                        getRxTxBytes(targetHost, iterInfo);
                    } else if (words[0].equals("link/ether")) {
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

    protected void parseEtherLine(String[] words, InterfaceInfo iterInfo) {
        iterInfo.setMacaddress(words[1]);
    }

    protected void parseUnkownLine(String[] words, InterfaceInfo iterInfo) {
        return;
    }

    protected void parseInet6line(String[] words, InterfaceInfo iterInfo) {
        String[] localhost6 = {"::1", "::1/128", "fe80::1%lo0"};

        String sAddr = words[1].split("/")[0];
        String sNetBits = words[1].split("/")[1];

        Ipv6Address address = new Ipv6Address();
        address.setAddress(sAddr);
        address.setPrefix(sNetBits);
        address.setScope(words[3]);

        iterInfo.getIpv6().add(address);
    }

    protected void parseInetLine(String[] words, InterfaceInfo iterInfo) {
        String ipNetmask = words[1];
        String ip = ipNetmask.split("/")[0];

        Ipv4Address address = NetworkParserUtil.getIpv4Address(ipNetmask, ip);

        iterInfo.getIpv4().add(address);
    }

    @NotNull
    private Ipv4Address getIpv4Address(String ipNetmask, String ip) {
        Ipv4Address address = new Ipv4Address();
        address.setAddress(ip);
        address.setNetmask(NetworkParserUtil.getNetMask(ipNetmask));
        address.setBroadcast(NetworkParserUtil.getBroadCase(ipNetmask));
        return address;
    }

    protected void getIfcfgScript(TargetHost targetHost, InterfaceInfo iterInfo) throws InterruptedException {
        try {
            // Centos 7.7
            String result = SSHUtil.executeCommand(targetHost, String.format(CommonCommand.INTERFACES_NETWORK_SCRIPTS, iterInfo.getDevice()));

            if (StringUtils.isNotEmpty(result)) {
                result = SSHUtil.executeCommand(targetHost, String.format("cat %s", result));
                iterInfo.setScript(result);
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
                result = SSHUtil.executeCommand(targetHost, String.format(CommonCommand.INTERFACES_RX_TX_SCRIPTS2, iterInfo.getDevice(), iterInfo.getDevice()));

                if (StringUtils.isNotEmpty(result)) {
                    result = result.replaceAll("(\r\n|\r|\n|\n\r)", StringUtils.SPACE);
                    String[] rxtxUptime = result.split(StringUtils.SPACE);

                    if (rxtxUptime.length == 3) {
                        long rxBytes = Long.parseLong(rxtxUptime[0]);
                        long txBytes = Long.parseLong(rxtxUptime[1]);
                        long uptime = Long.parseLong(rxtxUptime[2]);

                        rxBytes = rxBytes / uptime;
                        txBytes = txBytes / uptime;

                        iterInfo.setRxBytes(rxBytes);
                        iterInfo.setTxBytes(txBytes);
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
        }
    }

    protected void parseDefaultGateway(String result, InterfaceInfo iterInfo, Map<String, String> errorMap) throws InterruptedException {
        try {
            if (StringUtils.isNotEmpty(result)) {
                for (String line : result.split("\n")) {
                    String[] data = line.split("\\s+");

                    if (data.length > 1 && data[0].equals("default")) {

                        if (data[Arrays.asList(data).indexOf("dev") + 1].equals(iterInfo.getDevice())) {
                            iterInfo.setGateway(data[2]);
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

    protected InterfaceInfo initInterface(String[] words) {
        String device = words[1].substring(0, words[1].length() - 1);

        return NetworkParserUtil.getInitInterface(device, "unknown", "unknown", "unknown");
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

    protected Map<String, Map<String, String>> getDaemons(TargetHost targetHost, String daemonList, Map<String, String> errorMap) throws InterruptedException {
        Map<String, Map<String, String>> daemons = new HashMap<>();

        try {
            if (StringUtils.isNotEmpty(daemonList)) {
                for (String daemon : daemonList.split("\n")) {
                    if (StringUtils.isNotEmpty(daemon)) {
                        String[] value = daemon.trim().split("\\s+");

                        if (value.length >= 6) {
                            int offset = 0;
                            if (!Character.isAlphabetic(value[0].codePointAt(0))) {
                                // EX> ● vdo.service loaded failed failed  VDO volume services
                                offset += 1;
                            }
                            Map<String, String> infoMap = new HashMap<>();
                            infoMap.put("load", value[1 + offset]);
                            infoMap.put("active", value[2 + offset]);
                            infoMap.put("sub", value[3 + offset]);
                            infoMap.put("description", String.join(" ",
                                    Arrays.copyOfRange(value, 4 + offset, value.length)));

                            daemons.put(value[0 + offset], infoMap);
                        } else {
                            log.debug("GetDaemons:daemon [{}] is ignore because data format", Arrays.toString(value));
                        }
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

    protected List<ThirdPartyDiscoveryResult> getThirdPartySolutions(TargetHost targetHost, Map<String, RemoteExecResult> resultMap, String componentName) {
        List<ThirdPartyDiscoveryResult> thirdPartySolutions = null;

        try {
            List<Process> processList = getPsInfo(getResult(AssessmentItems.PROCESSES, resultMap), null);
            PortList ports = parseListenPort(targetHost, getResult(AssessmentItems.NET_LISTEN_PORT, resultMap), getResult(AssessmentItems.NET_TRAFFICS, resultMap), new HashMap<>());
            Map<String, String> crontabMap = getCronTabs(targetHost, getResult(AssessmentItems.CRONTAB1, resultMap), getResult(AssessmentItems.CRONTAB2, resultMap), new HashMap<>());
            Map<String, Map<String, String>> serviceMap = getDaemons(targetHost, getResult2(AssessmentItems.DAEMON_LIST, AssessmentItems.DAEMON_LIST_LOWDER_7, resultMap), new HashMap<>());

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

    protected PublicAgencyReportDto.ServerStatus getServerStatus(TargetHost targetHost) {
        PublicAgencyReportDto.ServerStatus serverStatus = new PublicAgencyReportDto.ServerStatus();

        try {
            // Set serverType
            String command = "sudo hostnamectl | grep Chassis | awk -F ':' '{print $2}'";
            String result = SSHUtil.executeCommand(targetHost, command).trim();

            if (StringUtils.isNotEmpty(result)) {
                if (result.toLowerCase().contains("vm")) {
                    result = "Virtual";
                } else {
                    result = "Physical";
                }
            } else {
                result = SSHUtil.executeCommand(targetHost, "systemd-detect-virt");
                if (StringUtils.isNotEmpty(result)) {
                    result = "Virtual";
                } else {
                    result = SSHUtil.executeCommand(targetHost, "dmesg | grep \"Hypervisor detected\"");
                    if (StringUtils.isNotEmpty(result) && result.contains("Hypervisor detected")) {
                        result = "Virtual";
                    } else {
                        result = "Physical";
                    }
                }
            }
            serverStatus.setServerType(result);

            // Set server manufacturer & model
            command = "sudo dmidecode | grep -A3 '^System Information' | grep 'Manufacturer' | awk -F ':' '{print $2}'";
            result = SSHUtil.executeCommand(targetHost, command).trim();
            if (StringUtils.isNotEmpty(result)) {
                serverStatus.setManufacturer(result);
            }

            command = "sudo dmidecode | grep -A3 '^System Information' | grep 'Product Name' | awk -F ':' '{print $2}'";
            result = SSHUtil.executeCommand(targetHost, command).trim();
            if (StringUtils.isNotEmpty(result)) {
                serverStatus.setModel(result);
            }

            // Set total local disk size(GB), disk count
            command = "sudo lsblk | grep disk | awk '{print $4}'";
            result = SSHUtil.executeCommand(targetHost, command).trim();
            if (StringUtils.isNotEmpty(result)) {
                List<String> lines = result.lines().collect(Collectors.toList());

                Long totalDiskSize = 0L;
                for (String size : lines) {
                    if (!size.endsWith("B")) {
                        size += "B";
                    }

                    DataSize dataSize = DataSize.parse(size);
                    totalDiskSize += dataSize.toBytes();
                }

                serverStatus.setDiskSize(totalDiskSize);
                serverStatus.setDiskCount(lines.size());
            }

            // Set total local disk used size(GB)
            command = "sudo lsblk | grep part | awk '{print $1}'";
            result = SSHUtil.executeCommand(targetHost, command).trim();

            if (StringUtils.isNotEmpty(result)) {
                result = result.replaceAll("├", StringUtils.EMPTY).replaceAll("└", StringUtils.EMPTY).replaceAll("─", StringUtils.EMPTY);

                List<String> lines = result.lines().collect(Collectors.toList());

                Long totalDiskUsed = 0L;
                for (String part : lines) {
                    command = "sudo df -B1 | grep " + part + " | awk '{print $3}'";
                    result = SSHUtil.executeCommand(targetHost, command).trim();

                    if (StringUtils.isNotEmpty(result)) {
                        totalDiskUsed += Long.parseLong(result);
                    }
                }

                serverStatus.setDiskUsed(totalDiskUsed);
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while collect server current status.", e);
        }

        return serverStatus;
    }

    protected List<PublicAgencyReportDto.StorageStatus> getStorageStatusList(TargetHost targetHost) {
        List<PublicAgencyReportDto.StorageStatus> storageStatusList = new ArrayList<>();
        PublicAgencyReportDto.StorageStatus storageStatus;

        try {
            // Disk 이름 조회
            String command = "sudo lsblk -o NAME,TYPE,ROTA | grep disk | awk '{print $1\" \"$3}'";
            String result = SSHUtil.executeCommand(targetHost, command).trim();

            if (StringUtils.isNotEmpty(result)) {
                List<String> disks = result.lines().collect(Collectors.toList());

                for (String disk : disks) {
                    storageStatus = new PublicAgencyReportDto.StorageStatus();

                    String[] diskInfo = disk.split(StringUtils.SPACE);

                    // disk vendor 조회
                    command = "sudo lsblk -o NAME,TYPE,VENDOR | grep " + diskInfo[0] + " | grep disk | awk '{$1=\"\"; $2=\"\"; print $0}'";
                    result = SSHUtil.executeCommand(targetHost, command).trim();
                    storageStatus.setManufacturer(result);

                    // disk model 조회
                    command = "sudo lsblk -o NAME,TYPE,MODEL | grep " + diskInfo[0] + " | grep disk | awk '{$1=\"\"; $2=\"\"; print $0}'";
                    result = SSHUtil.executeCommand(targetHost, command).trim();
                    storageStatus.setModel(result);

                    // disk type 조회
                    command = "sudo lsblk -o NAME,TYPE,TRAN | grep " + diskInfo[0] + " | grep disk | awk '{$1=\"\"; $2=\"\"; print $0}'";
                    result = SSHUtil.executeCommand(targetHost, command).trim();

                    if ("0".equals(diskInfo[1])) {
                        storageStatus.setDiskType("SSD");
                    } else {
                        if ("sata".equalsIgnoreCase(result)) {
                            storageStatus.setDiskType("HDD (SATA)");
                        } else if ("sas".equalsIgnoreCase(result)) {
                            storageStatus.setDiskType("HDD (SAS)");
                        } else {
                            storageStatus.setDiskType("HDD");
                        }
                    }

                    // disk connection type 조회
                    storageStatus.setConnectionType("Internal");
                    storageStatus.setSharingYn("N");

                    if ("fc".equalsIgnoreCase(result)) {
                        storageStatus.setConnectionType("SAN");
                    } else {
                        command = "sudo grep -vE '^#|^ *$' /etc/exports | awk '{print $1}'";
                        result = SSHUtil.executeCommand(targetHost, command).trim();

                        List<String> dirs = result.lines().collect(Collectors.toList());
                        for (String dir : dirs) {
                            command = "lsblk -o NAME,TYPE,MOUNTPOINT | grep part | egrep \"" + dir + "$\" | grep " + diskInfo[0] + " | awk '{print $1}'";
                            result = SSHUtil.executeCommand(targetHost, command).trim();
                            result = result.replaceAll("├", StringUtils.EMPTY).replaceAll("└", StringUtils.EMPTY).replaceAll("─", StringUtils.EMPTY);

                            if (result.startsWith(diskInfo[0])) {
                                storageStatus.setConnectionType("NAS");
                                storageStatus.setSharingYn("Y");
                                break;
                            }
                        }
                    }

                    storageStatusList.add(storageStatus);
                }
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while collect server storage status.", e);
        }

        return storageStatusList;
    }

    protected List<PublicAgencyReportDto.BackupStatus> getBackupStatusList(TargetHost targetHost) {
        List<PublicAgencyReportDto.BackupStatus> backupStatusList = new ArrayList<>();
        PublicAgencyReportDto.BackupStatus backupStatus;

        try {
            String command = "sudo cat /proc/scsi/scsi";
            String result = SSHUtil.executeCommand(targetHost, command).trim();

            Pattern pattern = Pattern.compile("\\s+Vendor:\\s+(\\S+.*?)\\s+Model:\\s+(\\S+.*?)\\s+Rev.*[\\n\\r].*Type:\\s+(\\S+.*?)\\s+ANSI");
            Matcher matcher = pattern.matcher(result);

            while (matcher.find()) {
                String vendor = matcher.group(1);
                String model = matcher.group(2);
                String type = matcher.group(3);

                if (type.toLowerCase().contains("sequential-access") || type.toLowerCase().contains("medium changer")) {
                    backupStatus = new PublicAgencyReportDto.BackupStatus();
                    backupStatus.setModel(vendor + StringUtils.SPACE + model);
                    backupStatusList.add(backupStatus);
                }
            }

        } catch (Exception e) {
            log.error("Unhandled exception occurred while collect server backup status.", e);
        }

        return backupStatusList;
    }
}
//end of AbstractServerAssessment.java
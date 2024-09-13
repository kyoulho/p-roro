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
 * Jeongho Baek   9월 11, 2021		First Draft.
 */
package io.playce.roro.asmt.windows.impl.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.asmt.windows.command.PowerShellVersion2UnderCommand;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.Process;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.*;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static io.playce.roro.asmt.windows.impl.factory.PowerShellParseUtil.splitToArrayByCrlf;
import static io.playce.roro.common.util.WinRmUtils.executePsShell;
import static io.playce.roro.common.util.WinRmUtils.executePsShellByOverThere;

/**
 * <pre>
 * PowerShell 2 버전 이하에서 공통으로 실행한다.
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Slf4j
public abstract class PowerShellVersion2UnderExecute implements PowerShellExecuteResult {

    protected static final String BACKWARD = "backward";

    @SuppressWarnings("DuplicatedCode")
    @Override
    public Environment getEnvironment(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        Environment environment = new Environment();

        try {
            String result = executePsShellByOverThere(targetHost, PowerShellVersion2UnderCommand.ENVIRONMENT);
            log.debug("\n" + PowerShellVersion2UnderCommand.ENVIRONMENT);

            environment.setAllUsersProfile(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ALLUSERSPROFILE"));
            environment.setAppdata(PowerShellParseUtil.getPropertyValueForMultiLine(result, "APPDATA"));
            environment.setCommonProgramFiles(PowerShellParseUtil.getPropertyValueForMultiLine(result, "CommonProgramFiles"));
            environment.setCommonProgramFilesx86(PowerShellParseUtil.getPropertyValueForMultiLine(result, "CommonProgramFiles(x86)"));
            environment.setCommonProgramW6432(PowerShellParseUtil.getPropertyValueForMultiLine(result, "CommonProgramW6432"));
            environment.setComputerName(PowerShellParseUtil.getPropertyValueForMultiLine(result, "COMPUTERNAME"));
            environment.setComSpec(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ComSpec"));
            environment.setLocalAppData(PowerShellParseUtil.getPropertyValueForMultiLine(result, "LOCALAPPDATA"));
            environment.setMsmpiBin(PowerShellParseUtil.getPropertyValueForMultiLine(result, "MSMPI_BIN"));
            environment.setFpNoHostCheck(PowerShellParseUtil.getPropertyValueForMultiLine(result, "FP_NO_HOST_CHECK"));
            environment.setHomeDrive(PowerShellParseUtil.getPropertyValueForMultiLine(result, "HOMEDRIVE"));
            environment.setHomePath(PowerShellParseUtil.getPropertyValueForMultiLine(result, "HOMEPATH"));
            environment.setLogonServer(PowerShellParseUtil.getPropertyValueForMultiLine(result, "LOGONSERVER"));
            environment.setNumberOfProcessors(PowerShellParseUtil.getPropertyValueForMultiLine(result, "NUMBER_OF_PROCESSORS"));
            environment.setOs(PowerShellParseUtil.getPropertyValueForMultiLine(result, "OS"));
            environment.setPath(PowerShellParseUtil.getPropertyValueForMultiLine(result, "Path"));
            environment.setPathExt(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PATHEXT"));
            environment.setProcessorArchitecture(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PROCESSOR_ARCHITECTURE"));
            environment.setProcessorIdentifier(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PROCESSOR_IDENTIFIER"));
            environment.setProcessorLevel(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PROCESSOR_LEVEL"));
            environment.setProcessorRevision(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PROCESSOR_REVISION"));
            environment.setProgramData(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ProgramData"));
            environment.setProgramFiles(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ProgramFiles"));
            environment.setProgramFilesx86(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ProgramFiles(x86)"));
            environment.setProgramW6432(PowerShellParseUtil.getPropertyValueForMultiLine(result, "ProgramW6432"));
            environment.setPrompt(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PROMPT"));
            environment.setPsModulePath(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PSModulePath"));
            environment.setPUBLIC(PowerShellParseUtil.getPropertyValueForMultiLine(result, "PUBLIC"));
            environment.setSystemDrive(PowerShellParseUtil.getPropertyValueForMultiLine(result, "SystemDrive"));
            environment.setSystemRoot(PowerShellParseUtil.getPropertyValueForMultiLine(result, "SystemRoot"));
            environment.setTemp(PowerShellParseUtil.getPropertyValueForMultiLine(result, "TEMP"));
            environment.setTmp(PowerShellParseUtil.getPropertyValueForMultiLine(result, "TMP"));
            environment.setUserDomain(PowerShellParseUtil.getPropertyValueForMultiLine(result, "USERDOMAIN"));
            environment.setUserDomainRoamingProfile(PowerShellParseUtil.getPropertyValueForMultiLine(result, "USERDOMAIN_ROAMINGPROFILE"));
            environment.setUsername(PowerShellParseUtil.getPropertyValueForMultiLine(result, "USERNAME"));
            environment.setUserprofile(PowerShellParseUtil.getPropertyValueForMultiLine(result, "USERPROFILE"));
            environment.setWindir(PowerShellParseUtil.getPropertyValueForMultiLine(result, "windir"));

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Environment", e.getMessage());
            return new Environment();
        }

        return environment;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public Cpu getCpu(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2UnderCommand.CPU);
            log.debug("\n" + PowerShellVersion2UnderCommand.CPU);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            List<Cpu> cpus = new ArrayList<>();
            Cpu tempCpu = new Cpu();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    // 5라인 마다 하나로 묶는다.
                    if (temp.startsWith("Name")) {
                        tempCpu.setName(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Name"));
                    } else if (temp.startsWith("Caption")) {
                        tempCpu.setCaption(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Caption"));
                    } else if (temp.startsWith("NumberOfCores")) {
                        tempCpu.setCores(PowerShellParseUtil.getPropertyValueForOneLine(temp, "NumberOfCores"));
                    } else if (temp.startsWith("NumberOfLogicalProcessors")) {
                        tempCpu.setLogicalProcessors(PowerShellParseUtil.getPropertyValueForOneLine(temp, "NumberOfLogicalProcessors"));
                    } else if (temp.startsWith("MaxClockSpeed")) {
                        tempCpu.setMaxClockSpeed(PowerShellParseUtil.getPropertyValueForOneLine(temp, "MaxClockSpeed"));
                    }
                    initCount++;

                    if (initCount == 5) {
                        cpus.add(tempCpu); // List에 등록
                        tempCpu = new Cpu(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }

                }
            }

            Cpu cpu;

            if (cpus.size() >= 2) {
                cpu = cpus.get(0);
                cpu.setSockets(cpus.size() + "");
            } else {
                cpu = cpus.get(0);
                cpu.setSockets("1");
            }

            return cpu;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("CPU", e.getMessage());
            return new Cpu();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<Network> getNetworks(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2UnderCommand.NETWORK);
            log.debug("\n" + PowerShellVersion2UnderCommand.NETWORK);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            List<Network> networks = new ArrayList<>();
            Network network = new Network();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    // 6라인 마다 하나로 묶는다.
                    if (temp.startsWith("InterfaceIndex")) {
                        network.setInterfaceIndex(PowerShellParseUtil.getPropertyValueForOneLine(temp, "InterfaceIndex"));
                    } else if (temp.startsWith("Description")) {
                        network.setInterfaceDescription(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Description"));
                    } else if (temp.startsWith("IPAddress")) {
                        String ipaddress = PowerShellParseUtil.getPropertyValueForOneLine(temp, "IPAddress");
                        ipaddress = ipaddress.replaceAll("\\{", "").replaceAll("}", "");
                        List<String> ip4Address = new ArrayList<>();
                        List<String> ip6Address = new ArrayList<>();

                        StringTokenizer stk = new StringTokenizer(ipaddress, ",");
                        while (stk.hasMoreTokens()) {
                            String ipAddress = stk.nextToken().trim();
                            if (PowerShellParseUtil.isValidIp4Address(ipAddress)) {
                                ip4Address.add(ipAddress);
                            } else {
                                ip6Address.add(ipAddress);
                            }
                        }

                        network.setIPv4Address(ip4Address);
                        network.setIPv6Address(ip6Address);
                    } else if (temp.startsWith("DefaultIPGateway")) {
                        String iPv4DefaultGateway = PowerShellParseUtil.getPropertyValueForOneLine(temp, "DefaultIPGateway");
                        iPv4DefaultGateway = iPv4DefaultGateway.replaceAll("\\{", "").replaceAll("}", "");
                        network.setIPv4DefaultGateway(Arrays.asList(iPv4DefaultGateway.split(",", -1)));
                    } else if (temp.startsWith("MACAddress")) {
                        String macAddress = PowerShellParseUtil.getPropertyValueForOneLine(temp, "MACAddress");
                        macAddress = macAddress.replaceAll("\\{", "").replaceAll("}", "");
                        network.setMacAddress(Arrays.asList(macAddress.split(",", -1)));
                    } else if (temp.startsWith("IPEnabled")) {
                        network.setStatus(PowerShellParseUtil.getPropertyValueForOneLine(temp, "IPEnabled"));
                    }

                    network.setIPv6DefaultGateway(new ArrayList<>());

                    initCount++;
                    if (initCount == 6) {
                        networks.add(network); // List에 등록
                        network = new Network(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }

                }
            }

            return networks;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Network", e.getMessage());
            return new ArrayList<>();
        }

    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<Dns> getDns(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2UnderCommand.DNS);
            log.debug("\n" + PowerShellVersion2UnderCommand.DNS);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            List<Dns> dnsList = new ArrayList<>();
            Dns dns = new Dns();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    if (temp.startsWith("InterfaceIndex")) {
                        dns.setInterfaceIndex(PowerShellParseUtil.getPropertyValueForOneLine(temp, "InterfaceIndex"));
                    } else if (temp.startsWith("Description")) {
                        dns.setInterfaceAlias(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Description"));
                    } else if (temp.startsWith("DNSServerSearchOrder")) {
                        String dnsServerSearchOrder = PowerShellParseUtil.getPropertyValueForOneLine(temp, "DNSServerSearchOrder");
                        dnsServerSearchOrder = dnsServerSearchOrder.replaceAll("\\{", "").replaceAll("}", "");
                        dns.setServerAddresses(dnsServerSearchOrder.split(",", -1));
                    }

                    dns.setAddressFamily("IPv4");

                    initCount++;
                    if (initCount == 3) {
                        dnsList.add(dns); // List에 등록
                        dns = new Dns(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }
                }
            }

            return dnsList;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("DNS", e.getMessage());
            return new ArrayList<>();
        }

    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<Route> getRoutes(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2UnderCommand.ROUTE);
            log.debug("\n" + PowerShellVersion2UnderCommand.ROUTE);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            List<Route> routes = new ArrayList<>();
            Route route = new Route();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    if (temp.startsWith("Destination")) {
                        route.setDestinationPrefix(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Destination"));
                    } else if (temp.startsWith("NextHop")) {
                        route.setNextHop(PowerShellParseUtil.getPropertyValueForOneLine(temp, "NextHop"));
                    } else if (temp.startsWith("Metric1")) {
                        route.setRouteMetric(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Metric1"));
                    } else if (temp.startsWith("InterfaceIndex")) {
                        route.setIfIndex(PowerShellParseUtil.getPropertyValueForOneLine(temp, "InterfaceIndex"));
                    }

                    route.setAddressFamily("IPv4");

                    initCount++;
                    if (initCount == 4) {
                        routes.add(route); // List에 등록
                        route = new Route(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }
                }
            }

            return routes;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Route", e.getMessage());
            return new ArrayList<>();
        }

    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<Port> getPorts(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShellByOverThere(targetHost, PowerShellVersion2UnderCommand.PORT);

            log.debug("\n" + PowerShellVersion2UnderCommand.PORT);

            String[] stringArrays = splitToArrayByCrlf(result);

            List<Port> ports = new ArrayList<>();
            List<String> pids = new ArrayList<>();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    String[] portArray = temp.trim().split("\\s+");

                    Port port = new Port();
                    port.setProtocol(portArray[0]);
                    port.setLocalAddress(portArray[1]);
                    port.setRemoteAddress(portArray[2]);
                    port.setState(portArray[3]);

                    try {
                        port.setPid(portArray[4]);
                    } catch (IndexOutOfBoundsException ignore) {
                        port.setState("");
                        port.setPid(portArray[3]);
                    }

                    pids.add(port.getPid());
                    ports.add(port);
                }
            }

            // Process Name을 구한다.
            Map<String, String> processMap = new HashMap<>();
            final String processNameCommand = "Get-Process -id " + String.join(", ", pids) + " | Select Id, ProcessName | ft -hide";
            String processNameResult = executePsShell(targetHost, processNameCommand);
            String[] processArrays = splitToArrayByCrlf(processNameResult);
            for (String temp : processArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    String[] portArray = temp.trim().split("\\s+");
                    processMap.put(portArray[0], portArray[1]);
                }
            }

            // 다시 Port 목록에서 ProcessName을 넣는다.
            for (Port tempPort : ports) {
                tempPort.setProcessName(processMap.get(tempPort.getPid()));
            }

            return ports;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Port", e.getMessage());
            return new ArrayList<>();
        }

    }

    public Hosts getHosts(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2UnderCommand.HOSTS);
            log.debug("\n" + PowerShellVersion2UnderCommand.HOSTS);

            List<String> hostContents = new ArrayList<>();

            String[] stringArrays = splitToArrayByCrlf(result);
            for (String temp : stringArrays) {
                if (!(temp.trim().startsWith("#") || temp.trim().equals(StringUtils.EMPTY))) {
                    hostContents.add(temp.trim().replaceAll("\\s+", StringUtils.SPACE));
                }
            }

            Hosts hosts = new Hosts();

            Map<String, List<String>> hostMap = new HashMap<>();
            for (String hostContent : hostContents) {
                if (hostContent.contains(StringUtils.SPACE)) {
                    String[] tempString = hostContent.split(StringUtils.SPACE, 2);
                    hostMap.put(tempString[0], new ArrayList<>(Arrays.asList(tempString[1].split(StringUtils.SPACE))));
                }
            }

            hosts.setContents(result);
            hosts.setMappings(hostMap);

            return hosts;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Firewall", e.getMessage());
            return new Hosts();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<Firewall> getFirewalls(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShellByOverThere(targetHost, PowerShellVersion2UnderCommand.FIREWALL);

            log.debug("\n" + PowerShellVersion2UnderCommand.FIREWALL);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            List<Firewall> firewalls = new ArrayList<>();
            Firewall firewall = new Firewall();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim()) &&
                        (!temp.startsWith("-") && !temp.startsWith(" ") && !temp.startsWith("Ok."))) {
                    if (temp.startsWith("Rule Name")) {
                        firewall.setName(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Rule Name"));
                    } else if (temp.startsWith("Protocol")) {
                        firewall.setProtocol(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Protocol"));
                    } else if (temp.startsWith("LocalPort")) {
                        firewall.setLocalPort(Collections.singletonList(PowerShellParseUtil.getPropertyValueForOneLine(temp, "LocalPort")));
                    } else if (temp.startsWith("RemotePort")) {
                        firewall.setRemotePort(Collections.singletonList(PowerShellParseUtil.getPropertyValueForOneLine(temp, "RemotePort")));
                    } else if (temp.startsWith("RemoteIP")) {
                        firewall.setRemoteAddress(Collections.singletonList(PowerShellParseUtil.getPropertyValueForOneLine(temp, "RemoteIP")));
                    } else if (temp.startsWith("Enabled")) {
                        firewall.setEnabled(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Enabled"));
                    } else if (temp.startsWith("Direction")) {
                        firewall.setDirection(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Direction"));
                    } else if (temp.startsWith("Action")) {
                        firewall.setAction(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Action"));
                    }

                    initCount++;
                    if (initCount == 12) {
                        firewalls.add(firewall); // List에 등록
                        firewall = new Firewall(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }
                }
            }

            return firewalls;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Firewall", e.getMessage());
            return new ArrayList<>();
        }

    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<Disk> getDisks(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Disk> disks = new ArrayList<>();

        //Local Disk Drive
        try {
            String result = executePsShellByOverThere(targetHost, PowerShellVersion2UnderCommand.DISK);

            log.debug("\n" + PowerShellVersion2UnderCommand.DISK);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            Disk disk = new Disk();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    if (temp.startsWith("DiskSerialNumber")) {
                        disk.setDiskSerialNumber(PowerShellParseUtil.getPropertyValueForOneLine(temp, "DiskSerialNumber"));
                    } else if (temp.startsWith("DiskMediaType")) {
                        disk.setDiskMediaType(PowerShellParseUtil.getPropertyValueForOneLine(temp, "DiskMediaType"));
                    } else if (temp.startsWith("PartitionName")) {
                        disk.setPartitionName(PowerShellParseUtil.getPropertyValueForOneLine(temp, "PartitionName"));
                    } else if (temp.startsWith("DiskStatus")) {
                        disk.setDiskStatus(PowerShellParseUtil.getPropertyValueForOneLine(temp, "DiskStatus"));
                    } else if (temp.startsWith("DriveLetter")) {
                        disk.setDriveLetter(PowerShellParseUtil.getPropertyValueForOneLine(temp, "DriveLetter"));
                    } else if (temp.startsWith("PartitionDiskIndex")) {
                        disk.setPartitionDiskIndex(PowerShellParseUtil.getPropertyValueForOneLine(temp, "PartitionDiskIndex"));
                    } else if (temp.startsWith("FreeSpace")) {
                        String freeSpaceStr = PowerShellParseUtil.getPropertyValueForOneLine(temp, "FreeSpace");

                        if (StringUtils.isEmpty(freeSpaceStr)) {
                            disk.setFreeSpace("0");
                        } else {
                            Double freeSpace = (Double.parseDouble(freeSpaceStr) / (1024 * 1024 * 1024));
                            disk.setFreeSpace(String.format("%.2f", freeSpace) + " GB");
                        }
                    } else if (temp.startsWith("TotalSize")) {
                        String totalSizeStr = PowerShellParseUtil.getPropertyValueForOneLine(temp, "TotalSize");

                        if (StringUtils.isEmpty(totalSizeStr)) {
                            disk.setTotalSize("0");
                        } else {
                            Double totalSize = (Double.parseDouble(totalSizeStr) / (1024 * 1024 * 1024));
                            disk.setTotalSize(String.format("%.2f", totalSize) + " GB");
                        }
                    } else if (temp.startsWith("DiskSystemName")) {
                        disk.setDiskSystemName(PowerShellParseUtil.getPropertyValueForOneLine(temp, "DiskSystemName"));
                    } else if (temp.startsWith("DiskVolumeName")) {
                        disk.setDiskVolumeName(PowerShellParseUtil.getPropertyValueForOneLine(temp, "DiskVolumeName"));
                    } else if (temp.startsWith("DiskModel")) {
                        disk.setDiskModel(PowerShellParseUtil.getPropertyValueForOneLine(temp, "DiskModel"));
                    } else if (temp.startsWith("DiskDeviceID")) {
                        disk.setDiskDeviceId(PowerShellParseUtil.getPropertyValueForOneLine(temp, "DiskDeviceID"));
                    } else if (temp.startsWith("PartitionType")) {
                        disk.setPartitionType(PowerShellParseUtil.getPropertyValueForOneLine(temp, "PartitionType"));
                    } else if (temp.startsWith("FileSystem")) {
                        disk.setFileSystem(PowerShellParseUtil.getPropertyValueForOneLine(temp, "FileSystem"));
                    }

                    initCount++;
                    if (initCount == 14) {
                        disks.add(disk); // List에 등록
                        disk = new Disk(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }

                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Local Disk", e.getMessage());
            throw new RoRoException(e);
        }


        try {
            //NetWork Disk Drive
            String result = executePsShellByOverThere(targetHost, PowerShellVersion2UnderCommand.NETWORK_DISK);
            log.debug("\n" + PowerShellVersion2UnderCommand.NETWORK_DISK);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            Disk disk = new Disk();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    if (temp.startsWith("DeviceId")) {
                        disk.setDriveLetter(PowerShellParseUtil.getPropertyValueForOneLine(temp, "DeviceId"));
                    } else if (temp.startsWith("FreeSpace")) {
                        String freeSpaceStr = PowerShellParseUtil.getPropertyValueForOneLine(temp, "FreeSpace");

                        if (StringUtils.isEmpty(freeSpaceStr)) {
                            disk.setFreeSpace("0");
                        } else {
                            Double freeSpace = (Double.parseDouble(freeSpaceStr) / (1024 * 1024 * 1024));
                            disk.setFreeSpace(String.format("%.2f", freeSpace) + " GB");
                        }
                    } else if (temp.startsWith("Size")) {
                        String totalSizeStr = PowerShellParseUtil.getPropertyValueForOneLine(temp, "Size");

                        if (StringUtils.isEmpty(totalSizeStr)) {
                            disk.setTotalSize("0");
                        } else {
                            Double totalSize = (Double.parseDouble(totalSizeStr) / (1024 * 1024 * 1024));
                            disk.setTotalSize(String.format("%.2f", totalSize) + " GB");
                        }
                    } else if (temp.startsWith("SystemName")) {
                        disk.setDiskSystemName(PowerShellParseUtil.getPropertyValueForOneLine(temp, "SystemName"));
                    } else if (temp.startsWith("VolumeSerialNumber")) {
                        disk.setDiskSerialNumber(PowerShellParseUtil.getPropertyValueForOneLine(temp, "VolumeSerialNumber"));
                    } else if (temp.startsWith("VolumeName")) {
                        disk.setDiskVolumeName(PowerShellParseUtil.getPropertyValueForOneLine(temp, "VolumeName"));
                    } else if (temp.startsWith("ProviderName")) {
                        disk.setDiskDeviceId(PowerShellParseUtil.getPropertyValueForOneLine(temp, "ProviderName"));
                    } else if (temp.startsWith("FileSystem")) {
                        disk.setFileSystem(PowerShellParseUtil.getPropertyValueForOneLine(temp, "FileSystem"));
                    }

                    initCount++;
                    if (initCount == 8) {
                        disks.add(disk); // List에 등록
                        disk = new Disk(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }

                }
            }


        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Network Disk", e.getMessage());
            throw new RoRoException(e);
        }

        return disks;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<InstalledSoftware> getInstalledSoftware(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2UnderCommand.INSTALLED_SOFTWARE);
            log.debug("\n" + PowerShellVersion2UnderCommand.INSTALLED_SOFTWARE);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            List<InstalledSoftware> installedSoftwares = new ArrayList<>();
            InstalledSoftware installedSoftware = new InstalledSoftware();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    if (temp.startsWith("DisplayName")) {
                        installedSoftware.setDisplayName(PowerShellParseUtil.getPropertyValueForOneLine(temp, "DisplayName"));
                    } else if (temp.startsWith("DisplayVersion")) {
                        installedSoftware.setDisplayVersion(PowerShellParseUtil.getPropertyValueForOneLine(temp, "DisplayVersion"));
                    } else if (temp.startsWith("Publisher")) {
                        installedSoftware.setPublisher(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Publisher"));
                    } else if (temp.startsWith("InstallDate")) {
                        installedSoftware.setInstallDate(PowerShellParseUtil.getPropertyValueForOneLine(temp, "InstallDate"));
                    }

                    initCount++;
                    if (initCount == 4) {
                        installedSoftwares.add(installedSoftware); // List에 등록
                        installedSoftware = new InstalledSoftware(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }
                }
            }

            return installedSoftwares;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Installed Software", e.getMessage());
            return new ArrayList<>();
        }

    }

    @Override
    public List<Process> getProcess(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2UnderCommand.PROCESS);
            log.debug("\n" + PowerShellVersion2UnderCommand.PROCESS);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            List<Process> processes = new ArrayList<>();
            Process process = new Process();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    if (temp.startsWith("Handles")) {
                        process.setHandles(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Handles"));
                    } else if (temp.startsWith("ProcessId")) {
                        process.setId(PowerShellParseUtil.getPropertyValueForOneLine(temp, "ProcessId"));
                    } else if (temp.startsWith("ProcessName")) {
                        process.setProcessName(PowerShellParseUtil.getPropertyValueForOneLine(temp, "ProcessName"));
                    } else if (temp.startsWith("WS")) {
                        String ws = PowerShellParseUtil.getPropertyValueForOneLine(temp, "WS");
                        process.setWs(StringUtils.isNotEmpty(ws) ? (Long.parseLong(ws) / 1024 + "") : "0");
                    } else if (temp.startsWith("Path")) {
                        process.setPath(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Path"));
                    } else if (temp.startsWith("Description")) {
                        process.setDescription(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Description"));
                    } else if (temp.startsWith("CommandLine")) {
                        System.out.println("CommandLine : " + PowerShellParseUtil.getPropertyValueForOneLine(temp, "CommandLine"));
                        process.setCommandLine(PowerShellParseUtil.getPropertyValueForOneLine(temp, "CommandLine"));
                    }

                    initCount++;
                    if (initCount == 7) {
                        processes.add(process); // List에 등록
                        process = new Process(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }
                }
            }

            return processes;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Process", e.getMessage());
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public Timezone getTimezone(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2UnderCommand.TIMEZONE);
            log.debug("\n" + PowerShellVersion2UnderCommand.TIMEZONE);

            Timezone timezone = new Timezone();

            timezone.setId(PowerShellParseUtil.getSystemPropertyValue(result, "StandardName"));
            timezone.setDisplayName(PowerShellParseUtil.getSystemPropertyValue(result, "Caption"));
            timezone.setStandardName(PowerShellParseUtil.getSystemPropertyValue(result, "StandardName"));
            timezone.setDaylightName(PowerShellParseUtil.getSystemPropertyValue(result, "DaylightName"));
            timezone.setSupportsDaylightSavingTime("");

            return timezone;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Timezone", e.getMessage());
            return new Timezone();
        }

    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<Schedule> getSchedules(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2UnderCommand.SCHEDULE_TASK);

            log.debug("\n" + PowerShellVersion2UnderCommand.SCHEDULE_TASK);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            List<Schedule> schedules = new ArrayList<>();
            Schedule schedule = new Schedule();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim()) && !temp.startsWith("Folder")) {
                    if (temp.startsWith("TaskName")) {
                        String taskPath = PowerShellParseUtil.getPropertyValueForOneLine(temp, "TaskName");
                        schedule.setTaskPath(taskPath.substring(0, StringUtils.lastIndexOf(taskPath, "\\") + 1));
                        schedule.setTaskName(taskPath.substring(StringUtils.lastIndexOf(taskPath, "\\") + 1));
                    } else if (temp.startsWith("Comment")) {
                        schedule.setDescription(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Comment"));
                    } else if (temp.startsWith("Status")) {
                        schedule.setState(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Status"));
                    }

                    initCount++;
                    if (initCount == 28) {
                        schedules.add(schedule); // List에 등록
                        schedule = new Schedule(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }
                }
            }

            return schedules;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Schedule", e.getMessage());
            return new ArrayList<>();
        }

    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<LocalUser> getLocalUsers(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2UnderCommand.LOCAL_USER);

            log.debug("\n" + PowerShellVersion2UnderCommand.LOCAL_USER);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            List<LocalUser> localUsers = new ArrayList<>();
            LocalUser localUser = new LocalUser();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    if (temp.startsWith("Name")) {
                        localUser.setName(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Name"));
                    } else if (temp.startsWith("Disabled")) {
                        String disabled = PowerShellParseUtil.getPropertyValueForOneLine(temp, "Disabled");
                        localUser.setEnabled(disabled.equals("False") ? "true" : "false");
                    } else if (temp.startsWith("Description")) {
                        localUser.setDescription(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Description"));
                    }
                    localUser.setObjectClass("User");

                    initCount++;
                    if (initCount == 3) {
                        localUsers.add(localUser); // List에 등록
                        localUser = new LocalUser(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }
                }
            }

            return localUsers;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("User", e.getMessage());
            return new ArrayList<>();
        }

    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public List<LocalGroupUser> getLocalGroupUsers(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        try {
            String result = executePsShell(targetHost, PowerShellVersion2UnderCommand.LOCAL_GROUP);
            String result2 = executePsShell(targetHost, PowerShellVersion2UnderCommand.LOCAL_GROUP_USER);

            log.debug("\n" + PowerShellVersion2UnderCommand.LOCAL_GROUP);
            log.debug("\n" + PowerShellVersion2UnderCommand.LOCAL_GROUP_USER);

            // 하나의 묶음으로 카운트할 라인 수.
            int initCount = 0;
            String[] stringArrays = splitToArrayByCrlf(result);

            List<LocalGroupUser> localGroupUsers = new ArrayList<>();
            LocalGroupUser localGroupUser = new LocalGroupUser();

            for (String temp : stringArrays) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    if (temp.startsWith("Name")) {
                        localGroupUser.setGroup(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Name"));
                    } else if (temp.startsWith("Description")) {
                        localGroupUser.setDescription(PowerShellParseUtil.getPropertyValueForOneLine(temp, "Description"));
                    }

                    initCount++;
                    if (initCount == 2) {
                        localGroupUsers.add(localGroupUser); // List에 등록
                        localGroupUser = new LocalGroupUser(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }
                }
            }

            initCount = 0;
            String[] stringArrays2 = splitToArrayByCrlf(result2);

            List<TempGroupUser> tempGroupUsers = new ArrayList<>();
            TempGroupUser tempGroupUser = new TempGroupUser();

            for (String temp : stringArrays2) {
                if (StringUtils.isNotEmpty(temp.trim())) {
                    if (temp.startsWith("GroupComponent")) {
                        String groupName = PowerShellParseUtil.getPropertyValueForOneLine(temp, "GroupComponent");
                        groupName = groupName.substring(StringUtils.lastIndexOf(groupName, "=") + 1).replaceAll("\"", "");
                        tempGroupUser.setGroup(groupName);
                    } else if (temp.startsWith("PartComponent")) {
                        String userName = PowerShellParseUtil.getPropertyValueForOneLine(temp, "PartComponent");
                        userName = userName.substring(StringUtils.lastIndexOf(userName, "=") + 1).replaceAll("\"", "");
                        tempGroupUser.setUser(userName);
                    }

                    initCount++;
                    if (initCount == 2) {
                        tempGroupUsers.add(tempGroupUser); // List에 등록
                        tempGroupUser = new TempGroupUser(); // 기존의 객체는 초기화
                        initCount = 0; // 다시 카운트도 초기화를 한다.
                    }
                }
            }

            // 중복되는 그룹에 속하는 User 정리.
            Map<String, String> tempMap = new HashMap<>();
            for (TempGroupUser tempGroupUser1 : tempGroupUsers) {
                if (tempMap.containsKey(tempGroupUser1.getGroup())) {
                    tempMap.put(tempGroupUser1.getGroup(), tempMap.get(tempGroupUser1.getGroup()) + ", " + tempGroupUser1.getUser());
                } else {
                    tempMap.put(tempGroupUser1.getGroup(), tempGroupUser1.getUser());
                }
            }

            for (LocalGroupUser localGroupUser1 : localGroupUsers) {
                if (tempMap.containsKey(localGroupUser1.getGroup())) {
                    localGroupUser1.setUsers(tempMap.get(localGroupUser1.getGroup()));
                }
            }

            return localGroupUsers;
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Group", e.getMessage());
            return new ArrayList<>();
        }

    }

}
//end of PowerShellVersion2UnderExecute.java
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
 * Jeongho Baek   9월 12, 2021		First Draft.
 */
package io.playce.roro.asmt.windows.impl.factory;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.asmt.windows.command.PowerShellVersion2UnderCommand;
import io.playce.roro.asmt.windows.command.PowerShellVersion3OverCommand;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.*;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static io.playce.roro.asmt.windows.impl.factory.PowerShellParseUtil.*;
import static io.playce.roro.common.util.WinRmUtils.executePsShell;
import static io.playce.roro.common.util.WinRmUtils.executePsShellByOverThere;

@SuppressWarnings("ALL")
@Slf4j
public abstract class PowerShellVersion3OverExecute implements PowerShellExecuteResult {

    public SystemInformation getSystemInformation(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        SystemInformation systemInformation = new SystemInformation();
        try {
            String result = executePsShell(targetHost, PowerShellVersion3OverCommand.SYSTEM);

            log.debug("\n" + PowerShellVersion3OverCommand.SYSTEM);

            if (StringUtils.isNotEmpty(result)) {
                systemInformation = objectMapper.readValue(result, SystemInformation.class);
                systemInformation.setManufacturer(systemInformation.getSystemManufacturer());
                systemInformation.setModel(systemInformation.getSystemModel());
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("System Information", e.getMessage());
            return new SystemInformation();
        }

        return systemInformation;
    }

    public Cpu getCpu(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        Cpu cpu = new Cpu();

        try {
            String result = executePsShell(targetHost, PowerShellVersion3OverCommand.CPU);

            log.debug("\n" + PowerShellVersion3OverCommand.CPU);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {

                if (isJsonArray(result)) {
                    List<Cpu> tempCpu = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Cpu[].class)));
                    cpu = tempCpu.get(0);
                    cpu.setSockets(tempCpu.size() + "");
                } else {
                    cpu = (objectMapper.readValue(result, Cpu.class));
                    cpu.setSockets("1");
                }
                return cpu;
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("CPU", e.getMessage());
            return new Cpu();
        }

        return cpu;
    }

    public List<Network> getNetworks(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Network> networks = new ArrayList<>();

        try {
            String result = executePsShell(targetHost, PowerShellVersion3OverCommand.NETWORK);

            log.debug("\n" + PowerShellVersion3OverCommand.NETWORK);
            log.debug("\n" + result);


            if (StringUtils.isNotEmpty(result)) {
                if (isJsonArray(result)) {
                    List<Map<String, Object>> networkMap = objectMapper.readValue(result, new TypeReference<>() {});
                    if (CollectionUtils.isNotEmpty(networkMap)) {
                        for (Map<String, Object> tempNetworkMap : networkMap) {
                            Network network = new Network();

                            String interfaceAlias = String.valueOf(tempNetworkMap.get("iAlias"));
                            try {
                                if (interfaceAlias != null && interfaceAlias.contains("?")) {
                                    result = executePsShell(targetHost, String.format(PowerShellVersion3OverCommand.NETWORK_NAME, String.valueOf(tempNetworkMap.get("iIndex"))));

                                    if (StringUtils.isNotEmpty(result)) {
                                        interfaceAlias = JsonUtil.readTree(result).get("NetConnectionID").asText();
                                    }
                                }
                            } catch (Exception e) {
                                // ignore
                            }

                            network.setInterfaceIndex(String.valueOf(tempNetworkMap.get("iIndex")));
                            network.setInterfaceDescription(String.valueOf(tempNetworkMap.get("iDesc")));
                            network.setInterfaceAlias(interfaceAlias);
                            network.setIPv4Address(convertObjectToList(tempNetworkMap.get("IPv4")));
                            network.setIPv4DefaultGateway(convertObjectToList(tempNetworkMap.get("IPv4DG")));
                            network.setIPv6Address(convertObjectToList(tempNetworkMap.get("IPv6")));
                            network.setIPv6DefaultGateway(convertObjectToList(tempNetworkMap.get("IPv6DG")));
                            network.setMacAddress(convertObjectToList(tempNetworkMap.get("Mac")));
                            network.setStatus(String.valueOf(tempNetworkMap.get("Status")));
                            networks.add(network);
                        }
                    }
                } else {
                    Map<String, Object> networkMap = objectMapper.readValue(result, new TypeReference<>() {});
                    Network network = new Network();

                    String interfaceAlias = String.valueOf(networkMap.get("iAlias"));
                    try {
                        if (interfaceAlias != null && interfaceAlias.contains("?")) {
                            result = executePsShell(targetHost, String.format(PowerShellVersion3OverCommand.NETWORK_NAME, String.valueOf(networkMap.get("iIndex"))));

                            if (StringUtils.isNotEmpty(result)) {
                                interfaceAlias = JsonUtil.readTree(result).get("NetConnectionID").asText();
                            }
                        }
                    } catch (Exception e) {
                        // ignore
                    }

                    network.setInterfaceIndex(String.valueOf(networkMap.get("iIndex")));
                    network.setInterfaceDescription(String.valueOf(networkMap.get("iDesc")));
                    network.setInterfaceAlias(interfaceAlias);
                    network.setIPv4Address(convertObjectToList(networkMap.get("IPv4")));
                    network.setIPv4DefaultGateway(convertObjectToList(networkMap.get("IPv4DG")));
                    network.setIPv6Address(convertObjectToList(networkMap.get("IPv6")));
                    network.setIPv6DefaultGateway(convertObjectToList(networkMap.get("IPv6DG")));
                    network.setMacAddress(convertObjectToList(networkMap.get("Mac")));
                    network.setStatus(String.valueOf(networkMap.get("Status")));
                    networks.add(network);
                }
            }

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Network", e.getMessage());
            return new ArrayList<>();
        }

        return networks;
    }

    public List<Dns> getDns(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Dns> dns = new ArrayList<>();

        try {
            String result = executePsShell(targetHost, PowerShellVersion3OverCommand.DNS);

            log.debug("\n" + PowerShellVersion3OverCommand.DNS);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {

                if (isJsonArray(result)) {
                    dns = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Dns[].class)));
                } else {
                    dns.add(objectMapper.readValue(result, Dns.class));
                }

                for (Dns tempDns : dns) {
                    tempDns.setAddressFamily(PowerShellParseUtil.getAddressFamily(tempDns.getAddressFamily()));
                }

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("DNS", e.getMessage());
            return new ArrayList<>();
        }

        return dns;
    }

    public List<Route> getRoutes(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Route> routes = new ArrayList<>();

        try {
            String result = executePsShell(targetHost, PowerShellVersion3OverCommand.ROUTE);

            log.debug("\n" + PowerShellVersion3OverCommand.ROUTE);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {

                if (isJsonArray(result)) {
                    routes = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Route[].class)));
                } else {
                    routes.add(objectMapper.readValue(result, Route.class));
                }

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Route", e.getMessage());
            return new ArrayList<>();
        }

        return routes;
    }

    public List<Port> getPorts(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Port> ports = new ArrayList<>();

        try {
            String result = executePsShellByOverThere(targetHost, PowerShellVersion3OverCommand.PORT);

            log.debug("\n" + PowerShellVersion3OverCommand.PORT);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {

                if (isJsonArray(result)) {
                    ports = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Port[].class)));
                } else {
                    ports.add(objectMapper.readValue(result, Port.class));
                }

                return ports;
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Port", e.getMessage());
            return new ArrayList<>();
        }

        return ports;
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

    public List<Firewall> getFirewalls(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Firewall> firewalls = new ArrayList<>();

        try {
            String result = executePsShell(targetHost, PowerShellVersion3OverCommand.FIREWALL);

            log.debug("\n" + PowerShellVersion3OverCommand.FIREWALL);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {

                if (isJsonArray(result)) {
                    firewalls = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Firewall[].class)));
                } else {
                    firewalls.add(objectMapper.readValue(result, Firewall.class));
                }

                for (Firewall firewall : firewalls) {
                    if (firewall.getTempLocalPort() instanceof Map) {
                        Map<String, List<String>> localPortMap = (Map<String, List<String>>) firewall.getTempLocalPort();
                        firewall.setLocalPort(localPortMap.get("value"));
                    } else {
                        if (firewall.getTempLocalPort() != null) {
                            firewall.setLocalPort(Collections.singletonList(firewall.getTempLocalPort().toString()));
                        }
                    }

                    if (firewall.getTempRemotePort() instanceof Map) {
                        Map<String, List<String>> remotePortMap = (Map<String, List<String>>) firewall.getTempRemotePort();
                        firewall.setRemotePort(remotePortMap.get("value"));
                    } else {
                        if (firewall.getTempRemotePort() != null) {
                            firewall.setRemotePort(Collections.singletonList(firewall.getTempRemotePort().toString()));
                        }
                    }

                    if (firewall.getTempRemoteAddress() instanceof Map) {
                        Map<String, List<String>> remoteAddressMap = (Map<String, List<String>>) firewall.getTempRemoteAddress();
                        firewall.setRemoteAddress(remoteAddressMap.get("value"));
                    } else {
                        if (firewall.getTempRemoteAddress() != null) {
                            firewall.setRemoteAddress(Collections.singletonList(firewall.getTempRemoteAddress().toString()));
                        }
                    }
                }

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Firewall", e.getMessage());
            return new ArrayList<>();
        }

        return firewalls;
    }

    public List<Disk> getDisks(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Disk> disks = new ArrayList<>();
        List<Disk> networkDisks = new ArrayList<>();

        //Local Disk Driver
        try {
            String result = executePsShellByOverThere(targetHost, PowerShellVersion3OverCommand.DISK);

            log.debug("\n" + PowerShellVersion3OverCommand.DISK);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {
                if (isJsonArray(result)) {
                    disks = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Disk[].class)));
                } else {
                    disks.add(objectMapper.readValue(result, Disk.class));
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Disk", e.getMessage());
            return new ArrayList<>();
        }

        //Network Disk Driver
        try {
            String result = executePsShellByOverThere(targetHost, PowerShellVersion3OverCommand.NETWORK_DISK);

            log.debug("\n" + PowerShellVersion3OverCommand.NETWORK_DISK);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {
                if (isJsonArray(result)) {
                    networkDisks = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Disk[].class)));
                    networkDisks = networkDisks.stream().distinct().collect(Collectors.toList());
                } else {
                    networkDisks.add(objectMapper.readValue(result, Disk.class));
                }
            }

            for (Disk networkDisk : networkDisks) {
                networkDisk.setTotalSize(convertByteToGByte(networkDisk.getTotalSize()));
                networkDisk.setFreeSpace(convertByteToGByte(networkDisk.getFreeSpace()));
            }

            networkDisks = null;

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("NETWORK_DISK", e.getMessage());
            return new ArrayList<>();
        }

        if (CollectionUtils.isNotEmpty(networkDisks)) {
            disks.addAll(networkDisks);
        }

        return disks;
    }

    public List<InstalledSoftware> getInstalledSoftware(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<InstalledSoftware> installedSoftwares = new ArrayList<>();

        try {
            String result = executePsShell(targetHost, PowerShellVersion3OverCommand.INSTALLED_SOFTWARE);

            log.debug("\n" + PowerShellVersion3OverCommand.INSTALLED_SOFTWARE);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {

                if (isJsonArray(result)) {
                    installedSoftwares = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, InstalledSoftware[].class)));
                } else {
                    installedSoftwares.add(objectMapper.readValue(result, InstalledSoftware.class));
                }

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Installed Software", e.getMessage());
            return new ArrayList<>();
        }

        return installedSoftwares;
    }

    public List<Service> getServices(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<Service> services = new ArrayList<>();

        try {
            String result = executePsShell(targetHost, PowerShellVersion3OverCommand.SERVICE);

            log.debug("\n" + PowerShellVersion3OverCommand.SERVICE);
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {

                if (isJsonArray(result)) {
                    services = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, Service[].class)));
                } else {
                    services.add(objectMapper.readValue(result, Service.class));
                }

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Service", e.getMessage());
            return new ArrayList<>();
        }

        return services;
    }

    public List<LocalGroupUser> getLocalGroupUsers(TargetHost targetHost, ObjectMapper objectMapper, Map<String, String> errorMap) throws InterruptedException {
        List<LocalGroupUser> localGroupUsers = new ArrayList<>();

        try {
            String result = executePsShellByOverThere(targetHost, PowerShellVersion3OverCommand.LOCAL_GROUP_USER);

            log.debug("\n" + PowerShellVersion3OverCommand.LOCAL_GROUP_USER.toString());
            log.debug("\n" + result);

            if (StringUtils.isNotEmpty(result)) {

                if (isJsonArray(result)) {
                    localGroupUsers = new ArrayList<>(Arrays.asList(objectMapper.readValue(result, LocalGroupUser[].class)));
                } else {
                    localGroupUsers.add(objectMapper.readValue(result, LocalGroupUser.class));
                }

            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            errorMap.put("Group", e.getMessage());
            return new ArrayList<>();
        }

        return localGroupUsers;
    }

    private String convertByteToGByte(String diskSize) {
        if (StringUtils.isEmpty(diskSize)) {
            return "0";
        } else {
            Double totalSize = (Double.parseDouble(diskSize) / (1024 * 1024 * 1024));
            return String.format("%.2f", totalSize) + " GB";
        }
    }
}
//end of PowerShellVersion3OverExecute.java
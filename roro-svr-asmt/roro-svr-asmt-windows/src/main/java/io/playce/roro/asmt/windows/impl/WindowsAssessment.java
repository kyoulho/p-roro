package io.playce.roro.asmt.windows.impl;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto;
import io.playce.roro.asmt.windows.dto.WindowsAssessmentDto.*;
import io.playce.roro.asmt.windows.impl.factory.PowerShellExecuteResultFactory;
import io.playce.roro.common.exception.NotsupportedException;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.AbstractServerAssessment;
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
import io.playce.roro.svr.asmt.dto.result.WindowsAssessmentResult;
import io.playce.roro.svr.asmt.dto.user.Group;
import io.playce.roro.svr.asmt.dto.user.User;
import io.playce.roro.svr.asmt.dto.windows.InstalledSoftware;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.playce.roro.common.util.ThirdPartySolutionUtil.detectThirdPartySolutionsFromServer;
import static io.playce.roro.common.util.WinRmUtils.IS_TEMP_DIR_CREATED;
import static io.playce.roro.common.util.WinRmUtils.POWERSHELL_VERSION;

@Slf4j
@Component("WINDOWSAssessment")
@RequiredArgsConstructor
public class WindowsAssessment extends AbstractServerAssessment {

    private final static String LISTENING = "LISTENING";
    private final static String ESTABLISHED = "ESTABLISHED";
    private final static String TIME_WAIT = "TIME_WAIT";

    private final PowerShellExecuteResultFactory powerShellExecuteResultFactory;
    private final WindowsExtractService windowsExtractService;
    private final ModelMapper modelMapper;

    @Override
    public Map<String, String> generateCommand() {
        return null;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public WindowsAssessmentResult assessment(TargetHost targetHost) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);

        // PowerShell Major Version
        int powerShellMajorVersion = WindowsCommonExecutor.getPowerShellVersion(targetHost);
        log.debug("PowerShell Version : {}", powerShellMajorVersion);

        if (powerShellMajorVersion > 0) {
            try {
                ThreadLocalUtils.add(POWERSHELL_VERSION, powerShellMajorVersion);
                ThreadLocalUtils.add(IS_TEMP_DIR_CREATED, false);

                WindowsResult windowsResult = getWindowsResult(targetHost, objectMapper, powerShellMajorVersion);

                // Middleware에서 검색된 서비스를 Process에 등록을 한다.
                windowsResult = windowsExtractService.convertMiddlewareServiceToProcess(targetHost, windowsResult);

                return convertWindowsResultToWindowsAssessmentResult(targetHost, windowsResult, objectMapper);
            } finally {
                ThreadLocalUtils.clearSharedObject();
            }
        } else {
            throw new NotsupportedException("PowerShell Not Found.");
        }

    }

    private WindowsResult getWindowsResult(TargetHost targetHost, ObjectMapper objectMapper, int powerShellMajorVersion) throws InterruptedException {
        Map<String, String> errorMap = new HashMap<>();

        return WindowsResult.builder()
                .systemInformation(powerShellExecuteResultFactory.getSystemInformation(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .environment(powerShellExecuteResultFactory.getEnvironment(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .cpu(powerShellExecuteResultFactory.getCpu(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .networks(powerShellExecuteResultFactory.getNetworks(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .dns(powerShellExecuteResultFactory.getDns(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .routes(powerShellExecuteResultFactory.getRoutes(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .ports(powerShellExecuteResultFactory.getPorts(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .hosts(powerShellExecuteResultFactory.getHosts(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .firewalls(powerShellExecuteResultFactory.getFirewalls(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .disks(powerShellExecuteResultFactory.getDisks(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .installedSoftware(powerShellExecuteResultFactory.getInstalledSoftware(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .process(powerShellExecuteResultFactory.getProcess(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .services(powerShellExecuteResultFactory.getServices(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .timezone(powerShellExecuteResultFactory.getTimezone(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .schedules(powerShellExecuteResultFactory.getSchedules(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .localUsers(powerShellExecuteResultFactory.getLocalUsers(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .localGroupUsers(powerShellExecuteResultFactory.getLocalGroupUsers(targetHost, objectMapper, powerShellMajorVersion, errorMap))
                .errorMap(errorMap)
                .build();
    }

    public WindowsAssessmentResult convertWindowsResultToWindowsAssessmentResult(WindowsResult windowsResult, ObjectMapper objectMapper) {
        return convertWindowsResultToWindowsAssessmentResult(null, windowsResult, objectMapper);
    }

    private WindowsAssessmentResult convertWindowsResultToWindowsAssessmentResult(TargetHost targetHost, WindowsResult windowsResult, ObjectMapper objectMapper) {

        List<String> processStringList = windowsResult.getProcess().stream()
                .filter(p -> p.getCommandLine() != null)
                .map(p -> String.join(" ", p.getCommandLine()))
                .collect(Collectors.toList());

        List<String> processUserList = windowsResult.getProcess().stream()
                .filter(p -> p.getUserName() != null)
                .map(p -> covertUsername(p.getUserName()))
                .collect(Collectors.toList());

        List<String> serviceList = windowsResult.getServices().stream()
                .filter(s -> s.getServiceName() != null)
                .map(s -> s.getServiceName())
                .collect(Collectors.toList());

        List<Integer> portList = windowsResult.getPorts().stream()
                .filter(p -> p.getState().equals("LISTENING"))
                .map(p -> Integer.parseInt(p.getLocalPort()))
                .collect(Collectors.toList());

        List<String> installedSoftwareList = windowsResult.getInstalledSoftware().stream()
                .filter(p -> p.getDisplayName() != null)
                .map(p -> p.getDisplayName())
                .collect(Collectors.toList());

        Map<String, String> scheduleMap = getCronTab(windowsResult.getSchedules(), new HashMap<>());

        return WindowsAssessmentResult.builder()
                .architecture(windowsResult.getSystemInformation().getSystemType())
                .biosVersion(windowsResult.getSystemInformation().getBiosVersion())
                .distribution(windowsResult.getSystemInformation().getOsName())
                .distributionRelease(windowsResult.getSystemInformation().getOsName())
                .family(windowsResult.getSystemInformation().getOsManufacturer())
                .hostname(windowsResult.getSystemInformation().getHostName())
                .kernel(windowsResult.getSystemInformation().getOsVersion())
                .uptime(windowsResult.getSystemInformation().getSystemBootTime())
                .timezone(windowsResult.getSystemInformation().getTimeZone())
                .systemVendor(windowsResult.getSystemInformation().getSystemManufacturer())
                .productName(windowsResult.getSystemInformation().getSystemModel())
                .productSerial(windowsResult.getSystemInformation().getProductId())
                .defInfo(new DefInfo())
                .memory(getMemoryInfo(windowsResult.getSystemInformation(), windowsResult.getErrorMap()))
                .cpu(getCpuInfo(windowsResult.getCpu(), windowsResult.getErrorMap()))
                .portList(getPort(windowsResult.getPorts(), windowsResult.getErrorMap()))
                .hosts(getHosts(windowsResult.getHosts(), windowsResult.getErrorMap()))
                .firewall(new Firewall())
                .dns(getDns(windowsResult.getDns(), windowsResult.getErrorMap()))
                .routeTables(getRoute(windowsResult.getRoutes(), windowsResult.getErrorMap()))
                .processes(getProcess(windowsResult.getProcess(), windowsResult.getErrorMap()))
                .fsTabs(getFsTab(windowsResult.getDisks(), windowsResult.getErrorMap()))
                .env(getEnvironment(windowsResult.getEnvironment(), objectMapper, windowsResult.getErrorMap()))
                .shadows(new HashMap<>())
                .crontabs(getCronTab(windowsResult.getSchedules(), windowsResult.getErrorMap()))
                .locale(getLocale(windowsResult.getSystemInformation(), windowsResult.getErrorMap()))
                .ulimits(new HashMap<>())
                .daemons(getDaemon(windowsResult.getServices(), windowsResult.getErrorMap()))
                .users(getUser(windowsResult.getLocalUsers(), windowsResult.getErrorMap()))
                .groups(getGroup(windowsResult.getLocalGroupUsers(), windowsResult.getErrorMap()))
                .partitions(getPartitions(windowsResult.getDisks(), windowsResult.getErrorMap()))
                .interfaces(getInterface(windowsResult.getNetworks(), windowsResult.getErrorMap()))
                .thirdPartySolutions(detectThirdPartySolutionsFromServer(
                        targetHost, true, this.getClass().getName(), processStringList, processUserList, installedSoftwareList, serviceList, portList, scheduleMap))
                .windowsResult(getOriginWindowsResult(windowsResult))
                .errorMap(windowsResult.getErrorMap())
                .build();
    }

    private MemoryInfo getMemoryInfo(SystemInformation systemInformation, Map<String, String> errorMap) {
        MemoryInfo memoryInfo = new MemoryInfo();

        try {
            String swapTotalMb = StringUtils.substringBefore(systemInformation.getVirtualMemoryMaxSize().replaceAll(",", ""), " ");
            String swapFreeMb = StringUtils.substringBefore(systemInformation.getVirtualMemoryAvailable().replaceAll(",", ""), " ");

            memoryInfo.setMemTotalMb(StringUtils.substringBefore(systemInformation.getTotalPhysicalMemory().replaceAll(",", ""), " "));
            memoryInfo.setMemFreeMb(StringUtils.substringBefore(systemInformation.getAvailablePhysicalMemory().replaceAll(",", ""), " "));
            memoryInfo.setSwapTotalMb(StringUtils.isEmpty(swapTotalMb) ? "0" : swapTotalMb);
            memoryInfo.setSwapFreeMb(StringUtils.isEmpty(swapFreeMb) ? "0" : swapFreeMb);
        } catch (Exception e) {
            errorMap.put("Memory", "Unable parse memory information.");
        }

        return memoryInfo;
    }

    private CpuInfo getCpuInfo(Cpu cpu, Map<String, String> errorMap) {
        CpuInfo cpuInfo = new CpuInfo();

        try {
            cpuInfo.setProcessor(cpu.getName());
            cpuInfo.setProcessorCores(cpu.getCores());
            cpuInfo.setProcessorCount(cpu.getSockets());
        } catch (Exception e) {
            errorMap.put("CPU", "Unable parse CPU information.");
        }

        return cpuInfo;
    }

    @SuppressWarnings("DuplicatedCode")
    private PortList getPort(List<Port> ports, Map<String, String> errorMap) {
        PortList portList = new PortList();

        try {
            List<ListenPort> listenPorts = new ArrayList<>();

            EstablishedPort establishedPort = new EstablishedPort();
            List<Traffic> establishedAnyToLocal = new ArrayList<>();
            List<Traffic> establishedLocalToAny = new ArrayList<>();

            WaitPort waitPort = new WaitPort();
            List<Traffic> waitAnyToLocal = new ArrayList<>();
            List<Traffic> waitLocalToAny = new ArrayList<>();

            // Listen Port
            for (Port port : ports) {
                ListenPort listenPort = new ListenPort();

                if (port.getState().equals(LISTENING) && isValidIp4Address(port.getLocalAddress())) {
                    listenPort.setProtocol(port.getProtocol());
                    listenPort.setBindAddr(port.getLocalAddress());
                    listenPort.setPort(port.getLocalPort());
                    listenPort.setPid(port.getPid());
                    listenPort.setName(port.getProcessName());

                    listenPorts.add(listenPort);
                }
            }

            for (Port port : ports) {
                if (port.getState().equals(ESTABLISHED) && isValidIp4Address(port.getLocalAddress())) {
                    Traffic establishedAnyToLocalTraffic = new Traffic();
                    Traffic establishedLocalToAnyTraffic = new Traffic();

                    List<ListenPort> tempListenPorts = listenPorts.stream()
                            .filter(s -> s.getPort().equals(port.getLocalPort()))
                            .collect(Collectors.toList());

                    // Listening 중에 포함 되어 있으면 AnyToLocal
                    // Loopback 또는 자신의 IP는 제외한다.
                    if (CollectionUtils.isNotEmpty(tempListenPorts)) {
                        if (!port.getLocalAddress().equals(port.getRemoteAddress())
                                && !port.getRemoteAddress().equals("127.0.0.1")) {
                            establishedAnyToLocalTraffic.setProtocol(port.getProtocol());
                            establishedAnyToLocalTraffic.setFaddr(port.getRemoteAddress());
                            establishedAnyToLocalTraffic.setFport(port.getRemotePort());
                            establishedAnyToLocalTraffic.setLaddr(port.getLocalAddress());
                            establishedAnyToLocalTraffic.setLport(port.getLocalPort());
                            establishedAnyToLocalTraffic.setPid(port.getPid());
                            establishedAnyToLocalTraffic.setName(port.getProcessName());
                            establishedAnyToLocalTraffic.setStatus(port.getState());

                            establishedAnyToLocal.add(establishedAnyToLocalTraffic);
                        }
                    } else {
                        if (!port.getLocalAddress().equals(port.getRemoteAddress())
                                && !port.getRemoteAddress().equals("127.0.0.1")) {
                            establishedLocalToAnyTraffic.setProtocol(port.getProtocol());
                            establishedLocalToAnyTraffic.setFaddr(port.getRemoteAddress());
                            establishedLocalToAnyTraffic.setFport(port.getRemotePort());
                            establishedLocalToAnyTraffic.setLaddr(port.getLocalAddress());
                            establishedLocalToAnyTraffic.setLport(port.getLocalPort());
                            establishedLocalToAnyTraffic.setPid(port.getPid());
                            establishedLocalToAnyTraffic.setName(port.getProcessName());
                            establishedLocalToAnyTraffic.setStatus(port.getState());

                            establishedLocalToAny.add(establishedLocalToAnyTraffic);
                        }
                    }
                }
            }

            establishedPort.setAnyToLocal(establishedAnyToLocal);
            establishedPort.setLocalToAny(establishedLocalToAny);

            for (Port port : ports) {
                if (port.getState().equals(TIME_WAIT) && isValidIp4Address(port.getLocalAddress())) {
                    Traffic waitAnyToLocalTraffic = new Traffic();
                    Traffic waitLocalToAnyTraffic = new Traffic();

                    List<ListenPort> tempListenPorts = listenPorts.stream()
                            .filter(s -> s.getPort().equals(port.getLocalPort()))
                            .collect(Collectors.toList());

                    // Listening 중에 포함 되어 있으면 AnyToLocal
                    if (CollectionUtils.isNotEmpty(tempListenPorts)) {
                        waitAnyToLocalTraffic.setProtocol(port.getProtocol());
                        waitAnyToLocalTraffic.setFaddr(port.getRemoteAddress());
                        waitAnyToLocalTraffic.setFport(port.getRemotePort());
                        waitAnyToLocalTraffic.setLaddr(port.getLocalAddress());
                        waitAnyToLocalTraffic.setLport(port.getLocalPort());
                        waitAnyToLocalTraffic.setPid(port.getPid());
                        waitAnyToLocalTraffic.setName(port.getProcessName());
                        waitAnyToLocalTraffic.setStatus(port.getState());

                        waitAnyToLocal.add(waitAnyToLocalTraffic);
                    } else {
                        waitLocalToAnyTraffic.setProtocol(port.getProtocol());
                        waitLocalToAnyTraffic.setFaddr(port.getRemoteAddress());
                        waitLocalToAnyTraffic.setFport(port.getRemotePort());
                        waitLocalToAnyTraffic.setLaddr(port.getLocalAddress());
                        waitLocalToAnyTraffic.setLport(port.getLocalPort());
                        waitLocalToAnyTraffic.setPid(port.getPid());
                        waitLocalToAnyTraffic.setName(port.getProcessName());
                        waitLocalToAnyTraffic.setStatus(port.getState());

                        waitLocalToAny.add(waitLocalToAnyTraffic);
                    }
                }
            }

            waitPort.setAnyToLocal(waitAnyToLocal);
            waitPort.setLocalToAny(waitLocalToAny);

            portList.setListen(listenPorts);
            portList.setEstablished(establishedPort);
            portList.setWait(waitPort);
        } catch (Exception e) {
            errorMap.put("Port", "Unable parse port information.");
        }

        return portList;
    }

    private Hosts getHosts(WindowsAssessmentDto.Hosts hosts, Map<String, String> errorMap) {
        Hosts tempHosts = new Hosts();

        tempHosts.setContents(hosts.getContents());
        tempHosts.setMappings(hosts.getMappings());

        return tempHosts;
    }

//    private List<io.playce.roro.svr.asmt.dto.windows.Firewall> getFirewall(List<Firewall> firewalls) {
//        List<io.playce.roro.svr.asmt.dto.windows.Firewall> firewallList = new ArrayList<>();
//
//        for (Firewall firewall : firewalls) {
//            io.playce.roro.svr.asmt.dto.windows.Firewall tempFirewall = new io.playce.roro.svr.asmt.dto.windows.Firewall();
//            tempFirewall.setName(firewall.getName());
//            tempFirewall.setDisplayName(firewall.getDisplayName());
//            tempFirewall.setDescription(firewall.getDescription());
//            tempFirewall.setProtocol(firewall.getProtocol());
//            tempFirewall.setLocalPort(firewall.getLocalPort());
//            tempFirewall.setRemotePort(firewall.getRemotePort());
//            tempFirewall.setRemoteAddress(firewall.getRemoteAddress());
//            tempFirewall.setEnabled(firewall.getEnabled());
//            tempFirewall.setDirection(firewall.getDirection());
//            tempFirewall.setAction(firewall.getAction());
//
//            firewallList.add(tempFirewall);
//        }
//
//        return firewallList;
//    }

    private List<String> getDns(List<Dns> dns, Map<String, String> errorMap) {
        List<String> dnsList = new ArrayList<>();

        try {
            for (Dns tempDns : dns) {
                Collections.addAll(dnsList, tempDns.getServerAddresses());
            }
        } catch (Exception e) {
            errorMap.put("DNS", "Unable parse DNS information.");
        }

        return dnsList;
    }

    // https://goduck2.tistory.com/5 참조.
    private List<RouteTable> getRoute(List<Route> routes, Map<String, String> errorMap) {
        List<RouteTable> routeTables = new ArrayList<>();

        try {
            for (Route route : routes) {
                RouteTable routeTable = new RouteTable();
                routeTable.setIface(route.getIfIndex());
                routeTable.setDestination(route.getDestinationPrefix());
                routeTable.setGateway(route.getNextHop());

                routeTables.add(routeTable);
            }
        } catch (Exception e) {
            errorMap.put("Route", "Unable parse route information.");
        }

        return routeTables;
    }

    private List<Process> getProcess(List<WindowsAssessmentDto.Process> processList, Map<String, String> errorMap) {
        List<Process> processes = new ArrayList<>();

        try {
            CSVParser parser = new CSVParserBuilder().withSeparator(' ').build();

            for (WindowsAssessmentDto.Process tempProcess : processList) {
                Process process = new Process();
                process.setName(tempProcess.getProcessName());
                process.setUser(tempProcess.getUserName());
                process.setPid(tempProcess.getId());

                String commandLine = StringUtils.defaultString(tempProcess.getCommandLine());

                if (commandLine.contains("nginx:master")) {
                    commandLine = commandLine.replaceAll("nginx:master", "nginx: master");
                }

                try {
                    String[] commandLineArray = parser.parseLine(commandLine.replaceAll("\\\\", "\\\\\\\\"));
                    process.setCmd(Arrays.asList(commandLineArray));
                } catch (IOException e) {
                    log.error("Exception CommandLine : {}", commandLine, e);
                    process.setCmd(convertIgnoreQuotations(commandLine));
                }

                processes.add(process);
            }
        } catch (Exception e) {
            errorMap.put("Process", "Unable parse process information.");
        }

        return processes;
    }

    private List<FsTab> getFsTab(List<Disk> disks, Map<String, String> errorMap) {
        List<FsTab> fsTabs = new ArrayList<>();

        try {
            for (Disk disk : disks) {
                FsTab fsTab = new FsTab();
                fsTab.setDevice(disk.getDiskDeviceId());
                fsTab.setMount(disk.getDriveLetter());
                fsTab.setType(disk.getFileSystem());

                fsTabs.add(fsTab);
            }
        } catch (Exception e) {
            errorMap.put("Disk", "Unable parse disk information.");
        }

        return fsTabs;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getEnvironment(Environment environment, ObjectMapper objectMapper, Map<String, String> errorMap) {
        return objectMapper.convertValue(environment, Map.class);
    }

    private Map<String, String> getCronTab(List<Schedule> schedules, Map<String, String> errorMap) {
        Map<String, String> cronTabMap = new HashMap<>();

        try {
            for (Schedule schedule : schedules) {
                cronTabMap.put(schedule.getTaskName(), schedule.getState());
            }
        } catch (Exception e) {
            errorMap.put("Schedule", "Unable parse schedule information.");
        }

        return cronTabMap;
    }

    private Map<String, String> getLocale(SystemInformation systemInformation, Map<String, String> errorMap) {
        Map<String, String> localeMap = new HashMap<>();

        try {
            localeMap.put("SystemLocale", systemInformation.getSystemLocale());
            localeMap.put("InputLocale", systemInformation.getInputLocale());
        } catch (Exception e) {
            errorMap.put("System Information", "Unable parse system information.");
        }

        return localeMap;
    }

    private Map<String, Map<String, String>> getDaemon(List<Service> services, Map<String, String> errorMap) {
        Map<String, Map<String, String>> demonMap = new HashMap<>();

        try {
            for (Service service : services) {
                Map<String, String> tempDaemonMap = new HashMap<>();
                tempDaemonMap.put("displayName", service.getDisplayName());
                tempDaemonMap.put("serviceName", service.getServiceName());
                tempDaemonMap.put("serviceType", service.getServiceType());
                tempDaemonMap.put("startType", service.getStartType());
                tempDaemonMap.put("status", service.getStatus());

                demonMap.put(service.getName(), tempDaemonMap);
            }
        } catch (Exception e) {
            errorMap.put("Service", "Unable parse service information.");
        }

        return demonMap;
    }

    private Map<String, User> getUser(List<LocalUser> localUsers, Map<String, String> errorMap) {
        Map<String, User> userMap = new HashMap<>();

        try {
            for (LocalUser localUser : localUsers) {
                userMap.put(localUser.getName(), new User());
            }
        } catch (Exception e) {
            errorMap.put("User", "Unable parse user information.");
        }

        return userMap;
    }

    private Map<String, Group> getGroup(List<LocalGroupUser> localGroupUsers, Map<String, String> errorMap) {
        Map<String, Group> groupMap = new HashMap<>();

        try {
            for (LocalGroupUser localGroupUser : localGroupUsers) {
                if (StringUtils.isNotEmpty(localGroupUser.getUsers())) {
                    Group group = new Group();
                    group.setGid(null);
                    group.setUsers(Collections.list(new StringTokenizer(localGroupUser.getUsers(), ",")).stream()
                            .map(token -> ((String) token).trim())
                            .collect(Collectors.toList()));

                    groupMap.put(localGroupUser.getGroup(), group);
                }
            }
        } catch (Exception e) {
            errorMap.put("Group", "Unable parse group information.");
        }

        return groupMap;
    }

    private Map<String, Partition> getPartitions(List<Disk> disks, Map<String, String> errorMap) {
        Map<String, Partition> partitionMap = new HashMap<>();

        try {
            for (Disk disk : disks) {
                Partition partition = new Partition();
                partition.setDevice(disk.getDiskDeviceId());
                partition.setSize(convertDiskSize(disk.getTotalSize()));
                partition.setFree(convertDiskSize(disk.getFreeSpace()));
                partition.setFsType(disk.getFileSystem());

                partitionMap.put(disk.getDriveLetter(), partition);
            }
        } catch (Exception e) {
            errorMap.put("Disk", "Unable parse disk information.");
        }

        return partitionMap;
    }

    private Map<String, InterfaceInfo> getInterface(List<Network> networks, Map<String, String> errorMap) {
        Map<String, InterfaceInfo> interfaceInfoMap = new HashMap<>();

        try {
            for (Network network : networks) {
                InterfaceInfo interfaceInfo = new InterfaceInfo();
                interfaceInfo.setDevice(StringUtils.defaultString(network.getInterfaceAlias(), network.getInterfaceDescription()));
                interfaceInfo.setMacaddress(StringUtils.join(network.getMacAddress(), ""));
                interfaceInfo.setGateway(StringUtils.join(network.getIPv4DefaultGateway(), ""));

                List<Ipv4Address> ipv4Addresses = new ArrayList<>();
                List<Ipv6Address> ipv6Addresses = new ArrayList<>();

                for (String ip4Address : network.getIPv4Address()) {
                    Ipv4Address ipv4Address = new Ipv4Address();
                    ipv4Address.setAddress(ip4Address);
                    ipv4Addresses.add(ipv4Address);
                }

                for (String ip6Address : network.getIPv6Address()) {
                    Ipv6Address ipv6Address = new Ipv6Address();
                    ipv6Address.setAddress(ip6Address);
                    ipv6Addresses.add(ipv6Address);
                }

                interfaceInfo.setIpv4(ipv4Addresses);
                interfaceInfo.setIpv6(ipv6Addresses);

                interfaceInfoMap.put(network.getInterfaceIndex(), interfaceInfo);
            }
        } catch (Exception e) {
            errorMap.put("Network", "Unable parse network information.");
        }

        return interfaceInfoMap;
    }

    @SuppressWarnings("unused")
    private List<InstalledSoftware> getInstalledSoftware(List<WindowsAssessmentDto.InstalledSoftware> winInstalledSoftware, Map<String, String> errorMap) {
        List<InstalledSoftware> installedSoftwares = new ArrayList<>();

        try {
            for (WindowsAssessmentDto.InstalledSoftware tempInstalledSoftware : winInstalledSoftware) {
                InstalledSoftware installedSoftware = new InstalledSoftware();
                installedSoftware.setDisplayName(tempInstalledSoftware.getDisplayName());
                installedSoftware.setDisplayVersion(tempInstalledSoftware.getDisplayVersion());
                installedSoftware.setPublisher(tempInstalledSoftware.getPublisher());
                installedSoftware.setInstallDate(tempInstalledSoftware.getInstallDate());

                installedSoftwares.add(installedSoftware);
            }
        } catch (Exception e) {
            errorMap.put("Installed Software", "Unable parse installed software information.");
        }

        return installedSoftwares;
    }

    private boolean isValidIp4Address(String ipAddress) {
        final InetAddressValidator validator = InetAddressValidator.getInstance();

        return validator.isValidInet4Address(ipAddress);
    }

    private String convertDiskSize(String diskSize) {
        if (diskSize.contains("MB")) {
            return diskSize.replaceAll("MB", "").trim();
        } else if (diskSize.contains("GB")) {
            return (int) (Double.parseDouble(diskSize.replaceAll("GB", "").trim()) * 1024) + "";
        } else {
            return diskSize.trim();
        }
    }

    private WindowsAssessmentResult.WindowsResult getOriginWindowsResult(WindowsResult windowsResult) {
        WindowsAssessmentResult.WindowsResult tempWindowsResult = new WindowsAssessmentResult.WindowsResult();

        tempWindowsResult.setSystemInformation(modelMapper.map(windowsResult.getSystemInformation(), WindowsAssessmentResult.SystemInformation.class));
        tempWindowsResult.setEnvironment(modelMapper.map(windowsResult.getEnvironment(), WindowsAssessmentResult.Environment.class));
        tempWindowsResult.setCpu(modelMapper.map(windowsResult.getCpu(), WindowsAssessmentResult.Cpu.class));
        tempWindowsResult.setNetworks(mapList(windowsResult.getNetworks(), WindowsAssessmentResult.Network.class));
        tempWindowsResult.setDns(mapList(windowsResult.getDns(), WindowsAssessmentResult.Dns.class));
        tempWindowsResult.setRoutes(mapList(windowsResult.getRoutes(), WindowsAssessmentResult.Route.class));
        tempWindowsResult.setPorts(mapList(windowsResult.getPorts(), WindowsAssessmentResult.Port.class));
        tempWindowsResult.setHosts(modelMapper.map(windowsResult.getHosts(), WindowsAssessmentResult.Hosts.class));
        tempWindowsResult.setFirewalls(mapList(windowsResult.getFirewalls(), WindowsAssessmentResult.Firewall.class));
        tempWindowsResult.setDisks(mapList(windowsResult.getDisks(), WindowsAssessmentResult.Disk.class));
        tempWindowsResult.setInstalledSoftware(mapList(windowsResult.getInstalledSoftware(), WindowsAssessmentResult.InstalledSoftware.class));
        tempWindowsResult.setProcess(mapList(windowsResult.getProcess(), WindowsAssessmentResult.Process.class));
        tempWindowsResult.setServices(mapList(windowsResult.getServices(), WindowsAssessmentResult.Service.class));
        tempWindowsResult.setTimezone(modelMapper.map(windowsResult.getTimezone(), WindowsAssessmentResult.Timezone.class));
        tempWindowsResult.setSchedules(mapList(windowsResult.getSchedules(), WindowsAssessmentResult.Schedule.class));
        tempWindowsResult.setLocalUsers(mapList(windowsResult.getLocalUsers(), WindowsAssessmentResult.LocalUser.class));
        tempWindowsResult.setLocalGroupUsers(mapList(windowsResult.getLocalGroupUsers(), WindowsAssessmentResult.LocalGroupUser.class));
        tempWindowsResult.setErrorMap(windowsResult.getErrorMap());

        return tempWindowsResult;
    }

    private <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }

    private List<String> convertIgnoreQuotations(String commandLine) {
        CSVParser parser = new CSVParserBuilder().withIgnoreQuotations(true).withSeparator(' ').build();
        String[] commandLineArray;

        try {
            commandLineArray = parser.parseLine(commandLine.replaceAll("\\\\", "\\\\\\\\"));
        } catch (IOException e) {
            log.error("ConvertIgnoreQuotations Error : {}", commandLine, e);
            return List.of(commandLine);
        }

        return Arrays.asList(commandLineArray);
    }

    private String covertUsername(String username) {
        int idx = username.indexOf("\\");
        if (idx > -1) {
            return username.substring(idx + 1);
        } else {
            return username;
        }
    }

}

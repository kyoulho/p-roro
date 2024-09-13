/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Hoon Oh       2월 08, 2022            First Draft.
 */
package io.playce.roro.discover.helper;

import com.google.gson.Gson;
import io.playce.roro.api.domain.insights.service.InsightsService;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1010;
import io.playce.roro.common.code.Domain1011;
import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.dto.common.ServerConnectionInfo;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.discover.server.util.ServerSummaryUtil;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.common.disk.Partition;
import io.playce.roro.svr.asmt.dto.common.interfaces.InterfaceInfo;
import io.playce.roro.svr.asmt.dto.common.interfaces.Ipv4Address;
import io.playce.roro.svr.asmt.dto.common.interfaces.Ipv6Address;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import io.playce.roro.svr.asmt.dto.result.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Pattern;

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
@Component
@RequiredArgsConstructor
@Transactional
public class ServerSummaryHelper {

    private final InventoryMasterRepository inventoryMasterRepository;
    private final ServerSummaryRepository serverSummaryRepository;
    private final ServerNetworkInformationRepository serverNetworkInformationRepository;
    private final ServerDiskInformationRepository serverDiskInformationRepository;
    private final ServerDaemonRepository serverDaemonRepository;
    private final ServerStatusRepository serverStatusRepository;
    private final ServerStorageRepository serverStorageRepository;
    private final BackupDeviceRepository backupDeviceRepository;
    private final InsightsService insightsService;

    private final Gson gson;

    public void addServerSummaryInfo(InventoryProcessConnectionInfo connectionInfo, ServerAssessmentResult result) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findById(connectionInfo.getInventoryId()).orElse(null);
        String osFamily = ServerSummaryUtil.getOsFamily(result);

        String edition = null;
        if (StringUtils.isNotEmpty(osFamily)) {
            osFamily = osFamily.toLowerCase();

            if (osFamily.contains("microsoft")) {
                inventoryMaster.setInventoryDetailTypeCode(Domain1013.WINDOWS.name());
            } else if (osFamily.contains("aix")) {
                inventoryMaster.setInventoryDetailTypeCode(Domain1013.AIX.name());
                edition = getEdition(Domain1013.AIX, connectionInfo);
            } else if (osFamily.contains("hp")) {
                inventoryMaster.setInventoryDetailTypeCode(Domain1013.HP_UX.name());
                edition = getEdition(Domain1013.HP_UX, connectionInfo);
            } else if (osFamily.contains("sun")) {
                inventoryMaster.setInventoryDetailTypeCode(Domain1013.SUNOS.name());
            } else {
                inventoryMaster.setInventoryDetailTypeCode(Domain1013.LINUX.name());

                if (osFamily.contains("fedora") || osFamily.contains("rhel")) {
                    if (result.getDistributionRelease().contains("Red Hat")) {
                        edition = getEdition(Domain1013.LINUX, connectionInfo);
                    }
                }
            }

            inventoryMasterRepository.save(inventoryMaster);
        }

        ServerSummary serverSummary = new ServerSummary();
        serverSummary.setServerInventoryId(connectionInfo.getInventoryId());
        serverSummary.setCpuModel(ServerSummaryUtil.getCpuModel(result));
        serverSummary.setCpuArchitecture(ServerSummaryUtil.getCpuArchitecture(result));
        serverSummary.setCpuCoreCount(ServerSummaryUtil.getCpuCoreCount(result));
        serverSummary.setCpuSocketCount(ServerSummaryUtil.getCpuSocketCount(result));
        if (serverSummary.getCpuCoreCount() != null && serverSummary.getCpuSocketCount() != null) {
            serverSummary.setCpuCount(serverSummary.getCpuCoreCount() * serverSummary.getCpuSocketCount());
        }
        serverSummary.setOsFamily(ServerSummaryUtil.getOsFamily(result));
        serverSummary.setOsKernel(ServerSummaryUtil.getOsKernel(result));
        serverSummary.setOsName(ServerSummaryUtil.getOsName(result));
        serverSummary.setVendorName(ServerSummaryUtil.getVendorName(result));
        serverSummary.setMemSize(ServerSummaryUtil.getMemSize(result));
        serverSummary.setSwapSize(ServerSummaryUtil.getSwapSize(result));
        serverSummary.setUserGroupConfigJson(ServerSummaryUtil.getUserGroupConfigJson(gson, result));
        serverSummary.setHostName(StringUtils.defaultString(result.getHostname()));

        if (osFamily.contains("microsoft")) {
            serverSummary.setOsAlias("Windows");
            serverSummary.setOsVersion(getWindowsVersion(serverSummary));
        } else {
            if (serverSummary.getOsName().contains("Red Hat")) {
                serverSummary.setOsAlias("RHEL");
            } else if (serverSummary.getOsName().contains("Oracle")) {
                serverSummary.setOsAlias("Oracle Linux");
            } else if (serverSummary.getOsName().contains("Rocky")) {
                serverSummary.setOsAlias("Rocky Linux");
            } else if (serverSummary.getOsName().contains("Sun")) {
                serverSummary.setOsAlias("Solaris");
            } else {
                serverSummary.setOsAlias(serverSummary.getOsName().split(" ")[0]);
            }
            serverSummary.setOsVersion(getUnixAndLinuxVersion(serverSummary.getOsName(), edition));

            if (result.getDistributionRelease().contains("Red Hat") && StringUtils.isNotEmpty(edition)) {
                String version = getUnixAndLinuxVersion(serverSummary.getOsName(), null);

                if (StringUtils.isNotEmpty(version)) {
                    version = version.split("\\.")[0];
                    serverSummary.setOsVersion(version + StringUtils.SPACE + edition);
                }
            }
        }

        serverSummaryRepository.save(serverSummary);
        log.debug("Update server summary - [{}]", serverSummary);

        // [PCR-6017] Insights - Product Lifecycle 처리를 위한 OS 정보 저장
        if (StringUtils.isNotEmpty(serverSummary.getOsAlias()) && StringUtils.isNotEmpty(serverSummary.getOsVersion())) {
            insightsService.createInventoryLifecycleVersionLink(connectionInfo.getInventoryId(), Domain1001.SVR, serverSummary.getOsAlias(), serverSummary.getOsVersion(), null, null);
        } else {
            log.debug("Not Found Server Name : {}, Server Version: {}", serverSummary.getOsAlias(), serverSummary.getOsVersion());
        }
    }

    public void deleteAllServerInformation(InventoryProcessConnectionInfo connectionInfo) {
        serverSummaryRepository.deleteByServerInventoryId(connectionInfo.getInventoryId());
        serverNetworkInformationRepository.deleteByServerInventoryId(connectionInfo.getInventoryId());
        serverDiskInformationRepository.deleteByServerInventoryId(connectionInfo.getInventoryId());
        serverDaemonRepository.deleteByServerInventoryId(connectionInfo.getInventoryId());
    }

    public void deleteAllServerStatus(InventoryProcessConnectionInfo connectionInfo) {
        serverStatusRepository.deleteByServerInventoryId(connectionInfo.getInventoryId());
        serverStorageRepository.deleteByServerInventoryId(connectionInfo.getInventoryId());
        backupDeviceRepository.deleteByServerInventoryId(connectionInfo.getInventoryId());
    }

    public void addServerNetworkInfo(InventoryProcessConnectionInfo connectionInfo, ServerAssessmentResult result) {
        log.debug("[InventoryId-{}] inventory update network information", connectionInfo.getInventoryId());
        Map<String, InterfaceInfo> interfaceInfoMap = result.getInterfaces();

        List<ServerNetworkInformation> infos = getServerNetworkInfos(connectionInfo.getInventoryId());

        for (InterfaceInfo interfaceInfo : interfaceInfoMap.values()) {

            TreeSet<String> ipSet = getUniqIps(interfaceInfo);

            for (String ip : ipSet) {
                if (existIp(infos, ip)) {
                    infos.add(addServerNetworkInfo(connectionInfo, interfaceInfo, ip));
                }
            }
        }
    }

    private ServerNetworkInformation addServerNetworkInfo(InventoryProcessConnectionInfo connectionInfo, InterfaceInfo interfaceInfo, String ip) {
        log.debug("[{}] inventory add ip {}", connectionInfo.getInventoryId(), ip);
        ServerNetworkInformation information = new ServerNetworkInformation();
        information.setServerInventoryId(connectionInfo.getInventoryId());
        information.setInterfaceName(StringUtils.defaultString(interfaceInfo.getDevice()));
        information.setGateway(StringUtils.defaultString(interfaceInfo.getGateway()));
        information.setNetworkScript(StringUtils.defaultString(interfaceInfo.getScript()));
        information.setMacAddress(interfaceInfo.getMacaddress());
        information.setAddress(StringUtils.defaultString(ip));

        serverNetworkInformationRepository.save(information);

        return information;
    }

    private TreeSet<String> getUniqIps(InterfaceInfo info) {
        TreeSet<String> ipSet = new TreeSet<>();

        for (Ipv4Address ipv4 : info.getIpv4()) {
            ipSet.add(ipv4.getAddress());
        }

        for (Ipv6Address ipv6 : info.getIpv6()) {
            ipSet.add(ipv6.getAddress());
        }

        return ipSet;
    }

    private List<ServerNetworkInformation> getServerNetworkInfos(Long inventoryId) {
        return serverNetworkInformationRepository.findByServerInventoryId(inventoryId);
    }

    private boolean existIp(List<ServerNetworkInformation> ips, String ipAddress) {
        return ips.stream().noneMatch(i -> i.getAddress().equals(ipAddress));
    }


    public void addServerDiskInfo(InventoryProcessConnectionInfo connectionInfo, ServerAssessmentResult result) {
        log.debug("[InventoryId-{}] inventory update disk information", connectionInfo.getInventoryId());
        Map<String, Partition> partitionMap = result.getPartitions();

        for (String key : partitionMap.keySet()) {
            Partition partition = partitionMap.get(key);

            ServerDiskInformation information = new ServerDiskInformation();
            information.setServerInventoryId(connectionInfo.getInventoryId());
            information.setDeviceName(StringUtils.defaultString(partition.getDevice()));
            information.setFreeSize(Double.parseDouble(StringUtils.defaultString(partition.getFree(), "0")));
            information.setTotalSize(Double.parseDouble(StringUtils.defaultString(partition.getSize(), "0")));
            information.setFilesystemType(StringUtils.defaultString(partition.getFsType()));
            information.setMountPath(partition.getMountPath());

            serverDiskInformationRepository.save(information);
        }
    }

    public void addServerDaemonInfo(InventoryProcessConnectionInfo connectionInfo, ServerAssessmentResult result) {
        log.debug("[InventoryId-{}] inventory update daemon information", connectionInfo.getInventoryId());
        if (result instanceof RedHatAssessmentResult || result instanceof DebianAssessmentResult || result instanceof UbuntuAssessmentResult) {
            saveLinuxDaemon(connectionInfo.getInventoryId(), result.getDaemons());
        } else if (result instanceof AixAssessmentResult) {
            saveAixDaemon(connectionInfo.getInventoryId(), result.getDaemons());
        } else if (result instanceof SolarisAssessmentResult) {
            saveSolarisDaemon(connectionInfo.getInventoryId(), result.getDaemons());
        } else if (result instanceof HpuxAssessmentResult) {
            // hp-ux daemon list same command ps -ef
            saveHpuxDaemon(connectionInfo.getInventoryId(), result.getProcesses());
        } else if (result instanceof WindowsAssessmentResult) {
            saveWindowDaemon(connectionInfo.getInventoryId(), ((WindowsAssessmentResult) result).getWindowsResult().getServices());
        }
    }

    private void saveWindowDaemon(Long serverInventoryId, List<WindowsAssessmentResult.Service> services) {
        for (WindowsAssessmentResult.Service service : services) {
            List<ServerDaemon> serverDaemons = serverDaemonRepository.findByServerInventoryIdAndDaemonName(serverInventoryId, service.getName());

            if (serverDaemons == null || serverDaemons.size() == 0) {
                ServerDaemon serverDaemon = new ServerDaemon();
                serverDaemon.setServerInventoryId(serverInventoryId);
                serverDaemon.setDaemonName(service.getName());
                serverDaemon.setDaemonDescription(service.getDisplayName());

                if (StringUtils.isNotEmpty(service.getStartType())) {
                    Domain1010 startTypeCode = Domain1010.findBy(service.getStartType());
                    serverDaemon.setDaemonStartTypeCode(startTypeCode != null ? startTypeCode.name() : "");
                } else {
                    serverDaemon.setDaemonStartTypeCode("");
                }

                if (service.getStatus().equals("Running")) {
                    serverDaemon.setDaemonStatus(Domain1011.RUN.name());
                } else {
                    serverDaemon.setDaemonStatus(Domain1011.STOP.name());
                }

                serverDaemonRepository.save(serverDaemon);
            }
        }
    }

    private void saveHpuxDaemon(Long serverInventoryId, List<Process> processes) {
        for (Process daemon : processes) {
            List<ServerDaemon> serverDaemons = serverDaemonRepository.findByServerInventoryIdAndDaemonName(serverInventoryId, daemon.getName());

            if (serverDaemons == null || serverDaemons.size() == 0) {
                ServerDaemon serverDaemon = new ServerDaemon();
                serverDaemon.setServerInventoryId(serverInventoryId);
                serverDaemon.setDaemonName(daemon.getName());
                serverDaemon.setDaemonDescription("");

                //ToDo : Hpux 데몬 시작 유형 판단 필요.
                serverDaemon.setDaemonStartTypeCode("");
                serverDaemon.setDaemonStatus(Domain1011.RUN.name());

                serverDaemonRepository.save(serverDaemon);
            }
        }
    }

    private void saveSolarisDaemon(Long serverInventoryId, Map<String, Map<String, String>> damaons) {
        for (String daemon : damaons.keySet()) {
            List<ServerDaemon> serverDaemons = serverDaemonRepository.findByServerInventoryIdAndDaemonName(serverInventoryId, daemon);

            if (serverDaemons == null || serverDaemons.size() == 0) {
                ServerDaemon serverDaemon = new ServerDaemon();
                serverDaemon.setServerInventoryId(serverInventoryId);
                serverDaemon.setDaemonName(daemon);
                serverDaemon.setDaemonDescription("");
                String status = damaons.get(daemon).get("status");
                switch (status) {
                    case "online":
                    case "legacy_run":
                        serverDaemon.setDaemonStatus(Domain1011.RUN.name());
                        break;
                    case "disabled":
//                        serverDaemon.setDaemonStatus(Domain1011.STOP.name());
//                        serverDaemon.setDaemonStartTypeCode(Domain1010.DIS.name());
//                        break;
                    case "maintenance":
                    case "offline":
                    case "uninitialized":
                        serverDaemon.setDaemonStatus(Domain1011.STOP.name());
//                        serverDaemon.setDaemonStartTypeCode(Domain1010.MAN.name());
                        break;
                    default:
                        serverDaemon.setDaemonStatus(Domain1011.NONE.name());
                }

                serverDaemonRepository.save(serverDaemon);
            }
        }
    }

    private void saveAixDaemon(Long serverInventoryId, Map<String, Map<String, String>> damaons) {
        for (String daemon : damaons.keySet()) {
            List<ServerDaemon> serverDaemons = serverDaemonRepository.findByServerInventoryIdAndDaemonName(serverInventoryId, daemon);

            /**
             *
             * Command result : (lssrc -a)
             *
             * Subsystem         Group            PID          Status
             *  platform_agent                    3997828      active
             *  policyd          qos                           inoperative
             * */
            if (serverDaemons == null || serverDaemons.size() == 0) {
                ServerDaemon serverDaemon = new ServerDaemon();
                serverDaemon.setServerInventoryId(serverInventoryId);
                serverDaemon.setDaemonName(daemon);
                serverDaemon.setDaemonDescription("");

                //ToDo : AIX 데몬 시작 유형 판단 필요.
                serverDaemon.setDaemonStartTypeCode("");

                if (damaons.get(daemon).get("status").equals("active")) {
                    serverDaemon.setDaemonStatus(Domain1011.RUN.name());
                } else {
                    serverDaemon.setDaemonStatus(Domain1011.STOP.name());
                }

                serverDaemonRepository.save(serverDaemon);
            }
        }
    }


    private void saveLinuxDaemon(Long serverInventoryId, Map<String, Map<String, String>> damaons) {
        for (String daemon : damaons.keySet()) {

            List<ServerDaemon> serverDaemons = serverDaemonRepository.findByServerInventoryIdAndDaemonName(serverInventoryId, daemon);

            /**
             *   UNIT                             LOAD   ACTIVE SUB     DESCRIPTION
             *   abrt-ccpp.service                loaded active exited  Install ABRT coredump hook
             *   abrt-oops.service                loaded active running ABRT kernel log watcher
             *   abrt-xorg.service                loaded active running ABRT Xorg log watcher
             *   ● tuned.service                    loaded failed failed  Dynamic System Tuning Daemon
             *
             * */
            if (serverDaemons == null || serverDaemons.size() == 0) {
                ServerDaemon serverDaemon = new ServerDaemon();
                serverDaemon.setServerInventoryId(serverInventoryId);
                serverDaemon.setDaemonName(daemon);
                serverDaemon.setDaemonDescription(damaons.get(daemon).getOrDefault("description", ""));

                //ToDo : 리눅스 데몬 시작 유형 판단 필요.
                serverDaemon.setDaemonStartTypeCode("");

                if (damaons.get(daemon).get("active").equals("active")) {
                    serverDaemon.setDaemonStatus(Domain1011.RUN.name());
                } else {
                    serverDaemon.setDaemonStatus(Domain1011.STOP.name());
                }

                serverDaemonRepository.save(serverDaemon);
            }
        }
    }

    private String getWindowsVersion(ServerSummary serverSummary) {
        StringBuilder osVersion = new StringBuilder();

        if (StringUtils.isNotEmpty(serverSummary.getOsName())) {
            StringTokenizer st = new StringTokenizer(serverSummary.getOsName(), " ");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.matches("[0-9]+")) {
                    osVersion.append(token);
                }
                if (token.equals("R2")) {
                    osVersion.append(" ");
                    osVersion.append(token);
                }
            }
        }


        if (StringUtils.isNotEmpty(serverSummary.getOsKernel()) && serverSummary.getOsKernel().contains("Service Pack")) {
            String[] sp = serverSummary.getOsKernel().split(" ");
            osVersion.append(" SP");
            osVersion.append(sp[3]);
        }

        return osVersion.toString();
    }

    private String getUnixAndLinuxVersion(String osName, String edition) {
        if (StringUtils.isEmpty(osName)) {
            return "";
        }

        String version = "";

        // ex) 0.1.2
        Pattern p1 = Pattern.compile("^([1-9]\\d*|0)(\\.(([1-9]\\d*)|0)){2}$");
        // ex) 1.1.5.0 or just 1.2
        Pattern p2 = Pattern.compile("^([1-9]\\d*|0)(\\.(([1-9]\\d*)|0)){0,3}$");

        StringTokenizer st = new StringTokenizer(osName, " ");

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (p1.matcher(token).find() || p2.matcher(token).find()) {
                version = token;
            }
        }

        if (osName.startsWith("HP-UX")) {
            if (osName.contains("11.11")) {
                version = "11i v1";
            } else if (osName.contains("11.23")) {
                version = "11i v2";
            } else if (osName.contains("11.31")) {
                version = "11i v3";
            }
        } else if (osName.startsWith("Sun")) {
            version = version.replaceAll("5.", StringUtils.EMPTY);
        } else if (osName.startsWith("Ubuntu")) {
            version = osName.replaceAll("Ubuntu", StringUtils.EMPTY).trim();
        }

        if (StringUtils.isNotEmpty(edition)) {
            return version + StringUtils.SPACE + edition;
        } else {
            return version;
        }
    }

    private String getEdition(Domain1013 type, InventoryProcessConnectionInfo connectionInfo) {
        String result, edition = null;

        try {
            TargetHost targetHost = ServerConnectionInfo.targetHost(connectionInfo);
            if (Domain1013.AIX.equals(type)) {
                result = SSHUtil.executeCommand(targetHost, "chedition -l");

                if (result.toLowerCase().contains("express")) {
                    edition = "Express Edition";
                } else if (result.toLowerCase().contains("enterprise")) {
                    edition = "Enterprise Edition";
                } else {
                    edition = "Standard Edition";
                }
            } else if (Domain1013.HP_UX.equals(type)) {
                result = SSHUtil.executeCommand(targetHost, "uname -a");

                if (result.toLowerCase().contains("ia64")) {
                    edition = "Integrity";
                } else {
                    edition = "HP 9000";
                }
            } else if (Domain1013.LINUX.equals(type)) {
                result = SSHUtil.executeCommand(targetHost, "uname -a");

                if (result.toLowerCase().contains("aarch64") || result.toLowerCase().contains("arm64")) {
                    edition = "ARM";
                } else if (result.toLowerCase().contains("ppc64") || result.toLowerCase().contains("power")) {
                    edition = "POWER";
                } else if (result.toLowerCase().contains("s390x") || result.toLowerCase().contains("ibm z")) {
                    edition = "System Z";
                }
            }
        } catch (InterruptedException e) {
            // ignore
        }

        return edition;
    }

}
//end of ServerSummaryHelper.java
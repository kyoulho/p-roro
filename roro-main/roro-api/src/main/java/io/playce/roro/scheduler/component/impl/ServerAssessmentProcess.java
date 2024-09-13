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
 * Dong-Heon Han    Jan 06, 2022		First Draft.
 */

package io.playce.roro.scheduler.component.impl;

import io.playce.roro.api.collector.HostStatCollector;
import io.playce.roro.api.domain.assessment.service.AssessmentService;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.api.domain.tracking.TrackingInfoService;
import io.playce.roro.asmt.windows.command.PowerShellCommonCommand;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.config.RoRoProperties;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.dto.info.LinuxInfo;
import io.playce.roro.common.dto.info.OSInfo;
import io.playce.roro.common.dto.inventory.middleware.MiddlewareResponse;
import io.playce.roro.common.dto.inventory.process.InventoryProcessDetailResponse;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.dto.publicagency.PublicAgencyReportDto;
import io.playce.roro.common.dto.thirdparty.ThirdPartySearchTypeResponse;
import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.common.exception.NotsupportedException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.SystemInfoUtil;
import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.discover.DiscoverResourceManager;
import io.playce.roro.discover.helper.ServerSummaryHelper;
import io.playce.roro.discover.server.util.ServerSummaryUtil;
import io.playce.roro.jpa.entity.BackupDevice;
import io.playce.roro.jpa.entity.InventoryProcessGroup;
import io.playce.roro.jpa.entity.ServerStatus;
import io.playce.roro.jpa.entity.ServerStorage;
import io.playce.roro.jpa.repository.BackupDeviceRepository;
import io.playce.roro.jpa.repository.ServerStatusRepository;
import io.playce.roro.jpa.repository.ServerStorageRepository;
import io.playce.roro.mybatis.domain.inventory.middleware.MiddlewareMapper;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import io.playce.roro.mybatis.domain.thirdparty.ThirdPartyMapper;
import io.playce.roro.scheduler.component.AbstractAssessmentProcess;
import io.playce.roro.scheduler.service.impl.AssessmentSchedulerManager;
import io.playce.roro.svr.asmt.ServerAssessment;
import io.playce.roro.svr.asmt.config.DistributionConfig;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServerAssessmentProcess extends AbstractAssessmentProcess {
    private final ServerMapper serverMapper;
    private final Map<String, ServerAssessment> serverAssessmentMap;
    private final DistributionConfig distributionConfig;
    private final InventoryProcessService inventoryProcessService;
    private final ServerService serverService;
    private final ThirdPartyMapper thirdPartyMapper;

    private final ServerSummaryHelper serverSummaryHelper;
    private final DiscoverResourceManager discoverResourceManager;

    private final ModelMapper modelMapper;
    private final ServerStatusRepository serverStatusRepository;
    private final ServerStorageRepository serverStorageRepository;
    private final BackupDeviceRepository backupDeviceRepository;
    private final TrackingInfoService trackingInfoService;

    private final AssessmentService assessmentService;
    private final MiddlewareMapper middlewareMapper;
    private final InventoryProcessMapper inventoryProcessMapper;
    private final RoRoProperties roRoProperties;

    @Override
    public Domain1003 assessment(InventoryProcessQueueItem item, Domain1003 resultState) throws InterruptedException {
        Long inventoryProcessId = item.getInventoryProcessId();
        ServerAssessmentResult result = null;
        String resultString = null;
        boolean saveReport = false;

        InventoryProcessConnectionInfo connectionInfo;
        TargetHost targetHost;
        try {
            connectionInfo = serverMapper.selectServerConnectionInfoByInventoryProcessId(item.getInventoryProcessId());
            log.debug("Step 4 ~ id: {}. load connection info: {}", inventoryProcessId, connectionInfo);

            targetHost = InventoryProcessConnectionInfo.targetHost(connectionInfo);
            if (!targetHost.isValid()) {
                throw new InsufficientException("Insufficient server connection information.");
            }

            ServerAssessment assessment = getComponent(targetHost, item);
            if (assessment == null) {
                // SUSELinux(SLESAssessment)와 같이 컴포넌트 조차 찾을 수 없는 경우..
                // throw new RoRoException("The processing component does not exist.");
                throw new NotsupportedException("Scan cannot be performed. It is not supported OS.");
            }

            List<ThirdPartySearchTypeResponse> thirdPartySearchTypeList = thirdPartyMapper.selectThirdPartySearchType();
            targetHost.setThirdPartySearchTypeList(thirdPartySearchTypeList);

            result = assessment.assessment(targetHost);

            // Scan이 정상적으로 완료된 경우 상태에 관계없이 Report 생성 대상이 된다.
            saveReport = true;

            // Unsupported OS version 체크
            if (!checkUnsupportedOS(ServerSummaryUtil.getOsName(result))) {
                // 지원되지 않는 버전의 OS 이지만 정상 수행 가능성이 있기 때문에 Exception을 throw 하지 않고 데이터 누락이 있을 수 있다는 메시지만 추가한다.
                // resultState = Domain1003.UNS;
                resultString = "Not tested OS version, some information may be missing.";
            }

            // https://cloud-osci.atlassian.net/browse/PCR-6304
            // Windows를 제외한 Linux / Unix에 sar 관련 명령 결과가 있다면 해당 값으로 저장하고, 명령이 실패하거나 결과가 없다면 HostStatCollector를 수행한다.
            if (!hasSarCommand(item.getInventoryDetailTypeCode(), targetHost, result)) {
                // Start 5 minutes monitoring for cpu & memory usage
                // Scan 시작 시 같이 실행을 하면 CPU가 높게 나올 수 있어 Scan이 완료된 후 진행한다.
                // Middleware가 발견되면서 Middleware 스캔과 동시에 실행되면 마찬가지로 CPU가 높게 나올 수 있다.
                new Thread(new HostStatCollector(item.getInventoryId(), item.getInventoryDetailTypeCode(), targetHost)).start();
            }

            /**
             * 서버 분석 후처리
             * Server Summary, Network info, Disk Info, Daemon Info
             **/
            synchronized (AssessmentSchedulerManager.lockSvr) {
                if (!InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
                    try {
                        runPostProcessing(connectionInfo, result);
                    } catch (Exception e) {
                        log.error("Unhandled exception occurred while execute server scan's post processing.", e);

                        // 후 처리(Middleware detect 등) 과정에서 에러가 발생한 경우도 Partially Completed로 처리한다.
                        resultState = Domain1003.PC;

                        if (StringUtils.isEmpty(resultString)) {
                            resultString = "Post processing failed. [Reason] " + e.getMessage();
                        } else {
                            resultString += "\nPost processing failed. [Reason] " + e.getMessage();
                        }
                    }
                }
            }

            // 관라자가 아닌 경우에도 Partially Completed로 처리한다.
            boolean isAdmin = false;

            if ("N".equals(item.getWindowsYn())) {
                if (targetHost.getUsername().equals("root") || SSHUtil.canExecuteCommandWithSu(targetHost) || SSHUtil.isSudoer(targetHost)) {
                    isAdmin = true;
                }
            } else {
                String resultMessage = WinRmUtils.executePsShell(targetHost, PowerShellCommonCommand.CHECK_ADMINISTRATOR).trim();
                if (resultMessage.equalsIgnoreCase("true")) {
                    isAdmin = true;
                }
            }

            if (!isAdmin) {
                resultState = Domain1003.PC;

                if (StringUtils.isEmpty(resultString)) {
                    resultString = "User haven't administrator privileges.";
                } else {
                    resultString += "\nUser haven't administrator privileges.";
                }
            }

            // ErrorMap에 데이터가 있는 경우 Partially Completed로 처리한다.
            if (result.getErrorMap() != null && result.getErrorMap().size() > 0) {
                resultState = Domain1003.PC;

                if (StringUtils.isEmpty(resultString)) {
                    resultString = "An error occurred when executing some commands.";
                } else {
                    resultString += "\nAn error occurred when executing some commands.";
                }
            }

            if (!resultState.equals(Domain1003.PC)) {
                resultState = Domain1003.CMPL;
            }

            // https://cloud-osci.atlassian.net/browse/PCR-6485
            // 서버 스캔 완료 시 서버 하위 미들웨어에 대한 자동 스캔
            if (roRoProperties.isMiddlewareAutoScanAfterServerScan()) {
                try {
                    List<MiddlewareResponse> middlewareList = middlewareMapper.selectMiddlewareList(item.getProjectId(), null, item.getInventoryId(), Domain1001.MW.name());

                    InventoryProcessGroup inventoryProcessGroup = null;

                    InventoryProcessDetailResponse inventoryProcess = inventoryProcessMapper.getInventoryProcessDetail(item.getProjectId(), item.getInventoryProcessId());
                    if (inventoryProcess != null) {
                        inventoryProcessGroup = new InventoryProcessGroup();
                        inventoryProcessGroup.setInventoryProcessGroupId(inventoryProcess.getInventoryProcessGroupId());
                    }

                    for (MiddlewareResponse mw : middlewareList) {
                        assessmentService.createAssessment(inventoryProcessGroup, mw.getProjectId(), mw.getMiddlewareInventoryId());
                    }
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while create scan requests for middlewares in server. Reason : [{}]", e.getMessage());
                }
            }
        } catch (Throwable e) {
            ScanResult scanResult = getScanResult(e);

            if (scanResult != null) {
                resultState = scanResult.getResultState();
                resultString = scanResult.getResultString();
            }

            log.error("item {} - {}", item, resultString, e);
        } finally {
            synchronized (AssessmentSchedulerManager.lockSvr) {
                if (!InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
                    String message = saveResult(item, inventoryProcessService, inventoryProcessId, result, resultString, saveReport);

                    if (StringUtils.isNotEmpty(message)) {
                        if (resultState.equals(Domain1003.CMPL)) {
                            resultState = Domain1003.PC;
                        }
                    }
                }
            }
        }

        return resultState;
    }

    private void runPostProcessing(InventoryProcessConnectionInfo connectionInfo, ServerAssessmentResult result) {
        try {
            serverSummaryHelper.deleteAllServerInformation(connectionInfo);

            serverSummaryHelper.addServerSummaryInfo(connectionInfo, result);
            serverSummaryHelper.addServerNetworkInfo(connectionInfo, result);
            serverSummaryHelper.addServerDiskInfo(connectionInfo, result);
            serverSummaryHelper.addServerDaemonInfo(connectionInfo, result);

            ServerStatus originServerStatus = serverStatusRepository.findById(connectionInfo.getInventoryId()).orElse(null);
            ServerStatus serverStatus = null;
            if (result.getServerStatus() != null) {
                serverStatus = modelMapper.map(result.getServerStatus(), ServerStatus.class);
                serverStatus.setServerInventoryId(connectionInfo.getInventoryId());

                if (serverStatus.getCpuUsage() == null && serverStatus.getMemUsage() == null && originServerStatus != null) {
                    serverStatus.setCpuUsage(originServerStatus.getCpuUsage());
                    serverStatus.setMemUsage(originServerStatus.getMemUsage());
                    serverStatus.setMonitoringDatetime(originServerStatus.getMonitoringDatetime());
                }
            }

            serverSummaryHelper.deleteAllServerStatus(connectionInfo);

            if (serverStatus != null) {
                serverStatusRepository.save(serverStatus);
            }

            if (result.getStorageStatusList() != null) {
                for (PublicAgencyReportDto.StorageStatus storageStatus : result.getStorageStatusList()) {
                    ServerStorage serverStorage = modelMapper.map(storageStatus, ServerStorage.class);
                    serverStorage.setServerInventoryId(connectionInfo.getInventoryId());
                    serverStorageRepository.save(serverStorage);
                }
            }

            if (result.getBackupStatusList() != null) {
                for (PublicAgencyReportDto.BackupStatus backupStatus : result.getBackupStatusList()) {
                    BackupDevice backupDevice = modelMapper.map(backupStatus, BackupDevice.class);
                    backupDevice.setServerInventoryId(connectionInfo.getInventoryId());
                    backupDeviceRepository.save(backupDevice);
                }
            }

            discoverResourceManager.discover(connectionInfo, result);
            trackingInfoService.saveServerTrackingInfo(connectionInfo.getInventoryProcessId(), result);

        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
            throw new RuntimeException(connectionInfo.toString() + " unhandled error occurred during post processing with " + e.getMessage());
        }
    }

    private ServerAssessment getComponent(TargetHost targetHost, InventoryProcessQueueItem item) throws InterruptedException {
        OSInfo osInfo;

        if (item.getOsVersion() == null) {
            if ("Y".equals(item.getWindowsYn())) {
                osInfo = new OSInfo(Domain1013.WINDOWS);
            } else {
                osInfo = SystemInfoUtil.getOSInfo(targetHost);
                serverService.updateOsInfo(item, osInfo);
            }
        } else {
            osInfo = new OSInfo(Domain1013.valueOf(item.getInventoryDetailTypeCode()));
            osInfo.setOsVersion(item.getOsVersion());
        }

        String key1 = null;
        String key2 = null;
        switch (osInfo.getInventoryDetailTypeCode()) {
            case LINUX:
                LinuxInfo linuxInfo = SystemInfoUtil.getLinuxInfo(osInfo.getInventoryDetailTypeCode(), targetHost);

                String like = linuxInfo.getLike();
                String id = linuxInfo.getIdOnly();

                if (like == null || id == null) {
                    return serverAssessmentMap.get("REDHATAssessment");
                }

                String distribution = distributionConfig.getComponentName().get(like);
                key1 = StringUtils.isEmpty(distribution) ? StringUtils.isEmpty(id) ? like.toUpperCase() : id.toUpperCase() : distribution;
                key2 = linuxInfo.getVersionOnly();
                if (key2.length() > 1) {
                    key2 = key2.substring(0, 1);
                }
                break;
            case HP_UX:
                key1 = osInfo.getInventoryDetailTypeCode().prefix();
                key2 = osInfo.getOsVersion();
                break;
            case AIX:
            case SUNOS:
                key1 = osInfo.getInventoryDetailTypeCode().name();
                key2 = osInfo.getOsVersion();
                break;
            case WINDOWS:
                key1 = Domain1013.WINDOWS.name();
                key2 = StringUtils.EMPTY;
                break;
            default:
                log.error("TODO search component name: {}", osInfo);
        }

        String componentName = makeComponentName(key1, key2);
        ServerAssessment assessment = serverAssessmentMap.get(componentName);
        if (assessment == null) {
            componentName = makeComponentName(key1);
            assessment = serverAssessmentMap.get(componentName);
        }
        log.debug("exec component name: {}", componentName);
        return assessment;
    }

    private boolean checkUnsupportedOS(String osName) {
        boolean isSupported = false;

        if (StringUtils.isEmpty(osName)) {
            log.warn("Can't check unsupported os. osName is empty.");
            return false;
        }

        String version;

        // CentOS, RHEL, Oracle, Fedora, Amazon Linux
        if (osName.contains("release")) {
            int idx = osName.indexOf("release");
            version = osName.substring(idx + 8);

            if (osName.contains("CentOS") || osName.contains("Red Hat") || osName.contains("RHEL") || osName.contains("Oracle")) {
                if (version.length() == 1) {
                    version += ".0";
                }

                if (version.substring(0, 3).compareTo("6.1") >= 0) {
                    isSupported = true;
                }
            } else if (osName.contains("Rocky")) {
                if (version.length() == 1) {
                    version += ".0";
                }

                if (version.substring(0, 3).compareTo("8.3") >= 0) {
                    isSupported = true;
                }
            } else if (osName.contains("Fedora")) {
                if (version.substring(0, 2).compareTo("19") >= 0) {
                    isSupported = true;
                }
            }
        } else if (osName.contains("Ubuntu") || osName.contains("Debian")) {
            version = osName.replaceAll("Ubuntu ", "")
                    .replaceAll("Debian ", "")
                    .replaceAll("GNU/Linux ", "");

            if (osName.contains("Ubuntu") && version.substring(0, 5).compareTo("14.04") >= 0) {
                isSupported = true;
            }
            if (osName.contains("Debian") && version.substring(0, 3).compareTo("6.0") >= 0) {
                isSupported = true;
            }
        } else if (osName.contains("AIX")) {
            version = osName.replaceAll("AIX ", "");

            if (version.substring(0, 3).compareTo("5.3") >= 0) {
                isSupported = true;
            }
        } else if (osName.contains("SunOS")) {
            version = osName.replaceAll("SunOS ", "");

            if (version.substring(0, 4).compareTo("5.10") >= 0) {
                isSupported = true;
            }
        } else if (osName.contains("HP-UX")) {
            version = osName.replaceAll("HP-UX ", "");

            if (version.substring(0, 5).compareTo("11.23") >= 0) {
                isSupported = true;
            }
        } else if (osName.contains("Windows")) {
            // Windows는 별도의 버전 체크를 하지 않음. (Prerequisite 를 통과하지 못함)
            isSupported = true;
        }

        return isSupported;
    }

    private boolean hasSarCommand(String inventoryDetailTypeCode, TargetHost targetHost, ServerAssessmentResult result) {
        String cpu = null, mem = null;
        Domain1013 os = null;

        try {
            if (StringUtils.isEmpty(inventoryDetailTypeCode)) {
                OSInfo osInfo = SystemInfoUtil.getOSInfo(targetHost);
                os = osInfo.getInventoryDetailTypeCode();
            } else {
                os = Domain1013.valueOf(inventoryDetailTypeCode);
            }
        } catch (Exception e) {
            log.warn("Unable to check server type. Reason : [{}]", e.getMessage());
        }

        if (os != null && !os.equals(Domain1013.WINDOWS)) {
            try {
                cpu = SSHUtil.executeCommand(targetHost, "sar -u | grep -i average | awk '{print 100 - $8}'");
                mem = SSHUtil.executeCommand(targetHost, "sar -r | grep -i average | awk '{print $4}'");
            } catch (Exception e) {
                // ignore
            }
        }

        if (StringUtils.isNotEmpty(cpu) && StringUtils.isNotEmpty(mem)) {
            try {
                PublicAgencyReportDto.ServerStatus serverStatus = result.getServerStatus();
                if (serverStatus != null) {
                    serverStatus.setCpuUsage(Double.parseDouble(cpu));
                    serverStatus.setMemUsage(Double.parseDouble(mem));
                    serverStatus.setMonitoringDatetime(new Date());

                    return true;
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return false;
    }
}
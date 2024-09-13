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
package io.playce.roro.svr.asmt.redhat.impl;

import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.svr.asmt.AssessmentItems;
import io.playce.roro.svr.asmt.config.DistributionConfig;
import io.playce.roro.svr.asmt.dto.Distribution;
import io.playce.roro.svr.asmt.dto.redhat.LogicalVolume;
import io.playce.roro.svr.asmt.dto.redhat.PhysicalVolume;
import io.playce.roro.svr.asmt.dto.redhat.VolumeGroup;
import io.playce.roro.svr.asmt.dto.result.RedHatAssessmentResult;
import io.playce.roro.svr.asmt.linux.LinuxServerAssessment;
import io.playce.roro.svr.asmt.redhat.RhelAsmtCommand;
import io.playce.roro.svr.asmt.redhat.util.DiskParserUtil;
import io.playce.roro.svr.asmt.util.DistributionChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
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

@Component("REDHATAssessment")
@RequiredArgsConstructor
@Slf4j
public class RedHatServerAssessment extends LinuxServerAssessment {
    private final DistributionConfig config;

    protected Map<String, VolumeGroup> getLvmInfo(TargetHost targetHost) throws InterruptedException {
        Map<String, VolumeGroup> vgs = new HashMap<>();
        try {
            parseVolumeGroup(targetHost, vgs);
            parseLogicalVolume(targetHost, vgs);
            parsePhysicalVolume(targetHost, vgs);
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
        }
        return vgs;
    }

    protected void parseVolumeGroup(TargetHost targetHost, Map<String, VolumeGroup> vgs) throws InterruptedException {
        String result = SSHUtil.executeCommand(targetHost, RhelAsmtCommand.LVM_VGS);

        if (StringUtils.isNotEmpty(result)) {
            String vgsName;
            for (String line : result.split("\n")) {
                vgsName = line.strip().split("\\s+")[0];
                VolumeGroup vg = new VolumeGroup();
                vgs.put(vgsName, vg);
            }
        }
    }

    protected void parsePhysicalVolume(TargetHost targetHost, Map<String, VolumeGroup> vgs) throws InterruptedException {

        String result = SSHUtil.executeCommand(targetHost, RhelAsmtCommand.LVM_PVS);

        if (StringUtils.isNotEmpty(result)) {
            Pattern p = Pattern.compile("(-+\\s\\w+\\s\\w+\\s+-+)");
            PhysicalVolume pvInfo = null;
            for (String line : result.split("\n")) {
                line = line.strip();

                Matcher m = p.matcher(line);
                if (m.find()) {
                    pvInfo = new PhysicalVolume();
                }

                DiskParserUtil.parsePhysicalVolumeDetail(vgs, pvInfo, line);
            }
        }
    }

    protected void parseLogicalVolume(TargetHost targetHost, Map<String, VolumeGroup> vgs) throws InterruptedException {
        String result = SSHUtil.executeCommand(targetHost, RhelAsmtCommand.LVM_LVS);

        if (StringUtils.isNotEmpty(result)) {
            Pattern p = Pattern.compile("(-+\\s\\w+\\s\\w+\\s+-+)");
            LogicalVolume lvInfo = null;
            for (String line : result.split("\n")) {
                line = line.strip();

                Matcher m = p.matcher(line);
                if (m.find()) {
                    lvInfo = new LogicalVolume();
                }

                DiskParserUtil.parseLogicalVolumeDetail(vgs, lvInfo, line);
            }
        }
    }

    @Override
    public Map<String, String> generateCommand() {
        Map<String, String> generateCommand = super.generateCommand();
        generateCommand.put(AssessmentItems.PACKAGES.toString(), RhelAsmtCommand.PACKAGES);
        return generateCommand;
    }

    @Override
    public RedHatAssessmentResult assessment(TargetHost targetHost) throws InterruptedException {
        Map<String, String> cmdMap = generateCommand();
        Map<String, RemoteExecResult> resultMap = runCommands(targetHost, cmdMap);
        Map<String, String> errorMap = new HashMap<>();

        boolean sudo = SSHUtil.isSudoer(targetHost);
        Distribution distribution = DistributionChecker.getDistribution(config, targetHost, sudo);

        return RedHatAssessmentResult.builder()
                .distribution(distribution.getDistribution())
                .distributionRelease(distribution.getDistributionRelease())
                .family(distribution.getOsFamily())
                .architecture(getArchitecture(getResult(AssessmentItems.ARCHITECTURE, resultMap), errorMap))
                .biosVersion(getBiosVersion(targetHost, errorMap))
                .cpu(getCpuInfo(targetHost, getResult(AssessmentItems.CPU_FACTS, resultMap), errorMap))
                .crontabs(
                        getCronTabs(
                                targetHost,
                                getResult(AssessmentItems.CRONTAB1, resultMap),
                                getResult(AssessmentItems.CRONTAB2, resultMap),
                                errorMap
                        )
                )
                .defInfo(getLoginDef(getResult(AssessmentItems.LOGIN_DEF, resultMap), errorMap))
                .interfaces(
                        getInterfacesInfo(
                                targetHost,
                                getResult(AssessmentItems.INTERFACES, resultMap),
                                getResult(AssessmentItems.INTERFACES_DEFAULT_GATEWAY, resultMap),
                                errorMap
                        )
                )
                .dns(getDns(getResult(AssessmentItems.DNS, resultMap), errorMap))
                .hostname(getHostname(getResult(AssessmentItems.HOSTNAME, resultMap), errorMap))
                .fsTabs(getFstabInfo(getResult(AssessmentItems.FSTAB, resultMap), errorMap))
                .groups(getGroups(getResult(AssessmentItems.GROUPS, resultMap), errorMap))
                .hosts(getHosts(getResult(AssessmentItems.HOSTS, resultMap), errorMap))
                .kernel(getKernel(getResult(AssessmentItems.KERNEL, resultMap), errorMap))
                .kernelParameters(getKernelParams(getResult(AssessmentItems.KERNEL_PARAM, resultMap), errorMap))
                .portList(
                        parseListenPort(
                                targetHost,
                                getResult(AssessmentItems.NET_LISTEN_PORT, resultMap),
                                getResult(AssessmentItems.NET_TRAFFICS, resultMap),
                                errorMap
                        )
                )
                .processes(getPsInfo(getResult(AssessmentItems.PROCESSES, resultMap), errorMap))
                .partitions(getDf(getResult(AssessmentItems.PARTITIONS, resultMap), errorMap))
                .shadows(getPasswordUsers(getResult(AssessmentItems.SHADOWS, resultMap), errorMap))
                .locale(getLocale(getResult(AssessmentItems.LOCALE, resultMap), errorMap))
                .productName(getProductName(targetHost, errorMap))
                .productSerial(getProductSerial(targetHost, errorMap))
                .routeTables(getRouteTable(getResult(AssessmentItems.ROUTE_TABLE, resultMap), errorMap))
                .systemVendor(getSystemVendor(targetHost, errorMap))
                .timezone(getTimezone(
                        targetHost,
                        getResult(AssessmentItems.TIMEZONE1, resultMap),
                        getResult(AssessmentItems.TIMEZONE1, resultMap),
                        errorMap))
                .memory(getMemoryInfo(getResult(AssessmentItems.MEMORY_FACTS, resultMap), errorMap))
                .vgs(getLvmInfo(targetHost))
                // .security(getSecurity())
                .firewall(
                        getFirewall(
                                getResult(AssessmentItems.FIREWALL_RULE, resultMap),
                                getResult(AssessmentItems.FIREWALL_EXTRA_RULE, resultMap),
                                errorMap))
                .ulimits(getUlimits(targetHost, getResult(AssessmentItems.USER_LIST, resultMap), errorMap))
                .users(getUsers(targetHost, getResult(AssessmentItems.USERS, resultMap), errorMap, sudo))
                .env(getEnv(getResult(AssessmentItems.ENV, resultMap), errorMap))
                // .daemons(getDaemons(targetHost, getResult(AssessmentItems.DAEMON_LIST, resultMap), errorMap))
                .daemons(getDaemons(targetHost, getResult2(AssessmentItems.DAEMON_LIST, AssessmentItems.DAEMON_LIST_LOWDER_7, resultMap), errorMap))
                .uptime(getUptime(getResult(AssessmentItems.UPTIME, resultMap), errorMap))
                .thirdPartySolutions(getThirdPartySolutions(targetHost, resultMap, this.getClass().getName()))
                .serverStatus(getServerStatus(targetHost))
                .storageStatusList(getStorageStatusList(targetHost))
                .backupStatusList(getBackupStatusList(targetHost))
                .errorMap(errorMap)
                .packages(getPackage(getResult(AssessmentItems.PACKAGES, resultMap)))
                .build();
    }

}
//end of RedHatServerAssessment.java
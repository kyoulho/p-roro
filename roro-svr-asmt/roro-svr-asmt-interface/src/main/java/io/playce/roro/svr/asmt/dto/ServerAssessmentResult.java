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
 * Hoon Oh       11월 12, 2021            First Draft.
 */
package io.playce.roro.svr.asmt.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.playce.roro.common.dto.publicagency.PublicAgencyReportDto;
import io.playce.roro.common.dto.thirdparty.ThirdPartyDiscoveryResult;
import io.playce.roro.svr.asmt.dto.common.Package;
import io.playce.roro.svr.asmt.dto.common.config.Hosts;
import io.playce.roro.svr.asmt.dto.common.disk.FsTab;
import io.playce.roro.svr.asmt.dto.common.disk.Partition;
import io.playce.roro.svr.asmt.dto.common.hardware.CpuInfo;
import io.playce.roro.svr.asmt.dto.common.hardware.MemoryInfo;
import io.playce.roro.svr.asmt.dto.common.interfaces.InterfaceInfo;
import io.playce.roro.svr.asmt.dto.common.network.PortList;
import io.playce.roro.svr.asmt.dto.common.network.RouteTable;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import io.playce.roro.svr.asmt.dto.linux.security.DefInfo;
import io.playce.roro.svr.asmt.dto.linux.security.Firewall;
import io.playce.roro.svr.asmt.dto.user.Group;
import io.playce.roro.svr.asmt.dto.user.User;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Getter
@SuperBuilder(toBuilder = true)
@ToString
public class ServerAssessmentResult {

    protected String architecture;
    protected String biosVersion;
    protected String distribution;
    protected String distributionRelease;
    protected String family;
    protected String hostname;
    protected String kernel;
    protected Object uptime;
    protected String timezone;
    protected String systemVendor;
    protected String productName;
    protected String productSerial;

    protected DefInfo defInfo;
    protected MemoryInfo memory;
    protected CpuInfo cpu;
    protected PortList portList;
    protected Hosts hosts;
    protected Firewall firewall;
    protected List<Package> packages;

    protected List<String> dns;
    protected List<RouteTable> routeTables;
    protected List<Process> processes;
    protected List<FsTab> fsTabs;

    protected Map<String, String> env;
    protected Map<String, String> shadows;
    protected Map<String, String> crontabs;
    protected Map<String, String> locale;
    protected Map<String, Map<String, String>> ulimits;
    protected Map<String, Map<String, String>> daemons;

    protected Map<String, String> errorMap;
    protected Map<String, User> users;
    protected Map<String, Group> groups;
    protected Map<String, Partition> partitions;
    protected Map<String, InterfaceInfo> interfaces;

    protected List<ThirdPartyDiscoveryResult> thirdPartySolutions;

    @JsonIgnore
    protected PublicAgencyReportDto.ServerStatus serverStatus;
    @JsonIgnore
    protected List<PublicAgencyReportDto.StorageStatus> storageStatusList;
    @JsonIgnore
    protected List<PublicAgencyReportDto.BackupStatus> backupStatusList;

    public List<ThirdPartyDiscoveryResult> getThirdPartySolutions() {
        if (thirdPartySolutions == null) {
            thirdPartySolutions = new ArrayList<>();
        }

        return thirdPartySolutions;
    }

    public List<PublicAgencyReportDto.StorageStatus> getStorageStatusList() {
        if (storageStatusList == null) {
            storageStatusList = new ArrayList<>();
        }

        return storageStatusList;
    }

    public List<PublicAgencyReportDto.BackupStatus> getBackupStatusList() {
        if (backupStatusList == null) {
            backupStatusList = new ArrayList<>();
        }

        return backupStatusList;
    }
}
//end of ServerAssessmentResult.java
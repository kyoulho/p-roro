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
 * SangCheon Park   Mar 10, 2022		    First Draft.
 */
package io.playce.roro.common.dto.migration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.playce.roro.common.dto.migration.enums.StatusType;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.common.util.support.TargetHost;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Getter
@Setter
@ToString
public class MigrationProcessDto {

    private Long inventoryProcessId;
    private TargetHost targetHost;

    private String uname;
    private Long serverInventoryId;
    private Long migrationPreConfigId;
    private MigrationPreConfig migrationPreConfig;
    private Long credentialId;
    private Credential credential;
    private String region;
    private String availabilityZone;
    private String vpcId;
    private String subnetId;
    private String enableEipYn;
    private String privateIp;
    private String hostName;
    private String instanceType;
    private String gcpProjectId;
    private List<String> securityGroupIds;
    private List<String> firewalls;
    @Getter(AccessLevel.NONE)
    private List<String> excludeDirectories;
    private List<Volume> volumes;
    private List<Tag> tags;
    private List<String> networkTags;
    @Getter(AccessLevel.NONE)
    private Integer swapSize;
    private String vpcName;
    private String subnetName;
    private Date instanceLaunchTime;
    private List<String> securityGroupNames;
    @Getter(AccessLevel.NONE)
    private List<String> blockDevices;
    private String publicIp;
    private String instanceId;
    @Getter(AccessLevel.NONE)
    private String instanceName;
    private String imageId;
    private String imageName;
    private ServerSummary serverSummary;
    private String description;

    private Long diskSize = 0L;
    private Long usedSize = 0L;

    private Date startDate;
    private Long elapsedTime = 0L;

    @Setter(AccessLevel.NONE)
    private StatusType internalStatus = StatusType.READY;

    @Setter(AccessLevel.NONE)
    private Long lastStatusChanged;

    @Getter
    @Setter
    @ToString
    public static class Credential {
        private Long credentialId;
        private String credentialName;
        private String credentialTypeCode;
        private String accessKey;
        private String secretKey;
        private String keyFileName;
        private String keyFilePath;
    }

    @Getter
    @Setter
    @ToString
    public static class MigrationPreConfig {
        private Long migrationPreConfigId;
        private Long credentialId;
        private String configName;
        private Credential credential;
        private String region;
        private String gcpProjectId;
        private String imageId;
        private String connectIpAddress;
        private Integer connectSshPort;
        private String connectUserName;
        @Getter(AccessLevel.NONE)
        private String connectUserPassword;
        private String keyPair;
        private String pubKey;
        private String keyFileName;
        private String keyFilePath;
        private List<String> packages;
        private String initScript;
        private List<MigrationPreConfigUser> migrationPreConfigUsers;
        private List<MigrationPreConfigGroup> migrationPreConfigGroups;
        private List<MigrationPreConfigFile> migrationPreConfigFiles;

        public String getConnectUserPassword() {
            return GeneralCipherUtil.decrypt(connectUserPassword);
        }
    }

    @Getter
    @Setter
    @ToString
    public static class MigrationPreConfigUser {
        private Integer uid;
        private String userName;
        private String userPassword;
        private List<String> groups;
        private String homeDir;
        private String profile;
        private String crontab;
    }

    @Getter
    @Setter
    @ToString
    public static class MigrationPreConfigGroup {
        private Integer gid;
        private String groupName;
    }

    @Getter
    @Setter
    @ToString
    public static class MigrationPreConfigFile {
        private Long sequence;
        private String source;
        private String target;
        private String type;
        private Long size;
        private String ownerUser;
        private String ownerGroup;
    }

    @Getter
    @Setter
    @ToString
    public static class Volume {
        private Long migrationVolumeId;
        private String volumePath;
        private Long volumeSize;
        private String rawFileName;
        private Long rawFileSize;
        private String rootYn;
        private String volumeId;
        private String deviceName;
        private String manifestUrl;
        private String taskId;
    }

    @Getter
    @Setter
    @ToString
    public static class Tag {
        private Long migrationTagId;
        private String tagName;
        private String tagValue;
    }

    @Getter
    @Setter
    @ToString
    public static class ServerSummary {
        private String hostName;
        private String vendorName;
        private String cpuModel;
        private int cpuCount;
        private int cpuCoreCount;
        private int cpuSocketCount;
        private String cpuArchitecture;
        private String osKernel;
        private String osName;
        @Getter(AccessLevel.NONE)
        private String osFamily;
        private long memSize;
        private long swapSize;
        @JsonIgnore
        private List<DiskInfo> diskInfos;

        public String getOsFamily() {
            if (osFamily != null && (osFamily.toLowerCase().contains("rhel") || osFamily.toLowerCase().contains("redhat")
                    || osFamily.toLowerCase().contains("fedora") || osFamily.toLowerCase().contains("ol"))) {
                osFamily = "redhat";
            }

            if (osFamily != null && (osFamily.toLowerCase().contains("debian") || osFamily.toLowerCase().contains("ubuntu"))) {
                osFamily = "debian";
            }

            return osFamily;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class DiskInfo {
        private String deviceName;
        private String mountPath;
        private Long freeSize;
        private String filesystemType;
        private Long totalSize;
    }

    public void addElapsedTime() {
        if (startDate == null) {
            startDate = new Date();
        }

        long currentTime = System.currentTimeMillis();
        long startTime = startDate.getTime();

        elapsedTime = currentTime - startTime;
    }

    public void setInternalStatus(StatusType internalStatus) {
        addElapsedTime();

        if (this.internalStatus != internalStatus) {
            this.lastStatusChanged = System.currentTimeMillis();
        }

        this.internalStatus = internalStatus;
    }

    public String getInstanceName() {
        if (instanceName == null) {
            instanceName = hostName;
        }

        return instanceName;
    }

    public Integer getSwapSize() {
        if (swapSize == null) {
            swapSize = 0;
        }

        return swapSize;
    }

    public List<String> getExcludeDirectories() {
        if (excludeDirectories == null) {
            excludeDirectories = new ArrayList<>();
        }

        return excludeDirectories;
    }

    public List<String> getBlockDevices() {
        if (blockDevices == null) {
            blockDevices = new ArrayList<>();
        }

        return blockDevices;
    }
}
//end of MigrationProcessDto.java
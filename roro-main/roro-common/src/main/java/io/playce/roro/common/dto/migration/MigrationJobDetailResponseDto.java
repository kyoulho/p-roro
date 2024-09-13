package io.playce.roro.common.dto.migration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class MigrationJobDetailResponseDto {

    private Detail detail;
    private SourceServer source;
    private TargetServer target;


    @Getter
    @Setter
    @ToString
    public static class Detail {

        private Long inventoryProcessId;
        private Long credentialId;
        private String migrationType;
        private String credentialTypeName;
        private String credentialTypeCode;
        private String credentialName;
        private String inventoryProcessResultCode;
        private String inventoryProcessResultTxt;
        private Double progress;
        private Integer estimateTime;
        private Long elapsedTime;
        private Date inventoryProcessStartDatetime;
        private Date inventoryProcessEndDatetime;
        private String registUserId;
        private String modifyUserId;
        private Date registDatetime;
        private Date modifyDatetime;

    }

    @Getter
    @Setter
    @ToString
    public static class SourceServer {

        private Long serverInventoryId;
        private String serverInventoryName;
        private String inventoryDetailTypeCode;
        private String serverIp;
        private String osName;
        private String cpuModel;
        private String memorySize;
        private String swapSize;
        private String osKernel;
        private String osFamily;
        private String vendorName;
        private String cpuArchitecture;
        private Long preConfigId;
        private String preConfigDeleteYn;
        private String preConfigName;
        private String preConfigImageId;

    }

    @Getter
    @Setter
    @ToString
    public static class TargetServer {

        private String instanceId;
        private String instanceName;
        private String publicIp;
        private String privateIp;
        private String gcpProjectId;
        private String instanceType;
        private String availabilityZone;
        private String vpcId;
        private String vpcName;
        private String subnetId;
        private String subnetName;
        private List<String> firewallRuleIds;
        private List<String> firewallRuleNames;
        private String imageId;
        private String imageName;
        private String blockDevices;
        @JsonProperty("launchTime")
        private Date instanceLaunchTime;
        private String region;
        private String enableEipYn;
        private String hostName;
        private List<String> securityGroupIds;
        private List<String> securityGroupNames;
        private List<String> excludeDirectories;
        private List<Volume> volumes;
        private List<Tag> tags;

        private String ipAddress;
        private String vendorName;
        private String cpuModel;
        private int cpuCores;
        private String osKernel;
        private String cpuArchitecture;
        private String osName;
        private String osFamily;
        private int memorySize;
        private int swapSize;


        @JsonIgnore
        private String tempFirewalls;
        @JsonIgnore
        private String tempSecurityGroupIds;
        @JsonIgnore
        private String tempSecurityGroupNames;
        @JsonIgnore
        private String tempExcludeDirectories;

    }

    @Getter
    @Setter
    public static class Volume {
        private Long migrationVolumeId;
        private String volumeId;
        private String volumePath;
        private Long volumeSize;
        private String rootYn;
        private String deviceName;
    }

    @Getter
    @Setter
    public static class Tag {
        private Long migrationTagId;
        private String tagName;
        private String tagValue;
    }

}

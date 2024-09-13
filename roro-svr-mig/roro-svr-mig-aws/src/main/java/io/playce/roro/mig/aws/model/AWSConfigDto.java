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
 * SangCheon Park   Feb 10, 2022		    First Draft.
 */
package io.playce.roro.mig.aws.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
public class AWSConfigDto {

    public static class Regions {
        private static List<Region> regions = new ArrayList<>();

        static {
            Region region;
            for (com.amazonaws.regions.Regions r : com.amazonaws.regions.Regions.values()) {
                if (r.getName().contains("gov")) {
                    continue;
                }

                region = new Region();
                region.setDisplayName(r.getDescription());
                region.setRegion(r.getName());
                regions.add(region);
            }
        }

        public static List<Region> getRegionList() {
            return regions;
        }
    }

    @Getter
    @Setter
    public static class Region {
        private String displayName;
        private String region;
    }

    @Getter
    @Setter
    public static class ImageResponse {
        @Schema(title = "Image ID", description = "Image ID")
        private String imageId;

        @Schema(title = "Image Name", description = "Image Name")
        private String imageName;

        @Schema(title = "Architecture", description = "Architecture")
        private String architecture;

        @Schema(title = "Owner ID", description = "Owner ID")
        private String owner;

        @Schema(title = "Visibility", description = "Visibility")
        private String visibility;

        @Schema(title = "Status", description = "Status")
        private String status;

        @Schema(title = "Platform", description = "Platform")
        private String platform;

        @Schema(title = "Creation Date", description = "Creation Date")
        private String creationDate;

        @Schema(title = "Virtualization", description = "Virtualization")
        private String virtualization;

        @Schema(title = "Block Device List", description = "Block Device List")
        private List<BlockDevice> blockDevices;
    }

    @Getter
    @Setter
    public static class BlockDevice {
        @Schema(title = "Block Device Name", description = "Block Device Name")
        private String name;

        @Schema(title = "Volume Size", description = "Volume Size")
        private Integer size;

        @Schema(title = "Volume Type", description = "Volume Type")
        private String type;

        @Schema(title = "Snapshot ID", description = "Snapshot ID")
        private String snapshotId;

        @Schema(title = "IOPS", description = "IOPS")
        private Integer iops;

        @Schema(title = "Delete on Termination", description = "Delete on Termination")
        private Boolean deleteOnTermination;
    }

    @Getter
    @Setter
    public static class VpcResponse {
        @Schema(title = "AWS Region", description = "AWS Region")
        private String region;

        @Schema(title = "VPC ID", description = "VPC ID")
        private String vpcId;

        @Schema(title = "VPC Name", description = "VPC 이름")
        private String vpcName;

        @Schema(title = "VPC CIDR", description = "VPC CIDR")
        private String vpcCidr;

        @Schema(title = "VPC State", description = "VPC State")
        private String state;

        @Schema(title = "DNS Resolution", description = "DNS Resolution")
        private Boolean dnsResolution;

        @Schema(title = "DNS Hostnames", description = "DNS Hostnames")
        private Boolean dnsHostnames;
    }

    @Getter
    @Setter
    public static class VpcUpdateRequest {
        @Schema(title = "AWS Region", description = "AWS Region", required = true)
        private String region;

        @Schema(title = "VPC Name", description = "VPC 이름", required = true)
        private String vpcName;

        @Schema(title = "DNS Resolution", description = "DNS Resolution", required = true)
        private Boolean dnsResolution;

        @Schema(title = "DNS Hostnames", description = "DNS Hostnames", required = true)
        private Boolean dnsHostnames;
    }

    @Getter
    @Setter
    public static class VpcCreateRequest extends VpcUpdateRequest {
        @Schema(title = "VPC CIDR", description = "VPC CIDR", required = true)
        private String vpcCidr;
    }

    @Getter
    @Setter
    public static class SubnetResponse {
        @Schema(title = "AWS Region", description = "AWS Region")
        private String region;

        @Schema(title = "VPC ID", description = "VPC ID")
        private String vpcId;

        @Schema(title = "VPC Name", description = "VPC 이름")
        private String vpcName;

        @Schema(title = "Subnet ID", description = "Subnet ID")
        private String subnetId;

        @Schema(title = "Subnet 이름", description = "Subnet 이름")
        private String subnetName;

        @Schema(title = "Subnet CIDR", description = "Subnet CIDR")
        private String subnetCidr;

        @Schema(title = "Subnet State", description = "Subnet State")
        private String state;

        @Schema(title = "사용 가능한 IP 갯수", description = "사용 가능한 IP 갯수")
        private Integer availableIPs;

        @Schema(title = "Availability Zone", description = "Availability Zone")
        private String availabilityZone;

        @Schema(title = "Public IP 자동 할당 여부", description = "Public IP 자동 할당 여부")
        private Boolean autoAssignPublicIP;
    }

    @Getter
    @Setter
    public static class SubnetUpdateRequest {
        @Schema(title = "AWS Region", description = "AWS Region", required = true)
        private String region;

        @Schema(title = "Subnet 이름", description = "Subnet 이름", required = true)
        private String subnetName;

        @Schema(title = "Public IP 자동 할당 여부", description = "Public IP 자동 할당 여부", required = true)
        private Boolean autoAssignPublicIP;
    }

    @Getter
    @Setter
    public static class SubnetCreateRequest extends SubnetUpdateRequest {
        @Schema(title = "VPC ID", description = "VPC ID", required = true)
        private String vpcId;

        @Schema(title = "Subnet CIDR", description = "Subnet CIDR", required = true)
        private String subnetCidr;

        @Schema(title = "Availability Zone", description = "Availability Zone", required = true)
        private String availabilityZone;
    }

    @Getter
    @Setter
    public static class SecurityGroupResponse {
        @Schema(title = "AWS Region", description = "AWS Region")
        private String region;

        @Schema(title = "VPC ID", description = "VPC ID")
        private String vpcId;

        @Schema(title = "VPC Name", description = "VPC 이름")
        private String vpcName;

        @Schema(title = "Security Group ID", description = "Security Group ID")
        private String groupId;

        @Schema(title = "Security Group Name", description = "Security Group Name")
        private String groupName;

        @Schema(title = "Description", description = "Description")
        private String description;

        @Schema(title = "Permission List", description = "Permission List")
        private List<Permission> permissions;
    }

    @Getter
    @Setter
    public static class SecurityGroupUpdateRequest {
        @Schema(title = "AWS Region", description = "AWS Region", required = true)
        private String region;

        @Schema(title = "Security Group Name", description = "Security Group Name", required = true)
        private String groupName;
    }

    @Getter
    @Setter
    public static class SecurityGroupCreateRequest extends SecurityGroupUpdateRequest {
        @Schema(title = "VPC ID", description = "VPC ID", required = true)
        private String vpcId;

        @Schema(title = "Description", description = "Description", required = true)
        private String description;

        @Schema(title = "Permission List", description = "Permission List")
        private List<Permission> permissionList;
    }

    @Getter
    @Setter
    public static class Permission {
        @Schema(title = "Protocol", description = "Protocol (-1 is ALL)")
        private String protocol;

        @Schema(title = "Start port range", description = "Start port range")
        private Integer fromPort;

        @Schema(title = "End port range", description = "End port range")
        private Integer toPort;

        @Schema(title = "Source", description = "Source IP Address or Security Group")
        private String source;

        @Schema(title = "Description", description = "Description")
        private String description;
    }

    @Getter
    @Setter
    public static class PermissionRequest {
        @Schema(title = "AWS Region", description = "AWS Region", required = true)
        private String region;

        @Schema(title = "Permission List", description = "Permission List", required = true)
        private List<Permission> permissionList;
    }
}
//end of AWSConfigDto.java
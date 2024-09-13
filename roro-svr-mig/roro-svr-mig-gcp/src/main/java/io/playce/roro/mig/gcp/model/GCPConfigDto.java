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
package io.playce.roro.mig.gcp.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

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
public class GCPConfigDto {

    @Getter
    @Setter
    public static class Projects {
        private String message;
        private List<Project> projectList;
    }

    @Getter
    @Setter
    public static class Project {
        private String id;
        private String name;
    }

    @Getter
    @Setter
    public static class AvailableZoneResponse {
        @Schema(title = "region", description = "region")
        private String region;
        @Schema(title = "zone", description = "zone")
        private List<String> zoneList;
    }

    @Getter
    @Setter
    public static class AvailableMachineResponse {
        // @Schema(title = "zone", description = "zone")
        // private String zone;

        @Schema(title = "MachineFamily", description = "machineFamily")
        private String machineFamily;

        @Schema(title = "MachineType", description = "MachineType")
        private String name;

        @Schema(title = "vCPUs", description = "vCPU")
        private String vCPUs;

        @Schema(title = "Memory", description = "Memory")
        private String memory;

        // @Schema(title = "Memory", description = "Memory")
        // private Integer maximumPersistentDisks;
        //
        // @Schema(title = "Memory", description = "Memory")
        // private Long maximumPersistentDisksSizeGb;
        //
        // @Schema(title = "Memory", description = "Memory")
        // private String description;

        // private Integer imageSpaceGb;

        // private Boolean isSharedCpu;

        // private Integer guestCpus;

    }

    @Getter
    @Setter
    @ToString
    public static class NetworkResponse {
        @Schema(title = "Network ID", description = "Network ID")
        private String networkId;

        @Schema(title = "Network Name", description = "Network 이름")
        private String networkName;

        @Schema(title = "SubnetWork count", description = "SubnetWork")
        private Integer subnetCount;

        @Schema(title = "Mtu", description = "Mtu")
        private String mtu;

        @Schema(title = "Routing Mode", description = "Routing Mode")
        private String routingMode;

        @Schema(title = "autoCreateSubnetWorks", description = "서브넷 생성 모드")
        private Boolean autoCreateSubnetWorks;

        @Schema(title = "DNS policy", description = "DNS 정책")
        private String dnsPolicy;

    }

    @Getter
    @Setter
    public static class NetworkCreateRequest extends NetworkUpdateRequest {
        @Schema(title = "Network Name", description = "Network 이름", required = true)
        private String networkName;

        @Schema(title = "Description", description = "설명")
        private String description;

        @Schema(title = "Mtu", description = "최대 전송 단위", allowableValues = "1460, 1500", defaultValue = "1460")
        private String mtu;
    }

    @Getter
    @Setter
    public static class NetworkUpdateRequest {
        @Schema(title = "GCP Project ID", description = "GCP Project ID", required = true)
        private String gcpProjectId;

        @Schema(title = "autoCreateSubnetWorks", description = "서브넷 생성 모드", defaultValue = "false")
        private Boolean autoCreateSubnetWorks;

        @Schema(title = "Routing Mode", description = "동적 라우팅 모드")
        private RoutingConfig routingConfig;

    }

    @Getter
    @Setter
    public static class RoutingConfig {
        @Pattern(regexp = "^(REGIONAL|GLOBAL)$")
        @Schema(title = "Routing Mode", allowableValues = "REGIONAL or GLOBAL", description = "라우팅 모드")
        private String routingMode;
    }

    @Getter
    @Setter
    @ToString
    public static class SubnetResponse {
        @Schema(title = "GCP Region", description = "GCP Region")
        private String region;

        @Schema(title = "Network ID", description = "Network ID")
        private String networkId;

        @Schema(title = "Network Name", description = "Network 이름")
        private String networkName;

        @Schema(title = "Auto create subnet", description = "Subnet 자동 생성 여부")
        private Boolean autoCreateSubnetworks;

        @Schema(title = "Subnet ID", description = "Subnet ID")
        private String id;

        @Schema(title = "Subnet 이름", description = "Subnet 이름")
        private String name;

        @Schema(title = "IpCidrRange", description = "ipCidrRange")
        private String ipCidrRange;

        @Schema(title = "Gateway", description = "gateway")
        private String gatewayAddress;

        @Schema(title = "privateIpGoogleAccess", description = "비공개 Google 액세스")
        private Boolean privateIpGoogleAccess;

        @Schema(title = "loadBalancing", description = "로드 벨런싱 여부.")
        private Boolean loadBalancing;

        @Schema(title = "Purpose", description = "Purpose")
        private String purpose;

        @Schema(title = "Role", description = "Role")
        private String role;

        @Schema(title = "state", description = "state")
        private String state;

        @Schema(title = "Description", description = "설명")
        private String description;

    }

    @Getter
    @Setter
    public static class SubnetWorkCreateRequest extends SubnetWorkUpdateRequest {
        @Schema(title = "Subnet Name", description = "Subnet 이름", required = true)
        private String name;

        @Schema(title = "Description", description = "설명")
        private String description;

        @Pattern(regexp = "^(PRIVATE|INTERNAL_HTTPS_LOAD_BALANCER)$")
        @Schema(title = "purposee", allowableValues = "PRIVATE or INTERNAL_HTTPS_LOAD_BALANCER", description = "서브넷 용도", required = true)
        private String purpose;

        @Pattern(regexp = "^(ACTIVE|BACKUP)$")
        @Schema(title = "role", allowableValues = "ACTIVE or BACKUP", description = "서브넷 역할")
        private String role;


    }

    @Getter
    @Setter
    public static class SubnetWorkUpdateRequest {
        @Schema(title = "GCP Project ID", description = "GCP Project ID", required = true)
        private String gcpProjectId;

        @Schema(title = "Network ID", description = "Network ID")
        private String networkId;

        @Schema(title = "ipCidrRange", description = "ipCidrRange")
        private String ipCidrRange;

        @Schema(title = "GCP Region", description = "GCP Region")
        private String region;

        @Schema(title = "privateIpGoogleAccess", description = "privateIpGoogleAccess")
        private Boolean privateIpGoogleAccess;


    }


    @Getter
    @Setter
    @ToString
    public static class FirewallResponse {
        @Schema(title = "Firewall ID", description = "Firewall ID")
        private String firewallId;

        @Schema(title = "Firewall Name", description = "Firewall 이름")
        private String firewallName;

        @Schema(title = "Description", description = "설명")
        private String description;

        @Schema(title = "Network ID", description = "Network ID")
        private String networkId;

        @Schema(title = "Network Name", description = "Network 이름")
        private String networkName;

        @Pattern(regexp = "^(INGRESS|EGRESS)$")
        @Schema(title = "direction", allowableValues = "INGRESS or EGRESS", description = "direction")
        private String direction;

        @Schema(title = "priority", description = "priority")
        private String priority;

        @Schema(title = "Enforcement", description = "사용유무")
        private Boolean enforcement;

        @Schema(title = "targetType", description = "대상 종류")
        private String targetType;

        @Schema(title = "Target tags", description = "대상 태그")
        private List<String> targetTags;

        @Schema(title = "Target service account", description = "대상 서비스 계정")
        private List<String> targetServiceAccount;

        @Schema(title = "sourceFilter", allowableValues = "ALL or TAG or SERVICEACCOUNT", description = "소스 필터")
        private String sourceFilter;

        @Schema(title = "Source ranges", description = "소스 IP 범위")
        private List<String> sourceRanges;

        @Schema(title = "Source tags", description = "소스 Tags")
        private List<String> sourceTags;

        @Schema(title = "SourceServiceAccount", description = "소스 서비스 계정")
        private List<String> sourceServiceAccount;

        @Schema(title = "destinationFilter", description = "대상 필터")
        private String destinationFilter;

        @Schema(title = "Destination ranges", description = "대상 IP 범위")
        private List<String> destinationRanges;

        // @Schema(title = "Ingress info", description = "인그레스 설정 정보")
        // private IngressInfo ingressInfo;
        //
        // @Schema(title = "Egress info", description = "이그레스 설정 정보")
        // private EgressInfo egressInfo;

        @Schema(title = "Port list", description = "Port list")
        private List<SpecifiedPort> specifiedPorts;

        @Schema(title = "actionType", description = "일치 시 작업")
        private String actionType;

    }

    @Getter
    @Setter
    @ToString
    public static class FirewallCreateRequest {
        @Schema(title = "GCP Project ID", description = "GCP Project ID", required = true)
        private String gcpProjectId;

        @Schema(title = "name", description = "name", required = true)
        private String firewallName;

        @Schema(title = "Description", description = "설명")
        private String description;

        @Schema(title = "networkName", description = "networkName", required = true)
        private String networkName;

        @Schema(title = "Priority", allowableValues = "0 ~ 65535", description = "Priority")
        private String priority;

        @Pattern(regexp = "^(INGRESS|EGRESS)$")
        @Schema(title = "direction", allowableValues = "INGRESS or EGRESS", description = "direction")
        private String direction;

        @Schema(title = "Enforcement", description = "사용 여부")
        private Boolean enforcement;

        @Schema(title = "Ingress info", description = "인그레스 설정 정보")
        private IngressInfo ingressInfo;

        @Schema(title = "Egress info", description = "이그레스 설정 정보")
        private EgressInfo egressInfo;

        @Schema(title = "Port list", description = "Port list")
        private List<SpecifiedPort> specifiedPorts;

        @Pattern(regexp = "^(ALLOW|DENY)$")
        @Schema(title = "Rule type", allowableValues = "ALLOW or DENY", description = "Firewall rule type")
        private String actionType;
    }

    @Getter
    @Setter
    @ToString
    public static class FirewallUpdateRequest {
        @Schema(title = "GCP Project ID", description = "GCP Project ID", required = true)
        private String gcpProjectId;

        @NotNull
        @Schema(title = "Firewall name", description = "firewallName", required = true)
        private String firewallName;

        @Schema(title = "Description", description = "설명")
        private String description;

        @NotNull
        @Schema(title = "networkName", description = "networkName", required = true)
        private String networkName;

        @Schema(title = "Priority", description = "Priority")
        private String priority;

        @Pattern(regexp = "^(INGRESS|EGRESS)$")
        @Schema(title = "direction", allowableValues = "INGRESS or EGRESS", description = "direction")
        private String direction;

        @Schema(title = "Enforcement", description = "사용 여부")
        private Boolean enforcement;

        @Schema(title = "Ingress info", description = "인그레스 설정 정보")
        private IngressInfo ingressInfo;

        @Schema(title = "Egress info", description = "이그레스 설정 정보")
        private EgressInfo egressInfo;

        @Schema(title = "Port list", description = "Port list")
        private List<SpecifiedPort> specifiedPorts;

        @Pattern(regexp = "^(ALLOW|DENY)$")
        @Schema(title = "Rule type", allowableValues = "ALLOW or DENY", description = "Firewall rule type")
        private String actionType;

    }


    @Getter
    @Setter
    @ToString
    public static class FirewallRule {
        @Schema(title = "Firewall ingress tags", description = "Firewall ingress 태그 목록")
        private List<Object> ingressTags;
        @Schema(title = "Firewall egress tags", description = "Firewall egress 태그 목록")
        private List<Object> egressTags;
    }

    @Getter
    @Setter
    public static class IngressInfo {
        @Schema(title = "targetType", description = "대상 종류")
        private String targetType;

        @Schema(title = "Target tags", description = "대상 태그")
        private List<String> targetTags;

        @Schema(title = "Target service account", description = "대상 서비스 계정")
        private List<String> targetServiceAccount;

        @Schema(title = "sourceFilter", allowableValues = "IP ranges or Source tags or SERVICEACCOUNT", description = "소스 필터")
        private String sourceFilter;

        @Schema(title = "Source ranges", description = "소스 IP 범위")
        private List<String> sourceRanges;

        @Schema(title = "Source tags", description = "소스 Tags")
        private List<String> sourceTags;

        @Schema(title = "SourceServiceAccount", description = "소스 서비스 계정")
        private List<String> sourceServiceAccount;

    }

    @Getter
    @Setter
    public static class EgressInfo {
        @Schema(title = "targetType", allowableValues = "TAG", description = "대상 종류")
        private String targetType;

        @Schema(title = "Target tags", description = "대상 태그")
        private List<String> targetTags;

        @Schema(title = "Target service account", description = "대상 서비스 계정")
        private List<String> targetServiceAccount;

        @Schema(title = "destinationFilter", allowableValues = "IP ranges", description = "대상 필터")
        private String destinationFilter;

        @Schema(title = "Destination ranges", description = "대상 IP 범위")
        private List<String> destinationRanges;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecifiedPort {
        private List<String> ports;
        private String ipProtocol;
    }

    @Getter
    @Setter
    @ToString
    public static class MachineImageResponse {
        @Schema(title = "Machine Image ID", description = "Machine Image ID")
        private String id;
        @Schema(title = "creationTimestamp", description = "creationTimestamp")
        private String creationTimestamp;
        @Schema(title = "Name", description = "name")
        private String name;
        @Schema(title = "Description", description = "description")
        private String description;
        @Schema(title = "selfLink", description = "selfLink")
        private String selfLink;
        @Schema(title = "sourceInstance", description = "sourceInstance")
        private String sourceInstance;
        @Pattern(regexp = "^(INVALID|CREATING|READY|DELETING|UPLOADING)$")
        @Schema(title = "Status", description = "status")
        private String status;
        @Schema(title = "sourceInstanceProperties", description = "sourceInstanceProperties")
        private SourceInstanceProperties sourceInstanceProperties;
        @Schema(title = "storageLocations", description = "storageLocations")
        private List<String> storageLocations;
        @Schema(title = "totalStorageBytes", description = "totalStorageBytes")
        private String totalStorageBytes;
        @Schema(title = "kind", description = "kind")
        private String kind;
    }

    @Getter
    @Setter
    public static class RawDisk {
        @Schema(title = "Source", description = "Source")
        private String source;
        @Schema(title = "sha1Checksum", description = "sha1Checksum")
        private String sha1Checksum;
        @Schema(title = "Container Type", description = "Container Type")
        private String containerType;
    }

    @Getter
    @Setter
    public static class Deprecated {
        @Schema(title = "State", description = "State")
        private String state;
        @Schema(title = "Replacement", description = "Replacement")
        private String replacement;
        @Schema(title = "Deprecated", description = "Deprecated")
        private String deprecated;
        @Schema(title = "Obsolete", description = "Obsolete")
        private String obsolete;
        @Schema(title = "Deleted", description = "Deleted")
        private String deleted;
    }

    @Getter
    @Setter
    public static class GuestOsFeatures {
        @Schema(title = "Type", description = "Type")
        private String type;
    }

    @Getter
    @Setter
    @ToString
    public static class VMInstanceRequest {
        @Schema(title = "Name", description = "Name")
        private String name;
        @Schema(title = "Description", description = "Description")
        private String description;
        @Schema(title = "Tags", description = "Tags")
        private Tags tags;
        @Schema(title = "Machine Type", description = "Machine Type")
        private String machineType;
        @Pattern(regexp = "^(PROVISIONING|STAGING|RUNNING|STOPPING|SUSPENDING|SUSPENDED|REPAIRING|TERMINATED)$")
        @Schema(title = "Status", allowableValues = "PROVISIONING or STAGING or RUNNING or STOPPING or SUSPENDING or SUSPENDED or REPAIRING or TERMINATED", description = "Status")
        private String status;
        @Schema(title = "can Ip Forward", description = "Can IP Forward")
        private boolean canIpForward;
        @Schema(title = "Network Interfaces", description = "Network Interfaces")
        private List<NetworkInterfaces> networkInterfaces;
        @Schema(title = "Disks", description = "Disks")
        private List<Disks> disks;
        @Schema(title = "Metadata", description = "Metadata")
        private Metadata metadata;
        @Schema(title = "serviceAccounts", description = "serviceAccounts")
        private List<ServiceAccounts> serviceAccounts;
        @Schema(title = "scheduling", description = "scheduling")
        private Scheduling scheduling;
        @Schema(title = "Lables", description = "Labels")
        private Map<String, String> labels;
        @Schema(title = "labelFingerprint", description = "labelFingerprint")
        private String labelFingerprint;
        @Schema(title = "minCpuPlatform", description = "minCpuPlatform")
        private String minCpuPlatform;
        @Schema(title = "guestAccelerators", description = "guestAccelerators")
        private List<GuestAccelerators> guestAccelerators;
        @Schema(title = "deletionProtection", description = "deletionProtection")
        private boolean deletionProtection;
        @Schema(title = "resourcePolicies", description = "resourcePolicies")
        private List<String> resourcePolicies;
        @Schema(title = "reservationAffinity", description = "reservationAffinity")
        private ReservationAffinity reservationAffinity;
        @Schema(title = "Host Name", description = "Host Name")
        private String hostname;
        @Schema(title = "displayDevice", description = "displayDevice")
        private DisplayDevice displayDevice;
        @Schema(title = "shieldedInstanceConfig", description = "shieldedInstanceConfig")
        private ShieldedInstanceConfig shieldedInstanceConfig;
        @Schema(title = "shieldedInstanceIntegrityPolicy", description = "shieldedInstanceIntegrityPolicy")
        private ShieldedInstanceIntegrityPolicy shieldedInstanceIntegrityPolicy;
        @Schema(title = "confidentialInstanceConfig", description = "confidentialInstanceConfig")
        private ConfidentialInstanceConfig confidentialInstanceConfig;
        @Schema(title = "fingerprint", description = "fingerprint")
        private String fingerprint;
        @Pattern(regexp = "^(INHERIT_FROM_SUBNETWORK)$")
        @Schema(title = "privateIpv6GoogleAccess", allowableValues = "INHERIT_FROM_SUBNETWORK", description = "privateIpv6GoogleAccess")
        private String privateIpv6GoogleAccess;
    }

    @Getter
    @Setter
    public static class Tags {
        @Schema(title = "Items", description = "Items")
        private List<String> items;
        @Schema(title = "fingerprint", description = "fingerprint")
        private String fingerprint;
    }

    @Getter
    @Setter
    public static class NetworkInterfaces {
        @Schema(title = "name", description = "name")
        private String name;
        @Schema(title = "network", description = "network")
        private String network;
        @Schema(title = "subnetwork", description = "subnetwork")
        private String subnetwork;
        @Schema(title = "networkIP", description = "networkIP")
        private String networkIP;
        @Schema(title = "Access Configs", description = "Access Configs")
        private List<AccessConfigs> accessConfigs;
        @Schema(title = "aliasIpRanges", description = "Alias IP Ranges")
        private List<AliasIpRanges> aliasIpRanges;
        @Schema(title = "fingerprint", description = "fingerfrint")
        private String fingerprint;
    }

    @Getter
    @Setter
    public static class AccessConfigs {
        @Schema(title = "type", description = "type")
        private String type;
        @Schema(title = "name", description = "name")
        private String name;
        @Schema(title = "natIP", description = "natIP")
        private String natIP;
        @Schema(title = "setPublicPtr", description = "set public ptt")
        private boolean setPublicPtr;
        @Schema(title = "public Ptr Domain Name", description = "Public Ptr Domain Name")
        private String publicPtrDomainName;
        @Pattern(regexp = "^(PREMIUM|STANDARD)$")
        @Schema(title = "networkTier", allowableValues = "PREMIUM or STANDARD", description = "Network Tier")
        private String networkTier;
    }

    @Getter
    @Setter
    public static class AliasIpRanges {
        @Schema(title = "ip cidr range", description = "IP Cidr Rande")
        private String ipCidrRange;
        @Schema(title = "subnetwork Range Name", description = "Subnetwork Range Name")
        private String subnetworkRangeName;
    }

    @Getter
    @Setter
    @ToString
    public static class Disks {
        @Pattern(regexp = "^(SCRATCH|PERSISTENT)$")
        @Schema(title = "Type", allowableValues = "SCRATCH or PERSISTENT", description = "Type")
        private String type;
        @Pattern(regexp = "^(READ_WRITE|READ_ONLY)$")
        @Schema(title = "Mode", allowableValues = "READ_WRITE or READ_ONLY", description = "Mode")
        private String mode;
        @Schema(title = "Source", description = "Source")
        private String source;
        @Schema(title = "Device Name", description = "Device Name")
        private String deviceName;
        @Schema(title = "Boot", description = "Boot")
        private boolean boot;
        @Schema(title = "Initialize Params", description = "Initialize Params")
        private InitializeParams initializeParams;
        @Schema(title = "autoDelete", description = "autoDelete")
        private boolean autoDelete;
        @Pattern(regexp = "^(SCSI|NVME)$")
        @Schema(title = "interface", allowableValues = "SCSI or NVME", description = "interface")
        private String interfaces;
        @Schema(title = "guestOsFeatures", description = "guestOsFeatures")
        private List<GuestOsFeatures> guestOsFeatures;
        @Schema(title = "diskEncryptionKey", description = "diskEncryptionKey")
        private DiskEncryptionKey diskEncryptionKey;
        @Schema(title = "diskSizeGb", description = "diskSizeGb")
        private String diskSizeGb;
        @Schema(title = "Disk Type", description = "diskType")
        private String diskType;
        @Schema(title = "Index", description = "index")
        private Integer index;
        @Schema(title = "Storage Bytes", description = "storageBytes")
        private String storageBytes;
        @Schema(title = "Storage Bytes Status", description = "storageBytesStatus")
        private String storageBytesStatus;
    }

    @Getter
    @Setter
    public static class InitializeParams {
        @Schema(title = "Disk Name", description = "Disk Name")
        private String diskName;
        @Schema(title = "Source Image", description = "Source Image")
        private String sourceImage;
        @Schema(title = "Disk Size GB", description = "Disk Size GB")
        private String diskSizeGb;
        @Schema(title = "Disk Type", description = "Disk Type")
        private String diskType;
        @Schema(title = "sourceImageEncryptionKey", description = "sourceImageEncryptionKey")
        private SourceImageEncryptionKey sourceImageEncryptionKey;
        @Schema(title = "Labels", description = "Labels")
        private Map<String, String> labels;
        @Schema(title = "Source Snapshot", description = "Source Snapshot")
        private String sourceSnapshot;
        @Schema(title = "sourceSnapshotEncryptionKey", description = "sourceSnapshotEncryptionKey")
        private SourceSnapshotEncryptionKey sourceSnapshotEncryptionKey;
        @Schema(title = "Description", description = "Description")
        private String description;
        @Schema(title = "Resource Policies", description = "Resources Policies")
        private List<String> resourcePolicies;
        @Schema(title = "onUpdateAction", description = "onUpdateAction")
        private String onUpdateAction;
    }

    @Getter
    @Setter
    public static class SourceImageEncryptionKey {
        @Schema(title = "Raw Key", description = "Raw Key")
        private String rawKey;
        @Schema(title = "kms Key Name", description = "kms Key Name")
        private String kmsKeyName;
        @Schema(title = "kms Key Service Account", description = "kms Key Service Account")
        private String kmsKeyServiceAccount;
    }

    @Getter
    @Setter
    public static class SourceSnapshotEncryptionKey {
        @Schema(title = "Raw Key", description = "Raw Key")
        private String rawKey;
        @Schema(title = "kms Key Name", description = "kms Key Name")
        private String kmsKeyName;
        @Schema(title = "kms Key Service Account", description = "kms Key Service Account")
        private String kmsKeyServiceAccount;
    }

    @Getter
    @Setter
    public static class DiskEncryptionKey {
        @Schema(title = "kmsKeyServiceAccount", description = "kmsKeyServiceAccount")
        private String kmsKeyServiceAccount;
        @Schema(title = "rawKey", description = "rawKey")
        private String rawKey;
        @Schema(title = "kmsKeyName", description = "kmsKeyName")
        private String kmsKeyName;
    }

    @Getter
    @Setter
    public static class ShieldedInstanceInitialState {
        @Schema(title = "pk", description = "pk")
        private Pk pk;
        @Schema(title = "keks", description = "keks")
        private List<Keks> keks;
        @Schema(title = "dbs", description = "dbs")
        private List<Dbs> dbs;
        @Schema(title = "dbxs", description = "dbxs")
        private List<Dbxs> dbxs;
    }

    @Getter
    @Setter
    public static class Pk {
        @Schema(title = "content", description = "content")
        private String content;
        @Schema(title = "fileType", description = "fileType")
        private String fileType;
    }

    @Getter
    @Setter
    public static class Keks {
        @Schema(title = "content", description = "content")
        private String content;
        @Schema(title = "fileType", description = "fileType")
        private String fileType;
    }

    @Getter
    @Setter
    public static class Dbs {
        @Schema(title = "content", description = "content")
        private String content;
        @Schema(title = "fileType", description = "fileType")
        private String fileType;
    }

    @Getter
    @Setter
    public static class Dbxs {
        @Schema(title = "content", description = "content")
        private String content;
        @Schema(title = "fileType", description = "fileType")
        private String fileType;
    }

    @Getter
    @Setter
    public static class Metadata {
        @Schema(title = "fingerprint", description = "fingerprint")
        private String fingerprint;
        @Schema(title = "Items", description = "Items")
        private List<Items> items;
    }

    @Getter
    @Setter
    public static class Items {
        @Schema(title = "key", description = "key", required = true)
        private String key;
        @Schema(title = "value", description = "value", required = true)
        private String value;
    }

    @Getter
    @Setter
    public static class ServiceAccounts {
        @Schema(title = "email", description = "email")
        private String email;
        @Schema(title = "scopes", description = "scopes")
        private List<String> scopes;
    }

    @Getter
    @Setter
    public static class Scheduling {
        @Pattern(regexp = "^(MIGRATE|TERMINATE)$")
        @Schema(title = "onHostMaintenance", allowableValues = "MIGRATE or TERMINATE", description = "onHostMaintenance")
        private String onHostMaintenance;
        @Schema(title = "automaticRestart", description = "automaticRestart")
        private boolean automaticRestart;
        @Schema(title = "preemptible", description = "preemptible")
        private boolean preemptible;
        @Schema(title = "nodeAffinities", description = "nodeAffinities")
        private List<NodeAffinities> nodeAffinities;
        @Schema(title = "minNodeCpus", description = "minNodeCpus")
        private Integer minNodeCpus;
    }

    @Getter
    @Setter
    public static class NodeAffinities {
        @Schema(title = "key", description = "key")
        private String key;
        @Pattern(regexp = "^(IN|NOT_IN)$")
        @Schema(title = "opertator", allowableValues = "IN or NOT_IN", description = "operator")
        private String operator;
        @Schema(title = "values", description = "values")
        private List<String> values;
    }

    @Getter
    @Setter
    public static class GuestAccelerators {
        @Schema(title = "acceleratorType", description = "acceleratorType")
        private String acceleratorType;
        @Schema(title = "acceleratorCount", description = "acceleratorCount")
        private Integer acceleratorCount;
    }

    @Getter
    @Setter
    public static class ReservationAffinity {
        @Pattern(regexp = "^(ANY_RESERVATION|SPECIFIC_RESERVATION|NO_RESERVATION)$")
        @Schema(title = "consumeReservationType", allowableValues = "ANY_RESERVATION or SPECIFIC_RESERVATION or NO_RESERVATION", description = "consumeReservationType", defaultValue = "ANY_RESERVATION")
        private String consumeReservationType;
        @Schema(title = "key", description = "key")
        private String key;
        @Schema(title = "values", description = "values")
        private List<String> values;
    }

    @Getter
    @Setter
    public static class DisplayDevice {
        @Schema(title = "enableDisplay", description = "enableDisplay")
        private boolean enableDisplay;
    }

    @Getter
    @Setter
    public static class ShieldedInstanceConfig {
        @Schema(title = "enableSecureBoot", description = "enableSecureBoot")
        private boolean enableSecureBoot;
        @Schema(title = "enableVtpm", description = "enableVtpm")
        private boolean enableVtpm;
        @Schema(title = "enableIntegrityMonitoring", description = "enableIntegrityMonitoring")
        private boolean enableIntegrityMonitoring;
    }

    @Getter
    @Setter
    public static class ShieldedInstanceIntegrityPolicy {
        @Schema(title = "updateAutoLearnPolicy", description = "updateAutoLearnPolicy")
        private boolean updateAutoLearnPolicy;
    }

    @Getter
    @Setter
    public static class ConfidentialInstanceConfig {
        @Schema(title = "enableConfidentialCompute", description = "enableConfidentialCompute")
        private boolean enableConfidentialCompute;
    }

    @Getter
    @Setter
    @ToString
    public static class DiskRequest {
        @Schema(title = "Name", description = "Name")
        private String name;
        @Schema(title = "Description", description = "Description")
        private String description;
        @Schema(title = "Size GB", description = "Size GB")
        private String sizeGb;
        @Schema(title = "Source Snapshot", description = "Source Snapshot")
        private String sourceSnapshot;
        @Schema(title = "options", description = "options")
        private String options;
        @Schema(title = "Source Image", description = "Source Image")
        private String sourceImage;
        @Schema(title = "Type", description = "Type")
        private String type;
        @Schema(title = "Licenses", description = "Licenses")
        private List<String> licenses;
        @Schema(title = "guestOsFeatures", description = "guestOsFeatures")
        private List<GuestOsFeatures> guestOsFeatures;
        @Schema(title = "diskEncryptionKey", description = "diskEncryptionKey")
        private DiskEncryptionKey diskEncryptionKey;
        @Schema(title = "sourceImageEncryptionKey", description = "sourceImageEncryptionKey")
        private SourceImageEncryptionKey sourceImageEncryptionKey;
        @Schema(title = "sourceSnapshotEncryptionKey", description = "sourceSnapshotEncryptionKey")
        private SourceSnapshotEncryptionKey sourceSnapshotEncryptionKey;
        @Schema(title = "Labels", description = "Labels")
        private Map<String, String> labels;
        @Schema(title = "labelFingerprint", description = "labelFingerprint")
        private String labelFingerprint;
        @Schema(title = "replicaZones", description = "replicaZones")
        private List<String> replicaZones;
        @Schema(title = "licenseCodes", description = "licenseCodes")
        private List<String> licenseCodes;
        @Schema(title = "physicalBlockSizeBytes", description = "physicalBlockSizeBytes")
        private String physicalBlockSizeBytes;
        @Schema(title = "resourcePolicies", description = "resourcePolicies")
        private List<String> resourcePolicies;
        @Schema(title = "sourceDisk", description = "sourceDisk")
        private String sourceDisk;
    }

    @Getter
    @Setter
    @ToString
    public static class SourceInstanceProperties {
        @Schema(title = "Description", description = "description")
        private String description;
        @Schema(title = "Tags", description = "tags")
        private Tags tags;
        @Schema(title = "Machine Type", description = "machineType")
        private String machineType;
        @Schema(title = "canIpForward", description = "canIpForward")
        private boolean canIpForward;
        @Schema(title = "networkInterfaces", description = "networkInterfaces")
        private List<NetworkInterfaces> networkInterfaces;
        @Schema(title = "Disks", description = "disks")
        private List<Disks> disks;
        @Schema(title = "Metadata", description = "metadata")
        private Metadata metadata;
        @Schema(title = "Service Accounts", description = "serviceAccounts")
        private List<ServiceAccounts> serviceAccounts;
        @Schema(title = "Scheduling", description = "scheduling")
        private Scheduling scheduling;
        @Schema(title = "Labels", description = "labels")
        private Map<String, String> labels;
        @Schema(title = "guestAccelerators", description = "guestAccelerators")
        private List<GuestAccelerators> guestAccelerators;
        @Schema(title = "minCpuPlatform", description = "minCpuPlatform")
        private String minCpuPlatform;
        @Schema(title = "deletionProtection", description = "deletionProtection")
        private boolean deletionProtection;
    }
}
//end of GCPConfigDto.java
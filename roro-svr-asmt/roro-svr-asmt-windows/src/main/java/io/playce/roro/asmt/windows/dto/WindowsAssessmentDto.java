package io.playce.roro.asmt.windows.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class WindowsAssessmentDto {

    @Getter
    @Builder
    @ToString
    public static class WindowsResult {

        private final SystemInformation systemInformation;
        private final Environment environment;
        private final Cpu cpu;
        private final List<Network> networks;
        private final List<Dns> dns;
        private final List<Route> routes;
        private final List<Port> ports;
        private final Hosts hosts;
        private final List<Firewall> firewalls;
        private final List<Disk> disks;
        private final List<InstalledSoftware> installedSoftware;
        private final List<Process> process;
        private final List<Service> services;
        private final Timezone timezone;
        private final List<Schedule> schedules;
        private final List<LocalUser> localUsers;
        private final List<LocalGroupUser> localGroupUsers;
        private final Map<String, String> errorMap;
//        private final List<LocalGroup> localGroups;

    }

    @Getter
    @Setter
    @ToString
    public static class SystemInformation {

        @JsonAlias("Host Name")
        private String hostName;

        @JsonAlias("OS Name")
        private String osName;

        @JsonAlias("OS Version")
        private String osVersion;

        @JsonAlias("OS Manufacturer")
        private String osManufacturer;

        @JsonAlias("OS Configuration")
        private String osConfiguration;

        @JsonAlias("OS Build Type")
        private String osBuildType;

        @JsonAlias("Registered Owner")
        private String registeredOwner;

        @JsonAlias("Registered Organization")
        private String registeredOrganization;

        @JsonAlias("Product ID")
        private String productId;

        @JsonAlias("Original Install Date")
        private String originalInstallDate;

        @JsonAlias("System Boot Time")
        private String systemBootTime;

        @JsonAlias("System Manufacturer")
        private String systemManufacturer;

        @JsonAlias("System Model")
        private String systemModel;

        @JsonAlias("System Type")
        private String systemType;

        @JsonAlias("Processor(s)")
        private String processors;

        @JsonAlias("BIOS Version")
        private String biosVersion;

        @JsonAlias("Windows Directory")
        private String windowsDirectory;

        @JsonAlias("System Directory")
        private String systemDirectory;

        @JsonAlias("Boot Device")
        private String bootDevice;

        @JsonAlias("System Locale")
        private String systemLocale;

        @JsonAlias("Input Locale")
        private String inputLocale;

        @JsonAlias("Time Zone")
        private String timeZone;

        @JsonAlias("Total Physical Memory")
        private String totalPhysicalMemory;

        @JsonAlias("Available Physical Memory")
        private String availablePhysicalMemory;

        @JsonAlias("Virtual Memory: Max Size")
        private String virtualMemoryMaxSize;

        @JsonAlias("Virtual Memory: Available")
        private String virtualMemoryAvailable;

        @JsonAlias("Virtual Memory: In Use")
        private String virtualMemoryInUse;

        @JsonAlias("Page File Location(s)")
        private String pageFileLocations;

        @JsonAlias("Domain")
        private String domain;

        @JsonAlias("Logon Server")
        private String logonServer;

        @JsonAlias("Hotfix(s)")
        private String hotFixes;

        @JsonAlias("Network Card(s)")
        private String networkCards;

        @JsonAlias("Hyper-V Requirements")
        private String hyperVRequirements;

        @JsonAlias("Manufacturer")
        private String manufacturer;

        @JsonAlias("Model")
        private String model;

    }

    @Getter
    @Setter
    @ToString
    public static class Environment {

        @JsonAlias("ALLUSERSPROFILE")
        private String allUsersProfile;

        @JsonAlias("APPDATA")
        private String appdata;

        @JsonAlias("CommonProgramFiles")
        private String commonProgramFiles;

        @JsonAlias("CommonProgramFiles(x86)")
        private String commonProgramFilesx86;

        @JsonAlias("CommonProgramW6432")
        private String commonProgramW6432;

        @JsonAlias("COMPUTERNAME")
        private String computerName;

        @JsonAlias("ComSpec")
        private String comSpec;

        @JsonAlias("LOCALAPPDATA")
        private String localAppData;

        @JsonAlias("MSMPI_BIN")
        private String msmpiBin;

        @JsonAlias("FP_NO_HOST_CHECK")
        private String fpNoHostCheck;

        @JsonAlias("HOMEDRIVE")
        private String homeDrive;

        @JsonAlias("HOMEPATH")
        private String homePath;

        @JsonAlias("LOGONSERVER")
        private String logonServer;

        @JsonAlias("NUMBER_OF_PROCESSORS")
        private String numberOfProcessors;

        @JsonAlias("OS")
        private String os;

        @JsonAlias("Path")
        private String path;

        @JsonAlias("PATHEXT")
        private String pathExt;

        @JsonAlias("PROCESSOR_ARCHITECTURE")
        private String processorArchitecture;

        @JsonAlias("PROCESSOR_IDENTIFIER")
        private String processorIdentifier;

        @JsonAlias("PROCESSOR_LEVEL")
        private String processorLevel;

        @JsonAlias("PROCESSOR_REVISION")
        private String processorRevision;

        @JsonAlias("ProgramData")
        private String programData;

        @JsonAlias("ProgramFiles")
        private String programFiles;

        @JsonAlias("ProgramFiles(x86)")
        private String programFilesx86;

        @JsonAlias("ProgramW6432")
        private String programW6432;

        @JsonAlias("PROMPT")
        private String prompt;

        @JsonAlias("PSModulePath")
        private String psModulePath;

        @JsonAlias("PUBLIC")
        private String PUBLIC;

        @JsonAlias("SystemDrive")
        private String systemDrive;

        @JsonAlias("SystemRoot")
        private String systemRoot;

        @JsonAlias("TEMP")
        private String temp;

        @JsonAlias("TMP")
        private String tmp;

        @JsonAlias("USERDOMAIN")
        private String userDomain;

        @JsonAlias("USERDOMAIN_ROAMINGPROFILE")
        private String userDomainRoamingProfile;

        @JsonAlias("USERNAME")
        private String username;

        @JsonAlias("USERPROFILE")
        private String userprofile;
        private String windir;

    }

    @Getter
    @Setter
    @ToString
    public static class Cpu {

        @JsonAlias("Name")
        private String name;

        @JsonAlias("Caption")
        private String caption;

        @JsonAlias("NumberOfCores")
        private String cores;

        @JsonAlias("NumberOfLogicalProcessors")
        private String logicalProcessors;

        @JsonAlias("MaxClockSpeed")
        private String maxClockSpeed;

        private String sockets;
    }

    @Getter
    @Setter
    @ToString
    public static class Network {

        @JsonAlias("iIndex")
        private String interfaceIndex;

        @JsonAlias("iDesc")
        private String interfaceDescription;

        @JsonAlias("iAlias")
        private String interfaceAlias;

        @JsonAlias("IPv4")
        private List<String> iPv4Address;

        @JsonAlias("IPv4DG")
        private List<String> iPv4DefaultGateway;

        @JsonAlias("IPv6")
        private List<String> iPv6Address;

        @JsonAlias("IPv6DG")
        private List<String> iPv6DefaultGateway;

        @JsonAlias("Mac")
        private List<String> MacAddress;

        @JsonAlias("Status")
        private String status;

    }

    @Getter
    @Setter
    @ToString
    public static class Dns {

        @JsonAlias("InterfaceAlias")
        private String interfaceAlias;

        @JsonAlias("InterfaceIndex")
        private String interfaceIndex;

        @JsonAlias("AddressFamily")
        private String addressFamily;

        @JsonAlias("ServerAddresses")
        private String[] serverAddresses;

    }

    @Getter
    @Setter
    @ToString
    public static class Route {

        @JsonAlias("AddressFamily")
        private String addressFamily;

        @JsonAlias("DestinationPrefix")
        private String destinationPrefix;

        @JsonAlias("NextHop")
        private String nextHop;

        @JsonAlias("RouteMetric")
        private String routeMetric;

        private String ifIndex;

    }

    @Getter
    @Setter
    @ToString
    public static class Port {

        @JsonAlias("Protocol")
        private String protocol;

        @JsonAlias("LocalAddress")
        private String localAddress;

        private String localPort;

        @JsonAlias("RemoteAddress")
        private String remoteAddress;

        private String remotePort;

        @JsonAlias("State")
        private String state;

        @JsonAlias("PID")
        private String pid;

        @JsonAlias("ProcessName")
        private String processName;

        @JsonIgnore
        private String type;

        public String getLocalAddress() {
            if (StringUtils.defaultString(type).equals("Manual")) {
                return localAddress;
            }

            if (StringUtils.isNotEmpty(localAddress)) {
                return localAddress.substring(0, localAddress.lastIndexOf(":"));
            } else {
                return "";
            }
        }

        public String getLocalPort() {
            if (StringUtils.defaultString(type).equals("Manual")) {
                return localPort;
            }

            if (StringUtils.isNotEmpty(localAddress)) {
                return localAddress.substring(localAddress.lastIndexOf(":") + 1);
            } else {
                return "";
            }
        }

        public String getLocalPort2() {
            return localPort;
        }

        public String getRemoteAddress() {
            if (StringUtils.defaultString(type).equals("Manual")) {
                return remoteAddress;
            }

            if (StringUtils.isNotEmpty(remoteAddress)) {
                return remoteAddress.substring(0, remoteAddress.lastIndexOf(":"));
            } else {
                return "";
            }
        }

        public String getRemoteAddress2() {
            return remoteAddress;
        }

        public String getRemotePort() {
            if (StringUtils.defaultString(type).equals("Manual")) {
                return remotePort;
            }

            if (StringUtils.isNotEmpty(remoteAddress)) {
                return remoteAddress.substring(remoteAddress.lastIndexOf(":") + 1);
            } else {
                return "";
            }
        }

        public String getRemotePort2() {
            return remotePort;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Hosts {
        private String contents;
        private Map<String, List<String>> mappings;
    }

    @Getter
    @Setter
    @ToString
    public static class Firewall {

        @JsonAlias({"NM", "Name"})
        private String name;

        @JsonAlias({"DP", "Display"})
        private String displayName;

        @JsonAlias({"DS", "Desc"})
        private String description;

        @JsonAlias({"PR", "Proto"})
        private String protocol;

        @JsonAlias({"LP", "LPort"})
        @JsonProperty(access = Access.WRITE_ONLY)
        private Object tempLocalPort;

        private List<String> localPort;

        @JsonAlias({"RP", "RemotePort"})
        @JsonProperty(access = Access.WRITE_ONLY)
        private Object tempRemotePort;

        private List<String> remotePort;

        @JsonAlias({"RA", "RAddress"})
        @JsonProperty(access = Access.WRITE_ONLY)
        private Object tempRemoteAddress;

        private List<String> remoteAddress;

        @JsonAlias({"EA", "Enabled"})
        private String enabled;

        @JsonAlias({"DR", "Direct"})
        private String direction;

        @JsonAlias({"AT", "Act"})
        private String action;

    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class Disk {

        @JsonAlias({"DiskSerialNumber", "VolumeSerialNumber"})
        private String diskSerialNumber;

        @JsonAlias("DiskMediaType")
        private String diskMediaType;

        @JsonAlias("PartitionName")
        private String partitionName;

        @JsonAlias("DiskStatus")
        private String diskStatus;

        @JsonAlias({"DriveLetter", "DeviceId"})
        private String driveLetter;

        @JsonAlias("PartitionDiskIndex")
        private String partitionDiskIndex;

        @JsonAlias("FreeSpace")
        private String freeSpace;

        @JsonAlias({"TotalSize", "Size"})
        private String totalSize;

        @JsonAlias({"DiskSystemName", "SystemName"})
        private String diskSystemName;

        @JsonAlias({"DiskVolumeName", "VolumeName"})
        private String diskVolumeName;

        @JsonAlias("DiskModel")
        private String diskModel;

        @JsonAlias({"DiskDeviceID", "ProviderName"})
        private String diskDeviceId;

        @JsonAlias("PartitionType")
        private String partitionType;

        @JsonAlias("FileSystem")
        private String fileSystem;
    }

    @Getter
    @Setter
    @ToString
    public static class InstalledSoftware {

        @JsonAlias("DisplayName")
        private String displayName;

        @JsonAlias("DisplayVersion")
        private String displayVersion;

        @JsonAlias("Publisher")
        private String publisher;

        @JsonAlias("InstallDate")
        private String installDate;
    }

    @Getter
    @Setter
    @ToString
    public static class Process {

        @JsonAlias("Handles")
        private String handles;

        @JsonAlias("Id")
        private String id;

        @JsonAlias("ProcessName")
        private String processName;

        @JsonAlias("UserName")
        private String userName;

        @JsonAlias("NPM")
        private String npm;

        @JsonAlias("PM")
        private String pm;

        @JsonAlias("WS")
        private String ws;

        @JsonAlias("CPU")
        private String cpu;

        @JsonAlias("Path")
        private String path;

        @JsonAlias("Company")
        private String company;

        @JsonAlias("Description")
        private String description;

        @JsonAlias("CommandLine")
        private String commandLine;

        public String getCommandLine() {
            return StringUtils.defaultString(commandLine).replaceAll(":\\s+", ":");
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Service {

        @JsonAlias("Name")
        private String name;

        @JsonAlias("DisplayName")
        private String displayName;

        @JsonAlias("ServiceName")
        private String serviceName;

        @JsonAlias("ServiceType")
        private String serviceType;

        @JsonAlias("StartType")
        private String startType;

        @JsonAlias("Status")
        private String status;

    }

    @Getter
    @Setter
    @ToString
    public static class Timezone {

        @JsonAlias("Id")
        private String id;

        @JsonAlias("DisplayName")
        private String displayName;

        @JsonAlias("StandardName")
        private String standardName;

        @JsonAlias("DaylightName")
        private String daylightName;

        @JsonAlias("SupportsDaylightSavingTime")
        private String supportsDaylightSavingTime;

    }

    @Getter
    @Setter
    @ToString
    public static class Schedule {

        @JsonAlias("TaskPath")
        private String taskPath;

        @JsonAlias("TaskName")
        private String taskName;

        @JsonAlias("Description")
        private String description;

        @JsonAlias("State")
        private String state;

    }

    @Getter
    @Setter
    @ToString
    public static class LocalGroup {

        @JsonAlias("Name")
        private String name;

        @JsonAlias("Description")
        private String description;

        @JsonAlias("PrincipalSource")
        private String principalSource;

    }

    @Getter
    @Setter
    @ToString
    public static class LocalUser {

        @JsonAlias("Name")
        private String name;

        @JsonAlias("Enabled")
        private String enabled;

        @JsonAlias("ObjectClass")
        private String objectClass;

        @JsonAlias("Description")
        private String description;

        @JsonAlias("PrincipalSource")
        private String principalSource;

        @JsonAlias("Disabled")
        @JsonProperty(access = Access.WRITE_ONLY)
        private boolean disabled;

    }

    @Getter
    @Setter
    @ToString
    public static class LocalGroupUser {

        @JsonAlias("Group")
        private String group;

        @JsonAlias("Description")
        private String description;

        @JsonAlias("User")
        private String users;

    }

    @Getter
    @Setter
    @ToString
    public static class TempGroupUser {

        private String group;
        private String user;

    }

}

package io.playce.roro.svr.asmt.dto.result;

import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Getter
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
public class WindowsAssessmentResult extends ServerAssessmentResult {

    private final WindowsResult windowsResult;


    @Getter
    @Setter
    @ToString
    public static class WindowsResult {

        private SystemInformation systemInformation;
        private Environment environment;
        private Cpu cpu;
        private List<Network> networks;
        private List<Dns> dns;
        private List<Route> routes;
        private List<Port> ports;
        private Hosts hosts;
        private List<Firewall> firewalls;
        private List<Disk> disks;
        private List<InstalledSoftware> installedSoftware;
        private List<Process> process;
        private List<Service> services;
        private Timezone timezone;
        private List<Schedule> schedules;
        private List<LocalUser> localUsers;
        private List<LocalGroupUser> localGroupUsers;
        private Map<String, String> errorMap;
    }

    @Getter
    @Setter
    @ToString
    public static class SystemInformation {

        private String hostName;
        private String osName;
        private String osVersion;
        private String osManufacturer;
        private String osConfiguration;
        private String osBuildType;
        private String registeredOwner;
        private String registeredOrganization;
        private String productId;
        private String originalInstallDate;
        private String systemBootTime;
        private String systemManufacturer;
        private String systemModel;
        private String systemType;
        private String processors;
        private String biosVersion;
        private String windowsDirectory;
        private String systemDirectory;
        private String bootDevice;
        private String systemLocale;
        private String inputLocale;
        private String timeZone;
        private String totalPhysicalMemory;
        private String availablePhysicalMemory;
        private String virtualMemoryMaxSize;
        private String virtualMemoryAvailable;
        private String virtualMemoryInUse;
        private String pageFileLocations;
        private String domain;
        private String logonServer;
        private String hotFixes;
        private String networkCards;
        private String hyperVRequirements;
        private String manufacturer;
        private String model;

    }

    @Getter
    @Setter
    @ToString
    public static class Environment {

        private String allUsersProfile;
        private String appdata;
        private String commonProgramFiles;
        private String commonProgramFilesx86;
        private String commonProgramW6432;
        private String computerName;
        private String comSpec;
        private String localAppData;
        private String msmpiBin;
        private String fpNoHostCheck;
        private String homeDrive;
        private String homePath;
        private String logonServer;
        private String numberOfProcessors;
        private String os;
        private String path;
        private String pathExt;
        private String processorArchitecture;
        private String processorIdentifier;
        private String processorLevel;
        private String processorRevision;
        private String programData;
        private String programFiles;
        private String programFilesx86;
        private String programW6432;
        private String prompt;
        private String psModulePath;
        private String PUBLIC;
        private String systemDrive;
        private String systemRoot;
        private String temp;
        private String tmp;
        private String userDomain;
        private String userDomainRoamingProfile;
        private String username;
        private String userprofile;
        private String windir;

    }

    @Getter
    @Setter
    @ToString
    public static class Cpu {

        private String name;
        private String caption;
        private String cores;
        private String logicalProcessors;
        private String maxClockSpeed;
        private String sockets;

    }

    @Getter
    @Setter
    @ToString
    public static class Network {

        private String interfaceIndex;
        private String interfaceDescription;
        private String interfaceAlias;
        private List<String> iPv4Address;
        private List<String> iPv4DefaultGateway;
        private List<String> iPv6Address;
        private List<String> iPv6DefaultGateway;
        private List<String> MacAddress;
        private String status;

    }

    @Getter
    @Setter
    @ToString
    public static class Dns {

        private String interfaceAlias;
        private String interfaceIndex;
        private String addressFamily;
        private String[] serverAddresses;

    }

    @Getter
    @Setter
    @ToString
    public static class Route {

        private String addressFamily;
        private String destinationPrefix;
        private String nextHop;
        private String routeMetric;
        private String ifIndex;

    }

    @Getter
    @Setter
    @ToString
    public static class Port {

        private String protocol;
        private String localAddress;
        private String localPort;
        private String remoteAddress;
        private String remotePort;
        private String state;
        private String pid;
        private String processName;
        private String type;

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
    @EqualsAndHashCode
    public static class Firewall {

        private String name;
        private String displayName;
        private String description;
        private String protocol;
        private List<String> localPort;
        private List<String> remotePort;
        private List<String> remoteAddress;
        private String enabled;
        private String direction;
        private String action;

    }

    @Getter
    @Setter
    @ToString
    public static class Disk {

        private String diskSerialNumber;
        private String diskMediaType;
        private String partitionName;
        private String diskStatus;
        private String driveLetter;
        private String partitionDiskIndex;
        private String freeSpace;
        private String totalSize;
        private String diskSystemName;
        private String diskVolumeName;
        private String diskModel;
        private String diskDeviceId;
        private String partitionType;
        private String fileSystem;

    }

    @Getter
    @Setter
    @ToString
    public static class InstalledSoftware {

        private String displayName;
        private String displayVersion;
        private String publisher;
        private String installDate;
    }

    @Getter
    @Setter
    @ToString
    public static class Process {

        private String handles;
        private String id;
        private String processName;
        private String userName;
        private String npm;
        private String pm;
        private String ws;
        private String cpu;
        private String path;
        private String company;
        private String description;
        private String commandLine;

    }

    @Getter
    @Setter
    @ToString
    public static class Service {

        private String name;
        private String displayName;
        private String serviceName;
        private String serviceType;
        private String startType;
        private String status;

    }

    @Getter
    @Setter
    @ToString
    public static class Timezone {

        private String id;
        private String displayName;
        private String standardName;
        private String daylightName;
        private String supportsDaylightSavingTime;

    }

    @Getter
    @Setter
    @ToString
    public static class Schedule {

        private String taskPath;
        private String taskName;
        private String description;
        private String state;

    }

    @Getter
    @Setter
    @ToString
    public static class LocalGroup {

        private String name;
        private String description;
        private String principalSource;

    }

    @Getter
    @Setter
    @ToString
    public static class LocalUser {

        private String name;
        private String enabled;
        private String objectClass;
        private String description;
        private String principalSource;

    }

    @Getter
    @Setter
    @ToString
    public static class LocalGroupUser {

        private String group;
        private String description;
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

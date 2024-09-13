package io.playce.roro.common.enums;

import lombok.Getter;

@Getter
public enum TrackingKey {
    CPU_MODEL("CPU", "CPU Model"),
    CPU_CORES("CPU", "CPU Cores"),
    CPU_COUNT("CPU", "CPU Count"),
    MEMORY_SIZE("Memory", "Memory Size(MB)"),
    KERNEL_VERSION("Kernel", "Kernel Version"),
    OS_VERSION("OS", "OS Version"),
    BIOS_VERSION("BIOS", "BIOS Version"),
    HOST("Host", null),
    NETWORK_INTERFACE("Network Interface", null),
    FILE_SYSTEM("File System", null),
    USER("User", null),
    GROUP("Group", null),
    PACKAGE("Package", null),
    VG("VG", null),
    FIREWALL("Firewall", null),
    ROUTE("Route", null),
    DNS("DNS", null),

    CONFIG_FILE("Config File", null),
    INSTANCE("Instance", null),
    ;

    private final String changeType;
    private final String property;

    TrackingKey(String changeType, String property) {
        this.property = property;
        this.changeType = changeType;
    }

}

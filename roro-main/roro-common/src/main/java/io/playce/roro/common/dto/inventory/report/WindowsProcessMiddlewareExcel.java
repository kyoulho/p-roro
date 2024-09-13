package io.playce.roro.common.dto.inventory.report;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WindowsProcessMiddlewareExcel {

    private String serviceIds;
    private String serviceNames;
    private String serverId;
    private String serverName;
    private String middlewareType;
    private String vendor;
    private String solutionName;
    private String solutionPath;
    private String javaVersion;

}

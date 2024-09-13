package io.playce.roro.prerequisite.server;

import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.prerequisite.config.PrerequisiteConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class ServerInfo {
    private TargetHost host;
    private InventoryProcessConnectionInfo inventoryProcessConnectionInfo;
    private PrerequisiteConfig config;
    private boolean window;
}

package io.playce.roro.prerequisite.server.impl;

import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.prerequisite.config.PrerequisiteConfig;
import io.playce.roro.prerequisite.server.ServerInfo;

public class HPUXPrerequisite extends AbstractUnixPrerequisite {
    public HPUXPrerequisite(ServerInfo serverInfo, ServerResult serverResult) {
        super(serverInfo, serverResult);
    }

    @Override
    protected PrerequisiteConfig.Software getSoftware() {
        PrerequisiteConfig config = serverInfo.getConfig();
        return config.getSoftware("HP-UX", "hp");
    }
}

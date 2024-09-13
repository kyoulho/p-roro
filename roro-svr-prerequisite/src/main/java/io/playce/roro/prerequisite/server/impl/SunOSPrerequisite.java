package io.playce.roro.prerequisite.server.impl;

import io.playce.roro.prerequisite.config.PrerequisiteConfig;
import io.playce.roro.prerequisite.server.ServerInfo;
import io.playce.roro.common.dto.prerequisite.ServerResult;

public class SunOSPrerequisite extends AbstractUnixPrerequisite {
    public SunOSPrerequisite(ServerInfo serverInfo, ServerResult serverResult) {
        super(serverInfo, serverResult);
    }

    @Override
    protected PrerequisiteConfig.Software getSoftware() {
        PrerequisiteConfig config = serverInfo.getConfig();
        return config.getSoftware("SunOS", "solaris");
    }
}

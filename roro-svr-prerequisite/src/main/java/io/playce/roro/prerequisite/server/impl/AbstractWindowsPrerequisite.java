package io.playce.roro.prerequisite.server.impl;

import io.playce.roro.prerequisite.server.ServerInfo;
import io.playce.roro.common.dto.prerequisite.ServerResult;

public abstract class AbstractWindowsPrerequisite extends AbstractPrerequisite {
    public AbstractWindowsPrerequisite(ServerInfo serverInfo, ServerResult serverResult) {
        super(serverInfo, serverResult);
    }
}

package io.playce.roro.prerequisite.server;

import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.svr.asmt.config.DistributionConfig;

public interface ServerPrerequisiteFactory {
    ServerPrerequisite get(DistributionConfig config, ServerInfo serverInfo, ServerResult serverResult) throws InterruptedException;
}

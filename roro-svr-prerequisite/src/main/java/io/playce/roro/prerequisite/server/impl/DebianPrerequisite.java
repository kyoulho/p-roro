package io.playce.roro.prerequisite.server.impl;

import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.prerequisite.config.PrerequisiteConfig;
import io.playce.roro.prerequisite.server.ServerInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DebianPrerequisite extends AbstractLinuxPrereqisite {
    public DebianPrerequisite(ServerInfo serverInfo, ServerResult serverResult) {
        super(serverInfo, serverResult);
    }

    @Override
    public void checkSoftwares() throws InterruptedException {
        serverResult.increaseStep();
        PrerequisiteConfig.Software common = getSoftware("common");
        PrerequisiteConfig.Software software = getSoftware("ubuntu");

        checkSoftware(common.getCommands(), software.getCommands());
    }
}

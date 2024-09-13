package io.playce.roro.prerequisite;

import io.playce.roro.prerequisite.server.ServerPrerequisite;
import io.playce.roro.prerequisite.server.ServerPrerequisiteFactory;
import io.playce.roro.prerequisite.server.impl.DefaultServerPrerequisiteFactory;
import io.playce.roro.prerequisite.server.ServerInfo;
import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.svr.asmt.config.DistributionConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
@RequiredArgsConstructor
@Slf4j
public class PrerequisiteComponent {
    private final DistributionConfig config;
    private ServerPrerequisiteFactory serverPrerequisiteFactory;

    @PostConstruct
    public void init() {
        serverPrerequisiteFactory = new DefaultServerPrerequisiteFactory();
    }

    public void executeCheckServer(ServerInfo serverInfo, ServerResult serverResult) throws InterruptedException {
        ServerPrerequisite prerequisite = serverPrerequisiteFactory.get(config, serverInfo, serverResult);
        if (prerequisite == null) {
            return;
        }

        prerequisite.checkAdminPermission();
        prerequisite.checkSoftwares();
    }
}

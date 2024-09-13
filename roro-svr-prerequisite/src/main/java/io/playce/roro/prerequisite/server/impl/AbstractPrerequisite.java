package io.playce.roro.prerequisite.server.impl;

import io.playce.roro.common.dto.prerequisite.CheckStatus;
import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.prerequisite.server.ServerInfo;
import io.playce.roro.prerequisite.server.ServerPrerequisite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPrerequisite implements ServerPrerequisite {
    protected final ServerInfo serverInfo;
    protected final ServerResult serverResult;

    @Override
    public void checkAdminPermission() throws InterruptedException {
        serverResult.increaseStep();

        TargetHost targetHost = serverInfo.getHost();

        if (targetHost.getUsername().equals("root") || SSHUtil.canExecuteCommandWithSu(targetHost)) {
            serverResult.updateState(CheckStatus.Icon.SUCCESS, CheckStatus.Result.Enable, StringUtils.EMPTY);
            log.debug("step3.exist admin privileges: {}", true);
            log.debug("step3 -> {}", serverResult);
            return;
        }

        if (SSHUtil.isSudoer(targetHost)) {
            log.debug("step3.exist admin privileges: {}", true);
            String result = SSHUtil.executeCommand(serverInfo.getHost(), "sudo egrep '^Defaults.*requiretty' /etc/sudoers", null, false);
            if (StringUtils.isEmpty(result)) {
                serverResult.updateState(CheckStatus.Icon.SUCCESS, CheckStatus.Result.Enable, StringUtils.EMPTY);
            } else {
                serverResult.updateState(CheckStatus.Icon.WARN, CheckStatus.Result.Disable, result);
            }
        } else {
            log.debug("step3.exist admin privileges: {}", false);
            serverResult.updateState(CheckStatus.Icon.FAIL, CheckStatus.Result.Disable, "User haven't administrator privileges");
        }
        log.debug("step3 -> {}", serverResult);
    }
}

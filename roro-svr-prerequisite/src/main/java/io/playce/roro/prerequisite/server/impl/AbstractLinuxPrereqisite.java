package io.playce.roro.prerequisite.server.impl;

import com.jcraft.jsch.JSchException;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.dto.prerequisite.CheckStatus;
import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil2;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.prerequisite.config.PrerequisiteConfig;
import io.playce.roro.prerequisite.server.ServerInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractLinuxPrereqisite extends AbstractPrerequisite {

    public AbstractLinuxPrereqisite(ServerInfo serverInfo, ServerResult serverResult) {
        super(serverInfo, serverResult);
    }

    protected PrerequisiteConfig.Software getSoftware(String id) {
        PrerequisiteConfig config = serverInfo.getConfig();
        String idLike = config.getIdLikeMap().get(id);
        if (idLike == null) {
            throw new RoRoException("Server type not found.");
        }
        return config.getSoftware("Linux", idLike);
    }

    @SafeVarargs
    protected final void checkSoftware(List<String>... commandSets) throws InterruptedException {
        CheckStatus.Icon icon = CheckStatus.Icon.SUCCESS;

        int successCnt = 0;
        serverResult.addStep4MessageList();

        try {
            TargetHost targetHost = serverInfo.getHost();

            for (List<String> commands : commandSets) {
                List<RemoteExecResult> results = SSHUtil2.runCommands(targetHost, commands.stream()
                        .map(c -> String.format(". /etc/profile;type %s", c)).collect(Collectors.toList()), false);

                assert commands.size() == results.size();
                for (int i = 0; i < results.size(); i++) {
                    String command = commands.get(i);
                    RemoteExecResult result = results.get(i);
                    if (result.isErr()) {
                        serverResult.addMessage(String.format("'%s' is not installed", command));
                        icon = CheckStatus.Icon.FAIL;
                    } else {
                        successCnt++;
                    }
                }
            }
        } catch (JSchException | IOException e) {
            RoRoException.checkInterruptedException(e);
            log.error("{}", e.getMessage(), e);
            serverResult.addMessage(e.getMessage());
            serverResult.updateState(CheckStatus.Icon.FAIL, CheckStatus.Result.NotInstalled);
            throw new RoRoException(e.getMessage(), e);
        }

        /*for (List<String> commands : commandSets) {
            for (String command : commands) {
                String result = SSHUtil.executeCommand(serverInfo.getHost(), String.format("type %s", command), null, false);
                log.trace("step4.check software - command: {}, result: {}", command, result);
                if (StringUtils.isEmpty(result)) {
                    serverResult.addMessage(String.format("'%s' is not installed", command));
                    icon = CheckStatus.Icon.FAIL;
                } else {
                    successCnt++;
                }
            }
        }*/

        if (icon == CheckStatus.Icon.FAIL) {
            serverResult.updateState(successCnt > 0 ? CheckStatus.Icon.WARN : icon, CheckStatus.Result.NotInstalled);
        } else {
            serverResult.updateState(icon, CheckStatus.Result.Installed);
        }
    }
}

package io.playce.roro.prerequisite.server.impl;


import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.dto.prerequisite.CheckStatus;
import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.SSHUtil2;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.prerequisite.config.PrerequisiteConfig;
import io.playce.roro.prerequisite.server.ServerInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractUnixPrerequisite extends AbstractPrerequisite {
    public AbstractUnixPrerequisite(ServerInfo serverInfo, ServerResult serverResult) {
        super(serverInfo, serverResult);
    }

    @Override
    public void checkSoftwares() throws InterruptedException {
        serverResult.increaseStep();

        CheckStatus.Icon icon = CheckStatus.Icon.SUCCESS;

        PrerequisiteConfig.Software software = getSoftware();
        AtomicInteger successCnt = new AtomicInteger();
        serverResult.addStep4MessageList();

        TargetHost targetHost = serverInfo.getHost();
        List<String> commands = software.getCommands().stream().map(c -> String.format("type %s", c)).collect(Collectors.toList());
        try {
            List<RemoteExecResult> results = SSHUtil2.runCommands(targetHost, commands, SSHUtil.isSudoer(targetHost));

            results.forEach(r -> {
                if(r.isErr() || r.getResult().contains("not found")) {
                    serverResult.addMessage(String.format("'%s' is not installed", r.getCommand()));
                } else {
                    successCnt.getAndIncrement();
                }
            });
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            throw new RuntimeException(e);
        }

        int scnt = successCnt.get();
        if (scnt > 0 && successCnt.get() != commands.size()) {
            serverResult.updateState(CheckStatus.Icon.WARN, CheckStatus.Result.NotInstalled);
        } else {
            serverResult.updateState(icon, CheckStatus.Result.Installed);
        }

//        for (String command : software.getCommands()) {
//            try {
//                String result = SSHUtil.executeCommand(serverInfo.getHost(), String.format("type %s", command));
//                log.trace("step4.check software - command: {}, result: {}", command, result);
//                // SSHUtil.executeCommand()의 에러 메시지는 응답으로 전달되지 않음. (/usr/sbin/rmsock not found. 같은 메시지는 로그로만 표시됨)
//                if (StringUtils.isEmpty(result) || result.contains("not found")) {
//                    serverResult.addMessage(String.format("'%s' is not installed", command));
//                    icon = CheckStatus.Icon.FAIL;
//                } else {
//                    successCnt.getAndIncrement();
//                }
//            } catch (Exception e) {
//                if (e instanceof InterruptedException) {
//                    throw e;
//                } else {
//                    // ignore
//                    log.error("step4. check software failed.", e);
//                    serverResult.addMessage(String.format("'%s' file check failed", command));
//                    icon = CheckStatus.Icon.FAIL;
//                }
//            }
//        }
//
//        if (successCnt.get() > 0 && icon == CheckStatus.Icon.FAIL) {
//            serverResult.updateState(CheckStatus.Icon.WARN, CheckStatus.Result.NotInstalled);
//        } else {
//            if (icon == CheckStatus.Icon.SUCCESS) {
//                serverResult.updateState(icon, CheckStatus.Result.Installed);
//            } else {
//                serverResult.updateState(icon, CheckStatus.Result.NotInstalled);
//            }
//        }
    }

    protected abstract PrerequisiteConfig.Software getSoftware();
}

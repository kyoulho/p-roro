package io.playce.roro.prerequisite.server.impl;

import io.playce.roro.asmt.windows.command.PowerShellCommonCommand;
import io.playce.roro.asmt.windows.impl.factory.PowerShellParseUtil;
import io.playce.roro.common.dto.prerequisite.CheckStatus;
import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.prerequisite.server.ServerInfo;
import lombok.extern.slf4j.Slf4j;

import static io.playce.roro.common.util.WinRmUtils.executeCommand;
import static io.playce.roro.common.util.WinRmUtils.executePsShell;

@Slf4j
public class WindowsPrerequisite extends AbstractWindowsPrerequisite {
    public WindowsPrerequisite(ServerInfo serverInfo, ServerResult serverResult) {
        super(serverInfo, serverResult);
    }

    @Override
    public void checkAdminPermission() throws InterruptedException {
        serverResult.increaseStep();
        try {
            String resultMessage = executePsShell(serverInfo.getHost(), PowerShellCommonCommand.CHECK_ADMINISTRATOR).trim();
            log.debug("Check Admin Privileges...");
            log.debug("Execute Command : {}", PowerShellCommonCommand.CHECK_ADMINISTRATOR);

            if (resultMessage.equalsIgnoreCase("true")) {
                log.debug("step3.exist admin privileges: {}", true);
                serverResult.updateState(CheckStatus.Icon.SUCCESS, CheckStatus.Result.Enable);
            } else {
                log.debug("step3.exist admin privileges: {}", false);
                serverResult.updateState(CheckStatus.Icon.WARN, CheckStatus.Result.Disable, resultMessage);
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            serverResult.updateState(CheckStatus.Icon.FAIL, CheckStatus.Result.Failed, e.getMessage());
        }
    }

    @Override
    public void checkSoftwares() throws InterruptedException {
        serverResult.increaseStep();
        final String correctVerifyMessage = "WMI repository is consistent";
        final String wmicServiceName = "Winmgmt";
        final String wmicServiceState = "Running";
        final String warnMessage = "Check WMIC config.";

        try {
            String repositoryResultMessage = executePsShell(serverInfo.getHost(), PowerShellCommonCommand.WMIC_VERIFY_REPOSITORY).trim();
            log.debug("Execute Command : {}", PowerShellCommonCommand.WMIC_VERIFY_REPOSITORY);

            String serviceResultMessage = executeCommand(serverInfo.getHost(), PowerShellCommonCommand.WMIC_SERVICE_COMMAND).trim();
            log.debug("Execute Command : {}", PowerShellCommonCommand.WMIC_SERVICE_COMMAND);

            String serviceName = PowerShellParseUtil.getPropertyValueForMultiLine(serviceResultMessage, "Name");
            String serviceState = PowerShellParseUtil.getPropertyValueForMultiLine(serviceResultMessage, "State");

            log.debug("Result : {}, {}, {}", repositoryResultMessage, serviceName, serviceState);

            if(repositoryResultMessage.equalsIgnoreCase(correctVerifyMessage)
                    && wmicServiceName.equalsIgnoreCase(serviceName) && wmicServiceState.equalsIgnoreCase(serviceState)) {
                log.debug("step4. wmic available : ok.");
                serverResult.updateState(CheckStatus.Icon.SUCCESS, CheckStatus.Result.Okay);
            } else {
                serverResult.updateState(CheckStatus.Icon.WARN, CheckStatus.Result.Disable, warnMessage);
            }

        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            serverResult.updateState(CheckStatus.Icon.WARN, CheckStatus.Result.Disable, e.getMessage());
        }

    }
}

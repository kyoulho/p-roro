package io.playce.roro.asmt.windows.impl;

import io.playce.roro.asmt.windows.command.PowerShellCommonCommand;
import io.playce.roro.common.util.support.TargetHost;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static io.playce.roro.common.util.WinRmUtils.executePsShell;

@Slf4j
public class WindowsCommonExecutor {

    @SneakyThrows
    public static int getPowerShellVersion(TargetHost targetHost) {
        String result = executePsShell(targetHost, PowerShellCommonCommand.POWERSHELL_VERSION).trim();

        log.debug("\n" + PowerShellCommonCommand.POWERSHELL_VERSION);
        log.debug("\n" + result);

        return StringUtils.defaultString(result).equals("") ? 0 : Integer.parseInt(result);
    }

}

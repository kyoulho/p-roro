package io.playce.roro.prerequisite.server.impl;

import io.playce.roro.common.dto.prerequisite.CheckStatus;
import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.prerequisite.server.ServerInfo;
import io.playce.roro.prerequisite.server.ServerPrerequisite;
import io.playce.roro.prerequisite.server.ServerPrerequisiteFactory;
import io.playce.roro.svr.asmt.config.DistributionConfig;
import io.playce.roro.svr.asmt.dto.Distribution;
import io.playce.roro.svr.asmt.util.DistributionChecker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

import static io.playce.roro.common.util.WinRmUtils.executePsShell;

@Slf4j
public class DefaultServerPrerequisiteFactory implements ServerPrerequisiteFactory {
    @Override
    public ServerPrerequisite get(DistributionConfig config, ServerInfo serverInfo, ServerResult serverResult) throws InterruptedException {
        TargetHost targetHost = serverInfo.getHost();
        log.debug("prerequisite target server ip: {}:{}, user: {}", targetHost.getIpAddress(), targetHost.getPort(), targetHost.getUsername());

        ServerPrerequisite result = null;

        if (serverInfo.isWindow()) {
            serverResult.increaseStep();
            log.debug("step0.server type: windows");
            log.debug("step1.connect winrm");

            try {
                log.debug("Execute Command : echo ok");
                String resultMessage = executePsShell(targetHost, "echo ok");
                log.debug("Result Message : {}", resultMessage);

                serverResult.updateState(CheckStatus.Icon.SUCCESS, CheckStatus.Result.Okay);
                log.debug("step2.authorized : oK.");
                serverResult.increaseStep();
                serverResult.updateState(CheckStatus.Icon.SUCCESS, CheckStatus.Result.Okay, "Authorized");

                result = new WindowsPrerequisite(serverInfo, serverResult);
            } catch (Exception e) {
                // 실패하면 다음 Step은 진행하지 않는다.
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String failMessage = sw.toString();
                log.error(failMessage);

                if (failMessage.contains("401") || failMessage.contains("privilege")) {
                    // 접속은 성공한 경우.
                    serverResult.updateState(CheckStatus.Icon.SUCCESS, CheckStatus.Result.Okay, StringUtils.EMPTY, false);
                    log.debug("Fail : Auth failed. Please check Username/Password or Auth Type or Administrator Privileges.");
                    failMessage = "Auth failed. Please check Username/Password or Auth Type or Administrator Privileges.";
                    serverResult.increaseStep();
                    serverResult.updateState(CheckStatus.Icon.FAIL, CheckStatus.Result.Failed, failMessage);
                } else if (failMessage.contains("Connection refused") || failMessage.contains("Timeout")) {
                    log.debug("Fail : Connection refused or timeout.");
                    failMessage = "WinRM connection refused or timeout. Please check Server IP or Port or Firewall";
                    serverResult.updateState(CheckStatus.Icon.FAIL, CheckStatus.Result.Failed, failMessage);
                } else {
                    log.debug("Fail : Check WinRM Config");
                    failMessage = "WinRM Connection Error. Please check server information or WinRM Config.";
                    serverResult.updateState(CheckStatus.Icon.FAIL, CheckStatus.Result.Failed, failMessage);
                }

                return null;
            }

        } else {
            serverResult.increaseStep();
            String uname;
            try {
                uname = SSHUtil.executeCommand(serverInfo.getHost(), "uname", null, false).trim();

                if (StringUtils.isNotEmpty(uname)) {
                    serverResult.updateState(CheckStatus.Icon.SUCCESS, CheckStatus.Result.Okay, uname);
                } else {
                    throw new RoRoException("uname is empty.");
                }
            } catch (Exception e) {
                RoRoException.checkInterruptedException(e);
                uname = "";
                serverResult.updateState(CheckStatus.Icon.FAIL, CheckStatus.Result.Failed, e.getMessage());
            }

            log.debug("step0.server type: {}", uname);
            log.debug("step1.connect ssh");
            if (serverResult.getIcon(1).equals(CheckStatus.Icon.SUCCESS) && StringUtils.isNotEmpty(uname)) {
                serverResult.increaseStep();
                serverResult.updateState(CheckStatus.Icon.SUCCESS, CheckStatus.Result.Okay, "Authorized");
            } else {
                String compareMessage = "Auth fail";
                if (serverResult.getMessage(1).contains(compareMessage)) {
                    serverResult.updateState(CheckStatus.Icon.SUCCESS, CheckStatus.Result.Okay, StringUtils.EMPTY, false);
                    serverResult.increaseStep();
                    serverResult.updateState(CheckStatus.Icon.FAIL, CheckStatus.Result.Failed, compareMessage);
                }

                return null;
            }
            log.debug("step2.authorized");

            if (uname.contains("Linux")) {
                result = findDistribution(config, serverInfo, serverResult);
            } else if (uname.contains("AIX")) {
                result = new AIXPrerequisite(serverInfo, serverResult);
            } else if (uname.contains("HP-UX")) {
                result = new HPUXPrerequisite(serverInfo, serverResult);
            } else if (uname.contains("SunOS")) {
                result = new SunOSPrerequisite(serverInfo, serverResult);
            }
        }

        if (result == null) {
            serverResult.updateState(CheckStatus.Icon.FAIL, CheckStatus.Result.Failed, "Unsupported OS: " + serverInfo.getHost());
            // throw new RoRoException("Unsupported OS: " + serverInfo);
        }
        return result;
    }

    private ServerPrerequisite findDistribution(DistributionConfig config, ServerInfo serverInfo, ServerResult serverResult) throws InterruptedException {
//        String distribution = SSHUtil.executeCommand(serverInfo.getHost(), "cat /etc/os-release | egrep '^ID_LIKE='", null, false);
//        if (distribution.length() == 0) {
//            return null;
//        }
//        distribution = extractName(distribution.trim());
        Distribution dist = DistributionChecker.getDistribution(config, serverInfo.getHost(), false);
        String distribution = dist.getOsFamily();

        log.debug("step0.linux distribution: {}", distribution);
        if ("debian".equalsIgnoreCase(distribution) || "ubuntu".equalsIgnoreCase(distribution)) {
            return new DebianPrerequisite(serverInfo, serverResult);
        }
        return new RhelPrerequisite(serverInfo, serverResult);
    }

    /*private String extractName(String distribution) {
        distribution = distribution.substring(distribution.indexOf("=") + 1);
        if (distribution.length() > 2 && distribution.contains("\"")) {
            distribution = distribution.substring(1, distribution.length() - 1);
        }
        return distribution;
    }*/
}

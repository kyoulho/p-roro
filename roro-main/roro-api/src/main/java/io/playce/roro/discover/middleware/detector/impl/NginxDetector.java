package io.playce.roro.discover.middleware.detector.impl;

import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.code.Domain1102;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.discover.middleware.detector.AbstractDetector;
import io.playce.roro.discover.middleware.dto.DetectResultInfo;
import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.discover.middleware.scenario.nginx.NginxEnginePathScenario;
import io.playce.roro.discover.middleware.scenario.nginx.NginxExecutePathScenario;
import io.playce.roro.discover.middleware.scenario.nginx.NginxInstancePathScenario;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static io.playce.roro.common.util.StringUtil.splitToArrayByCrlf;

@Slf4j
public class NginxDetector extends AbstractDetector {

    public NginxDetector(Process process) {
        super(process);
    }

    @Override
    public DetectResultInfo generateMiddleware(TargetHost targetHost, InventoryProcessConnectionInfo connectionInfo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {

        String enginePath = generateEnginePathScenario(targetHost).execute(process, commandConfig, strategy);
        String nginxExecutePath = generateExecutePathScenario(targetHost).execute(process, commandConfig, strategy);
        String configPath = generateInstancePathScenario(targetHost, nginxExecutePath).execute(process, commandConfig, strategy);

        return DetectResultInfo.builder()
                .vendor(Domain1013.NGINX.enname())
                .mwDetailType(Domain1013.NGINX)
                .mwType(Domain1102.WEB)
                .pid(process.getPid())
                .runUser(getRunUser(strategy, targetHost, process))
                .version(getVersion(commandConfig, strategy, targetHost, nginxExecutePath))
                .enginePath(enginePath)
                .domainPath(configPath)  // config file location.
                .instancePath(configPath)
//                .javaVersion(null)
                .build();
    }

    private ExtractScenario generateEnginePathScenario(TargetHost targetHost) {
        return new NginxEnginePathScenario(targetHost);
    }

    private ExtractScenario generateExecutePathScenario(TargetHost targetHost) {
        return new NginxExecutePathScenario(targetHost);
    }

    private ExtractScenario generateInstancePathScenario(TargetHost targetHost, String nginxExecutePath) {
        return new NginxInstancePathScenario(targetHost, nginxExecutePath);
    }

    private String getVersion(CommandConfig commandConfig, GetInfoStrategy strategy, TargetHost targetHost, String nginxExecutePath) throws InterruptedException {
        String version = MWCommonUtil.getExecuteResult(targetHost, COMMAND.NGINX_VERSION, commandConfig, strategy, nginxExecutePath);
        String[] lineArrays = splitToArrayByCrlf(version);

        for (String line : lineArrays) {
            if (line.contains("version")) {
                return line.substring(line.indexOf("/") + 1);
            }
        }

        return StringUtils.EMPTY;
    }

    private String getRunUser(GetInfoStrategy strategy, TargetHost targetHost, Process process) throws InterruptedException {
        if (!strategy.isWindows()) {
            String pidCommand = "sudo ps -ef | grep worker | grep " + process.getPid() + " | awk '{print $1}' | head -1";
            return StringUtils.defaultString(SSHUtil.executeCommand(targetHost, pidCommand), process.getUser());
        }

        return process.getUser();
    }

}

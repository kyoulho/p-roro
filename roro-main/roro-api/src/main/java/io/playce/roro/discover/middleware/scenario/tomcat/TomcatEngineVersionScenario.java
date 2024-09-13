package io.playce.roro.discover.middleware.scenario.tomcat;

import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.mw.asmt.util.MWCommonUtil;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class TomcatEngineVersionScenario {
    public static class Step1 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            String processName = process.getName();
            int index = processName.indexOf(".");
            if (index > -1) {
                processName = processName.substring(0, index);
            }

            try {
                String version = MWCommonUtil.getExecuteResult(targetHost, COMMAND.TOMCAT_VERSION, commandConfig, strategy, processName);
                int start = version.indexOf("Tomcat");
                int end = version.lastIndexOf("Server");
                if (start > -1 && end > -1) {
                    result = StringUtils.strip(version.substring(start + 6, end));
                }
//                result = MWCommonUtil.getExecuteResult(targetHost, COMMAND.TOMCAT_VERSION_SERVICE, commandConfig, strategy, processName);
            } catch (InterruptedException e) {
                log.error("version not found - {}", e.getMessage());
                return false;
            }
            return StringUtils.isNotEmpty(result);
        }
    }
}
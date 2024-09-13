/*
 * Copyright 2022 The Playce-RoRo Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       1월 28, 2022            First Draft.
 */
package io.playce.roro.discover.middleware.detector;

import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public abstract class AbstractDetector implements MiddlewareDetector {

    protected Process process;

    public AbstractDetector(Process process) {
        this.process = process;
    }

    protected static String getJavaPath(List<String> array) {
        for (String param : array) {
            if (StringUtils.isEmpty(param))
                continue;

            if (param.charAt(0) != '-' && (param.contains("java") || param.contains("jvm.dll"))) {

                param = param.replaceAll("server\\\\jvm.dll", "java");

                return param;
            }
        }
        return null;
    }

    protected static String getJavaVersion(List<String> commands, TargetHost targetHost, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        String javaPath = getJavaPath(commands);
//
//        String version = null;
//        if (javaPath != null) {
//            String command = javaPath + " -version 2>&1 | head -n 1 | awk -F '\"' '{print $2}'";
//            version = SSHUtil.executeCommand(targetHost, command);
//            version = version.replace(strategy.getLineFeed(), "");
//        }
//        return version;
        String command = COMMAND.JAVA_VERSION.command(commandConfig, strategy.isWindows(), javaPath);
        return strategy.executeCommand(targetHost, command, COMMAND.JAVA_VERSION);
    }

}
//end of AbstractDetector.java
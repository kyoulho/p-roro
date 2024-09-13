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
 * Hoon Oh       2월 15, 2022            First Draft.
 */
package io.playce.roro.discover.middleware.scenario.apache;

import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.util.GetInfoStrategy;
import io.playce.roro.svr.asmt.dto.common.processes.Process;
import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
public class ApacheHttpdParentDirScenario extends ExtractScenario {

    @Override
    protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
        String separator = strategy.getSeparator();

        String enginePath = process.getCmd().get(0);
        String httpdParentDir = null;
        if (enginePath.contains(separator + "sbin")) {
            // Install by yum
            if (StringUtils.isNotEmpty(enginePath)) {
                httpdParentDir = separator + "sbin";
            }
        } else if (enginePath.contains(separator + "bin")) {
            if (StringUtils.isNotEmpty(enginePath)) {
                httpdParentDir = separator + "bin";
            }
        }

        result = httpdParentDir;
        return StringUtils.isNotEmpty(result);
    }
}
//end of ApacheHttpdParentDirScenario.java
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
 * Hoon Oh       1월 27, 2022            First Draft.
 */
package io.playce.roro.discover.middleware.scenario.jeus;

import io.playce.roro.discover.middleware.scenario.ExtractScenario;
import io.playce.roro.discover.server.util.ProcessCmdUtil;
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
public class JeusNameScenario {

    // 1. -server 옵션 뒤 인자 중 -로 시작하는 않는 값을 추출
    public static class Step1 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            int svrNameIdx = ProcessCmdUtil.getIndexFromString(process.getCmd(), "-server");
            if (svrNameIdx > -1) {
                result = process.getCmd().get(svrNameIdx + 1);
                if (result.startsWith("-")) {
                    result = null;
                }
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    //2. 프로세스 Command 중 EngineContainerBootstrapper가 있다면 뒤의 첫 번째 인자를 이름으로 설정
    public static class Step2 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            int svrNameIdx = ProcessCmdUtil.getIndexFromString(process.getCmd(), "jeus.server.enginecontainer.EngineContainerBootstrapper");
            if (svrNameIdx > -1) {
                result = process.getCmd().get(svrNameIdx + 1);
            }

            return StringUtils.isNotEmpty(result);
        }
    }

    //3.-D로 시작하는 옵션 중 =가 없는 옵션을 이름으로 설정 (eg. -DContKDIPUSHADM_01)
    public static class Step3 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            for (int i = 0; i < process.getCmd().size(); i++) {
                String param = process.getCmd().get(i);

                if (param.startsWith("-D") && !param.contains("=")) {
                    result = param.replaceAll("-D", StringUtils.EMPTY);
                    break;
                }
            }
            return StringUtils.isNotEmpty(result);
        }
    }

    // 4. 프로세스 Command 중 jeus.server.JeusBootstrapper가 있고, -U, -P 옵션이 있는 경우 adminServer로 이름을 설정
    public static class Step4 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            boolean hasUser = false;
            boolean hasPassword = false;
            if (ProcessCmdUtil.getIndexFromString(process.getCmd(), "jeus.server.JeusBootstrapper") > -1) {
                for (int i = 0; i < process.getCmd().size(); i++) {
                    String param = process.getCmd().get(i);

                    if (param.startsWith("-U") && !param.contains("=")) {
                        hasUser = true;
                    }

                    if (param.startsWith("-P") && !param.contains("=")) {
                        hasPassword = true;
                    }

                    if (hasUser && hasPassword) {
                        break;
                    }
                }
            }

            if (hasUser && hasPassword) {
                result = "adminServer";
            }
            return StringUtils.isNotEmpty(result);
        }
    }

    // 5. 프로세스 command 중 jeus.server.admin.DomainAdminServerBootstrapper가 있는 경우 adminServer로 이름을 설정
    public static class Step5 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            if (ProcessCmdUtil.getIndexFromString(process.getCmd(), "jeus.server.admin.DomainAdminServerBootstrapper") > -1) {
                result = "adminServer";
            }
            return StringUtils.isNotEmpty(result);
        }
    }

    // 6. 프로세스 command 중 jeus.server.NodemanagerBootstrapper가 있는 경우 nodeManager로 이름을 설정
    public static class Step6 extends ExtractScenario {

        @Override
        protected boolean extract(Process process, CommandConfig commandConfig, GetInfoStrategy strategy) {
            if (ProcessCmdUtil.getIndexFromString(process.getCmd(), "jeus.server.NodemanagerBootstrapper") > -1) {
                result = "nodeManager";
            }
            return StringUtils.isNotEmpty(result);
        }
    }


}
//end of Step1.java
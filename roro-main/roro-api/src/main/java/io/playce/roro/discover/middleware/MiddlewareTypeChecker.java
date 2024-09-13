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
package io.playce.roro.discover.middleware;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public enum MiddlewareTypeChecker {
    TOMCAT("Tomcat", List.of("catalina.base", "catalina.home")),
    JEUS("Jeus", List.of("jeus.server")),
    WEBLOGIC("Weblogic", List.of("weblogic.Server")),
    WEBSPHERE("WebSphere", List.of("was.install.root")),
    APACHE("Apache", List.of("httpd", "httpd.exe", "apache2")),
    //    WEBTOB("WebToB", List.of("wsm", "hth", "thl"));
    WEBTOB("WebToB", List.of("wsm")),
    JBOSS("JBoss", List.of("jboss.server.base.dir", "jboss.domain.base.dir")),
    NGINX("Nginx", List.of("nginx"));


    private String type;
    private List<String> params;

    MiddlewareTypeChecker(String type, List<String> params) {
        this.type = type;
        this.params = params;
    }

    public static MiddlewareTypeChecker isContain(List<String> cmdList) {
        MiddlewareTypeChecker result;
        for (MiddlewareTypeChecker checker : MiddlewareTypeChecker.values()) {
            switch (checker) {
                case APACHE:
                case WEBTOB:
                    result = checkFirstCommand(checker, cmdList.get(0));
                    break;
                case NGINX:
                    result = checkMasterProcessCommand(checker, cmdList);
                    break;
                default:
                    result = checkCommands(checker, cmdList);
            }
            if (result != null) {
                return result;
            }
//            if(checker == APACHE || checker == WEBTOB) {
//                for(String param: checker.params) {
//                    String firstOfCmdList = cmdList.get(0);
//                    if(firstOfCmdList != null && firstOfCmdList.endsWith(param)) {
//                        return checker;
//                    }
//                }
//            } else {
//                for (String param : cmdList) {
//                    if (param != null && checker.hasParam(param)) {
//                        return checker;
//                    }
//                }
//            }
        }
        return null;
    }

    private static MiddlewareTypeChecker checkCommands(MiddlewareTypeChecker checker, List<String> cmdList) {
        for (String command : cmdList) {
            if (StringUtils.isEmpty(command))
                continue;

            for (String checkValue : checker.params) {
                if (command.contains(checkValue)) {
                    log.debug("{} => {}", checkValue, cmdList);
                    return checker;
                }
            }
        }
        return null;
    }

    private static MiddlewareTypeChecker checkMasterProcessCommand(MiddlewareTypeChecker checker, List<String> cmdList) {
        if (cmdList.contains("master")) {
            for (String cmd : cmdList) {
                for (String checkValue : checker.params) {
                    if (cmd.contains(checkValue)) {
                        log.debug("nginx checker : {} => {}", checkValue, cmdList);
                        return checker;
                    }
                }
            }
        }

        return null;
    }

    private static MiddlewareTypeChecker checkFirstCommand(MiddlewareTypeChecker checker, String firstCommand) {
        if (StringUtils.isEmpty(firstCommand))
            return null;

        for (String checkValue : checker.params) {
            // Apache, WebToB 프로세스 첫 번째 command의 마지막을 비교한다. (패스 상에 존재하는 경우 제외)
            if (firstCommand.endsWith(checkValue)) {
                log.debug("{} => {}", checkValue, firstCommand);
                return checker;
            }
        }
        return null;
    }

    public boolean hasParam(String param) {
        return params.stream().anyMatch(param::contains);
    }

    public String getType() {
        return type;
    }
}
//end of MiddlewareTypeChecker.java
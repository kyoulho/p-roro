/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Hoon Oh          3월 02, 2021		First Draft.
 */
package io.playce.roro.mw.asmt.jeus.enums;

import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 2.0.0
 */
public enum MiddlewareChecker {
    TOMCAT("Tomcat", Arrays.asList("catalina.base", "catalina.home")),
    JEUS("Jeus", Arrays.asList("jeus.server")),
    WEBLOGIC("Weblogic", Arrays.asList("weblogic.Server")),
    WEBSPHERE("WebSphere", Arrays.asList("was.install.root")),
    APACHE("Apache", Arrays.asList("httpd")),
    WEBTOB("WebToB", Arrays.asList("wsm", "hth", "thl")) ,
    JBOSS("JBoss" , Arrays.asList("jboss.home.dir" , "jboss.server.base.dir", "jboss.domain.base.dir"));

    private String type;
    private List<String> params;

    MiddlewareChecker(String type, List<String> params) {
        this.type = type;
        this.params = params;
    }

    public static MiddlewareChecker isContain(Object[] params) {
        MiddlewareChecker[] values = MiddlewareChecker.values();
        for (MiddlewareChecker value : values) {
            for (Object param : params) {
                if (value.hasParam(param.toString())) {
                    return value;
                }
            }
        }
        return null;
    }

    public boolean hasParam(String param) {
        return params.stream().anyMatch(m -> param.indexOf(m) > -1);
    }

    public String getType() {
        return type;
    }
}
//end of SupportMiddlewareClue.java
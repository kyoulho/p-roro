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
 * Jhpark       8월 10, 2022            First Draft.
 */
package io.playce.roro.mw.asmt.jboss.enums;

import org.jetbrains.annotations.NotNull;

/**
 * <pre>
 *
 * </pre>
 *
 * @author jhpark
 * @version 3.0
 */
public enum ENGINE {
     STANDALONE_PROVIDER("org.jboss.as.standalone"),
     DOMAIN_PROVIDER ("org.jboss.as.host-controller"),
     STANDALONE_NAME("Standalone"),
     DOMAIN_NAME("Domain"),
     DSERVER("-DSERVER="),
     HOST_CONTROLLER("Host Controller"),
     VENDOR("Red Hat"),
     HOSTCONFIG("host-config"),

     DOMAINCONFIG("domain-config"),

     STANDALONECONF("-c");

    private final String codename;

    public String codeName() {
        return codename;
    }

    ENGINE(String codename) {
        this.codename = codename;
    }
}
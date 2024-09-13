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
 * Jhpark       8월 05, 2022            First Draft.
 */
package io.playce.roro.mw.asmt.jboss.enums;

/**
 * <pre>
 *
 * </pre>
 *
 * @author jhpark
 * @version 3.0
 */
public enum NODE {
    CONNECTOR("/socket-binding-group"),

    DOMAINCONNECTORS("/socket-binding-groups/socket-binding-group"),
    RESOURCE("/profile"),

    PROFILES("/profiles"),

    SUBSYSTEM("/subsystem"),

    MANAGEMENT("/management"),

    SERVERS("/servers"),

    JVMS("/jvms"),

    SERVERGROUPS("/server-groups"),

    INTERFACES("/interfaces"),

    INTERFACE("/interface"),

    DOMAINRESOURCE("/profiles/profile"),

    DEPLOY("/deployments"),
    DEPLOYPATH(DEPLOY + "/fs-exploded"),
    CONTEXT("/Context"),
    HOMEDIR("home.dir"),
    BASEDIR("base.dir"),

    PORTOFFSET("port-offset"),
    EXTENSIONS("/extensions");


    private final String path;

    NODE(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
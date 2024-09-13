/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * Dong-Heon Han    Feb 04, 2022		First Draft.
 */

package io.playce.roro.common.code;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
public enum Domain1013 {
    LINUX("LINUX"),
    AIX("AIX"),
    SUNOS("Solaris"),
    HP_UX("HP UX"),
    WINDOWS("Windows"),
    ORACLE("Oracle"),
    MYSQL("MySQL"),
    MARIADB("MariaDB"),
    POSTGRE("PostgreSQL"),
    MSSQL("MSSQL"),
    TIBERO("Tibero"),
    SYBASE("Sybase"),
    EAR("EAR"),
    WAR("WAR"),
    JAR("JAR"),
    WSPHERE("Websphere"),
    WEBLOGIC("Weblogic"),
    TOMCAT("Tomcat"),
    JEUS("Jeus"),
    APACHE("Apache"),
    WEBTOB("WebToB"),
    JBOSS("JBoss"),
    NGINX("Nginx"),
    ETC("Etc");

    private final String enname;
    private final String prefix;

    Domain1013(String enname) {
        this.enname = enname;
        this.prefix = enname.toLowerCase().replaceAll(" ", "").toUpperCase();
    }

    public String enname() {
        return enname;
    }

    public String prefix() {
        return prefix;
    }
}
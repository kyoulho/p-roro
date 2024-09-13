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
 * Dong-Heon Han    Feb 12, 2022		First Draft.
 */

package io.playce.roro.mw.asmt.enums;

import io.playce.roro.mw.asmt.component.CommandConfig;
import lombok.RequiredArgsConstructor;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@RequiredArgsConstructor
public enum COMMAND {
    CAT,
    CAT_QUOTATION,
    FILE_EXISTS,
    JAVA_VERSION_WITH_JAVAHOME, //websphere
    JAVA_VERSION_WITH_JAVAPATH,
    JAVA_VENDOR_WITH_JAVAHOME,
    JAVA_VENDOR_WITH_JAVAPATH,
    JAVA_VERSION,
    JAVA_VENDOR,
    PROCESS_STATUS, //websphere
    PROCESS_ARGUMENT, //jeus, websphere
    RUN_USER, //jeus, weblogic, websphere
    JAVA_HOME_COMMAND,
    CELL_COMMAND_CHECK_DIRECTORY, //websphere
    CELL_COMMAND_EMPTY_DIRECTORY,
    LS_FILES,
    GET_USER_BY_PID,
    FILE_LIST_ONE_LINE,
    TOMCAT_JAVA_PATH,
    TOMCAT_CONFIG_SERVER,
    TOMCAT_CONFIG_ENV,
    TOMCAT_CONFIG_CONTEXT,
    TOMCAT_CONFIG_SETENV,
    CHECK_PATH,
    TOMCAT_VERSION,
    TOMCAT_NUMBER,
    TOMCAT_VERSION_SERVICE,
    TOMCAT_RELEASE_NOTE,
    TOMCAT_VMOPTION,
    TOMCAT_DIRECTORY,
    TOMCAT_RUN_USER,
    APACHE_RUN_USER,
    APACHE_RUN_USER1,

    JAVA_PATH,
    GET_PROCESS,
//    WEBLOGIC_CHECK_PROCESS,
//    WEBLOGIC_DOMAIN,
//    WEBLOGIC_PROCESS_NAME,
//    WEBLOGIC_PROCESS_LINK,

    JEUS_VERSION,
    JEUS_PROCESS,
    EXECUTED_TIME,
    FIND_FILE_WITH_PATH1,
    FIND_FILE_WITH_PATH2,


    JEUS_EXECUTED_TIME,
    APACHE_EXECUTED_TIME,
    APACHE_EXECUTED_TIME_NOT_MONITOR,
    // JEUS_CHECK_DIR,
    JEUS_DEPLOYED_DATE,
    APACHE_VERSION,
    APACHE_DUMP,
    JEUS_DOMAIN_PATH_SCENARIO_STEP31,
    JEUS_DOMAIN_PATH_SCENARIO_STEP32,
    JEUS_VERSION_SCENARIO1,
    JEUS_VERSION_SCENARIO2,
    JEUS_VERSION_SCENARIO3,

    WEBTOB_ENGINE_PATH_SCENARIO_STEP1,
    WEBTOB_ENGINE_PATH_SCENARIO_STEP21,
    WEBTOB_ENGINE_PATH_SCENARIO_STEP22,
    WEBTOB_ENGINE_PATH_SCENARIO_STEP51,
    WEBTOB_ENGINE_PATH_SCENARIO_STEP31,
    WEBTOB_ENGINE_PATH_SCENARIO_STEP32,
    COMMAND_WHICH,
    WEBTOB_VERSION,
    WEBTOB_RUN_USER,
    WEBTOB_FIND_CONFIG_FILE1,
    WEBTOB_FIND_CONFIG_FILE2,
    GET_USERNAME_FROM_UID,
    GET_USERNAME_FROM_USER,
    JBOSS_VERSION,
    JBOSS_VMOPTION,
    JBOSS_VERSION_NOTE,
    JBOSS_STANDALONE_XML,
    JBOSS_DOMAIN_XML,
    JBOSS_DOMAIN_HOST_XML,
    JBOSS_STANDALONE_SETUP_ENV,
    JBOSS_WEB_XML,
    JBOSS_JAVA_PATH,
    JBOSS_RUN_USER,
    JBOSS_RUN_SERVER,
    JBOSS_PROCESS_RUN_SERVER,
    JBOSS_CURRENT_SEVER_VMOPTION,
    JBOSS_LISTEN_PORT,
    NGINX_MASTER_PROCESS_PID,
    NGINX_VERSION,
    NGINX_CONFIG_FILE;

    public String command(CommandConfig commandConfig, boolean windows, Object... bindVariables) {
        String cmd = command(commandConfig, windows);
        return String.format(cmd, bindVariables);
    }

    public String command(CommandConfig commandConfig, boolean windows) {
        if (windows) {
            return commandConfig.getWindows().get(this);
        }

        return commandConfig.getUnix().get(this);
    }
}
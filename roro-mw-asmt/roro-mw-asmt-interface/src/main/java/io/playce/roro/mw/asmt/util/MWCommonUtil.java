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
 * Dong-Heon Han    Jun 08, 2022		First Draft.
 */

package io.playce.roro.mw.asmt.util;

import com.jcraft.jsch.JSchException;
import io.playce.roro.common.dto.common.RemoteExecResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mw.asmt.component.CommandConfig;
import io.playce.roro.mw.asmt.enums.COMMAND;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Slf4j
public class MWCommonUtil {

    public static final String ORACLE_JAVA_VENDOR = "Oracle Corporation";

    public static Map<String, RemoteExecResult> executeCommand(TargetHost targetHost, Map<String, String> commandMap, boolean sudo, GetInfoStrategy strategy) throws InterruptedException {
        try {
            return strategy.runCommands(targetHost, commandMap, sudo);
        } catch (JSchException | IOException e) {
            RoRoException.checkInterruptedException(e);
            log.error(e.getMessage());
            throw new RoRoException(e.getMessage());
        }
    }

    public static RemoteExecResult executeCommand(TargetHost targetHost, COMMAND command, CommandConfig commandConfig, GetInfoStrategy strategy, boolean sudo, Object ... args) throws InterruptedException {
        return MWCommonUtil.executeCommand(targetHost, Map.of(command.name(), command.command(commandConfig, strategy.isWindows(), args)), sudo, strategy).get(command.name());
    }

    public static String getJavaVersion(TargetHost targetHost, RemoteExecResult result, boolean sudo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        if(result.isErr()) {
            return getJavaVersionFromJAVA_HOME(targetHost, commandConfig, strategy);
        } else {
            String javaPath = result.getResult().trim();
            log.debug("finded java path: {}", javaPath);
            Map<String, String> commandMap = Map.of(COMMAND.JAVA_VERSION.name(), COMMAND.JAVA_VERSION.command(commandConfig, strategy.isWindows(), javaPath));
            Map<String, RemoteExecResult> resultMap = executeCommand(targetHost, commandMap, sudo, strategy);
            result = resultMap.get(COMMAND.JAVA_VERSION.name());
            if (!result.isErr()) {
                return result.getResult().trim();
            }
        }
        return null;
    }

    public static String getJavaVendor(TargetHost targetHost, RemoteExecResult result, boolean sudo, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        if(result.isErr()) {
            return getJavaVersionFromJAVA_VENDOR(targetHost, commandConfig, strategy);
        } else {
            String javaPath = result.getResult().trim();
            log.debug("finded java path: {}", javaPath);

            Map<String, String> commandMap = Map.of(COMMAND.JAVA_VENDOR.name(), COMMAND.JAVA_VENDOR.command(commandConfig, strategy.isWindows(), javaPath));
            Map<String, RemoteExecResult> resultMap = executeCommand(targetHost, commandMap, sudo, strategy);
            result = resultMap.get(COMMAND.JAVA_VENDOR.name());
            if (!result.isErr()) {
                return getJavaVendorProperty(result.getResult().trim());
            }
        }

        return null;
    }

    public static String getJavaVersionFromJAVA_HOME(TargetHost targetHost, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        String command = COMMAND.JAVA_VERSION_WITH_JAVAHOME.command(commandConfig, strategy.isWindows());
        return strategy.executeCommand(targetHost, command, COMMAND.JAVA_VERSION_WITH_JAVAHOME);
    }

    public static String getJavaVersionFromJAVA_VENDOR(TargetHost targetHost, CommandConfig commandConfig, GetInfoStrategy strategy) throws InterruptedException {
        String command = COMMAND.JAVA_VENDOR_WITH_JAVAHOME.command(commandConfig, strategy.isWindows());
        return getJavaVendorProperty(strategy.executeCommand(targetHost, command, COMMAND.JAVA_VENDOR_WITH_JAVAHOME));
    }

    public static String getJavaVendorProperty(String javaVendorProperties) {
        String vendorName = "";

        if (StringUtils.isNotEmpty(javaVendorProperties)) {
            String[] vendor = javaVendorProperties.split("\\r?\\n");

            for (String tempString : vendor) {
                if (tempString.contains("java.vendor =")) {
                    vendorName = tempString.substring(tempString.lastIndexOf("=") + 1).trim();
                }
            }
        }

        return vendorName;
    }

    public static String getExecuteResult(TargetHost targetHost, COMMAND cmd, CommandConfig commandConfig, GetInfoStrategy strategy, Object ... args) throws InterruptedException {
        String command = cmd.command(commandConfig, strategy.isWindows(), args);
        return strategy.executeCommand(targetHost, command, cmd);
    }

    public static String extractUser(String runUser) {
        if(StringUtils.isEmpty(runUser)) return StringUtils.EMPTY;
        String[] splitValue = runUser.split("=");
        if(splitValue.length < 2) return StringUtils.EMPTY;

        return StringUtils.strip(splitValue[1], StringUtils.SPACE + ";'\"");
    }

    /*
     * org.json.simple object의 내용을 String value로 리턴
     */
    public static String getStringValue(JSONObject obj, String key) {
        if (obj.containsKey(key)) {
            return obj.get(key) + StringUtils.EMPTY;
        }

        return StringUtils.EMPTY;
    }

    public static Long getLongValue(JSONObject obj, String key) {
        String value = getStringValue(obj, key);
        if(StringUtils.isNotEmpty(value)) {
            return Long.parseLong(value);
        }
        return null;
    }

    public static Boolean getBooleanValue(JSONObject obj, String key) {
        String value = getStringValue(obj, key);
        if(StringUtils.isNotEmpty(value)) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }
}
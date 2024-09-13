/*
 * Copyright 2020 The Playce-RoRo Project.
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
 * SangCheon Park   Oct 29, 2020		First Draft.
 */
package io.playce.roro.common.property;

import io.playce.roro.common.setting.SettingsHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.playce.roro.common.setting.SettingsConstants.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonProperties implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static Environment environment;
    private static String applicationVersion;
    private static String workDir;
    private static Boolean usePty;

    private static String startWhoamiCmd;
    private static String endWhoamiCmd;
    private static String startCustomCmd;
    private static String endCustomCmd;
    private static String cmdError;
    private static Boolean useBouncyCastleProvider;
    private static String preferredAuthentications;
    private static List<String> skipMessages;
    private static String timeout;
    private static String windowsTempDir;
    private static Boolean useOutFile;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CommonProperties.applicationContext = applicationContext;
        environment = applicationContext.getEnvironment();
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static String getProperty(String key) {
        String value = null;

        if (environment != null) {
            value = environment.getProperty(key);
        }

        if (value == null) {
            value = System.getProperty(key);
        }

        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);

        if (StringUtils.isEmpty(value)) {
            value = defaultValue;
        }

        return value;
    }

    @Value("${roro.app-version}")
    private void setApplicationVersion(String appVersion) {
        CommonProperties.applicationVersion = appVersion;
    }

    public static String getApplicationVersion() {
        return applicationVersion;
    }

    @Value("${roro.working.dir-path}")
    private void setWorkDir(String value) {
        CommonProperties.workDir = value;
    }

    public static String getWorkDir() {
        if (StringUtils.isEmpty(workDir)) {
            return FileUtils.getTempDirectory().getAbsolutePath();
        } else {
            return workDir;
        }
    }

    @Value("${ssh.pseudo.tty:true}")
    private void setUsePty(Boolean enabled) {
        CommonProperties.usePty = enabled;
    }

    public static Boolean getUsePty() {
        if (usePty == null) {
            usePty = true;
        }

        return usePty;
    }

    @Value("${ssh.cmd.whoami.start:RORO:WHOAMI:CMD:START}")
    private void setStartWhoamiCmd(String startWhoamiCmd) {
        CommonProperties.startWhoamiCmd = startWhoamiCmd;
    }

    public static String getStartWhoamiCmd() {
        if (StringUtils.isEmpty(startWhoamiCmd)) {
            startWhoamiCmd = "RORO:WHOAMI:CMD:START";
        }

        return startWhoamiCmd;
    }

    @Value("${ssh.cmd.whoami.end:RORO:WHOAMI:CMD:END}")
    private void setEndWhoamiCmd(String endWhoamiCmd) {
        CommonProperties.endWhoamiCmd = endWhoamiCmd;
    }

    public static String getEndWhoamiCmd() {
        if (StringUtils.isEmpty(endWhoamiCmd)) {
            endWhoamiCmd = "RORO:WHOAMI:CMD:END";
        }

        return endWhoamiCmd;
    }

    @Value("${ssh.cmd.custom.start:RORO:CUSTOM:CMD:START}")
    private void setStartCustomCmd(String startCustomCmd) {
        CommonProperties.startCustomCmd = startCustomCmd;
    }

    public static String getStartCustomCmd() {
        if (StringUtils.isEmpty(startCustomCmd)) {
            startCustomCmd = "RORO:CUSTOM:CMD:START";
        }

        return startCustomCmd;
    }

    @Value("${ssh.cmd.custom.end:RORO:CUSTOM:CMD:END}")
    private void setEndCustomCmd(String endCustomCmd) {
        CommonProperties.endCustomCmd = endCustomCmd;
    }

    public static String getEndCustomCmd() {
        if (StringUtils.isEmpty(endCustomCmd)) {
            endCustomCmd = "RORO:CUSTOM:CMD:END";
        }

        return endCustomCmd;
    }

    @Value("${ssh.cmd.error:RORO:CMD:ERROR}")
    private void setCmdError(String cmdError) {
        CommonProperties.cmdError = cmdError;
    }

    public static String getCmdError() {
        if (StringUtils.isEmpty(cmdError)) {
            cmdError = "RORO:CMD:ERROR";
        }

        return cmdError;
    }

//    @Value("${ssh.use.bouncy.castle.provider:false}")
//    private void setUseBouncyCastleProvider(String useBouncyCastleProvider) {
//        if (StringUtils.isNotEmpty(useBouncyCastleProvider)) {
//            CommonProperties.useBouncyCastleProvider = Boolean.valueOf(useBouncyCastleProvider);
//        }
//    }

    public static Boolean getUseBouncyCastleProvider() {
//        if (useBouncyCastleProvider == null) {
//            useBouncyCastleProvider = Boolean.FALSE;
//        }

        return Boolean.parseBoolean(SettingsHandler.getSettingsValue(RORO_SSH_USE_BOUNCY_CASTLE_PROVIDER));
    }

    @Value("${ssh.preferred.authentications:password,publickey,keyboard-interactive,gssapi-with-mic}")
    public void setPreferredAuthentications(String preferredAuthentications) {
        CommonProperties.preferredAuthentications = preferredAuthentications;
    }

    public static String getPreferredAuthentications() {
        if (StringUtils.isEmpty(preferredAuthentications)) {
            preferredAuthentications = "password,publickey,keyboard-interactive,gssapi-with-mic";
        }

        return preferredAuthentications;
    }

//    @Value("${ssh.skip.messages}")
//    public void setSkipMessages(String skipMessages) {
//        if (StringUtils.isNotEmpty(skipMessages)) {
//            CommonProperties.skipMessages = Arrays.asList(skipMessages.split("\\|"));
//        }
//    }

    public static List<String> getSkipMessages() {
        String skipMessages = SettingsHandler.getSettingsValue(RORO_SSH_SKIP_MESSAGE);

        if (StringUtils.isEmpty(skipMessages)) {
            return new ArrayList<>();
        } else {
            return Arrays.asList(skipMessages.split("\\|"));
        }
    }

//    @Value("${ssh.connect.timeout:10}")
//    public void setTimeout(String timeout) {
//        CommonProperties.timeout = timeout;
//    }

    public static Integer getTimeout() {
        Integer t = null;

        try {
            t = Integer.parseInt(SettingsHandler.getSettingsValue(RORO_SSH_CONNECT_TIMEOUT));
        } catch (NumberFormatException e) {
            // ignore
        }

        if (t == null || t <= 0) {
            t = 10;
        }

        if (t > 60 * 60) {
            t = 60 * 60;
        }

        return t;
    }

    @Value("${windows.temp-dir:C:\\temp\\roro}")
    public void setWindowsTempDir(String windowsTempDir) {
        CommonProperties.windowsTempDir = windowsTempDir;
    }

    public static String getWindowsTempDir() {
        if (StringUtils.isEmpty(windowsTempDir)) {
            windowsTempDir = "C:\\temp\\roro";
        }

        return windowsTempDir;
    }

//    @Value("${windows.powershell.use-outfile:false}")
//    public void setUseOutfile(String useOutFile) {
//        if (StringUtils.isNotEmpty(useOutFile)) {
//            CommonProperties.useOutFile = Boolean.valueOf(useOutFile);
//        }
//    }

    public static Boolean getUseOutFile() {
        return Boolean.parseBoolean(SettingsHandler.getSettingsValue(WINDOWS_POWERSHELL_USE_OUTFILE));
    }
}
//end of AssessmentManager.java
/*
 * Copyright 2021 The playce-roro-v3} Project.
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
 * Dong-Heon Han    Nov 15, 2021		    First Draft.
 */

package io.playce.roro.common.config;

import io.playce.roro.common.setting.SettingsHandler;
import io.playce.roro.common.util.GeneralCipherUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static io.playce.roro.common.setting.SettingsConstants.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component("roroProperties")
@ConfigurationProperties(prefix = "roro")
@Getter
@Setter
public class RoRoProperties {
    private String appVersion;
    private Working working;
    private SSH ssh;

    @Getter(AccessLevel.NONE)
    private boolean webTerminal;
    @Getter(AccessLevel.NONE)
    private boolean middlewareAutoScan;
    @Getter(AccessLevel.NONE)
    private boolean applicationAutoScan;

    @Getter
    @Setter
    public static class Working {
        private String dirPath;
    }

    @Setter
    public static class SSH {
        private String ipAddress;
        private Integer port;
        private String userName;
        private String password;
        private String ppkFilePath;

        public String getIpAddress() {
            return SettingsHandler.getSettingsValue(RORO_SSH_IP_ADDRESS);
        }

        public Integer getPort() {
            return Integer.parseInt(SettingsHandler.getSettingsValue(RORO_SSH_PORT));
        }

        public String getUserName() {
            return SettingsHandler.getSettingsValue(RORO_SSH_USER_NAME);
        }

        public String getPassword() {
            return GeneralCipherUtil.decrypt(SettingsHandler.getSettingsValue(RORO_SSH_PASSWORD));
        }

        public String getPpkFilePath() {
            return SettingsHandler.getSettingsValue(RORO_SSH_PPK_FILE_PATH);
        }
    }

    public boolean isWebTerminal() {
        return Boolean.parseBoolean(SettingsHandler.getSettingsValue(RORO_WEB_TERMINAL));
    }

    public boolean isMiddlewareAutoScan() {
        return Boolean.parseBoolean(SettingsHandler.getSettingsValue(RORO_MIDDLEWARE_AUTO_SCAN));
    }

    public boolean isMiddlewareAutoScanAfterServerScan() {
        return Boolean.parseBoolean(SettingsHandler.getSettingsValue(RORO_MIDDLEWARE_AUTO_SCAN_AFTER_SERVER_SCAN));
    }

    public boolean isApplicationAutoScan() {
        return Boolean.parseBoolean(SettingsHandler.getSettingsValue(RORO_APPLICATION_AUTO_SCAN));
    }
}
//end of RoRoProperties.java
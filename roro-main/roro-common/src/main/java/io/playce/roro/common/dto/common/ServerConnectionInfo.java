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
 * Dong-Heon Han    May 04, 2022		First Draft.
 */

package io.playce.roro.common.dto.common;

import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.common.util.support.TargetHost;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */

@Setter
@Getter
public class ServerConnectionInfo {
    private Long projectId;
    private Long inventoryId;
    private String deleteYn;
    private Long inventoryProcessId;
    private String representativeIpAddress;
    private String userName;
    @ToString.Exclude
    private String userPassword;
    private String keyFilePath;
    private String keyFileContent;
    private Integer connectionPort;
    private String enableSuYn;
    @ToString.Exclude
    private String rootPassword;
    private String windowsYn;

    public String getRootPassword() {
        return GeneralCipherUtil.decrypt(rootPassword);
    }

    public String getUserPassword() {
        return GeneralCipherUtil.decrypt(userPassword);
    }

    public static TargetHost targetHost(ServerConnectionInfo info) {
        TargetHost targetHost = new TargetHost();
        targetHost.setIpAddress(info.getRepresentativeIpAddress());
        targetHost.setPort(info.getConnectionPort());
        targetHost.setUsername(info.getUserName());
        targetHost.setPassword(info.getUserPassword());
        targetHost.setKeyFilePath(info.getKeyFilePath());
        targetHost.setKeyString(info.getKeyFileContent());

        if (Domain101.Y.name().equals(info.getEnableSuYn()) && StringUtils.isNotEmpty(info.getRootPassword())) {
            targetHost.setRootPassword(info.getRootPassword());
        }

        return targetHost;
    }
}
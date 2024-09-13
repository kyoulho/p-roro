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
 * Sang-cheon Park	2020. 3. 3.		First Draft.
 */
package io.playce.roro.common.util.support;

import io.playce.roro.common.dto.thirdparty.ThirdPartySearchTypeResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * <pre>
 * 서버의 정보를 담는 POJO 객체
 * </pre>
 *
 * @author Sang -cheon Park
 * @version 1.0
 */
@Getter
@Setter
@ToString(exclude = {"password", "rootPassword"})
public class TargetHost implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Callback 받을 URL */
    private String callback;

    /** 프로비저닝 대상 IP Address */
    private String ipAddress;

    /** 프로비저닝 대상의 SSH Port 번호 */
    private Integer port;

    /** 프로비저닝 대상 Host의 Shell 계정 */
    private String username;

    /** 프로비저닝 대상 Host의 Shell 패스워드 */
    private String password;

    /** 프로비저닝 대상 Host의 root 패스워드 */
    private String rootPassword;

    /** is trust Y/N (default : true) */
    private boolean trust = true;

    /** ssh key file */
    private String keyFilePath;

    /** ssh key file */
    private String keyString;

    /** 인벤토리 스캔시 3rd Party Solution을 검사하기 위한 목록 (별도의 방법으로 각 Assessment에 전달하려면 수정 사항이 너무 많아 TargetHost에 추가) */
    private List<ThirdPartySearchTypeResponse> thirdPartySearchTypeList;

    public boolean isValid() {
        return StringUtils.isNotEmpty(ipAddress) && port != null && port > 0 && port < 65535 && StringUtils.isNotEmpty(username) &&
                (StringUtils.isNotEmpty(password) || StringUtils.isNotEmpty(keyFilePath) || StringUtils.isNotEmpty(keyString));
    }

    /**
     * Convert target host.
     *
     * @param server the server
     * @return the target host
     */
    // public static TargetHost convert(Server server) {
    // 	TargetHost targetHost = new TargetHost();
    // 	targetHost.setIpAddress(server.getIpAddress());
    // 	targetHost.setPort(server.getPort());
    // 	targetHost.setUsername(server.getUsername());
    // 	targetHost.setPassword(GeneralCipherUtil.decrypt(server.getPassword()));
    // 	targetHost.setKeyFilePath(server.getKeyFilePath());
    // 	targetHost.setKeyString(server.getKeyFileString());
    //
    // 	return targetHost;
    // }
}
// end of TargetHost.java
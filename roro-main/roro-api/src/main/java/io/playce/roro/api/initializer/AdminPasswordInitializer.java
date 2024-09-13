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
 * SangCheon Park   Feb 08, 2022		    First Draft.
 */
package io.playce.roro.api.initializer;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.jpa.entity.UserAccess;
import io.playce.roro.jpa.repository.UserAccessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * <pre>
 * roro.admin.password.reset 파라미터가 있으면 admin 유저 패스워드 변경
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminPasswordInitializer implements InitializingBean {

    private static final String ADMIN = "admin";

    private final UserAccessRepository userAccessRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        // java option read
        String value = System.getProperty("roro.admin.password.reset");

        if (StringUtils.isNotEmpty(value)) {
            initPassword(value);
        }
    }

    private void initPassword(String value) {
        log.debug(":+:+:+:+:   Initialize for Reset Admin Password.   :+:+:+:+:");

        try {
            // get an admin user
            UserAccess userAccess = userAccessRepository.findByUserLoginId(ADMIN).orElse(null);

            if (userAccess != null) {
                // 패스워드 초기화 (패스워드 초기화가 되면 로그인 실패 카운트, 로그인 방지 초기화)
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                userAccess.setUserLoginPassword("{bcrypt}" + encoder.encode(value));
                userAccess.setUserPasswordModifyDatetime(new Date());
                userAccess.setLoginFailCnt(0);
                userAccess.setBlockYn(CommonConstants.NO);
                userAccess.setModifyDatetime(new Date());
                userAccessRepository.save(userAccess);
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while reset password. [Reason] : ", e);
        }
    }
}
//end of AdminPasswordInitializer.java
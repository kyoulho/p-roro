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
 * Jeongho Baek     10월 21, 2020       First Draft.
 */
package io.playce.roro.api.domain.authentication.jwt.extractor;

import io.playce.roro.api.common.error.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import static io.playce.roro.api.common.CommonConstants.AUTHENTICATION_TYPE_BEARER;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Component
public class JwtHeaderTokenExtractor implements TokenExtractor {

    @Override
    public String extract(String header) {

        if (StringUtils.isBlank(header)) {
            throw new AuthenticationServiceException(ErrorCode.AUTH_HEADER_BLANK.getCode());
        }

        if (header.length() < AUTHENTICATION_TYPE_BEARER.length()) {
            throw new AuthenticationServiceException(ErrorCode.INVALID_AUTH_HEADER_SIZE.getCode());
        }

        return header.substring(AUTHENTICATION_TYPE_BEARER.length());

    }

}
//end of JwtHeaderTokenExtractor.java
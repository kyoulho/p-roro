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
package io.playce.roro.api.domain.authentication.jwt.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.domain.authentication.jwt.JwtTokenException;
import io.playce.roro.api.domain.authentication.jwt.dto.Roles;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@AllArgsConstructor
public class AccessJwtToken implements JwtToken {

    private String rawToken;

    @Getter
    private Claims claims;

    @Getter
    private Jws<Claims> jwsClaims;

    public AccessJwtToken(Jws<Claims> jwsClaims) {
        this.jwsClaims = jwsClaims;
    }

    public AccessJwtToken(String rawToken, Claims claims) {
        this.rawToken = rawToken;
        this.claims = claims;
    }

    public static Optional<AccessJwtToken> verify(RawAccessJwtToken token, String signingKey) {
        Jws<Claims> claims = token.parseClaims(signingKey);
        List<String> roles = claims.getBody().get("roles", List.class);

        if(roles.contains(Roles.REFRESH_TOKEN.authority())) {
            throw new JwtTokenException(ErrorCode.NOT_PERMITTED_ACCESS_TOKEN.getCode());
        }

        return Optional.of(new AccessJwtToken(claims));
    }

    @Override
    public String getToken() {
        return this.rawToken;
    }

}
//end of AccessJwtToken.java
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

import io.jsonwebtoken.*;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.domain.authentication.jwt.JwtTokenException;
import lombok.AllArgsConstructor;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@AllArgsConstructor
public class RawAccessJwtToken implements JwtToken {

    private final String token;

    @Override
    public String getToken() {
        return this.token;
    }

    public Jws<Claims> parseClaims(String signingKey) {
        try {
            return Jwts.parser().setSigningKey(signingKey).parseClaimsJws(this.token);
        } catch (SignatureException ex) {
            throw new JwtTokenException(ErrorCode.INVALID_JWT_SIGN.getCode());
        } catch (MalformedJwtException ex) {
            throw new JwtTokenException(ErrorCode.INVALID_JWT_FORM.getCode());
        } catch (ExpiredJwtException ex) {
            throw new JwtTokenException(ErrorCode.EXPIRED_JWT.getCode());
        } catch (UnsupportedJwtException ex) {
            throw new JwtTokenException(ErrorCode.UNSUPPORTED_JWT.getCode());
        } catch (IllegalArgumentException ex) {
            throw new JwtTokenException(ErrorCode.JWT_EMPTY_CLAIMS.getCode());
        }
    }
}
//end of RawAccessJwtToken.java
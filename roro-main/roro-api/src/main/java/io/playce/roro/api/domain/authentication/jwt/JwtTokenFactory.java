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
package io.playce.roro.api.domain.authentication.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.playce.roro.api.config.JwtProperties;
import io.playce.roro.api.domain.authentication.dto.SecurityUser;
import io.playce.roro.api.domain.authentication.jwt.dto.JwtPayload.SecurityUserJwtPayload;
import io.playce.roro.api.domain.authentication.jwt.dto.Roles;
import io.playce.roro.api.domain.authentication.jwt.token.AccessJwtToken;
import io.playce.roro.api.domain.authentication.jwt.token.JwtToken;
import io.playce.roro.api.domain.authentication.jwt.token.RefreshJwtToken;
import io.playce.roro.mybatis.domain.authentication.UserMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

import static io.playce.roro.api.common.CommonConstants.JWT_ISSUER;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Component
@RequiredArgsConstructor
public class JwtTokenFactory {

    private final JwtProperties jwtProperties;
    private final ModelMapper modelMapper;
    private final UserMapper userMapper;

    public JwtToken createAccessJwtToken(SecurityUser securityUser) {
        SecurityUserJwtPayload securityUserJwtPayload = modelMapper.map(securityUser, SecurityUserJwtPayload.class);
        securityUserJwtPayload.setUserLoginId(securityUser.getUsername());

        Claims claims = Jwts.claims().setSubject("RoRo User Info.");
        claims.setIssuer(JWT_ISSUER);
        claims.put("user", securityUserJwtPayload);
        String username = securityUser.getUsername();
        claims.put("roles", userMapper.selectUserRoles(username));

        return new AccessJwtToken(generateToken(claims, jwtProperties.getTokenExpirationTime()), claims);
    }

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public JwtToken createRefreshJwtToken(SecurityUser securityUser) {
        SecurityUserJwtPayload securityUserJwtPayload = modelMapper.map(securityUser, SecurityUserJwtPayload.class);

        Claims claims = Jwts.claims().setSubject("RoRo User Info.");
        claims.setIssuer(JWT_ISSUER);
        claims.put("username", securityUserJwtPayload.getUserLoginId());
        claims.put("roles", Arrays.asList(Roles.REFRESH_TOKEN.authority()));

        return new RefreshJwtToken(generateToken(claims, jwtProperties.getRefreshTokenExpirationTime()), claims);
    }

    /**
     * Token을 만든다.
     *
     * @param claims         claims
     * @param expirationTime JWT 만료시간
     *
     * @return Token
     */
    private String generateToken(Claims claims, int expirationTime) {
        LocalDateTime currentTime = LocalDateTime.now();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(currentTime.plusMinutes(expirationTime).atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getTokenSigningKey())
                .compact();
    }
}
//end of JwtTokenFactory.java
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
 * Jeongho Baek     10월 21, 2020        First Draft.
 */
package io.playce.roro.api.domain.authentication.jwt.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.config.JwtProperties;
import io.playce.roro.api.domain.authentication.dto.SecurityUser;
import io.playce.roro.api.domain.authentication.jwt.JwtTokenException;
import io.playce.roro.api.domain.authentication.jwt.token.JwtAuthenticationToken;
import io.playce.roro.api.domain.authentication.jwt.token.RawAccessJwtToken;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtProperties jwtProperties;
    private final ModelMapper modelMapper;

    /**
     * Authenticate authentication.
     *
     * @param authentication the authentication
     * @return the authentication
     * @throws AuthenticationException the authentication exception
     */
    @SuppressWarnings("unchecked")
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        RawAccessJwtToken rawAccessToken = (RawAccessJwtToken) authentication.getCredentials();
        Jws<Claims> jwsClaims = rawAccessToken.parseClaims(jwtProperties.getTokenSigningKey());
        SecurityUser securityUser;

        try {
            securityUser = modelMapper.map(jwsClaims.getBody().get("user"), SecurityUser.class);
        } catch (IllegalArgumentException ex) {
            throw new JwtTokenException(ErrorCode.INVALID_JWT_FORM.getCode());
        }

        List<String> roles = jwsClaims.getBody().get("roles", List.class);
        List<GrantedAuthority> userRoleAuthorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        securityUser.setAuthorities(userRoleAuthorities);

        return new JwtAuthenticationToken(securityUser, userRoleAuthorities);
    }

    /**
     * Supports boolean.
     *
     * @param authentication the authentication
     * @return the boolean
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
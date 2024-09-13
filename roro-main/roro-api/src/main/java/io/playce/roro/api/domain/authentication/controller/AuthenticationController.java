/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       11월 04, 2021            First Draft.
 */
package io.playce.roro.api.domain.authentication.controller;

import io.playce.roro.api.config.JwtProperties;
import io.playce.roro.api.domain.authentication.dto.SecurityUser;
import io.playce.roro.api.domain.authentication.jwt.JwtTokenException;
import io.playce.roro.api.domain.authentication.jwt.JwtTokenFactory;
import io.playce.roro.api.domain.authentication.jwt.extractor.JwtHeaderTokenExtractor;
import io.playce.roro.api.domain.authentication.jwt.token.RawAccessJwtToken;
import io.playce.roro.api.domain.authentication.jwt.token.RefreshJwtToken;
import io.playce.roro.api.domain.authentication.service.CustomUserDetailsService;
import io.playce.roro.common.config.RoRoProperties;
import io.playce.roro.common.dto.auth.VersionResponse;
import io.playce.roro.common.dto.common.User;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static io.playce.roro.api.common.CommonConstants.AUTHENTICATION_HEADER_NAME;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenFactory jwtTokenFactory;
    private final JwtHeaderTokenExtractor jwtHeaderTokenExtractor;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProperties jwtProperties;
    private final RoRoProperties roroProperties;

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody User.LoginRequest loginRequest) {
        // CustomAuthenticationProvider 부터 처리 시작.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(GeneralCipherUtil.decrypt(loginRequest.getUsername()),
                        GeneralCipherUtil.decrypt(loginRequest.getPassword())));

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

        User.LoginResponse loginResponse = User.LoginResponse.builder()
                .token(jwtTokenFactory.createAccessJwtToken(securityUser).getToken())
                .refreshToken(jwtTokenFactory.createRefreshJwtToken(securityUser).getToken())
                .build();

        return ResponseEntity.ok(loginResponse);
    }

    // TODO 삭제 : 사용자 정보는 rorodbd에서 조회하지  않고, oauth에서 발행한 jwt 에서 parsing
//    @GetMapping(path = "/refresh-token")
//    @SecurityRequirement(name = "bearerAuth")
//    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
//        String tokenPayload = jwtHeaderTokenExtractor.extract(request.getHeader(AUTHENTICATION_HEADER_NAME));
//        RawAccessJwtToken rawToken = new RawAccessJwtToken(tokenPayload);
//
//        RefreshJwtToken refreshToken = RefreshJwtToken.create(rawToken, jwtProperties.getTokenSigningKey())
//                .orElseThrow(() -> new JwtTokenException("Refresh Token creation failed."));
//
//        String username = (String) refreshToken.getJwsClaims().getBody().get("username");
//        SecurityUser securityUser = customUserDetailsService.loadUserByUsername(username);
//
//        User.LoginResponse loginResponse = User.LoginResponse.builder()
//                .token(jwtTokenFactory.createAccessJwtToken(securityUser).getToken())
//                .refreshToken(jwtTokenFactory.createRefreshJwtToken(securityUser).getToken())
//                .build();
//
//        return ResponseEntity.ok(loginResponse);
//    }

    @GetMapping("/version")
    @ResponseStatus(HttpStatus.OK)
    public VersionResponse getVersion() {
        return VersionResponse.builder().applicationVersion(roroProperties.getAppVersion()).build();
    }

}
//end of AuthenticationController.java

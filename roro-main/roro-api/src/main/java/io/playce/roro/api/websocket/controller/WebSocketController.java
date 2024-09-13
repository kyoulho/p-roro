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
 * SangCheon Park   Mar 22, 2022		    First Draft.
 */
package io.playce.roro.api.websocket.controller;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.config.JwtProperties;
import io.playce.roro.api.domain.authentication.dto.SecurityUser;
import io.playce.roro.api.domain.authentication.jwt.JwtTokenException;
import io.playce.roro.api.domain.authentication.jwt.token.AccessJwtToken;
import io.playce.roro.api.domain.authentication.jwt.token.RawAccessJwtToken;
import io.playce.roro.api.domain.authentication.service.CustomUserDetailsService;
import io.playce.roro.api.websocket.listener.RoRoSessionListener;
import io.playce.roro.common.dto.websocket.BrowserSession;
import io.playce.roro.common.dto.websocket.RoRoMessage;
import io.playce.roro.common.dto.websocket.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;

import static io.playce.roro.api.websocket.constants.WebSocketConstants.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService customUserDetailsService;
    private final RoRoSessionListener sessionListener;

    /**
     * Init Login.
     *
     * @param headerAccessor the header accessor
     * @param message        the message
     *
     * @throws Exception the exception
     */
    @MessageMapping("/login")
    @SendToUser(WS_QUEUE_REPLY)
    public RoRoMessage login(SimpMessageHeaderAccessor headerAccessor, final RoRoMessage message) throws Exception {
        // log.debug("Login - SessionID : [{}], Message : [{}]", headerAccessor.getSessionId(), message);

        RoRoMessage response = new RoRoMessage(WS_CODE_LOGIN_RESPONSE);
        if (message.getCode() == WS_CODE_LOGIN) {
            try {
                String tokenPayload = (String) message.getData();

                // data로 절달받은 jwt 토큰을 검증한다.
                RawAccessJwtToken rawToken = new RawAccessJwtToken(tokenPayload);
                AccessJwtToken token = AccessJwtToken.verify(rawToken, jwtProperties.getTokenSigningKey())
                        .orElseThrow(() -> new JwtTokenException("Access token verification failed."));

                Map<String, String> userMap = token.getJwsClaims().getBody().get("user", Map.class);

                Long userId = Long.parseLong(String.valueOf(userMap.get("userId")));
                String username = String.valueOf(userMap.get("userLoginId"));
                // 사용자 정보는 oauthdb에 저장하므로 rorodb에서 조회 불가
//                SecurityUser securityUser = (SecurityUser) customUserDetailsService.loadUserByUsername(username);
//
//                if (securityUser == null) {
//                    throw new ResourceNotFoundException("User does Not Found.");
//                }

                String uuid = headerAccessor.getSessionId();

                BrowserSession browserSession = new BrowserSession();
                browserSession.setSessionId(headerAccessor.getSessionId());
//              browserSession.setMemberId(securityUser.getUserId());
//              browserSession.setUsername(securityUser.getUsername());
                browserSession.setMemberId(userId);
                browserSession.setUsername(username);
                browserSession.setUuid(uuid);

                sessionListener.registerBrowserSession(headerAccessor.getSessionId(), browserSession);

                response.setStatus(Status.success);
                response.setData(WS_QUEUE_USER + WS_QUEUE_NOTIFICATION + "/" + uuid);
            } catch (Exception e) {
                // ignore AUTH_HEADER_BLANK(AUTH_004) & EXPIRED_JWT(AUTH_008) code
                if (!e.getMessage().contains(ErrorCode.AUTH_HEADER_BLANK.getCode()) && !e.getMessage().contains(ErrorCode.EXPIRED_JWT.getCode())) {
                    log.error("Unhandled exception occurred while handle /app/login.", e);
                }

                response.setStatus(Status.fail);
                response.setMessage(e.getMessage());
            }
        } else {
            response.setStatus(Status.fail);
            response.setMessage("Message code must be [" + WS_CODE_LOGIN + "] for /app/login.");
        }

        return response;
    }

    /**
     * Handle exception string.
     *
     * @param exception the exception
     *
     * @return the string
     */
    @MessageExceptionHandler
    @SendToUser(WS_QUEUE_ERROR)
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

    /**
     * Heartbeat.
     *
     * @param message the message
     *
     * @throws Exception the exception
     */
    @MessageMapping("/heartbeat")
    public void heartbeat(SimpMessageHeaderAccessor headerAccessor, final RoRoMessage message) throws Exception {
        //log.debug("Heartbeat message received from session ID : ({})", headerAccessor.getSessionId());
    }
}
//end of WebSocketController.java
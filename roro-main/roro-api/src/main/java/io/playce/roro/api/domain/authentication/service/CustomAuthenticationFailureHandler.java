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
package io.playce.roro.api.domain.authentication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.ErrorResponse;
import io.playce.roro.api.common.i18n.LocaleMessageConvert;
import io.playce.roro.api.domain.authentication.jwt.JwtTokenException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

import static io.playce.roro.api.common.CommonConstants.LOCALE_KOREAN_LANGUAGE;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Slf4j
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    private final LocaleMessageConvert localeMessageConvert;

    public CustomAuthenticationFailureHandler(ObjectMapper objectMapper, LocaleMessageConvert localeMessageConvert) {
        this.objectMapper = objectMapper;
        this.localeMessageConvert = localeMessageConvert;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e)
            throws IOException, ServletException {
        LocaleContextHolder.setLocale(getLocaleCookieValue(httpServletRequest));

        // ignore AUTH_HEADER_BLANK(AUTH_004) & EXPIRED_JWT(AUTH_008) code
        if (!e.getMessage().contains(ErrorCode.AUTH_HEADER_BLANK.getCode()) && !e.getMessage().contains(ErrorCode.EXPIRED_JWT.getCode())) {
            log.error("AuthenticationException occurred while execute.", e);
        }

        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

        if (e instanceof AuthenticationServiceException || e instanceof JwtTokenException || e instanceof AuthenticationException) {
            objectMapper.writeValue(httpServletResponse.getWriter(), ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(e.getMessage())));
        } else {
            httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            objectMapper.writeValue(httpServletResponse.getWriter(), ErrorResponse.of(ErrorCode.UNKNOWN_ERROR));
        }
    }

    private Locale getLocaleCookieValue(HttpServletRequest httpServletRequest) {

        final String localeCookieName = "locale";

        Cookie[] cookies = httpServletRequest.getCookies();
        String localeCookieValue = null;

        if (cookies == null) {
            return Locale.ENGLISH;
        }

        for (Cookie cookie : cookies) {
            if (localeCookieName.equals(cookie.getName())) {
                localeCookieValue = cookie.getValue();
            }
        }

        if (StringUtils.isNotEmpty(localeCookieValue) && localeCookieValue.equals(LOCALE_KOREAN_LANGUAGE)) {
            return Locale.KOREAN;
        } else {
            return Locale.ENGLISH;
        }

    }

}
//end of CustomAuthenticationFailureHandler.java
/*
 * Copyright 2023 The playce-roro-v3 Project.
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
 * Jihyun Park      7월 17, 2023            First Draft.
 */
package io.playce.roro.api.domain.common.aop;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;



/**
 * <pre>
 *
 * 사용자의 token에 있는 roles를 확인하여 권한을 체크한다.
 *
 * </pre>
 *
 * @author Jihyun Park
 * @version 1.0
 */

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(2)
public class AuthenticationAspect {

    private static final String AUTH_URI = "/api/auth";
    private static final String COMMON_URI = "/api/common";

    @Before("execution(* io.playce.roro.api.domain.*.controller.*Controller*.*(..)) ")
    public void loggerBefore(JoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        if (!request.getRequestURI().contains(AUTH_URI) && !request.getRequestURI().contains(COMMON_URI)) {
            // Administrator가 아닌데, get 이외의 method를 호출하는 경우
            if(!WebUtil.hasRole("ROLE_ADMIN") && !request.getMethod().equals(HttpMethod.GET.name())) {
//                log.debug("start - " + joinPoint.getSignature().getDeclaringTypeName() + " / " + joinPoint.getSignature().getName());
//                log.debug(request.getMethod());
//                log.debug(request.getHeader(CommonConstants.AUTHENTICATION_HEADER_NAME));
                log.info("[{}] has no permission. Roles are {}", WebUtil.getUsername(), WebUtil.getLoginUser().getAuthorities().toString());
                throw new RoRoApiException(ErrorCode.FORBIDDEN);
            }

        }

    }

}

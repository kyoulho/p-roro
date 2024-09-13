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
 * Sang-cheon Park	2020. 3. 30.		First Draft.
 */
package io.playce.roro.api.common.util;

import io.playce.roro.api.domain.authentication.dto.SecurityUser;
import io.playce.roro.common.util.ThreadLocalUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static io.playce.roro.api.common.CommonConstants.ORIGIN_HOST;
import static io.playce.roro.api.common.CommonConstants.RORO_HOST;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
public class WebUtil {
    /**
     * <pre>
     * 로그인 사용자 정보를 가져온다.
     * </pre>
     *
     * @return login user
     */
    public static UserDetails getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        }

        return null;
    }

    // /**
    //  * <pre>
    //  * 로그인 사용자의 display name을 가져온다.
    //  * </pre>
    //  *
    //  * @return username
    //  */
    // public static String getDisplayName() {
    //     return ((SecurityUser) getLoginUser()).getFirstName() + " " + ((SecurityUser) getLoginUser()).getLastName();
    // }

    /**
     * <pre>
     * 로그인 사용자의 id를 가져온다.
     * </pre>
     *
     * @return the user id
     */
    public static Long getUserId() {
        Long id = null;

        try {
            id = ((SecurityUser) Objects.requireNonNull(getLoginUser())).getUserId();
        } catch (Exception ignored) {}

        // API를 통한 요청시에는 JWT 토큰을 통해 userId를 가져올 수 있지만 스케쥴러 등 내부 로직에서는 로그인 사용자가 없기 때문에
        // 관리자 ID를 리턴한다.
        if (id == null) {
            id = 1L;
        }

        return id;
    }

    /**
     * <pre>
     * 로그인 사용자의 username을 가져오다.
     * </pre>
     *
     * @return
     */
    public static String getUsername() {
        UserDetails userDetails = getLoginUser();

        if (userDetails != null) {
            return ((SecurityUser) userDetails).getUserLoginId();
        }

        return "admin";
    }

    /**
     * <pre>
     * WebApplicationContext 객체를 가져온다.
     * </pre>
     *
     * @param request
     *
     * @return
     */
    public static WebApplicationContext getWebApplicationContext(HttpServletRequest request) {
        return WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
    }

    /**
     * <pre>
     * 로그인한 사용자의 권한을 비교한다.
     * </pre>
     *
     * @param role
     *
     * @return
     */
    public static boolean hasRole(String role) {
        // get security context from thread local
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return false;
        }

        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return false;
        }

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (role.equals(auth.getAuthority())) {
                return true;
            }
        }

        return false;
    }

    public static String getBaseUrl() {
        String baseUrl = "http://127.0.0.1:8080";

        try {
            baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

            String roroHost = (String) ThreadLocalUtils.get(RORO_HOST);
            String originHost = (String) ThreadLocalUtils.get(ORIGIN_HOST);

            if (StringUtils.isNotEmpty(roroHost) && StringUtils.isNotEmpty(originHost)) {
                baseUrl = baseUrl.replaceAll(roroHost, originHost);
            }
        } catch (Exception e) {
            // ignore
        }

        return baseUrl;
    }
}
//end of WebUtil.java
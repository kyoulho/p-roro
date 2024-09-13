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
 * Author			Date				Description
 * ---------------	----------------	------------
 * Jeongho Baek   10월 28, 2021		First Draft.
 */
package io.playce.roro.api.domain.authentication.service;

import io.playce.roro.api.common.CommonConstants;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.domain.authentication.dto.SecurityUser;
import io.playce.roro.common.dto.auth.LoginUser;
import io.playce.roro.jpa.entity.UserAccess;
import io.playce.roro.jpa.repository.UserAccessRepository;
import io.playce.roro.mybatis.domain.authentication.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccessRepository userAccessRepository;
    private final UserMapper userMapper;
    private final ModelMapper modelMapper;

    private static final Integer MAX_FAIL_COUNT = 7;

    @Override
    public SecurityUser loadUserByUsername(String username) {
        LoginUser loginUser = userMapper.selectLoginUser(username);
        if (loginUser == null) {
            throw new UsernameNotFoundException(ErrorCode.USER_NOT_FOUND.getCode());
        }

        SecurityUser securityUser = modelMapper.map(loginUser, SecurityUser.class);
        securityUser.setUserId(loginUser.getUserId());
        securityUser.setUsername(loginUser.getUserLoginId());
        securityUser.setPassword(loginUser.getUserLoginPassword());
        securityUser.setAuthorities(getGrantedAuthorityList(username));

        return securityUser;
    }

    private List<GrantedAuthority> getGrantedAuthorityList(String username) {
        List<String> userRoles = userMapper.selectUserRoles(username);
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String userRole : userRoles) {
            grantedAuthorities.add(new SimpleGrantedAuthority(userRole));
        }
        return grantedAuthorities;
    }

    @Transactional
    public void successLogin(String userId) {
        UserAccess userAccess = userAccessRepository.findByUserLoginId(userId).orElse(null);

        if (userAccess != null) {
            userAccess.setLoginFailCnt(0);
            userAccess.setBlockYn(CommonConstants.NO);
            userAccess.setLastLoginDatetime(new Date());

            userAccessRepository.save(userAccess);
        }
    }

    @Transactional
    public void failLogin(String userId) {
        UserAccess userAccess = userAccessRepository.findByUserLoginId(userId).orElse(null);

        if (userAccess != null) {
            Integer loginFailCnt = userAccess.getLoginFailCnt() + 1;

            if (loginFailCnt >= MAX_FAIL_COUNT) {
                userAccess.setBlockYn(CommonConstants.YES);
            }

            userAccess.setLoginFailCnt(loginFailCnt);
            userAccessRepository.save(userAccess);
        }
    }
}
//end of CustomUserDetailsService.java
/*
 * Copyright 2021 The playce-roro-v3} Project.
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
 * Dong-Heon Han    Nov 15, 2021		    First Draft.
 */

package io.playce.roro.api.domain.common.service;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.dto.common.User;
import io.playce.roro.common.dto.inventory.report.SettingType;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.jpa.entity.UserAccess;
import io.playce.roro.jpa.entity.UserConfig;
import io.playce.roro.jpa.entity.UserMaster;
import io.playce.roro.jpa.repository.UserAccessRepository;
import io.playce.roro.jpa.repository.UserConfigRepository;
import io.playce.roro.jpa.repository.UserMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserMasterRepository userMasterRepository;
    private final UserAccessRepository userAccessRepository;
    private final UserConfigRepository userConfigRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public void updatePassword(User.PasswordChangeRequest passwordChangeRequest) {
        Long userId = WebUtil.getUserId();
        UserAccess userAccess = userAccessRepository.findById(userId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.USER_NOT_FOUND));

        // origin Password check
        if (!encoder.matches(GeneralCipherUtil.decrypt(passwordChangeRequest.getOriginPassword()),
                userAccess.getUserLoginPassword().substring(8))) { // '{bcrypt}'를 제외 비교
            throw new RoRoApiException(ErrorCode.PASSWORD_INCORRECT);
        }

        String newPassword = String.format("{bcrypt}%s",
                encoder.encode(GeneralCipherUtil.decrypt(passwordChangeRequest.getNewPassword())));
        userAccess.setUserLoginPassword(newPassword);
    }

    /**
     * 각 User 설정 정보를 가져온다.
     */
    public String getUserConfig(Long projectId, Long userId, SettingType property) {
        List<UserConfig> userConfigList = userConfigRepository.findByUserIdAndPropertyAndProjectId(userId, property.toString(), projectId);

        if (!userConfigList.isEmpty()) {
            return userConfigList.get(0).getConfigValue();
        }

        return null;
    }

    public void saveUserConfig(Long projectId, Long userId, SettingType property, String value) {
        List<UserConfig> userConfigList = userConfigRepository.findByUserIdAndPropertyAndProjectId(userId, property.toString(), projectId);

        // 기존 유저 설정 정보가 있으면 업데이트하고 없는 경우 신규로 등록한다.
        if (!userConfigList.isEmpty()) {
            userConfigList.get(0).setConfigValue(value);
            userConfigList.get(0).setModifyUserId(WebUtil.getUserId());
            userConfigList.get(0).setModifyDatetime(new Date());
        } else {
            UserMaster user = userMasterRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User(" + userId + ") does Not Found."));

            UserConfig userConfig = new UserConfig();
            userConfig.setProjectId(projectId);
            userConfig.setUserId(user.getUserId());
            userConfig.setProperty(property.toString());
            userConfig.setConfigValue(value);
            userConfig.setDeleteYn("N");
            userConfig.setRegistUserId(WebUtil.getUserId());
            userConfig.setRegistDatetime(new Date());
            userConfig.setModifyUserId(WebUtil.getUserId());
            userConfig.setModifyDatetime(new Date());
            userConfigRepository.save(userConfig);
        }
    }
}
//end of UserService.java

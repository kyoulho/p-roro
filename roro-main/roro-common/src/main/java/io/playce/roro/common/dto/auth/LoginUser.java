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
 * Jeongho Baek   11월 10, 2021		First Draft.
 */
package io.playce.roro.common.dto.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Getter
@Setter
@ToString
public class LoginUser {

    private long userId;
    private String userLoginId;
    private String userLoginPassword;
    private String userStatusCode;
    private String tempPasswordYn;
    private String userNameKorean;
    private String userNameEnglish;
    private String userEmail;
    private String blockYn;

    @Getter
    @Setter
    public static class AuditUser {
        private Long userId;
        private String userNameKorean;
        private String userNameEnglish;
    }

}
//end of LoginUser.java
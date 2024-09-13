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

package io.playce.roro.common.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
public class User {
    @Getter @Setter
    @Schema(description = "패스워드 변경")
    public static class PasswordChangeRequest{
        @Schema(title = "Origin Password", description = "Origin Password")
        private String originPassword;
        @Schema(title = "New Password", description = "New Password")
        private String newPassword;
    }

    @Getter @Setter
    @ToString
    @Schema(description = "사용자 로그인")
    public static class LoginRequest {
        /**
         * The Username.
         */
        @Schema(description = "사용자 ID")
        private String username;

        /**
         * The Password.
         */
        @Schema(description = "패스워드")
        private String password;

    }

    @Getter @Setter
    @Builder
    public static class LoginResponse {
        /**
         * The Token.
         */
        private String token;
        /**
         * The Refresh token.
         */
        private String refreshToken;

    }
}
//end of User.java
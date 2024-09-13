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
package io.playce.roro.api.domain.authentication.jwt.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 1.0
 */
public class JwtPayload {

    @Getter
    @Setter
    public static class SecurityUserJwtPayload {

        private Long userId;
        private String userLoginId;
        private String username;
        private String adminChangeYn;

        @JsonIgnore
        private List<String> authorities;

    }

    @Getter
    @Setter
    public static class SecurityUserRefreshJwtPayload {

        private String username;

    }

}

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
 * SangCheon Park   Jan 25, 2022		    First Draft.
 */
package io.playce.roro.common.dto.preconfig;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
public class PreConfigUserRequest {

    private Integer uid;
    private String userName;
    private String userPassword;
    @Getter(AccessLevel.NONE)
    private List<String> groups;
    private String homeDir;
    private String profile;
    private String crontab;

    public String getGroups() {
        if (groups == null) {
            return null;
        }

        return String.join(",", groups);
    }
}
//end of PreConfigUserRequest.java
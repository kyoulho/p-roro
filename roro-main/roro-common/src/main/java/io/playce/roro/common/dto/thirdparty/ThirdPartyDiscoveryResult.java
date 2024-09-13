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
 * SangCheon Park   Sep 27, 2022		    First Draft.
 */
package io.playce.roro.common.dto.thirdparty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
public class ThirdPartyDiscoveryResult {
    @JsonIgnore
    private Long thirdPartySolutionId;
    // third party solution name
    private String name;
    // third party solution vendor
    private String vendor;

    private List<ThirdPartyDiscoveryDetail> discoveryDetails = new ArrayList<>();

    @Getter
    @Setter
    public static class ThirdPartyDiscoveryDetail {
        @JsonIgnore
        private Long thirdPartySearchTypeId;
        private String type;  // search type
        private String value; // find contents
    }
}
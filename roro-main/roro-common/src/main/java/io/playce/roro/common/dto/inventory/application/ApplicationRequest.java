/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * SangCheon Park   Nov 25, 2021		    First Draft.
 */
package io.playce.roro.common.dto.inventory.application;

import io.playce.roro.common.dto.inventory.inventory.InventoryRequest;
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
public class ApplicationRequest extends InventoryRequest {

    private String applicationInventoryName;
    private String deployPath;
    private String sourceLocationUri;
    @Getter(AccessLevel.NONE)
    private List<String> analysisLibList;
    @Getter(AccessLevel.NONE)
    private List<String> analysisStringList;
    // private String automaticRegistProtectionYn;

    // https://cloud-osci.atlassian.net/wiki/spaces/PRUS/pages/22225223757
    private String dedicatedAuthenticationYn = "N";
    private Long credentialId;
    private String userName;
    private String userPassword;

    public String getAnalysisLibList() {
        if (analysisLibList == null) {
            return null;
        }

        return String.join(",", analysisLibList);
    }

    public String getAnalysisStringList() {
        if (analysisStringList == null) {
            return null;
        }

        return String.join(",", analysisStringList);
    }
}
//end of ApplicationRequest.java
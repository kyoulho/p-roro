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
 * SangCheon Park   Jan 20, 2022		    First Draft.
 */
package io.playce.roro.common.dto.assessment;

import lombok.AccessLevel;
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
// @JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AssessmentResponseDto {

    private Long projectId;
    private String inventoryTypeCode;
    private Long inventoryId;
    private String inventoryName;
    private String result;
    private String message;
    @Getter(AccessLevel.NONE)
    private List<AssessmentResponseDto> subInventoryList;

    public List<AssessmentResponseDto> getSubInventoryList() {
        if (subInventoryList == null) {
            subInventoryList = new ArrayList<>();
        }

        return subInventoryList;
    }
}
//end of AssessmentResponseDto.java
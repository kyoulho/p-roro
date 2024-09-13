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
 * SangCheon Park   Nov 15, 2021		    First Draft.
 */
package io.playce.roro.common.dto.assessment;

import io.playce.roro.common.config.RoRoProperties;
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
public class ApplicationDto extends ServerDto {
    private Long applicationId;
    private String windowsYn;
    private String deployPath;
    private String sourceLocationUri;
    private String uploadSourceFileName;
    private String uploadSourceFilePath;
    private List<String> analysisLibList;
    private List<String> analysisStringList;
    private RoRoProperties.SSH ssh;
}
//end of ApplicationDto.java
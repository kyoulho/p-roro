/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       1월 30, 2022            First Draft.
 */
package io.playce.roro.discover.middleware.dto;

import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.code.Domain1102;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Getter
@Builder
@ToString
public class DetectResultInfo {
    private String vendor;
    private Domain1013 mwDetailType;
    private Domain1102 mwType;
    private String enginePath;
    private String domainPath;
    private String instancePath;
    private String version;
    private String name;
    private String processName;
    private String pid;
    private String runUser;
    private String javaVersion;

    /**
     * For WebSphere
     */
    private String cellName;
    private String nodeName;
    private String profile;
}
//end of DetectResultInfo.java
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
 * Dong-Heon Han    Mar 03, 2022		First Draft.
 */

package io.playce.roro.mw.asmt.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Setter @Getter @ToString
public class DiscMiddlewareInstance {
    private String middlewareInstanceName;
    private String middlewareInstanceDetailDivision;
    private String middlewareInstancePath;
    private String middlewareConfigPath;
    private String middlewareInstanceServicePort;
    private String middlewareInstanceProtocol;
    private String runningUser;
    private String javaVersion;
    private String javaVendor;
    private String deployPath;
    private List<String> applications;
    private boolean isRuuning;
}
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       11월 10, 2021            First Draft.
 */
package io.playce.roro.mw.asmt.tomcat.dto;

import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Getter
@Setter
public class TomcatAssessmentResult extends MiddlewareAssessmentResult {

    @Getter
    @Setter
    public static class Engine extends MiddlewareAssessmentResult.Engine {
        private String path;
        private String name;
        private String version;
    }

    @Getter
    @Setter
    public static class Instance extends MiddlewareAssessmentResult.Instance {
        private String path;
        private List<String> options;
        private Map<String, ConfigFile> configFiles;
        private List<Map<String, String>> connectors;
        private List<Applications> applications;
        private List<Map<String, String>> deployApps;
        private List<Map<String, String>> globalResources;
        private List<Map<String, String>> resources;
        private String minHeap;
        private String maxHeap;
        private String javaVersion;
        private String javaVendor;
        private String runUser;
        private String configFileLocation;
        private String contextFileLocation;
        private String isRunning;
        private List<Map<String, String>> executors;
    }

    @Getter
    @Setter
    public static class ConfigFile {
        private String path;
        private String contents;
    }

    @Getter
    @Setter
    public static class Applications {
        private String serviceName;
        private Webapps webapps;
        private List<Object> context;
    }

    @Getter
    @Setter
    public static class Webapps {
        private String basePath;
        private String unpackWARs;
        private String autoDeploy;
        private List<String> apps; // 물리적인 디렉토리 목록
    }

}
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
 * jhpark       08월 24, 2022            First Draft.
 */
package io.playce.roro.mw.asmt.jboss.dto;

import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author jhpark
 * @version 3.0
 */
@Getter
@Setter
public class JbossAssessmentResult extends MiddlewareAssessmentResult {

    @Getter
    @Setter
    public static class Engine extends MiddlewareAssessmentResult.Engine {
        private String path;
        private String name;
        private String version;
        private String mode;
        private String vendor;
    }

    @Getter
    @Setter
    public static class Instance extends MiddlewareAssessmentResult.Instance {
        private String domainName;
        private String domainPath;
        private String configPath;
        private List<String> runTimeOptions;
        private Map<String ,String> configFileName;
        private Map<String, ConfigFile> configFiles;
        private List<Map<String ,String>> connectors;
        private List<Applications> applications;
        private List<LinkedHashMap> resources;
        private List<String> modules;
        private String minHeap;
        private String maxHeap;
        private String maxMetaspaceSize;
        private String maxPermSize;
        private String javaVersion;
        private String javaVendor;
        private String homeDir;
        private String baseDir;
        private String runUser;
        private String configFileLocation;
        private String isRunning;
        private List<Map<String, String>> threads;
        private List<Instances> instances;

    }

    @Getter
    @Setter
    @ToString
    public static class Instances {
        private String name;
        private String svrGroupName;
        private String configPath;
        private List<Map<String, String>> svrConnectors;
        private String ipAddress;
        private String minHeap;
        private String maxHeap;
        private String maxPermSize;
        private String javaVersion;
        private String javaVendor;
        private String runUser;
        private Boolean isRunning;
        private String portOffset;
        private String jvmOptions;
        private String profileName;
        private String socketBindName;
        private List<String> runTimeOptions;
    }

    @Getter
    @Setter
    @ToString
    public static class Server {
        private String name;
        private String group;
        private String autostart;
        private String socketBindingGroup;
        private String portOffset;
        private String host;
        private String heapSize;
        private String maxHeap;
        private String jvmOptions;


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
        private String sourcePath;
        private String deployFileName;
        private String contextPath;
        private String serverGroup;
        private String type;
        private int AssignmentCnt;
    }

}

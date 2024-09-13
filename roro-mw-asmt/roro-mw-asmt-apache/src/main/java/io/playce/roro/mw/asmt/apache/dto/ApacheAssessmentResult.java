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
package io.playce.roro.mw.asmt.apache.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
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
public class ApacheAssessmentResult extends MiddlewareAssessmentResult {

    @Getter
    @Setter
    @ToString
    public static class Engine extends MiddlewareAssessmentResult.Engine {
        private String path;
        private String name;
        private String version;
        private String runUser;
    }

    @Getter
    @Setter
    @ToString
    public static class Instance extends MiddlewareAssessmentResult.Instance {
        private General general;
        private Map<String, String> define;
        private List<ConfigFile> configFiles;
        private List<LoadModule> modules;
        private Map<String, String> keepAlive;
        private SolutionSpecific solutionSpecific;
        @JsonIgnore
        private transient Map<String, String> documentRoot;
        private Map<String, List<String>> logFormat;
        private Map<String, String> errorDocuments;
        private Map<String, String> browserMatches;
        private Map<String, List<String>> ifModule;
        private Map<String, List<String>> virtualHost;
        private Map<String, List<String>> directory;
        private Map<String, List<String>> location;
        private Map<String, List<String>> files;
        private Map<String, List<String>> proxy;
        private List<String> includeFiles;
    }

    @Getter
    @Setter
    public static class General {
        private String vendor;
        private String solutionName;
        private String solutionVersion;
        private String installHome;
        private String serverRoot;
        private String serverName;
        private String runningType;
        private List<Integer> listenPort;
        private String documentRoot;
        private boolean useSsl;
        private String serverStatus;
//        private String startedDate;
        private String runUser;
        private Date scannedDate;
        private List<String> env;
    }

    @Getter
    @Setter
    @ToString
    public static class LoadModule {
        private String name;
        private String location;
    }

    @Getter
    @Setter
    @ToString
    public static class ConfigFile {
        private String path;
        private String content;
    }

    @Getter
    @Setter
    @ToString
    public static class SolutionSpecific {
        private String useCanonicalName;
        private String serverTokens;
        private String traceEnable;
        private String hostnameLookups;
        private String user;
        private String group;
        private String serverAdmin;
        private String serverSignature;
    }
}
//end of ApacheHttpdAssessmentResult.java
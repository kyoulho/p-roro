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
package io.playce.roro.mw.asmt.websphere.dto;

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
public class WebSphereAssessmentResult extends MiddlewareAssessmentResult {

    @Getter
    @Setter
    @ToString
    public static class Engine extends MiddlewareAssessmentResult.Engine {

        private String path;
        private String name;
        private String version;

    }

    @Getter
    @Setter
    @ToString
    public static class Instance extends MiddlewareAssessmentResult.Instance {

        private General general;
        private List<Server> servers;
        private List<Cluster> clusters;
        private List<DataSource> dataSources;
        private List<Application> applications;
        private List<Port> ports;
        private List<Instances> instances;

    }
    @Getter
    @Setter
    @ToString
    public static class General {

        private String vendor;
        private String engineName;
        private String engineVersion;
        private String installedHome;
        private String javaVersion;
        private String javaVendor;
        private Date scannedDate;
        private List<Profile> profiles;

    }


    @Getter
    @Setter
    @ToString
    public static class Profile {

        private String name;
        private String path;
//        private String maxHeap;
//        private String minHeap;
//        private String runUser;

    }

    @Getter
    @Setter
    @ToString
    public static class Server {

        private String profileName;
        private String cellName;
        private String nodeName;
        private String clusterName;
        private String serverName;
        private String hostName;
        private String status;
        private Integer listenPort;
        private String jvmOptions;
        private Config config;
        private String serverType;

    }

    @Getter
    @Setter
    @ToString
    public static class Config {

        private IoRedirect ioRedirect;
        private JvmEntries jvmEntries;

    }

    @Getter
    @Setter
    @ToString
    public static class IoRedirect {

        private String stdoutFilename;
        private String stderrFilename;

    }

    @Getter
    @Setter
    @ToString
    public static class JvmEntries {

        private List<SystemProperty> systemProperties;
        private List<String> bootClasspath;
        private Map<String, Object> properties;

    }

    @Getter
    @Setter
    @ToString
    public static class SystemProperty {

        private String name;
        private String value;

    }

    @Getter
    @Setter
    @ToString
    public static class Cluster {

        private String profileName;
        private String cellName;
        private String clusterName;
        private String nodeGroup;
        private Boolean dwlm;
        private List<Member> members;

    }

    @Getter
    @Setter
    @ToString
    public static class Member {

        private String nodeName;
        private String serverName;

    }


    @Getter
    @Setter
    @ToString
    public static class DataSource {

        private String profileName;
        private String dataSourceName;
        private String jndiName;
        private String connectionUrl;
        private String authDataAlias;
        private String userId;
        private String password;
        private String jdbcProvider;
        private String range;
        private int min;
        private int max;
        private String timeout;

    }

    @Getter
    @Setter
    @ToString
    public static class Application {

        private String profileName;
        private String cellName;
        private String nodeName;
        private String clusterName;
        private String serverName;
        private String serverType;
        private String applicationName;
        private String editionStatus;

        @JsonIgnore
        private transient String applicationDeploymentFilePath;

        // 실제 배포되는 경로
        private String applicationBinaryUrlPath;

    }

    @Getter
    @Setter
    @ToString
    public static class Port {

        private String profileName;
        private String cellName;
        private String nodeName;
        private List<ServerEntry> serverEntries;

    }

    @Getter
    @Setter
    @ToString
    public static class ServerEntry {

        private String serverName;
        private List<EndPoint> endPoint;

    }

    @Getter
    @Setter
    @ToString
    public static class EndPoint {

        private String endPointName;
        private int port;

    }

    @Getter
    @Setter
    @ToString
    public static class Instances {
        private String name;
        private String minHeap;
        private String maxHeap;
        private String runUser;
        private String vmOption;
    }

}
//end of WebSphereAssessmentResult.java
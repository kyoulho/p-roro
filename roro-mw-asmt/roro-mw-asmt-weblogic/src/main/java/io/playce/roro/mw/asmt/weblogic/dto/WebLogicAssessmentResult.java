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
package io.playce.roro.mw.asmt.weblogic.dto;

import io.playce.roro.mw.asmt.dto.MiddlewareAssessmentResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
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
public class WebLogicAssessmentResult extends MiddlewareAssessmentResult {

    @Getter
    @Setter
    @ToString
    public static class Engine extends MiddlewareAssessmentResult.Engine {
        private String path;
        private String name;
        private String version;
        private String vendor;
    }

    @Getter
    @Setter
    @ToString
    public static class Instance extends MiddlewareAssessmentResult.Instance {
        private String domainHome;
//        private Long pid;
//        private Long parentPid;
        private List<ConfigFile> configFiles;
        private Cluster cluster;
        private Resource resource;
        private String javaVersion;
        private String javaVendor;
        private List<Application> application;
        private List<Instances> instances;
    }

    @Getter
    @Setter
    @ToString
    public static class ConfigFile {
        private String path;
        private String contents;
    }

    @Getter
    @Setter
    @ToString
    public static class Instances {
        private String name;
        private String type;
        private Long listenPort;
        private String listenAddress;
        private Boolean sslEnabled;
        private Log log;
        private String clusterName;
        private String minHeap;
        private String maxHeap;
        private String runUser;
        private String vmOption;
    }

    @Getter
    @Setter
    @ToString
    public static class Log {
        private String rotationType;
        private String logFileSeverity;
        private String stdoutSeverity;
        private String domainLogBroadcastSeverity;
        private String memoryBufferSeverity;
        private String path;
    }

    @Getter
    @Setter
    @ToString
    public static class Cluster {
        private String name;
        private String clusterMessagingMode;
        private String defaultLoadAlgorithm;
    }

    @Getter
    @Setter
    @ToString
    public static class Application {
        private String name;
        private String target;
        private String moduleType;
        private String sourcePath;
        private String securityDDMode;
        private String stagingMode;
    }

    @Getter
    @Setter
    @ToString
    public static class Resource {
        List<Jdbc> jdbc;
        Jms jms;
    }

    @Getter
    @Setter
    @ToString
    public static class Jdbc {
        private String name;
        private String target;
        private String descriptorFileName;
        private Datasource datasource;
        private JdbcConnectionPoolParams jdbcConnectionPoolParams;
        private JdbcDataSourceParams jdbcDataSourceParams;
    }

    @Getter
    @Setter
    @ToString
    public static class Datasource {
        private String name;
        private JdbcDriverParams jdbcDriverParams;
    }

    @Getter
    @Setter
    @ToString
    public static class JdbcDriverParams {
        private String url;
        private String driverName;
        private List<Properties> properties;
    }

    @Getter
    @Setter
    @ToString
    public static class Properties {
        private String name;
        private String value;
    }

    @Getter
    @Setter
    @ToString
    public static class JdbcConnectionPoolParams {
        private Long initialCapacity;
        private Long maxCapacity;
        private Boolean testConnectionsOnReserve;
        private String testTableName;
        private Long secondsToTrustAnIdlePoolConnection;
    }

    @Getter
    @Setter
    @ToString
    public static class JdbcDataSourceParams {
        private List<String> jndiName;
        private String globalTransactionsProtocol;
    }

    @Getter
    @Setter
    @ToString
    public static class Jms {
        private List<JmsSystemResource> jmsSystemResource;
        private List<JmsServer> jmsServer;
    }

    @Getter
    @Setter
    @ToString
    public static class JmsSystemResource {
        private String name;
        private String descriptorFileName;
        private String target;
        private List<SubDeployment> subDeployment;
    }

    @Getter
    @Setter
    @ToString
    public static class SubDeployment {
        private String name;
        private String target;
    }

    @Getter
    @Setter
    public static class JmsServer {
        private String persistentStore;
        private String name;
        private String target;
        private Boolean hostingTemporaryDestinations;
    }
}
//end of WebLogicAssessmentResult.java
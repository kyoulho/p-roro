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
public class WebSphereAssessmentDto {

    @Getter
    @Setter
    @ToString
    public static class ProfileRegistry {

        private String name;
        private String path;
        private String template;
        private String isDefault;

        // Augmentor는 Version에 따라 존재 유무가 달라질 수가 있다.
        // 6 버전에서는 존재안함.
        // 9 버전에서는 존재함.
        private Augmentor augmentor;

    }

    @Getter
    @Setter
    @ToString
    public static class Augmentor {

        private String template;

    }

    @Getter
    @Setter
    @ToString
    public static class FileInfo {

        private String name;
        private String path;

    }

    @Getter
    @Setter
    @ToString
    public static class DirectoryStructure {

        private List<ProfileFileInfo> profileFileInfos;

    }

    @Getter
    @Setter
    @ToString
    public static class ProfileFileInfo extends FileInfo {

        private List<CellFileInfo> cellFileInfos;

    }

    @Getter
    @Setter
    @ToString
    public static class CellFileInfo extends FileInfo {

        private List<ClusterFileInfo> clusterFileInfos;
        private List<NodeFileInfo> nodeFileInfos;

    }

    @Getter
    @Setter
    @ToString
    public static class ClusterFileInfo extends FileInfo {
    }

    @Getter
    @Setter
    @ToString
    public static class NodeFileInfo extends FileInfo {

        private List<ServerFileInfo> serverFileInfos;

    }

    @Getter
    @Setter
    @ToString
    public static class ServerFileInfo extends FileInfo {
    }

    @Getter
    @Setter
    @ToString
    public static class ClusterParse {

        private String name;
        private String nodeGroupName;
        private List<ClusterMember> members;

    }

    @Getter
    @Setter
    @ToString
    public static class ClusterMember {

        private String nodeName;
        private String memberName;

    }

    @Getter
    @Setter
    @ToString
    public static class ServerIndex {

        private String hostName;
        private List<ServerEntries> serverEntries;

    }

    @Getter
    @Setter
    @ToString
    public static class ServerEntries {

        private String serverType;
        private String serverName;
        private List<String> deployedApplications;
        private List<SpecialEndpoints> specialEndpoints;

    }

    @Getter
    @Setter
    @ToString
    public static class SpecialEndpoints {

        private String endPointName;
        private EndPoint endPoint;

    }

    @Getter
    @Setter
    @ToString
    public static class EndPoint {

        private Integer port;
        private String host;

    }

    @Getter
    @Setter
    @ToString
    public static class SecurityParse {

        private String alias;
        private String userId;
        private String password;

    }


    @Getter
    @Setter
    @ToString
    public static class ResourceParse {

        private List<JdbcProvider> jdbcProvider;

    }

    @Getter
    @Setter
    @ToString
    public static class JdbcProvider {

        private String name;
        private String description;
        private String providerType;
        private String implementationClassName;
        private List<Factories> factories;

    }

    @Getter
    @Setter
    @ToString
    public static class Factories {

        private Long statementCacheSize;
        private String name;
        private String description;
        private String category;
        private String jndiName;
        private String authDataAlias;
        private ConnectionPool connectionPool;
        private PropertySet propertySet;

    }

    @Getter
    @Setter
    @ToString
    public static class ConnectionPool {

        private Integer minConnections;
        private Integer maxConnections;
        private String connectionTimeout;

    }

    @Getter
    @Setter
    @ToString
    public static class PropertySet {

        private List<ResourceProperties> resourceProperties;

    }

    @Getter
    @Setter
    @ToString
    public static class ResourceProperties {

        private String name;
        private String value;

    }

}
//end of WebSphereAssessmentDto.java
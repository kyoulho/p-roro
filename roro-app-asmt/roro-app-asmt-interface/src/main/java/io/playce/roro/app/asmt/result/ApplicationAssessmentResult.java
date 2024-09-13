/*
 * Copyright 2020 The Playce-RoRo Project.
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
 * SangCheon Park   Nov 26, 2020		First Draft.
 */
package io.playce.roro.app.asmt.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.playce.roro.common.dto.thirdparty.ThirdPartyDiscoveryResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 2.0.0
 */
@Getter
@Setter
@ToString
public class ApplicationAssessmentResult {

    protected String applicationDir;
    protected String applicationFile;
    protected String assessmentDir;
    protected String fileName;
    protected String applicationType;
    protected Long applicationSize;
    protected Map<String, FileSummary> fileSummaryMap = new HashMap<>();
    protected List<EEModule> eeModules = new ArrayList<>();
    protected List<File> descriptorFiles = new CopyOnWriteArrayList<>();
    protected List<File> buildFiles = new ArrayList<>();
    protected List<File> configFiles = new ArrayList<>();
    protected Libraries libraries = new Libraries();
    protected List<Check> checkList = new ArrayList<>();
    protected List<DataSource> dataSourceList = new ArrayList<>();
    protected List<HardCodedIp> hardCodedIpList = new ArrayList<>();
    protected List<Deprecated> deprecatedList = new ArrayList<>();
    protected List<Removed> removedList = new ArrayList<>();

    // File 타입 3rd party가 제외되면서 더 이상 사용되지 않음.
    @JsonIgnore
    protected List<ThirdPartyDiscoveryResult> thirdPartySolutions = new ArrayList<>();

    // 추가 분석 라이브러리 목록
    @JsonIgnore
    private transient List<String> analysisLibList;

    // 추가 분석 라이브러리 목록에 대한 Full Path가 저장
    @JsonIgnore
    private transient List<String> analysisLibPathList = new ArrayList<>();

    @Getter
    @Setter
    @ToString
    public static class FileSummary {
        private Long fileCount;
        private Long fileSize;
    }

    @Getter
    @Setter
    @ToString
    public static class EEModule {
        private String displayName;
        private String description;
        private List<String> ejb = new ArrayList<>();
        private List<String> java = new ArrayList<>();
        private List<Web> web = new ArrayList<>();

        @Getter
        @Setter
        @ToString
        public static class Web {
            private String webUri;
            private String contextRoot;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class File {
        private String file;
        private String location;
        private String contents;
    }

    @Getter
    @Setter
    @ToString
    public static class Libraries {
        private List<File> all = new ArrayList<>();
        private List<String> deleteRecommended = new ArrayList<>();
    }

    @Getter
    @Setter
    @ToString
    public static class Check {
        private String fileName;
        private List<Point> apiUsages = new ArrayList<>();
        private List<Point> servletExtends = new ArrayList<>();
        // private List<Point> lookups = new ArrayList<>();
        private List<Point> ipPatterns = new ArrayList<>();
        private List<Point> customPatterns = new ArrayList<>();

        @Getter
        @Setter
        @ToString
        public static class Point {
            private Integer line;
            private String value;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class DataSource {
        // URL or JNDI
        private String type;
        // url string or jndi name
        private String value;
        private List<JdbcProperty> jdbcProperties = new ArrayList<>();
        private List<Use> uses = new ArrayList<>();

        @Setter
        @Getter
        @Builder
        public static class JdbcProperty {
            private String type;
            private String host;
            private Integer port;
            private String database;
            private Map<String, String> params;
        }

        @Getter
        @Setter
        @ToString
        public static class Use {
            private String fileName;
            private Integer line;
            private String value;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class Deprecated {
        private Integer release;
        private List<Use> uses = new ArrayList<>();
    }

    @Getter
    @Setter
    @ToString
    public static class Use {
        @JsonProperty("class")
        private String clazz;
        private Reference reference;
    }

    @Getter
    @Setter
    @ToString
    public static class Reference {
        @JsonProperty("class")
        private String clazz;
        private String method;
        private Boolean forRemoval;
    }

    @Getter
    @Setter
    @ToString
    @JsonPropertyOrder({"clazz", "api", "replacement"})
    public static class Removed {
        @JsonProperty("class")
        private String clazz;
        private String api;
        private String replacement;
    }

    @Getter
    @Setter
    @ToString
    public static class HardCodedIp implements Comparable<HardCodedIp> {
        private String fileName;
        private Integer lineNum;
        private String ipAddress;
        private Integer port;
        private String protocol;

        @Override
        public int compareTo(@NotNull ApplicationAssessmentResult.HardCodedIp y) {
            int compare = this.getFileName().compareTo(y.getFileName());

            if (compare == 0) {
                compare = this.getIpAddress().compareTo(y.getIpAddress());

                if (compare == 0) {
                    int xPort = this.getPort() == null ? 0 : this.getPort();
                    int yPort = y.getPort() == null ? 0 : y.getPort();

                    compare = Integer.compare(xPort, yPort);
                }
            }

            return compare;
        }
    }
}
//end of ApplicationAssessmentResult.java
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
 * Jaeeon Bae       3월 21, 2022            First Draft.
 */
package io.playce.roro.common.dto.assessment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
public class AssessmentResultDto {

    @Getter
    @Setter
    @Builder
    @ToString
    public static class ServerProperty {
        String totalDisk;
        String freeDisk;
        String upTime;
        String model;
        String cpu;
        String memory;
        String osName;
        String osVersion;
        String kernel;
        int userCount;
        int groupCount;
        String listenPort;
        String architecture;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    public static class WebProperty {
        String engineVersion;
        String enginePath;
        Long listenPort;
        boolean sslUsed;
        Long sslPort;
        String documentRoot;
        String logDirectory;
        String includeFiles;
        String runUser;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    public static class WasProperty {
        String engineVersion;
        String enginePath;
        String domainHome;
        boolean clusterUsed;
        String javaVersion;
        String configFiles;
        String minHeap;
        String maxHeap;
        String runUser;
        String vmOption;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    public static class DatabaseProperty {
        String version;
        Long tableCount;
        Long viewCount;
        Long indexCount;
        Long functionCount;
        Long procedureCount;
        Long triggerCount;
        Long sequenceCount;
        Long dbLinkCount;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    public static class ApplicationProperty {
        String fileName;
        String type;
        Long cssCount;
        Long htmlCount;
        Long xmlCount;
        Long jspCount;
        Long jsCount;
        Long javaCount;
        Long classCount;
        Long buildFileCount;
        Long configFileCount;
        Long libraryCount;
        Long servletCount;
        Long ejbJtaCount;
        Long specificIpIncludeCount;
        Long lookupPatternCount;
        Long customPatternCount;
        Long deprecatedApiClassCount;
        Long deleteApiClassCount;
    }
}
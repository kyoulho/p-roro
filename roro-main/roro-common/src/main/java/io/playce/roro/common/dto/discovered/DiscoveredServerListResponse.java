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
 * Hoon Oh       2월 22, 2022            First Draft.
 */
package io.playce.roro.common.dto.discovered;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

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
@Setter
@ToString
public class DiscoveredServerListResponse {

    private Data data;

    @Getter
    @Setter
    public static class Data {
        private Long totalCount;
        private List<Content> contents;
    }

    @Getter
    @Setter
    public static class Content {
        private Long discoveredInstanceId;
        private Long targetId;
        private String targetName;
        private String targetIp;
        private int port;
        private String protocol;
        private String resourceType;
        private Long resourceId;
        private String resourceName;
        private String resourceSubType;
        private String serviceName;
        private String sourceId;
        private String sourceName;
        private String sourceIp;
        private Date discoveredDate;
    }

}
//end of DiscoveredServerResponse.java
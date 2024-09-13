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
package io.playce.roro.mw.asmt.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.playce.roro.common.dto.thirdparty.ThirdPartyDiscoveryResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
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
@ToString
public class MiddlewareAssessmentResult {

    protected Engine engine;

    protected Instance instance;

    // File 타입 3rd party가 제외되면서 더 이상 사용되지 않음.
    @JsonIgnore
    protected List<ThirdPartyDiscoveryResult> thirdPartySolutions = new ArrayList<>();

    @JsonIgnore
    protected transient List<DiscApplication> applicationList;

    @JsonIgnore
    protected transient List<DiscDatabase> databaseList;

    public static class Engine {}

    public static class Instance {}

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
}
//end of MiddlewareAssessmentResult.java
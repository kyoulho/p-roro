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
 * Hoon Oh       2월 17, 2022            First Draft.
 */
package io.playce.roro.discover.database.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

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
public class UnknownDatabaseDto {

    private String type;
    private Integer port;
    private String dbServiceName;
    private String connectionUrl;
    private String username;

    @Setter
    @Getter
    @ToString
    public static class Process {

        private List<String> cmd;
        private String pid;
        private String user;

    }

    @Setter
    @Getter
    @Builder
    @ToString
    public static class JdbcProperty {

        private String driverName;
        private String host;
        private Integer port;
        private String database;
        private Map<String, String> params;

    }

}
//end of UnknownDatabaseDto.java
/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * SangCheon Park   Mar 22, 2022		    First Draft.
 */
package io.playce.roro.common.dto.websocket;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
@ToString
public class AssessmentMessage extends Message {

    private ServerMessage server;
    private MiddlewareMessage middleware;
    private ApplicationMessage application;
    private DatabaseMessage database;

    @Getter
    @Setter
    @ToString
    public static class ServerMessage {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    @ToString
    public static class MiddlewareMessage {
        private Long id;
        private String name;
        private String version;
        private Integer instances;
    }

    @Getter
    @Setter
    @ToString
    public static class ApplicationMessage {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    @ToString
    public static class DatabaseMessage {
        private Long id;
        private String name;
        private String version;
        private Integer instances;
    }
}
//end of AssessmentMessage.java
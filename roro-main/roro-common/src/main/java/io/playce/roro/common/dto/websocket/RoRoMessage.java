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
 * RoRo Create, Update, Delete의 결과에 대한 Json Response 및 WebSocket을 이용한 메시지 송수신에 사용하는 공통 메시지 클래스
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Getter
@Setter
@ToString
public class RoRoMessage {

    private long timestamp;
    private String message;
    private Object status;
    private Integer code;
    private Object data;

    /**
     * Instantiates a new Roro Message.
     */
    public RoRoMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Instantiates a new Wasup message.
     *
     * @param code the code
     */
    public RoRoMessage(int code) {
        this();
        this.code = code;
    }
}
//end of RoRoMessage.java
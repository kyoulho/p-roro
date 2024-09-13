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
package io.playce.roro.api.websocket.constants;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
public class WebSocketConstants {

    /** Browser -> RoRo로 초기 시스템 정보 전송 (1) */
    public static final int WS_CODE_LOGIN = 0x01;

    /** RoRo -> Browser UUID 전송 (2) */
    public static final int WS_CODE_LOGIN_RESPONSE = 0x02;

    /** RoRo -> Browser 로 Notification Message 전송 (3) */
    public static final int WS_CODE_NOTIFICATION_MESSAGE = 0x03;

    /** RoRo -> Browser Settings Message 전송 (4) */
    public static final int WS_CODE_MESSAGE_SETTINGS = 0x04;

    /** Browser -> RoRo Heartbeat 전송 (5) */
    public static final int WS_CODE_HEARTBEAT = 0x05;

    /** RoRo -> Browser Log Tail 용 Topic UUID 전송 (16) */
    public static final int WS_CODE_LOG_TAIL_INIT = 0x10;

    /** Browser -> RoRo, RoRo -> Browser 로 READY 이벤트 전송 (17) */
    public static final int WS_CODE_LOG_TAIL_READY = 0x11;

    /** Browser -> RoRo로 LISTENING 이벤트 전송 (18) */
    public static final int WS_CODE_LOG_TAIL_LISTENING = 0x12;

    /** RoRo -> Browser로 Log Tail Message 전송 (19) */
    public static final int WS_CODE_LOG_TAIL_MESSAGE = 0x13;

    /** Browser -> RoRo로 STOP 이벤트 전송 (20) */
    public static final int WS_CODE_LOG_TAIL_STOP = 0x14;

    /**
     * End point
     */
    public static final String WS_QUEUE_USER = "/user";
    public static final String WS_QUEUE_REPLY = "/queue/reply";
    public static final String WS_QUEUE_ERROR = "/queue/error";

    /**
     * 구독하고 있는 하나의 로그인 대상에 Notification 메시지를 전달한다.
     */
    public static final String WS_QUEUE_NOTIFICATION = "/queue/notification";

    /**
     * /topic/log/{Something Like UUID} 는 사용자에 의해 요청된 Tail Log 등 브라우저와 RoRo 간 통신에 사용.
     */
    public static final String WS_TOPIC_LOG = "/topic/log";

    public static final String WS_APP_HEARTBEAT = "/app/heartbeat";
    public static final String WS_APP_RESULT = "/app/result";

    // WEB_SSH
    public static final String USER_UUID_KEY = "user_uuid";
    public static final String WEBSSH_OPERATE_CONNECT = "connect";
    public static final String WEBSSH_OPERATE_COMMAND = "command";
    public static final String WEBSSH_OPERATE_RESIZE = "resize";
    public static final String WEBSSH_OPERATE_PING = "ping";
}
//end of WebSocketConstants.java
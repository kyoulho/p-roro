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
package io.playce.roro.api.websocket.manager;

import io.playce.roro.api.websocket.listener.RoRoSessionListener;
import io.playce.roro.common.dto.websocket.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.playce.roro.api.websocket.constants.WebSocketConstants.WS_CODE_NOTIFICATION_MESSAGE;
import static io.playce.roro.api.websocket.constants.WebSocketConstants.WS_QUEUE_NOTIFICATION;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketManager {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoRoSessionListener sessionListener;

    /**
     * send notification.
     */
    public void sendNotification(Message data) {
        StringBuffer sb = new StringBuffer();

        RoRoMessage message = new RoRoMessage();
        message.setCode(WS_CODE_NOTIFICATION_MESSAGE);
        message.setTimestamp(System.currentTimeMillis());
        message.setStatus(data.getStatus().fullname().toUpperCase());
        message.setData(data);

        if (data instanceof AssessmentMessage) {
            AssessmentMessage msg = (AssessmentMessage) data;

            if (msg.getServer() != null) {
                sb.append("Server ");
            } else if (msg.getMiddleware() != null) {
                sb.append("Middleware ");
            } else if (msg.getApplication() != null) {
                sb.append("Application ");
            } else if (msg.getDatabase() != null) {
                sb.append("Database ");
            }

            sb.append("assessment ").append(msg.getStatus().fullname().toLowerCase());
        } else if (data instanceof MigrationMessage) {
            MigrationMessage msg = (MigrationMessage) data;

            sb.append("Migration ").append(msg.getStatus().fullname().toLowerCase())
                    .append(" (").append(msg.getServer().getName()).append(" → ").append(msg.getInstanceName()).append(")");
        } else if (data instanceof PreRequisiteMessage) {
            PreRequisiteMessage msg = (PreRequisiteMessage) data;

            sb.append("Server prerequisite check ").append(msg.getStatus().fullname().toLowerCase());
        }

        message.setMessage(sb.toString());

        List<String> sessionIdList = sessionListener.getAllBrowserSessionIds();

        // send to user
        for (String sessionId : sessionIdList) {
            simpMessagingTemplate.convertAndSendToUser(sessionId, WS_QUEUE_NOTIFICATION + "/" + sessionId, message, sessionListener.createHeaders(sessionId));
        }
    }
}
//end of WebSocketManager.java
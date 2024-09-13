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
package io.playce.roro.api.websocket;

import io.playce.roro.common.dto.websocket.RoRoMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

import static io.playce.roro.api.websocket.constants.WebSocketConstants.*;

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
public class RoRoSessionHandler extends StompSessionHandlerAdapter {

    /**
     * The session.
     */
    private StompSession session;

    /**
     * After connected.
     *
     * @param session          the session
     * @param connectedHeaders the connected headers
     */
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        log.debug("New websocket session established.");
        this.session = session;

        subscribe(WS_QUEUE_USER + WS_QUEUE_REPLY);
        subscribe(WS_QUEUE_USER + WS_QUEUE_ERROR);
    }

    /**
     * Handle exception.
     *
     * @param session   the session
     * @param command   the command
     * @param headers   the headers
     * @param payload   the payload
     * @param exception the exception
     */
    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        log.error("Got an exception. Reason : {}", exception.getMessage());
        log.error("StompHeaders : [{}], Payload : [{}]", headers, new String(payload));
    }

    /**
     * Gets payload type.
     *
     * @param headers the headers
     *
     * @return the payload type
     */
    @Override
    public Type getPayloadType(StompHeaders headers) {
        return RoRoMessage.class;
    }

    /**
     * Handle frame.
     *
     * @param headers the headers
     * @param payload the payload
     */
    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        RoRoMessage msg = (RoRoMessage) payload;
        log.debug("Message ID : [{}], Received Message : [{}]", headers.getMessageId(), msg);
    }

    /**
     * Is connected boolean.
     *
     * @return the boolean
     */
    public synchronized boolean isConnected() {
        boolean isConnected = false;

        if (session != null && session.isConnected()) {
            isConnected = true;
        }

        return isConnected;
    }

    /**
     * Subscribe.
     *
     * @param destination the destination
     */
    public synchronized void subscribe(String destination) {
        session.subscribe(destination, this);
        log.debug("[{}] Subscribed.", destination);
    }
}
//end of RoRoSessionHandler.java
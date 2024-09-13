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
package io.playce.roro.api.websocket.listener;

import io.playce.roro.common.dto.websocket.BrowserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class RoRoSessionListener {

    /**
     * The Browser Session map.
     * Key is Session ID & Value is BrowserSession.
     */
    private static Map<String, BrowserSession> browserSessionMap = new ConcurrentHashMap<>();

    /**
     * On session connected event.
     *
     * @param event the event
     */
    @EventListener
    public void onSessionConnectedEvent(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        GenericMessage msg = (GenericMessage) sha.getMessageHeaders().get("simpConnectMessage");

        // log.debug("Received a new websocket connection. Session ID : [{}], message : [{}]", sha.getSessionId(), msg);
    }

    /**
     * On session disconnect event.
     *
     * @param event the event
     */
    @EventListener
    public void onSessionDisconnectedEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        browserSessionMap.remove(sha.getSessionId());

        // log.debug("Websocket session closed. Message : [{}]", event.getMessage());
    }

    /**
     * Register browser session.
     *
     * @param sessionId      the session id
     * @param browserSession the browser session
     */
    public synchronized void registerBrowserSession(String sessionId, BrowserSession browserSession) {
        browserSessionMap.put(sessionId, browserSession);
    }

    /**
     * Gets all browser session Ids.
     *
     * @return the all browser session Ids
     */
    public List<String> getAllBrowserSessionIds() {
        return new ArrayList<>(browserSessionMap.keySet());
    }

    /**
     * Find session ids by member id list.
     *
     * @param memberId the member id
     *
     * @return the list
     */
    public List<String> findSessionIdsByMemberId(Long memberId) {
        List<String> sessionIdList = new ArrayList<String>();

        for (Map.Entry<String, BrowserSession> entry : browserSessionMap.entrySet()) {
            if (entry.getValue().getMemberId().equals(memberId)) {
                sessionIdList.add(entry.getKey());
            }
        }

        return sessionIdList;
    }

    /**
     * Find session id by uuid string.
     *
     * @param uuid the uuid
     *
     * @return the string
     */
    public String findSessionIdByUUID(String uuid) {
        String sessionId = null;

        for (Map.Entry<String, BrowserSession> entry : browserSessionMap.entrySet()) {
            if (entry.getValue().getUuid().equals(uuid)) {
                sessionId = entry.getKey();
                break;
            }
        }

        return sessionId;
    }

    /**
     * Find browser session browser session.
     *
     * @param sessionId the session id
     *
     * @return the browser session
     */
    public BrowserSession findBrowserSession(String sessionId) {
        return browserSessionMap.get(sessionId);
    }

    /**
     * Create headers message headers.
     *
     * @param sessionId the session id
     *
     * @return the message headers
     */
    public MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);

        return headerAccessor.getMessageHeaders();
    }
}
//end of RoRoSessionListener.java
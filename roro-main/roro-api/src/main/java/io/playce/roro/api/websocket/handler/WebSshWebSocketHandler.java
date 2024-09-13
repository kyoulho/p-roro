package io.playce.roro.api.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.api.websocket.constants.WebSocketConstants;
import io.playce.roro.api.websocket.dto.WebSshDto;
import io.playce.roro.api.websocket.service.WebSshService;
import io.playce.roro.common.config.RoRoProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
@Component
public class WebSshWebSocketHandler implements WebSocketHandler {
    private final WebSshService webSSHService;
    private final ObjectMapper objectMapper;
    private final RoRoProperties roroProperties;

    // callback of websocket on user connection
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        // 옵션에 따른 Web Terminal 기능 사용
        if (roroProperties.isWebTerminal()) {
            log.info("web terminal - Connected. uuid: {}", webSocketSession.getAttributes().get(WebSocketConstants.USER_UUID_KEY));
            //Call initialize connection
            webSSHService.initConnection(webSocketSession);
        } else {
            try {
                webSSHService.sendMessage(webSocketSession, "Web terminal is disabled and will be disconnected.");
                webSocketSession.close();
            } catch (Exception ignore) {
                // ignore
            }
        }
    }

    // callback of received message
    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) {
        // 옵션에 따른 Web Terminal 기능 사용
        if (roroProperties.isWebTerminal()) {
            if (webSocketMessage instanceof TextMessage) {
                try {
                    //Convert JSON sent by the front end
                    WebSshDto webSshDto = objectMapper.readValue(((TextMessage) webSocketMessage).getPayload(), WebSshDto.class);

                    if (webSshDto.getOperate().equals(WebSocketConstants.WEBSSH_OPERATE_PING)) {
                        webSocketSession.sendMessage(new PongMessage());
                    } else if (webSshDto.getOperate().equals(WebSocketConstants.WEBSSH_OPERATE_RESIZE)) {
                        webSSHService.resize(webSshDto, webSocketSession);
                    } else {
                        //log.info("user: {}, send command: {}", webSocketSession.getAttributes().get(WebSocketConstants.USER_UUID_KEY), webSocketMessage);
                        //Call service to receive messages
                        webSSHService.receiveHandle(webSshDto, webSocketSession);
                    }
                } catch (IOException e) {
                    log.error("JSON conversion exception occurred.", e);
                    return;
                }
            } else if (webSocketMessage instanceof BinaryMessage) {
            } else if (webSocketMessage instanceof PingMessage) {
                webSSHService.receivePing((PingMessage) webSocketMessage, webSocketSession);
            } else if (webSocketMessage instanceof PongMessage) {
            } else {
                log.info("Unexpected WebSocket message type: {}", webSocketMessage);
            }
        } else {
            try {
                webSSHService.sendMessage(webSocketSession, "Web terminal is disabled and will be disconnected.");
                webSocketSession.close();
            } catch (Exception ignore) {
                // ignore
            }
        }
    }

    // wrong callback occurred
    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) {
        log.error("web terminal - Data transfer error : {}", throwable.getMessage());
    }

    // callback for connection closing
    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        log.info("web terminal - Disconnected. uuid: {}", webSocketSession.getAttributes().get(WebSocketConstants.USER_UUID_KEY));
        //Call service to close the connection
        webSSHService.close(webSocketSession);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}
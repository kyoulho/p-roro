package io.playce.roro.api.websocket.service;

import io.playce.roro.api.websocket.dto.WebSshDto;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface WebSshService {

    // initialize SSH connection
    void initConnection(WebSocketSession session);

    //process the data sent by customers
    void receiveHandle(WebSshDto webSshDto, WebSocketSession session);

    //write data back to the front end for websocket
    void sendMessage(WebSocketSession session, String message) throws IOException;

    //write data back to the front end for websocket
    void sendMessage(WebSocketSession session, byte[] buffer) throws IOException;

    // close the connection
    void close(WebSocketSession session);

    void receivePing(PingMessage message, WebSocketSession session);

    void resize(WebSshDto webSshDto, WebSocketSession session);
}

package io.playce.roro.api.config;

import io.playce.roro.api.websocket.handler.WebSshWebSocketHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@AllArgsConstructor
public class WebSshWebSocketConfig implements WebSocketConfigurer {
    private final WebSshWebSocketHandler webSshWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        //Socket channel
        //Specify processor and path, and set cross domain
        webSocketHandlerRegistry.addHandler(webSshWebSocketHandler, "/webssh")
                .addInterceptors(new WebSocketInterceptor())
                .setAllowedOrigins("*");
    }
}

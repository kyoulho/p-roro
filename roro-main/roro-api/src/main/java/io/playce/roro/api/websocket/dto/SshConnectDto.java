package io.playce.roro.api.websocket.dto;

import com.jcraft.jsch.ChannelShell;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Setter
public class SshConnectDto {

    private WebSocketSession webSocketSession;
    private ChannelShell channel;

}

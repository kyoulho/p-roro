package io.playce.roro.api.websocket.service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.config.JwtProperties;
import io.playce.roro.api.domain.authentication.dto.SecurityUser;
import io.playce.roro.api.domain.authentication.jwt.JwtTokenException;
import io.playce.roro.api.domain.authentication.jwt.token.AccessJwtToken;
import io.playce.roro.api.domain.authentication.jwt.token.RawAccessJwtToken;
import io.playce.roro.api.domain.authentication.service.CustomUserDetailsService;
import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.api.websocket.constants.WebSocketConstants;
import io.playce.roro.api.websocket.dto.SshConnectDto;
import io.playce.roro.api.websocket.dto.WebSshDto;
import io.playce.roro.common.dto.common.ServerConnectionInfo;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@AllArgsConstructor
@Service
public class WebSshServiceImpl implements WebSshService {

    private final ServerService serverService;
    private final ServerMapper serverMapper;
    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService customUserDetailsService;

    private static final Map<String, Object> sshMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void initConnection(WebSocketSession session) {
        SshConnectDto sshConnectDto = new SshConnectDto();
        sshConnectDto.setWebSocketSession(session);

        String uuid = String.valueOf(session.getAttributes().get(WebSocketConstants.USER_UUID_KEY));

        //Put the SSH connection information into the map
        sshMap.put(uuid, sshConnectDto);
    }

    @Override
    public void receiveHandle(WebSshDto webSshDto, WebSocketSession session) {
        //Get the random UUID just set
        String userId = String.valueOf(session.getAttributes().get(WebSocketConstants.USER_UUID_KEY));

        if (WebSocketConstants.WEBSSH_OPERATE_CONNECT.equals(webSshDto.getOperate())) {
            try {
                String tokenPayload = webSshDto.getToken();

                // JWT Validation
                RawAccessJwtToken rawToken = new RawAccessJwtToken(tokenPayload);
                AccessJwtToken token = AccessJwtToken.verify(rawToken, jwtProperties.getTokenSigningKey())
                        .orElseThrow(() -> new JwtTokenException("Access token verification failed."));
// 사용자 정보는 oauthdb에 저장하므로 rorodb에서 조회 불가능
//                Map<String, String> userMap = token.getJwsClaims().getBody().get("user", Map.class);
//
//                String username = userMap.get("userLoginId");
//                SecurityUser securityUser = customUserDetailsService.loadUserByUsername(username);
//
//                if (securityUser == null) {
//                    throw new ResourceNotFoundException("User does not exists.");
//                }
            } catch (Exception e) {
                try {
                    sendMessage(session, e.getMessage());
                } catch (IOException ignore) {
                    // ignore
                }

                log.error("web terminal - JWT validation failed.", e);
                close(session);
                return;
            }

            // Windows 서버는 Web Terminal 기능 사용 불가
            try {
                ServerConnectionInfo connectionInfo = serverMapper.selectServerConnectionInfoByInventoryId(webSshDto.getServerInventoryId());
                if (connectionInfo == null || "Y".equals(connectionInfo.getDeleteYn())) {
                    throw new ResourceNotFoundException("Server does not exist.");
                }
                if ("Y".equals(connectionInfo.getWindowsYn())) {
                    throw new RoRoException("Windows server can't connect via web terminal.");
                }
            } catch (Exception e) {
                try {
                    sendMessage(session, e.getMessage());
                } catch (IOException ignore) {
                    // ignore
                }

                log.error("web terminal - Server does not exist or server is windows.", e);
                close(session);
                return;
            }

            //If it's a connection request
            //Find the SSH connection object you just stored
            SshConnectDto sshConnectDto = (SshConnectDto) sshMap.get(userId);

            //Start thread asynchronous processing
            WebSshDto finalWebSshData = webSshDto;
            executorService.execute(() -> {
                try {
                    //Connect to terminal
                    connectToSsh(sshConnectDto, finalWebSshData, session);
                } catch (Exception e) {
                    log.error("web terminal - Connect operation execute failed.", e);
                    close(session);
                }
            });
        } else if (WebSocketConstants.WEBSSH_OPERATE_COMMAND.equals(webSshDto.getOperate())) {
            //If it's a request to send a command
            String command = webSshDto.getCommand();
            SshConnectDto sshConnectInfo = (SshConnectDto) sshMap.get(userId);
            if (sshConnectInfo != null) {
                try {
                    //Send command to terminal
                    transToSsh(sshConnectInfo.getChannel(), command);
                } catch (IOException e) {
                    log.error("web terminal - Command operation execute failed.", e);
                    close(session);
                }
            }
        } else {
            log.error("web terminal - Unsupported operation({})", webSshDto.getOperate());
            close(session);
        }
    }

    @Override
    public void sendMessage(WebSocketSession session, String message) throws IOException {
        if (StringUtils.isNotEmpty(message)) {
            sendMessage(session, message.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void sendMessage(WebSocketSession session, byte[] buffer) throws IOException {
        session.sendMessage(new TextMessage(buffer));
    }

    @Override
    public void close(WebSocketSession session) {
        //Get randomly generated UUID
        String userId = String.valueOf(session.getAttributes().get(WebSocketConstants.USER_UUID_KEY));
        SshConnectDto sshConnectDto = (SshConnectDto) sshMap.get(userId);
        if (sshConnectDto != null) {
            //Disconnect
            if (sshConnectDto.getChannel() != null) {
                sshConnectDto.getChannel().disconnect();
            }

            //Remove the SSH connection information from the map
            sshMap.remove(userId);
        }

        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException ignore) {
            // ignore
        }
    }

    @Override
    public void receivePing(PingMessage message, WebSocketSession session) {
        try {
            session.sendMessage(new PongMessage());
        } catch (IOException ignore) {
            // ignore
        }
    }

    @Override
    public void resize(WebSshDto webSshDto, WebSocketSession session) {
        //Get the random UUID just set
        String userId = String.valueOf(session.getAttributes().get(WebSocketConstants.USER_UUID_KEY));

        SshConnectDto sshConnectInfo = (SshConnectDto) sshMap.get(userId);
        if (sshConnectInfo != null) {
            sshConnectInfo.getChannel().setPtySize(webSshDto.getCols(), webSshDto.getRows(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
    }

    private void connectToSsh(SshConnectDto sshConnectDto, WebSshDto webSshDto, WebSocketSession webSocketSession) throws Exception {
        TargetHost targetHost = serverService.getTargetHostByServerInventoryId(webSshDto.getServerInventoryId());

        Session session = null;
        ChannelShell channel = null;
        InputStream inputStream = null;

        try {
            session = SSHUtil.getSession(targetHost, 20 * 1000);

            channel = (ChannelShell) session.openChannel("shell");
            // channel.setPty(true);
            // 기본값 : 80, 24, 640, 480
            channel.setPtySize(webSshDto.getCols(), webSshDto.getRows(), Integer.MAX_VALUE, Integer.MAX_VALUE);
            channel.connect(10000);
            sshConnectDto.setChannel(channel);

            inputStream = channel.getInputStream();

            byte[] buffer = new byte[1024];
            int i = 0;
            boolean suRequired = false;

            if (!"root".equals(targetHost.getUsername()) && StringUtils.isNotEmpty(targetHost.getRootPassword())) {
                suRequired = true;
            }

            while ((i = inputStream.read(buffer)) != -1) {
                sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));

                // Switch User to root (웹 터미널 상에 su - 명령 및 암호 입력을 위해 while 문 안에 위치시킴)
                if (suRequired) {
                    transToSsh(channel, "su -\r");
                    Thread.sleep(500);
                    transToSsh(channel, targetHost.getRootPassword() + "\r");

                    suRequired = false;
                }
            }
        } catch (Exception e) {
            StringBuilder message = new StringBuilder("SSH connect to ")
                    .append(targetHost.getUsername())
                    .append("@")
                    .append(targetHost.getIpAddress())
                    .append(" port ")
                    .append(targetHost.getPort())
                    .append(" : ");

            if (e instanceof JSchException && e.getCause() != null) {
                message.append(e.getCause().getMessage());
            } else {
                message.append(e.getMessage());
            }

            message.append("\n");

            log.error("{}", message, e);
            sendMessage(webSocketSession, message.toString());
        } finally {
            close(webSocketSession);
            if (session != null) {
                session.disconnect();
            }

            if (channel != null) {
                channel.disconnect();
            }

            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void transToSsh(Channel channel, String command) throws IOException {
        if (channel != null) {
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
        }
    }
}

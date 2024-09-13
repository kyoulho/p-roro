package io.playce.roro.db.asmt.sybase.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Server {

    private int serverId;
    private String serverName;
    private String serverNetName;
    private String serverStatus;
    private String serverClass;
    private String securityMechanism;

}
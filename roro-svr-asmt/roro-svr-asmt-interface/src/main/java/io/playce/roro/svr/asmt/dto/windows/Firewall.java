package io.playce.roro.svr.asmt.dto.windows;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Firewall {

    private String name;
    private String displayName;
    private String description;
    private String protocol;
    private List<String> localPort;
    private List<String> remotePort;
    private List<String> remoteAddress;
    private String enabled;
    private String direction;
    private String action;

}

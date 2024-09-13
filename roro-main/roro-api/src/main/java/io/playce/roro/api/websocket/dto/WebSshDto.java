package io.playce.roro.api.websocket.dto;

import io.playce.roro.common.util.support.TargetHost;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WebSshDto extends TargetHost {

    private String token;
    private String operate;
    private String command;
    private Integer cols = 150;
    private Integer rows = 50;
    private Long serverInventoryId;

}

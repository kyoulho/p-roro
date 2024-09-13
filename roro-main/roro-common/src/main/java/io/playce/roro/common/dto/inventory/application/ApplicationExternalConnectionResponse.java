package io.playce.roro.common.dto.inventory.application;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationExternalConnectionResponse {
    private Long externalConnectionId;
    private String fileName;
    private Integer lineNum;
    private String ip;
    private Integer port;
    private String protocol;
}

package io.playce.roro.common.dto.websocket;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HostScanMessage extends Message{
    private Long scanHistoryId;
    private String cidr;
}

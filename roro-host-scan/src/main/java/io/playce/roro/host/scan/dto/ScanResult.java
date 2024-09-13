package io.playce.roro.host.scan.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class ScanResult {
    private String ipAddress;

    private String osName;

    private Integer replyTTL;

    private Date discoveredDatetime;
}

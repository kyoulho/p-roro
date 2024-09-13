package io.playce.roro.common.dto.hostscan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class HostScanRequest {
    @Schema(title = "CIDR",description = "스캔을 요청한 CIDR")
    private String cidr;
}

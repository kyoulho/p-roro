package io.playce.roro.common.dto.hostscan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 *  스캔 내역이 담기는 dto
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostScanHistoryDto {
    @Schema(title = "Scan History ID" ,description = "스캔 내역 ID")
    private Long scanHistoryId;

    @Schema(title = "CIDR",description = "스캔을 진행한 CIDR")
    private String cidr;

    @Schema(title ="Scanned Date" ,description = "스캔을 진행한 일시")
    private Long scannedDate;

    @Schema(title = "Discovered Hos Count",description = "발견된 호스트의 개수")
    private int discoveredHostCount;

    @Schema(title = "Completed YN" , description = "완료 여부")
    private String completedYn;


}

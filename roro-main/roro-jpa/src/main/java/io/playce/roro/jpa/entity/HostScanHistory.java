package io.playce.roro.jpa.entity;

import io.playce.roro.common.dto.hostscan.HostScanHistoryDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

@Where(clause = "DELETE_YN='N'")
@Table(name = "HOST_SCAN_HISTORY")
@Entity
@Getter
@NoArgsConstructor
public class HostScanHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCAN_HISTORY_ID")
    private Long scanHistoryId;
    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;
    @Column(name = "CIDR", nullable = false)
    private String cidr;
    @Column(name = "SCAN_START_DATETIME", nullable = false)
    private Date scanStartDatetime;
    @Column(name = "SCAN_END_DATETIME")
    private Date scanEndDatetime;
    @Column(name = "DELETE_YN", nullable = false)
    private String deleteYn;
    @Column(name = "DISCOVERED_HOST_COUNT")
    private int discoveredHostCount;
    @Column(name = "COMPLETED_YN", nullable = false)
    private String completedYn;

    @Builder
    public HostScanHistory(Long projectId, String cidr, Date scanStartDateTime, Date scanEndDateTime, int discoveredHostCount) {
        this.projectId = projectId;
        this.cidr = cidr;
        this.scanStartDatetime = scanStartDateTime;
        this.scanEndDatetime = scanEndDateTime;
        this.discoveredHostCount = discoveredHostCount;
        this.deleteYn = "N";
        this.completedYn = "N";
    }

    public HostScanHistoryDto toDto() {
        return HostScanHistoryDto.builder()
                .scanHistoryId(scanHistoryId)
                .cidr(cidr)
                .scannedDate(scanStartDatetime.getTime())   // Long 으로 변환
                .discoveredHostCount(discoveredHostCount)
                .completedYn(completedYn)
                .build();
    }

    public void complete(Date scanEndDatetime, int discoveredHostCount) {
        this.scanEndDatetime = scanEndDatetime;
        this.discoveredHostCount = discoveredHostCount;
        this.completedYn = "Y";
    }

    public void delete() {
        this.deleteYn = "Y";
    }
}

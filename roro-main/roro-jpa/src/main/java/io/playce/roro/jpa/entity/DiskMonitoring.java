package io.playce.roro.jpa.entity;

import io.playce.roro.jpa.entity.pk.DiskMonitoringId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "disk_monitoring")
@Setter @Getter
@ToString
public class DiskMonitoring {
    @EmbeddedId
    private DiskMonitoringId id;

    @Column(name = "SAMPLE_COUNT")
    private Integer sampleCount;

    @Column(name = "DISK_AVG")
    private Double diskAvg;

    @Column(name = "DISK_MAX")
    private Double diskMax;

    @Column(name = "DISK_USAGE_AVG")
    private Double diskUsageAvg;

    @Column(name = "DISK_USAGE_MAX")
    private Double diskUsageMax;
}
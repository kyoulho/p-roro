package io.playce.roro.jpa.entity;

import io.playce.roro.jpa.entity.pk.MemoryMonitoringId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "memory_monitoring")
@Setter @Getter
@ToString
public class MemoryMonitoring {
    @EmbeddedId
    private MemoryMonitoringId id;

    @Column(name = "SAMPLE_COUNT")
    private Integer sampleCount;

    @Column(name = "MEMORY_AVG")
    private Double memoryAvg;

    @Column(name = "MEMORY_MAX")
    private Double memoryMax;

    @Column(name = "MEMORY_USAGE_AVG")
    private Double memoryUsageAvg;

    @Column(name = "MEMORY_USAGE_MAX")
    private Double memoryUsageMax;
}
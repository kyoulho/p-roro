package io.playce.roro.jpa.entity;

import io.playce.roro.jpa.entity.pk.CpuMonitoringId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "cpu_monitoring")
@Setter @Getter
@ToString
public class CpuMonitoring {
    @EmbeddedId
    private CpuMonitoringId id;

    @Column(name = "SAMPLE_COUNT")
    private Integer sampleCount;

    @Column(name = "CPU_AVG")
    private Double cpuAvg;

    @Column(name = "CPU_MAX")
    private Double cpuMax;
}
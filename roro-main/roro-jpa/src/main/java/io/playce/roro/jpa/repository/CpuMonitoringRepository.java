package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.CpuMonitoring;
import io.playce.roro.jpa.entity.pk.CpuMonitoringId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CpuMonitoringRepository extends JpaRepository<CpuMonitoring, CpuMonitoringId> {
}
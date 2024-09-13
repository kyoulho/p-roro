package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.MemoryMonitoring;
import io.playce.roro.jpa.entity.pk.MemoryMonitoringId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemoryMonitoringRepository extends JpaRepository<MemoryMonitoring, MemoryMonitoringId> {
}
package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.DiskMonitoring;
import io.playce.roro.jpa.entity.pk.DiskMonitoringId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiskMonitoringRepository extends JpaRepository<DiskMonitoring, DiskMonitoringId> {
}
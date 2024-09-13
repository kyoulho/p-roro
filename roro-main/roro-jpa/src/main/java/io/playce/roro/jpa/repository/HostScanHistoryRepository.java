package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.HostScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HostScanHistoryRepository extends JpaRepository<HostScanHistory, Long>, JpaSpecificationExecutor<HostScanHistory> {
    Optional<HostScanHistory> findByProjectIdAndScanHistoryId(Long projectId, Long scanHistoryId);

    List<HostScanHistory> findByProjectIdOrderByScanStartDatetimeDesc(Long projectId);

    List<HostScanHistory> findByProjectId(Long projectId);

    Optional<HostScanHistory> findFirstByOrderByScanHistoryIdDesc();
}

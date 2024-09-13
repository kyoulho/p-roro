package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.DiscoveredHost;
import io.playce.roro.jpa.entity.pk.DiscoveredHostId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscoveredHostRepository extends JpaRepository<DiscoveredHost, DiscoveredHostId>, JpaSpecificationExecutor<DiscoveredHost> {

    int countByDiscoveredHostIdScanHistoryId(Long scanHistoryId);
}

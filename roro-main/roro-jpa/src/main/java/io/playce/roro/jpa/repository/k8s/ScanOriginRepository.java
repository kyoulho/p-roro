package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.ScanOrigin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScanOriginRepository extends JpaRepository<ScanOrigin, Long> {
    Optional<ScanOrigin> findByClusterScanIdAndCommandKey(Long clusterScanId, String commandKey);
}

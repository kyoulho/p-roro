package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NamespaceRepository extends JpaRepository<Namespace, Long> {
    Optional<Namespace> findByClusterScanIdAndName(Long clusterScanId, String name);
}

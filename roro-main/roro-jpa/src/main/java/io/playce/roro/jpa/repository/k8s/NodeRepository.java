package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.Node;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node, Long> {
    Optional<Node> findByClusterScanIdAndName(Long clusterScanId, String s);
}

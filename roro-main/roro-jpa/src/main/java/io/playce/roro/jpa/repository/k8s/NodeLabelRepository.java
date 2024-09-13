package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.NodeLabel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeLabelRepository extends JpaRepository<NodeLabel, Long> {
}

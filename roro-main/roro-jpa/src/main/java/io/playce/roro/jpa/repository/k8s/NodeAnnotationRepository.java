package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.NodeAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeAnnotationRepository extends JpaRepository<NodeAnnotation, Long> {
}

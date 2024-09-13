package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.K8sServicePort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface K8sServicePortRepository extends JpaRepository<K8sServicePort, Long> {
}

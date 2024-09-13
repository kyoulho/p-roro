package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.K8sService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface K8sServiceRepository extends JpaRepository<K8sService, Long> {
}

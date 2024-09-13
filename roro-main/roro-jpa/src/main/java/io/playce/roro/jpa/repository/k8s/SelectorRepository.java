package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.K8sService;
import io.playce.roro.jpa.entity.k8s.Selector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SelectorRepository extends JpaRepository<Selector, Long> {
}

package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.Ingress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngressRepository extends JpaRepository<Ingress, Long> {
}

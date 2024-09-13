package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.IngressRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngressRuleRepository extends JpaRepository<IngressRule, Long> {
}

package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.IngressRuleTarget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngressRuleTargetRepository extends JpaRepository<IngressRuleTarget, Long> {
}

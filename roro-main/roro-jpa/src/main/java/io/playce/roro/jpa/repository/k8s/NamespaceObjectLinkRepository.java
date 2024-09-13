package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.NamespaceObjectLink;
import io.playce.roro.jpa.entity.pk.NamespaceObjectLinkId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NamespaceObjectLinkRepository extends JpaRepository<NamespaceObjectLink, NamespaceObjectLinkId> {
}

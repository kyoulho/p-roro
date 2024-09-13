package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.ObjectOriginJson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjectOriginJsonRepository extends JpaRepository<ObjectOriginJson, Long> {
}

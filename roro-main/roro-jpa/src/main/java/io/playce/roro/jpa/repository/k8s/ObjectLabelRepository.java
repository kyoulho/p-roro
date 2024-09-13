package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.ObjectLabel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjectLabelRepository extends JpaRepository<ObjectLabel, Long> {
    void deleteByObjectId(Long objectId);
}

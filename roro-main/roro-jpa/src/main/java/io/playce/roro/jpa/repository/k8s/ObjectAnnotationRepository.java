package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.ObjectAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjectAnnotationRepository extends JpaRepository<ObjectAnnotation, Long> {
    void deleteByObjectId(Long objectId);
}

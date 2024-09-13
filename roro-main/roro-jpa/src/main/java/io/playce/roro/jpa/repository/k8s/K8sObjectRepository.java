package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.K8sObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface K8sObjectRepository extends JpaRepository<K8sObject, Long> {
    Optional<K8sObject> findByClusterScanIdAndUid(Long clusterScanId, String uid);
}

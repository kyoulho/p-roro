package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.MiddlewareInstanceApplicationInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MiddlewareInstanceApplicationInstanceRepository extends JpaRepository<MiddlewareInstanceApplicationInstance, Long> {
    Optional<MiddlewareInstanceApplicationInstance> findByMiddlewareInstanceIdAndApplicationInstanceId(Long middlewareDiscoveredInstanceId, Long applicationDiscoveredInstanceId);
}
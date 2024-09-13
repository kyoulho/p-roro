package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.MiddlewareInstance;
import io.playce.roro.jpa.entity.MiddlewareInstanceProtocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MiddlewareInstanceProtocolRepository extends JpaRepository<MiddlewareInstanceProtocol, Long>, JpaSpecificationExecutor<MiddlewareInstance> {
    Optional<MiddlewareInstanceProtocol> findByMiddlewareInstanceIdAndMiddlewareInstanceServicePort(Long middlewareInstanceId, int port);
}
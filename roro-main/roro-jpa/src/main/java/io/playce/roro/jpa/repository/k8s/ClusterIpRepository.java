package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.ClusterIp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClusterIpRepository extends JpaRepository<ClusterIp, Long> {
}

package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.ClusterScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterScanRepository extends JpaRepository<ClusterScan,Long >{
}

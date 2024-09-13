package io.playce.roro.jpa.repository.k8s;

import io.playce.roro.jpa.entity.k8s.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster,Long > {

    boolean existsByNameAndProjectId(String name, Long projectId);
}

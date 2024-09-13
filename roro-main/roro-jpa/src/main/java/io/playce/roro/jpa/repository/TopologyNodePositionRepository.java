package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.TopologyNodePosition;
import io.playce.roro.jpa.entity.pk.TopologyNodePositionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopologyNodePositionRepository extends JpaRepository<TopologyNodePosition, TopologyNodePositionId> {
}
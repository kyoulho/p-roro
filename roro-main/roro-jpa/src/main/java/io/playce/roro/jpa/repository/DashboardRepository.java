package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long>, JpaSpecificationExecutor<Dashboard> {

    Dashboard findByProjectIdAndUserId(Long projectId, Long userId);

}
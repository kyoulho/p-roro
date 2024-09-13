package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.DatabaseSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseSummaryRepository extends JpaRepository<DatabaseSummary, Long>, JpaSpecificationExecutor<DatabaseSummary> {

}

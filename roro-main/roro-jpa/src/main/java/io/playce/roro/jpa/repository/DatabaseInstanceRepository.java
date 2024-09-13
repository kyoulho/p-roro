package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.DatabaseInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseInstanceRepository extends JpaRepository<DatabaseInstance, Long>, JpaSpecificationExecutor<DatabaseInstance> {

    DatabaseInstance findByDatabaseInstanceId(long discoveredInstanceId);

    DatabaseInstance findByJdbcUrl(String jdbcUrl);
}

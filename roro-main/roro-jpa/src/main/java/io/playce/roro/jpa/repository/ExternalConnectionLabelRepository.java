package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.ExternalConnectionLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Jihyun Park
 * @version 1.0
 */
@Repository
public interface ExternalConnectionLabelRepository extends JpaRepository<ExternalConnectionLabel, Long>, JpaSpecificationExecutor<ExternalConnectionLabel> {

    Optional<ExternalConnectionLabel> findByProjectIdAndIp(Long projectId, String ip);
    void deleteExternalConnectionLabelByProjectIdAndIp(Long projectId, String ip);

}

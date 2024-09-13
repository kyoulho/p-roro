package io.playce.roro.mybatis.domain.k8s;

import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.k8s.ClusterResponse;
import io.playce.roro.common.dto.k8s.CommandProcessorRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterScanMapper {
    List<CommandProcessorRequest> selectByStatus(Domain1003 ... status);

}

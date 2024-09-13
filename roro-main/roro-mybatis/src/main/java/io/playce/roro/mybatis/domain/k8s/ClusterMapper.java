package io.playce.roro.mybatis.domain.k8s;

import io.playce.roro.common.dto.k8s.ClusterResponse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterMapper {
    List<ClusterResponse> findByProjectId(Long projectId);

    ClusterResponse findByK8sClusterId(Long k8sClusterId);
}


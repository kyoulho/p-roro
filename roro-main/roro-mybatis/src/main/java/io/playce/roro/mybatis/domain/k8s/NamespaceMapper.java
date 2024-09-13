package io.playce.roro.mybatis.domain.k8s;

import io.playce.roro.common.dto.k8s.NamespaceResponse;
import io.playce.roro.common.dto.k8s.TopologyNode;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NamespaceMapper {
    List<NamespaceResponse> selectNamespaces(@Param("projectId") Long projectId, @Param("k8sClusterId") Long k8sClusterId, @Param("clusterScanId") Long clusterScanId);

    List<TopologyNode> selectNamespaceNodes(@Param("projectId") Long projectId, @Param("k8sClusterId") Long k8sClusterId, @Param("clusterScanId") Long clusterScanId);
}

package io.playce.roro.mybatis.domain.k8s;

import io.playce.roro.common.dto.k8s.K8sServiceResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface K8sServiceMapper {
    List<K8sServiceResponse> selectServices(@Param("projectId") Long projectId, @Param("k8sClusterId") Long k8sClusterId, @Param("clusterScanId") Long clusterScanId);
}

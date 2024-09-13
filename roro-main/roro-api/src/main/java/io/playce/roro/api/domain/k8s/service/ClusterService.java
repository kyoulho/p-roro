package io.playce.roro.api.domain.k8s.service;


import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.ConfigReader;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.dto.k8s.ClusterRequest;
import io.playce.roro.common.dto.k8s.ClusterResponse;
import io.playce.roro.jpa.entity.k8s.Cluster;
import io.playce.roro.jpa.entity.ClusterScan;
import io.playce.roro.jpa.repository.ProjectMasterRepository;
import io.playce.roro.jpa.repository.k8s.ClusterRepository;
import io.playce.roro.mybatis.domain.k8s.ClusterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClusterService {
    private final ClusterRepository clusterRepository;
    private final ClusterMapper clusterMapper;
    private final ProjectMasterRepository projectMasterRepository;
    private final ClusterScanService clusterScanService;

    public List<ClusterResponse> getClusterList(Long projectId) {
        projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));
        return clusterMapper.findByProjectId(projectId);
    }

    public ClusterResponse getCluster(Long projectId, Long k8sClusterId) {
        projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));
        clusterRepository.findById(k8sClusterId)
                .orElseThrow(() -> new ResourceNotFoundException("k8sClusterId ID : " + k8sClusterId + " Not Found."));

        return clusterMapper.findByK8sClusterId(k8sClusterId);
    }

    public void createCluster(Long projectId, ClusterRequest clusterRequest) {
        projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));
        String clusterName = clusterRequest.getName();
        String config = clusterRequest.getConfig();

        validateClusterName(clusterName, projectId);
        validateConfig(config);

        Cluster cluster = new Cluster();
        cluster.setProjectId(projectId);
        cluster.setName(clusterName);
        cluster.setConfig(config);
        cluster.setRegisterDatetime(new Date());
        cluster.setRegisterUserId(WebUtil.getUserId());
        cluster.setModifyDatetime(new Date());
        cluster.setModifyUserId(WebUtil.getUserId());
        cluster.setRegisterUserLoginId(Objects.requireNonNull(WebUtil.getLoginUser()).getUsername());
        cluster.setModifyUserLoginId(Objects.requireNonNull(WebUtil.getLoginUser()).getUsername());
        cluster.setLastClusterScanId(0L);
        clusterRepository.save(cluster);

        ClusterScan clusterScan = clusterScanService.clusterScan(cluster.getK8sClusterId());
        cluster.setLastClusterScanId(clusterScan.getClusterScanId());
    }

    public void deleteCluster(Long projectId, Long k8sClusterId) {
        projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));
        clusterRepository.findById(k8sClusterId)
                .orElseThrow(() -> new ResourceNotFoundException("k8sClusterId ID : " + k8sClusterId + " Not Found."));
        clusterRepository.deleteById(k8sClusterId);
    }

    public void modifyCluster(Long projectId, Long k8sClusterId, ClusterRequest clusterRequest) {
        String clusterName = clusterRequest.getName();
        String config = clusterRequest.getConfig();
        validateConfig(config);

        projectMasterRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project ID : " + projectId + " Not Found."));
        Cluster cluster = clusterRepository.findById(k8sClusterId)
                .orElseThrow(() -> new ResourceNotFoundException("Cluster ID : " + k8sClusterId + " Not Found."));
        cluster.setName(clusterName);
        cluster.setConfig(config);
        cluster.setProjectId(projectId);
        cluster.setModifyDatetime(new Date());
        cluster.setModifyUserLoginId(Objects.requireNonNull(WebUtil.getLoginUser()).getUsername());

        ClusterScan clusterScan = clusterScanService.clusterScan(cluster.getK8sClusterId());
        cluster.setLastClusterScanId(clusterScan.getClusterScanId());
    }

    private void validateClusterName(String clusterName, Long projectId) {
        if (clusterRepository.existsByNameAndProjectId(clusterName, projectId)) {
            throw new RoRoApiException(ErrorCode.K8s_CLUSTER_NAME_DUPLICATED);
        }
    }

    private static void validateConfig(String config) {
        ConfigReader.readClusterConfigFromJson(config);
    }

}

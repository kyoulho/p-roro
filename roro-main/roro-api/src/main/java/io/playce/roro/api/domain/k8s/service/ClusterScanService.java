package io.playce.roro.api.domain.k8s.service;

import io.playce.roro.common.code.Domain1003;
import io.playce.roro.jpa.entity.ClusterScan;
import io.playce.roro.jpa.repository.k8s.ClusterScanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClusterScanService {

    private final ClusterScanRepository clusterScanRepository;

    public ClusterScan clusterScan(Long k8sClusterId) {
        ClusterScan clusterScan = new ClusterScan();
        clusterScan.setScanDatetime(new Date());
        clusterScan.setK8sClusterId(k8sClusterId);
        clusterScan.setScanStatusMessage(StringUtils.EMPTY);
        clusterScan.setScanStatus(Domain1003.REQ.name());
        clusterScan.setServerVersion(StringUtils.EMPTY);

        return clusterScanRepository.save(clusterScan);
    }

 }

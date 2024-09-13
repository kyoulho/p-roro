/*
 * Copyright 2023 The playce-roro Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * Dong-Heon Han    Jul 19, 2023		First Draft.
 */

package io.playce.roro.k8s.service;

import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.k8s.CommandProcessorRequest;
import io.playce.roro.jpa.entity.ClusterScan;
import io.playce.roro.jpa.entity.ScanOrigin;
import io.playce.roro.jpa.repository.k8s.ScanOriginRepository;
import io.playce.roro.jpa.repository.k8s.ClusterScanRepository;
import io.playce.roro.k8s.command.enums.COMMAND_KEY;
import io.playce.roro.k8s.common.exception.CancelException;
import io.playce.roro.mybatis.domain.k8s.ClusterScanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("k8s")
@RequiredArgsConstructor
@Transactional
public class ClusterScanService {
    private final ClusterScanMapper clusterScanMapper;
    private final ClusterScanRepository clusterScanRepository;
    private final ScanOriginRepository scanOriginRepository;

    public List<CommandProcessorRequest> getClusterScanByStatus(Domain1003 ... status) {
        return clusterScanMapper.selectByStatus(status);
    }

    public void setStatus(Domain1003 status, CommandProcessorRequest req) {
        ClusterScan clusterScan = clusterScanRepository.findById(req.getClusterScanId()).orElseThrow(() ->
                new RuntimeException("There is no cluster scan data.")
        );
        clusterScan.setScanStatus(status.name());
    }

    public void checkCancel(CommandProcessorRequest req) {
        ClusterScan clusterScan = clusterScanRepository.findById(req.getClusterScanId()).orElseThrow(() ->
                new RuntimeException("There is no cluster scan data.")
        );
        if(clusterScan.getScanStatus().equals(Domain1003.CNCL.name())) {
            throw new CancelException();
        }
    }

    public void saveOriginResult(Long clusterScanId, COMMAND_KEY commandKey, String result) {
        ScanOrigin scanOrigin = scanOriginRepository.findByClusterScanIdAndCommandKey(clusterScanId, commandKey.name()).orElse(new ScanOrigin());
        scanOrigin.setClusterScanId(clusterScanId);
        scanOrigin.setCommandKey(commandKey.name());
        scanOrigin.setOriginData(result);
        scanOriginRepository.save(scanOrigin);
    }
}
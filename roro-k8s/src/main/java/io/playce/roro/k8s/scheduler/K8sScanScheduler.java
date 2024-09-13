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

package io.playce.roro.k8s.scheduler;

import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.k8s.CommandProcessorRequest;
import io.playce.roro.k8s.config.K8sConfig;
import io.playce.roro.k8s.service.ClusterScanService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class K8sScanScheduler {
    private final BlockingQueue<CommandProcessorRequest> k8sQueue;
    private final K8sScanManager k8sScanManager;
    private final K8sConfig k8sConfig;

    private final ClusterScanService clusterScanService;

    @PostConstruct
    public void init() {
        List<CommandProcessorRequest> requests = clusterScanService.getClusterScanByStatus(Domain1003.PROC, Domain1003.PEND);
        requests.forEach(r -> clusterScanService.setStatus(Domain1003.FAIL, r));
    }

    @Scheduled(initialDelayString = "#{k8sConfig.scheduler.initDelay}", fixedDelayString = "#{k8sConfig.scheduler.interval}")
    public void start() {
        if(!k8sConfig.getScheduler().isEnable()) return;

        List<CommandProcessorRequest> requests = clusterScanService.getClusterScanByStatus(Domain1003.REQ, Domain1003.PEND);

        for(CommandProcessorRequest request: requests) {
            try {
                k8sQueue.put(request);
                clusterScanService.setStatus(Domain1003.PEND, request);
            } catch (InterruptedException | RuntimeException e) {
                log.error(e.getMessage());
                continue;
            }
            try {
                k8sScanManager.run();
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }
}
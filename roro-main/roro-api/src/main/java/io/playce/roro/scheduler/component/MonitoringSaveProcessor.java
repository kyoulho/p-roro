/*
 * Copyright 2022 The playce-roro-v3 Project.
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
 * Dong-Heon Han    May 10, 2022		First Draft.
 */

package io.playce.roro.scheduler.component;

import io.playce.roro.common.dto.monitoring.MonitoringSaveItem;
import io.playce.roro.jpa.entity.CpuMonitoring;
import io.playce.roro.jpa.entity.DiskMonitoring;
import io.playce.roro.jpa.entity.MemoryMonitoring;
import io.playce.roro.jpa.repository.CpuMonitoringRepository;
import io.playce.roro.jpa.repository.DiskMonitoringRepository;
import io.playce.roro.jpa.repository.MemoryMonitoringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonitoringSaveProcessor {
    private final BlockingQueue<MonitoringSaveItem> monitoringResultQueue;
    private final CpuMonitoringRepository cpuMonitoringRepository;
    private final MemoryMonitoringRepository memoryMonitoringRepository;
    private final DiskMonitoringRepository diskMonitoringRepository;

    @Async
    public void process() {
        try {
            MonitoringSaveItem item = monitoringResultQueue.take();
            Object obj = item.getItem();
            if(obj instanceof CpuMonitoring) {
                CpuMonitoring monitoring = (CpuMonitoring) obj;
                cpuMonitoringRepository.save(monitoring);
            } else if(obj instanceof MemoryMonitoring) {
                MemoryMonitoring monitoring = (MemoryMonitoring) obj;
                memoryMonitoringRepository.save(monitoring);
            } else if(obj instanceof DiskMonitoring) {
                DiskMonitoring monitoring = (DiskMonitoring) obj;
                diskMonitoringRepository.save(monitoring);
            }
            log.debug("{}", obj);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
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
 * Dong-Heon Han    May 07, 2022		First Draft.
 */

package io.playce.roro.scheduler.component.impl;

import io.playce.roro.common.dto.inventory.process.MonitoringQueueItem;
import io.playce.roro.common.dto.monitoring.MonitoringSaveItem;
import io.playce.roro.common.util.SummaryStat;
import io.playce.roro.jpa.entity.MemoryMonitoring;
import io.playce.roro.jpa.entity.pk.MemoryMonitoringId;
import io.playce.roro.scheduler.component.AbstractMonitoringStat;
import io.playce.roro.scheduler.component.MonitoringSaveProcessor;
import io.playce.roro.scheduler.service.impl.MonitoringManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component("MEMORY_STAT")
@Slf4j
public class MonitoringStatForMemory extends AbstractMonitoringStat {
    public MonitoringStatForMemory(BlockingQueue<MonitoringSaveItem> monitoringResultQueue, MonitoringSaveProcessor monitoringSaveProcessor) {
        super(monitoringResultQueue, monitoringSaveProcessor);
    }

    @Override
    public void processLine(MonitoringQueueItem item, String line) {
        super.process(MonitoringManager.STAT_FILE.MEMORY_STAT.name(), item, line, 0, 4, 2);
    }

    @Override
    protected MonitoringSaveItem getMonitoringSaveItem(SummaryStat stat, String[] data) {
        MemoryMonitoringId id = makeId(stat);
        MemoryMonitoring monitoring = makeData(id, stat);
        MonitoringSaveItem saveItem = new MonitoringSaveItem();
        saveItem.setItem(monitoring);
        return saveItem;
    }

    private MemoryMonitoring makeData(MemoryMonitoringId id, SummaryStat stat) {
        MemoryMonitoring monitoring = new MemoryMonitoring();
        monitoring.setId(id);
        monitoring.setSampleCount((int) stat.getCnt());
        monitoring.setMemoryAvg(stat.getAvg());
        monitoring.setMemoryMax(stat.getMax());
        monitoring.setMemoryUsageAvg(stat.getUsageAvg());
        monitoring.setMemoryUsageMax(stat.getUsageMax());
        return monitoring;
    }

    private MemoryMonitoringId makeId(SummaryStat stat) {
        MemoryMonitoringId id = new MemoryMonitoringId();
        id.setServerInventoryId(stat.getServerInventoryId());
        id.setMonitoringDatetime(new Date(stat.getWindowDate()));
        return id;
    }
}
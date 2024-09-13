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
import io.playce.roro.jpa.entity.CpuMonitoring;
import io.playce.roro.jpa.entity.pk.CpuMonitoringId;
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
@Component("CPU_STAT")
@Slf4j
public class MonitoringStatForCPU extends AbstractMonitoringStat {
    public MonitoringStatForCPU(BlockingQueue<MonitoringSaveItem> monitoringResultQueue, MonitoringSaveProcessor monitoringSaveProcessor) {
        super(monitoringResultQueue, monitoringSaveProcessor);
    }

    @Override
    public void processLine(MonitoringQueueItem item, String line) {
        super.process(MonitoringManager.STAT_FILE.CPU_STAT.name(), item, line, 0, 1);
    }

    @Override
    protected MonitoringSaveItem getMonitoringSaveItem(SummaryStat stat, String[] data) {
        CpuMonitoringId id = makeId(stat);
        CpuMonitoring monitoring = makeData(id, stat);
        MonitoringSaveItem saveItem = new MonitoringSaveItem();
        saveItem.setItem(monitoring);
        return saveItem;
    }

    private CpuMonitoring makeData(CpuMonitoringId id, SummaryStat stat) {
        CpuMonitoring monitoring = new CpuMonitoring();
        monitoring.setId(id);
        monitoring.setSampleCount((int) stat.getCnt());
        monitoring.setCpuAvg(stat.getAvg());
        monitoring.setCpuMax(stat.getMax());
        return monitoring;
    }

    private CpuMonitoringId makeId(SummaryStat stat) {
        CpuMonitoringId id = new CpuMonitoringId();
        id.setServerInventoryId(stat.getServerInventoryId());
        id.setMonitoringDatetime(new Date(stat.getWindowDate()));
        return id;
    }

}
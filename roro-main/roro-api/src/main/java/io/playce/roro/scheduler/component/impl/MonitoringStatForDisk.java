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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.playce.roro.common.dto.inventory.process.MonitoringQueueItem;
import io.playce.roro.common.dto.monitoring.MonitoringSaveItem;
import io.playce.roro.common.util.SummaryStat;
import io.playce.roro.jpa.entity.DiskMonitoring;
import io.playce.roro.jpa.entity.pk.DiskMonitoringId;
import io.playce.roro.scheduler.component.AbstractMonitoringStat;
import io.playce.roro.scheduler.component.MonitoringSaveProcessor;
import io.playce.roro.scheduler.service.impl.MonitoringManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
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
@Component("DISK_STAT")
@Slf4j
public class MonitoringStatForDisk extends AbstractMonitoringStat {
    public MonitoringStatForDisk(BlockingQueue<MonitoringSaveItem> monitoringResultQueue, MonitoringSaveProcessor monitoringSaveProcessor) {
        super(monitoringResultQueue, monitoringSaveProcessor);
    }

    @Override
    public void processLine(MonitoringQueueItem item, String line) {
        super.process(MonitoringManager.STAT_FILE.DISK_STAT.name(), item, line, 0, 7, 5);
    }

    @Override
    protected MonitoringSaveItem getMonitoringSaveItem(SummaryStat stat, String[] data) {
        DiskMonitoringId id = makeId(stat, data[3]);
        DiskMonitoring monitoring = makeData(id, stat);
        MonitoringSaveItem saveItem = new MonitoringSaveItem();
        saveItem.setItem(monitoring);
        return saveItem;
    }

    private DiskMonitoring makeData(DiskMonitoringId id, SummaryStat stat) {
        DiskMonitoring monitoring = new DiskMonitoring();
        monitoring.setId(id);
        monitoring.setSampleCount((int) stat.getCnt());
        monitoring.setDiskAvg(stat.getAvg());
        monitoring.setDiskMax(stat.getMax());
        monitoring.setDiskUsageAvg(stat.getUsageAvg());
        monitoring.setDiskUsageMax(stat.getUsageMax());
        return monitoring;
    }

    private DiskMonitoringId makeId(SummaryStat stat, String partition) {
        DiskMonitoringId id = new DiskMonitoringId();
        id.setServerInventoryId(stat.getServerInventoryId());
        id.setMonitoringDatetime(new Date(stat.getWindowDate()));
        id.setDeviceName(partition);
        return id;
    }

    @Override
    protected String makeMapKey(Long serverInventoryId, String[] data) {
/*
        ["date", "device", "type", "partition", "total", "used", "available", "utilization"]
        0         1         2       3            4        5       6            7
 */
        return super.makeMapKey(serverInventoryId, data) + File.separator + data[3];
    }
}
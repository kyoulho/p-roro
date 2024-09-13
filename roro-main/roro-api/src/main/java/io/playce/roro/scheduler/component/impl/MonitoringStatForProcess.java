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
import io.playce.roro.scheduler.component.AbstractMonitoringStat;
import io.playce.roro.scheduler.component.MonitoringSaveProcessor;
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
@Component("PROCESS_STAT")
public class MonitoringStatForProcess extends AbstractMonitoringStat {

    public MonitoringStatForProcess(BlockingQueue<MonitoringSaveItem> monitoringResultQueue, MonitoringSaveProcessor monitoringSaveProcessor) {
        super(monitoringResultQueue, monitoringSaveProcessor);
    }

    @Override
    public void processLine(MonitoringQueueItem item, String line) {

    }

    @Override
    protected MonitoringSaveItem getMonitoringSaveItem(SummaryStat stat, String[] data) {
        return null;
    }
}
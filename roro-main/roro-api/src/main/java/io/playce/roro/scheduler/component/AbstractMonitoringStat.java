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

package io.playce.roro.scheduler.component;

import io.playce.roro.api.common.util.DateTimeUtils;
import io.playce.roro.common.dto.inventory.process.MonitoringQueueItem;
import io.playce.roro.common.dto.monitoring.MonitoringSaveItem;
import io.playce.roro.common.util.SummaryStat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractMonitoringStat implements MonitoringStat {
    private final BlockingQueue<MonitoringSaveItem> monitoringResultQueue;
    private final MonitoringSaveProcessor monitoringSaveProcessor;
    private final Map<String, SummaryStat> summaryMap = new ConcurrentHashMap<>();
    protected final int windowSize = 5;
    private final Calendar calendar = Calendar.getInstance();

    @NotNull
    protected Date getWindowDate(Date date) {
        calendar.setTime(date);
        int minute = calendar.get(Calendar.MINUTE);
        int windowMinute = windowSize - minute % windowSize - 1;
        calendar.add(Calendar.MINUTE, windowMinute);
        return DateUtils.ceiling(calendar, Calendar.MINUTE).getTime();
    }

    protected SummaryStat getSummaryStat(Long serverInventoryId, String mapKey) {
        SummaryStat stat = summaryMap.get(mapKey);
        if(stat == null) {
            stat = putNewSummaryStat(serverInventoryId, mapKey);
        }
        return stat;
    }

    protected SummaryStat putNewSummaryStat(Long serverInventoryId, String mapKey) {
        SummaryStat stat = new SummaryStat();
        stat.setServerInventoryId(serverInventoryId);
        stat.setMapKey(mapKey);
        summaryMap.put(mapKey, stat);
        return stat;
    }

    protected Date getLineDate(String word) {
        return DateTimeUtils.convertToDate(word, new SimpleDateFormat(DateTimeUtils.DEFAULT_FILEPATH_FORMAT));
    }

    protected SummaryStat saveMonitoringStat(SummaryStat stat, long now, Long before, String[] data) {
        if(before != null && now != before) {
            MonitoringSaveItem saveItem = getMonitoringSaveItem(stat, data);
            try {
                monitoringResultQueue.put(saveItem);
                monitoringSaveProcessor.process();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
            stat = putNewSummaryStat(stat.getServerInventoryId(), stat.getMapKey());
        }
        return stat;
    }

    protected void process(String monitorName, MonitoringQueueItem item, String line, int ... dataIndex) {
        String[] data = line.split(",");
        Long serverInventoryId = item.getServerInventoryId();
        String mapKey = makeMapKey(serverInventoryId, data);
        SummaryStat stat = getSummaryStat(item.getServerInventoryId(), mapKey);

        Date date = getLineDate(data[dataIndex[0]]);
        Date windowDate = getWindowDate(date);

        long now = windowDate.getTime();
        Long before = stat.getWindowDate();
        stat = saveMonitoringStat(stat, now, before, data);
        stat.setWindowDate(now);

        int utilizationIndex = dataIndex[1];
        String utilizationStr = data[utilizationIndex].trim();
        if(utilizationStr.contains("%")) {
            utilizationStr = utilizationStr.replaceAll("%", StringUtils.EMPTY);
        }
        double utilization = StringUtils.isEmpty(utilizationStr) ? Double.NaN : Double.parseDouble(utilizationStr);
        stat.addValue(utilization);
        if(dataIndex.length == 3) {
            double usage = StringUtils.isEmpty(data[dataIndex[2]]) ? Double.NaN : Double.parseDouble(data[dataIndex[2]]);
            stat.addUsageValue(usage);
            log.trace("{}. {} -> {} usage: {}", monitorName, date, windowDate, usage);
        }

        log.trace("{}. {} -> {} value: {}", monitorName, date, windowDate, utilization);
    }

    /*
    통계 기준 키.
     */
    protected String makeMapKey(Long serverInventoryId, String[] data) {
        return String.valueOf(serverInventoryId);
    }

    protected abstract MonitoringSaveItem getMonitoringSaveItem(SummaryStat stat, String[] data);
}
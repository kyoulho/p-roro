/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * Dong-Heon Han    Dec 09, 2021		First Draft.
 */

package io.playce.roro.scheduler;

import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.common.dto.inventory.process.MonitoringQueueItem;
import io.playce.roro.common.setting.SettingsHandler;
import io.playce.roro.scheduler.config.ScheduleConfig;
import io.playce.roro.scheduler.service.Manager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import static io.playce.roro.common.setting.SettingsConstants.ENABLE_MONITORING_SCHEDULE;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringScheduler {
    private final BlockingQueue<MonitoringQueueItem> queue;
    private final ScheduleConfig scheduleConfig;
    private final Manager monitoringManager;
    private final ServerService serverService;
    private final TriggerContext context = new SimpleTriggerContext();
    private final Map<Long, Date> runDateMap = new ConcurrentHashMap<>();

    // @Value("${enable.monitoring.schedule: true}")
    // private boolean jobMonitoring;

    @Scheduled(cron = "#{scheduleConfig.monitoring.cron}")
    public void startGlobalMonitoringScheduler() {
        if (!Boolean.parseBoolean(SettingsHandler.getSettingsValue(ENABLE_MONITORING_SCHEDULE))) {
            return;
        }

        runScheduleJob();
    }

    private void runScheduleJob() {
        List<MonitoringQueueItem> items = serverService.getMonitoringServers(null);

        items.forEach(item -> {
            Date now = new Date();
            Long serverInventoryId = item.getServerInventoryId();

            Date start = item.getMonitoringStartDatetime();
            Date end = item.getMonitoringEndDatetime();
            start = start == null ? now : start;
            end = end == null ? now : end;

            if (start.getTime() <= now.getTime() && now.getTime() <= end.getTime()) {
                log.debug("global monitoring server id:{} run:{}", serverInventoryId, now);
                run(item);
            }
        });
    }

    private void run(MonitoringQueueItem item) {
        try {
            queue.put(item);
            monitoringManager.run();

            log.debug("enqueue id: {}, cycle: {}", item.getServerInventoryId(), item.getMonitoringCycle());
        } catch (InterruptedException e) {
            log.error("{} enqueue error: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelayString = "#{scheduleConfig.monitoring.defaultPeriod * 500}")
    // scheduler.schedule.monitoring.default-period 설정값 60(기본값) * 500 = 30000 30초 주기로 실행.
    public void runIndividualServerScheduler() {
        if (!Boolean.parseBoolean(SettingsHandler.getSettingsValue(ENABLE_MONITORING_SCHEDULE))) {
            return; // enable.monitoring.schedule설정값이 false이면 실행안함.
        }

        Date now = new Date();
        long nowLong = now.getTime();
        long beforeLong = DateUtils.addSeconds(now, scheduleConfig.getMonitoring().getDefaultPeriod() / 2 * -1).getTime();
        // scheduler.schedule.monitoring.default-period 설정값 60(기본값) / 2 * -1 = -30 - 30초 전부터 현재까지 해당 서버 조회.
        List<MonitoringQueueItem> items = serverService.getMonitoringServers(now);

        for (MonitoringQueueItem item : items) {//모니터링 설정회된 서버별.
            Long serverInventoryId = item.getServerInventoryId();
            CronTrigger trigger = getCronTrigger(item);
            if (trigger == null)
                continue;

            Date nextDate = trigger.nextExecutionTime(context);
            Date runDate = getRunDate(serverInventoryId, nextDate); //다음 실행시간.

            long runDateLong = runDate.getTime();
            if (beforeLong < runDateLong && runDateLong <= nowLong) {
                log.debug("individual monitoring server id: {} run date:{}", serverInventoryId, runDate);
                run(item); //모니터링 프로세스 실행.
                runDateMap.put(serverInventoryId, nextDate);
            } else {
                if (runDate.compareTo(now) < 0) {
                    runDateMap.put(serverInventoryId, nextDate);
                }
            }
        }
    }

    private CronTrigger getCronTrigger(MonitoringQueueItem item) {
        CronTrigger trigger;
        try {
            trigger = new CronTrigger(item.getMonitoringCycle());
        } catch (IllegalArgumentException e) {
            log.error("Check the cron expression on serverInventoryId: {}, cron: {}", item.getServerInventoryId(), item.getMonitoringCycle());
            return null;
        }
        return trigger;
    }

    private Date getRunDate(Long serverInventoryId, Date nextDate) {
        if (!runDateMap.containsKey(serverInventoryId)) {
            runDateMap.put(serverInventoryId, nextDate);
        }
        return runDateMap.get(serverInventoryId);
    }
}
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
 * SangCheon Park   May 23, 2022		    First Draft.
 */
package io.playce.roro.scheduler;

import io.playce.roro.api.domain.assessment.service.AssessmentService;
import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.common.dto.inventory.server.ScheduledScanServer;
import io.playce.roro.common.setting.SettingsHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.playce.roro.common.setting.SettingsConstants.ENABLE_SCHEDULED_SCAN;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledScanScheduler {

    private final ServerService serverService;
    private final AssessmentService assessmentService;

    // @Value("${enable.scheduled.scan: true}")
    // private boolean enableScheduledScan;

    @Scheduled(cron = "#{scheduleConfig.scheduledScan.cron}")
    public void startScanScheduler() {
        if (!Boolean.parseBoolean(SettingsHandler.getSettingsValue(ENABLE_SCHEDULED_SCAN))) {
            return;
        }

        runScheduleJob();
    }

    private void runScheduleJob() {
        log.debug("Scheduled scan process will be start.");

        List<ScheduledScanServer> items = serverService.getScheduledScanServers();

        log.debug("[{}] servers scan will be requested.", items.size());

        items.forEach(item -> {
            try {
                assessmentService.createAssessment(item.getProjectId(), item.getServerInventoryId());
            } catch (Exception e) {
                log.error("Unhandled exception occurred while request a server scan.", e);
            }
        });
    }
}
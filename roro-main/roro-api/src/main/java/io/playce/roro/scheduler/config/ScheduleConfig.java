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
 * Dong-Heon Han    Dec 15, 2021		First Draft.
 */

package io.playce.roro.scheduler.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@EnableScheduling
@EnableAsync
@ConfigurationProperties(prefix = "scheduler.schedule")
@Setter
@Getter
public class ScheduleConfig {
    private ScheduleItem prerequisite;
    private ScheduleItem assessment;
    private ScheduleItem migration;
    private ScheduleCronItem monitoring;
    private ScheduledScanItem scheduledScan;

    @Setter
    @Getter
    public static class ScheduleItem {
        private int init;
        private int delay;
    }

    @Setter
    @Getter
    public static class ScheduleCronItem {
        private String cron;
        private Integer window;
        private Integer defaultPeriod;
        private Integer scriptLifeHours;
        private String defaultDir;
    }

    @Setter
    @Getter
    public static class ScheduledScanItem {
        private String cron;
    }
}
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

package io.playce.roro.scheduler.config;

import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.dto.inventory.process.MigrationProgressQueueItem;
import io.playce.roro.common.dto.inventory.process.MonitoringQueueItem;
import io.playce.roro.common.dto.monitoring.MonitoringSaveItem;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Configuration
@ConfigurationProperties(prefix = "worker")
@Setter
@Getter
public class BlockingQueueConfig implements SchedulingConfigurer {
    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity;
    private String threadNamePrefix;
    private int migCorePoolSize;
    private int migMaxPoolSize;
    private int migQueueCapacity;
    private String migThreadNamePrefix;
    private int hsCorePoolSize;
    private int hsMaxPoolSize;
    private int hsQueueCapacity;
    private String hsThreadNamePrefix;


    @Bean
    public BlockingQueue<InventoryProcessQueueItem> prerequisiteQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public BlockingQueue<InventoryProcessQueueItem> assessmentQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public BlockingQueue<InventoryProcessQueueItem> migrationQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public BlockingQueue<MigrationProgressQueueItem> migrationProgressQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public BlockingQueue<MonitoringQueueItem> monitoringQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public BlockingQueue<MonitoringSaveItem> monitoringResultQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor migrationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(migCorePoolSize);
        executor.setMaxPoolSize(migMaxPoolSize);
        executor.setQueueCapacity(migQueueCapacity);
        executor.setThreadNamePrefix(migThreadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor hostScanTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(hsCorePoolSize);
        executor.setMaxPoolSize(hsMaxPoolSize);
        executor.setQueueCapacity(hsQueueCapacity);
        executor.setThreadNamePrefix(hsThreadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor monitoringTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("mo-");
        executor.initialize();
        return executor;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler= new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(corePoolSize);
        scheduler.setThreadNamePrefix("st-");
        scheduler.initialize();

        taskRegistrar.setTaskScheduler(scheduler);
    }
}
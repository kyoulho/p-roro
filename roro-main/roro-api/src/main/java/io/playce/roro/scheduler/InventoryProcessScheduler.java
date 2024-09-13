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

import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.config.InventoryProcessCancelProcessor;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.scheduler.component.MigrationProgressMessageProcessor;
import io.playce.roro.scheduler.service.InventoryProcessManager;
import io.playce.roro.scheduler.service.Manager;
import io.playce.roro.scheduler.service.impl.MigrationSchedulerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

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
public class InventoryProcessScheduler {
    private final InventoryProcessService inventoryProcessService;
    private final InventoryProcessManager inventoryProcessManager;

    @Value("${enable.prerequisite.schedule: true}")
    private boolean jobPrerequisite;
    private final BlockingQueue<InventoryProcessQueueItem> prerequisiteQueue;
    private final Manager prerequisiteSchedulerManager;

    @Value("${enable.assessment.schedule: true}")
    private boolean jobAssessment;
    private final BlockingQueue<InventoryProcessQueueItem> assessmentQueue;
    private final Manager assessmentSchedulerManager;

    @Value("${enable.migration.schedule: true}")
    private boolean jobMigration;
    private final BlockingQueue<InventoryProcessQueueItem> migrationQueue;
    private final Manager migrationSchedulerManager;

    private final MigrationProgressMessageProcessor migrationProgressMessageProcessor;

    private final InventoryProcessCancelProcessor inventoryProcessCancelProcessor;

    @PostConstruct
    public void init() throws InterruptedException {
        // PROC -> FAIL
        inventoryProcessService.changeState(Domain1003.PROC, Domain1003.FAIL);

        // PEND -> ENQUEUE
        if(jobPrerequisite) {
            initRunScheduleJob(Domain1002.PREQ, prerequisiteQueue, prerequisiteSchedulerManager);
        }
        if(jobAssessment) {
            initRunScheduleJob(Domain1002.SCAN, assessmentQueue, assessmentSchedulerManager);
        }
        if(jobMigration) {
            initRunScheduleJob(Domain1002.MIG, migrationQueue, migrationSchedulerManager);
        }
    }

    private void initRunScheduleJob(Domain1002 domain1002, BlockingQueue<InventoryProcessQueueItem> queue, Manager manager) throws InterruptedException {
        List<InventoryProcessQueueItem> pendingItemList = getInventoryProcessQueueItems(Domain1003.PEND, domain1002);
        for(InventoryProcessQueueItem item: pendingItemList) {
            enqueueAndRun(queue, manager, item);
        }
        if(!pendingItemList.isEmpty()) {
            log.info("enqueue(state PEND) -> {} queue, size: {}", domain1002.desc(), pendingItemList.size());
        }
    }

    @NotNull
    private List<InventoryProcessQueueItem> getInventoryProcessQueueItems(Domain1003 domain1003, Domain1002 domain1002) {
        return inventoryProcessService.getInventoryProcessByCode(domain1003, domain1002);
    }

    @Scheduled(initialDelayString = "#{scheduleConfig.prerequisite.init}", fixedDelayString = "#{scheduleConfig.prerequisite.delay}")
    public void requestPrerequisiteInventoryProcess() {
        if(!jobPrerequisite) return;

        runScheduleJob(Domain1002.PREQ, prerequisiteQueue, prerequisiteSchedulerManager);
    }

    @Scheduled(initialDelayString = "#{scheduleConfig.assessment.init}", fixedDelayString = "#{scheduleConfig.assessment.delay}")
    public void requestAssessmentInventoryProcess() {
        if(!jobAssessment) return;

        runScheduleJob(Domain1002.SCAN, assessmentQueue, assessmentSchedulerManager);
    }

    @Scheduled(initialDelayString = "#{scheduleConfig.migration.init}", fixedDelayString = "#{scheduleConfig.migration.delay}")
    public void requestMigrationInventoryProcess() {
        if(!jobMigration) return;

        runScheduleJob(Domain1002.MIG, migrationQueue, migrationSchedulerManager);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000 * 60 * 30)
    public void cleanJob() {
        inventoryProcessCancelProcessor.removeJobs();
    }

    private void runScheduleJob(Domain1002 domain1002, BlockingQueue<InventoryProcessQueueItem> queue, Manager manager) {
        List<InventoryProcessQueueItem> itemList = getInventoryProcessQueueItems(Domain1003.REQ, domain1002);
        if(itemList.isEmpty()) return;

        log.info("req list, size: {}", itemList.size());
        inventoryProcessService.changeState(Domain1003.REQ, Domain1003.PEND, domain1002);
        for(InventoryProcessQueueItem item: itemList) {
            // pending 상태의 websocket message를 전송한다.
            inventoryProcessManager.sendNotification(item, null, Domain1003.PEND);
            try {
                enqueueAndRun(queue, manager, item);

                if(manager instanceof MigrationSchedulerManager) {
                    migrationProgressMessageProcessor.process();
                }
            } catch (InterruptedException e) {
                log.error("{} enqueue error: {}", domain1002.desc(), e.getMessage());
            }
        }
        log.info("enqueue request: {}, size: {}", domain1002.desc(), itemList.size());
    }

    private void enqueueAndRun(BlockingQueue<InventoryProcessQueueItem> queue, Manager manager, InventoryProcessQueueItem item) throws InterruptedException {
        queue.put(item);
        Future<Void> future = manager.run();
        String key = Domain1002.valueOf(item.getInventoryProcessTypeCode()).executeKey(item.getInventoryProcessId());
        inventoryProcessCancelProcessor.addJob(key, future);
    }
}
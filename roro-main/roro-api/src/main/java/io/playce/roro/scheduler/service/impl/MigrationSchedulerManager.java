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
 * Dong-Heon Han    Dec 11, 2021		First Draft.
 */

package io.playce.roro.scheduler.service.impl;

import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.scheduler.component.MigrationProcess;
import io.playce.roro.scheduler.service.InventoryProcessManager;
import io.playce.roro.scheduler.service.Manager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class MigrationSchedulerManager implements Manager {
    private final BlockingQueue<InventoryProcessQueueItem> migrationQueue;
    private final InventoryProcessManager inventoryProcessManager;
    private final MigrationProcess migrationProcess;

    @Async("migrationTaskExecutor")
    public Future<Void> run() throws InterruptedException {
        InventoryProcessQueueItem item = null;
        try {
            item = migrationQueue.take();
            log.debug("Step 1 ~ dequeue, id: {}, queue size: {}", item.getInventoryProcessId(), migrationQueue.size());
            if (inventoryProcessManager.step2(item))
                return null;

            inventoryProcessManager.step3(item);
            Domain1003 domain1003 = step4(item);
            inventoryProcessManager.step5(item, domain1003);
        } catch (InterruptedException e) {
            log.error("Migration Thread interrupted.. {}", item);
            throw e;
        }
        return null;
    }

    @Transactional
    public Domain1003 step4(InventoryProcessQueueItem item) throws InterruptedException {
        Domain1003 resultState = Domain1003.FAIL;

        if (InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
            InventoryProcessCancelInfo.removeCancelRequest(item.getInventoryProcessId());
            return Domain1003.CNCL;
        }

        resultState = migrationProcess.migration(item, resultState);

        if (InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
            InventoryProcessCancelInfo.removeCancelRequest(item.getInventoryProcessId());
            return Domain1003.CNCL;
        }

        return resultState;
    }
}
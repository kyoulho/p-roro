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
 * Dong-Heon Han    Mar 21, 2022		First Draft.
 */

package io.playce.roro.scheduler.component;

import io.playce.roro.common.dto.inventory.process.MigrationProgressQueueItem;
import io.playce.roro.jpa.entity.InventoryMigrationProcess;
import io.playce.roro.jpa.repository.InventoryMigrationProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.BlockingQueue;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationProgressMessageProcessor {
    private final BlockingQueue<MigrationProgressQueueItem> migrationProgressQueue;
    private final InventoryMigrationProcessRepository inventoryMigrationProcessRepository;

    @Async
    public void process() {
        while (true) {
            try {
                MigrationProgressQueueItem item = migrationProgressQueue.take();
                //log.debug("recieved progress: {}", item);

                InventoryMigrationProcess inventoryMigrationProcess = inventoryMigrationProcessRepository.findById(item.getInventoryProcessId()).orElse(null);
                saveMigrationProcess(item, inventoryMigrationProcess);
            } catch (InterruptedException e) {
                log.debug("{}", e.getMessage(), e);
            }
        }
    }

    @Transactional
    public void saveMigrationProcess(MigrationProgressQueueItem item, InventoryMigrationProcess inventoryMigrationProcess) {
        if (inventoryMigrationProcess != null) {
            inventoryMigrationProcess.setAvailabilityZone(item.getAvailabilityZone());
            inventoryMigrationProcess.setVpcName(item.getVpcName());
            inventoryMigrationProcess.setSubnetName(item.getSubnetName());
            inventoryMigrationProcess.setImageId(item.getImageId());
            inventoryMigrationProcess.setImageName(item.getImageName());
            inventoryMigrationProcess.setInstanceId(item.getInstanceId());
            inventoryMigrationProcess.setInstanceName(item.getInstanceName());
            inventoryMigrationProcess.setPublicIp(item.getPublicIp());
            inventoryMigrationProcess.setPrivateIp(item.getPrivateIp());
            inventoryMigrationProcess.setInstanceLaunchTime(item.getInstanceLaunchTime());
            inventoryMigrationProcess.setBlockDevices(item.getBlockDevices());
            inventoryMigrationProcess.setSecurityGroupNames(item.getSecurityGroupNames());
            inventoryMigrationProcess.setElapsedTime(item.getElapsedTime());
            inventoryMigrationProcess.setInternalStatus(item.getInternalStatus());
            if (inventoryMigrationProcess.getProgress() == null) {
                inventoryMigrationProcess.setProgress(item.getProgress());
            } else {
                if (item.getProgress() != null && inventoryMigrationProcess.getProgress() < item.getProgress()) {
                    inventoryMigrationProcess.setProgress(item.getProgress());
                }
            }

            if (Double.isNaN(inventoryMigrationProcess.getProgress())) {
                inventoryMigrationProcess.setProgress(0.0);
            }
            inventoryMigrationProcessRepository.save(inventoryMigrationProcess);
            //log.debug("update progress: {}", item);
        }
    }
}
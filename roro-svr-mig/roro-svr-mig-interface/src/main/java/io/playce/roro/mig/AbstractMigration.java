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
 * SangCheon Park   Mar 10, 2022		    First Draft.
 */
package io.playce.roro.mig;

import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.dto.inventory.process.MigrationProgressQueueItem;
import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.common.dto.migration.enums.StatusType;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.BlockingQueue;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public abstract class AbstractMigration implements Migration {

    protected MigrationProcessDto migration;
    protected BlockingQueue<MigrationProgressQueueItem> migrationProgressQueue;
    protected TargetHost targetHost;
    protected String workDir;
    protected String logDir;
    protected long lastLogTime;

    protected abstract MigrationProcessDto migration() throws Exception;

    protected abstract void init();

    protected abstract void setResourceNames();

    protected abstract void cancel();

    @Override
    public MigrationProcessDto migration(MigrationProcessDto migrationProcessDto, BlockingQueue<MigrationProgressQueueItem> migrationProgressQueue) throws Exception {
        init(migrationProcessDto, migrationProgressQueue);
        migrationProcessDto.addElapsedTime();

        try {
            migration = migration();
        } catch (Exception e) {
            if (e instanceof InterruptedException || InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                cancel();
            }

            throw e;
        }

        return migration;
    }

    private void init(MigrationProcessDto migration, BlockingQueue<MigrationProgressQueueItem> migrationProgressQueue) {
        this.migration = migration;
        this.migrationProgressQueue = migrationProgressQueue;
        this.targetHost = migration.getTargetHost();
        this.workDir = MigrationManager.getWorkDir() + File.separator + migration.getInventoryProcessId();
        this.logDir = this.workDir + File.separator + "logs";
        new File(this.logDir).mkdirs();

        init();
    }

    protected void updateStatus(StatusType status) {
        try {
            migration.setInternalStatus(status);

            MigrationProgressQueueItem item = MigrationProgressQueueItem.builder()
                    .inventoryProcessId(migration.getInventoryProcessId())
                    .region(migration.getRegion())
                    .availabilityZone(migration.getAvailabilityZone())
                    .vpcName(migration.getVpcName())
                    .subnetName(migration.getSubnetName())
                    .imageId(migration.getImageId())
                    .imageName(migration.getImageName())
                    .instanceId(migration.getInstanceId())
                    .instanceName(migration.getInstanceName())
                    .publicIp(migration.getPublicIp())
                    .privateIp(migration.getPrivateIp())
                    .instanceLaunchTime(migration.getInstanceLaunchTime())
                    .elapsedTime(migration.getElapsedTime())
                    .internalStatus(status.getDescription())
                    .progress(MigrationManager.getProgress(migration))
                    .build();

            if (migration.getBlockDevices() != null) {
                item.setBlockDevices(String.join(",", migration.getBlockDevices()));
            }

            if (migration.getSecurityGroupNames() != null) {
                item.setSecurityGroupNames(String.join(",", migration.getSecurityGroupNames()));
            }

            migrationProgressQueue.put(item);
        } catch (InterruptedException e) {
            log.warn("Unhandled exception occurred while put MigrationProgressQueueItem.", e);
        }
    }

    /*protected void cancel() {
        DefaultExecutor executor = null;
        PumpStreamHandler streamHandler = null;
        CommandLine cl = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            executor = new DefaultExecutor();
            streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("sh"),
                    MigrationManager.getCancelMigrationFile().getAbsolutePath(),
                    workDir,
                    targetHost.getIpAddress());

            log.debug("Migration will be cancel using [{}]", cl.toString());

            int exitCode = executor.execute(cl);

            if (exitCode == 0) {
                log.debug("Migration({}) has been cancelled.", migration.getInventoryProcessId());
            } else {
                log.debug("Migration({}) cancel failed.", migration.getInventoryProcessId());
            }
        } catch (Exception e) {
            log.error("Migration({}) cancel failed.", migration.getInventoryProcessId(), e);
        }
    }*/
}
//end of AbstractMigration.java
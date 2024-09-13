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
 * SangCheon Park   Mar 11, 2022		    First Draft.
 */
package io.playce.roro.mig.gcp;

import com.google.api.services.compute.model.AttachedDisk;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.Operation;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.common.dto.migration.enums.StatusType;
import io.playce.roro.common.exception.CancelException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.mig.AbstractRehostMigration;
import io.playce.roro.mig.MigrationManager;
import io.playce.roro.mig.gcp.compute.ComputeClient;
import io.playce.roro.mig.gcp.enums.ResourceType;
import io.playce.roro.mig.gcp.model.GCPVolume;
import io.playce.roro.mig.gcp.model.network.FirewallDetail;
import io.playce.roro.mig.gcp.model.network.NetworkDetail;
import io.playce.roro.mig.gcp.model.network.SubnetDetail;
import io.playce.roro.mig.gcp.storage.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Component("GCPRehostMigration")
@Scope("prototype")
public class GCPRehostMigration extends AbstractRehostMigration {

    private ComputeClient computeClient;
    private StorageClient storageClient;
    private String bucketName;
    private String folderName;
    private final String DEFAULT_ZONE = "a";
    private final String RAW_FILE = "disk.raw";
    private final String BOOT_TAR_GZ = "bootable.tar.gz";
    private final String DATA_TAR_GZ = "data%s.tar.gz";
    private List<BucketUpload> uploadThreadList = new ArrayList<>();
    private Map<Long, GCPVolume> gcpVolumeMap = new HashMap<>();

    private String vpcName = null;
    private String subnetName = null;

    @Override
    public MigrationProcessDto migration() throws Exception {
        log.debug("GCPRehostMigration.migration() invoked.");

        try {
            String keyString = IOUtils.toString(new File(migration.getCredential().getKeyFilePath()).toURI(), StandardCharsets.UTF_8);
            computeClient = new ComputeClient(migration.getGcpProjectId(), keyString);
            storageClient = new StorageClient(migration.getGcpProjectId(), keyString, migration.getRegion());

            this.bucketName = MigrationManager.getBucketName();
            this.folderName = Long.toString(migration.getInventoryProcessId());

            setResourceNames();

            /**
             * PRE-migration
             **/
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            preMigrate();

            /**
             *  Step 1. Compress raw data
             **/
            updateStatus(StatusType.COMPRESSING);
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            compress();

            /**
             *  Step 2. Upload to Bucket
             **/
            updateStatus(StatusType.UPLOAD_TO_STORAGE);
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            upload();

            /**
             *  Step 4. (Optional) delete raw files
             **/
            if (MigrationManager.getDirectoryRemove()) {
                deleteRawFiles();
            }

            /**
             *  Step 5. Create DISK Image
             **/
            updateStatus(StatusType.CREATING_DISK_IMAGE);
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createDiskImage();
            statusCheck();

            /**
             *  Step 6. (Optional) Creating DISK
             *
             *  생략가능
             **/
            updateStatus(StatusType.CREATING_DISK);
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createDisks();
            statusCheck();

            /**
             *  Step 7. Create Instance
             **/
            updateStatus(StatusType.CREATING_INSTANCE);
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createInstance();

            statusCheckInstance();

            /**
             *  Step 8. (Optional)Delete Bucket folder
             **/
            if (MigrationManager.getBucketRemove()) {
                deleteFolder();
            }

            updateStatus(StatusType.CREATING_MACHINE_IMAGE);
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createImage();

            updateStatus(StatusType.COMPLETED);
        } catch (Exception e) {
            // 취소 여부와 관계없이 해당 스텝에서 오류가 발생하는 경우 VM Import 작업을 취소한다. (cancel()로 옮겨가지 않는 이유)
            if (migration.getInternalStatus().equals(StatusType.CREATING_DISK_IMAGE) ||
                    migration.getInternalStatus().equals(StatusType.CREATING_DISK)) {

                for (Map.Entry<Long, GCPVolume> entry : gcpVolumeMap.entrySet()) {
                    GCPVolume gcpVolume = entry.getValue();
                    try {
                        if (gcpVolume.getOperation() != null) {
                            computeClient.cancelOperationTask(
                                    gcpVolume.getOperation().getId().toString(),
                                    gcpVolume.getResourceType(),
                                    gcpVolume.getLocation());
                        }
                    } catch (Exception ex) {
                        log.error("Unhandled exception occurred while execute cancel conversion task.", ex);
                    }
                }
            }

            throw e;
        }

        return migration;
    }

    @Override
    public void setResourceNames() {
        try {
            NetworkDetail networkDetail = computeClient.getNetwork(migration.getVpcId());
            if (networkDetail != null) {
                vpcName = networkDetail.getNetworkName();
            }
        } catch (Exception e) {
            log.warn("Unhandled exception occurred while get GCP network name.", e);
        }

        try {
            SubnetDetail subnetDetail = computeClient.getSubnet(migration.getRegion(), migration.getSubnetId());
            if (subnetDetail != null) {
                subnetName = subnetDetail.getName();
            }
        } catch (Exception e) {
            log.warn("Unhandled exception occurred while get GCP subnet name.", e);
        }

        migration.setVpcName(vpcName);
        migration.setSubnetName(subnetName);
    }

    @Override
    protected void cancel() {
        if (migration.getInternalStatus().equals(StatusType.CREATE_RAW_FILES)) {
            try {
                DefaultExecutor executor = new DefaultExecutor();

                CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                        CommandUtil.findCommand("sh"),
                        MigrationManager.getCancelMigrationFile().getAbsolutePath(),
                        workDir,
                        targetHost.getIpAddress());

                log.debug("Execute shell script for GCP rehost migration process kill : [{}]", cl);

                int exitCode = executor.execute(cl);

                if (exitCode == 0) {
                    log.debug("GCP rehost migration({}) has been cancelled.", migration.getInventoryProcessId());
                } else {
                    log.debug("GCP rehost migration({}) cancel failed.", migration.getInventoryProcessId());
                }
            } catch (Exception e) {
                log.error("Unhandled exception occurred while execute cancel_migration.sh.", e);
            }
        }

        if (migration.getInternalStatus().equals(StatusType.COMPRESSING)) {
            for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
                GCPVolume gcpVolume = gcpVolumeMap.get(volume.getVolumeId());
                try {
                    DefaultExecutor executor = new DefaultExecutor();

                    CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                            CommandUtil.findCommand("sh"),
                            MigrationManager.getCancelMigrationFile().getAbsolutePath(),
                            String.format("tar -czf %s", gcpVolume.getTarGz().getName()));

                    log.debug("Execute shell script for GCP rehost migration process kill : [{}]", cl);

                    int exitCode = executor.execute(cl);

                    if (exitCode == 0) {
                        log.debug("GCP rehost migration({}) has been cancelled.", migration.getInventoryProcessId());
                    } else {
                        log.debug("GCP rehost migration({}) cancel failed.", migration.getInventoryProcessId());
                    }
                } catch (Exception e) {
                    log.error("Unhandled exception occurred while execute cancel_migration.sh.", e);
                } finally {
                    if (gcpVolume.getTarGz().delete()) {
                        log.debug("Delete compress raw file : [{}]", gcpVolume.getTarGz().getAbsoluteFile());
                    }

                    File source = new File(volume.getRawFileName());
                    if (source.exists()) {
                        String targetFileName = source.getAbsoluteFile().getParentFile().getPath() + File.separator + RAW_FILE;
                        try {
                            rename(targetFileName, volume.getRawFileName());
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }
        }

        if (migration.getInternalStatus().equals(StatusType.UPLOAD_TO_STORAGE)) {
            for (BucketUpload uploadThread : uploadThreadList) {
                if (uploadThread.isAlive()) {
                    try {
                        uploadThread.interrupt();
                    } catch (Exception e) {
                        log.error("Unhandled exception occurred while interrupt upload thread.", e);
                    }
                }
            }
        }
    }

    @Override
    public void upload() throws Exception {
        // Storage Bucket 생성
        storageClient.createBucket(bucketName, migration.getInventoryProcessId().toString());

        for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
            if (gcpVolumeMap.get(volume.getMigrationVolumeId()) != null) {
                BucketUpload uploadThread = new BucketUpload(gcpVolumeMap.get(volume.getMigrationVolumeId()));
                uploadThreadList.add(uploadThread);
                uploadThread.start();
            }
        }

        while (true) {
            int cnt = 0;
            for (BucketUpload uploadThread : uploadThreadList) {
                if (uploadThread.isError()) {
                    throw new RuntimeException(uploadThread.getException());
                }

                if (!uploadThread.isAlive() || uploadThread.isDone()) {
                    cnt++;
                }
            }

            if (cnt == uploadThreadList.size()) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                // Nothing to do
            }
        }
    }

    @Override
    protected void deleteFolder() throws Exception {
        storageClient.deleteFolder(bucketName, folderName);
    }

    @Override
    public void statusCheck() throws Exception {
        int cnt = 0;
        while (true) {
            cnt = 0;
            Operation response = null;
            for (GCPVolume gcpVolume : gcpVolumeMap.values()) {
                try {
                    if (gcpVolume.getResourceType().equals(ResourceType.GLOBAL)) {
                        response = computeClient.getOperationState(
                                gcpVolume.getOperation().getId().toString(), ResourceType.GLOBAL, null);
                    } else if (gcpVolume.getResourceType().equals(ResourceType.ZONAL)) {
                        response = computeClient.getOperationState(
                                gcpVolume.getOperation().getId().toString(), ResourceType.ZONAL, migration.getAvailabilityZone());
                    }

                    gcpVolume.setOperation(response);

                    if (response.getStatus().equals("PENDING") || response.getStatus().equals("RUNNING")) {
                        continue;
                    } else if (response.getStatus().equals("DONE") && response.getProgress() == 100) {
                        cnt++;
                    }

                    if (response.getError() != null) {
                        throw new RoRoException("Operation failed with error : " + response.getError().getErrors().get(0).getMessage());
                    }

                } catch (Exception e) {
                    throw new RoRoException("Unhandled exception occurred while status check failed. ", e);
                }
            }

            if (cnt == migration.getVolumes().size()) {
                break;
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                //Ignore
            }
        }

        // 디스크 생성 완료
        if (migration.getInternalStatus().equals(StatusType.CREATING_DISK)) {
            updateStatus(StatusType.CREATED_DISK);
        } else {
            updateStatus(StatusType.CREATED_DISK_IMAGE);
        }
    }

    @Override
    public void attachVolumes() throws Exception {
        computeClient.attachedDisk(migration, gcpVolumeMap);
    }

    @Override
    public void createImage() throws Exception {
        log.debug("Start create machine image for {}", migration.getInstanceName());
        Operation operation = computeClient.createMachineImage(migration);
        migration.setImageName(migration.getInstanceName());
        migration.setImageId(operation.getTargetId().toString());
    }

    @Override
    public void terminateInstance() {
        try {
            computeClient.terminateInstance(migration);

            for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
                GCPVolume gcpVolume = gcpVolumeMap.get(volume.getMigrationVolumeId());
                if (gcpVolume.getOperation() != null) {
                    computeClient.deleteDisk(
                            gcpVolume.getGcpProjectId(), gcpVolume.getZone(),
                            gcpVolume.getOperation().getTargetId().toString());
                    // volume.setVolumeId(null);
                }
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while terminate GCP instance.", e);
        }
    }

    @Override
    public void createInstance() throws Exception {
        computeClient.runInstances(migration, gcpVolumeMap, null);

        computeClient.addAccessConfigs(migration);

        Instance instance = computeClient.getInstance(migration);

        migration.setInstanceId(instance.getId().toString());

        for (AttachedDisk disk : instance.getDisks()) {
            migration.getBlockDevices().add(disk.getDeviceName());
        }

        int idx = instance.getZone().lastIndexOf("zones/");
        String zoneName = instance.getZone().substring(idx).split("/")[1];
        // String zoneName = zone.substring(idx, zone.length()).split("/")[1];

        migration.setAvailabilityZone(zoneName);

        try {
            migration.setInstanceLaunchTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(instance.getCreationTimestamp()));
        } catch (Exception e) {
            //ignore
        }
        // set security group names
        try {
            migration.setSecurityGroupNames(new ArrayList<>());
            for (NetworkInterface net : instance.getNetworkInterfaces()) {
                String networkName = computeClient.getNetworkName(net.getNetwork());
                List<FirewallDetail> fws = computeClient.getFirewallList(networkName);
                for (FirewallDetail fw : fws) {
                    if (StringUtils.isNotEmpty(fw.getFirewallName())) {
                        migration.getSecurityGroupNames().add(fw.getFirewallName());
                    } else {
                        migration.getSecurityGroupNames().add("");
                    }
                }
            }
        } catch (Exception e) {
            //ignore
        }

        migration.setPrivateIp(instance.getNetworkInterfaces().get(0).getNetworkIP());
    }

    private void compress() throws Exception {
        List<MigrationProcessDto.Volume> volumeList = migration.getVolumes();

        String bootTarGzPath = null;
        String dataTarGzPath = null;

        for (MigrationProcessDto.Volume volume : volumeList) {
            int idx = volumeList.indexOf(volume);

            File source = new File(volume.getRawFileName());
            if (source.exists()) {
                String parentPath = source.getAbsoluteFile().getParentFile().getPath();
                String targetPath = parentPath + File.separator + RAW_FILE;
                rename(source.getAbsolutePath(), targetPath);

                if (volume.getRootYn().equals("Y")) {
                    bootTarGzPath = parentPath + File.separator + BOOT_TAR_GZ;
                    compress(volume, parentPath, bootTarGzPath);
                } else {
                    dataTarGzPath = parentPath + File.separator + String.format(DATA_TAR_GZ, idx);
                    compress(volume, parentPath, dataTarGzPath);
                }
            }
        }

        chmod();
    }

    /**
     * Compress tar gz.
     *
     * @param volume the volume
     * @param source the source
     * @param target the target
     */
    private void compress(MigrationProcessDto.Volume volume, String source, String target) throws Exception {
        try {
            DefaultExecutor executor = new DefaultExecutor();

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("tar"),
                    "-czf",
                    target,
                    "-C",
                    source,
                    RAW_FILE,
                    "--format=oldgnu");

            /*
            CommandLine cl = new CommandLine(CollectionHelper.findCommand("sudo"))
                    .addArguments(CollectionHelper.findCommand("tar"))
                    .addArguments("-czf")
                    .addArguments(target)
                    .addArguments("-C")
                    .addArguments(source)
                    .addArguments(RAW_FILE)
                    .addArguments("--format=oldgnu");
             */

            log.debug("Command for tar.gz raw file(s) : [{}]", cl);

            int result = executor.execute(cl);

            if (result == 0) {
                GCPVolume gcpVol = new GCPVolume(migration.getGcpProjectId(), migration.getInventoryProcessId().toString());
                gcpVol.setTarGz(new File(target));
                gcpVol.setVol(volume);
                gcpVol.setRegion(migration.getRegion());

                if (migration.getAvailabilityZone() == null || migration.getAvailabilityZone().equals("")) {
                    log.debug("[{}] Migration set default zone ", migration.getInventoryProcessId());
                    gcpVol.setZone(migration.getRegion() + "-" + DEFAULT_ZONE);
                } else {
                    gcpVol.setZone(migration.getAvailabilityZone());
                }

                gcpVolumeMap.put(volume.getMigrationVolumeId(), gcpVol);
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while tar.gz the raw file.", e);
            throw e;
        } finally {
            rename(source + File.separator + RAW_FILE, volume.getRawFileName());
        }
    }

    /**
     * Rename.
     *
     * @param sourceFileName the source file name
     * @param targetFileName the target file name
     */
    private void rename(String sourceFileName, String targetFileName) throws Exception {

        try {
            DefaultExecutor executor = new DefaultExecutor();

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    "mv",
                    sourceFileName,
                    targetFileName);

            /*
            CommandLine cl = new CommandLine(CollectionHelper.findCommand("sudo"))
                    .addArguments("mv")
                    .addArguments(sourceFileName)
                    .addArguments(targetFileName);
            */

            log.debug("Command for rename raw file(s) : [{}]", cl);

            executor.execute(cl);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while rename the raw file.", e);
            throw e;
        }
    }

    /**
     * <pre>
     * Disk Image 생성
     * </pre>
     *
     * @throws Exception
     */
    private void createDiskImage() throws Exception {
        for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
            if (gcpVolumeMap.get(volume.getMigrationVolumeId()) != null) {
                log.debug("Start create disk image for volume[{}]", volume.getMigrationVolumeId());
                Operation response = computeClient.createDiskImage(bucketName, gcpVolumeMap.get(volume.getMigrationVolumeId()));

                // gcpVolumeMap.get(volume.getMigrationVolumeId()).setDiskImage("global/images/" + requestBody.getName());
                gcpVolumeMap.get(volume.getMigrationVolumeId()).setDiskImage(response.getTargetLink());
                gcpVolumeMap.get(volume.getMigrationVolumeId()).setOperation(response);
                gcpVolumeMap.get(volume.getMigrationVolumeId()).setResourceType(ResourceType.GLOBAL);
            }
        }
    }

    /**
     * <pre>
     * Disk 생성
     * </pre>
     *
     * @throws Exception
     */
    private void createDisks() throws Exception {
        for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
            if (gcpVolumeMap.get(volume.getMigrationVolumeId()) != null) {
                log.debug("Start create disk for volume[{}]", volume.getMigrationVolumeId());
                Operation response = computeClient.createDisk(bucketName, gcpVolumeMap.get(volume.getMigrationVolumeId()));

                volume.setTaskId(response.getId().toString());
                volume.setVolumeId(response.getTargetId().toString());

                gcpVolumeMap.get(volume.getMigrationVolumeId()).setDiskUrl(response.getTargetLink());
                gcpVolumeMap.get(volume.getMigrationVolumeId()).setOperation(response);
                gcpVolumeMap.get(volume.getMigrationVolumeId()).setResourceType(ResourceType.ZONAL);
            }
        }
    }

    /**
     * Status check instance.
     */
    private void statusCheckInstance() throws Exception {
        while (true) {
            Instance instance = computeClient.getInstance(migration);

            if (instance.getStatus().equals("PROVISIONING") || instance.getStatus().equals("STAGING")) {
                updateStatus(StatusType.INITIATE_INSTANCE);
            } else if (instance.getStatus().equals("RUNNING")) {
                // 인스턴스가 부팅 중이거나 실행중
                updateStatus(StatusType.COMPLETED);
                break;
            } else if (instance.getStatus().equals("TERMINATED")) {
                throw new RoRoException(StatusType.CANCELLED.toString());
            } else {
                throw new RoRoException("[GCP] " + instance.getStatusMessage());
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <pre>
     * Storage upload를 위한 thread
     * </pre>
     *
     * @version 1.0
     */
    private class BucketUpload extends Thread {
        private GCPVolume gcpVolume;
        private boolean isDone = false;
        private boolean isError = false;
        private Exception exception = null;

        /**
         * Instantiates a new Bucket upload.
         *
         * @param gcpVolume the gcp volume
         */
        public BucketUpload(GCPVolume gcpVolume) {
            this.gcpVolume = gcpVolume;
        }

        @Override
        public void run() {
            try {
                storageClient.uploadObject(bucketName, gcpVolume.getMigrationId(), gcpVolume.getTarGz());

                log.debug("[{}] file upload is done.", gcpVolume.getTarGz().getAbsolutePath());

                isDone = true;
            } catch (Exception e) {
                log.error("Unhandled exception occurred while execute upload to GCP Storage.", e);
                isError = true;
                exception = e;
            }
        }

        /**
         * Is done boolean.
         *
         * @return the isDone
         */
        public boolean isDone() {
            return isDone;
        }

        /**
         * Is error boolean.
         *
         * @return the isError
         */
        public boolean isError() {
            return isError;
        }

        /**
         * Gets exception.
         *
         * @return the exception
         */
        public Exception getException() {
            return exception;
        }
    }
}
//end of GCPRehostMigration.java
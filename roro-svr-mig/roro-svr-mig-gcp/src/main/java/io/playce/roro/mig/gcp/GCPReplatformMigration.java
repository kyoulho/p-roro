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

import com.google.api.services.compute.model.*;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.common.dto.migration.enums.StatusType;
import io.playce.roro.common.exception.CancelException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mig.AbstractReplatformMigration;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Component("GCPReplatformMigration")
@Scope("prototype")
public class GCPReplatformMigration extends AbstractReplatformMigration {

    private ComputeClient computeClient;
    private StorageClient storageClient;
    private String bucketName;
    private String folderName;
    private static final String DEFAULT_ZONE = "a";
    private final String INIT_SCRIPT = "init-script.sh";
    private Map<Long, GCPVolume> gcpVolumeMap = new HashMap<>();

    private String vpcName = null;
    private String subnetName = null;

    @Override
    public MigrationProcessDto migration() throws Exception {
        log.debug("GCPReplatformMigration.migration() invoked.");

        try {
            String keyString = IOUtils.toString(new File(migration.getCredential().getKeyFilePath()).toURI(), StandardCharsets.UTF_8);
            computeClient = new ComputeClient(migration.getGcpProjectId(), keyString);
            storageClient = new StorageClient(migration.getGcpProjectId(), keyString, migration.getRegion());

            this.bucketName = MigrationManager.getBucketName();
            this.folderName = Long.toString(migration.getInventoryProcessId());

            setResourceNames();

            /** File copy from AIX to RoRo worker */
            updateStatus(StatusType.CREATE_RAW_FILES);

            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            fileDownload(migration.getInventoryProcessId(), migration.getTargetHost());
            changeFileModes();

            updateStatus(StatusType.CREATED_RAW_FILES);
            updateStatus(StatusType.UPLOAD_TO_STORAGE);

            /**
             *  Creating DISK
             *
             **/
            updateStatus(StatusType.CREATING_DISK);
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createDisks();
            statusCheck();

            /**
             *  Creating instance
             *
             **/
            updateStatus(StatusType.CREATING_INSTANCE);
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createInstance();

            /**
             * Set osLogin policy using Service account
             *
             **/
            // setAccessControlPolicy();
            // String account = getServiceAccount();
            // LoginProfile loginProfile = createSshKey(account);

            TargetHost targetHost = new TargetHost();

            if (StringUtils.isNotEmpty(migration.getPublicIp())) {
                targetHost.setIpAddress(migration.getPublicIp());
            } else {
                targetHost.setIpAddress(migration.getPrivateIp());
            }
            targetHost.setPort(config.getConnectSshPort());
            targetHost.setUsername(config.getConnectUserName());
            targetHost.setPassword(config.getConnectUserPassword());
            targetHost.setKeyFilePath(config.getKeyFilePath());

            int cnt = 0;
            while (cnt++ < 5) {
                if (SSHUtil.healthCheck(targetHost)) {
                    break;
                }

                Thread.sleep(1000);
            }

            if (!SSHUtil.healthCheck(targetHost) && StringUtils.isNotEmpty(migration.getPublicIp())) {
                targetHost.setIpAddress(migration.getPrivateIp());

                if (!SSHUtil.healthCheck(targetHost)) {
                    throw new RoRoException("Unable connect to target GCE instance(" + migration.getInstanceId() + ", " + targetHost.getIpAddress() + ")");
                }
            }

            /** Target Configuration */
            /** Make filesystem and attach to instance */
            mount(targetHost);

            /** Add groups */
            addGroup(targetHost);

            /** Add users */
            addUser(migration.getTargetHost(), targetHost);

            /** File copy from RoRo worker to target instance */
            fileUpload(targetHost);

            /** Install packages */
            installPackages(targetHost);

            updateStatus(StatusType.CREATING_MACHINE_IMAGE);
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createImage();

            // Worker 서버에 저장된 backup 파일을 삭제한다.
            if (MigrationManager.getDirectoryRemove()) {
                deleteBackupFiles();
            }

            updateStatus(StatusType.COMPLETED);
        } catch (Exception e) {
            // 취소 여부와 관계없이 해당 스텝에서 오류가 발생하는 경우 VM Import 작업을 취소한다. (cancel()로 옮겨가지 않는 이유)
            if (migration.getInternalStatus().equals(StatusType.CREATING_DISK)) {
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
                        log.error("Unhandled exception occurred while execute cancel operation task.", ex);
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
        try {
            DefaultExecutor executor = new DefaultExecutor();

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("sh"),
                    MigrationManager.getCancelMigrationFile().getAbsolutePath(),
                    workDir,
                    targetHost.getIpAddress());

            log.debug("Execute shell script for GCP replatform migration process kill : [{}]", cl);

            int exitCode = executor.execute(cl);

            if (exitCode == 0) {
                log.debug("GCP replatform migration({}) has been cancelled.", migration.getInventoryProcessId());
            } else {
                log.debug("GCP replatform migration({}) cancel failed.", migration.getInventoryProcessId());
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while execute cancel_migration.sh.", e);
        }
    }

    private void statusCheck() {
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
        }
    }

    private void createImage() throws Exception {
        Operation operation = computeClient.createMachineImage(migration);
        migration.setImageName(migration.getInstanceName());
        migration.setImageId(operation.getTargetId().toString());
    }


    private void setAccessControlPolicy() throws Exception {
        Binding binding = new Binding();
        binding.setMembers(Arrays.asList("serviceAccount:" + computeClient.getServiceAccountClientEmail()));
        binding.setRole("roles/compute.osAdminLogin");

        ZoneSetPolicyRequest request = new ZoneSetPolicyRequest();
        request.setBindings(Arrays.asList(binding));

        computeClient.setIamPolicy(migration.getGcpProjectId(), migration.getAvailabilityZone(), migration.getInstanceId(), request);
    }

    /*
    private LoginProfile createSshKey(String account) throws Exception {
        String tempKeyName = "place-roro" + "-" + DateUtils.formatDate(new Date(), "yyyyMMddHHmm");
        File tempKeyFile = new File(workDir, tempKeyName);

        try {
            // String command = "ssh-keygen -t rsa -N ' ' -m PEM -f " + tempKeyFile.getAbsolutePath();
            DefaultExecutor executor = new DefaultExecutor();

            CommandLine cl = new CommandLine("ssh-keygen")
                    .addArguments("-t").addArgument("rsa")
                    .addArguments("-N").addArgument(" ")
                    .addArguments("-m").addArgument("PEM")
                    .addArguments("-f").addArgument(tempKeyFile.getAbsolutePath());
            log.debug("Execute command script for create key pair : [{}]", cl);

            executor.execute(cl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String publicKeyStr = null;
        try {
            publicKeyStr = new String(Files.readAllBytes(Paths.get(tempKeyFile.getAbsolutePath() + ".pub")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        LocalDateTime currentTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = currentTime.plusMinutes(30).atZone(ZoneId.systemDefault());
        long expiretime = zonedDateTime.toInstant().toEpochMilli() * 1000000;
        ImportSshPublicKeyRequest request = ImportSshPublicKeyRequest.newBuilder()
                .setSshPublicKey(
                        OsLoginProto.SshPublicKey.newBuilder()
                                .setKey(publicKeyStr)
                                .setExpirationTimeUsec(expiretime).build())
                .setProjectId(migration.getGcpProjectId())
                .setParent(account)
                .build();
        ImportSshPublicKeyResponse response = computeClient.insertPublicKey(request);

        // // key file 퍼미션 변경 (600)
        chmod("600", tempKeyFile.getAbsolutePath());
        log.debug("[{}] Temoprary key pair created.", tempKeyFile.getAbsoluteFile());

        migration.setKeyName(tempKeyName);
        migration.setKeyFile(tempKeyFile.getAbsolutePath());

        return response.getLoginProfile();
    }
    */

    private String getServiceAccount() {
        return "users/" + computeClient.getServiceAccountClientEmail();
    }

    private void createDisks() throws Exception {
        List<MigrationProcessDto.Volume> volumeList = migration.getVolumes();

        MachineImage machineImage = computeClient.getMachineImage(config.getGcpProjectId(), config.getImageId());

        if (machineImage == null) {
            throw new RuntimeException("[" + config.getImageId() + "] Image ID is not suitable.");
        }

        List<SavedAttachedDisk> attachedDisks = machineImage.getSourceInstanceProperties().getDisks();

        /*
         *  GCPVolume은 설정된 region, zone에 request 볼륨 정보를 가지는 디스크 생성을 위해 사용한다.
         *  volume에 device 정보가 없다면, 추가할 볼륨으로 여기고 만약 부트 볼륨인데 디바이스 정보가 없다면 머신이미지의 부트 볼륨 이미지를 설정한다.
         *  volume에 device 정보가 있다면, 머신이미지에 해당 디스크의 소스이미지 또는 디스크 url정보를 설정한다.
         * */
        char device = 'b';

        for (MigrationProcessDto.Volume volume : volumeList) {
            GCPVolume gcpVol = new GCPVolume(migration.getGcpProjectId(), folderName);
            gcpVol.setVol(volume);
            gcpVol.setRegion(migration.getRegion());
            gcpVol.setZone(migration.getAvailabilityZone());

            if ("N".equals(volume.getRootYn())) {
                volume.setDeviceName("/dev/sd" + device++);
            }

            if (StringUtils.isEmpty(volume.getDeviceName())) {
                if ("Y".equals(volume.getRootYn())) {
                    SavedAttachedDisk bootableDisk = attachedDisks.get(0);

                    // projects/{projectName}/zones/{zoneName}/disks/{diskName}
                    String zoneName = bootableDisk.getSource().split("/")[3];
                    String diskName = bootableDisk.getSource().split("/")[5];

                    Disk detailDisk = computeClient.getDisk(config.getGcpProjectId(), zoneName, diskName);

                    if (detailDisk.getSourceImage() != null) {
                        gcpVol.setDiskImage(detailDisk.getSourceImage());
                    } else {
                        gcpVol.setDiskUrl(detailDisk.getSelfLink());
                    }
                }
            } else {
                for (SavedAttachedDisk disk : attachedDisks) {
                    if (disk.getDeviceName().equals(volume.getDeviceName())) {
                        // projects/{projectName}/zones/{zoneName}/disks/{diskName}
                        String zoneName = disk.getSource().split("/")[3];
                        String diskName = disk.getSource().split("/")[5];

                        Disk detailDisk = computeClient.getDisk(config.getGcpProjectId(), zoneName, diskName);

                        if (detailDisk.getSourceImage() != null) {
                            gcpVol.setDiskImage(detailDisk.getSourceImage());
                        } else {
                            gcpVol.setDiskUrl(detailDisk.getSelfLink());
                        }

                        break;
                    }
                }
            }


            log.debug("[GCP] Migration [{}] replatform image info : {}", migration.getInventoryProcessId(), gcpVol.toString());

            Operation response = computeClient.createDisk(bucketName, gcpVol);

            volume.setTaskId(response.getId().toString());
            volume.setVolumeId(response.getTargetId().toString());

            gcpVolumeMap.put(volume.getMigrationVolumeId(), gcpVol);

            gcpVolumeMap.get(volume.getMigrationVolumeId()).setDiskUrl(response.getTargetLink());
            gcpVolumeMap.get(volume.getMigrationVolumeId()).setOperation(response);
            gcpVolumeMap.get(volume.getMigrationVolumeId()).setResourceType(ResourceType.ZONAL);
        }
    }

    private void createInstance() throws Exception {
        Metadata metadata = getInstanceMetadata();

        computeClient.runInstances(migration, gcpVolumeMap, metadata);
        computeClient.addAccessConfigs(migration);

        Instance instance = computeClient.getInstance(migration);

        migration.setInstanceId(instance.getId().toString());

        for (AttachedDisk disk : instance.getDisks()) {
            migration.getBlockDevices().add(disk.getDeviceName());
        }

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

        // set public & private ip address
        // migration.setPublicIp(instance.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP());
        migration.setPrivateIp(instance.getNetworkInterfaces().get(0).getNetworkIP());
    }

    private Metadata getInstanceMetadata() throws Exception {
        Metadata metadata = new Metadata();

        List<Metadata.Items> items = new ArrayList<>();
        /* Instance level ssh */
        if (config.getPubKey() != null && config.getPubKey().length() > 0) {
            Metadata.Items sshItem = new Metadata.Items().setKey("ssh-keys")
                    .setValue(String.format("%s:%s", config.getConnectUserName(), config.getPubKey()));
            Metadata.Items blockPrjSSH = new Metadata.Items().setKey("block-project-ssh-keys").setValue("true");
            items.addAll(Arrays.asList(sshItem, blockPrjSSH));
        }

        /*
         *  Init script
         * */
        if (config.getInitScript() != null) {
            Metadata.Items initItem = new Metadata.Items();
            if (config.getInitScript().getBytes().length / 1024 < 256) {
                initItem.setKey("startup-script").setValue(config.getInitScript());
            } else {
                String tmpDir = System.getProperty("java.io.tmpdir");
                File initScript = new File(tmpDir, INIT_SCRIPT);
                Path path = Paths.get(initScript.getAbsolutePath());
                Files.write(path, config.getInitScript().getBytes());

                storageClient.createBucket(bucketName, migration.getInventoryProcessId().toString());
                storageClient.uploadObject(bucketName, migration.getInventoryProcessId().toString(), initScript);

                String filePath = "gs://" + bucketName + "/" + migration.getInventoryProcessId() + "/" + INIT_SCRIPT;
                initItem = new Metadata.Items().setKey("startup-script-url").setValue(filePath);

                initScript.delete();
            }
            items.add(initItem);
        }
        metadata.setItems(items);
        return metadata;
    }
}
//end of GCPReplatformMigration.java
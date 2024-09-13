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
package io.playce.roro.mig.aws;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.util.StringUtils;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.common.dto.migration.enums.StatusType;
import io.playce.roro.common.exception.CancelException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.mig.AbstractRehostMigration;
import io.playce.roro.mig.MigrationManager;
import io.playce.roro.mig.aws.auth.BasicAWSCredentials;
import io.playce.roro.mig.aws.ec2.EC2Client;
import io.playce.roro.mig.aws.model.SecurityGroupDetail;
import io.playce.roro.mig.aws.model.SubnetDetail;
import io.playce.roro.mig.aws.model.VpcDetail;
import io.playce.roro.mig.aws.s3.S3Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Component("AWSRehostMigration")
@Scope("prototype")
public class AWSRehostMigration extends AbstractRehostMigration {

    private List<S3Upload> uploadThreadList = new ArrayList<>();

    private EC2Client ec2;
    private S3Client s3;
    private AWSCredentials credentials;

    private String vpcName = null;
    private String subnetName = null;
    private List<String> securityGroupNames = new ArrayList<>();

    @Override
    public MigrationProcessDto migration() throws Exception {
        log.debug("AWSRehostMigration.migration() invoked.");

        try {
            MigrationProcessDto.Credential credential = migration.getCredential();
            credentials = new BasicAWSCredentials(credential.getAccessKey(), credential.getSecretKey());

            ec2 = new EC2Client(credentials, migration.getRegion());

            // S3 region은 migration 타깃 region이 아닌 Bucket이 생성된 region으로 설정한다.
            s3 = new S3Client(credentials, MigrationManager.getBucketRegion());

            setResourceNames();

            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            preMigrate();

            // s3Upload
            updateStatus(StatusType.UPLOAD_TO_S3);

            // manifestUrl이 있으면 S3에 업로드 하지 않고 manifestUrl 정보를 이용하여 importInstance / importVolume을 수행한다.
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            upload();

            // Worker 서버에 저장된 raw image 파일을 삭제한다.
            if (MigrationManager.getDirectoryRemove()) {
                deleteRawFiles();
            }

            for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
                if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                    throw new CancelException("Migration Cancel Requested.");
                }
                /// importInstance & importVolume
                if ("Y".equals(volume.getRootYn())) {
                    migration = ec2.importInstance(migration, volume);
                } else {
                    migration = ec2.importVolume(migration, volume);
                }
            }

            updateStatus(StatusType.DOWNLOAD_FROM_S3);

            // status check
            statusCheck();

            // attachVolumes
            updateStatus(StatusType.ATTACHING_VOLUME);

            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            attachVolumes();
            updateStatus(StatusType.ATTACHED_VOLUME);

            // createPostScript / uploadScript (sudo mount)
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createScript();

            // createImage
            updateStatus(StatusType.CREATING_AMI);

            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createImage();
            updateStatus(StatusType.CREATED_AMI);

            if (ec2.getInstance(migration.getInstanceId()) != null) {
                // terminateInstance
                updateStatus(StatusType.TERMINATING_INSTANCE);

                terminateInstance();
                updateStatus(StatusType.TERMINATED_INSTANCE);
            }

            // createInstance
            updateStatus(StatusType.CREATING_INSTANCE);

            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createInstance();

            updateStatus(StatusType.COMPLETED);

            // delete folder in S3
            if (MigrationManager.getBucketRemove()) {
                deleteFolder();
            }
        } catch (Exception e) {
            // 취소 여부와 관계없이 해당 스텝에서 오류가 발생하는 경우 VM Import 작업을 취소한다. (cancel()로 옮겨가지 않는 이유)
            if (migration.getInternalStatus().equals(StatusType.DOWNLOAD_FROM_S3) ||
                    migration.getInternalStatus().equals(StatusType.CONVERTING) ||
                    migration.getInternalStatus().equals(StatusType.INITIATE_INSTANCE) ||
                    migration.getInternalStatus().equals(StatusType.ATTACHING_VOLUME) ||
                    migration.getInternalStatus().equals(StatusType.ATTACHED_VOLUME)) {
                List<MigrationProcessDto.Volume> volumeList = migration.getVolumes();

                for (MigrationProcessDto.Volume volume : volumeList) {
                    try {
                        ec2.cancelConversionTask(volume.getTaskId());
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
        VpcDetail vpcDetail = new VpcDetail();
        vpcDetail.setSearch(migration.getVpcId());
        List<VpcDetail> vpcList = ec2.getVPCList(vpcDetail);
        if (vpcList != null && vpcList.size() == 1) {
            vpcName = vpcList.get(0).getVpcName();
        }

        SubnetDetail subnetDetail = new SubnetDetail();
        subnetDetail.setSearch(migration.getSubnetId());
        List<SubnetDetail> subnetList = ec2.getSubnetList(subnetDetail);
        if (subnetList != null && subnetList.size() == 1) {
            subnetName = subnetList.get(0).getSubnetName();
        }

        securityGroupNames = new ArrayList<>();
        for (String sgId : migration.getSecurityGroupIds()) {
            SecurityGroupDetail securityGroupDetail = new SecurityGroupDetail();
            securityGroupDetail.setSearch(sgId);
            List<SecurityGroupDetail> securityGroupList = ec2.getSecurityGroupList(securityGroupDetail);
            if (securityGroupList != null && securityGroupList.size() == 1) {
                securityGroupNames.add(securityGroupList.get(0).getGroupName());
            } else {
                securityGroupNames.add("");
            }
        }

        migration.setVpcName(vpcName);
        migration.setSubnetName(subnetName);
        migration.setSecurityGroupNames(securityGroupNames);
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

                log.debug("Execute shell script for AWS rehost migration process kill : [{}]", cl);

                int exitCode = executor.execute(cl);

                if (exitCode == 0) {
                    log.debug("AWS rehost migration({}) has been cancelled.", migration.getInventoryProcessId());
                } else {
                    log.debug("AWS rehost migration({}) cancel failed.", migration.getInventoryProcessId());
                }
            } catch (Exception e) {
                log.error("Unhandled exception occurred while execute cancel_migration.sh.", e);
            }
        }

        if (migration.getInternalStatus().equals(StatusType.UPLOAD_TO_S3)) {
            for (S3Upload uploadThread : uploadThreadList) {
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

    /**
     * <pre>
     * raw image 파일을 S3에 업로드한다.
     * </pre>
     *
     * @throws Exception
     */
    @Override
    public void upload() throws Exception {
        // S3 Bucket 생성
        s3.createBucketUnlessExist(bucketName);

        List<MigrationProcessDto.Volume> volumeList = migration.getVolumes();

        for (MigrationProcessDto.Volume volume : volumeList) {
            S3Upload uploadThread = new S3Upload(folderName, volume, credentials, MigrationManager.getBucketRegion());
            uploadThreadList.add(uploadThread);
            uploadThread.start();
        }

        while (true) {
            int cnt = 0;
            for (S3Upload uploadThread : uploadThreadList) {
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
    public void deleteFolder() throws Exception {
        s3.deleteFolder(bucketName, folderName);
    }

    /**
     * <pre>
     * importInstance, importVolume 상태 체크
     * </pre>
     */
    @Override
    public void statusCheck() throws Exception {
        List<MigrationProcessDto.Volume> volumeList = migration.getVolumes();

        while (true) {
            int cnt = 0;
            for (MigrationProcessDto.Volume volume : volumeList) {
                ConversionTask conversionTask = ec2.describeConversionTask(volume.getTaskId());

                if ("Y".equals(volume.getRootYn())) {
                    ImportInstanceTaskDetails iDetail = conversionTask.getImportInstance();
                    List<ImportInstanceVolumeDetailItem> volumeDeatilList = iDetail.getVolumes();

                    if (conversionTask.getState().equals("active")) {
                        // state가 active이고, statusMessage가 Pending 상태에서는 볼륨 상태를 확인한다.
                        // state가 active이고, statusMessage가 Pending xx% 에서는 해당 볼륨을 이용하여 인스턴스가 생성된다.
                        // state가 complete 상태에서는 별도의 상태메시지가 없으며, 마이그레이션이 종료된 상태이다. (ec2 인스턴스 시작 필요)

                        ImportInstanceVolumeDetailItem volumeDetail = volumeDeatilList.get(0);

                        if ((System.currentTimeMillis() - lastLogTime) > 60000) {
                            log.debug("MigrationId : [{}], TaskId : [{}],  State : [{}], Message : [{}], ByteConverted : [{}], VolumeStatus : [{}], VolumeStatusMessage: [{}]",
                                    new Object[]{migration.getInventoryProcessId(), volume.getTaskId(), conversionTask.getState(), conversionTask.getStatusMessage(), volumeDetail.getBytesConverted()
                                            , volumeDetail.getStatus(), volumeDetail.getStatusMessage()});

                            lastLogTime = System.currentTimeMillis();
                        }

                        if (volumeDetail.getStatus().equals("active")) {
                            if (volumeDetail.getBytesConverted() > 0) {
                                // converting
                                updateStatus(StatusType.CONVERTING);
                            }
                        } else {
                            // root device에 대한 EBS 볼륨 생성이 완료된 상태
                            if (StringUtils.isNullOrEmpty(volume.getVolumeId())) {
                                volume.setVolumeId(volumeDetail.getVolume().getId());
                            }

                            // create an instance
                            updateStatus(StatusType.INITIATE_INSTANCE);
                        }
                    } else if (conversionTask.getState().equals("completed")) {
                        // EC2 instance 생성이 완료된 상태
                        cnt++;
                    } else if (conversionTask.getState().equals("deleted")) {
                        throw new RoRoException(StatusType.CANCELLED.toString());
                    } else {
                        throw new RoRoException("[AWS] " + conversionTask.getStatusMessage());
                    }
                } else {
                    if (StringUtils.isNullOrEmpty(volume.getVolumeId())) {
                        if (conversionTask.getState().equals("completed")) {
                            ImportVolumeTaskDetails vDetail = conversionTask.getImportVolume();
                            volume.setVolumeId(vDetail.getVolume().getId());
                            cnt++;
                        }
                    } else {
                        cnt++;
                    }
                }
            }

            if (cnt == volumeList.size()) {
                break;
            }

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                // Nothing to do
            }
        }

        // 상태는 ATTACHING_VOLUME 으로 업데이트 한다.
        updateStatus(StatusType.ATTACHING_VOLUME);
    }

    /**
     * <pre>
     * 추가 EBS 볼륨을 attach 한다.
     * </pre>
     */
    @Override
    public void attachVolumes() throws Exception {
        ec2.attachVolumes(migration);
    }

    /**
     * <pre>
     * gp3 변환을 위해 AMI를 생성한다.
     * </pre>
     *
     * @throws Exception
     */
    @Override
    public void createImage() throws Exception {
        String imageId = ec2.createImage(migration);

        while (true) {
            if ("available".equals(ec2.getImageState(imageId))) {
                break;
            }

            if ((System.currentTimeMillis() - lastLogTime) > 60000) {
                log.debug("[{}] Create image({}) is not yet completed.", migration.getInventoryProcessId(), imageId);

                lastLogTime = System.currentTimeMillis();
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                // Nothing to do
            }
        }
    }

    /**
     * <pre>
     * ec2 인스턴스를 종료한다.
     * </pre>
     *
     * @throws Exception
     */
    @Override
    public void terminateInstance() {
        ec2.terminateInstance(migration.getInstanceId());

        while (true) {
            if ("terminated".equals(ec2.getInstanceState(migration.getInstanceId()))) {
                break;
            }

            if ((System.currentTimeMillis() - lastLogTime) > 30000) {
                log.debug("[{}] Terminate instance({}) is not yet completed.", migration.getInventoryProcessId(), migration.getInstanceId());

                lastLogTime = System.currentTimeMillis();
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                // Nothing to do
            }
        }

        for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
            if (!StringUtils.isNullOrEmpty(volume.getVolumeId())) {
                ec2.deleteVolume(volume.getVolumeId());
                // volume.setVolumeId(null);
            }
        }
    }

    /**
     * <pre>
     * AMI를 이용하여 인스턴스를 생성하고 구동한다.
     * </pre>
     *
     * @throws Exception
     */
    @Override
    public void createInstance() throws Exception {
        String instanceId = ec2.runInstances(migration, targetHost.getUsername(), !StringUtils.isNullOrEmpty(targetHost.getPassword()));
        migration.setInstanceId(instanceId);

        while (true) {
            if ("running".equals(ec2.getInstanceState(migration.getInstanceId())) ||
                    "stopped".equals(ec2.getInstanceState(migration.getInstanceId())) ||
                    "terminated".equals(ec2.getInstanceState(migration.getInstanceId()))) {
                break;
            }

            if ((System.currentTimeMillis() - lastLogTime) > 30000) {
                log.debug("[{}] Create instance({}) is not yet completed.", migration.getInventoryProcessId(), migration.getInstanceId());

                lastLogTime = System.currentTimeMillis();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Nothing to do
            }
        }

        if ("Y".equals(migration.getEnableEipYn())) {
            try {
                String publicIp = ec2.getElasticIp();
                ec2.associateAddress(instanceId, publicIp);
            } catch (Exception e) {
                log.error("Elastic IP associate failed.", e);
            }
        }

        boolean nameTagExists = false;

        // Add tags
        for (MigrationProcessDto.Tag tag : migration.getTags()) {
            if ("Name".equals(tag.getTagName())) {
                nameTagExists = true;
            }

            if (!StringUtils.isNullOrEmpty(tag.getTagName())) {
                ec2.createTag(instanceId, tag.getTagName(), tag.getTagValue());
            }
        }

        if (!nameTagExists) {
            if (!StringUtils.isNullOrEmpty(migration.getHostName())) {
                ec2.createTag(instanceId, "Name", migration.getHostName());
            }
        }

        Instance instance = ec2.getInstance(instanceId);

        // set device blocks
        for (InstanceBlockDeviceMapping ibdm : instance.getBlockDeviceMappings()) {
            migration.getBlockDevices().add(ibdm.getDeviceName());
        }

        // set availability zone
        migration.setAvailabilityZone(instance.getPlacement().getAvailabilityZone());

        // set launch time
        migration.setInstanceLaunchTime(instance.getLaunchTime());

        // set security group names
        migration.setSecurityGroupNames(new ArrayList<>());
        for (GroupIdentifier group : instance.getSecurityGroups()) {
            if (StringUtils.isNullOrEmpty(group.getGroupName())) {
                migration.getSecurityGroupNames().add("");
            } else {
                migration.getSecurityGroupNames().add(group.getGroupName());
            }
        }

        // set image name
        // imageId & imageName already set in createImage()
        //migration.setImageName(migration.getHostname() + "_" + migration.getInstanceId());

        // set instance name
        migration.setInstanceName(migration.getHostName());

        // set vpcId
        migration.setVpcId(instance.getVpcId());

        // set public & private ip address
        migration.setPublicIp(instance.getPublicIpAddress());
        migration.setPrivateIp(instance.getPrivateIpAddress());

        while (true) {
            if (ec2.isStatucCheckOk(instanceId)) {
                break;
            }

            if ((System.currentTimeMillis() - lastLogTime) > 60000) {
                log.debug("[{}] Instance({}) status check is not yet passed.", migration.getInventoryProcessId(), instanceId);

                lastLogTime = System.currentTimeMillis();
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                // Nothing to do
            }
        }

		/*
		try {
			// runScript(config) 수행 시 추가 볼륨이 있는 경우 UUID 형태로 fstab에 추가함.
			Server server = serverRepository.findFirstByOwnerAndHost(migration.getUsername(), migration.getHost());
			server.setHost(instance.getPublicIpAddress());

			for (Volume volume : migration.getVolumes()) {
				if ("N".equals(volume.getRootYn())) {
					SSHUtil.executeCommand(TargetHost.convert(server), "sudo mount " + volume.getDevice().replaceAll("sd", "xvd") + "1 " + volume.getPath());
				}
			}
		} catch (Exception e) {
			log.error("Unhandled exception occurred while execute mount volume(s).", e);
		}
		//*/
    }

    /**
     * <pre>
     * 인스턴스 구동 시 스크립트를 내려받아 실행할 수 있도록 init script 작성하고 s3에 업로드한다.
     * </pre>
     */
    private void createScript() {
		/*
		String fstype = "ext4";
		for (Disk disk : server.getSummary().getDisks()) {
			if ("/".equals(disk.getMount()) && !StringUtils.isNullOrEmpty(disk.getType())) {
				fstype = disk.getType();
				break;
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("#!/bin/bash").append("\n");

		for (Volume volume : migration.getVolumes()) {
			if ("N".equals(volume.getRootYn())) {
				sb.append("echo -e ")
				.append("\"")
				.append(volume.getDevice().replaceAll("sd", "xvd") + "1").append("\t")
				.append(volume.getPath()).append("\t")
				.append(fstype).append("\t")
				.append("defaults,noatime").append("\t")
				.append("0 0")
				.append("\"")
				.append(" >> /etc/fstab")
				.append("\n");
			}
		}

		sb.append("mount -a").append("\n");

		String scriptName = folderName + "/roro_init.sh";
		scriptUrl = s3.generatePresignedUrl(bucketName, scriptName, HttpMethod.GET);

		s3.putObject(bucketName, scriptName, sb.toString());
		//*/
    }

    /**
     * <pre>
     * Multipart upload를 위한 thread
     * </pre>
     *
     * @author Sang-cheon Park
     * @version 1.0
     */
    private class S3Upload extends Thread {
        private String folderName;
        private MigrationProcessDto.Volume volume;
        private boolean isDone = false;
        private boolean isError = false;
        private Exception exception = null;
        private S3Client s3;

        public S3Upload(String folderName, MigrationProcessDto.Volume volume, AWSCredentials credentials, String region) {
            this.folderName = folderName;
            this.volume = volume;
            this.s3 = new S3Client(credentials, region);
        }

        @Override
        public void run() {
            try {
                File file = new File(volume.getRawFileName());

                // if (StringUtils.isNullOrEmpty(volume.getManifestUrl())) {
                // S3 Upload as multipart
                s3.putObjectAsMultiPart(MigrationManager.getBucketName(), folderName, file);

                // S3 Upload with Transfer Acceleration
                //s3.putObject(bucketName, folderName + "/" + file.getName(), file);

                // S3 Upload with CLI
                //s3.cp(bucketName, folderName + "/" + file.getName(), file);

                StringBuilder prefix = new StringBuilder();
                prefix.append(folderName + "/");
                prefix.append(file.getName());
                prefix.append("manifest.xml");

                String manifestXmlStr = s3.getManifest(bucketName, folderName, file, volume.getVolumeSize());
                s3.putObject(bucketName, prefix.toString(), manifestXmlStr);

                volume.setManifestUrl(s3.generatePresignedUrl(bucketName, prefix.toString(), HttpMethod.GET));
                // }

                log.debug("[{}] file upload is done.", file.getAbsolutePath());

                isDone = true;
            } catch (Exception e) {
                log.error("Unhandled exception occurred while execute upload to S3.", e);
                isError = true;
                exception = e;
            }
        }

        /**
         * @return the isDone
         */
        public boolean isDone() {
            return isDone;
        }

        /**
         * @return the isError
         */
        public boolean isError() {
            return isError;
        }

        /**
         * @return the exception
         */
        public Exception getException() {
            return exception;
        }
    }
}
//end of AWSRehostMigration.java
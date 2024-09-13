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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.util.StringUtils;
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
import io.playce.roro.mig.aws.auth.BasicAWSCredentials;
import io.playce.roro.mig.aws.ec2.EC2Client;
import io.playce.roro.mig.aws.model.SecurityGroupDetail;
import io.playce.roro.mig.aws.model.SubnetDetail;
import io.playce.roro.mig.aws.model.VpcDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
@Component("AWSReplatformMigration")
@Scope("prototype")
public class AWSReplatformMigration extends AbstractReplatformMigration {

    private EC2Client ec2;
    private AWSCredentials credentials;

    private String vpcName = null;
    private String subnetName = null;
    private List<String> securityGroupNames = new ArrayList<>();

    @Override
    public MigrationProcessDto migration() throws Exception {
        /**
         1. Create an EC2 instance using given Image

         2. (Optional) Create EBS Volumes & Attach

         3. Target Configuration
         - (Optional) EBS Volume mount
         - Group Add
         - User Add
         - User Modify (group & password)
         - Write .bash_profile
         - Write crontab
         - (Optional) Install Package
         - File copy from source to target
         - (Optional) Change file owner and group

         4. Create AMI
         //*/

        log.debug("AWSReplatformMigration.migration() invoked.");

        try {
            MigrationProcessDto.Credential credential = migration.getCredential();
            credentials = new BasicAWSCredentials(credential.getAccessKey(), credential.getSecretKey());

            ec2 = new EC2Client(credentials, migration.getRegion());

            setResourceNames();

            /** File copy from AIX to RoRo worker */
            updateStatus(StatusType.CREATE_RAW_FILES);

            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            fileDownload(migration.getInventoryProcessId(), migration.getTargetHost());

            changeFileModes();

            updateStatus(StatusType.CREATED_RAW_FILES);

            /** Create an EC2 instance using given AMI */
            updateStatus(StatusType.CREATING_INSTANCE);
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createInstance();

            TargetHost targetHost = new TargetHost();

            if (!StringUtils.isNullOrEmpty(migration.getPublicIp())) {
                targetHost.setIpAddress(migration.getPublicIp());
            } else {
                targetHost.setIpAddress(migration.getPrivateIp());
            }
            targetHost.setPort(config.getConnectSshPort());
            targetHost.setUsername(config.getConnectUserName());
            targetHost.setPassword(config.getConnectUserPassword());
            targetHost.setKeyFilePath(config.getKeyFilePath());

            if (!SSHUtil.healthCheck(targetHost) && !StringUtils.isNullOrEmpty(migration.getPublicIp())) {
                log.debug("Unable connect to target EC2 instance(" + migration.getInstanceId() + ", " +
                        migration.getPublicIp() + ") and trying to connect using private IP(" + migration.getPrivateIp() + ")");

                targetHost.setIpAddress(migration.getPrivateIp());

                if (!SSHUtil.healthCheck(targetHost)) {
                    throw new RoRoException("Unable connect to target EC2 instance(" + migration.getInstanceId() + ", " + targetHost.getIpAddress() + ")");
                }
            }

            // if (!SSHUtil.isSudoer(targetHost)) {
            //     throw new RoRoException("Can't get sudo privilege for EC2 instance(" + migration.getInstanceId() + ", " + targetHost.getIpAddress() + ")");
            // }

            /** Make filesystem and attach to instance */
            mount(targetHost);

            /** Add groups */
            addGroup(targetHost);

            /** Add users */
            addUser(migration.getTargetHost(), targetHost);

            /** File copy from RoRo worker to target instance */
            updateStatus(StatusType.DOWNLOAD_FROM_S3);
            fileUpload(targetHost);

            /** Install packages */
            installPackages(targetHost);

            /** createInstance */
            updateStatus(StatusType.CREATING_AMI);
            if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
                throw new CancelException("Migration Cancel Requested.");
            }
            createImage();

            updateStatus(StatusType.COMPLETED);
        } catch (Exception e) {
            throw e;
        } finally {
            deleteBackupFiles();
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
        try {
            DefaultExecutor executor = new DefaultExecutor();

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("sh"),
                    MigrationManager.getCancelMigrationFile().getAbsolutePath(),
                    workDir,
                    targetHost.getIpAddress());

            log.debug("Execute shell script for AWS replatform migration process kill : [{}]", cl);

            int exitCode = executor.execute(cl);

            if (exitCode == 0) {
                log.debug("AWS replatform migration({}) has been cancelled.", migration.getInventoryProcessId());
            } else {
                log.debug("AWS replatform migration({}) cancel failed.", migration.getInventoryProcessId());
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while execute cancel_migration.sh.", e);
        }
    }

    /**
     * Create instance.
     */
    private void createInstance() {
        List<Image> imageList = ec2.getImages(config.getImageId(), null, null);

        if (imageList == null || imageList.size() > 1) {
            throw new RuntimeException("[" + config.getImageId() + "] Image ID is not suitable.");
        }

        List<BlockDeviceMapping> blockDeviceMappingList = imageList.get(0).getBlockDeviceMappings();
        List<BlockDeviceMapping> deleteMappingList = new ArrayList<>();

        boolean isExist = false;
        for (BlockDeviceMapping mapping : blockDeviceMappingList) {
            isExist = false;

            for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
                if (mapping.getDeviceName().equals(volume.getDeviceName())) {
                    if (mapping.getEbs().getVolumeSize() != volume.getVolumeSize().intValue()) {
                        mapping.getEbs().setVolumeSize(volume.getVolumeSize().intValue());
                    }

                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                deleteMappingList.add(mapping);
            }
        }

        for (BlockDeviceMapping mapping : deleteMappingList) {
            blockDeviceMappingList.remove(mapping);
        }

        for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
            if (StringUtils.isNullOrEmpty(volume.getDeviceName())) {
                isExist = true;
                char device = 'b';

                while (isExist) {
                    isExist = false;
                    for (BlockDeviceMapping mapping : blockDeviceMappingList) {
                        if (mapping.getDeviceName().endsWith(device + "")) {
                            isExist = true;
                            break;
                        } else {
                            isExist = false;
                        }
                    }

                    if (isExist) {
                        device++;
                    }
                }

                EbsBlockDevice ebs = new EbsBlockDevice();
                ebs.setDeleteOnTermination(true);
                ebs.setEncrypted(false);
                ebs.setVolumeType("gp2");
                ebs.setVolumeSize(volume.getVolumeSize().intValue());

                BlockDeviceMapping mapping = new BlockDeviceMapping();
                mapping.setDeviceName("/dev/sd" + device);
                mapping.setEbs(ebs);

                blockDeviceMappingList.add(mapping);

                volume.setDeviceName("/dev/sd" + device); // mount 하기 위해 필요
            }
        }

        // sshd_config의 PasswordAuthentication, PermitRootPassword의 설정 변경 기준을 preconfig의 접속 유저 기준으로 함
        // String instanceId = ec2.runInstances(config.getImageId(), config.getKeyPair(), migration.getSubnetId(),
        //         migration.getSecurityGroupIds().toArray(new String[0]), migration.getInstanceType(), migration.getPrivateIp(),
        //         blockDeviceMappingList, config.getInitScript(),
        //         config.getConnectUserName(), !StringUtils.isNullOrEmpty(config.getConnectUserPassword()));

        // sshd_config의 PasswordAuthentication, PermitRootPassword의 설정 변경 기준을 preconfig가 아닌 소스 서버의 접속 유저 기준으로 변경
        String instanceId = ec2.runInstances(config.getImageId(), config.getKeyPair(), migration.getSubnetId(),
                migration.getSecurityGroupIds().toArray(new String[0]), migration.getInstanceType(), migration.getPrivateIp(),
                blockDeviceMappingList, config.getInitScript(),
                migration.getTargetHost().getUsername(), !StringUtils.isNullOrEmpty(migration.getTargetHost().getPassword()));

        migration.setInstanceId(instanceId);

        log.debug("[{}] EC2 instance create requested.", migration.getInstanceId());

        // 볼륨 생성
        // String zone = ec2.getAvailabilityZone(migration.getSubnetId());
        // ec2.createVolumes(migration, zone);
        // log.debug("EBS volume created.");

        while (true) {
            if ("running".equals(ec2.getInstanceState(migration.getInstanceId())) ||
                    "stopped".equals(ec2.getInstanceState(migration.getInstanceId())) ||
                    "terminated".equals(ec2.getInstanceState(migration.getInstanceId()))) {
                break;
            }

            if ((System.currentTimeMillis() - lastLogTime) > 30000) {
                log.debug("[{}] Instance({}) for replatform is not yet running.", migration.getInventoryProcessId(), migration.getInstanceId());

                lastLogTime = System.currentTimeMillis();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // nothing to do
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

        // 볼륨 추가
        // ec2.attachVolumes(migration);
        // log.debug("EBS volume attached.");

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

        // set instance name
        //migration.setInstanceName(instanceName);

        // set vpcId
        migration.setVpcId(instance.getVpcId());

        // set public & private ip address
        migration.setPublicIp(instance.getPublicIpAddress());
        migration.setPrivateIp(instance.getPrivateIpAddress());

        while (true) {
            if (ec2.isStatucCheckOk(migration.getInstanceId())) {
                break;
            }

            if ((System.currentTimeMillis() - lastLogTime) > 60000) {
                log.debug("[{}] Instance({}) status check is not yet passed.", migration.getInventoryProcessId(), migration.getInstanceId());

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
     * EC2 Template Instance로부터 AMI Image를 생성한다.
     * </pre>
     */
    private void createImage() {
        // Image 관련 정보를 migration에 저장
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
}
//end of AWSReplatformMigration.java
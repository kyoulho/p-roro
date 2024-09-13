/*
 * Copyright 2020 The Playce-RoRo Project.
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
 * Sang-cheon Park	2020. 3. 22.		First Draft.
 */
package io.playce.roro.mig.aws.ec2;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.util.StringUtils;
import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.mig.aws.model.Permission;
import io.playce.roro.mig.aws.model.SecurityGroupDetail;
import io.playce.roro.mig.aws.model.SubnetDetail;
import io.playce.roro.mig.aws.model.VpcDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <pre>
 * Playce-RoRo에서 사용될 AWS EC2 전용 클라이언트
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
@Slf4j
public class EC2Client {

    /**
     * #cloud-config
     * ssh_pwauth: 1
     * users:
     * - user: "ubuntu"
     * lock_passwd: 0
     * passwd: "$6$SALT$M7YF61Lj0eq8W9JJHgjYa69tNRtHvJ5yGxffqCdLzEA3wxDziWfZ..I2tj3hplOhFne2Oge1RQ8e74xkijykE."
     * runcmd:
     * - sed -i -e '/^PermitRootLogin/s/^.*$/PermitRootLogin yes/' /etc/ssh/sshd_config
     * - [sh, -c, 'service sshd restart']
     * - [sh, -c, 'passwd -u ubuntu']
     * - [sh, -c, 'groupmod -g `stat -c %g /home/ec2-user/` ec2-user']
     * - [sh, -c, 'usermod -u `stat -c %u /home/ec2-user/` ec2-user']
     */
    private static final String USER_DATA_DEFAULT = "#cloud-config\n" +
            "ssh_pwauth: 1\n" +
            "runcmd:\n" +
            " - [sh, -c, 'passwd -u {USERNAME}']";

    private static final String USER_DATA_WITH_ROOT = "#cloud-config\n" +
            "ssh_pwauth: 1\n" +
            "runcmd:\n" +
            " - sed -i -e 's/^\\(#\\?\\)PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config\n" +
            " - [sh, -c, 'service sshd restart']\n" +
            " - [sh, -c, 'passwd -u root']";
    // " - [sh, -c, 'groupmod -g `stat -c %g /home/ec2-user/` ec2-user']\n" +
    // " - [sh, -c, 'usermod -u `stat -c %u /home/ec2-user/` ec2-user']";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    // " - [sh, -c, 'groupmod -g `stat -c %g /home/ec2-user/` ec2-user']\n" +
    // " - [sh, -c, 'usermod -u `stat -c %u /home/ec2-user/` ec2-user']";

    private static Map<String, List<io.playce.roro.mig.aws.model.InstanceType>> instanceTypeMap = new HashMap<>();

    private AmazonEC2 ec2;

    public EC2Client(AWSCredentials credentials, String region) {
        ec2 = AmazonEC2ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Gets instance type offering.
     *
     * @param subnetId the subnet id
     *
     * @return the instance type offering
     */
    public List<io.playce.roro.mig.aws.model.InstanceType> getInstanceTypeOffering(String subnetId) {
        String availabilityZone = getAvailabilityZone(subnetId);

        if (instanceTypeMap.get(availabilityZone) == null) {
            List<io.playce.roro.mig.aws.model.InstanceType> instanceTypeList = new ArrayList<>();

            String nextToken = null;
            DescribeInstanceTypeOfferingsRequest describeInstanceTypeOfferingsRequest = new DescribeInstanceTypeOfferingsRequest();

            Filter filter = new Filter();
            filter.setName("location");
            filter.getValues().add(availabilityZone);
            describeInstanceTypeOfferingsRequest.getFilters().add(filter);
            describeInstanceTypeOfferingsRequest.setLocationType("availability-zone");
            describeInstanceTypeOfferingsRequest.setMaxResults(100);
            DescribeInstanceTypeOfferingsResult describeInstanceTypeOfferingsResult = ec2.describeInstanceTypeOfferings(describeInstanceTypeOfferingsRequest);

            int i = 1;
            List<String> instanceTypeNames = null;
            while (true) {
                instanceTypeNames = new ArrayList<>();
                for (InstanceTypeOffering type : describeInstanceTypeOfferingsResult.getInstanceTypeOfferings()) {
                    instanceTypeNames.add(type.getInstanceType());
                }

                DescribeInstanceTypesRequest describeInstanceTypesRequest = new DescribeInstanceTypesRequest();
                describeInstanceTypesRequest.setInstanceTypes(instanceTypeNames);
                DescribeInstanceTypesResult describeInstanceTypesResult = ec2.describeInstanceTypes(describeInstanceTypesRequest);

                for (InstanceTypeInfo info : describeInstanceTypesResult.getInstanceTypes()) {
                    io.playce.roro.mig.aws.model.InstanceType instanceType = new io.playce.roro.mig.aws.model.InstanceType();
                    instanceType.setType(info.getInstanceType());
                    instanceType.setVCPUs(Integer.toString(info.getVCpuInfo().getDefaultVCpus()));
                    instanceType.setMemory(String.format("%.1f", (float) info.getMemoryInfo().getSizeInMiB() / 1024).replaceAll("\\.0", ""));
                    instanceType.setFamily(info.getInstanceType().substring(0, info.getInstanceType().indexOf(".")));

                    if ("t1".equals(instanceType.getFamily())) {
                        instanceType.setGroup(11);
                    } else if ("t2".equals(instanceType.getFamily())) {
                        instanceType.setGroup(12);
                    } else if ("t3".equals(instanceType.getFamily())) {
                        instanceType.setGroup(13);
                    } else if ("t3a".equals(instanceType.getFamily())) {
                        instanceType.setGroup(14);
                    } else if ("t4g".equals(instanceType.getFamily())) {
                        instanceType.setGroup(15);
                    } else if ("a1".equals(instanceType.getFamily())) {
                        instanceType.setGroup(21);
                    } else if ("c1".equals(instanceType.getFamily())) {
                        instanceType.setGroup(31);
                    } else if ("c3".equals(instanceType.getFamily())) {
                        instanceType.setGroup(32);
                    } else if ("c4".equals(instanceType.getFamily())) {
                        instanceType.setGroup(33);
                    } else if ("c5".equals(instanceType.getFamily())) {
                        instanceType.setGroup(34);
                    } else if ("c5a".equals(instanceType.getFamily())) {
                        instanceType.setGroup(35);
                    } else if ("c5ad".equals(instanceType.getFamily())) {
                        instanceType.setGroup(36);
                    } else if ("c5d".equals(instanceType.getFamily())) {
                        instanceType.setGroup(37);
                    } else if ("c5n".equals(instanceType.getFamily())) {
                        instanceType.setGroup(38);
                    } else if ("c6g".equals(instanceType.getFamily())) {
                        instanceType.setGroup(39);
                    } else if ("c6gd".equals(instanceType.getFamily())) {
                        instanceType.setGroup(40);
                    } else if ("c6gn".equals(instanceType.getFamily())) {
                        instanceType.setGroup(41);
                    } else if ("cc2".equals(instanceType.getFamily())) {
                        instanceType.setGroup(42);
                    } else if ("d2".equals(instanceType.getFamily())) {
                        instanceType.setGroup(51);
                    } else if ("d3".equals(instanceType.getFamily())) {
                        instanceType.setGroup(52);
                    } else if ("d3en".equals(instanceType.getFamily())) {
                        instanceType.setGroup(53);
                    } else if ("f1".equals(instanceType.getFamily())) {
                        instanceType.setGroup(61);
                    } else if ("g2".equals(instanceType.getFamily())) {
                        instanceType.setGroup(71);
                    } else if ("g3".equals(instanceType.getFamily())) {
                        instanceType.setGroup(72);
                    } else if ("g3s".equals(instanceType.getFamily())) {
                        instanceType.setGroup(73);
                    } else if ("g4ad".equals(instanceType.getFamily())) {
                        instanceType.setGroup(74);
                    } else if ("g4dn".equals(instanceType.getFamily())) {
                        instanceType.setGroup(75);
                    } else if ("h1".equals(instanceType.getFamily())) {
                        instanceType.setGroup(81);
                    } else if ("i2".equals(instanceType.getFamily())) {
                        instanceType.setGroup(91);
                    } else if ("i3".equals(instanceType.getFamily())) {
                        instanceType.setGroup(92);
                    } else if ("i3en".equals(instanceType.getFamily())) {
                        instanceType.setGroup(93);
                    } else if ("inf1".equals(instanceType.getFamily())) {
                        instanceType.setGroup(94);
                    } else if ("m1".equals(instanceType.getFamily())) {
                        instanceType.setGroup(101);
                    } else if ("m2".equals(instanceType.getFamily())) {
                        instanceType.setGroup(102);
                    } else if ("m3".equals(instanceType.getFamily())) {
                        instanceType.setGroup(103);
                    } else if ("m4".equals(instanceType.getFamily())) {
                        instanceType.setGroup(104);
                    } else if ("m5".equals(instanceType.getFamily())) {
                        instanceType.setGroup(105);
                    } else if ("m5a".equals(instanceType.getFamily())) {
                        instanceType.setGroup(106);
                    } else if ("m5ad".equals(instanceType.getFamily())) {
                        instanceType.setGroup(107);
                    } else if ("m5d".equals(instanceType.getFamily())) {
                        instanceType.setGroup(108);
                    } else if ("m5dn".equals(instanceType.getFamily())) {
                        instanceType.setGroup(109);
                    } else if ("m5n".equals(instanceType.getFamily())) {
                        instanceType.setGroup(110);
                    } else if ("m5zn".equals(instanceType.getFamily())) {
                        instanceType.setGroup(111);
                    } else if ("m6g".equals(instanceType.getFamily())) {
                        instanceType.setGroup(112);
                    } else if ("m6gd".equals(instanceType.getFamily())) {
                        instanceType.setGroup(113);
                    } else if ("mac1".equals(instanceType.getFamily())) {
                        instanceType.setGroup(114);
                    } else if ("p2".equals(instanceType.getFamily())) {
                        instanceType.setGroup(201);
                    } else if ("p3".equals(instanceType.getFamily())) {
                        instanceType.setGroup(202);
                    } else if ("p3dn".equals(instanceType.getFamily())) {
                        instanceType.setGroup(203);
                    } else if ("p4d".equals(instanceType.getFamily())) {
                        instanceType.setGroup(204);
                    } else if ("r3".equals(instanceType.getFamily())) {
                        instanceType.setGroup(301);
                    } else if ("r4".equals(instanceType.getFamily())) {
                        instanceType.setGroup(302);
                    } else if ("r5".equals(instanceType.getFamily())) {
                        instanceType.setGroup(303);
                    } else if ("r5a".equals(instanceType.getFamily())) {
                        instanceType.setGroup(304);
                    } else if ("r5ad".equals(instanceType.getFamily())) {
                        instanceType.setGroup(305);
                    } else if ("r5b".equals(instanceType.getFamily())) {
                        instanceType.setGroup(306);
                    } else if ("r5d".equals(instanceType.getFamily())) {
                        instanceType.setGroup(307);
                    } else if ("r5dn".equals(instanceType.getFamily())) {
                        instanceType.setGroup(308);
                    } else if ("r5n".equals(instanceType.getFamily())) {
                        instanceType.setGroup(309);
                    } else if ("r6g".equals(instanceType.getFamily())) {
                        instanceType.setGroup(310);
                    } else if ("r6gd".equals(instanceType.getFamily())) {
                        instanceType.setGroup(311);
                    } else if ("x1".equals(instanceType.getFamily())) {
                        instanceType.setGroup(401);
                    } else if ("x1e".equals(instanceType.getFamily())) {
                        instanceType.setGroup(402);
                    } else if ("z1d".equals(instanceType.getFamily())) {
                        instanceType.setGroup(501);
                    } else {
                        instanceType.setGroup(999);
                    }
                    instanceTypeList.add(instanceType);
                }

                nextToken = describeInstanceTypeOfferingsResult.getNextToken();

                if (nextToken != null && !"".equals(nextToken)) {
                    describeInstanceTypeOfferingsRequest.setNextToken(nextToken);
                    describeInstanceTypeOfferingsResult = ec2.describeInstanceTypeOfferings(describeInstanceTypeOfferingsRequest);
                    continue;
                } else {
                    break;
                }
            }

            Collections.sort(instanceTypeList, new Comparator<io.playce.roro.mig.aws.model.InstanceType>() {
                @Override
                public int compare(io.playce.roro.mig.aws.model.InstanceType i1, io.playce.roro.mig.aws.model.InstanceType i2) {
                    if (i2.getGroup() == i1.getGroup()) {
                        if (i1.getVCPUs().equals(i2.getVCPUs())) {
                            return Double.parseDouble(i1.getMemory()) < Double.parseDouble(i2.getMemory()) ? -1 : 1;
                        }
                        return Integer.parseInt(i1.getVCPUs()) < Integer.parseInt(i2.getVCPUs()) ? -1 : 1;
                    } else {
                        return i1.getGroup() < i2.getGroup() ? -1 : 1;
                    }
                }

            });

            instanceTypeMap.put(availabilityZone, instanceTypeList);
        }

        return instanceTypeMap.get(availabilityZone);
    }

    /**
     * <pre>
     * vpc subnet에 매핑된 Availability Zone을 조회한다.
     * </pre>
     *
     * @param subnetId
     *
     * @return
     */
    public String getAvailabilityZone(String subnetId) {
        String availabilityZone = null;

        DescribeSubnetsResult result = ec2.describeSubnets(new DescribeSubnetsRequest().withSubnetIds(subnetId));
        List<Subnet> subnetList = result.getSubnets();

        if (subnetList != null && subnetList.size() > 0) {
            availabilityZone = subnetList.get(0).getAvailabilityZone();
        }

        return availabilityZone;
    }

    /**
     * <pre>
     * ec2-import-instance를 수행한다.
     * </pre>
     *
     * @param migration
     * @param volume
     *
     * @return
     */
    public MigrationProcessDto importInstance(MigrationProcessDto migration, MigrationProcessDto.Volume volume) {
        //File file = new File(volume.getRawFileName());

        ImportInstanceRequest request = new ImportInstanceRequest()
                .withPlatform("Linux")
                .withLaunchSpecification(new ImportInstanceLaunchSpecification()
                        .withSubnetId(migration.getSubnetId())
                        .withGroupIds(migration.getSecurityGroupIds())
                        .withPlacement(new Placement().withAvailabilityZone(getAvailabilityZone(migration.getSubnetId())))
                        .withInstanceInitiatedShutdownBehavior(ShutdownBehavior.Stop)
                        .withArchitecture(ArchitectureValues.fromValue("x86_64"))
                        .withInstanceType(InstanceType.fromValue(migration.getInstanceType())))
                .withDiskImages(new DiskImage()
                        .withImage(new DiskImageDetail()
                                .withFormat(DiskImageFormat.fromValue("RAW"))
                                //.withBytes(file.length())
                                .withBytes(volume.getRawFileSize())
                                .withImportManifestUrl(volume.getManifestUrl()))
                        .withVolume(new VolumeDetail()
                                .withSize(volume.getVolumeSize() + migration.getSwapSize())))
                .withDescription(migration.getDescription());

        ImportInstanceResult result = ec2.importInstance(request);

        migration.setInstanceId(result.getConversionTask().getImportInstance().getInstanceId());
        volume.setTaskId(result.getConversionTask().getConversionTaskId());

        return migration;
    }

    /**
     * <pre>
     * ec2-import-volume을 수행한다.
     * </pre>
     *
     * @param migration
     * @param volume
     *
     * @return
     */
    public MigrationProcessDto importVolume(MigrationProcessDto migration, MigrationProcessDto.Volume volume) {
        File file = new File(volume.getRawFileName());

        ImportVolumeRequest request = new ImportVolumeRequest()
                .withAvailabilityZone(getAvailabilityZone(migration.getSubnetId()))
                .withImage(new DiskImageDetail()
                        .withFormat(DiskImageFormat.fromValue("RAW"))
                        .withBytes(file.length())
                        //.withBytes(file.length())
                        .withBytes(volume.getRawFileSize())
                        .withImportManifestUrl(volume.getManifestUrl()))
                .withVolume(new VolumeDetail()
                        .withSize(volume.getVolumeSize()))
                .withDescription(migration.getDescription());

        ImportVolumeResult result = ec2.importVolume(request);

        volume.setTaskId(result.getConversionTask().getConversionTaskId());

        return migration;
    }

    /**
     * <pre>
     * taskId에 해당하는 ConversionTask를 조회한다.
     * </pre>
     *
     * @param taskId
     *
     * @return
     */
    public ConversionTask describeConversionTask(String taskId) {
        DescribeConversionTasksResult result = ec2.describeConversionTasks(new DescribeConversionTasksRequest().withConversionTaskIds(taskId));
        return result.getConversionTasks().get(0);
    }

    /**
     * <pre>
     * taskId에 해당하는 ConversionTask를 취소한다.
     * </pre>
     *
     * @param taskId
     */
    public void cancelConversionTask(String taskId) {
        ec2.cancelConversionTask(new CancelConversionTaskRequest().withConversionTaskId(taskId));
    }

    /**
     * <pre>
     * EBS를 생성한다.
     * </pre>
     *
     * @param migration
     */
    public void createVolumes(MigrationProcessDto migration, String zone) {
        List<MigrationProcessDto.Volume> volumeList = migration.getVolumes();

        for (MigrationProcessDto.Volume volume : volumeList) {
            if ("N".equals(volume.getRootYn())) {
                CreateVolumeResult result = ec2.createVolume(new CreateVolumeRequest()
                        .withAvailabilityZone(zone)
                        .withSize(volume.getVolumeSize().intValue())
                        .withVolumeType("gp3"));

                volume.setVolumeId(result.getVolume().getVolumeId());
            }
        }
    }

    /**
     * <pre>
     * EC2 인스턴스에 EBS를 추가한다.
     * </pre>
     *
     * @param migration
     */
    public void attachVolumes(MigrationProcessDto migration) {
        List<MigrationProcessDto.Volume> volumeList = migration.getVolumes();

        char device = 'b';
        for (MigrationProcessDto.Volume volume : volumeList) {
            if ("N".equals(volume.getRootYn())) {
                volume.setDeviceName("/dev/sd" + device++);

                ec2.attachVolume(new AttachVolumeRequest()
                        .withInstanceId(migration.getInstanceId())
                        .withVolumeId(volume.getVolumeId())
                        .withDevice(volume.getDeviceName()));
            }
        }
    }

    /**
     * <pre>
     * EC2 인스턴스로부터 AMI를 생성한다.
     * </pre>
     *
     * @param migration
     *
     * @return
     */
    public String createImage(MigrationProcessDto migration) {
        String imageName = migration.getInstanceName() + "_" + DATE_FORMAT.format(new Date());

        CreateImageRequest ciRequest = new CreateImageRequest()
                .withName(imageName)
                .withInstanceId(migration.getInstanceId())
                .withDescription("Auto Created AMI for " + migration.getInstanceName());

        //*
        ciRequest.withBlockDeviceMappings(createBlockDeviceMappingList(migration));
    	/*/
    	if (migration.getSummary().getFamily() != null &&
    			("redhat".equals(migration.getSummary().getFamily().toLowerCase()) ||
    			"debian".equals(migration.getSummary().getFamily().toLowerCase()))) {
    		ciRequest.withBlockDeviceMappings(createBlockDeviceMappingList(migration));
    	}
    	//*/

        ciRequest.setNoReboot(true);

        CreateImageResult ciResult = ec2.createImage(ciRequest);

        if (ciResult != null && ciResult.getImageId() != null) {
            createTag(ciResult.getImageId(), "Name", migration.getInstanceName());
        }

        migration.setImageId(ciResult.getImageId());
        migration.setImageName(imageName);

        return ciResult.getImageId();
    }

    /**
     * <pre>
     * AMI 상태를 조회한다.
     * </pre>
     *
     * @param imageId
     *
     * @return
     */
    public String getImageState(String imageId) {
        Image image = ec2.describeImages(new DescribeImagesRequest().withImageIds(imageId)).getImages().get(0);
        return image.getState();
    }

    /**
     * <pre>
     * EC2 인스턴스를 종료한다.
     * </pre>
     *
     * @param instanceId
     */
    public void terminateInstance(String instanceId) {
        ec2.terminateInstances(new TerminateInstancesRequest().withInstanceIds(instanceId));
    }

    /**
     * <pre>
     * EC2 인스턴스 정보를 조회한다.
     * </pre>
     *
     * @param instanceId
     *
     * @return
     */
    public Instance getInstance(String instanceId) {
        DescribeInstancesResult disResult = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId));

        if (disResult.getReservations().size() == 0) {
            return null;
        } else {
            return disResult.getReservations().get(0).getInstances().get(0);
        }
    }

    /**
     * <pre>
     * EC2 인스턴스 상태 정보를 조회한다.
     * </pre>
     *
     * @param instanceId
     *
     * @return
     */
    public String getInstanceState(String instanceId) {
        Instance instance = getInstance(instanceId);

        if (instance == null) {
            return null;
        } else {
            return getInstance(instanceId).getState().getName();
        }
    }

    /**
     * <pre>
     * EC2 인스턴스의 시스템 상태 및 인스턴스 상태가 ok인지 조회한다.
     * </pre>
     *
     * @param instanceId
     *
     * @return
     */
    public Boolean isStatucCheckOk(String instanceId) {
        DescribeInstanceStatusResult result = ec2.describeInstanceStatus(new DescribeInstanceStatusRequest().withInstanceIds(instanceId));

        String systemStatus = null;
        String instanceStatus = null;

        try {
            systemStatus = result.getInstanceStatuses().get(0).getSystemStatus().getStatus();
            instanceStatus = result.getInstanceStatuses().get(0).getInstanceStatus().getStatus();
        } catch (Exception e) {
            log.warn("Instance status check failed. instanceId : [{}], message : [{}]", instanceId, e.getMessage());

            // 인스턴스 생성 이후 상태 체크를 수행하며, IndexOutOfBoundsException이 발생하는 경우는 인스턴스가 중지 또는 종료된 상태로
            // 마이그레이션 완료로 처리를 한다.
            if (e instanceof IndexOutOfBoundsException) {
                return true;
            }
        }

        if ("ok".equals(systemStatus) && "ok".equals(instanceStatus)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <pre>
     * AMI로부터 EC2 인스턴스를 생성하고 실행한다.
     * </pre>
     *
     * @param migration
     * @param username
     *
     * @return
     */
    public String runInstances(MigrationProcessDto migration, String username, boolean passwordAuthentication) {
        RunInstancesRequest request = new RunInstancesRequest()
                .withImageId(migration.getImageId())
                .withMinCount(1)
                .withMaxCount(1)
                .withSubnetId(migration.getSubnetId())
                .withSecurityGroupIds(migration.getSecurityGroupIds())
                .withPlacement(new Placement().withAvailabilityZone(getAvailabilityZone(migration.getSubnetId())))
                .withInstanceInitiatedShutdownBehavior(ShutdownBehavior.Stop)
                .withPrivateIpAddress(StringUtils.isNullOrEmpty(migration.getPrivateIp()) ? null : migration.getPrivateIp())
                .withInstanceType(InstanceType.fromValue(migration.getInstanceType()));

        if (passwordAuthentication) {
            if ("root".equals(username)) {
                request.withUserData(Base64.getEncoder().encodeToString(USER_DATA_WITH_ROOT.getBytes()));
            } else {
                request.withUserData(Base64.getEncoder().encodeToString(USER_DATA_DEFAULT.replaceAll("\\{USERNAME\\}", username).getBytes()));
            }
        }

        RunInstancesResult result = ec2.runInstances(request);

        return result.getReservation().getInstances().get(0).getInstanceId();
    }

    /**
     * <pre>
     * AMI로부터 EC2 인스턴스를 생성하고 실행한다.
     * </pre>
     *
     * @param imageId                the image id
     * @param keyName                the key name
     * @param subnetId               the subnet id
     * @param sgId                   the sg id
     * @param instanceType           the instance type
     * @param privateIp              the private ip
     * @param blockDeviceMappingList the block device mapping list
     * @param userData               the user data
     * @param username               the username
     * @param passwordAuthentication the password authentication
     *
     * @return string string
     */
    public String runInstances(String imageId, String keyName, String subnetId, String[] sgId, String instanceType, String privateIp,
                               List<BlockDeviceMapping> blockDeviceMappingList, String userData, String username, boolean passwordAuthentication) {
        if ("".equals(keyName)) {
            keyName = null;
        }

        RunInstancesRequest request = new RunInstancesRequest()
                .withImageId(imageId)
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(keyName)
                .withSubnetId(subnetId)
                .withSecurityGroupIds(sgId)
                .withPlacement(new Placement().withAvailabilityZone(getAvailabilityZone(subnetId)))
                .withInstanceInitiatedShutdownBehavior(ShutdownBehavior.Stop)
                .withPrivateIpAddress(StringUtils.isNullOrEmpty(privateIp) ? null : privateIp)
                .withInstanceType(InstanceType.fromValue(instanceType));

        if (blockDeviceMappingList != null) {
            request.withBlockDeviceMappings(blockDeviceMappingList);
        }

        if (!StringUtils.isNullOrEmpty(userData)) {
            StringBuilder sb = new StringBuilder(userData);
            //boolean cloudConfig = userData.startsWith("#cloud-config");

            if (passwordAuthentication) {
                sb.append("\n");
                sb.append("# Set sshd config from RoRo\n");
                sb.append("sed -i -e 's/^PasswordAuthentication.*/PasswordAuthentication yes/' /etc/ssh/sshd_config\n");

                if ("root".equals(username)) {
                    sb.append("sed -i -e 's/^\\(#\\?\\)PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config\n");
                }

                sb.append("systemctl restart sshd\n");
                sb.append("passwd -u ").append(username);
            }

            request.withUserData(Base64.getEncoder().encodeToString(sb.toString().getBytes()));
        } else {
            if (passwordAuthentication) {
                if ("root".equals(username)) {
                    request.withUserData(Base64.getEncoder().encodeToString(USER_DATA_WITH_ROOT.getBytes()));
                } else {
                    request.withUserData(Base64.getEncoder().encodeToString(USER_DATA_DEFAULT.replaceAll("\\{USERNAME\\}", username).getBytes()));
                }
            }
        }

        RunInstancesResult result = ec2.runInstances(request);

        return result.getReservation().getInstances().get(0).getInstanceId();
    }

    /**
     * <pre>
     * EBS 볼륨을 삭제한다.
     * </pre>
     *
     * @param volumeId
     */
    public void deleteVolume(String volumeId) {
        ec2.deleteVolume(new DeleteVolumeRequest().withVolumeId(volumeId));
    }

    /**
     * <pre>
     * 사용 가능한 Elastic IP 조회한다. 사용 가능한 IP가 없을 경우 신규로 할당한다.
     * </pre>
     *
     * @return
     */
    public String getElasticIp() {
        String publicIp = null;

        try {
            DescribeAddressesResult addressResult = ec2.describeAddresses();

            for (Address address : addressResult.getAddresses()) {
                if (address.getAssociationId() == null && address.getPrivateIpAddress() == null) {
                    publicIp = address.getPublicIp();
                    break;
                }
            }

            if (publicIp == null) {
                publicIp = ec2.allocateAddress().getPublicIp();
            }
        } catch (Exception e) {
            log.error("Elastic IP associate failed.", e);
        }

        return publicIp;
    }

    /**
     * <pre>
     * Elastic IP를 인스턴스에 추가한다.
     * </pre>
     *
     * @param instanceId
     * @param publicIp
     */
    public void associateAddress(String instanceId, String publicIp) {
        ec2.associateAddress(new AssociateAddressRequest()
                .withInstanceId(instanceId)
                .withPublicIp(publicIp));
    }

    /**
     * <pre>
     * 주어진 리소스 ID에 태그를 추가한다.
     * </pre>
     *
     * @param resourceId
     * @param key
     * @param value
     */
    public void createTag(String resourceId, String key, String value) {
        ec2.createTags(new CreateTagsRequest()
                .withResources(resourceId)
                .withTags(new Tag().withKey(key).withValue(value)));
    }

    /**
     * <pre>
     * AMI 생성 시 필요한 정보로 EC2 인스턴스로부터 블록 디바이스 정보 목록을 조회하여 새로운 블록 디바이스 목록을 생성한다.
     * </pre>
     *
     * @param migration
     *
     * @return
     */
    public List<BlockDeviceMapping> createBlockDeviceMappingList(MigrationProcessDto migration) {
        List<MigrationProcessDto.Volume> volumeList = migration.getVolumes();

        DescribeInstancesResult diResult = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(migration.getInstanceId()));
        List<InstanceBlockDeviceMapping> ibdMappingList = diResult.getReservations().get(0).getInstances().get(0).getBlockDeviceMappings();

        List<BlockDeviceMapping> bdMappingList = new ArrayList<BlockDeviceMapping>();
        for (InstanceBlockDeviceMapping ibdMapping : ibdMappingList) {
            for (MigrationProcessDto.Volume volume : volumeList) {
                if (ibdMapping.getEbs().getVolumeId().equals(volume.getVolumeId())) {
                    bdMappingList.add(convert(ibdMapping));
                }
            }
        }

        return bdMappingList;
    }

    /**
     * <pre>
     * 인스턴스 블록 디바이스 정보를 블록 디아비스로 변환한다.
     * </pre>
     *
     * @param mapping
     *
     * @return
     */
    public BlockDeviceMapping convert(InstanceBlockDeviceMapping mapping) {
        BlockDeviceMapping converted = new BlockDeviceMapping();
        converted.setDeviceName(mapping.getDeviceName());

        EbsBlockDevice ebs = new EbsBlockDevice();
        ebs.setDeleteOnTermination(true);
        ebs.setVolumeType(VolumeType.Gp3);
        converted.setEbs(ebs);

        return converted;
    }

    /**
     * <pre>
     * 지역 내 AvailabilityZone 목록을 조회한다.
     * </pre>
     *
     * @return
     */
    public List<String> getAvailabilityZoneList() {
        DescribeAvailabilityZonesResult result = ec2.describeAvailabilityZones(new DescribeAvailabilityZonesRequest()
                .withFilters(new Filter().withName("state").withValues("available")));

        List<String> azList = new ArrayList<String>();
        for (AvailabilityZone az : result.getAvailabilityZones()) {
            azList.add(az.getZoneName());
        }

        return azList;
    }

    /**
     * <pre>
     * VPC 목록을 조회한다.
     * </pre>
     *
     * @return
     */
    public List<VpcDetail> getVPCList(VpcDetail detail) {
        DescribeVpcsResult result = ec2.describeVpcs();

        List<VpcDetail> vpcList = new ArrayList<VpcDetail>();
        VpcDetail vpcDetail = null;
        String vpcName = null;
        String search = detail.getSearch();
        for (Vpc vpc : result.getVpcs()) {
            if (search != null) {
                vpcName = "";
                for (Tag tag : vpc.getTags()) {
                    if (tag.getKey().equals("Name")) {
                        vpcName = tag.getValue();
                        break;
                    }
                }

                // vpcId, vpcName, cidrBlock, state 에서 like 검색
                if (vpc.getVpcId().indexOf(search) > -1 ||
                        vpcName.indexOf(search) > -1 ||
                        vpc.getCidrBlock().indexOf(search) > -1 ||
                        vpc.getState().indexOf(search) > -1) {
                    vpcDetail = new VpcDetail();
                    vpcDetail.setVpcId(vpc.getVpcId());
                    vpcDetail.setVpcName(vpcName);
                    vpcDetail.setState(vpc.getState());
                    vpcDetail.setVpcCidr(vpc.getCidrBlock());
                    vpcDetail.setDnsResolution(ec2.describeVpcAttribute(new DescribeVpcAttributeRequest().withVpcId(vpc.getVpcId()).withAttribute("enableDnsSupport")).getEnableDnsSupport());
                    vpcDetail.setDnsHostnames(ec2.describeVpcAttribute(new DescribeVpcAttributeRequest().withVpcId(vpc.getVpcId()).withAttribute("enableDnsHostnames")).getEnableDnsHostnames());

                    vpcList.add(vpcDetail);
                }

            } else {
                vpcDetail = new VpcDetail();
                vpcDetail.setVpcId(vpc.getVpcId());
                vpcDetail.setState(vpc.getState());
                vpcDetail.setVpcCidr(vpc.getCidrBlock());
                vpcDetail.setDnsResolution(ec2.describeVpcAttribute(new DescribeVpcAttributeRequest().withVpcId(vpc.getVpcId()).withAttribute("enableDnsSupport")).getEnableDnsSupport());
                vpcDetail.setDnsHostnames(ec2.describeVpcAttribute(new DescribeVpcAttributeRequest().withVpcId(vpc.getVpcId()).withAttribute("enableDnsHostnames")).getEnableDnsHostnames());

                for (Tag tag : vpc.getTags()) {
                    if (tag.getKey().equals("Name")) {
                        vpcDetail.setVpcName(tag.getValue());
                    }
                }

                vpcList.add(vpcDetail);
            }
        }

        return vpcList;
    }

    /**
     * <pre>
     * VPC를 생성한다.
     * </pre>
     *
     * @param vpcDetail
     */
    public void createVpc(VpcDetail vpcDetail) {
        String vpcId = null;
        String igwId = null;
        String routeTableId = null;

        vpcId = ec2.createVpc(new CreateVpcRequest()
                .withCidrBlock(vpcDetail.getVpcCidr())).getVpc().getVpcId();

        createTag(vpcId, "Name", vpcDetail.getVpcName());

        // Internet Gateway 생성 후 Attach
        igwId = ec2.createInternetGateway().getInternetGateway().getInternetGatewayId();

        ec2.attachInternetGateway(new AttachInternetGatewayRequest()
                .withVpcId(vpcId)
                .withInternetGatewayId(igwId));

        createTag(igwId, "Name", "igw-" + vpcDetail.getVpcName());

        // Route Table에 igw 추가
        routeTableId = ec2.describeRouteTables(new DescribeRouteTablesRequest()
                        .withFilters(new Filter().withName("vpc-id").withValues(vpcId))
                        .withFilters(new Filter().withName("association.main").withValues("true")))
                .getRouteTables().get(0).getRouteTableId();

        ec2.createRoute(new CreateRouteRequest()
                .withRouteTableId(routeTableId)
                .withDestinationCidrBlock("0.0.0.0/0")
                .withGatewayId(igwId));

        // VPC DNS Resolution, DNS hostnames 옵션 변경
        ec2.modifyVpcAttribute(new ModifyVpcAttributeRequest()
                .withVpcId(vpcId)
                .withEnableDnsSupport(vpcDetail.getDnsResolution()));

        ec2.modifyVpcAttribute(new ModifyVpcAttributeRequest()
                .withVpcId(vpcId)
                .withEnableDnsHostnames(vpcDetail.getDnsHostnames()));
    }

    /**
     * <pre>
     * VPC를 수정한다.
     * </pre>
     *
     * @param vpcDetail
     */
    public void updateVpc(VpcDetail vpcDetail) {
        // VPC 이름 변경
        createTag(vpcDetail.getVpcId(), "Name", vpcDetail.getVpcName());

        // VPC DNS Resolution, DNS hostnames 옵션 변경
        ec2.modifyVpcAttribute(new ModifyVpcAttributeRequest()
                .withVpcId(vpcDetail.getVpcId())
                .withEnableDnsSupport(vpcDetail.getDnsResolution()));

        ec2.modifyVpcAttribute(new ModifyVpcAttributeRequest()
                .withVpcId(vpcDetail.getVpcId())
                .withEnableDnsHostnames(vpcDetail.getDnsHostnames()));
    }

    /**
     * <pre>
     * VPC를 삭제한다.
     * </pre>
     *
     * @param vpcId the vpc id
     *
     * @throws Exception the exception
     */
    public void deleteVpc(String vpcId) throws Exception {
        DescribeSubnetsResult result = ec2.describeSubnets(new DescribeSubnetsRequest()
                .withFilters(new Filter().withName("vpc-id").withValues(vpcId)));

        if (result != null && result.getSubnets().size() > 0) {
            throw new Exception("This VPC contains one or more subnets, and cannot be deleted until those subnets have been deleted");
        }

        String igwId = getInternetGatewayId(vpcId);

        if (igwId != null) {
            detachInternetGateway(igwId, vpcId);
            deleteInternetGateway(igwId);
        }

        ec2.deleteVpc(new DeleteVpcRequest().withVpcId(vpcId));
    }

    /**
     * <pre>
     * VPC내의 subnet 목록을 조회한다.
     * </pre>
     *
     * @param detail the detail
     *
     * @return subnet list
     */
    public List<SubnetDetail> getSubnetList(SubnetDetail detail) {
        // DescribeSubnetsRequest request = new DescribeSubnetsRequest();
        //
        // if (detail.getVpcId() != null) {
        // 	request.withFilters(new Filter().withName("vpc-id").withValues(detail.getVpcId()));
        // }

        DescribeSubnetsResult result = ec2.describeSubnets();

        List<SubnetDetail> subnetList = new ArrayList<SubnetDetail>();
        List<Vpc> vpcList = ec2.describeVpcs().getVpcs();

        SubnetDetail subnetDetail = null;
        String vpcName = null;
        String subnetName = null;
        String search = detail.getSearch();
        for (Subnet subnet : result.getSubnets()) {
            if (search != null) {
                vpcName = "";
                for (Vpc vpc : vpcList) {
                    if (vpc.getVpcId().equals(subnet.getVpcId())) {
                        for (Tag tag : vpc.getTags()) {
                            if (tag.getKey().equals("Name")) {
                                vpcName = tag.getValue();
                                break;
                            }
                        }
                    }

                    if (!"".equals(vpcName)) {
                        break;
                    }
                }

                subnetName = "";
                for (Tag tag : subnet.getTags()) {
                    if (tag.getKey().equals("Name")) {
                        subnetName = tag.getValue();
                        break;
                    }
                }

                // vpcId, vpcName, subnetId, subnetName, subnetCidr, state 에서 like 검색
                if (subnet.getVpcId().indexOf(search) > -1 ||
                        vpcName.indexOf(search) > -1 ||
                        subnet.getSubnetId().indexOf(search) > -1 ||
                        subnetName.indexOf(search) > -1 ||
                        subnet.getCidrBlock().indexOf(search) > -1 ||
                        subnet.getState().indexOf(search) > -1) {
                    subnetDetail = new SubnetDetail();
                    subnetDetail.setVpcId(subnet.getVpcId());
                    subnetDetail.setVpcName(vpcName);
                    subnetDetail.setSubnetId(subnet.getSubnetId());
                    subnetDetail.setSubnetName(subnetName);
                    subnetDetail.setState(subnet.getState());
                    subnetDetail.setSubnetCidr(subnet.getCidrBlock());
                    subnetDetail.setAvailableIPs(subnet.getAvailableIpAddressCount());
                    subnetDetail.setAvailabilityZone(subnet.getAvailabilityZone());
                    subnetDetail.setAutoAssignPublicIP(subnet.getMapPublicIpOnLaunch());

                    subnetList.add(subnetDetail);
                }

            } else {
                subnetDetail = new SubnetDetail();
                subnetDetail.setVpcId(subnet.getVpcId());
                subnetDetail.setSubnetId(subnet.getSubnetId());
                subnetDetail.setState(subnet.getState());
                subnetDetail.setSubnetCidr(subnet.getCidrBlock());
                subnetDetail.setAvailableIPs(subnet.getAvailableIpAddressCount());
                subnetDetail.setAvailabilityZone(subnet.getAvailabilityZone());
                subnetDetail.setAutoAssignPublicIP(subnet.getMapPublicIpOnLaunch());

                for (Tag tag : subnet.getTags()) {
                    if (tag.getKey().equals("Name")) {
                        subnetDetail.setSubnetName(tag.getValue());
                        break;
                    }
                }

                vpcName = null;
                for (Vpc vpc : vpcList) {
                    if (vpc.getVpcId().equals(subnet.getVpcId())) {
                        for (Tag tag : vpc.getTags()) {
                            if (tag.getKey().equals("Name")) {
                                vpcName = tag.getValue();
                                break;
                            }
                        }
                    }

                    if (vpcName != null) {
                        break;
                    }
                }
                subnetDetail.setVpcName(vpcName);

                subnetList.add(subnetDetail);
            }
        }

        return subnetList;
    }

    /**
     * <pre>
     * Subnet을 추가한다.
     * </pre>
     *
     * @param subnetDetail
     */
    public void createSubnet(SubnetDetail subnetDetail) {
        CreateSubnetResult result = ec2.createSubnet(new CreateSubnetRequest()
                .withVpcId(subnetDetail.getVpcId())
                .withCidrBlock(subnetDetail.getSubnetCidr())
                .withAvailabilityZone(subnetDetail.getAvailabilityZone()));

        subnetDetail.setSubnetId(result.getSubnet().getSubnetId());

        subnetDetail.setAutoAssignPublicIP(subnetDetail.getAutoAssignPublicIP());

        updateSubnet(subnetDetail);
    }

    /**
     * <pre>
     * Subnet을 수정한다.
     * </pre>
     *
     * @param subnetDetail
     */
    public void updateSubnet(SubnetDetail subnetDetail) {
        ec2.modifySubnetAttribute(new ModifySubnetAttributeRequest()
                .withSubnetId(subnetDetail.getSubnetId())
                .withMapPublicIpOnLaunch(subnetDetail.getAutoAssignPublicIP()));

        createTag(subnetDetail.getSubnetId(), "Name", subnetDetail.getSubnetName());
    }

    /**
     * <pre>
     * Subnet을 삭제한다.
     * </pre>
     *
     * @param subnetId the subnet id
     */
    public void deleteSubnet(String subnetId) {
        ec2.deleteSubnet(new DeleteSubnetRequest()
                .withSubnetId(subnetId));
    }

    /**
     * <pre>
     * VPC내의 Security Group 목록을 조회한다.
     * </pre>
     *
     * @param detail the detail
     *
     * @return security group list
     */
    public List<SecurityGroupDetail> getSecurityGroupList(SecurityGroupDetail detail) {
        // DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
        //
        // if (detail.getVpcId() != null) {
        // 	request.withFilters(new Filter().withName("vpc-id").withValues(detail.getVpcId()));
        // }

        DescribeSecurityGroupsResult result = ec2.describeSecurityGroups();
        List<Vpc> vpcList = ec2.describeVpcs().getVpcs();

        List<SecurityGroupDetail> sgList = new ArrayList<SecurityGroupDetail>();
        SecurityGroupDetail sgDetail = null;
        Permission permission = null;
        String vpcName = null;
        String nameTag = null;
        String search = detail.getSearch();
        for (SecurityGroup sg : result.getSecurityGroups()) {
            if (search != null) {
                vpcName = "";
                for (Vpc vpc : vpcList) {
                    if (vpc.getVpcId().equals(sg.getVpcId())) {
                        for (Tag tag : vpc.getTags()) {
                            if (tag.getKey().equals("Name")) {
                                vpcName = tag.getValue();
                                break;
                            }
                        }
                    }

                    if (!"".equals(vpcName)) {
                        break;
                    }
                }

                nameTag = "";
                for (Tag tag : sg.getTags()) {
                    if (tag.getKey().equals("Name")) {
                        nameTag = tag.getValue();
                        break;
                    }
                }

                // vpcId, vpcName, groupId, groupName, nameTag, description 에서 like 검색
                if (sg.getVpcId().indexOf(search) > -1 ||
                        vpcName.indexOf(search) > -1 ||
                        sg.getGroupId().indexOf(search) > -1 ||
                        sg.getGroupName().indexOf(search) > -1 ||
                        nameTag.indexOf(search) > -1 ||
                        sg.getDescription().indexOf(search) > -1) {
                    sgDetail = new SecurityGroupDetail();
                    sgDetail.setVpcId(sg.getVpcId());
                    sgDetail.setVpcName(vpcName);
                    sgDetail.setGroupId(sg.getGroupId());
                    sgDetail.setGroupName(sg.getGroupName());
                    sgDetail.setNameTag(nameTag);
                    sgDetail.setDescription(sg.getDescription());

                    for (IpPermission p : sg.getIpPermissions()) {
                        for (IpRange range : p.getIpv4Ranges()) {
                            permission = new Permission();

                            permission.setGroupId(sg.getGroupId());
                            if (p.getIpProtocol().equals("-1")) {
                                permission.setProtocol("ALL");
                            } else {
                                permission.setProtocol(p.getIpProtocol().toUpperCase());
                            }
                            permission.setFromPort(p.getFromPort());
                            permission.setToPort(p.getToPort());

                            permission.setSource(range.getCidrIp());
                            permission.setDescription(range.getDescription());

                            sgDetail.getPermissions().add(permission);
                        }

                        /**
                         * Ipv6에 대한 Rule Permission은 제외한다.
                         *
                         for (Ipv6Range range : p.getIpv6Ranges()) {
                         permission = new Permission();

                         permission.setGroupId(sg.getGroupId());
                         if (p.getIpProtocol().equals("-1")) {
                         permission.setProtocol("ALL");
                         } else {
                         permission.setProtocol(p.getIpProtocol().toUpperCase());
                         }
                         permission.setFromPort(p.getFromPort());
                         permission.setToPort(p.getToPort());

                         permission.setSource(range.getCidrIpv6());
                         permission.setDescription(range.getDescription());

                         sgDetail.getPermissions().add(permission);
                         }
                         */

                        for (UserIdGroupPair pair : p.getUserIdGroupPairs()) {
                            permission = new Permission();

                            permission.setGroupId(sg.getGroupId());
                            if (p.getIpProtocol().equals("-1")) {
                                permission.setProtocol("ALL");
                            } else {
                                permission.setProtocol(p.getIpProtocol().toUpperCase());
                            }
                            permission.setFromPort(p.getFromPort());
                            permission.setToPort(p.getToPort());

                            permission.setSource(pair.getGroupId());
                            permission.setDescription(pair.getDescription());

                            sgDetail.getPermissions().add(permission);
                        }
                    }

                    sgList.add(sgDetail);
                }
            } else {
                sgDetail = new SecurityGroupDetail();
                sgDetail.setVpcId(sg.getVpcId());
                sgDetail.setGroupId(sg.getGroupId());
                sgDetail.setGroupName(sg.getGroupName());
                sgDetail.setDescription(sg.getDescription());

                for (IpPermission p : sg.getIpPermissions()) {
                    for (IpRange range : p.getIpv4Ranges()) {
                        permission = new Permission();

                        permission.setGroupId(sg.getGroupId());
                        if (p.getIpProtocol().equals("-1")) {
                            permission.setProtocol("ALL");
                        } else {
                            permission.setProtocol(p.getIpProtocol().toUpperCase());
                        }
                        permission.setFromPort(p.getFromPort());
                        permission.setToPort(p.getToPort());

                        permission.setSource(range.getCidrIp());
                        permission.setDescription(range.getDescription());

                        sgDetail.getPermissions().add(permission);
                    }

                    /**
                     * Ipv6에 대한 Rule Permission은 제외한다.
                     *
                     for (Ipv6Range range : p.getIpv6Ranges()) {
                     permission = new Permission();

                     permission.setGroupId(sg.getGroupId());
                     if (p.getIpProtocol().equals("-1")) {
                     permission.setProtocol("ALL");
                     } else {
                     permission.setProtocol(p.getIpProtocol().toUpperCase());
                     }
                     permission.setFromPort(p.getFromPort());
                     permission.setToPort(p.getToPort());

                     permission.setSource(range.getCidrIpv6());
                     permission.setDescription(range.getDescription());

                     sgDetail.getPermissions().add(permission);
                     }
                     */

                    for (UserIdGroupPair pair : p.getUserIdGroupPairs()) {
                        permission = new Permission();

                        permission.setGroupId(sg.getGroupId());
                        if (p.getIpProtocol().equals("-1")) {
                            permission.setProtocol("ALL");
                        } else {
                            permission.setProtocol(p.getIpProtocol().toUpperCase());
                        }
                        permission.setFromPort(p.getFromPort());
                        permission.setToPort(p.getToPort());

                        permission.setSource(pair.getGroupId());
                        permission.setDescription(pair.getDescription());

                        sgDetail.getPermissions().add(permission);
                    }
                }

                vpcName = null;
                for (Vpc vpc : vpcList) {
                    if (vpc.getVpcId().equals(sg.getVpcId())) {
                        for (Tag tag : vpc.getTags()) {
                            if (tag.getKey().equals("Name")) {
                                vpcName = tag.getValue();
                                break;
                            }
                        }
                    }

                    if (vpcName != null) {
                        break;
                    }
                }
                sgDetail.setVpcName(vpcName);

                nameTag = null;
                for (Tag tag : sg.getTags()) {
                    if (tag.getKey().equals("Name")) {
                        sgDetail.setNameTag(tag.getValue());
                        break;
                    }
                }

                sgList.add(sgDetail);
            }
        }

        return sgList;
    }

    /**
     * <pre>
     * Security Group을 추가한다.
     * </pre>
     *
     * @param groupDetail
     *
     * @return
     */
    public String createSecurityGroup(SecurityGroupDetail groupDetail) {
        CreateSecurityGroupResult result = ec2.createSecurityGroup(new CreateSecurityGroupRequest()
                .withVpcId(groupDetail.getVpcId())
                .withGroupName(groupDetail.getGroupName())
                .withDescription(groupDetail.getDescription()));

        createTag(result.getGroupId(), "Name", groupDetail.getGroupName());

        return result.getGroupId();
    }

    /**
     * <pre>
     * Security Group을 삭제한다.
     * </pre>
     *
     * @param groupId the group id
     */
    public void deleteSecurityGroup(String groupId) {
        ec2.deleteSecurityGroup(new DeleteSecurityGroupRequest()
                .withGroupId(groupId));
    }

    /**
     * <pre>
     * Security Group내의 rule 목록을 조회한다.
     * </pre>
     *
     * @param groupId
     *
     * @return
     */
    public List<Permission> getPermissionList(String groupId) {
        DescribeSecurityGroupsResult result = ec2.describeSecurityGroups(new DescribeSecurityGroupsRequest()
                .withGroupIds(groupId));

        List<Permission> permissionList = null;
        Permission permission = null;
        if (result.getSecurityGroups().size() == 1) {
            permissionList = new ArrayList<Permission>();
            SecurityGroup sg = result.getSecurityGroups().get(0);

            for (IpPermission p : sg.getIpPermissions()) {
                for (IpRange range : p.getIpv4Ranges()) {
                    permission = new Permission();

                    permission.setGroupId(sg.getGroupId());
                    if (p.getIpProtocol().equals("-1")) {
                        permission.setProtocol("ALL");
                    } else {
                        permission.setProtocol(p.getIpProtocol().toUpperCase());
                    }
                    permission.setFromPort(p.getFromPort());
                    permission.setToPort(p.getToPort());

                    permission.setSource(range.getCidrIp());
                    permission.setDescription(range.getDescription());

                    permissionList.add(permission);
                }

                //*
                // Ipv6에 대한 Rule Permission은 제외한다.
                for (Ipv6Range range : p.getIpv6Ranges()) {
                    permission = new Permission();

                    permission.setGroupId(sg.getGroupId());
                    if (p.getIpProtocol().equals("-1")) {
                        permission.setProtocol("ALL");
                    } else {
                        permission.setProtocol(p.getIpProtocol().toUpperCase());
                    }
                    permission.setFromPort(p.getFromPort());
                    permission.setToPort(p.getToPort());

                    permission.setSource(range.getCidrIpv6());
                    permission.setDescription(range.getDescription());

                    permissionList.add(permission);
                }
                //*/

                for (UserIdGroupPair pair : p.getUserIdGroupPairs()) {
                    permission = new Permission();

                    permission.setGroupId(sg.getGroupId());
                    if (p.getIpProtocol().equals("-1")) {
                        permission.setProtocol("ALL");
                    } else {
                        permission.setProtocol(p.getIpProtocol().toUpperCase());
                    }
                    permission.setFromPort(p.getFromPort());
                    permission.setToPort(p.getToPort());

                    permission.setSource(pair.getGroupId());
                    permission.setDescription(pair.getDescription());

                    permissionList.add(permission);
                }
            }
        }

        return permissionList;
    }

    /**
     * <pre>
     * Security Group내의 rule을 추가한다.
     * </pre>
     *
     * @param groupId
     * @param permissions
     * @param rollback
     */
    public void createPermissions(String groupId, List<Permission> permissions, boolean rollback) {
        List<Permission> permissionList = null;

        if (!rollback) {
            // 1. 기존 Permission 목록을 조회한다.
            permissionList = getPermissionList(groupId);

            // 2. 기존 Permission 목록을 삭제한다.
            if (permissionList.size() > 0) {
                deletePermissions(groupId, permissionList);
            }
        }

        // Inbound Rule이 하나도 없는 경우 별도의 처리없이 리턴한다.
        if (permissions.size() == 0) {
            return;
        }

        if (permissions.size() == 1) {
            Permission permission = permissions.get(0);

            if (StringUtils.isNullOrEmpty(permission.getProtocol()) &&
                    StringUtils.isNullOrEmpty(permission.getSource()) &&
                    permission.getFromPort() == null &&
                    permission.getToPort() == null) {
                return;
            }
        }

        // 3. 신규 Permission 목록을 추가한다.
        AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest()
                .withGroupId(groupId);

        List<IpPermission> ipPermissionList = new ArrayList<IpPermission>();

        for (Permission permission : permissions) {
            IpPermission ipPermission = new IpPermission();
            ipPermission.setIpProtocol(permission.getProtocol());
            ipPermission.setFromPort(permission.getFromPort());
            ipPermission.setToPort(permission.getToPort());

            if (permission.getSource().startsWith("sg")) {
                List<UserIdGroupPair> pairList = new ArrayList<UserIdGroupPair>();
                UserIdGroupPair pair = new UserIdGroupPair();
                pair.setGroupId(permission.getSource());
                pair.setDescription(permission.getDescription());
                pairList.add(pair);

                ipPermission.setUserIdGroupPairs(pairList);
            } else {
                if (permission.getSource().contains(":")) {
                    List<Ipv6Range> rangeList = new ArrayList<>();
                    Ipv6Range range = new Ipv6Range();
                    range.setCidrIpv6(permission.getSource());
                    range.setDescription(permission.getDescription());
                    rangeList.add(range);

                    ipPermission.setIpv6Ranges(rangeList);
                } else {
                    List<IpRange> rangeList = new ArrayList<>();
                    IpRange range = new IpRange();
                    range.setCidrIp(permission.getSource());
                    range.setDescription(permission.getDescription());
                    rangeList.add(range);

                    ipPermission.setIpv4Ranges(rangeList);
                }
            }

            ipPermissionList.add(ipPermission);
        }

        request.withIpPermissions(ipPermissionList);

        try {
            ec2.authorizeSecurityGroupIngress(request);
        } catch (Exception e) {
            // 신규 Permission 목록 추가 시 에러가 발생하면 앞서 삭제한 기존 Permission 목록을 추가 후 예외를 발생한다.
            if (!rollback) {
                createPermissions(groupId, permissionList, true);

                throw e;
            }
        }
    }

    /**
     * <pre>
     * Security Group내의 rule을 추가한다.
     * </pre>
     *
     * @param permission
     */
    public void createPermission(Permission permission) {
        AuthorizeSecurityGroupIngressRequest request = new AuthorizeSecurityGroupIngressRequest()
                .withGroupId(permission.getGroupId());

        IpPermission ipPermission = new IpPermission();
        ipPermission.setIpProtocol(permission.getProtocol());
        ipPermission.setFromPort(permission.getFromPort());
        ipPermission.setToPort(permission.getToPort());

        if (permission.getSource().startsWith("sg")) {
            List<UserIdGroupPair> pairList = new ArrayList<UserIdGroupPair>();
            UserIdGroupPair pair = new UserIdGroupPair();
            pair.setGroupId(permission.getSource());
            pair.setDescription(permission.getDescription());
            pairList.add(pair);

            ipPermission.setUserIdGroupPairs(pairList);
        } else {
            if (permission.getSource().contains(":")) {
                List<Ipv6Range> rangeList = new ArrayList<>();
                Ipv6Range range = new Ipv6Range();
                range.setCidrIpv6(permission.getSource());
                range.setDescription(permission.getDescription());
                rangeList.add(range);

                ipPermission.setIpv6Ranges(rangeList);
            } else {
                List<IpRange> rangeList = new ArrayList<>();
                IpRange range = new IpRange();
                range.setCidrIp(permission.getSource());
                range.setDescription(permission.getDescription());
                rangeList.add(range);

                ipPermission.setIpv4Ranges(rangeList);
            }
        }

        request.withIpPermissions(ipPermission);

        ec2.authorizeSecurityGroupIngress(request);
    }

    /**
     * <pre>
     * Security Group내의 rule을 삭제한다.
     * </pre>
     *
     * @param groupId     the group id
     * @param permissions the permissions
     */
    public void deletePermissions(String groupId, List<Permission> permissions) {
        RevokeSecurityGroupIngressRequest request = new RevokeSecurityGroupIngressRequest()
                .withGroupId(groupId);

        List<IpPermission> ipPermissionList = new ArrayList<IpPermission>();

        for (Permission permission : permissions) {
            IpPermission ipPermission = new IpPermission();
            ipPermission.setIpProtocol(permission.getProtocol());
            ipPermission.setFromPort(permission.getFromPort());
            ipPermission.setToPort(permission.getToPort());

            if (permission.getSource().startsWith("sg")) {
                List<UserIdGroupPair> pairList = new ArrayList<UserIdGroupPair>();
                UserIdGroupPair pair = new UserIdGroupPair();
                pair.setGroupId(permission.getSource());
                pairList.add(pair);

                ipPermission.setUserIdGroupPairs(pairList);
            } else {
                if (permission.getSource().contains(":")) {
                    List<Ipv6Range> rangeList = new ArrayList<>();
                    Ipv6Range range = new Ipv6Range();
                    range.setCidrIpv6(permission.getSource());
                    rangeList.add(range);

                    ipPermission.setIpv6Ranges(rangeList);
                } else {
                    List<IpRange> rangeList = new ArrayList<>();
                    IpRange range = new IpRange();
                    range.setCidrIp(permission.getSource());
                    rangeList.add(range);

                    ipPermission.setIpv4Ranges(rangeList);
                }
            }

            ipPermissionList.add(ipPermission);
        }

        request.withIpPermissions(ipPermissionList);

        ec2.revokeSecurityGroupIngress(request);
    }

    /**
     * <pre>
     * Security Group내의 rule을 삭제한다.
     * </pre>
     *
     * @param permission
     */
    public void deletePermission(Permission permission) {
        RevokeSecurityGroupIngressRequest request = new RevokeSecurityGroupIngressRequest()
                .withGroupId(permission.getGroupId());

        IpPermission ipPermission = new IpPermission();
        ipPermission.setIpProtocol(permission.getProtocol());
        ipPermission.setFromPort(permission.getFromPort());
        ipPermission.setToPort(permission.getToPort());

        if (permission.getSource().startsWith("sg")) {
            List<UserIdGroupPair> pairList = new ArrayList<UserIdGroupPair>();
            UserIdGroupPair pair = new UserIdGroupPair();
            pair.setGroupId(permission.getSource());
            pairList.add(pair);

            ipPermission.setUserIdGroupPairs(pairList);
        } else {
            if (permission.getSource().contains(":")) {
                List<Ipv6Range> rangeList = new ArrayList<>();
                Ipv6Range range = new Ipv6Range();
                range.setCidrIpv6(permission.getSource());
                rangeList.add(range);

                ipPermission.setIpv6Ranges(rangeList);
            } else {
                List<IpRange> rangeList = new ArrayList<>();
                IpRange range = new IpRange();
                range.setCidrIp(permission.getSource());
                rangeList.add(range);

                ipPermission.setIpv4Ranges(rangeList);
            }
        }

        request.withIpPermissions(ipPermission);

        ec2.revokeSecurityGroupIngress(request);
    }

    /**
     * <pre>
     * 인스턴스에 매핑된 Security Group을 수정한다.
     * </pre>
     *
     * @param instanceId
     * @param groups
     */
    public void modifyInstanceAttribute(String instanceId, List<String> groups) {
        ec2.modifyInstanceAttribute(new ModifyInstanceAttributeRequest()
                .withInstanceId(instanceId)
                .withGroups(groups));
    }

    /**
     * <pre>
     * 인스턴스에 매핑된 Security Group 목록을 조회한다.
     * </pre>
     *
     * @param instanceId
     *
     * @return
     */
    public List<String> getSecurityGroupIdList(String instanceId) {
        DescribeInstancesResult disResult = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId));

        List<GroupIdentifier> gidList = disResult.getReservations().get(0).getInstances().get(0).getSecurityGroups();
        List<String> sgIdList = new ArrayList<String>();
        for (GroupIdentifier id : gidList) {
            sgIdList.add(id.getGroupId());
        }

        return sgIdList;
    }

    /**
     * <pre>
     * 주어진 keyName으로 AWS KeyPair를 생성하고 private key를 keyFile에 저장한다.
     * </pre>
     *
     * @param keyName
     * @param keyFile
     *
     * @return
     *
     * @throws IOException
     */
    public String createKeyPair(String keyName, File keyFile) throws IOException {
        CreateKeyPairResult result = ec2.createKeyPair(new CreateKeyPairRequest().withKeyName(keyName));
        FileUtils.writeStringToFile(keyFile, result.getKeyPair().getKeyMaterial(), "UTF-8");
        return result.getKeyPair().getKeyMaterial();
    }

    /**
     * <pre>
     * 주어진 keyName으로 AWS KeyPair를 삭제한다.
     * </pre>
     *
     * @param keyName
     */
    public void deleteKeyPair(String keyName) {
        ec2.deleteKeyPair(new DeleteKeyPairRequest().withKeyName(keyName));
    }

    /**
     * <pre>
     * rc 버전을 제외한 가장 최근의 Amazon Linux AMI를 조회한다.
     * </pre>
     *
     * @return
     */
    public Image getLatestAmazinLinuxImage() {
        DescribeImagesResult result = ec2.describeImages(new DescribeImagesRequest()
                .withFilters(new Filter().withName("architecture").withValues("x86_64"))
                .withFilters(new Filter().withName("image-type").withValues("machine"))
                .withFilters(new Filter().withName("hypervisor").withValues("xen"))
                .withFilters(new Filter().withName("name").withValues("amzn2-ami-hvm*"))
                .withFilters(new Filter().withName("root-device-type").withValues("ebs"))
                .withFilters(new Filter().withName("block-device-mapping.volume-type").withValues("gp2"))
                .withFilters(new Filter().withName("is-public").withValues("true"))
                .withFilters(new Filter().withName("owner-alias").withValues("amazon")));

        List<Image> imageList = result.getImages();
        for (int i = 0; i < imageList.size(); i++) {
            if (imageList.get(i).getName().indexOf("rc") > -1) {
                imageList.remove(i);
            }
        }

        Collections.sort(imageList, new Comparator<Image>() {
            @Override
            public int compare(Image first, Image second) {
                String firstDate = first.getCreationDate();
                String secondDate = second.getCreationDate();

                return secondDate.compareTo(firstDate);
            }
        });

        return imageList.get(0);
    }

    /**
     * Gets images.
     *
     * @param id         the id
     * @param name       the name
     * @param visibility the visibility
     *
     * @return the images
     */
    public List<Image> getImages(String id, String name, String visibility) {
        DescribeImagesRequest request = new DescribeImagesRequest();

        if (!StringUtils.isNullOrEmpty(id)) {
            request.withImageIds(id);
        }

        if (!StringUtils.isNullOrEmpty(name)) {
            if (!name.startsWith("*")) {
                name = "*" + name;
            }

            if (!name.endsWith("*")) {
                name += "*";
            }

            request.withFilters(new Filter().withName("name").withValues(name));
        }

        if (StringUtils.isNullOrEmpty(id) && StringUtils.isNullOrEmpty(name)) {
            if ("public".equals(visibility.toLowerCase())) {
                request.withFilters(new Filter().withName("is-public").withValues("true"));
            } else if ("private".equals(visibility.toLowerCase())) {
                request.withFilters(new Filter().withName("is-public").withValues("false"));
            } else {
                request.withOwners("self");
            }
        }

        return ec2.describeImages(request).getImages();
    }

    /**
     * <pre>
     * VPC에 attach된 Internet Gateway ID를 조회한다.
     * </pre>
     *
     * @return
     */
    public String getInternetGatewayId(String vpcId) {
        DescribeInternetGatewaysResult result = ec2.describeInternetGateways(new DescribeInternetGatewaysRequest()
                .withFilters(new Filter().withName("attachment.vpc-id").withValues(vpcId)));

        if (result.getInternetGateways().size() == 0) {
            return null;
        } else {
            return result.getInternetGateways().get(0).getInternetGatewayId();
        }
    }

    /**
     * <pre>
     * VPC에 attach된 Internet Gateway를 삭제한다.
     * </pre>
     *
     * @return
     */
    public void detachInternetGateway(String igwId, String vpcId) {
        ec2.detachInternetGateway(new DetachInternetGatewayRequest()
                .withInternetGatewayId(igwId)
                .withVpcId(vpcId));
    }

    /**
     * <pre>
     * VPC에 attach된 Internet Gateway를 삭제한다.
     * </pre>
     *
     * @return
     */
    public void deleteInternetGateway(String igwId) {
        ec2.deleteInternetGateway(new DeleteInternetGatewayRequest().withInternetGatewayId(igwId));
    }

    /**
     * <pre>
     * Default VPC 조회
     * </pre>
     *
     * @return
     */
    public String getDefaultVPC() {
        DescribeVpcsResult result = ec2.describeVpcs();

        String vpcId = null;
        for (Vpc vpc : result.getVpcs()) {
            if (vpc.isDefault()) {
                vpcId = vpc.getVpcId();
                break;
            }
        }

        return vpcId;
    }

    /**
     * <pre>
     * Default Subnet 조회
     * </pre>
     *
     * @return
     */
    public String getDefaultSubnet(String vpcId, String zone) {
        DescribeSubnetsResult result = ec2.describeSubnets(new DescribeSubnetsRequest()
                .withFilters(new Filter()
                        .withName("vpc-id")
                        .withValues(vpcId)));

        String subnetId = null;

        for (Subnet subnet : result.getSubnets()) {
            if (subnet.isDefaultForAz() && subnet.getAvailabilityZone().endsWith(zone)) {
                subnetId = subnet.getSubnetId();
                break;
            }
        }

        return subnetId;
    }

    /**
     * Key pair list list.
     *
     * @return the list
     */
    public List<String> keyPairList() {
        DescribeKeyPairsResult result = ec2.describeKeyPairs();

        List<String> keyPairList = new ArrayList<>();
        for (KeyPairInfo keyPair : result.getKeyPairs()) {
            keyPairList.add(keyPair.getKeyName());
        }

        return keyPairList;
    }
}
//end of EC2Client.java
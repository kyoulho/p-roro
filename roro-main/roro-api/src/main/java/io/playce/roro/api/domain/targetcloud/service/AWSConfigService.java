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
 * SangCheon Park   Feb 10, 2022		    First Draft.
 */
package io.playce.roro.api.domain.targetcloud.service;

import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.Image;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.common.code.Domain1009;
import io.playce.roro.jpa.entity.CredentialMaster;
import io.playce.roro.jpa.repository.CredentialMasterRepository;
import io.playce.roro.mig.aws.auth.BasicAWSCredentials;
import io.playce.roro.mig.aws.ec2.EC2Client;
import io.playce.roro.mig.aws.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
@Service
@Slf4j
@RequiredArgsConstructor
public class AWSConfigService {

    private final ModelMapper modelMapper;

    private final CredentialMasterRepository credentialMasterRepository;

    public List<AWSConfigDto.Region> getRegionList(Long projectId, Long credentialId) {
        getAWSCredential(projectId, credentialId);
        return AWSConfigDto.Regions.getRegionList();
    }

    /**
     * <pre>
     * 주어진 projectId, credentialId 로부터 AWS용 Credential 객체를 생성한다.
     * </pre>
     *
     * @param projectId
     * @param credentialId
     *
     * @return
     */
    private BasicAWSCredentials getAWSCredential(Long projectId, Long credentialId) {
        CredentialMaster credentialMaster = credentialMasterRepository.findByProjectIdAndCredentialId(projectId, credentialId);

        if (credentialMaster == null || !Domain1009.AWS.name().equals(credentialMaster.getCredentialTypeCode())) {
            throw new RoRoApiException(ErrorCode.TC_CREDENTIAL_NOT_FOUND);
        }

        return new BasicAWSCredentials(credentialMaster.getAccessKey(), credentialMaster.getSecretKey());
    }

    /**
     * @param projectId
     * @param credentialId
     * @param region
     * @param id
     * @param name
     * @param visibility
     *
     * @return
     */
    public List<AWSConfigDto.ImageResponse> getImageList(Long projectId, Long credentialId, String region, String id, String name, String visibility) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), region);

        try {
            if (StringUtils.isEmpty(visibility)) {
                visibility = "self";
            }

            List<Image> imageList = ec2.getImages(id, name, visibility);

            List<AWSConfigDto.ImageResponse> imageResponseList = new ArrayList<>();
            for (Image image : imageList) {
                AWSConfigDto.ImageResponse imageResponse = new AWSConfigDto.ImageResponse();
                imageResponse.setImageId(image.getImageId());
                imageResponse.setImageName(image.getName());
                imageResponse.setArchitecture(image.getArchitecture());
                imageResponse.setOwner(image.getOwnerId());
                imageResponse.setVisibility(image.getPublic() == true ? "Public" : "Private");
                imageResponse.setStatus(image.getState());
                imageResponse.setPlatform(image.getPlatformDetails());
                imageResponse.setCreationDate(image.getCreationDate());
                imageResponse.setVirtualization(image.getVirtualizationType());
                imageResponse.setBlockDevices(new ArrayList<>());
                for (BlockDeviceMapping mapping : image.getBlockDeviceMappings()) {
                    if (mapping.getEbs() != null) {
                        AWSConfigDto.BlockDevice blockDevice = new AWSConfigDto.BlockDevice();
                        blockDevice.setName(mapping.getDeviceName());
                        blockDevice.setSize(mapping.getEbs().getVolumeSize());
                        blockDevice.setType(mapping.getEbs().getVolumeType());
                        blockDevice.setSnapshotId(mapping.getEbs().getSnapshotId());
                        blockDevice.setIops(mapping.getEbs().getIops());
                        blockDevice.setDeleteOnTermination(mapping.getEbs().getDeleteOnTermination());
                        imageResponse.getBlockDevices().add(blockDevice);
                    }
                }
                imageResponseList.add(imageResponse);
            }

            return imageResponseList;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get image list.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, region);
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * Gets key pairs.
     *
     * @param projectId
     * @param credentialId
     * @param region
     *
     * @return the key pairs
     */
    public List<String> getKeyPairs(Long projectId, Long credentialId, String region) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), region);

        try {
            List<String> keyPairList = ec2.keyPairList();

            return keyPairList;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get key pair list.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, region);
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param region
     *
     * @return
     */
    public List<String> getAvailabilityZoneList(Long projectId, Long credentialId, String region) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), region);

        try {
            return ec2.getAvailabilityZoneList();
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get availability zone list.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, region);
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param region
     * @param subnetId
     *
     * @return
     */
    public List<InstanceType> getInstanceTypes(Long projectId, Long credentialId, String region, String subnetId) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), region);

        if (subnetId != null) {
            try {
                return ec2.getInstanceTypeOffering(subnetId);
            } catch (Exception e) {
                log.error("Unhandled exception occurred while get instance type list.", e);
                if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                    throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, region);
                } else {
                    throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
                }
            }
        } else {
            return InstanceTypes.getInstanceTypeList();
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param region
     * @param search
     *
     * @return
     */
    public List<AWSConfigDto.VpcResponse> getVpcList(Long projectId, Long credentialId, String region, String search) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), region);

        try {
            VpcDetail vpcDetail = new VpcDetail();
            vpcDetail.setSearch(search);
            List<VpcDetail> vpcDetailList = ec2.getVPCList(vpcDetail);

            List<AWSConfigDto.VpcResponse> vpcResponsesList = new ArrayList<>();
            for (VpcDetail vd : vpcDetailList) {
                AWSConfigDto.VpcResponse vpcResponse = modelMapper.map(vd, AWSConfigDto.VpcResponse.class);
                vpcResponse.setRegion(region);
                vpcResponsesList.add(vpcResponse);
            }

            return vpcResponsesList;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get vpc list.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, region);
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param vpcRequest
     */
    public void createVpc(Long projectId, Long credentialId, AWSConfigDto.VpcCreateRequest vpcRequest) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), vpcRequest.getRegion());

        try {
            VpcDetail vpcDetail = modelMapper.map(vpcRequest, VpcDetail.class);
            ec2.createVpc(vpcDetail);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create a vpc.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, vpcRequest.getRegion());
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param vpcId
     * @param vpcRequest
     */
    public void updateVpc(Long projectId, Long credentialId, String vpcId, AWSConfigDto.VpcUpdateRequest vpcRequest) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), vpcRequest.getRegion());

        try {
            VpcDetail vpcDetail = modelMapper.map(vpcRequest, VpcDetail.class);
            vpcDetail.setVpcId(vpcId);
            ec2.updateVpc(vpcDetail);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while update the vpc.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, vpcRequest.getRegion());
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param vpcId
     * @param region
     */
    public void deleteVpc(Long projectId, Long credentialId, String vpcId, String region) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), region);

        try {
            ec2.deleteVpc(vpcId);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while delete the vpc.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, region);
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param region
     * @param search
     *
     * @return
     */
    public List<AWSConfigDto.SubnetResponse> getSubnetList(Long projectId, Long credentialId, String region, String search) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), region);

        try {
            SubnetDetail subnetDetail = new SubnetDetail();
            subnetDetail.setSearch(search);
            List<SubnetDetail> subnetDetailList = ec2.getSubnetList(subnetDetail);

            List<AWSConfigDto.SubnetResponse> subnetResponseList = new ArrayList<>();
            for (SubnetDetail sd : subnetDetailList) {
                AWSConfigDto.SubnetResponse subnetResponse = modelMapper.map(sd, AWSConfigDto.SubnetResponse.class);
                subnetResponse.setRegion(region);
                subnetResponseList.add(subnetResponse);
            }

            return subnetResponseList;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get subnet list.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, region);
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param subnetRequest
     */
    public void createSubnet(Long projectId, Long credentialId, AWSConfigDto.SubnetCreateRequest subnetRequest) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), subnetRequest.getRegion());

        try {
            SubnetDetail subnetDetail = modelMapper.map(subnetRequest, SubnetDetail.class);
            ec2.createSubnet(subnetDetail);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create a subnet.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, subnetRequest.getRegion());
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param subnetId
     * @param subnetRequest
     */
    public void updateSubnet(Long projectId, Long credentialId, String subnetId, AWSConfigDto.SubnetUpdateRequest subnetRequest) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), subnetRequest.getRegion());

        try {
            SubnetDetail subnetDetail = modelMapper.map(subnetRequest, SubnetDetail.class);
            subnetDetail.setSubnetId(subnetId);
            ec2.updateSubnet(subnetDetail);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while update the subnet.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, subnetRequest.getRegion());
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param subnetId
     * @param region
     */
    public void deleteSubnet(Long projectId, Long credentialId, String subnetId, String region) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), region);

        try {
            ec2.deleteSubnet(subnetId);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while delete the subnet.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, region);
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param region
     * @param search
     *
     * @return
     */
    public List<AWSConfigDto.SecurityGroupResponse> getSecurityGroupList(Long projectId, Long credentialId, String region, String search) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), region);

        try {
            SecurityGroupDetail securityGroupDetail = new SecurityGroupDetail();
            securityGroupDetail.setSearch(search);
            List<SecurityGroupDetail> securityGroupDetailList = ec2.getSecurityGroupList(securityGroupDetail);

            List<AWSConfigDto.SecurityGroupResponse> securityGroupResponseList = new ArrayList<>();
            for (SecurityGroupDetail sgd : securityGroupDetailList) {
                AWSConfigDto.SecurityGroupResponse securityGroupResponse = modelMapper.map(sgd, AWSConfigDto.SecurityGroupResponse.class);
                securityGroupResponse.setRegion(region);
                securityGroupResponseList.add(securityGroupResponse);
            }

            return securityGroupResponseList;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get security group list.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, region);
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param securityGroupCreateRequest
     */
    public void createSecurityGroup(Long projectId, Long credentialId, AWSConfigDto.SecurityGroupCreateRequest securityGroupCreateRequest) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), securityGroupCreateRequest.getRegion());

        String groupId = null;
        try {
            SecurityGroupDetail securityGroupDetail = modelMapper.map(securityGroupCreateRequest, SecurityGroupDetail.class);
            groupId = ec2.createSecurityGroup(securityGroupDetail);

            if (securityGroupCreateRequest.getPermissionList().size() > 0) {
                List<io.playce.roro.mig.aws.model.Permission> permissionList = new ArrayList<>();
                for (AWSConfigDto.Permission permission : securityGroupCreateRequest.getPermissionList()) {
                    io.playce.roro.mig.aws.model.Permission p = modelMapper.map(permission, io.playce.roro.mig.aws.model.Permission.class);
                    p.setGroupId(groupId);
                    permissionList.add(p);
                }

                ec2.createPermissions(permissionList.get(0).getGroupId(), permissionList, false);
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create a security group.", e);

            // Security Group이 만들어진 후 Rule 등록이 실패하면 기존 만들어진 Security Group을 삭제한다.
            if (groupId != null) {
                ec2.deleteSecurityGroup(groupId);
            }
            
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, securityGroupCreateRequest.getRegion());
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param groupId
     * @param securityGroupUpdateRequest
     */
    public void updateSecurityGroup(Long projectId, Long credentialId, String groupId, AWSConfigDto.SecurityGroupUpdateRequest securityGroupUpdateRequest) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), securityGroupUpdateRequest.getRegion());

        try {
            ec2.createTag(groupId, "Name", securityGroupUpdateRequest.getGroupName());
        } catch (Exception e) {
            log.error("Unhandled exception occurred while update the security group.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, securityGroupUpdateRequest.getRegion());
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param groupId
     * @param region
     */
    public void deleteSecurityGroup(Long projectId, Long credentialId, String groupId, String region) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), region);

        try {
            ec2.deleteSecurityGroup(groupId);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while delete the security group.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, region);
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param groupId
     * @param region
     *
     * @return
     */
    public List<AWSConfigDto.Permission> getPermissionList(Long projectId, Long credentialId, String groupId, String region) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), region);

        try {
            List<io.playce.roro.mig.aws.model.Permission> permissionList = ec2.getPermissionList(groupId);

            List<AWSConfigDto.Permission> permissionResponseList = new ArrayList<>();
            for (io.playce.roro.mig.aws.model.Permission permission : permissionList) {
                AWSConfigDto.Permission permissionResponse = modelMapper.map(permission, AWSConfigDto.Permission.class);
                permissionResponseList.add(permissionResponse);
            }

            return permissionResponseList;
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get security group permission(rule) list.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, region);
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }

    /**
     * @param projectId
     * @param credentialId
     * @param groupId
     * @param permissionRequest
     */
    public void createPermissions(Long projectId, Long credentialId, String groupId, AWSConfigDto.PermissionRequest permissionRequest) {
        EC2Client ec2 = new EC2Client(getAWSCredential(projectId, credentialId), permissionRequest.getRegion());

        try {
            List<io.playce.roro.mig.aws.model.Permission> permissionList = new ArrayList<>();
            for (AWSConfigDto.Permission permission : permissionRequest.getPermissionList()) {
                io.playce.roro.mig.aws.model.Permission p = modelMapper.map(permission, io.playce.roro.mig.aws.model.Permission.class);
                permissionList.add(p);
            }

            ec2.createPermissions(groupId, permissionList, false);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create security group permission(rule)s.", e);
            if (e instanceof AmazonEC2Exception && e.getMessage().contains("AuthFailure")) {
                throw new RoRoApiException(ErrorCode.TC_AWS_AUTH_FAIL, permissionRequest.getRegion());
            } else {
                throw new RoRoApiException(ErrorCode.TC_AWS_EC2_ERROR, e.getMessage());
            }
        }
    }
}
//end of AWSConfigService.java
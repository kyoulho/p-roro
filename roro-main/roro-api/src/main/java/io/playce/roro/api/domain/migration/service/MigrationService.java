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
package io.playce.roro.api.domain.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.DateTimeUtils;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.code.Domain1009;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.config.InventoryProcessCancelProcessor;
import io.playce.roro.common.dto.migration.*;
import io.playce.roro.common.dto.migration.MigrationJobDetailResponseDto.TargetServer;
import io.playce.roro.common.dto.migration.MigrationProcessListResponseDto.Data;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mig.aws.auth.BasicAWSCredentials;
import io.playce.roro.mig.aws.ec2.EC2Client;
import io.playce.roro.mig.aws.model.SubnetDetail;
import io.playce.roro.mig.gcp.compute.ComputeClient;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MigrationService {

    public static final String SPLIT_CHAR = ",";

    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;

    private final InventoryProcessService inventoryProcessService;
    private final InventoryProcessMapper inventoryProcessMapper;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final CredentialMasterRepository credentialMasterRepository;
    private final InventoryProcessRepository inventoryProcessRepository;

    private final InventoryMigrationProcessRepository inventoryMigrationProcessRepository;
    private final InventoryMigrationProcessVolumeRepository inventoryMigrationProcessVolumeRepository;
    private final InventoryMigrationProcessTagRepository inventoryMigrationProcessTagRepository;
    private final InventoryProcessCancelProcessor inventoryProcessCancelProcessor;

    public MigrationResponseDto createMigration(Long projectId, MigrationRequestDto migrationRequestDto) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, migrationRequestDto.getServerInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + migrationRequestDto.getServerInventoryId() + " Not Found in Project ID : " + projectId));

        io.playce.roro.common.dto.inventory.process.InventoryProcess.Result lastMigrationProcess =
                inventoryProcessMapper.selectLastInventoryProcess(inventoryMaster.getInventoryId(), Domain1002.MIG.name());

        MigrationResponseDto migrationResponseDto = new MigrationResponseDto();
        migrationResponseDto.setProjectId(projectId);
        migrationResponseDto.setInventoryTypeCode(inventoryMaster.getInventoryTypeCode());
        migrationResponseDto.setInventoryId(inventoryMaster.getInventoryId());
        migrationResponseDto.setInventoryName(inventoryMaster.getInventoryName());

        // 가장 마지막 상태가 완료, 취소, 실패, NS, PC 상태일 경우에만 추가한다.
        if (lastMigrationProcess == null ||
                lastMigrationProcess.getInventoryProcessResultCode().equals(Domain1003.CMPL.name()) ||
                lastMigrationProcess.getInventoryProcessResultCode().equals(Domain1003.CNCL.name()) ||
                lastMigrationProcess.getInventoryProcessResultCode().equals(Domain1003.FAIL.name()) ||
                lastMigrationProcess.getInventoryProcessResultCode().equals(Domain1003.NS.name()) ||
                lastMigrationProcess.getInventoryProcessResultCode().equals(Domain1003.PC.name())) {

            // Private IP 에 대한 Validation
            if (StringUtils.isNotEmpty(migrationRequestDto.getPrivateIp())) {
                if (migrationRequestDto.getCredentialId() != null) {
                    CredentialMaster credentialMaster = credentialMasterRepository.findById(migrationRequestDto.getCredentialId())
                            .orElseThrow(() -> new ResourceNotFoundException("Credential ID(" + migrationRequestDto.getCredentialId() + ") does not exist."));

                    InetAddressValidator validator = InetAddressValidator.getInstance();

                    if (validator.isValid(migrationRequestDto.getPrivateIp())) {
                        if ("AWS".equals(credentialMaster.getCredentialTypeCode())) {
                            EC2Client ec2 = new EC2Client(new BasicAWSCredentials(credentialMaster.getAccessKey(), credentialMaster.getSecretKey()), migrationRequestDto.getRegion());

                            SubnetDetail subnetDetail = new SubnetDetail();
                            subnetDetail.setSearch(migrationRequestDto.getSubnetId());
                            List<SubnetDetail> subnetDetailList = ec2.getSubnetList(subnetDetail);

                            if (subnetDetailList != null && subnetDetailList.size() == 1) {
                                String cidr = subnetDetailList.get(0).getSubnetCidr();

                                SubnetUtils subnetUtils = new SubnetUtils(cidr);
                                subnetUtils.setInclusiveHostCount(true);

                                if (!subnetUtils.getInfo().isInRange(migrationRequestDto.getPrivateIp())) {
                                    throw new RoRoApiException(ErrorCode.MIGRATION_INVALID_IP_ADDRESS, migrationRequestDto.getPrivateIp());
                                }
                            } else {
                                throw new RoRoApiException(ErrorCode.MIGRATION_SUBNET_NOT_FOUND, migrationRequestDto.getSubnetId());
                            }
                        } else if ("GCP".equals(credentialMaster.getCredentialTypeCode())) {
                            try {
                                String accountKey = IOUtils.toString(new File(credentialMaster.getKeyFilePath()).toURI(), "UTF-8");
                                JSONObject json = (JSONObject) JSONValue.parse(accountKey);
                                String gcpProjectId = json.getAsString("project_id");
                                ComputeClient compute = new ComputeClient(gcpProjectId, accountKey);

                                List<io.playce.roro.mig.gcp.model.network.SubnetDetail> subnetDetailList =
                                        compute.getSubnetList(migrationRequestDto.getRegion(), migrationRequestDto.getSubnetId());

                                if (subnetDetailList != null && subnetDetailList.size() == 1) {
                                    String cidr = subnetDetailList.get(0).getIpCidrRange();

                                    SubnetUtils subnetUtils = new SubnetUtils(cidr);
                                    subnetUtils.setInclusiveHostCount(true);

                                    if (!subnetUtils.getInfo().isInRange(migrationRequestDto.getPrivateIp())) {
                                        throw new RoRoApiException(ErrorCode.MIGRATION_INVALID_IP_ADDRESS, migrationRequestDto.getPrivateIp());
                                    }
                                } else {
                                    throw new RoRoApiException(ErrorCode.MIGRATION_SUBNET_NOT_FOUND, migrationRequestDto.getSubnetId());
                                }
                            } catch (Exception e) {
                                if (!(e instanceof RoRoApiException)) {
                                    log.error("Unhandled exception occurred while check private ip address.", e);
                                    throw new RoRoApiException(ErrorCode.UNKNOWN_ERROR, e.getMessage());
                                }
                            }
                        }
                    } else {
                        throw new RoRoApiException(ErrorCode.MIGRATION_INVALID_IP_ADDRESS, migrationRequestDto.getPrivateIp());
                    }
                }
            }

            InventoryProcessGroup inventoryProcessGroup = inventoryProcessService.addInventoryGroup();
            InventoryProcess inventoryProcess = inventoryProcessService.addInventoryProcess(inventoryProcessGroup.getInventoryProcessGroupId(), inventoryMaster.getInventoryId(), Domain1002.MIG, Domain1003.REQ);

            InventoryMigrationProcess inventoryMigrationProcess = new InventoryMigrationProcess();
            inventoryMigrationProcess.setInventoryProcessId(inventoryProcess.getInventoryProcessId());
            inventoryMigrationProcess.setMigrationPreConfigId(migrationRequestDto.getMigrationPreConfigId());
            inventoryMigrationProcess.setCredentialId(migrationRequestDto.getCredentialId());
            inventoryMigrationProcess.setRegion(migrationRequestDto.getRegion());
            inventoryMigrationProcess.setAvailabilityZone(migrationRequestDto.getAvailabilityZone());
            inventoryMigrationProcess.setVpcId(migrationRequestDto.getVpcId());
            inventoryMigrationProcess.setSubnetId(migrationRequestDto.getSubnetId());
            inventoryMigrationProcess.setEnableEipYn(migrationRequestDto.getEnableEipYn());
            inventoryMigrationProcess.setPrivateIp(migrationRequestDto.getPrivateIp());
            inventoryMigrationProcess.setHostName(migrationRequestDto.getHostName());
            inventoryMigrationProcess.setInstanceType(migrationRequestDto.getInstanceType());
            inventoryMigrationProcess.setGcpProjectId(migrationRequestDto.getGcpProjectId());
            if (migrationRequestDto.getSecurityGroupIds() != null && migrationRequestDto.getSecurityGroupIds().size() > 0) {
                inventoryMigrationProcess.setSecurityGroupIds(String.join(SPLIT_CHAR, migrationRequestDto.getSecurityGroupIds()));
            }
            if (migrationRequestDto.getFirewalls() != null && migrationRequestDto.getFirewalls().size() > 0) {
                inventoryMigrationProcess.setSecurityGroupIds(String.join(SPLIT_CHAR, migrationRequestDto.getFirewalls()));
            }
            if (migrationRequestDto.getExcludeDirectories() != null && migrationRequestDto.getExcludeDirectories().size() > 0) {
                inventoryMigrationProcess.setExcludeDirectories(String.join(SPLIT_CHAR, migrationRequestDto.getExcludeDirectories()));
            }
            if (migrationRequestDto.getNetworkTags() != null && migrationRequestDto.getNetworkTags().size() > 0) {
                inventoryMigrationProcess.setNetworkTags(String.join(SPLIT_CHAR, migrationRequestDto.getNetworkTags()));
            }

            inventoryMigrationProcess = inventoryMigrationProcessRepository.save(inventoryMigrationProcess);

            for (MigrationRequestDto.Volume volume : migrationRequestDto.getVolumes()) {
                InventoryMigrationProcessVolume inventoryMigrationProcessVolume = new InventoryMigrationProcessVolume();
                inventoryMigrationProcessVolume.setInventoryProcessId(inventoryMigrationProcess.getInventoryProcessId());
                inventoryMigrationProcessVolume.setVolumePath(volume.getVolumePath());
                inventoryMigrationProcessVolume.setDeviceName(volume.getDeviceName());
                inventoryMigrationProcessVolume.setVolumeSize(volume.getVolumeSize());
                inventoryMigrationProcessVolume.setRootYn(volume.getRootYn());

                inventoryMigrationProcessVolumeRepository.save(inventoryMigrationProcessVolume);
            }

            for (MigrationRequestDto.Tag tag : migrationRequestDto.getTags()) {
                InventoryMigrationProcessTag inventoryMigrationProcessTag = new InventoryMigrationProcessTag();
                inventoryMigrationProcessTag.setInventoryProcessId(inventoryMigrationProcess.getInventoryProcessId());
                inventoryMigrationProcessTag.setTagName(tag.getTagName());
                inventoryMigrationProcessTag.setTagValue(tag.getTagValue());

                inventoryMigrationProcessTagRepository.save(inventoryMigrationProcessTag);
            }

            migrationResponseDto.setInventoryProcessId(inventoryProcess.getInventoryProcessId());
            migrationResponseDto.setResult("SUCCESS");
        } else {
            throw new RoRoApiException(ErrorCode.MIGRATION_DUPLICATED);
        }

        return migrationResponseDto;
    }

    public MigrationProcessListResponseDto getMigrationServerList(long projectId, Long inventoryId, PageMigrationRequestDto pageMigrationRequestDto) {
        MigrationProcessListResponseDto migrationProcessListResponseDto = new MigrationProcessListResponseDto();
        Data data = new Data();

        if (inventoryId == null) {
            data.setTotalCount(inventoryProcessMapper.selectMigrationServerCount(projectId, pageMigrationRequestDto));
            data.setContents(inventoryProcessMapper.selectMigrationServerList(projectId, pageMigrationRequestDto));
        } else {
            data.setTotalCount(inventoryProcessMapper.selectMigrationServerDetailCount(projectId, inventoryId));
            data.setContents(inventoryProcessMapper.selectMigrationServerDetailList(projectId, inventoryId));
        }

        migrationProcessListResponseDto.setData(data);
        return migrationProcessListResponseDto;
    }

    @Transactional
    public void cancelMigrationTask(long projectId, long migrationId) {
        InventoryProcess inventoryProcess = inventoryProcessRepository.findById(migrationId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.MIGRATION_NOT_FOUND));

        Long inventoryProcessId = inventoryProcess.getInventoryProcessId();

        if (inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.CMPL.name())
                || inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.CNCL.name())
                || inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.FAIL.name())
                || inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.NS.name())
                || inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.PC.name())) {
            log.debug("migration cancel failed status: {}, inventoryProcess: {}", inventoryProcess.getInventoryProcessResultCode(), inventoryProcess);
            throw new RoRoApiException(ErrorCode.MIGRATION_CANCEL_INVALID_STATUS);
        } else {
            InventoryProcessCancelInfo.addCancelRequest(inventoryProcessId, System.currentTimeMillis());

            if (inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.PROC.name())) {
                boolean canceled = inventoryProcessCancelProcessor.jobCancel(Domain1002.valueOf(inventoryProcess.getInventoryProcessTypeCode()).executeKey(inventoryProcessId));
                if (canceled) {
                    saveInventoryProcessCancel(inventoryProcess);
                }
            } else {
                saveInventoryProcessCancel(inventoryProcess);
            }
        }
    }

    private void saveInventoryProcessCancel(InventoryProcess inventoryProcess) {
        inventoryProcess.setInventoryProcessResultCode(Domain1003.CNCL.name());
        inventoryProcess.setModifyUserId(WebUtil.getUserId());
        inventoryProcess.setModifyDatetime(new Date());
        inventoryProcessRepository.save(inventoryProcess);
    }

    @Transactional
    public void removeMigrationTask(long projectId, long migrationId) {
        InventoryProcess inventoryProcess = inventoryProcessRepository.findById(migrationId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.MIGRATION_NOT_FOUND));

        if (inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.CNCL.name())
                || inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.FAIL.name())
                || inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.NS.name())) {
            inventoryProcess.setDeleteYn(Domain101.Y.name());
            inventoryProcess.setModifyUserId(WebUtil.getUserId());
            inventoryProcess.setModifyDatetime(new Date());

            inventoryProcessRepository.save(inventoryProcess);
        } else {
            throw new RoRoApiException(ErrorCode.MIGRATION_DELETE_INVALID_STATUS);
        }

    }

    @SneakyThrows
    public MigrationJobDetailResponseDto getMigrationTask(long projectId, long migrationId) {
        InventoryProcess inventoryProcess = inventoryProcessRepository.findById(migrationId)
                .orElseThrow(() -> new RoRoApiException(ErrorCode.MIGRATION_NOT_FOUND));

        String credentialTypeName = "Existing Linux";

        MigrationJobDetailResponseDto migrationJobDetailResponseDto = new MigrationJobDetailResponseDto();

        migrationJobDetailResponseDto.setDetail(inventoryProcessMapper.selectMigrationJob(projectId, migrationId));
        migrationJobDetailResponseDto.setSource(inventoryProcessMapper.selectMigrationSourceServer(projectId, migrationId));

        TargetServer targetServer = new TargetServer();

        // Existing Linux 인 경우 결과 값이 다르다.
        if (migrationJobDetailResponseDto.getDetail().getCredentialTypeName().equals(credentialTypeName)) {
            Map<String, String> resultMap = inventoryProcessMapper.selectExistLinux(projectId, migrationId);

            if (resultMap != null && StringUtils.isNotEmpty(resultMap.get("resultJson"))) {
                log.debug("Existing Linux Result JSON : {}", resultMap.get("resultJson"));

                Map<String, Object> jsonMap = objectMapper.readValue(resultMap.get("resultJson"), HashMap.class);
                targetServer = modelMapper.map(jsonMap, TargetServer.class);
                targetServer.setIpAddress(resultMap.get("ipAddress"));
                targetServer.setCpuCores((int) jsonMap.get("cpuCount"));
                targetServer.setMemorySize((int) jsonMap.get("memSize"));
            }

        } else {
            targetServer = inventoryProcessMapper.selectMigrationTargetServer(projectId, migrationId);

            if ("GCP".equals(migrationJobDetailResponseDto.getDetail().getCredentialTypeCode())) {
                if (StringUtils.isNotEmpty(targetServer.getTempSecurityGroupIds())) {
                    targetServer.setFirewallRuleIds(new ArrayList<>(Arrays.asList(targetServer.getTempSecurityGroupIds().split(","))));
                }
                if (StringUtils.isNotEmpty(targetServer.getTempSecurityGroupNames())) {
                    targetServer.setFirewallRuleNames(new ArrayList<>(Arrays.asList(targetServer.getTempSecurityGroupNames().split(","))));
                }
            } else {
                if (StringUtils.isNotEmpty(targetServer.getTempSecurityGroupIds())) {
                    targetServer.setSecurityGroupIds(new ArrayList<>(Arrays.asList(targetServer.getTempSecurityGroupIds().split(","))));
                }
                if (StringUtils.isNotEmpty(targetServer.getTempSecurityGroupNames())) {
                    targetServer.setSecurityGroupNames(new ArrayList<>(Arrays.asList(targetServer.getTempSecurityGroupNames().split(","))));
                }
            }

            if (StringUtils.isEmpty(targetServer.getTempExcludeDirectories())) {
                targetServer.setExcludeDirectories(new ArrayList<>());
            } else {
                targetServer.setExcludeDirectories(new ArrayList<>(Arrays.asList(targetServer.getTempExcludeDirectories().split(","))));
            }

            targetServer.setVolumes(inventoryProcessMapper.selectMigrationVolumes(projectId, migrationId));
            targetServer.setTags(inventoryProcessMapper.selectMigrationTags(projectId, migrationId));
        }

        migrationJobDetailResponseDto.setTarget(targetServer);

        return migrationJobDetailResponseDto;
    }

    public ByteArrayInputStream getMigrationCsvDownload(Long projectId, PageMigrationRequestDto pageMigrationRequestDto) {
        pageMigrationRequestDto.setExcelDownload(true);

        List<MigrationJobDto> migrationJobs = inventoryProcessMapper.selectMigrationServerList(projectId, pageMigrationRequestDto);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT);

            String[] header = {"Job ID", "Source", "Target Platform", "Target", "Progress", "Started Date", "Ended Date"};

            csvPrinter.printRecord(header);

            for (MigrationJobDto migrationJobDto : migrationJobs) {
                String targetPlatform = "";
                String target = "";

                if (StringUtils.isNotEmpty(migrationJobDto.getCredentialTypeCode())) {
                    if (migrationJobDto.getCredentialTypeCode().equals(Domain1009.AWS.name()) ||
                            migrationJobDto.getCredentialTypeCode().equals(Domain1009.GCP.name())) {
                        targetPlatform = migrationJobDto.getCredentialTypeName() + "(" + migrationJobDto.getCredentialTypeCode() + ")";
                    } else {
                        targetPlatform = migrationJobDto.getCredentialTypeName();
                    }
                }

                if (StringUtils.isEmpty(migrationJobDto.getPublicIp()) && StringUtils.isEmpty(migrationJobDto.getPrivateIp())) {
                    target = "";
                } else {
                    target = migrationJobDto.getPublicIp() + " / " + migrationJobDto.getPrivateIp();
                }

                List<String> data = Arrays.asList(
                        migrationJobDto.getInventoryProcessId() + "",
                        migrationJobDto.getInventoryName() + "(" + migrationJobDto.getServerIp() + ")",
                        targetPlatform,
                        target,
                        (migrationJobDto.getProgress() == null ? 0 : (int) Math.round(migrationJobDto.getProgress())) + "%",
                        DateTimeUtils.convertDefaultDateTimeFormat(migrationJobDto.getInventoryProcessStartDatetime()),
                        DateTimeUtils.convertDefaultDateTimeFormat(migrationJobDto.getInventoryProcessEndDatetime())
                );

                csvPrinter.printRecord(data);
            }

            csvPrinter.flush();

            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Unhandled error occured with " + e.getMessage());
        }
    }

}
//end of MigrationService.java
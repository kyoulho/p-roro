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
package io.playce.roro.scheduler.component;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.dto.inventory.process.MigrationProgressQueueItem;
import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.common.dto.migration.enums.StatusType;
import io.playce.roro.common.exception.CancelException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mig.Migration;
import io.playce.roro.mig.MigrationManager;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.ExecuteException;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static io.playce.roro.api.domain.migration.service.MigrationService.SPLIT_CHAR;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationProcess {

    private final BlockingQueue<MigrationProgressQueueItem> migrationProgressQueue;

    private final ModelMapper modelMapper;
    private final ServerMapper serverMapper;

    private final InventoryProcessRepository inventoryProcessRepository;
    private final InventoryProcessResultRepository inventoryProcessResultRepository;
    private final InventoryMigrationProcessRepository inventoryMigrationProcessRepository;
    private final InventoryMigrationProcessVolumeRepository inventoryMigrationProcessVolumeRepository;
    private final InventoryMigrationProcessTagRepository inventoryMigrationProcessTagRepository;
    private final CredentialMasterRepository credentialMasterRepository;
    private final ServerSummaryRepository serverSummaryRepository;
    private final ServerDiskInformationRepository serverDiskInformationRepository;
    private final MigrationPreConfigRepository migrationPreConfigRepository;
    private final MigrationPreConfigGroupRepository migrationPreConfigGroupRepository;
    private final MigrationPreConfigUserRepository migrationPreConfigUserRepository;
    private final MigrationPreConfigFileRepository migrationPreConfigFileRepository;

    /**
     * @param item
     * @param resultState
     *
     * @return
     */
    public Domain1003 migration(InventoryProcessQueueItem item, Domain1003 resultState) throws InterruptedException {
        Long inventoryProcessId = item.getInventoryProcessId();
        InventoryProcess inventoryProcess = null;
        String resultString = null;

        try {
            inventoryProcess = inventoryProcessRepository.findById(inventoryProcessId)
                    .orElseThrow(() -> new ResourceNotFoundException("Migration ID(" + inventoryProcessId + ") does not exist."));

            InventoryProcessConnectionInfo connectionInfo = serverMapper.selectServerConnectionInfoByInventoryProcessId(item.getInventoryProcessId());
            log.debug("Step 4 ~ id: {}. load connection info: {}", inventoryProcessId, connectionInfo);

            TargetHost targetHost = InventoryProcessConnectionInfo.targetHost(connectionInfo);

            String uname = SSHUtil.executeCommand(targetHost, "uname").trim();

            InventoryMigrationProcess inventoryMigrationProcess = inventoryMigrationProcessRepository.findById(inventoryProcessId)
                    .orElseThrow(() -> new ResourceNotFoundException("Migration ID(" + inventoryProcessId + ") does not exist."));

            // 1. MigrationRequestDto 의 필드 값 세팅
            MigrationProcessDto migrationProcessDto = modelMapper.map(inventoryMigrationProcess, MigrationProcessDto.class);
            if (migrationProcessDto.getInternalStatus() == null) {
                migrationProcessDto.setInternalStatus(StatusType.READY);
            }
            migrationProcessDto.setServerInventoryId(inventoryProcess.getInventoryId());
            migrationProcessDto.setTargetHost(targetHost);
            migrationProcessDto.setStartDate(inventoryProcess.getInventoryProcessStartDatetime());

            if (inventoryMigrationProcess.getSecurityGroupIds() != null) {
                migrationProcessDto.setSecurityGroupIds(Arrays.asList(inventoryMigrationProcess.getSecurityGroupIds().split(SPLIT_CHAR)));
            }

            if (inventoryMigrationProcess.getFirewalls() != null) {
                migrationProcessDto.setFirewalls(Arrays.asList(inventoryMigrationProcess.getFirewalls().split(SPLIT_CHAR)));
            }

            if (inventoryMigrationProcess.getExcludeDirectories() != null) {
                migrationProcessDto.setExcludeDirectories(Arrays.asList(inventoryMigrationProcess.getExcludeDirectories().split(SPLIT_CHAR)));
            }

            if (inventoryMigrationProcess.getNetworkTags() != null) {
                migrationProcessDto.setNetworkTags(Arrays.asList(inventoryMigrationProcess.getNetworkTags().split(SPLIT_CHAR)));
            }

            // 1-1. Uname
            migrationProcessDto.setUname(uname);

            // 1-2. Credential 정보
            if (inventoryMigrationProcess.getCredentialId() != null) {
                CredentialMaster credentialMaster = credentialMasterRepository.findById(inventoryMigrationProcess.getCredentialId())
                        .orElseThrow(() -> new ResourceNotFoundException("Credential ID(" + inventoryMigrationProcess.getCredentialId() + ") does not exist."));

                MigrationProcessDto.Credential credential = modelMapper.map(credentialMaster, MigrationProcessDto.Credential.class);
                migrationProcessDto.setCredential(credential);
            }

            // 1-3. Volume 정보
            migrationProcessDto.setVolumes(new ArrayList<>());
            List<InventoryMigrationProcessVolume> inventoryMigrationProcessVolumeList = inventoryMigrationProcessVolumeRepository.findByInventoryProcessId(inventoryProcessId);
            for (InventoryMigrationProcessVolume v : inventoryMigrationProcessVolumeList) {
                migrationProcessDto.getVolumes().add(modelMapper.map(v, MigrationProcessDto.Volume.class));
            }

            // 1-4. Tag 정보
            migrationProcessDto.setTags(new ArrayList<>());
            List<InventoryMigrationProcessTag> inventoryMigrationProcessTagList = inventoryMigrationProcessTagRepository.findByInventoryProcessId(inventoryProcessId);
            for (InventoryMigrationProcessTag t : inventoryMigrationProcessTagList) {
                migrationProcessDto.getTags().add(modelMapper.map(t, MigrationProcessDto.Tag.class));
            }

            // 1-5. Server Summary 정보(Disk 정보 포함)
            Long inventoryId = inventoryProcess.getInventoryId();
            ServerSummary serverSummary = serverSummaryRepository.findByServerInventoryId(inventoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("ServerSummary(" + inventoryId + ") does Not Found."));
            MigrationProcessDto.ServerSummary summary = modelMapper.map(serverSummary, MigrationProcessDto.ServerSummary.class);
            summary.setDiskInfos(new ArrayList<>());

            List<ServerDiskInformation> serverDiskInformationList = serverDiskInformationRepository.findByServerInventoryId(inventoryProcess.getInventoryId());
            for (ServerDiskInformation serverDiskInformation : serverDiskInformationList) {
                summary.getDiskInfos().add(modelMapper.map(serverDiskInformation, MigrationProcessDto.DiskInfo.class));
            }
            migrationProcessDto.setServerSummary(summary);

            // 1-6. Preconfig 관련 user, group, file, profile, crontab 정보 저장
            if (inventoryMigrationProcess.getMigrationPreConfigId() != null) {
                MigrationPreConfig migrationPreConfig = migrationPreConfigRepository.findById(inventoryMigrationProcess.getMigrationPreConfigId())
                        .orElseThrow(() -> new ResourceNotFoundException("Preconfig ID(" + inventoryMigrationProcess.getMigrationPreConfigId() + ") does not exist."));

                MigrationProcessDto.MigrationPreConfig preConfig = modelMapper.map(migrationPreConfig, MigrationProcessDto.MigrationPreConfig.class);
                if (migrationPreConfig.getCredentialId() != null) {
                    CredentialMaster credentialMaster = credentialMasterRepository.findById(migrationPreConfig.getCredentialId())
                            .orElseThrow(() -> new ResourceNotFoundException("Credential ID(" + migrationPreConfig.getCredentialId() + ") does not exist."));

                    preConfig.setCredential(modelMapper.map(credentialMaster, MigrationProcessDto.Credential.class));
                }

                if (migrationPreConfig.getPackages() != null) {
                    preConfig.setPackages(Arrays.asList(migrationPreConfig.getPackages().split(SPLIT_CHAR)));
                }

                preConfig.setMigrationPreConfigGroups(new ArrayList<>());
                List<MigrationPreConfigGroup> migrationPreConfigGroupList = migrationPreConfigGroupRepository.findByMigrationPreConfigId(migrationPreConfig.getMigrationPreConfigId());
                for (MigrationPreConfigGroup g : migrationPreConfigGroupList) {
                    preConfig.getMigrationPreConfigGroups().add(modelMapper.map(g, MigrationProcessDto.MigrationPreConfigGroup.class));
                }

                preConfig.setMigrationPreConfigUsers(new ArrayList<>());
                List<MigrationPreConfigUser> migrationPreConfigUserList = migrationPreConfigUserRepository.findByMigrationPreConfigId(migrationPreConfig.getMigrationPreConfigId());
                for (MigrationPreConfigUser u : migrationPreConfigUserList) {
                    MigrationProcessDto.MigrationPreConfigUser user = modelMapper.map(u, MigrationProcessDto.MigrationPreConfigUser.class);
                    if (u.getGroups() != null) {
                        user.setGroups(Arrays.asList(u.getGroups().split(SPLIT_CHAR)));
                    }
                    preConfig.getMigrationPreConfigUsers().add(user);
                }

                preConfig.setMigrationPreConfigFiles(new ArrayList<>());
                List<MigrationPreConfigFile> migrationPreConfigFileList = migrationPreConfigFileRepository.findByMigrationPreConfigId(migrationPreConfig.getMigrationPreConfigId());
                for (MigrationPreConfigFile f : migrationPreConfigFileList) {
                    preConfig.getMigrationPreConfigFiles().add(modelMapper.map(f, MigrationProcessDto.MigrationPreConfigFile.class));
                }

                migrationProcessDto.setMigrationPreConfig(preConfig);
            }

            // 2. Target Cloud 및 Migration Type에 따라 migrationMap에서 적합한 Component 탐색
            String componentName = getComponentName(migrationProcessDto);
            log.debug("[{}] Migration Component Name : {}", inventoryProcessId, componentName);

            Migration migration = getMigrationBean(componentName);
            log.debug("[{}] Migration bean found : {}", inventoryProcessId, migration);

            // 3. Estimate 시간 계산 및 DB Update
            int estimateTime = MigrationManager.getEstimateTime(migrationProcessDto);
            inventoryMigrationProcess.setEstimateTime(estimateTime);
            inventoryMigrationProcessRepository.save(inventoryMigrationProcess);

            log.debug("[{}] Migration Request Detail : {}", inventoryProcessId, JsonUtil.objToJson(migrationProcessDto, true));

            if (InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
                return Domain1003.CNCL;
            }

            // 4. Migration Component의 migration() 실행
            migrationProcessDto = migration.migration(migrationProcessDto, migrationProgressQueue);

            // migrationProgressQueue 에 쌓인 결과가 정상적으로 반영되기 위해 잠시 대기한다.
            Thread.sleep(1000);

            // 5. 결과 DB Update
            if (migrationProcessDto != null) {
                InventoryMigrationProcess imp = inventoryMigrationProcessRepository.findById(inventoryProcessId)
                        .orElseThrow(() -> new ResourceNotFoundException("Migration ID(" + inventoryProcessId + ") does not exist."));

                if (migrationProcessDto.getBlockDevices() != null) {
                    imp.setBlockDevices(String.join(SPLIT_CHAR, migrationProcessDto.getBlockDevices()));
                }
                imp.setRegion(migrationProcessDto.getRegion());
                imp.setAvailabilityZone(migrationProcessDto.getAvailabilityZone());
                imp.setVpcName(migrationProcessDto.getVpcName());
                imp.setSubnetName(migrationProcessDto.getSubnetName());
                if (migrationProcessDto.getSecurityGroupNames() != null) {
                    imp.setSecurityGroupNames(String.join(SPLIT_CHAR, migrationProcessDto.getSecurityGroupNames()));
                }
                imp.setImageId(migrationProcessDto.getImageId());
                imp.setImageName(migrationProcessDto.getImageName());
                imp.setInstanceId(migrationProcessDto.getInstanceId());
                imp.setInstanceName(migrationProcessDto.getInstanceName());
                imp.setPublicIp(migrationProcessDto.getPublicIp());
                imp.setPrivateIp(migrationProcessDto.getPrivateIp());
                imp.setInstanceLaunchTime(migrationProcessDto.getInstanceLaunchTime());
                imp.setElapsedTime(migrationProcessDto.getElapsedTime());
                imp.setInternalStatus(StatusType.COMPLETED.getDescription());
                imp.setProgress(100.0);

                inventoryMigrationProcessRepository.save(imp);

                for (MigrationProcessDto.Volume volume : migrationProcessDto.getVolumes()) {
                    InventoryMigrationProcessVolume inventoryMigrationProcessVolume = inventoryMigrationProcessVolumeRepository.findById(volume.getMigrationVolumeId()).orElse(null);

                    if (inventoryMigrationProcessVolume != null) {
                        inventoryMigrationProcessVolume.setVolumeId(volume.getVolumeId());
                        inventoryMigrationProcessVolume.setRawFileName(volume.getRawFileName());
                        inventoryMigrationProcessVolume.setRawFileSize(volume.getRawFileSize());
                        inventoryMigrationProcessVolume.setManifestUrl(volume.getManifestUrl());

                        inventoryMigrationProcessVolumeRepository.save(inventoryMigrationProcessVolume);
                    }
                }

                // Existing Linux Migration의 경우 migrationProcessDto.getServerSummary()에 타깃 호스트에 대한 서버 정보가 포함되어 있다.
                if (migrationProcessDto.getCredential() == null) {
                    if (migrationProcessDto.getServerSummary() != null) {
                        InventoryProcessResult result = new InventoryProcessResult();
                        result.setInventoryProcessId(inventoryProcessId);
                        result.setInventoryProcessResultJson(JsonUtil.objToJson(migrationProcessDto.getServerSummary(), true));
                        inventoryProcessResultRepository.save(result);
                    }
                }
            }

            resultState = Domain1003.CMPL;

            log.debug("[{}] Migration completed.", migrationProcessDto.getInventoryProcessId());
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);

            e = getCausedException(e);

            if (e instanceof GoogleJsonResponseException) {
                resultString = "[GCP] " + ((GoogleJsonResponseException) e).getDetails().getMessage();
            } else if (e instanceof ExecuteException) {
                resultString = "Can not execute migration script file. Please see the log file.";
            } else {
                if (e.getClass().getCanonicalName().startsWith("com.amazonaws")) {
                    resultString = "[AWS] " + e.getMessage();
                } else if (e.getClass().getCanonicalName().startsWith("com.google.api")) {
                    resultString = "[GCP] " + e.getMessage();
                } else {
                    resultString = e.getMessage();
                }
            }

            if (!(e instanceof CancelException)) {
                log.error("item {}", item, e);
                log.error(resultString);
            }
        } finally {
            if (InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
                resultState = Domain1003.CNCL;
                resultString = null;
            }

            inventoryProcess.setInventoryProcessResultCode(resultState.name());
            inventoryProcess.setInventoryProcessResultTxt(resultString);
            inventoryProcess.setInventoryProcessEndDatetime(new Date());
            inventoryProcess.setModifyUserId(WebUtil.getUserId());
            inventoryProcess.setModifyDatetime(new Date());

            inventoryProcessRepository.save(inventoryProcess);
        }

        return resultState;
    }

    /**
     * @param migrationProcessDto
     *
     * @return
     */
    private String getComponentName(MigrationProcessDto migrationProcessDto) {
        String componeName = null;

        if (migrationProcessDto.getCredential() == null) {
            componeName = "CustomReplatformMigration";
        } else {
            if ("AWS".equals(migrationProcessDto.getCredential().getCredentialTypeCode())) {
                if (migrationProcessDto.getMigrationPreConfigId() != null) {
                    componeName = "AWSReplatformMigration";
                } else {
                    if ("LINUX".equalsIgnoreCase(migrationProcessDto.getUname())) {
                        componeName = "AWSRehostMigration";
                    } else {
                        throw new RuntimeException(migrationProcessDto.getUname() + " is an unsupported OS Type for rehost migration.");
                    }
                }
            } else if ("GCP".equals(migrationProcessDto.getCredential().getCredentialTypeCode())) {
                if (migrationProcessDto.getMigrationPreConfigId() != null) {
                    componeName = "GCPReplatformMigration";
                } else {
                    if ("LINUX".equalsIgnoreCase(migrationProcessDto.getUname())) {
                        componeName = "GCPRehostMigration";
                    } else {
                        throw new RuntimeException(migrationProcessDto.getUname() + " is an unsupported OS Type for rehost migration.");
                    }
                }
            }
        }

        return componeName;
    }

    /**
     * Gets caused exception.
     *
     * @param e the e
     *
     * @return the caused exception
     */
    private Exception getCausedException(Exception e) {
        if (e.getCause() != null && e.getCause() instanceof Exception) {
            return getCausedException((Exception) e.getCause());
        }

        return e;
    }

    private Migration getMigrationBean(String componentName) {
        ApplicationContext applicationContext = CommonProperties.getApplicationContext();
        Migration migration = (Migration) applicationContext.getBean(componentName);

        if (migration != null) {
            return migration;
        } else {
            throw new RoRoException("Can't find " + componentName + " bean.");
        }
    }
}
//end of MigrationProcess.java
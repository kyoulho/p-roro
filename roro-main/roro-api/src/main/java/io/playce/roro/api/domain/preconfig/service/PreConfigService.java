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
 * SangCheon Park   Jan 21, 2022		    First Draft.
 */
package io.playce.roro.api.domain.preconfig.service;

import com.google.gson.Gson;
import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.ResourceNotFoundException;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.dto.inventory.process.InventoryProcessResponse;
import io.playce.roro.common.dto.preconfig.*;
import io.playce.roro.common.dto.targetcloud.CredentialDto.CredentialSimpleResponse;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.setting.SettingsHandler;
import io.playce.roro.common.util.GeneralCipherUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.common.credential.CredentialMapper;
import io.playce.roro.mybatis.domain.preconfig.PreConfigFileMapper;
import io.playce.roro.mybatis.domain.preconfig.PreConfigGroupMapper;
import io.playce.roro.mybatis.domain.preconfig.PreConfigMapper;
import io.playce.roro.mybatis.domain.preconfig.PreConfigUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.playce.roro.api.common.CommonConstants.YES;
import static io.playce.roro.common.dto.preconfig.PreConfigDto.*;
import static io.playce.roro.common.setting.SettingsConstants.RORO_MIGRATION_INCLUDE_SYSTEM_UID;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PreConfigService {

    private static final String DIRECTORY_TYPE = "d";
    private static final String FILE_TYPE = "f";
    private static final String LINK_TYPE = "l";

    private static final String DEF_INFO_KEY = "defInfo";
    private static final String GROUPS_KEY = "groups";
    private static final String USERS_KEY = "users";
    private static final String SHADOWS_KEY = "shadows";
    private static final String PROFILE_KEY = "profile";
    private static final String CRONTABS_KEY = "crontabs";

    private static final Gson gson = new Gson();

    private final InventoryProcessService inventoryProcessService;

    private final ModelMapper modelMapper;
    private final PreConfigMapper preConfigMapper;
    private final PreConfigUserMapper preConfigUserMapper;
    private final PreConfigGroupMapper preConfigGroupMapper;
    private final PreConfigFileMapper preConfigFileMapper;
    private final CredentialMapper credentialMapper;

    private final InventoryMasterRepository inventoryMasterRepository;
    private final ServerMasterRepository serverMasterRepository;
    private final CredentialMasterRepository credentialMasterRepository;
    private final MigrationPreConfigRepository migrationPreConfigRepository;
    private final MigrationPreConfigUserRepository migrationPreConfigUserRepository;
    private final MigrationPreConfigGroupRepository migrationPreConfigGroupRepository;
    private final MigrationPreConfigFileRepository migrationPreConfigFileRepository;
    private final InventoryProcessResultRepository inventoryProcessResultRepository;

    /**
     * <pre>
     * PreConfig 목록 조회
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @return
     */
    public List<PreConfigResponse> getPreConfigs(Long projectId, Long serverId) {
        List<PreConfigResponse> preConfigs = preConfigMapper.getPreConfigs(projectId, serverId);

        for (PreConfigResponse preConfig : preConfigs) {
            setPreConfigDetail(preConfig);
        }

        return preConfigs;
    }

    /**
     * <pre>
     * PreConfig 상세 조회
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @return
     */
    public PreConfigResponse getPreConfig(Long projectId, Long serverId, Long preConfigId) {
        inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        migrationPreConfigRepository.findByMigrationPreConfigIdAndServerInventoryId(preConfigId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("PreConfig ID : " + preConfigId + " Not Found in Server ID : " + serverId));

        PreConfigResponse preConfig = preConfigMapper.getPreConfig(projectId, serverId, preConfigId);
        setPreConfigDetail(preConfig);

        return preConfig;
    }

    /**
     * <pre>
     * PreConfig 등록
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param preConfigRequest
     * @param keyFile
     * @return
     */
    @SneakyThrows(IOException.class)
    @Transactional(rollbackFor = Exception.class)
    public PreConfigSimpleResponse createPreConfig(Long projectId, long serverId, PreConfigRequest preConfigRequest, MultipartFile keyFile) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        MigrationPreConfig preConfig = modelMapper.map(preConfigRequest, MigrationPreConfig.class);

        // Start business logic for create a preConfig
        // 1. pre-config 내에 credentialId가 있는 경우에는 imageId가 반드시 설정되어 있어야 하며, credentialId가 없는 경우에는 타깃 서버의 IP 주소가 있어야 한다.
        if (preConfig.getCredentialId() != null) {
            if (preConfig.getImageId() == null) {
                throw new RoRoApiException(ErrorCode.MIGRATION_IMAGE_ID_NOT_DEFINED);
            }
        } else {
            if (preConfig.getConnectIpAddress() == null) {
                throw new RoRoApiException(ErrorCode.MIGRATION_NOT_DEFINED_IP_ADDRESS);
            }
        }

        // Existing Linux에는 imageId가 없지만 private key를 사용할 수 있음
        // 2. keyFile이 업로드 된 경우 파일시스템에 저장한다.
        if (keyFile != null) {
            String fileNameExtension = FilenameUtils.getExtension(keyFile.getOriginalFilename());

            String hashFileName = RandomStringUtils.randomAlphanumeric(32);

            File destinationFile = new File(getPreConfigFilePath() + hashFileName + "." + fileNameExtension);
            String keyStr = IOUtils.toString(keyFile.getInputStream(), "UTF-8");

            if (!keyStr.startsWith("-----BEGIN RSA PRIVATE KEY")) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
            }

            if (keyStr.length() > 4096) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE_SIZE);
            }

            preConfig.setConnectUserPassword(null);
            preConfig.setKeyFileName(keyFile.getOriginalFilename());
            preConfig.setKeyFilePath(destinationFile.getAbsolutePath());

            FileUtils.forceMkdirParent(destinationFile);
            keyFile.transferTo(destinationFile);
        }

        if (StringUtils.isNotEmpty(preConfig.getConnectUserPassword())) {
            preConfig.setKeyFileName(null);
            preConfig.setKeyFilePath(null);
        }

        preConfig.setDeleteYn("N");
        preConfig.setRegistDatetime(new Date());
        preConfig.setRegistUserId(WebUtil.getUserId());
        preConfig.setModifyDatetime(new Date());
        preConfig.setModifyUserId(WebUtil.getUserId());
        preConfig = migrationPreConfigRepository.save(preConfig);

        // 3. User 설정 정보를 저장한다.
        for (PreConfigUserRequest configUser : preConfigRequest.getMigrationPreConfigUsers()) {
            MigrationPreConfigUser migrationPreConfigUser = modelMapper.map(configUser, MigrationPreConfigUser.class);
            migrationPreConfigUser.setMigrationPreConfigId(preConfig.getMigrationPreConfigId());
            migrationPreConfigUserRepository.save(migrationPreConfigUser);
        }

        // 4. Group 설정 정보를 저장한다.
        for (PreConfigGroupRequest configGroup : preConfigRequest.getMigrationPreConfigGroups()) {
            MigrationPreConfigGroup migrationPreConfigGroup = modelMapper.map(configGroup, MigrationPreConfigGroup.class);
            migrationPreConfigGroup.setMigrationPreConfigId(preConfig.getMigrationPreConfigId());
            migrationPreConfigGroupRepository.save(migrationPreConfigGroup);
        }

        // 5. File 설정 정보를 저장한다.
        List<MigrationPreConfigFile> migrationPreConfigFiles = new ArrayList<>();
        int sequence = 1;
        for (PreConfigFileRequest configFile : preConfigRequest.getMigrationPreConfigFiles()) {
            MigrationPreConfigFile migrationPreConfigFile = modelMapper.map(configFile, MigrationPreConfigFile.class);
            migrationPreConfigFile.setSequence(sequence++);
            migrationPreConfigFile.setMigrationPreConfigId(preConfig.getMigrationPreConfigId());
            migrationPreConfigFile = migrationPreConfigFileRepository.save(migrationPreConfigFile);
            migrationPreConfigFiles.add(migrationPreConfigFile);
        }

        // 6. 파일 목록에 대한 일괄 사이즈 확인
        try {
            ServerMaster serverMaster = serverMasterRepository.findById(serverId).orElse(null);
            CredentialMaster credentialMaster = credentialMasterRepository.findById(inventoryMaster.getCredentialId()).orElse(null);

            TargetHost targetHost = new TargetHost();
            targetHost.setIpAddress(serverMaster.getRepresentativeIpAddress());
            targetHost.setPort(serverMaster.getConnectionPort());
            targetHost.setUsername(credentialMaster.getUserName());
            targetHost.setPassword(GeneralCipherUtil.decrypt(credentialMaster.getUserPassword()));
            targetHost.setKeyFilePath(credentialMaster.getKeyFilePath());
            targetHost.setKeyString(credentialMaster.getKeyFileContent());

            checkFileSize(targetHost, migrationPreConfigFiles);
        } catch (Exception e) {
            log.warn("Unhandled exception occurred while check pre-config's file size.", e);
        }

        PreConfigSimpleResponse response = new PreConfigSimpleResponse();
        response.setPreConfigId(preConfig.getMigrationPreConfigId());
        response.setPreConfigName(preConfig.getConfigName());

        return response;
    }

    /**
     * <pre>
     * preConfig 수정
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param preConfigId
     * @param preConfigRequest
     * @param keyFile
     * @return
     */
    @SneakyThrows(IOException.class)
    @Transactional(rollbackFor = Exception.class)
    public PreConfigSimpleResponse modifyPreConfig(Long projectId, long serverId, long preConfigId, PreConfigRequest preConfigRequest, MultipartFile keyFile) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        MigrationPreConfig preConfig = migrationPreConfigRepository.findByMigrationPreConfigIdAndServerInventoryId(preConfigId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("PreConfig ID : " + preConfigId + " Not Found in Server ID : " + serverId));

        modelMapper.map(preConfigRequest, preConfig);

        // Start business logic for modify a preConfig
        // 1. pre-config 내에 credentialId가 있는 경우에는 imageId가 반드시 설정되어 있어야 하며, credentialId가 없는 경우에는 타깃 서버의 IP 주소가 있어야 한다.
        if (preConfig.getCredentialId() != null) {
            if (preConfig.getImageId() == null) {
                throw new RoRoApiException(ErrorCode.MIGRATION_IMAGE_ID_NOT_DEFINED);
            }
        } else {
            if (preConfig.getConnectIpAddress() == null) {
                throw new RoRoApiException(ErrorCode.MIGRATION_NOT_DEFINED_IP_ADDRESS);
            }
        }

        // Existing Linux에는 imageId가 없지만 private key를 사용할 수 있음
        // 2. keyFile이 업로드 된 경우 파일시스템에 저장한다.
        if (keyFile != null) {
            String fileNameExtension = FilenameUtils.getExtension(keyFile.getOriginalFilename());

            String hashFileName = RandomStringUtils.randomAlphanumeric(32);

            File destinationFile = new File(getPreConfigFilePath() + hashFileName + "." + fileNameExtension);
            String keyStr = IOUtils.toString(keyFile.getInputStream(), "UTF-8");

            if (!keyStr.startsWith("-----BEGIN RSA PRIVATE KEY")) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE);
            }

            if (keyStr.length() > 4096) {
                throw new RoRoApiException(ErrorCode.INVENTORY_SERVER_INVALID_KEY_FILE_SIZE);
            }

            preConfig.setConnectUserPassword(null);
            preConfig.setKeyFileName(keyFile.getOriginalFilename());
            preConfig.setKeyFilePath(destinationFile.getAbsolutePath());

            FileUtils.forceMkdirParent(destinationFile);
            keyFile.transferTo(destinationFile);
        }

        if (StringUtils.isNotEmpty(preConfig.getConnectUserPassword())) {
            preConfig.setKeyFileName(null);
            preConfig.setKeyFilePath(null);
        }

        preConfig.setModifyDatetime(new Date());
        preConfig.setModifyUserId(WebUtil.getUserId());
        preConfig = migrationPreConfigRepository.save(preConfig);

        // 3. 기존에 연결되어 있는것을 모두 삭제한다.
        migrationPreConfigUserRepository.deleteAllByMigrationPreConfigId(preConfigId);
        migrationPreConfigGroupRepository.deleteAllByMigrationPreConfigId(preConfigId);
        migrationPreConfigFileRepository.deleteAllByMigrationPreConfigId(preConfigId);

        // 4. User 설정 정보를 저장한다.
        for (PreConfigUserRequest configUser : preConfigRequest.getMigrationPreConfigUsers()) {
            MigrationPreConfigUser migrationPreConfigUser = modelMapper.map(configUser, MigrationPreConfigUser.class);
            migrationPreConfigUser.setMigrationPreConfigId(preConfig.getMigrationPreConfigId());
            migrationPreConfigUserRepository.save(migrationPreConfigUser);
        }

        // 5. Group 설정 정보를 저장한다.
        for (PreConfigGroupRequest configGroup : preConfigRequest.getMigrationPreConfigGroups()) {
            MigrationPreConfigGroup migrationPreConfigGroup = modelMapper.map(configGroup, MigrationPreConfigGroup.class);
            migrationPreConfigGroup.setMigrationPreConfigId(preConfig.getMigrationPreConfigId());
            migrationPreConfigGroupRepository.save(migrationPreConfigGroup);
        }

        // 6. File 설정 정보를 저장한다.
        List<MigrationPreConfigFile> migrationPreConfigFiles = new ArrayList<>();
        int sequence = 1;
        for (PreConfigFileRequest configFile : preConfigRequest.getMigrationPreConfigFiles()) {
            MigrationPreConfigFile migrationPreConfigFile = modelMapper.map(configFile, MigrationPreConfigFile.class);
            migrationPreConfigFile.setMigrationPreConfigId(preConfig.getMigrationPreConfigId());
            migrationPreConfigFile.setSequence(sequence++);
            migrationPreConfigFile = migrationPreConfigFileRepository.save(migrationPreConfigFile);
            migrationPreConfigFiles.add(migrationPreConfigFile);
        }

        // 7. 파일 목록에 대한 일괄 사이즈 확인
        try {
            ServerMaster serverMaster = serverMasterRepository.findById(serverId).orElse(null);
            CredentialMaster credentialMaster = credentialMasterRepository.findById(inventoryMaster.getCredentialId()).orElse(null);

            TargetHost targetHost = new TargetHost();
            targetHost.setIpAddress(serverMaster.getRepresentativeIpAddress());
            targetHost.setPort(serverMaster.getConnectionPort());
            targetHost.setUsername(credentialMaster.getUserName());
            targetHost.setPassword(GeneralCipherUtil.decrypt(credentialMaster.getUserPassword()));
            targetHost.setKeyFilePath(credentialMaster.getKeyFilePath());
            targetHost.setKeyString(credentialMaster.getKeyFileContent());

            checkFileSize(targetHost, migrationPreConfigFiles);
        } catch (Exception e) {
            log.warn("Unhandled exception occurred while check pre-config's file size.", e);
        }

        PreConfigSimpleResponse response = new PreConfigSimpleResponse();
        response.setPreConfigId(preConfig.getMigrationPreConfigId());
        response.setPreConfigName(preConfig.getConfigName());

        return response;
    }

    /**
     * <pre>
     * preConfig 삭제
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param preConfigId
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePreConfig(Long projectId, long serverId, long preConfigId) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        MigrationPreConfig preConfig = migrationPreConfigRepository.findByMigrationPreConfigIdAndServerInventoryId(preConfigId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("PreConfig ID : " + preConfigId + " Not Found."));

        preConfig.setDeleteYn(Domain101.Y.name());
        preConfig.setModifyDatetime(new Date());
        preConfig.setModifyUserId(WebUtil.getUserId());
    }

    /**
     * <pre>
     * preConfig 부가 정보를 추가한다.
     * </pre>
     *
     * @param preConfig
     */
    private void setPreConfigDetail(PreConfigResponse preConfig) {
        if (preConfig != null) {
            // set credential info
            preConfig.setCredential(new CredentialSimpleResponse(credentialMapper.getCredential(preConfig.getProjectId(), preConfig.getCredentialId())));

            // set preConfigUser info
            preConfig.setMigrationPreConfigUsers(preConfigUserMapper.getPreConfigUsers(preConfig.getMigrationPreConfigId()));

            // set preConfigGroup info
            preConfig.setMigrationPreConfigGroups(preConfigGroupMapper.getPreConfigGroups(preConfig.getMigrationPreConfigId()));

            // set preConfigFile info
            preConfig.setMigrationPreConfigFiles(preConfigFileMapper.getPreConfigFiles(preConfig.getMigrationPreConfigId()));
        }
    }

    /**
     * <pre>
     * pre-config 용 키파일이 저장될 파일 경로
     * </pre>
     *
     * @return
     */
    private String getPreConfigFilePath() {
        return CommonProperties.getWorkDir() + File.separator + "pre-configs" + File.separator;
    }

    /**
     * <pre>
     * Migration 대상 파일(디렉토리)의 사이즈를 조회한다.
     * </pre>
     *
     * @param targetHost              the targetHost
     * @param migrationPreConfigFiles the migrationPreConfigFiles
     */
    private void checkFileSize(TargetHost targetHost, List<MigrationPreConfigFile> migrationPreConfigFiles) {
        for (MigrationPreConfigFile file : migrationPreConfigFiles) {
            new Thread() {
                public void run() {
                    String path;
                    String command;
                    String result;

                    path = file.getSource();

                    if (!"root".equals(targetHost.getUsername())) {
                        command = "sudo ";
                    } else {
                        command = "";
                    }

                    try {
                        if (file.getType().toLowerCase().equals("directory")) {
                            if (!path.equals("/") && path.endsWith("/")) {
                                path = getParentPath(path);
                            }

                            command += "du -ks " + path + " | awk '{ printf \"%d\\n\", $1 * 1024 }'";
                        } else {
                            command += "ls -al " + path + " | awk ' { printf \"%d\\n\", $5}'";
                        }

                        result = SSHUtil.executeCommand(targetHost, command).trim();
                        file.setSize(Long.parseLong(result));

                        migrationPreConfigFileRepository.save(file);
                    } catch (RoRoApiException | InterruptedException e) {
                        log.error("Unhandled exception occurred.", e);
                    }
                }
            }.start();
        }
    }

    /**
     * @param path
     * @return
     */
    private String getParentPath(String path) {
        return path.substring(0, path.lastIndexOf("/"));
    }

    /**
     * @param projectId
     * @param serverId
     * @param preConfigId
     * @return
     */
    public File getKeyFile(Long projectId, Long serverId, Long preConfigId) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        MigrationPreConfig preConfig = migrationPreConfigRepository.findByMigrationPreConfigIdAndServerInventoryId(preConfigId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("PreConfig ID : " + preConfigId + " Not Found."));

        File keyFile = new File(preConfig.getKeyFilePath());

        if (!keyFile.exists()) {
            throw new RoRoApiException(ErrorCode.FAIL_DOWNLOAD_FILE, "The file does not exist.");
        }

        return keyFile;
    }

    /**
     * <pre>
     * 서버 사용자 및 그룹 조회
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @return
     */
    public PreConfigDto.UserGroups getUserGroups(Long projectId, Long serverId) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        InventoryProcessResponse inventoryProcess = inventoryProcessService.getLatestCompleteScanProcess(Domain1001.SVR.name(), serverId);

        checkAssessmentValidation(inventoryMaster, inventoryProcess);

        Map<String, Object> resultMap = null;
        Map<String, Object> defInfo = null;
        Map<String, Object> groupsMap = null;
        Map<String, Object> usersMap = null;
        Map<String, String> shadowsMap = null;

        InventoryProcessResult result = inventoryProcessResultRepository.findByInventoryProcessId(inventoryProcess.getInventoryProcessId());
        if (result != null) {
            resultMap = gson.fromJson(result.getInventoryProcessResultJson(), Map.class);
        }

        if (SettingsHandler.getSettingsValue(RORO_MIGRATION_INCLUDE_SYSTEM_UID).equalsIgnoreCase("false")
                && resultMap != null) {
            defInfo = (Map<String, Object>) resultMap.get(DEF_INFO_KEY);
        }

        if (resultMap != null) {
            groupsMap = (Map<String, Object>) resultMap.get(GROUPS_KEY);
            usersMap = (Map<String, Object>) resultMap.get(USERS_KEY);
            shadowsMap = (Map<String, String>) resultMap.get(SHADOWS_KEY);
        }

        if (defInfo == null) {
            defInfo = new HashMap<>();
            defInfo.put("uidMin", "1");
            defInfo.put("gidMin", "1");
            defInfo.put("uidMax", "60000");
            defInfo.put("gidMax", "60000");
        }

        if (groupsMap == null) {
            groupsMap = new HashMap<>();
        }
        if (usersMap == null) {
            usersMap = new HashMap<>();
        }
        if (shadowsMap == null) {
            shadowsMap = new HashMap<>();
        }

        return PreConfigDto.UserGroups.builder()
                .groups(convertMapToGroup(groupsMap, defInfo))
                .users(convertMapToUser(usersMap, shadowsMap, defInfo))
                .build();
    }

    /**
     * <pre>
     * Profile 조회
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param username
     * @return
     */
    public PreConfigDto.Profile getUserProfile(Long projectId, Long serverId, String username) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        InventoryProcessResponse inventoryProcess = inventoryProcessService.getLatestCompleteScanProcess(Domain1001.SVR.name(), serverId);

        checkAssessmentValidation(inventoryMaster, inventoryProcess);

        Map<String, Object> resultMap = null;
        Map<String, Object> usersMap = null;
        Map<String, String> subUserMap = null;

        InventoryProcessResult result = inventoryProcessResultRepository.findByInventoryProcessId(inventoryProcess.getInventoryProcessId());
        if (result != null) {
            resultMap = gson.fromJson(result.getInventoryProcessResultJson(), Map.class);

            if (resultMap != null) {
                usersMap = (Map<String, Object>) resultMap.get(USERS_KEY);
                subUserMap = (Map<String, String>) usersMap.get(username);
            }
        }

        PreConfigDto.Profile profile = new PreConfigDto.Profile();

        if (subUserMap != null) {
            profile.setProfile(subUserMap.get(PROFILE_KEY));
        }

        return profile;
    }

    /**
     * <pre>
     * Crontab 조회
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @return
     */
    public List<PreConfigDto.Crontab> getCrontabList(Long projectId, Long serverId) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        InventoryProcessResponse inventoryProcess = inventoryProcessService.getLatestCompleteScanProcess(Domain1001.SVR.name(), serverId);

        checkAssessmentValidation(inventoryMaster, inventoryProcess);

        Map<String, Object> resultMap = null;
        Map<String, String> crontabMap = null;

        InventoryProcessResult result = inventoryProcessResultRepository.findByInventoryProcessId(inventoryProcess.getInventoryProcessId());
        if (result != null) {
            resultMap = gson.fromJson(result.getInventoryProcessResultJson(), Map.class);

            if (resultMap != null) {
                crontabMap = (Map<String, String>) resultMap.get(CRONTABS_KEY);
            }
        }

        return convertMapToCrontab(crontabMap);
    }

    /**
     * <pre>
     * Pre-Configuration을 하기 위해 path 하위의 디렉토리/파일/심볼릭링크 목록을 가져온다.
     * </pre>
     *
     * @param projectId
     * @param serverId
     * @param path
     * @return
     */
    public List<PreConfigDto.File> getFileList(Long projectId, Long serverId, String path) {
        InventoryMaster inventoryMaster = inventoryMasterRepository.findByProjectIdAndInventoryId(projectId, serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Server ID : " + serverId + " Not Found in Project ID : " + projectId));

        List<PreConfigDto.File> configFileList = new ArrayList<>();

        try {
            ServerMaster serverMaster = serverMasterRepository.findById(serverId).orElse(null);
            CredentialMaster credentialMaster = credentialMasterRepository.findById(inventoryMaster.getCredentialId()).orElse(null);

            TargetHost targetHost = new TargetHost();
            targetHost.setIpAddress(serverMaster.getRepresentativeIpAddress());
            targetHost.setPort(serverMaster.getConnectionPort());
            targetHost.setUsername(credentialMaster.getUserName());
            targetHost.setPassword(GeneralCipherUtil.decrypt(credentialMaster.getUserPassword()));
            targetHost.setKeyFilePath(credentialMaster.getKeyFilePath());
            targetHost.setKeyString(credentialMaster.getKeyFileContent());

            List<String> directoryList = getFileList(targetHost, DIRECTORY_TYPE, path);
            List<String> fileList = getFileList(targetHost, FILE_TYPE, path);
            List<String> linkList = getFileList(targetHost, LINK_TYPE, path);

            PreConfigDto.File file = null;
            for (String name : directoryList) {
                if (!StringUtils.isEmpty(name)) {
                    file = new PreConfigDto.File();
                    file.setSource(name.replaceAll("/./", "/"));
                    file.setOwnerGroup("root");
                    file.setOwnerUser("root");
                    file.setType("directory");
                    configFileList.add(file);
                }
            }

            for (String name : fileList) {
                if (!StringUtils.isEmpty(name)) {
                    file = new PreConfigDto.File();
                    file.setSource(name.replaceAll("/./", "/"));
                    file.setOwnerGroup("root");
                    file.setOwnerUser("root");
                    file.setType("file");
                    configFileList.add(file);
                }
            }

            for (String name : linkList) {
                if (!StringUtils.isEmpty(name)) {
                    file = new PreConfigDto.File();
                    file.setSource(name.replaceAll("/./", "/"));
                    file.setOwnerGroup("root");
                    file.setOwnerUser("root");
                    file.setType("link");
                    configFileList.add(file);
                }
            }
        } catch (Exception e) {
            log.warn("Unhandled exception occurred while get file list.", e);
        }

        return configFileList;
    }

    /**
     * <pre>
     *  <ul>
     *     <li>Directory : d</li>
     *     <li>File : f</li>
     *     <li>Link : l</li>
     *  </ul>
     * </pre>
     *
     * @param targetHost
     * @param type
     * @param path
     * @return
     */
    private List<String> getFileList(TargetHost targetHost, String type, String path) {
        List<String> fileList = null;

        try {
            if (!path.endsWith("/")) {
                path += "/";
            }

            String command = "sudo find " + path + ". \\( ! -name . -prune \\) -type " + type;
            String result = SSHUtil.executeCommand(targetHost, command);
            fileList = Arrays.asList(result.split("\\r\\n|\\n|\\r"));
        } catch (Exception e) {
            log.error("Unhandled exception occurred while get files.", e);
            throw new RoRoException(e.getMessage());
        }

        return fileList;
    }

    private void checkAssessmentValidation(InventoryMaster inventoryMaster, InventoryProcessResponse inventoryProcess) {
        ServerMaster serverMaster = serverMasterRepository.findById(inventoryMaster.getInventoryId()).orElse(null);

        if (serverMaster.getWindowsYn().equals(YES)) {
            throw new RoRoApiException(ErrorCode.MIGRATION_WINDOWS_NOT_SUPPORTED);
        }

        if (inventoryProcess == null) {
            throw new RoRoApiException(ErrorCode.MIGRATION_ASSESSMENT_NOT_FOUND);
        }
    }
}
//end of PreConfigService.java
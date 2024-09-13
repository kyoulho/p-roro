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
 * SangCheon Park   Mar 14, 2022		    First Draft.
 */
package io.playce.roro.mig;

import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.common.dto.migration.enums.StatusType;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.setting.SettingsHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import static io.playce.roro.common.setting.SettingsConstants.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Component
@Slf4j
public class MigrationManager implements InitializingBean {

    // TODO 평균값으로 변환
    private static int DOWNLOAD_SPEED = 18;
    private static int UPLOAD_SPEED = 10;

    private static File linuxMigrationFile;
    private static File migrationFileDownloaderFile;
    private static File migrationFileUploaderFile;
    private static File cancelMigrationFile;
    private static String workDir;
    private static Boolean directoryRemove;
    private static String bucketName;
    private static String bucketRegion;
    private static Boolean bucketRemove;

    @Override
    public void afterPropertiesSet() throws InterruptedException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        linuxMigrationFile = new File(tmpDir, "scripts/linux_migration");
        migrationFileDownloaderFile = new File(tmpDir, "scripts/migration_file_downloader.py");
        migrationFileUploaderFile = new File(tmpDir, "scripts/migration_file_uploader.py");
        cancelMigrationFile = new File(tmpDir, "scripts/cancel_migration.sh");

        try {
            FileUtils.copyURLToFile(getClass().getClassLoader().getResource("scripts/linux_migration"), linuxMigrationFile);
            FileUtils.copyURLToFile(getClass().getClassLoader().getResource("scripts/migration_file_downloader.py"), migrationFileDownloaderFile);
            FileUtils.copyURLToFile(getClass().getClassLoader().getResource("scripts/migration_file_uploader.py"), migrationFileUploaderFile);
            FileUtils.copyURLToFile(getClass().getClassLoader().getResource("scripts/cancel_migration.sh"), cancelMigrationFile);

            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);

            Files.setPosixFilePermissions(linuxMigrationFile.toPath(), perms);
            Files.setPosixFilePermissions(migrationFileDownloaderFile.toPath(), perms);
            Files.setPosixFilePermissions(migrationFileUploaderFile.toPath(), perms);
            Files.setPosixFilePermissions(cancelMigrationFile.toPath(), perms);

            log.debug("Migration scripts file copied to " + linuxMigrationFile.getParentFile().getAbsolutePath());
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception occurred while copy migration scripts.", e);
        }
    }

    /**
     * Gets work dir.
     *
     * @return the work dir
     */
    public static String getWorkDir() {
        if (workDir == null || "null".equals(workDir)) {
            workDir = CommonProperties.getWorkDir() + File.separator + "migration";
        }

        return workDir;
    }

    /**
     * @return the linuxMigrationFile
     */
    public static File getLinuxMigrationFile() {
        return linuxMigrationFile;
    }

    /**
     * Gets aix file download file.
     *
     * @return the aix file download file
     */
    public static File getMigrationFileDownloaderFile() {
        return migrationFileDownloaderFile;
    }

    /**
     * Gets aix file upload file.
     *
     * @return the aix file upload file
     */
    public static File getMigrationFileUploaderFile() {
        return migrationFileUploaderFile;
    }

    /**
     * Gets cancel migration file.
     *
     * @return the cancel migration file
     */
    public static File getCancelMigrationFile() {
        return cancelMigrationFile;
    }

    /**
     * @return the directoryRemove
     */
    public static Boolean getDirectoryRemove() {
        return SettingsHandler.getSettingsValue(RORO_MIGRATION_DIR_REMOVE).equalsIgnoreCase("true");
    }

    @Value("${roro.migration.dir.remove}")
    public void setDirectoryRemove(Boolean remove) {
        directoryRemove = remove;
    }

    /**
     * @return the bucketName
     */
    public static String getBucketName() {
        return SettingsHandler.getSettingsValue(RORO_MIGRATION_BUCKET_NAME);
    }

    @Value("${roro.migration.bucket.name}")
    public void setBucketName(String name) {
        bucketName = name;
    }

    /**
     * @return the bucketRegion
     */
    public static String getBucketRegion() {
        return SettingsHandler.getSettingsValue(RORO_MIGRATION_BUCKET_REGION);
    }

    @Value("${roro.migration.bucket.region}")
    public void setBucketRegion(String region) {
        bucketRegion = region;
    }

    /**
     * @return the bucketRemove
     */
    public static Boolean getBucketRemove() {
        return SettingsHandler.getSettingsValue(RORO_MIGRATION_BUCKET_REMOVE).equalsIgnoreCase("true");
    }

    @Value("${roro.migration.bucket.remove}")
    public void setBucketRemove(Boolean remove) {
        bucketRemove = remove;
    }

    /**
     * @param migrationProcessDto
     * @return
     */
    public static Integer getEstimateTime(MigrationProcessDto migrationProcessDto) {
        int estimate = 0;

        if (StringUtils.isNotEmpty(migrationProcessDto.getImageId())) {
            // AMI가 있는 상태로 인스턴스 구동부터 실행
            // TODO Preconfig FileSize에 따른 시간 계산 필요
            estimate = 15;
        } else {
            long rootVolumeSize = 0L;
            long extraVolumeSize = 0L;
            for (MigrationProcessDto.Volume volume : migrationProcessDto.getVolumes()) {
                if ("Y".equals(volume.getRootYn())) {
                    rootVolumeSize += volume.getVolumeSize();
                } else {
                    extraVolumeSize += volume.getVolumeSize();
                }
            }

            if (migrationProcessDto.getMigrationPreConfig() != null) {
                MigrationProcessDto.MigrationPreConfig migrationPreConfig = migrationProcessDto.getMigrationPreConfig();

                Long fileSize = 0L;
                for (MigrationProcessDto.MigrationPreConfigFile file : migrationPreConfig.getMigrationPreConfigFiles()) {
                    fileSize += file.getSize();
                }

                // 파일 다운로드 시간
                estimate += (fileSize / 1024 / 1024 / DOWNLOAD_SPEED / 60);

                // 파일 업로드 시간
                estimate += (fileSize / 1024 / 1024 / UPLOAD_SPEED / 60);

                // Instance 생성 및 Image 생성 시간
                estimate += (rootVolumeSize + extraVolumeSize) / 2;

                if (estimate < 20) {
                    estimate = 20;
                }
            } else {
                if (migrationProcessDto.getServerSummary() != null && migrationProcessDto.getServerSummary().getDiskInfos() != null) {
                    Long usedSize = 0L;
                    for (MigrationProcessDto.DiskInfo diskInfo : migrationProcessDto.getServerSummary().getDiskInfos()) {
                        usedSize += (diskInfo.getTotalSize() - diskInfo.getFreeSize());
                    }

                    // RAW 이미지 생성 시간
                    estimate += (usedSize / DOWNLOAD_SPEED / 60);

                    // 버킷 업로드 시간
                    estimate += (rootVolumeSize + extraVolumeSize) * 1024 / UPLOAD_SPEED / 60;

                    // Converting 시간
                    estimate += (rootVolumeSize + extraVolumeSize) * 1.4;

                    if ("AWS".equals(migrationProcessDto.getCredential().getCredentialTypeCode())) {
                        // Image 생성 및 인스턴스 시작 시간
                        estimate += (rootVolumeSize + extraVolumeSize) / 2;
                    }
                } else {
                    estimate += (rootVolumeSize * 4);
                    estimate += (extraVolumeSize);
                }

                if ("GCP".equals(migrationProcessDto.getCredential().getCredentialTypeCode())) {
                    // tar.gz 압축 시간
                    estimate += (rootVolumeSize + extraVolumeSize);
                }
            }
        }

        log.debug("[{}] Migration estimated {}(m).", migrationProcessDto.getInventoryProcessId(), estimate);

        return estimate;
    }

    public static Double getProgress(MigrationProcessDto migrationProcessDto) throws InterruptedException {
        Double progress = 0.0;
        Long diskSize = migrationProcessDto.getDiskSize();
        Long usedSize = migrationProcessDto.getUsedSize();

        try {
            // IN_PROGRESS
            if (diskSize == 0L) {
                if (migrationProcessDto.getVolumes() != null && migrationProcessDto.getVolumes().size() > 0) {
                    for (MigrationProcessDto.Volume volume : migrationProcessDto.getVolumes()) {
                        diskSize += (volume.getVolumeSize() * 1024);
                    }

                    migrationProcessDto.setDiskSize(diskSize);
                }
            }

            if (migrationProcessDto.getMigrationPreConfig() != null) {
                // Replatform
                if (usedSize == 0L) {
                    if (migrationProcessDto.getMigrationPreConfig().getMigrationPreConfigFiles() != null) {
                        for (MigrationProcessDto.MigrationPreConfigFile configFile : migrationProcessDto.getMigrationPreConfig().getMigrationPreConfigFiles()) {
                            usedSize += configFile.getSize();
                        }

                        usedSize /= (1024 * 1024);
                    } else {
                        usedSize = diskSize;
                    }

                    migrationProcessDto.setUsedSize(usedSize);
                }
                if (migrationProcessDto.getCredential() != null) {
                    if ("AWS".equals(migrationProcessDto.getCredential().getCredentialTypeCode())) {
                        // AWS
                        if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATE_RAW_FILES)) {
                            float totalSec = (usedSize / DOWNLOAD_SPEED);
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 45.0;
                            progress = (subPercentage > 45.0 ? 45.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATED_RAW_FILES)
                                || migrationProcessDto.getInternalStatus().equals(StatusType.UPLOAD_TO_S3)) {
                            progress = 45.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATING_INSTANCE)) {
                            float totalSec = 200;
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 5.0;
                            progress = 45.0 + (subPercentage > 5.0 ? 5.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.DOWNLOAD_FROM_S3)) {
                            float totalSec = (usedSize / UPLOAD_SPEED);
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 45.0;
                            progress = 50.0 + (subPercentage > 45.0 ? 45.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATING_AMI)) {
                            progress = 95.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.COMPLETED)) {
                            progress = 100.0;
                        }
                    } else if ("GCP".equals(migrationProcessDto.getCredential().getCredentialTypeCode())) {
                        // GCP
                        if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATE_RAW_FILES)) {
                            float totalSec = (usedSize / DOWNLOAD_SPEED);
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 45.0;
                            progress = (subPercentage > 45.0 ? 45.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATED_RAW_FILES)
                                || migrationProcessDto.getInternalStatus().equals(StatusType.UPLOAD_TO_STORAGE)
                                || migrationProcessDto.getInternalStatus().equals(StatusType.CREATING_DISK)) {
                            progress = 45.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATING_INSTANCE)) {
                            float totalSec = 200;
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 50.0;
                            progress = 45.0 + (subPercentage > 50.0 ? 50.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.DOWNLOAD_FROM_S3)) {
                            float totalSec = (usedSize / UPLOAD_SPEED);
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 45.0;
                            progress = 50.0 + (subPercentage > 45.0 ? 45.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATING_MACHINE_IMAGE)) {
                            progress = 95.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.COMPLETED)) {
                            progress = 100.0;
                        }
                    }
                } else {
                    // Custom
                    if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATE_RAW_FILES)) {
                        float totalSec = (usedSize / DOWNLOAD_SPEED);
                        float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                        double subPercentage = (diff / totalSec) * 50.0;
                        progress = (subPercentage > 45.0 ? 45.0 : subPercentage);
                    } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATED_RAW_FILES)) {
                        progress = 50.0;
                    } else if (migrationProcessDto.getInternalStatus().equals(StatusType.INITIATE_INSTANCE)
                            || migrationProcessDto.getInternalStatus().equals(StatusType.DOWNLOAD_FROM_S3)) {
                        float totalSec = 200;
                        float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                        double subPercentage = (diff / totalSec) * 50.0;
                        progress = 50.0 + (subPercentage > 50.0 ? 50.0 : subPercentage);
                    } else if (migrationProcessDto.getInternalStatus().equals(StatusType.COMPLETED)) {
                        progress = 100.0;
                    }
                }
            } else {
                // Rehost
                if (usedSize == 0L) {
                    if (migrationProcessDto.getServerSummary() != null && migrationProcessDto.getServerSummary().getDiskInfos() != null) {
                        for (MigrationProcessDto.DiskInfo diskInfo : migrationProcessDto.getServerSummary().getDiskInfos()) {
                            usedSize += (diskInfo.getTotalSize() - diskInfo.getFreeSize());
                        }
                    } else {
                        usedSize = diskSize;
                    }

                    migrationProcessDto.setUsedSize(usedSize);
                }
                if (migrationProcessDto.getCredential() != null) {
                    if ("AWS".equals(migrationProcessDto.getCredential().getCredentialTypeCode())) {
                        // AWS
                        if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATE_RAW_FILES)) {
                            float totalSec = (usedSize / DOWNLOAD_SPEED);
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 20.0;
                            progress = (subPercentage > 20.0 ? 20.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATED_RAW_FILES)) {
                            progress = 20.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.UPLOAD_TO_S3)) {
                            float totalSec = (diskSize / UPLOAD_SPEED);
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 30.0;
                            progress = 20.0 + (subPercentage > 30.0 ? 30.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.DOWNLOAD_FROM_S3)
                                || migrationProcessDto.getInternalStatus().equals(StatusType.CONVERTING)
                                || migrationProcessDto.getInternalStatus().equals(StatusType.INITIATE_INSTANCE)) {
                            float totalSec = (diskSize / 1024) * 60; // 1GB 당 60초
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 20.0;
                            progress = 50.0 + (subPercentage > 20.0 ? 20.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.ATTACHING_VOLUME)
                                || migrationProcessDto.getInternalStatus().equals(StatusType.ATTACHED_VOLUME)) {
                            progress = 80.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATING_AMI)) {
                            float totalSec = (diskSize / 1024) * 30;  // 1GB 당 30초
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 10.0;
                            progress = 80.0 + (subPercentage > 10.0 ? 10.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATED_AMI)) {
                            progress = 90.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.TERMINATING_INSTANCE)) {
                            progress = 92.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.TERMINATED_INSTANCE)) {
                            progress = 94.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATING_INSTANCE)) {
                            progress = 97.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.COMPLETED)) {
                            progress = 100.0;
                        }
                    } else if ("GCP".equals(migrationProcessDto.getCredential().getCredentialTypeCode())) {
                        // GCP
                        if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATE_RAW_FILES)) {
                            float totalSec = (usedSize / DOWNLOAD_SPEED);
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 20.0;
                            progress = (subPercentage > 20.0 ? 20.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATED_RAW_FILES)) {
                            progress = 20.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.COMPRESSING)) {
                            float totalSec = (usedSize / 1024) * 60;
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 25.0;
                            progress = 20.0 + (subPercentage > 25.0 ? 25.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.UPLOAD_TO_STORAGE)) {
                            float totalSec = (diskSize / UPLOAD_SPEED);
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 15.0;
                            progress = 45.0 + (subPercentage > 15.0 ? 15.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATING_DISK_IMAGE) ||
                                migrationProcessDto.getInternalStatus().equals(StatusType.CREATED_DISK_IMAGE)) {
                            float totalSec = 30;
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 15.0;
                            progress = 60.0 + (subPercentage > 15.0 ? 15.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATING_DISK)) {
                            float totalSec = 30;
                            float diff = (System.currentTimeMillis() - migrationProcessDto.getLastStatusChanged()) / 1000;
                            double subPercentage = (diff / totalSec) * 15.0;
                            progress = 75.0 + (subPercentage > 15.0 ? 15.0 : subPercentage);
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATED_DISK)) {
                            progress = 85.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATING_MACHINE_IMAGE)) {
                            progress = 90.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.TERMINATING_INSTANCE)) {
                            progress = 92.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.TERMINATED_INSTANCE)) {
                            progress = 94.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.CREATING_INSTANCE)) {
                            progress = 97.0;
                        } else if (migrationProcessDto.getInternalStatus().equals(StatusType.COMPLETED)) {
                            progress = 100.0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            RoRoException.checkInterruptedException(e);
            log.error("Unhandled exception occurred while get progress.", e);
        }

        return progress;
    }
}
//end of MigrationManager.java
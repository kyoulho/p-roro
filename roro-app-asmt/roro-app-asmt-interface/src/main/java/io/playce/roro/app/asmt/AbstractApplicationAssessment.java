/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * SangCheon Park   Nov 12, 2021		    First Draft.
 */
package io.playce.roro.app.asmt;

import io.playce.roro.app.asmt.result.ApplicationAssessmentResult;
import io.playce.roro.app.asmt.util.ApplicationFileUtil;
import io.playce.roro.common.dto.assessment.ApplicationDto;
import io.playce.roro.common.dto.assessment.InventoryProcessDto;
import io.playce.roro.common.exception.InsufficientException;
import io.playce.roro.common.exception.NotsupportedException;
import io.playce.roro.common.exception.UnauthorizedException;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.common.util.FileUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.WinRmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public abstract class AbstractApplicationAssessment implements ApplicationAssessment {

    /**
     * Discover application
     */
    protected abstract void discover(ApplicationAssessmentResult result, InventoryProcessDto inventoryProcess) throws Exception;

    /**
     * Initializing for application assessment
     *
     * @param workDir
     * @param applicationId
     * @param assessmentId
     * @param result
     */
    protected void initialize(String workDir, Long applicationId, Long assessmentId, ApplicationAssessmentResult result) {
        log.debug("Before initialize() in AbstractApplicationAssessment");

        String applicationDir;
        String assessmentDir;

        if (workDir.endsWith(File.separator)) {
            applicationDir = FilenameUtils.separatorsToSystem(workDir + "application" + File.separator);
        } else {
            applicationDir = FilenameUtils.separatorsToSystem(workDir + File.separator + "application" + File.separator);
        }

        if (FileUtils.getTempDirectoryPath().endsWith(File.separator)) {
            assessmentDir = FilenameUtils.separatorsToSystem(FileUtils.getTempDirectoryPath() + "assessment" + File.separator);
        } else {
            assessmentDir = FilenameUtils.separatorsToSystem(FileUtils.getTempDirectoryPath() + File.separator + "assessment" + File.separator);
        }

        if (applicationId == null) {
            applicationDir += UUID.randomUUID().toString();
        } else {
            applicationDir += applicationId;
        }

        if (assessmentId == null) {
            assessmentDir += UUID.randomUUID().toString();
        } else {
            assessmentDir += assessmentId;
        }

        try {
            FileUtils.forceMkdir(new File(applicationDir));
            FileUtils.forceMkdir(new File(assessmentDir));

            ApplicationFileUtil.chmod(applicationDir, "777");
            ApplicationFileUtil.chmod(assessmentDir, "777");

            // 이전 Assessment 결과가 존재한다면 초기화
            try {
                FileUtils.cleanDirectory(new File(assessmentDir));
            } catch (Exception e) {
                cleanDirectory(assessmentDir);
            }
        } catch (IOException e) {
            // ignore
        }

        result.setApplicationDir(applicationDir);
        result.setAssessmentDir(assessmentDir);
    }

    /**
     * Download application file
     *
     * @param application
     * @param result
     *
     * @throws Exception
     */
    protected void download(ApplicationDto application, ApplicationAssessmentResult result) throws Exception {
        boolean isSupportedTypes;
        String applicationUri = application.getSourceLocationUri();
        String applicationFile = null;

        // 자동 등록된 Application의 경우 deployPath만 저장될 수 있으며 해당 정보를 applicationUri로 사용한다.
        if (StringUtils.isEmpty(application.getUploadSourceFilePath()) && StringUtils.isEmpty(application.getSourceLocationUri())) {
            if (StringUtils.isNotEmpty(application.getDeployPath())) {
                applicationUri = application.getDeployPath();
            }
        }

        if (applicationUri != null && !"".equals(applicationUri)) {
            if ("/".equals(applicationUri) || "file:/".equals(applicationUri) || "file:///".equals(applicationUri)
                    || applicationUri.matches("^[a-zA-Z]:/") || applicationUri.matches("^[a-zA-Z]:\\\\")) {
                // throw new Exception("Application(" + applicationUri + ") file path is invalid.");
                throw new InsufficientException("Application(" + applicationUri + ") file path is invalid.");
            }

            // ApplicationUri가 있는 경우 기존 디렉토리를 비운다.
            try {
                FileUtils.cleanDirectory(new File(result.getApplicationDir()));
            } catch (Exception e) {
                cleanDirectory(result.getApplicationDir());
            }

            // Exploded application support
            isSupportedTypes = true;

            if (applicationUri.startsWith("/") || applicationUri.matches("^[a-zA-Z]:.*$")) {
                if ("Y".equals(application.getWindowsYn())) {
                    String r = WinRmUtils.executeCommand(application.getTargetHost(), "IF EXIST \"" + applicationUri + "\" (echo EXIST) ELSE (echo NOT_EXIST)").trim();
                    if (!"EXIST".equals(r)) {
                        //throw new Exception("Application(" + applicationUri + ") does not exist or has no permission to read.");
                        throw new UnauthorizedException("Application(" + applicationUri + ") does not exist or has no permission to read.");
                    }

                    applicationFile = ApplicationFileUtil.remoteCopyWithPscp(application, applicationUri, result.getApplicationDir(), result);
                } else {
                    String r = SSHUtil.executeCommand(application.getTargetHost(), "sudo sh -c \"[ -f '" + applicationUri + "' ] || [ -d '" + applicationUri + "' ] && echo 'EXIST' || echo 'NOT_EXIST'\"").trim();
                    if (!"EXIST".equals(r)) {
                        // throw new Exception("Application(" + applicationUri + ") does not exist or has no permission to read.");
                        throw new UnauthorizedException("Application(" + applicationUri + ") does not exist or has no permission to read.");
                    }

                    // applicationFile = ApplicationFileUtil.remoteCopy(targetHost, applicationUri, result.getApplicationDir(), result);
                    applicationFile = ApplicationFileUtil.remoteCopyWithTar(application.getTargetHost(), applicationUri, result.getApplicationDir(), result);
                }

                if (StringUtils.isNotEmpty(applicationFile)) {
                    File f = new File(applicationFile);

                    if (!f.exists()) {
                        // throw new Exception("Application(" + applicationUri + ") download failed. Please check the application is exist and not empty.");
                        throw new InsufficientException("Application(" + applicationUri + ") download failed. Please check the application is exist and not empty or sshpass and python packages installed.");
                    }

                    if (f.isDirectory() && (f.listFiles() == null || f.listFiles().length == 0)) {
                        // throw new Exception("Application(" + applicationUri + ") download failed. Please check the application is exist and not empty.");
                        throw new InsufficientException("Application(" + applicationUri + ") download failed. Please check the application is exist and not empty or sshpass and python packages installed.");
                    }

                    if (f.isFile() && f.length() <= 0L) {
                        // throw new Exception("Application(" + applicationUri + ") is empty.");
                        throw new InsufficientException("Application(" + applicationUri + ") is empty.");
                    }
                }
            } else {
                applicationFile = FileUtil.download(applicationUri, result.getApplicationDir());
            }

            result.setFileName(FilenameUtils.getName(applicationUri));
        } else {
            applicationFile = application.getUploadSourceFilePath();
        }

        isSupportedTypes = applicationFile.toLowerCase().endsWith(".zip") || applicationFile.toLowerCase().endsWith(".tar.gz") ||
                applicationFile.toLowerCase().endsWith(".ear") || applicationFile.toLowerCase().endsWith(".war") ||
                applicationFile.toLowerCase().endsWith(".jar");

        if (new File(applicationFile).isFile() && !isSupportedTypes) {
            // throw new Exception("Application does support only zip, tar.gz, ear, war and jar.");
            throw new NotsupportedException("Application does support only zip, tar.gz, ear, war and jar.");
        }

        result.setApplicationFile(applicationFile);
        log.debug("Application saved to [{}].", applicationFile);
    }

    /**
     * Extract application archive file
     *
     * @param result
     *
     * @throws Exception
     */
    protected void extract(ApplicationAssessmentResult result) throws Exception {
        File f = new File(result.getApplicationFile());

        if (f.exists()) {
            if (f.isFile()) {
                ApplicationFileUtil.unzip(result.getApplicationFile(), result.getAssessmentDir(), result);
                log.debug("Application extracted to [{}].", result.getAssessmentDir());
            } else {
                result.setAssessmentDir(result.getApplicationFile());
                log.debug("Application({}) is a directory and doesn't need to extract.", result.getApplicationFile());
            }
        } else {
            // throw new Exception("Application(" + result.getApplicationFile() + ") does not exist.");
            throw new InsufficientException("Application(" + result.getApplicationFile() + ") does not exist.");
        }
    }

    /**
     * clean directory
     *
     * @param filePath
     */
    protected void cleanDirectory(String filePath) {
        try {
            if (filePath.startsWith(CommonProperties.getWorkDir())) {
                // App Scan ID에 해당하는 directory를 초기화 한다. ID 값이 디렉토리가 아닌 파일인 경우를 위해 ID 자체를 지우고 디렉토리를 생성한다.
                CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                        CommandUtil.findCommand("rm"),
                        "-rf",
                        filePath);

                CommandUtil.executeCommand(cl);

                FileUtils.forceMkdir(new File(filePath));
                ApplicationFileUtil.chmod(filePath, "777");
            }
        } catch (Exception e) {
            log.error("Shell execution error while clean directory. Error Log => [{}]", e.getMessage());
        }
    }

    /**
     * Remove.
     */
    protected void remove(String dir) {
        ApplicationFileUtil.rm(dir);
    }
}
//end of AbstractApplicationAssessment.java
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
 * Sang-cheon Park	2020. 3. 2.		First Draft.
 */
package io.playce.roro.app.asmt.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import io.playce.roro.app.asmt.result.ApplicationAssessmentResult;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.ManifestUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.Vector;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Sang-cheon Park
 * @version 1.0
 */
@Slf4j
public class ApplicationSSHUtil {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationSSHUtil.class);


    /**
     * Get dir.
     *
     * @param targetHost the target host
     * @param source     the source
     * @param target     the target
     *
     * @throws RoRoException the ro ro exception
     */
    public static void getDir(TargetHost targetHost, String source, String target, ApplicationAssessmentResult result) throws RoRoException {
        Session session = null;
        Channel channel = null;

        try {
            target = FilenameUtils.separatorsToSystem(target);

            logger.debug("[{}] will be saved to [{}]",
                    targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + source,
                    target);

            //JSch jsch = new JSch();
            session = getSession(targetHost, 20 * 1000);

            channel = session.openChannel("sftp");
            channel.connect();
            ((ChannelSftp) channel).setFilenameEncoding("UTF-8");

            File targetFile = new File(target);
            if (!((ChannelSftp) channel).lstat(source).isDir() && !target.endsWith("/")) {
                targetFile = targetFile.getParentFile();
            }

            FileUtils.forceMkdir(targetFile);
            ((ChannelSftp) channel).lcd(targetFile.getAbsolutePath());
            getDir((ChannelSftp) channel, source, target, target, result);

            logger.debug("Transfer completed to [{}].", target);
        } catch (Exception e) {
            logger.error("Unhandled exception occurred during getDir().", e);
            throw new RoRoException(e.getMessage(), e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }

            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * Get dir.
     *
     * @param sftpChannel
     * @param sourcePath
     * @param destPath
     * @param baseDir
     * @param result
     *
     * @throws SftpException
     * @throws IOException
     */
    private static void getDir(ChannelSftp sftpChannel, String sourcePath, String destPath, String baseDir, ApplicationAssessmentResult result) throws Exception {
        Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(sourcePath);

        File destFile = null;
        Set<PosixFilePermission> perms = null;
        if (list.size() == 1 && !list.get(0).getAttrs().isDir()) {
            try {
                if (destPath.endsWith("/")) {
                    destFile = new File(destPath, list.get(0).getFilename());
                } else {
                    destFile = new File(destPath);
                }

                sftpChannel.get(sourcePath, destPath);

                // 압축 또는 아카이브 형태의 Application 파일인 경우는 1차 분석이 압축 해제 시 진행될 수 있도록 무시한다.
                if (!destPath.equals(result.getFileName())) {
                    parseFile(baseDir, destFile, result);
                }

                // Set file permissions
                perms = PosixFilePermissions.fromString(list.get(0).getAttrs().getPermissionsString().substring(1));
                Files.setPosixFilePermissions(destFile.toPath(), perms);
            } catch (Exception e) {
                if (!"Invalid mode".equals(e.getMessage())) {
                    log.warn("Unhandled exception occurred while get file({}) and will be exception ignored. [Message] : {}", destFile.getAbsolutePath(), e.getMessage());
                }
            }
        } else {
            for (ChannelSftp.LsEntry oListItem : list) {
                try {
                    destFile = new File(destPath, oListItem.getFilename());

                    // SymbolicLink에 대해서는 별도의 작업을 하지 않는다. (링크 내에 감당할 수 없는 용량의 파일이 있을 수 있으며 대부분 Static Contents 일 것이라 판단)
                    if (oListItem.getAttrs().isLink()) {
                        FileUtils.forceMkdir(destFile);
                    } else {
                        if (!oListItem.getAttrs().isDir()) {
                            sftpChannel.get(sourcePath + File.separator + oListItem.getFilename(), destFile.getAbsolutePath());
                            parseFile(baseDir, destFile, result);
                        } else if (!".".equals(oListItem.getFilename()) && !"..".equals(oListItem.getFilename())) {
                            FileUtils.forceMkdir(destFile);
                            getDir(sftpChannel, sourcePath + File.separator + oListItem.getFilename(), destFile.getAbsolutePath(), baseDir, result);
                        }
                    }

                    // Set file permissions
                    perms = PosixFilePermissions.fromString(oListItem.getAttrs().getPermissionsString().substring(1));
                    Files.setPosixFilePermissions(destFile.toPath(), perms);
                } catch (Exception e) {
                    if (!"Invalid mode".equals(e.getMessage())) {
                        log.warn("Unhandled exception occurred while get file({}) and will be exception ignored. [Message] : {}", destFile.getAbsolutePath(), e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Gets session.
     *
     * @param targetHost the target host
     *
     * @return the session
     *
     * @throws Exception the exception
     */
    private static Session getSession(TargetHost targetHost, int connectTimeout) throws Exception {
        return SSHUtil.getSession(targetHost, connectTimeout);
    }

    /**
     * @param targetDir
     * @param currFile
     * @param result
     *
     * @throws Exception
     */
    public static void parseFile(String targetDir, File currFile, ApplicationAssessmentResult result) throws Exception {
        // File currFile = new File(targetDir, entryName);
        String fileName = currFile.getName();

        if (result != null) {
            String location = currFile.getParentFile().getAbsolutePath().replaceAll(targetDir, ".");
            location = ".".equals(location) ? location + File.separator : location;

            // Descriptor Files
            if (fileName.endsWith(".xml") &&
                    (currFile.getParentFile().getName().equals("META-INF") ||
                            currFile.getParentFile().getName().equals("APP-INF") ||
                            currFile.getParentFile().getName().equals("WEB-INF"))) {
                if (!location.startsWith("./target")) {
                    ApplicationAssessmentResult.File descriptorFile = new ApplicationAssessmentResult.File();
                    descriptorFile.setFile(fileName);
                    descriptorFile.setLocation(location);
                    descriptorFile.setContents(ApplicationFileUtil.getFileContents(currFile));
                    result.getDescriptorFiles().add(descriptorFile);
                }
            }

            // Build Files
            if (fileName.equals("build.xml") || fileName.equals("pom.xml") || fileName.equals("build.gradle")) {
                if (!location.startsWith("./target") && !location.contains("META-INF")) {
                    ApplicationAssessmentResult.File buildFile = new ApplicationAssessmentResult.File();
                    buildFile.setFile(fileName);
                    buildFile.setLocation(location);
                    buildFile.setContents(ApplicationFileUtil.getFileContents(currFile));
                    result.getBuildFiles().add(buildFile);
                }
            }

            // Libraries
            if (fileName.endsWith(".jar") && currFile.getParentFile().getName().equals("lib")) {
                ApplicationAssessmentResult.File jarFile = new ApplicationAssessmentResult.File();
                jarFile.setFile(fileName);
                jarFile.setLocation(currFile.getAbsolutePath().replaceAll(targetDir, "."));
                jarFile.setContents(ManifestUtil.getDescription(currFile));
                result.getLibraries().getAll().add(jarFile);
                // result.getLibraries().getAll().add(currFile.getAbsolutePath().replaceAll(targetDir, "."));

                // 추가 분석 라이브러리에 대한 전체 Path를 등록한다.
                if (result.getAnalysisLibList() != null && result.getAnalysisLibList().contains(fileName)) {
                    result.getAnalysisLibPathList().add(currFile.getAbsolutePath());
                }

                if (fileName.startsWith("xerces") || fileName.startsWith("xalan") ||
                        fileName.startsWith("xml-api") || fileName.startsWith("jboss-")) {
                    result.getLibraries().getDeleteRecommended().add(currFile.getAbsolutePath().replaceAll(targetDir, "."));
                }
            }

            // Config Files
            if ((fileName.endsWith(".xml") || fileName.endsWith(".yml") || fileName.endsWith(".yaml") || fileName.endsWith(".properties")) &&
                    (currFile.getParentFile().getAbsolutePath().contains("classes") ||
                            currFile.getParentFile().getAbsolutePath().contains("resources") ||
                            (!currFile.getParentFile().getAbsolutePath().contains("META-INF") &&
                                    !currFile.getParentFile().getAbsolutePath().contains("APP-INF") &&
                                    !currFile.getParentFile().getAbsolutePath().contains("WEB-INF")))) {
                if (!location.startsWith("./target") && !location.startsWith("./.settings") && !location.startsWith("./.idea") && !location.contains("/node_modules/") &&
                        !fileName.equals("build.xml") && !fileName.equals("pom.xml")) {
                    ApplicationAssessmentResult.File configFile = new ApplicationAssessmentResult.File();
                    configFile.setFile(fileName);
                    configFile.setLocation(location);

                    if (fileName.endsWith(".properties")) {
                        // unescape for unicode
                        configFile.setContents(StringEscapeUtils.unescapeJava(ApplicationFileUtil.getFileContents(currFile)));
                    } else {
                        configFile.setContents(ApplicationFileUtil.getFileContents(currFile));
                    }
                    result.getConfigFiles().add(configFile);
                }
            }

            // File Summary
            if (!location.startsWith("./target") && !location.startsWith("./.settings") && !location.startsWith("./.idea") && !location.contains("/node_modules/")) {
                String extension = getExtension(fileName);

                if (StringUtils.isNotEmpty(extension)) {
                    if (result.getFileSummaryMap().get(extension.toLowerCase()) == null) {
                        result.getFileSummaryMap().put(extension.toLowerCase(), new ApplicationAssessmentResult.FileSummary());
                    }

                    Long count = result.getFileSummaryMap().get(extension.toLowerCase()).getFileCount();
                    Long size = result.getFileSummaryMap().get(extension.toLowerCase()).getFileSize();

                    if (count == null) {
                        result.getFileSummaryMap().get(extension.toLowerCase()).setFileCount(1L);
                    } else {
                        result.getFileSummaryMap().get(extension.toLowerCase()).setFileCount(count + 1L);
                    }

                    if (size == null) {
                        result.getFileSummaryMap().get(extension.toLowerCase()).setFileSize(currFile.length());
                    } else {
                        result.getFileSummaryMap().get(extension.toLowerCase()).setFileSize(size + currFile.length());
                    }
                }
            }
        }
    }

    /**
     * Gets extension.
     *
     * @param fileName the file name
     *
     * @return the extension
     */
    private static String getExtension(String fileName) {
        String[] fileNames = fileName.split("\\.");

        String extension;
        if (fileNames.length == 1) {
            extension = "Unidentified";
        } else if (fileNames.length == 2) {
            extension = fileNames[1];
        } else {
            extension = FilenameUtils.getExtension(fileName).replaceAll("^(\\d+(\\-)?)+$", "BACKUP_IDX");

            if (extension.contains("BACKUP_IDX")) {
                extension = "Unidentified";
            }
            /*
            extension = "";
            for (int i = 1; i < fileNames.length; i++) {
                if (i > 1) {
                    extension += ".";
                }

                extension += fileNames[i].replaceAll("^(\\d+(\\-)?)+$", "BACKUP_IDX");

                if (extension.contains("BACKUP_IDX")) {
                    extension = "Unidentified";
                    break;
                }
            }
            //*/
        }

        return extension;
    }
}
// end of SSHUtil.java
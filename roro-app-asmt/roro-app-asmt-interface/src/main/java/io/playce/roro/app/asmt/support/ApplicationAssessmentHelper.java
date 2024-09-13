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
 * SangCheon Park   Nov 11, 2021		    First Draft.
 */
package io.playce.roro.app.asmt.support;

import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.CommandUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Component
public class ApplicationAssessmentHelper implements InitializingBean {

    private static File jdeprscanFile;
    private static File jdepsFile;
    private static File applicationFileDownloadFile;
    private static File appScanCancelFile;
    private static File javapFile;

    public static File getJdeprscanFile() {
        return jdeprscanFile;
    }

    public static File getJdepsFile() {
        return jdepsFile;
    }

    public static File getApplicationFileDownloadFile() {
        return applicationFileDownloadFile;
    }

    public static File getAppScanCancelFile() {
        return appScanCancelFile;
    }
    public static File getJavapFile() {
        return javapFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir");
        jdeprscanFile = new File(tmpDir, "scripts/jdeprscan.sh");
        jdepsFile = new File(tmpDir, "scripts/jdeps.sh");
        applicationFileDownloadFile = new File(tmpDir, "scripts/application_file_download.py");
        appScanCancelFile = new File(tmpDir, "scripts/app_scan_cancel.sh");
        javapFile = new File(tmpDir, "scripts/java_build_version.sh");

        try {
            FileUtils.copyURLToFile(getClass().getClassLoader().getResource("scripts/jdeprscan.sh"), jdeprscanFile);
            FileUtils.copyURLToFile(getClass().getClassLoader().getResource("scripts/jdeps.sh"), jdepsFile);
            FileUtils.copyURLToFile(getClass().getClassLoader().getResource("scripts/application_file_download.py"), applicationFileDownloadFile);
            FileUtils.copyURLToFile(getClass().getClassLoader().getResource("scripts/app_scan_cancel.sh"), appScanCancelFile);
            FileUtils.copyURLToFile(getClass().getClassLoader().getResource("scripts/java_build_version.sh"), javapFile);

            Set<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);

            Files.setPosixFilePermissions(jdeprscanFile.toPath(), perms);
            Files.setPosixFilePermissions(jdepsFile.toPath(), perms);
            Files.setPosixFilePermissions(applicationFileDownloadFile.toPath(), perms);
            Files.setPosixFilePermissions(appScanCancelFile.toPath(), perms);
            Files.setPosixFilePermissions(javapFile.toPath(), perms);

            log.debug("Application assessment scripts file copied to " + jdeprscanFile.getParentFile().getAbsolutePath());
        } catch (Exception e) {
            log.error("Unhandled exception occurred while copy application assessment scripts.", e);
        }

        try {
            String workDir = CommonProperties.getWorkDir();
            String applicationDir;

            if (workDir.endsWith(File.separator)) {
                applicationDir = FilenameUtils.separatorsToSystem(workDir + "application" + File.separator);
            } else {
                applicationDir = FilenameUtils.separatorsToSystem(workDir + File.separator + "application" + File.separator);
            }

            FileUtils.forceMkdir(new File(applicationDir));

            chmod(workDir, "777");
            chmod(applicationDir, "777");
        } catch (Exception e) {
            log.error("Unhandled exception occurred while execute chmod for application directory.", e);
        }
    }

    private void chmod(String filePath, String permission) throws Exception {
        CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                CommandUtil.findCommand("chmod"),
                permission,
                filePath);

        CommandUtil.executeCommand(cl);
    }
}

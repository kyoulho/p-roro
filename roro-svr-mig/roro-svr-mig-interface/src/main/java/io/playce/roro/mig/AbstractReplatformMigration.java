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
package io.playce.roro.mig;

import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.common.dto.migration.enums.StatusType;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public abstract class AbstractReplatformMigration extends AbstractMigration {

    protected MigrationProcessDto.MigrationPreConfig config;
    protected Map<String, String> linuxFileMap;

    private String backupDir;
    private List<String> homeDirList = new ArrayList<>();

    @Override
    public void init() {
        linuxFileMap = new HashMap<>();
        linuxFileMap.put("/boot", "555");
        linuxFileMap.put("/dev", "755");
        linuxFileMap.put("/etc", "755");
        linuxFileMap.put("/home", "755");
        linuxFileMap.put("/local", "755");
        linuxFileMap.put("/media", "755");
        linuxFileMap.put("/mnt", "755");
        linuxFileMap.put("/opt", "755");
        linuxFileMap.put("/proc", "555");
        linuxFileMap.put("/root", "550");
        linuxFileMap.put("/run", "755");
        linuxFileMap.put("/srv", "755");
        linuxFileMap.put("/sys", "555");
        linuxFileMap.put("/tmp", "776");
        linuxFileMap.put("/usr", "755");
        linuxFileMap.put("/var", "755");

        backupDir = workDir + "/backup";
        new File(backupDir).mkdirs();

        config = migration.getMigrationPreConfig();
    }

    /**
     * <pre>
     * AIX 서버에서 선택된 파일을 다운로드한다.
     * </pre>
     *
     * @throws Exception
     */
    protected void fileDownload(Long inventoryProcessId, TargetHost targetHost) throws Exception {
        DefaultExecutor executor = null;
        PumpStreamHandler streamHandler = null;
        CommandLine cl = null;

        boolean isSudoer = SSHUtil.isSudoer(targetHost);

        for (MigrationProcessDto.MigrationPreConfigFile file : config.getMigrationPreConfigFiles()) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                if (InventoryProcessCancelInfo.hasCancelRequest(inventoryProcessId)) {
                    break;
                }

                executor = new DefaultExecutor();
                streamHandler = new PumpStreamHandler(baos);
                executor.setStreamHandler(streamHandler);

                cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                        CommandUtil.findPython(),
                        MigrationManager.getMigrationFileDownloaderFile().getAbsolutePath(),
                        "-H " + targetHost.getIpAddress(),
                        "-P " + targetHost.getPort(),
                        "-u " + targetHost.getUsername());

                /*
                cl = new CommandLine(CommandUtil.findCommand("sudo"))
                        .addArguments(CommandUtil.findPython())
                        .addArguments(MigrationManager.getAixFileDownloadFile().getAbsolutePath())
                        .addArguments("-H " + targetHost.getIpAddress())
                        .addArguments("-P " + targetHost.getPort())
                        .addArguments("-u " + targetHost.getUsername());
                */

                if (StringUtils.isNotEmpty(targetHost.getPassword())) {
                    cl = cl.addArguments("-p " + targetHost.getPassword());
                }

                if (StringUtils.isNotEmpty(targetHost.getKeyFilePath())) {
                    cl = cl.addArguments("-k " + targetHost.getKeyFilePath());
                }

                cl = cl.addArguments("-s " + isSudoer)
                        .addArguments("--source_dir " + file.getSource())
                        .addArguments("--target_dir " + backupDir);

                // source와 taget이 같지 않으면 mv 시킨다.
                if (!file.getSource().equals(file.getTarget())) {
                    cl.addArguments("--parent_dir " + backupDir + getParentDir(file.getTarget()));
                    cl.addArguments("--asis_dir " + backupDir + file.getSource());
                    cl.addArguments("--tobe_dir " + backupDir + file.getTarget());
                }

                // https://cloud-osci.atlassian.net/browse/PCR-6223
                log.debug("Download files using [{}]", getLoggingMessage(Arrays.asList(cl.toStrings())));

                int exitCode = executor.execute(cl);

                if (exitCode == 0) {
                    log.debug("File downloaded from [{}] to [{}].",
                            targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + file.getSource(),
                            backupDir + file.getTarget());
                } else {
                    log.warn("File({}) download failed. [Reason] : ",
                            targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + file.getSource(),
                            baos.toString());
                }
                updateStatus(StatusType.CREATE_RAW_FILES);
            } catch (Exception e) {
                log.error("File({}) download failed.", file.getSource(), e);
            }
        }
    }

    /**
     * <pre>
     * 마이그레이션 대상 파일을 복사하고 Owner를 변경한다.
     * </pre>
     */
    protected void fileUpload(TargetHost targetHost) throws Exception {
        // 1. file upload from RoRo Worker to target instance
        //    - backup directory가 '/'로 끝나지 않으면 '/'를 추가한다.
        // 2. change owner:group
        DefaultExecutor executor = null;
        PumpStreamHandler streamHandler = null;
        CommandLine cl = null;

        if (!backupDir.endsWith("/")) {
            backupDir = backupDir + "/";
        }

        boolean isSudoer = SSHUtil.isSudoer(targetHost);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            executor = new DefaultExecutor();
            streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findPython(),
                    MigrationManager.getMigrationFileUploaderFile().getAbsolutePath(),
                    "-H " + targetHost.getIpAddress(),
                    "-P " + targetHost.getPort(),
                    "-u " + targetHost.getUsername());

            /*
            cl = new CommandLine(CommandUtil.findCommand("sudo"))
                    .addArguments(CommandUtil.findPython())
                    .addArguments(MigrationManager.getAixFileUploadFile().getAbsolutePath())
                    .addArguments("-H " + targetHost.getIpAddress())
                    .addArguments("-P " + targetHost.getPort())
                    .addArguments("-u " + targetHost.getUsername());
            */

            if (StringUtils.isNotEmpty(targetHost.getPassword())) {
                cl = cl.addArguments("-p " + targetHost.getPassword());
            }

            if (StringUtils.isNotEmpty(targetHost.getKeyFilePath())) {
                cl = cl.addArguments("-k " + targetHost.getKeyFilePath());
            }

            cl = cl.addArguments("-s " + isSudoer)
                    .addArguments("--backup_dir " + backupDir)
                    .addArguments("--log_dir " + logDir);

            // https://cloud-osci.atlassian.net/browse/PCR-6223
            log.debug("Upload files using [{}]", getLoggingMessage(Arrays.asList(cl.toStrings())));

            int exitCode = executor.execute(cl);

            if (exitCode == 0) {
                log.debug("File uploaded from [{}] to [{}].",
                        backupDir,
                        targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":/");
            } else {
                log.warn("File({}) upload failed. [Reason] : ",
                        backupDir,
                        baos.toString());
            }
            updateStatus(StatusType.DOWNLOAD_FROM_S3);
        } catch (Exception e) {
            log.error("File({}) upload failed.", backupDir, e);
        }

        if (isSudoer) {
            String command = null;
            for (MigrationProcessDto.MigrationPreConfigFile file : config.getMigrationPreConfigFiles()) {
                command = "sudo chown -Rf " + file.getOwnerUser() + ":" + file.getOwnerGroup() + " " + file.getTarget();
                SSHUtil.executeCommand(targetHost, command);
            }

            // 사용자 Home Directory는 기본 700으로 변경한다.
            for (String homeDir : homeDirList) {
                if (linuxFileMap.get(homeDir) == null) {
                    command = "sudo chmod 700 " + homeDir;
                    SSHUtil.executeCommand(targetHost, command);
                }
            }
        } else {
            log.info("[{}] is not a sudoer and skip change owner/mode for directories.", targetHost.getUsername());
        }
    }

    /**
     * <pre>
     * 소스 또는 타깃 시스템의 Top level 디렉토리의 모드를 맞춘다. (일부 RoRo 시스템에 umask 값에 따라 /home 이 750 이 되는 경우도 있고..)
     * </pre>
     *
     * @throws Exception
     */
    protected void changeFileModes() throws Exception {
        Map<String, String> fileMap = checkDirectory();
        log.debug("Migration ID : [{}], fileMap : [{}]", migration.getInventoryProcessId(), fileMap);

        for (MigrationProcessDto.MigrationPreConfigFile file : config.getMigrationPreConfigFiles()) {
            try {
                if (!"link".equals(file.getType().toLowerCase())) {
                    String topLevelDir = getTopLevelDirName(file.getTarget());

                    // file.getTarget()의 top level directory가 Amazon Linux의 기본디렉토리라면..
                    if (linuxFileMap.get(topLevelDir) != null) {
                        chmod(linuxFileMap.get(topLevelDir), backupDir + topLevelDir);
                    } else if (fileMap.get(topLevelDir) != null) {
                        chmod(fileMap.get(topLevelDir), backupDir + topLevelDir);
                    } else {
                        chmod(fileMap.get(getTopLevelDirName(file.getSource())), backupDir + topLevelDir);
                    }
                }
            } catch (Exception e) {
                log.warn("[{}] Change Permission has been failed. [Reason] : [{}]", file.getTarget(), e.getMessage());
            }
        }

        try {
            chmod("555", backupDir);
        } catch (Exception e) {
            log.warn("[{}] Change Permission has been failed. [Reason] : [{}]", backupDir, e.getMessage());
        }
    }

    /**
     * <pre>
     * 추가 볼륨을 마운트 한다.
     * </pre>
     */
    protected void mount(TargetHost targetHost) throws InterruptedException {
        // volume 갯수가 1개인 경우 root 볼륨으로 mount 대상이 아님.
        if (migration.getVolumes() == null || migration.getVolumes().size() == 1) {
            return;
        }

        /**
         1. ~]$ sudo cp /etc/fstab /etc/fstab.orig
         2. ~]$ sudo mkfs -t ext4 /dev/xvdb ~ /dev/xvdf
         3. ~]$ sudo file -s /dev/xvdb ~ /dev/xvdf
         4. ~]$ sudo echo "UUID=xxxxx	/mount_path	ext4		defaults,nofail	0	2" >> /etc/fstab
         5. ~]$ sudo mount -a
         */
        boolean isSudoer = SSHUtil.isSudoer(targetHost);

        if (isSudoer) {
            try {
                SSHUtil.executeCommand(targetHost, "sudo cp /etc/fstab /etc/fstab.orig");
            } catch (Exception e) {
                log.warn("[{}] /etc/fstab move failed to /etc/fstab.orig. [Reason] : [{}]", migration.getInventoryProcessId(), e.getMessage());
            }

            String result = null;
            String uuid = null;

            for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
                try {
                    if ("N".equals(volume.getRootYn())) {
                        String deviceName = null;

                        if ("AWS".equals(migration.getCredential().getCredentialTypeCode())) {
                            String lsblk = SSHUtil.executeCommand(targetHost, "lsblk | grep nvme");
                            if (StringUtils.isNotEmpty(lsblk) && lsblk.contains("nvme")) {
                                deviceName = volume.getDeviceName().replaceAll("sdb", "nvme1n1")
                                        .replaceAll("sdc", "nvme2n1")
                                        .replaceAll("sdd", "nvme3n1")
                                        .replaceAll("sde", "nvme4n1")
                                        .replaceAll("sdf", "nvme5n1")
                                        .replaceAll("sdg", "nvme6n1")
                                        .replaceAll("sdh", "nvme7n1")
                                        .replaceAll("sdi", "nvme8n1")
                                        .replaceAll("sdj", "nvme9n1");
                            } else {
                                deviceName = volume.getDeviceName().replaceAll("sd", "xvd");
                            }
                        } else if ("GCP".equals(migration.getCredential().getCredentialTypeCode())) {
                            deviceName = volume.getDeviceName();
                        }

                        String diskInfo = SSHUtil.executeCommand(targetHost, "df | grep " + deviceName).trim();
                        String dirExist = SSHUtil.executeCommand(targetHost, "sudo bash -c \"[ -d '" + volume.getVolumePath() + "' ] && echo 'EXIST' || echo 'NOT_EXIST'\"").trim();

                        if (StringUtils.isEmpty(diskInfo) && "NOT_EXIST".equals(dirExist)) {
                            SSHUtil.executeCommand(targetHost, "sudo mkdir -p " + volume.getVolumePath());
                            SSHUtil.executeCommand(targetHost, "sudo mkfs -F -t ext4 " + deviceName);
                            result = SSHUtil.executeCommand(targetHost, "sudo file -s " + deviceName);
                            uuid = result.substring(result.indexOf("UUID=") + 5, result.indexOf("UUID=") + 41);
                            SSHUtil.executeCommand(targetHost, "sudo bash -c \"echo 'UUID=" + uuid + "	" + volume.getVolumePath() + "	ext4		defaults,nofail	0	2' >> /etc/fstab\"");

                            // Below command is "Permission Denied"
                            //SSHUtil.executeCommand(targetHost, "sudo echo \"UUID=" + uuid + "	" + volume.getPath() + "	ext4		defaults,nofail	0	2\" >> /etc/fstab");
                        }
                    }
                } catch (Exception e) {
                    log.warn("[{}] EBS volume format failed. [Reason] : [{}]", migration.getInventoryProcessId(), e.getMessage());
                }
            }

            try {
                SSHUtil.executeCommand(targetHost, "sudo mount -a");
                log.debug("Additional volume mounted.");
            } catch (Exception e) {
                log.warn("[{}] Additional volume mount failed. [Reason] : [{}]", migration.getInventoryProcessId(), e.getMessage());
            }
        } else {
            log.info("[{}] is not a sudoer and skip mount volume(s).", targetHost.getUsername());
        }
    }

    /**
     * <pre>
     * Group을 추가한다.
     * </pre>
     */
    protected void addGroup(TargetHost targetHost) throws InterruptedException {
        boolean isSudoer = SSHUtil.isSudoer(targetHost);

        if (isSudoer) {
            List<MigrationProcessDto.MigrationPreConfigGroup> configGroupList = config.getMigrationPreConfigGroups();

            for (MigrationProcessDto.MigrationPreConfigGroup configGroup : configGroupList) {
                try {
                    // root, wheel은 Linux의 Default Group으로 제외한다.
                    if (!"root".equals(configGroup.getGroupName()) && !"wheel".equals(configGroup.getGroupName())) {
                        SSHUtil.executeCommand(targetHost, "sudo groupadd " + configGroup.getGroupName());

                        if (configGroup.getGid() != null) {
                            SSHUtil.executeCommand(targetHost, "sudo groupmod -g " + configGroup.getGid() + " " + configGroup.getGroupName());
                        }
                    }
                } catch (Exception e) {
                    log.warn("[{}] Add group failed. [Reason] : [{}]", configGroup.getGroupName(), e.getMessage());
                }
            }
        } else {
            log.info("[{}] is not a sudoer and skip add group(s).", targetHost.getUsername());
        }
    }

    /**
     * <pre>
     * User를 추가/수정하고 필요한 정보를 등록한다.
     * </pre>
     */
    protected void addUser(TargetHost sourceHost, TargetHost targetHost) throws InterruptedException {
        boolean isSudoer = SSHUtil.isSudoer(targetHost);

        if (isSudoer) {
            List<MigrationProcessDto.MigrationPreConfigUser> userList = config.getMigrationPreConfigUsers();

            StringBuilder command = null;
            for (MigrationProcessDto.MigrationPreConfigUser user : userList) {
                try {

                    /**
                     * 1. Get added user shadow
                     * */
                    String addedUserShadow = user.getUserPassword();
                    if (StringUtils.isEmpty(addedUserShadow)) {
                        addedUserShadow = SSHUtil.executeCommand(sourceHost, "sudo cat /etc/shadow | grep " + user.getUserName() + " | awk 'BEGIN {FS = \":\"} {print $2}'");
                    }

                    /**
                     * 2. Add User with group
                     */
                    // root는 Linux의 Default User로 제외한다.
                    if (!"root".equals(user.getUserName())) {
                        // sudo useradd user1 -g group1 -G group2,group3 -d /homedir
                        command = new StringBuilder();
                        command.append("sudo")
                                .append(" ")
                                .append("useradd")
                                .append(" ")
                                .append(user.getUserName());

                        for (int i = 0; i < user.getGroups().size(); i++) {
                            if (i == 0) {
                                command.append(" ")
                                        .append("-g")
                                        .append(" ")
                                        .append(user.getGroups().get(i));
                            } else if (i == 1) {
                                command.append(" ")
                                        .append("-G")
                                        .append(" ")
                                        .append(user.getGroups().get(i));
                            } else {
                                command.append(",")
                                        .append(user.getGroups().get(i));
                            }
                        }

                        if (StringUtils.isNotEmpty(user.getHomeDir())) {
                            command.append(" ")
                                    .append("-d")
                                    .append(" ")
                                    .append(user.getHomeDir());
                        }

                        SSHUtil.executeCommand(targetHost, command.toString());
                        log.debug("[{}] useradd command executed.", user.getUserName());
                    }

                    /**
                     * 3. Set UID
                     */
                    if (user.getUid() != null) {
                        SSHUtil.executeCommand(targetHost, "sudo usermod -u '" + user.getUid() + "' " + user.getUserName());
                        log.debug("[{}] usermod command executed.", user.getUserName());
                    }

                    /**
                     * 4. Set Password
                     */
                    if (StringUtils.isNotEmpty(addedUserShadow)) {
                        SSHUtil.executeCommand(targetHost, "sudo usermod -p '" + addedUserShadow + "' " + user.getUserName());
                        log.debug("[{}] usermod command executed.", user.getUserName());
                    }

                    //*
                    String homedir = SSHUtil.executeCommand(targetHost, "sudo getent passwd \"" + user.getUserName() + "\" | cut -d ':' -f6").trim();
                    String group = SSHUtil.executeCommand(targetHost, "sudo groups " + user.getUserName() + " | cut -d ' ' -f3").trim();
                    /*/
                    String homedir = user.getHomedir();
                    String group = user.getGroups().get(0);
                    //*/

                    homeDirList.add(homedir);

                    /**
                     * 5. Set .bash_profile
                     */
                    if (StringUtils.isNotEmpty(user.getProfile())) {
                        SSHUtil.executeCommand(targetHost, "sudo cp " + homedir + "/.bash_profile " + homedir + "/.bash_profile.orig");
                        SSHUtil.executeCommand(targetHost, "echo \"" + user.getProfile().replaceAll("[$]", "\\\\\\$").replaceAll("\"", "\\\\\"") + "\" | sudo tee " + homedir + "/.bash_profile");
                        SSHUtil.executeCommand(targetHost, "sudo chown " + user.getUserName() + ":" + group + " " + homedir + "/.bash_profile");
                        SSHUtil.executeCommand(targetHost, "sudo chown " + user.getUserName() + ":" + group + " " + homedir + "/.bash_profile.orig");

                        log.debug("[{}] .bash_profile saved to [{}].", user.getUserName(), homedir + "/.bash_profile");
                    }

                    /**
                     * 6. Set crontab
                     */
                    if (StringUtils.isNotEmpty(user.getCrontab())) {
                        SSHUtil.executeCommand(targetHost, "echo \"" + user.getCrontab().replaceAll("[$]", "\\\\\\$").replaceAll("\"", "\\\\\"") + "\" | sudo tee /var/spool/cron/" + user.getUserName());
                        SSHUtil.executeCommand(targetHost, "sudo chown " + user.getUserName() + ":" + group + " /var/spool/cron/" + user.getUserName());
                        SSHUtil.executeCommand(targetHost, "sudo chmod 600 /var/spool/cron/" + user.getUserName());

                        log.debug("[{}] crontab saved to [{}].", user.getUserName(), "/var/spool/cron/" + user.getUserName());
                    }
                } catch (Exception e) {
                    log.warn("[{}] Add user failed. [Reason] : [{}]", user.getUserName(), e.getMessage());
                }
            }
        } else {
            log.info("[{}] is not a sudoer and skip add user(s).", targetHost.getUsername());
        }
    }

    /**
     * Install packages.
     */
    protected void installPackages(TargetHost targetHost) throws InterruptedException {
        boolean isSudoer = SSHUtil.isSudoer(targetHost);

        if (isSudoer) {
            String command = "apt-get -v &> /dev/null && sudo apt-get install -y {PACKAGE} || sudo yum install -y {PACKAGE}";
            for (String packageName : config.getPackages()) {
                if (StringUtils.isNotEmpty(packageName)) {
                    log.debug("[{}] package will be installed.", packageName);
                    SSHUtil.executeCommand(targetHost, command.replaceAll("\\{PACKAGE\\}", packageName));
                }
            }
        } else {
            log.info("[{}] is not a sudoer and skip install package(s).", targetHost.getUsername());
        }
    }

    /**
     * <pre>
     * Worker 서버에 저장된 backup 파일을 삭제한다.
     * </pre>
     */
    protected void deleteBackupFiles() {
        try {
            DefaultExecutor executor = new DefaultExecutor();

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("rm"),
                    "-rf",
                    backupDir);

            /*
            CommandLine cl = new CommandLine(CommandUtil.findCommand("sudo"))
                    .addArguments(CommandUtil.findCommand("rm"))
                    .addArguments("-rf")
                    .addArguments(MigrationManager.getWorkDir() + "/" + migration.getInventoryProcessId());
            */

            log.debug("Command for remove backup file(s) : [{}]", cl);

            executor.execute(cl);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while delete backup directory.", e);
        }
    }

    /**
     * <pre>
     *
     * </pre>
     *
     * @param path
     *
     * @return
     */
    private String getParentDir(String path) {
        if (!path.equals("/") && path.endsWith("/")) {
            path = path.substring(0, path.lastIndexOf("/"));
        }

        return path.substring(0, path.lastIndexOf("/"));
    }

    /**
     * <pre>
     * / Directroy 하위의 파일(디렉토리 포함) 퍼미션을 조회한다.
     * </pre>
     *
     * @return
     *
     * @throws Exception
     */
    private Map<String, String> checkDirectory() throws Exception {
        String command = "ls -al / | awk '{k=0;for(i=0;i<=8;i++)k+=((substr($1,i+2,1)~/[rwx]/)*2^(8-i));if(k)printf(\"%0o \",k);print}'";

        String result = SSHUtil.executeCommand(targetHost, command);

        String[] lines = result.split("\\r?\\n");

        Map<String, String> fileMap = new HashMap<String, String>();
        for (String line : lines) {
            if (line.startsWith("total")) {
                continue;
            }

            if (line.indexOf("->") > -1) {
                continue;
            }

            // split on whitespace
            String[] temp = line.split("\\s+");

            fileMap.put("/" + temp[temp.length - 1], temp[0]);
        }

        return fileMap;
    }

    /**
     * <pre>
     * 최상위 레벨의 디렉토리, 파일명을 반환한다.
     * </pre>
     *
     * @param path
     *
     * @return
     */
    private String getTopLevelDirName(String path) {
        if (path.indexOf("/", 1) < 0) {
            return path;
        } else {
            return path.substring(0, path.indexOf("/", 1));
        }
    }

    /**
     * <pre>
     * 파일에 대한 퍼미션 변경
     * </pre>
     *
     * @param permission
     * @param filePath
     *
     * @return
     *
     * @throws Exception
     */
    protected String chmod(String permission, String filePath) throws Exception {
        String result = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DefaultExecutor executor = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("chmod"),
                    permission,
                    filePath);

            /*
            CommandLine cl = new CommandLine(CommandUtil.findCommand("sudo"))
                    .addArguments(CommandUtil.findCommand("chmod"))
                    .addArguments(permission)
                    .addArguments(filePath);
            */

            log.debug("chmod()'s CommandLine : {}", cl);

            int exitCode = executor.execute(cl);

            if (exitCode == 0) {
                result = baos.toString();
            } else {
                throw new Exception(baos.toString());
            }
        } catch (Exception e) {
            log.error("Shell execution error while change permissions. Error Log => [{}]", e.getMessage());
            throw e;
        }

        return result;
    }

    /**
     * <pre>
     * 소스 서버에서 선택된 파일을 다운로드한다.
     * </pre>
     *
     * @throws Exception
     */
    @Deprecated
    private void fileDownloadWithSSH(TargetHost targetHost) throws Exception {
        for (MigrationProcessDto.MigrationPreConfigFile file : config.getMigrationPreConfigFiles()) {
            //*
            SSHUtil.getDir(targetHost, file.getSource(), backupDir + file.getTarget());
            /*/
            SSHUtil.getDir(TargetHost.convert(server), file.getSource(), backupDir + file.getSource());

            // source와 taget이 같이 않으면 mv 시킨다.
            if (!file.getSource().equals(file.getTarget())) {
                FileUtils.forceMkdir(new File(backupDir, getParentDir(file.getTarget())));
                if (new File(backupDir, file.getSource()).isDirectory()) {
                    FileUtils.moveDirectory(new File(backupDir, file.getSource()), new File(backupDir, file.getTarget()));
                } else {
                    FileUtils.moveFile(new File(backupDir, file.getSource()), new File(backupDir, file.getTarget()));
                }
            }
            //*/

            log.debug("File downloaded from [{}] to [{}].",
                    new String(targetHost.getUsername() + "@" + targetHost.getIpAddress() + ":" + file.getSource()),
                    backupDir + file.getTarget());
        }
    }

    /**
     * <pre>
     * 마이그레이션 대상 파일을 복사하고 Owner를 변경한다.
     * </pre>
     */
    @Deprecated
    private void fileUploadWithSSH(TargetHost targetHost) throws Exception {
        if (!backupDir.endsWith("/")) {
            backupDir = backupDir + "/";
        }

        SSHUtil.putDir(targetHost, backupDir, "/");
        log.debug("File uploaded from [{}] to [{}].", backupDir, "/");

        String command = null;
        for (MigrationProcessDto.MigrationPreConfigFile file : config.getMigrationPreConfigFiles()) {
            command = "sudo chown -Rf " + file.getOwnerUser() + ":" + file.getOwnerGroup() + " " + file.getTarget();
            SSHUtil.executeCommand(targetHost, command);
        }

        // 사용자 Home Directory는 기본 700으로 변경한다.
        for (String homeDir : homeDirList) {
            if (linuxFileMap.get(homeDir) == null) {
                command = "sudo chmod 700 " + homeDir;
                SSHUtil.executeCommand(targetHost, command);
            }
        }
    }

    // https://cloud-osci.atlassian.net/browse/PCR-6223
    public String getLoggingMessage(List<String> messages) {
        StringBuilder sb = new StringBuilder();
        String msg;
        for (int i = 0; i < messages.size(); i++) {
            msg = messages.get(i);

            if (sb.length() > 0) {
                sb.append(StringUtils.SPACE);
            }
            sb.append(msg);

            if (msg.equals("-p") || msg.equals("-pw")) {
                i++;
                sb.append(StringUtils.SPACE).append("*****");
                continue;
            }
        }

        return sb.toString();
    }
}
//end of AbstractReplatformMigration.java
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
 * SangCheon Park   Mar 11, 2022		    First Draft.
 */
package io.playce.roro.mig.el;

import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.common.dto.migration.enums.StatusType;
import io.playce.roro.common.exception.CancelException;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.mig.AbstractReplatformMigration;
import io.playce.roro.mig.MigrationManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
@Component("CustomReplatformMigration")
@Scope("prototype")
public class CustomReplatformMigration extends AbstractReplatformMigration {

    @Override
    public MigrationProcessDto migration() throws Exception {
        log.debug("CustomReplatformMigration.migration() invoked.");

        updateStatus(StatusType.CREATE_RAW_FILES);
        if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
            throw new CancelException("Migration Cancel Requested.");
        }
        fileDownload(migration.getInventoryProcessId(), migration.getTargetHost());

        changeFileModes();
        updateStatus(StatusType.CREATED_RAW_FILES);

        TargetHost targetHost = new TargetHost();
        targetHost.setIpAddress(config.getConnectIpAddress());
        targetHost.setPort(config.getConnectSshPort());
        targetHost.setUsername(config.getConnectUserName());
        targetHost.setPassword(config.getConnectUserPassword());
        targetHost.setKeyFilePath(config.getKeyFilePath());

        if (!SSHUtil.healthCheck(targetHost)) {
            throw new RoRoException("Unable connect to target machine [" + targetHost.getIpAddress() + ":" + targetHost.getPort() + "]");
        }

        /** Target Configuration */
        updateStatus(StatusType.INITIATE_INSTANCE);

        /** Add groups */
        addGroup(targetHost);
        updateStatus(StatusType.INITIATE_INSTANCE);

        /** Add users */
        addUser(migration.getTargetHost(), targetHost);
        updateStatus(StatusType.DOWNLOAD_FROM_S3);

        /** File copy from RoRo worker to target instance */
        if (InventoryProcessCancelInfo.hasCancelRequest(migration.getInventoryProcessId())) {
            throw new CancelException("Migration Cancel Requested.");
        }
        fileUpload(targetHost);
        updateStatus(StatusType.DOWNLOAD_FROM_S3);

        /** Install packages */
        installPackages(targetHost);
        updateStatus(StatusType.DOWNLOAD_FROM_S3);

        // Worker 서버에 저장된 backup 파일을 삭제한다.
        if (MigrationManager.getDirectoryRemove()) {
            deleteBackupFiles();
        }

        MigrationProcessDto.ServerSummary serverSummary = new MigrationProcessDto.ServerSummary();
        getHostname(targetHost, serverSummary);
        getArchitecture(targetHost, serverSummary);
        getOperationSystem(targetHost, serverSummary);
        getCpuInfo(targetHost, serverSummary);
        getMemoryInfo(targetHost, serverSummary);
        getKernel(targetHost, serverSummary);
        getVendor(targetHost, serverSummary);
        migration.setServerSummary(serverSummary);

        migration.setPublicIp(config.getConnectIpAddress());
        updateStatus(StatusType.COMPLETED);

        return migration;
    }

    @Override
    public void setResourceNames() {
        // nothing to do
    }

    @Override
    protected void cancel() {
        try {
            DefaultExecutor executor = new DefaultExecutor();

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("sh"),
                    MigrationManager.getCancelMigrationFile().getAbsolutePath(),
                    workDir,
                    targetHost.getIpAddress());

            log.debug("Execute shell script for Custom replatform migration process kill : [{}]", cl);

            int exitCode = executor.execute(cl);

            if (exitCode == 0) {
                log.debug("Custom replatform migration({}) has been cancelled.", migration.getInventoryProcessId());
            } else {
                log.debug("Custom replatform migration({}) cancel failed.", migration.getInventoryProcessId());
            }
        } catch (Exception e) {
            log.error("Unhandled exception occurred while execute cancel_migration.sh.", e);
        }
    }

    private void getHostname(TargetHost targetHost, MigrationProcessDto.ServerSummary serverSummary) throws InterruptedException {
        String hostname = SSHUtil.executeCommand(targetHost, "uname -n");
        serverSummary.setHostName(removeSpecialCharacter(hostname));
    }

    private void getVendor(TargetHost targetHost, MigrationProcessDto.ServerSummary serverSummary) throws InterruptedException {
        String vendor = SSHUtil.executeCommand(targetHost, "sudo cat /sys/devices/virtual/dmi/id/sys_vendor");
        serverSummary.setVendorName(removeSpecialCharacter(vendor));
    }

    private void getKernel(TargetHost targetHost, MigrationProcessDto.ServerSummary serverSummary) throws InterruptedException {
        String kernel = SSHUtil.executeCommand(targetHost, "uname -r");
        serverSummary.setOsKernel(removeSpecialCharacter(kernel));
    }

    private void getOperationSystem(TargetHost targetHost, MigrationProcessDto.ServerSummary serverSummary) throws InterruptedException {
        String os_release = SSHUtil.executeCommand(targetHost, "sudo cat /etc/os-release");

        if (os_release != null) {
            for (String facts : os_release.split("\n")) {
                if (facts.indexOf("PRETTY_NAME") > -1) {
                    serverSummary.setOsName(removeSpecialCharacter(facts.split("=")[1]));
                    break;
                }
            }
        } else {
            String system_release = SSHUtil.executeCommand(targetHost, "sudo cat /etc/system-release");
            serverSummary.setOsName(removeSpecialCharacter(system_release));
        }

        if (serverSummary.getOsName() != null) {
            OsFamily family = OsFamily.findByOs(serverSummary.getOsName());

            if (family != null) {
                serverSummary.setOsFamily(family.getType());
            }
        }
    }

    private void getCpuInfo(TargetHost targetHost, MigrationProcessDto.ServerSummary serverSummary) throws InterruptedException {
        String cpu_infos = SSHUtil.executeCommand(targetHost, "lscpu");
        for (String facts : cpu_infos.split("\n")) {
            if (facts.indexOf("CPU(s)") == 0) {
                facts = facts.replaceAll("^CPU\\(s\\)[=|:]", "");
                serverSummary.setCpuCount(Integer.parseInt(facts.trim()));
            } else if (facts.indexOf("Core(s) per socket") > -1) {
                facts = facts.replaceAll("^Core\\(s\\) per socket[=|:]", "");
                serverSummary.setCpuCoreCount(Integer.parseInt(facts.trim()));
            } else if (facts.indexOf("Socket(s)") > -1) {
                facts = facts.replaceAll("^Socket\\(s\\)[=|:]", "");
                serverSummary.setCpuSocketCount(Integer.parseInt(facts.trim()));
            } else if (facts.indexOf("Architecture") > -1) {
                facts = facts.replaceAll("^Architecture[=|:]", "");
                serverSummary.setCpuArchitecture(facts.trim());
            } else if (facts.indexOf("Model name") > -1) {
                facts = facts.replaceAll("^Model name[=|:]", "");
                serverSummary.setCpuModel(facts.trim());
            }
        }
    }

    private void getMemoryInfo(TargetHost targetHost, MigrationProcessDto.ServerSummary serverSummary) throws InterruptedException {
        String vmstat = SSHUtil.executeCommand(targetHost, "vmstat -s");

        for (String facts : vmstat.split("\n")) {
            if (facts.indexOf("total memory") > -1) {
                Pattern pattern = Pattern.compile("(\\w+)");
                Matcher matcher = pattern.matcher(facts);
                if (matcher.find()) {
                    serverSummary.setMemSize(Integer.parseInt(matcher.group(1)) / 1024);
                }
            } else if (facts.indexOf("total swap") > -1) {
                Pattern pattern = Pattern.compile("(\\w+)");
                Matcher matcher = pattern.matcher(facts);
                if (matcher.find()) {
                    serverSummary.setSwapSize(Integer.parseInt(matcher.group(1)) / 1024);
                }
            }
        }
    }

    private void getArchitecture(TargetHost targetHost, MigrationProcessDto.ServerSummary serverSummary) throws InterruptedException {
        String architecture = SSHUtil.executeCommand(targetHost, "uname -m");
        serverSummary.setCpuArchitecture(removeSpecialCharacter(architecture));
    }

    private String removeSpecialCharacter(String value) {
        if (value != null) {
            String match = "[\"\n|\"\r]";
            return value.replaceAll(match, "");
        }
        return null;
    }

    private enum OsFamily {
        REDHAT("RedHat", Arrays.asList("Red Hat", "RedHat", "Fedora", "CentOS", "OracleLinux")),
        DEBIAN("Debian", Arrays.asList("Debian", "Ubuntu")),
        SOLARIS("Solaris", Arrays.asList("Solaris")),
        AIX("AIX", Arrays.asList("AIX")),
        HPUX("HP-UX", Arrays.asList("HP-UX"));

        private String type;
        private List<String> members;

        OsFamily(String type, List<String> machineList) {
            this.type = type;
            this.members = machineList;
        }

        public static OsFamily findByOs(String os) {
            return Arrays.stream(OsFamily.values())
                    .filter(m -> m.hasMember(os))
                    .findAny()
                    .orElse(null);
        }

        public boolean hasMember(String member) {
            return members.stream().anyMatch(m -> member.indexOf(m) > -1);
        }

        public String getType() {
            return type;
        }
    }
}
//end of CustomReplatformMigration.java
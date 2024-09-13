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
 * Dong-Heon Han    Apr 28, 2022		First Draft.
 */

package io.playce.roro.scheduler.service.impl;

import io.playce.roro.api.common.util.DateTimeUtils;
import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.config.RoRoProperties;
import io.playce.roro.common.dto.inventory.process.MonitoringQueueItem;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.CollectionHelper;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.scheduler.component.MonitoringStat;
import io.playce.roro.scheduler.config.ScheduleConfig;
import io.playce.roro.scheduler.service.Manager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringManager implements Manager {
    private final ScheduleConfig scheduleConfig;
    private final RoRoProperties roroProperties;
    private final ServerService serverService;
    private final Map<String, MonitoringStat> monitoringStatMap;
    private final BlockingQueue<MonitoringQueueItem> queue;
//    public static final Integer MIN_PERIOD = 60;

    @AllArgsConstructor
    @Getter
    public enum STAT_FILE {
        CPU_STAT("cpu.stat"),
        MEMORY_STAT("memory.stat"),
        DISK_STAT("disk.stat"),
        PROCESS_STAT("process.stat"),
        NETWORK_STAT("network.stat"),
        ;
        private final String fileName;
    }

    @Async("monitoringTaskExecutor")
    public Future<Void> run() throws InterruptedException {
        MonitoringQueueItem item = queue.take();
        log.debug("dequeue id: {}, cycle: {}", item.getServerInventoryId(), item.getMonitoringCycle());
        Domain1013 domain1013;

        try {
            domain1013 = Domain1013.valueOf(item.getInventoryDetailTypeCode());
        } catch (IllegalArgumentException e) {
            throw new RoRoException("This OS is not supported: " + item.getInventoryDetailTypeCode());
        }
        if (!(domain1013 == Domain1013.LINUX || domain1013 == Domain1013.AIX)) {
            throw new RoRoException("This OS is not supported: " + item.getInventoryDetailTypeCode());
        }

        TargetHost targetHost = serverService.getTargetHostByServerInventoryId(item.getServerInventoryId());
        String outDir = scheduleConfig.getMonitoring().getDefaultDir();
        Integer period = scheduleConfig.getMonitoring().getDefaultPeriod();
        Integer scriptLifeHours = scheduleConfig.getMonitoring().getScriptLifeHours();
        String downloadDir = getDownloadPath(item.getServerInventoryId());

        File linuxMonitoringScript = CollectionHelper.getLinuxMonitoringFile();
        File aixMonitoringScript = CollectionHelper.getAixMonitoringFile();
        File script = domain1013 == Domain1013.LINUX ? linuxMonitoringScript : aixMonitoringScript;
        boolean sudo = SSHUtil.isSudoer(targetHost) && domain1013 != Domain1013.LINUX; //aix 일때만 sudo 유효

        makeDownloadDir(downloadDir);
        stopMonitoring(targetHost, script, outDir);
        downloadStatFiles(targetHost, outDir, downloadDir);

        uploadMonitoringScript(targetHost, outDir, script);
        startMonitoring(targetHost, script, outDir, sudo, period, scriptLifeHours);

        parseFiles(downloadDir, item);
        return null;
    }

    private void makeDownloadDir(String downloadDir) {
        try {
            FileUtils.forceMkdir(new File(downloadDir));
            chmod(downloadDir);
        } catch (IOException e) {
            log.error("Unhandled exception occurred while stop monitoring process.", e);
        }
    }

    private void chmod(String filePath) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DefaultExecutor executor = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            CommandLine cl = CollectionHelper.getCommandLine(
                    CollectionHelper.findCommand("sudo"), CollectionHelper.findCommand("chmod"), "-R", "755", filePath);
            log.debug("chmod()'s CommandLine : {}", cl);

            int exitCode = executor.execute(cl);

            if (exitCode != 0) {
                throw new RoRoException(baos.toString());
            }
            log.debug("chmod()'s result : {}", baos);
        } catch (Exception e) {
            log.error("Shell execution error while change permissions. Error Log => [{}]", e.getMessage(), e);
        }
    }

    private void parseFiles(String downloadDir, MonitoringQueueItem item) {
        for (STAT_FILE statFile : STAT_FILE.values()) {
            File stat = new File(downloadDir, statFile.getFileName());
            if (!stat.exists()) {
                continue;
            }

            MonitoringStat monitoringStat = monitoringStatMap.get(statFile.name());
            if (monitoringStat == null) {
                throw new RoRoException("The monitoring component does not exist.");
            }

            if (statFile == STAT_FILE.NETWORK_STAT) {
                processForNetwork(monitoringStat, item, stat);
            } else {
                processForLine(monitoringStat, item, stat);
            }
        }
    }

    private void processForNetwork(MonitoringStat monitoringStat, MonitoringQueueItem item, File stat) {
        try {
            String lines = Files.readString(Paths.get(stat.getPath()));
            monitoringStat.processLine(item, lines);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void processForLine(MonitoringStat monitoringStat, MonitoringQueueItem item, File stat) {
        try (Stream<String> stream = Files.lines(Paths.get(stat.getPath()))) {
            stream.filter(l -> StringUtils.isNotEmpty(l.trim())).forEach(l -> monitoringStat.processLine(item, l));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void startMonitoring(TargetHost targetHost, File script, String outDir, boolean sudo, Integer period, Integer scriptLifeHours) throws InterruptedException {
        String scriptName = script.getName(); //FilenameUtils.getName(script);
//        period = period == null || period < MIN_PERIOD ? MIN_PERIOD : period;

        // nohup sh /tmp/roro_linux_migration.sh 60 24 /tmp > /dev/null 2>&1 &
        StringBuilder sb = new StringBuilder("nohup").append(StringUtils.SPACE)
                .append("bash").append(StringUtils.SPACE)
                .append(outDir).append(File.separator).append(scriptName).append(StringUtils.SPACE)
                .append(period).append(StringUtils.SPACE)
                .append(scriptLifeHours).append(StringUtils.SPACE)
                .append(outDir).append(StringUtils.SPACE);
        
        if (sudo) {
            sb.append("true").append(StringUtils.SPACE);
        } else {
            sb.append("false").append(StringUtils.SPACE);
        }
        sb.append("> /dev/null 2>&1").append(StringUtils.SPACE).append("&");

        // https://cloud-osci.atlassian.net/browse/PCR-5348
        // su 환경에서 아래 명령은 Syntax Error 발생.
        // stat 파일 다운로드 및 스크립트 파일은 su 사용하지 않기 때문에 여기에서도 su를 사용하지 않도록 root passowrd를 비워준다.
        // echo RORO:CUSTOM:CMD:START;nohup sh /tmp/roro_linux_moditoring.sh 60 48 /tmp > /dev/null 2>&1 &; if [ $? != 0 ]; then echo RORO:CMD:ERROR; fi; echo RORO:CUSTOM:CMD:END
        targetHost.setRootPassword(null);

        SSHUtil.executeCommand(targetHost, sb.toString());
    }

    private void downloadStatFiles(TargetHost targetHost, String outDir, String downloadDir) throws InterruptedException {
        for (STAT_FILE file : STAT_FILE.values()) {
            try {
                //process stat파일은 현재 처리하지 않으므로 가져오지않고 삭제만 함.
                if (file != STAT_FILE.PROCESS_STAT) {
                    SSHUtil.getFile(targetHost, outDir + File.separator + file.getFileName(), downloadDir);
                }
                SSHUtil.executeCommand(targetHost, "sudo rm -f " + outDir + File.separator + file.getFileName());
            } catch (Exception ignored) {}
        }
    }

    private void stopMonitoring(TargetHost targetHost, File script, String outDir) throws InterruptedException {
        String command = "ps -ef | grep '" + script.getName() + "' | grep -v grep | awk {'print \"kill -9 \" $2'} | sh -x";
        SSHUtil.executeCommand(targetHost, command);
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException ignored) {}
        SSHUtil.executeCommand(targetHost, "sudo rm -f " + outDir + File.separator + script);
    }

    private void uploadMonitoringScript(TargetHost targetHost, String outDir, File script) throws InterruptedException {
        SSHUtil.putFile(targetHost, script, outDir);
    }

    private String getDownloadPath(Long serverInventoryId) {
        return roroProperties.getWorking().getDirPath() + File.separator
                + "monitoring" + File.separator
                + serverInventoryId + File.separator
                + DateTimeUtils.getDefaultFilePath(new Date()) + File.separator;
    }
}
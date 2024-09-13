/*
 * Copyright 2023 The playce-roro-v3 Project.
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
 * SangCheon Park   Feb 22, 2023		    First Draft.
 */
package io.playce.roro.api.collector;

import io.playce.roro.common.code.Domain1013;
import io.playce.roro.common.dto.info.OSInfo;
import io.playce.roro.common.property.CommonProperties;
import io.playce.roro.common.util.SSHUtil;
import io.playce.roro.common.util.SystemInfoUtil;
import io.playce.roro.common.util.WinRmUtils;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.jpa.entity.ServerStatus;
import io.playce.roro.jpa.repository.ServerStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public class HostStatCollector implements Runnable {

    private final Long serverInventoryId;
    private final TargetHost targetHost;
    private final Long startTime;
    private final Long endTime;
    private final ServerStatusRepository serverStatusRepository;

    private Domain1013 os;
    private List<HostStat> hostStatList = new ArrayList<>();

    public HostStatCollector(Long serverInventoryId, String inventoryDetailTypeCode, TargetHost targetHost) {
        this.serverInventoryId = serverInventoryId;
        this.targetHost = targetHost;
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + (5 * 60 * 1000);
        this.serverStatusRepository = CommonProperties.getApplicationContext().getBean(ServerStatusRepository.class);

        try {
            if (StringUtils.isEmpty(inventoryDetailTypeCode)) {
                OSInfo osInfo = SystemInfoUtil.getOSInfo(targetHost);
                this.os = osInfo.getInventoryDetailTypeCode();
            } else {
                this.os = Domain1013.valueOf(inventoryDetailTypeCode);
            }
        } catch (Exception e) {
            log.warn("Unable to check server type. Reason : [{}]", e.getMessage());
        }
    }

    @Override
    public void run() {
        String cpu, mem;

        int cnt = 0;
        if (os != null) {
            while (System.currentTimeMillis() < endTime) {
                cpu = null;
                mem = null;

                try {
                    if (cnt++ > 0) {
                        Thread.sleep(10 * 1000);
                    }

                    if (os.equals(Domain1013.LINUX)) {
                        cpu = getLinuxCpuUsage();
                        mem = getLinuxMemUsage();
                    } else if (os.equals(Domain1013.AIX)) {
                        cpu = getAixCpuUsage();
                        mem = getAixMemUsage();
                    } else if (os.equals(Domain1013.SUNOS)) {
                        cpu = getSolarisCpuUsage();
                        mem = getSolarisMemUsage();
                    } else if (os.equals(Domain1013.HP_UX)) {
                        cpu = getHpuxCpuUsage();
                        mem = getHpuxMemUsage();
                    } else if (os.equals(Domain1013.WINDOWS)) {
                        cpu = getWindowsCpuUsage();
                        mem = getWindowsMemUsage();
                    }

                    if (StringUtils.isNotEmpty(cpu) && StringUtils.isNotEmpty(mem)) {
                        HostStat hostStat = new HostStat();

                        try {
                            hostStat.setCpuUsage(Double.parseDouble(cpu));
                        } catch (NumberFormatException e) {
                            // ignore
                        }

                        try {
                            hostStat.setMemUsage(Double.parseDouble(mem));
                        } catch (NumberFormatException e) {
                            // ignore
                        }

                        if (hostStat.getCpuUsage() != null || hostStat.getMemUsage() != null) {
                            hostStatList.add(hostStat);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Unhandled exception occurred while collect host stat. Reason : [{}]", e.getMessage());
                }
            }

            if (hostStatList.size() > 0) {
                saveHostStat();
            }
        }
    }

    private String getLinuxCpuUsage() throws Exception {
        String command = "top -b -n1 | head -5 | grep -Po '[0-9.]+.id' | sed -e 's/%/ /' | awk '{print 100 - $1}'";
        return SSHUtil.executeCommand(targetHost, command);
    }

    private String getLinuxMemUsage() throws Exception {
        String command = "free -k | grep Mem: | awk '{printf \"%.2f\", 100 - ($7/$2*100)}'";
        return SSHUtil.executeCommand(targetHost, command);
    }

    private String getAixCpuUsage() throws Exception {
        String command = "sudo sar 1 2 | grep -i average | awk '{print $2+$3}'";
        return SSHUtil.executeCommand(targetHost, command);
    }

    private String getAixMemUsage() throws Exception {
        String command = "sudo svmon -i 1 1 | grep -i memory | awk '{print ($3/$2)*100}'";
        return SSHUtil.executeCommand(targetHost, command);
    }

    private String getSolarisCpuUsage() throws Exception {
        String command = "sudo sar 1 2 | grep -i average | awk '{print $2+$3}'";
        return SSHUtil.executeCommand(targetHost, command);
    }

    private String getSolarisMemUsage() throws Exception {
        String command = "echo ::memstat | mdb -k | grep -i freelist | awk -F'%' '{print $1}' | awk '{print 100-$5}'";
        return SSHUtil.executeCommand(targetHost, command);
    }

    private String getHpuxCpuUsage() throws Exception {
        String command = "vmstat | tail -1 | awk '{print 100-$18}'";
        return SSHUtil.executeCommand(targetHost, command);
    }

    private String getHpuxMemUsage() throws Exception {
        String command = "sudo dmesg | grep Physical: | awk '{print ($2-$8)/$2*100}'";
        return SSHUtil.executeCommand(targetHost, command);
    }

    private String getWindowsCpuUsage() throws Exception {
        String command = "(Get-Counter '\\Processor(_Total)\\% Processor Time').CounterSamples.CookedValue |\n" +
                "   SELECT @{Name='cpuUsage'; Expression = {$_.ToString(\"#,0.000\")}}|\n" +
                "   Format-Table -HideTableHeaders";
        return WinRmUtils.executePsShell(targetHost, command);
    }

    private String getWindowsMemUsage() throws Exception {
        String command = "Get-WmiObject win32_OperatingSystem |\n" +
                "   SELECT @{Name='memUsage'; Expression = {(100 - (100 * $_.freephysicalmemory / $_.totalvisiblememorysize)).ToString(\"#,0.0\")}} |\n" +
                "   Format-Table -HideTableHeaders";
        return WinRmUtils.executePsShell(targetHost, command);
    }

    private void saveHostStat() {
        Double cpuSum = Double.valueOf(0);
        Double memSum = Double.valueOf(0);
        Double cpuAvg = Double.valueOf(0);
        Double memAvg = Double.valueOf(0);

        for (HostStat hostStat : hostStatList) {
            cpuSum += hostStat.getCpuUsage();
            memSum += hostStat.getMemUsage();
        }

        if (hostStatList.size() > 0) {
            cpuAvg = cpuSum / hostStatList.size();
            memAvg = memSum / hostStatList.size();
        }

        ServerStatus serverStatus = serverStatusRepository.findById(serverInventoryId).orElse(null);

        if (serverStatus == null) {
            serverStatus = new ServerStatus();
            serverStatus.setServerInventoryId(serverInventoryId);
        }

        log.debug("Server InventoryID : [{}], CPU Usage : [{}], Memory Usage : [{}]", serverInventoryId, cpuAvg, memAvg);

        serverStatus.setCpuUsage(cpuAvg);
        serverStatus.setMemUsage(memAvg);
        serverStatus.setMonitoringDatetime(new Date(startTime));
        serverStatusRepository.save(serverStatus);
    }
}
//end of HostStatCollector.java
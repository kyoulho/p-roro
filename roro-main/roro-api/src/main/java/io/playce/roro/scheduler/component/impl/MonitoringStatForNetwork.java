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
 * Dong-Heon Han    May 07, 2022		First Draft.
 */

package io.playce.roro.scheduler.component.impl;

import io.playce.roro.common.dto.inventory.process.MonitoringQueueItem;
import io.playce.roro.common.dto.monitoring.MonitoringSaveItem;
import io.playce.roro.common.util.SummaryStat;
import io.playce.roro.discover.helper.UnknownServerDiscoverHelper;
import io.playce.roro.jpa.entity.ServerMaster;
import io.playce.roro.jpa.entity.ServerNetworkInformation;
import io.playce.roro.jpa.repository.ServerMasterRepository;
import io.playce.roro.jpa.repository.ServerNetworkInformationRepository;
import io.playce.roro.scheduler.component.AbstractMonitoringStat;
import io.playce.roro.scheduler.component.MonitoringSaveProcessor;
import io.playce.roro.svr.asmt.dto.common.network.Traffic;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component("NETWORK_STAT")
@Slf4j
public class MonitoringStatForNetwork extends AbstractMonitoringStat {
    private final Object lock = new Object();
    private final ServerMasterRepository serverMasterRepository;
    private final ServerNetworkInformationRepository serverNetworkInformationRepository;
    private final UnknownServerDiscoverHelper unknownServerDiscoverHelper;
    public MonitoringStatForNetwork(BlockingQueue<MonitoringSaveItem> monitoringResultQueue, MonitoringSaveProcessor monitoringSaveProcessor, UnknownServerDiscoverHelper unknownServerDiscoverHelper, ServerMasterRepository serverMasterRepository, ServerNetworkInformationRepository serverNetworkInformationRepository) {
        super(monitoringResultQueue, monitoringSaveProcessor);
        this.unknownServerDiscoverHelper = unknownServerDiscoverHelper;
        this.serverMasterRepository = serverMasterRepository;
        this.serverNetworkInformationRepository = serverNetworkInformationRepository;
    }

    @Override
    @Transactional
    public void processLine(MonitoringQueueItem item, String network) {
        /*      line data
                "date", "protocol", "state", "localAddress", "localPort", "foreignAddress", "foreignPort", "pid"
                0        1           2        3               4            5                  6             7
        */
        String[] lines = network.split("\n");

        List<String> listenPorts = Arrays.stream(lines).filter(l-> l.contains("LISTEN,")).map(l-> {
            String[] data = l.split(",");
            return data[4];
        }).collect(Collectors.toList());

        List<Traffic> toAny = new ArrayList<>();
        List<Traffic> toLocal = new ArrayList<>();
        for(String line: lines) {
            if (line.contains("LISTEN,")) continue;

            String[] data = line.split(",", 8);
            if (data[3].equals("127.0.0.1") && data[5].equals("127.0.0.1")) continue;
            if (data[2].equalsIgnoreCase("wait")) continue;

            int lport = NumberUtils.toInt(data[4], -1);
            if(lport == -1) continue;
            int fport = NumberUtils.toInt(data[6], -1);
            if(fport == -1) continue;

            Traffic traffic = getTraffic(data);
            if (listenPorts.contains(traffic.getLport())) {
                toLocal.add(traffic);
            } else {
                toAny.add(traffic);
            }
        }

        synchronized (lock) {
            ServerMaster serverMaster = serverMasterRepository.getById(item.getServerInventoryId());
            List<String> ips = serverNetworkInformationRepository.findByServerInventoryId(item.getServerInventoryId()).stream().map(ServerNetworkInformation::getAddress).collect(Collectors.toList());
            unknownServerDiscoverHelper.extractTraffics(item.getProjectId(), item.getServerInventoryId(), -1L,
                    serverMaster.getRepresentativeIpAddress(), ips, toAny, toLocal, toAny.stream().map(Traffic::getFaddr).collect(Collectors.toSet()));
            unknownServerDiscoverHelper.extractTraffics(item.getProjectId(), item.getServerInventoryId(), -1L,
                    serverMaster.getRepresentativeIpAddress(), ips, toAny, toLocal, toLocal.stream().map(Traffic::getFaddr).collect(Collectors.toSet()));
        }
    }

    @NotNull
    private Traffic getTraffic(String[] data) {
        Traffic traffic = new Traffic();
        traffic.setProtocol(data[1]);
        traffic.setLaddr(data[3]);
        traffic.setLport(data[4]);
        traffic.setFaddr(data[5]);
        traffic.setFport(data[6]);
        traffic.setStatus(data[2]);
        traffic.setPid(data[7]);
        return traffic;
    }

    @Override
    protected MonitoringSaveItem getMonitoringSaveItem(SummaryStat stat, String[] data) {
        return null;
    }
}
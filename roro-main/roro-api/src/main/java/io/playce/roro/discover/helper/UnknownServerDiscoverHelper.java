/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Hoon Oh       1월 26, 2022            First Draft.
 */
package io.playce.roro.discover.helper;

import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.code.*;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.util.WellKnownPortUtil;
import io.playce.roro.common.util.support.TargetHost;
import io.playce.roro.jpa.entity.DiscoveredInstanceMaster;
import io.playce.roro.jpa.entity.DiscoveredPortRelation;
import io.playce.roro.jpa.entity.DiscoveredPortRelationDetail;
import io.playce.roro.jpa.entity.ServerMaster;
import io.playce.roro.jpa.repository.DiscoveredPortRelationDetailRepository;
import io.playce.roro.jpa.repository.DiscoveredPortRelationRepository;
import io.playce.roro.jpa.repository.ServerMasterRepository;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import io.playce.roro.svr.asmt.dto.common.interfaces.InterfaceInfo;
import io.playce.roro.svr.asmt.dto.common.interfaces.Ipv4Address;
import io.playce.roro.svr.asmt.dto.common.network.EstablishedPort;
import io.playce.roro.svr.asmt.dto.common.network.PortList;
import io.playce.roro.svr.asmt.dto.common.network.Traffic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnknownServerDiscoverHelper {

    private final ServerMasterRepository serverMasterRepository;
    private final DiscoveredInstanceManager discoveredInstanceManager;
    private final DiscoveredPortRelationRepository discoveredPortRelationRepository;
    private final DiscoveredPortRelationDetailRepository discoveredPortRelationDetailRepository;

    public void extract(InventoryProcessConnectionInfo connectionInfo, ServerAssessmentResult result) {
        TargetHost targethost = InventoryProcessConnectionInfo.targetHost(connectionInfo);
        Map<String, InterfaceInfo> interfaceInfoMap = result.getInterfaces();
        PortList portList = result.getPortList();
        try {
            // Read IP Addresses for host
            List<String> ips = getIpsFromInterfaces(interfaceInfoMap);

            EstablishedPort establishedPort = portList.getEstablished();

            /**
             * 모든 트래픽(Inbound, Outbound)의 IP 정보를 추출.
             * 중복을 제거한다.
             * */
            TreeSet<String> uniqIp = getUniqIps(establishedPort);
            extractTraffics(connectionInfo.getProjectId(), connectionInfo.getInventoryId(), connectionInfo.getInventoryProcessId(), targethost.getIpAddress(), ips, establishedPort.getLocalToAny(), establishedPort.getAnyToLocal(), uniqIp);
        } catch (RuntimeException e) {
            log.error("Unhandled error occurred {}", e.getMessage(), e);
        }
    }

    public void extractTraffics(Long projectId, Long inventoryId, Long inventoryProcessId, String targetIpAddress, List<String> ips, List<Traffic> toAny, List<Traffic> toLocal, Set<String> uniqIp) {
        for (String ipAddress : uniqIp) {
            /**
             * 1. 자신의 Interface에 속한 IP는 제어
             * 2. Ipv6인지 확인
             * */
            if (StringUtils.countMatches(ipAddress, ":") < 2 && !ips.contains(ipAddress)) {
                extractTrafficAndDiscovered(projectId, inventoryId, inventoryProcessId, targetIpAddress, toAny, toLocal, ipAddress);
            }
        }
    }

    private void extractTrafficAndDiscovered(Long projectId, Long inventoryId, Long inventoryProcessId, String targetIpAddress, List<Traffic> toAny, List<Traffic> toLocal, String ipAddress) {
        /**
         * 1. 등록된 IpAddress 조회
         * 2. ServerSummary 네트워크 정보 조회
         * */
        List<ServerMaster> knownServers = serverMasterRepository.findByRepresentativeIpAddress(ipAddress, projectId);
        if (knownServers == null || knownServers.size() == 0) {
            /**
             * 언노운 서버라면 repository에 저장이 되어있는지 확인하고 없다면 등록한다.
             * */
            DiscoveredInstanceMaster unknownServer = getDiscoveredInstanceMaster(projectId, inventoryId, inventoryProcessId, ipAddress);

            /**
             * 해당 언노운 서버의 트래픽을 추출한다.
             * */
            extractTraffic(Domain1004.OUTB, targetIpAddress, unknownServer.getFinderInventoryId(), unknownServer.getDiscoveredIpAddress(), toAny, inventoryProcessId);
            extractTraffic(Domain1004.INB, targetIpAddress, unknownServer.getFinderInventoryId(), unknownServer.getDiscoveredIpAddress(), toLocal, inventoryProcessId);
        } else {
            for (ServerMaster knownServer : knownServers) {
                extractTraffic(Domain1004.OUTB, targetIpAddress, inventoryId, knownServer.getRepresentativeIpAddress(), toAny, inventoryProcessId);
                extractTraffic(Domain1004.INB, targetIpAddress, inventoryId, knownServer.getRepresentativeIpAddress(), toLocal, inventoryProcessId);
            }
        }
    }

    @NotNull
    private DiscoveredInstanceMaster getDiscoveredInstanceMaster(Long projectId, Long serverInventoryId, Long inventoryProcessId, String ipAddress) {
        DiscoveredInstanceMaster unknownServer;
        List<DiscoveredInstanceMaster> unknownServerList =
                discoveredInstanceManager.getDiscoveredInstanceByIpAddressAndInventoryTypeCodeAndProjectId(ipAddress, Domain1001.SVR.name(), projectId);
        if (unknownServerList == null || unknownServerList.size() == 0) {
            unknownServer = new DiscoveredInstanceMaster();
            unknownServer.setDiscoveredIpAddress(ipAddress);
            unknownServer.setRegistDatetime(new Date());
            unknownServer.setInventoryRegistTypeCode(Domain1006.DISC.name());
            unknownServer.setInventoryTypeCode(Domain1001.SVR.name());
            unknownServer.setProjectId(projectId);
            unknownServer.setFinderInventoryId(serverInventoryId);
            unknownServer.setInventoryProcessId(inventoryProcessId);
            unknownServer.setDeleteYn(Domain101.N.name());
        } else {
            unknownServer = unknownServerList.get(0);
            unknownServer.setFinderInventoryId(serverInventoryId);
        }
        return discoveredInstanceManager.saveUnknownServer(unknownServer);
    }

    private void extractTraffic(Domain1004 direction, String ipAddress, Long serverInventoryId, String discoverIpAddress, List<Traffic> traffics, Long inventoryProcessId) {
        List<Traffic> filteredTraffics = traffics.stream()
                .filter(traffic -> traffic.getFaddr().equals(discoverIpAddress))
                .collect(Collectors.toList());

        for (Traffic traffic : filteredTraffics) {
            try {
                DiscoveredPortRelation relation = getDiscoveredPortRelation(ipAddress, serverInventoryId, discoverIpAddress, traffic, direction);

                List<DiscoveredPortRelation> duplicateRelations = discoveredPortRelationRepository.findByServerInventoryIdAndServicePortAndInventoryDirectionPortTypeCodeAndTargetIpAddress(
                        serverInventoryId, relation.getServicePort(), direction.name(), discoverIpAddress
                );

                if (duplicateRelations.size() == 0) {
                    discoveredPortRelationRepository.save(relation);
                    DiscoveredPortRelationDetail detail = getDiscoveredPortRelationDetail(inventoryProcessId, traffic, relation);

                    discoveredPortRelationDetailRepository.save(detail);
                }

            } catch (Exception e) {
                log.error("Unhandled error occurred {}", e.getMessage(), e);
            }
        }
    }

    private DiscoveredPortRelationDetail getDiscoveredPortRelationDetail(Long inventoryProcessId, Traffic traffic, DiscoveredPortRelation relation) {
        DiscoveredPortRelationDetail detail = new DiscoveredPortRelationDetail();
        detail.setRegistDatetime(new Date());
        detail.setRegistUserId(WebUtil.getUserId());
        detail.setForeignPort(Integer.parseInt(traffic.getFport()));
        detail.setLocalPort(Integer.parseInt(traffic.getLport()));
        detail.setConnectionStatusTypeCode(Domain1007.EST.name());
        detail.setDiscoveredPortRelationId(relation.getDiscoveredPortRelationId());
        detail.setInventoryProcessId(inventoryProcessId);
        detail.setProcessCommand(WellKnownPortUtil.getType(traffic.getProtocol(), Integer.valueOf(traffic.getLport())));
        return detail;
    }

    public DiscoveredPortRelation getDiscoveredPortRelation(String svrInvIpAddr, Long serverInventoryId, String discoverIpAddress, Traffic traffic, Domain1004 trafficType) {
        String inventoryConnectionTypeCode = Domain1005.REAL.name();
        String inventoryDirectionPortTypeCode = trafficType.name();
        int servicePort = trafficType == Domain1004.INB ? Integer.parseInt(traffic.getLport()) : Integer.parseInt(traffic.getFport());

        String uniqueKey = String.format("%d|%s|%s|%s|%d|%s", serverInventoryId, svrInvIpAddr, inventoryConnectionTypeCode, inventoryDirectionPortTypeCode, servicePort, discoverIpAddress);
        DiscoveredPortRelation portRelation = discoveredPortRelationRepository.findByUniqueKey(uniqueKey).orElse(new DiscoveredPortRelation());
        portRelation.setKnownPortName(WellKnownPortUtil.getType(
                traffic.getProtocol(), servicePort)
        );
        portRelation.setUniqueKey(uniqueKey);
        portRelation.setServerInventoryId(serverInventoryId);
        portRelation.setSvrInvIpAddr(svrInvIpAddr);
        portRelation.setInventoryConnectionTypeCode(inventoryConnectionTypeCode);
        portRelation.setInventoryDirectionPortTypeCode(inventoryDirectionPortTypeCode);
        portRelation.setServicePort(servicePort);
        portRelation.setTargetIpAddress(discoverIpAddress);
        portRelation.setProtocol(traffic.getProtocol());

        return portRelation;
    }

    private TreeSet<String> getUniqIps(EstablishedPort established) {
        TreeSet<String> traffics = new TreeSet<>();

        for (Traffic traffic : established.getAnyToLocal()) {
            traffics.add(traffic.getFaddr());
        }

        for (Traffic traffic : established.getLocalToAny()) {
            traffics.add(traffic.getFaddr());
        }

        return traffics;
    }

    private List<String> getIpsFromInterfaces(Map<String, InterfaceInfo> interfaceInfoMap) {
        List<String> ips = new ArrayList<>();

        for (InterfaceInfo intfInfo : interfaceInfoMap.values()) {
            ips.addAll(intfInfo.getIpv4().stream().map(Ipv4Address::getAddress).collect(Collectors.toList()));
        }

        return ips;
    }
}
//end of UnknownServerDiscoverManager.java
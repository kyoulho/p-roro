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
 * Dong-Heon Han    Mar 15, 2022		First Draft.
 */

package io.playce.roro.api.domain.topology.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.topology.component.Common;
import io.playce.roro.api.domain.topology.component.DatasourceWork;
import io.playce.roro.api.domain.topology.component.ExternalConnectionWork;
import io.playce.roro.api.domain.topology.component.ServerWork;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.dto.topology.TopologyNodeResponse;
import io.playce.roro.common.dto.topology.TopologyPortmapResponse;
import io.playce.roro.common.dto.topology.TopologyServerResponse;
import io.playce.roro.common.dto.topology.TrafficResponse;
import io.playce.roro.common.dto.topology.attr.LINK_CATE;
import io.playce.roro.common.dto.topology.attr.Link;
import io.playce.roro.common.dto.topology.attr.Node;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.TopologyNodePosition;
import io.playce.roro.jpa.entity.pk.TopologyNodePositionId;
import io.playce.roro.jpa.repository.TopologyNodePositionRepository;
import io.playce.roro.mybatis.domain.topology.TopologyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
@Transactional
@Slf4j
public class TopologyService {
    private final Common common;
    private final ServerWork serverWork;
    private final DatasourceWork datasourceWork;
    private final ExternalConnectionWork externalConnectionWork;
    private final TopologyMapper mapper;
    private final TopologyNodePositionRepository topologyNodePositionRepository;

    public List<TopologyServerResponse> getServerList(Long projectId, String type, Long typeId) {
        List<Long> servers = getServerIds(type, typeId);
        if (servers.isEmpty()) return new ArrayList<>();

        return mapper.selectTopologyServers(projectId, servers);
    }

    private List<Long> getServerIds(String type, Long typeId) {
        if (type.equals("SERV")) {
            return mapper.selectServerIdsByServiceId(typeId);
        } else {
            return List.of(typeId);
        }
    }

    public List<TopologyPortmapResponse> getPortList(Long projectId, String type, Long typeId) {
        List<Long> servers = getServerIds(type, typeId);
        if (servers.isEmpty()) return new ArrayList<>();

        List<TopologyPortmapResponse> list = mapper.selectTopologyPortmap(projectId, servers);
        Map<String, String> nameMap = new HashMap<>();

        //inventory name 추출.
        list.forEach(l -> {
            String targetIp = l.getTargetIp();
            String targetName = l.getTargetName();
            String sourceIp = l.getSourceIp();
            String sourceName = l.getSourceName();

            if (StringUtils.isNotEmpty(targetName)) {
                nameMap.put(targetIp, targetName);
            }
            if (StringUtils.isNotEmpty(sourceName)) {
                nameMap.put(sourceIp, sourceName);
            }
        });

        //inventory name 설정.
        list.forEach(l -> {
            String targetIp = l.getTargetIp();
            String targetName = l.getTargetName();
            String sourceIp = l.getSourceIp();
            String sourceName = l.getSourceName();

            if (StringUtils.isEmpty(targetName)) {
                l.setTargetName(nameMap.get(targetIp));
                l.setIsTargetInventory(true);
            }
            if (StringUtils.isEmpty(sourceName)) {
                l.setSourceName(nameMap.get(sourceIp));
                l.setIsSourceInventory(true);
            }
        });

        //set DISC_SVR
        list.forEach(l -> {
            String targetName = l.getTargetName();
            String sourceName = l.getSourceName();

            if (StringUtils.isEmpty(targetName)) {
                l.setTargetName("DISC_SVR");
                l.setIsTargetInventory(false);
            }
            if (StringUtils.isEmpty(sourceName)) {
                l.setSourceName("DISC_SVR");
                l.setIsSourceInventory(false);
            }
        });
        return list;
    }

    public TopologyNodeResponse getNodeList(Long projectId, String rootType, Long typeId) {
        Map<String, Link> totalLinkMap = new HashMap<>();
        String rootId = common.makeParentId(rootType, typeId);
        List<Node> inventoryNodes = serverWork.getInventoryNodeTree(projectId, rootType, totalLinkMap, rootId);
        Map<String, Node> resultNodeMap = new HashMap<>();
        common.addToResultNodes(resultNodeMap, inventoryNodes);

        Map<String, Node> serverNodes = getNodeMap(inventoryNodes, Domain1001.SVR.name());
        serverWork.makeLinkInfo(projectId, rootType, resultNodeMap, totalLinkMap, serverNodes);

        Map<String, Node> middlewareMap = getNodeMap(inventoryNodes, Domain1001.MW.name());
        datasourceWork.makeLinkInfo(projectId, rootType, resultNodeMap, totalLinkMap, middlewareMap);

        Map<String, Node> applicationMap = getNodeMap(inventoryNodes, Domain1001.APP.name());
        datasourceWork.makeLinkInfo(projectId, rootType, resultNodeMap, totalLinkMap, applicationMap);

        Set<Long> appIds = applicationMap.values().stream().map(Node::getEngineId).collect(Collectors.toSet());
        if(!appIds.isEmpty()) {
            externalConnectionWork.makeLinkInfo(projectId, resultNodeMap, totalLinkMap, appIds);
        }

        List<Node> result = sortResult(rootType, typeId, resultNodeMap);
        return new TopologyNodeResponse(result, totalLinkMap.values());
    }

    @NotNull
    private List<Node> sortResult(String rootType, Long typeId, Map<String, Node> resultNodeMap) {
        List<Node> nodes = new ArrayList<>(resultNodeMap.values());
        nodes.forEach(n -> {
            switch (n.getType()) {
                case "SERV":
                    n.setLevel(0);
                    break;
                case "SVR":
                    n.setLevel(1);
                    break;
                case "MW":
                    n.setLevel(2);
                    break;
                case "APP":
                    n.setLevel(3);
                    break;
                case "DBMS":
                    n.setLevel(4);
                    break;
                case "EXT":
                    n.setLevel(5);
                    break;
            }
        });
        int rootIndex = findRootNodeIndex(nodes, rootType, typeId);
        List<Node> result = new ArrayList<>();
        String rootName = null;
        if(rootIndex != -1) {
            Node root = nodes.remove(rootIndex);
            rootName = root.getName();
            result.add(root);
        }

        String finalRootName = rootName;
        nodes.sort(Comparator.comparing(n -> n.order(finalRootName)));

        result.addAll(nodes);
        return result;
    }

    private int findRootNodeIndex(List<Node> result, String rootType, Long typeId) {
        for(Node node: result) {
            if(node.getType().equals(rootType) && Objects.equals(node.getTypeId(), typeId)) {
                return result.indexOf(node);
            }
        }
        return -1;
    }

    private Map<String, Node> getNodeMap(List<Node> nodes, String type) {
        return nodes.stream().filter(n -> n.getType().equals(type)).collect(Collectors.toMap(Node::getId, Function.identity(), (a1, a2) -> a1));
    }

    public TrafficResponse getTraffic(Long projectId, String type, Long typeId) {
        TrafficResponse trafficResponse = new TrafficResponse();

        if (type.equals("SVR")) {
            trafficResponse.setInbound(mapper.selectServerInbound(projectId, typeId));
            trafficResponse.setOutbound(mapper.selectServerOutbound(projectId, typeId));
        }

        // Discovered Server 입장에서는 InBound, OutBound 가 반대이다.
        if (type.equals("DISC_SVR")) {
            trafficResponse.setInbound(mapper.selectDiscoveredServerInbound(projectId, typeId));
            trafficResponse.setOutbound(mapper.selectDiscoveredServerOutbound(projectId, typeId));
        }

        return trafficResponse;
    }

    public JsonNode getNodePosition(Long projectId, String type, Long typeId) {
        String content = mapper.selectNodePosition(type, typeId, WebUtil.getUserId());
        return JsonUtil.readTree(StringUtils.defaultString(content, "{}"));
    }

    public void saveNodePosition(Long projectId, String type, Long typeId, JsonNode content) {
        TopologyNodePosition position = getTopologyNodePosition(type, typeId, content);
        topologyNodePositionRepository.save(position);
    }

    @NotNull
    private TopologyNodePosition getTopologyNodePosition(String type, Long typeId, JsonNode content) {
        TopologyNodePositionId id = getTopologyNodePositionId(type, typeId);
        TopologyNodePosition position = new TopologyNodePosition();
        position.setId(id);
        position.setUserId(WebUtil.getUserId());
        position.setConfigContents(JsonUtil.writeValueAsString(content));
        return position;
    }

    @NotNull
    private TopologyNodePositionId getTopologyNodePositionId(String type, Long typeId) {
        TopologyNodePositionId id = new TopologyNodePositionId();
        id.setType(type);
        id.setTypeId(typeId);
        return id;
    }

    public TopologyNodeResponse getAppTopology(Long projectId, String rootType, Long typeId) {
        Map<String, Link> totalLinkMap = new HashMap<>();
        Map<String, Node> resultNodeMap = new HashMap<>();

        // app 과 상위 node 정보 조회
        List<Node> appTopologyNodes = mapper.selectApplicationTopologyNodes(typeId);
        common.addToResultNodes(resultNodeMap, appTopologyNodes);
        common.makeLinkBasic(totalLinkMap, Domain1001.APP.name(), appTopologyNodes, LINK_CATE.TREE_INV);

        Map<String, Node> applicationMap = appTopologyNodes.stream()
                .filter(a -> Objects.equals(a.getType(), Domain1001.APP.name()))
                .collect(Collectors.toMap(Node::getId, n -> n));
        datasourceWork.makeLinkInfo(projectId, rootType, resultNodeMap, totalLinkMap, applicationMap);

        Set<Long> appIds = Set.of(typeId);
        externalConnectionWork.makeLinkInfo(projectId, resultNodeMap, totalLinkMap, appIds);

        List<Node> result = sortResult(rootType, typeId, resultNodeMap);
        return new TopologyNodeResponse(result, totalLinkMap.values());
    }
}
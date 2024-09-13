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
 * Dong-Heon Han    Mar 24, 2022		First Draft.
 */

package io.playce.roro.api.domain.topology.component;

import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1004;
import io.playce.roro.common.dto.topology.DiscoveredRelation;
import io.playce.roro.common.dto.topology.attr.LINK_CATE;
import io.playce.roro.common.dto.topology.attr.Link;
import io.playce.roro.common.dto.topology.attr.Node;
import io.playce.roro.jpa.entity.ServerNetworkInformation;
import io.playce.roro.jpa.repository.ServerNetworkInformationRepository;
import io.playce.roro.mybatis.domain.topology.TopologyMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Component
@RequiredArgsConstructor
public class ServerWork {
    private final Common common;
    private final TopologyMapper mapper;
    private final ServerNetworkInformationRepository serverNetworkInformationRepository;

    public void makeLinkInfo(Long projectId, String rootType, Map<String, Node> resultNodeMap, Map<String, Link> totalLinkMap, Map<String, Node> serverNodes) {
        for (String key : serverNodes.keySet()) {
            Node serverNode = serverNodes.get(key);
            List<DiscoveredRelation> relations = mapper.selectDiscoveredPortRelations(projectId, serverNode.getIp());
            if(relations.size() == 0) continue;

            List<String> ips = relations.stream().map(DiscoveredRelation::getTargetIpAddress).collect(Collectors.toList());

            List<Node> discoveredNodes = getDiscoveredNodes(projectId, rootType, totalLinkMap, ips);
            common.addToResultNodes(resultNodeMap, discoveredNodes);

            List<DiscoveredRelation> inBoundRelations = relations.stream()
                    .filter(i -> i.getInventoryDirectionPortTypeCode().equals(Domain1004.INB.name())).collect(Collectors.toList());
            List<DiscoveredRelation> outBoundRelations = relations.stream()
                    .filter(i -> i.getInventoryDirectionPortTypeCode().equals(Domain1004.OUTB.name())).collect(Collectors.toList());
            List<String> serverIps = serverNetworkInformationRepository.findByServerInventoryId(serverNode.getTypeId()).stream()
                    .map(ServerNetworkInformation::getAddress).collect(Collectors.toList());
            //add representative_ip_address
            serverIps.add(serverNode.getIp());

            //create link info
            Map<String, Link> linkMap = new HashMap<>();
            addInboundLink(linkMap, serverNode, serverIps, discoveredNodes, inBoundRelations);
            addOutboundLink(linkMap, serverNode, serverIps, discoveredNodes, outBoundRelations);

            List<Long> inventoryIds = mapper.selectServerInventoryIdsByIps(projectId, ips);
            for (Long inventoryId : inventoryIds) {
                List<Node> inventoryNodes = getInventoryNodeTree(projectId, rootType, totalLinkMap, common.makeParentId(Domain1001.SVR.name(), inventoryId));
//                if(inventoryNodes.isEmpty()) continue;

                common.addToResultNodes(resultNodeMap, inventoryNodes);

                addInboundLink(linkMap, serverNode, serverIps, inventoryNodes.subList(0, 1), inBoundRelations);
                addOutboundLink(linkMap, serverNode, serverIps, inventoryNodes.subList(0, 1), outBoundRelations);
            }

            common.putLinkMapAll(totalLinkMap, linkMap.values());
        }
    }

    private List<Node> getDiscoveredNodes(Long projectId, String rootType, Map<String, Link> totalLinkMap, List<String> ips) {
        List<Node> discoveredNodes = mapper.selectDiscoveredNodesByIp(projectId, ips);
        common.makeLinkBasic(totalLinkMap, rootType, discoveredNodes, LINK_CATE.TREE_DISC);
        return discoveredNodes;
    }

    public List<Node> getInventoryNodeTree(Long projectId, String rootType, Map<String, Link> totalLinkMap, String parentId) {
        List<Node> inventoryNodes = mapper.selectInventoryNodesByParentId(projectId, parentId);
        common.makeLinkBasic(totalLinkMap, rootType, inventoryNodes, LINK_CATE.TREE_INV);
        return inventoryNodes;
    }

    private void addInboundLink(Map<String, Link> linkMap, Node targetNode, List<String> targetIps, List<Node> sourceNodes, List<DiscoveredRelation> relations) {
        for (DiscoveredRelation relation : relations) {
            String targetIp = relation.getSvrInvIpAddr();
            String sourceIp = relation.getTargetIpAddress();

            for (Node sourceNode : sourceNodes) {
                List<String> ips = getIps(sourceNode);

                if (targetIps.contains(targetIp) && ips.contains(sourceIp)) {
                    createLink(sourceNode, targetNode, linkMap, LINK_CATE.SVR_INB, targetIp, relation);
                }
            }
        }
    }

    private void addOutboundLink(Map<String, Link> linkMap, Node sourceNode, List<String> sourceIps, List<Node> targetNodes, List<DiscoveredRelation> relations) {
        for (DiscoveredRelation relation : relations) {
            String targetIp = relation.getTargetIpAddress();
            String sourceIp = relation.getSvrInvIpAddr();

            for (Node targetNode : targetNodes) {
                List<String> targetIps = getIps(targetNode);

                if (targetIps.contains(targetIp) && sourceIps.contains(sourceIp)) {
                    createLink(sourceNode, targetNode, linkMap, LINK_CATE.SVR_OUTB, targetIp, relation);
                }
            }
        }
    }

    private void createLink(Node sourceNode, Node targetNode, Map<String, Link> linkMap, LINK_CATE cate, String targetIp, DiscoveredRelation relation) {
        String sourceId = sourceNode.getId();
        String targetId = targetNode.getId();

        if(sourceId.contains(Domain1001.SVR.name()) && targetId.contains(Domain1001.SVR.name())) {
            String key = sourceNode.getId() + "." + targetNode.getId();

            if (!linkMap.containsKey(key)) {
                linkMap.put(key, new Link(sourceId, targetId, cate));
            }
            Link link = linkMap.get(key);
            common.addLabel(link, targetIp, relation);
        }
    }

    @NotNull
    private List<String> getIps(Node node) {
        List<String> ips = new ArrayList<>();
        if (node.getIsInventory()) {
            List<ServerNetworkInformation> networkInfos = serverNetworkInformationRepository.findByServerInventoryId(node.getTypeId());
            List<String> findIps = networkInfos.stream().map(ServerNetworkInformation::getAddress).collect(Collectors.toList());
            ips.addAll(findIps);
        }
        ips.add(node.getIp());

        return ips;
    }
}
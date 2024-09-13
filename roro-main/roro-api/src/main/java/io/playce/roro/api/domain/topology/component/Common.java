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

import io.playce.roro.common.dto.topology.DiscoveredRelation;
import io.playce.roro.common.dto.topology.attr.LINK_CATE;
import io.playce.roro.common.dto.topology.attr.Label;
import io.playce.roro.common.dto.topology.attr.Link;
import io.playce.roro.common.dto.topology.attr.Node;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

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
public class Common {
    public void makeLinkBasic(Map<String, Link> linkMap, String rootType, Collection<Node> nodes, LINK_CATE category) {
        for(Node node: nodes) {
            String parentId = node.getParentId();
            String nodeId = node.getId();

            if(parentId.equals("ROOT")) continue;
            if(rootType.equals("SVR") && parentId.startsWith("SERV")) continue;

            Link link = new Link(parentId, nodeId, category);
            putLinkMap(link, linkMap);
//            linkMap.add(new Link(nodeId, parentId, category));
        }
    }

    @NotNull
    public String makeParentId(String type, Long typeId) {
        return type + "-" + typeId;
    }

    public void addLabel(Link link, String targetIp, DiscoveredRelation relation) {
        link.addLabel(new Label(targetIp, relation.getServicePort(), relation.getProtocol()));
    }

    public void addToResultNodes(Map<String, Node> resultNode, Collection<Node> nodes) {
        for(Node node: nodes) {
            String nodeId = node.getId();
            if(resultNode.containsKey(nodeId)) continue;
            resultNode.put(nodeId, node);
        }
    }

    public void putLinkMap(Link link, Map<String, Link> totalLinkMap) {
        String key = link.toString();
        if(!totalLinkMap.containsKey(key)) {
            totalLinkMap.put(key, link);
        }
    }

    public void putLinkMapAll(Map<String, Link> totalLinkMap, Collection<Link> linkMap) {
        linkMap.forEach(l -> putLinkMap(l, totalLinkMap));
    }
}
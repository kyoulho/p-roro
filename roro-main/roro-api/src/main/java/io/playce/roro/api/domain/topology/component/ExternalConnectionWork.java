/*
 * Copyright 2023 The playce-roro Project.
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
 * Dong-Heon Han    Apr 25, 2023		First Draft.
 */

package io.playce.roro.api.domain.topology.component;

import io.playce.roro.common.dto.topology.attr.Label;
import io.playce.roro.common.dto.topology.attr.Link;
import io.playce.roro.common.dto.topology.attr.Node;
import io.playce.roro.jpa.entity.ExcludedExternalConnection;
import io.playce.roro.jpa.entity.pk.ExcludedExternalConnectionId;
import io.playce.roro.jpa.repository.ExcludedExternalConnectionRepository;
import io.playce.roro.mybatis.domain.topology.TopologyMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional
public class ExternalConnectionWork {
    private final Common common;
    private final TopologyMapper mapper;
    private final ExcludedExternalConnectionRepository excludedExternalConnectionRepository;

    public void makeLinkInfo(Long projectId, Map<String, Node> resultNodeMap, Map<String, Link> totalLinkMap, Set<Long> appIds) {
        // add external node
        List<Node> nodes = mapper.selectExternalNodes(projectId, appIds);
        common.addToResultNodes(resultNodeMap, nodes);

        // add external node link
        List<Link> links = mapper.selectExternalLinks(projectId, appIds);
        List<Map<String, Object>> labels = mapper.selectExternalLinkLabels(projectId, appIds);

        for(Map<String, Object> label: labels) {
            String id = (String) label.get("id");
            for(Link link: links) {
                if(id.startsWith(link.toString())) {
                    link.addLabel(new Label((String) label.get("ip"), (Integer) label.get("port"), (String) label.get("protocol")));
                }
            }
        }
        links.forEach(l -> totalLinkMap.put(l.toString(), l));
    }

    public List<String> getExcludedExternalConnections(Long projectId, String type, Long typeId) {
        return mapper.selectExcludedExternalConnections(projectId, type, typeId);
    }

    public void excludeExcludedExternalConnections(Long projectId, List<String> ips) {
        List<ExcludedExternalConnection> ecs = ips.stream().map(ip -> {
            ExcludedExternalConnectionId id = getExcludedExternalConnectionId(projectId, ip);
            return getExcludedExternalConnection(id);
        }).collect(Collectors.toList());
        excludedExternalConnectionRepository.saveAll(ecs);
    }

    @NotNull
    private static ExcludedExternalConnection getExcludedExternalConnection(ExcludedExternalConnectionId id) {
        ExcludedExternalConnection ec = new ExcludedExternalConnection();
        ec.setId(id);
        return ec;
    }

    @NotNull
    private static ExcludedExternalConnectionId getExcludedExternalConnectionId(Long projectId, String ip) {
        ExcludedExternalConnectionId id = new ExcludedExternalConnectionId();
        id.setProjectId(projectId);
        id.setIp(ip);
        return id;
    }

    public void restoreExcludedExternalConnections(Long projectId, List<String> ips) {
        List<ExcludedExternalConnectionId> ids = ips.stream().map(ip -> getExcludedExternalConnectionId(projectId, ip))
                .filter(id -> {
                    ExcludedExternalConnection eecId = excludedExternalConnectionRepository.findById(id).orElse(null);
                    return eecId != null;
                })
                .collect(Collectors.toList());
        excludedExternalConnectionRepository.deleteAllById(ids);
    }
}
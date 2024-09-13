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

import io.playce.roro.common.dto.topology.attr.LINK_CATE;
import io.playce.roro.common.dto.topology.attr.Link;
import io.playce.roro.common.dto.topology.attr.Node;
import io.playce.roro.mybatis.domain.topology.TopologyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
@Component
@RequiredArgsConstructor
public class DatasourceWork {
    private final TopologyMapper mapper;
    private final Common common;

    public void makeLinkInfo(Long projectId, String rootType, Map<String, Node> resultNodeMap, Map<String, Link> totalLinkMap, Map<String, Node> sourceMap) {
        Map<String, Node> mergeMap = new HashMap<>();
        for(Node source: sourceMap.values()) {
            Set<String> datasourceIds = mapper.selectDatasourceIdsByUrl(projectId, source.getType(), source.getTypeId());
            if(datasourceIds.isEmpty()) continue;

            List<Node> dataSources = mapper.selectNodeById(projectId, datasourceIds);
            for(Node datasource: dataSources) {
                Link link = new Link(source.getId(), datasource.getId(), LINK_CATE.CONN_DBMS);
                common.putLinkMap(link, totalLinkMap);
//                totalLinkMap.add(new Link(source.getId(), datasource.getId(), LINK_CATE.CONN_DBMS));
//                totalLinkMap.add(new Link(datasource.getId(), source.getId(), LINK_CATE.CONN_DBMS));
            }
            Map<String, Node> datasourceNodeMap = dataSources.stream().collect(Collectors.toMap(Node::getId, Function.identity(), (n1, n2) -> n1));
            mergeMap.putAll(datasourceNodeMap);

        }
        common.makeLinkBasic(totalLinkMap, rootType, mergeMap.values(), LINK_CATE.TREE_DBMS);
        common.addToResultNodes(resultNodeMap, mergeMap.values());

        Set<String> nodeIds = mergeMap.values().stream().map(Node::getParentId).filter(i -> !i.equals("ROOT")).collect(Collectors.toSet());
        if(nodeIds.size() > 0) {
            List<Node> servers = mapper.selectNodeById(projectId, nodeIds);
            Map<String, Node> serverNodeMap = servers.stream().collect(Collectors.toMap(Node::getId, Function.identity(), (n1, n2) -> n1));
            common.addToResultNodes(resultNodeMap, serverNodeMap.values());
        }
    }
}
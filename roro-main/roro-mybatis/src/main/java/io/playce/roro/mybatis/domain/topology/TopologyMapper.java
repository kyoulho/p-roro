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

package io.playce.roro.mybatis.domain.topology;

import io.playce.roro.common.dto.topology.DiscoveredRelation;
import io.playce.roro.common.dto.topology.TopologyPortmapResponse;
import io.playce.roro.common.dto.topology.TopologyServerResponse;
import io.playce.roro.common.dto.topology.TrafficResponse.TrafficInbound;
import io.playce.roro.common.dto.topology.TrafficResponse.TrafficOutbound;
import io.playce.roro.common.dto.topology.attr.Link;
import io.playce.roro.common.dto.topology.attr.Node;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Repository
public interface TopologyMapper {

    List<TopologyServerResponse> selectTopologyServers(@Param("projectId") Long projectId, @Param("servers") List<Long> servers);

    List<Long> selectServerIdsByServiceId(@Param("serviceId") Long serviceId);

    List<TopologyPortmapResponse> selectTopologyPortmap(@Param("projectId") Long projectId, @Param("servers") List<Long> servers);

    List<Node> selectInventoryNodesByParentId(@Param("projectId") Long projectId, @Param("parentId") String parentId);

    List<Node> selectDiscoveredNodesByIp(@Param("projectId") Long projectId, @Param("ips") List<String> ips);

    List<Long> selectServerInventoryIdsByIps(@Param("projectId") Long projectId, @Param("ips") List<String> ips);

    Set<String> selectDatasourceIdsByUrl(@Param("projectId") Long projectId, @Param("type") String type, @Param("typeId") Long typeId);

    List<Node> selectNodeById(@Param("projectId") Long projectId, @Param("ids") Set<String> ids);

    List<TrafficInbound> selectServerInbound(@Param("projectId") Long projectId, @Param("serverId") Long typeId);

    List<TrafficOutbound> selectServerOutbound(@Param("projectId") Long projectId, @Param("serverId") Long typeId);

    List<TrafficInbound> selectDiscoveredServerInbound(@Param("projectId") Long projectId, @Param("discoveredInstanceId") Long typeId);

    List<TrafficOutbound> selectDiscoveredServerOutbound(@Param("projectId") Long projectId, @Param("discoveredInstanceId") Long typeId);

    List<DiscoveredRelation> selectDiscoveredPortRelations(@Param("projectId") Long projectId, @Param("ip") String ip);

    String selectNodePosition(@Param("type") String type, @Param("typeId") Long typeId, @Param("userId") Long userId);

    List<Node> selectExternalNodes(@Param("projectId") Long projectId, @Param("appIds") Set<Long> appIds);

    List<Link> selectExternalLinks(@Param("projectId") Long projectId, @Param("appIds") Set<Long> appIds);

    @MapKey("id")
    List<Map<String, Object>> selectExternalLinkLabels(@Param("projectId") Long projectId, @Param("appIds") Set<Long> appIds);

    List<Node> selectApplicationTopologyNodes(@Param("appId") Long appId);

    List<String> selectExcludedExternalConnections(@Param("projectId") Long projectId, @Param("type") String type, @Param("typeId") Long typeId);
}
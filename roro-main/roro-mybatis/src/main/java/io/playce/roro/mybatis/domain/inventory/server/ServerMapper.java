/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * Jaeeon Bae       11월 22, 2021            First Draft.
 */
package io.playce.roro.mybatis.domain.inventory.server;

import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.dto.common.ServerConnectionInfo;
import io.playce.roro.common.dto.history.SubscriptionCount;
import io.playce.roro.common.dto.inventory.process.MonitoringQueueItem;
import io.playce.roro.common.dto.inventory.server.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Repository
public interface ServerMapper {

    Integer selectServerCount();

    Long selectServerCountByProjectId(@Param("projectId") Long projectId);
    List<SubscriptionCount> selectServerCountPerProjectId();

    List<InventoryProcessConnectionInfo> selectServerConnectionInfoByInventoryProcess(@Param("projectId") Long projectId,
                                                                                      @Param("inventoryProcessType") String inventoryProcessType,
                                                                                      @Param("inventoryProcessResult") String inventoryProcessResult);

    Server getServerSummary(@Param("inventoryId") Long inventoryId);

    List<ServerResponse> selectServerList(@Param("projectId") Long projectId, @Param("serviceId") Long serviceId);

    int selectServerCountByIpAddressAndPortAndServerId(@Param("projectId") Long projectId,
                                                       @Param("representativeIpAddress") String representativeIpAddress,
                                                       @Param("connectionPort") int connectionPort,
                                                       @Param("serverId") Long serverId);

    ServerDetailResponse selectServerDetail(@Param("projectId") Long projectId, @Param("serverId") Long serverId);

    int selectMiddlewareCountByProjectIdAndServerId(@Param("projectId") Long projectId, @Param("serverId") Long serverId);

    int selectApplicationCountByProjectIdAndServerId(@Param("projectId") Long projectId, @Param("serverId") Long serverId);

    int selectDatabaseCountByProjectIdAndServerId(@Param("projectId") Long projectId, @Param("serverId") Long serverId);

    InventoryProcessConnectionInfo selectServerConnectionInfoByInventoryProcessId(Long inventoryProcessId);

    int selectServerCountByRepresentativeIpAddressAndPortAndProjectId(@Param("representativeIpAddress") String representativeIpAddress,
                                                                      @Param("connectionPort") Integer connectionPort,
                                                                      @Param("projectId") Long projectId,
                                                                      @Param("inventoryId") Long inventoryId);

    List<ServerExcelResponse> selectServerExcel(@Param("projectId") Long projectId, @Param("serviceId") Long serviceId);

    List<ServerResponse> selectWindowsServer(@Param("projectId") Long projectId);

    List<MonitoringQueueItem> selectMonitoringServers(@Param("now") Date now);

    List<MonitoringQueueItem> selectProjectMonitoringServers(@Param("projectIds") List<Integer> projectIds,
                                                             @Param("now") Date now);

    List<ScheduledScanServer> getScheduledScanServers();

    ServerConnectionInfo selectServerConnectionInfoByInventoryId(@Param("serverInventoryId") Long serverInventoryId);

    List<String> selectServiceTypesForServer(@Param("serverInventoryId") Long serverInventoryId);

}
//end of ServerMapper.java
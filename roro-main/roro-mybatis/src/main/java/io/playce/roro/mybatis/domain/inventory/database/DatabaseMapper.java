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
 * Dong-Heon Han    Jan 06, 2022		First Draft.
 */

package io.playce.roro.mybatis.domain.inventory.database;

import io.playce.roro.common.dto.assessment.DatabaseDto;
import io.playce.roro.common.dto.inventory.database.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Repository
public interface DatabaseMapper {

    List<DatabaseEngineListResponseDto> selectDatabaseEngineList(@Param("projectId") Long projectId,
                                                                 @Param("serviceId") Long serviceId,
                                                                 @Param("serverId") Long serverId);

    List<DatabaseInstanceListResponseDto> selectDatabaseInstanceList(@Param("projectId") Long projectId,
                                                                     @Param("databaseInventoryId") Long databaseInventoryId);

    DatabaseEngineResponseDto selectDatabaseEngine(@Param("projectId") Long projectId,
                                                   @Param("databaseInventoryId") Long databaseInventoryId);

    DatabaseInstanceResponseDto selectDatabaseInstance(@Param("projectId") Long projectId,
                                                       @Param("databaseInventoryId") Long databaseInventoryId,
                                                       @Param("databaseInstanceId") Long databaseInstanceId);

    Map<String, String> selectDatabaseServerAndName(@Param("projectId") Long projectId,
                                                    @Param("databaseInventoryId") Long databaseInventoryId,
                                                    @Param("databaseInstanceId") Long databaseInstanceId);

    List<DatabaseInstanceMiddlewareResponseDto> selectDatabaseInstanceMiddlewares(@Param("serverIp") String serverIp,
                                                                                  @Param("databaseServiceName") String databaseServiceName);

    List<DatabaseInstanceApplicationResponseDto> selectDatabaseInstanceApplications(@Param("serverIp") String serverIp,
                                                                                    @Param("databaseServiceName") String databaseServiceName);

    int selectDatabaseCountByServiceNameAndJdbcUrlAndProjectId(@Param("databaseServiceName") String dbmsServiceName,
                                                               @Param("jdbcUrl") String jdbcUrl,
                                                               @Param("projectId") Long projectId,
                                                               @Param("inventoryId") Long inventoryId,
                                                               @Param("serverInventoryId") Long serverInventoryId);

    DatabaseDto selectDatabaseDtoInfo(@Param("inventoryProcessId") Long inventoryProcessId);

    Map<String, Object> selectDatabaseServerInfo(DatabaseDto databaseDto);

    Long selectDiscoveredInstanceId(Map<String, Object> databaseInstanceMap);

    int selectDuplicateDatabaseInventory(@Param("projectId") Long projectId, @Param("inventoryId") Long inventoryId, @Param("port") Integer port);

    List<DatabaseExcelResponse> selectDatabaseExcel(Long projectId, Long serviceId, Long serverId);

    void updateDiscoveredInstanceMaster(Map<String, Object> discoveredInstanceMasterMap);

    void updateDatabaseInstance(Map<String, Object> tempDatabaseInstanceMap);

    Long selectDiscoveredDatabaseInstance(@Param("projectId") Long projectId, @Param("databaseRequest") DatabaseRequest databaseRequest);

}
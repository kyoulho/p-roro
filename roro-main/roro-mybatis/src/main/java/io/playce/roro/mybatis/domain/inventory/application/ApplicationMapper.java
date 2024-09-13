/*
 * Copyright 2021 The playce-roro-v3 Project.
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
 * SangCheon Park   Nov 30, 2021		    First Draft.
 */
package io.playce.roro.mybatis.domain.inventory.application;

import io.playce.roro.common.dto.inventory.application.*;
import io.playce.roro.common.dto.inventory.middleware.InstanceResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Repository
public interface ApplicationMapper {

    List<ApplicationResponse> getApplications(@Param("projectId") Long projectId, @Param("serviceId") Long serviceId, @Param("serverId") Long serverId);

    ApplicationDetailResponse getApplication(@Param("projectId") Long projectId, @Param("applicationInventoryId") Long applicationInventoryId);

    List<Map<String, Object>> selectApplicationDatasource(@Param("projectId") Long projectId,
                                                          @Param("applicationInventoryId") Long applicationInventoryId);

    Map<String, Object> selectApplicationDatabaseInstance(@Param("projectId") Long projectId,
                                                          @Param("serverIp") String serverIp,
                                                          @Param("databaseServiceName") String databaseServiceName);

    List<ApplicationExcelResponse> selectApplicationExcel(@Param("projectId") Long projectId, @Param("serviceId") Long serviceId, @Param("serverId") Long serverId);

    List<InstanceResponse> selectApplicationMiddlewareInstance(@Param("projectId") Long projectId,
                                                               @Param("applicationId") Long applicationId);

    LastInventoryApplication selectInventoryApplication(@Param("projectId") Long projectId, @Param("serverInventoryId") Long serverInventoryId, @Param("deployPath") String deployPath);

    List<Long> selectDuplicatedApplication(@Param("projectId") Long projectId, @Param("serverInventoryId") Long serverInventoryId, @Param("deployPath") String deployPath);

    String selectMiddlewareInstanceProtocolHttpsYN(Long applicationId);

    Map<String, String> selectApplicationJavaInfo(@Param("projectId") Long projectId,
                                                  @Param("applicationId") Long applicationId);

    List<ApplicationExternalConnectionResponse> selectExternalConnections(Long projectId, Long applicationId);
}
//end of ApplicationMapper.java

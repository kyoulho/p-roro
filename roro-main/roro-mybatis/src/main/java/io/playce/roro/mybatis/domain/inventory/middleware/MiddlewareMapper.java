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
 * Jaeeon Bae       1월 26, 2022            First Draft.
 */
package io.playce.roro.mybatis.domain.inventory.middleware;

import io.playce.roro.common.dto.inventory.middleware.*;
import io.playce.roro.common.util.support.MiddlewareInventory;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Repository
public interface MiddlewareMapper {

    List<MiddlewareResponse> selectMiddlewareList(@Param("projectId") Long projectId, @Param("serviceId") Long serviceId,
                                                  @Param("serverId") Long serverId, @Param("inventoryTypeCode") String inventoryTypeCode);

    List<InstanceResponse> selectMiddlewareInstanceList(@Param("projectId") Long projectId,
                                                        @Param("middlewareInventoryId") Long middlewareInventoryId);

    MiddlewareInventory selectMiddlewareInventory(@Param("inventoryId") Long inventoryId);

    MiddlewareDetailResponse selectMiddlewareDetail(@Param("projectId") Long projectId,
                                                    @Param("middlewareInventoryId") Long middlewareInventoryId);

    InstanceDetailResponse selectMiddlewareInstanceDetail(@Param("projectId") Long projectId,
                                                          @Param("middlewareInventoryId") Long middlewareInventoryId,
                                                          @Param("middlewareInstanceId") Long middlewareInstanceId);

    @Deprecated
    List<MiddlewareInventory> selectDuplicateMiddlewareInventory(@Param("projectId") Long projectId,
                                                                 @Param("serverInventoryId") Long serverInventoryId,
                                                                 @Param("engineInstallationPath") String engineInstallationPath,
                                                                 @Param("domainHomePath") String domainHomePath);

    List<MiddlewareExcelResponse> selectMiddlewareExcel(@Param("projectId") Long projectId,
                                                        @Param("serviceId") Long serviceId,
                                                        @Param("serverId") Long serverId);

    List<DeployApplicationList> selectDeployApplicationList(@Param("projectId") Long projectId,
                                                            @Param("middlewareInstanceId") Long middlewareInstanceId);

    List<Map<String, Object>> selectDatasourceList(@Param("projectId") Long projectId,
                                                   @Param("middlewareInstanceId") Long middlewareInstanceId);


    /**
     * @param projectId
     * @param serverInventoryId
     * @param engineInstallPath
     * @return
     * @Deprecated Use io.playce.roro.jpa.repository.MiddlewareMasterRepository#selectDuplicateMiddlewareInventory() instead.
     */
    @Deprecated
    List<Long> selectDuplicatedMiddleware(@Param("projectId") Long projectId,
                                          @Param("serverInventoryId") Long serverInventoryId,
                                          @Param("engineInstallPath") String engineInstallPath);
}
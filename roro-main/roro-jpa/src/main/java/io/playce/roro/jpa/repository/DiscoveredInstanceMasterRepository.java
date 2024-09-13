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
 * Hoon Oh       1월 28, 2022            First Draft.
 */
package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.DiscoveredInstanceMaster;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */
@Repository
public interface DiscoveredInstanceMasterRepository extends PagingAndSortingRepository<DiscoveredInstanceMaster, Long>, JpaSpecificationExecutor<DiscoveredInstanceMaster> {

    Optional<DiscoveredInstanceMaster> findByPossessionInventoryId(Long inventoryId);

    DiscoveredInstanceMaster findByProjectIdAndDiscoveredInstanceId(Long projectId, Long discoveredInstanceId);

    @Query(value = "SELECT * FROM discovered_instance_master WHERE project_id = :projectId AND discovered_instance_id = :discoveredInstanceId", nativeQuery = true)
    DiscoveredInstanceMaster selectDiscoveredInstanceMaster(@Param(value = "projectId") Long projectId,
                                                            @Param(value = "discoveredInstanceId") Long discoveredInstanceId);

    List<DiscoveredInstanceMaster> findByDiscoveredIpAddressAndInventoryTypeCodeAndProjectId(String ipAddress, String inventoryTypeCode, Long projectId);

//    List<DiscoveredInstanceMaster> findByDiscoveredIpAddressAndDiscoveredServicePort(String ipAddress, Integer port);

    List<DiscoveredInstanceMaster> findByFinderInventoryId(Long inventoryId);


    Optional<DiscoveredInstanceMaster> findByProjectIdAndDiscoveredIpAddressAndDiscoveredDetailDivision(Long projectId, String ipAddress, String middlewareInstanceDetailDivision);

    List<DiscoveredInstanceMaster> findAllByPossessionInventoryId(Long inventoryId);
}

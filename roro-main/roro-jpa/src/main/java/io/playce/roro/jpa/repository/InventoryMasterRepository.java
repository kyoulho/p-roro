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
 * Author			Date				Description
 * ---------------	----------------	------------
 * SangCheon Park   Oct 29, 2021		First Draft.
 */
package io.playce.roro.jpa.repository;

import io.playce.roro.jpa.entity.InventoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryMasterRepository extends JpaRepository<InventoryMaster, Long>, JpaSpecificationExecutor<InventoryMaster> {

    List<InventoryMaster> findByProjectIdAndInventoryTypeCode(Long projectId, String typeCode);

    Optional<InventoryMaster> findByProjectIdAndInventoryId(Long projectId, Long inventoryId);

    Optional<InventoryMaster> findByInventoryIdAndInventoryTypeCode(Long inventoryId, String inventoryTypeCode);

    List<InventoryMaster> findByProjectIdAndCustomerInventoryCode(Long projectId, String customerInventoryCode);

    InventoryMaster findByCustomerInventoryCodeAndProjectId(String customerInventoryCode, Long projectId);

    int countByCustomerInventoryCodeAndProjectId(String customerServerCode, Long projectId);

    InventoryMaster findByCustomerInventoryCodeAndServerInventoryId(String customerInventoryCode, Long inventoryId);

    int countByServerInventoryIdAndInventoryTypeCode(Long inventoryId, String type);

    @Query(value = "SELECT inventory_name FROM inventory_master WHERE server_inventory_id = :serverInventoryId AND project_id = :projectId AND delete_yn = 'N'", nativeQuery = true)
    List<String> getMiddlewarNames(@Param("serverInventoryId") Long serverInventoryId, @Param("projectId") Long projectId);

    InventoryMaster findByCustomerInventoryCodeAndProjectIdAndInventoryTypeCodeAndInventoryIdNot(String customerInventoryCode, Long projectId, String inventoryTypeCode, Long inventoryId);

    Optional<InventoryMaster> findByProjectIdAndServerInventoryIdAndInventoryName(Long projectId, Long serverInventoryId, String application);

    List<InventoryMaster> findByServerInventoryIdAndInventoryTypeCode(Long serverInventoryId, String inventoryTypeCode);

    InventoryMaster findByProjectIdAndInventoryIdAndInventoryTypeCode(Long projectId, Long inventoryId, String inventoryTypeCode);

    InventoryMaster findByProjectIdAndCustomerInventoryCodeAndInventoryIdNot(Long projectId, String customerInventoryCode, Long inventoryId);
}
//end of InventoryMasterRepository.java
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

import io.playce.roro.common.dto.inventory.application.LastInventoryApplication;
import io.playce.roro.jpa.entity.ApplicationMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationMasterRepository extends JpaRepository<ApplicationMaster, Long>, JpaSpecificationExecutor<ApplicationMaster> {
//    @Query(value = " select im.inventory_id" +
//            "          from application_master am" +
//            "          join inventory_master im" +
//            "            on im.inventory_id = am.application_inventory_id" +
//            "         where (im.delete_yn = 'N' or (im.delete_yn = 'Y' and am.automatic_regist_protection_yn = 'Y'))" +
//            "           and im.server_inventory_id = :serverInventoryId" +
//            "           and am.deploy_path = :deployPath", nativeQuery = true)
    @Query(name = "LastInventoryApplicationQuery", nativeQuery = true)
    LastInventoryApplication selectInventoryApplication(@Param("serverInventoryId") Long serverInventoryId, @Param("deployPath") String deployPath);
}
//end of ApplicationMasterRepository.java
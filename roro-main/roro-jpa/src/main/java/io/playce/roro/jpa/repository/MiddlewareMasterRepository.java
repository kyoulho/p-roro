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

import io.playce.roro.jpa.entity.MiddlewareMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MiddlewareMasterRepository extends JpaRepository<MiddlewareMaster, Long>, JpaSpecificationExecutor<MiddlewareMaster> {

    @Query(value = "select im.inventory_id from inventory_master im join middleware_master mm on mm.middleware_inventory_id = im.inventory_id" +
            " where im.server_inventory_id = :serverInventoryId" +
            "   and (im.delete_yn = 'N' or (im.delete_yn = 'Y' and mm.automatic_regist_protection_yn = 'Y'))" +
            "   and (mm.engine_installation_path = :engineInstallationPath or mm.engine_installation_path IS null)" +
            "   and (mm.domain_home_path = :domainHomePath or mm.domain_home_path IS null)", nativeQuery = true)
    List<Long> selectDuplicateMiddlewareInventory(@Param("serverInventoryId") Long serverInventoryId,
                                                  @Param("engineInstallationPath") String engineInstallationPath, @Param("domainHomePath") String domainHomePath);

    @Query(value = "select im.inventory_id from inventory_master im join middleware_master mm on mm.middleware_inventory_id = im.inventory_id" +
            " where im.server_inventory_id = :serverInventoryId" +
            "   and (im.delete_yn = 'N' or (im.delete_yn = 'Y' and mm.automatic_regist_protection_yn = 'Y'))" +
            "   and im.inventory_detail_type_code = :middlewareDetailType" +
            "   and (mm.engine_installation_path = :engineInstallationPath or mm.engine_installation_path IS null)" +
            "   and (mm.domain_home_path = :domainHomePath or mm.domain_home_path IS null)", nativeQuery = true)
    List<Long> selectDuplicateMiddlewareInventory(@Param("serverInventoryId") Long serverInventoryId, @Param("middlewareDetailType") String middlewareDetailType,
                                                  @Param("engineInstallationPath") String engineInstallationPath, @Param("domainHomePath") String domainHomePath);
}
//end of MiddlewareMasterRepository.java
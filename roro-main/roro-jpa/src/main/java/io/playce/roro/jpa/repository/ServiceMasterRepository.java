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

import io.playce.roro.jpa.entity.ServiceMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceMasterRepository extends JpaRepository<ServiceMaster, Long>, JpaSpecificationExecutor<ServiceMaster> {

    ServiceMaster findByProjectIdAndServiceNameAndDeleteYn(Long projectId, String serviceName, String deleteYn);

    ServiceMaster findByProjectIdAndServiceIdAndServiceNameNotAndDeleteYn(Long projectId, Long serviceId, String serviceName, String deleteYn);

    ServiceMaster findByProjectIdAndServiceId(Long projectId, Long serviceId);

    // @Query(value = "SELECT * FROM service_master WHERE proejct_id = :projectId AND delete_yn = 'N'", nativeQuery = true)
    List<ServiceMaster> findAllByProjectIdAndDeleteYn(Long projectId, String deleteYn);

    List<ServiceMaster> findByProjectIdAndCustomerServiceCode(Long projectId, String customerServiceCode);

    ServiceMaster findByCustomerServiceCodeAndProjectId(String customerServiceCode, Long projectId);

    ServiceMaster findByProjectIdAndCustomerServiceCodeAndDeleteYn(long projectId, String customerServiceCode, String deleteYn);

    ServiceMaster findByProjectIdAndServiceIdNotAndCustomerServiceCodeAndDeleteYn(Long projectId, Long serviceId, String customerServiceCode, String deleteYn);

    ServiceMaster findByProjectIdAndServiceIdAndDeleteYn(Long projectId, Long serviceId, String deleteYn);

    ServiceMaster findByProjectIdAndServiceIdAndServiceNameAndDeleteYn(Long projectId, Long serviceId, String name, String deleteYn);

    List<ServiceMaster> findByProjectId(Long projectId);
}
//end of ServiceMasterRepository.java
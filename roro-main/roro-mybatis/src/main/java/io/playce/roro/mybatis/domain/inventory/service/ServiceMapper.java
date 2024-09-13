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
package io.playce.roro.mybatis.domain.inventory.service;

import io.playce.roro.common.dto.inventory.manager.Manager;
import io.playce.roro.common.dto.inventory.service.Service;
import io.playce.roro.common.dto.inventory.service.ServiceDetail;
import io.playce.roro.common.dto.inventory.service.ServiceResponse;
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
public interface ServiceMapper {

    List<ServiceResponse> selectServiceByInventoryId(Long inventoryId);

    ServiceResponse selectServiceByServiceId(Long serviceId);

    List<Service> getServiceSummaries(@Param("inventoryId") Long inventoryId);

    List<ServiceDetail> selectServiceList(long projectId);

    ServiceDetail selectService(long projectId, long serviceId);

    List<Manager> selectServiceManagerList(long serviceId);

    List<Map<String, Object>> getDatasources(long projectId, long serviceId);

}
//end of ServiceMapper.java

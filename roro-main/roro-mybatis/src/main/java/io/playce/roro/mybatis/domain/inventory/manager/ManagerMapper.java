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
package io.playce.roro.mybatis.domain.inventory.manager;

import io.playce.roro.common.dto.inventory.manager.Manager;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import io.playce.roro.common.dto.inventory.manager.InventoryManager;
import io.playce.roro.common.dto.inventory.manager.Manager;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Repository
public interface ManagerMapper {

    /**
     * <pre>
     * 서비스에 설정된 Manager 목록을 조회한다.
     * </pre>
     *
     * @param serviceId
     *
     * @return
     */
    List<Manager> getServiceManagers(@Param("serviceId") Long serviceId);

    /**
     * <pre>
     * 인벤토리에 설정된 Manager 목록을 조회한다.
     * </pre>
     *
     * @param inventoryId
     *
     * @return
     */
    List<Manager> getInventoryManagers(@Param("inventoryId") Long inventoryId);

    List<InventoryManager> selectInventoryManager(Long inventoryId);

    InventoryManager selectInventoryManagerByUserId(Long userId);
}
//end of ManagerMapper.java

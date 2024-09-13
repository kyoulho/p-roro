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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       11월 22, 2021            First Draft.
 */
package io.playce.roro.mybatis.domain.inventory.inventory;

import io.playce.roro.common.dto.inventory.inventory.InventoryCountResponse;
import io.playce.roro.common.dto.inventory.inventory.InventoryResourceCount;
import io.playce.roro.common.dto.inventory.inventory.InventoryResponse;
import io.playce.roro.common.dto.inventory.inventory.UploadInventoryResponse;
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
public interface InventoryMapper {

    List<InventoryResponse> selectInventoryList(long projectId);

    InventoryCountResponse selectInventoryDatabaseCount(Map<String, Object> map);

    InventoryCountResponse selectInventoryCount(Map<String, Object> map);

    UploadInventoryResponse selectInventory(Map<String, Long> map);

    InventoryResourceCount selectInventoryResourceCountByServiceId(Map<String, Object> map);

    Long selectInventoryResourceCountByDiscDefaultName(Map<String, Object> map);


}
//end of InventoryMapper.java
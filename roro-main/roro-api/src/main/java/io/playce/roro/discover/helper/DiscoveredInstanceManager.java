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
 * Hoon Oh       2월 08, 2022            First Draft.
 */
package io.playce.roro.discover.helper;

import io.playce.roro.jpa.entity.DiscoveredInstanceMaster;
import io.playce.roro.jpa.repository.DiscoveredInstanceMasterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <pre>
 *
 *
 * </pre>
 *
 * @author Hoon Oh
 * @version 1.0
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscoveredInstanceManager {

    private final DiscoveredInstanceMasterRepository discoveredInstanceMasterRepository;

    public synchronized DiscoveredInstanceMaster saveUnknownServer(DiscoveredInstanceMaster instanceMaster) {
        Long discoveredInstanceId = instanceMaster.getDiscoveredInstanceId();
        if (discoveredInstanceId == null) {// 신규일때만 logging
            log.debug("Add new discoveredInstanceMaster : {} ", instanceMaster.toString());
        }
        return discoveredInstanceMasterRepository.save(instanceMaster);
    }

    public List<DiscoveredInstanceMaster> getDiscoveredInstanceByIpAddressAndInventoryTypeCodeAndProjectId(String ipAddress, String inventoryTypeCode, Long projectId) {
        return discoveredInstanceMasterRepository.findByDiscoveredIpAddressAndInventoryTypeCodeAndProjectId(ipAddress, inventoryTypeCode, projectId);
    }

}
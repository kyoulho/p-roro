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
 * Jaeeon Bae       1월 20, 2022            First Draft.
 */
package io.playce.roro.api.domain.inventory.service.helper;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.playce.roro.common.dto.inventory.server.ServerDetailResponse;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.jpa.entity.InventoryProcess;
import io.playce.roro.jpa.entity.InventoryProcessResult;
import io.playce.roro.jpa.entity.ServerMaster;
import io.playce.roro.jpa.repository.InventoryProcessRepository;
import io.playce.roro.jpa.repository.InventoryProcessResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 2.0.0
 */
@Component
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SummaryHelper {

    private final ModelMapper modelMapper;
    private final InventoryProcessRepository inventoryProcessRepository;
    private final InventoryProcessResultRepository inventoryProcessResultRepository;

    public void serverSummary(Long inventoryProcessId, ServerDetailResponse serverResponse) {
        InventoryProcess inventoryProcess = getInventoryProcess(inventoryProcessId);
        ServerMaster server = modelMapper.map(serverResponse, ServerMaster.class);
        InventoryProcessResult result = getInventoryProcessResult(inventoryProcessId);
        if(result == null) return;

        ReadContext ctx = JsonPath.parse(result.getInventoryProcessResultJson());
        List<String> ips;
        List<String> ports;

        if("Y".equals(server.getWindowsYn())) {
            // TODO Windows Summary 추가 필요
        } else {
            // TODO Discover_Port_Relation 테이블 확인 후 추가 작업 필요
        }

    }

    private InventoryProcess getInventoryProcess(Long inventoryProcessId) {
        return inventoryProcessRepository.findById(inventoryProcessId).orElseThrow(() -> {
            throw new RoRoException("not exists inventory process id : " + inventoryProcessId);
        });
    }

    private InventoryProcessResult getInventoryProcessResult(Long inventoryProcessId) {
        InventoryProcessResult result = inventoryProcessResultRepository.findByInventoryProcessId(inventoryProcessId);
        if(result == null) {
            log.debug("not exists inventory process result id: " + inventoryProcessId);
        }
        return result;
    }
}
//end of SummaryComponent.java

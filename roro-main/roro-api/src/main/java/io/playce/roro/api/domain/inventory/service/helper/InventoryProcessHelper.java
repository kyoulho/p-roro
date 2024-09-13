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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.dto.inventory.server.ServerDetailResponse;
import io.playce.roro.discover.helper.ServerSummaryHelper;
import io.playce.roro.discover.helper.UnknownDatabaseDiscoverHelper;
import io.playce.roro.discover.helper.UnknownServerDiscoverHelper;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.entity.InventoryProcess;
import io.playce.roro.jpa.entity.InventoryProcessResult;
import io.playce.roro.jpa.repository.InventoryProcessResultRepository;
import io.playce.roro.jpa.repository.ServerSummaryRepository;
import io.playce.roro.scheduler.service.InventoryProcessManager;
import io.playce.roro.svr.asmt.dto.ServerAssessmentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryProcessHelper {

    private final InventoryProcessService inventoryProcessService;

    private final ServerSummaryHelper serverSummaryHelper;
    private final UnknownServerDiscoverHelper unkSvrHelper;
    private final UnknownDatabaseDiscoverHelper unkDBHelper;
    private final ManualProcess manualProcess;

    private final InventoryProcessManager inventoryProcessManager;


    public void runPostProcessing(InventoryProcessConnectionInfo connectionInfo, ServerAssessmentResult result) {
        try {
            serverSummaryHelper.deleteAllServerInformation(connectionInfo);

            serverSummaryHelper.addServerSummaryInfo(connectionInfo, result);
            serverSummaryHelper.addServerNetworkInfo(connectionInfo, result);
            serverSummaryHelper.addServerDiskInfo(connectionInfo, result);
            serverSummaryHelper.addServerDaemonInfo(connectionInfo, result);

            extractUnknownResources(connectionInfo, result);
        } catch (Exception e) {
            log.error("{}", e.getMessage(), e);
            throw new RuntimeException(connectionInfo.toString() + " unhandled error occurred during post processing with " + e.getMessage());
        }
    }

    public void extractUnknownResources(InventoryProcessConnectionInfo connectionInfo, ServerAssessmentResult result) {
        log.debug("[InventoryProcess-{}] inventoryProcess start extract unknown server", connectionInfo.getInventoryProcessId());
        unkSvrHelper.extract(connectionInfo, result);
        log.debug("[InventoryProcess-{}] inventoryProcess end extract unknown server", connectionInfo.getInventoryProcessId());

        log.debug("[InventoryProcess-{}] inventoryProcess start extract unknown database", connectionInfo.getInventoryProcessId());
        unkDBHelper.extract(connectionInfo, result);
        log.debug("[InventoryProcess-{}] inventoryProcess end extract unknown database", connectionInfo.getInventoryProcessId());
    }

    public void saveResult(Long projectId, InventoryProcess inventoryProcess, ServerDetailResponse server, Object resultObject, Object entity) throws IOException {
        InventoryProcessResult inventoryProcessResult = new InventoryProcessResult();

        try {
            inventoryProcessResult = manualProcess.saveReport(inventoryProcess.getInventoryProcessId(), resultObject);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while create a scan report.", e);
        }

        InventoryProcessQueueItem item = new InventoryProcessQueueItem();
        item.setProjectId(projectId);
        item.setInventoryId(server.getServerInventoryId());
        item.setInventoryName(server.getServerInventoryName());
        item.setInventoryProcessId(inventoryProcess.getInventoryProcessId());
        item.setInventoryProcessTypeCode(inventoryProcess.getInventoryProcessTypeCode());
        item.setInventoryTypeCode(server.getInventoryTypeCode());
        item.setInventoryDetailTypeCode(server.getInventoryDetailTypeCode());
        item.setWindowsYn(server.getWindowsYn());

        inventoryProcessService.addInventoryProcessResult(item, inventoryProcess.getInventoryProcessId(), resultObject, "", inventoryProcessResult);

        inventoryProcessManager.step5(item, Domain1003.CMPL);
    }

}
//end of AssessmentHelper.java

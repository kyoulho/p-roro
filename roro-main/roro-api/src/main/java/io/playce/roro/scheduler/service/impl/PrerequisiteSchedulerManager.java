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
 * Dong-Heon Han    Dec 11, 2021		First Draft.
 */

package io.playce.roro.scheduler.service.impl;

import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.api.domain.inventory.service.InventoryService;
import io.playce.roro.common.cancel.InventoryProcessCancelInfo;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.dto.common.InventoryProcessConnectionInfo;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.dto.prerequisite.CheckStatus;
import io.playce.roro.common.dto.prerequisite.ServerResult;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import io.playce.roro.prerequisite.PrerequisiteComponent;
import io.playce.roro.prerequisite.config.PrerequisiteConfig;
import io.playce.roro.prerequisite.server.ServerInfo;
import io.playce.roro.scheduler.service.InventoryProcessManager;
import io.playce.roro.scheduler.service.Manager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Dong-Heon Han
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrerequisiteSchedulerManager implements Manager {
    private final BlockingQueue<InventoryProcessQueueItem> prerequisiteQueue;
    private final InventoryProcessManager inventoryProcessManager;
    private final PrerequisiteConfig prerequisiteConfig;
    private final PrerequisiteComponent prerequisiteComponent;

    private final ServerMapper serverMapper;

    private final InventoryProcessService inventoryProcessService;
    private final InventoryService inventoryService;

    @Async
    public Future<Void> run() throws InterruptedException {
        InventoryProcessQueueItem item = null;
        try {
            item = prerequisiteQueue.take();
            log.debug("Step 1 ~ dequeue, id: {}, queue size: {}", item.getInventoryProcessId(), prerequisiteQueue.size());
            if (inventoryProcessManager.step2(item))
                return null;

            inventoryProcessManager.step3(item);
            Domain1003 domain1003 = step4(item);
            inventoryProcessManager.step5(item, domain1003);
        } catch (InterruptedException e) {
            log.error("Prerequisite Thread interrupted.. {}", item);
            throw e;
        }
        return null;
    }

    @Transactional
    public Domain1003 step4(InventoryProcessQueueItem item) throws InterruptedException {
        Domain1003 resultState = Domain1003.FAIL;
        ServerResult.PrerequisiteJson prerequisiteJson = new ServerResult.PrerequisiteJson();
        String resultString = null;
        InventoryProcessConnectionInfo connectionInfo = null;
        String assessmentEanbled = "N";

        Long inventoryProcessId = item.getInventoryProcessId();
        connectionInfo = serverMapper.selectServerConnectionInfoByInventoryProcessId(item.getInventoryProcessId());
        boolean isWindows = connectionInfo.getWindowsYn().equals("Y");

        try {
            log.debug("Step 4 ~ id: {}. load connection info: {}", inventoryProcessId, connectionInfo);
            ServerInfo serverInfo = ServerInfo.builder()
                    .inventoryProcessConnectionInfo(connectionInfo)
                    .host(InventoryProcessConnectionInfo.targetHost(connectionInfo))
                    .config(prerequisiteConfig)
                    .window(connectionInfo.getWindowsYn().equals(Domain101.Y.name()))
                    .build();
            ServerResult serverResult = new ServerResult(connectionInfo.getUserName());

            if (InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
                InventoryProcessCancelInfo.removeCancelRequest(item.getInventoryProcessId());
                return Domain1003.CNCL;
            }

            prerequisiteComponent.executeCheckServer(serverInfo, serverResult);

            prerequisiteJson = ServerResult.prerequisiteJson(serverResult, new Date(), isWindows);
            log.debug("Step 4 ~ id: {}. run result: {}", inventoryProcessId, prerequisiteJson);
            assessmentEanbled = prerequisiteJson.getAssessmentEnabled();
            if (assessmentEanbled.equals("Y")) {
                resultState = Domain1003.CMPL;
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw (InterruptedException) e;
            }
            resultString = e.getMessage();
            prerequisiteJson.setUserName(WebUtil.getUsername());
            prerequisiteJson.setStatusMessage(List.of(resultString));
            prerequisiteJson.setCheckStatus(List.of(CheckStatus.builder()
                    .icon(CheckStatus.Icon.FAIL)
                    .status(CheckStatus.Result.Failed)
                    .message(ServerResult.getStaticMessage(1, isWindows))
                    .build()));
            prerequisiteJson.setAssessmentEnabled("N");
            prerequisiteJson.setCheckedDate(System.currentTimeMillis());
            log.error("Step 4 ~ error: {}", e.getMessage());
        }

        // error message를 만들어준다.
        if (resultString == null && CollectionUtils.isNotEmpty(prerequisiteJson.getStatusMessage())) {
            resultString = String.join(", ", prerequisiteJson.getStatusMessage());
        }

        inventoryProcessService.addInventoryProcessResult(item, inventoryProcessId, prerequisiteJson, resultString, null);
        if (connectionInfo != null) {
            inventoryService.setInventoryAnalysisYn(connectionInfo.getInventoryId(), assessmentEanbled);
        }

        if (InventoryProcessCancelInfo.hasCancelRequest(item.getInventoryProcessId())) {
            InventoryProcessCancelInfo.removeCancelRequest(item.getInventoryProcessId());
            return Domain1003.CNCL;
        }

        return resultState;
    }
}
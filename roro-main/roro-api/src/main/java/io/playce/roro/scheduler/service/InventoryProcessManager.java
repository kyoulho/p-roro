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
 * Dong-Heon Han    Dec 09, 2021		First Draft.
 */

package io.playce.roro.scheduler.service;

import com.google.gson.Gson;
import io.playce.roro.api.domain.inventory.service.InventoryProcessService;
import io.playce.roro.api.websocket.manager.WebSocketManager;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.inventory.database.DatabaseEngineResponseDto;
import io.playce.roro.common.dto.inventory.middleware.MiddlewareDetailResponse;
import io.playce.roro.common.dto.inventory.process.InventoryProcessQueueItem;
import io.playce.roro.common.dto.websocket.AssessmentMessage;
import io.playce.roro.common.dto.websocket.Message;
import io.playce.roro.common.dto.websocket.MigrationMessage;
import io.playce.roro.common.dto.websocket.PreRequisiteMessage;
import io.playce.roro.jpa.entity.InventoryMigrationProcess;
import io.playce.roro.jpa.entity.InventoryProcess;
import io.playce.roro.jpa.entity.InventoryProcessResult;
import io.playce.roro.jpa.repository.InventoryMigrationProcessRepository;
import io.playce.roro.jpa.repository.InventoryProcessResultRepository;
import io.playce.roro.mybatis.domain.inventory.database.DatabaseMapper;
import io.playce.roro.mybatis.domain.inventory.middleware.MiddlewareMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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
@Transactional
@Slf4j
public class InventoryProcessManager {
    private final InventoryProcessService inventoryProcessService;
    private final InventoryProcessResultRepository inventoryProcessResultRepository;
    private final InventoryMigrationProcessRepository inventoryMigrationProcessRepository;
    private final MiddlewareMapper middlewareMapper;
    private final DatabaseMapper databaseMapper;
    private final WebSocketManager webSocketManager;
    private final Gson gson;

    private InventoryProcess getInventoryProcess(InventoryProcessQueueItem item) {
        Long inventoryProcessId = item.getInventoryProcessId();
        return inventoryProcessService.getInventoryProcessById(inventoryProcessId);
    }

    public boolean step2(InventoryProcessQueueItem item) {
        InventoryProcess inventoryProcess = getInventoryProcess(item);
        boolean result = inventoryProcess.getInventoryProcessResultCode().equals(Domain1003.CNCL.name());
        if (result) {
            sendNotification(item, inventoryProcess, Domain1003.valueOf(inventoryProcess.getInventoryProcessResultCode()));
        }
        log.debug("Step 2 ~ id: {}, change state: {}", inventoryProcess.getInventoryProcessId(), inventoryProcess.getInventoryProcessResultCode());
        return result;
    }

    public void step3(InventoryProcessQueueItem item) {
        InventoryProcess inventoryProcess = getInventoryProcess(item);

        Long inventoryProcessId = inventoryProcess.getInventoryProcessId();
        inventoryProcess.setInventoryProcessStartDatetime(new Date());
        inventoryProcessService.setInventoryProcessResultCode(new Date(), inventoryProcess, Domain1003.PROC);
        log.debug("Step 3 ~ id: {}, state: {}, start datetime: {}", inventoryProcessId,
                inventoryProcess.getInventoryProcessResultCode(), inventoryProcess.getInventoryProcessStartDatetime());

        sendNotification(item, inventoryProcess, Domain1003.PROC);
    }

    public void step5(InventoryProcessQueueItem item, Domain1003 domain1003) {
        InventoryProcess inventoryProcess = getInventoryProcess(item);

        Date now = new Date();
        Long inventoryProcessId = inventoryProcess.getInventoryProcessId();
        inventoryProcess.setInventoryProcessEndDatetime(now);
        inventoryProcessService.setInventoryProcessResultCode(now, inventoryProcess, domain1003);
        log.debug("Step 5 ~ id: {}, state: {}, end datetime: {}", inventoryProcessId,
                inventoryProcess.getInventoryProcessResultCode(), inventoryProcess.getInventoryProcessEndDatetime());

        try {
            sendNotification(item, inventoryProcess, Domain1003.valueOf(inventoryProcess.getInventoryProcessResultCode()));
        } catch (Exception e) {
            log.error("Unhandled exception occurred while send notification using websocket.", e);
        }
    }

    /**
     * WebSocket Inventory Process Message 전송
     */
    public void sendNotification(InventoryProcessQueueItem item, InventoryProcess inventoryProcess, Domain1003 status) {
        if (inventoryProcess == null)
            inventoryProcess = new InventoryProcess();

        // websocket message를 만들어 내려준다.
        Message message = generateWebSocketMessage(item, status);

        if (message != null) {
            message.setProjectId(item.getProjectId());
            message.setId(item.getInventoryProcessId());
            message.setMessage(StringUtils.defaultString(inventoryProcess.getInventoryProcessResultTxt()));
            message.setStatus(status);
            message.setStartDate(inventoryProcess.getInventoryProcessStartDatetime());
            message.setEndDate(inventoryProcess.getInventoryProcessEndDatetime());

            webSocketManager.sendNotification(message);
        }
    }

    /**
     * WebSocket Message 를 만들어서 내려준다.
     */
    private Message generateWebSocketMessage(InventoryProcessQueueItem item, Domain1003 status) {
        Message message = null;
        if (item.getInventoryProcessTypeCode().equals(Domain1002.SCAN.name())) {
            message = new AssessmentMessage();
            message.setType(Domain1002.SCAN);

            Domain1001 processType = Domain1001.valueOf(item.getInventoryTypeCode());
            if (processType.equals(Domain1001.SVR)) {
                AssessmentMessage.ServerMessage msg = new AssessmentMessage.ServerMessage();
                msg.setId(item.getInventoryId());
                msg.setName(item.getInventoryName());
                ((AssessmentMessage) message).setServer(msg);
            } else if (processType.equals(Domain1001.MW)) {
                AssessmentMessage.MiddlewareMessage msg = new AssessmentMessage.MiddlewareMessage();
                msg.setId(item.getInventoryId());
                msg.setName(item.getInventoryName());

                if (Domain1003.CMPL.equals(status)) {
                    MiddlewareDetailResponse middlewareDetailResponse = middlewareMapper.selectMiddlewareDetail(item.getProjectId(), item.getInventoryId());
                    msg.setVersion(middlewareDetailResponse.getEngineVersion());
                    msg.setInstances(middlewareDetailResponse.getInstanceCount());
                }

                ((AssessmentMessage) message).setMiddleware(msg);
            } else if (processType.equals(Domain1001.APP)) {
                AssessmentMessage.ApplicationMessage msg = new AssessmentMessage.ApplicationMessage();
                msg.setId(item.getInventoryId());
                msg.setName(item.getInventoryName());
                ((AssessmentMessage) message).setApplication(msg);
            } else if (processType.equals(Domain1001.DBMS)) {
                AssessmentMessage.DatabaseMessage msg = new AssessmentMessage.DatabaseMessage();
                msg.setId(item.getInventoryId());
                msg.setName(item.getInventoryName());
                
                if (Domain1003.CMPL.equals(status)) {
                    DatabaseEngineResponseDto databaseEngineResponseDto = databaseMapper.selectDatabaseEngine(item.getProjectId(), item.getInventoryId());
                    msg.setVersion(databaseEngineResponseDto.getEngineVersion());
                    msg.setInstances(Long.valueOf(databaseEngineResponseDto.getInstanceCount()).intValue());
                }

                ((AssessmentMessage) message).setDatabase(msg);
            }

        } else if (item.getInventoryProcessTypeCode().equals(Domain1002.PREQ.name())) {
            message = new PreRequisiteMessage();
            message.setType(Domain1002.PREQ);

            PreRequisiteMessage.ServerMessage msg = new PreRequisiteMessage.ServerMessage();
            msg.setId(item.getInventoryId());
            msg.setName(item.getInventoryName());
            ((PreRequisiteMessage) message).setServer(msg);

            InventoryProcessResult inventoryProcessResult = inventoryProcessResultRepository.findById(item.getInventoryProcessId()).orElse(null);

            if (inventoryProcessResult != null) {
                ((PreRequisiteMessage) message).setResult(gson.fromJson(inventoryProcessResult.getInventoryProcessResultJson(), PreRequisiteMessage.PrerequisiteJson.class));
            }
        } else if (item.getInventoryProcessTypeCode().equals(Domain1002.MIG.name())) {
            message = new MigrationMessage();
            message.setType(Domain1002.MIG);

            InventoryMigrationProcess inventoryMigrationProcess = inventoryMigrationProcessRepository.findById(item.getInventoryProcessId()).orElse(null);

            if (inventoryMigrationProcess != null) {
                ((MigrationMessage) message).setInstanceId(inventoryMigrationProcess.getInstanceId());

                if (StringUtils.isNotEmpty(inventoryMigrationProcess.getInstanceName())) {
                    ((MigrationMessage) message).setInstanceName(inventoryMigrationProcess.getInstanceName());
                } else {
                    ((MigrationMessage) message).setInstanceName(inventoryMigrationProcess.getHostName());
                }
            }

            MigrationMessage.ServerMessage msg = new MigrationMessage.ServerMessage();
            msg.setId(item.getInventoryId());
            msg.setName(item.getInventoryName());
            ((MigrationMessage) message).setServer(msg);
        }

        return message;
    }

}
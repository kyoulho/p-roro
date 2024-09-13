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
 * Jaeeon Bae       11월 24, 2021            First Draft.
 */
package io.playce.roro.api.domain.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.api.config.SplitJsonProperties;
import io.playce.roro.common.code.Domain1001;
import io.playce.roro.common.code.Domain1002;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.code.Domain101;
import io.playce.roro.common.dto.inventory.process.*;
import io.playce.roro.jpa.entity.InventoryProcess;
import io.playce.roro.jpa.entity.*;
import io.playce.roro.jpa.repository.*;
import io.playce.roro.mybatis.domain.inventory.process.InventoryProcessMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
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
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InventoryProcessService {
    private final InventoryProcessGroupRepository inventoryProcessGroupRepository;
    private final InventoryProcessRepository inventoryProcessRepository;
    private final InventoryProcessResultRepository inventoryProcessResultRepository;
    private final InventoryProcessJsonDetailRepository inventoryProcessJsonDetailRepository;
    private final InventoryMasterRepository inventoryMasterRepository;

    private final InventoryProcessMapper inventoryProcessMapper;
    private final ObjectMapper objectMapper;
    private final Gson gson;
    private final SplitJsonProperties splitJsonProperties;

    public synchronized void addInventoryProcess(Long projectId, InventoryProcessRequest request, Domain1002 domain1002) {
        List<Long> inventoryIds = request.getInventoryIds();

        List<InventoryMaster> inventoryMasters;
        if (inventoryIds.isEmpty()) {
            inventoryMasters = inventoryMasterRepository.findByProjectIdAndInventoryTypeCode(projectId, Domain1001.SVR.name());
        } else {
            inventoryMasters = inventoryMasterRepository.findAllById(inventoryIds);
        }
        if (inventoryMasters.isEmpty()) {
            log.debug("Inventory information not found. inventory ids: {}", inventoryIds);
            return;
        }

        //inventory group 생성
        InventoryProcessGroup ipg = addInventoryGroup();
        for (InventoryMaster inventoryMaster : inventoryMasters) {
            addInventoryProcess(ipg.getInventoryProcessGroupId(), inventoryMaster.getInventoryId(), domain1002, Domain1003.REQ);
        }
    }

    public void changeState(Domain1003 from, Domain1003 to) {
        List<InventoryProcess> inventoryProcesses = inventoryProcessRepository.findAllByInventoryProcessResultCode(from.name());
        Date update = new Date();
        inventoryProcesses.forEach(i -> {
            String processName;

            if (i.getInventoryProcessTypeCode().equals(Domain1002.SCAN.name())) {
                processName = "Assessment";
            } else if (i.getInventoryProcessTypeCode().equals(Domain1002.PREQ.name())) {
                processName = "Prerequisite";
            } else if (i.getInventoryProcessTypeCode().equals(Domain1002.MIG.name())) {
                processName = "Migration";
            } else {
                processName = "Unknown";
            }

            i.setInventoryProcessResultTxt(processName + " process does not exist. Please check the RoRo server restarted or not.");
            setInventoryProcessResultCode(update, i, to);
        });
        log.info("start roro .. change inventory_process_result_code {} -> {}, count: {}", from, to, inventoryProcesses.size());
    }

    public void changeState(Domain1003 from, Domain1003 to, Domain1002 domain1002) {
        List<InventoryProcess> inventoryProcesses = inventoryProcessRepository
                .findAllByInventoryProcessResultCodeAndInventoryProcessTypeCode(from.name(), domain1002.name());
        Date update = new Date();
        inventoryProcesses.forEach(i -> setInventoryProcessResultCode(update, i, to));
        log.info("start roro .. change inventory_process_result_code {} -> {}, count: {}", from, to, inventoryProcesses.size());
    }

    public InventoryProcess getInventoryProcessById(Long inventoryProcessId) {
        return inventoryProcessRepository.getById(inventoryProcessId);
    }

    public List<InventoryProcessQueueItem> getInventoryProcessByCode(Domain1003 domain1003, Domain1002 domain1002) {
        return inventoryProcessMapper.selectInventoryProcessQueueItems(domain1003.name(), domain1002.name());
    }

    public void setInventoryProcessResultCode(Date update, InventoryProcess inventoryProcess, Domain1003 domain1003) {
        inventoryProcess.setInventoryProcessResultCode(domain1003.name());
        Long userId = WebUtil.getUserId();
        inventoryProcess.setModifyUserId(userId);
        inventoryProcess.setModifyDatetime(update);
    }

    public void addInventoryProcessResult(InventoryProcessQueueItem item, Long inventoryProcessId, Object result, String errorMessage, InventoryProcessResult inventoryProcessResult) {
        InventoryProcess inventoryProcess = getInventoryProcessById(inventoryProcessId);
        inventoryProcess.setInventoryProcessResultTxt(errorMessage);
        if (result == null)
            return;

        List<String> keys = splitJsonProperties.getKeys(item.getInventoryTypeCode() + "-" + item.getInventoryDetailTypeCode());
        String jsonString = gson.toJson(result);

        if (inventoryProcessResult == null) {
            inventoryProcessResult = new InventoryProcessResult();
        }
        inventoryProcessResult.setInventoryProcessId(inventoryProcessId);
        // Windows의 경우 io.playce.roro.scheduler.component.AbstractAssessmentProcess.saveReport() 에서 별도의 처리가 되고 있어서 overwrite 하면 안됨.
        if (StringUtils.isEmpty(inventoryProcessResult.getInventoryProcessResultJson())) {
            inventoryProcessResult.setInventoryProcessResultJson(jsonString);
        }
        inventoryProcessResult = inventoryProcessResultRepository.save(inventoryProcessResult);

        List<String> usedKeys = new ArrayList<>();
        if (jsonString.length() > splitJsonProperties.getSplitSize() && !keys.isEmpty()) {
            JsonNode node = objectMapper.valueToTree(result);
            try {
                JsonNode returnNode = saveSplitDataAndRemoveNode(inventoryProcessId, keys, node, usedKeys);
                inventoryProcessResult.setInventoryProcessResultJson(objectMapper.writeValueAsString(returnNode));
            } catch (JsonProcessingException e) {
                log.error("{}", e.getMessage(), e);
            }
        }
        inventoryProcessResult.setInventoryProcessResultMetaList(gson.toJson(usedKeys));
    }

    private JsonNode saveSplitDataAndRemoveNode(Long inventoryProcessId, List<String> keys, JsonNode node, List<String> usedKey) throws JsonProcessingException {
        if (keys == null)
            return null;

        for (String key : keys) {
            JsonNode n = node.at(key);
            if (n.isNull())
                continue;
            if (n.isMissingNode())
                continue;

            changeValueToKey(node, key);
            usedKey.add(key);

            InventoryProcessJsonDetail detail = getIventoryProcessJsonDetail(inventoryProcessId, key, objectMapper.writeValueAsString(n));
            inventoryProcessJsonDetailRepository.save(detail);
        }
        return node;
    }

    private void changeValueToKey(JsonNode node, String key) {
        int parentIndex = key.lastIndexOf("/");
        JsonNode parent = node.at(key.substring(0, parentIndex));
        String nodeName = key.substring(parentIndex + 1);
        if (parent instanceof ArrayNode) {
            ArrayNode p = (ArrayNode) parent;
            if (nodeName.equals("*")) {
                p.removeAll();
            } else {
                int index = Integer.parseInt(nodeName);
                p.remove(index);
            }
        } else if (parent instanceof ObjectNode) {

            ((ObjectNode) parent).set(nodeName, objectMapper.nullNode());
        }
    }

    public InventoryProcess addInventoryProcess(Long inventoryProcessGroupId, Long inventoryId, Domain1002 invProcessType, Domain1003 invProcessResult) {
        InventoryProcess ip = new InventoryProcess();
        ip.setInventoryProcessGroupId(inventoryProcessGroupId);
        ip.setInventoryId(inventoryId);
        ip.setInventoryProcessTypeCode(invProcessType.name());
        ip.setInventoryProcessResultCode(invProcessResult.name());
        ip.setRegistUserId(WebUtil.getUserId());
        ip.setRegistDatetime(new Date());
        ip.setModifyUserId(WebUtil.getUserId());
        ip.setModifyDatetime(new Date());
        ip.setDeleteYn(Domain101.N.name());
        return inventoryProcessRepository.save(ip);
    }

    public InventoryProcessGroup addInventoryGroup() {
        InventoryProcessGroup ipg = new InventoryProcessGroup();
        ipg.setRegistUserId(WebUtil.getUserId());
        ipg.setRegistDatetime(new Date());
        return inventoryProcessGroupRepository.save(ipg);
    }

    public List<LatestInventoryProcess> getLatestInventoryProcessesByType(Long projectId, Domain1002 domain1002) {
        return inventoryProcessMapper.selectLatestInventoryProcessByInventoryProcessType(projectId, domain1002.name());
    }

    /**
     * 인벤토리의 가장 마지막 성공된 스캔 정보를 조회한다.
     */
    public InventoryProcessResponse getLatestCompleteScanProcess(String inventoryTypeCode, Long inventoryId) {
        return inventoryProcessMapper.selectLastCompletedScanByInventoryId(inventoryTypeCode, Domain1002.SCAN.name(), inventoryId);
    }

    public List<InventoryProcessHistory> getInventoryProcessByTypeAndDate(Long projectId, Domain1002 domain1002, String from, String to) {
        return inventoryProcessMapper.selectInventoryProcessByInventoryProcessTypeAndDate(projectId, domain1002.name(), from, to);
    }

    public void addResultJsonDetail(Long inventoryProcessId, Map<String, Object> map) {
        if (map == null)
            return;

        List<InventoryProcessJsonDetail> details = new ArrayList<>();
        for (String key : map.keySet()) {
            InventoryProcessJsonDetail detail = getIventoryProcessJsonDetail(inventoryProcessId, key, gson.toJson(map.get(key)));
            details.add(detail);
        }
        inventoryProcessJsonDetailRepository.saveAll(details);
    }

    private InventoryProcessJsonDetail getIventoryProcessJsonDetail(Long inventoryProcessId, String key, String jsonString) {
        InventoryProcessJsonDetail detail = new InventoryProcessJsonDetail();
        detail.setInventoryProcessId(inventoryProcessId);
        detail.setJsonKey(key);
        detail.setJsonContent(jsonString);
        return detail;
    }
}
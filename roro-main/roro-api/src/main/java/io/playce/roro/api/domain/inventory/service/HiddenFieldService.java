/*
 * Copyright 2023 The playce-roro Project.
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
 * Dong-Heon Han    Jun 02, 2023		First Draft.
 */

package io.playce.roro.api.domain.inventory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.api.common.util.WebUtil;
import io.playce.roro.common.util.JsonUtil;
import io.playce.roro.jpa.entity.HiddenField;
import io.playce.roro.jpa.entity.pk.HiddenFieldId;
import io.playce.roro.jpa.repository.HiddenFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HiddenFieldService {
    private final HiddenFieldRepository repository;
    private final ObjectMapper objectMapper;

    public JsonNode getHiddenFields(Long projectId, String inventoryTypeCode) {
        Long userId = WebUtil.getUserId();
        HiddenFieldId id = makeHiddenFieldId(projectId, inventoryTypeCode, userId);
        HiddenField hiddenField = repository.findById(id).orElse(null);

        if(hiddenField == null) return objectMapper.createArrayNode();
        return JsonUtil.readTree(hiddenField.getFieldNames());
    }

    private HiddenFieldId makeHiddenFieldId(Long projectId, String inventoryTypeCode, Long userId) {
        HiddenFieldId id = new HiddenFieldId();
        id.setProjectId(projectId);
        id.setUserId(userId);
        id.setInventoryTypeCode(inventoryTypeCode);
        return id;
    }

    public void saveHiddenFields(Long projectId, String inventoryTypeCode, JsonNode hiddenFields) {
        Long userId = WebUtil.getUserId();
        HiddenFieldId id = makeHiddenFieldId(projectId, inventoryTypeCode, userId);
        HiddenField field = makeHiddenField(id, hiddenFields);
        repository.save(field);
    }

    private HiddenField makeHiddenField(HiddenFieldId id, JsonNode hiddenFields) {
        HiddenField hiddenField = new HiddenField();
        hiddenField.setId(id);
        hiddenField.setFieldNames(JsonUtil.writeValueAsString(hiddenFields));
        return hiddenField;
    }
}